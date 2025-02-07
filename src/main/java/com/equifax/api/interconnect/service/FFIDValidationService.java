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

import javax.net.ssl.SSLContext;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.ArrayList;

@Service
public class FFIDValidationService {
    private static final CommonLogger logger = CommonLogger.getLogger(FFIDValidationService.class);

    @Value("${ffid.validation.url}")
    private String validationUrl;

    @Autowired
    private OktaTokenService oktaTokenService;

    public DecisionResponse validateFFID() {
        logger.info("[FFIDValidationService] Starting FFID validation");
        logger.info("[FFIDValidationService] Using validation URL: {}", validationUrl);
        
        try {
            // Disable SSL certificate validation (curl -k equivalent)
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

            // Get token from OktaTokenService
            OktaTokenResponse tokenResponse = oktaTokenService.getOktaToken();
            logger.info("[FFIDValidationService] Successfully obtained Okta token");
            
            // Set up headers with bearer token
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(tokenResponse.getAccessToken());
            
            // Create request body (hardcoded for now)
            ReferenceFFIDRequest requestBody = createHardcodedRequest();

            // Create request entity
            HttpEntity<ReferenceFFIDRequest> requestEntity = new HttpEntity<>(requestBody, headers);
            
            logger.info("[FFIDValidationService] Making POST request to validation endpoint");
            // Make the POST request
            ResponseEntity<DecisionResponse[]> response = restTemplate.exchange(
                validationUrl,
                HttpMethod.POST,
                requestEntity,
                DecisionResponse[].class
            );

            logger.info("[FFIDValidationService] FFID validation request successful");
            return response.getBody()[0];  // Return first element as per example

        } catch (HttpClientErrorException e) {
            logger.error("[FFIDValidationService] HTTP error during FFID validation: {} - {}", 
                e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Failed to validate FFID: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
        } catch (Exception e) {
            logger.error("[FFIDValidationService] Unexpected error during FFID validation", e);
            throw new RuntimeException("Failed to validate FFID", e);
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
