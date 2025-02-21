package com.equifax.c2o.api.interconnect.model;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ReferenceFFIDRequest {
    @JsonProperty("referenceFFID")
    private ReferenceFFID referenceFFID;

    public ReferenceFFID getReferenceFFID() {
        return referenceFFID;
    }

    public void setReferenceFFID(ReferenceFFID referenceFFID) {
        this.referenceFFID = referenceFFID;
    }

    public static class ReferenceFFID {
        @JsonProperty("newBillTo")
        private boolean newBillTo;
        
        @JsonProperty("activeChargeOffers")
        private List<String> activeChargeOffers;
        
        @JsonProperty("activeChargeOffersTypes")
        private List<String> activeChargeOffersTypes;
        
        @JsonProperty("c2oGTMEfxID")
        private String c2oGTMEfxID;
        
        @JsonProperty("sfdGTMEfxID")
        private String sfdGTMEfxID;
        
        @JsonProperty("DSGs")
        private List<DSG> DSGs;
        
        @JsonProperty("feeChargeOffers")
        private List<FeeChargeOffer> feeChargeOffers;

        public boolean isNewBillTo() {
            return newBillTo;
        }

        public void setNewBillTo(boolean newBillTo) {
            this.newBillTo = newBillTo;
        }

        public List<String> getActiveChargeOffers() {
            return activeChargeOffers;
        }

        public void setActiveChargeOffers(List<String> activeChargeOffers) {
            this.activeChargeOffers = activeChargeOffers;
        }

        public List<String> getActiveChargeOffersTypes() {
            return activeChargeOffersTypes;
        }

        public void setActiveChargeOffersTypes(List<String> activeChargeOffersTypes) {
            this.activeChargeOffersTypes = activeChargeOffersTypes;
        }

        public String getC2oGTMEfxID() {
            return c2oGTMEfxID;
        }

        public void setC2oGTMEfxID(String c2oGTMEfxID) {
            this.c2oGTMEfxID = c2oGTMEfxID;
        }

        public String getSfdGTMEfxID() {
            return sfdGTMEfxID;
        }

        public void setSfdGTMEfxID(String sfdGTMEfxID) {
            this.sfdGTMEfxID = sfdGTMEfxID;
        }

        public List<DSG> getDSGs() {
            return DSGs;
        }

        public void setDSGs(List<DSG> DSGs) {
            this.DSGs = DSGs;
        }

        public List<FeeChargeOffer> getFeeChargeOffers() {
            return feeChargeOffers;
        }

        public void setFeeChargeOffers(List<FeeChargeOffer> feeChargeOffers) {
            this.feeChargeOffers = feeChargeOffers;
        }
    }

    public static class DSG {
        @JsonProperty("comitmentDSGs")
        private ComitmentDSGs comitmentDSGs;
        
        @JsonProperty("grantDSGs")
        private GrantDSGs grantDSGs;
        
        @JsonProperty("hasAGGRs")
        private boolean hasAGGRs;

        public ComitmentDSGs getComitmentDSGs() {
            return comitmentDSGs;
        }

        public void setComitmentDSGs(ComitmentDSGs comitmentDSGs) {
            this.comitmentDSGs = comitmentDSGs;
        }

        public GrantDSGs getGrantDSGs() {
            return grantDSGs;
        }

        public void setGrantDSGs(GrantDSGs grantDSGs) {
            this.grantDSGs = grantDSGs;
        }

        public boolean isHasAGGRs() {
            return hasAGGRs;
        }

        public void setHasAGGRs(boolean hasAGGRs) {
            this.hasAGGRs = hasAGGRs;
        }
    }

    public static class ComitmentDSGs {
        @JsonProperty("isOwner")
        private boolean isOwner;
        
        @JsonProperty("hasAggrLinkage")
        private boolean hasAggrLinkage;

        public boolean isIsOwner() {
            return isOwner;
        }

        public void setIsOwner(boolean isOwner) {
            this.isOwner = isOwner;
        }

        public boolean isHasAggrLinkage() {
            return hasAggrLinkage;
        }

        public void setHasAggrLinkage(boolean hasAggrLinkage) {
            this.hasAggrLinkage = hasAggrLinkage;
        }
    }

    public static class GrantDSGs {
        @JsonProperty("isOnwer")
        private boolean isOnwer;

        public boolean isIsOnwer() {
            return isOnwer;
        }

        public void setIsOnwer(boolean isOnwer) {
            this.isOnwer = isOnwer;
        }
    }

    public static class FeeChargeOffer {
        @JsonProperty("chargeOffer")
        private String chargeOffer;
        
        @JsonProperty("amount")
        private int amount;

        public String getChargeOffer() {
            return chargeOffer;
        }

        public void setChargeOffer(String chargeOffer) {
            this.chargeOffer = chargeOffer;
        }

        public int getAmount() {
            return amount;
        }

        public void setAmount(int amount) {
            this.amount = amount;
        }
    }
}
