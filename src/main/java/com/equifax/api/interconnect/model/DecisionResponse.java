package com.equifax.api.interconnect.model;

import java.util.List;

public class DecisionResponse {
    private Outcome Outcome;

    public Outcome getOutcome() {
        return Outcome;
    }

    public void setOutcome(Outcome outcome) {
        this.Outcome = outcome;
    }

    public static class Outcome {
        private String status;
        private List<String> chargeOffersExclusions;
        private List<String> ChargeOffersTypes;
        private List<Reason> Reasons;

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
        private String message;

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
