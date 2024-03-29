/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.android.exoplayer2.trackselection;

import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.source.TrackGroup;
import com.google.android.exoplayer2.source.chunk.MediaChunk;
import com.google.android.exoplayer2.upstream.BandwidthMeter;

import java.util.List;

/**
 * A bandwidth based adaptive {@link TrackSelection} for video, whose selected track is updated to
 * be the one of highest quality given the current network conditions and the state of the buffer.
 */
public class AdaptiveVideoTrackSelection extends BaseTrackSelection {

  private boolean isFirstTime = true;

  /**
   * Factory for {@link AdaptiveVideoTrackSelection} instances.
   */
  public static final class Factory implements TrackSelection.Factory {

    private final BandwidthMeter bandwidthMeter;
    private final int maxInitialBitrate;
    private final int minDurationForQualityIncreaseMs;
    private final int maxDurationForQualityDecreaseMs;
    private final int minDurationToRetainAfterDiscardMs;
    private final float bandwidthFraction;
    private final String savedTrackName;

    /**
     * @param bandwidthMeter Provides an estimate of the currently available bandwidth.
     * @param savedTrackName custom param for initializing track before playback starts.Custom param for client requirements.
     */
    public Factory(BandwidthMeter bandwidthMeter, String savedTrackName) {
      this (bandwidthMeter, DEFAULT_MAX_INITIAL_BITRATE,
          DEFAULT_MIN_DURATION_FOR_QUALITY_INCREASE_MS,
          DEFAULT_MAX_DURATION_FOR_QUALITY_DECREASE_MS,
          DEFAULT_MIN_DURATION_TO_RETAIN_AFTER_DISCARD_MS, DEFAULT_BANDWIDTH_FRACTION, savedTrackName);
    }
    public Factory(BandwidthMeter bandwidthMeter) {
            this (bandwidthMeter, DEFAULT_MAX_INITIAL_BITRATE,
                            DEFAULT_MIN_DURATION_FOR_QUALITY_INCREASE_MS,
                            DEFAULT_MAX_DURATION_FOR_QUALITY_DECREASE_MS,
                            DEFAULT_MIN_DURATION_TO_RETAIN_AFTER_DISCARD_MS, DEFAULT_BANDWIDTH_FRACTION,null);
          }


    /**
     * @param bandwidthMeter Provides an estimate of the currently available bandwidth.
     * @param maxInitialBitrate The maximum bitrate in bits per second that should be assumed
     *     when a bandwidth estimate is unavailable.
     * @param minDurationForQualityIncreaseMs The minimum duration of buffered data required for
*     the selected track to switch to one of higher quality.
     * @param maxDurationForQualityDecreaseMs The maximum duration of buffered data required for
*     the selected track to switch to one of lower quality.
     * @param minDurationToRetainAfterDiscardMs When switching to a track of significantly higher
*     quality, the selection may indicate that media already buffered at the lower quality can
*     be discarded to speed up the switch. This is the minimum duration of media that must be
*     retained at the lower quality.
     * @param bandwidthFraction The fraction of the available bandwidth that the selection should
*     consider available for use. Setting to a value less than 1 is recommended to account
     * @param savedTrackName
     */
    public Factory(BandwidthMeter bandwidthMeter, int maxInitialBitrate,
                   int minDurationForQualityIncreaseMs, int maxDurationForQualityDecreaseMs,
                   int minDurationToRetainAfterDiscardMs, float bandwidthFraction, String savedTrackName) {
      this.bandwidthMeter = bandwidthMeter;
      this.maxInitialBitrate = maxInitialBitrate;
      this.minDurationForQualityIncreaseMs = minDurationForQualityIncreaseMs;
      this.maxDurationForQualityDecreaseMs = maxDurationForQualityDecreaseMs;
      this.minDurationToRetainAfterDiscardMs = minDurationToRetainAfterDiscardMs;
      this.bandwidthFraction = bandwidthFraction;
      this.savedTrackName = savedTrackName;
    }

    @Override
    public AdaptiveVideoTrackSelection createTrackSelection(TrackGroup group, int... tracks) {
      return new AdaptiveVideoTrackSelection(group, tracks, bandwidthMeter, maxInitialBitrate,
          minDurationForQualityIncreaseMs, maxDurationForQualityDecreaseMs,
          minDurationToRetainAfterDiscardMs, bandwidthFraction, savedTrackName);
    }

  }

