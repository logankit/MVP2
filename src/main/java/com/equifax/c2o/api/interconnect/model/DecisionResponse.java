package com.equifax.c2o.api.interconnect.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class DecisionResponse {
    @JsonProperty("Outcome")
    private Outcome Outcome;

    public Outcome getOutcome() {
        return Outcome;
    }

    public void setOutcome(Outcome outcome) {
        this.Outcome = outcome;
    }

    public static class Outcome {
        @JsonProperty("status")
        private String status;
        
        @JsonProperty("chargeOffersExclusions")
        private List<String> chargeOffersExclusions;
        
        @JsonProperty("ChargeOffersTypes")
        private List<String> ChargeOffersTypes;
        
        @JsonProperty("Reasons")
        private List<Reason> Reasons;

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public List<String> getChargeOffersExclusions() {
            return chargeOffersExclusions;
        }

        public void setChargeOffersExclusions(List<String> chargeOffersExclusions) {
            this.chargeOffersExclusions = chargeOffersExclusions;
        }

        public List<String> getChargeOffersTypes() {
            return ChargeOffersTypes;
        }

        public void setChargeOffersTypes(List<String> chargeOffersTypes) {
            this.ChargeOffersTypes = chargeOffersTypes;
        }

        public List<Reason> getReasons() {
            return Reasons;
        }

        public void setReasons(List<Reason> reasons) {
            this.Reasons = reasons;
        }
    }

    public static class Reason {
        @JsonProperty("message")
        private String message;

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
