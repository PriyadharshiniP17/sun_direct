package com.myplex.myplex.ui;


import android.widget.MediaController;

public interface TrackChangeInterface extends MediaController.MediaPlayerControl {
    void changeQuality(float minBitRate, float maxBitRate, int rendererIndex);
    boolean isPlayingAd();
}
