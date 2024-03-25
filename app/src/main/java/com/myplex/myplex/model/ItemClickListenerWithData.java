package com.myplex.myplex.model;

import android.view.View;

import com.myplex.model.CardData;

public interface ItemClickListenerWithData {
    void onClick(View view, int position, int parentPosition, CardData movieData);
}