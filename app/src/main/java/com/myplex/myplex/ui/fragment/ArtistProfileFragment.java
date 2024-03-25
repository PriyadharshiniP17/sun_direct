package com.myplex.myplex.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.github.pedrovgs.LoggerD;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;
import com.myplex.api.APICallback;
import com.myplex.api.APIConstants;
import com.myplex.api.APIResponse;
import com.myplex.api.APIService;
import com.myplex.api.request.content.RequestContentList;
import com.myplex.api.request.user.ArtistProfileContentList;
import com.myplex.model.ApplicationConfig;
import com.myplex.model.CardData;
import com.myplex.model.CardDataImagesItem;
import com.myplex.model.CardResponseData;
import com.myplex.model.CarouselInfoData;
import com.myplex.model.ProfileAPIListAndroid;
import com.myplex.util.AlertDialogUtil;
import com.myplex.util.PrefUtils;
import com.myplex.util.SDKUtils;
import com.myplex.myplex.R;
import com.myplex.myplex.analytics.Analytics;
import com.myplex.myplex.analytics.CleverTap;
import com.myplex.myplex.analytics.FirebaseAnalytics;
import com.myplex.myplex.model.CacheManager;
import com.myplex.myplex.model.ItemClickListenerWithData;
import com.myplex.myplex.ui.activities.BaseActivity;
import com.myplex.myplex.ui.activities.MainActivity;
import com.myplex.myplex.ui.adapter.AdapterArtistProfile;
import com.myplex.myplex.ui.adapter.AdapterBigHorizontalCarousel;
import com.myplex.myplex.ui.views.CardDetailViewFactory;
import com.myplex.myplex.ui.views.HorizontalItemDecorator;
import com.myplex.myplex.ui.views.VerticalSpaceItemDecoration;
import com.myplex.myplex.utils.OnSingleClickListenerWithCardData;
import com.myplex.myplex.utils.Util;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Created by apalya on 12/22/2016.
 */

public class ArtistProfileFragment extends BaseFragment {
    private View rootView;
    private CardData mCardData;
    private String mId;

    private LinearLayout mParentContentLayout;
    private CardDetailViewFactory mCardDetailViewFactory;

    private int mStartIndex = 1;
    private Bundle args;
    private String name, description, fulldescription, imageUrl;
    private static final String TAG = ArtistProfileFragment.class.getSimpleName();
    private List<CardData> mMovieCardData;
    private List<CardData> mComedyClipData;
    private List<CardData> mMusicCardData;
    private List<CardData> mFourthCardData;
    private List<CardData> mFifthCardData;
    private CarouselInfoData carouselInfoData;
    private boolean isShowComedyViewAll;
    private boolean isShowMusicViewAll;
    private boolean isShowMovieViewAll;
    private boolean isShowFifthViewAll;
    private boolean isShowFourthViewAll;
    private ProfileAPIListAndroid mViewAllMovieProfileData;
    private ProfileAPIListAndroid mViewAllProfileMusicData;
    private ProfileAPIListAndroid mViewAllProfileComedyData;
    private ProfileAPIListAndroid mViewAllProfileFourthData;
    private ProfileAPIListAndroid mViewAllProfileFifthData;
    private  ImageView sharebtnimage;
    private CardData mArtistData;

    TextView profileMovieLayoutViewAll,musicViewAll,comedyViewAll,fourthViewAll,fifthViewAll;


    RelativeLayout comedyClipsLinearLayout, musicLinearLayout, fourthLayout, fifthLayout;
    RelativeLayout moviesLinearLayout;
    public Bundle getArgs() {
        return args;
    }

    public void setArgs(Bundle args) {
        this.args = args;
    }

