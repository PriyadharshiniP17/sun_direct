package com.myplex.myplex.events;

import com.myplex.model.CardData;

/**
 * Created by Samir on 12/12/2015.
 */
public class EventSearchMovieDataOnOTTApp {
    public static final int TYPE_FROM_SEARCH_DATA = -222;
    private CardData mMovieData;

    public String getSearchString() {
        return mSearchString;
    }

    private String mSearchString;
    public EventSearchMovieDataOnOTTApp(CardData movieData, String searchedString){
        mMovieData = movieData;
        mSearchString = searchedString;
    }
    public CardData getMovieData(){
        return mMovieData;
    }
}
