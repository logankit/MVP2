package com.equifax.c2o.api.interconnect.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Collections;
import java.util.stream.Collectors;
import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

import com.equifax.c2o.api.interconnect.model.DecisionResponse;
import com.equifax.c2o.api.interconnect.model.ReferenceFFIDRequest;
import com.equifax.c2o.api.interconnect.model.OktaTokenResponse;
import com.equifax.c2o.api.interconnect.util.CommonLogger;
import com.equifax.c2o.api.contract.common.type.ApiErrorCodeEnum;
import com.equifax.c2o.api.contract.common.type.response.mirrorFfid.MirrorFfidResponse;
import com.equifax.c2o.api.contract.common.type.response.mirrorFfid.MirrorFfidErrorResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.AllArgsConstructor;

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

    private List<FFIDQueryResult> executeFFIDQuery(Long contractId, String fulfillmentId) {
        String sql = """
            SELECT 
                NEW_BILL_TO,
                CONTRACT_ID,
                CHARGE_OFFER_ID,
                BU_ID,
                EFX_ID,
                CHARGE_OFFER_TYPE,
                GRANT_DSG,
                CMT_DSG,
                CASE WHEN HAS_AGGR_LINKAGE = 'Y' THEN 1 ELSE 0 END as HAS_AGGR_LINKAGE,
                AMOUNT,
                FULFILLMENT_ID
            FROM 
                MIRROR_FFID_VALIDATION
            WHERE 
                CONTRACT_ID = :contractId
                AND FULFILLMENT_ID = :fulfillmentId
        """;

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("contractId", contractId);
        query.setParameter("fulfillmentId", fulfillmentId);

        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();
        List<FFIDQueryResult> queryResults = results.stream()
            .map(result -> new FFIDQueryResult(
                ((Number) result[8]).intValue() == 1,
                ((Number) result[1]).longValue(),
                String.valueOf(result[4]),
                String.valueOf(result[2]),
                String.valueOf(result[5]),
                String.valueOf(result[0]),
                null,
                null,
                null,
                (BigDecimal) result[9],
                String.valueOf(result[10])
            ))
            .collect(Collectors.toList());

        return queryResults;
    }

    @Data
    private static class FFIDQueryResult {
        private Boolean hasAggrLinkage;
        private long contractId;
        private String efxId;
        private String chargeOfferId;
        private String chargeOfferType;
        private String newBillTo;
        private String oldBillTo;
        private String status;
        private String reason;
        private BigDecimal amount;
        private String fulfillmentId;

        public FFIDQueryResult(Boolean hasAggrLinkage, long contractId, String efxId, String chargeOfferId, 
                String chargeOfferType, String newBillTo, String oldBillTo, String status, String reason, 
                BigDecimal amount, String fulfillmentId) {
            this.hasAggrLinkage = hasAggrLinkage;
            this.contractId = contractId;
            this.efxId = efxId;
            this.chargeOfferId = chargeOfferId;
            this.chargeOfferType = chargeOfferType;
            this.newBillTo = newBillTo;
            this.oldBillTo = oldBillTo;
            this.status = status;
            this.reason = reason;
            this.amount = amount;
            this.fulfillmentId = fulfillmentId;
        }
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
        
        referenceFFID.setNewBillTo(firstResult.getNewBillTo() != null && firstResult.getNewBillTo().equals("true"));
        referenceFFID.setC2oGTMEfxID(firstResult.getEfxId());
        referenceFFID.setSfdGTMEfxID(firstResult.getEfxId());

        // Set active charge offers (unique)
        List<String> activeChargeOffers = results.stream()
            .map(FFIDQueryResult::getChargeOfferId)
            .distinct()
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
        comitmentDSGs.setHasAggrLinkage(firstResult.getHasAggrLinkage());
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

    private MirrorFfidResponse transformDecisionResponse(List<DecisionResponse> decisionResponses, String fulfillmentId) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            // Log the input payload
            logger.info("Received DecisionResponse payload: {}", 
                mapper.writerWithDefaultPrettyPrinter().writeValueAsString(decisionResponses));
        } catch (Exception e) {
            logger.error("Error logging decision response", e);
        }

        if (decisionResponses == null || decisionResponses.isEmpty()) {
            return new MirrorFfidResponse(
                null, "error", 
                ApiErrorCodeEnum.EFX_INTERCONNECT_API_ERROR,
                "No decision response received",
                Collections.emptyList()
            );
        }

        DecisionResponse.Outcome outcome = decisionResponses.get(0).getOutcome();
        List<MirrorFfidErrorResponse> errorResponses = new ArrayList<>();

        // Case 1: Outcome is null but has exclusions/types
        if (outcome == null) {
            MirrorFfidErrorResponse errorResponse = new MirrorFfidErrorResponse();
            errorResponse.setRequestStatus("Error");
            errorResponse.setCode(ApiErrorCodeEnum.EFX_INTERCONNECT_API_ERROR.getErrorCode());
            errorResponse.setMessage("Invalid decision response format");
            errorResponse.setRequestType("MIRROR_FFID");
            errorResponse.setFulfillmentId(fulfillmentId);
            
            // Still include exclusions and types if available in the decision response
            if (decisionResponses.get(0).getOutcome() != null) {
                if (decisionResponses.get(0).getOutcome().getChargeOffersExclusions() != null) {
                    errorResponse.setChargeOfferExclusions(decisionResponses.get(0).getOutcome().getChargeOffersExclusions());
                }
                if (decisionResponses.get(0).getOutcome().getChargeOffersTypes() != null) {
                    errorResponse.setChargeOfferTypes(decisionResponses.get(0).getOutcome().getChargeOffersTypes());
                }
            }
            
            errorResponses.add(errorResponse);
        }
        // Case 2: Outcome exists with Rejected status
        else if ("Rejected".equals(outcome.getStatus())) {
            if (outcome.getReasons() != null && !outcome.getReasons().isEmpty()) {
                for (DecisionResponse.Reason reason : outcome.getReasons()) {
                    MirrorFfidErrorResponse errorResponse = new MirrorFfidErrorResponse();
                    errorResponse.setRequestStatus(outcome.getStatus());
                    errorResponse.setCode(ApiErrorCodeEnum.EFX_INTERCONNECT_API_ERROR.getErrorCode());
                    errorResponse.setMessage(reason.getMessage());
                    errorResponse.setRequestType("MIRROR_FFID");
                    errorResponse.setFulfillmentId(fulfillmentId);
                    errorResponse.setChargeOfferExclusions(outcome.getChargeOffersExclusions());
                    errorResponse.setChargeOfferTypes(outcome.getChargeOffersTypes());
                    errorResponses.add(errorResponse);
                }
            }
        }
        // Case 3: Outcome exists but no status
        else {
            MirrorFfidErrorResponse errorResponse = new MirrorFfidErrorResponse();
            errorResponse.setRequestStatus("Error");
            errorResponse.setCode(ApiErrorCodeEnum.EFX_INTERCONNECT_API_ERROR.getErrorCode());
            errorResponse.setMessage("Invalid or empty status in decision response");
            errorResponse.setRequestType("MIRROR_FFID");
            errorResponse.setFulfillmentId(fulfillmentId);
            errorResponse.setChargeOfferExclusions(outcome.getChargeOffersExclusions());
            errorResponse.setChargeOfferTypes(outcome.getChargeOffersTypes());
            errorResponses.add(errorResponse);
        }

        MirrorFfidResponse response = new MirrorFfidResponse(
            null,
            "error",
            ApiErrorCodeEnum.EFX_INTERCONNECT_API_ERROR,
            "The FFID validation request was rejected. Please check error details.",
            errorResponses
        );

        try {
            // Log the transformed response
            logger.info("Transformed MirrorFfidResponse: {}", 
                mapper.writerWithDefaultPrettyPrinter().writeValueAsString(response));
        } catch (Exception e) {
            logger.error("Error logging transformed response", e);
        }

        return response;
    }

    private ResponseEntity<MirrorFfidResponse> handleError(String message, Exception e) {
        MirrorFfidResponse errorResponse = new MirrorFfidResponse(
            null,
            "error",
            ApiErrorCodeEnum.EFX_INTERCONNECT_API_ERROR,
            message,
            Collections.emptyList()
        );
        
        try {
            // Log error response
            logger.error("Error Response:\n{}", 
                objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(errorResponse), e);
        } catch (Exception ex) {
            logger.error("Error logging error response", ex);
        }
        
        return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    public ResponseEntity<MirrorFfidResponse> validateFFID(Long contractId, String fulfillmentId) {
        logger.info("Validating FFID for contract: {} and fulfillment: {}", contractId, fulfillmentId);

        try {
            List<FFIDQueryResult> queryResults = executeFFIDQuery(contractId, fulfillmentId);
            if (queryResults.isEmpty()) {
                logger.warn("No results found for contract: {}", contractId);
                return handleError("No results found for the given contract", null);
            }

            ReferenceFFIDRequest request = buildFFIDPayload(queryResults);
            if (request == null) {
                logger.error("Failed to build FFID payload for contract: {}", contractId);
                return handleError("Failed to build FFID payload", null);
            }

            // Log the JSON payload
            try {
                String jsonPayload = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(request);
                logger.info("FFID Validation Request Payload:\n{}", jsonPayload);
            } catch (Exception e) {
                logger.error("Error serializing FFID request payload", e);
            }
            
            logger.info("Successfully built FFID payload for contract: {}", contractId);

            try {
                // Get BU ID using the dedicated method
                String buId = getBuIdForContract(contractId);
                String fullUrl = ffidValidationUrl + "_" + buId;
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
                ResponseEntity<DecisionResponse[]> response = restTemplate.exchange(
                    fullUrl,
                    HttpMethod.POST,
                    requestEntity,
                    DecisionResponse[].class
                );

                logger.info("[FFIDValidationService] FFID validation request successful");
                DecisionResponse[] responses = response.getBody();
                if (responses != null && responses.length > 0) {
                    DecisionResponse decisionResponse = responses[0];
                    // Log the response payload
                    try {
                        String responseJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(responses);
                        logger.info("FFID Validation Response:\n{}", responseJson);
                    } catch (Exception e) {
                        logger.error("Error serializing response payload", e);
                    }
                    
                    if (decisionResponse.getOutcome() != null) {
                        logger.info("[FFIDValidationService] Outcome status: {}", decisionResponse.getOutcome().getStatus());
                        MirrorFfidResponse mirrorFfidResponse = transformDecisionResponse(List.of(decisionResponse), fulfillmentId);
                        return ResponseEntity.ok(mirrorFfidResponse);
                    }
                }
                logger.warn("[FFIDValidationService] Response body has null outcome");
                return ResponseEntity.ok(null);
            } catch (Exception e) {
                return handleError("Internal server error occurred while validating FFID", e);
            }
        } catch (Exception e) {
            return handleError("Internal server error occurred while validating FFID", e);
        }
    }
}
