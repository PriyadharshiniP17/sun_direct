package com.myplex.myplex.ui.adapter;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.myplex.api.APIConstants.PWA_URL;
import static com.myplex.myplex.ui.fragment.epg.EPGView.CURRENT_PROGRAM;
import static com.myplex.myplex.ui.fragment.epg.EPGView.FUTURE_PROGRAM;
import static com.myplex.myplex.ui.fragment.epg.EPGView.PAST_PROGRAM;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.github.pedrovgs.LoggerD;
import com.google.android.exoplayer2.ParserException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;
import com.google.gson.Gson;
import com.myplex.api.APICallback;
import com.myplex.api.APIConstants;
import com.myplex.api.APIRequest;
import com.myplex.api.APIResponse;
import com.myplex.api.APIService;
import com.myplex.api.myplexAPI;
import com.myplex.api.request.content.ContentLikedRequest;
import com.myplex.api.request.content.FetchLikeCheckRequest;
import com.myplex.model.AlarmData;
import com.myplex.model.ApplicationConfig;
import com.myplex.model.CardData;
import com.myplex.model.CardDataContent;
import com.myplex.model.CardDataImagesItem;
import com.myplex.model.CardDataRelatedCastItem;
import com.myplex.model.FavouriteResponse;
import com.myplex.model.RelatedCastList;
import com.myplex.myplex.ApplicationController;
import com.myplex.myplex.R;
import com.myplex.myplex.ui.fragment.epg.DateHelper;
import com.myplex.myplex.ui.fragment.epg.EPG;
import com.myplex.myplex.ui.fragment.epg.EPGUtil;
import com.myplex.myplex.utils.PicassoUtil;
import com.myplex.myplex.utils.ReminderListener;
import com.myplex.myplex.utils.Util;
import com.myplex.util.AlertDialogUtil;
import com.myplex.util.PrefUtils;
import com.myplex.util.SDKUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import kotlin.jvm.internal.Intrinsics;
import kotlin.text.StringsKt;

/**
 * Created by Uday Kumar V on  03/03/22.
 */
public class DetailsPageDailogAdapter extends RecyclerView.Adapter<DetailsPageDailogAdapter.DetailsPageViewHolder>{

    List<CardData> cardDataList;
    List<EPG.EPGProgram> epgProgramList;
    Context context;
    EPG.EPGProgram epgProgram;
    Boolean isContentLiked = false;
    private DetailsPageDailogAdapter.OnItemClickListener mOnItemClickListener;


