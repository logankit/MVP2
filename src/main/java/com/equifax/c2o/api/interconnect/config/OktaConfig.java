package com.equifax.c2o.api.interconnect.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import com.equifax.c2o.api.interconnect.util.CommonLogger;

@Configuration
public class OktaConfig {
    @Value("${okta.token-url}")
    private String tokenUrl;

    @Value("${okta.username}")
    private String username;

    @Value("${okta.password}")
    private String password;

    @Value("${okta.client-id}")
    private String clientId;

    @Value("${okta.client-secret}")
    private String clientSecret;

    @Value("${okta.grant-type}")
    private String grantType;

    public String getTokenUrl() {
        return tokenUrl;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getGrantType() {
        return grantType;
    }
}