  public static final int DEFAULT_MAX_INITIAL_BITRATE = 8000000;
  public static final int DEFAULT_MIN_DURATION_FOR_QUALITY_INCREASE_MS = 10000;
  public static final int DEFAULT_MAX_DURATION_FOR_QUALITY_DECREASE_MS = 25000;
  public static final int DEFAULT_MIN_DURATION_TO_RETAIN_AFTER_DISCARD_MS = 25000;
  public static final float DEFAULT_BANDWIDTH_FRACTION = 0.75f;

  private final BandwidthMeter bandwidthMeter;
  private final int maxInitialBitrate;
  private final long minDurationForQualityIncreaseUs;
  private final long maxDurationForQualityDecreaseUs;
  private final long minDurationToRetainAfterDiscardUs;
  private final float bandwidthFraction;

  private int selectedIndex;
  private int reason;

  /**
   * @param group The {@link TrackGroup}. Must not be null.
   * @param tracks The indices of the selected tracks within the {@link TrackGroup}. Must not be
   *     null or empty. May be in any order.
   * @param bandwidthMeter Provides an estimate of the currently available bandwidth.
   */
  public AdaptiveVideoTrackSelection(TrackGroup group, int[] tracks,
                                     BandwidthMeter bandwidthMeter) {
    this (group, tracks, bandwidthMeter, DEFAULT_MAX_INITIAL_BITRATE,
        DEFAULT_MIN_DURATION_FOR_QUALITY_INCREASE_MS,
        DEFAULT_MAX_DURATION_FOR_QUALITY_DECREASE_MS,
        DEFAULT_MIN_DURATION_TO_RETAIN_AFTER_DISCARD_MS, DEFAULT_BANDWIDTH_FRACTION, null);
  }

  /**
   * @param group The {@link TrackGroup}. Must not be null.
   * @param tracks The indices of the selected tracks within the {@link TrackGroup}. Must not be
   *     null or empty. May be in any order.
   * @param bandwidthMeter Provides an estimate of the currently available bandwidth.
   * @param maxInitialBitrate The maximum bitrate in bits per second that should be assumed when a
*     bandwidth estimate is unavailable.
   * @param minDurationForQualityIncreaseMs The minimum duration of buffered data required for the
*     selected track to switch to one of higher quality.
   * @param maxDurationForQualityDecreaseMs The maximum duration of buffered data required for the
*     selected track to switch to one of lower quality.
   * @param minDurationToRetainAfterDiscardMs When switching to a track of significantly higher
*     quality, the selection may indicate that media already buffered at the lower quality can
*     be discarded to speed up the switch. This is the minimum duration of media that must be
*     retained at the lower quality.
   * @param bandwidthFraction The fraction of the available bandwidth that the selection should
*     consider available for use. Setting to a value less than 1 is recommended to account
   * @param savedTrackName
   */
  public AdaptiveVideoTrackSelection(TrackGroup group, int[] tracks, BandwidthMeter bandwidthMeter,
                                     int maxInitialBitrate, long minDurationForQualityIncreaseMs,
                                     long maxDurationForQualityDecreaseMs, long minDurationToRetainAfterDiscardMs,
                                     float bandwidthFraction, String savedTrackName) {
    super(group, tracks);
    this.bandwidthMeter = bandwidthMeter;
    this.maxInitialBitrate = maxInitialBitrate;
    this.minDurationForQualityIncreaseUs = minDurationForQualityIncreaseMs * 1000L;
    this.maxDurationForQualityDecreaseUs = maxDurationForQualityDecreaseMs * 1000L;
    this.minDurationToRetainAfterDiscardUs = minDurationToRetainAfterDiscardMs * 1000L;
    this.bandwidthFraction = bandwidthFraction;
    setUserChoiceQuality(savedTrackName);

    reason = C.SELECTION_REASON_INITIAL;
  }

