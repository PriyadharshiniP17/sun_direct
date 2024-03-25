package com.myplex.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * Created by Apalya on 4/3/2019.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Sdbitrate implements Serializable{
    public Auto auto;
    public HD hd;
    public Medium medium;
    public Low low;
    public Sdbitrate(){

    }
}
