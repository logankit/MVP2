package com.equifax.api.interconnect.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.equifax.api.interconnect.config.OktaConfig;
import com.equifax.api.interconnect.model.OktaTokenResponse;
import com.equifax.api.interconnect.util.CommonLogger;

import java.util.Arrays;
import java.util.Base64;

@Service
public class OktaTokenService {
    private static final CommonLogger logger = CommonLogger.getLogger(OktaTokenService.class);

    @Autowired
    private OktaConfig oktaConfig;

    @Autowired
    private Environment environment;

    public OktaTokenResponse getOktaToken() {
        // Check if running in local profile
        if (isLocalProfile()) {
            return getSimulatedToken();
        }
        return getRealToken();
    }

    private boolean isLocalProfile() {
        return Arrays.asList(environment.getActiveProfiles()).contains("local");
    }

    private OktaTokenResponse getSimulatedToken() {
        logger.info("Using simulated Okta token for local testing");
        OktaTokenResponse mockResponse = new OktaTokenResponse();
        
        // Create a simulated JWT token with username and timestamp
        String simulatedToken = String.format(
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIlcyIsImlhdCI6JXMifQ.signature",
            oktaConfig.getUsername(),
            System.currentTimeMillis()
        );
        
        mockResponse.setAccessToken(simulatedToken);
        mockResponse.setTokenType("Bearer");
        mockResponse.setExpiresIn(3600);
        mockResponse.setScope("openid");
        
        logger.info("Simulated token generated successfully");
        logger.debug("Token type: Bearer, Expires in: 3600 seconds");
        
        return mockResponse;
    }

    private OktaTokenResponse getRealToken() {
        logger.info("Initiating real Okta token request");
        logger.debug("Token URL: %s", oktaConfig.getTokenUrl());
        logger.debug("Username: %s", oktaConfig.getUsername());
        logger.debug("Client ID: %s", oktaConfig.getClientId());
        logger.debug("Grant Type: %s", oktaConfig.getGrantType());

        try {
            RestTemplate restTemplate = new RestTemplate();
            
            // Set up headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            
            // Add Basic Authentication
            String auth = oktaConfig.getClientId() + ":" + oktaConfig.getClientSecret();
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
            headers.add(HttpHeaders.AUTHORIZATION, "Basic " + encodedAuth);
            
            // Set up form parameters
            MultiValueMap<String, String> formParams = new LinkedMultiValueMap<>();
            formParams.add("grant_type", oktaConfig.getGrantType());
            formParams.add("username", oktaConfig.getUsername());
            formParams.add("password", oktaConfig.getPassword());
            formParams.add("scope", "openid");

            // Create request entity
            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(formParams, headers);
            
            // Log request details (excluding sensitive info)
            logger.debug("Request headers: %s", headers.entrySet().stream()
                .filter(e -> !e.getKey().equalsIgnoreCase("Authorization"))
                .map(e -> e.getKey() + ": " + e.getValue())
                .reduce("", (a, b) -> a + "\n" + b));
            logger.debug("Request parameters: grant_type=%s, username=%s, scope=%s", 
                oktaConfig.getGrantType(), oktaConfig.getUsername(), "openid");

            // Make the request
            ResponseEntity<OktaTokenResponse> response = restTemplate.postForEntity(
                oktaConfig.getTokenUrl(),
                requestEntity,
                OktaTokenResponse.class
            );

            logger.info("Token request successful");
            return response.getBody();

        } catch (HttpClientErrorException e) {
            logger.error("HTTP error during token request: %s - %s", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Failed to obtain Okta token: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
        } catch (Exception e) {
            logger.error("Error obtaining Okta token: %s", e.getMessage());
            throw new RuntimeException("Failed to obtain Okta token", e);
        }
    }
}
