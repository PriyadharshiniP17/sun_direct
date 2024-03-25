package com.myplex.myplex.media;

import android.app.Activity;
import android.media.AudioManager;
import android.os.Handler;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.WindowManager;

import com.myplex.api.APIConstants;
import com.myplex.myplex.media.exoVideo.MyplexVideoViewPlayer;

/**
 * Created by Srikanth on 20-Feb-17.
 */
public class PlayerGestureListener extends GestureDetector.SimpleOnGestureListener {
    private final MyplexVideoViewPlayer mPlayer;
    private final MediaController2 mMediaController;
    private boolean firstTouch;
    private boolean volumeControl;
    private boolean toSeek;
    private boolean isLive;
    private int mVolume = -1;
    private float mBrightness = -1;
    private int mMaxVolume;
    private long mNewPosition = -1;
    private Activity mActivity;
    private float mScreenWidthPixels;
    private float mScreenHeightPixels;
    private Handler handler;
    private boolean isFullScreen;
    private GestureDetector mGestureDetector;

    public PlayerGestureListener(Activity activity, MyplexVideoViewPlayer player, MediaController2 mediaController, GestureListener gestureListener) {
        this.mActivity = activity;
        this.mPlayer = player;
        this.mMediaController = mediaController;
        this.mGestureListener = gestureListener;
        mScreenWidthPixels = mActivity.getResources().getDisplayMetrics().widthPixels;
        mScreenHeightPixels = mActivity.getResources().getDisplayMetrics().heightPixels;
        mMaxVolume = mMediaController.getAudioManger(mActivity).getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        mGestureDetector = new GestureDetector(mActivity, this);
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        return true;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        firstTouch = true;
//        if (mPlayer.mMediaController.isFullScreen()) {

        if (mPlayer == null || mMediaController == null) {
            return false;
        }
        if (!mPlayer.isMinimized() && mMediaController.isFullScreen()) {
            return true;
        } else {
            switch (e.getAction()) {
                case MotionEvent.ACTION_DOWN:
//                    if (mPlayer.getCurrentState() < mPlayer.STATE_PREPARED) {
                    if (mPlayer.getCurrentPosition() <= 0) {
                        return false;
                    }
                    //TODO : Commented for check touch of hungama player
                    if (mMediaController != null && !mPlayer.isMinimized()) {
                        mMediaController.doShowHideControl();
                    }
                    break;
            }

        }
        return false;

    }


    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        if(APIConstants.IS_PLAYER_SCREEN_LOCKED){
            return true;
        }
        if(e1 == null
            || e1 == null){
            return super.onScroll(e1, e2, distanceX, distanceY);
        }
        float mOldX = e1.getX(), mOldY = e1.getY();
        float deltaY = mOldY - e2.getY();
        float deltaX = mOldX - e2.getX();
        if (firstTouch) {
            toSeek = Math.abs(distanceX) >= Math.abs(distanceY);
            if (mMediaController.isFullScreen()) {
                volumeControl = mOldX > mScreenHeightPixels * 0.5f;
            } else {
                volumeControl = mOldX > mScreenWidthPixels * 0.5f;
            }
            firstTouch = false;
        }
            if (toSeek) {
                if (!isLive) {
                    float per = -deltaX / mPlayer.getWidth();
                    onProgressSlide(per);
                }
            } else {
//                percent = percent;
                if (volumeControl && mOldX > 1200) {
                    float percent = deltaY * 1.26f / mPlayer.getHeight();
                    onVolumeSlide(percent);
                } else if(mOldX < 400) {
                    float percent = deltaY * 0.87f / mPlayer.getHeight();
                    onBrightnessSlide(percent);
                }
        }


