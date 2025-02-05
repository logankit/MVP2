package com.equifax.api.interconnect.service;

import com.equifax.api.interconnect.model.RulesEditorRequest;
import com.equifax.api.interconnect.model.RulesEditorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class RulesEditorService {
    private static final Logger logger = LoggerFactory.getLogger(RulesEditorService.class);

    @Value("${rules.editor.url}")
    private String rulesEditorUrl;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private OktaTokenService oktaTokenService;

    public List<RulesEditorResponse> processRulesEditorRequest(RulesEditorRequest request) {
        logger.info("Processing Rules Editor request");
        logger.debug("Rules Editor URL: {}", rulesEditorUrl);
        
        String token = oktaTokenService.getRealToken();
        logger.debug("Obtained Okta token");
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        
        HttpEntity<RulesEditorRequest> entity = new HttpEntity<>(request, headers);
        
        try {
            logger.info("Making request to Rules Editor service");
            ResponseEntity<List<RulesEditorResponse>> response = restTemplate.exchange(
                rulesEditorUrl,
                HttpMethod.POST,
                entity,
                new org.springframework.core.ParameterizedTypeReference<List<RulesEditorResponse>>() {}
            );
            logger.info("Successfully received response from Rules Editor service");
            logger.debug("Response status code: {}", response.getStatusCode());
            
            return response.getBody();
        } catch (Exception e) {
            logger.error("Error while calling Rules Editor service: {}", e.getMessage(), e);
            throw e;
        }
    }
}
