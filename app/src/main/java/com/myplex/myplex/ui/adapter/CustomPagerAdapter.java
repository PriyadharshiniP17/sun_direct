package com.myplex.myplex.ui.adapter;

import android.content.Context;
import androidx.viewpager.widget.PagerAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.myplex.model.FilterItem;
import com.myplex.myplex.R;
import com.myplex.myplex.ui.activities.MainActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.androidquery.util.AQUtility.getContext;

public class CustomPagerAdapter extends PagerAdapter implements AdapterFilterItems.OnItemClickListener {

    private final List<FilterItem> mFilterLanguages;
    private final OnItemClickListener mOnItemClickListener;
    private int mSectionType;

    public List<FilterItem> getmFilterLanguages() {
        return mFilterLanguages;
    }

    public List<FilterItem> getmFilterGenres() {
        return mFilterGenres;
    }

    private final List<FilterItem> mFilterGenres;
    private Context mContext;
    private RecyclerView[] rvFilterItems;


    public CustomPagerAdapter(Context context, List<FilterItem> mFilterLanguages, List<FilterItem> mFilterGeners, OnItemClickListener onItemClickListener) {
        mContext = context;
        this.mFilterLanguages = mFilterLanguages;
        this.mFilterGenres = mFilterGeners;
        this.mOnItemClickListener = onItemClickListener;
        rvFilterItems = new RecyclerView[getCount()];
    }

    @Override
    public Object instantiateItem(ViewGroup collection, int position) {

        LayoutInflater inflater = LayoutInflater.from(mContext);
        ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.fragment_fragment_filter, collection, false);

        rvFilterItems[position] = (RecyclerView) layout.findViewById(R.id.rvFilterItems);
        if (position == 0)
            setmFilterItems(mFilterLanguages, position);
        if (position == 1)
            setmFilterItems(mFilterGenres, position);
        collection.addView(layout);
        return layout;
    }

    public void setmFilterItems(List<FilterItem> mFilterItems, int position) {
        AdapterFilterItems mFilterItemsAdapter = new AdapterFilterItems(mFilterItems);
        mFilterItemsAdapter.setOnItemClickListener(this);
        LinearLayoutManager manager = new LinearLayoutManager(getContext());
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        mFilterItemsAdapter.setItemType(getItemType(position));
        rvFilterItems[position].setLayoutManager(manager);
        rvFilterItems[position].setAdapter(mFilterItemsAdapter);
    }

    private String getItemType(int position) {
        switch (position) {
            case 0:
                return mContext.getResources().getString(R.string.languages_txt);
            case 1:
                return mContext.getResources().getString(R.string.genre_txt);
            default:
                return "";
        }
    }

    @Override
    public void destroyItem(ViewGroup collection, int position, Object view) {
        collection.removeView((View) view);
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return getItemType(position);
    }

    private HashMap<Integer, ArrayList<String>> collectUpdatedFilterItems() {
        HashMap<Integer, ArrayList<String>> filterMap = new HashMap<>();
        int genrePosition = 0;
        int languagePosition = 1;
        if (mSectionType == MainActivity.SECTION_MOVIES) {
            genrePosition = 1;
            languagePosition = 0;
        }
        ArrayList<String> langItemsList = new ArrayList<>();
        ArrayList<String> genreItemsList = new ArrayList<>();
        for (int i = 0; i < ((mFilterLanguages.size() > mFilterGenres.size()) ? mFilterLanguages.size() : mFilterGenres.size()); i++) {
            if (mFilterLanguages.size() > i)
                if (mFilterLanguages.get(i).isChecked())
                    langItemsList.add(mFilterLanguages.get(i).getTitle());
            if (mFilterGenres.size() > i)
                if (mFilterGenres.get(i).isChecked())
                    genreItemsList.add(mFilterGenres.get(i).getTitle());

        }
        filterMap.put(genrePosition, genreItemsList);
        filterMap.put(languagePosition, langItemsList);
        return filterMap;
    }

    @Override
    public void onClick(String itemType, FilterItem item, int position) {
        if (itemType.equalsIgnoreCase(mContext.getResources().getString(R.string.languages_txt))) {
            if (mFilterLanguages != null && mFilterLanguages.size() > position) {
                mFilterLanguages.get(position).setIsChecked(item.isChecked());
            }
        }
        if (itemType.equalsIgnoreCase(mContext.getResources().getString(R.string.genre_txt))) {
            if (mFilterGenres != null && mFilterGenres.size() > position) {
                mFilterGenres.get(position).setIsChecked(item.isChecked());
            }
        }
    }

    public void filterOnClickApply() {
        HashMap<Integer, ArrayList<String>> listenerFilterMap = collectUpdatedFilterItems();
        mOnItemClickListener.onItemClicked(listenerFilterMap);
        //EventBus.getDefault().post(new UpdateFilterDataEvent(listenerFilterMap));
    }

    public void reset() {
        for (int i = 0; i < ((mFilterLanguages.size() > mFilterGenres.size()) ? mFilterLanguages.size() : mFilterGenres.size()); i++) {
            if (mFilterLanguages.size() > i)
                mFilterLanguages.get(i).setIsChecked(false);
            if (mFilterGenres.size() > i)
                mFilterGenres.get(i).setIsChecked(false);
        }
        for(int i=0;i<rvFilterItems.length;i++){
            if(rvFilterItems[i]!=null)
            rvFilterItems[i].getAdapter().notifyDataSetChanged();
            else
                Log.e("Adapter","NULL");
        }

    }

    public void setFilterSectionType(int filterSectionType) {
        this.mSectionType = filterSectionType;
    }

    public boolean isFiltersAvailable() {
        for (int i = 0; i < (((mFilterLanguages == null ? 0 : mFilterLanguages.size()) > (mFilterGenres == null ? 0 : mFilterGenres.size())) ? (mFilterLanguages == null ? 0 : mFilterLanguages.size()) : (mFilterGenres == null ? 0 : mFilterGenres.size())); i++) {
            if (mFilterLanguages != null && mFilterLanguages.size() > i && mFilterLanguages.get(i) != null && mFilterLanguages.get(i).isChecked())
                return mFilterLanguages.get(i).isChecked();
            if (mFilterGenres != null && mFilterGenres.size() > i && mFilterGenres.get(i) != null && mFilterGenres.get(i).isChecked())
                return mFilterGenres.get(i).isChecked();
        }
        return false;
    }

    public interface OnItemClickListener {
       public void onItemClicked(HashMap<Integer, ArrayList<String>> filterMap);
    }

}