        return super.onScroll(e1, e2, distanceX, distanceY);
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        mMediaController.doShowHideControl();
        return true;
    }


    public void endGesture() {
        mVolume = -1;
        mBrightness = -1f;
        if (mNewPosition >= 0) {
//            handler.removeMessages(MESSAGE_SEEK_NEW_POSITION);
//            handler.sendEmptyMessage(MESSAGE_SEEK_NEW_POSITION);
            mPlayer.seekTo((int) mNewPosition);
            mNewPosition = -1;
        }

        if (mGestureListener != null) {
            mGestureListener.onEndGesture();
        }
        /*if (mMediaController != null) {
            mMediaController*//**//*.onEndGesture();
        }*/
//        handler.removeMessages(MESSAGE_HIDE_CENTER_BOX);
//        handler.sendEmptyMessageDelayed(MESSAGE_HIDE_CENTER_BOX, 500);

    }

    private void onProgressSlide(float percent) {
        if(APIConstants.IS_PLAYER_SCREEN_LOCKED){
            return;
        }
        long position = mPlayer.getCurrentPosition();
        long duration = mPlayer.getCachedDuration();
        long deltaMax = Math.min(100 * 1000, duration - position);
        long delta = (long) (deltaMax * percent);


        mNewPosition = delta + position;
        if (mNewPosition > duration) {
            mNewPosition = duration;
        } else if (mNewPosition <= 0) {
            mNewPosition = 0;
            delta = -position;
        }
        int showDelta = (int) delta / 1000;
        /*if (showDelta != 0) {
            $.id(R.id.app_video_fastForward_box).visible();
            String text = showDelta > 0 ? ("+" + showDelta) : "" + showDelta;
            $.id(R.id.app_video_fastForward).text(text + "s");
            $.id(R.id.app_video_fastForward_target).text(generateTime(mNewPosition)+"/");
            $.id(R.id.app_video_fastForward_all).text(generateTime(duration));
        }*/
        mMediaController.show();
        mMediaController.setProgress(mNewPosition, duration);
//        mMediaController.onProgressSlide(showDelta, mNewPosition, duration);
        if (mGestureListener != null) {
            mGestureListener.onProgressSlide(showDelta, mNewPosition, duration);
        }
    }

    private void onBrightnessSlide(float percent) {
        if(APIConstants.IS_PLAYER_SCREEN_LOCKED){
            return;
        }
        if (mBrightness < 0) {
            mBrightness = mActivity.getWindow().getAttributes().screenBrightness;
            if (mBrightness <= 0.00f) {
                mBrightness = 0.50f;
            } else if (mBrightness < 0.01f) {
                mBrightness = 0.01f;
            }
        }
        mMediaController.hide();
//        Log.d(this.getClass().getSimpleName(),"mBrightness:"+mBrightness+",percent:"+ percent);
        WindowManager.LayoutParams lpa = mActivity.getWindow().getAttributes();
//        0.99829686
        float brightness = mBrightness + percent;
        /*if(brightness >= 1.0f){
            brightness = 0.99829686f;
        }*/
        lpa.screenBrightness = brightness;
        if (lpa.screenBrightness > 1.0f) {
            lpa.screenBrightness = 1.0f;
        } else if (lpa.screenBrightness < 0.01f) {
            lpa.screenBrightness = 0.01f;
        }
//        $.id(R.id.app_video_brightness).text(((int) (lpa.screenBrightness * 100))+"%");
//        $.id(R.id.app_video_brightness_box).visible();


        mActivity.getWindow().setAttributes(lpa);
//        mMediaController.onBrightnessSlide(((int) (lpa.screenBrightness * 15)) + "%");
        if (mGestureListener != null) {
            mGestureListener.onBrightnessSlide(String.valueOf((int) (lpa.screenBrightness * 15)));
        }

    }

    private void onVolumeSlide(float percent) {
        if (mVolume == -1) {
            mVolume = mMediaController.getAudioManger(mActivity).getStreamVolume(AudioManager.STREAM_MUSIC);
            if (mVolume < 0)
                mVolume = 0;
        }
        mMediaController.hide();

        int index = (int) (percent * mMaxVolume) + mVolume;
        if (index > mMaxVolume)
            index = mMaxVolume;
        else if (index < 0)
            index = 0;

        try {
            mMediaController.getAudioManger(mActivity).setStreamVolume(AudioManager.STREAM_MUSIC, index, 0);
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (Throwable e) {
            e.printStackTrace();
        }

        int i = (int) (index * 1.0 / mMaxVolume * 15);
        /*$.id(R.id.app_video_volume_icon).image(i==0?R.drawable.ic_volume_off_white_36dp:R.drawable.ic_volume_up_white_36dp);
        $.id(R.id.app_video_brightness_box).gone();
        $.id(R.id.app_video_volume_box).visible();
        $.id(R.id.app_video_volume_box).visible();
        $.id(R.id.app_video_volume).text(s).visible();*/
//        mMediaController.onVolumeSlide(i);
        if (mGestureListener != null) {
            mGestureListener.onVolumeSlide(i);
        }
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (mGestureDetector.onTouchEvent(motionEvent))
            return true;

        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_UP:
                endGesture();
                break;
            case MotionEvent.ACTION_DOWN:
                break;
        }

        return false;
    }

    private GestureListener mGestureListener;

    public void setLive(boolean b) {
        this.isLive = b;
    }

    public interface GestureListener {
        void onVolumeSlide(int i);

        void onBrightnessSlide(String s);

        void onProgressSlide(int showDelta, long newPosition, long duration);

        void onEndGesture();

    }
}
