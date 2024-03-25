package com.myplex.myplex.ui.views;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.pedrovgs.LoggerD;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;
import com.myplex.api.APIConstants;
import com.myplex.model.ApplicationConfig;
import com.myplex.model.CardData;
import com.myplex.model.CardDataImagesItem;
import com.myplex.model.ProfileAPIListAndroid;
import com.myplex.util.PrefUtils;
import com.myplex.myplex.R;
import com.myplex.myplex.analytics.FirebaseAnalytics;
import com.myplex.myplex.ui.activities.BaseActivity;
import com.myplex.myplex.utils.DeviceUtils;
import com.myplex.myplex.utils.PicassoUtil;
import com.myplex.myplex.utils.Util;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ArtistProfileBannerAndDescriptionItem extends GenericListViewCompoment {

    private List<ProfileAPIListAndroid> profileAPIListAndroids;
    private Context mContext;
    private RecyclerView mRecyclerViewCarouselInfo;
    private GenericListViewCompoment holder = this;

    public ArtistProfileBannerAndDescriptionItem(View view, Context context, List<ProfileAPIListAndroid> profileAPIListAndroids,
                                                 RecyclerView mRecyclerViewCarouselInfo) {
        super(view);
        this.mContext = context;
        this.profileAPIListAndroids = profileAPIListAndroids;
        this.mRecyclerViewCarouselInfo = mRecyclerViewCarouselInfo;
    }

    public static ArtistProfileBannerAndDescriptionItem createView(Context context, ViewGroup parent, List<ProfileAPIListAndroid> profileAPIList,
                                                                   RecyclerView mRecyclerViewCarouselInfo) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_artist_profile_banner_description, parent, false);
        return new ArtistProfileBannerAndDescriptionItem(view, context, profileAPIList, mRecyclerViewCarouselInfo);
    }

    @Override
    public void bindItemViewHolder(int position) {
        this.position = position;
        ProfileAPIListAndroid bannerImageDescriptionData=profileAPIListAndroids.get(position);
        if (bannerImageDescriptionData == null || bannerImageDescriptionData.mArtistData == null) {
            return;
        }
        Display display = ((BaseActivity) mContext).getWindowManager().getDefaultDisplay();
        int width = display.getWidth();
        int height = (width * 9) / 16;
        LinearLayout.LayoutParams parms = new LinearLayout.LayoutParams(width, height);
        holder.artistProfileBannerImage.setLayoutParams(parms);
        String imageUrl = "";
        if (DeviceUtils.isTablet(mContext)) {
            imageUrl = Util.getSquareImageLink(bannerImageDescriptionData.mArtistData, true);
        } else {
            imageUrl = Util.getSquareImageLink(bannerImageDescriptionData.mArtistData, false);
        }
        if (TextUtils.isEmpty(imageUrl)) {
            Picasso.get().load(R.drawable.movie_thumbnail_placeholder).error(R.drawable
                    .movie_thumbnail_placeholder).placeholder(R.drawable
                    .movie_thumbnail_placeholder).into(holder.artistProfileBannerImage);
        } else {
            PicassoUtil.with(mContext).load(imageUrl, holder.artistProfileBannerImage, R.drawable.movie_thumbnail_placeholder);
        }
        holder.artistProfileHeadingText.setText(bannerImageDescriptionData.mArtistData.generalInfo.title);
        holder.artistProfileReadMoreText.setText(bannerImageDescriptionData.mArtistData.generalInfo.description);
        showShareUi(bannerImageDescriptionData);
    }


    private void showShareUi(final ProfileAPIListAndroid bannerImageDescriptionData) {
        holder.artistProfileSharImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.artistProfileSharImage.setEnabled(false);
                constructShareUrl(bannerImageDescriptionData.mArtistData);
                holder.artistProfileSharImage.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (holder.artistProfileSharImage != null) {
                            holder.artistProfileSharImage.setEnabled(true);
                        }
                    }
                }, 2000);
                FirebaseAnalytics.getInstance().onShareClick(true, bannerImageDescriptionData.mArtistData);
                return;
            }
        });
    }

    private void constructShareUrl(final CardData mData) {
        String dynamicLink = null;
        final Uri deepLink = Uri.parse(APIConstants.SHARE_ARTIST_DEEP_LINK_URL);
        String packageName = mContext.getPackageName();
        Uri.Builder builder = new Uri.Builder()
                .scheme("https")
                .authority(mContext.getString(R.string.appcode_for_deeplink))
                .path("/")
                .appendQueryParameter("link", deepLink + mData.generalInfo.type + "/detail/" + mData._id + "/" + mData.generalInfo.title)
                .appendQueryParameter("apn", packageName);

        dynamicLink = builder.build().toString();
        FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLongLink(Uri.parse(dynamicLink))
                .buildShortDynamicLink()
                .addOnCompleteListener((Activity) mContext, new OnCompleteListener<ShortDynamicLink>() {
                    @Override
                    public void onComplete(@NonNull Task<ShortDynamicLink> task) {
                        if (task.isSuccessful()) {
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
                                path = Util.takeScreenShot((Activity) mContext);
                            }
                            String contentName = null;
                            if (mData != null
                                    && mData.generalInfo != null
                                    && mData.generalInfo.title != null) {
                                contentName = mData.generalInfo.title;
                            }

                            String msg = mContext.getString(R.string.share_message);
                            if (!TextUtils.isEmpty(PrefUtils.getInstance().getPrefShareMessage())) {
                                msg = PrefUtils.getInstance().getPrefShareMessage();
                            }
                            if (msg.contains(APIConstants.HASH_CONTENT_NAME)) {
                                msg = msg.replace(APIConstants.HASH_CONTENT_NAME, TextUtils.isEmpty(contentName) ? "" : contentName);
                            }
                            String url = task.getResult().getShortLink().toString();
                            if (!TextUtils.isEmpty(url)) {
                                msg = msg + "\n" + Uri.parse(url.trim());
                            }
                            Util.shareData(mContext, 1, path, msg);// send message.
                        } else {
                            LoggerD.debugOTP(String.valueOf(task.getException()));
                        }
                    }
                });
    }

}
