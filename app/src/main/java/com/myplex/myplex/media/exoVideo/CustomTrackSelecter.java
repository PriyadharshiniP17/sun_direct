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
package com.myplex.myplex.media.exoVideo;

import static com.myplex.api.APIConstants.TYPE_LIVE;
import static com.myplex.api.APIConstants.TYPE_PROGRAM;
import static com.myplex.api.APIConstants.VIDEO_QUALITY_AUTO;
import static com.myplex.api.APIConstants.VIDEO_QUALITY_HD;
import static com.myplex.api.APIConstants.VIDEO_QUALITY_LOW;
import static com.myplex.api.APIConstants.VIDEO_QUALITY_MEDIUM;
import static com.myplex.util.PropertiesHandler.BITRATE_CAP;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.pedrovgs.LoggerD;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.source.TrackGroup;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector;
import com.myplex.api.APIConstants;
import com.myplex.model.ApplicationConfig;
import com.myplex.model.CardData;
import com.myplex.myplex.media.MediaController2;
import com.myplex.myplex.ui.TrackChangeInterface;
import com.myplex.myplex.ui.adapter.AudioTrackAdapter;
import com.myplex.player_sdk.MyplexPlayer;
import com.myplex.util.AlertDialogUtil;
import com.myplex.util.PrefUtils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * Helper class for displaying track selection dialogs.
 */
public class CustomTrackSelecter {

    private static final int NONE = -2;
    private static String sSavedQualityName;
    private static String selectedQuality;
    private final Context mContext;
    private final MediaController2 mMediaController;
    private MyplexVideoViewPlayer mVideoViewPlayer;
    private TrackChangeInterface control;
    private CardData mCardData;
    private String contentType;
    private  String trackName ;
    private boolean isCDNTypeAzure;

    public static String getSavedQualityName() {
        return sSavedQualityName;
    }

    private int mSelectedPosition;
    private boolean isToolbarShown;
    private AlertDialog mDialog;
    private AlertDialog.Builder mBuilder;
    private List<TrackData> mTrackDataList;

    private final MappingTrackSelector selector;

    private MappingTrackSelector.MappedTrackInfo trackInfo;
    private TrackGroupArray trackGroups;


    private View.OnClickListener mItemClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            int position = 0;
            if (view.getTag() instanceof Integer) {
                position = (int) view.getTag();
            }
            dismiss();

            float minHDRate;
            float maxHDRate;
            boolean isContentHD = isContentQualityHd();

          //  String trackName;
            int trackPosition;

            switch (position) {
                case 1:
                    minHDRate = (float) getMaxMinBitRate(VIDEO_QUALITY_LOW, isContentHD, false);
                    maxHDRate = (float) getMaxMinBitRate(VIDEO_QUALITY_LOW, isContentHD, true);
                    trackName = PrefUtils.getInstance().getplayerControlsBitratesLow();
                    mMediaController.setText(trackName);
                    trackPosition = 1;
                    break;
                case 2:
                    minHDRate = (float) getMaxMinBitRate(VIDEO_QUALITY_MEDIUM, isContentHD, false);
                    maxHDRate = (float) getMaxMinBitRate(VIDEO_QUALITY_MEDIUM, isContentHD, true);
                    trackName = PrefUtils.getInstance().getplayerControlsBitratesMedium();
                    mMediaController.setText(trackName);
                    trackPosition = 2;
                    break;
                case 3:
                    minHDRate = (float) getMaxMinBitRate(VIDEO_QUALITY_HD, isContentHD, false);
                    maxHDRate = (float) getMaxMinBitRate(VIDEO_QUALITY_HD, isContentHD, true);
                    trackName = PrefUtils.getInstance().getplayerControlsBitratesHigh();
                    mMediaController.setText(trackName);
                    trackPosition = 3;
                    break;
                case 0:
                default:
                    minHDRate = (float) getMaxMinBitRate(VIDEO_QUALITY_AUTO, isContentHD, false);
                    maxHDRate = (float) getMaxMinBitRate(VIDEO_QUALITY_AUTO, isContentHD, true);
                    trackPosition = 0;
                    trackName = PrefUtils.getInstance().getplayerControlsBitratesAuto();
                    mMediaController.setText(trackName);
                    break;
            }

            if (!TextUtils.isEmpty(trackName)) {
                String msg = "Switching to " + trackName + " quality";
               // PrefUtils.getInstance().setContentVideoQuality(trackName);
                //if (!"Auto".equalsIgnoreCase(trackName)) {
                    AlertDialogUtil.showToastNotification(msg);
                //}
            }
            if (trackPosition == NONE) {
                LoggerD.debugExoVideoViewResizable("c: onItemClick: player not exists");
                return;
            }

