package com.myplex.analytics;

import java.util.Map;

/**
 * Created by Apalya on 2/2/2018.
 */

public class Event {
    private String eventAction;
    public String eventName;
    private String category;
    private String value;
    public Map<String, Object> params;
    public int priority;


    public Event(String eventAction, String eventName, String category, String value, Map<String, Object> params,int priority) {
        this.eventAction = eventAction;
        this.eventName = eventName;
        this.category = category;
        this.value = value;
        this.params = params;
        this.priority = priority;

    }


    public Event(String eventName, Map<String, Object> params, int priority) {
        this.eventName = eventName;
        this.params = params;
        this.priority = priority;
    }


    public boolean isValid() {
        return eventName != null;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("eventName- " + eventName).append(" priority- " + priority).toString();
    }
}
