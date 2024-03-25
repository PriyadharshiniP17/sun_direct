package com.myplex.myplex.ui.component;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.myplex.myplex.R;
import com.myplex.myplex.ui.fragment.dummy.DetailsViewContent;
import com.myplex.myplex.ui.views.CardDetailViewFactory;

import java.util.List;

public class PlayerLogsViewComponent extends GenericListViewCompoment {
    private final List<DetailsViewContent.DetailsViewDataItem> mValues;
    private final TextView textView;
    private final CardDetailViewFactory.CardDetailViewFactoryListener mListener;
    private Context mContext;


    public PlayerLogsViewComponent(Context mContext, List<DetailsViewContent.DetailsViewDataItem> data, View view, CardDetailViewFactory.CardDetailViewFactoryListener listener) {
        super(view);
        this.mContext = mContext;
        this.mValues = data;
        this.mListener = listener;
        this.textView = (TextView) view;
    }

    public static PlayerLogsViewComponent createView(Context context, List<DetailsViewContent.DetailsViewDataItem> data, ViewGroup parent, CardDetailViewFactory.CardDetailViewFactoryListener listener) {
        View view = LayoutInflater.from(context).inflate(R.layout.center_text_view,
                parent, false);
        PlayerLogsViewComponent briefDescriptionComponent = new PlayerLogsViewComponent(context, data, view, listener);
        return briefDescriptionComponent;
    }

    @Override
    public void bindItemViewHolder(int position) {
        DetailsViewContent.DetailsViewDataItem viewData = mValues.get(position);
        textView.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
        textView.setText(viewData.title);
        textView.setTextColor(mContext.getResources().getColor(R.color.player_logs__text_color));
    }
}
