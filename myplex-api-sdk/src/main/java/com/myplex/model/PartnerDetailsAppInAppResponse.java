package com.myplex.model;


import java.io.Serializable;
import java.util.List;

public class PartnerDetailsAppInAppResponse implements Serializable {
    public List<PartnerDetailAppinAppItem> partnerDetails;

    @Override
    public String toString() {
        return partnerDetails.toString();
    }
}
