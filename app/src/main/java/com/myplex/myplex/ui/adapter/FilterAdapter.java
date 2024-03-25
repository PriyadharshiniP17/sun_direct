package com.myplex.myplex.ui.adapter;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.myplex.model.FilterData;
import com.myplex.model.FilterItem;
import com.myplex.myplex.R;
import com.myplex.myplex.events.UpdateFilterDataEvent;
import com.myplex.myplex.ui.activities.MainActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by phani on 12/23/2015.
 */
public class FilterAdapter extends BaseExpandableListAdapter {
    private Context mContext;
    private LinearLayout mApplyLayut;
    private ArrayList<String> genreItemsList;
    private ArrayList<String> langItemsList;

    private List<FilterData> mFilterGroupList; // header titles
    // private HashMap<Integer, ArrayList<String>> filterMap;
    private HashMap<Integer, ArrayList<String>> listenerFilterMap;

    private Handler mNextSlideHandler = new Handler();
    private ExpandableListView eLV;
    private ImageView grpSelectorImg;
    private boolean isExpandDelay = false;
    private OnItemClickListener mOnItemClickListener;
    private int mSectionType;

    public void filterOnClickApply() {
        HashMap<Integer, ArrayList<String>> listenerFilterMap = collectUpdatedFilterItems();
        mOnItemClickListener.onItemClicked(listenerFilterMap);
        EventBus.getDefault().post(new UpdateFilterDataEvent(listenerFilterMap));
    }

    public void setFilterSectionType(int mSectionType) {
        this.mSectionType = mSectionType;
    }


    public interface OnItemClickListener {
        void onItemClicked(HashMap<Integer, ArrayList<String>> filterMap);
    }


    private HashMap<Integer, ArrayList<String>> collectUpdatedFilterItems() {
        HashMap<Integer, ArrayList<String>> filterMap = new HashMap<>();
        genreItemsList = new ArrayList<>();
        langItemsList = new ArrayList<>();
        int genrePosition = 0;
        int languagePosition = 1;
        if (mSectionType == MainActivity.SECTION_MOVIES) {
            genrePosition = 1;
            languagePosition = 0;
        }
        List<FilterItem> genreData = mFilterGroupList.get(genrePosition).mFilterItemList;
        List<FilterItem> langData = mFilterGroupList.get(languagePosition).mFilterItemList;
        for (int i = 0; i < genreData.size(); i++) {
            if (genreData.get(i).isChecked()) {
                genreItemsList.add(genreData.get(i).getTitle());
            }
        }
        for (int i = 0; i < langData.size(); i++) {
            if (langData.get(i).isChecked()) {
                langItemsList.add(langData.get(i).getTitle());
            }
        }
        filterMap.put(genrePosition, genreItemsList);
        filterMap.put(languagePosition, langItemsList);
        return filterMap;


    }


    public FilterAdapter(Context context, List<FilterData> mFilterGroupList, OnItemClickListener mOnItemClickListener) {
        mContext = context;
        this.mFilterGroupList = mFilterGroupList;
        this.mOnItemClickListener = mOnItemClickListener;
       /* this.mApplyLayut = mApplyLayout;
        mApplyLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mContext,"Filter adap ",Toast.LENGTH_SHORT).show();
            }
        });*/
        // this.filterMap = filterMap;
        // applyBtn.setTag(filterMap);
        //applyBtn.setOnClickListener(mApplyOnClickListener);
        /*if (filterMap.size() == 0) {
            langItemsList = new ArrayList<>();
            genreItemsList = new ArrayList<>();
            return;
        }
        if (filterMap.size() > 0 && filterMap.containsKey(0)) {
            genreItemsList = filterMap.get(0);
        } else {
            genreItemsList = new ArrayList<>();
        }

        if (filterMap.size() > 0 && filterMap.containsKey(1)) {
            langItemsList = filterMap.get(1);
        } else {
            langItemsList = new ArrayList<>();
        }*/


    }


    @Override
    public int getGroupCount() {
        return mFilterGroupList.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return mFilterGroupList.get(groupPosition).mFilterItemList.size();
    }

    @Override
    public FilterData getGroup(int groupPosition) {
        return mFilterGroupList.get(groupPosition);
    }

    @Override
    public String getChild(int groupPosition, int childPosition) {
        FilterItem filterItem = mFilterGroupList.get(groupPosition).mFilterItemList.get(childPosition);
        return filterItem.getTitle();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }


    @Override
    public View getGroupView(final int groupPosition, final boolean isExpanded, View convertView, ViewGroup parent) {
        final ViewHolder mViewHolder;
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.carddetailtitlesectionview, null);
            mViewHolder = new ViewHolder();
            mViewHolder.mItemText = (TextView) convertView.findViewById(R.id.carddetail_title_text);
            mViewHolder.mItemMinus = (ImageView) convertView.findViewById(R.id.carddetail_minusbar);
            mViewHolder.mItemMinus.setVisibility(View.VISIBLE);
            mViewHolder.mItemClear = (ImageView) convertView.findViewById(R.id
                    .thumbnail_filter_clear);
            mViewHolder.mApLinearLayout = (LinearLayout) convertView.findViewById(R.id.apply_layout);
            // convertView.setBackgroundColor(mContext.getResources().getColor(R.color.list_item_bkg));
            grpSelectorImg = mViewHolder.mItemMinus;

