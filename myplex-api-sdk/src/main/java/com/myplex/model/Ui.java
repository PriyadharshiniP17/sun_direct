
package com.myplex.model;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

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
