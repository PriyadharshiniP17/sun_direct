package com.myplex.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

/**
 * Created by Apalya on 05-Jan-16.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class OTTApp implements Serializable{
    public String title;
    public String description;
    public String offerDescription;
    public String androidAppUrl;
    public String iOSAppUrl;
    public String installType;
    public String confirmationMessage;
    public String installationHelp;
    public String iOSBundleId;
    public String androidPackageName;
    public String imageUrl;
    public int siblingOrder;
    public String contentType;
    public String appName;
    public CardDataOttImages images;
    public String _id;
    public String partnerContentId;
}
