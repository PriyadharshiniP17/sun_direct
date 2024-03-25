package com.myplex.myplex.ui.component;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.myplex.myplex.R;
import com.myplex.myplex.ui.fragment.dummy.DetailsViewContent;
import com.myplex.myplex.ui.views.CardDetailViewFactory;

import java.util.List;

public class FooterProgressLoadingComponent extends GenericListViewCompoment{
    private final List<DetailsViewContent.DetailsViewDataItem> mValues;
    private final ProgressBar progressBar;
    private final CardDetailViewFactory.CardDetailViewFactoryListener mListener;
    private Context mContext;


    public FooterProgressLoadingComponent(Context mContext, List<DetailsViewContent.DetailsViewDataItem> data, View view, CardDetailViewFactory.CardDetailViewFactoryListener listener) {
        super(view);
        this.mContext = mContext;
        this.mValues = data;
        this.mListener = listener;
        progressBar = view.findViewById(R.id.footer_progressbar);

    }

    public static FooterProgressLoadingComponent createView(Context context, List<DetailsViewContent.DetailsViewDataItem> data, ViewGroup parent, CardDetailViewFactory.CardDetailViewFactoryListener listener) {
        View view = LayoutInflater.from(context).inflate(R.layout.view_footer_layout,
                parent, false);
        FooterProgressLoadingComponent briefDescriptionComponent = new FooterProgressLoadingComponent(context, data, view, listener);
        return briefDescriptionComponent;
    }

    @Override
    public void bindItemViewHolder(int position) {
    }
}
