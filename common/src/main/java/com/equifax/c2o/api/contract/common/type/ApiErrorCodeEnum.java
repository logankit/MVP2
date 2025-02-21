package com.equifax.c2o.api.contract.common.type;

/**
 * Enumeration for API error codes
 */
public enum ApiErrorCodeEnum {
    // Common error codes
    VALIDATION_ERROR("VAL_001", "Validation Error"),
    SYSTEM_ERROR("SYS_001", "System Error"),
    NOT_FOUND("NF_001", "Resource Not Found"),
    UNAUTHORIZED("AUTH_001", "Unauthorized Access"),
    BAD_REQUEST("REQ_001", "Bad Request"),
    
    // FFID specific error codes
    FFID_INVALID("FFID_001", "Invalid FFID"),
    FFID_NOT_FOUND("FFID_002", "FFID Not Found"),
    FFID_EXPIRED("FFID_003", "FFID Expired"),
    FFID_PROCESSING_ERROR("FFID_004", "Error Processing FFID"),
    EFX_INTERCONNECT_API_ERROR("EFX_INTERCONNECT_API_ERROR", "Error occurred in EFX Interconnect API");

    private final String errorCode;
    private final String details;

    ApiErrorCodeEnum(String errorCode, String details) {
        this.errorCode = errorCode;
        this.details = details;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getDetails() {
        return details;
    }

    public static ApiErrorCodeEnum fromErrorCode(String errorCode) {
        for (ApiErrorCodeEnum error : ApiErrorCodeEnum.values()) {
            if (error.getErrorCode().equals(errorCode)) {
                return error;
            }
        }
        return null;
    }
}
