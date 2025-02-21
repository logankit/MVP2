package com.equifax.c2o.api.interconnect.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;`
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.equifax.c2o.api.interconnect.model.OktaTokenResponse;
import com.equifax.c2o.api.contract.common.type.response.mirrorFfid.MirrorFfidResponse;
import com.equifax.c2o.api.interconnect.service.OktaTokenService;
import com.equifax.c2o.api.interconnect.service.FFIDValidationService;
import com.equifax.c2o.api.interconnect.util.CommonLogger;

@RestController
@RequestMapping("/interconnect/api/v1")
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

    @GetMapping("/validate")
    public ResponseEntity<MirrorFfidResponse> validateFFID(
        @RequestParam("contractId") Long contractId,
        @RequestParam("fulfillmentId") String fulfillmentId
    ) {
        return ffidValidationService.validateFFID(contractId, fulfillmentId);
    }
}
