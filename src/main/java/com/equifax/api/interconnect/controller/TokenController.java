package com.equifax.api.interconnect.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.equifax.api.interconnect.model.OktaTokenResponse;
import com.equifax.api.interconnect.service.OktaTokenService;
import com.equifax.api.interconnect.util.CommonLogger;

@RestController
public class TokenController {
    private static final CommonLogger logger = CommonLogger.getLogger(TokenController.class);

    @Autowired
    private OktaTokenService oktaTokenService;

    @PostMapping("/token")
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
}
