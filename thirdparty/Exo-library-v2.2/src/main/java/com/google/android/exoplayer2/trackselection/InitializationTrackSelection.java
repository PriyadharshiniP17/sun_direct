package com.google.android.exoplayer2.trackselection;  // Private classes.

import android.os.SystemClock;
import android.text.TextUtils;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.source.TrackGroup;

/**
 * A {@link TrackSelection} to use for initialization.
 */
public final class InitializationTrackSelection extends BaseTrackSelection {

    private int selectedIndex;

    public InitializationTrackSelection(TrackGroup group, int[] tracks, String prefPlayBackQuality) {
        super(group, tracks);
        setUserChoiceQuality(prefPlayBackQuality);
    }

    @Override
    public void updateSelectedTrack(long bufferedDurationUs) {
        long nowMs = SystemClock.elapsedRealtime();
        if (!isBlacklisted(selectedIndex, nowMs)) {
            return;
        }
        // Try from lowest bitrate to highest.
        for (int i = length - 1; i >= 0; i--) {
            if (!isBlacklisted(i, nowMs)) {
                selectedIndex = i;
                return;
            }
        }
        // Should never happen.
        throw new IllegalStateException();
    }

    @Override
    public int getSelectedIndex() {
        return selectedIndex;
    }

    @Override
    public int getSelectionReason() {
        return C.SELECTION_REASON_UNKNOWN;
    }

    @Override
    public Object getSelectionData() {
        return null;
    }

    public void setUserChoiceQuality(String prefPlayBackQuality) {
        String sSavedQualityName = prefPlayBackQuality;

        if (TextUtils.isEmpty(sSavedQualityName)) {
            selectedIndex = indexOf(group.getFormat(0));
            return;
        }
        try {
            switch (sSavedQualityName.toLowerCase()) {
                case "low":
                    selectTrackIndexAtMax(200);
                    break;
                case "medium":
                    selectTrackIndexAtMax(480);
                    break;
                case "high":
                    selectTrackIndexAtMin(480);
                    break;
                default:
                    selectedIndex = indexOf(group.getFormat(0));
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void selectTrackIndexAtMin(int minBitrate) {
        Format mediaFormat;
        float floatBitrate = 0f;
        for (int position = 0; position < length; position++) {
            mediaFormat = getFormat(position);
            floatBitrate = mediaFormat.bitrate / 1000000f;
            floatBitrate = floatBitrate * 1024;
//              LoggerD.debugExoVideoViewResizable("SaveTrackList: bitrate- " + floatBitrate + " i- " + position);
            if (floatBitrate > minBitrate) {
                selectedIndex = position;
            }
        }
    }

    private void selectTrackIndexAtMax(int maxBitrate) {
        Format mediaFormat;
        float floatBitrate = 0f;
        for (int position = 0; position < length; position++) {
            mediaFormat = getFormat(position);
            floatBitrate = mediaFormat.bitrate / 1000000f;
            floatBitrate = floatBitrate * 1024;
//              LoggerD.debugExoVideoViewResizable("SaveTrackList: bitrate- " + floatBitrate + " i- " + position);
            if (floatBitrate < maxBitrate) {
                selectedIndex = position;
            }
        }
    }


}