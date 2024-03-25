
package com.myplex.model;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class Content {

    @SerializedName("language")
    @Expose
    private List<String> language = null;
    @SerializedName("duration")
    @Expose
    private String duration;
    @SerializedName("categoryName")
    @Expose
    private String categoryName;
    @SerializedName("categoryType")
    @Expose
    private String categoryType;
    @SerializedName("releaseDate")
    @Expose
    private String releaseDate;
    @SerializedName("parentIds")
    @Expose
    private ParentIds parentIds;
    @SerializedName("certifiedRatings")
    @Expose
    private CertifiedRatings certifiedRatings;
    @SerializedName("drmEnabled")
    @Expose
    private Boolean drmEnabled;
    @SerializedName("isMpegDash")
    @Expose
    private Boolean isMpegDash;
    @SerializedName("isChromeCastEnabled")
    @Expose
    private Boolean isChromeCastEnabled;
    @SerializedName("contentRating")
    @Expose
    private String contentRating;
    @SerializedName("adType")
    @Expose
    private String adType;
    @SerializedName("adProvider")
    @Expose
    private String adProvider;
    @SerializedName("adConfig")
    @Expose
    private AdConfig adConfig;
    @SerializedName("adEnabled")
    @Expose
    private Boolean adEnabled;
    @SerializedName("drmType")
    @Expose
    private String drmType;
    @SerializedName("is3d")
    @Expose
    private Boolean is3d;
    @SerializedName("genre")
    @Expose
    private List<Object> genre = null;
    @SerializedName("siblingOrder")
    @Expose
    private Integer siblingOrder;
    @SerializedName("serialNo")
    @Expose
    private String serialNo;
    @SerializedName("actionType")
    @Expose
    private String actionType;
    @SerializedName("actionURL")
    @Expose
    private String actionURL;
    @SerializedName("author")
    @Expose
    private String author;
    @SerializedName("seoTitle")
    @Expose
    private String seoTitle;
    @SerializedName("seoDescription")
    @Expose
    private Object seoDescription;
    @SerializedName("seoKeywords")
    @Expose
    private String seoKeywords;
    @SerializedName("showDisplayTabs")
    @Expose
    private ShowDisplayTabs showDisplayTabs;
    @SerializedName("cuePoints")
    @Expose
    private String cuePoints;
    @SerializedName("channelNumber")
    @Expose
    private String channelNumber;

    public List<String> getLanguage() {
        return language;
    }

    public void setLanguage(List<String> language) {
        this.language = language;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getCategoryType() {
        return categoryType;
    }

    public void setCategoryType(String categoryType) {
        this.categoryType = categoryType;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public ParentIds getParentIds() {
        return parentIds;
    }

    public void setParentIds(ParentIds parentIds) {
        this.parentIds = parentIds;
    }

    public CertifiedRatings getCertifiedRatings() {
        return certifiedRatings;
    }

    public void setCertifiedRatings(CertifiedRatings certifiedRatings) {
        this.certifiedRatings = certifiedRatings;
    }

    public Boolean getDrmEnabled() {
        return drmEnabled;
    }

    public void setDrmEnabled(Boolean drmEnabled) {
        this.drmEnabled = drmEnabled;
    }

    public Boolean getIsMpegDash() {
        return isMpegDash;
    }

    public void setIsMpegDash(Boolean isMpegDash) {
        this.isMpegDash = isMpegDash;
    }

    public Boolean getIsChromeCastEnabled() {
        return isChromeCastEnabled;
    }

    public void setIsChromeCastEnabled(Boolean isChromeCastEnabled) {
        this.isChromeCastEnabled = isChromeCastEnabled;
    }

    public String getContentRating() {
        return contentRating;
    }

    public void setContentRating(String contentRating) {
        this.contentRating = contentRating;
    }

    public String getAdType() {
        return adType;
    }

    public void setAdType(String adType) {
        this.adType = adType;
    }

    public String getAdProvider() {
        return adProvider;
    }

    public void setAdProvider(String adProvider) {
        this.adProvider = adProvider;
    }

    public AdConfig getAdConfig() {
        return adConfig;
    }

    public void setAdConfig(AdConfig adConfig) {
        this.adConfig = adConfig;
    }

    public Boolean getAdEnabled() {
        return adEnabled;
    }

    public void setAdEnabled(Boolean adEnabled) {
        this.adEnabled = adEnabled;
    }

    public String getDrmType() {
        return drmType;
    }

    public void setDrmType(String drmType) {
        this.drmType = drmType;
    }

    public Boolean getIs3d() {
        return is3d;
    }

    public void setIs3d(Boolean is3d) {
        this.is3d = is3d;
    }

    public List<Object> getGenre() {
        return genre;
    }

    public void setGenre(List<Object> genre) {
        this.genre = genre;
    }

    public Integer getSiblingOrder() {
        return siblingOrder;
    }

    public void setSiblingOrder(Integer siblingOrder) {
        this.siblingOrder = siblingOrder;
    }

    public String getSerialNo() {
        return serialNo;
    }

    public void setSerialNo(String serialNo) {
        this.serialNo = serialNo;
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

    public String getSeoTitle() {
        return seoTitle;
    }

    public void setSeoTitle(String seoTitle) {
        this.seoTitle = seoTitle;
    }

    public Object getSeoDescription() {
        return seoDescription;
    }

    public void setSeoDescription(Object seoDescription) {
        this.seoDescription = seoDescription;
    }

    public String getSeoKeywords() {
        return seoKeywords;
    }

    public void setSeoKeywords(String seoKeywords) {
        this.seoKeywords = seoKeywords;
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
