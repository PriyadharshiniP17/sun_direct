package com.myplex.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SignupResponseData {
    public String status;
    public String message;
    public String request_id;
    public int code;
    public String mobile;
    public boolean display;
    public String userid;
    public String serviceName;
    public String email;
    public String response;
    public String web_url;
    public boolean isGDPREnabled;
    public String show_privacy_popup;
    @SerializedName("ui")
    @Expose
    private Ui ui;

    public SignupResponseData() {

    }

    @Override
    public String toString() {
        return "BaseResponseData: status- " + status + " code- " + code + " message- " + message + " userid- " + userid + " email- " + email + " response- " + response;
    }

    public Ui getUi() {
        return ui;
    }

    public void setUi(Ui ui) {
        this.ui = ui;
    }

    public class Ui {

        @SerializedName("action")
        @Expose
        private String action;
        @SerializedName("redirect")
        @Expose
        private String redirect;

        public String getAction() {
            return action;
        }

        public void setAction(String action) {
            this.action = action;
        }

        public String getRedirect() {
            return redirect;
        }

        public void setRedirect(String redirect) {
            this.redirect = redirect;
        }

    }
}
