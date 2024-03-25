package com.myplex.model;

import java.io.Serializable;

public class PartnerDetailAppinAppItem implements Serializable{
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
