
package com.myplex.model;

import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class GeneralInfo {

    @SerializedName("_id")
    @Expose
    private String id;
    @SerializedName("type")
    @Expose
    private String type;
    @SerializedName("title")
    @Expose
    private String title;
    @SerializedName("category")
    @Expose
    private String category;
    @SerializedName("assetId")
    @Expose
    private String assetId;
    @SerializedName("isSellable")
    @Expose
    private Boolean isSellable;
    @SerializedName("isDownloadable")
    @Expose
    private Object isDownloadable;
    @SerializedName("videoAvailable")
    @Expose
    private Boolean videoAvailable;
    @SerializedName("briefDescription")
    @Expose
    private String briefDescription;
    @SerializedName("description")
    @Expose
    private String description;
    @SerializedName("altTitle")
    @Expose
    private List<AltTitle> altTitle = null;
    @SerializedName("altBriefDescription")
    @Expose
    private List<AltBriefDescription> altBriefDescription = null;
    @SerializedName("altDescription")
    @Expose
    private Object altDescription;
    @SerializedName("isDvr")
    @Expose
    private Boolean isDvr;
    @SerializedName("deepLink")
    @Expose
    private String deepLink;
    @SerializedName("deepLinkiOS")
    @Expose
    private String deepLinkiOS;
    @SerializedName("partnerId")
    @Expose
    private Object partnerId;
    @SerializedName("fairplayAssetId")
    @Expose
    private Object fairplayAssetId;
    @SerializedName("contentRights")
    @Expose
    private Object contentRights;
    @SerializedName("actionType")
    @Expose
    private String actionType;
    @SerializedName("actionURL")
    @Expose
    private String actionURL;
    @SerializedName("author")
    @Expose
    private String author;
    @SerializedName("displayStatistics")
    @Expose
    private Boolean displayStatistics;
    @SerializedName("accessLabel")
    @Expose
    private String accessLabel;
    @SerializedName("accessLabelImage")
    @Expose
    private String accessLabelImage;
    @SerializedName("showDisplayTabs")
    @Expose
    private ShowDisplayTabs showDisplayTabs;
    @SerializedName("cuePoints")
    @Expose
    private String cuePoints;
    @SerializedName("channelNumber")
    @Expose
    private String channelNumber;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getAssetId() {
        return assetId;
    }

    public void setAssetId(String assetId) {
        this.assetId = assetId;
    }

    public Boolean getIsSellable() {
        return isSellable;
    }

    public void setIsSellable(Boolean isSellable) {
        this.isSellable = isSellable;
    }

    public Object getIsDownloadable() {
        return isDownloadable;
    }

    public void setIsDownloadable(Object isDownloadable) {
        this.isDownloadable = isDownloadable;
    }

    public Boolean getVideoAvailable() {
        return videoAvailable;
    }

    public void setVideoAvailable(Boolean videoAvailable) {
        this.videoAvailable = videoAvailable;
    }

    public String getBriefDescription() {
        return briefDescription;
    }

    public void setBriefDescription(String briefDescription) {
        this.briefDescription = briefDescription;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<AltTitle> getAltTitle() {
        return altTitle;
    }

    public void setAltTitle(List<AltTitle> altTitle) {
        this.altTitle = altTitle;
    }

    public List<AltBriefDescription> getAltBriefDescription() {
        return altBriefDescription;
    }

    public void setAltBriefDescription(List<AltBriefDescription> altBriefDescription) {
        this.altBriefDescription = altBriefDescription;
    }

    public Object getAltDescription() {
        return altDescription;
    }

    public void setAltDescription(Object altDescription) {
        this.altDescription = altDescription;
    }

    public Boolean getIsDvr() {
        return isDvr;
    }

    public void setIsDvr(Boolean isDvr) {
        this.isDvr = isDvr;
    }

    public String getDeepLink() {
        return deepLink;
    }

    public void setDeepLink(String deepLink) {
        this.deepLink = deepLink;
    }

    public String getDeepLinkiOS() {
        return deepLinkiOS;
    }

    public void setDeepLinkiOS(String deepLinkiOS) {
        this.deepLinkiOS = deepLinkiOS;
    }

    public Object getPartnerId() {
        return partnerId;
    }

    public void setPartnerId(Object partnerId) {
        this.partnerId = partnerId;
    }

    public Object getFairplayAssetId() {
        return fairplayAssetId;
    }

    public void setFairplayAssetId(Object fairplayAssetId) {
        this.fairplayAssetId = fairplayAssetId;
    }

    public Object getContentRights() {
        return contentRights;
    }

    public void setContentRights(Object contentRights) {
        this.contentRights = contentRights;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public String getActionURL() {
        return actionURL;
    }

    public void setActionURL(String actionURL) {
        this.actionURL = actionURL;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Boolean getDisplayStatistics() {
        return displayStatistics;
    }

    public void setDisplayStatistics(Boolean displayStatistics) {
        this.displayStatistics = displayStatistics;
    }

    public String getAccessLabel() {
        return accessLabel;
    }

    public void setAccessLabel(String accessLabel) {
        this.accessLabel = accessLabel;
    }

    public String getAccessLabelImage() {
        return accessLabelImage;
    }

    public void setAccessLabelImage(String accessLabelImage) {
        this.accessLabelImage = accessLabelImage;
    }

    public ShowDisplayTabs getShowDisplayTabs() {
        return showDisplayTabs;
    }

    public void setShowDisplayTabs(ShowDisplayTabs showDisplayTabs) {
        this.showDisplayTabs = showDisplayTabs;
    }

    public String getCuePoints() {
        return cuePoints;
    }

    public void setCuePoints(String cuePoints) {
        this.cuePoints = cuePoints;
    }

    public String getChannelNumber() {
        return channelNumber;
    }

    public void setChannelNumber(String channelNumber) {
        this.channelNumber = channelNumber;
    }

}
