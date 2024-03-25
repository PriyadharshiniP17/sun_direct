package com.myplex.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PreviewVideoConfig implements Serializable {

    public int AUTOPLAY_WAIT_TIME;
    public int POST_AUTO_PLAY_WAIT_TIME;
    public int AUTO_PLAY_VIDEO_TIME_OUT;
    public List<String> AUTOPLAY_NETWORK;
    public String AUTOPLAY_MUTE;
    public boolean PREVIEWS_ENABLE;
    public List<String> PREVIEWS_BLACKLIST_DEVICES;
    public String PREVIEWS_MIN_MEMORY;
    public String PREVIEWS_MIN_RESOLUTION_ANDROID;
    public String PREVIEW_DISABLE_ANDROID_OS;

}