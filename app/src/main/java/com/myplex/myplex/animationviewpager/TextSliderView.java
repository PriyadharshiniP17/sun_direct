package com.myplex.myplex.animationviewpager;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.pedrovgs.LoggerD;
import com.myplex.myplex.R;


/**
 * This is a slider with a description TextView.
 */
public class TextSliderView extends BaseSliderView {
    public TextSliderView(Context context) {
        super(context);
    }

    @Override
    public View getView() {
        View v = LayoutInflater.from(getContext()).inflate(R.layout.render_type_text, null);
        ImageView target = (ImageView) v.findViewById(R.id.daimajia_slider_image);
        TextView description = (TextView) v.findViewById(R.id.description);
        ImageView vf_logo = (ImageView) v.findViewById(R.id.logo_onboarding);
        bindEventAndShow(v, target);
        vf_logo.setVisibility(View.GONE);
        description.setText(getDescription());
        LoggerD.debugDownload("vf_logo url- " + imageUrl);
        if (!TextUtils.isEmpty(imageUrl)
                && imageUrl.contains("?overlay=vflogo")) {
            vf_logo.setVisibility(View.VISIBLE);
        }
        return v;
    }
}
