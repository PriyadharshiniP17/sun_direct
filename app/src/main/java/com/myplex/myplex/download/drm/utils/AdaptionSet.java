package com.myplex.myplex.download.drm.utils;

import java.util.List;

/**
 * Created by Srikanth on 18-Aug-17.
 */

public class AdaptionSet {
    public static final int TYPE_VIDEO = 1;
    public static final int TYPE_AUDIO = 2;
    public static final int TYPE_OTHER = 3;

    public AdaptionSet() {
    }

    public int type;
    public List<RepresentationData> listRepresentations;
}
