package com.equifax.api.interconnect.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class RulesEditorRequest {
    @JsonProperty("ReferenceFFID")
    private ReferenceFFID referenceFFID;

    // Getters and Setters
    public ReferenceFFID getReferenceFFID() {
        return referenceFFID;
    }

    public void setReferenceFFID(ReferenceFFID referenceFFID) {
        this.referenceFFID = referenceFFID;
    }

    public static class ReferenceFFID {
        private boolean newBillTo;
        private List<String> activeChargeOffers;
        private List<String> activeChargeOffersTypes;
        private String c2oGTMEfxID;
        private String sfdGTMEfxID;
        private List<DSG> dsgs;
        private List<FeeChargeOffer> feeChargeOffers;

        // Getters and Setters
        @JsonProperty("NewBillTo")
        public boolean isNewBillTo() {
            return newBillTo;
        }

        public void setNewBillTo(boolean newBillTo) {
            this.newBillTo = newBillTo;
        }

        @JsonProperty("ActiveChargeOffers")
        public List<String> getActiveChargeOffers() {
            return activeChargeOffers;
        }

        public void setActiveChargeOffers(List<String> activeChargeOffers) {
            this.activeChargeOffers = activeChargeOffers;
        }

        @JsonProperty("ActiveChargeOffersTypes")
        public List<String> getActiveChargeOffersTypes() {
            return activeChargeOffersTypes;
        }

        public void setActiveChargeOffersTypes(List<String> activeChargeOffersTypes) {
            this.activeChargeOffersTypes = activeChargeOffersTypes;
        }

        @JsonProperty("C2oGTMEfxID")
        public String getC2oGTMEfxID() {
            return c2oGTMEfxID;
        }

        public void setC2oGTMEfxID(String c2oGTMEfxID) {
            this.c2oGTMEfxID = c2oGTMEfxID;
        }

        @JsonProperty("SfdGTMEfxID")
        public String getSfdGTMEfxID() {
            return sfdGTMEfxID;
        }

        public void setSfdGTMEfxID(String sfdGTMEfxID) {
            this.sfdGTMEfxID = sfdGTMEfxID;
        }

        @JsonProperty("DSGs")
        public List<DSG> getDsgs() {
            return dsgs;
        }

        public void setDsgs(List<DSG> dsgs) {
            this.dsgs = dsgs;
        }

        @JsonProperty("FeeChargeOffers")
        public List<FeeChargeOffer> getFeeChargeOffers() {
            return feeChargeOffers;
        }

        public void setFeeChargeOffers(List<FeeChargeOffer> feeChargeOffers) {
            this.feeChargeOffers = feeChargeOffers;
        }
    }

    public static class DSG {
        private ComitmentDSGs comitmentDSGs;
        private GrantDSGs grantDSGs;
        private boolean hasAGGRs;

        @JsonProperty("ComitmentDSGs")
        public ComitmentDSGs getComitmentDSGs() {
            return comitmentDSGs;
        }

        public void setComitmentDSGs(ComitmentDSGs comitmentDSGs) {
            this.comitmentDSGs = comitmentDSGs;
        }

        @JsonProperty("GrantDSGs")
        public GrantDSGs getGrantDSGs() {
            return grantDSGs;
        }

        public void setGrantDSGs(GrantDSGs grantDSGs) {
            this.grantDSGs = grantDSGs;
        }

        @JsonProperty("hasAGGRs")
        public boolean isHasAGGRs() {
            return hasAGGRs;
        }

        public void setHasAGGRs(boolean hasAGGRs) {
            this.hasAGGRs = hasAGGRs;
        }
    }

    public static class ComitmentDSGs {
        private boolean isOwner;
        private boolean hasAggrLinkage;

        @JsonProperty("IsOwner")
        public boolean isOwner() {
            return isOwner;
        }

        public void setOwner(boolean owner) {
            isOwner = owner;
        }

        @JsonProperty("HasAggrLinkage")
        public boolean isHasAggrLinkage() {
            return hasAggrLinkage;
        }

        public void setHasAggrLinkage(boolean hasAggrLinkage) {
            this.hasAggrLinkage = hasAggrLinkage;
        }
    }

    public static class GrantDSGs {
        private boolean isOnwer;

        @JsonProperty("IsOnwer")
        public boolean isOnwer() {
            return isOnwer;
        }

        public void setOnwer(boolean onwer) {
            isOnwer = onwer;
        }
    }

    public static class FeeChargeOffer {
        private String chargeOffer;
        private int amount;

        @JsonProperty("ChargeOffer")
        public String getChargeOffer() {
            return chargeOffer;
        }

        public void setChargeOffer(String chargeOffer) {
            this.chargeOffer = chargeOffer;
        }

        @JsonProperty("Amount")
        public int getAmount() {
            return amount;
        }

        public void setAmount(int amount) {
            this.amount = amount;
        }
    }
}
