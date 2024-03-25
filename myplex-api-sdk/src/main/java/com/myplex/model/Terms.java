package com.myplex.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

/**
 * Created by phani on 1/11/2016.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Terms implements Serializable {
    public String humanReadable;
    public String term;
    public boolean is_default;

}
