package com.equifax.c2o.api.contract.common.type.response.mirrorFfid;

import com.equifax.c2o.api.contract.common.type.ApiErrorCodeEnum;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import java.io.Serializable;
import java.util.List;

/**
 * Response class for Mirror FFID operations
 */
@JsonInclude(value = Include.NON_NULL)
public class MirrorFfidResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private String requestId;
    private String status;
    private String code;
    private String message;
    private List<MirrorFfidErrorResponse> errors;

    /**
     * Default constructor
     */
    public MirrorFfidResponse() {
        super();
    }

    /**
     * Constructor with all fields
     * 
     * @param requestId Request identifier
     * @param status Status of the request
     * @param errorCodeEnum Error code enumeration
     * @param message Error message
     * @param errors List of detailed error responses
     */
    public MirrorFfidResponse(String requestId, String status, ApiErrorCodeEnum errorCodeEnum, String message, List<MirrorFfidErrorResponse> errors) {
        super();
        this.requestId = requestId;
        this.status = status;
        this.code = errorCodeEnum.getErrorCode();
        this.message = message;
        this.errors = errors;
    }

    public List<MirrorFfidErrorResponse> getErrors() {
        return errors;
    }

    public void setErrors(List<MirrorFfidErrorResponse> errors) {
        this.errors = errors;
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

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "MirrorFfidResponse{" +
                "requestId='" + requestId + '\'' +
                ", status='" + status + '\'' +
                ", code='" + code + '\'' +
                ", message='" + message + '\'' +
                ", errors=" + errors +
                '}';
    }
}
