package com.equifax.c2o.api.contract.common.type;

import java.io.Serializable;

/**
 * Class representing information about who created/submitted a Mirror FFID request
 */
public class MirrorFfidCreatedByInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    private String userId;
    private String userName;
    private String email;
    private String department;
    private String role;

    public MirrorFfidCreatedByInfo() {
        super();
    }

    public MirrorFfidCreatedByInfo(String userId, String userName, String email, String department, String role) {
        this.userId = userId;
        this.userName = userName;
        this.email = email;
        this.department = department;
        this.role = role;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
