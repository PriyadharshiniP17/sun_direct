package com.myplex.myplex.ui.activities;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.myplex.api.APICallback;
import com.myplex.api.APIConstants;
import com.myplex.api.APIRequest;
import com.myplex.api.APIResponse;
import com.myplex.api.APIService;
import com.myplex.api.request.user.DeleteAllNotificationtRequest;
import com.myplex.api.request.user.DeleteNotificationtRequest;
import com.myplex.api.request.user.NotificationsListRequest;
import com.myplex.model.DeleteNotificationResponse;
import com.myplex.model.NotificationList;
import com.myplex.model.ResultNotification;
import com.myplex.myplex.R;
import com.myplex.myplex.model.NotificationListener;
import com.myplex.myplex.subscribe.SubscriptionWebActivity;
import com.myplex.myplex.ui.adapter.NotificationAdapter;
import com.myplex.myplex.utils.AlertDialogUtil;
import com.myplex.myplex.utils.DeviceUtils;
import com.myplex.myplex.utils.SwipeHelper;


import java.util.ArrayList;
import java.util.List;


public class NotificationActivityNew extends AppCompatActivity {
    private static final String TAG = NotificationActivityNew.class.getSimpleName();

    RecyclerView rvNotification;
    TextView tvNoData;
    ImageView emptyNotification;
    LinearLayout linearLayout;
    TextView noContent, reference, tvToolbarTitle;
    ProgressBar progress;
    ImageView ibBack, noNotificationCloseIcon;

    List<ResultNotification> notificationList;

    NotificationAdapter notificationAdapter;
    LinearLayout llClearAll, llNoNotifications;
    private boolean isNotificationRead;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (DeviceUtils.isTabletOrientationEnabled(this)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        setContentView(R.layout.notification_activity_new);

        initViews();
        initAdapter();
        loadDummyData();

    }

    private void initViews() {
        ibBack = findViewById(R.id.ib_back);
        noNotificationCloseIcon = findViewById(R.id.noNotificationCloseIcon);
        tvToolbarTitle = findViewById(R.id.tv_toolbar_title);
        llClearAll = findViewById(R.id.ll_clear_all);
        llNoNotifications = findViewById(R.id.llNoNotifications);
        rvNotification = findViewById(R.id.rv_notification);
        tvNoData = findViewById(R.id.tv_no_data);
        emptyNotification = findViewById(R.id.empty_notification_img);
        noContent = findViewById(R.id.no_notification_found);
        reference = findViewById(R.id.get_notified);
        progress = findViewById(R.id.progress);
        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_from_bottom);
        emptyNotification.startAnimation(animation);
        emptyNotification.startAnimation(animation);
        noContent.startAnimation(animation);
        reference.startAnimation(animation);

