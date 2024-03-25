package com.myplex.model;

public interface MiniPlayerStatusUpdate extends PlayerStatusUpdate{
    void onCompleted(boolean isAd);
    void onClosePlayer();
    void onPlayerStarted(boolean isAd);
    void onUpdatePlayerData(CardData mData);
    int getMiniPlayerState();
    void onPlayerBackPressed();
    boolean isFragmentVisible();
    void changeMiniplayerState(int state);
    void startInLandscape();
    boolean isDragging();
}