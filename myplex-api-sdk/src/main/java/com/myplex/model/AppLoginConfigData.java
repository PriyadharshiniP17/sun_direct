package com.myplex.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)

    /**
     *
     */
public class AppLoginConfigData implements Serializable {
    public String type;
    public String medium;
    public String msisdn;

    public AppLoginConfigData(){

    }

}
