package com.myplex.myplex.model;

import android.view.View;

import com.myplex.model.CardDataRelatedMultimediaItem;

public interface ItemClickListenerWithRelatedMediaData {
    void onClick(View view, int position, int parentPosition, CardDataRelatedMultimediaItem movieData);
}