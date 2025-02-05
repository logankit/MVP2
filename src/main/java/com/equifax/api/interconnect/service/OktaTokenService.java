package com.equifax.api.interconnect.service;

import com.equifax.api.interconnect.model.CombinedResponse;
import com.equifax.api.interconnect.model.OktaTokenResponse;
import com.equifax.api.interconnect.model.RulesEditorRequest;
import com.equifax.api.interconnect.model.RulesEditorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class OktaTokenService {
    private static final Logger logger = LoggerFactory.getLogger(OktaTokenService.class);

    @Value("${okta.token-url}")
    private String tokenUrl;

    @Value("${okta.username}")
    private String username;

    @Value("${okta.password}")
    private String password;

    @Value("${okta.client-id}")
    private String clientId;

    @Value("${okta.client-secret}")
    private String clientSecret;

    @Value("${okta.grant-type}")
    private String grantType;

    @Value("${rules.editor.url}")
    private String rulesEditorUrl;

    @Autowired
    private Environment environment;

    @Autowired
    private RestTemplate restTemplate;

    private boolean isLocalProfile() {
        return Arrays.asList(environment.getActiveProfiles()).contains("local");
    }

    private String getSimulatedToken() {
        System.out.println("Using simulated token for local testing");
        logger.info("Using simulated token for local testing");
        
        // Create a simulated JWT token with username and timestamp
        String simulatedToken = String.format(
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIlcyIsImlhdCI6JXMifQ.signature",
            username,
            System.currentTimeMillis()
        );
        
        System.out.println("Generated simulated token: " + simulatedToken);
        logger.debug("Generated simulated token for local testing");
        
        return simulatedToken;
    }

    public CombinedResponse getTokenAndRulesResponse() {
        System.out.println("\n=== Starting Combined Token and Rules Response Process ===");
        logger.info("Starting combined token and rules response process");
        
        // Get token
        System.out.println("Step 1: Getting Okta token...");
        String token;
        if (isLocalProfile()) {
            token = getSimulatedToken();
        } else {
            token = getRealToken();
        }
        System.out.println("Token obtained successfully");
        System.out.println("Token value: " + token);
        logger.info("Token obtained successfully");
        logger.debug("Token value: {}", token);

        // Create rules request
        System.out.println("\nStep 2: Creating Rules Editor request payload...");
        RulesEditorRequest rulesRequest = createHardcodedPayload();
        System.out.println("Payload created successfully");
        
        // Call rules editor service
        System.out.println("\nStep 3: Calling Rules Editor service with obtained token...");
        List<RulesEditorResponse> rulesResponse = callRulesEditorService(token, rulesRequest);
        System.out.println("Rules Editor service call completed");
        logger.debug("Received response from Rules Editor service");

        // Combine responses
        System.out.println("\nStep 4: Creating combined response...");
        CombinedResponse response = new CombinedResponse();
        response.setToken(token);
        response.setRulesResponse(rulesResponse);

        System.out.println("=== Combined Token and Rules Response Process Completed ===\n");
        logger.info("Completed combined token and rules response process");
        return response;
    }

    public String getRealToken() {
        System.out.println("Initiating real Okta token request...");
        logger.info("Getting Okta token");
        
        System.out.println("Token URL: " + tokenUrl);
        System.out.println("Username: " + username);
        System.out.println("Client ID: " + clientId);
        System.out.println("Grant Type: " + grantType);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        
        // Add Basic Authentication header
        String auth = clientId + ":" + clientSecret;
        byte[] encodedAuth = java.util.Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8));
        String authHeader = "Basic " + new String(encodedAuth);
        headers.set("Authorization", authHeader);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type", grantType);
        map.add("username", username);
        map.add("password", password);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        try {
            System.out.println("Making POST request to Okta token endpoint...");
            ResponseEntity<OktaTokenResponse> response = restTemplate.postForEntity(
                    tokenUrl,
                    request,
                    OktaTokenResponse.class
            );
            System.out.println("Token request successful");
            logger.debug("Successfully obtained Okta token");
            return response.getBody().getAccessToken();
        } catch (Exception e) {
            System.out.println("ERROR: Failed to obtain Okta token - " + e.getMessage());
            logger.error("Error getting Okta token: {}", e.getMessage(), e);
            throw e;
        }
    }

    private List<RulesEditorResponse> callRulesEditorService(String token, RulesEditorRequest request) {
        if (isLocalProfile()) {
            return getMockRulesEditorResponse(request);
        }

        System.out.println("Preparing Rules Editor service call...");
        System.out.println("Rules Editor URL: " + rulesEditorUrl);
        logger.info("Calling Rules Editor service");
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<RulesEditorRequest> entity = new HttpEntity<>(request, headers);

        try {
            System.out.println("Making POST request to Rules Editor service...");
            ResponseEntity<List<RulesEditorResponse>> response = restTemplate.exchange(
                rulesEditorUrl,
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<List<RulesEditorResponse>>() {}
            );
            System.out.println("Rules Editor service call successful");
            System.out.println("Response Status: " + response.getStatusCode());
            logger.debug("Successfully received Rules Editor response");
            return response.getBody();
        } catch (Exception e) {
            System.out.println("ERROR: Failed to call Rules Editor service - " + e.getMessage());
            logger.error("Error calling Rules Editor service: {}", e.getMessage(), e);
            throw e;
        }
    }

    private List<RulesEditorResponse> getMockRulesEditorResponse(RulesEditorRequest request) {
        System.out.println("Generating mock Rules Editor response for local testing");
        logger.info("Using mock Rules Editor response");

        // Create mock response
        RulesEditorResponse mockResponse = new RulesEditorResponse();
        RulesEditorResponse.Outcome outcome = new RulesEditorResponse.Outcome();
        
        // Set status based on fee amount in request
        boolean hasHighFee = request.getReferenceFFID().getFeeChargeOffers().stream()
            .anyMatch(fee -> fee.getAmount() > 500);
        
        if (hasHighFee) {
            outcome.setStatus("Rejected");
            // Add exclusions
            outcome.setChargeOffersExclusions(Arrays.asList("0005111421_CPF.GEN.M01.I.ARR_6"));
            // Add charge offer types
            outcome.setChargeOffersTypes(Arrays.asList("M00", "M01"));
            // Add reason
            RulesEditorResponse.Reason reason = new RulesEditorResponse.Reason();
            reason.setMessage("Charge Offer - Fee Charge offer > $500 found on the reference FID. To be handled manually.");
            outcome.setReasons(Collections.singletonList(reason));
        } else {
            outcome.setStatus("Approved");
            outcome.setChargeOffersExclusions(Collections.emptyList());
            outcome.setChargeOffersTypes(Arrays.asList("M00", "M01"));
            outcome.setReasons(Collections.emptyList());
        }

        mockResponse.setOutcome(outcome);

        System.out.println("Generated mock response with status: " + outcome.getStatus());
        System.out.println("Mock response details:");
        System.out.println("- Status: " + outcome.getStatus());
        System.out.println("- Charge Offers Exclusions: " + outcome.getChargeOffersExclusions());
        System.out.println("- Charge Offers Types: " + outcome.getChargeOffersTypes());
        if (!outcome.getReasons().isEmpty()) {
            System.out.println("- Reason: " + outcome.getReasons().get(0).getMessage());
        }

        return Collections.singletonList(mockResponse);
    }

    private RulesEditorRequest createHardcodedPayload() {
        System.out.println("Creating hardcoded Rules Editor payload...");
        logger.debug("Creating hardcoded payload");
        
        // Create main request object
        RulesEditorRequest request = new RulesEditorRequest();
        RulesEditorRequest.ReferenceFFID referenceFFID = new RulesEditorRequest.ReferenceFFID();
        
        // Set basic fields
        System.out.println("Setting basic fields...");
        referenceFFID.setNewBillTo(false);
        referenceFFID.setC2oGTMEfxID("23353223");
        referenceFFID.setSfdGTMEfxID("23353223");
        
        // Set Active Charge Offers
        System.out.println("Setting Active Charge Offers...");
        List<String> activeChargeOffers = Arrays.asList(
            "0004104301_CND.GEN.M01.I.ARR_6",
            "0004116033_ACO.GEN.M01.I.ARR_2",
            "0004116035_ACO.GEN.M01.I.ARR_2"
        );
        referenceFFID.setActiveChargeOffers(activeChargeOffers);
        
        // Set Active Charge Offers Types
        System.out.println("Setting Active Charge Offers Types...");
        List<String> activeChargeOffersTypes = Arrays.asList("M00", "M01");
        referenceFFID.setActiveChargeOffersTypes(activeChargeOffersTypes);
        
        // Set DSGs
        System.out.println("Setting DSGs...");
        List<RulesEditorRequest.DSG> dsgs = new ArrayList<>();
        RulesEditorRequest.DSG dsg = new RulesEditorRequest.DSG();
        
        // Set Fee Charge Offers with a high fee to trigger rejection
        System.out.println("Setting Fee Charge Offers...");
        List<RulesEditorRequest.FeeChargeOffer> feeChargeOffers = new ArrayList<>();
        RulesEditorRequest.FeeChargeOffer feeOffer = new RulesEditorRequest.FeeChargeOffer();
        feeOffer.setAmount(600); // Fixed: Changed from double to int
        feeChargeOffers.add(feeOffer);
        
        referenceFFID.setDsgs(dsgs);
        referenceFFID.setFeeChargeOffers(feeChargeOffers);
        request.setReferenceFFID(referenceFFID);
        
        System.out.println("Hardcoded payload created successfully");
        return request;
    }
}
