package com.myplex.myplex.model;

import android.view.View;


public interface ItemClickListener {
    void onClick(View view, int position, int parentPosition, String name);
}
