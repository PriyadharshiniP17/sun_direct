package com.myplex.model;

import java.io.Serializable;

/**
 * Created by Srikanth on 02-Aug-16.
 */
public class EmailData implements Serializable{
    public String email;
    public boolean is_primary;
    public boolean is_verified;

    public EmailData(){

    }
}
