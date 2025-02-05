package com.equifax.api.interconnect.controller;

import com.equifax.api.interconnect.model.CombinedResponse;
import com.equifax.api.interconnect.service.OktaTokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/interconnect/api/v1")
public class TokenController {
    private static final Logger logger = LoggerFactory.getLogger(TokenController.class);

    @Autowired
    private OktaTokenService oktaTokenService;

    @GetMapping("/token")
    public ResponseEntity<CombinedResponse> getToken() {
        System.out.println("\n=== Token Controller: Processing Request ===");
        logger.info("Received request for token and rules response");
        
        try {
            System.out.println("Calling OktaTokenService for combined response...");
            CombinedResponse response = oktaTokenService.getTokenAndRulesResponse();
            
            System.out.println("Response received successfully");
            System.out.println("Token present: " + (response.getToken() != null));
            System.out.println("Rules response present: " + (response.getRulesResponse() != null));
            
            if (response.getRulesResponse() != null && !response.getRulesResponse().isEmpty()) {
                System.out.println("Rules response status: " + 
                    response.getRulesResponse().get(0).getOutcome().getStatus());
            }
            
            logger.info("Successfully processed token and rules response request");
            System.out.println("=== Token Controller: Request Completed ===\n");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("ERROR in TokenController: " + e.getMessage());
            System.out.println("=== Token Controller: Request Failed ===\n");
            logger.error("Error processing request: {}", e.getMessage(), e);
            throw e;
        }
    }
}
