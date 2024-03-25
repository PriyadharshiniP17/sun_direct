package com.myplex.model;

import android.text.TextUtils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)

public class PromoAdData implements Serializable {
    public String id;
    public String frequency;
    public String htmlURL;

    @Override
    public String toString() {
        return "PromoAdData" +
                " id: " + id +
                " frequency- " + frequency +
                " htmlURL- " + htmlURL;
    }
}
