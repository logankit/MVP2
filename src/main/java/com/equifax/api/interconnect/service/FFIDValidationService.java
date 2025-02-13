package com.equifax.api.interconnect.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.equifax.api.interconnect.model.DecisionResponse;
import com.equifax.api.interconnect.model.OktaTokenResponse;
import com.equifax.api.interconnect.model.ReferenceFFIDRequest;
import com.equifax.api.interconnect.util.CommonLogger;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collectors;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import lombok.AllArgsConstructor;
import lombok.Data;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class FFIDValidationService {
    private static final Logger logger = LoggerFactory.getLogger(FFIDValidationService.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${ffid.validation.url}")
    private String ffidValidationUrl;

    @Autowired
    private OktaTokenService oktaTokenService;

    @Autowired
    private RestTemplate restTemplate;

    @PersistenceContext
    private EntityManager entityManager;

    private static final String FFID_QUERY = """
        SELECT 
            null new_billTo,
            c.contract_id, 
            cco.charge_offer_id,
            cbi.bu_id, 
            cust.efx_Id,
            decode(cor.pricing_period, 'On-Demand', 'M00', substr(cor.charge_offer_id, -2)) charge_offer_type,
            decode(cor.rate_tier, 'Grant', 'Y', 'N') grant_dsg,
            decode(cor.rate_tier, 'Commitment', 'Y', 'N') cmt_dsg,
            decode(cco.aggregator_link, NULL, 'N', 'Y') Has_Aggr_Linkage,
            cco.contract_price amount
        FROM 
            c2o_contract_entitlement ce,
            c2o_contract c,
            c2o_Contract_charge_offers cco,
            c2o_charge_offers_ref cor,
            c2o_contract_bu_intr cbi,
            c2o_account a,
            c2o_customer cust
        WHERE 
            ce.contract_Id = cco.contract_Id
            AND c.contract_id = :contractId
            AND c.account_id = a.row_Id
            AND cust.customer_id = a.customer_id
            AND c.contract_Id = cco.contract_Id
            AND c.contract_Id = cbi.contract_Id
            AND cor.charge_offer_id = cco.charge_offer_id
            AND cco.id = ce.line_charge_offer_id
            AND cco.purchase_end_date is null
            AND c.version_status in (32,34)
        ORDER BY 
            cco.charge_offer_id
    """;

    @Data
    @AllArgsConstructor
    private static class FFIDQueryResult {
        private String newBillTo;
        private Long contractId;
        private String chargeOfferId;
        private String buId;
        private String efxId;
        private String chargeOfferType;
        private String grantDsg;
        private String cmtDsg;
        private String hasAggrLinkage;
        private java.math.BigDecimal amount;
    }

    private List<FFIDQueryResult> executeFFIDQuery(Long contractId) {
        try {
            Query query = entityManager.createNativeQuery(FFID_QUERY)
                .setParameter("contractId", contractId);

            List<Object[]> results = query.getResultList();
            
            return results.stream()
                .map(row -> new FFIDQueryResult(
                    (String) row[0],           // newBillTo
                    ((Number) row[1]).longValue(), // contractId
                    (String) row[2],           // chargeOfferId
                    (String) row[3],           // buId
                    (String) row[4],           // efxId
                    (String) row[5],           // chargeOfferType
                    String.valueOf(row[6]),    // grantDsg
                    String.valueOf(row[7]),    // cmtDsg
                    String.valueOf(row[8]),    // hasAggrLinkage
                    (java.math.BigDecimal) row[9]        // amount
                ))
                .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error executing FFID query for contract {}: {}", contractId, e.getMessage());
            throw new RuntimeException("Error executing FFID query", e);
        }
    }

    private String getBuIdForContract(Long contractId) {
        String sql = "SELECT BU_ID FROM C2O_CONTRACT_BU_INTR WHERE CONTRACT_ID = :contractId";
        try {
            return (String) entityManager.createNativeQuery(sql)
                                      .setParameter("contractId", contractId)
                                      .getSingleResult();
        } catch (NoResultException e) {
            throw new RuntimeException("No BU found for contract: " + contractId);
        }
    }

    private ReferenceFFIDRequest buildFFIDPayload(List<FFIDQueryResult> results) {
        if (results.isEmpty()) {
            throw new RuntimeException("No data found for the given contract");
        }

        ReferenceFFIDRequest request = new ReferenceFFIDRequest();
        ReferenceFFIDRequest.ReferenceFFID referenceFFID = new ReferenceFFIDRequest.ReferenceFFID();

        // Set basic fields
        FFIDQueryResult firstResult = results.get(0);
        referenceFFID.setNewBillTo(false); // Default to false as per requirement
        referenceFFID.setC2oGTMEfxID(firstResult.getEfxId());
        referenceFFID.setSfdGTMEfxID(firstResult.getEfxId());

        // Set charge offers
        referenceFFID.setActiveChargeOffers(
            results.stream()
                .map(FFIDQueryResult::getChargeOfferId)
                .collect(Collectors.toList())
        );

        // Set unique charge offer types
        referenceFFID.setActiveChargeOffersTypes(
            results.stream()
                .map(FFIDQueryResult::getChargeOfferType)
                .distinct()
                .collect(Collectors.toList())
        );

        // Set DSGs
        ReferenceFFIDRequest.DSG dsg = new ReferenceFFIDRequest.DSG();
        ReferenceFFIDRequest.ComitmentDSGs comitmentDSGs = new ReferenceFFIDRequest.ComitmentDSGs();
        comitmentDSGs.setIsOwner("Y".equals(firstResult.getCmtDsg()));
        comitmentDSGs.setHasAggrLinkage("Y".equals(firstResult.getHasAggrLinkage()));

        ReferenceFFIDRequest.GrantDSGs grantDSGs = new ReferenceFFIDRequest.GrantDSGs();
        grantDSGs.setIsOnwer("Y".equals(firstResult.getGrantDsg()));

        dsg.setComitmentDSGs(comitmentDSGs);
        dsg.setGrantDSGs(grantDSGs);
        dsg.setHasAGGRs(false); // Hardcoded as per requirement

        referenceFFID.setDSGs(Collections.singletonList(dsg));

        // Set fee charge offers
        List<ReferenceFFIDRequest.FeeChargeOffer> feeChargeOffers = results.stream()
            .filter(r -> r.getAmount() != null)
            .map(r -> {
                ReferenceFFIDRequest.FeeChargeOffer fco = new ReferenceFFIDRequest.FeeChargeOffer();
                fco.setChargeOffer(r.getChargeOfferId());
                fco.setAmount(r.getAmount().intValue());
                return fco;
            })
            .collect(Collectors.toList());

        referenceFFID.setFeeChargeOffers(feeChargeOffers);

        request.setReferenceFFID(referenceFFID);
        return request;
    }

    public DecisionResponse validateFFID(Long contractId) {
        logger.info("Validating FFID for contract: {}", contractId);
        
        List<FFIDQueryResult> queryResults = executeFFIDQuery(contractId);
        ReferenceFFIDRequest request = buildFFIDPayload(queryResults);
        
        // Log the JSON payload
        try {
            String jsonPayload = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(request);
            logger.info("FFID Validation Request Payload:\n{}", jsonPayload);
        } catch (Exception e) {
            logger.error("Error serializing FFID request payload", e);
        }
        
        logger.info("Successfully built FFID payload for contract: {}", contractId);

        // Get BU ID using the dedicated method
        String buId = getBuIdForContract(contractId);
        String fullUrl = ffidValidationUrl + "_" + buId;

        logger.info("[FFIDValidationService] Starting FFID validation");
        logger.info("[FFIDValidationService] Using validation URL: {}", fullUrl);

        try {
            // Get Okta token
            OktaTokenResponse tokenResponse = oktaTokenService.getOktaToken();

            // Set up headers with bearer token
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(tokenResponse.getAccessToken());
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Create request entity
            HttpEntity<ReferenceFFIDRequest> requestEntity = new HttpEntity<>(request, headers);

            logger.info("[FFIDValidationService] Making POST request to validation endpoint");
            ResponseEntity<DecisionResponse> response = restTemplate.exchange(
                fullUrl,
                HttpMethod.POST,
                requestEntity,
                DecisionResponse.class
            );

            logger.info("[FFIDValidationService] FFID validation request successful");
            DecisionResponse body = response.getBody();
            if (body != null) {
                logger.info("[FFIDValidationService] Response body received");
                if (body.getOutcome() != null) {
                    logger.info("[FFIDValidationService] Outcome status: {}", body.getOutcome().getStatus());
                } else {
                    logger.warn("[FFIDValidationService] Response body has null outcome");
                }
            } else {
                logger.warn("[FFIDValidationService] Received null response body");
            }
            return body;

        } catch (HttpClientErrorException e) {
            logger.error("[FFIDValidationService] HTTP error during FFID validation: {} - {}", 
                e.getStatusCode(), e.getResponseBodyAsString());
            throw e;
        } catch (Exception e) {
            logger.error("[FFIDValidationService] Error during FFID validation: {}", e.getMessage());
            throw e;
        }
    }
}