        tvToolbarTitle.setText(getString(R.string.notification));
        noNotificationCloseIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        ibBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        llClearAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteNotificationAll();
            }
        });

    }

    private void clearAllNotifications() {
        rvNotification.setVisibility(View.GONE);
        llClearAll.setVisibility(View.GONE);
        llNoNotifications.setVisibility(View.VISIBLE);
        ibBack.setVisibility(View.GONE);
        tvToolbarTitle.setVisibility(View.GONE);
        noNotificationCloseIcon.setVisibility(View.VISIBLE);
//        emptyNotification.setVisibility(View.VISIBLE);
//        noContent.setVisibility(View.VISIBLE);
//        reference.setVisibility(View.VISIBLE);
//        tvNoData.setVisibility(View.GONE);
    }

    private void initAdapter() {
        notificationList = new ArrayList<>();

        notificationAdapter = new NotificationAdapter(this, notificationList, new NotificationListener() {
            @Override
            public void onNotificationClick(int id) {
                Log.d(TAG, "onNotificationClick: id " + id);
                ResultNotification notification = notificationList.get(id);
                if(notification != null) {
                    if(!notification.getAction().isEmpty() && notification.getAction().equalsIgnoreCase("externalPage") && notification.getAndroidActionUrl() != null) {
                        startActivityForResult(SubscriptionWebActivity.createIntent(NotificationActivityNew.this, notification.getAndroidActionUrl(),  SubscriptionWebActivity.PARAM_LAUNCH_NONE), 1);
                    }
                    if(!notification.getAction().isEmpty() && notification.getAction().equalsIgnoreCase("deeplink") && notification.getAndroidActionUrl() != null) {
                      //  startActivityForResult(SubscriptionWebActivity.createIntent(NotificationActivityNew.this, notification.getAndroidActionUrl(),  SubscriptionWebActivity.PARAM_LAUNCH_NONE), 1);
                        constructDeepLinkUrl(notification.getAndroidActionUrl());
                    }
                    isNotificationRead = true;
                    deleteNotification(notificationList.get(id).getId(), 0,"viewed");
                }
            }

            @Override
            public void onNotificationDelete(int position) {
                Log.d(TAG, "onNotificationDelete: position " + position);
            }

            @Override
            public void onNotificationRemainder(int position, int isEnabled) {
                Log.d(TAG, "onNotificationRemainder: position " + position + " isEnabled " + isEnabled);
            }
        });
        //rvNotification.addItemDecoration(new SimpleDividerItemDecoration(this));

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        rvNotification.setLayoutManager(linearLayoutManager);
        rvNotification.setAdapter(notificationAdapter);


        new SwipeHelper(this, rvNotification, false) {
            @Override
            public void instantiateUnderlayButton(RecyclerView.ViewHolder viewHolder, List<UnderlayButton> underlayButtons) {
                underlayButtons.add(new SwipeHelper.UnderlayButton(getString(R.string.label_delete),
                        getResources().getDrawable(R.drawable.icon_notification_delete),
                        getResources().getColor(R.color.bg_delete), getResources().getColor(R.color.white),
                        new UnderlayButtonClickListener() {
                            @Override
                            public void onClick(int pos) {
//                                Toast.makeText(NotificationActivity.this, "Delete clicked at " + pos, Toast.LENGTH_SHORT).show();

                                deleteNotification(notificationList.get(pos).getId(), pos, "delete");
                            }
                        }));
             /*   if(!notificationList.get(viewHolder.getAdapterPosition()).getEventTime().equals("")){
                    String label = getString(R.string.label_remind);
//                    Drawable remainderIcon =;
                    if(notificationList.get(viewHolder.getAdapterPosition()).getIsAlreadyRemainded() == 1){
//                        remainderIcon =getResources().getDrawable(R.drawable.icon_remainder_selected);
                        label = getString(R.string.cancel);
                    }

                    underlayButtons.add(new SwipeHelper.UnderlayButton(label,
                            getResources().getDrawable(R.drawable.icon_remainder_unselected),
                            getResources().getColor(R.color.bg_remainder), getResources().getColor(R.color.white),
                            new UnderlayButtonClickListener() {
                                @Override
                                public void onClick(int pos) {
//                                    Toast.makeText(NotificationActivity.this, "Remind clicked at " + pos, Toast.LENGTH_SHORT).show();
                                    NotificationModel notificationModel = notificationList.get(pos);
                                    if(notificationModel.getIsAlreadyRemainded() == 1)
                                        notificationModel.setIsAlreadyRemainded(0);
                                    else
                                        notificationModel.setIsAlreadyRemainded(1);
                                    notificationAdapter.changeItem(pos, notificationModel);

                                }
                            }));

                }
*/
            }
        };
    }

    private void constructDeepLinkUrl(String uri) {
        Intent intent = new Intent(this, UrlGatewayActivity.class);
        intent.setData(Uri.parse(uri));
        Log.d("LOG_TAG", "Intent: " + uri);
        startActivity(intent);
        return;
    }
    private void deleteNotification(int notificationId, int position, String status) {
        progress.setVisibility(View.VISIBLE);
        DeleteNotificationtRequest.Params params = new DeleteNotificationtRequest.Params(notificationId, status);
        DeleteNotificationtRequest mRequestFavourites = new DeleteNotificationtRequest(params,
                new APICallback<DeleteNotificationResponse>() {
                    @Override
                    public void onResponse(APIResponse<DeleteNotificationResponse> response) {

                        if (response == null
                                || response.body() == null) {
                            progress.setVisibility(View.GONE);
                            return;
                        }
                        if(response.body().getStatus().equalsIgnoreCase("SUCCESS")) {
                            if(!status.equalsIgnoreCase("viewed")){
                                if(position < notificationList.size())
                                    notificationAdapter.deleteItem(position);
                                loadDummyData();
                            }else
                                progress.setVisibility(View.GONE);
                        }else
                            progress.setVisibility(View.GONE);
                    }

                    @Override
                    public void onFailure(Throwable t, int errorCode) {
                        progress.setVisibility(View.GONE);
                        Log.d("Favourite", "FavouriteRequest: onResponse: t- " + t);
                        if (errorCode == APIRequest.ERR_NO_NETWORK) {
                            AlertDialogUtil.showToastNotification(getString(R.string.network_error));
                            return;
                        }
                        AlertDialogUtil.showToastNotification(getString(R.string.msg_fav_failed_update));
                    }
                });
        APIService.getInstance().execute(mRequestFavourites);
    }

    private void deleteNotificationAll() {
        progress.setVisibility(View.VISIBLE);
        DeleteAllNotificationtRequest mRequestFavourites = new DeleteAllNotificationtRequest(
                new APICallback<DeleteNotificationResponse>() {
                    @Override
                    public void onResponse(APIResponse<DeleteNotificationResponse> response) {
                        if (response == null
                                || response.body() == null) {
                            return;
                        }
                        if(response.body().getStatus().equalsIgnoreCase("SUCCESS")) {
                            clearAllNotifications();
                        }
                        progress.setVisibility(View.GONE);
                    }

                    @Override
                    public void onFailure(Throwable t, int errorCode) {
                        if (errorCode == APIRequest.ERR_NO_NETWORK) {
                            AlertDialogUtil.showToastNotification(getString(R.string.network_error));
                            return;
                        }
                        AlertDialogUtil.showToastNotification(getString(R.string.msg_fav_failed_update));
                        progress.setVisibility(View.GONE);
                    }
                });
        APIService.getInstance().execute(mRequestFavourites);
    }

    private void loadDummyData() {
        progress.setVisibility(View.VISIBLE);
        NotificationsListRequest mRequestFavourites = new NotificationsListRequest(
                new APICallback<NotificationList>() {
                    @Override
                    public void onResponse(APIResponse<NotificationList> response) {

                        if (response == null
                                || response.body() == null) {
                            return;
                        }
                        notificationList = response.body().getResults();
                      /*  if(response.body().getResults() != null)
                        for (int i=0; i < response.body().getResults().size(); i++) {
                            if(response.body().getResults().get(i).getStatus() != null && !response.body().getResults().get(i).getStatus().equalsIgnoreCase("archive"))
                            notificationList.add(response.body().getResults().get(i));
                        }*/

                        if(notificationList != null ){
                            if (notificationList.size() >= 21) {
                                deleteNotification(notificationList.get(20).getId(), 21, "delete");
                            }else if (notificationList.size() > 0) {
                                progress.setVisibility(View.GONE);
                                rvNotification.setVisibility(View.VISIBLE);
                                llClearAll.setVisibility(View.VISIBLE);
                                tvNoData.setVisibility(View.GONE);
                                ibBack.setVisibility(View.VISIBLE);
                                tvToolbarTitle.setVisibility(View.VISIBLE);
                                noNotificationCloseIcon.setVisibility(View.GONE);
                                notificationAdapter.setData(notificationList);
                                notificationAdapter.notifyDataSetChanged();

                            } else {
                                setEmptyNotification();
                            }
                        }else {
                            setEmptyNotification();
                        }
                    }

                    @Override
                    public void onFailure(Throwable t, int errorCode) {
                        Log.d("Favourite", "FavouriteRequest: onResponse: t- " + t);
                        progress.setVisibility(View.GONE);
                        if (errorCode == APIRequest.ERR_NO_NETWORK) {
                            AlertDialogUtil.showToastNotification(getString(R.string.network_error));
                            return;
                        }
                        AlertDialogUtil.showToastNotification(getString(R.string.msg_fav_failed_update));
                    }
                });
        APIService.getInstance().execute(mRequestFavourites);

    }

    private void setEmptyNotification(){
        progress.setVisibility(View.GONE);
        llClearAll.setVisibility(View.GONE);
        ibBack.setVisibility(View.GONE);
        tvToolbarTitle.setVisibility(View.GONE);
        noNotificationCloseIcon.setVisibility(View.VISIBLE);
        clearAllNotifications();
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        finish();
        if(isNotificationRead)
            setResult(APIConstants.NOTIFICATION_REQUEST);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        tvNoData = null;
        notificationList = null;
        notificationAdapter = null;
    }
}