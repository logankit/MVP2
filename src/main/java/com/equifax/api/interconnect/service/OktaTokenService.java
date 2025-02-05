package com.equifax.api.interconnect.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
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

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import java.security.SecureRandom;

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
        logger.info("[OktaTokenService] Initiating real Okta token request");
        logger.info("[OktaTokenService] Token URL: {}", oktaConfig.getTokenUrl());
        logger.info("[OktaTokenService] Username: {}", oktaConfig.getUsername());
        logger.info("[OktaTokenService] Client ID: {}", oktaConfig.getClientId());
        logger.info("[OktaTokenService] Grant Type: {}", oktaConfig.getGrantType());
        logger.info("[OktaTokenService] Scope: sa.readprofile");

        try {
            // Disable SSL certificate validation
            TrustManager[] trustAllCerts = new TrustManager[] {
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() { return null; }
                    public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) { }
                    public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) { }
                }
            };

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new SecureRandom());

            // Create RestTemplate with SSL context that trusts all certificates
            RestTemplate restTemplate = new RestTemplate();
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
            
            // Set up headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            
            // Add Basic Authentication
            String auth = oktaConfig.getClientId() + ":" + oktaConfig.getClientSecret();
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
            headers.add(HttpHeaders.AUTHORIZATION, "Basic " + encodedAuth);
            logger.debug("[OktaTokenService] Basic auth header created");
            
            // Set up form parameters
            MultiValueMap<String, String> formParams = new LinkedMultiValueMap<>();
            formParams.add("grant_type", oktaConfig.getGrantType());
            formParams.add("username", oktaConfig.getUsername());
            formParams.add("password", oktaConfig.getPassword());
            formParams.add("scope", "sa.readprofile");

            // Create request entity
            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(formParams, headers);
            logger.info("[OktaTokenService] Making POST request to Okta token endpoint");

            // Make the request
            ResponseEntity<OktaTokenResponse> response = restTemplate.exchange(
                oktaConfig.getTokenUrl(),
                HttpMethod.POST,
                requestEntity,
                OktaTokenResponse.class
            );

            logger.info("[OktaTokenService] Token request successful");
            return response.getBody();

        } catch (HttpClientErrorException e) {
            logger.error("[OktaTokenService] HTTP error during token request: {} - {}", 
                e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Failed to obtain Okta token: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
        } catch (Exception e) {
            logger.error("[OktaTokenService] Unexpected error during token request", e);
            throw new RuntimeException("Failed to obtain Okta token", e);
        }
    }
}
