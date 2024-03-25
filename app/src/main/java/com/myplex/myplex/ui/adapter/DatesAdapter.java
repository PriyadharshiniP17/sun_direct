package com.myplex.myplex.ui.adapter;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.text.style.SuperscriptSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.myplex.api.APIConstants;
import com.myplex.util.StringManager;
import com.myplex.myplex.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by phani on 12/23/2015.
 */
public class DatesAdapter extends BaseAdapter {
    private Context mContext;
    private List<String> datesList;
    private List<String> datesListVernacularLang;
    private int adapterType;
    private int selectedPosition;
    private String seasonInHindi = StringManager.getInstance().getString(APIConstants.SEASON);

    public DatesAdapter(Context context, List<String> datesList) {
        this(context, AdapterType.DATES, datesList);
    }

    public DatesAdapter(Context context, int adapterType, List<String> datesList) {
        mContext = context;
        this.datesList = datesList;
        this.adapterType = adapterType;
        if (datesList != null && datesList.size() > 0) {
            datesListVernacularLang = new ArrayList<>();
            for (int i = 0; i < datesList.size(); i++) {
                String[] arrSplit = datesList.get(i).split(" ");
                if (arrSplit != null && arrSplit.length > 1) {
                    if (arrSplit[1] != null) {
                        datesListVernacularLang.add(seasonInHindi + " " + arrSplit[1]);
                    } else {
                        datesListVernacularLang.add(seasonInHindi + " " + i);
                    }
                }
            }
        }
    }

    @Override
    public int getCount() {
        if (datesList == null) {
            return 0;
        }
        return datesList.size();
    }

    @Override
    public String getItem(int position) {
        return datesList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder mViewHolder;
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.view_dates_layout,null);
            mViewHolder = new ViewHolder();
            mViewHolder.dateTxt = (TextView)convertView.findViewById(R.id.dateTxt);
            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (ViewHolder)convertView.getTag();
        }

        if (adapterType == AdapterType.SEASONS){
            mViewHolder.dateTxt.setText(datesList.get(position));

            return convertView;
        }

        if(datesList.get(position)!=null && adapterType == AdapterType.DATES){
            Spannable cs = new SpannableString(datesList.get(position));
            if(datesList.get(position).contains("Today")){
                cs.setSpan(new SuperscriptSpan(), 10, 12, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                cs.setSpan(new RelativeSizeSpan(0.7f), 10, 12, 0);
            }else {
                cs.setSpan(new SuperscriptSpan(), 7, 9, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                cs.setSpan(new RelativeSizeSpan(0.7f), 7, 9, 0);
            }

            mViewHolder.dateTxt.setText(cs);

        }
        mViewHolder.dateTxt.setTextColor(mContext.getResources().getColor(R.color.date_text_selector));
        if (selectedPosition == position) {
            mViewHolder.dateTxt.setTextColor(mContext.getResources().getColor(R.color.red_highlight_color));
        }


        return convertView;
    }

    public void addData(List<String> listSeasonNames) {
        if(datesList == null || datesList.isEmpty()){
            datesList = listSeasonNames;
            return;
        }
        datesList.addAll(listSeasonNames);
    }

    public void setSelectedPosition(int selectedPosition) {
        this.selectedPosition = selectedPosition;
    }

    public void setData(List<String> seasonsList) {
        datesList = seasonsList;
    }

    public class ViewHolder {
        TextView dateTxt;
    }

    public static final class AdapterType {
        public static final int DATES = 1;
        public static final int SEASONS = 2;
    }
}