    public DetailsPageDailogAdapter(Context context, List<CardData> cardDataList, List<EPG.EPGProgram> epgProgramList) {
        this.cardDataList = cardDataList;
        this.context = context;
        this.epgProgramList = epgProgramList;

    }
    @NonNull
    @Override
    public DetailsPageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new DetailsPageViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.vilite_details_page_items,parent,false
                )
        );
    }

    public void setOnItemClickListener(OnItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }

    @Override
    public void onBindViewHolder(@NonNull DetailsPageViewHolder holder, int position) {
        CardData carouselData = cardDataList.get(position);
      /*  if(carouselData != null)
            return;*/
        if(epgProgramList != null) {
            epgProgram = epgProgramList.get(position);
        }
        int programType1 = PAST_PROGRAM;
        boolean isfutureProgram = isFutureProgram(getEventStartTime(epgProgram));
        boolean iscurrentProgram = isfutureProgram ? false : isCurrent(getEventStartTime(epgProgram),getEventEndTime(epgProgram));
        if(isfutureProgram)
            programType1 = FUTURE_PROGRAM;
        else if(iscurrentProgram)
            programType1 = CURRENT_PROGRAM;
        LoggerD.debugLog("programType1 "+programType1);
        if(epgProgram != null) {
            if (epgProgram.getMetadata() != null && epgProgram.getMetadata().getId() != null) {
                if (epgProgram.getMetadata().getId().equalsIgnoreCase(carouselData.globalServiceId)) {
                  //  Date date1 = new Date();
                  //  Date date2 = new Date(getEventStartTime(epgProgram));
                    if (programType1 == CURRENT_PROGRAM) {
                        Log.e("app", "Date1 is after Date2");
                        holder.likeLL.setVisibility(View.GONE);
                        holder.playLL.setVisibility(View.VISIBLE);
                        holder.reminderLL.setVisibility(View.GONE);
                        holder.playpause.setVisibility(View.GONE);

                    } else if (programType1 == PAST_PROGRAM) {
                        Log.e("app", "Date1 is before Date2");
                        holder.likeLL.setVisibility(View.GONE);
                        holder.playLL.setVisibility(View.VISIBLE);
                        holder.reminderLL.setVisibility(View.GONE);
                        holder.playpause.setVisibility(View.GONE);
                    } else  {
                        Log.e("app", "Date1 is equal to Date2");
                        holder.likeLL.setVisibility(View.GONE);
                        holder.playLL.setVisibility(View.GONE);
                        holder.reminderLL.setVisibility(View.VISIBLE);
                        holder.playpause.setVisibility(View.GONE);
                    }
                }
            }
        }
      /*  if (carouselData.generalInfo != null && carouselData.generalInfo.title != null && !carouselData.generalInfo.title.isEmpty())
            holder.tvSubTitle.setText(carouselData.generalInfo.title+" - "+ carouselData.generalInfo.type.substring(0,1).toUpperCase()+""+carouselData.generalInfo.type.substring(1));*/
        //holder.program_name_type.setText(cardData.getTitle()+" - "+cardData.getType().substring(0,1).toUpperCase()+""+cardData.getType().substring(1));
        if(carouselData.globalServiceName != null && carouselData.content!= null && carouselData.content.channelNumber != null) {
            // Hided channel number for client requirement
               holder.tvTitle.setText(carouselData.content.channelNumber + ". " + carouselData.globalServiceName);
//            holder.tvTitle.setText(carouselData.globalServiceName);
        }
        if(carouselData.content != null && carouselData.content.genre != null && carouselData.content.genre.size() > 0 && carouselData.content.genre.get(0)!= null) {
            if(carouselData.content.subGenres != null && carouselData.content.subGenres.size()>0 && carouselData.content.subGenres.get(0)!= null)
                holder.tvSubTitle.setText(carouselData.getTitle() + "  " + "|" + "  " + carouselData.content.genre.get(0).name+" • "+carouselData.content.subGenres.get(0));
            else
                holder.tvSubTitle.setText(carouselData.getTitle() + "  " + "|" + "  " + carouselData.content.genre.get(0).name);
        }
        else
            holder.tvSubTitle.setText(carouselData.getTitle());
        if (carouselData.generalInfo != null && carouselData.generalInfo.description != null && !carouselData.generalInfo.description.isEmpty())
            holder.tvDescription.setText(carouselData.generalInfo.description);

        if(carouselData.relatedCast!=null && carouselData.relatedCast.values!=null /*&& carouselData.relatedCast.values.isEmpty()*/ && carouselData.relatedCast.values.size()>0) {
            /*holder.tvCast.setVisibility(View.VISIBLE);
            if (carouselData.relatedCast.values.size()> 0 && carouselData.relatedCast.values.get(0) != null && carouselData.relatedCast.values.get(0).name!=null && !TextUtils.isEmpty(carouselData.relatedCast.values.get(0).name)){
                holder.tvCast.setText("Cast:"+carouselData.relatedCast.values.get(0).name);
                   }*/
            {
                StringBuilder castMembers = new StringBuilder();
                for (CardDataRelatedCastItem relatedCast : carouselData.relatedCast.values) {
                    if (castMembers.length() != 0) {
                        castMembers.append(", ");
                    }
                    castMembers.append(relatedCast.name);
                }
                if (TextUtils.isEmpty(castMembers) || castMembers.length() == 0) {
                   holder.tvCast.setVisibility(View.GONE);
                    holder.cast_ll.setVisibility(GONE);
                } else {
                    if (carouselData.relatedCast.values.size() > 0) {
                       holder.tvCast.setVisibility(View.VISIBLE);
                       holder.cast_ll.setVisibility(VISIBLE);

                        List<CardDataRelatedCastItem> roleNamesList = new ArrayList<>();
                        for (int p = 0; p < carouselData.relatedCast.values.size(); p++) {
                            if (carouselData.relatedCast.values.get(p).images.values != null && carouselData.relatedCast.values.get(p).images.values.size() != 0) {
                                //  pillarItemsList.add(mData.relatedCast.values.get(p));
                            } else {
                                roleNamesList.add(carouselData.relatedCast.values.get(p));
                            }
                        }
                        RelatedCastList roleNamesRelatedCastList = new RelatedCastList();
                        roleNamesRelatedCastList.values = roleNamesList;
                        roleNamesRelatedCastList.mLayoutType = APIConstants.LAYOUT_TYPE_ROLE_NAME_LAYOUT;
                        CardData finalMData = carouselData;
                        String actorName = "", directorName = "";
                        if (roleNamesList != null) {
                            for (int i = 0; i < roleNamesList.size(); i++) {
                                if (roleNamesList.get(i).types.get(0).equalsIgnoreCase("Actor")) {
                                    if (actorName.isEmpty())
                                        actorName = roleNamesList.get(i).name;
                                    else
                                        actorName = actorName + ", " + roleNamesList.get(i).name;
                                }
                                if (roleNamesList.get(i).types.get(0).equalsIgnoreCase("director")) {
                                    if (directorName.isEmpty())
                                        directorName = roleNamesList.get(i).name;
                                    else
                                        directorName = directorName + ", " + roleNamesList.get(i).name;
                                }
                            }
                            if(actorName!=null && !actorName.trim().equals("")){
                               holder.tvCast.setVisibility(VISIBLE);
                               holder.cast_ll.setVisibility(VISIBLE);
                               holder.tvCast.setText(actorName);
                            }else {
                                holder.tvCast.setVisibility(GONE);
                                holder.cast_ll.setVisibility(GONE);
                            }

                           /* if(directorName!=null && !directorName.trim().equals("")){
                               holder.tvDirector.setVisibility(VISIBLE);
                                holder.tvDirector.setText("Director:"+directorName);
                            }else {
                                holder.tvDirector.setVisibility(GONE);
                            }*/

                        }

                    }
                }

            }
        }else{
            holder.cast_ll.setVisibility(GONE);
        }
        Gson gson=new Gson();
        Log.d("CAST","Related Cast Data"+gson.toJson(carouselData.relatedCast));
        holder.playLL.setTag(carouselData);
        holder.playpause.setTag(carouselData);
        String imageLink = carouselData.getImageLink(APIConstants.IMAGE_TYPE_COVERPOSTER);
        if (TextUtils.isEmpty(imageLink)
                || APIConstants.IMAGE_TYPE_NO_IMAGE.compareTo(imageLink) == 0) {
            holder.ivImage.setImageResource(R.drawable
                    .movie_thumbnail_placeholder);
        } else {
            String imageLink1 = Util.getImageLink(carouselData);
            PicassoUtil.with(context).load(imageLink1, holder.ivImage, R.drawable.movie_thumbnail_placeholder);
        }

        if(carouselData.currentUserData!= null && carouselData.currentUserData.favorite)
            holder.ivLike.setImageResource(R.drawable.live_tv_favourite_icon_highlighted);
        else
            holder.ivLike.setImageResource(R.drawable.ic_unlike);
        String genre = "";
        if (carouselData.content != null && carouselData.content.genre != null && carouselData.content.genre.size() > 0) {
            for (int i = 0; i < carouselData.content.genre.size(); i++) {
                if (carouselData.content.genre != null && !TextUtils.isEmpty(carouselData.content.genre.get(i).name)) {
                    if (genre == null || TextUtils.isEmpty(genre)) {
                        genre = carouselData.content.genre.get(i).name;
                    } else {
                        String[] strArr = genre.split("|");
                       /* if (strArr.length < Utils.GENRE_DISPLAY_LIMIT) {
                            genre = genre + " | " + carouselData.content.genre.get(i).name;
                        }*/
                    }
                }
            }
        }
     /*   if (carouselData.content.categoryType != null && carouselData.content != null && !TextUtils.isEmpty(carouselData.content.categoryType)) {
            if (genre == null || TextUtils.isEmpty(genre)) {
                genre = carouselData.content.categoryType;
            } else {
                genre = genre + " | " + carouselData.content.categoryType;
            }
        }*/

        String releaseDate = getYYYY(carouselData.content);
        try {
            long startDate = Util.parseXsDateTime(carouselData.startDate);
            EPGUtil.getShortTime(startDate);
            String startTime=EPGUtil.getShortTime(startDate);

            long endDate=Util.parseXsDateTime(carouselData.endDate);
            EPGUtil.getShortTime(endDate);
            String endTime=EPGUtil.getShortTime(endDate);

            holder.tvTime.setText(startTime + " - " + endTime);
            Log.d("endTime","EPG"+EPGUtil.getShortTime(endDate));

        } catch (ParserException e) {
            e.printStackTrace();
        }
       /* if (!carouselData.isLive() && !TextUtils.isEmpty(releaseDate)) {
            holder.tvTime.setText(releaseDate + " | " + genre);
        } else {
            holder.tvTime.setText(genre);
        }*/
//        holder.tvTime.setText(Util.convertDate(carouselData.startDate));
        /*String categoryTextView ="";

        if (TextUtils.isEmpty(categoryTextView)) {
            if (!carouselData.isLive() && !carouselData.isTVSeries())
                categoryTextView = carouselData.getDurationWithFormat();
        } else {
            if (!carouselData.isLive() && !carouselData.isTVSeries())
                categoryTextView = categoryTextView + " | " + carouselData.getDurationWithFormat();
        }
        holder.tvTime.setText(categoryTextView);*/
       // checkLikeRequest(carouselData._id, carouselData.getType(), holder.ivLike);

        try {
            long startDate = Util.parseXsDateTime(carouselData.startDate);
            long endDate = Util.parseXsDateTime(carouselData.endDate);
            String time = EPGUtil.getShortTime(startDate)+" - "+EPGUtil.getShortTime(endDate);
            holder.tvTime.setText(time.toLowerCase(Locale.ROOT));
        } catch (ParserException e) {
            e.printStackTrace();
        }
        holder.ivShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               /* constructShareUrl(carouselData);
                ComScoreAnalytics.getInstance().setEventShare(carouselData);
                FirebaseAnalytics.getInstance().onShareClick(false, carouselData);
                return;*/
            }
        });
        ReminderListener reminderListener = new ReminderListener(context, null, ApplicationController.DATE_POSITION);
      /*  holder.reminderLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(myplexAPI.TAG, "ContentLikedRequest: onFailure: t- ");
            }
        });*/
        holder.mReminderImage.setOnClickListener(reminderListener);
        holder.mReminderImage.setTag(carouselData);
        AlarmData alarmData = Util.getReminderProgmaNameIfExistAtThisTime(carouselData);
        holder.mReminderImage.setImageResource(R.drawable.ic_notification);
        holder.mReminderImage.setColorFilter(SDKUtils.getColor(context,R.color.white), PorterDuff.Mode.MULTIPLY);
        if(alarmData != null
                && alarmData.title != null
                && alarmData.title.equalsIgnoreCase(carouselData.generalInfo.title)){
            holder.mReminderImage.setColorFilter(SDKUtils.getColor(context,R.color.theme_app_color), PorterDuff.Mode.MULTIPLY);
        }
        int finalProgramType = programType1;
        holder.playLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //((BaseActivity) context).setOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                if (mOnItemClickListener != null) {
                    CardData data = null;
                    if (view.getTag() instanceof CardData) {
                        data = (CardData) view.getTag();
                    }
                    if(finalProgramType == PAST_PROGRAM)
                        mOnItemClickListener.onItemClicked(data, true);
                    else
                        mOnItemClickListener.onItemClicked(data, false);
                }
            }
        });

        holder.playpause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                ((BaseActivity) context).setOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
               /* if (mOnItemClickListener != null) {
                    CardData data = null;
                    if (view.getTag() instanceof CardData) {
                        data = (CardData) view.getTag();
                    }

                    mOnItemClickListener.onItemClicked(data);
                }*/
            }
        });

        holder.likeLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clevertapClickEventThumbnail("favourite",carouselData);
               /* if (isContentLiked) {
                    isContentLiked = false;
                } else {
                    isContentLiked = true;
                }*/
               isContentLiked = false;
                if(carouselData.currentUserData != null)
                    isContentLiked = carouselData.currentUserData.favorite;

                ContentLikedRequest.Params likedContentParams = new ContentLikedRequest.Params(carouselData.globalServiceId, carouselData.getType(), isContentLiked);
                ContentLikedRequest mRequestLikedContent = new ContentLikedRequest(likedContentParams, (APICallback) (new APICallback() {
                    public void onResponse(@Nullable APIResponse response) {
                        if (response != null && response.body() != null) {
                            Object var10001 = response.body();
                            Intrinsics.checkNotNull(var10001);
                            if (StringsKt.equals("SUCCESS", ((FavouriteResponse) var10001).status, true)) {
                                var10001 = response.body();
                                Intrinsics.checkNotNull(var10001);
                                if (StringsKt.equals("movie is liked", ((FavouriteResponse) var10001).message, true)) {


                                }
                                if(carouselData.currentUserData != null) {
                                    cardDataList.get(position).currentUserData.favorite = !isContentLiked;
                                }
                                notifyItemChanged(position);
                               // notifyDataSetChanged();
                              /*  if (isContentLiked) {
                                    holder.ivLike.setImageResource(R.drawable.ic_like);
                                } else {
                                    holder.ivLike.setImageResource(R.drawable.ic_unlike);
                                }*/
                            }

                        }
                    }

                    public void onFailure(@Nullable Throwable t, int errorCode) {
                        Log.d(myplexAPI.TAG, "ContentLikedRequest: onFailure: t- " + t);
                        if (errorCode == -300) {
                            AlertDialogUtil.showToastNotification((CharSequence) context.getResources().getString(R.string.network_error));
                        }
                    }
                }));
                APIService.getInstance().execute((APIRequest) mRequestLikedContent);

            }
        });
    }

    private void constructShareUrl(final CardData mData) {
        String dynamicLink = null;
        String pwa_url = "";
        if (!TextUtils.isEmpty(PrefUtils.getInstance().getAppSharePwaUrl())) {
            pwa_url = PrefUtils.getInstance().getAppSharePwaUrl() + mData.generalInfo.type + "/detail/" + mData._id + "/" + mData.generalInfo.title;
        } else {
            pwa_url = PWA_URL + mData.generalInfo.type + "/detail/" + mData._id + "/" + mData.generalInfo.title;
        }

        Uri deepLink;
        if (!TextUtils.isEmpty(PrefUtils.getInstance().getAppDeepLinkUrl())) {
            deepLink = Uri.parse(PrefUtils.getInstance().getAppDeepLinkUrl());
        } else {
            deepLink = Uri.parse(APIConstants.SHARE_ARTIST_DEEP_LINK_URL);
        }

        final Uri fallBackUri = Uri.parse(pwa_url);

        String packageName = context.getPackageName();

// Build the link with all required parameters
        Uri.Builder builder = new Uri.Builder()
                .scheme("https")
                .authority(context.getString(R.string.appcode_for_deeplink))
                .path("/")
                .appendQueryParameter("link", deepLink + mData.generalInfo.type + "/detail/" + mData._id + "/" +
                        mData.generalInfo.title)
                .appendQueryParameter("apn", packageName);

        dynamicLink = builder.build().toString();
        FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLongLink(Uri.parse(dynamicLink))
                .setAndroidParameters(new
                        DynamicLink.AndroidParameters.Builder(packageName)
                        .setFallbackUrl(fallBackUri)
                        .build())
                .buildShortDynamicLink()
                .addOnCompleteListener((Activity) context, new OnCompleteListener<ShortDynamicLink>() {
                    @Override
                    public void onComplete(@NonNull Task<ShortDynamicLink> task) {
                        if (task.isSuccessful()) {
// Short link created
//Util.showFeedback(shareLayout);
                            String path = null;
                            if (mData.images != null
                                    && mData.images.values != null
                                    && !mData.images.values.isEmpty()
                                    && mData.images.values.size() > 0) {
                                for (CardDataImagesItem imageItem : mData.images.values) {
                                    if (imageItem.type != null && imageItem.type.equalsIgnoreCase("coverposter") && imageItem.profile != null
                                            && imageItem.profile.equalsIgnoreCase(ApplicationConfig.MDPI) && imageItem.resolution != null && imageItem.resolution.equalsIgnoreCase("640x360")) {
                                        if (imageItem.link != null
                                                || imageItem.link.compareTo("Images/NoImage.jpg") != 0) {
                                            path = imageItem.link;
                                        }
                                        break;
                                    }
                                }
                            }
                            if (TextUtils.isEmpty(path)) {
                                path = Util.takeScreenShot((Activity) context);
                            }
                            String contentName = null;
                            if (mData != null
                                    && mData.generalInfo != null
                                    && mData.generalInfo.title != null) {
                                contentName = mData.generalInfo.title;
                            }

                            String msg;
                            if (mData.isNewsContent()) {
                                msg = context.getString(R.string.share_news_message);
                            } else {
                                msg = context.getString(R.string.share_message);
                            }
                            if (!TextUtils.isEmpty(PrefUtils.getInstance().getPrefShareMessage())) {
                                msg = PrefUtils.getInstance().getPrefShareMessage();
                            }
                            if (msg.contains(APIConstants.HASH_CONTENT_NAME)) {
                                msg = msg.replace(APIConstants.HASH_CONTENT_NAME, TextUtils.isEmpty(contentName) ? "" : contentName);
                            }

                            if (mData.isNewsContent()) {
                                if (msg.contains(APIConstants.HASH_DESCRIPTION)) {
                                    msg = msg.replace(APIConstants.HASH_DESCRIPTION,
                                            TextUtils.isEmpty(mData.getBriefDescription()) ? "" : mData.getBriefDescription());
                                }
                            }

                            String url = task.getResult().getShortLink().toString();
/*if (mData.generalInfo.type != null && mData.generalInfo._id != null && mData.generalInfo.title != null) {
url = APIConstants.SHARE_ARTIST_DEEP_LINK_URL + mData.generalInfo.type + "/detail/" + mData.generalInfo._id + "/" + mData.generalInfo.title + "?sourceType=android";
} else {
url = APIConstants.getShareUrl(mContext);
}*/
// String shareUrl=PrefUtils.getInstance().getShortLink();
                            if (!TextUtils.isEmpty(url)) {
                                msg = msg + "\n" + Uri.parse(url.trim());
                            }
                            Util.shareData(context, 1, path, msg);// send message.

// PrefUtils.getInstance().setShortLink(task.getResult().getShortLink().toString());
                        } else {
                            LoggerD.debugOTP(String.valueOf(task.getException()));
                        }
                    }
                });
    }

    private void checkLikeRequest(String contentId, String contentType, ImageView ivLike) {
        FetchLikeCheckRequest.Params likeCheckRequestParams = new FetchLikeCheckRequest.Params(contentId, contentType);
        FetchLikeCheckRequest mRequestLikeStatus = new FetchLikeCheckRequest(likeCheckRequestParams,
                new APICallback<FavouriteResponse>() {
                    @Override
                    public void onResponse(APIResponse<FavouriteResponse> response) {
                        if (response == null || response.body() == null) {
                            onFailure(new Throwable(APIConstants.ERROR_RESPONSE_OR_RESPONSE_BODY_NULL), APIRequest.ERR_UN_KNOWN);
                            return;
                        }
                      //  //Log.d(TAG, "FetchContentLikeStatus: onResponse: message - " + response.body().message);
                        if (APIConstants.SUCCESS.equalsIgnoreCase(response.body().status)) {
                            isContentLiked = response.body().like;
                            ivLike.setImageResource(R.drawable.live_tv_favourite_icon_highlighted);
                            //updateLikeButtonBgAndIcon(isContentLiked);
                        } else {
                            isContentLiked = false;
                            ivLike.setImageResource(R.drawable.ic_unlike);
                            //updateLikeButtonBgAndIcon(isContentLiked);
                        }
                    }

                    @Override
                    public void onFailure(Throwable t, int errorCode) {
                        ////Log.d(TAG, "FetchContentLikeStatus: onFailure: t- " + t);
                        if (errorCode == APIRequest.ERR_NO_NETWORK) {
                            AlertDialogUtil.showToastNotification(context.getString(R.string.network_error));
                            return;
                        }
                      //  AlertDialogUtil.showToastNotification(context.getString(R.string.msg_fav_failed_update));
                    }
                });
        APIService.getInstance().execute(mRequestLikeStatus);
    }

    public final String getYYYY(@NotNull CardDataContent content) {
        Intrinsics.checkNotNullParameter(content, "content");
        if (content.releaseDate == null) {
            return null;
        } else {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            Date date = (Date)null;

            Date var4;
            try {
                var4 = format.parse(content.releaseDate);
            } catch (ParseException var6) {
                var6.printStackTrace();
                return "";
            }

            date = var4;
            if (var4 != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
                sdf.setTimeZone(TimeZone.getDefault());
                return sdf.format(date).toString();
            } else {
                return "";
            }
        }
    }
    private final void clevertapClickEventThumbnail(String buttonLabel,CardData carouselData) {
        String contentModel = "svod";
        CardData var10000 = carouselData;
        if (var10000 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("carouselData");
        }

        if (var10000 != null) {
            var10000 = carouselData;
            if (var10000 == null) {
                Intrinsics.throwUninitializedPropertyAccessException("carouselData");
            }

            Intrinsics.checkNotNull(var10000);
            if (var10000.generalInfo != null) {
                var10000 = carouselData;
                if (var10000 == null) {
                    Intrinsics.throwUninitializedPropertyAccessException("carouselData");
                }

                Intrinsics.checkNotNull(var10000);
                if (var10000.generalInfo.contentRights != null) {
                    var10000 = carouselData;
                    if (var10000 == null) {
                        Intrinsics.throwUninitializedPropertyAccessException("carouselData");
                    }

                    Intrinsics.checkNotNull(var10000);
                    if (var10000.generalInfo.contentRights.size() > 0) {
                        var10000 = carouselData;
                        if (var10000 == null) {
                            Intrinsics.throwUninitializedPropertyAccessException("carouselData");
                        }

                        Intrinsics.checkNotNull(var10000);
                        Iterator var4 = var10000.generalInfo.contentRights.iterator();

                        while (var4.hasNext()) {
                            String contentRight = (String) var4.next();
                            if (StringsKt.equals("tvod", contentRight, true)) {
                                contentModel = contentRight;
                                break;
                            }
                        }
                    }
                }
            }
        }
    }
    @Override
    public int getItemCount() {
        return cardDataList.size();
    }

    class DetailsPageViewHolder extends RecyclerView.ViewHolder{

        CardView cardView;
        ImageView ivImage,ivLike,ivShare,playpause, mReminderImage;
        TextView tvTitle,tvSubTitle,tvDescription, tvTime,textCount,cast_ll, tvCast;
        ImageView btPlayNow;
        LinearLayout playLL, likeLL, reminderLL,cast_layout;

        public DetailsPageViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            ivImage = itemView.findViewById(R.id.ivImage);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvSubTitle = itemView.findViewById(R.id.tvSubTitle);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvCast=itemView.findViewById(R.id.tvCast);
            cast_ll=itemView.findViewById(R.id.cast_ll);
            cast_layout=itemView.findViewById(R.id.cast_layout);
            textCount=itemView.findViewById(R.id.text_count);
            btPlayNow = itemView.findViewById(R.id.btPlayNow);
            playLL = itemView.findViewById(R.id.playLL);
            likeLL = itemView.findViewById(R.id.likeLL);
            ivLike = itemView.findViewById(R.id.ivLike);
            ivShare = itemView.findViewById(R.id.ivShare);
            reminderLL = itemView.findViewById(R.id.reminderLL);
            playpause = itemView.findViewById(R.id.playpause);
            mReminderImage = itemView.findViewById(R.id.reminder_image);

            /*ViewGroup.LayoutParams params = cast_layout.getLayoutParams();
            params.height = 500;
            cast_layout.setLayoutParams(params);*/
        }
    }
    public interface OnItemClickListener {
        void onItemClicked(CardData cardData, boolean isFromCatchup);
    }
    public long getEventStartTime(EPG.EPGProgram event) {
        if (event != null && event.getDisplay().getMarkers() != null) {

            List<EPG.PosterDisplay.Marker> markers = event.getDisplay().getMarkers();
            int size = markers.size();
            for (int i = 0; i < size; i++) {
                String markertype = markers.get(i).getMarkerType();
                try {
                    if (markertype.equalsIgnoreCase("startTime"))
                        return (Long.parseLong(markers.get(i).getValue()) + DateHelper.getInstance().getTimezoneOffsetValue());
                } catch (NumberFormatException e) {

                }

            }

        }
        return -1l;
    }
    public boolean isCurrent(long eventstarttime,long eventendtime) {

        long now = getCurrentTimeInMillis();
        //CustomLog.e("EPG"," event start time :"+getEventStartTime(event));
        //CustomLog.e("EPG"," event end time :"+getEventEndTime(event));
        // CustomLog.e("EPG"," event now time :"+now);
        return now >= eventstarttime && now <= eventendtime;
    }

    public long getCurrentTimeInMillis() {
        return DateHelper.getInstance().getCurrentLocalTime();

        // return System.currentTimeMillis();
    }

    public boolean isFutureProgram(long eventstarttime){
        long now = getCurrentTimeInMillis();
        return now < eventstarttime;
    }

    public long getEventEndTime(EPG.EPGProgram event) {
        if (event != null && event.getDisplay().getMarkers() != null) {
            List<EPG.PosterDisplay.Marker> markers = event.getDisplay().getMarkers();
            int size = markers.size();
            for (int i = 0; i < size; i++) {
                try {
                    if (markers.get(i).getMarkerType().equalsIgnoreCase("endTime"))
                        return (Long.parseLong(markers.get(i).getValue()) + DateHelper.getInstance().getTimezoneOffsetValue());
                } catch (NumberFormatException e) {

                }

            }
        }
        return -1l;
    }

}