  @Override
  public void updateSelectedTrack(long bufferedDurationUs) {
    long nowMs = SystemClock.elapsedRealtime();
    // Get the current and ideal selections.
    int currentSelectedIndex = selectedIndex;
    Format currentFormat = getSelectedFormat();
    int idealSelectedIndex = determineIdealSelectedIndex(nowMs);
    Format idealFormat = getFormat(idealSelectedIndex);
    // Assume we can switch to the ideal selection.
    selectedIndex = idealSelectedIndex;
    // Revert back to the current selection if conditions are not suitable for switching.
    if (currentFormat != null && !isBlacklisted(selectedIndex, nowMs)) {
      if (idealFormat.bitrate > currentFormat.bitrate
          && bufferedDurationUs < minDurationForQualityIncreaseUs) {
        // The ideal track is a higher quality, but we have insufficient buffer to safely switch
        // up. Defer switching up for now.
        selectedIndex = currentSelectedIndex;
      } else if (idealFormat.bitrate < currentFormat.bitrate
          && bufferedDurationUs >= maxDurationForQualityDecreaseUs) {
        // The ideal track is a lower quality, but we have sufficient buffer to defer switching
        // down for now.
        selectedIndex = currentSelectedIndex;
      }
    }
    // If we adapted, update the trigger.
    if (selectedIndex != currentSelectedIndex) {
      reason = C.SELECTION_REASON_ADAPTIVE;
    }
  }

  @Override
  public int getSelectedIndex() {
    return selectedIndex;
  }

  @Override
  public int getSelectionReason() {
    return reason;
  }

  @Override
  public Object getSelectionData() {
    return null;
  }

  @Override
  public int evaluateQueueSize(long playbackPositionUs, List<? extends MediaChunk> queue) {
    if (queue.isEmpty()) {
      return 0;
    }
    int queueSize = queue.size();
    long bufferedDurationUs = queue.get(queueSize - 1).endTimeUs - playbackPositionUs;
    if (bufferedDurationUs < minDurationToRetainAfterDiscardUs) {
      return queueSize;
    }
    int idealSelectedIndex = determineIdealSelectedIndex(SystemClock.elapsedRealtime());
    Format idealFormat = getFormat(idealSelectedIndex);
    // Discard from the first SD chunk beyond minDurationToRetainAfterDiscardUs whose resolution and
    // bitrate are both lower than the ideal track.
    for (int i = 0; i < queueSize; i++) {
      MediaChunk chunk = queue.get(i);
      long durationBeforeThisChunkUs = chunk.startTimeUs - playbackPositionUs;
      if (durationBeforeThisChunkUs >= minDurationToRetainAfterDiscardUs
          && chunk.trackFormat.bitrate < idealFormat.bitrate
          && chunk.trackFormat.height < idealFormat.height
          && chunk.trackFormat.height < 720 && chunk.trackFormat.width < 1280) {
        return i;
      }
    }
    return queueSize;
  }

  /**
   * Computes the ideal selected index ignoring buffer health.
   *
   * @param nowMs The current time in the timebase of {@link SystemClock#elapsedRealtime()}, or
   *     {@link Long#MIN_VALUE} to ignore blacklisting.
   */
  private int determineIdealSelectedIndex(long nowMs) {
    long bitrateEstimate = bandwidthMeter.getBitrateEstimate();
    long effectiveBitrate = bitrateEstimate == BandwidthMeter.NO_ESTIMATE
        ? maxInitialBitrate : (long) (bitrateEstimate * bandwidthFraction);
    if (isFirstTime) {
      isFirstTime = false;
      effectiveBitrate = 80000L;
    }
    int lowestBitrateNonBlacklistedIndex = 0;
    for (int i = 0; i < length; i++) {
      if (nowMs == Long.MIN_VALUE || !isBlacklisted(i, nowMs)) {
        Format format = getFormat(i);
        Log.d("Bitrates:", "format.bitrate- " + format.bitrate + " widthxheight- " + format.width + "x" + format.height + " id- " + format.id);
        if (format.bitrate <= effectiveBitrate) {
          return i;
        } else {
          lowestBitrateNonBlacklistedIndex = i;
        }
      }
    }
    return lowestBitrateNonBlacklistedIndex;
  }
  
  public void setUserChoiceQuality(String prefPlayBackQuality) {
    String sSavedQualityName = prefPlayBackQuality;

    if (TextUtils.isEmpty(sSavedQualityName)) {
      selectedIndex = indexOf(group.getFormat(0));
      int selectedIndex = determineIdealSelectedIndex(Long.MIN_VALUE);
      this.selectedIndex = selectedIndex;
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
          int selectedIndex = determineIdealSelectedIndex(Long.MIN_VALUE);
          this.selectedIndex = selectedIndex;
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
