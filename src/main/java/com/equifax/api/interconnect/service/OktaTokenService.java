package com.equifax.api.interconnect.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.equifax.api.interconnect.config.OktaConfig;
import com.equifax.api.interconnect.model.OktaTokenResponse;
import com.equifax.api.interconnect.util.CommonLogger;

import java.util.Base64;

@Service
public class OktaTokenService {
    private static final CommonLogger logger = CommonLogger.getLogger(OktaTokenService.class);

    @Autowired
    private OktaConfig oktaConfig;

    public OktaTokenResponse getOktaToken() {
        logger.info("Initiating Okta token request");
        logger.info("Token URL: %s", oktaConfig.getTokenUrl());
        logger.info("Username: %s", oktaConfig.getUsername());
        logger.info("Client ID: %s", oktaConfig.getClientId());
        logger.info("Grant Type: %s", oktaConfig.getGrantType());

        // Real implementation for Okta token generation
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            
            // Add Basic Authentication
            String auth = oktaConfig.getClientId() + ":" + oktaConfig.getClientSecret();
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
            headers.add("Authorization", "Basic " + encodedAuth);
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            // Add form parameters
            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            map.add("grant_type", oktaConfig.getGrantType());
            map.add("username", oktaConfig.getUsername());
            map.add("password", oktaConfig.getPassword());

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
            
            OktaTokenResponse response = restTemplate.postForObject(
                oktaConfig.getTokenUrl(), 
                request, 
                OktaTokenResponse.class
            );

            logger.info("Token generated successfully");
            logger.info("Token type: %s", response.getTokenType());
            logger.info("Token expires in: %d seconds", response.getExpiresIn());
            
            return response;

        } catch (Exception e) {
            logger.error("Error generating Okta token", e);
            throw new RuntimeException("Failed to generate Okta token", e);
        }

        /* Simulation code - commented out
        OktaTokenResponse mockResponse = new OktaTokenResponse();
        String simulatedToken = String.format("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIlcyIsImlhdCI6JXMifQ.signature",
            oktaConfig.getUsername(),
            System.currentTimeMillis());
            
        mockResponse.setAccessToken(simulatedToken);
        mockResponse.setTokenType("Bearer");
        mockResponse.setExpiresIn(3600);
        
        return mockResponse;
        */
    }
}
