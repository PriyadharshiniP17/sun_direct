package com.myplex.myplex.events;

import com.myplex.model.FilterData;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by phani on 1/7/2016.
 */
public class FilterDataUpdateEvent {
    public  List<FilterData> mFilterGroupList;
    private ArrayList<String>genreList;
    private ArrayList<String>langList;
    public FilterDataUpdateEvent(List<FilterData> mFilterGroupList){
        this.mFilterGroupList = mFilterGroupList;
    }

    public List<FilterData>  getFilterList(){
        return mFilterGroupList;
    }
    public ArrayList<String> getGenreList(){
        return genreList;
    }

}
