package com.myplex.myplex.ui.component;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.myplex.myplex.R;
import com.myplex.myplex.ui.fragment.dummy.DetailsViewContent;
import com.myplex.myplex.ui.views.CardDetailViewFactory;

import java.util.List;

public class DummyItemView extends GenericListViewCompoment {

    RelativeLayout dummyLayout;

    public DummyItemView(Context mContext, List<DetailsViewContent.DetailsViewDataItem> data,
                         View view, CardDetailViewFactory.CardDetailViewFactoryListener listener) {
        super(view);
        dummyLayout=view.findViewById(R.id.layout_item_episode);
    }

    public static DummyItemView createView(Context context, List<DetailsViewContent.DetailsViewDataItem> data,
                                                 ViewGroup parent, CardDetailViewFactory.CardDetailViewFactoryListener listener) {
        View view = LayoutInflater.from(context).inflate(R.layout.dummy_view,
                parent, false);
        DummyItemView briefDescriptionComponent = new DummyItemView(context, data, view, listener);
        return briefDescriptionComponent;
    }

    @Override
    public void bindItemViewHolder(int position) {
        dummyLayout.setVisibility(View.INVISIBLE);
    }
}
