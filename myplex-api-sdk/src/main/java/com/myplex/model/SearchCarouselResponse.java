package com.myplex.model;

import java.io.Serializable;
import java.util.List;

public class SearchCarouselResponse implements Serializable {
    public String enableBackKey;
    public int searchTimespan;
    public int characterLimit;
    public int recentSearchCountLimit;
    public List<SearchFilterCarouselResponse> carousel;
}