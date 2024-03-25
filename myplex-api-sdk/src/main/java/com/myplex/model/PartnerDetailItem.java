package com.myplex.model;

import java.io.Serializable;
import java.util.List;

public class PartnerDetailItem implements Serializable{
    public String name;
    public String imageURL;
    public boolean subtitlesEnabled;
    public String episodePlaybackOrder;
    public String packageName;
    public String webViewCloseString;

    @Override
    public String toString() {
        return "PartnerDetailItem" +
                " name: " + name +
                " imageURL: " + imageURL +
                " subtitlesEnabled " + subtitlesEnabled;
    }
}
