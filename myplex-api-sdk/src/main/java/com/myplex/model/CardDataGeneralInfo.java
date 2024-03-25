package com.myplex.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CardDataGeneralInfo implements Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = -1897942798104694998L;
	public String _id;
    public String type;
    public String title;
    public String category;
    public boolean isSellable;
    public String description;
    public String briefDescription;
    public String myplexDescription;
    public String studioDescription;
	public List<LanguageTitleData> altTitle;
    public List<LanguageDescriptionData> altDescription;
    public List<String> contentRights;
    public boolean videoAvailable;
    public boolean isDownloadable;
    public boolean displayStatistics;
    public String deepLink;
    public String partnerId;
    public boolean showWatermark;
    public boolean isDvr;
    public String accessLabelImage;
    public ShowDisplayTabs showDisplayTabs;

    public boolean getShowWatermark() {
        if (showWatermark) {
            return true;
        }
        return false;
    }

    public void setShowWatermark(boolean showWatermark) {
        this.showWatermark = showWatermark;
    }

    public CardDataGeneralInfo(){}
}
