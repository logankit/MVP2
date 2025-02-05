package com.equifax.api.interconnect.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.equifax.api.interconnect.config.OktaConfig;
import com.equifax.api.interconnect.util.CommonLogger;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/health")
public class HealthController implements HealthIndicator {
    private static final CommonLogger logger = CommonLogger.getLogger(HealthController.class);
    private final LocalDateTime startTime = LocalDateTime.now();

    @Autowired
    private OktaConfig oktaConfig;

    @GetMapping
    public Map<String, Object> healthCheck() {
        logger.info("Health check requested");
        Map<String, Object> healthStatus = new HashMap<>();
        
        // Basic application info
        healthStatus.put("status", "UP");
        healthStatus.put("startTime", startTime);
        healthStatus.put("currentTime", LocalDateTime.now());
        
        // Check Okta configuration
        Map<String, Object> oktaStatus = new HashMap<>();
        oktaStatus.put("tokenUrlConfigured", oktaConfig.getTokenUrl() != null);
        oktaStatus.put("clientIdConfigured", oktaConfig.getClientId() != null);
        oktaStatus.put("usernameConfigured", oktaConfig.getUsername() != null);
        healthStatus.put("oktaConfig", oktaStatus);

        // Add memory information
        Runtime runtime = Runtime.getRuntime();
        Map<String, Object> memory = new HashMap<>();
        memory.put("total", runtime.totalMemory());
        memory.put("free", runtime.freeMemory());
        memory.put("used", runtime.totalMemory() - runtime.freeMemory());
        healthStatus.put("memory", memory);

        logger.info("Health check completed successfully");
        return healthStatus;
    }

    @Override
    public Health health() {
        try {
            // Check if Okta URL is accessible
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.headForHeaders(oktaConfig.getTokenUrl());
            return Health.up()
                    .withDetail("oktaService", "UP")
                    .withDetail("startTime", startTime)
                    .build();
        } catch (Exception e) {
            return Health.down()
                    .withDetail("oktaService", "DOWN")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
