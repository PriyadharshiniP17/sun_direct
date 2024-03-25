package com.myplex.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

/**
 * Created by Srikanth on 21-Oct-16.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class VstbSessionItem implements Serializable {
    /**
     *
     */
    public String status;
    public String message;
    public String sessionToken;
    public String HMAC;
    public String sessionExpiryTime;
    public int elapsedTime;

    public VstbSessionItem() {

    }
}