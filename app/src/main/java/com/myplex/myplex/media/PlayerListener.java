package com.myplex.myplex.media;

import android.media.MediaPlayer;


public interface PlayerListener {
	// Variables added for Player states
	int STATE_PREPARING = 1;
	int STATE_PREPARED = 2;
	int STATE_PLAYING = 3;
	int STATE_PAUSED = 4;
	int STATE_PLAYBACK_COMPLETED = 5;
	int STATE_STOP = 6;
	int STATE_RESUME = 7;
	int STATE_SUSPEND_UNSUPPORTED = 8;
	int STATE_STARTED = 9;
	int STATE_COMPLETED = 10;

	public void onSeekComplete(MediaPlayer mp, boolean isSeeking) ;

	void onBufferingUpdate(MediaPlayer mp, int perBuffer);


	boolean onError(MediaPlayer mp, int what, int arg2, String errorMessage, String stackTrace);
	public boolean onInfo(MediaPlayer mp, int arg1, int arg2) ;
	void onCompletion(MediaPlayer mp) ;

	void onFullScreen(boolean value);

	void onDrmError();

	void onStateChanged(int state, int pos);
	
	void onRetry();
	
	void onBuffering();

	/*For Hooq player callbacks */
	void onBufferingUpdate(boolean isBuffering);

	boolean onError(Object errorInfo ,String stackTrace);

    void onSubtitleChanged(String subtitleTrack);
}
