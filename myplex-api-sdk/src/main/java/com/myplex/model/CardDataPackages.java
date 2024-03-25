package com.myplex.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CardDataPackages implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = -2077770756068004273L;
    public List<CardDataPackagePriceDetailsItem> priceDetails;
    public String contentType;
    public boolean couponFlag;
    public String contentId;
    public String packageName;
    public String displayName;
    public List<CardDataPromotionDetailsItem> promotionDetails;
    public String bbDescription;
    public String cpDescripton;
    public String packageId;
    public String duration;
    public String commercialModel;
    public boolean packageIndicator;
    public boolean renewalFlag;
    public String validityPeriod;
    public boolean operatorPriority;
    public boolean subscribed;
    public boolean unsubscription;
    public String actionButtonText;
    public boolean autoSubscribe;
    public String packageType;
    public String cpDescriptionV2;
    public String actionButtonTextV2;
    public String unsubdescription;
    public String validityEndDate;
    public String orderId;
    public String subscriptionType;
    public String currencyCode;
    public String priceCharged;
    public String packagePrice;
    public String packDataValue;
    public String validityStartDate;
    public String label;
    public String consumptionType;
    public String downloadType;
    public String remainingTime;
    public String packValidatyPeriod;
    public String packDuration;
    public String showMypack;
    public String packType;
    public String country;
    public String paymentMode;


    public CardDataPackages() {

    }

    @Override
    public String toString() {
        return "packageName- " + packageName + " packageId- " + packageId + " displayName- " + displayName + " bbDescription- " + bbDescription + " validityEndDate- " + validityEndDate +"" +
                " subscribed- " + subscribed + " cpDescriptionV2- " + cpDescriptionV2 + " actionButtonTextV2- " + actionButtonTextV2 + " unsubdescription- " + unsubdescription;
    }
}
