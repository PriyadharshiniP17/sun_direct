package com.myplex.myplex.ui.component;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.myplex.util.AlertDialogUtil;
import com.myplex.myplex.R;
import com.myplex.myplex.ui.fragment.dummy.DetailsViewContent;
import com.myplex.myplex.ui.views.CardDetailViewFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class PlayerLogsTitleViewComponent extends GenericListViewCompoment{
    private final List<DetailsViewContent.DetailsViewDataItem> mValues;
    private final CardDetailViewFactory.CardDetailViewFactoryListener mListener;
    private final View saveLogs;
    private Context mContext;


    public PlayerLogsTitleViewComponent(final Context mContext, final List<DetailsViewContent.DetailsViewDataItem> data, View view, CardDetailViewFactory.CardDetailViewFactoryListener listener) {
        super(view);
        this.mContext = mContext;
        this.mValues = data;
        this.mListener = listener;
        saveLogs = view.findViewById(R.id.save_logs_btn);
        saveLogs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getAdapterPosition() < 0 || getAdapterPosition() >= mValues.size()) {
                    return;
                }
                DetailsViewContent.DetailsViewDataItem  item = mValues.get(getAdapterPosition());
                String path = Environment.getExternalStorageDirectory() + File.separator + "playerlogs.txt";

                try {
                    File file = new File(path);
                    file.createNewFile();
                    if (file.exists()) {
                        OutputStream fo = new FileOutputStream(file);
                        fo.write(item.title.getBytes());
                        fo.close();
                    }
                    AlertDialogUtil.showToastNotification("Logs saved at " + path);
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("text/plain");
                    intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"dev@apalya.myplex.tv", "qa@apalya.myplex.tv"});
                    intent.putExtra(Intent.EXTRA_SUBJECT, "Player Logs");
                    String manufacturer = Build.MANUFACTURER;
                    String model = Build.MODEL;
                    intent.putExtra(Intent.EXTRA_TEXT, "Please find the attached logs for " + manufacturer + " " + model);
                    Uri uri = Uri.parse("file://" + file);
                    intent.putExtra(Intent.EXTRA_STREAM, uri);
                    try {
                        mContext.startActivity(Intent.createChooser(intent, "Send email..."));
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
    }

    public static PlayerLogsTitleViewComponent createView(Context context, List<DetailsViewContent.DetailsViewDataItem> data, ViewGroup parent, CardDetailViewFactory.CardDetailViewFactoryListener listener) {
        View view = LayoutInflater.from(context).inflate(R.layout.player_logs_title_layout,
                parent, false);
        PlayerLogsTitleViewComponent briefDescriptionComponent = new PlayerLogsTitleViewComponent(context, data, view, listener);
        return briefDescriptionComponent;
    }

    @Override
    public void bindItemViewHolder(int position) {
//        DetailsViewContent.DetailsViewDataItem viewData = mValues.get(position);
    }
}
