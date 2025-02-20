package com.equifax.api.interconnect.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.equifax.api.interconnect.model.DecisionResponse;
import com.equifax.api.interconnect.model.OktaTokenResponse;
import com.equifax.api.interconnect.model.ReferenceFFIDRequest;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
            cco.contract_price amount,
            cco.fulfillment_id
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
            AND (cco.purchase_end_date is null or cco.purchase_end_date > trunc(sysdate))
            AND ce.fulfillment_id = :fulfillmentId
            AND c.version_status in (32,34)
        ORDER BY 
            cco.charge_offer_id
    """;

    @Data
    @AllArgsConstructor
    public class FFIDQueryResult {
        private Boolean newBillTo;
        private Long contractId;
        private String chargeOfferId;
        private String buId;
        private String efxId;
        private String chargeOfferType;
        private String grantDsg;
        private String cmtDsg;
        private String hasAggrLinkage;
        private BigDecimal amount;
        private String fulfillmentId;
    }

    private List<FFIDQueryResult> executeFFIDQuery(Long contractId, String fulfillmentId) {
        Query query = entityManager.createNativeQuery(FFID_QUERY)
            .setParameter("contractId", contractId)
            .setParameter("fulfillmentId", fulfillmentId);
        
        List<Object[]> results = query.getResultList();
        return results.stream()
            .map(result -> new FFIDQueryResult(
                (Boolean) result[0],
                ((Number) result[1]).longValue(),
                String.valueOf(result[2]),
                String.valueOf(result[3]),
                String.valueOf(result[4]),
                String.valueOf(result[5]),
                String.valueOf(result[6]),
                String.valueOf(result[7]),
                String.valueOf(result[8]),
                (BigDecimal) result[9],
                String.valueOf(result[10])
            ))
            .collect(Collectors.toList());
    }

    private String getBuIdForContract(Long contractId) {
        String query = "SELECT bu_id FROM c2o_contract_bu_intr WHERE contract_id = :contractId";
        return (String) entityManager.createNativeQuery(query)
            .setParameter("contractId", contractId)
            .getSingleResult();
    }

    private ReferenceFFIDRequest buildFFIDPayload(List<FFIDQueryResult> results) {
        if (results.isEmpty()) {
            return null;
        }

        FFIDQueryResult firstResult = results.get(0);
        ReferenceFFIDRequest request = new ReferenceFFIDRequest();
        ReferenceFFIDRequest.ReferenceFFID referenceFFID = new ReferenceFFIDRequest.ReferenceFFID();
        
        referenceFFID.setNewBillTo(firstResult.getNewBillTo() != null ? firstResult.getNewBillTo() : false);
        referenceFFID.setC2oGTMEfxID(firstResult.getEfxId());
        referenceFFID.setSfdGTMEfxID(firstResult.getEfxId());

        // Set active charge offers
        List<String> activeChargeOffers = results.stream()
            .map(FFIDQueryResult::getChargeOfferId)
            .collect(Collectors.toList());
        referenceFFID.setActiveChargeOffers(activeChargeOffers);

        // Set active charge offer types (unique)
        List<String> activeChargeOfferTypes = results.stream()
            .map(FFIDQueryResult::getChargeOfferType)
            .distinct()
            .collect(Collectors.toList());
        referenceFFID.setActiveChargeOffersTypes(activeChargeOfferTypes);

        // Set DSGs
        List<ReferenceFFIDRequest.DSG> dsgs = new ArrayList<>();
        ReferenceFFIDRequest.DSG dsg = new ReferenceFFIDRequest.DSG();
        
        ReferenceFFIDRequest.ComitmentDSGs comitmentDSGs = new ReferenceFFIDRequest.ComitmentDSGs();
        comitmentDSGs.setIsOwner(false);
        comitmentDSGs.setHasAggrLinkage(firstResult.getHasAggrLinkage().equals("Y"));
        dsg.setComitmentDSGs(comitmentDSGs);
        
        ReferenceFFIDRequest.GrantDSGs grantDSGs = new ReferenceFFIDRequest.GrantDSGs();
        grantDSGs.setIsOnwer(false);
        dsg.setGrantDSGs(grantDSGs);
        
        dsg.setHasAGGRs(false);
        dsgs.add(dsg);
        referenceFFID.setDSGs(dsgs);

        // Set fee charge offers
        List<ReferenceFFIDRequest.FeeChargeOffer> feeChargeOffers = results.stream()
            .map(result -> {
                ReferenceFFIDRequest.FeeChargeOffer feeChargeOffer = new ReferenceFFIDRequest.FeeChargeOffer();
                feeChargeOffer.setChargeOffer(result.getChargeOfferId());
                feeChargeOffer.setAmount(result.getAmount().intValue());
                return feeChargeOffer;
            })
            .collect(Collectors.toList());
        referenceFFID.setFeeChargeOffers(feeChargeOffers);

        request.setReferenceFFID(referenceFFID);
        return request;
    }

    public ResponseEntity<DecisionResponse> validateFFID(Long contractId, String fulfillmentId) {
        logger.info("Validating FFID for contract: {} and fulfillment: {}", contractId, fulfillmentId);
        
        List<FFIDQueryResult> queryResults = executeFFIDQuery(contractId, fulfillmentId);
        if (queryResults.isEmpty()) {
            logger.warn("No results found for contract: {}", contractId);
            return ResponseEntity.notFound().build();
        }

        ReferenceFFIDRequest request = buildFFIDPayload(queryResults);
        if (request == null) {
            logger.error("Failed to build FFID payload for contract: {}", contractId);
            return ResponseEntity.badRequest().build();
        }

        try {
            String fullUrl = ffidValidationUrl;
            logger.info("[FFIDValidationService] Using validation URL: {}", fullUrl);

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
            DecisionResponse decisionResponse = response.getBody();
            if (decisionResponse != null && decisionResponse.getOutcome() != null) {
                logger.info("[FFIDValidationService] Outcome status: {}", decisionResponse.getOutcome().getStatus());
                return ResponseEntity.ok(decisionResponse);
            } else {
                logger.warn("[FFIDValidationService] Response body has null outcome");
                return ResponseEntity.ok(null);
            }

        } catch (Exception e) {
            logger.error("[FFIDValidationService] Error during FFID validation: {}", e.getMessage());
            throw e;
        }
    }
}
