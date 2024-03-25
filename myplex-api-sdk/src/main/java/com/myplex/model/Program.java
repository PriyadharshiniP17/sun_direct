
package com.myplex.model;

import static com.myplex.model.ApplicationConfig.XHDPI;

import android.media.Image;
import android.text.TextUtils;

import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.myplex.api.APIConstants;


public class Program {

    @SerializedName("_id")
    @Expose
    private String id;
    @SerializedName("_expiresAt")
    @Expose
    private String expiresAt;
    @SerializedName("_lastModifiedAt")
    @Expose
    private String lastModifiedAt;
    @SerializedName("title")
    @Expose
    private String title;
    @SerializedName("altTitle")
    @Expose
    private List<AltTitle> altTitle = null;
    @SerializedName("promotionInfo")
    @Expose
    private String promotionInfo;
    @SerializedName("programId")
    @Expose
    private String programId;
    @SerializedName("startDate")
    @Expose
    private String startDate;
    @SerializedName("endDate")
    @Expose
    private String endDate;
    @SerializedName("releaseDate")
    @Expose
    private String releaseDate;
    @SerializedName("globalServiceId")
    @Expose
    private String globalServiceId;
    @SerializedName("globalServiceName")
    @Expose
    private String globalServiceName;
    @SerializedName("displayStatistics")
    @Expose
    private Boolean displayStatistics;
    @SerializedName("nextProgram")
    @Expose
    private String nextProgram;
    @SerializedName("contentProvider")
    @Expose
    private String contentProvider;
    @SerializedName("generalInfo")
    @Expose
    private GeneralInfo generalInfo;
    @SerializedName("images")
    @Expose
    private Images images;
    @SerializedName("content")
    @Expose
    private Content content;
    @SerializedName("_type")
    @Expose
    private String type;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(String expiresAt) {
        this.expiresAt = expiresAt;
    }

    public String getLastModifiedAt() {
        return lastModifiedAt;
    }

    public void setLastModifiedAt(String lastModifiedAt) {
        this.lastModifiedAt = lastModifiedAt;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<AltTitle> getAltTitle() {
        return altTitle;
    }

    public void setAltTitle(List<AltTitle> altTitle) {
        this.altTitle = altTitle;
    }

    public String getPromotionInfo() {
        return promotionInfo;
    }

    public void setPromotionInfo(String promotionInfo) {
        this.promotionInfo = promotionInfo;
    }

    public String getProgramId() {
        return programId;
    }

    public void setProgramId(String programId) {
        this.programId = programId;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getGlobalServiceId() {
        return globalServiceId;
    }

    public void setGlobalServiceId(String globalServiceId) {
        this.globalServiceId = globalServiceId;
    }

    public String getGlobalServiceName() {
        return globalServiceName;
    }

    public void setGlobalServiceName(String globalServiceName) {
        this.globalServiceName = globalServiceName;
    }

    public Boolean getDisplayStatistics() {
        return displayStatistics;
    }

    public void setDisplayStatistics(Boolean displayStatistics) {
        this.displayStatistics = displayStatistics;
    }

    public String getNextProgram() {
        return nextProgram;
    }

    public void setNextProgram(String nextProgram) {
        this.nextProgram = nextProgram;
    }

    public String getContentProvider() {
        return contentProvider;
    }

    public void setContentProvider(String contentProvider) {
        this.contentProvider = contentProvider;
    }

    public GeneralInfo getGeneralInfo() {
        return generalInfo;
    }

    public void setGeneralInfo(GeneralInfo generalInfo) {
        this.generalInfo = generalInfo;
    }

    public Images getImages() {
        return images;
    }

    public void setImages(Images images) {
        this.images = images;
    }

    public Content getContent() {
        return content;
    }

    public void setContent(Content content) {
        this.content = content;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public final String getImageLink(String imageType) {
        if (images == null || images.getValues() == null || images.getValues().isEmpty()) {
            return null;
        }
        String[] imageTypes = new String[]{APIConstants.IMAGE_TYPE_THUMBNAIL};
        String profile = ApplicationConfig.MDPI;
        if (APIConstants.IMAGE_TYPE_PORTRAIT_COVERPOSTER.equalsIgnoreCase(imageType)) {
            imageTypes = new String[]{APIConstants.IMAGE_TYPE_PORTRAIT_COVERPOSTER, APIConstants.IMAGE_TYPE_COVERPOSTER};
            profile = XHDPI;
        }
        if (APIConstants.IMAGE_TYPE_COVERPOSTER.equalsIgnoreCase(imageType)) {
            imageTypes = new String[]{APIConstants.IMAGE_TYPE_COVERPOSTER, APIConstants.IMAGE_TYPE_BANNER, APIConstants.IMAGE_TYPE_PORTRAIT_COVERPOSTER};
        }
        if (APIConstants.IMAGE_TYPE_BANNER.equalsIgnoreCase(imageType)) {
            imageTypes = new String[]{APIConstants.IMAGE_TYPE_BANNER, APIConstants.IMAGE_TYPE_COVERPOSTER, APIConstants.IMAGE_TYPE_PORTRAIT_COVERPOSTER};
        }
       /* if (APIConstants.IMAGE_TYPE_THUMBNAIL.equalsIgnoreCase(imageType)) {
            if (!TextUtils.isEmpty(thumbnailImageLink)) {
                return thumbnailImageLink;
            }
        }*/
        for (String type : imageTypes) {
            for (Value imageItem : images.getValues()) {
                if (type.equalsIgnoreCase(imageItem.getType())
                        && profile.equalsIgnoreCase(imageItem.getProfile())) {
                    if (APIConstants.IMAGE_TYPE_PORTRAIT_COVERPOSTER.equalsIgnoreCase(imageType)) {
                        return imageItem.getLink();
                    }
                    if (APIConstants.IMAGE_TYPE_COVERPOSTER.equalsIgnoreCase(imageType)) {
                        return imageItem.getLink();
                    }
                    if (APIConstants.IMAGE_TYPE_BANNER.equalsIgnoreCase(imageType)) {
                        return imageItem.getLink();
                    }
                    if (APIConstants.IMAGE_TYPE_BANNER.equalsIgnoreCase(imageType)) {
                        return imageItem.getLink();
                    }


                    return imageItem.getLink();
                }
            }
        }
        return null;
    }

}
