package com.myplex.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

/**
 * Created by phani on 1/6/2016.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GenreFilterData extends BaseResponseData implements Serializable {
    public FilterResData results ;
   // public List<Languages>languages = new ArrayList<>();
   // public List<GenresData> genres = new ArrayList<>();
    public GenreFilterData(){

    }



}
