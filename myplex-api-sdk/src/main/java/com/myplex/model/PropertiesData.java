package com.myplex.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.List;

/**
 * Created by phani on 2/2/2016.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PropertiesData extends BaseResponseData implements Serializable{
    public PropertiesRes properties;
    public List<CommonParams> commonParams;

   // public PropertiesRes properties ;
    public PropertiesData(){
        
    }


}
