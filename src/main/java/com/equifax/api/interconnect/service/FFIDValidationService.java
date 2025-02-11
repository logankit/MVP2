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

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

@Service
public class FFIDValidationService {
    private static final CommonLogger logger = CommonLogger.getLogger(FFIDValidationService.class);

    @Value("${ffid.validation.url}")
    private String ffidValidationUrl;

    @Autowired
    private OktaTokenService oktaTokenService;

    @Autowired
    private RestTemplate restTemplate;

    @PersistenceContext
    private EntityManager entityManager;

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

    public DecisionResponse validateFFID(Long contractId) {
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

            // Create request body
            ReferenceFFIDRequest requestBody = createHardcodedRequest();
            HttpEntity<ReferenceFFIDRequest> requestEntity = new HttpEntity<>(requestBody, headers);

            logger.info("[FFIDValidationService] Making POST request to validation endpoint");
            ResponseEntity<DecisionResponse> response = restTemplate.exchange(
                fullUrl,
                HttpMethod.POST,
                requestEntity,
                DecisionResponse.class
            );

            logger.info("[FFIDValidationService] FFID validation request successful");
            return response.getBody();

        } catch (HttpClientErrorException e) {
            logger.error("[FFIDValidationService] HTTP error during FFID validation: {} - {}", 
                e.getStatusCode(), e.getResponseBodyAsString());
            throw e;
        } catch (Exception e) {
            logger.error("[FFIDValidationService] Error during FFID validation: {}", e.getMessage());
            throw e;
        }
    }

    private ReferenceFFIDRequest createHardcodedRequest() {
        ReferenceFFIDRequest request = new ReferenceFFIDRequest();
        ReferenceFFIDRequest.ReferenceFFID referenceFFID = new ReferenceFFIDRequest.ReferenceFFID();
        
        // Set basic fields
        referenceFFID.setNewBillTo(false);
        referenceFFID.setC2oGTMEfxID("23353223");
        referenceFFID.setSfdGTMEfxID("23353223");
        
        // Set ActiveChargeOffers
        referenceFFID.setActiveChargeOffers(Arrays.asList(
            "0004104301_CND.GEN.M01.I.ARR_6",
            "0004116033_ACO.GEN.M01.I.ARR_2",
            "0004116035_ACO.GEN.M01.I.ARR_2"
        ));
        
        // Set ActiveChargeOffersTypes
        referenceFFID.setActiveChargeOffersTypes(Arrays.asList("M00", "_1", "_2", "_3", "_4"));
        
        // Set DSGs
        ReferenceFFIDRequest.DSG dsg = new ReferenceFFIDRequest.DSG();
        ReferenceFFIDRequest.ComitmentDSGs comitmentDSGs = new ReferenceFFIDRequest.ComitmentDSGs();
        comitmentDSGs.setIsOwner(false);
        comitmentDSGs.setHasAggrLinkage(false);
        
        ReferenceFFIDRequest.GrantDSGs grantDSGs = new ReferenceFFIDRequest.GrantDSGs();
        grantDSGs.setIsOnwer(false);
        
        dsg.setComitmentDSGs(comitmentDSGs);
        dsg.setGrantDSGs(grantDSGs);
        dsg.setHasAGGRs(false);
        
        referenceFFID.setDSGs(Arrays.asList(dsg));
        
        // Set FeeChargeOffers
        ReferenceFFIDRequest.FeeChargeOffer feeChargeOffer = new ReferenceFFIDRequest.FeeChargeOffer();
        feeChargeOffer.setChargeOffer("0004116033_ACO.GEN.M01.I.ARR_6");
        feeChargeOffer.setAmount(700);
        
        referenceFFID.setFeeChargeOffers(Arrays.asList(feeChargeOffer));
        
        request.setReferenceFFID(referenceFFID);
        return request;
    }
}
