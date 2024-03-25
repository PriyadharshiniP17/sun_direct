package com.myplex.myplex.events;

/**
 * Created by Samir on 12/12/2015.
 */
public class ChangeMenuVisibility {
    private boolean makeMenuVisible;
    private int sectionType;
    public ChangeMenuVisibility(boolean makeMenuVisible,int sectionType){
        this.makeMenuVisible = makeMenuVisible;
        this.sectionType = sectionType;
    }

    public boolean isMakeMenuVisible(){
        return makeMenuVisible;
    }

    public int getSectionType(){
        return sectionType;
    }

}
