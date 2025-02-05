package com.equifax.api.interconnect.model;

import java.util.List;

public class CombinedResponse {
    private String token;
    private List<RulesEditorResponse> rulesResponse;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public List<RulesEditorResponse> getRulesResponse() {
        return rulesResponse;
    }

    public void setRulesResponse(List<RulesEditorResponse> rulesResponse) {
        this.rulesResponse = rulesResponse;
    }
}
