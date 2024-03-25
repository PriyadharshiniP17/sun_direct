package com.myplex.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CardDataTagsItem implements Serializable {

    public String category;
    public String qualifier;
    public Long weight;
    public String genre;
    public String id;
    public String name;

    public CardDataTagsItem() {
    }
}
