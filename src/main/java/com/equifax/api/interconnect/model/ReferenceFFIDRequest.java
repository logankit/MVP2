package com.equifax.api.interconnect.model;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ReferenceFFIDRequest {
    @JsonProperty("ReferenceFFID")
    private ReferenceFFID referenceFFID;

    public ReferenceFFID getReferenceFFID() {
        return referenceFFID;
    }

    public void setReferenceFFID(ReferenceFFID referenceFFID) {
        this.referenceFFID = referenceFFID;
    }

    public static class ReferenceFFID {
        private boolean NewBillTo;
        private List<String> ActiveChargeOffers;
        private List<String> ActiveChargeOffersTypes;
        private String C2oGTMEfxID;
        private String SfdGTMEfxID;
        private List<DSG> DSGs;
        private List<FeeChargeOffer> FeeChargeOffers;

        // Getters and Setters
        public boolean isNewBillTo() {
            return NewBillTo;
        }

        public void setNewBillTo(boolean newBillTo) {
            this.NewBillTo = newBillTo;
        }

        public List<String> getActiveChargeOffers() {
            return ActiveChargeOffers;
        }

        public void setActiveChargeOffers(List<String> activeChargeOffers) {
            this.ActiveChargeOffers = activeChargeOffers;
        }

        public List<String> getActiveChargeOffersTypes() {
            return ActiveChargeOffersTypes;
        }

        public void setActiveChargeOffersTypes(List<String> activeChargeOffersTypes) {
            this.ActiveChargeOffersTypes = activeChargeOffersTypes;
        }

        public String getC2oGTMEfxID() {
            return C2oGTMEfxID;
        }

        public void setC2oGTMEfxID(String c2oGTMEfxID) {
            this.C2oGTMEfxID = c2oGTMEfxID;
        }

        public String getSfdGTMEfxID() {
            return SfdGTMEfxID;
        }

        public void setSfdGTMEfxID(String sfdGTMEfxID) {
            this.SfdGTMEfxID = sfdGTMEfxID;
        }

        public List<DSG> getDSGs() {
            return DSGs;
        }

        public void setDSGs(List<DSG> dSGs) {
            this.DSGs = dSGs;
        }

        public List<FeeChargeOffer> getFeeChargeOffers() {
            return FeeChargeOffers;
        }

        public void setFeeChargeOffers(List<FeeChargeOffer> feeChargeOffers) {
            this.FeeChargeOffers = feeChargeOffers;
        }
    }

    public static class DSG {
        private ComitmentDSGs ComitmentDSGs;
        private GrantDSGs GrantDSGs;
        private boolean hasAGGRs;

        // Getters and Setters
        public ComitmentDSGs getComitmentDSGs() {
            return ComitmentDSGs;
        }

        public void setComitmentDSGs(ComitmentDSGs comitmentDSGs) {
            this.ComitmentDSGs = comitmentDSGs;
        }

        public GrantDSGs getGrantDSGs() {
            return GrantDSGs;
        }

        public void setGrantDSGs(GrantDSGs grantDSGs) {
            this.GrantDSGs = grantDSGs;
        }

        public boolean isHasAGGRs() {
            return hasAGGRs;
        }

        public void setHasAGGRs(boolean hasAGGRs) {
            this.hasAGGRs = hasAGGRs;
        }
    }

    public static class ComitmentDSGs {
        private boolean IsOwner;
        private boolean HasAggrLinkage;

        // Getters and Setters
        public boolean isIsOwner() {
            return IsOwner;
        }

        public void setIsOwner(boolean isOwner) {
            this.IsOwner = isOwner;
        }

        public boolean isHasAggrLinkage() {
            return HasAggrLinkage;
        }

        public void setHasAggrLinkage(boolean hasAggrLinkage) {
            this.HasAggrLinkage = hasAggrLinkage;
        }
    }

    public static class GrantDSGs {
        private boolean IsOnwer;

        // Getters and Setters
        public boolean isIsOnwer() {
            return IsOnwer;
        }

        public void setIsOnwer(boolean isOnwer) {
            this.IsOnwer = isOnwer;
        }
    }

    public static class FeeChargeOffer {
        private String ChargeOffer;
        private int Amount;

        // Getters and Setters
        public String getChargeOffer() {
            return ChargeOffer;
        }

        public void setChargeOffer(String chargeOffer) {
            this.ChargeOffer = chargeOffer;
        }

        public int getAmount() {
            return Amount;
        }

        public void setAmount(int amount) {
            this.Amount = amount;
        }
    }
}
