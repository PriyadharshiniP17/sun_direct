package com.myplex.myplex.ui.fragment.dummy;

import com.myplex.model.CardData;
import com.myplex.model.CardDataPackages;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class DetailsViewContent {
    public static final int CARDDETAIL_BRIEF_DESCRIPTION = 0;
    public static final int CARDDETAIL_TITLE_SECTION_VIEW = 1;
    public static final int CARDDETAIL_PACKAGES_VIEW = 2;
    public static final int CARDDETAIL_SIMILAR_CONTENT_VIEW_CAROUSEL = 3;
    public static final int CARDDETAIL_SIMILAR_CONTENT_VIEW_EPG = 4;
    public static final int CARDDETAIL_SIMILAR_CONTENT_VIEW_VOD = 5;
    public static final int CARDDETAIL_EPG_DROPDOWN_VIEW = 6;
    public static final int CARDDETAIL_EPG_VIEW = 7;
    public static final int CARDDETAIL_SEASON_DROPDOWN_VIEW = 8;
    public static final int CARDDETAIL_EPISODES_VIEW = 9;
    public static final int CARDDETAIL_FOOTER_LOADING_VIEW = 10;
    public static final int CARDDETAIL_ERROR_MESSAGE_VIEW = 11;
    public static final int CARDDETAIL_PLAYER_LOGS_VIEW = 12;
    public static final int CARDDETAIL_PLAYER_LOGS_TITLE_VIEW = 13;
    public static final int CARDDETAIL_RELATED_MEDIA_VIEW=14;
    public static final int CARDDETAIL_SEASONS_TABS_VIEW=15;
    public static final int CARDDETAIL_DUMMY_VIEW=16;
    public static final int CARDDETAIL_RECOMMENDED_FOR_YOU_VIEW=17;
    /**
     * An array of sample (dummy) items.
     */
    public static final List<DetailsViewDataItem> ITEMS = new ArrayList<DetailsViewDataItem>();

    /**
     * A map of sample (dummy) items, by ID.
     */
    public static final Map<String, DetailsViewDataItem> ITEM_MAP = new HashMap<String, DetailsViewDataItem>();

    private static final int COUNT = 25;

    static {
        // Add some sample items.
        for (int i = 1; i <= COUNT; i++) {
            addItem(createDummyItem());
        }
    }

    private static void addItem(DetailsViewDataItem item) {
        ITEMS.add(item);
    }

    private static DetailsViewDataItem createDummyItem() {
        return new DetailsViewDataItem(CARDDETAIL_BRIEF_DESCRIPTION, CardData.DUMMY_LIST.get(0));
    }

    private static String makeDetails(int position) {
        StringBuilder builder = new StringBuilder();
        builder.append("Details about Item: ").append(position);
        for (int i = 0; i < position; i++) {
            builder.append("\nMore details information here.");
        }
        return builder.toString();
    }

    /**
     * A dummy item representing a piece of content.
     */
    public static class DetailsViewDataItem {
        public String tabName;
        public String title;
        public String vernacularTitle;
        public CardData cardData;
        public CardData tvshowCardData;
        public int layoutType;
        public String isSubscribed;
        public String latestEpisodeText;
        public String mBgColor;
        public CardDataPackages cardDataPackageItem;
        public String carouselLayoutType;
        public boolean isToShowWatchLatestEpisodeButton;
        public boolean isToShowEpisodesLayout;
        public List<CardData> seasonsList;

        public DetailsViewDataItem(int layoutType, CardData cardData) {
            this.layoutType = layoutType;
            this.cardData = cardData;
        }

        public DetailsViewDataItem(int layoutType, CardData cardData,String mBgColor,String carouselLayoutType) {
            this.layoutType = layoutType;
            this.cardData = cardData;
            this.mBgColor=mBgColor;
            this.carouselLayoutType=carouselLayoutType;
        }

        public DetailsViewDataItem(int layoutType, CardData cardData, CardDataPackages cardDataPackageItem) {
            this.layoutType = layoutType;
            this.cardData = cardData;
            this.cardDataPackageItem = cardDataPackageItem;
        }

        public DetailsViewDataItem(int layoutType, CardData cardData, String title, String vernacularTitle,String mBgColor,boolean isToShowEpisodesLayout) {
            this.layoutType = layoutType;
            this.cardData = cardData;
            this.title = title;
            this.mBgColor=mBgColor;
            this.isToShowEpisodesLayout=isToShowEpisodesLayout;
            this.vernacularTitle = vernacularTitle;
        }

        public DetailsViewDataItem(int layoutType, CardData cardData, String title, String vernacularTitle,String mBgColor) {
            this.layoutType = layoutType;
            this.cardData = cardData;
            this.title = title;
            this.mBgColor=mBgColor;
            this.vernacularTitle = vernacularTitle;
        }

        public DetailsViewDataItem(int layoutType, CardData cardData, String title, String vernacularTitle,String mBgColor,List<CardData> seasonsList) {
            this.layoutType = layoutType;
            this.cardData = cardData;
            this.title = title;
            this.mBgColor=mBgColor;
            this.seasonsList=seasonsList;
            this.vernacularTitle = vernacularTitle;
        }

        public DetailsViewDataItem(int layoutType, CardData cardData, CardData tvshowCardData) {
            this.layoutType = layoutType;
            this.cardData = cardData;
            this.tvshowCardData = tvshowCardData;
        }

        public DetailsViewDataItem(int layoutType, CardData cardData, CardData tvshowCardData, String tabName,String isSubscribed,String mBgColor,
                                   String latestEpisodeText,boolean isToShowWatchLatestEpisodeButton) {
            this.layoutType = layoutType;
            this.cardData = cardData;
            this.tvshowCardData = tvshowCardData;
            this.tabName = tabName;
            this.latestEpisodeText=latestEpisodeText;
            this.isSubscribed=isSubscribed;
            this.mBgColor=mBgColor;
            this.isToShowWatchLatestEpisodeButton=isToShowWatchLatestEpisodeButton;
        }

        @Override
        public String toString() {
            return "layoutType::" + layoutType + " CardData::" + cardData + " title::" + title ;
        }
    }
}
