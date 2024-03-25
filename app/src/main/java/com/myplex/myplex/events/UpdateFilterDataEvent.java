package com.myplex.myplex.events;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Srikanth on 29-Aug-17.
 */

public class UpdateFilterDataEvent {
    public HashMap<Integer, ArrayList<String>> filteredValues;

    public UpdateFilterDataEvent(HashMap<Integer, ArrayList<String>> filteredValues) {
        this.filteredValues = filteredValues;
    }
}
