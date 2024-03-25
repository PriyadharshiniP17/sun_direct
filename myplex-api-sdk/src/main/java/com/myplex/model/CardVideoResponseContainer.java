package com.myplex.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;


@JsonIgnoreProperties(ignoreUnknown = true)
public class CardVideoResponseContainer extends BaseResponseData {
    public List<CardVideoResponseResult> results;
}
