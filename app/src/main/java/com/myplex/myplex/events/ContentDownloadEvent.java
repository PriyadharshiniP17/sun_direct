package com.myplex.myplex.events;

import com.myplex.model.CardData;

/**
 * Created by apalya on 1/9/2017.
 */

public class ContentDownloadEvent {

    public CardData cardData;
    public CardData tvShowCardData;
    public String seasonName;
    public String downloadUrl;

    public ContentDownloadEvent(CardData cardData){
        this.cardData = cardData;
    }

    public ContentDownloadEvent(CardData cardData, String seasonData, CardData tvShowCardData){
        this.cardData = cardData;
        this.seasonName = seasonData;
        this.tvShowCardData = tvShowCardData;
    }
}

