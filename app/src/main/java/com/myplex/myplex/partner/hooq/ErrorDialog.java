/*
 * Copyright Quickplay Media Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of Quickplay Media Inc. and is protected by copyright
 * law. No license, implied or otherwise is granted by its use, unless licensed by Quickplay Media Inc.
 */
package com.myplex.myplex.partner.hooq;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.myplex.myplex.R;

import java.util.ArrayList;
import java.util.List;


/**
 * Fragment used to display a ErrorInfo object.
 */
public class ErrorDialog extends DialogFragment {
    private static final String ERROR_INFO_KEY = "ERROR_INFO";
    private TextView mErrorContent;


    public static ErrorDialog newInstance( ) {
        ErrorDialog newFragment = new ErrorDialog();

        Bundle args = new Bundle();

        newFragment.setArguments(args);

        return newFragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();


        View contentView = inflater.inflate(R.layout.fragment_error_dialog, null);
        mErrorContent = (TextView) contentView.findViewById(R.id.error_dialog_content);

        AlertDialog.Builder b=  new  AlertDialog.Builder(getActivity())
                .setTitle("Error <" +  ":" +  ">")
                .setPositiveButton("Copy Text",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                copyText();
                            }
                        })
                .setNegativeButton("Dismiss",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.dismiss();
                            }
                        }
                );
        b.setView(contentView);

        List<String> options = getOptionKeys();
        b.setSingleChoiceItems(options.toArray(new CharSequence[options.size()]), 0,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        showContent(which);
                    }
                });
        showContent(0);


        return b.create();
    }

    private void showContent(int contentIndex) {
        List<Pair<String, String>> optionData = getOptions();
        Pair<String, String> optionInfo = optionData.get(contentIndex);
        showContent(optionInfo.second);
    }

    private void showContent(String content) {
        if(content == null || content.length() == 0) {
            content = "Not Available";
        }
        mErrorContent.setText(content);
    }

    private List<String> getOptionKeys() {
        List<String> optionKeys = new ArrayList<>();
        List<Pair<String, String>> optionData = getOptions();
        for(Pair<String, String> option : optionData) {
            optionKeys.add(option.first);
        }
        return optionKeys;
    }

    private List<Pair<String, String>> getOptions() {
        List<Pair<String, String>> options = new ArrayList<>();



        return options;
    }

    private void copyText() {
        SafeToast.showAnyThread("Copied to Clipboard!");
    }
}
