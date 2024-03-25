package com.myplex.myplex.utils;

import java.io.Serializable;
import java.util.ArrayList;


/**
 * Created by apalya on 11/28/2018.
 */

public class MOUUpdateRequestStorageList implements Serializable {
    public ArrayList<MOUUpdateRequestStorage> mDownloadedList = new ArrayList<MOUUpdateRequestStorage>();
    @Override
    public String toString() {
        if (mDownloadedList == null) {
            return "null list";

        }
        String objString = "\n size- " + mDownloadedList.size();
        int i = 1;
        for (int j =0;j<mDownloadedList.size();j++){
            objString = objString + "\n "+ i + ". " + mDownloadedList.get(j);
            i++;
        }
        return objString;
    }
}
