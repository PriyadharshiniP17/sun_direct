package com.myplex.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by phani on 1/6/2016.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Languages implements Serializable{
    public List<Terms> terms ;
  

}
