
package com.myplex.model;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class UserSigninResponse {

    @SerializedName("code")
    @Expose
    private Integer code;
    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("message")
    @Expose
    private String message;
    @SerializedName("ui")
    @Expose
    private Ui ui;
    @SerializedName("userid")
    @Expose
    private String userid;

    @SerializedName("mobile_no")
    @Expose
    private String mobile_no;
    public String response;

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Ui getUi() {
        return ui;
    }

    public void setUi(Ui ui) {
        this.ui = ui;
    }

    public void setMobile_no(String mobile_no) {
        this.mobile_no = mobile_no;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getMobile_no() {
        return mobile_no;
    }

    public String getUserid() {
        return userid;
    }
}
