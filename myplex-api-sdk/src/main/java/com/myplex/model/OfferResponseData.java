package com.myplex.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OfferResponseData extends BaseResponseData {

    /**
     *
     */
    public List<CardDataPackages> results = new ArrayList<>();
    public CardDataPackagesUI ui;
    public Map<String, String> responseHeaders = Collections.emptyMap();
    public int mStartIndex = 1;
    public OfferResponseData(){

    }


    @Override
    public String toString() {
        return "OfferResponseData: results- " + results + "" +
                " ui- " + ui;
    }
}
