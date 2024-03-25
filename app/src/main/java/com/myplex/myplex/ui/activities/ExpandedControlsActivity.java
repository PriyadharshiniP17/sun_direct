/*
 * Copyright (C) 2016 Google Inc. All Rights Reserved.
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

package com.myplex.myplex.ui.activities;

import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;

import com.myplex.myplex.R;
import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.media.widget.ExpandedControllerActivity;
import com.myplex.api.APIConstants;
import com.myplex.util.SDKLogger;

/**
 * An example of extending {@link ExpandedControllerActivity} to add a cast button.
 */
public class ExpandedControlsActivity extends ExpandedControllerActivity {


    // RemoteMediaClient.Listener  -   deprecated


    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        SDKLogger.debug("ExpandedControlsActivity -- onCreate");
        boolean isSubtitleAvailable = false;
        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            isSubtitleAvailable = extras.getBoolean(APIConstants.IS_SUBTITLES_AVAILABLE);
        }

        ImageView cc = getButtonImageViewAt(0);
        if(!isSubtitleAvailable)
            cc.setVisibility(View.INVISIBLE);
        else
            cc.setVisibility(View.VISIBLE);

    }

    @Override
    protected void onResume() {
        super.onResume();
        SDKLogger.debug("ExpandedControlsActivity -- onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        SDKLogger.debug("ExpandedControlsActivity -- onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        SDKLogger.debug("ExpandedControlsActivity -- onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SDKLogger.debug("ExpandedControlsActivity -- onDestroy");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.cast_menu, menu);
        CastButtonFactory.setUpMediaRouteButton(this, menu, R.id.media_route_menu_item);
        return true;
    }
}