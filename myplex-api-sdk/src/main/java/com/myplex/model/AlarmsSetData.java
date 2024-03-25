package com.myplex.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AlarmsSetData implements Serializable {

    /**
     *
     */
    public List<AlarmData> results = new ArrayList<>();

    public AlarmsSetData() {

    }
}
