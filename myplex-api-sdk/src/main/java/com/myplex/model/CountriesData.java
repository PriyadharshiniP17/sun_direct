package com.myplex.model;

import java.io.Serializable;

public class CountriesData {
    public String indexCode;
    public String code;
    public String name;

    public CountriesData(String indexCode, String code, String name) {
        this.indexCode = indexCode;
        this.code = code;
        this.name = name;
    }
}
