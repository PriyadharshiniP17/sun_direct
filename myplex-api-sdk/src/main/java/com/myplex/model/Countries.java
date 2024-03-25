package com.myplex.model;

import java.io.Serializable;

/**
 * Created by apalya on 7/18/2017.
 */

public class Countries implements Serializable {

    public String code;
    public String name;
    public String dial_code;


    public Countries(String name,String code, String dial_code){
        this.code = code;
        this.name = name;
        this.dial_code = dial_code;
    }

}
