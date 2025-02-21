package com.equifax.c2o.api.contract.common.type.response.mirrorFfid;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.io.Serializable;
import java.util.List;

import com.equifax.c2o.api.contract.common.type.ApiErrorCodeEnum;
import com.equifax.c2o.api.contract.common.type.MirrorFfidCreatedByInfo;

/**
 * Response class for Mirror FFID error responses
 */
public class MirrorFfidErrorResponse implements Serializable {
    /**
     * Serial version UID
     */
    private static final long serialVersionUID = 1L;

    private String requestId;
    private String requestStatus;
    private String code;
    private String message;
    private BigDecimal contractVersionId;
    private BigDecimal contractRootId;
    private MirrorFfidCreatedByInfo versionCreatedBy;
    private String createdBy;
    private String sourceSystem;
    private String requestType;
    private String businessUnit;
    private BigDecimal processingRequestID;
    private BigDecimal batchId;
    private MirrorFfidCreatedByInfo batchSubmittedBy;
    private Integer shipToID;
    private Integer billToID;
    private Integer contactID;
    private String contractDataApiUrl;
    private String contractDataApiDocUrl;
    private Integer orderId;
    private String correlationID;
    private Timestamp requestReceivedOn;
    private String fulfillmentId;
    private List<String> chargeOfferExclusions;
    private List<String> chargeOfferTypes;

    /**
     * Default constructor
     */
    public MirrorFfidErrorResponse() {
        super();
    }

    /**
     * Constructor with all fields
     */
    public MirrorFfidErrorResponse(String requestId, String requestStatus, ApiErrorCodeEnum errorCodeEnum, String message,
            BigDecimal contractVersionId, BigDecimal contractRootId, MirrorFfidCreatedByInfo versionCreatedBy,
            String createdBy, String sourceSystem, String requestType, String businessUnit,
            BigDecimal processingRequestID, BigDecimal batchId, MirrorFfidCreatedByInfo batchSubmittedBy,
            Integer shipToID, Integer billToID, Integer contactID, String contractDataApiUrl,
            String contractDataApiDocUrl, Integer orderId, String correlationID, Timestamp requestReceivedOn) {
        super();
        this.requestId = requestId;
        this.requestStatus = requestStatus;
        this.code = errorCodeEnum != null ? errorCodeEnum.getErrorCode() : null;
        this.message = message;
        this.contractVersionId = contractVersionId;
        this.contractRootId = contractRootId;
        this.versionCreatedBy = versionCreatedBy;
        this.createdBy = createdBy;
        this.sourceSystem = sourceSystem;
        this.requestType = requestType;
        this.businessUnit = businessUnit;
        this.processingRequestID = processingRequestID;
        this.batchId = batchId;
        this.batchSubmittedBy = batchSubmittedBy;
        this.shipToID = shipToID;
        this.billToID = billToID;
        this.contactID = contactID;
        this.contractDataApiUrl = contractDataApiUrl;
        this.contractDataApiDocUrl = contractDataApiDocUrl;
        this.orderId = orderId;
        this.correlationID = correlationID;
        this.requestReceivedOn = requestReceivedOn;
    }

    // Getters and Setters
    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getRequestStatus() {
        return requestStatus;
    }

    public void setRequestStatus(String requestStatus) {
        this.requestStatus = requestStatus;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public BigDecimal getContractVersionId() {
        return contractVersionId;
    }

    public void setContractVersionId(BigDecimal contractVersionId) {
        this.contractVersionId = contractVersionId;
    }

    public BigDecimal getContractRootId() {
        return contractRootId;
    }

    public void setContractRootId(BigDecimal contractRootId) {
        this.contractRootId = contractRootId;
    }

    public MirrorFfidCreatedByInfo getVersionCreatedBy() {
        return versionCreatedBy;
    }

    public void setVersionCreatedBy(MirrorFfidCreatedByInfo versionCreatedBy) {
        this.versionCreatedBy = versionCreatedBy;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getSourceSystem() {
        return sourceSystem;
    }

    public void setSourceSystem(String sourceSystem) {
        this.sourceSystem = sourceSystem;
    }

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public String getBusinessUnit() {
        return businessUnit;
    }

    public void setBusinessUnit(String businessUnit) {
        this.businessUnit = businessUnit;
    }

    public BigDecimal getProcessingRequestID() {
        return processingRequestID;
    }

    public void setProcessingRequestID(BigDecimal processingRequestID) {
        this.processingRequestID = processingRequestID;
    }

    public BigDecimal getBatchId() {
        return batchId;
    }

    public void setBatchId(BigDecimal batchId) {
        this.batchId = batchId;
    }

    public MirrorFfidCreatedByInfo getBatchSubmittedBy() {
        return batchSubmittedBy;
    }

    public void setBatchSubmittedBy(MirrorFfidCreatedByInfo batchSubmittedBy) {
        this.batchSubmittedBy = batchSubmittedBy;
    }

    public Integer getShipToID() {
        return shipToID;
    }

    public void setShipToID(Integer shipToID) {
        this.shipToID = shipToID;
    }

    public Integer getBillToID() {
        return billToID;
    }

    public void setBillToID(Integer billToID) {
        this.billToID = billToID;
    }

    public Integer getContactID() {
        return contactID;
    }

    public void setContactID(Integer contactID) {
        this.contactID = contactID;
    }

    public String getContractDataApiUrl() {
        return contractDataApiUrl;
    }

    public void setContractDataApiUrl(String contractDataApiUrl) {
        this.contractDataApiUrl = contractDataApiUrl;
    }

    public String getContractDataApiDocUrl() {
        return contractDataApiDocUrl;
    }

    public void setContractDataApiDocUrl(String contractDataApiDocUrl) {
        this.contractDataApiDocUrl = contractDataApiDocUrl;
    }

    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

    public String getCorrelationID() {
        return correlationID;
    }

    public void setCorrelationID(String correlationID) {
        this.correlationID = correlationID;
    }

    public Timestamp getRequestReceivedOn() {
        return requestReceivedOn;
    }

    public void setRequestReceivedOn(Timestamp requestReceivedOn) {
        this.requestReceivedOn = requestReceivedOn;
    }

    public String getFulfillmentId() {
        return fulfillmentId;
    }

    public void setFulfillmentId(String fulfillmentId) {
        this.fulfillmentId = fulfillmentId;
    }

    public List<String> getChargeOfferExclusions() {
        return chargeOfferExclusions;
    }

    public void setChargeOfferExclusions(List<String> chargeOfferExclusions) {
        this.chargeOfferExclusions = chargeOfferExclusions;
    }

    public List<String> getChargeOfferTypes() {
        return chargeOfferTypes;
    }

    public void setChargeOfferTypes(List<String> chargeOfferTypes) {
        this.chargeOfferTypes = chargeOfferTypes;
    }
}
