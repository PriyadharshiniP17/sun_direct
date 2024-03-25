package com.myplex.paasanalytics;

import java.util.Map;

public class PaasAnalytics {
    private String eventAction;
    public String eventName;
    private String category;
    private String value;
    public Map<String, Object> params;
    public Map<String,String> stringParams;
    public int priority;
}