            if (mVideoViewPlayer == null) {
                return;
            }
            sSavedQualityName = trackName;
            mSelectedPosition = position;
            PrefUtils.getInstance().setPrefPlayBackQuality(trackName);
            mVideoViewPlayer.onChangeQuality(minHDRate, maxHDRate, MyplexPlayer.VIDEO_TRACK_RENDERER);
            /*int position = 0;
            if (view.getTag() instanceof Integer) {
                position = (int) view.getTag();
            }
            dismiss();

            if (mTrackDataList == null
                    || mTrackDataList.isEmpty()) {
                LoggerD.debugExoVideoViewResizable("showQualitySelectionGrid: onItemClick: player not exists");
                return;
            }

            TrackData track = mTrackDataList.get(position);
            if (!TextUtils.isEmpty(track.name)) {
                String msg = "Switching to " + track.name + " quality";
                if (!"Auto".equalsIgnoreCase(track.name)) {
                    msg = "Switching to " + track.name + " bitrate";
                } else {
                    msg = "Switching to Auto";
                }
                AlertDialogUtil.showToastNotification(msg);
            }
            if (track.position == NONE) {
                LoggerD.debugExoVideoViewResizable("showQualitySelectionGrid: onItemClick: player not exists");
                return;
            }

            if (mVideoViewPlayer == null) {
                return;
            }
            sSavedQualityName = track.name;
            mSelectedPosition = position;
            PrefUtils.getInstance().setPrefPlayBackQuality(track.name);
            if (isHungama) {
                PrefUtils.getInstance().setPrefHungamaPlayBackQuality(track.name);
                mVideoViewPlayer.setSelectedTrack(track, mSelectedPosition);
                PrefUtils.getInstance().setPrefPlayBackQualityPos(mSelectedPosition);
            } else {
                PrefUtils.getInstance().setPrefPlayBackQuality(track.name);
                mVideoViewPlayer.setSelectedTrack(track);
            }*/
        }
    };
    private int trackRendererGroupIndex;

    public CustomTrackSelecter(Context context, MediaController2 mediaController2,
                               MyplexVideoViewPlayer mVideoViewPlayer) {
        this.mContext = context;
        this.mMediaController = mediaController2;
        this.mVideoViewPlayer = mVideoViewPlayer;
        selector = null;
        LoggerD.debugLog("mSelectedPosition- " + mSelectedPosition);
    }


    public CustomTrackSelecter(Context context, MediaController2 mediaController2,
                               MyplexVideoViewPlayer mVideoViewPlayer,
                               MappingTrackSelector selector) {
        this.mContext = context;
        this.mMediaController = mediaController2;
        this.mVideoViewPlayer = mVideoViewPlayer;
        this.selector = selector;
        LoggerD.debugLog("mSelectedPosition- " + mSelectedPosition);
    }

    /*public void showQualitySelectionGrid() {
        View grid = LayoutInflater.from(mContext).inflate(R.layout.quality_selection_grid, null);
        LinearLayout parent = (LinearLayout) grid;
        mTrackDataList = mVideoViewPlayer.getBitrateCappingTracks();
        int availablePosition = getUserChoiceQuality();
        if (mTrackDataList == null || mTrackDataList.isEmpty()) {
            TrackData track = new TrackData();
            track.bitrate = -1;
            track.name = "Auto";
            track.position = NONE;
        }
        if ((mDialog != null && mDialog.isShowing())
                || mTrackDataList == null) {
            return;
        }
//        if(mBuilder == null){
        mBuilder = new AlertDialog.Builder(mContext);
        mBuilder.setView(grid);
        mDialog = mBuilder.create();
//        }

        LinearLayout rowLinearLayout = null;
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        for (int i = 0; i < mTrackDataList.size(); i++) {

            if (i % 2 == 0) {
                rowLinearLayout = new LinearLayout(mContext);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.bottomMargin = (int) mContext.getResources().getDimension(R.dimen.margin_gap_16);
                params.gravity = Gravity.CENTER;
                rowLinearLayout.setLayoutParams(params);
                rowLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
                parent.addView(rowLinearLayout);
            }
            TrackData track = mTrackDataList.get(i);
            View quality_grid = inflater.inflate(R.layout.player_grid_child, null);
            TextView textView = (TextView) quality_grid.findViewById(R.id.text1);
            textView.setText(track.name);
            if (sSavedQualityName == null
                    || availablePosition == -1) {
                if ("auto".equalsIgnoreCase(track.name)) {
                    textView.setBackgroundResource(R.drawable.quality_button_selected);
                } else {
                    textView.setBackgroundResource(R.drawable.quality_button_default);
                }
            } else {
                if (sSavedQualityName != null
                        && track.name.equalsIgnoreCase(sSavedQualityName)) {
                    textView.setBackgroundResource(R.drawable.quality_button_selected);
                } else {
                    textView.setBackgroundResource(R.drawable.quality_button_default);
                }
            }

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.leftMargin = (int) mContext.getResources().getDimension(R.dimen.margin_gap_16);
            quality_grid.setLayoutParams(params);
            quality_grid.setTag(i);
            rowLinearLayout.addView(quality_grid);
            quality_grid.setOnClickListener(mItemClickListener);
        }

        mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mDialog.getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);

        int gridHeight = getViewHeight(grid);
        int statusBarHeight = getStatusBarHeight(mContext);

        if (mMediaController != null && mMediaController.isFullScreen()) {
            mDialog.show();
            LoggerD.debugExoVideoViewResizable("showQualitySelectionGrid: isFullScreen ");
        } else {
            LoggerD.debugExoVideoViewResizable("showQualitySelectionGrid: not isFullScreen ");
            WindowManager.LayoutParams params = new WindowManager.LayoutParams();

            int videoviewHeight = -mVideoViewPlayer.getHeight();
            if (isToolbarShown) {
                videoviewHeight = (int) (-mVideoViewPlayer.getHeight() + mContext.getResources().getDimension(R.dimen.tool_bar_height));
            }

            if (DeviceUtils.isTablet(mContext)) {
//                params.y = (videoviewHeight +((int) mContext.getResources().getDimension(R.dimen.margin_gap_16)))/2 ;
                params.y = -(((statusBarHeight - videoviewHeight) / 2) + ((gridHeight))+ ((int) mContext.getResources().getDimension(R.dimen.margin_gap_16)*2));
                params.x = 0;
            } else {
//                params.y = videoviewHeight+(((int) mContext.getResources().getDimension(R.dimen.margin_gap_8)));
                params.y = -(((statusBarHeight - videoviewHeight) / 2) + ((gridHeight / 2) + ((int) mContext.getResources().getDimension(R.dimen.margin_gap_16))));
                params.x = -mVideoViewPlayer.getWidth();
            }

            params.gravity = Gravity.CENTER;
            LoggerD.debugExoVideoViewResizable("showQualitySelectionGrid: portrait mode window manager layout " +
                    "videoviewHeight- " + videoviewHeight + " mVideoViewPlayer.getWidth()- " + mVideoViewPlayer.getWidth() +
                    "params.y- " + params.y + " params.x- " + params.x + " Grid height " + gridHeight + "Status bar height " + statusBarHeight
            );
            mDialog.getWindow().setAttributes(params);
            mDialog.getWindow().setLayout(grid.getWidth(), grid.getHeight());
            mDialog.show();
        }
        mMediaController.hide();
        grid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
    }*/

    public void showQualitySelectionGrid(RecyclerView qualityList, String videoQuality, MediaController2.QualitySelection selection) {

       /* View grid = LayoutInflater.from(mContext).inflate(R.layout.quality_selection_grid, null);
        LinearLayout parent = (LinearLayout) grid;*/

       // String[] lowBitRates = {PrefUtils.getInstance().getplayerControlsBitratesAuto(),PrefUtils.getInstance().getplayerControlsBitratesLow(),PrefUtils.getInstance().getplayerControlsBitratesMedium(), PrefUtils.getInstance().getplayerControlsBitratesHigh()};

        List<String> mList = new ArrayList<String>();
      //  Collections.addAll(mList, lowBitRates);
        int position = 0;
        String bitrateConfig = PrefUtils.getInstance().getplayerControlsBitrates();
        String selectedQuality = PrefUtils.getInstance().getContentVideoQuality() != null ? PrefUtils.getInstance().getContentVideoQuality() : "Auto" ;
        if(!videoQuality.isEmpty())
            selectedQuality = videoQuality;
        if(bitrateConfig != null) {
            try {
                JSONObject jsonObject = new JSONObject(bitrateConfig);
                int i = 0;
                for (Iterator<String> it = jsonObject.keys(); it.hasNext(); i++ ) {
                    String keyObj = it.next();
                    System.out.println("key : " + keyObj);
                    if(selectedQuality.equalsIgnoreCase(keyObj))
                        position = i;
                    mList.add(keyObj);
                    // System.out.println("value : " + valObj.toString());
                }
            } catch (Exception e){

            }
          //  RecyclerView qualityList = grid.findViewById(R.id.rv_quality_list);
            qualityList.setHasFixedSize(true);
            // recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
            qualityList.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));
            AudioTrackAdapter adapter = new AudioTrackAdapter(mList, new MediaController2.AudioTrackListner() {
                @Override
                public void getSelectedItem(int audio) {
               /* if(mPlayer instanceof ExoPlayerView) {
                    ((ExoPlayerView)mPlayer).setAudioTrack(mList.get(audio));
                    audioPosition = audio;
                    rvAudioTracks.setVisibility(GONE);
                }*/
                  //  PrefUtils.getInstance().setContentVideoQuality(mList.get(audio));
                    selection.getSelectedItem(mList.get(audio));
                    qualityList.setVisibility(View.GONE);
                    setUserChoiceQuality(mList.get(audio),true);
                  //  mDialog.dismiss();
                }
            }, position, mContext);
            qualityList.setAdapter(adapter);


        }
      /*  mBuilder = new AlertDialog.Builder(mContext);
        mBuilder.setView(grid);
        mDialog = mBuilder.create();*/

        LinearLayout rowLinearLayout = null;
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
       /* for (int i = 0; i < mList.size(); i++) {

            if (i % 2 == 0) {
                rowLinearLayout = new LinearLayout(mContext);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.bottomMargin = (int) mContext.getResources().getDimension(R.dimen.margin_gap_16);
                params.gravity = Gravity.CENTER;
                rowLinearLayout.setLayoutParams(params);
                rowLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
                parent.addView(rowLinearLayout);
            }
            //     TrackData track = mTrackDataList.get(i);
            String packageQuality = PrefUtils.getInstance().getPackageQuality();
            if (!TextUtils.isEmpty(packageQuality)) {
                String[] minMaxValue = packageQuality.split("-");
                float startBitrate = Float.parseFloat(minMaxValue[0]);
                float endBitrate = Float.parseFloat(minMaxValue[1]);
                boolean isContentHD = isContentQualityHd();
                float minHDRate;
                float maxHDRate;
                if (mList.get(i).equalsIgnoreCase("high")) {
                    minHDRate = (float) getMaxMinBitRate(VIDEO_QUALITY_HD, isContentHD, false);
                    maxHDRate = (float) getMaxMinBitRate(VIDEO_QUALITY_HD, isContentHD, true);
                } else {
                    minHDRate = (float) getMaxMinBitRate(mList.get(i).toLowerCase(), isContentHD, false);
                    maxHDRate = (float) getMaxMinBitRate(mList.get(i).toLowerCase(), isContentHD, true);
                }
                if(trackName==null) {
                    if(PrefUtils.getInstance().getContentVideoQuality() != null)
                        trackName = PrefUtils.getInstance().getContentVideoQuality();
                    else
                        trackName = "Auto" ;
                }
                if (minHDRate >= startBitrate && maxHDRate <= endBitrate) {
                    if (inflater == null) return;
                    View quality_grid = inflater.inflate(R.layout.player_grid_child, null);
                    TextView textView = (TextView) quality_grid.findViewById(R.id.text1);
                    textView.setText(mList.get(i));
                    if (trackName !=null &&
                            !trackName.isEmpty()) {
                        if (trackName.equalsIgnoreCase(mList.get(i))) {
                            textView.setBackgroundResource(R.drawable.quality_button_selected);
                        } else {
                            textView.setBackgroundResource(R.drawable.quality_button_default);
                        }
                    } else {
                        if (trackName != null
                                && mList.get(i).equalsIgnoreCase(sSavedQualityName)) {
                            textView.setBackgroundResource(R.drawable.quality_button_selected);
                        } else {
                            textView.setBackgroundResource(R.drawable.quality_button_default);
                        }
                    }

                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    params.leftMargin = (int) mContext.getResources().getDimension(R.dimen.margin_gap_16);
                    quality_grid.setLayoutParams(params);
                    quality_grid.setTag(i);
                    rowLinearLayout.addView(quality_grid);
                    quality_grid.setOnClickListener(mItemClickListener);
                }
            }
        }*/


       /* mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mDialog.getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);

        int gridHeight = getViewHeight(grid);
        int statusBarHeight = getStatusBarHeight(mContext);*/

      /*  if (mMediaController != null && mMediaController.isFullScreen()) {
            mDialog.show();
            LoggerD.debugExoVideoViewResizable("showQualitySelectionGrid: isFullScreen ");
        } else {
            LoggerD.debugExoVideoViewResizable("showQualitySelectionGrid: not isFullScreen ");
            WindowManager.LayoutParams params = new WindowManager.LayoutParams();

            int videoviewHeight = -mVideoViewPlayer.getHeight();
            if (isToolbarShown) {
                videoviewHeight = (int) (-mVideoViewPlayer.getHeight() + mContext.getResources().getDimension(R.dimen.tool_bar_height));
            }

            if (DeviceUtils.isTablet(mContext)) {
//                params.y = (videoviewHeight +((int) mContext.getResources().getDimension(R.dimen.margin_gap_16)))/2 ;
                params.y = -(((statusBarHeight - videoviewHeight) / 2) + ((gridHeight)) + ((int) mContext.getResources().getDimension(R.dimen.margin_gap_16) * 2));
                params.x = 0;
            } else {
//                params.y = videoviewHeight+(((int) mContext.getResources().getDimension(R.dimen.margin_gap_8)));
                params.y = -(((statusBarHeight - videoviewHeight) / 2) + ((gridHeight / 2) + ((int) mContext.getResources().getDimension(R.dimen.margin_gap_16))));
                params.x = -mVideoViewPlayer.getWidth();
            }

            params.gravity = Gravity.CENTER;
            LoggerD.debugExoVideoViewResizable("showQualitySelectionGrid: portrait mode window manager layout " +
                    "videoviewHeight- " + videoviewHeight + " mVideoViewPlayer.getWidth()- " + mVideoViewPlayer.getWidth() +
                    "params.y- " + params.y + " params.x- " + params.x + " Grid height " + gridHeight + "Status bar height " + statusBarHeight
            );
            mDialog.getWindow().setAttributes(params);
            mDialog.getWindow().setLayout(grid.getWidth(), grid.getHeight());
            mDialog.show();
        }
        mMediaController.hide();
        grid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });*/
    }


    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public void setCardData(com.myplex.model.CardData cardData) {
        this.mCardData = cardData;
    }

    public void setCDNTypeAzure(boolean CDNTypeAzure) {
        isCDNTypeAzure = CDNTypeAzure;
    }

    public static int getStatusBarHeight(final Context context) {
        final Resources resources = context.getResources();
        final int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0)
            return resources.getDimensionPixelSize(resourceId);
        else
            return (int) Math.ceil((Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? 24 : 25) * resources.getDisplayMetrics().density);
    }

    public static int getViewHeight(View view) {
        WindowManager wm =
                (WindowManager) view.getContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();

        int deviceWidth;

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            Point size = new Point();
            display.getSize(size);
            deviceWidth = size.x;
        } else {
            deviceWidth = display.getWidth();
        }

        int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(deviceWidth, View.MeasureSpec.AT_MOST);
        int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        view.measure(widthMeasureSpec, heightMeasureSpec);
        return view.getMeasuredHeight(); //        view.getMeasuredWidth();
    }


    private static TrackData cehckAndUpdateTrack(String trackname, List<TrackData> trackList, int i, float minBitrate, int maxBitrate) {
        TrackData track = null;
        for (TrackData existingtrack : trackList) {
            if (trackname.equalsIgnoreCase(existingtrack.name)) {
                track = existingtrack;
                break;
            }
        }
        if (track == null) {
            track = new TrackData();
            track.minBitrate = minBitrate;
            track.position = i;
            track.name = trackname;
            trackList.add(track);
        } else {
            track.maxBitrate = maxBitrate;
            track.position = i;
        }
        return track;
    }

    public List<TrackData> prepareTrackList() {
        final List<TrackData> trackList = new ArrayList<>();
        int trackCount = 0;
        int vod_low_min = 1;
        int vod_low_max = 240;

        int vod_medium_max = 480;
        int vod_medium_min = 1;
        int vod_hd_min = 480;
        int vod_hd_max = 720;


        if (trackName != null) {
            if (PrefUtils.getInstance().getQUALITY_MAP() != null) {
                HashMap<String, Integer> a = PrefUtils.getInstance().getQUALITY_MAP();
                if (trackName.contains("hd")) {

                    vod_low_min = a.get("vod_hd_low_min");
                    vod_low_max = a.get("vod_hd_low_max");
                    vod_medium_min = a.get("vod_hd_medium_min");
                    vod_medium_max = a.get("vod_hd_medium_max");
                    vod_hd_min = a.get("vod_hd_hd_min");
                    vod_hd_max = a.get("vod_hd_hd_max");

                } else {

                    vod_low_min = a.get("vod_sd_low_min");
                    vod_low_max = a.get("vod_sd_low_max");
                    vod_medium_min = a.get("vod_sd_medium_min");
                    vod_medium_max = a.get("vod_sd_medium_max");
                    vod_hd_min = a.get("vod_sd_hd_min");
                    vod_hd_max = a.get("vod_sd_hd_max");

                }
        /*{vod_hd_medium_max=480, vod_sd_hd_min=480, vod_sd_low_max=240,
           vod_hd_hd_min=480, vod_sd_medium_min=240, vod_hd_low_min=0,
           vod_hd_hd_max=720, vod_sd_auto_max=100, vod_hd_auto_max=100,
           vod_sd_hd_max=720, vod_sd_low_min=0, vod_hd_low_max=240,
            vod_sd_auto_min=0, vod_hd_auto_min=0, vod_sd_medium_max=480,
             vod_hd_medium_min=240}
            */
            }
        }
        if (selector == null) {
            return null;
        }
        trackInfo = selector.getCurrentMappedTrackInfo();
        if (trackInfo == null) {
            return null;
        }
        trackRendererGroupIndex = 0;
        for (int i = 0; i < trackInfo.length; i++) {
            if (mVideoViewPlayer.getRendererType(i) == C.TRACK_TYPE_VIDEO) {
                trackRendererGroupIndex = i;
                break;
            }
        }
        trackGroups = trackInfo.getTrackGroups(trackRendererGroupIndex);
        if (trackGroups == null) {
            return null;
        }
        trackCount = trackGroups.length;

        if (trackCount == 0) {
            return null;
        }
//        if (mediaFormat.adaptive) {
        int rendererIndex = 0;
        cehckAndUpdateTrack("Auto", trackList, -1, 0, rendererIndex, trackGroups);
//        }

        for (int i = 0; i < trackCount; i++) {
            TrackGroup track = trackGroups.get(i);
            for (int j = 0; j < track.length; j++) {
                Format mediaFormat = track.getFormat(j);
                String stringBitrate = mediaFormat.bitrate == Format.NO_VALUE ? ""
                        : String.format(Locale.US, "%.2fMbit", mediaFormat.bitrate / 1000000f);
                float floatBitrate = mediaFormat.bitrate / 1000000f;
                floatBitrate = floatBitrate * 1024;
                LoggerD.debugExoVideoViewResizable("showQualitySelectionGrid: prepareTrackList: bitrate- " + floatBitrate + " stringBitrate- " + stringBitrate + " i- " + j);
/* bitratehps
240,480480,480,720/  1 240 1 480
*/
                if (vod_low_min < floatBitrate && floatBitrate <= vod_low_max) {
                    cehckAndUpdateTrack(Integer.toString(vod_low_max), trackList, j, floatBitrate, rendererIndex, trackGroups);
                } else if (vod_medium_min < floatBitrate && floatBitrate < vod_medium_max) {
                    cehckAndUpdateTrack(Integer.toString(vod_medium_max), trackList, j, floatBitrate, rendererIndex, trackGroups);
                } else if (floatBitrate >= vod_hd_min) {
                    cehckAndUpdateTrack(Integer.toString(vod_hd_max), trackList, j, floatBitrate, rendererIndex, trackGroups);
                }
            }
        }
        Collections.sort(trackList);
        return trackList;
    }

    private static TrackData cehckAndUpdateTrack(String trackname, List<TrackData> trackList, int i, float floatBitrate, int rendererIndex, TrackGroupArray trackGroups) {
        LoggerD.debugLog("TrackData: trackname- " + trackname
                + " i- " + i
                + " floatBitrate- " + floatBitrate
                + " rendererIndex- " + rendererIndex
                + " rendererIndex- " + rendererIndex
        );
        TrackData track = null;
        if (trackname.equalsIgnoreCase("high")) {
            trackname = "high";
        } else if (trackname.equalsIgnoreCase("medium")) {
            trackname = "medium";
        } else if (trackname.equalsIgnoreCase("Auto")) {
            trackname = "Auto";
        } else if (trackname.equalsIgnoreCase("low")) {
            trackname = "low";
        }

        for (TrackData existingtrack : trackList) {
            if (trackname.equalsIgnoreCase(existingtrack.name)) {
                track = existingtrack;
                break;
            }
        }
        if (track == null) {
            track = new TrackData();
            track.bitrate = floatBitrate;
            track.position = i;
            track.name = trackname;
            track.trackRenderGroupPosition = rendererIndex;
            track.trackGroups = trackGroups;
            trackList.add(track);
        } else {
            track.bitrate = floatBitrate;
            track.position = i;
        }
        return track;
    }

    public void isToolbarShown(boolean isToolbarShown) {
        this.isToolbarShown = isToolbarShown;
    }


    public void dismiss() {
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }
    }


    public int getOriginalTrackCount() {
        return mVideoViewPlayer == null ? 0 : mVideoViewPlayer.getTrackCount(C.TRACK_TYPE_VIDEO);
    }

    public static TrackData getSavedTrack(List<TrackData> trackDataList) {
        sSavedQualityName = PrefUtils.getInstance().getPrefPlayBackQuality();
        if (trackDataList == null || TextUtils.isEmpty(sSavedQualityName)
                || "Auto".equalsIgnoreCase(sSavedQualityName)) {
            return null;
        }
        for (TrackData trackData : trackDataList) {
            if (sSavedQualityName.equalsIgnoreCase(trackData.name)) {
                return trackData;
            }
        }
        return null;
    }

    public void setSelectedTrackPosition(int position) {
        this.mSelectedPosition = position;
        LoggerD.debugHooqVstbLog("Tracks: setSelectedTrackPosition position- " + position);
    }

    public void setUserChoiceQuality(String selectedQuality, boolean fromPlayer) {
        sSavedQualityName = selectedQuality;
        switch (selectedQuality) {
            case "Data Saver" :
                selectedQuality = "low";
                break;
            case "Medium" :
                selectedQuality = "medium";
                break;
            case "High" :
                selectedQuality = "high";
                break;
            default:
                selectedQuality = "auto";
                break;
        }
        /*if (selector == null || TextUtils.isEmpty(sSavedQualityName)) {
            return;
        }*/
        try {
            /*MappingTrackSelector.MappedTrackInfo trackInfo = selector.getCurrentMappedTrackInfo();
            if (trackInfo == null || trackInfo.length <= 1 || trackInfo.getTrackGroups(0) == null) {
                return;
            }
            trackGroups = trackInfo.getTrackGroups(0);
            if (trackGroups == null || trackGroups.length <= 0) return;
            TrackGroup trackGroup = null;
            if (trackGroups.length > 0) {
                trackGroup = trackGroups.get(0);
            }
            if (trackGroup == null || trackGroup.length <= 1) {
                return;
                //Only single track is present.So no user selection.
            }
            int trackPosition = 0;
            Format mediaFormat;
            float floatBitrate = 0f;
            TrackGroup track = trackGroups.get(0);*/

            boolean isContentHD = isContentQualityHd();
            float minHDRate;
            float maxHDRate;


            switch (selectedQuality) {
                case "low":
                    minHDRate = (float) getMaxMinBitRate(VIDEO_QUALITY_LOW, isContentHD, false);
                    maxHDRate = (float) getMaxMinBitRate(VIDEO_QUALITY_LOW, isContentHD, true);
                    PrefUtils.getInstance().setPrefPlayBackQuality("low");
                    PrefUtils.getInstance().setPrefPlayBackQualityPos(mSelectedPosition);
                    PrefUtils.getInstance().selectedQuality = 1;

                    /*mSelectedPosition = 1;
                    for (int position = 0; position < track.length; position++) {
                        mediaFormat = track.getFormat(position);
                        floatBitrate = mediaFormat.bitrate / 1000000f;
                        floatBitrate = floatBitrate * 1024;
                        LoggerD.debugExoVideoViewResizable("SaveTrackList: bitrate- " + floatBitrate + " i- " + position);
                        if (floatBitrate <= 240) {
                            trackPosition = position;
                        }
                    }
//                    trackPosition = 2;*/
                    break;
                case "medium":

                    minHDRate = (float) getMaxMinBitRate(VIDEO_QUALITY_MEDIUM, isContentHD, false);
                    maxHDRate = (float) getMaxMinBitRate(VIDEO_QUALITY_MEDIUM, isContentHD, true);
                    PrefUtils.getInstance().setPrefPlayBackQuality("medium");
                    PrefUtils.getInstance().setPrefPlayBackQualityPos(mSelectedPosition);
                    PrefUtils.getInstance().selectedQuality = 2;
                    /*mSelectedPosition = 2;
                    for (int position = 0; position < track.length; position++) {
                        mediaFormat = track.getFormat(position);
                        floatBitrate = mediaFormat.bitrate / 1000000f;
                        floatBitrate = floatBitrate * 1024;
                        LoggerD.debugExoVideoViewResizable("SaveTrackList: bitrate- " + floatBitrate + " i- " + position);
                        if (floatBitrate < 480) {
                            trackPosition = position;
                        }
                    }
//                    trackPosition = 3;*/
                    break;
                case "high":
                    minHDRate = (float) getMaxMinBitRate(VIDEO_QUALITY_HD, isContentHD, false);
                    maxHDRate = (float) getMaxMinBitRate(VIDEO_QUALITY_HD, isContentHD, true);
                    PrefUtils.getInstance().setPrefPlayBackQuality("high");
                    PrefUtils.getInstance().setPrefPlayBackQualityPos(mSelectedPosition);
                    PrefUtils.getInstance().selectedQuality = 3;
                    /*mSelectedPosition = 3;
                    for (int position = 0; position < track.length; position++) {
                        mediaFormat = track.getFormat(position);
                        floatBitrate = mediaFormat.bitrate / 1000000f;
                        floatBitrate = floatBitrate * 1024;
                        LoggerD.debugExoVideoViewResizable("SaveTrackList: bitrate- " + floatBitrate + " i- " + position);
                        if (floatBitrate > 480) {
                            trackPosition = position;
                        }
                    }
//                    trackPosition = 5;*/
                    break;
                default:
                    minHDRate = (float) getMaxMinBitRate(VIDEO_QUALITY_AUTO, isContentHD, false);
                    maxHDRate = (float) getMaxMinBitRate(VIDEO_QUALITY_AUTO, isContentHD, true);
                    PrefUtils.getInstance().setPrefPlayBackQuality("auto");
                    PrefUtils.getInstance().setPrefPlayBackQualityPos(mSelectedPosition);
                    PrefUtils.getInstance().selectedQuality = 0;
                    break;

            }

            control.changeQuality(minHDRate, maxHDRate, MyplexPlayer.VIDEO_TRACK_RENDERER);
            if (!TextUtils.isEmpty(sSavedQualityName) && fromPlayer) {
                String msg = "Switching to " + sSavedQualityName + " quality";
                // PrefUtils.getInstance().setContentVideoQuality(trackName);
                //if (!"Auto".equalsIgnoreCase(trackName)) {
                AlertDialogUtil.showToastNotification(msg);
                //}
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    private int getMaxMinBitRate(String quality, boolean isContentHD, boolean isMax) {
        int value = 0;
        switch (quality) {
            case "low":
                if (contentType != null && (contentType.equalsIgnoreCase(TYPE_LIVE)
                        || contentType.equalsIgnoreCase(TYPE_PROGRAM))) {
                    if (isContentHD) {
                        if (isMax) {
                            value = (int) BITRATE_CAP.get("live_hd_low_max");
                        } else {
                            value = (int) BITRATE_CAP.get("live_hd_low_min");
                        }
                    } else {
                        if (isMax) {
                            value = (int) BITRATE_CAP.get("live_sd_low_max");
                        } else {
                            value = (int) BITRATE_CAP.get("live_sd_low_min");
                        }

                    }
                }else if (contentType != null && (contentType.equalsIgnoreCase(APIConstants.TYPE_TVEPISODE)||
                        (contentType.equalsIgnoreCase(APIConstants.TYPE_NEWS)))) {
                    if (isContentHD) {
                        if (isMax) {
                            value = (int) BITRATE_CAP.get("vod_hd_low_tv_show_max");
                        } else {
                            value = (int) BITRATE_CAP.get("vod_hd_low_tv_show_min");
                        }
                    } else {
                        if (isMax) {
                            value = (int) BITRATE_CAP.get("vod_sd_low_tv_show_max");
                        } else {
                            value = (int) BITRATE_CAP.get("vod_sd_low_tv_show_min");
                        }

                    }
                }else {
                    if (isContentHD) {
                        if (isMax) {
                            value = (int) BITRATE_CAP.get("vod_hd_low_max");
                        } else {
                            value = (int) BITRATE_CAP.get("vod_hd_low_min");
                        }
                    } else {
                        if (isMax) {
                            value = (int) BITRATE_CAP.get("vod_sd_low_max");
                        } else {
                            value = (int) BITRATE_CAP.get("vod_sd_low_min");
                        }

                    }
                }
                break;
            case "medium":
                if (contentType != null && (contentType.equalsIgnoreCase(TYPE_LIVE)
                        || contentType.equalsIgnoreCase(TYPE_PROGRAM))) {
                    if (isContentHD) {
                        if (isMax) {
                            value = (int) BITRATE_CAP.get("live_hd_medium_max");
                        } else {
                            value = (int) BITRATE_CAP.get("live_hd_medium_min");
                        }
                    } else {
                        if (isMax) {
                            value = (int) BITRATE_CAP.get("live_sd_medium_max");
                        } else {
                            value = (int) BITRATE_CAP.get("live_sd_medium_min");
                        }

                    }
                }else if (contentType != null && (contentType.equalsIgnoreCase(APIConstants.TYPE_TVEPISODE)
                        ||
                        (contentType.equalsIgnoreCase(APIConstants.TYPE_NEWS)))) {
                    if (isContentHD) {
                        if (isMax) {
                            value = (int) BITRATE_CAP.get("vod_hd_medium_tv_show_max");
                        } else {
                            value = (int) BITRATE_CAP.get("vod_hd_medium_tv_show_min");
                        }
                    } else {
                        if (isMax) {
                            value = (int) BITRATE_CAP.get("vod_sd_medium_tv_show_max");
                        } else {
                            value = (int) BITRATE_CAP.get("vod_sd_medium_tv_show_min");
                        }

                    }
                } else {
                    if (isContentHD) {
                        if (isMax) {
                            value = (int) BITRATE_CAP.get("vod_hd_medium_max");
                        } else {
                            value = (int) BITRATE_CAP.get("vod_hd_medium_min");
                        }
                    } else {
                        if (isMax) {
                            value = (int) BITRATE_CAP.get("vod_sd_medium_max");
                        } else {
                            value = (int) BITRATE_CAP.get("vod_sd_medium_min");
                        }

                    }
                }
                break;
            case "hd":
                if (contentType != null && (contentType.equalsIgnoreCase(TYPE_LIVE)
                        || contentType.equalsIgnoreCase(TYPE_PROGRAM))) {
                    if (isContentHD) {
                        if (isMax) {
                            value = (int) BITRATE_CAP.get("live_hd_hd_max");
                        } else {
                            value = (int) BITRATE_CAP.get("live_hd_hd_min");
                        }
                    } else {
                        if (isMax) {
                            value = (int) BITRATE_CAP.get("live_sd_hd_max");
                        } else {
                            value = (int) BITRATE_CAP.get("live_sd_hd_min");
                        }

                    }
                }else if (contentType != null && (contentType.equalsIgnoreCase(APIConstants.TYPE_TVEPISODE)
                        ||
                        (contentType.equalsIgnoreCase(APIConstants.TYPE_NEWS)))) {
                    if (isContentHD) {
                        if (isMax) {
                            value = (int) BITRATE_CAP.get("vod_hd_hd_tv_show_max");
                        } else {
                            value = (int) BITRATE_CAP.get("vod_hd_hd_tv_show_min");
                        }
                    } else {
                        if (isMax) {
                            value = (int) BITRATE_CAP.get("vod_sd_hd_tv_show_max");
                        } else {
                            value = (int) BITRATE_CAP.get("vod_sd_hd_tv_show_min");
                        }

                    }
                } else {
                    if (isContentHD) {
                        if (isMax) {
                            value = (int) BITRATE_CAP.get("vod_hd_hd_max");
                        } else {
                            value = (int) BITRATE_CAP.get("vod_hd_hd_min");
                        }
                    } else {
                        if (isMax) {
                            // value = 1800
                            value = (int) BITRATE_CAP.get("vod_sd_hd_max");
                        } else {
                            value = (int) BITRATE_CAP.get("vod_sd_hd_min");
                        }

                    }
                }
                break;

            case "auto":
                if (contentType != null && (contentType.equalsIgnoreCase(TYPE_LIVE)
                        || contentType.equalsIgnoreCase(TYPE_PROGRAM))) {
                    if (isContentHD) {
                        if (isMax) {
                            value = (int) BITRATE_CAP.get("live_hd_auto_max");
                        } else {
                            value = (int) BITRATE_CAP.get("live_hd_auto_min");
                        }
                    } else {
                        if (isMax) {
                            value = (int) BITRATE_CAP.get("live_sd_auto_max");
                        } else {
                            value = (int) BITRATE_CAP.get("live_sd_auto_min");
                        }

                    }
                }else if (contentType != null && (contentType.equalsIgnoreCase(APIConstants.TYPE_TVEPISODE)
                        ||
                        (contentType.equalsIgnoreCase(APIConstants.TYPE_NEWS)))) {
                    if (isContentHD) {
                        if (isMax) {
                            value = (int) BITRATE_CAP.get("vod_hd_auto_tv_show_max");
                        } else {
                            value = (int) BITRATE_CAP.get("vod_hd_auto_tv_show_min");
                        }
                    } else {
                        if (isMax) {
                            value = (int) BITRATE_CAP.get("vod_sd_auto_tv_show_max");
                        } else {
                            value = (int) BITRATE_CAP.get("vod_sd_auto_tv_show_min");
                        }

                    }
                } else {
                    if (isContentHD) {
                        if (isMax) {
                            value = (int) BITRATE_CAP.get("vod_hd_auto_max");
                        } else {
                            value = (int) BITRATE_CAP.get("vod_hd_auto_min");
                        }
                    } else {
                        if (isMax) {
                            value = (int) BITRATE_CAP.get("vod_sd_auto_max");
                        } else {
                            value = (int) BITRATE_CAP.get("vod_sd_auto_min");
                        }

                    }
                }
                break;
            default:
                if (contentType != null && (contentType.equalsIgnoreCase(TYPE_LIVE)
                        || contentType.equalsIgnoreCase(TYPE_PROGRAM))) {
                    if (isContentHD) {
                        if (isMax) {
                            value = (int) BITRATE_CAP.get("live_hd_auto_max");
                        } else {
                            value = (int) BITRATE_CAP.get("live_hd_auto_min");
                        }
                    } else {
                        if (isMax) {
                            value = (int) BITRATE_CAP.get("live_sd_auto_max");
                        } else {
                            value = (int) BITRATE_CAP.get("live_sd_auto_min");
                        }

                    }
                }else if (contentType != null && (contentType.equalsIgnoreCase(APIConstants.TYPE_TVEPISODE)
                        ||
                        (contentType.equalsIgnoreCase(APIConstants.TYPE_NEWS)))) {
                    if (isContentHD) {
                        if (isMax) {
                            value = (int) BITRATE_CAP.get("vod_hd_auto_tv_show_max");
                        } else {
                            value = (int) BITRATE_CAP.get("vod_hd_auto_tv_show_min");
                        }
                    } else {
                        if (isMax) {
                            value = (int) BITRATE_CAP.get("vod_sd_auto_tv_show_max");
                        } else {
                            value = (int) BITRATE_CAP.get("vod_sd_auto_tv_show_min");
                        }

                    }
                }  else {
                    if (isContentHD) {
                        if (isMax) {
                            value = (int) BITRATE_CAP.get("vod_hd_auto_max");
                        } else {
                            value = (int) BITRATE_CAP.get("vod_hd_auto_min");
                        }
                    } else {
                        if (isMax) {
                            value = (int) BITRATE_CAP.get("vod_sd_auto_max");
                        } else {
                            value = (int) BITRATE_CAP.get("vod_sd_auto_min");
                        }

                    }
                }
        }
        if (isMax) {
            //TODO: safety valve
            if (value == 0) {
                value = 4000;
            }
        }
        return value;
    }

    private boolean isContentQualityHd() {
        boolean isContentHD = false;
        if (contentType != null && (contentType.equalsIgnoreCase(TYPE_LIVE)
                || contentType.equalsIgnoreCase(TYPE_PROGRAM))) {
            isContentHD = true;
        } else {
            if (isCDNTypeAzure) {
                TrackGroup videoTrackGroup = trackGroups.get(ApplicationConfig.VIDEO_RENDERER_INDEX);
                if (videoTrackGroup != null && videoTrackGroup.length > (int) BITRATE_CAP.get("vod_sd_max_track")) {
                    isContentHD = true;
                }
            } else {
                if (mCardData != null && mCardData.content != null && mCardData.content.videoQuality != null) {
                    isContentHD = mCardData.content.videoQuality.contains("HD");
                }
                //isContentHD = isContentHD;

            }
        }
        return isContentHD;
    }
    public float[] getUserChoiceQualityArray(String playableUrl) {
        sSavedQualityName = PrefUtils.getInstance().getPrefPlayBackQuality();
        try {
            boolean isContentHD;
            if (contentType != null && (contentType.equalsIgnoreCase(TYPE_LIVE)
                    || contentType.equalsIgnoreCase(TYPE_PROGRAM))) {
                if (playableUrl.contains("hd")) {
                    isContentHD = true;
                } else {
                    isContentHD = false;
                }
            } else {
                isContentHD = isContentQualityHd();
            }
            float minHDRate ;
            float maxHDRate;
            if (!TextUtils.isEmpty(sSavedQualityName)) {
                switch (sSavedQualityName.toLowerCase()) {
                    case "low":
                        minHDRate = (float) getMaxMinBitRate(VIDEO_QUALITY_LOW, isContentHD, false);
                        maxHDRate = (float) getMaxMinBitRate(VIDEO_QUALITY_LOW, isContentHD, true);
                        PrefUtils.getInstance().setPrefPlayBackQuality("low");
                        PrefUtils.getInstance().setPrefPlayBackQualityPos(mSelectedPosition);
                        PrefUtils.getInstance().selectedQuality = 1;

                        break;
                    case "medium":
                        minHDRate = (float) getMaxMinBitRate(VIDEO_QUALITY_MEDIUM, isContentHD, false);
                        maxHDRate = (float) getMaxMinBitRate(VIDEO_QUALITY_MEDIUM, isContentHD, true);

                            PrefUtils.getInstance().setPrefPlayBackQuality("medium");
                        PrefUtils.getInstance().setPrefPlayBackQualityPos(mSelectedPosition);
                        PrefUtils.getInstance().selectedQuality = 2;

                        break;
                    case "high":
                        minHDRate = (float) getMaxMinBitRate(VIDEO_QUALITY_HD, isContentHD, false);
                        maxHDRate = (float) getMaxMinBitRate(VIDEO_QUALITY_HD, isContentHD, true);
                        PrefUtils.getInstance().setPrefPlayBackQuality("high");
                        PrefUtils.getInstance().setPrefPlayBackQualityPos(mSelectedPosition);
                        PrefUtils.getInstance().selectedQuality = 3;

                        break;
                    default:
                        minHDRate = (float) getMaxMinBitRate(VIDEO_QUALITY_AUTO, isContentHD, false);
                        maxHDRate = (float) getMaxMinBitRate(VIDEO_QUALITY_AUTO, isContentHD, true);

                        PrefUtils.getInstance().setPrefPlayBackQuality("auto");
                        PrefUtils.getInstance().setPrefPlayBackQualityPos(mSelectedPosition);
                        PrefUtils.getInstance().selectedQuality = 0;

                        break;
                }
            } else {
                minHDRate = (float) getMaxMinBitRate(VIDEO_QUALITY_AUTO, isContentHD, false);
                maxHDRate = (float) getMaxMinBitRate(VIDEO_QUALITY_AUTO, isContentHD, true);
                PrefUtils.getInstance().setPrefPlayBackQuality("auto");
                PrefUtils.getInstance().setPrefPlayBackQualityPos(mSelectedPosition);
                PrefUtils.getInstance().selectedQuality = 0;

            }
            //control.changeQuality(minHDRate,maxHDRate, MyplexPlayer.VIDEO_TRACK_RENDERER);
            Log.e("QUALITY_SELECTION","minHDRate : "+ minHDRate);
            Log.e("QUALITY_SELECTION","maxHDRate : "+ maxHDRate);
            return new float[]{minHDRate, maxHDRate};
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public int getUserChoiceQuality() {
        int trackPosition = -1;
        if (TextUtils.isEmpty(sSavedQualityName) || mTrackDataList == null || mTrackDataList.isEmpty()) {
            return trackPosition;
        }
        try {
            for (int i = 0; i < mTrackDataList.size(); i++) {
                TrackData track = mTrackDataList.get(i);
                if (sSavedQualityName.toLowerCase().equalsIgnoreCase(track.name)) {
                    return i;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return trackPosition;
    }

    public void setTrackPlayer(TrackChangeInterface control) {
        this.control = control;
    }

    public int getSelectedTrackIndex() {
        return mSelectedPosition;
    }
}