            convertView.setTag(mViewHolder);
            // ((ExpandableListView) parent).expandGroup(groupPosition);
        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }
        mViewHolder.mApLinearLayout.setVisibility(View.GONE);
        mViewHolder.mFilterData = mFilterGroupList.get(groupPosition);
        mViewHolder.mItemText.setText(mViewHolder.mFilterData.title);
        if (checkBoxesChecked(mFilterGroupList.get(groupPosition))) {
            mViewHolder.mItemClear.setVisibility(View.VISIBLE);
            mViewHolder.mItemClear.setTag(groupPosition);
        } else {
            mViewHolder.mItemClear.setVisibility(View.GONE);
            mViewHolder.mItemClear.setTag(groupPosition);
        }
        eLV = (ExpandableListView) parent;
        if (isExpanded) {
            mViewHolder.mItemMinus.setImageResource(R.drawable.filter_collapse_icon);
        } else {
            mViewHolder.mItemMinus.setImageResource(R.drawable.filter_expand_icon);
        }
        if (!isExpandDelay) {
            isExpandDelay = true;
            mNextSlideHandler.postDelayed(mNextSlideTimeTask, 1000);
        }

        mViewHolder.mItemMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mNextSlideHandler.removeCallbacksAndMessages(null);
                if (isExpanded) {
                    eLV.collapseGroup(groupPosition);
                    mViewHolder.mItemMinus.setImageResource(R.drawable.filter_expand_icon);
                } else {
                    eLV.expandGroup(groupPosition);
                    mViewHolder.mItemMinus.setImageResource(R.drawable.filter_collapse_icon);

                }
            }
        });
        mViewHolder.mItemClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int groupPosition = (int) v.getTag();
                if (groupPosition >= 0) {
                    clearSelected(mFilterGroupList.get(groupPosition).mFilterItemList);
                }
            }
        });

        return convertView;
    }

    private boolean checkBoxesChecked(FilterData filterData) {
        for (FilterItem filterItem : filterData.mFilterItemList) {
            if (filterItem.isChecked()) {
                return true;
            }
        }
        return false;
    }

    private void clearSelected(List<FilterItem> mFilterItemList) {
        for (FilterItem childItem : mFilterItemList) {
            childItem.setIsChecked(false);
        }
        notifyDataSetChanged();
    }

    private Runnable mNextSlideTimeTask = new Runnable() {
        @Override
        public void run() {
            eLV.expandGroup(0);
        }
    };

    @Override
    public View getChildView(final int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        final ViewHolder mViewHolder;
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.listitem_settings_genre, null);
            mViewHolder = new ViewHolder();
            mViewHolder.mItemText = (TextView) convertView.findViewById(R.id.settings_item_text);
            mViewHolder.mItemCheckBox = (ImageView) convertView.findViewById(R.id.settings_item_checkbox);
            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }
        final FilterItem filter = mFilterGroupList.get(groupPosition).mFilterItemList.get(childPosition);
        mViewHolder.mItemText.setText(filter.getTitle());
        mViewHolder.checked = filter.isChecked();
        final View groupView = getGroupView(groupPosition, true, null, eLV);
        if (mViewHolder.checked) {
            ViewHolder viewHolder = (ViewHolder) groupView.getTag();
            viewHolder.mItemClear.setVisibility(View.VISIBLE);
            mViewHolder.mItemCheckBox.setImageResource(R.drawable.checkbox_selected);
        } else {
            ViewHolder viewHolder = (ViewHolder) groupView.getTag();
            viewHolder.mItemClear.setVisibility(View.GONE);
            mViewHolder.mItemCheckBox.setImageResource(R.drawable.checkbox_selector_bg);
        }

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkedStatus(groupPosition, childPosition)) {
                    final View groupView = getGroupView(groupPosition, true, null, eLV);
                    ViewHolder viewHolder = (ViewHolder) groupView.getTag();
                    viewHolder.mItemClear.setVisibility(View.VISIBLE);

                    if (getChild(groupPosition, childPosition).equals("All")) {
                        if (groupPosition == 0) {
                            for (int i = 0; i < mFilterGroupList.get(0).mFilterItemList.size(); i++) {
                                FilterItem item = mFilterGroupList.get(0).mFilterItemList.get(i);
                                item.setIsChecked(false);
                                notifyDataSetChanged();
                            }
                            for (int j = 0; j < mFilterGroupList.get(0).mFilterItemList.size(); j++) {
                                genreItemsList.remove(mFilterGroupList.get(0).mFilterItemList.get(j).getTitle());
                                //filterMap.put(0,genreItemsList);
                            }
                        } else {
                            for (int i = 0; i < mFilterGroupList.get(1).mFilterItemList.size(); i++) {
                                FilterItem item = mFilterGroupList.get(1).mFilterItemList.get(i);
                                item.setIsChecked(false);
                                notifyDataSetChanged();
                            }
                            for (int j = 0; j < mFilterGroupList.get(1).mFilterItemList.size(); j++) {
                                langItemsList.remove(mFilterGroupList.get(1).mFilterItemList.get(j).getTitle());
                                // filterMap.put(groupPosition,langItemsList);
                            }
                        }
                        mViewHolder.mItemCheckBox.setImageResource(R.drawable.checkbox_selector_bg);
                        // applyBtn.setTag(filterMap);
                        return;
                    }

                    mViewHolder.mItemCheckBox.setImageResource(R.drawable.checkbox_selector_bg);
                    updateCheckedPosition(false, groupPosition, childPosition);
                   /* if (groupPosition == 0) {
                        genreItemsList.remove(getChild(groupPosition, childPosition));
                        filterMap.put(groupPosition, genreItemsList);
                    } else {
                        langItemsList.remove(getChild(groupPosition, childPosition));
                        filterMap.put(groupPosition, langItemsList);
                    }*/
                   /* for(int i =0;i<genreItemsList.size();i++){
                        if(genreItemsList.get(i).equals("All")){
                             for(int j =0;j< mFilterGroupList.get(groupPosition).mFilterItemList.size() ;j++){
                                 if( mFilterGroupList.get(groupPosition).mFilterItemList.get(j).getTitle().equals("All")){
                                     mFilterGroupList.get(groupPosition).mFilterItemList.get(j).setIsChecked(false);
                                     notifyDataSetChanged();
                                 }
                             }

                        }
                    }*/

                    //  applyBtn.setTag(filterMap);
                    return;

                }
                if (getChild(groupPosition, childPosition).equals("All")) {


                    if (groupPosition == 0) {
                        for (int i = 0; i < mFilterGroupList.get(0).mFilterItemList.size(); i++) {
                            FilterItem item = mFilterGroupList.get(0).mFilterItemList.get(i);
                            item.setIsChecked(true);
                            notifyDataSetChanged();


                        }
                        for (int j = 0; j < mFilterGroupList.get(0).mFilterItemList.size(); j++) {
                            genreItemsList.add(mFilterGroupList.get(0).mFilterItemList.get(j).getTitle());
                            //filterMap.put(0, genreItemsList);


                        }
                    } else {
                        for (int i = 0; i < mFilterGroupList.get(1).mFilterItemList.size(); i++) {
                            FilterItem item = mFilterGroupList.get(1).mFilterItemList.get(i);
                            item.setIsChecked(true);
                            notifyDataSetChanged();


                        }
                        for (int j = 0; j < mFilterGroupList.get(1).mFilterItemList.size(); j++) {
                            langItemsList.add(mFilterGroupList.get(1).mFilterItemList.get(j).getTitle());
                            // filterMap.put(1, langItemsList);


                        }
                    }
                    mViewHolder.mItemCheckBox.setImageResource(R.drawable.checkbox_selector_bg);
                    //applyBtn.setTag(filterMap);
                    return;
                }

                final View groupView = getGroupView(groupPosition, true, null, eLV);
                ViewHolder viewHolder = (ViewHolder) groupView.getTag();
                viewHolder.mItemClear.setVisibility(View.VISIBLE);
                mViewHolder.mItemCheckBox.setImageResource(R.drawable.checkbox_selected);
                updateCheckedPosition(true, groupPosition, childPosition);
                    /*if (groupPosition == 0) {
                        genreItemsList.add(getChild(groupPosition, childPosition));
                        filterMap.put(groupPosition, genreItemsList);
                    } else {
                        langItemsList.add(getChild(groupPosition, childPosition));
                        filterMap.put(groupPosition, langItemsList);
                    }*/
                // applyBtn.setTag(filterMap);

            }


        });

        return convertView;
    }

    private boolean checkedStatus(int groupPosition, int childPosition) {
        return mFilterGroupList.get(groupPosition).mFilterItemList.get(childPosition).isChecked();
    }


    private void updateCheckedPosition(boolean isChecked, int groupPosition, int childPosition) {
        mFilterGroupList.get(groupPosition).mFilterItemList.get(childPosition).setIsChecked(isChecked);
//        notifyDataSetChanged();

    }


    @Override
    public boolean areAllItemsEnabled() {
        return true;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    public class ViewHolder {
        TextView mItemText;
        ImageView mItemCheckBox;
        ImageView mItemClear;
        ImageView mItemMinus;
        FilterData mFilterData;
        boolean checked;
        LinearLayout mApLinearLayout;
    }

}