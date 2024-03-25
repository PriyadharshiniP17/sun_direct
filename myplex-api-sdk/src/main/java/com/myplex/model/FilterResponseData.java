package com.myplex.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

/**
 * Created by phani on 1/6/2016.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class FilterResponseData extends BaseResponseData implements Serializable {
    public FilterData results ;
   // public List<Languages>languages = new ArrayList<>();
   // public List<GenresData> genres = new ArrayList<>();
    public FilterResponseData(){

    }



}
