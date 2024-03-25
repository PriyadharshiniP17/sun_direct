package com.myplex.myplex.model;

public interface NotificationListener {
    void onNotificationClick(int id);

    void onNotificationDelete(int position);

    void onNotificationRemainder(int position, int enable);
}
