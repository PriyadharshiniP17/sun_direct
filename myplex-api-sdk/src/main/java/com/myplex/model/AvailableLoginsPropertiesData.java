package com.myplex.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AvailableLoginsPropertiesData extends BaseResponseData {
    public LoginProperties properties = new LoginProperties();

    public AvailableLoginsPropertiesData() {
    }

    @Override
    public String toString() {
        return "properties- " + properties;
    }
}