    public static ArtistProfileFragment newInstance(Bundle args) {
        ArtistProfileFragment artistDetails = new ArtistProfileFragment();
        artistDetails.mId=args.getString("ID");
        artistDetails.name = args.getString("NAME");
        artistDetails.description = args.getString("DESCRIPTION");
        artistDetails.fulldescription = args.getString("FULL_DESCRIPTION");
        artistDetails.imageUrl = args.getString("IMAGE_URL");
        artistDetails.mArtistData= (CardData) args.getSerializable("CARD_DATA");
        artistDetails.carouselInfoData = (CarouselInfoData) args.getSerializable(FragmentCarouselViewAll.PARAM_CAROUSEL_DATA);
        artistDetails.setArguments(args);
        return artistDetails;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(CardDetails.PARAM_CARD_ID, mId);
        outState.putSerializable(CardDetails.PARAM_SELECTED_CARD_DATA, mCardData);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (mContext == null) {
            mContext = getActivity();
            mBaseActivity = (BaseActivity) getActivity();
        }
        rootView = inflater.inflate(R.layout.actor_profile_new, container, false);
        //final RelativeLayout relativeLayout = rootView.findViewById(R.id.RLArtistInfo);
        //TextView txtName = rootView.findViewById(R.id.txt1);
        Toolbar mToolbar = rootView.findViewById(R.id.toolbar);
        /*TextView txtdesc = rootView.findViewById(R.id.txt3);
        ImageView actorImageView = rootView.findViewById(R.id.imageView1);*/


        /*Display display =( (BaseActivity)mContext).getWindowManager().getDefaultDisplay();
        int width = display.getWidth();
        int height = (width *9)/16;
        RelativeLayout.LayoutParams parms = new RelativeLayout.LayoutParams(width,height);
        actorImageView.setLayoutParams(parms);
        if (TextUtils.isEmpty(imageUrl)) {
            Picasso.with(mContext).load(R.drawable.movie_thumbnail_placeholder).error(R.drawable
                    .movie_thumbnail_placeholder).placeholder(R.drawable
                    .movie_thumbnail_placeholder).into(actorImageView);
        } else {
            PicassoUtil.with(mContext).load(imageUrl, actorImageView, R.drawable.movie_thumbnail_placeholder);
           *//* PicassoUtil.with(mContext).load(imageUrl, new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    relativeLayout.setBackground(new BitmapDrawable(mContext.getResources(),bitmap));
                }

                @Override
                public void onBitmapFailed(Drawable errorDrawable) {

                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {

                }
            });*//*
        }*/
        //Caused by java.lang.NullPointerException
        // at com.android.myplex.ui.sun.fragment.ArtistProfileFragment.onCreateView(ArtistProfileFragment.java:145)
        if (!TextUtils.isEmpty(name)) {
           // txtName.setText(name);
            mToolbar.setTitle(name.toUpperCase());
        }
        mToolbar.setNavigationIcon(R.drawable.back_icon);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getActivity() != null)
                    getActivity().onBackPressed();
            }
        });

        RecyclerView artistProfileRecyclerView=rootView.findViewById(R.id.recyclerview_artist_profile);
        artistProfileRecyclerView.addItemDecoration(new VerticalSpaceItemDecoration((int) mContext.getResources().getDimension(R.dimen.margin_gap_2)));
        artistProfileRecyclerView.setItemAnimator(null);
        List<ProfileAPIListAndroid> profileDataList=getProfileData();
        ProfileAPIListAndroid bannerDescriptionItem=new ProfileAPIListAndroid();
        bannerDescriptionItem.layoutType=APIConstants.LAYOUT_TYPE_ARTIST_BANNER_DESCRIPTION;
        bannerDescriptionItem.mArtistData=mArtistData;
        profileDataList.add(0,bannerDescriptionItem);
        AdapterArtistProfile adapterArtistProfile=new AdapterArtistProfile(profileDataList,mContext);
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext, RecyclerView.VERTICAL, false);
        artistProfileRecyclerView.setLayoutManager(layoutManager);
        int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, mContext.getResources().getDisplayMetrics());
        VerticalSpaceItemDecoration mVerticalSpaceItemDecoration = new VerticalSpaceItemDecoration(margin);
        artistProfileRecyclerView.removeItemDecoration(mVerticalSpaceItemDecoration);
        artistProfileRecyclerView.addItemDecoration(mVerticalSpaceItemDecoration);
        artistProfileRecyclerView.setAdapter(adapterArtistProfile);

        //getArtistProfileData(mContext);

        //txtdesc.setText(fulldescription);
        /*if (mBaseActivity != null) {
            //ApplicationController.configureOrientation(mBaseActivity);
            mBaseActivity.hideActionBar();
        }*/
        /*sharebtnimage = rootView.findViewById(R.id.artistProfileShareImage);
        mParentContentLayout = rootView.findViewById(R.id.artistdetail_detaillayout);
        moviesLinearLayout = rootView.findViewById(R.id.artist_profile_movie_layout);
        comedyClipsLinearLayout = rootView.findViewById(R.id.artist_profile_comedy_layout);
        musicLinearLayout = rootView.findViewById(R.id.artist_profile_music_layout);
        fourthLayout = rootView.findViewById(R.id.artist_profile_fourth_layout);
        fifthLayout = rootView.findViewById(R.id.artist_profile_fifth_layout);

        profileMovieLayoutViewAll=rootView.findViewById(R.id.textview_view_all);
        comedyViewAll=rootView.findViewById(R.id.profile_comedy_view_all);
        musicViewAll=rootView.findViewById(R.id.music_view_all);
        fourthViewAll=rootView.findViewById(R.id.artist_profile_fourth_view_all);
        fifthViewAll=rootView.findViewById(R.id.artist_profile_fifth_view_all);
*/
        /*if (savedInstanceState == null) {
            savedInstanceState = getArguments();
        }*/
        //initializeActorWorkView();
        //showShareUI();
        //getArtistProfileData(mContext);
        return rootView;
    }

    private List<ProfileAPIListAndroid> getProfileData() {
        APIConstants.profileAPIListAndroid = APIConstants.getProfileAPIListAndroid(mContext);
        List<ProfileAPIListAndroid> profileAPIListAndroids=new ArrayList<>();
        try {
             profileAPIListAndroids = (List<ProfileAPIListAndroid>) SDKUtils.loadObject(APIConstants.profileAPIListAndroid);
            return profileAPIListAndroids;
        } catch (ClassCastException ce) {
            ce.printStackTrace();
        }
        return profileAPIListAndroids;
    }

    private void getArtistProfileData(Context mContext) {
//        ProfileAPIListAndroid profileAPIListAndroids = null;
        APIConstants.profileAPIListAndroid = APIConstants.getProfileAPIListAndroid(mContext);
        try {
            List<ProfileAPIListAndroid> profileAPIListAndroids = (List<ProfileAPIListAndroid>) SDKUtils.loadObject(APIConstants.profileAPIListAndroid);
            if (profileAPIListAndroids != null && profileAPIListAndroids.size() > 0) {
                for (int i=0;i<profileAPIListAndroids.size();i++){
                    ProfileAPIListAndroid profileAndroid = profileAPIListAndroids.get(i);
                    if (i==0) {
                        fetchMovieData(profileAndroid);
                    } else if (i==1) {
                        fetchMusicVideoData(profileAndroid);
                    } else if (i==2) {
                        fetchComedyClipData(profileAndroid);
                    } else if (i==3) {
                        fetchFourthCardDetails(profileAndroid);
                    } else if(i==4){
                        fetchFifthCardDetails(profileAndroid);
                    }
                }
            }

        } catch (ClassCastException ce) {
            ce.printStackTrace();
        }
    }


    private void initializeActorWorkView() {
        mCardDetailViewFactory = new CardDetailViewFactory(mContext);
        mCardDetailViewFactory.setParent(rootView);
//        getArtistComedyClipData();

    }


    private void fetchMovieData(final ProfileAPIListAndroid profileAndroid) {

        ArtistProfileContentList.Params param = new ArtistProfileContentList.Params(profileAndroid.type, mStartIndex, profileAndroid.pageCount, name,
                profileAndroid.publishingHouseId, profileAndroid.orderBy, "-1", profileAndroid.language,profileAndroid.tags);

        ArtistProfileContentList artistProfileMovieList = new ArtistProfileContentList(param, new APICallback<CardResponseData>() {
            @Override
            public void onResponse(APIResponse<CardResponseData> response) {
                mMovieCardData = response.body().results;
                /*moviesLinearLayout.setVisibility(View.GONE);
                if (mMovieCardData != null && mMovieCardData.size() != 0) {
                    moviesLinearLayout.setVisibility(View.VISIBLE);
                    fillMoviesData(mMovieCardData, profileAndroid);
                }*/
            }

            @Override
            public void onFailure(Throwable t, int errorCode) {
                moviesLinearLayout.setVisibility(View.GONE);
            }
        });
        APIService.getInstance().execute(artistProfileMovieList);
    }


    private void fetchComedyClipData(final ProfileAPIListAndroid profileComedyData) {

        String path = null;


        ArtistProfileContentList.Params param = new ArtistProfileContentList.Params(profileComedyData.type, mStartIndex, profileComedyData.pageCount, name,
                profileComedyData.publishingHouseId, profileComedyData.orderBy, "-1", profileComedyData.language,profileComedyData.tags);
        ArtistProfileContentList artistProfileComedyList = new ArtistProfileContentList(param, new APICallback<CardResponseData>() {
            @Override
            public void onResponse(APIResponse<CardResponseData> response) {
                mComedyClipData = response.body().results;
                /*comedyClipsLinearLayout.setVisibility(View.GONE);

                if (mComedyClipData != null && mComedyClipData.size() != 0) {
                    Log.e("Artist","Comedy"+mComedyClipData.size());
                    comedyClipsLinearLayout.setVisibility(View.VISIBLE);
                    fillComedyData(mComedyClipData, profileComedyData);
                }*/
            }

            @Override
            public void onFailure(Throwable t, int errorCode) {
                comedyClipsLinearLayout.setVisibility(View.GONE);
            }
        });
        APIService.getInstance().execute(artistProfileComedyList);
    }

    private void fetchMusicVideoData(final ProfileAPIListAndroid profileMusicVideo) {
        ArtistProfileContentList.Params param = new ArtistProfileContentList.Params(profileMusicVideo.type, mStartIndex, profileMusicVideo.pageCount, name,
                profileMusicVideo.publishingHouseId, profileMusicVideo.orderBy, "-1", profileMusicVideo.language,profileMusicVideo.tags);

        ArtistProfileContentList artistProfileMusicVideoList = new ArtistProfileContentList(param, new APICallback<CardResponseData>() {
            @Override
            public void onResponse(APIResponse<CardResponseData> response) {
                mMusicCardData = response.body().results;
                /*musicLinearLayout.setVisibility(View.GONE);

                if (mMusicCardData != null && mMusicCardData.size() != 0) {
                    Log.e("Artist","Music"+mMusicCardData.size());
                    musicLinearLayout.setVisibility(View.VISIBLE);
                    fillMusicVideosData(mMusicCardData, profileMusicVideo);

                }*/
            }

            @Override
            public void onFailure(Throwable t, int errorCode) {
                musicLinearLayout.setVisibility(View.GONE);
            }
        });
        APIService.getInstance().execute(artistProfileMusicVideoList);
    }

    private void getArtistComedyClipData() {
        RequestContentList.Params param = new RequestContentList.Params("vod", mStartIndex, PrefUtils.getInstance().getArtistProfileComedyClipsListCount(), name, "46", "releaseDate", "-1");
        RequestContentList mRequestContentActorList = new RequestContentList(param, new APICallback<CardResponseData>() {
            @Override
            public void onResponse(APIResponse<CardResponseData> response) {
                if (response != null && response.body() != null && response.body().results != null) {
                    if (isAdded()) {
                        mComedyClipData = response.body().results;
                        if (mComedyClipData != null && mComedyClipData.size() != 0) {
//                            fillComedyData(mComedyClipData);
                        }
                        getArtistData();
                        //Log.d(TAG, "GOT COMEDY CLIPS");
                    }
                }
            }

            @Override
            public void onFailure(Throwable t, int errorCode) {
                if (isAdded()) {
                    AlertDialogUtil.showToastNotification("Unable to get Data");
                    getArtistData();
                    //Log.d(TAG, "GOT COMEDY CLIPS");
                }
            }
        });
        APIService.getInstance().execute(mRequestContentActorList);
    }

    private void getArtistData() {
        RequestContentList.Params param = new RequestContentList.Params("movie", mStartIndex, PrefUtils.getInstance().getArtistProfileMoviesListCount(), name, "releaseDate", "-1");
        RequestContentList mRequestContentActorList = new RequestContentList(param, new APICallback<CardResponseData>() {
            @Override
            public void onResponse(APIResponse<CardResponseData> response) {
                mMovieCardData = response.body().results;
                if (mMovieCardData != null && mMovieCardData.size() != 0) {
//                    fillMoviesData(mMovieCardData);
                }
            }

            @Override
            public void onFailure(Throwable t, int errorCode) {
                AlertDialogUtil.showToastNotification("Unable to get Data");
            }
        });
        APIService.getInstance().execute(mRequestContentActorList);
    }




    private void fetchFifthCardDetails(final ProfileAPIListAndroid profileAPIFifthList) {
        ArtistProfileContentList.Params param = new ArtistProfileContentList.Params(profileAPIFifthList.type, mStartIndex, profileAPIFifthList.pageCount, name,
                profileAPIFifthList.publishingHouseId, profileAPIFifthList.orderBy, "-1", profileAPIFifthList.language,profileAPIFifthList.tags);

        ArtistProfileContentList artistProfileMovieList = new ArtistProfileContentList(param, new APICallback<CardResponseData>() {
            @Override
            public void onResponse(APIResponse<CardResponseData> response) {
                mFifthCardData = response.body().results;
                fifthLayout.setVisibility(View.GONE);
                if (mFifthCardData != null && mFifthCardData.size() != 0) {
                    fifthLayout.setVisibility(View.VISIBLE);
                    //fillFifthCardData(mFifthCardData, profileAPIFifthList);
                }
            }

            @Override
            public void onFailure(Throwable t, int errorCode) {

            }
        });
        APIService.getInstance().execute(artistProfileMovieList);
    }

 /*   private void fillFifthCardData(List<CardData> mFifthCardData, ProfileAPIListAndroid profileAPIFifthList) {
        TextView comedyTitle = rootView.findViewById(R.id.textview_fifth_genre_title);
        RecyclerView fifthClipsRecyclerView = rootView.findViewById(R.id.recycler_view_fifth);
        comedyTitle.setText(profileAPIFifthList.displayName);
        final RecyclerView.ItemDecoration mHorizontalDividerItemDecoration;
        mHorizontalDividerItemDecoration = new HorizontalItemDecorator((int) mContext.getResources().getDimension(R.dimen.margin_gap_2));
        if (profileAPIFifthList.viewAll && mFifthCardData.size() >= profileAPIFifthList.pageCount) {
            isShowFifthViewAll = true;
            fifthViewAll.setVisibility(View.VISIBLE);
        }
        fifthViewAll.setTag(profileAPIFifthList);
        fifthViewAll.setOnClickListener(mViewAllClickListener);
        mViewAllProfileFifthData=profileAPIFifthList;
        final LinearLayoutManager layoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false);
        layoutManager.setItemPrefetchEnabled(false);
        fifthClipsRecyclerView.setLayoutManager(layoutManager);

        if (profileAPIFifthList.layoutType.equalsIgnoreCase(APIConstants.LAYOUT_TYPE_HORIZONTAL_LIST_BIG_ITEM)) {
            AdapterBigHorizontalCarousel adapterTrailers = new AdapterBigHorizontalCarousel(mContext, mFifthCardData);
            adapterTrailers.setData(mFifthCardData);
            adapterTrailers.setOnItemClickListenerWithMovieData(mOnItemClickListenerMovies);
            fifthClipsRecyclerView.setAdapter(adapterTrailers);
        } else if (profileAPIFifthList.layoutType.equalsIgnoreCase(APIConstants.LAYOUT_TYPE_WEEKLY_TRENDING_SMALL_ITEM)) {
            AdapterBigHorizontalCarousel adapterTrailers = new AdapterBigHorizontalCarousel(mContext, mFifthCardData);
            adapterTrailers.setData(mFifthCardData);
            adapterTrailers.setOnItemClickListenerWithMovieData(mOnItemClickListenerMovies);
            fifthClipsRecyclerView.setAdapter(adapterTrailers);
        } else {
            AdapterBigHorizontalCarousel adapterTrailers = new AdapterBigHorizontalCarousel(mContext,mFifthCardData);
            adapterTrailers.setData(mFifthCardData);
            adapterTrailers.setOnItemClickListenerWithMovieData(mOnItemClickListenerMovies);
            fifthClipsRecyclerView.setAdapter(adapterTrailers);
        }

        fifthClipsRecyclerView.addItemDecoration(mHorizontalDividerItemDecoration);
    }*/

    private void fetchFourthCardDetails(final ProfileAPIListAndroid profileFourthList) {
        ArtistProfileContentList.Params param = new ArtistProfileContentList.Params(profileFourthList.type, mStartIndex, profileFourthList.pageCount, name,
                profileFourthList.publishingHouseId, profileFourthList.orderBy, "-1", profileFourthList.language,profileFourthList.tags);

        ArtistProfileContentList artistProfileMovieList = new ArtistProfileContentList(param, new APICallback<CardResponseData>() {
            @Override
            public void onResponse(APIResponse<CardResponseData> response) {
                mFourthCardData = response.body().results;
                fourthLayout.setVisibility(View.GONE);
                if (mFourthCardData != null && mFourthCardData.size() != 0) {
                    fourthLayout.setVisibility(View.VISIBLE);
                    //fillFourthCardData(mFourthCardData, profileFourthList);
                }
            }

            @Override
            public void onFailure(Throwable t, int errorCode) {

            }
        });
        APIService.getInstance().execute(artistProfileMovieList);
    }




    /*replacing url %person% with profile Actor name*/
    private String replaceUrlWithActor(String link, String mName) {
        if (link.contains("%person%")) {
            String path = link.replace("%person%", mName);
            return path;
        }

        return link;
    }

    public final ItemClickListenerWithData mOnItemClickListenerMovies = new ItemClickListenerWithData() {
        @Override
        public void onClick(View view, int position, int parentPosition, CardData carouselData) {
            //movie item clicked we can have movie data here
//            CardData carouselData1 = mMovieCardData.get(position);
            CacheManager.setSelectedCardData(carouselData);
            Bundle args = new Bundle();
            args.putString(CardDetails.PARAM_CARD_ID, carouselData._id);
            args.putBoolean(CardDetails.PARAM_AUTO_PLAY, true);
            if (carouselData != null
                    && carouselData.generalInfo != null) {
                args.putString(CardDetails
                        .PARAM_CARD_DATA_TYPE, carouselData.generalInfo.type);
                if (APIConstants.TYPE_PROGRAM.equalsIgnoreCase(carouselData.generalInfo.type)) {
                    args.putString(CardDetails.PARAM_CARD_ID, carouselData.globalServiceId);
                    args.putString(CardDetails.PARAM_CARD_DATA_TYPE, APIConstants.TYPE_PROGRAM);
                    if (null != carouselData.startDate
                            && null != carouselData.endDate) {
                        Date startDate = Util.getDate(carouselData.startDate);
                        Date endDate = Util.getDate(carouselData.endDate);
                        Date currentDate = new Date();
                        if ((currentDate.after(startDate)
                                && currentDate.before(endDate))
                                || currentDate.after(endDate)) {
                            args.putBoolean(CardDetails
                                    .PARAM_AUTO_PLAY, true);
                            args.putBoolean(CardDetails
                                    .PARAM_AUTO_PLAY_MINIMIZED, false);
                        }
                    }
                }

            }
            args.putString(Analytics.PROPERTY_SOURCE, Analytics.ARTIST_PROFILE);
            args.putString(Analytics.PROPERTY_SOURCE_DETAILS, parentPosition+"");

            ((BaseActivity) mContext).showDetailsFragment(args, carouselData);



        }

    };
    public final OnSingleClickListenerWithCardData mOnItemClickListenerComedyClips = new OnSingleClickListenerWithCardData() {
        @Override
        public void onSingleClick(View view, int position, int parentPosition, CardData carouselData) {
            //movie item clicked we can have movie data here
            try {
                CardData carouselData1 = mComedyClipData.get(position);
                if (carouselData1 != null && position == mComedyClipData.size() - 1 && isShowComedyViewAll) {
                    Intent intent = new Intent(mContext, MainActivity.class);
                    intent.putExtra(APIConstants.NAME_OF_ARTIST, name);
                    intent.putExtra(APIConstants.COMING_FROM_ARTIST_PROFILE, true);
                    intent.putExtra(APIConstants.TYPE_OF_CONTENT, APIConstants.TYPE_VOD);
                    intent.putExtra(APIConstants.TYPE_ACTOR_ROFILE_VIEW_ALL_DATA,mViewAllProfileComedyData);
                    if (carouselData1.publishingHouse != null)
                        intent.putExtra(APIConstants.PUBLISHING_ID, carouselData1.publishingHouse.publishingHouseId);
                /*Caused by android.os.TransactionTooLargeException: data parcel size 1371932 bytes
                at com.android.myplex.ui.views.BigHorizontalItem$2.onClick(BigHorizontalItem.java:333)
                at com.android.myplex.ui.adapters.AdapterBigHorizontalCarousel$CarouselDataViewHolder.onClick(AdapterBigHorizontalCarousel.java:261)
                */
                    try {
                        mContext.startActivity(intent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (carouselData1 != null && carouselData._id != null /*&& position < mComedyClipData.size() - 1*/) {
                    /*Util.showAlertDialog("Clicked");
                    carouselData.source = Analytics.ARTIST_PROFILE;
                    carouselData.sourceDetails = Analytics.COMEDY_CLIPS;
                    carouselData.comedy_mCount = PrefUtils.getInstance().getArtistProfileComedyClipsListCount();
                    carouselData.comedy_startIndex = 1;
                    carouselData.comedy_relatedCast = name;
                    ScopedBus.getInstance().post(new ContentDetailEvent(carouselData, carouselInfoData));*/
                } else {
                    //Log.d(TAG,"Clicked else");
                }
            } catch (Exception e) {
                e.printStackTrace();
                AlertDialogUtil.showToastNotification("No Data");
            }
        }

    };



    public final OnSingleClickListenerWithCardData mOnItemClickListenerFourth = new OnSingleClickListenerWithCardData() {
        @Override
        public void onSingleClick(View view, int position, int parentPosition, CardData carouselData) {
            //movie item clicked we can have movie data here
            CardData carouselData1 = mFourthCardData.get(position);
            if (carouselData1 != null && position == mFourthCardData.size() - 1 && isShowFourthViewAll) {
                Intent intent = new Intent(mContext, MainActivity.class);
                intent.putExtra(APIConstants.NAME_OF_ARTIST, name);
                intent.putExtra(APIConstants.COMING_FROM_ARTIST_PROFILE, true);
                intent.putExtra(APIConstants.TYPE_OF_CONTENT, APIConstants.TYPE_MOVIE);
                intent.putExtra(APIConstants.TYPE_ACTOR_ROFILE_VIEW_ALL_DATA,mViewAllProfileFourthData);
                if (carouselData1.publishingHouse != null)
                    intent.putExtra(APIConstants.PUBLISHING_ID, carouselData1.publishingHouse.publishingHouseId);
                try {
                    mContext.startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (carouselData1 != null && carouselData._id != null /*&& position < mFourthCardData.size() - 1*/) {
                //Log.d(TAG,"Clicked");
/*                carouselData.source = Analytics.ARTIST_PROFILE;
                carouselData.sourceDetails = Analytics.TYPE_TVSHOWS;
                ScopedBus.getInstance().post(new ContentDetailEvent(carouselData, carouselInfoData));*/
            } else {
                //Log.d(TAG,"Clicked else");
            }
        }

    };


    public final OnSingleClickListenerWithCardData mOnItemClickListenerFifth = new OnSingleClickListenerWithCardData() {
        @Override
        public void onSingleClick(View view, int position, int parentPosition, CardData carouselData) {
            //movie item clicked we can have movie data here
            CardData carouselData1 = mFifthCardData.get(position);
            if (carouselData1 != null && position == mFifthCardData.size() - 1 && isShowFifthViewAll) {
                Intent intent = new Intent(mContext, MainActivity.class);
                intent.putExtra(APIConstants.NAME_OF_ARTIST, name);
                intent.putExtra(APIConstants.COMING_FROM_ARTIST_PROFILE, true);
                intent.putExtra(APIConstants.TYPE_OF_CONTENT, APIConstants.TYPE_MOVIE);
                intent.putExtra(APIConstants.TYPE_ACTOR_ROFILE_VIEW_ALL_DATA,mViewAllProfileFifthData);
                if (carouselData1.publishingHouse != null)
                    intent.putExtra(APIConstants.PUBLISHING_ID, carouselData1.publishingHouse.publishingHouseId);
                try {
                    mContext.startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (carouselData1 != null && carouselData._id != null /*&& position < mFifthCardData.size() - 1*/) {
                //Log.d(TAG,"Clicked");
                /*carouselData.source = Analytics.ARTIST_PROFILE;
                carouselData.sourceDetails = Analytics.MOVIES;
                ScopedBus.getInstance().post(new ContentDetailEvent(carouselData, carouselInfoData));*/
            } else {
                //Log.d(TAG,"Clicked else");
            }
        }

    };

    private final List<CardData> mDummyCarouselData = new ArrayList<>();

    private List<CardData> getDummyCarouselData() {
        if (!mDummyCarouselData.isEmpty()) {
            return mDummyCarouselData;
        }
        for (int i = 0; i < 10; i++) {
            mDummyCarouselData.add(new CardData());
        }
        return mDummyCarouselData;
    }

    private void showShareUI() {
        sharebtnimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sharebtnimage.setEnabled(false);
                constructShareUrl(mArtistData);
                sharebtnimage.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (sharebtnimage != null) {
                            sharebtnimage.setEnabled(true);
                        }
                    }
                }, 2000);
                FirebaseAnalytics.getInstance().onShareClick(true, mArtistData);
                return;
            }
        });
    }

    private void constructShareUrl(final CardData mData) {
        String dynamicLink = null;

        final Uri deepLink = Uri.parse(APIConstants.SHARE_ARTIST_DEEP_LINK_URL);
        String packageName = mContext.getPackageName();

// Build the link with all required parameters
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
/*if (mData.generalInfo.type != null && mData.generalInfo._id != null && mData.generalInfo.title != null) {
url = APIConstants.SHARE_ARTIST_DEEP_LINK_URL + mData.generalInfo.type + "/detail/" + mData.generalInfo._id + "/" + mData.generalInfo.title + "?sourceType=android";
} else {
url = APIConstants.getShareUrl(mContext);
}*/
// String shareUrl=PrefUtils.getInstance().getShortLink();
                            if (!TextUtils.isEmpty(url)) {
                                msg = msg + "\n" + Uri.parse(url.trim());
                            }
                            Util.shareData(mContext, 1, path, msg);// send message.

// PrefUtils.getInstance().setShortLink(task.getResult().getShortLink().toString());
                        } else {
                            LoggerD.debugOTP(String.valueOf(task.getException()));
                        }
                    }
                });
    }


    private final View.OnClickListener mViewAllClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
               if(v.getTag() instanceof ProfileAPIListAndroid){
                ProfileAPIListAndroid profileAPIListAndroid= (ProfileAPIListAndroid) v.getTag();
                   showCarouselViewAllFragment(profileAPIListAndroid);
               }
        }
    };

    private void showCarouselViewAllFragment(ProfileAPIListAndroid profileAPIListAndroid) {
        Bundle args = new Bundle();
        CacheManager.setCarouselInfoData(carouselInfoData);
        args.putString(FragmentCarouselViewAll.PARAM_FRAGMENT_TYPE,FragmentCarouselViewAll.PARAM_FRAGMENT_TYPE_ARTIST_PROFILE);
        args.putSerializable(FragmentCarouselViewAll.PARAM_ARTIST_NAME,name);
        args.putSerializable(CleverTap.PROPERTY_PROFILE_API_LIST, profileAPIListAndroid);
        ((MainActivity) mContext).pushFragment(FragmentCarouselViewAll.newInstance(args));
    }
}
