package com.myplex.model;


import java.io.Serializable;
import java.util.List;

public class PartnerDetailsResponse implements Serializable {
    public List<PartnerDetailItem> partnerDetails;

    @Override
    public String toString() {
        return partnerDetails.toString();
    }
}
