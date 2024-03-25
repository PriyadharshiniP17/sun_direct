package com.myplex.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Apalya on 9/19/2014.
 */
@JsonIgnoreProperties(ignoreUnknown = true)

public class Word implements Serializable {
    public String text;
    public String lan;
    public ArrayList<Word> localString = new ArrayList<Word>();
}
