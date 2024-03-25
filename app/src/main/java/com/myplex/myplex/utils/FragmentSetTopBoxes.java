package com.myplex.myplex.utils;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.github.pedrovgs.LoggerD;
import com.google.android.material.card.MaterialCardView;
import com.myplex.api.APICallback;
import com.myplex.api.APIConstants;
import com.myplex.api.APIResponse;
import com.myplex.api.APIService;
import com.myplex.api.request.user.UserConnection;
import com.myplex.model.ConnectionResponseData;
import com.myplex.model.SetupBox;
import com.myplex.model.SetupBoxListResponse;
import com.myplex.myplex.R;
import com.myplex.myplex.ui.fragment.BaseFragment;
import com.myplex.util.PropertiesHandler;

import java.util.List;

public class FragmentSetTopBoxes extends BaseFragment {
    private View rootView;
    private RecyclerView setTopBoxesList;
    private AdapterSetTopBoxes adapterSetTopBoxes;
    List<SetupBox> setupBoxList;
    ImageView backNavigation;
    ProgressBar progress;
    RelativeLayout progressLayout;
    Button continueButton;
    public String name, mobilenumber, email, pincode;
    String selectedBox = "";

    public static FragmentSetTopBoxes newInstance(Bundle args) {
        FragmentSetTopBoxes fragmentSetTopBoxes = new FragmentSetTopBoxes();
        fragmentSetTopBoxes.setArguments(args);
        return fragmentSetTopBoxes;
    }

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mContext = getActivity();
        rootView = inflater.inflate(R.layout.fragment_set_top_boxes, container, false);
        setTopBoxesList = rootView.findViewById(R.id.set_top_boxes_recyclerview);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(mContext);
        setTopBoxesList.setHasFixedSize(true);
        setTopBoxesList.setLayoutManager(mLayoutManager);
        backNavigation = rootView.findViewById(R.id.back_navigation);
        continueButton = rootView.findViewById(R.id.continue_button);
        progress = rootView.findViewById(R.id.progress);
        progressLayout=rootView.findViewById(R.id.progress_layout);
        Bundle bundle = getArguments();
        if (bundle.containsKey("name") && !bundle.getString("name").isEmpty()) {
            name = bundle.getString("name");
        }
        if (bundle.containsKey("pincode") && !bundle.getString("pincode").isEmpty()) {
            pincode = bundle.getString("pincode");
        }
        if (bundle.containsKey("emailID") && !bundle.getString("emailID").isEmpty()) {
            email = bundle.getString("emailID");
        }
        if (bundle.containsKey("mobile") && !bundle.getString("mobile").isEmpty()) {
            mobilenumber = bundle.getString("mobile");
        }
        backNavigation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               // getActivity().onBackPressed();
                onBackClicked();
            }

        });
        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < setupBoxList.size(); i++) {
                    if(setupBoxList.get(i).isSelected()) {
                        setupBoxList.get(i).setSelected(false);
                        selectedBox = setupBoxList.get(i).getType();
                    }
                }
                if(selectedBox.isEmpty()) {
                    Toast.makeText(getActivity(), "Please select the atleast one Set up box", Toast.LENGTH_SHORT).show();
                    return;
                }
                setUserConnection(name, mobilenumber, email, pincode, selectedBox);
            }
        });
        SetupBoxListResponse setupBoxListResponse = PropertiesHandler.getSetupBoxList(mContext);
        if(setupBoxListResponse != null) {
            setupBoxList = setupBoxListResponse.setupBoxList;
            if(setupBoxList != null) {
                adapterSetTopBoxes = new AdapterSetTopBoxes();
                setTopBoxesList.setAdapter(adapterSetTopBoxes);
            }
        }
        return rootView;
    }

    @Override
    public boolean onBackClicked() {
        getActivity().onBackPressed();
        return false;
    }


    private void setUserConnection(String name, String mobile, String email, String pincode, String connection) {
        progressLayout.setVisibility(View.VISIBLE);
        progress.setVisibility(View.VISIBLE);
        UserConnection.Params params = new UserConnection.Params(name, mobile, email, pincode, connection);

        final UserConnection requestSMCNumbers = new UserConnection(params, new APICallback<ConnectionResponseData>() {
            @Override
            public void onResponse(APIResponse<ConnectionResponseData> response) {
                if (response != null && response.body() != null && response.body().status != null  && response.body().status.equals(APIConstants.SUCCESS)) {
                    if (response != null && response.body() != null && response.body().message != null) {
                        Toast.makeText(getActivity(), response.body().message, Toast.LENGTH_SHORT).show();
                    }
                    Bundle args = new Bundle();
                    mBaseActivity.pushFragment(FragmentSignIn.newInstance(args));
                    progressLayout.setVisibility(View.GONE);
                    progress.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Throwable t, int errorCode) {
                LoggerD.debugOTP("Failed: " + t);
                progressLayout.setVisibility(View.GONE);
                progress.setVisibility(View.GONE);

            }
        });

        APIService.getInstance().execute(requestSMCNumbers);
    }


    private class AdapterSetTopBoxes extends RecyclerView.Adapter<FragmentSetTopBoxes.ViewHolder> {


        @NonNull
        @Override
        public FragmentSetTopBoxes.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            View view = inflater.inflate(R.layout.set_top_box_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull FragmentSetTopBoxes.ViewHolder holder, int position) {
            holder.title.setText(setupBoxList.get(position).getTitle());
            holder.subTitle.setText(setupBoxList.get(position).getSubTitle());
            holder.setTopBoxCard.setTag(position);
            if (Util.isValidContextForGlide(mContext))
                Glide.with(mContext)
                        .load(setupBoxList.get(position).getLogoUrl())
                        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                        .into(holder.logoImage);

            if(setupBoxList.get(position).isSelected()){
                continueButton.setBackgroundResource(R.drawable.rounded_corner_button_white);
                holder.setTopBoxCard.setStrokeColor(getResources().getColor(R.color.set_top_box_card_bg_clolor));
               // holder.setTopBoxCard.setSelected(true);
            } else {
                holder.setTopBoxCard.setStrokeColor(getResources().getColor(R.color.white_60));
                //holder.setTopBoxCard.setSelected(false);
            }

            holder.setTopBoxCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                  /*  if (!holder.setTopBoxCard.isSelected()) {

                    } else {
                        holder.setTopBoxCard.setStrokeColor(getResources().getColor(R.color.white_60));
                        holder.setTopBoxCard.setSelected(false);
                    }*/
                    int position = (int) view.getTag();
                    for (int i = 0; i < setupBoxList.size(); i++) {
                        if (i == position) {
                            if (setupBoxList.get(i).isSelected())
                                setupBoxList.get(i).setSelected(true);
                            else
                                setupBoxList.get(i).setSelected(true);
                        } else
                            setupBoxList.get(i).setSelected(false);
                    }
                    notifyDataSetChanged();
                    //notifyItemChanged(position);
                }
            });

        }

        @Override
        public int getItemCount() {
            return setupBoxList.size();
        }
    }

    private class ViewHolder extends RecyclerView.ViewHolder {
        private MaterialCardView setTopBoxCard;
        private TextView title, subTitle;
        private ImageView logoImage;

        public ViewHolder(View itemView) {
            super(itemView);
            setTopBoxCard = (MaterialCardView) itemView.findViewById(R.id.set_top_box_cardview);
            title = (TextView) itemView.findViewById(R.id.set_top_box_name);
            subTitle = (TextView) itemView.findViewById(R.id.set_top_box_description);
            logoImage = (ImageView) itemView.findViewById(R.id.logo_iv);
        }
    }
}
