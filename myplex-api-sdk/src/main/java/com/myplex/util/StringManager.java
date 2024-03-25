package com.myplex.util;

import android.util.ArrayMap;

import java.util.Map;

public class StringManager {
    private static StringManager sInstance = null;
    private Map<String, String> mLanguageMap = new ArrayMap<>();
    private StringManager() {
    }
    public static StringManager getInstance() {
        if (sInstance == null) {
            synchronized (StringManager.class) {
                if (sInstance == null)
                    sInstance = new StringManager();
            }
        }
        return sInstance;
    }
    public  void addString(String key, String value) {
        mLanguageMap.put(key, value);
    }

    public String getString(String key) {
        return mLanguageMap.get(key);
    }
}
