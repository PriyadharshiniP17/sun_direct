package com.myplex.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AlarmData {
    public String title;
    public String startDate;
    public String _id;

    public AlarmData() {

    }

}
