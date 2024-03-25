package com.myplex.model;

import java.io.Serializable;
import java.util.List;

public class PreferredLanguageData extends BaseResponseData implements Serializable {
    public List<PreferredLanguages> languages;

    @Override
    public String toString() {
        return "languages " + languages;
    }
}
