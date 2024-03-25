package com.myplex.myplex.ui.adapter;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.myplex.myplex.R;
import com.myplex.myplex.utils.FontRegularTypeface;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by apalya on 12/18/2016.
 */

public class AdapterRatingFeedBackPopUP extends RecyclerView.Adapter<AdapterRatingFeedBackPopUP.CarouselDataViewHolder>{

    private static final String TAG = AdapterRatingFeedBackPopUP.class.getSimpleName();
    private boolean mIsDummyData = false;

    private final Context mContext;
    private List<String> mListString;
    private ArrayList<Boolean> mListBoolean;


    //adapter view click listener

    public AdapterRatingFeedBackPopUP(Context context, List<String> itemList) {
        mContext = context;
        mListString = itemList;
        mListBoolean=new ArrayList<Boolean>(Arrays.asList(new Boolean[itemList.size()]));
        Collections.fill(mListBoolean, Boolean.FALSE);
    }




    @Override
    public CarouselDataViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view;
        CarouselDataViewHolder customItemViewHolder;
        view = LayoutInflater.from(mContext).inflate(R.layout.rating_feedback_items, parent, false);

           /* int viewSize = ApplicationController.getApplicationConfig().screenHeight / 7;
            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
            layoutParams.height = viewSize;
            layoutParams.width = viewSize;
            view.requestLayout();*/


        customItemViewHolder = new CarouselDataViewHolder(view);

        return customItemViewHolder;
    }

    @Override
    public void onBindViewHolder(CarouselDataViewHolder holder, int position) {
        //Log.d(TAG,"onBindViewHolder" + position);
        bindGenreViewHolder(holder, mListString.get(position),position);
    }


    private void bindGenreViewHolder(final CarouselDataViewHolder holder, final String carouselData,final int position) {


        if (carouselData != null) {

            //Log.d(TAG,"bindGenreViewHolder" + mListBoolean.get(position));
            holder.mButtonCategory.setTextSize(12);
            holder.mButtonCategory.setTextAppearance(mContext, R.style.TextAppearance_FontRegular);
            holder.mButtonCategory.setTextColor(mContext.getResources().getColor(R.color.white));
            holder.mButtonCategory.setText(carouselData);
            holder.mButtonCategory.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Log.d(TAG,"onCLlick" +mListBoolean.get(position));
                    mListBoolean.set(position,!mListBoolean.get(position));
                    if (mListBoolean.get(position)){
                        holder.mButtonCategory.setBackgroundResource(R.drawable.rounded_btn_red);
                    }else{
                        holder.mButtonCategory.setBackgroundResource(R.drawable.rounded_btn);
                    }
                }
            });



        }




//            }
//        });

    }

    @Override
    public int getItemCount() {
        if(mListString == null) return 0;

        return mListString.size();
    }

    private static final int TYPE_ITEM = 1;
    @Override
    public int getItemViewType(int position) {

        return TYPE_ITEM;


    }

    public String getSelectedFields(){
        String commaSeparatedString = "";
        for(int i =0;i<mListBoolean.size();i++){
            if(mListBoolean.get(i)){
                if (commaSeparatedString.isEmpty()){
                    commaSeparatedString = mListString.get(i);
                }else{
                    commaSeparatedString = commaSeparatedString+" | "+mListString.get(i);
                }
            }
        }
        return commaSeparatedString;
    }

    /**
     * ViewHolder of the related movies element which contains reference
     * to related movies recyclerView and textView header of that element
     */
    class CarouselDataViewHolder extends RecyclerView.ViewHolder  {

        private View.OnClickListener clickListener;

        final FontRegularTypeface mButtonCategory;



        public CarouselDataViewHolder(View view) {
            super(view);
            mButtonCategory = (FontRegularTypeface)view.findViewById(R.id.category);

        }

        public void setClickListener(View.OnClickListener itemClickListener) {
            this.clickListener = itemClickListener;
        }



    }

    // Overriding this method to get access to recyclerView on which current MovieAdapter has been attached.
    // In the future we will use that reference for scrolling to newly added relatedMovies item
    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        RecyclerView mRecyclerViewMovies = recyclerView;
        recyclerView.getRecycledViewPool().setMaxRecycledViews(TYPE_ITEM, 10);
        recyclerView.setItemViewCacheSize(10);
    }





}
