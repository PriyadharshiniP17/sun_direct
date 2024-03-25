package com.myplex.myplex.events;

import com.myplex.model.CardData;

/**
 * Created by apalya on 1/9/2017.
 */

public class EventSoftUpdateData {

    public CardData cardData;
    public CardData tvShowCardData;

    public EventSoftUpdateData(CardData cardData){
        this.cardData = cardData;
    }

    public EventSoftUpdateData(CardData cardData, String seasonData, CardData tvShowCardData){
        this.cardData = cardData;
        this.tvShowCardData = tvShowCardData;
    }
}

