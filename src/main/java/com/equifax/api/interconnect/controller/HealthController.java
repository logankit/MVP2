package com.equifax.api.interconnect.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.equifax.api.interconnect.model.OktaTokenResponse;
import com.equifax.api.interconnect.service.OktaTokenService;

@RestController
@RequestMapping("/api")
public class HealthController {

    @Autowired
    private OktaTokenService oktaTokenService;

    @GetMapping("/health")
    public String healthCheck() {
        return "Service is up and running!";
    }

    @GetMapping("/oktaTokenDetails")
    public ResponseEntity<OktaTokenResponse> getOktaTokenDetails() {
        OktaTokenResponse tokenResponse = oktaTokenService.getOktaToken();
        return ResponseEntity.ok(tokenResponse);
    }
}
