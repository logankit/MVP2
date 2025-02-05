package com.equifax.api.interconnect.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class RulesEditorResponse {
    @JsonProperty("Outcome")
    private Outcome outcome;

    public Outcome getOutcome() {
        return outcome;
    }

    public void setOutcome(Outcome outcome) {
        this.outcome = outcome;
    }

    public static class Outcome {
        @JsonProperty("status")
        private String status;

        @JsonProperty("chargeOffersExclusions")
        private List<String> chargeOffersExclusions;

        @JsonProperty("ChargeOffersTypes")
        private List<String> chargeOffersTypes;

        @JsonProperty("Reasons")
        private List<Reason> reasons;

        // Getters and Setters
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
            return chargeOffersTypes;
        }

        public void setChargeOffersTypes(List<String> chargeOffersTypes) {
            this.chargeOffersTypes = chargeOffersTypes;
        }

        public List<Reason> getReasons() {
            return reasons;
        }

        public void setReasons(List<Reason> reasons) {
            this.reasons = reasons;
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
