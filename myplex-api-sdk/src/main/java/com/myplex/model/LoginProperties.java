package com.myplex.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Created by Srikanth on 20-03-2018.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoginProperties {
    public List<String> loginSupported;

    public LoginProperties() {
    }

    @Override
    public String toString() {
        return "loginSupported- " + loginSupported;
    }
}
