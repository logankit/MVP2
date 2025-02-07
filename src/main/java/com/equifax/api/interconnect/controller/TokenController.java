package com.equifax.api.interconnect.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.equifax.api.interconnect.model.OktaTokenResponse;
import com.equifax.api.interconnect.model.DecisionResponse;
import com.equifax.api.interconnect.service.OktaTokenService;
import com.equifax.api.interconnect.service.FFIDValidationService;
import com.equifax.api.interconnect.util.CommonLogger;

@RestController
public class TokenController {
    private static final CommonLogger logger = CommonLogger.getLogger(TokenController.class);

    @Autowired
    private OktaTokenService oktaTokenService;

    @Autowired
    private FFIDValidationService ffidValidationService;

    @GetMapping("/token")
    public OktaTokenResponse getToken() {
        logger.info("Received token request");
        try {
            OktaTokenResponse response = oktaTokenService.getOktaToken();
            logger.info("Token request processed successfully");
            return response;
        } catch (Exception e) {
            logger.error("Error processing token request: %s", e.getMessage());
            throw e;
        }
    }

    @GetMapping("/validateFFID")
    public ResponseEntity<DecisionResponse> getValidateFFID() {
        logger.info("[TokenController] Received request to validate FFID");
        DecisionResponse response = ffidValidationService.validateFFID();
        logger.info("[TokenController] Successfully validated FFID");
        return ResponseEntity.ok(response);
    }
}
