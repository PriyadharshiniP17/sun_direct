package com.myplex.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LocationInfo implements Serializable {
    public String country, state, postalCode, area, latitude,longitude, languageCode, languageName;

    public LocationInfo() {

    }


}