package com.myplex.myplex.ui.activities;

import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_USER;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.google.android.play.core.install.model.AppUpdateType.FLEXIBLE;
import static com.myplex.api.APIConstants.APP_ACTION_WATCH_HISTORY;
import static com.myplex.api.APIConstants.CONTACT_US;
import static com.myplex.api.APIConstants.IS_SeasonUIForBack;
import static com.myplex.api.APIConstants.MENU_HOME;
import static com.myplex.api.APIConstants.MENU_LIVE_TV;
import static com.myplex.api.APIConstants.MENU_SEARCH;
import static com.myplex.api.APIConstants.MENU_VOD;
import static com.myplex.api.APIConstants.PREMIUM;
import static com.myplex.api.APIConstants.PRIVACY_POLICY;
import static com.myplex.api.APIConstants.TERMS_AND_CONDITIONS;
import static com.myplex.myplex.ApplicationController.getAppContext;
import static com.myplex.myplex.ApplicationController.getApplicationConfig;
import static com.myplex.myplex.BuildConfig.FLAVOR;
import static com.myplex.myplex.BuildConfig.VERSION_NAME;
import static com.myplex.myplex.ui.fragment.FragmentCardDetailsDescription.IS_PROFILE_UPDATE_SUCCESS;
import static com.myplex.myplex.ui.fragment.FragmentCardDetailsDescription.PROFILE_UPDATE_REQUEST;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.display.DisplayManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.provider.Settings;
import android.text.Editable;
import android.text.Html;
import android.text.InputFilter;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.core.view.MenuItemCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.mediarouter.app.MediaRouteActionProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.apalya.myplex.eventlogger.MyplexEvent;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.github.pedrovgs.DraggableListener;
import com.github.pedrovgs.DraggablePanel;
import com.github.pedrovgs.LoggerD;
import com.google.ads.interactivemedia.pal.ConsentSettings;
import com.google.ads.interactivemedia.pal.NonceLoader;
import com.google.ads.interactivemedia.pal.NonceManager;
import com.google.ads.interactivemedia.pal.NonceRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.SessionManager;
import com.google.android.gms.cast.framework.SessionManagerListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.InstallState;
import com.google.android.play.core.install.InstallStateUpdatedListener;
import com.google.android.play.core.install.model.InstallStatus;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.tasks.OnSuccessListener;
import com.google.android.play.core.tasks.Task;
import com.google.firebase.inappmessaging.FirebaseInAppMessaging;
import com.google.firebase.inappmessaging.FirebaseInAppMessagingDisplayCallbacks;
import com.google.firebase.inappmessaging.model.InAppMessage;
import com.google.gson.Gson;
import com.myplex.api.APICallback;
import com.myplex.api.APIConstants;
import com.myplex.api.APIRequest;
import com.myplex.api.APIResponse;
import com.myplex.api.APIService;
import com.myplex.api.myplexAPI;
import com.myplex.api.myplexAPISDK;
import com.myplex.api.request.content.FavouriteCheckRequest;
import com.myplex.api.request.content.FavouriteRequest;
import com.myplex.api.request.content.FilterRequest;
import com.myplex.api.request.content.RequestMySubscribedPacks;
import com.myplex.api.request.epg.ChannelListEPG;
import com.myplex.api.request.user.CityListRequest;
import com.myplex.api.request.user.CommentsMessagePost;
import com.myplex.api.request.user.CountriesListRequest;
import com.myplex.api.request.user.DeviceUnRegRequest;
import com.myplex.api.request.user.MSISDNLoginEncrypted;
import com.myplex.api.request.user.NotificationsListRequest;
import com.myplex.api.request.user.OfferedPacksRequest;
import com.myplex.api.request.user.SSOLoginRequest;
import com.myplex.api.request.user.SignOut;
import com.myplex.api.request.user.StatesListRequest;
import com.myplex.api.request.user.UpdateProfileRequest;
import com.myplex.api.request.user.UserProfileRequest;
import com.myplex.model.AdPopUpNotificationListResponse;
import com.myplex.model.AlarmData;
import com.myplex.model.AlarmsSetData;
import com.myplex.model.ApplicationConfig;
import com.myplex.model.BaseResponseData;
import com.myplex.model.CardData;
import com.myplex.model.CardDataGeneralInfo;
import com.myplex.model.CardDataImagesItem;
import com.myplex.model.CardDataPackages;
import com.myplex.model.CardDataVideos;
import com.myplex.model.CardDataVideosItem;
import com.myplex.model.CardDownloadData;
import com.myplex.model.CardDownloadedDataList;
import com.myplex.model.CarouselInfoData;
import com.myplex.model.ChannelsEPGResponseData;
import com.myplex.model.CountriesData;
import com.myplex.model.CountriesResponse;
import com.myplex.model.FavouriteResponse;
import com.myplex.model.FilterItem;
import com.myplex.model.GenreFilterData;
import com.myplex.model.GenresData;
import com.myplex.model.Languages;
import com.myplex.model.MenuDataModel;
import com.myplex.model.MsisdnData;
import com.myplex.model.MySubscribedPacksResponseData;
import com.myplex.model.NotificationList;
import com.myplex.model.OfferResponseData;
import com.myplex.model.PromoAdData;
import com.myplex.model.RatingScreen;
import com.myplex.model.ResultNotification;
import com.myplex.model.Terms;
import com.myplex.model.UserProfileResponseData;
import com.myplex.model.VersionData;
import com.myplex.myplex.ApplicationController;
import com.myplex.myplex.R;
import com.myplex.myplex.analytics.Analytics;
import com.myplex.myplex.analytics.AppsFlyerTracker;
import com.myplex.myplex.analytics.CleverTap;
import com.myplex.myplex.analytics.ComScoreAnalytics;
import com.myplex.myplex.analytics.FirebaseAnalytics;
import com.myplex.myplex.download.ErosNowDownloadManager;
import com.myplex.myplex.events.EventNetworkConnectionChange;
import com.myplex.myplex.events.MessageEvent;
import com.myplex.myplex.events.OpenFilterEvent;
import com.myplex.myplex.events.ScopedBus;
import com.myplex.myplex.events.SubscriptionsDataEvent;
import com.myplex.myplex.events.UpdateFilterDataEvent;
import com.myplex.myplex.model.CacheManager;
import com.myplex.myplex.model.EPG;
import com.myplex.myplex.model.recyclerViewScrollListener;
import com.myplex.myplex.partner.hungama.HungamaPartnerHandler;
import com.myplex.myplex.recievers.ConnectivityReceiver;
import com.myplex.myplex.subscribe.SubscriptionWebActivity;
import com.myplex.myplex.ui.adapter.AdapterRatingFeedBackPopUP;
import com.myplex.myplex.ui.adapter.CustomPagerAdapter;
import com.myplex.myplex.ui.adapter.DrawerListAdapter;
import com.myplex.myplex.ui.adapter.FragmentWatchlistHistory;
import com.myplex.myplex.ui.adapter.HomePagerAdapter;
import com.myplex.myplex.ui.adapter.HomePagerAdapterDynamicMenu;
import com.myplex.myplex.ui.fragment.ArtistProfileFragment;
import com.myplex.myplex.ui.fragment.BaseFragment;
import com.myplex.myplex.ui.fragment.CardDetails;
import com.myplex.myplex.ui.fragment.FilterFragment;
import com.myplex.myplex.ui.fragment.FragmentAppCarouselInfo;
import com.myplex.myplex.ui.fragment.FragmentCardDetailsDescription;
import com.myplex.myplex.ui.fragment.FragmentCardDetailsPlayer;
import com.myplex.myplex.ui.fragment.FragmentCarouselInfo;
import com.myplex.myplex.ui.fragment.FragmentCarouselViewAll;
import com.myplex.myplex.ui.fragment.FragmentChannelEpg;
import com.myplex.myplex.ui.fragment.FragmentLanguageCarouselInfo;
import com.myplex.myplex.ui.fragment.FragmentLanguageInfo;
import com.myplex.myplex.ui.fragment.FragmentPreferredLanguages;
import com.myplex.myplex.ui.fragment.FragmentRelatedVODList;
import com.myplex.myplex.ui.fragment.FragmentVODList;
import com.myplex.myplex.ui.fragment.FragmentWebView;
import com.myplex.myplex.ui.fragment.FullScreenWebViewFragment;
import com.myplex.myplex.ui.fragment.LiveTVFragment;
import com.myplex.myplex.ui.fragment.MyDownloadsFragment;
import com.myplex.myplex.ui.fragment.MyWatchlistFavouritesFragment;
import com.myplex.myplex.ui.fragment.PackagesFragment;
import com.myplex.myplex.ui.fragment.SearchSuggestions;
import com.myplex.myplex.ui.fragment.SearchSuggestionsWithFilter;
import com.myplex.myplex.ui.fragment.SmallSquareItemsFragment;
import com.myplex.myplex.ui.views.AboutDialogWebView;
import com.myplex.myplex.ui.views.ContinueLiveCardPlayerCallback;
import com.myplex.myplex.ui.views.ContinueVODPlayerCallback;
import com.myplex.myplex.ui.views.FeedBackDialog;
import com.myplex.myplex.ui.views.HorizontalItemDecorator;
import com.myplex.myplex.ui.views.PopUpWindow;
import com.myplex.myplex.ui.views.RatingbarCustom;
import com.myplex.myplex.utils.Blur;
import com.myplex.myplex.utils.ChromeTabUtils;
import com.myplex.myplex.utils.CustomHorizontalScroll;
import com.myplex.myplex.utils.DeviceUtils;
import com.myplex.myplex.utils.LiveCardPlayerCallback;
import com.myplex.myplex.utils.MOUUpdateRequestStorageList;
import com.myplex.myplex.utils.OtpReader;
import com.myplex.myplex.utils.TypefaceSpan;
import com.myplex.myplex.utils.UiUtil;
import com.myplex.myplex.utils.Util;
import com.myplex.player_config.DownloadManagerMaintainer;
import com.myplex.subscribe.SubcriptionEngine;
import com.myplex.util.AlertDialogUtil;
import com.myplex.util.ConnectivityUtil;
import com.myplex.util.PrefUtils;
import com.myplex.util.PropertiesHandler;
import com.myplex.util.SDKLogger;
import com.myplex.util.SDKUtils;
import com.myplex.util.VersionUpdateUtil;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;

import de.hdodenhof.circleimageview.CircleImageView;
import eightbitlab.com.blurview.RenderScriptBlur;
import viewpagerindicator.CustomViewPager;
import viewpagerindicator.TabPageIndicator;


//import com.hifx.lens.Lens;
//import com.hifx.ssolib.Model.SSOcallback;
//import com.hifx.ssolib.SSO;

//import com.facebook.AccessToken;
//import com.facebook.login.LoginManager;
//import com.hifx.lens.Lens;
//import com.hifx.ssolib.Model.SSOcallback;
//import com.hifx.ssolib.SSO;
//import com.myplex.myplex.BuildConfig;
//import static com.myplex.myplex.ui.fragment.PackagesFragment.PARAM_SUBSCRIPTION_TYPE_NONE;
//import static com.myplex.myplex.ApplicationController.getApplicationConfig;
//import static com.myplex.myplex.ApplicationController.getApplicationConfig;
//import static com.myplex.myplex.subscribe.SubscriptionWebActivity.SUBSCRIPTION_REQUEST;
//import static com.myplex.myplex.ui.fragment.FragmentCardDetailsDescription.IS_PROFILE_UPDATE_SUCCESS;
//import static com.myplex.myplex.ui.fragment.FragmentCardDetailsDescription.PROFILE_UPDATE_REQUEST;
//import static com.myplex.myplex.ui.fragment.LoginFragment.PLAY_SERVICES_RESOLUTION_REQUEST;
//import static com.myplex.myplex.utils.MOUTracker.makeMouUpdateRequestForOffline;

public class MainActivity extends BaseActivity implements DrawerListAdapter.OnItemClickListener, OtpReader.OTPListener2, recyclerViewScrollListener, InstallStateUpdatedListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    public static final int SECTION_MUSIC = 334;
    public static final int SECTION_MUSIC_VIDEOS = 335;
    public static final int SECTION_KIDS = 336;
    private static final String FRAGMENT_PLAYER_ARGS_STATE = "player_args_state";
    private static final String FRAGMENT_PLAYER_CARD_DATA_STATE = "player_carddata_state";
    private static final int SMS_CONFIRMATION_TIMEOUT = 60 * 15;
    public static final int INTENT_REQUEST_TYPE_LOGIN = 10;
    public static final int INTENT_RESPONSE_TYPE_SUCCESS = 10;
    public static final int INTENT_RESPONSE_TYPE_SUCCESS_SUBSCRIPTION_FAILED = 11;
    public static final String INTENT_PARAM_TOAST_MESSAGE = "launchMessage";
    public static final int INTENT_RESPONSE_TYPE_EMAIL_UPDATE_SUCCESS = 12;
    private static final int PERMISSION_REQUEST_CODE = 221;
    private static final int PARAM_SUBSCRIPTION_TYPE_NONE =0;
    private static long SEARCH_TIMER_DELAY = 200;
    private static final int REQUEST_PERMISSION_SETTING = 421;
    private int mSectionType = SECTION_LIVE;
    public static final int SECTION_MOVIES = 331;
    public static final int SECTION_TVSHOWS = 332;
    public static final int SECTION_VIDEOS = 333;
    public static final int SECTION_OTHER = 337;
    public static final int SECTION_LIVE = 330;
    private TabPageIndicator mTabPageIndicator;
    private TabLayout menuTabs;
    private CustomViewPager mViewPager;
    public HomePagerAdapterDynamicMenu homePagerAdapterDynamicMenu;
    private Toolbar mToolbar;
    private ImageView appLogo;
    private ImageView editProfileIcon;
    public static boolean isOpen = false;

    private int changeFrequency;
    private String mSearchQuery;
    private String searchedQuery;

    private Context mContext;
    private CastContext mCastContext;
    private SearchView mSearchView;
    private boolean isSearchviewFocusAllowed = false;
    private PopUpWindow mFilterMenuPopupWindow;
    private View mFilterMenuPopup;
    private RelativeLayout mPopBlurredLayout;
    //    private LinearLayout mApplyLayout;
    private TextView mFilterLoadingTxt;
    private RelativeLayout helpLayout;
    private Menu mMenu;
    SearchView.SearchAutoComplete mSearchText;
    private boolean isRetryAlreadyDone = false;
    private BaseFragment mCurrentFragment;
    private Stack<BaseFragment> mFragmentStack = new Stack<>();
    public DraggablePanel mDraggablePanel;
    public static final int PLAYER_INITIATED = 4;
    public static final int PLAYER_STOPPED = 3;
    public static final int PLAYER_PLAY = 1;
    public static final int  WAIT_FORRETRY = 99 ;
    public FragmentCardDetailsPlayer mFragmentCardDetailsPlayer;
    private FragmentCardDetailsDescription mFragmentCardDetailsDescription;
    private DraggableListener mDetailsDraggableListener;
    private LiveCardPlayerCallback liveCardPlayerCallback;
    private ContinueVODPlayerCallback continueVODPlayerCallback;
    private ContinueLiveCardPlayerCallback continueLiveCardPlayerCallback;

    public DrawerLayout mDrawerLayout;
    private RecyclerView mDrawerRecycleView;
    private DrawerListAdapter drawerListAdapter;
    private FrameLayout mFilterLayout;
    private HashMap<String, Integer> tabAdState = new HashMap<>();
    private boolean isAnimating = false;
    private FrameLayout mTabPagerRootLayout;
    private int ORIGINAL_VMAX_PARENT_BOTTOM_PARAM;
    private RelativeLayout.LayoutParams vmaxLayoutParams;
    private int TAB_PAGER_HEIGHT = 0;
    public int APP_UPDATE_REQUEST_CODE = 101;
    MenuItem notifyMenuItem;
    private CardData mOldDownloadContentCardData;
    private Bundle mOldDownloadContentBundle;

    private String state = "";
    private String country = "";
    private String city = "";
    private List<CountriesData> countriesList = new ArrayList<>();
    private List<CountriesData> statesList = new ArrayList<>();
    private List<CountriesData> citiesList = new ArrayList<>();
    Spinner countrySpinner, stateSpinner, citySpinner;
    EditText cityEdit, pincodeEt, addressEt;
    Dialog editProfileDialog;
    private String editUserName, editLastName, editGender, editMobile, editAge, editDob, editEmail;

    private NonceLoader nonceLoader;
    private NonceManager nonceManager = null;
    private ConsentSettings consentSettings;
    public static String nonceString = "";
    private com.myplex.myplex.ui.fragment.epg.EPG epgData;
    private HashMap<String,Boolean> epgDataTracker = new HashMap<String,Boolean>();
    private static final float END_SCALE = 0.8f;
    private CardView homeLinearLayout;
    private CardView homeLinearLayoutcard;
    float radius = 15f;

    public boolean isLaunch = false;
    public boolean inAppMessageShow = false;
    public boolean isInAppDisplaying = false;
    public boolean showSubscriptionView = false;
    private CardData inAppCardData;
    private FirebaseInAppMessagingDisplayCallbacks callbacks;
    private AlertDialog alert;
    public boolean isShowHomePopUp=true;
    private boolean isPushedFragment = false;

    @Keep
    public static int certificateStatusBitField = 0;
    @Keep
    public static int dateStatusBitField = 0;
    @Keep
    public static int contextStatusBitField = 0;
    @Keep
    public static int manifestStatusBitField = 0;
    @Keep
    public static int dexSignatureStatusBitField = 0;

    public static AnalyticsPlayerState ANALYTICS_STATE = AnalyticsPlayerState.RESUME;

    public static boolean isRefreshScreen = false;
    public boolean isSearchScreenVisible=false;
    private int enabledisablePlayerLogsClickCount = 0;

    public boolean IsScrolled1st = true;


    public String getQuery() {
        if (mSearchView != null && mSearchView.getQuery() != null) {
            return mSearchView.getQuery().toString();
        }
        return null;
    }


    private DraggableListener mDraggableListener = new DraggableListener() {
        @Override
        public void onMaximized() {
            if (mDetailsDraggableListener != null){
                mDetailsDraggableListener.onMaximized();
            }
            if (mDraggablePanel.getVisibility() == View.VISIBLE) {
                //sendMiniPlayerEnabledBroadCast();
            }
            if (DeviceUtils.isTablet(mContext) && mDraggablePanel!=null) {
                mDraggablePanel.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
                mDraggablePanel.requestLayout();
                ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) mDraggablePanel.getLayoutParams();
                layoutParams.setMargins(0, 0, 0, 0);
                //mFragmentCardDetailsPlayer.disableDraggablePanel();
                mFragmentCardDetailsPlayer.enableDraggablePanel();
                mDraggablePanel.setLayoutParams(layoutParams);
               // mDraggablePanel.setBackgroundColor(getResources().getColor(R.color.red1));
            }
            try {
                hideSoftInputKeyBoard(mSearchView);
            } catch (Exception e) {
                e.printStackTrace();
            }
            //Log.v(TAG, "onMaximized()");
            //Fixed the player in landscape displaying the bottom carousels and the back button is not displaying
            if (isMediaPlaying() || (((MainActivity) mContext).mFragmentCardDetailsPlayer!=null
                    && ((MainActivity) mContext).mFragmentCardDetailsPlayer.mPlayer!=null
                    && (((MainActivity) mContext).mFragmentCardDetailsPlayer.mPlayer.mPlayerState==PLAYER_PLAY )
                    || ((MainActivity) mContext).mFragmentCardDetailsPlayer.mPlayer.mPlayerState==PLAYER_INITIATED)) {
                hideTabIndicator();
            }
            //new Handler(Looper.getMainLooper()).post(() -> {
                if( ((MainActivity) mContext).mFragmentCardDetailsDescription.topContent_layout != null &&
                        !((MainActivity) mContext).mFragmentCardDetailsPlayer.mPlayer.isMinimized()){
                    ((MainActivity) mContext).mFragmentCardDetailsDescription.topContent_layout.setVisibility(View.VISIBLE);
                }
          //  });
        }

        @Override
        public void onMinimized() {
            //Empty
            if(mTabPagerRootLayout.getVisibility() == VISIBLE || isSearchScreenVisible)
                updateBottomBar(true,0);
            if (mDetailsDraggableListener != null && !(PrefUtils.getInstance().getSubscriptionStatusString()!=null && PrefUtils.getInstance().getSubscriptionStatusString().equalsIgnoreCase(APIConstants.USER_NOT_SUBSCRIBED))) {
                if(((MainActivity) mContext).mFragmentCardDetailsPlayer.mPlayer.mPlayerState==PLAYER_INITIATED || ((MainActivity) mContext).mFragmentCardDetailsPlayer.mPlayer.mPlayerState==PLAYER_STOPPED){
                    // commented below code due to swipe the doc player in loading state also
                /*    if(!((MainActivity) mContext).mFragmentCardDetailsPlayer.mPlayer.isMinimized()) {
                        ((MainActivity) mContext).mFragmentCardDetailsPlayer.closePlayerFragment();
                        ((MainActivity) mContext).mFragmentCardDetailsPlayer.mPlayer.closePlayer();
                    }
                        else*/ {
                            mDetailsDraggableListener.onMinimized();
                        }

                    }else{
                        mDetailsDraggableListener.onMinimized();
                    }
                }else{
//                    mDetailsDraggableListener.onMinimized();
//                closeDraggablePanel();
                closePlayerFragment();
                }

            if (mDraggablePanel.getVisibility() == View.VISIBLE) {
               // sendMiniPlayerEnabledBroadCast();

            }
            enableNavigation();
            //Log.v(TAG, "onMinimized()");
            if (mCurrentFragment == null) {
                if(mTabPagerRootLayout.getVisibility() == VISIBLE)
                    showTabIndicator();
            }
           /* if(((MainActivity) mContext).mFragmentCardDetailsPlayer.isMiniMized) {
              //  minizePlayerAboveTabPageIndicator();
               *//* updateBottomBar(true);
                showTabIndicator();*//*
                mTabPagerRootLayout.setVisibility(VISIBLE);
                ((MainActivity) mContext).mFragmentCardDetailsPlayer.isMiniMized = false;
            }*/
            //new Handler(Looper.getMainLooper()).post(() -> {
                if( ((MainActivity) mContext).mFragmentCardDetailsDescription.topContent_layout != null &&
                        ((MainActivity) mContext).mFragmentCardDetailsPlayer.mPlayer.isMinimized()){
                    ((MainActivity) mContext).mFragmentCardDetailsDescription.topContent_layout.setVisibility(View.GONE);

                    if(isPushedFragment){
                        minimizePlayerAtTabPageIndicator();
                    }
                }else{

                }
          //  });
        }

        @Override
        public void onClosedToLeft() {
            //Log.v(TAG, "onClosedToLeft()");
            enableNavigation();
            if (mDetailsDraggableListener != null) {
                mDetailsDraggableListener.onClosedToLeft();
            }
//            mDraggablePanel.maximize();
            closePlayerFragment();
            showTabIndicator();
            if (mFragmentStack != null && mFragmentStack.size() < 1) {
                sendMiniPlayerDisableddBroadCast();
            }
        }

        @Override
        public void onClosedToRight() {
            //Log.v(TAG, "onClosedToRight()");
            if (mDetailsDraggableListener != null) {
                mDetailsDraggableListener.onClosedToRight();
            }
            enableNavigation();
//            mDraggablePanel.maximize();
            closePlayerFragment();
            showTabIndicator();
            if (mFragmentStack != null && mFragmentStack.size() < 1) {
                sendMiniPlayerDisableddBroadCast();
            }
        }
    };


    private boolean isVoiceButtonClicked;
    private ImageView mVoiceButton;
    private ImageView mGoButton;
    private ImageView mSearchButton;
    private ImageView mSearchCollapsedIcon;
    private ImageView mSearchCloseButton;


    private SessionManager mCastManager;
    private SessionManagerListener<CastSession> mCastConsumer;
    private MenuItem mMediaRouteMenuItem;
    private PlaybackState mPlaybackState;
    private ProgressDialog mProgressDialog;
    private List<CarouselInfoData> mListCarouselInfo;
    private List<CarouselInfoData> mListCarouselInfoDrawer;
    private RelativeLayout mLayoutRetry;
    private TextView mTextViewErrorRetryAgain;
    private ImageView mImageViewRetry;
    private int mCurrentSelectedPagePosition;
    private String mCurrentSelectedPagePositionTitle;
    private ActionBarDrawerToggle toggle;
    private ProgressBar mProgressBar;
    private  CircleImageView profile_iv;
    private TextView mTitle;
    private LinearLayout myAccountLayout;
    private TextView navMenuMainHeadingText;
    // private TextView navMenuSubHeadingText;
    private TextView mobile_number;
    private TextView mSMCNoDisplay;
    private CheckBox checkbox_auto_pause_toggle;
    private CheckBox checkboxAutoPlayToggle;
    private CollapsingToolbarLayout collapsingToolbarLayout;

    private ViewPager.OnPageChangeListener mTabPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            CarouselInfoData carouselInfoData = tabListData.get(position);
            mCurrentSelectedPagePositionTitle = carouselInfoData.title;
            if (carouselInfoData.title.equalsIgnoreCase(getResources().getString(R.string.navigation_my_watchlist))) {
                MyWatchlistFavouritesFragment myWatchlistFavouritesFragment = new MyWatchlistFavouritesFragment();
                if (PrefUtils.getInstance().getVernacularLanguage()) {
                    myWatchlistFavouritesFragment.setAlttitle(carouselInfoData.altTitle);
                    myWatchlistFavouritesFragment.setRequestType(APIConstants.WATCHLIST_FETCH_REQUEST);
                }
                mViewPager.setCurrentItem(0);
                pushFragment(myWatchlistFavouritesFragment);
            }
            if (carouselInfoData.title.equalsIgnoreCase(getResources().getString(R.string.myStuff))) {
                CleverTap.eventPageViewed(CleverTap.PAGE_MYPACKS);
                mViewPager.setCurrentItem(0);
                Intent intent = new Intent(mContext, ActivityMyPacks.class);
                startActivity(intent);
            }
            if (carouselInfoData.title.equalsIgnoreCase(getResources().getString(R.string.search))) {
                mViewPager.setCurrentItem(0);
                final MenuItem searchItem = mMenu.findItem(R.id.action_search);
                searchItem.setVisible(false);
                mSearchView.setIconified(false);
                mSearchView.setFocusable(true);
                showSearchFragment("", true);
            }
            /*if (carouselInfoData.title.equalsIgnoreCase(getResources().getString(R.string.home))) {
                mViewPager.setCurrentItem(0);
                MyWatchlistFavouritesFragment myWatchlistFavouritesFragment = new MyWatchlistFavouritesFragment();
                pushFragment(myWatchlistFavouritesFragment);
                myWatchlistFavouritesFragment.setRequestType(APIConstants.FAVOURITES_FETCH_REQUEST);
            }*/
            if (carouselInfoData.title.equalsIgnoreCase(getResources().getString(R.string.favorites))) {
                mViewPager.setCurrentItem(0);
                MyWatchlistFavouritesFragment myWatchlistFavouritesFragment = new MyWatchlistFavouritesFragment();
                pushFragment(myWatchlistFavouritesFragment);
                myWatchlistFavouritesFragment.setRequestType(APIConstants.FAVOURITES_FETCH_REQUEST);
            }
            Log.d("Title", carouselInfoData.title);
            if (carouselInfoData.title.equalsIgnoreCase(getResources().getString(R.string.Profile))) {
                mViewPager.setCurrentItem(0);
//               Intent intent=new Intent(getApplicationContext(),ProfileActivity.class);
                startActivityForResult(ProfileActivity.createIntent(mContext), ProfileActivity.edit_profile_code);
//               startActivity(intent);

            }


            if (tabAdState != null) {
                if (Util.doesCurrentTabHasPortraitBanner(getCurrentTabForVmax())) {
                    tabAdState.put(getCurrentTabForVmax(), vmaxBannerAdFrameParent.getVisibility());
                }
            }
            mCurrentSelectedPagePosition = getPositionInCarousalWhenClickedBottomTab(mCurrentSelectedPagePositionTitle);
            updateNavigationBarAndToolbar();
            List<FilterItem> groupGenres = new ArrayList<>();
            List<FilterItem> groupLanguages = new ArrayList<>();
            if (mListCarouselInfo != null && mListCarouselInfo.size() > mCurrentSelectedPagePosition && mListCarouselInfo.get(mCurrentSelectedPagePosition) != null) {
                CleverTap.eventTabViewed(mListCarouselInfo.get(mCurrentSelectedPagePosition).title != null ? mListCarouselInfo.get(mCurrentSelectedPagePosition).title : "");
            }
            /*if (vmaxInterStitialAdView != null
                    && Util.checkUserLoginStatus()
                    && (!checkIsAdShownToday()
                    || isToShowAdForFirst)) {
                isToShowAdForFirst = false;
                //vmaxInterStitialAdView.showAd();
            }*/
            showAppBar();
            boolean isFilterAvailable = checkIsFilterSelected(carouselInfoData.cachedFilterResponse, groupLanguages, groupGenres);
            if (mMenu != null) {
                setFilterIcon(R.drawable.actionbar_filter_icon_default);
            }
            if (vmaxBannerAdFrameParent != null && !Util.doesCurrentTabHasPortraitBanner(getCurrentTabForVmax())) {
                vmaxBannerAdFrameParent.setVisibility(View.VISIBLE);
            } else {
                if (vmaxBannerAdFrameParent != null) {
                    if (tabAdState.containsKey(getCurrentTabForVmax())) {
                        vmaxBannerAdFrameParent.setVisibility(tabAdState.get(getCurrentTabForVmax()));
                    }
                }
            }
            ComScoreAnalytics.getInstance().setTabClickedEvent(carouselInfoData.title);
            sendPageChangeListener();
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };


    public CarouselInfoData carouselInfoData;

    private void showAppBar() {
        if (mAppBar == null) return;
        mAppBar.setExpanded(true);
    }

    private RelativeLayout mRLayoutGestureTipLayout;
    private LinearLayout mLinear_layout_edit;
    private List<CarouselInfoData> tabListData;
    private boolean isLoginCheckInProgress;
    private String launchMessage;
    private FilterFragment mFilterFragment;
    //private VmaxAdView vmaxBannerAdView;
    private FrameLayout vmaxBannerAdFrameParent;
    //private VmaxAdView vmaxInterStitialAdView;
    private boolean isToShowAdForFirst;
    private TextView mTextUserName, mSignout, mVersion,more,contactUs,chatWithUs,editProfile,submitProfile;
    private AppBarLayout mAppBar;
    private RelativeLayout mFrameLL;
    private eightbitlab.com.blurview.BlurView blurLayout;
    public eightbitlab.com.blurview.BlurView blurlayout_toolbar;
    //private eightbitlab.com.blurview.BlurView blurlayout_toolbar22;
  //  private AppBarLayout blurlayout_toolbar2;
    private boolean allowAppBarScroll = true;
    private AppUpdateManager appUpdateManager;

    public void setFilterIcon(int filterIcon) {
        MenuItem filterItem = mMenu.findItem(R.id.action_filter);
        filterItem.setIcon(ContextCompat.getDrawable(mContext, filterIcon));
    }

    private boolean isToReloadData = false;

    public void updateNavigationBarAndToolbar() {
        updateSelectedPageToolbar();
        if (TextUtils.isEmpty(mCurrentSelectedPagePositionTitle) && tabListData != null && tabListData.size() > 0 && !TextUtils.isEmpty(tabListData.get(0).title)) {
            mCurrentSelectedPagePositionTitle = (viewPager != null && viewPager.getCurrentItem() >= 0 && viewPager.getCurrentItem() < tabListData.size()) ? tabListData.get(viewPager.getCurrentItem()).title : tabListData.get(0).title;
        }
        int positionClicked = getPositionInSideNavigaitonWhenClickedBottomTab(mCurrentSelectedPagePositionTitle);
        DrawerListAdapter.selectedItem = positionClicked;
        if (mDrawerRecycleView != null && mDrawerRecycleView.getAdapter() != null) {
            mDrawerRecycleView.getAdapter().notifyDataSetChanged();
        }
    }

    private Bundle mArgumentsOfPlayer;
    private CardData mCardData;
    private boolean isChromeCastClickEventDone;
    private boolean isNavigation = false;
    private OtpReader mOtpReader;

    private void updateSelectedPageToolbar() {

        if (mMenu == null || mListCarouselInfo == null) {
            LoggerD.debugLog("updateSelectedPageToolbar: is mMenu " + (mMenu == null) + " mListCarouselInfo is " + (mListCarouselInfo == null));
            return;
        }

        //showToolbar();
        int positionClicked = getPositionInCarousalWhenClickedBottomTab(mCurrentSelectedPagePositionTitle);
        CarouselInfoData carouselInfoData = mListCarouselInfo.get(positionClicked);
        MenuItem filterItem = mMenu.findItem(R.id.action_filter);
        MenuItem searchItem = mMenu.findItem(R.id.action_search);
        if (carouselInfoData.enableShowAll) {
            if (mSearchView != null && mSearchView.isIconified()) {
                filterItem.setVisible(true);
            }
        } else {
            filterItem.setVisible(false);
        }

        if (TextUtils.isEmpty(carouselInfoData.showAll)) {
            searchItem.setVisible(false);
        } else {
            searchItem.setVisible(false);
        }
        checkAndEnableChromeCast();
        LoggerD.debugLog("updateSelectedPageToolbar: searchview hint- " + mContext.getString(R.string.msg_search_hint_generic_placeholder, carouselInfoData.title));
//        setSearchViewHint(mContext.getString(R.string.msg_search_hint_generic_placeholder, carouselInfoData.title));
        setSearchViewHint(carouselInfoData.showAll);
        CleverTap.eventCategoryViewed(carouselInfoData.title);

        if (APIConstants.LAYOUT_TYPE_EPG.equalsIgnoreCase(carouselInfoData.appAction)) {
            mSectionType = SECTION_LIVE;
        } else {
            mSectionType = SECTION_MOVIES;
        }

        if (tabListData.get(positionClicked).bgColor != null && !TextUtils.isEmpty(tabListData.get(positionClicked).bgColor)) {
            String bgColor = tabListData.get(positionClicked).bgColor;
            int backgroundColor = Color.parseColor(bgColor);
//            mTabPagerRootLayout.setBackground(ContextCompat.getDrawable(mContext, R.drawable.tab_layout_background_light_theme));
            //mTabPageIndicator.setBackground(ContextCompat.getDrawable(mContext,R.drawable.tab_layout_background_light_theme));
//            mToolbar.setBackgroundColor(backgroundColor);
            appLogo.setImageResource(R.drawable.toolbar_logo);
            //appLogo.setImageResource(R.drawable.actionbar_logo_light_theme);
            searchItem.setIcon(R.drawable.actionbar_search_icon_light_theme);
            mToolbar.setNavigationIcon(R.drawable.actionbar_menu_icon_light_theme);
            enableNavigation();
        } else {
//            mTabPagerRootLayout.setBackground(ContextCompat.getDrawable(mContext, R.drawable.tab_layout_background));
            //mTabPageIndicator.setBackground(ContextCompat.getDrawable(mContext,R.drawable.tab_layout_background));
            /*if(Util.doesCurrentTabHasPortraitBanner(getCurrentTabForVmax())) {
                mAppBar.setBackground(mContext.getResources().getDrawable(R.drawable.banner_top_gradient));
                mToolbar.setBackground(mContext.getResources().getDrawable(R.drawable.banner_top_gradient));
            }else {
                mAppBar.setBackgroundColor(mContext.getResources().getColor(R.color.toolbar_bg_colour));
                mToolbar.setBackgroundColor(mContext.getResources().getColor(R.color.toolbar_bg_colour));
            }*/
//            mToolbar.setBackgroundColor(mContext.getResources().getColor(R.color.toolbar_bg_colour));
            appLogo.setImageResource(R.drawable.sun_direct_header_logo);
            searchItem.setIcon(R.drawable.actionbar_search_icon);
            mToolbar.setNavigationIcon(R.drawable.actionbar_menu_icon);
            mToolbar.setNavigationOnClickListener(v -> {
                Log.d("Debug", "navigation clik");
                if(mDrawerLayout != null && !mDrawerLayout.isDrawerOpen(GravityCompat.START)){
                    mDrawerLayout.openDrawer(GravityCompat.START);
                    if(isMediaPlaying()){
                        if(mDraggablePanel != null && mDraggablePanel.isMinimized()) {
                            if(mFragmentCardDetailsPlayer != null && mFragmentCardDetailsPlayer.mPlayer != null && mFragmentCardDetailsPlayer.mPlayer.mVideoViewPlayer != null && mFragmentCardDetailsPlayer.mPlayer.mVideoViewPlayer.isPlaying()) {
                                mFragmentCardDetailsPlayer.mPlayer.playLayout.performClick();
                            }
                        } else {
                            mFragmentCardDetailsPlayer.mPlayer.onPause();
                        }
                    }
                }
            });
          /*  RequestOptions requestOptions = new RequestOptions();
            requestOptions = requestOptions.transforms(new CenterCrop(), new RoundedCorners(30));
            Glide.with(mContext)
                    .load("https://metaimg-sdirect.myplex.com/epgChannels/141072_b803f505-9396-4046-a268-cf1ef5541011.jpg")
                    .placeholder(R.drawable.movie_thumbnail_placeholder)
                  // .transform(new RoundedCorners((int)getContext().getResources().getDimension(R.dimen._10sdp)))
                    .apply(requestOptions)
                    .error(R.drawable.movie_thumbnail_placeholder)
                    .override(50,50)
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .into(new SimpleTarget<Drawable>() {
                        @Override
                        public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                            mToolbar.setNavigationIcon(resource);
                        }

                        @Override
                        public void onLoadFailed(@Nullable Drawable errorDrawable) {
                            mToolbar.setNavigationIcon(R.drawable.nav_drawer_profile_thumbnail);
                        }
                    });;*/
            enableNavigation();
        }

    }

    public static Intent createIntent(Context context, String page) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(APIConstants.NOTIFICATION_PARAM_PAGE, page);
        return intent;
    }

    @Override
    public void onStart() {
        try {
            super.onStart();
            //Log.d(TAG, "onResume() was called");
        } catch (Throwable t) {
            t.printStackTrace();
            //  Crashlytics.logException(t);
        }

    }

    @Override
    public void otpReceived(String messageText) {

    }

    @Override
    public void otpTimeOut() {
        stopOtpReader();
    }

    @Override
    public void otpReceived(String address, String message) {
        if (!TextUtils.isEmpty(address)
                && address.contains(APIConstants.SENDER_VFCARE)) {
            loadDataAndInitializeUI();
            stopOtpReader();
        }
    }

    public void showGestureTip() {
        if (mRLayoutGestureTipLayout == null) {
            return;
        }
//        if (mContext != null) {
//            ((BaseActivity) mContext).setOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//        }
        if (mContext != null) {
            ((BaseActivity) mContext).setOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        }
        mFragmentCardDetailsPlayer.disableDraggablePanel();
        mRLayoutGestureTipLayout.setVisibility(View.VISIBLE);
        mHandlerShowMediaController.postDelayed(mRunnableShowMediaController, DEFAULT_MEDIACTROLLER_TIMEOUT);
        mRLayoutGestureTipLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideGestureTips();
                return false;
            }
        });
    }

    public static void handelBranchDeeplink(String deepLink) {

    }

    public void hideGestureTips() {
        if (mFragmentCardDetailsPlayer != null) {
            mFragmentCardDetailsPlayer.resumePreviousOrientaionTimer();
            mFragmentCardDetailsPlayer.setShowingHelpScreen(false);
        }
        if (mRLayoutGestureTipLayout == null) {
            return;
        }
        mHandlerShowMediaController.removeCallbacks(mRunnableShowMediaController);
        mRLayoutGestureTipLayout.setVisibility(GONE);
    }

    public void setCarouselInfoData(CarouselInfoData carouselInfoData) {
        this.carouselInfoData = carouselInfoData;
    }

    public void showSearch(String query) {
        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction()
                .hide(selectedMenuFragment)
                .commit();
        showSearchFragment("", true);
        mSearchView.setIconified(false);
        //setSearchQuery(query);
        //mSearchView.performClick();
//            showOnlyMicButton();
        isVoiceButtonClicked = false;
        LoggerD.debugLog("search querey- " + query);
    }

    public void isFavoriteRequestFromPortraitBanner(boolean isFavoriteRequest, String _id, String type) {
        this.isFavoriteRequest = isFavoriteRequest;
        this._id = _id;
        this.type = type;
    }

    private boolean isFavoriteRequest = false;
    private String _id = "";
    private String type = "";


    /**
     * Enum created to represent the DraggablePanel and DraggableView different states.
     */
    public enum DraggableState implements Serializable {

        MINIMIZED, MAXIMIZED, CLOSED_AT_LEFT, CLOSED_AT_RIGHT

    }


    private static final String DRAGGABLE_PANEL_STATE = "draggable_panel_state";
    private static final int DELAY_MILLIS = 50;
    DisplayManager displayManager;
    boolean isScreenMirrorInProgress = false;


    private void closeFilterMenuPopup() {
        if (mFilterMenuPopupWindow != null) {
            mFilterMenuPopupWindow.dismissPopupWindow();
        }
    }

    public void updateFilterData(HashMap<Integer, ArrayList<String>> filterValuesMap) {
        if (filterValuesMap == null)
            return;
        ArrayList<String> languageList = new ArrayList<>();
        ArrayList<String> genreFilterList = new ArrayList<>();
        int genreKey = 0;
        int languageKey = 1;
        if (mSectionType == MainActivity.SECTION_MOVIES) {
            genreKey = 1;
            languageKey = 0;
        }
        if (filterValuesMap != null && filterValuesMap.containsKey(genreKey)) {
            genreFilterList = filterValuesMap.get(genreKey);
        }
        if (filterValuesMap != null && filterValuesMap.containsKey(languageKey)) {
            languageList = filterValuesMap.get(languageKey);
        }
        if (filterValuesMap == null) filterValuesMap = new HashMap<>();

        CarouselInfoData carouselInfoData = mListCarouselInfo.get(mCurrentSelectedPagePosition);
        carouselInfoData.filteredData = filterValuesMap;
            /*if(genreFilterList.size()== 0 && languageList.size() == 0){
                closeFilterMenuPopup();
                return;
            }*/
        if (genreFilterList.size() > 0 && genreFilterList.get(0).equals("All")) {
            genreFilterList = new ArrayList<>();
        }
        if (languageList.size() > 0 && languageList.get(0).equals("All")) {
            languageList = new ArrayList<>();
        }

        String genreValues = joinList(genreFilterList, ",");
        String langValues = joinList(languageList, ",");
        if (mMenu != null) {
//            setFilterIcon(R.drawable.actionbar_filter_icon_default);
            if ((genreFilterList != null && !genreFilterList.isEmpty()
                    || (languageList != null && !languageList.isEmpty()))) {
                //filterItem.setIcon(R.drawable.actionbar_filter_icon_highlighted_icon);
            }
        }
        if (TextUtils.isEmpty(langValues) && TextUtils.isEmpty(genreValues)) {
            removeFilterFragment();
            return;
        }

        if (TextUtils.isEmpty(langValues)
                && TextUtils.isEmpty(genreValues)) {
            removeFilterFragment();
            return;
        }

        EPG.genreFilterValues = genreValues;
        EPG.langFilterValues = langValues;

        String gaFilterNames = null;
        if (genreValues != null
                && !genreValues.equals("")) {
            gaFilterNames = genreValues;
        }

        if (langValues != null
                && !langValues.equals("")) {
            if (gaFilterNames != null) {
                gaFilterNames = gaFilterNames + "," + langValues;
            } else {
                gaFilterNames = langValues;
            }

            Analytics.mixpanelEventAppliedFilter(langValues, genreValues);
            CleverTap.eventFilterApplied(genreValues, langValues);
            Analytics.mixpanelSetPeopleProperty(Analytics.MIXPANEL_PEOPLE_SETTINGS_LANGUAGE_USED, true);
        }

        if (gaFilterNames != null) {
            Analytics.gaBrowseFilter(gaFilterNames, 1l);
        }
        ApplicationController.isDateChanged = true;
        ApplicationController.pageVisiblePos = 0;
        ApplicationController.pageItemPos = 0;
        EPG.globalPageIndex = 1;

        if (SECTION_MOVIES == mSectionType) {
            removeFragment(mCurrentFragment);
            showViewAllFragmentWithFilter(langValues, genreValues);
            return;
        }

        homePagerAdapterDynamicMenu.notifyDataSetChanged();
        closeFilterMenuPopup();
        if (mToolbar != null) {
            mToolbar.setVisibility(View.VISIBLE);
        }
        //mToolbar.showOverflowMenu();

    }

    private void showVODListFragment(Bundle args) {
        //TODO show VODListFragment from MainActivity with bundle
        removeFragment(mCurrentFragment);
        pushFragment(FragmentVODList.newInstance(args));
    }


    public void showViewAllFragmentWithFilter(String langValues, String genreValues) {
        CarouselInfoData carouselInfoData = mListCarouselInfo.get(mCurrentSelectedPagePosition);
        Bundle args = new Bundle();
        args.putString(FragmentCarouselViewAll.PARAM_LANGUAGE_FILTER_VALUE, langValues);
        args.putString(FragmentCarouselViewAll.PARAM_GENRE_FILTER_VALUE, genreValues);
        args.putString(FragmentCarouselViewAll.PARAM_FRAGMENT_TYPE, FragmentCarouselViewAll.PARAM_FRAGMENT_TYPE_FILTER);
        args.putString(FragmentCarouselViewAll.PARAM_FRAGMENT_MENU_GROUP_TYPE, carouselInfoData.shortDesc);
        CacheManager.setCarouselInfoData(carouselInfoData);

        if (APIConstants.LAYOUT_TYPE_BIG_BROWSE_LIST.equalsIgnoreCase(carouselInfoData.showAllLayoutType)
                || APIConstants.LAYOUT_TYPE_BIG_BROWSE_LIST_WITHOUT_FILTER.equalsIgnoreCase(carouselInfoData.showAllLayoutType)) {
            showCarouselViewAllFragment(args);
            return;
        }
        MenuDataModel.setSelectedMoviesCarouselList(carouselInfoData.name + "_" + 1, carouselInfoData.listCarouselData);

        if (!APIConstants.LAYOUT_TYPE_BIG_BROWSE_LIST.equalsIgnoreCase(carouselInfoData.showAllLayoutType)
                || !APIConstants.LAYOUT_TYPE_BIG_BROWSE_LIST_WITHOUT_FILTER.equalsIgnoreCase(carouselInfoData.showAllLayoutType)) {
            args.putBoolean(FragmentCarouselViewAll.PARAM_FRAGMENT_TYPE_CAROUSEL_GRID, true);
        }

        CacheManager.setCarouselInfoData(carouselInfoData);

        if (!TextUtils.isEmpty(carouselInfoData.shortDesc)) {
            args.putString(FragmentCarouselViewAll.PARAM_FRAGMENT_MENU_GROUP_TYPE, carouselInfoData.shortDesc);
        }
        showVODListFragment(args);

    }

    public void handleInAppUrl(Uri uri) {

        if (uri.getQueryParameterNames().contains("mode") && !uri.getQueryParameter("mode").isEmpty() && uri.getQueryParameter("mode").equalsIgnoreCase("subscribe")) {
            if (!PrefUtils.getInstance().getPrefLoginStatus().equalsIgnoreCase("success")) {
          //      FirebaseAnalytics.getInstance().eventRegistrationSource(null,APIConstants.SOURCE_HOME,APIConstants.VALUE_SOURCE_IN_APP);
                launchLoginActivity(APIConstants.SOURCE_HOME, APIConstants.VALUE_SOURCE_IN_APP);
                return;
            }
            Intent ip=new Intent(mContext,SubscriptionWebActivity.class);
            ip.putExtra(SubscriptionWebActivity.IS_FROM_PREMIUM,true);
         //   FirebaseAnalytics.getInstance().eventSubscriptionSource(null,APIConstants.SOURCE_HOME,APIConstants.VALUE_SOURCE_IN_APP);
            startActivity(ip);
            return;
        }

        List<String> list = new ArrayList<>();
        if (uri.getPathSegments() != null) {
            list = uri.getPathSegments();
        }
        if (list != null && list.size() != 1){
            callContentFragment(list.get(2));
        }else if (list != null && list.size() == 1){
            redirectToPage(list.get(0));
        }

    }

    private void showCarouselViewAllFragment(Bundle args) {
        pushFragment(FragmentCarouselViewAll.newInstance(args));
    }

    private String joinList(ArrayList list, String literal) {
        return TextUtils.join(literal, list);
        //return list.toString().replaceAll(",", literal).replaceAll("[\\[.\\].\\s+]", "");
    }

    private boolean onHandleExternalIntent(final Intent intent) {
        if (intent == null || getIntent().getExtras() == null) {
            //Log.d(TAG, "intent is null");
            return false;
        }
        for (String key : getIntent().getExtras().keySet()) {
            LoggerD.debugLog("CleverTap: MainActivity: key- " + key + " value- " + getIntent().getExtras().get(key));
        }
        LoggerD.debugLog("CleverTap: MainActivity: getExtras- " + getIntent().getExtras());
        if (intent.hasExtra(APIConstants.NOTIFICATION_LAUNCH_URL)) {
            String url = intent.getStringExtra(APIConstants.NOTIFICATION_LAUNCH_URL);
            //Log.d(TAG, "NOTIFICATION_PARAM_URL, url:- " + url);
            SDKUtils.launchBrowserIntent(this, url);
            boolean intentHandled = true;
            return  intentHandled;
        }
        if (intent.hasExtra(APIConstants.NOTIFICATION_PARAM_NOTIFICATION_ID)) {
            try {
                String snotificationId = intent.getStringExtra(APIConstants
                        .NOTIFICATION_PARAM_NOTIFICATION_ID);
                //Log.d(TAG, "snotificationId " + snotificationId);
                if (snotificationId != null) {
                    long notificationId = Long.parseLong(snotificationId);
                    NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    if (manager != null)
                        manager.cancel((int) notificationId);
                }

            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }


        boolean intentHandled = false;

        String notificationTitle = null;
        if (intent.hasExtra(APIConstants.NOTIFICATION_PARAM_TITLE)) {
            //launch card details
            //Log.d(TAG, "notification title " + intent.getStringExtra(APIConstants.NOTIFICATION_PARAM_TITLE));
            notificationTitle = intent.getStringExtra(APIConstants.NOTIFICATION_PARAM_TITLE);
            Analytics.gaNotificationEvent(Analytics.EVENT_ACTION_OPENS_AUTO_REMINDER, intent.getStringExtra(APIConstants.NOTIFICATION_PARAM_TITLE));
        } else if (intent.hasExtra(APIConstants.NOTIFICATION_PARAM_MESSAGE)) {
            //Log.d(TAG, "notification message " + intent.getStringExtra(APIConstants.NOTIFICATION_PARAM_MESSAGE));
            notificationTitle = intent.getStringExtra(APIConstants.NOTIFICATION_PARAM_MESSAGE);
            Analytics.mixpanelNotificationOpened(intent.getStringExtra(APIConstants.NOTIFICATION_PARAM_MESSAGE), intent.getStringExtra(APIConstants.NOTIFICATION_PARAM_NID));
            Analytics.gaNotificationEvent(Analytics.EVENT_ACTION_OPENS, intent.getStringExtra(APIConstants.NOTIFICATION_PARAM_MESSAGE));
            AppsFlyerTracker.eventNotificationOpened(new HashMap<String, Object>());
        }
        //Log.d(TAG, "notification notificationTitle- " + notificationTitle);

        /*if (intent.hasExtra(APIConstants.NOTIFICATION_PARAM_PARTNER_ID) && intent.hasExtra(APIConstants.NOTIFICATION_PARAM_PARTNER_NAME)) {
            handleIfPartnerContent(intent, null);
            intentHandled = true;
        }*/
        if (intent.getExtras() != null && intent.getExtras().containsKey(APIConstants.NOTIFICATION_PARAM_CAROUSEL_NAME) && intent.getExtras().getString(APIConstants.NOTIFICATION_PARAM_CAROUSEL_NAME) != null) {
            showFragmentsFromNotification(
                    intent.getExtras().getString(APIConstants.NOTIFICATION_PARAM_LAYOUT),
                    Integer.parseInt(intent.getExtras().getString(APIConstants.NOTIFICATION_PARAM_PAGE_COUNT)),
                    intent.getExtras().getString(APIConstants.NOTIFICATION_PARAM_CAROUSEL_NAME),
                    intent.getExtras().getString(APIConstants.NOTIFICATION_PARAM_CAROUSEL_TITLE)
            );
        }
        if (intent.hasExtra(APIConstants.NOTIFICATION_PARAM_CONTENT_ID)) {
            final String _id = intent.getStringExtra(APIConstants.NOTIFICATION_PARAM_CONTENT_ID);
            final String _aid = intent.getExtras().getString(APIConstants.NOTIFICATION_PARAM_AID);
            final String videoUrl = intent.getExtras().getString(APIConstants.NOTIFICATION_PARAM_VIDEO_URL);
            final String yuid = intent.getExtras().getString(APIConstants.NOTIFICATION_PARAM_YUID);
            CacheManager cacheManager = new CacheManager();
            //Log.d(TAG, "notification _id " + _id);
          /*  if (intent.hasExtra(APIConstants.NOTIFICATION_PARAM_CONTENT_TYPE)) {
                String contentType = intent.getStringExtra(APIConstants.NOTIFICATION_PARAM_CONTENT_TYPE);
                //Log.d(TAG, "notification contentType " + contentType);
                if (contentType.equalsIgnoreCase(APIConstants.TYPE_PROGRAM)) {
                    cacheManager.setNotifiationTitle(intent.getStringExtra(APIConstants.NOTIFICATION_PARAM_TITLE));
                    cacheManager.setNotifiationNid(intent.getStringExtra(APIConstants.NOTIFICATION_PARAM_NID));
                    final String finalNotificationTitle = notificationTitle;
                    cacheManager.getProgramDetail(_id, true, new CacheManager.CacheManagerCallback() {
                        @Override
                        public void OnCacheResults(List<CardData> dataList) {
                            //Log.d(TAG, "OnCacheResults() ");
                            if (dataList == null
                                    || dataList.isEmpty()) {
                                return;
                            }
                            final CardData programData = dataList.get(0);
                            if (null == programData) {
                                return;
                            }
                            if (null == programData.globalServiceId) {
                                return;
                            }

                            CacheManager.setSelectedCardData(programData);
                            final Bundle args = new Bundle();
                            args.putString(CardDetails.PARAM_CARD_ID, programData.globalServiceId);
                            args.putString(CardDetails.PARAM_CARD_DATA_TYPE, APIConstants.TYPE_PROGRAM);

                            if (intent.hasExtra(APIConstants.NOTIFICATION_PARAM_NID)) {
                                args.putString(APIConstants.NOTIFICATION_PARAM_NID, intent.getStringExtra(APIConstants.NOTIFICATION_PARAM_NID));
                            }
                            if (null != programData.startDate
                                    && null != programData.endDate) {
                                Date startDate = Util.getDate(programData.startDate);
                                Date endDate = Util.getDate(programData.endDate);
                                Date currentDate = new Date();
                                if ((currentDate.after(startDate)
                                        && currentDate.before(endDate))
                                        || currentDate.after(endDate)) {
                                    args.putBoolean(CardDetails
                                            .PARAM_AUTO_PLAY, true);
                                }
                            }

                            args.putString(APIConstants.NOTIFICATION_PARAM_TITLE, finalNotificationTitle);
                            if (!TextUtils.isEmpty(finalNotificationTitle)) {
                                //launch card details
                                Analytics.gaNotificationEvent(Analytics.EVENT_ACTION_OPENS_AUTO_REMINDER, finalNotificationTitle);
                            }

                            if (intent.hasExtra(APIConstants.NOTIFICATION_PARAM_ACTION)) {
                                //launch card details
                                String action = intent.getStringExtra(APIConstants.NOTIFICATION_PARAM_ACTION);
                                //Log.d(TAG, "action:- " + action);
                                if (action != null
                                        && action.equals(APIConstants.NOTIFICATION_PARAM_AUTOPLAY)) {
                                    args.putBoolean(CardDetails
                                            .PARAM_AUTO_PLAY, true);
                                }
                            }
                            args.putString(Analytics.PROPERTY_SOURCE, Analytics.VALUE_SOURCE_NOTIFICATION);
                            args.putString(Analytics.PROPERTY_SOURCE_DETAILS, "reminder");
                            if (intent.hasExtra(CleverTap.SOURCE_PROMO_VIDEO_AD)) {
                                args.putString(Analytics.PROPERTY_SOURCE, CleverTap.SOURCE_PROMO_VIDEO_AD);
                                PromoAdData promoAdData = PropertiesHandler.getPromoAdData(mContext);
                                args.putString(Analytics.PROPERTY_SOURCE_DETAILS, promoAdData == null ? APIConstants.NOT_AVAILABLE : promoAdData.id);
                            }
                            if (mDraggablePanel != null) {
                                mDraggablePanel.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        showDetailsFragment(args, programData);
                                    }
                                });
                            }

                        }

                        @Override
                        public void OnOnlineResults(List<CardData> dataList) {
                            //Log.d(TAG, "OnOnlineResults() ");
                            if (dataList == null
                                    || dataList.isEmpty()) {
                                //Log.d(TAG, "OnOnlineResults dataList- " + dataList);
                                return;
                            }
                            final CardData programData = dataList.get(0);
                            if (programData == null
                                    || programData.globalServiceId == null) {
                                //Log.d(TAG, "OnOnlineResults dataList- " + dataList);
                                return;
                            }

                            CacheManager.setSelectedCardData(programData);
                            final Bundle args = new Bundle();
                            args.putString(CardDetails.PARAM_CARD_ID, programData.globalServiceId);
                            args.putString(CardDetails.PARAM_CARD_DATA_TYPE, APIConstants.TYPE_PROGRAM);
                            if (intent.hasExtra(APIConstants.NOTIFICATION_PARAM_NID)) {
                                args.putString(APIConstants.NOTIFICATION_PARAM_NID, intent.getStringExtra(APIConstants.NOTIFICATION_PARAM_NID));
                            }
                            if (null != programData.startDate
                                    && null != programData.endDate) {
                                Date startDate = Util.getDate(programData.startDate);
                                Date endDate = Util.getDate(programData.endDate);
                                Date currentDate = new Date();
                                if ((currentDate.after(startDate)
                                        && currentDate.before(endDate))
                                        || currentDate.after(endDate)) {
                                    args.putBoolean(CardDetails
                                            .PARAM_AUTO_PLAY, true);
                                }
                            }
                            args.putString(APIConstants.NOTIFICATION_PARAM_TITLE, finalNotificationTitle);
                            if (!TextUtils.isEmpty(finalNotificationTitle)) {
                                //launch card details
                                Analytics.gaNotificationEvent(Analytics.EVENT_ACTION_OPENS_AUTO_REMINDER, finalNotificationTitle);
                            }
                            if (intent.hasExtra(APIConstants.NOTIFICATION_PARAM_ACTION)) {
                                //launch card details
                                String action = intent.getStringExtra(APIConstants.NOTIFICATION_PARAM_ACTION);
                                //Log.d(TAG, "action:- " + action);
                                if (action != null
                                        && action.equals(APIConstants.NOTIFICATION_PARAM_AUTOPLAY)) {
                                    args.putBoolean(CardDetails
                                            .PARAM_AUTO_PLAY, true);
                                }

                            }
                            args.putString(Analytics.PROPERTY_SOURCE, Analytics.VALUE_SOURCE_NOTIFICATION);
                            args.putString(Analytics.PROPERTY_SOURCE_DETAILS, "reminder");
                            if (mDraggablePanel != null) {
                                mDraggablePanel.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        showDetailsFragment(args, programData);
                                    }
                                });
                            }

                        }

                        @Override
                        public void OnOnlineError(Throwable error, int errorCode) {
                            //Log.d(TAG, "onOnlineError " + error);
                            if (error != null) {
                                String errorMessage = error.getMessage();
                                Log.e(TAG, "showErrorMessage: errorMessage: " + errorMessage);
                                if (errorMessage != null && errorMessage.contains(APIConstants.MESSAGE_ERROR_CONN_RESET) && !isRetryAlreadyDone) {
                                    //Retry for data connection
                                    Log.e(TAG, "showErrorMessage: retrying again for reconnection");
                                    isRetryAlreadyDone = true;
                                    onHandleExternalIntent(intent);
                                }

                            }

                        }
                    });
                    intentHandled = true;
                }
            } else*/
                cacheManager.setNotifiationTitle(intent.getStringExtra(APIConstants.NOTIFICATION_PARAM_MESSAGE));
                cacheManager.setNotifiationNid(intent.getStringExtra(APIConstants.NOTIFICATION_PARAM_NID));
                final String finalNotificationTitle1 = notificationTitle;
                cacheManager.getCardDetails(_id, true, new CacheManager.CacheManagerCallback() {
                    @Override
                    public void OnCacheResults(List<CardData> dataList) {
                        //Log.d(TAG, "OnCacheResults ");
                        if (dataList == null
                                || dataList.isEmpty()) {
                            return;
                        }
                        final CardData cardData = dataList.get(0);
                        if (null == cardData) {
                            return;
                        }
                        /*if (handleIfPartnerContent(intent, cardData)) {
                            return;
                        }*/
                        if (!TextUtils.isEmpty(_aid)) {
                            //Log.d(TAG, "_aid- " + _aid);
                            cardData._aid = _aid;
                        }
                        if (!TextUtils.isEmpty(videoUrl)) {
                            //Log.d(TAG, "videoUrl- " + videoUrl);
                            setVideoUrlCardData(cardData, videoUrl, APIConstants.TYPE_NEWS);
                        }
                        if (!TextUtils.isEmpty(yuid)) {
                            //Log.d(TAG, "yuid- " + yuid);
                            setVideoUrlCardData(cardData, yuid, APIConstants.TYPE_YOUTUBE);
                        }

                        CacheManager.setSelectedCardData(cardData);
//                        TODO show RelatedVOdListFragment
                        final Bundle args = new Bundle();
                        if (intent.hasExtra(APIConstants.NOTIFICATION_PARAM_NID)) {
                            args.putString(APIConstants.NOTIFICATION_PARAM_NID, intent.getStringExtra(APIConstants.NOTIFICATION_PARAM_NID));
                        }
                        if (null != cardData.startDate
                                && null != cardData.endDate) {
                            Date startDate = Util.getDate(cardData.startDate);
                            Date endDate = Util.getDate(cardData.endDate);
                            Date currentDate = new Date();
                            if ((currentDate.after(startDate)
                                    && currentDate.before(endDate))
                                    || currentDate.after(endDate)) {
                                args.putBoolean(CardDetails
                                        .PARAM_AUTO_PLAY, true);
                            }
                        }

                        if(intent.hasExtra("from_alaram") && intent.getBooleanExtra("from_alaram" , false)) {
                            String contentType = intent.getStringExtra(APIConstants.NOTIFICATION_PARAM_CONTENT_TYPE);
                            String time = intent.getStringExtra(APIConstants.NOTIFICATION_PARAM_TIME);
                            cardData.startDate = time;
                          //  Util.updateAalarmTimes(cardData, true);
                            if(time != null) {
                                Util.cancelReminder(mContext, finalNotificationTitle1, _id,
                                        Util.getDate(time), mContext.getString(R.string.notification_livetv_message), contentType);
                                cardData.startDate = time;
                                Util.updateAalarmTimes(cardData, true);
                            }

                        }
                        if (!TextUtils.isEmpty(finalNotificationTitle1)) {
                            //launch cardData details
                            args.putString(APIConstants.NOTIFICATION_PARAM_TITLE, finalNotificationTitle1);
                        }
                        if (intent.hasExtra(APIConstants.NOTIFICATION_PARAM_ACTION)) {
                            //launch cardData details
                            String action = intent.getStringExtra(APIConstants.NOTIFICATION_PARAM_ACTION);
                            //Log.d(TAG, "action:- " + action);
                            if (action != null
                                    && action.equals(APIConstants.NOTIFICATION_PARAM_AUTOPLAY)) {
                                args.putBoolean(CardDetails
                                        .PARAM_AUTO_PLAY, true);
                            }
                        }
                        if (cardData != null
                                && cardData.generalInfo != null) {
                            String contentId = cardData._id;
                            if (APIConstants.TYPE_PROGRAM.equalsIgnoreCase(cardData.generalInfo.type)
                                    && cardData.globalServiceId != null) {
                                contentId = cardData.globalServiceId;
                            }
                            args.putString(CardDetails.PARAM_CARD_ID, contentId);
                            if (APIConstants.TYPE_VODCHANNEL.equalsIgnoreCase(cardData.generalInfo.type)
                                    || APIConstants.TYPE_VODCATEGORY.equalsIgnoreCase(cardData.generalInfo.type)
                                    || APIConstants.TYPE_VODYOUTUBECHANNEL.equalsIgnoreCase(cardData.generalInfo.type)
                                    || APIConstants.TYPE_TVSEASON.equalsIgnoreCase(cardData.generalInfo.type)) {
                                //Launching ActivityRelatedVODList for vodcategory,vodchannel content type's
                                args.putSerializable(FragmentRelatedVODList.PARAM_SELECTED_VOD_DATA, cardData);
                                pushFragment(FragmentRelatedVODList.newInstance(args));
                                return;
                            }

                            if (intent.hasExtra(APIConstants.MESSAGE_TYPE)) {
                                if (APIConstants.NOTIFICATION_PARAM_MESSAGE_TYPE_INAPP.equalsIgnoreCase(intent.getStringExtra(APIConstants.MESSAGE_TYPE))) {
                                    args.putBoolean(CardDetails
                                            .PARAM_AUTO_PLAY, true);
                                }
                            }

                            args.putInt(CardDetails.PARAM_PARTNER_TYPE, Util.getPartnerTypeContent(cardData));
                            String partnerId = cardData == null || cardData.generalInfo == null || cardData.generalInfo.partnerId == null ? null : cardData.generalInfo.partnerId;
                            args.putString(CardDetails.PARAM_PARTNER_ID, partnerId);
                            String adProvider = null;
                            boolean adEnabled = false;
                            if (cardData != null
                                    && cardData.content != null) {
                                if (!TextUtils.isEmpty(cardData.content.adProvider)) {
                                    adProvider = cardData.content.adProvider;
                                }
                                adEnabled = cardData.content.adEnabled;
                            }
//                            args.putString(CardDetails.PARAM_AD_PROVIDER, adProvider);
//                            args.putBoolean(CardDetails.PARAM_AD_ENBLED, adEnabled);
                            args.putString(APIConstants.AFFILIATE_VALUE, intent.getStringExtra(APIConstants.AFFILIATE_VALUE));
                            args.putString(Analytics.PROPERTY_SOURCE, Analytics.VALUE_SOURCE_NOTIFICATION);
                            args.putString(Analytics.PROPERTY_SOURCE_DETAILS, intent.getStringExtra(APIConstants.NOTIFICATION_PARAM_NID));
                            if (mDraggablePanel != null) {
                                mDraggablePanel.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        if(cardData != null && cardData.isLive()) {
                                            getEPGData(cardData._id);
                                        } else {
                                            showDetailsFragment(args, cardData);
                                        }
                                    }
                                });
                            }
                        }
                    }

                    @Override
                    public void OnOnlineResults(List<CardData> dataList) {
                        //Log.d(TAG, "OnOnlineResults ");
                        if (dataList == null
                                || dataList.isEmpty()) {
                            return;
                        }
                        final CardData cardData = dataList.get(0);
                        if (null == cardData) {
                            return;
                        }
                        /*if (handleIfPartnerContent(intent, cardData)) {
                            return;
                        }*/
                        /*if (!TextUtils.isEmpty(_aid)) {
                            //Log.d(TAG, "_aid- " + _aid);
                            cardData._aid = _aid;
                        }*/
                        if (!TextUtils.isEmpty(videoUrl)) {
                            //Log.d(TAG, "videoUrl- " + videoUrl);
                            setVideoUrlCardData(cardData, videoUrl, APIConstants.TYPE_NEWS);
                        }
                        if (!TextUtils.isEmpty(yuid)) {
                            //Log.d(TAG, "yuid- " + yuid);
                            setVideoUrlCardData(cardData, yuid, APIConstants.TYPE_YOUTUBE);
                        }

                        CacheManager.setSelectedCardData(cardData);
//                        TODO show RelatedVOdListFragment
                        final Bundle args = new Bundle();
                        if (intent.hasExtra(APIConstants.NOTIFICATION_PARAM_NID)) {
                            args.putString(APIConstants.NOTIFICATION_PARAM_NID, intent.getStringExtra(APIConstants.NOTIFICATION_PARAM_NID));
                        }
                        if (null != cardData.startDate
                                && null != cardData.endDate) {
                            Date startDate = Util.getDate(cardData.startDate);
                            Date endDate = Util.getDate(cardData.endDate);
                            Date currentDate = new Date();
                            if ((currentDate.after(startDate)
                                    && currentDate.before(endDate))
                                    || currentDate.after(endDate)) {
                                args.putBoolean(CardDetails
                                        .PARAM_AUTO_PLAY, true);
                            }
                        }

                        if (intent.hasExtra(APIConstants.MESSAGE_TYPE)) {
                            if (APIConstants.NOTIFICATION_PARAM_MESSAGE_TYPE_INAPP.equalsIgnoreCase(intent.getStringExtra(APIConstants.MESSAGE_TYPE))) {
                                args.putBoolean(CardDetails
                                        .PARAM_AUTO_PLAY, true);
                            }
                        }

                        if (!TextUtils.isEmpty(finalNotificationTitle1)) {
                            //launch cardData details
                            args.putString(APIConstants.NOTIFICATION_PARAM_TITLE, finalNotificationTitle1);
                        }
                        if (intent.hasExtra(APIConstants.NOTIFICATION_PARAM_ACTION)) {
                            //launch cardData details
                            String action = intent.getStringExtra(APIConstants.NOTIFICATION_PARAM_ACTION);
                            //Log.d(TAG, "action:- " + action);
                            if (action != null
                                    && action.equals(CardDetails.PARAM_AUTO_PLAY)) {
                                args.putBoolean(CardDetails
                                        .PARAM_AUTO_PLAY, true);
                            }
                        }

                        if(intent.hasExtra("from_alaram") && intent.getBooleanExtra("from_alaram" , false)) {
                            String contentType = intent.getStringExtra(APIConstants.NOTIFICATION_PARAM_CONTENT_TYPE);
                            String time = intent.getStringExtra(APIConstants.NOTIFICATION_PARAM_TIME);
                            if(time != null) {
                                Util.cancelReminder(mContext, finalNotificationTitle1, _id,
                                        Util.getDate(time), mContext.getString(R.string.notification_livetv_message), contentType);
                                cardData.startDate = time;
                                Util.updateAalarmTimes(cardData, true);
                            }
                        }
                        if (cardData != null
                                && cardData.generalInfo != null) {
                            String contentId = cardData._id;
                            if (APIConstants.TYPE_PROGRAM.equalsIgnoreCase(cardData.generalInfo.type)
                                    && cardData.globalServiceId != null) {
                                contentId = cardData.globalServiceId;
                            }
                            args.putString(CardDetails.PARAM_CARD_ID, contentId);
                            if (APIConstants.TYPE_VODCHANNEL.equalsIgnoreCase(cardData.generalInfo.type)
                                    || APIConstants.TYPE_VODCATEGORY.equalsIgnoreCase(cardData.generalInfo.type)
                                    || APIConstants.TYPE_VODYOUTUBECHANNEL.equalsIgnoreCase(cardData.generalInfo.type)
                                    || APIConstants.TYPE_TVSEASON.equalsIgnoreCase(cardData.generalInfo.type)) {
                                //Launching ActivityRelatedVODList for vodcategory,vodchannel content type's
                                args.putSerializable(FragmentRelatedVODList.PARAM_SELECTED_VOD_DATA, cardData);
                                pushFragment(FragmentRelatedVODList.newInstance(args));
                                return;
                            }
                            args.putInt(CardDetails.PARAM_PARTNER_TYPE, Util.getPartnerTypeContent(cardData));
                            String partnerId = cardData == null || cardData.generalInfo == null || cardData.generalInfo.partnerId == null ? null : cardData.generalInfo.partnerId;
                            args.putString(CardDetails.PARAM_PARTNER_ID, partnerId);
                            String adProvider = null;
                            boolean adEnabled = false;
                            if (cardData != null
                                    && cardData.content != null) {
                                if (!TextUtils.isEmpty(cardData.content.adProvider)) {
                                    adProvider = cardData.content.adProvider;
                                }
                                adEnabled = cardData.content.adEnabled;
                            }
                            args.putString(CardDetails.PARAM_AD_PROVIDER, adProvider);
                            args.putBoolean(CardDetails.PARAM_AD_ENBLED, adEnabled);

                            args.putString(APIConstants.AFFILIATE_VALUE, intent.getStringExtra(APIConstants.AFFILIATE_VALUE));
                            args.putString(CleverTap.PROPERTY_TAB, getCurrentTabName());
                            args.putString(Analytics.PROPERTY_SOURCE, Analytics.VALUE_SOURCE_NOTIFICATION);
                            args.putString(Analytics.PROPERTY_SOURCE_DETAILS, intent.getStringExtra(APIConstants.NOTIFICATION_PARAM_NID));

                            if (mDraggablePanel != null) {
                                mDraggablePanel.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        if(cardData!=null && cardData.isLive()) {
                                            getEPGData(cardData._id);
                                        } else
                                            showDetailsFragment(args, cardData);
                                    }
                                });
                            }
                        }
                    }

                    @Override
                    public void OnOnlineError(Throwable error, int errorCode) {
                        //Log.d(TAG, "onOnlineError " + error);
                        if (error != null) {
                            String errorMessage = error.getMessage();
                            if (errorMessage != null && errorMessage.contains(APIConstants.MESSAGE_ERROR_CONN_RESET) && !isRetryAlreadyDone) {
                                //Retry for data connection
                                isRetryAlreadyDone = true;
                                onHandleExternalIntent(intent);
                            }

                        }

                    }
                });
                intentHandled = true;


            return intentHandled;
        } else if (intent.hasExtra(APIConstants.NOTIFICATION_PARAM_CHANNEL)) {
            System.out.println("phani channel ");
            Intent programIntent = new Intent(this, ProgramGuideChannelActivity.class);
            String id = intent.getStringExtra(APIConstants.NOTIFICATION_PARAM_CONTENT_ID);
            programIntent.putExtra(APIConstants.NOTIFICATION_PARAM_CONTENT_ID, id);
            programIntent.putExtra(ProgramGuideChannelActivity.DATE_POS, PrefUtils.getInstance().getPrefEnablePastEpg() ? PrefUtils.getInstance().getPrefNoOfPastEpgDays() : ApplicationController.DATE_POSITION);
            programIntent.putExtra(ProgramGuideChannelActivity.PARAM_FROM, false);
            //Log.d(TAG, "channelId, _id:- " + id + "datePos- " + ApplicationController.DATE_POSITION);
            startActivity(programIntent);
            intentHandled = true;

        }
        if (intent.hasExtra(APIConstants.NOTIFICATION_PARAM_VURL)) {
            String vurl = intent.getExtras().getString(APIConstants.NOTIFICATION_PARAM_VURL);
            Intent videoLaunchIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse(vurl));
            videoLaunchIntent.setDataAndType(Uri.parse(vurl), "video/mp4");
            final PackageManager pm = mContext.getPackageManager();
            int i = 0;
            for (ResolveInfo ri : pm.queryIntentActivities(videoLaunchIntent,
                    PackageManager.MATCH_DEFAULT_ONLY)) {
                if ((i == 0 || i == 1) && ri.activityInfo.enabled) {
                    videoLaunchIntent.setClassName(ri.activityInfo.packageName,
                            ri.activityInfo.name);
                }
                i++;
            }
            //Log.d(TAG, "vurl- " + vurl);
            startActivity(videoLaunchIntent);
            intentHandled = true;
            return intentHandled;
        }
        if (intent.hasExtra(APIConstants.NOTIFICATION_PARAM_YUID)) {
            String yuid = intent.getExtras().getString(APIConstants.NOTIFICATION_PARAM_YUID);
            //Log.d(TAG, "yuid- " + yuid);
            Util.launchYouyubePlayer((Activity) mContext, yuid);
            intentHandled = true;
        }
        if (intent.hasExtra(APIConstants.NOTIFICATION_PARAM_DOWNLOAD)) {
            if (intent.getBooleanExtra(APIConstants.NOTIFICATION_PARAM_DOWNLOAD, false)) {
                MyDownloadsFragment myDownloadsFragment = new MyDownloadsFragment();
                pushFragment(myDownloadsFragment);
                Bundle args = new Bundle();
                args.getBoolean(MyDownloadsFragment.PARAM_SHOW_TOOLBAR, true);
                if (PrefUtils.getInstance().getVernacularLanguage()) {
                    args.putString(APIConstants.LanguageTitle, getNavigationDownloadItemTitle());
                }
                myDownloadsFragment.setArguments(args);
                intentHandled = true;
            }
        }
        if (intent.hasExtra(APIConstants.NOTIFICATION_PARAM_PAGE)) {
            String page = intent.getExtras().getString(APIConstants.NOTIFICATION_PARAM_PAGE);

            //Log.d(TAG, "page- " + page);
            intentHandled = true;
            if (mTabPageIndicator == null || homePagerAdapterDynamicMenu == null) {
                return intentHandled;
            }
            redirectToPage(page);
        }
        if (intent.hasExtra(APIConstants.NOTIFICATION_PARAM_URL)) {
            String url = intent.getStringExtra(APIConstants.NOTIFICATION_PARAM_URL);
            //Log.d(TAG, "NOTIFICATION_PARAM_URL, url:- " + url);
            SDKUtils.launchBrowserIntent(this, url);
            intentHandled = true;

        }
        if (intent.hasExtra(APIConstants.NOTIFICATION_PARAM_ADD_TO_WATCHLIST)) {
            String contentId = intent.getStringExtra(APIConstants.NOTIFICATION_PARAM_ADD_TO_WATCHLIST);
            String contentType = intent.getStringExtra(APIConstants.NOTIFICATION_PARAM_ADD_TO_WATCHLIST_CONTENT_TYPE);
            //Log.d(TAG, "NOTIFICATION_PARAM_ADD_TO_WATCHLIST, url:- " + contentId + " contentTpe- " + contentType);
            postCheckFavoriteContent(contentId, contentType);
            intentHandled = true;

        }
        return intentHandled;
    }

    public void redirectToPage(String page) {
        //            TODO Needs updation
        try {
            int index = pickPageIndex(page);
            setDefaultPageSelected(index);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int pickPageIndex(String title) {
        if (TextUtils.isEmpty(title) || tabListData == null) {
            return 0;
        }
        for (int i = 0; i < tabListData.size(); i++) {
            CarouselInfoData carouselInfoData = tabListData.get(i);
            if (carouselInfoData.title.equalsIgnoreCase(title)) {
                return i;
            }
        }
        return 0;
    }

    private boolean handleIfPartnerContent(final Intent intent, final CardData movieData) {

        String publishingHouse = (movieData != null
                && movieData.publishingHouse != null
                && !TextUtils.isEmpty(movieData.publishingHouse.publishingHouseName) ? movieData.publishingHouse.publishingHouseName : intent != null ? intent.getStringExtra(APIConstants.NOTIFICATION_PARAM_PARTNER_NAME) : null);

        final String partnerId = movieData != null
                && movieData.generalInfo != null
                && !TextUtils.isEmpty(movieData.generalInfo.partnerId) ? movieData.generalInfo.partnerId : intent != null && intent.hasExtra(APIConstants.NOTIFICATION_PARAM_PARTNER_ID) ? intent.getStringExtra(APIConstants.NOTIFICATION_PARAM_PARTNER_ID) : null;

        if (!TextUtils.isEmpty(publishingHouse) && publishingHouse.toLowerCase().startsWith(APIConstants.TYPE_HUNGAMA) && !TextUtils.isEmpty(partnerId)) {
            HungamaPartnerHandler.launchDetailsPage(movieData, mContext, null, intent.getStringExtra(APIConstants.NOTIFICATION_PARAM_NID));
            return true;
        }
        return false;
    }

    private void setVideoUrlCardData(CardData cardData, String videoUrl, String type) {
        CardDataVideos videos = new CardDataVideos();
        CardDataVideosItem videosItem = new CardDataVideosItem();
        if (null != type) {
            if (type.equalsIgnoreCase(APIConstants.TYPE_YOUTUBE)) {
                videosItem.type = APIConstants.TYPE_YOUTUBE;
                if (null != cardData.generalInfo) {
                    cardData.generalInfo.videoAvailable = true;
                }
            } else {
                videosItem.type = APIConstants.TYPE_NEWS;
            }
        }
        videosItem.link = videoUrl;
        videos.values = new ArrayList<>();
        videos.values.add(videosItem);
        cardData.videos = videos;

    }

    public void fireOfflineMOUs() {
        MOUUpdateRequestStorageList sMOUNotFiredList = ApplicationController.getMOUNotFiredData();
        if (sMOUNotFiredList != null && sMOUNotFiredList.mDownloadedList != null
                && sMOUNotFiredList.mDownloadedList.size() > 0) {
            if (Util.isNetworkAvailable(mContext)) {
                //for (String key : sMOUNotFiredList.mDownloadedList.keySet()) {
                // ...
                // makeMouUpdateRequestForOffline(sMOUNotFiredList.mDownloadedList.get(0));
                // }

            }
        }
    }

    public static void setWindowFlag(Activity activity, final int bits, boolean state) {
        Window win = activity.getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();

        if (state) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
    }

    public static void logMainData() {
        // Print status values in hex
        System.out.println("Nagravision certificateStatusBitField : 0x"
                + Integer.toHexString(certificateStatusBitField));
        System.out.println("Nagravision dateStatusBitField : 0x"
                + Integer.toHexString(dateStatusBitField));
        System.out.println("Nagravision contextStatusBitField : 0x"
                + Integer.toHexString(contextStatusBitField));
        System.out.println("Nagravision manifestStatusBitField : 0x"
                + Integer.toHexString(manifestStatusBitField));
        System.out.println("Nagravision dexSignatureStatusBitField : 0x"
                + Integer.toHexString(dexSignatureStatusBitField));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isOpen = true;
        showSystemUI();
        logMainData();
        canShowSearchWithFilter = true;
        LoggerD.debugLog("CleverTap: MainActivity: onCreate");
        setContentView(R.layout.activity_main);
        checkboxAutoPlayToggle = (CheckBox) findViewById(R.id.checkbox_auto_play_toggle);
        //Firebase In-app Event Trigger
        // CONDITION: Don't show in app message when navigating through deeplink
       /* if(!ApplicationController.navigateToDeeplink){*/
//            Log.d("Leaderboard", " MAinActicity :call for inapp ");
            com.google.firebase.analytics.FirebaseAnalytics.getInstance(mContext).logEvent("sundirectgo_qa_testing",null);
            //        FirebaseInAppMessaging.getInstance().triggerEvent("main_activity_ready");
        //}
        isLaunch = true;


        PrefUtils.getInstance().setAppLanguageToShow("");
        mProgressBar = (ProgressBar) findViewById(R.id.card_loading_progres_bar);
        profile_iv = (CircleImageView) findViewById(R.id.profile_iv);
        mTitle = (TextView) findViewById(R.id.title);
        homeLinearLayout=(CardView)findViewById(R.id.home_linear_layout);
        homeLinearLayoutcard=(CardView)findViewById(R.id.home_linear_layout_card);
        if(homeLinearLayout!=null) {
            ViewGroup.MarginLayoutParams layoutParamss = (ViewGroup.MarginLayoutParams) homeLinearLayout.getLayoutParams();
            layoutParamss.setMargins(0, 0, 0, 0);
            homeLinearLayout.requestLayout();
            if (homeLinearLayout != null)
                homeLinearLayout.setRadius(0);
        }
        homeLinearLayoutcard.setVisibility(GONE);
        showProgressBar(true);
        mContext = this;
        replaceAdvertisingID();
        profile_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enabledisablePlayerLogsClickCount++;
                if(enabledisablePlayerLogsClickCount % 6 == 0){

                    if (!ApplicationController.SHOW_PLAYER_LOGS && FLAVOR.equals("sundirectprod")) {
                        ApplicationController.SHOW_PLAYER_LOGS = true;
//                        PrefUtils.getInstance().setPrefIsExoplayerEnabled(true);
//                        PrefUtils.getInstance().setPrefIsExoplayerDvrEnabled(true);
                        if (ApplicationController.ENABLE_SAVE_LOGS_TO_FILE) {
                            SDKUtils.captureLogsToSDCard(MainActivity.this);
                        }
                        AlertDialogUtil.showToastNotification("Player logs and ad tags are enabled");
                    }else{
                        if (ApplicationController.ENABLE_SAVE_LOGS_TO_FILE) {
                            SDKUtils.deleteLogFile(MainActivity.this);
                        }
                        ApplicationController.SHOW_PLAYER_LOGS = false;
//                        PrefUtils.getInstance().setPrefIsExoplayerEnabled(false);
//                        PrefUtils.getInstance().setPrefIsExoplayerDvrEnabled(false);
                        AlertDialogUtil.showToastNotification("Player logs and ad tags are disabled");
                    }
                    PrefUtils.getInstance().setPlayerLogs(ApplicationController.SHOW_PLAYER_LOGS);
                }
            }
        });


        if (TextUtils.isEmpty(PrefUtils.getInstance().getForceAcceptLanguage())) {
            ApplicationController.IS_VERNACULAR_TO_BE_SHOWN = PrefUtils.getInstance().getVernacularLanguage();
        } else {
            ApplicationController.IS_VERNACULAR_TO_BE_SHOWN = true;
        }
        //Check whether clientkey is available or not if not launch login activity
        final String clientKey = PrefUtils.getInstance().getPrefClientkey();
        if (clientKey == null) {
            Util.launchActivity(this, new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        //check whether permission location

       /* if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //TODO: show Dialog
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                super.onBackPressed();
            }
            SDKLogger.debug("Mandatory permissions Not Accepted");
        }*/
        helpLayout = (RelativeLayout) findViewById(R.id.help_layout);
        mDraggablePanel = (DraggablePanel) findViewById(R.id.draggable_panel);
        mRLayoutGestureTipLayout = (RelativeLayout) findViewById(R.id.player_gesture_overlay_container);
        mLinear_layout_edit = (LinearLayout) findViewById(R.id.linear_layout_edit);
        collapsingToolbarLayout = findViewById(R.id.collapsingtoolbarlayout);
        mRLayoutTimeShiftHelp = (RelativeLayout) findViewById(R.id.layout_timeshift_help_screen);
        mTextViewErrorRetryAgain = (TextView) findViewById(R.id.textview_error_retry);
        mLayoutRetry = (RelativeLayout) findViewById(R.id.retry_layout);
        mImageViewRetry = (ImageView) findViewById(R.id.imageview_error_retry);
        mSignout = (TextView) findViewById(R.id.sign_out);
        more =(TextView) findViewById(R.id.more);
        contactUs =(TextView)findViewById(R.id.contact_us);
        chatWithUs =(TextView)findViewById(R.id.chat_with_us);
        editProfile=(TextView)findViewById(R.id.editProfile);
        mVersion = (TextView) findViewById(R.id.version_tv);
        mLayoutRetry.setVisibility(GONE);
        mImageViewRetry.setOnClickListener(mRetryClickListener);
        mTextViewErrorRetryAgain.setOnClickListener(mRetryClickListener);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
       // mToolbar.setBackgroundResource(R.drawable.gradient_toolbar_bar);

        editProfileIcon=findViewById(R.id.edit_profile_icon);
        editProfileIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent editProfileText = new Intent(MainActivity.this,EditProfileActivity.class);
                String url=PrefUtils.getInstance().getString("PROFILE_IMAGE_URL");
                editProfileText.putExtra("profile_url",url);
                startActivityForResult(editProfileText, EditProfileActivity.PERMISSION_REQUEST_CODE);
            }
        });
        if (mToolbar != null) {
            mToolbar.setVisibility(GONE);
        }
//        mToolbarDraggablePanel = (Toolbar) findViewById(R.id.toolbar_draggablepanel);
        mToolbar.setContentInsetsAbsolute(0, 0);
        //mToolbar.setBackgroundColor(getResources().getColor(R.color.toolbar_bg_colour));
        appLogo = (ImageView) findViewById(R.id.app_logo);
        vmaxBannerAdFrameParent = (FrameLayout) findViewById(R.id.vmax_banner_ad_frame);
        vmaxBannerAdFrameParent.setVisibility(GONE);
        //Taking the layoutParams of VMAX_FOOTER_AD
        if(vmaxBannerAdFrameParent!=null) {
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) vmaxBannerAdFrameParent.getLayoutParams();
            vmaxLayoutParams = layoutParams;
            ORIGINAL_VMAX_PARENT_BOTTOM_PARAM = layoutParams.bottomMargin;
        }

        mTabPageIndicator = (TabPageIndicator) findViewById(R.id.tabs);
        menuTabs = (TabLayout) findViewById(R.id.menuTabs);
        mViewPager = (CustomViewPager) findViewById(R.id.viewpager);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        mDrawerRecycleView = (RecyclerView) findViewById(R.id.recycleviewDrawerList);
        myAccountLayout = findViewById(R.id.my_account_nav_item);
        navMenuMainHeadingText = findViewById(R.id.title_text_nav_drawer);
        mSMCNoDisplay = findViewById(R.id.sub_title_text_nav_drawer);
        checkbox_auto_pause_toggle = findViewById(R.id.checkbox_auto_pause_toggle);
        ImageView closeDrawer = findViewById(R.id.close_drawer_image);
        mobile_number = findViewById(R.id.mobile_number);

        float percentage = 0.7f;
        if(DeviceUtils.getScreenOrientation(this) == ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE ){
            percentage = 0.4f;
        }

        int width = (int)(percentage * getResources().getDisplayMetrics().widthPixels);

        LinearLayout ll_drawer = findViewById(R.id.ll_drawer);
        if(ll_drawer!=null) {
            ll_drawer.getLayoutParams().width = width;
        }

        closeDrawer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeDrawer();
            }
        });
        Typeface myCustomFont = Typeface.createFromAsset(getAssets(), "fonts/amazon_ember_cd_bold.ttf");
        mTitle.setTypeface(myCustomFont);
        myAccountLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                closeDrawer();
                if (Util.checkUserLoginStatus()) {
                    //  startActivityForResult(ProfileActivity.createIntent(mContext), ProfileActivity.edit_profile_code);
                } else {
                    launchLoginActivity("MY ACCOUNT", "MY ACCOUNT");
                }
            }
        });
        mVersion.setText("Version "+VERSION_NAME+"\nEnvironment "+FLAVOR);
        if( PrefUtils.getInstance().getBoolean(PrefUtils.PREF_APPS_AS_HOME, false)){
            checkbox_auto_pause_toggle.setChecked(true);
        } else {
            checkbox_auto_pause_toggle.setChecked(false);
        }
        checkbox_auto_pause_toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                    PrefUtils.getInstance().setBoolean(PrefUtils.PREF_APPS_AS_HOME, isChecked);
                else
                    PrefUtils.getInstance().setBoolean(PrefUtils.PREF_APPS_AS_HOME, isChecked);
            }
        });

        if (PrefUtils.getInstance().getWhiteMode()) {
           checkboxAutoPlayToggle.setChecked(true);
        } else {
            checkboxAutoPlayToggle.setChecked(false);
        }
        checkboxAutoPlayToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

               /* if (PrefUtils.getInstance().getWhiteMode()) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    PrefUtils.getInstance().setWhiteMode(false);
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    PrefUtils.getInstance().setWhiteMode(true);
                }
                Intent intent = getIntent();
                finish();
                startActivity(intent);*/
              //  recreate();
            }
        });
        mSignout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeDrawer();
                makeSignOutRequest();
            }
        });
        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent editProfileText = new Intent(MainActivity.this,EditProfileActivity.class);
                startActivityForResult(editProfileText, EditProfileActivity.PERMISSION_REQUEST_CODE);
            }
        });
        more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent settings = new Intent(MainActivity.this,SettingsActivity.class);
                settings.putExtra(SettingsActivity.SECTION_TYPE, SettingsActivity.APPLICATION_MORE);
                startActivity(settings);
            }
        });
        contactUs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent mIntent = new Intent(mContext, LiveScoreWebView.class);
                mIntent.putExtra(LiveScoreWebView.PARAM_TOOLBAR_TITLE, getString(R.string.contact_us));

                if (TextUtils.isEmpty(PrefUtils.getInstance().getContactUsPageURL())) {
                    return;
                }
                mIntent.putExtra("url", PrefUtils.getInstance().getContactUsPageURL());
                startActivity(mIntent);
            }

        });
        chatWithUs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent mIntent = new Intent(mContext, LiveScoreWebView.class);
                mIntent.putExtra(LiveScoreWebView.PARAM_TOOLBAR_TITLE, getString(R.string.chat));

                if (TextUtils.isEmpty(PrefUtils.getInstance().getContactUsPageURL())) {
                    return;
                }
                mIntent.putExtra("url", PrefUtils.getInstance().getContactUsPageURL());
                startActivity(mIntent);
            }
        });
        mVersion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              /*  Dialog dialog = new Dialog(mContext);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.version_dailog);

                dialog.getWindow().setBackgroundDrawable(
                        new ColorDrawable(Color.BLACK));
              //  dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
                WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                Window window = dialog.getWindow();
                lp.copyFrom(window.getAttributes());
                lp.width = WindowManager.LayoutParams.MATCH_PARENT;;
                lp.height = WindowManager.LayoutParams.MATCH_PARENT;;
                window.setAttributes(lp);
                lp.gravity= Gravity.FILL;
                ImageView closeIcon= (ImageView) dialog
                        .findViewById(R.id.close_icon_alert);

                closeIcon.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        dialog.cancel();

                    }
                });

                dialog.show();*/
            }
        });
        menuTabs.addOnTabSelectedListener(bottomMenuTabListener);
        //  menuTabs.setupWithViewPager(mViewPager);
        updateNavMenuMyAccountSection();
        mTextUserName = (TextView) findViewById(R.id.text_user_name);

        mAppBar = findViewById(R.id.app_bar);
        //blurlayout_toolbar22 = (BlurView) findViewById(R.id.blurlayout_toolbar22);

        mFrameLL = findViewById(R.id.frame_ll);
        int statusBarHeight = Util.getStatusBarHeight(mContext);
        if(mAppBar!=null) {
            CoordinatorLayout.LayoutParams layoutParams1 = (CoordinatorLayout.LayoutParams) mAppBar.getLayoutParams();
            layoutParams1.topMargin = statusBarHeight;
            mAppBar.setLayoutParams(layoutParams1);
        }
        initScrollBehavior();
        continueStartUp();
        fireOfflineMOUs();
        checkInAppRatingPopUp();
        updateExoDownloads();
        try {
            CastContext.getSharedInstance(this);
        } catch (Exception e) {
            e.printStackTrace();
            checkPlayServices();

        }
        displayManager = ((DisplayManager) this.getSystemService(Context.DISPLAY_SERVICE));
        Display[] displays = displayManager.getDisplays();
        if (displays != null) {
            Log.e("ScreenMirror", "" + displays.length);
            if (displays.length > 1) {
                isScreenMirrorInProgress = true;
                Toast.makeText(mContext, "Screen Mirroring in Progress", Toast.LENGTH_SHORT).show();
            } else {
                isScreenMirrorInProgress = false;
            }
        }
        Object mDisplayListener = new DisplayManager.DisplayListener() {
            @Override
            public void onDisplayAdded(int displayId) {
                Log.e("ScreenMirror", "onDisplayAdded" + displayId);
                Display[] displays = displayManager.getDisplays();
                if (displays != null) {
                    Log.e("ScreenMirror", "" + displays.length);
                    if (displays.length > 1) {
                        isScreenMirrorInProgress = true;
                        Toast.makeText(mContext, "Screen Mirroring in Progress", Toast.LENGTH_SHORT).show();
                    } else {
                        isScreenMirrorInProgress = false;
                    }
                }
            }

            @Override
            public void onDisplayRemoved(int displayId) {
                Log.e("ScreenMirror", "onDisplayRemoved" + displayId);
                Display[] displays = displayManager.getDisplays();
                if (displays != null) {
                    Log.e("ScreenMirror", "" + displays.length);
                    if (displays.length > 1) {
                        isScreenMirrorInProgress = true;
                        Toast.makeText(mContext, "Screen Mirroring in Progress", Toast.LENGTH_SHORT).show();
                    } else {
                        isScreenMirrorInProgress = false;
                    }
                }
            }

            @Override
            public void onDisplayChanged(int displayId) {
                Log.e("ScreenMirror", "onDisplayChanged" + displayId);
            }
        };
        displayManager.registerDisplayListener((DisplayManager.DisplayListener) mDisplayListener, null);
        //TODO IN-APP Update Code added, not yet tested.
        //initAndroidTracker();
        //makeUserProfileRequest();

        blurLayout = findViewById(R.id.blurLayout);

        blurlayout_toolbar = findViewById(R.id.blurlayout_toolbar);


        View decorView = getWindow().getDecorView();
        //ViewGroup you want to start blur from. Choose root as close to BlurView in hierarchy as possible.
        ViewGroup rootView = (ViewGroup) decorView.findViewById(android.R.id.content);
        //Set drawable to draw in the beginning of each blurred frame (Optional).
        //Can be used in case your layout has a lot of transparent space and your content
        //gets kinda lost after after blur is applied.
        Drawable windowBackground = decorView.getBackground();

        blurLayout.setupWith(rootView)
                .setFrameClearDrawable(windowBackground)
                .setBlurAlgorithm(new RenderScriptBlur(this))
                .setBlurRadius(radius)
                .setBlurAutoUpdate(true)
                .setHasFixedTransformationMatrix(false);

        blurlayout_toolbar.setupWith(rootView)
                .setFrameClearDrawable(windowBackground)
                .setBlurAlgorithm(new RenderScriptBlur(this))
                .setBlurRadius(radius)
                .setBlurAutoUpdate(true)
                .setHasFixedTransformationMatrix(false);

        /*blurlayout_toolbar22.setupWith(rootView)
                .setFrameClearDrawable(windowBackground)
                .setBlurAlgorithm(new RenderScriptBlur(this))
                .setBlurRadius(radius)
                .setBlurAutoUpdate(true)
                .setHasFixedTransformationMatrix(false);*/


    }

    private void getHomePromotionPopUpBasedOnPackage() {
        AdPopUpNotificationListResponse adPopUpNotificationListResponse = PropertiesHandler.getAdPopupNotification(mContext);
        if(adPopUpNotificationListResponse != null) {
            if (adPopUpNotificationListResponse.getAdFullScreenConfig() != null && adPopUpNotificationListResponse.getAdFullScreenConfig().size() > 0) {
                for (int i = 0; i < adPopUpNotificationListResponse.getAdFullScreenConfig().size(); i++) {
                    if (adPopUpNotificationListResponse.getAdFullScreenConfig().get(0).getEnableAdPopUp() != null && adPopUpNotificationListResponse.getAdFullScreenConfig().get(i).getEnableAdPopUp().equalsIgnoreCase("true")) {
                        List<String> subscribed_languages = PrefUtils.getInstance().getSubscribedLanguage();
                        String packLanguage = "";
                        if (subscribed_languages != null && subscribed_languages.size() > 0 && subscribed_languages.get(0) != null) {
                            packLanguage = subscribed_languages.get(0);
                        } else {
                            packLanguage = "Tamil";
                        }
                        if (packLanguage != null && !TextUtils.isEmpty(packLanguage) && adPopUpNotificationListResponse.getAdFullScreenConfig().get(i).getLanguage()!=null) {
                            if (adPopUpNotificationListResponse.getAdFullScreenConfig().get(i).getLanguage().equalsIgnoreCase(packLanguage)) {
                                if (!PrefUtils.getInstance().isPopup()) {
                                    PrefUtils.getInstance().setPopupAdId(adPopUpNotificationListResponse.getAdFullScreenConfig().get(i).getId());
                                    showHomePopUpPromotion(adPopUpNotificationListResponse.getAdFullScreenConfig().get(i).getImageURL());
                                } else if (PrefUtils.getInstance().getPopupId() != null && adPopUpNotificationListResponse.getAdFullScreenConfig().get(i).getId() != null && !PrefUtils.getInstance().getPopupId().equalsIgnoreCase(adPopUpNotificationListResponse.getAdFullScreenConfig().get(i).getId())) {
                                    PrefUtils.getInstance().setPopupAdId(adPopUpNotificationListResponse.getAdFullScreenConfig().get(i).getId());
                                    showHomePopUpPromotion(adPopUpNotificationListResponse.getAdFullScreenConfig().get(i).getImageURL());
                                }
                            }
                        }

                    }
                }
            }
        }

        mNetworkReceiver = new ConnectivityReceiver();
        registerNetworkBroadcast();

    }


    String url = "";
    Dialog promoDialog = null;
    CardView cardView;
    private void showHomePopUpPromotion(String imageUrl) {
//        PrefUtils.getInstance().setPopup(true);
        isShowHomePopUp=false;
        if(promoDialog == null){
            url = imageUrl;
            promoDialog = new Dialog(mContext);
            promoDialog.setCanceledOnTouchOutside(false);
            promoDialog.setContentView(R.layout.fragment_adpopup_dialog);
            promoDialog.getWindow().setBackgroundDrawable(
                    new ColorDrawable(getResources().getColor(R.color.transparent)));
//            lp = new WindowManager.LayoutParams();
//            window = promoDialog.getWindow();
//            lp.copyFrom(window.getAttributes());
//            lp.y = (int) mContext.getResources().getDimension(R.dimen.home_page_popup_height); // Here is the param to set your dialog position. Same with params.x
//            promoDialog.getWindow().setAttributes(lp);
            int displayWidth;
            int displayHeight;
            if(!DeviceUtils.isTablet(this)){
                displayWidth = ((MainActivity) mContext).getWindowManager().getDefaultDisplay().getWidth();
                displayHeight = ((MainActivity) mContext).getWindowManager().getDefaultDisplay().getHeight() - displayWidth /2;
                displayWidth = displayWidth - 150;
            }else {
                displayWidth = (int)((MainActivity) mContext).getResources().getDimensionPixelSize(R.dimen.promo_width_portrait);
                displayHeight = (int)((MainActivity) mContext).getResources().getDimensionPixelSize(R.dimen.promo_height_portrait);

                if(DeviceUtils.getScreenOrientation(this) == ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE ){
                    displayWidth = (int)((MainActivity) mContext).getResources().getDimensionPixelSize(R.dimen.promo_width_land);
                    displayHeight = (int)((MainActivity) mContext).getResources().getDimensionPixelSize(R.dimen.promo_height_land);
                }
                if(displayHeight >= ((MainActivity) mContext).getWindowManager().getDefaultDisplay().getHeight()){
                    displayHeight = (int)(((MainActivity) mContext).getWindowManager().getDefaultDisplay().getHeight() - (((MainActivity) mContext).getWindowManager().getDefaultDisplay().getHeight() * 0.1));
                    displayWidth = (int)(displayHeight * ((float)9)/16);
                }
//                lp.width = displayWidth;
//                lp.height = displayHeight;
            }

            cardView = promoDialog.findViewById(R.id.cd_ad_popup);
            if(cardView!=null) {
                ViewGroup.LayoutParams params = cardView.getLayoutParams();
                params.width = displayWidth;
                params.height = displayHeight;
                cardView.setLayoutParams(params);
            }
//            window.setAttributes(lp);
//            lp.gravity= Gravity.FILL;

            ImageView imageView = (ImageView) promoDialog.findViewById(R.id.image_view);
            //imageView.setBackground(mContext.getResources().getDrawable(R.drawable.homepage_promo_banner));
            TextView skip_text= (TextView) promoDialog.findViewById(R.id.skip_text);
            skip_text.setVisibility(GONE);
            Picasso.get().load(imageUrl)
                    .into(imageView);
      /*  RelativeLayout.LayoutParams params=new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        params.setMargins(50,50,50,50);
        params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        imageView.setLayoutParams(params);*/
            promoDialog.show();
            promoDialog.setCancelable(false);
            ImageView closeIcon= (ImageView) promoDialog.findViewById(R.id.close_icon_alert);
//        SuperEllipseCardView dummy = dialog.findViewById(R.id.sec);
        /*RelativeLayout.LayoutParams closeIconParams=new RelativeLayout.LayoutParams(100, 100);
        closeIconParams.addRule(RelativeLayout.ABOVE, imageView.getId());
        closeIcon.setLayoutParams(closeIconParams);*/

            closeIcon.setVisibility(VISIBLE);
            closeIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    PrefUtils.getInstance().setPopup(true);
                    if(promoDialog!=null) {
                        promoDialog.dismiss();
                    }
                    promoDialog = null;
                    url = "";
                }
            });

            promoDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialogInterface) {
                    promoDialog = null;
                    url = "";
                }
            });
        }else{
//            lp.copyFrom(window.getAttributes());
//            lp.y = (int) mContext.getResources().getDimension(R.dimen.home_page_popup_height); // Here is the param to set your dialog position. Same with params.x
//            promoDialog.getWindow().setAttributes(lp);

            int displayWidth;
            int displayHeight;
            if(!DeviceUtils.isTablet(this)){
                displayWidth = ((MainActivity) mContext).getWindowManager().getDefaultDisplay().getWidth();
                displayHeight = ((MainActivity) mContext).getWindowManager().getDefaultDisplay().getHeight() - displayWidth /2;
//                lp.width = displayWidth - 150;
//                lp.height = displayHeight;
            }else {
                displayWidth = (int)((MainActivity) mContext).getResources().getDimensionPixelSize(R.dimen.promo_width_portrait);
                displayHeight = (int)((MainActivity) mContext).getResources().getDimensionPixelSize(R.dimen.promo_height_portrait);

                if(DeviceUtils.getScreenOrientation(this) == ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE ){
                    displayWidth = (int)((MainActivity) mContext).getResources().getDimensionPixelSize(R.dimen.promo_width_land);
                    displayHeight = (int)((MainActivity) mContext).getResources().getDimensionPixelSize(R.dimen.promo_height_land);
                }
                if(displayHeight >= ((MainActivity) mContext).getWindowManager().getDefaultDisplay().getHeight()){
                    displayHeight = (int)(((MainActivity) mContext).getWindowManager().getDefaultDisplay().getHeight() - (((MainActivity) mContext).getWindowManager().getDefaultDisplay().getHeight() * 0.1));
                    displayWidth = (int)(displayHeight * ((float)9)/16);
                }
//                lp.width = displayWidth;
//                lp.height = displayHeight;
            }
            if(cardView!=null) {
                ViewGroup.LayoutParams params = cardView.getLayoutParams();
                params.width = displayWidth;
                params.height = displayHeight;
                cardView.setLayoutParams(params);
            }
        }



//        SuperEllipseCardView dummy = new SuperEllipseCardView(this);

//        dummy.setShapeBorderWidth(2.f);
//        dummy.setShapeRadius(10.f);
//        dummy.setShapeCurveFactor(10.f);
//        dummy.setShapeScale(10.f);
//        dummy.setShapeForegroundColor(10);
//        dummy.setShapeBackgroundColor(10);

//        assert dummy.shapeBorderWidth == 2.f;
//        assert dummy.shapeRadius == 10.f;
//        assert dummy.shapeCurveFactor == 10.f;
//        assert dummy.shapeScale == 10.f;
//        assert dummy.shapeForegroundColor == 10;
//        assert dummy.shapeBackgroundColor == 10;

    }

    /*private void initAndroidTracker() {
        HashMap<String, Object> payload = new HashMap<>();
        HashMap<String, Object> data = new HashMap<>();
        data.put("engagementTime", 15724);
        data.put("scrollDepth", 100);
        HashMap<String, String> page = new HashMap<>();
        page.put("site", "sundirect.com");
        payload.put("data", data);
        payload.put("page", page);

//        Lens.Builder().defaultEventBuilder().eventName("Loaded Home screen").eventType("generic").eventData(payload)
//                .context(this).track();
    }*/

    public void setToolBarCollapsible(boolean b, boolean updateUi) {
        if(collapsingToolbarLayout!=null) {
            AppBarLayout.LayoutParams params = (AppBarLayout.LayoutParams)
                    collapsingToolbarLayout.getLayoutParams();


            if (b) {
//            OttLog.debug("toolbar", "1212 " + itemPosition);
                params.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL | AppBarLayout.LayoutParams.SCROLL_FLAG_SNAP);
                //not to expand toolbar everytime
                mAppBar.setExpanded(!updateUi, false);
            } else {
//            OttLog.debug("toolbar", "1313");
                if (viewPager != null && viewPager.getCurrentItem() == 0) {
                    params.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL | AppBarLayout.LayoutParams.SCROLL_FLAG_SNAP);
                    mAppBar.setExpanded(true, true);

                } else {
                    params.setScrollFlags(0);
                    mAppBar.setExpanded(true, false);
                }

            }
            collapsingToolbarLayout.setLayoutParams(params);
        }
    }
    public void replaceAdvertisingID() {

        AsyncTask<Void, Void, String> task = new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                AdvertisingIdClient.Info idInfo = null;
                try {
                    idInfo = AdvertisingIdClient.getAdvertisingIdInfo(mContext);
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String advertId = null;
                try {
                    advertId = idInfo.getId();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }

                return advertId;
            }

            @Override
            protected void onPostExecute(String advertId) {
                //TODO: when advertID is empty
                if (advertId != null && !advertId.isEmpty()) {
                    ApplicationController.setAdvertiserID(advertId);

                }
            }

        };
        task.execute();

        // }
    }

    private void updateNavMenuMyAccountSection() {
        if (Util.checkUserLoginStatus()) {
            navMenuMainHeadingText.setVisibility(VISIBLE);
            if(PrefUtils.getInstance().getPrefFullName() != null)
                navMenuMainHeadingText.setText(PrefUtils.getInstance().getPrefFullName());
            else
                navMenuMainHeadingText.setText("Sun Direct User");
            mSMCNoDisplay.setVisibility(VISIBLE);
            //  navMenuSubHeadingText.setVisibility(VISIBLE);
            mobile_number.setVisibility(VISIBLE);

            String mobileNumberText = PrefUtils.getInstance().getPrefMobileNumber();
            if (mobileNumberText != null) {
                String mobileNumberString = mobileNumberText.substring(0, 5) + " " + mobileNumberText.substring(5,10);
                mobile_number.setText(mobileNumberString);
                // mobile_number.setText(PrefUtils.getInstance().getPrefMobileNumber());
                mLinear_layout_edit.setVisibility(VISIBLE);
                mLinear_layout_edit.setVisibility(VISIBLE);
                mSignout.setVisibility(VISIBLE);
            }
            Typeface myCustomFont = Typeface.createFromAsset(getAssets(), "fonts/amazon_ember_cd_bold.ttf");
            mSignout.setTypeface(myCustomFont);
            if(PrefUtils.getInstance().getPrefSmartCardNumber() != null) {
                mSMCNoDisplay.setText( PrefUtils.getInstance().getPrefSmartCardNumber());
            }
            String url = PrefUtils.getInstance().getString("PROFILE_IMAGE_URL");
            if(TextUtils.isEmpty(url)) {
//                url=PrefUtils.getInstance().getDefaultProfileImage();
                profile_iv.setImageResource(R.drawable.nav_drawer_profile_thumbnail);
            }
            Glide.with(getApplicationContext())
                    .asBitmap()
                    .load(url)
                    .placeholder(R.drawable.nav_drawer_profile_thumbnail)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .skipMemoryCache(false)
                    .error(R.drawable.nav_drawer_profile_thumbnail)
                    .dontAnimate()
                    .into(new SimpleTarget<Bitmap>() {

                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            profile_iv.setImageBitmap(resource);
                        }
                    });
            /*String mSmartCardNumber = PrefUtils.getInstance().getPrefSmartCardNumber();
            if (mSmartCardNumber == null) {
                String smartCardNumberText = mSmartCardNumber.substring(0, 4) + " " + mSmartCardNumber.substring(4,8)+" "+mSmartCardNumber.substring(8,12);
                mSMCNoDisplay.setText(smartCardNumberText);
            }*/
            // userID.setVisibility(GONE);
        } else {
            navMenuMainHeadingText.setText("Login");
            mSMCNoDisplay.setText("Or Create Account");
            mLinear_layout_edit.setVisibility(GONE);
            mSignout.setVisibility(GONE);
            mobile_number.setVisibility(GONE);

        }
    }

    private void checkPlayStoreUpdate(final boolean isMandatory) {
        appUpdateManager = AppUpdateManagerFactory.create(this);
        Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();

        appUpdateInfoTask.addOnSuccessListener(new OnSuccessListener<AppUpdateInfo>() {
            @Override
            public void onSuccess(AppUpdateInfo appUpdateInfo) {
                if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                    if (isMandatory) {
                        try {
                            appUpdateManager.startUpdateFlowForResult(
                                    appUpdateInfo,
                                    FLEXIBLE,
                                    MainActivity.this,
                                    APP_UPDATE_REQUEST_CODE);
                        } catch (IntentSender.SendIntentException e) {
                            e.printStackTrace();
                        }
                    } else {
                        try {
                            appUpdateManager.startUpdateFlowForResult(
                                    appUpdateInfo,
                                    FLEXIBLE,
                                    MainActivity.this,
                                    APP_UPDATE_REQUEST_CODE);
                        } catch (IntentSender.SendIntentException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });

    }

    public void checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(mContext);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                if (!((Activity) mContext).isFinishing()) {
//                    apiAvailability.getErrorDialog((Activity) mContext, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
//                            .show();
                }
            } else {
                //Log.i(TAG, "This device is not supported.");
                ((Activity) mContext).finish();
            }

        }

    }


    private void continueStartUp() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (isFinishing()) return;

                if (PrefUtils.getInstance().getPrefEventLoggerEnabled()) {
                    MyplexEvent myplexEvent = MyplexEvent.getInstance(mContext);
                    String userid = String.valueOf(PrefUtils.getInstance().getPrefUserId());
                    myplexEvent.identify(userid);
                    myplexEvent.setHostURL(PrefUtils.getInstance().getEventLoggerUrl());
                    SDKLogger.debug("eventLoggerURL-  " + PrefUtils.getInstance().getEventLoggerUrl());
                }
                ApplicationController.SHOW_PLAYER_LOGS = PrefUtils.getInstance().getPlayerLogs();
                LoggerD.debugLog("userid - " + PrefUtils.getInstance().getPrefUserId());
                LoggerD.debugLog("getPrefMsisdnNo = " + PrefUtils.getInstance().getPrefMsisdnNo());
                ApplicationController.ENABLE_HELP_SCREEN = PrefUtils.getInstance().getPrefEnableHelpScreen();
                ApplicationController.ENABLE_MIXPANEL_API = false;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (isFinishing()) return;
                        initBundle();
                        if (!TextUtils.isEmpty(launchMessage))
                            AlertDialogUtil.showToastNotification(launchMessage);
                        loadDataAndInitializeUI();
                        //initHungamaSDK();
                        initVMAXAds();
//                        showPromoVideoAdIfAvailable();
                    }
                });
            }
        }).start();

    }
    private void showHomePopUpPromotion() {
        PrefUtils.getInstance().setPopup(true);
        Dialog dialog = new Dialog(mContext);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(R.layout.fragment_subscription_dialog);
        dialog.getWindow().setBackgroundDrawable(
                new ColorDrawable(getResources().getColor(R.color.black)));
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
       // ((Activity) mContext).getWindow().setStatusBarColor(getResources().getColor(R.color.black_30));
        setStatusBarGradiant(MainActivity.this, false);
        Window window = dialog.getWindow();
        lp.copyFrom(window.getAttributes());
        // lp.y = -30; // Here is the param to set your dialog position. Same with params.x
        dialog.getWindow().setAttributes(lp);
        int displayWidth = ((MainActivity) mContext).getWindowManager().getDefaultDisplay().getWidth();
        int height = ((MainActivity) mContext).getWindowManager().getDefaultDisplay().getHeight() ;
        lp.width = displayWidth ;
        lp.height = height;
        window.setAttributes(lp);
        lp.gravity= Gravity.FILL;
        TextView title = (TextView) dialog.findViewById(R.id.title);
        AppCompatButton button_done = (AppCompatButton) dialog.findViewById(R.id.button_done);
        button_done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
               // ((Activity) mContext).getWindow().setStatusBarColor(getResources().getColor(R.color.black_30));
                setStatusBarGradiant(MainActivity.this, false);

//                closeDrawer();
               // ((MainActivity) mContext).mFragmentCardDetailsPlayer.onBackClicked();

            }
        });
        Typeface amazonEmberBold = Typeface.createFromAsset(getAppContext().getAssets(), "font/amazon_ember_cd_bold.ttf");
        title.setTypeface(amazonEmberBold);
        dialog.show();

    }

    private void showPromoVideoAdIfAvailable() {
        try {
            PromoAdData promoAdData = PropertiesHandler.getPromoAdData(mContext);
            if (promoAdData == null)
                return;
            String adId = PrefUtils.getInstance().getPromoAdId();
            SDKLogger.debug("promoAdData- " + promoAdData);
            if (adId == null) {
                PrefUtils.getInstance().setPromoAdId(promoAdData.id);
            }
            if (adId != null && !adId.equalsIgnoreCase(promoAdData.id)) {
                SDKLogger.debug("resetting adId " + "new adId- " + adId + " old adId- " + promoAdData.id);
                PrefUtils.getInstance().setAppLaunchCountUpUntill20(1);
                PrefUtils.getInstance().setPromoAdId(promoAdData.id);
            }


            if (checkPromoEligibility(promoAdData)) {
                ApplicationController.IS_PROMO_AD_SHOWN = true;
                startActivityForResult(FullscreenWebViewActivity.createIntent(this, promoAdData, false, false, null, null), FullScreenWebViewFragment.REQUEST_PROMO_AD);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean checkPromoEligibility(PromoAdData promoAdData) {
        if (promoAdData == null
                || TextUtils.isEmpty(promoAdData.htmlURL)
                || TextUtils.isEmpty(promoAdData.frequency)) {
            SDKLogger.debug("some thing is wrong with promo ad data or user is not logged in");
            return false;
        }
        String[] promoFrequencies = promoAdData.frequency.split(",");
        for (String frequent : promoFrequencies) {
            int iFrequent = Integer.parseInt(frequent);
            int launchCount = PrefUtils.getInstance().getAppLaunchCountUpUntill20();
            SDKLogger.debug("isFrequent- " + iFrequent + " launchCount- " + launchCount);
            if (!ApplicationController.IS_PROMO_AD_SHOWN
                    && launchCount != 0
                    && (iFrequent == launchCount
                    || iFrequent == -1)) {
                SDKLogger.debug("valid promo data show promo ad");
                return true;
            }
        }
        return false;
    }

    private void initHungamaSDK() {
        if (isFinishing()) return;
        try {

            int userid = PrefUtils.getInstance().getPrefUserId();
            LoggerD.debugLog("user id = " + userid);
            LoggerD.debugLog("getPrefMsisdnNo = " + PrefUtils.getInstance().getPrefMsisdnNo());
            if (userid != 0) {
                Analytics.mixpanelIdentify();
                if (mContext.getResources().getBoolean(R.bool.crashlytics_enable)) {
                    //Crashlytics.setUserIdentifier(PrefUtils.getInstance().getPrefUserId() + "");
                }
                if (TextUtils.isEmpty(PrefUtils.getInstance().getServiceName())) {
                    fetchUserId();
                }
//            PlayUtils.initialization((Activity) mContext);
                return;
            }
//        PlayUtils.initialization((Activity) mContext);
            fetchUserId();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initVMAXAds() {
//        PrefUtils.getInstance().setPrefVmaxBannerAdId("f63b82c2");
//        PrefUtils.getInstance().setPrefVmaxInterStitialAdId("96fc0b27");
//        Instream Ads Actual Id: 813cb254,
//        Test Id: 7553f735
//        PrefUtils.getInstance().setPrefVmaxInStreamAdId("813cb254");
        if (!Util.checkUserLoginStatus()) {
            SDKLogger.debug("user is not logged in");
            return;
        }
        if (PrefUtils.getInstance().isAdEnabled()) {
            initGoogleAds();
        }
//TODO enable to go live for every day check
        cacheInterstitial();
        cacheBannerAds();
    }

    private boolean checkIsAdShownToday() {

        Date date = new Date(); // or simply new Date();

        // converting it back to a milliseconds representation:
        long current_time = date.getTime();
        SDKLogger.debug("interstrialAd current_time-  " + current_time);


        long savedSystemTime = PrefUtils.getInstance().getLastVMXAdShownDate();

        if (savedSystemTime == 0) {
            isToShowAdForFirst = true;
            PrefUtils.getInstance().setLastVMXAdShownDate(current_time);
            return false;
        }
        savedSystemTime = PrefUtils.getInstance().getLastVMXAdShownDate();
        long timeOffSetMillis = PrefUtils.getInstance().getPrefVmaxInterStitialAdFrequency();
        SDKLogger.debug("savedSystemTime- " + savedSystemTime + " timeOffSetMillis- " + timeOffSetMillis + " current_time- " + current_time);
        if ((current_time < (savedSystemTime + timeOffSetMillis))) {
            SDKLogger.debug("Interstial Ad already shown ignore for now");
            return true;
        }
        PrefUtils.getInstance().setLastVMXAdShownDate(current_time);
        return false;

    }
    float lastTranslate = 0.0f;
    private void setUpDrawer() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
        //For Animation required for Sundirect we added this lines
        mDrawerLayout.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
                                           @Override
                                           public void onDrawerSlide(View drawerView, float slideOffset) {

                                                        Log.d(TAG, "slow navigation drawer run: handler inside");
                                                        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) homeLinearLayout.getLayoutParams();
                                                        homeLinearLayoutcard.setVisibility(View.VISIBLE);
                                                        int density = mContext.getResources().getDisplayMetrics().densityDpi;

                                                        switch (density) {
                                                            case DisplayMetrics.DENSITY_LOW:
                                                            case DisplayMetrics.DENSITY_MEDIUM:
                                                            case DisplayMetrics.DENSITY_HIGH:
                                                            case DisplayMetrics.DENSITY_XHIGH:
//
                                                                new Handler().postDelayed(new Runnable() {
                                                                    @Override
                                                                    public void run() {
                                                                        layoutParams.setMargins(3, 0, 0, 0);
//                                                                        homeLinearLayout.setLayoutParams(layoutParams);
                                                                        homeLinearLayout.requestLayout();
                                                                        homeLinearLayout.setRadius(48);
                                                                        homeLinearLayoutcard.setRadius(48);
                                                                    }
                                                                }, 100);

//
                                                                break;
                                                            default:
                                                                new Handler().postDelayed(new Runnable() {
                                                                    @Override
                                                                    public void run() {
                                                                        layoutParams.setMargins(4, 0, 0, 0);
                                                                        homeLinearLayout.requestLayout();
                                                                        homeLinearLayout.setRadius(71);
                                                                        homeLinearLayoutcard.setRadius(71);

                                                                    }
                                                                }, 100);
                                                                 break;
                                                        }


                                               // Scale the View based on current slide offset
                                               final float diffScaledOffset = slideOffset * (1 - END_SCALE);
                                               final float offsetScale = 1 - diffScaledOffset;
                                               homeLinearLayout.setScaleX(offsetScale);
                                               homeLinearLayout.setScaleY(offsetScale);
                                               homeLinearLayoutcard.setScaleX(offsetScale);
                                               homeLinearLayoutcard.setScaleY(offsetScale);

                                               // Translate the View, accounting for the scaled width
                                               final float xOffset = drawerView.getWidth() * slideOffset;
                                               final float xOffsetDiff = homeLinearLayout.getWidth() * diffScaledOffset / 2;
                                               final float xOffsetDiff1 = homeLinearLayoutcard.getWidth() * diffScaledOffset / 2;
                                               final float xTranslation = xOffset - xOffsetDiff;
                                               final float xTranslation1 = xOffset - xOffsetDiff1;
                                               homeLinearLayout.setTranslationX(xTranslation);
                                               homeLinearLayoutcard.setTranslationX(xTranslation1);

                                           }

                                       }
        );
        if (mDrawerLayout != null && mToolbar != null) {
            toggle = new ActionBarDrawerToggle(
                    this, mDrawerLayout, mToolbar, R.string.app_name, R.string.app_name) {
                @Override
                public void onDrawerClosed(View drawerView) {


                   /* ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) homeLinearLayout.getLayoutParams();
                    homeLinearLayoutcard.setVisibility(View.VISIBLE);
                    int density = mContext.getResources().getDisplayMetrics().densityDpi;

                    homeLinearLayout.requestLayout();
                    homeLinearLayout.setRadius(48);
                    homeLinearLayoutcard.setRadius(48);*/

                    // Code here will be triggered once the drawer closes as we dont want anything to happen so we leave this blank

                    super.onDrawerClosed(drawerView);


                    InputMethodManager inputMethodManager = (InputMethodManager)
                            getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (inputMethodManager != null && getCurrentFocus() != null)
                        inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) homeLinearLayout.getLayoutParams();
                            layoutParams.setMargins(0, 0, 0, 0);

                            homeLinearLayout.requestLayout();
                            homeLinearLayout.setRadius(0);
                            homeLinearLayoutcard.setRadius(0);
                            homeLinearLayoutcard.setVisibility(GONE);
                        }
                    }, 100);

                  /*  if( mFragmentCardDetailsPlayer.mPlayer!=null)
                    mFragmentCardDetailsPlayer.mPlayer.onResume();*/
                /*    if( PrefUtils.getInstance().getBoolean(PrefUtils.PREF_APPS_AS_HOME, false)){
                        if(menuTabs != null ) {
                            menuTabs.getTabAt(1).select();
                        }
                    } else {
                        if(menuTabs != null ) {
                            menuTabs.getTabAt(0).select();
                        }
                    }*/
                }

                @Override
                public void onDrawerOpened(View drawerView) {
                    // Code here will be triggered once the drawer open as we dont want anything to happen so we leave this blank
                    super.onDrawerOpened(drawerView);
                    InputMethodManager inputMethodManager = (InputMethodManager)
                            getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (inputMethodManager != null && getCurrentFocus() != null)
                        inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                }

                @Override
                public void onDrawerSlide(View drawerView, float slideOffset) {
                    super.onDrawerSlide(drawerView, slideOffset);
                    Log.d(TAG, "slow navigation drawer onDrawerSlide: slideOffset "+ slideOffset);
//                    float moveFactor = (mDrawerLayout.getWidth() * slideOffset);

                   /* if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                    {
                        mViewPager.setTranslationX(moveFactor);
                    }
                    else
                    {
                        TranslateAnimation anim = new TranslateAnimation(lastTranslate, moveFactor, 0.0f, 0.0f);
                        anim.setDuration(0);
                        anim.setFillAfter(true);
                        mViewPager.startAnimation(anim);

                        lastTranslate = moveFactor;
                    }*/
                }
            };
            if (isNavigation && toggle != null) {
                toggle.setDrawerIndicatorEnabled(true);
            }
            mDrawerLayout.addDrawerListener(toggle);
            toggle.syncState();
            disableNavigation();
         /*   if( PrefUtils.getInstance().getBoolean(PrefUtils.PREF_APPS_AS_HOME, false)){
                if(menuTabs != null ) {
                    menuTabs.getTabAt(1).select();
                }
            } else {
                if(menuTabs != null ) {
                    menuTabs.getTabAt(0).select();
                }
            }*/
        }
    }

    private void prepareDrawerRecycleView() {
        if (mDrawerRecycleView != null) {
            mListCarouselInfoDrawer = new ArrayList<>();
            if (mListCarouselInfo != null && mListCarouselInfo.size() > 0) {
                //Filtering the carouselData based on state variable to show in NavDrawer
                mListCarouselInfoDrawer = getItemsToShowFromCarouselInfoData(mListCarouselInfo);
            }

//            Log.e("LIST SIZE", mListCarouselInfo.size() + "");
//            Log.e("LIST DRAWER SIZE", mListCarouselInfoDrawer.size() + "");

            addStaticDataToDrawer();
//            addCarouselSideNavMenuData();
//            Log.e("LIST SIZE", mListCarouselInfo.size() + "");
//            Log.e("LIST DRAWER SIZE", mListCarouselInfoDrawer.size() + "");

            drawerListAdapter = new DrawerListAdapter(mListCarouselInfoDrawer, MainActivity.this);
            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
/*            DividerItemDecoration divider = new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL);
            divider.setDrawable(ContextCompat.getDrawable(mContext, R.drawable.rv_divider_horizontal));
            mDrawerRecycleView.addItemDecoration(divider);*/

            mDrawerRecycleView.setHasFixedSize(true);
            mDrawerRecycleView.setAdapter(null);
            mDrawerRecycleView.setLayoutManager(mLayoutManager);
            mDrawerRecycleView.setItemAnimator(new DefaultItemAnimator());
            mDrawerRecycleView.setAdapter(drawerListAdapter);
            drawerListAdapter.addOnItemClickListener(this);
            homePagerAdapterDynamicMenu.notifyDataSetChanged();
        }
    }

    /*
        Filters the CarouselInfoData based on state
        @param listCarouselInfoData
     */
    private List<CarouselInfoData> getItemsToShowFromCarouselInfoData(List<CarouselInfoData> listCarouselInfo) {

        List<CarouselInfoData> filteredCarouselInfo = new ArrayList<>();
        for (CarouselInfoData infoData : listCarouselInfo) {
            if (infoData.isSideNavMenuItem() && infoData.userState == null) {
                filteredCarouselInfo.add(infoData);
                continue;
            }
            if ((infoData.isSideNavMenuItem() || infoData.isSideNavMenuSeperatorItem()) && (infoData.userState.equalsIgnoreCase(APIConstants.ALL) || isLoggedInItemAndUserLoggedIn(infoData))) {
                filteredCarouselInfo.add(infoData);
            }

        }

        return filteredCarouselInfo;

    }

    private boolean isLoggedInItemAndUserLoggedIn(CarouselInfoData infoData) {
        if (infoData.userState.equalsIgnoreCase(
                APIConstants.LOGIN)
                && PrefUtils.getInstance().getPrefLoginStatus() != null
                && PrefUtils.getInstance().getPrefLoginStatus().equalsIgnoreCase(APIConstants.SUCCESS)) {
            return true;
        }
        return false;

    }

    private void addCarouselSideNavMenuData() {
        List<CarouselInfoData> sideNavMenuData = getSideNavMenuData();
        if (sideNavMenuData == null || sideNavMenuData.isEmpty()) return;
        mListCarouselInfoDrawer.addAll(sideNavMenuData);
    }

    private void addStaticDataToDrawer() {

        CarouselInfoData mCarouselInfoDataMyWatchlist = new CarouselInfoData();
        mCarouselInfoDataMyWatchlist.title = getResources().getString(R.string.navigation_my_watchlist);
        mCarouselInfoDataMyWatchlist.menuIcon = R.drawable.ic_navigation_my_watchlist;

        CarouselInfoData mCarouselInfoDataMyDownloads = new CarouselInfoData();
        mCarouselInfoDataMyDownloads.title = getResources().getString(R.string.navigation_my_downloads);
        mCarouselInfoDataMyDownloads.menuIcon = R.drawable.ic_navigation_my_downloads;

        CarouselInfoData mCarouselInfoDataAppSettings = new CarouselInfoData();
        mCarouselInfoDataAppSettings.title = getResources().getString(R.string.navigation_app_settings);
        mCarouselInfoDataAppSettings.menuIcon = R.drawable.ic_navigation_settings;

        CarouselInfoData mCarouselInfoDataAbout = new CarouselInfoData();
        mCarouselInfoDataAbout.title = getResources().getString(R.string.navigation_about);
        mCarouselInfoDataAbout.menuIcon = R.drawable.ic_navigation_about;

        CarouselInfoData mCarouselInfoDataHelp = new CarouselInfoData();
        mCarouselInfoDataHelp.title = getResources().getString(R.string.navigation_help);
        mCarouselInfoDataHelp.menuIcon = R.drawable.ic_navigation_help;

        CarouselInfoData mCarouselInfoMyAccount = new CarouselInfoData();
        mCarouselInfoMyAccount.title = getResources().getString(R.string.sun_direct);
        mCarouselInfoMyAccount.menuIcon = R.drawable.ic_navigation_about;

        //mListCarouselInfoDrawer.add(mCarouselInfoMyAccount);

        CarouselInfoData mCarouselInfoDataSignOut = new CarouselInfoData();
        mCarouselInfoDataSignOut.name = APIConstants.SIDENAV_MENU_NAME_LOGOUT;
        mCarouselInfoDataSignOut.title = APIConstants.SIDENAV_MENU_NAME_LOGOUT;
        mCarouselInfoDataSignOut.menuIcon = R.drawable.ic_navigation_sign_out;
        /*mListCarouselInfoDrawer.add(mCarouselInfoDataMyWatchlist);
        mListCarouselInfoDrawer.add(mCarouselInfoDataMyDownloads);
        mListCarouselInfoDrawer.add(mCarouselInfoDataAppSettings);
        mListCarouselInfoDrawer.add(mCarouselInfoDataAbout);
        mListCarouselInfoDrawer.add(mCarouselInfoDataHelp);*/
        if (Util.checkUserLoginStatus()) {
            mListCarouselInfoDrawer.add(mCarouselInfoDataSignOut);
        }
    }

    public void makeSignOutRequest() {
        SignOut deviceUnRegRequest = new SignOut(new APICallback() {
            @Override
            public void onResponse(APIResponse response) {
                if (response != null) {
                    SDKLogger.debug(response.toString());
                    launchLoginActivity("MY ACCOUNT", "MY ACCOUNT");
                    Toast.makeText(mContext, response.message(), Toast.LENGTH_SHORT).show();
                }
                SDKLogger.debug("SUCCESS");
                if (response != null && response.isSuccess()) {
                    CleverTap.eventLogOut();
                    doLogout();
                }
                dismissProgressBar(true);

            }

            @Override
            public void onFailure(Throwable t, int errorCode) {
                SDKLogger.debug("Error deregestering device " + errorCode);
                Toast.makeText(mContext,
                        "No network",
                        Toast.LENGTH_SHORT).show();
                dismissProgressBar(true);
            }
        });
        APIService.getInstance().execute(deviceUnRegRequest);
        showProgressBar(true);
    }

    @Override
    public void onOnItemClicked(CarouselInfoData navDrawerItem, int position) {

        //TODO: Remove this condition
        if ((APIConstants.LAYOUT_TYPE_MENU.equalsIgnoreCase(navDrawerItem.layoutType) || APIConstants.LAYOUT_TYPE_NAV_MENU.equalsIgnoreCase(navDrawerItem.layoutType)) && mViewPager != null && position > 0) {
            int positionInTab = getPositionInBottomTab(navDrawerItem.title);
            mViewPager.setCurrentItem(positionInTab);
            DrawerListAdapter.selectedItem = position;
            mDrawerRecycleView.getAdapter().notifyDataSetChanged();
        } else {
            Intent settings = new Intent(mContext, SettingsActivity.class);
            if (!TextUtils.isEmpty(navDrawerItem.name) && APIConstants.SIDENAV_MENU_NAME_LOGOUT.equalsIgnoreCase(navDrawerItem.name)) {
                //TODO: logout the user by calling deregisterAPI and call doLogout().
                makeSignOutRequest();
            }
            if (navDrawerItem.title.equalsIgnoreCase(getResources().getString(R.string.navigation_my_downloads))) {
                MyDownloadsFragment myDownloadsFragment = new MyDownloadsFragment();
                Bundle args = new Bundle();
                args.getBoolean(MyDownloadsFragment.PARAM_SHOW_TOOLBAR, true);
                if (PrefUtils.getInstance().getVernacularLanguage()) {
                    args.putString(APIConstants.LanguageTitle, navDrawerItem.altTitle);
                }
                myDownloadsFragment.setArguments(args);
                pushFragment(myDownloadsFragment);
            }
            else if (navDrawerItem.title.equalsIgnoreCase("Others")) {
                settings.putExtra(SettingsActivity.SECTION_TITLE, "Others");
             /*   if (PrefUtils.getInstance().getVernacularLanguage()) {
                    settings.putExtra(SettingsActivity.SECTION_TITLE_LANGUAGE, navDrawerItem.altTitle);
                }*/
                settings.putExtra(SettingsActivity.SECTION_TYPE, SettingsActivity.APPLICATION_MORE);
                startActivity(settings);
                return;
            } else if (navDrawerItem.title.equalsIgnoreCase(getResources().getString(R.string.navigation_help))) {
                settings.putExtra(SettingsActivity.SECTION_TITLE, navDrawerItem.title);
                if (PrefUtils.getInstance().getVernacularLanguage()) {
                    settings.putExtra(SettingsActivity.SECTION_TITLE_LANGUAGE, navDrawerItem.altTitle);
                }
                settings.putExtra(SettingsActivity.SECTION_TYPE, SettingsActivity.APPLICATION_HELP);
                startActivity(settings);
            }
            else if (navDrawerItem.title.equalsIgnoreCase("Packages") ||navDrawerItem.title.equalsIgnoreCase("Subscriptions") || navDrawerItem.title.equalsIgnoreCase("Recharge")) {
                String sundirectMusicUrl = null;
                String clientKey = PrefUtils.getInstance().getPrefClientkey();
                if (!TextUtils.isEmpty(navDrawerItem.actionUrl)) {
                    sundirectMusicUrl = navDrawerItem.actionUrl;
                    if (sundirectMusicUrl.contains("@CLIENT_KEY@")) {
                        sundirectMusicUrl = sundirectMusicUrl.replace("@CLIENT_KEY@", clientKey);
                    }
                    if (sundirectMusicUrl.contains(getResources().getString(R.string.is_login_true_text))) {
                        if (!Util.checkUserLoginStatus()) {
                            launchLoginActivity(navDrawerItem.title, navDrawerItem.title);
                        }
                        if (Util.checkUserLoginStatus()) {
                            sundirectMusicUrl = sundirectMusicUrl.replace(getResources().getString(R.string.is_login_true_text), "");
                        } else {
                            return;
                        }
                    }
                }
                startActivityForResult(SubscriptionWebActivity.createIntent(MainActivity.this, sundirectMusicUrl, SubscriptionWebActivity.PARAM_LAUNCH_NONE), 1);
                return;
            } else if (navDrawerItem.title.equalsIgnoreCase(getResources().getString(R.string.navigation_app_settings))) {
                settings.putExtra(SettingsActivity.SECTION_TITLE, navDrawerItem.title);
                if (PrefUtils.getInstance().getVernacularLanguage()) {
                    settings.putExtra(SettingsActivity.SECTION_TITLE_LANGUAGE, navDrawerItem.altTitle);
                }
                settings.putExtra(SettingsActivity.SECTION_TYPE, SettingsActivity.APPLICATION_SETTINGS);
                startActivity(settings);
//            } else if (navDrawerItem.title.equalsIgnoreCase(getResources().getString(R.string.cancel_subscription))) {
//                String clientKey = PrefUtils.getInstance().getPrefClientkey();
//                String url = navDrawerItem.actionUrl+"?clientKey="+clientKey;
//                startActivityForResult(SubscriptionWebActivity.createIntent(mContext, url, SubscriptionWebActivity.PARAM_LAUNCH_HOME), SubscriptionWebActivity.SUBSCRIPTION_REQUEST);
//                return;
////                CleverTap.eventPageViewed(CleverTap.PAGE_MYPACKS);
////                Intent intent = new Intent(mContext, SubscriptionWebActivity.class);
////                startActivity(intent);
            } else if (navDrawerItem.title.equalsIgnoreCase(getResources().getString(R.string.myStuff))) {
//                if (BuildConfig.FLAVOR.contains("bcn")) {
//                    CleverTap.eventPageViewed(CleverTap.PAGE_MYPACKS);
//                    Intent intent = new Intent(mContext, ActivityMyPacks.class);
//                    startActivity(intent);
//                }
            }
           /* else if (navDrawerItem.title.equalsIgnoreCase(getResources().getString(R.string.recharge))) {
                showHomePopUpPromotion();
                return;
            }*/
            else if (navDrawerItem.title.equalsIgnoreCase(getResources().getString(R.string.my_account))) {
                if (!Util.checkUserLoginStatus()) {
                    launchLoginActivity(navDrawerItem.title, navDrawerItem.title);
                } else {
                    startActivityForResult(ProfileActivity.createIntent(mContext), ProfileActivity.edit_profile_code);
                }
            } else if (navDrawerItem.title.equalsIgnoreCase(getResources().getString(R.string.contact_us))) {
                Intent mIntent = new Intent(mContext, LiveScoreWebView.class);
                mIntent.putExtra(LiveScoreWebView.PARAM_TOOLBAR_TITLE, getString(R.string.contact_us));

                if (TextUtils.isEmpty(PrefUtils.getInstance().getContactUsPageURL())) {
                    return;
                }
                mIntent.putExtra("url", PrefUtils.getInstance().getContactUsPageURL());
                startActivity(mIntent);
            } else if (navDrawerItem.title.equalsIgnoreCase(getResources().getString(R.string.support))) {
                settings.putExtra(SettingsActivity.SECTION_TITLE, navDrawerItem.title);
                if (PrefUtils.getInstance().getVernacularLanguage()) {
                    settings.putExtra(SettingsActivity.SECTION_TITLE_LANGUAGE, navDrawerItem.altTitle);
                }
                settings.putExtra(SettingsActivity.SECTION_TYPE, SettingsActivity.APPLICATION_HELP);
                startActivity(settings);
            } else if (navDrawerItem.name.equalsIgnoreCase(PREMIUM)) {
                if (!Util.checkUserLoginStatus()) {
                    launchLoginActivity(navDrawerItem.title, navDrawerItem.title);
                    return;
                }

                if (TextUtils.isEmpty(PrefUtils.getInstance().getUserCountry()) || TextUtils.isEmpty(PrefUtils.getInstance().getUserState()) ||
                        TextUtils.isEmpty(PrefUtils.getInstance().getUSerCity())) {
                    editProfileAlertDialog();
                    return;
                }

                Intent ip = new Intent(mContext, SubscriptionWebActivity.class);
                ip.putExtra(SubscriptionWebActivity.IS_FROM_PREMIUM, true);
                startActivity(ip);
            } else if (APIConstants.LAYOUT_TYPE_SIDE_NAV_MENU.equalsIgnoreCase(navDrawerItem.layoutType)) {
                if (navDrawerItem.name.equalsIgnoreCase("notificationsMenu")) {
                    Intent notifications = new Intent(mContext, NotificationActivityNew.class);
                    startActivity(notifications);
                }
                if (navDrawerItem.name.equalsIgnoreCase("settingsMenu5x")) {
                    settings.putExtra(SettingsActivity.SECTION_TITLE, navDrawerItem.title);
                    if (PrefUtils.getInstance().getVernacularLanguage()) {
                        settings.putExtra(SettingsActivity.SECTION_TITLE_LANGUAGE, navDrawerItem.altTitle);
                    }
                    settings.putExtra(SettingsActivity.SECTION_TYPE, SettingsActivity.APPLICATION_SETTINGS);
                    startActivity(settings);
                }
                if(navDrawerItem.name.equalsIgnoreCase("aboutUSMenu5x")){
                    Intent aboutUs = new Intent(MainActivity.this,SettingsActivity.class);
                    aboutUs.putExtra(SettingsActivity.SECTION_TYPE, SettingsActivity.APPLICATION_MORE);
                    startActivity(aboutUs);

                }
//                else if (navDrawerItem.name.equalsIgnoreCase("packagesMenu")) {
//                    String clientKey = PrefUtils.getInstance().getPrefClientkey();
//                    String url = navDrawerItem.actionUrl+"?clientKey="+clientKey;
//                    startActivityForResult(SubscriptionWebActivity.createIntent(mContext, url, SubscriptionWebActivity.PARAM_LAUNCH_HOME), SubscriptionWebActivity.SUBSCRIPTION_REQUEST);
//                    return;
//                }
//                else if (navDrawerItem.name.equalsIgnoreCase("rechargeMenu")) {
//                    String clientKey = PrefUtils.getInstance().getPrefClientkey();
//                    String url = navDrawerItem.actionUrl+"?clientKey="+clientKey;
//                    startActivityForResult(SubscriptionWebActivity.createIntent(mContext, url, SubscriptionWebActivity.PARAM_LAUNCH_HOME), SubscriptionWebActivity.SUBSCRIPTION_REQUEST);
//                    return;
//                }
                if (navDrawerItem.appAction == null)
                    return;
                switch (navDrawerItem.appAction) {
                    case HomePagerAdapterDynamicMenu.ACTION_LAUNCH_WEBPAGE:
                        String sundirectMusicUrl = null;
                        String clientKey = PrefUtils.getInstance().getPrefClientkey();
                        if (!TextUtils.isEmpty(navDrawerItem.actionUrl)) {
                            sundirectMusicUrl = navDrawerItem.actionUrl;
                            if (sundirectMusicUrl.contains("@CLIENT_KEY@")) {
                                sundirectMusicUrl = sundirectMusicUrl.replace("@CLIENT_KEY@", clientKey);
                            }
                            if (sundirectMusicUrl.contains(getResources().getString(R.string.is_login_true_text))) {
                                if (!Util.checkUserLoginStatus()) {
                                    launchLoginActivity(navDrawerItem.title, navDrawerItem.title);
                                }
                                if (Util.checkUserLoginStatus()) {
                                    sundirectMusicUrl = sundirectMusicUrl.replace(getResources().getString(R.string.is_login_true_text), "");
                                } else {
                                    return;
                                }
                            } else if (sundirectMusicUrl.contains(getResources().getString(R.string.is_login_false_text))) {
                                sundirectMusicUrl = sundirectMusicUrl.replace(getResources().getString(R.string.is_login_false_text), "");
                            }

                        }
                        Bundle bundle = new Bundle();
                        bundle.putString(FragmentWebView.PARAM_URL, sundirectMusicUrl);
                        bundle.putBoolean(FragmentWebView.PARAM_SHOW_TOOLBAR, true);
                        bundle.putString(FragmentWebView.PARAM_TOOLBAR_TITLE, navDrawerItem.title);
                        if (PrefUtils.getInstance().getVernacularLanguage()) {
                            bundle.putString(FragmentWebView.PARAM_TOOLBAR_TITLE_LANG, navDrawerItem.altTitle);
                        }
                       /* BaseFragment fragment = (BaseFragment) Fragment.instantiate(mContext, FragmentWebView.class.getName(), bundle);
                        pushFragment(fragment);*/
                        startActivityForResult(SubscriptionWebActivity.createIntent(MainActivity.this, sundirectMusicUrl, SubscriptionWebActivity.PARAM_LAUNCH_NONE), 1);
                        break;
                    case HomePagerAdapterDynamicMenu.ACTION_DEEPLINK: {
                        switch (navDrawerItem.actionUrl) {
                            case APIConstants.APP_ACTION_MYDOWNLOADS:
                                MyDownloadsFragment myDownloadsFragment = new MyDownloadsFragment();
                                myDownloadsFragment.setToolBar(true);
                                Bundle downloadArgs = new Bundle();
                                downloadArgs.getBoolean(MyDownloadsFragment.PARAM_SHOW_TOOLBAR, true);
                                if (PrefUtils.getInstance().getVernacularLanguage()) {
                                    downloadArgs.putString(APIConstants.LanguageTitle, navDrawerItem.altTitle);
                                }
                                myDownloadsFragment.setArguments(downloadArgs);
                                pushFragment(myDownloadsFragment);
                                break;
                            case APIConstants.APP_ACTION_MYWATCHLIST:
                                MyWatchlistFavouritesFragment myWatchlistFavouritesFragment = new MyWatchlistFavouritesFragment();
                                if (PrefUtils.getInstance().getVernacularLanguage()) {
                                    myWatchlistFavouritesFragment.setAlttitle(navDrawerItem.altTitle);
                                }
                                myWatchlistFavouritesFragment.setRequestType(APIConstants.WATCHLIST_FETCH_REQUEST);
                                myWatchlistFavouritesFragment.settitle(navDrawerItem.title);
                                pushFragment(myWatchlistFavouritesFragment);
                                break;
                            case APIConstants.APP_ACTION_MYFAVOURITES:
                                MyWatchlistFavouritesFragment myFavouritesFragment = new MyWatchlistFavouritesFragment();
                                if (PrefUtils.getInstance().getVernacularLanguage()) {
                                    myFavouritesFragment.setAlttitle(navDrawerItem.altTitle);
                                }
                                myFavouritesFragment.setRequestType(APIConstants.FAVOURITES_FETCH_REQUEST);
                                myFavouritesFragment.settitle(navDrawerItem.title);
                                pushFragment(myFavouritesFragment);
                                break;
                            case APIConstants.APP_ACTION_APP_SETTINGS:
                                settings.putExtra(SettingsActivity.SECTION_TITLE, navDrawerItem.title);
                                if (PrefUtils.getInstance().getVernacularLanguage()) {
                                    settings.putExtra(SettingsActivity.SECTION_TITLE_LANGUAGE, navDrawerItem.altTitle);
                                }
                                settings.putExtra(SettingsActivity.SECTION_TYPE, SettingsActivity.APPLICATION_SETTINGS);
                                startActivity(settings);
                                break;
                            case APIConstants.APP_ACTION_ABOUT:
                                settings.putExtra(SettingsActivity.SECTION_TITLE, navDrawerItem.title);
                                if (PrefUtils.getInstance().getVernacularLanguage()) {
                                    settings.putExtra(SettingsActivity.SECTION_TITLE_LANGUAGE, navDrawerItem.altTitle);
                                }
                                settings.putExtra(SettingsActivity.SECTION_TYPE, SettingsActivity.APPLICATION_ABOUT);
                                startActivity(settings);
                                break;
                            case APIConstants.APP_ACTION_APP_LANGUAGE:
                                settings.putExtra(SettingsActivity.SECTION_TITLE, navDrawerItem.title);
                                if (PrefUtils.getInstance().getVernacularLanguage()) {
                                    settings.putExtra(SettingsActivity.SECTION_TITLE_LANGUAGE, navDrawerItem.altTitle);
                                }
                                settings.putExtra(SettingsActivity.SECTION_TYPE, SettingsActivity.APPLICATION_APP_LANGUAGE);
                                startActivity(settings);
                                break;
                            case APIConstants.APP_ACTION_HELP:
                                settings.putExtra(SettingsActivity.SECTION_TITLE, navDrawerItem.title);
                                if (PrefUtils.getInstance().getVernacularLanguage()) {
                                    settings.putExtra(SettingsActivity.SECTION_TITLE_LANGUAGE, navDrawerItem.altTitle);
                                }
                                settings.putExtra(SettingsActivity.SECTION_TYPE, SettingsActivity.APPLICATION_HELP);
                                startActivity(settings);
                                break;
                            case APIConstants.GENRE:
                            case APIConstants.LANGUAGES:
                                Bundle args = new Bundle();
                                args.putSerializable(SmallSquareItemsFragment.PARAM_CAROUSEL_INFO_DATA, navDrawerItem);
                                if (PrefUtils.getInstance().getVernacularLanguage()) {
                                    args.putBoolean(SmallSquareItemsFragment.PARAM_HINDI_VISIBLE, true);
                                } else {
                                    args.putBoolean(SmallSquareItemsFragment.PARAM_HINDI_VISIBLE, false);
                                }

                                pushFragment(SmallSquareItemsFragment.newInstance(args));
                                break;
                            case APIConstants.PREFERRED_LANGUAGES:
                                FragmentPreferredLanguages fragmentPreferredLanguages = FragmentPreferredLanguages.newInstance();
                                if (PrefUtils.getInstance().getVernacularLanguage()) {
                                    fragmentPreferredLanguages.setAltTitle(navDrawerItem.altTitle);
                                }
                                pushFragment(fragmentPreferredLanguages);
                                fragmentPreferredLanguages.setOnPreferredLanguagesActionPerformedListener(new FragmentPreferredLanguages.OnPreferredLanguagesActionPerformedListener() {
                                    @Override
                                    public void onSkipClicked() {
                                        removeFragment(mCurrentFragment);
                                    }

                                    @Override
                                    public void onDoneClicked() {
                                        removeFragment(mCurrentFragment);
                                        reloadData();
                                        if (!TextUtils.isEmpty(PrefUtils.getInstance().getPrefLoginStatus()) && APIConstants.SUCCESS.equalsIgnoreCase(PrefUtils.getInstance().getPrefLoginStatus()))
                                            updateProfile();
                                    }
                                });
                                break;
                            case APP_ACTION_WATCH_HISTORY:
                                FragmentWatchlistHistory myWatchlistFragmentHistory = new FragmentWatchlistHistory();
                                if (PrefUtils.getInstance().getVernacularLanguage()) {
                                    myWatchlistFragmentHistory.setAlttitle(navDrawerItem.altTitle);
                                }
                                myWatchlistFragmentHistory.setCarouselName(navDrawerItem.name);
                                pushFragment(myWatchlistFragmentHistory);
                                break;
                            case CONTACT_US:
                                Intent mIntent = new Intent(mContext, LiveScoreWebView.class);
                                mIntent.putExtra(LiveScoreWebView.PARAM_TOOLBAR_TITLE, getString(R.string.contact_us));
                                if (TextUtils.isEmpty(PrefUtils.getInstance().getContactUsPageURL())) {
                                    return;
                                }
                                mIntent.putExtra("url", PrefUtils.getInstance().getContactUsPageURL());
                                startActivity(mIntent);
                                break;
                            case PRIVACY_POLICY:
                                Intent ppc = new Intent(mContext, LiveScoreWebView.class);
                                ppc.putExtra(LiveScoreWebView.PARAM_TOOLBAR_TITLE, getString(R.string.privacy_policy));
                                if (!TextUtils.isEmpty(PrefUtils.getInstance().getPrivacy_policy_url())) {
                                    ppc.putExtra("url", PrefUtils.getInstance().getPrivacy_policy_url());
                                } else {
                                    ppc.putExtra("url", APIConstants.getFAQURL() + APIConstants.PRIVACY_POLICY_URL);
                                }
                                startActivity(ppc);
                                break;
                            case TERMS_AND_CONDITIONS:
                                Intent tnc = new Intent(mContext, LiveScoreWebView.class);
                                CleverTap.eventPageViewed(CleverTap.PAGE_TERMS_AND_CONDITIONS);
                                AppsFlyerTracker.eventBrowseHelp();
                                tnc.putExtra("url", APIConstants.getFAQURL() + APIConstants.TNC_URL);
                                if (!TextUtils.isEmpty(PrefUtils.getInstance().getTncUrl())) {
                                    tnc.putExtra("url", PrefUtils.getInstance().getTncUrl());
                                } else {
                                    tnc.putExtra("url", APIConstants.getFAQURL() + APIConstants.TNC_URL);
                                }
                                mContext.startActivity(tnc);
                                break;
                            case PREMIUM:
                                if (!Util.checkUserLoginStatus()) {
                                    launchLoginActivity(navDrawerItem.title, navDrawerItem.title);
                                    return;
                                }
                                Intent ip = new Intent(mContext, SubscriptionWebActivity.class);
                                ip.putExtra(SubscriptionWebActivity.IS_FROM_PREMIUM, true);
                                startActivity(ip);
                                break;
                        }
                    }
                    break;
                    case APIConstants.LAYOUT_TYPE_MENU:
                        //TODO: Show kids in seperate Fragment
                        setCarouselInfoData(navDrawerItem);
                        if (carouselInfoData != null) {
                            Bundle args = new Bundle();
                            CacheManager.setCarouselInfoData(navDrawerItem);
                            args.putString(FragmentCarouselInfo.PARAM_APP_FRAG_TYPE, navDrawerItem.name);
                            args.putString(FragmentCarouselInfo.PARAM_APP_PAGE_TITLE, navDrawerItem.title);
                            args.putBoolean(FragmentCarouselInfo.PARAM_SHOW_TOOLBAR, true);
                            args.putString(CleverTap.PROPERTY_TAB, getSelectedPageName());
                            selectedMenuFragment = (BaseFragment) Fragment.instantiate(mContext, FragmentCarouselInfo.class.getName(), args);
                            pushFragment(selectedMenuFragment);
                        }
                        break;
                }
            }
        }
       // closeDrawer();
    }

    private void updateProfile() {
        UserProfileRequest userProfileRequest = new UserProfileRequest(new APICallback<UserProfileResponseData>() {
            @Override
            public void onResponse(APIResponse<UserProfileResponseData> response) {
                SDKLogger.debug("Profile update onResponse");
                if (response == null || response.body() == null) {
                    return;
                }
                if (response.body().code == 402) {
                    PrefUtils.getInstance().setPrefLoginStatus("");
                    return;
                }
                if (response.body().code == 200) {
                    UserProfileResponseData responseData = response.body();
                    if ( responseData.result != null
                            && responseData.result.profile != null) {
                        if (responseData.result.profile.locations != null && responseData.result.profile.locations.size() > 0) {
                            if (responseData.result.profile.locations.get(0) != null
                                    && !TextUtils.isEmpty(responseData.result.profile.locations.get(0)))
                                PrefUtils.getInstance().setUSerCountry(responseData.result.profile.locations.get(0));
                        }
                        if (responseData.result.profile.packageLanguages != null && responseData.result.profile.packageLanguages.size() > 0) {
                            if (responseData.result.profile.packageLanguages.get(0) != null
                                    && !TextUtils.isEmpty(responseData.result.profile.packageLanguages.get(0)))
                                PrefUtils.getInstance().setSubscribedLanguage(responseData.result.profile.packageLanguages);
                        }

                        if (responseData.result.profile.name != null && !TextUtils.isEmpty(responseData.result.profile.name)) {
                            PrefUtils.getInstance().setPrefFullName(responseData.result.profile.name );
                        }

                        if (responseData.result.profile.mobile_no != null && !TextUtils.isEmpty(responseData.result.profile.mobile_no)) {
                            PrefUtils.getInstance().setPrefMobileNumber(responseData.result.profile.mobile_no );
                        }
                        if (responseData.result.profile.email != null && !TextUtils.isEmpty(responseData.result.profile.email)) {
                            PrefUtils.getInstance().setPrefEmailID(responseData.result.profile.email );
                        }
                        if (responseData.result.profile.smc_no != null && !TextUtils.isEmpty(responseData.result.profile.smc_no)) {
                            PrefUtils.getInstance().setPrefSmartCardNumber(responseData.result.profile.smc_no );
                        }

                     /*   String url = PrefUtils.getInstance().getString("PROFILE_IMAGE_URL");
                        if(TextUtils.isEmpty(url)) {
                            url=PrefUtils.getInstance().getDefaultProfileImage();
                        }*/
                        updateNavMenuMyAccountSection();
                    }
                }

            }

            @Override
            public void onFailure(Throwable t, int errorCode) {
                SDKLogger.debug("Profile update onFailure");
            }
        });
        APIService.getInstance().execute(userProfileRequest);
    }

    BaseFragment selectedMenuFragment;


    public void closeDrawer() {
        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                SDKLogger.debug("closeDrawer is opened- " + (mDrawerLayout != null
                        && mDrawerLayout.isDrawerOpen(GravityCompat.START)));
                if (mDrawerLayout != null
                        && mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                    mDrawerLayout.closeDrawer(GravityCompat.START);
                }
              /*  if( PrefUtils.getInstance().getBoolean(PrefUtils.PREF_APPS_AS_HOME, false)){
                    if(menuTabs != null ) {
                        menuTabs.getTabAt(1).select();
                    }
                } else {
                    if(menuTabs != null ) {
                        menuTabs.getTabAt(0).select();
                    }
                }*/
            }
        });

    }

    private void initBundle() {
        mListCarouselInfo = CacheManager.getCarouselInfoDataList();
        launchMessage = null;
        if (getIntent().hasExtra(INTENT_PARAM_TOAST_MESSAGE)) {
            launchMessage = getIntent().getStringExtra(INTENT_PARAM_TOAST_MESSAGE);
        }
    }

    private void loadCarouselInfo() {
        AlertDialogUtil.showProgressAlertDialog(mContext);
        showRetryOption(false);
        new MenuDataModel().fetchMenuList(getString(R.string.MENU_TYPE_GROUP_ANDROID_NAV_MENU), 1, APIConstants.PARAM_CAROUSEL_API_VERSION, new MenuDataModel.MenuDataModelCallback() {
            @Override
            public void onCacheResults(List<CarouselInfoData> dataList) {
                AlertDialogUtil.dismissProgressAlertDialog();
                if (dataList == null) {
                    showRetryOption(true);
                    return;
                }
                mListCarouselInfo = dataList;
                if (mListCarouselInfo.size() > 0) {
                    initUI();
                }
                //Log.d(TAG, "fetchData: onResponse: size - " + mListCarouselInfo.size());
            }

            @Override
            public void onOnlineResults(List<CarouselInfoData> dataList) {

                AlertDialogUtil.dismissProgressAlertDialog();
                if (dataList == null) {
                    showRetryOption(true);
                    return;
                }
                mListCarouselInfo = dataList;
                if (mListCarouselInfo.size() > 0) {
                    initUI();
                }
                //Log.d(TAG, "fetchData: onResponse: size - " + mListCarouselInfo.size());
            }

            @Override
            public void onOnlineError(Throwable error, int errorCode) {

                AlertDialogUtil.dismissProgressAlertDialog();
                //Log.d(TAG, "onOnlineError: error- " + error.getMessage() + " errorCode- " + errorCode);
//                TODO Check if previous response data is available.
                if (APIConstants.menuListPath == null) {
                    APIConstants.menuListPath = getFilesDir() + "/" + "menuList.bin";
                }
                try {
                    mListCarouselInfo = (List<CarouselInfoData>) SDKUtils.loadObject(APIConstants.menuListPath);
                    if (mListCarouselInfo == null || mListCarouselInfo.isEmpty()) {
                        showRetryOption(true);
                        return;
                    }

                    initUI();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        });

    }

    private View.OnClickListener mRetryClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            loadCarouselInfo();
        }
    };

    private void showRetryOption(boolean b) {
        if (b) {
            mLayoutRetry.setVisibility(View.VISIBLE);
            return;
        }
        mLayoutRetry.setVisibility(GONE);
    }

    private Handler handler;
    private Runnable runnable;

    private void initUI() {
        if (isFinishing()) {
            return;
        }
        if (getIntent().getExtras() != null && getIntent().getExtras().containsKey(APIConstants.NOTIFICATION_PARAM_CAROUSEL_NAME) && getIntent().getExtras().getString(APIConstants.NOTIFICATION_PARAM_CAROUSEL_NAME) != null) {
            showFragmentsFromNotification(
                    getIntent().getExtras().getString(APIConstants.NOTIFICATION_PARAM_LAYOUT),
                    Integer.parseInt(getIntent().getExtras().getString(APIConstants.NOTIFICATION_PARAM_PAGE_COUNT)),
                    getIntent().getExtras().getString(APIConstants.NOTIFICATION_PARAM_CAROUSEL_NAME),
                    getIntent().getExtras().getString(APIConstants.NOTIFICATION_PARAM_CAROUSEL_TITLE)
            );
        }
        showProgressBar(true);
        Util.prepareDisplayinfo(this);
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (APIConstants.menuListPath == null) {
                    APIConstants.menuListPath = getFilesDir() + "/" + "menuList.bin";
                }
                SDKUtils.saveObject(mListCarouselInfo, APIConstants.menuListPath);
            }
        }).start();
        loadMenuImages();
    }

    private void loadMenuImages() {
        if (isFinishing()) {
            return;
        }
        Util.setImageCount(0);
        handler = new Handler();
        tabListData = getTabListData();
        if (tabListData != null && tabListData.size() > 0 && tabListData.get(0) != null) {
            ApplicationController.FIRST_TAB_NAME = tabListData.get(0).name;
        }
        saveMenuIconsOffline(mListCarouselInfo);
        final int tabCount = mListCarouselInfo.size();
        runnable = new Runnable() {
            @Override
            public void run() {
                if (isFinishing()) {
                    return;
                }
                if (Util.allImagesLoaded(getAllMenuImagesDataOnly(mListCarouselInfo).size())) {
                    showUI();
                } else {
                    loadMenuImages();
                }
            }
        };
        handler.postDelayed(runnable, 400);

    }

    private void showUI() {
        try {
            dismissProgressBar(true);
            mTabPagerRootLayout = findViewById(R.id.root_layout);
            mTabPagerRootLayout.setVisibility(View.VISIBLE);
            if (mToolbar != null) {
                mToolbar.setVisibility(View.VISIBLE);
            }
            //set up toolbar logo and title
            homePagerAdapterDynamicMenu = new HomePagerAdapterDynamicMenu(getSupportFragmentManager(), MainActivity.this, "", "", tabListData);
            homePagerAdapterDynamicMenu.setViewScrollListener(this);
            mViewPager.setAdapter(homePagerAdapterDynamicMenu);
            mViewPager.setPagingEnabled(false);
            mViewPager.setOffscreenPageLimit(5);
            int pageMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0, getResources().getDisplayMetrics());
            mViewPager.setPageMargin(pageMargin);
            //mTabPageIndicator.setBackgroundColor(getResources().getColor(R.color.app_theme_color));
            mTabPageIndicator.setViewPager(mViewPager);
            int listSize = tabListData.size();
            if (listSize > getResources().getInteger(R.integer.tab_count)) {
                menuTabs.setTabMode(TabLayout.MODE_SCROLLABLE);
            } else {
                menuTabs.setTabMode(TabLayout.MODE_FIXED);
                menuTabs.setTabGravity(TabLayout.GRAVITY_FILL);
            }
        /*    int density = mContext.getResources().getDisplayMetrics().densityDpi;
            if(density <= DisplayMetrics.DENSITY_420){
                menuTabs.setTabMode(TabLayout.MODE_SCROLLABLE);
            } else {
                menuTabs.setTabMode(TabLayout.MODE_FIXED);
                menuTabs.setTabGravity(TabLayout.GRAVITY_FILL);
            }*/
         /*   switch (density) {
                case DisplayMetrics.DENSITY_XHIGH:
                    menuTabs.setTabMode(TabLayout.MODE_SCROLLABLE);
                    break;
                default:
                    menuTabs.setTabMode(TabLayout.MODE_FIXED);
                    menuTabs.setTabGravity(TabLayout.GRAVITY_FILL);
                    break;
            }*/
            updateTabsMenu();
            mCurrentSelectedPagePosition = getPositionInCarousalWhenClickedBottomTab(tabListData.get(0).title);
            mCurrentSelectedPagePositionTitle = tabListData.get(0).title;
            DrawerListAdapter.selectedItem = getPositionInSideNavigaitonWhenClickedBottomTab(tabListData.get(0).title);
//            setUpToolBarMenu();

//            setToolBarLogoAndTitle(getResources().getString(R.string.app_name));
//            updateSelectedPageToolbar();

//            if(Util.doesCurrentTabHasPortraitBanner(getCurrentTabForVmax())) {
//                mAppBar.setBackground(mContext.getResources().getDrawable(R.drawable.banner_top_gradient));
//                mToolbar.setBackground(mContext.getResources().getDrawable(R.drawable.banner_top_gradient));
//            }else {
//                mToolbar.setBackgroundColor(mContext.getResources().getColor(R.color.toolbar_bg_colour));
//            }

//            mToolbar.setBackgroundColor(mContext.getResources().getColor(R.color.toolbar_bg_colour));

            hookDraggablePanelListeners();
            initializeDraggablePanel();
            mViewPager.postDelayed(new Runnable() {
                @Override
                public void run() {
                    ScopedBus.getInstance().post(new MessageEvent());
                }
            }, 3000);


            initGoogleChromeCast();
            onHandleExternalIntent(MainActivity.this.getIntent());
            // mTabPageIndicator.setOnPageChangeListener(mTabPageChangeListener);
            mToolbar.setNavigationIcon(R.drawable.menu_icon);
            setUpDrawer();

            prepareDrawerRecycleView();
//            mTextUserName.setText(PrefUtils.getInstance().getPrefMsisdnNo());

            toggleNavigation();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateTabsMenu() {

        int listSize = tabListData.size();
        for (int i = 0; i < listSize; i++) {
            LinearLayout tabLinearLayout = null;
            TextView tabContent = null;
            ImageView tabIcon = null;
            //   if (!lowerDevices) {
            if (DeviceUtils.isTablet(this)) {
                tabLinearLayout = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.custom_tab1, null);
            }else{
                tabLinearLayout = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
            }
            tabContent = (TextView) tabLinearLayout.findViewById(R.id.tabContent);
            tabIcon = (ImageView) tabLinearLayout.findViewById(R.id.tab_icon);
            tabContent.setText(tabListData.get(i).title);
            Typeface myCustomFont = Typeface.createFromAsset(getAssets(), "fonts/amazon_ember_cd_bold.ttf");
            tabContent.setTypeface(myCustomFont);
              String imageLink = getImageLink(tabListData.get(i).images);
            if (DeviceUtils.isTablet(this)) {
               // PicassoUtil.with(mContext).load(imageLink, tabIcon, R.drawable.ic_navigation_settings);
                // Picasso.get().load(imageLink).into(tabIcon);
                tabIcon.setImageBitmap(Util.getBitmap(mContext, null, tabListData.get(i), false));
            }else{
                tabIcon.setImageBitmap(Util.getBitmap(mContext, null, tabListData.get(i), false));
            }
            //tabIcon.setImageBitmap(Util.getBitmap(mContext, null, tabListData.get(i), false));
           //  PicassoUtil.with(mContext).load(imageLink, tabIcon, R.drawable.ic_navigation_settings);
            menuTabs.addTab(menuTabs.newTab().setTag(i).setCustomView(tabLinearLayout));
            // tabLayout.addTab(tabLayout.newTab().setTag(tabListData.get(i).getCode()).setCustomView(tabLinearLayout));
        }
        if( PrefUtils.getInstance().getBoolean(PrefUtils.PREF_APPS_AS_HOME, false)){
            if(menuTabs != null ) {
                setToolBarCollapsible(false, false);
                if(tabListData.get(2).name.equalsIgnoreCase(APIConstants.MENU_VOD)) {
                    menuTabs.getTabAt(2).select();
                }
            }
        } else {
            if(menuTabs != null ) {
                setToolBarCollapsible(false, false);
                menuTabs.getTabAt(0).select();
            }
        }


    }

    private List<CarouselInfoData> getTabListData() {
        if (mListCarouselInfo == null || mListCarouselInfo.isEmpty()) return null;
        List<CarouselInfoData> tabList = new ArrayList<>();
        for (CarouselInfoData carouselInfoData : mListCarouselInfo) {
            if (carouselInfoData.isMenuItem())
                tabList.add(carouselInfoData);
        }
        return tabList;
    }


    private List<CarouselInfoData> getSideNavMenuData() {
        if (mListCarouselInfo == null || mListCarouselInfo.isEmpty()) return null;
        List<CarouselInfoData> tabList = new ArrayList<>();
        for (CarouselInfoData carouselInfoData : mListCarouselInfo) {
            if (carouselInfoData.isSideNavMenuItem())
                tabList.add(carouselInfoData);
        }
        return tabList;
    }

    private void saveMenuIconsOffline(List<CarouselInfoData> mListCarouselInfo) {
        String imageLink;
        for (int i = 0; i < mListCarouselInfo.size(); i++) {
            for (int j = 0; j < mListCarouselInfo.get(i).images.size(); j++) {
                if (mListCarouselInfo.get(i).images.size() != 0) {
                    imageLink = getImageLink(mListCarouselInfo.get(i).images);
                   // imageLink = getImageLink(mListCarouselInfo.get(i).images.get(j), mListCarouselInfo.get(i).name);
                    if (imageLink != null) {
                        Util.saveMenuIcons(mContext, imageLink, mListCarouselInfo.get(i).name, mListCarouselInfo.get(i).images);
                        break;
                    }
                }
            }
        }
    }

    private String getImageLink(CardDataImagesItem imagesItem, String name) {
        if (DeviceUtils.isTablet(this)) {
            if (imagesItem.profile.equalsIgnoreCase(ApplicationConfig.XXHDPI))
                return imagesItem.link;
        } else {
            if (imagesItem.profile.equalsIgnoreCase(ApplicationConfig.XHDPI))
                return imagesItem.link;

        }
        return null;
    }

    private String getImageLink(List<CardDataImagesItem> imagesItem) {
        for (int j = 0; j < imagesItem.size(); j++) {
            if (DeviceUtils.isTablet(this)) {
                if (imagesItem.get(j).profile.equalsIgnoreCase(ApplicationConfig.XXHDPI))
                    return imagesItem.get(j).link;
            } else {
                if (imagesItem.get(j).profile.equalsIgnoreCase(ApplicationConfig.XHDPI)) {
                    return imagesItem.get(j).link;
                }
            }
        }

        return null;
    }


    /**
     * Toggle between Navigation View and Three Dot Menu
     * do isNavigation true for enable navigation view and vice versa.
     */
    @SuppressLint("RestrictedApi")
    public void toggleNavigation() {
        isNavigation = true;
        if (isNavigation) {
//            enableNavigation();
            disableNavigation();
            mToolbar.setTitleTextColor(getResources().getColor(R.color.white));
            mToolbar.setCollapsible(true);
            // mToolbar.setTitle(robotoStringFont(getResources().getString(R.string.app_name)));
        } else {
            disableNavigation();
            mToolbar.setNavigationIcon(null);
            setToolBarLogoAndTitle(getResources().getString(R.string.app_name));
        }
        setUpToolBarMenu();
        updateSelectedPageToolbar();
    }

    /**
     * Disable Naviigation Drawer
     */
    public void disableNavigation() {
        SDKLogger.debug("disableNavigation");
        if (mDrawerLayout != null) {
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            if(homeLinearLayout!=null) {
                ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) homeLinearLayout.getLayoutParams();
                layoutParams.setMargins(0, 0, 0, 0);
                homeLinearLayout.requestLayout();
                homeLinearLayout.setRadius(0);
                homeLinearLayoutcard.setVisibility(GONE);
            }

        }
    }

    /**
     * Enable Navigation Drawer
     */
    public void enableNavigation() {
        //SDKLogger.debug("enableNavigation");
//        if (mDrawerLayout != null) {
//            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
//        }
    }


    private void initGoogleChromeCast() {
        try {
            // initialize chrome cast
            initCastConsumer();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setDefaultPageSelected(final int selectedPageIndex) {
        if (mTabPageIndicator == null
                || homePagerAdapterDynamicMenu == null
                || selectedPageIndex > homePagerAdapterDynamicMenu.getCount() - 1) {
            LoggerD.debugLog("invalid case of setDefaultPageSelected selectedPageIndex- "
                    + selectedPageIndex
                    + " mTabPageIndicator is null - " + (mTabPageIndicator == null)
                    + " homePagerAdapter2 is null - " + (homePagerAdapterDynamicMenu == null));
            return;
        }
        LoggerD.debugLog("selectedPageIndex- "
                + selectedPageIndex);
        new Handler(getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (mTabPageIndicator != null) {
                    try {
                        mTabPageIndicator.setCurrentItem(selectedPageIndex);
                    } catch (IllegalStateException e) {
                        if (mViewPager != null) {
                            mTabPageIndicator.setViewPager(mViewPager);
                            mTabPageIndicator.setCurrentItem(selectedPageIndex);
                        }
                    }
                }
            }
        });
    }

    private void loadDataAndInitializeUI() {
        if (mListCarouselInfo != null && !mListCarouselInfo.isEmpty()) {
            initUI();
        } else {
            loadCarouselInfo();
        }
        List<String> subscribedCardDataPackages = ApplicationController.getCardDataPackages();
        if ((subscribedCardDataPackages == null
                || subscribedCardDataPackages.isEmpty())
                && Util.checkUserLoginStatus()) {
            LoggerD.debugDownload("empty subscribed packages " + subscribedCardDataPackages);
            //fetchMyPackages();
        }
        HungamaPartnerHandler.getInstance(this).checkDownloadsProgress();
        if(getIntent() != null && getIntent().hasExtra("isFromSplash") && getIntent().getBooleanExtra("isFromSplash", false))
            checkAppVersionUpgrade();
    }

    private void fetchMyPackages() {
        //Content list call
        SDKLogger.debug("fetchMyPackages");
        Log.e(getClass().getSimpleName(), "MyPackages Called in MainActivity");
        RequestMySubscribedPacks mRequestRequestContentList = new RequestMySubscribedPacks(new APICallback<MySubscribedPacksResponseData>() {
            @Override
            public void onResponse(APIResponse<MySubscribedPacksResponseData> response) {
                if (response == null || response.body() == null || response.body().results == null) {
                    ApplicationController.ENABLE_RUPEE_SYMBOL = false;
                    return;
                }
                ApplicationController.ENABLE_RUPEE_SYMBOL = true;
                ApplicationController.setSubscribedPackages(response.body().results);
                if (isToReloadData) {
                    reloadData();
                    refreshNavigationDrawer();
                }
                //Log.d(TAG, "fetchMyPackages: onResponse: size - " + response.body().results.size());
            }

            @Override
            public void onFailure(Throwable t, int errorCode) {
                ApplicationController.ENABLE_RUPEE_SYMBOL = false;
                //Log.d(TAG, "fetchMyPackages: onResponse: t- " + t);
            }
        });
        //  APIService.getInstance().execute(mRequestRequestContentList);
    }

    @Override
    public void overlayFragment(BaseFragment fragment) {
        if (isFinishing() || fragment == null) {
            SDKLogger.debug("isFinishing " + isFinishing() + "or fragment is null " + fragment);
            return;
        }

        try {
            fragment.setContext(this);
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.add(R.id.fragment_related_vodlist_or_carousel_view_all, fragment);
            mCurrentFragment = fragment;

            transaction.addToBackStack(null);
            transaction.commitAllowingStateLoss();

            mFragmentStack.push(fragment);
            mCurrentFragment.setBaseActivity(this);
            mCurrentFragment.setContext(this);
        } catch (Throwable e) {
            e.printStackTrace();

        }
    }

    private void setUpToolBarMenu() {
        if (isNavigation) {
            mToolbar.inflateMenu(R.menu.main_nav_settings);
        } else {
            mToolbar.inflateMenu(R.menu.main_settings);
        }
        mMenu = mToolbar.getMenu();
        MenuItem searchItem = mMenu.findItem(R.id.action_search);
        searchItem.setVisible(false);
        notifyMenuItem = mMenu.findItem(R.id.action_notify);
        if (!isNavigation) {
            MenuItem settingsItem = mMenu.findItem(R.id.action_settings);
            MenuItem profileItem = mMenu.findItem(R.id.action_profile);
            MenuItem launchPlayerItem = mMenu.findItem(R.id.action_launch_player);
            MenuItem mypacksItem = mMenu.findItem(R.id.action_my_packs);

            settingsItem.setVisible(false);
            searchItem.setVisible(false);
            profileItem.setVisible(false);
            launchPlayerItem.setVisible(false);
            if (!PrefUtils.getInstance().getPrefEnableMyPackScreen()) {
                mypacksItem.setVisible(false);
            }
        } //TODO: Commented for hide the menu items

        mMediaRouteMenuItem = mMenu.findItem(R.id.media_route_menu_item);
        MenuItemCompat.setActionProvider(mMediaRouteMenuItem, new MediaRouteActionProvider(new ContextThemeWrapper(this, R.style.CastVideosTheme)));
        mMediaRouteMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        loadNotification();
        checkAndEnableChromeCast();

        for (int i = 0; i < mMenu.size(); i++) {
            MenuItem item = mMenu.getItem(i);
            SpannableString s = new SpannableString(item.getTitle());
            s.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.white_100)), 0, s.length(), 0);
            item.setTitle(s);
        }

        if (searchItem != null) {
            mSearchView = (SearchView) MenuItemCompat.getActionView(searchItem);
            if (Build.VERSION.SDK_INT >= 21) {
                mSearchView.setNestedScrollingEnabled(false);
            }
            mSearchView.setMaxWidth(Integer.MAX_VALUE);
        }
        int searchImgId = androidx.appcompat.R.id.search_button; // I used the explicit layout ID of searchview's ImageView
        ImageView v = (ImageView) mSearchView.findViewById(searchImgId);
        v.setImageResource(R.drawable.actionbar_search_icon);
        mToolbar.hideOverflowMenu();
        //mSearchView.requestFocus();
        CarouselInfoData carouselInfoData = mListCarouselInfo.get(mCurrentSelectedPagePosition);
        mSearchText = mSearchView.findViewById(R.id.search_src_text);
//        setSearchViewHint(sourceString);
//        editText.requestFocus();
        if (carouselInfoData != null)
            setSearchViewHint(carouselInfoData.showAll);

        invalidateOptionsMenu();
        setUpSearchView();
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                setToolBarLogoAndTitle(getResources().getString(R.string.app_name));
                Analytics.mixpanelIncrementPeopleProperty(Analytics.MIXPANEL_PEOPLE_MENU_USED);
                Intent i = new Intent(mContext, LiveScoreWebView.class);
                switch (item.getItemId()) {

//                    case R.id.action_download:s
//                        pushFragment(new FragmentDownloadedMovies());
//                        return true;

                    case R.id.action_notify:
                        Intent intent = new Intent(getApplicationContext(), NotificationActivityNew.class);
                        startActivityForResult(intent, APIConstants.NOTIFICATION_REQUEST);
                        return true;
                   /* case R.id.action_chrome_cast:
                       initGoogleChromeCast();
                        return true;*/
                    case R.id.action_profile:
//                        Intent profileIntent = new Intent(mContext, NotificationDebugActivity.class);
//                        mContext.startActivity(profileIntent);
                        startActivityForResult(ProfileActivity.createIntent(mContext), ProfileActivity.edit_profile_code);
                        return true; //TODO: Commented for Hide sundirect LOGO FROM TOOLBAR

                    case R.id.action_settings:
//                        i = new Intent(mContext, SettingsActivity.class);
//                        mContext.startActivity(i);
//                        return true;
                        final CharSequence quality[] = new CharSequence[]{
                                "Auto",
                                "VeryHigh",
                                "High",
                                "Low"};
                        // Sets dialog for popup dialog list
                        // AlertDialog dialog;
                        ListAdapter itemList = new ArrayAdapter(mContext, R.layout.alert_network_type, quality);

                        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AppCompatAlertDialogStyle);

                        //builder.setTitle("Choose Stream Type");
                        LayoutInflater inflater = LayoutInflater.from(mContext);
                        final View dialogView = inflater.inflate(R.layout.playback_layout, null);
                        ListView playbackList = (ListView) dialogView.findViewById(R.id.playback_listView);
                        playbackList.setAdapter(itemList);


                        builder.setView(dialogView);
                        final AlertDialog alertDialog = builder.create();

//                        alertDialog.setCancelable(false);
                        alertDialog.show();
                        playbackList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                String qualityType = quality[position].toString();
                                if (position != 0)
                                    PrefUtils.getInstance().setPrefPlayBackQuality(qualityType);
                                alertDialog.cancel();
                            }
                        });
                        // dialog = builder.create();
                        //dialog.show();
                        return true; //TODO: COMMENTED FOR HIDE sundirect LOGO FROM TOOLBAR

                    case R.id.action_feedback:
                        CardData profileData = new CardData();
                        profileData._id = "0";
                        FeedBackDialog feedBackDialog = new FeedBackDialog(mContext);
                        feedBackDialog.showDialog(profileData, new FeedBackDialog.MessagePostCallback() {
                            @Override
                            public void sendMessage(boolean status) {
                                if (status) {
                                    AlertDialogUtil.showToastNotification("Thanks for your feedback.");
                                } else {
                                    String message = "Unable to post your review.";
                                    if (ConnectivityUtil.isConnected(mContext) && !Util.checkUserLoginStatus()) {
                                        message = "Please register to share your feedback.";
                                    }
                                    AlertDialogUtil.showToastNotification(message);
                                }
                            }
                        });
                        return true; //TODO: Commented for Hide sundirect LOGO FROM TOOLBAR

                    case R.id.action_termsncond:
                        i.putExtra("url", APIConstants.getFAQURL());
                        mContext.startActivity(i);
                        return true; //TODO: Commented for Hide sundirect LOGO FROM TOOLBAR

                    case R.id.action_launch_player:
                        String contentid = "18261833";
                        String title = "Mastizaade";
                        String description = "";
                        String tileurl = "";
                        CardData movieData = new CardData();
                        movieData.generalInfo = new CardDataGeneralInfo();
                        movieData.generalInfo.partnerId = contentid;
                        movieData.generalInfo.title = title;
                        HungamaPartnerHandler.launchDetailsPage(movieData, mContext, null, null);
                        return true; //TODO: Commented for Hide sundirect LOGO FROM TOOLBAR

                    case R.id.action_help:
                        i.putExtra("url", APIConstants.getHelpURL());
                        mContext.startActivity(i);
                        AppsFlyerTracker.eventBrowseHelp();
                        return true; //TODO: Commented for Hide sundirect LOGO FROM TOOLBAR

                    case R.id.action_filter:
                        //TODO: show new Filter popup
                        addFilterFragment();
                        // showNewFilterMenuPopUp(mFilterLayout);
                        return true;

                    case R.id.action_about:
                        showAboutDialog();
                        return true; //TODO: Commented for Hide sundirect LOGO FROM TOOLBAR

                    case R.id.action_my_packs:
                        CleverTap.eventPageViewed(CleverTap.PAGE_MYPACKS);
                        Intent mySubscribedPacksIntent = new Intent(mContext, ActivityMyPacks.class);
                        mContext.startActivity(mySubscribedPacksIntent);
                        return true; //TODO: Commented for Hide sundirect LOGO FROM TOOLBAR
                    default:
                        Intent settingsIntent = new Intent(mContext, SettingsActivity.class);
                        mContext.startActivity(settingsIntent);
                        //  fetchFilterData(mToolbar);
                        return true;
                }
            }
        });
    }

    public void removeFilterFragment() {
        if (mFilterFragment != null) {
            removeFragment(mFilterFragment);
        }
    }

    private Drawable buildCounterDrawable(int count, int backgroundImageId) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.notification_unread_layout, null);
        view.setBackgroundResource(backgroundImageId);

        if (count == 0) {
            RelativeLayout counterTextPanel = view.findViewById(R.id.container);
            counterTextPanel.setVisibility(View.GONE);
        } else {
            TextView textView = (TextView) view.findViewById(R.id.count_text);
            if(count > 20)
                count = 20;
            textView.setText(String.valueOf(count));
        }
 if(!DeviceUtils.isTablet(mContext)) {
     view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
             View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
     view.layout(-5, 0, view.getMeasuredWidth()+15, view.getMeasuredHeight()-15);
 }else{
     view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
             View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
     view.layout(-20, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
 }
        view.setDrawingCacheEnabled(true);
        view.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache());
        view.setDrawingCacheEnabled(false);


        return new BitmapDrawable(getResources(), bitmap);
    }

    public void addFilterFragment() {
        if (mFilterFragment != null && mFilterFragment.isAdded()) {
            return;
        }
        if (mListCarouselInfo != null) {
            CarouselInfoData carouselInfoData = mListCarouselInfo.get(mCurrentSelectedPagePosition);
            CleverTap.eventClicked(carouselInfoData.title, CleverTap.ACTION_FILTER);
        }
        try {
            toggle.setHomeAsUpIndicator(R.drawable.back_icon);
            toggle.setDrawerIndicatorEnabled(false);
            toggle.setToolbarNavigationClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Log.i(TAG, "setOnCloseListener: onClose");
                    removeFilterFragment();
                }
            });
            disableNavigation();
            setFilterIcon(R.drawable.actionbar_filter_icon_highlighted_icon);
        } catch (Exception e) {
            e.printStackTrace();
        }
        CleverTap.eventPageViewed(CleverTap.PAGE_FILTER);
        if (mFilterFragment == null)
            mFilterFragment = new FilterFragment().newInstance(mListCarouselInfo.get(mCurrentSelectedPagePosition), mSectionType);
        pushFragment(mFilterFragment);
        disableNavigation();
    }


    private void showAboutDialog() {
        AboutDialogWebView aboutDialogWebView = new AboutDialogWebView(mContext);
        aboutDialogWebView.showDialog();
    }

    private void setSearchViewHint(final String sourceString) {
        if (mSearchText == null) return;
        LoggerD.debugLog("setSearchViewHint sourceString- " + sourceString);
        mSearchText.post(new Runnable() {
            @Override
            public void run() {
                String text = sourceString;
                if (text == null || TextUtils.isEmpty(text)) {
                    text = "Enter the text to search";
                }
                String sourceString1 = "<i>" + text + "</i>";
                mSearchText.setTextColor(getResources().getColor(R.color.navigation_drawer_color_text));
                mSearchText.setHintTextColor(getResources().getColor(R.color.navigation_drawer_color_text));
                mSearchText.setHint(Html.fromHtml(text));
                mSearchText.setHintTextColor(getResources().getColor(R.color.navigation_drawer_color_text));
                Typeface myCustomFont = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Regular.ttf");
                mSearchText.setTypeface(myCustomFont);
            }
        });
    }

    public void setTextQuery(String query) {
        if (mSearchView != null) {
            mSearchView.setQuery(query, true);

        }

    }

    public void removeFragment(BaseFragment fragment) {
        try {
            if (isFinishing() || fragment == null) {
                SDKLogger.debug("isFinishing " + isFinishing() + "or fragment is null " + fragment);
                return;
            }
            if (fragment instanceof FilterFragment) {
                setAllowScrollBar(true);
//                if (mAppBar.isShown()) {
//                    mAppBar.setExpanded(false);
//                }
                setFilterIcon(R.drawable.actionbar_filter_icon_default);
                mFilterFragment = null;
                toggle.setDrawerIndicatorEnabled(true);
                enableNavigation();
            }
            //Log.i(TAG, "remove " + fragment);
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
        /*if (findViewById(fragment.getId()) != null) {
            SDKLogger.debug("fragment.getId()- " + fragment.getId() + " tag- " + fragment.getTag());
//            findViewById(fragment.getId()).setVisibility(GONE);
        } else {
            SDKLogger.debug("fragment.getId()- is invalid or fragment is null");
        }*/
            transaction.remove(fragment);
            transaction.commitAllowingStateLoss();
//            checkFragmentVODINstancesPresent();
            if (mFragmentStack.size() == 1) {
                mFragmentStack.pop();
                mCurrentFragment = null;
                minizePlayerAboveTabPageIndicator();
                showTabIndicator();
                if(mDraggablePanel != null)
                    mDraggablePanel.minimize();
            } else if (mFragmentStack.size() > 1) {
                mFragmentStack.pop();
                mCurrentFragment = mFragmentStack.peek();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        if (mDraggablePanel != null && mDraggablePanel.getVisibility() != View.VISIBLE) {
            sendMiniPlayerDisableddBroadCast();
        }

    }

    private boolean checkFragmentVODINstancesPresent() {

        for (BaseFragment item : mFragmentStack) {
            if (!(item instanceof FragmentVODList)) {
                return false;
            }

        }
        while (mFragmentStack.size() > 1) {
            mFragmentStack.pop();
        }


        return true;

    }

    private SearchSuggestions mSearchSuggestionFrag;

    private boolean canShowSearchWithFilter;
    private SearchSuggestionsWithFilter mSearchSuggestionsWithFilterFrag;

    private void doSearch(String query) {
//        showActionBarProgressBar();
        final List<CardData> searchString = new ArrayList<>();
        CardData temp = new CardData();
        temp._id = query;
        searchString.add(temp);

    }

    public boolean HideSearchView() {
        try {
            if (mSearchView != null && !mSearchView.isIconified()) {
                mSearchView.setQuery("", false);
                mSearchView.setIconified(true);
                //AddAnalytics
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void setUpSearchView() {

        if (mSearchView == null) {
            return;
        }
        new Handler().post(new Runnable() {
            @Override
            public void run() {
//                 Get the SearchView and set the searchable configuration
// Assumes current activity is the searchable activity
                try {
                    SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
                    if (searchManager != null)
                        mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                mSearchView.setFocusable(true);
                mSearchView.setSubmitButtonEnabled(true);
                mSearchView.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI | EditorInfo.IME_ACTION_SEARCH);
                Field f;

                try {

                    f = SearchView.class.getDeclaredField("mGoButton");
                    f.setAccessible(true);//Very important, this allows the setting to work.
                    mGoButton = (ImageView) f.get(mSearchView);
                    mGoButton.setPadding(16, 0, 16, 0);
                    mGoButton.setImageResource(R.drawable.search_icon);

                    f = SearchView.class.getDeclaredField("mSearchButton");
                    f.setAccessible(true);//Very important, this allows the setting to work.
                    mSearchButton = (ImageView) f.get(mSearchView);
                    f = SearchView.class.getDeclaredField("mCollapsedIcon");
                    f.setAccessible(true);//Very important, this allows the setting to work.
                    mSearchCollapsedIcon = (ImageView) f.get(mSearchView);
                    f = SearchView.class.getDeclaredField("mCloseButton");
                    f.setAccessible(true);//Very important, this allows the setting to work.
                    mSearchCloseButton = (ImageView) f.get(mSearchView);
                    showMicButton(false);
                    TextView searchText = (TextView)
                            mSearchView.findViewById(androidx.appcompat.R.id.search_src_text);
                    Typeface myCustomFont = Typeface.createFromAsset(getAssets(), "fonts/amazon_ember_cd_regular.ttf");
                    searchText.setTypeface(myCustomFont);
                    searchText.setPrivateImeOptions("");
                    mGoButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            showOnlySearchView(true);
                            String query = null;
                            if (mSearchView != null) {
                                query = "" + mSearchView.getQuery();
                                Log.d("EntireQuery", query);
                                LoggerD.debugLog("showSearchFragment from mGoButton.setOnClickListener query- " + query);
                                showSearchFragment(query, true);
                            }

                            if (query == null || query.length() == 0) return;
                            hideSoftInputKeyBoard(mSearchView);
                        }
                    });

                    f = SearchView.class.getDeclaredField("mVoiceButton");
                    f.setAccessible(true);//Very important, this allows the setting to work.
                    mVoiceButton = (ImageView) f.get(mSearchView);
                    mVoiceButton.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View view, MotionEvent motionEvent) {
                            isVoiceButtonClicked = true;
                            LoggerD.debugLog("search view: voiceButtonClicked");
                            return false;
                        }
                    });
                    mSearchText.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View view, MotionEvent motionEvent) {
                            LoggerD.debugLog("search view: search text Clicked");
                            return false;
                        }
                    });
//                    TODO Hide search go btn by default never should work
                    if (mGoButton != null) {
                        mGoButton.setEnabled(false);//  mSearchCloseButton.setEnabled(false);
                        mGoButton.setImageDrawable(getResources().getDrawable(R.drawable.transparent));
                    }
                    if (mSearchCloseButton != null) {
                        mSearchCloseButton.setEnabled(true);
                        mSearchCloseButton.setImageDrawable(getResources().getDrawable(R.drawable.search_cancel_icon));
                    }

/*
                    int searchFrameId = mSearchView.getContext().getResources().getIdentifier("android:id/search_edit_frame", null, null);
                    View searchFrame = mSearchView.findViewById(searchFrameId);
                    searchFrame.setBackgroundResource(R.drawable.searchview_background);
*/

                    /*int searchPlateId = mSearchView.getContext().getResources().getIdentifier("android:id/search_plate", null, null);
                    View searchPlate = findViewById(searchPlateId);
                    searchPlate.setBackgroundResource(R.drawable.searchview_background);

                    */

                    /*int searchBarId = mSearchView.getContext().getResources().getIdentifier("android:id/search_bar", null, null);
                    View searchBar = findViewById(searchBarId);
                    searchBar.setBackgroundResource(R.drawable.searchview_background);*/


                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        });


        mSearchView.setOnSearchClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //Log.i(TAG, "setOnSearchClickListener: onClick");
                showOnlySearchView(true);
                if (mSearchView != null) {
                    mSearchView.requestFocus();

                    String query = "" + mSearchView.getQuery();


                    showSearViewSearchButton(false);
                    toggleSearchView(query);
                    showSearchFragment(query, true);
                    if (appLogo != null)
                        appLogo.setVisibility(GONE);
                    removeFilterFragment();
                    toggle.setDrawerIndicatorEnabled(false);
                    toggle.setHomeAsUpIndicator(R.drawable.back_icon);
                    showCloseButton(true);
                    toggle.setToolbarNavigationClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            //Log.i(TAG, "setOnCloseListener: onClose");
                            removeSearchFragment();
                            showOnlySearchView(false);
                            if (appLogo != null)
                                appLogo.setVisibility(View.VISIBLE);
                            toggle.setDrawerIndicatorEnabled(true);
                            enableNavigation();
                        }
                    });
                    disableNavigation();
                    LoggerD.debugLog("showSearchFragment from mSearchView.setOnSearchClickListener query- " + query);

//                    TODO show Only mic button
                }
            }
        });

        mSearchView.setOnCloseListener(new SearchView.OnCloseListener() {

            @Override
            public boolean onClose() {
                //Log.i(TAG, "setOnCloseListener: onClose");
                removeSearchFragment();
                showOnlySearchView(false);
                if (appLogo != null)
                    appLogo.setVisibility(View.VISIBLE);
                toggle.setDrawerIndicatorEnabled(true);
                enableNavigation();
                return false;
            }
        });


        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                //Log.d(TAG, "onSearchClick " + query);
//                HideSearchView();
                doSearch(query);

       /*         String supportSearch = String.join(",", query);

                Log.d("RECENT::::", supportSearch);*/

                String storedQuery = PrefUtils.getInstance().getLastSearchQuery();
                Log.d("Stored Query:::", mSearchQuery + ",");

                String entireQuery = storedQuery + query;

                SharedPreferences upDatedQuery = getApplicationContext().getSharedPreferences("MySearchPref", MODE_PRIVATE);
                SharedPreferences.Editor editor = upDatedQuery.edit();
                editor.putString("SearchQuery", entireQuery + ",");
                editor.commit();


                if (mSearchView != null) {
                    query = "" + mSearchView.getQuery();


                    toggleSearchView(query);
                    isVoiceButtonClicked = false;
                    LoggerD.debugLog("showSearchFragment from mSearchView.onQueryTextSubmit query- " + query);
                    showSearchFragment(query, true);
                    if (mListCarouselInfo != null
                            && !mListCarouselInfo.isEmpty()
                            && mListCarouselInfo.get(mCurrentSelectedPagePosition) != null) {
                        CleverTap.eventSearched(query, mListCarouselInfo.get(mCurrentSelectedPagePosition).shortDesc, Analytics.NO);
                    }

                }

                if (query == null || query.length() == 0) return true;
                hideSoftInputKeyBoard(mSearchView);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
//                if (TextUtils.isEmpty(newText)) {
//                    return false;
//                }

                changeFrequency++;
                mSearchQuery = newText;


                //if (changeFrequency <= 1) {
                if (searchHandler != null && searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }/*
                searchHandler = new Handler();
                searchRunnable = new Runnable() {
                    @Override
                    public void run() {


                        //changeFrequency = 0;
                        LoggerD.debugLog("showSearchFragment from mSearchView.onQueryTextChange newText- " + mSearchQuery);
                        searchedQuery = mSearchQuery;
                        if ((mSearchSuggestionFrag != null && mSearchSuggestionFrag.isAdded()) || (mSearchSuggestionsWithFilterFrag != null && mSearchSuggestionsWithFilterFrag.isAdded()))
                            //if ((mSearchSuggestionFrag != null && mCurrentFragment != null && mCurrentFragment instanceof SearchSuggestions) || (mSearchSuggestionsWithFilterFrag != null && mCurrentFragment != null && mCurrentFragment instanceof SearchSuggestionsWithFilter))
                            showSearchFragment(mSearchQuery, true);

                    }
                };
                searchHandler.postDelayed(searchRunnable, SEARCH_TIMER_DELAY);*/
                if (SEARCH_TIMER_DELAY == 0) {
                    showSearchFragment(mSearchQuery, true);
                } else {
                    if (timer != null) {
                        stoptimertask();
                        startTimer();
                    } else {
                        startTimer();
                    }
                }

                isVoiceButtonClicked = false;
                toggleSearchView(newText);
                LoggerD.debugLog("showSearchFragment from mSearchView.onQueryTextChange newText- " + newText);
                /* showSearchFragment(newText, true);*/
                //if (mSearchSuggestionFrag != null && TextUtils.isEmpty(newText)) {
                if (canShowSearchWithFilter) {
                    if (mSearchSuggestionsWithFilterFrag != null && TextUtils.isEmpty(newText)) {
                        mSearchSuggestionsWithFilterFrag.clear();
                    }
                } else if (mSearchSuggestionFrag != null && TextUtils.isEmpty(newText)) {
                    mSearchSuggestionFrag.clear();
                }
                return false;
            }
        });


        mSearchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, final boolean hasFocus) {
                if (mSearchView == null) {
                    return;
                }
                if (isSearchviewFocusAllowed) {
                    return;
                }
                isSearchviewFocusAllowed = true;
                //added delay to avoid multiple focus change on touch action down and up
                view.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        isSearchviewFocusAllowed = false;
                    }
                }, 100);
                hideSoftInputKeyBoard(mSearchView);
            }
        });
    }

    Handler searchHandler;
    Runnable searchRunnable;

    void hideSoftInputKeyBoard(View view) {
        if (isFinishing()) return;
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && view != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void showSearchFragment(String query, boolean isBrowseMore) {
        CarouselInfoData carouselInfoData;
        if (mListCarouselInfo == null || mListCarouselInfo.isEmpty()) {
            return;
        }
        if (this.carouselInfoData == null) {
            int positionClicked = getPositionInCarousalWhenClickedBottomTab(mCurrentSelectedPagePositionTitle);
            carouselInfoData = mListCarouselInfo.get(positionClicked);
        } else
            carouselInfoData = this.carouselInfoData;

        //Log.i(TAG, "showSearchFragment: onClick type- " + carouselInfoData.shortDesc == null ? "NA" : carouselInfoData.shortDesc + " query- " + query);

        if (carouselInfoData == null
                || TextUtils.isEmpty(carouselInfoData.shortDesc)) {
            //Log.i(TAG, "showSearchFragment: onClick type");
            return;
        }

        //Addanalytics just record textchanges
        if (canShowSearchWithFilter) {
            if (mSearchSuggestionsWithFilterFrag == null) {
                Bundle args = new Bundle();
                args.putString(SearchSuggestionsWithFilter.PARAM_SEARCH_QUERY, query);
                args.putBoolean(SearchSuggestionsWithFilter.PARAM_SEARCH_ALLOW_BROWSE_MORE, isBrowseMore);
                args.putString(SearchSuggestionsWithFilter.PARAM_SEARCH_CONTENT_TYPE, carouselInfoData.shortDesc);
                args.putString(SearchSuggestionsWithFilter.PARAM_TAB_NAME, carouselInfoData.title);
                mSearchSuggestionsWithFilterFrag = SearchSuggestionsWithFilter.newInsance(args);
                mSearchSuggestionsWithFilterFrag.setContext(mContext);
                setAllowScrollBar(false);
                showAppBar();
                minimizePlayerAtTabPageIndicator();
                hideTabIndicator();
                pushFragment(mSearchSuggestionsWithFilterFrag);
                CleverTap.eventSearched(null, carouselInfoData.shortDesc, Analytics.NO);
            }
        } else if (mSearchSuggestionFrag == null) {
            Bundle args = new Bundle();
            args.putString(SearchSuggestions.PARAM_SEARCH_QUERY, query);
            args.putBoolean(SearchSuggestions.PARAM_SEARCH_ALLOW_BROWSE_MORE, isBrowseMore);
            args.putString(SearchSuggestions.PARAM_SEARCH_CONTENT_TYPE, carouselInfoData.shortDesc);
            args.putString(SearchSuggestions.PARAM_TAB_NAME, carouselInfoData.title);
            mSearchSuggestionFrag = SearchSuggestions.newInsance(args);
            mSearchSuggestionFrag.setContext(mContext);
            setAllowScrollBar(false);
            showAppBar();
            minimizePlayerAtTabPageIndicator();
            hideTabIndicator();
            pushFragment(mSearchSuggestionFrag);
            CleverTap.eventSearched(null, carouselInfoData.shortDesc, Analytics.NO);
        }
        if (canShowSearchWithFilter) {
            if (mSearchSuggestionsWithFilterFrag != null && query.length() > 0) {
                //  mSearchSuggestionsWithFilterFrag.setQuery(query, carouselInfoData.shortDesc, isBrowseMore, carouselInfoData.title);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    mSearchSuggestionsWithFilterFrag.setQuery(query, carouselInfoData.shortDesc, isBrowseMore, carouselInfoData.title);
                }
            } else {
                mSearchSuggestionsWithFilterFrag.clear();
            }
        } else {
            if (mSearchSuggestionFrag != null && query.length() > 0) {
                mSearchSuggestionFrag.setQuery(query, carouselInfoData.shortDesc, isBrowseMore, carouselInfoData.title);
            } else if (mSearchSuggestionFrag != null) {
                mSearchSuggestionFrag.clear();
            }
        }


    }


    private void showOnlySearchView(boolean value) {
        if (mMenu == null) {
            return;
        }

        //Log.d(TAG, "showOnlySearchView: value- " + value + " mSectionType- " + mSectionType);
        MenuItem filterMenuItem = mMenu.findItem(R.id.action_filter);
        if (value) {
            mMediaRouteMenuItem.setVisible(false);
            filterMenuItem.setVisible(false);
            return;
        }

        mMediaRouteMenuItem.setVisible(true);
        if (mListCarouselInfo == null) {
            return;
        }
        CarouselInfoData carouselInfoData = mListCarouselInfo.get(mCurrentSelectedPagePosition);
        if (carouselInfoData != null && carouselInfoData.enableShowAll) {
            filterMenuItem.setVisible(true);
        } else {
            filterMenuItem.setVisible(false);
        }
    }

    @Override
    public void setOrientation(int REQUEST_ORIENTATION) {
        setRequestedOrientation(REQUEST_ORIENTATION);
    }

    @Override
    public int getOrientation() {
        return getRequestedOrientation();
    }

    @Override
    public void hideActionBar() {
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
    }

    @Override
    public void showActionBar() {
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.show();
        }
    }

    private int exitBackPressCounts = 1;
    private final int EXIT_ON_BACK_COUNT = 1;
    private final int RESET_BACKPRESS_TIMER = 5 * 1000;
    private boolean mShowExitToast = true;

    private boolean closeApplication() {
        if (IS_SeasonUIForBack) {
            IS_SeasonUIForBack = false;
            return false;

        }
        if (mShowExitToast) {
            exitBackPressCounts++;
            //Log.d(TAG, "back press count " + exitBackPressCounts);
            if (exitBackPressCounts > EXIT_ON_BACK_COUNT) {
                //Log.d(TAG, "back press count reached to max " + exitBackPressCounts);
                AlertDialogUtil.showToastNotification("Press back again to close the application.");
                mShowExitToast = false;
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {

                    @Override
                    public void run() {
                        mShowExitToast = true;
                        exitBackPressCounts = 0;
                        //Log.d(TAG, "timer reset back press count " + exitBackPressCounts);
                    }
                }, RESET_BACKPRESS_TIMER);
            }
            return false;
        } else {
            return true;
        }
    }

    private void exitApp() {
        Process.killProcess(Process.myPid());
        System.runFinalizersOnExit(true);
        System.exit(0);
        finish();
    }

    public boolean isOpenDrawer(){
        if (mDrawerLayout != null && mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
           return true;
        }
        return false;
    }
    @Override
    public void onBackPressed() {
        try {
            //Log.d(TAG, "onBackPressed");
            if (mDraggablePanel != null && mDraggablePanel.getVisibility() != View.VISIBLE) {
                IsScrolled1st = true;
                sendMiniPlayerDisableddBroadCast();
            }

            if (canShowSearchWithFilter) {
                if (mSearchSuggestionsWithFilterFrag != null) {
                    removeSearchFragment();
                    //TODO: show if kidsFragment is shown
                    if (selectedMenuFragment != null && selectedMenuFragment.isHidden()) {
                        FragmentManager fm = getSupportFragmentManager();
                        fm.beginTransaction()
                                .show(selectedMenuFragment)
                                .commit();
                    }
                    return;
                }
            } else {
                if (mSearchSuggestionFrag != null) {
                    removeSearchFragment();
                    //TODO: show if kidsFragment is shown
                    if (selectedMenuFragment != null && selectedMenuFragment.isHidden()) {
                        FragmentManager fm = getSupportFragmentManager();
                        fm.beginTransaction()
                                .show(selectedMenuFragment)
                                .commit();
                    }
                    return;
                }
            }

            if (mDrawerLayout != null && mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                mDrawerLayout.closeDrawer(GravityCompat.START);
            }


            isPushedFragment = false;

            if ((mCurrentFragment==null || mCurrentFragment instanceof FragmentLanguageCarouselInfo) && mDraggablePanel != null && mDraggablePanel.getVisibility() == View.VISIBLE
                    && mFragmentCardDetailsDescription != null
                    && mFragmentCardDetailsPlayer != null
                    && mFragmentCardDetailsPlayer.onBackClicked()) {
                if (mDraggablePanel.draggableView != null && mDraggablePanel.draggableView.
                        checkMinimized()) {
                    closePlayerFragment();
                }else{
                    mFragmentCardDetailsPlayer.minimizePlayer();
                }
                enableNavigation();
                if (!DeviceUtils.isTabletOrientationEnabled(mContext)) {

                    if (getScreenOrientation() == SCREEN_ORIENTATION_SENSOR_LANDSCAPE) {
                        ((BaseActivity) mContext).setOrientation(SCREEN_ORIENTATION_SENSOR_PORTRAIT);
                    }
                }else{
                    ((BaseActivity) mContext).setOrientation(SCREEN_ORIENTATION_USER);
                }
                return;
            }
            if (mCurrentFragment != null && !mCurrentFragment.onBackClicked()) {
                updateNavigationBarAndToolbar();
                if (mCurrentFragment instanceof FilterFragment) {
                    removeFilterFragment();
                    return;
                }
                if (mCurrentFragment instanceof FragmentLanguageCarouselInfo) {
                    PrefUtils.getInstance().setAppLanguageToShow("");
                    myplexAPISDK.ENABLE_FORCE_CACHE = true;
                    MenuDataModel.clearCache();
                    myplexAPI.clearCache(APIConstants.BASE_URL);
                }
                removeFragment(mCurrentFragment);
                //Added fix for doc player issue in tab when click on device back button from see all page
               /* if(DeviceUtils.isTablet(mContext) && APIConstants.IS_BACK_FROM_FRAGMENT_VOD_LIST){
                    minimizePlayerAtTabPageIndicator();
                    updateBottomBar(false,2);
                }
                APIConstants.IS_BACK_FROM_FRAGMENT_VOD_LIST=true;*/
                return;
            }
            if (closeApplication()) {
                super.onBackPressed();
//                PlayUtils.prepareForExit(this);
                //Log.d(TAG, "exiting App");
                ApplicationController.pageVisiblePos = 0;
                ApplicationController.pageItemPos = 0;
                EPG.globalPageIndex = 1;
                EPG.genreFilterValues = "";
                EPG.langFilterValues = "";
//                exitApp();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     * Fetching the list data
     */
    private void fetchFilterData() {
        String contentType = APIConstants.TYPE_LIVE;
        CarouselInfoData carouselInfoData = mListCarouselInfo.get(mCurrentSelectedPagePosition);
        if (carouselInfoData == null) return;
        if (carouselInfoData != null && !TextUtils.isEmpty(carouselInfoData.shortDesc)) {
            contentType = carouselInfoData.shortDesc;
        }
        if (carouselInfoData.cachedFilterResponse != null) {
            parseFilterResponseData(carouselInfoData.cachedFilterResponse);
            return;
        }
        FilterRequest.Params requestParams = new FilterRequest.Params(contentType);
        final FilterRequest request = new FilterRequest(requestParams, new APICallback<GenreFilterData>() {
            @Override
            public void onResponse(APIResponse<GenreFilterData> response) {
                if (null == response.body() || response.body().results == null) {
                    closeFilterMenuPopup();
                    if (mToolbar != null) {
                        mToolbar.setVisibility(View.VISIBLE);
                    }
                    return;
                }

                try {
                    CarouselInfoData carouselInfoData = mListCarouselInfo.get(mCurrentSelectedPagePosition);
                    carouselInfoData.cachedFilterResponse = response.body();
                    parseFilterResponseData(response.body());
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(Throwable t, int errorCode) {
                closeFilterMenuPopup();
                if (mToolbar != null) {
                    mToolbar.setVisibility(View.VISIBLE);
                }
            }
        });

        APIService.getInstance().execute(request);
    }

    private void parseFilterResponseData(GenreFilterData body) {
        List<FilterItem> groupGenres = new ArrayList<>();
        List<FilterItem> groupLanguages = new ArrayList<>();

        boolean isFilterAvailable = checkIsFilterSelected(body, groupLanguages, groupGenres);
       /* int checkCnt =0;
        for(int i =0;i<groupGenres.size();i++){
            if(groupGenres.get(i).isChecked()){
                checkCnt++;
            }
        }*/
        /*if(checkCnt == groupGenres.size()){
            filterItem.setIsChecked(true);
            filterItem.setTitle("All");
            groupGenres.add(0, filterItem);
        }else {
            filterItem.setIsChecked(false);
            filterItem.setTitle("All");
            groupGenres.add(0, filterItem);
        }*/
        //TODO: change here
        if (groupLanguages == null || groupGenres == null) {
            Log.e("lang or gener", "null");
            return;
        }
        if (mFilterMenuPopup != null)
            //((FilterView) mFilterMenuPopup).setData(groupLanguages, groupGenres);
            setData(groupLanguages, groupGenres);
        else
            Log.e("popupmenu", "isEmpty");


    }

    private boolean checkIsFilterSelected(GenreFilterData body, List<FilterItem> groupLanguages, List<FilterItem> groupGenres) {
        if (body == null || body.results == null) {
            LoggerD.debugLog("GenreFilterData == null");
            closeFilterMenuPopup();
            return false;
        }

        Languages languages = body.results.languages;
        List<Terms> languagesList = null;
        if (languages != null) {
            languagesList = languages.terms;
        }
        GenresData genresData = body.results.genres;
        List<Terms> genresDataList = null;
        if (genresData != null) {
            genresDataList = genresData.terms;
        }
        if (languagesList == null || genresDataList == null) {
            System.out.println("filter null");
            closeFilterMenuPopup();
            return false;
        }
        if (languagesList.size() == 0 && genresDataList.size() == 0) {
            System.out.println("filter empty");
            closeFilterMenuPopup();
            return false;
        }
        CarouselInfoData carouselInfoData = mListCarouselInfo.get(mCurrentSelectedPagePosition);
        if (carouselInfoData.filteredData == null) {
            carouselInfoData.filteredData = new HashMap<>();
        }
        HashMap<Integer, ArrayList<String>> alreadyFilteredMap = carouselInfoData.filteredData;
        int genreKey = 0;
        int languageKey = 1;
        if (mSectionType == SECTION_MOVIES) {
            genreKey = 1;
            languageKey = 0;
        }
        boolean isFilterAvailable = false;
        if (languagesList != null && alreadyFilteredMap != null) {
            for (int i = 0; i < languagesList.size(); i++) {
                FilterItem filterItem = new FilterItem();
                filterItem.setTitle(languagesList.get(i).term);
                if (alreadyFilteredMap.size() > 0) {
                    if (alreadyFilteredMap.containsKey(languageKey)) {
                        ArrayList<String> genreFilterItems = alreadyFilteredMap.get(languageKey);
                        if (genreFilterItems != null && genreFilterItems.size() > 0) {
                            for (int k = 0; k < genreFilterItems.size(); k++) {
                                if (genreFilterItems.get(k).equals(languagesList.get(i).term)) {
                                    isFilterAvailable = true;
                                    filterItem.setIsChecked(true);
                                    break;
                                } else {
                                    filterItem.setIsChecked(false);
                                }
                            }
                        } else {
                            filterItem.setIsChecked(false);
                        }
                    }
                } else {
                    filterItem.setIsChecked(false);
                }
                groupLanguages.add(filterItem);
            }

        }
      /*  int langCheckCnt =0;
        for(int i =0;i<groupLanguages.size();i++){
            if(groupLanguages.get(i).isChecked()){
                langCheckCnt++;
            }
        }
        FilterItem langFilterItem = new FilterItem();
       *//* if(langCheckCnt == groupLanguages.size()){
            langFilterItem.setIsChecked(true);
            langFilterItem.setTitle("All");
            groupLanguages.add(0, langFilterItem);
        }else {
            langFilterItem.setIsChecked(false);
            langFilterItem.setTitle("All");
            groupLanguages.add(0, langFilterItem);
        }*/

        if (languagesList != null && alreadyFilteredMap != null) {
            for (int i = 0; i < genresDataList.size(); i++) {
                FilterItem filterItem = new FilterItem();
                filterItem.setTitle(genresDataList.get(i).humanReadable);

                if (alreadyFilteredMap.size() > 0) {
                    if (alreadyFilteredMap.containsKey(genreKey)) {
                        ArrayList<String> genreFilterItems = alreadyFilteredMap.get(genreKey);
                        if (genreFilterItems != null && genreFilterItems.size() > 0) {
                            for (int k = 0; k < genreFilterItems.size(); k++) {
                                if (genreFilterItems.get(k).equals(genresDataList.get(i).humanReadable)) {
                                    isFilterAvailable = true;
                                    filterItem.setIsChecked(true);
                                    break;
                                } else {
                                    filterItem.setIsChecked(false);
                                }
                            }
                        } else {
                            filterItem.setIsChecked(false);
                        }
                    }
                } else {
                    filterItem.setIsChecked(false);
                }
                groupGenres.add(filterItem);
            }
        }
        return isFilterAvailable;
    }


    @Override
    protected void onPause() {
        try {
            super.onPause();
            if(alert != null ){
                Log.d("FIAM", "onPause: alert cancel");
                if(callbacks != null)
                    callbacks.messageDismissed(FirebaseInAppMessagingDisplayCallbacks.InAppMessagingDismissType.SWIPE);

                alert.cancel();
            }

//        if (mSectionType != SECTION_OTHER) {
//            if (mSearchSuggestionFrag != null && !isVoiceButtonClicked) {
//                removeSearchFragment();
//            }


            //SDKUtils.saveObject(ApplicationController.getDownloadData(), getApplicationConfig().downloadCardsPath);
//        }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendPauseBroadCast() {
        Log.e("MOUTRACKER_AUTOPLAY", "PAUSE_BROADCAST");
        Intent intent = new Intent(APIConstants.PAUSE_BROADCAST);
        ApplicationController.getLocalBroadcastManager().sendBroadcast(intent);
    }

    private void sendPageChangeListener() {
        Log.e("MOUTRACKER_AUTOPLAY", "PAGE_CHANGE_BROADCAST");
        ApplicationController.FIRST_TAB_NAME = getCurrentTabForVmax();
        Intent intent = new Intent(APIConstants.PAGE_CHANGE_BROADCAST);
        intent.putExtra(APIConstants.TAB_NAME, getCurrentTabForVmax());
        ApplicationController.getLocalBroadcastManager().sendBroadcast(intent);
    }

    private void sendResuemBroadCast() {
        Log.e("MOUTRACKER_AUTOPLAY", "RESUME_BROADCAST");
        Intent intent = new Intent(APIConstants.RESUME_BROADCAST);
        ApplicationController.getLocalBroadcastManager().sendBroadcast(intent);
    }

    public void sendMiniPlayerEnabledBroadCast() {
        Log.e("MOUTRACKER_AUTOPLAY", "MINI_PLAYER_ENABLED_BROADCAST");
        Intent intent = new Intent(APIConstants.MINI_PLAYER_ENABLED_BROADCAST);
        //ApplicationController.isMiniPlayerEnabled = true;
        ApplicationController.getLocalBroadcastManager().sendBroadcast(intent);

    }

    private void sendMiniPlayerDisableddBroadCast() {
        Log.e("MOUTRACKER_AUTOPLAY", "MINI_PLAYER_DISABLED_BROADCAST");
        Intent intent = new Intent(APIConstants.MINI_PLAYER_DISABLED_BROADCAST);
        intent.putExtra(APIConstants.TAB_NAME, getCurrentTabForVmax());
        ApplicationController.isMiniPlayerEnabled = false;
        ApplicationController.getLocalBroadcastManager().sendBroadcast(intent);
        if(liveCardPlayerCallback != null) {
            liveCardPlayerCallback.onDocPlayerClosed();
        }
            if (continueLiveCardPlayerCallback != null) {
                continueLiveCardPlayerCallback.onDocPlayerClosed();
            }
            if (continueVODPlayerCallback != null) {
                continueVODPlayerCallback.onDocPlayerClosed();
            }

    }

    public  void setLiveCardPlayerCallback(LiveCardPlayerCallback liveCardPlayerCallback){
        this.liveCardPlayerCallback = liveCardPlayerCallback;
    }
    public  void setVODCardPlayerCallback(ContinueVODPlayerCallback liveCardPlayerCallback){
        this.continueVODPlayerCallback = liveCardPlayerCallback;
    }
    public  void setContinueLiveCardPlayerCallback(ContinueLiveCardPlayerCallback continueLiveCardPlayerCallback){
        this.continueLiveCardPlayerCallback = continueLiveCardPlayerCallback;
    }
    public void removeSearchFragment() {

        if (mSearchView != null) {
            minizePlayerAboveTabPageIndicator();
            showTabIndicator();
            showOnlySearchView(false);
            //Log.i(TAG, "setOnCloseListener: onClose");
            if (appLogo != null)
                appLogo.setVisibility(View.VISIBLE);
            toggle.setDrawerIndicatorEnabled(true);
            enableNavigation();
            mSearchView.onActionViewCollapsed();
//            TODO show Search button
            showSearViewSearchButton(true);
            updateSelectedPageToolbar();
            if (mSearchSuggestionFrag != null || mSearchSuggestionsWithFilterFrag != null) {
                String query = null;
                if (mSearchView != null
                        && mSearchView.getQuery() != null) {
                    query = mSearchView.getQuery() + "";
                }
                String category = null;
                if (mListCarouselInfo != null
                        && mListCarouselInfo.get(mCurrentSelectedPagePosition) != null) {
                    category = mListCarouselInfo.get(mCurrentSelectedPagePosition).shortDesc;
                }
                if (canShowSearchWithFilter) {
                    if (mSearchSuggestionsWithFilterFrag != null) {
                        //    CleverTap.eventSearched(mSearchSuggestionsWithFilterFrag != null ? mSearchSuggestionsWithFilterFrag.itemSelected.key : category, !TextUtils.isEmpty(query) ? query : searchedQuery, mSearchSuggestionsWithFilterFrag.itemSelected != null ? mSearchSuggestionsWithFilterFrag.itemSelected.displayName : "", Analytics.NO);
                    } else
                        CleverTap.eventSearched(query, category, Analytics.NO);
                } else {
                    CleverTap.eventSearched(query, category, Analytics.NO);
                }
                if (canShowSearchWithFilter) {
                    removeFragment(mSearchSuggestionsWithFilterFrag);
                } else {
                    removeFragment(mSearchSuggestionFrag);
                }
                setAllowScrollBar(true);
                if (canShowSearchWithFilter)
                    mSearchSuggestionsWithFilterFrag = null;
                else
                    mSearchSuggestionFrag = null;
                //TODO: show if kidsFragment is present
                if (selectedMenuFragment != null && selectedMenuFragment.isHidden()) {
                    FragmentManager fm = getSupportFragmentManager();
                    fm.beginTransaction()
                            .show(selectedMenuFragment)
                            .commit();
                }
            }
        }
    }

    private void showSearViewSearchButton(boolean b) {
//        TODO  show or hide the search button based on flag
        if (mSearchButton == null) {
            return;
        }
        if (b) {
            mSearchButton.setEnabled(true);
            //  mSearchCloseButton.setEnabled(false);
            mSearchButton.setImageDrawable(getResources().getDrawable(R.drawable.actionbar_search_icon));
            return;
        }
        mSearchButton.setEnabled(false);//  mSearchCloseButton.setEnabled(false);
        mSearchButton.setImageDrawable(getResources().getDrawable(R.drawable.transparent));
    }

    public void setToolBarLogoAndTitle(String title) {
        if (mToolbar != null) {
            Field f; //NoSuchFieldException
            ImageView mToolbarLogo = null;
            if (!isNavigation)
                mToolbar.setLogo(R.drawable.app_icon_display); //TODO: Commented for Hide sundirect LOGO FROM TOOLBAR
            try {
                f = mToolbar.getClass().getDeclaredField("mLogoView");
                f.setAccessible(true); //IllegalAccessException
                mToolbarLogo = (ImageView) f.get(mToolbar);
//                mToolbarLogo.setPadding(16, 0, 16, 0);
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            if (mToolbarLogo != null) {
                Toolbar.LayoutParams toolbarParams = (Toolbar.LayoutParams) mToolbarLogo.getLayoutParams();
                toolbarParams.leftMargin = (int) mContext.getResources().getDimension(R.dimen.margin_gap_16);
            }
           /* mToolbar.setTitleTextColor(getResources().getColor(R.color.white));
            mToolbar.setTitle(robotoStringFont(title));*/
        }
    }

    public Spannable robotoStringFont(String name) {
        //SpannableString s = new SpannableString(name);
        //s.setSpan(new TypefaceSpan(mContext,"sans-serif-condensed"), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        Spannable spanString = new SpannableString(name);
        spanString.setSpan(new TypefaceSpan(mContext, "Roboto-Medium.ttf"), 0, spanString.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        return spanString;
    }

    // Called in Android UI's main thread
    public void onEventMainThread(OpenFilterEvent event) {
        fetchFilterData();
    }

    private void checkAndEnableChromeCast() {
        initCastConsumer();
        MediaRouteActionProvider mediaRouteActionProvider =
                (MediaRouteActionProvider) MenuItemCompat.getActionProvider(mMediaRouteMenuItem);
        if (mediaRouteActionProvider != null) {
            mediaRouteActionProvider.setAlwaysVisible(true);
        }
        if(mediaRouteActionProvider!=null) {
            mediaRouteActionProvider.getMediaRouteButton();
        }

        if (ApplicationController.ENABLE_CHROME_CAST) {
            /*if (mCastManager != null && mMenu != null) {
                mCastManager.addMediaRouterButton(mMenu, R.id.media_route_menu_item);
            }
            if (mMediaRouteMenuItem != null) {
                mMediaRouteMenuItem.setVisible(true);
            }*/

            CastButtonFactory.setUpMediaRouteButton(getApplicationContext(),
                    mMenu,
                    R.id.media_route_menu_item);
            if (mMediaRouteMenuItem != null) {
                mMediaRouteMenuItem.setVisible(false);
            }
        }
    }
   public void hideStatusBar() {
        if (Build.VERSION.SDK_INT < 16) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }

    public void showToolbar() {
        if (collapsingToolbarLayout == null) {
            return;
        }
        setToolBarCollapsible(false, false);
        collapsingToolbarLayout.setVisibility(View.VISIBLE);
      /*  if (mAppBar.isShown()) {
            mAppBar.setExpanded(true);
        }*/
    }

    public void hideToolbar() {
        if (collapsingToolbarLayout == null) {
            return;
        }
        setToolBarCollapsible(true, true);
        collapsingToolbarLayout.setVisibility(GONE);
      /*  if (mAppBar.isShown()) {
            mAppBar.setExpanded(false);
        }*/
    }

    //
    private void hideMenuSettings() {
        if (mMenu == null) {
            return;
        }
        MenuItem filterItem = mMenu.findItem(R.id.action_filter);
        filterItem.setVisible(false);
        MenuItem searchItem = mMenu.findItem(R.id.action_search);
        searchItem.setVisible(false);
    }

    private void showMenuSettings() {
        if (mMenu == null) {
            return;
        }
        MenuItem filterItem = mMenu.findItem(R.id.action_filter);
        filterItem.setVisible(true);
        MenuItem searchItem = mMenu.findItem(R.id.action_search);
        searchItem.setVisible(true);
    }

    private Bitmap mOrginalBitmap;
    private Blur mBlurEngine;

    private void addBlur() {
        //  if(mCurrentFragment == null){return;}
        //if(mCurrentFragment.getView() == null){return;}
        if (mFilterMenuPopup == null) {
            return;
        }
        try {
//			mFilterListView.setVisibility(View.INVISIBLE);
//			ValueAnimator fadeAnim = ObjectAnimator.ofFloat(mFilterListView, "alpha", 0f,1f);
//			fadeAnim.setDuration(1200);
//			fadeAnim.addListener(new AnimatorListenerAdapter() {
//				public void onAnimationEnd(Animator animation) {
////					mFilterListView.setVisibility(View.VISIBLE);
//				}
//			});
//			fadeAnim.start();
            final FrameLayout content = findViewById(R.id.root_layout);
            // mToolbar.setVisibility(View.GONE);
            //mTabPageIndicator.setVisibility(View.GONE);
            content.setDrawingCacheEnabled(true);
            mOrginalBitmap = content.getDrawingCache();
            if (mBlurEngine != null) {
                mBlurEngine.abort();
            }
            mBlurEngine = new Blur();
            Drawable bg = new ColorDrawable(Color.parseColor("#00000000"));
            mPopBlurredLayout.setBackgroundDrawable(bg);
            mBlurEngine.fastblur(mContext, mOrginalBitmap, 12, new Blur.BlurResponse() {

                @Override
                public void BlurredBitmap(Bitmap b) {
                    if (mOrginalBitmap != null)
                        mOrginalBitmap.recycle();
                    mOrginalBitmap = null;
                    if (b == null || mFilterMenuPopup == null) {
                        return;
                    }
                    Drawable d = new BitmapDrawable(b);
                    mPopBlurredLayout.setBackgroundDrawable(d);
                    ValueAnimator fadeAnim = ObjectAnimator.ofFloat(mPopBlurredLayout, "alpha", 0f, 1f);
                    fadeAnim.setDuration(500);
                    fadeAnim.addListener(new AnimatorListenerAdapter() {
                        public void onAnimationEnd(Animator animation) {
//							mFilterListView.setVisibility(View.VISIBLE);
                           /* mApplyLayout.setVisibility(View.VISIBLE);
                            mFilterLoadingTxt.setVisibility(View.GONE);

                            mPopupListAdapter = new FilterAdapter(mContext, mFilterListGroup, listener, mButtonFilterApply, filterMapLive);
                            mPopUpListView.setAdapter(mPopupListAdapter);
*/
                        }
                    });
                    fadeAnim.start();
                    if (content != null) {
                        content.setDrawingCacheEnabled(false);
                    }
                  /* mApplyLayout.setVisibility(View.VISIBLE);
                    mFilterLoadingTxt.setVisibility(View.GONE);

                    mPopupListAdapter = new FilterAdapter(mContext, mFilterListGroup, listener, mButtonFilterApply, filterMapLive);
                    mPopUpListView.setAdapter(mPopupListAdapter);*/
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        LoggerD.debugLog("CleverTap: MainActivity: onNewIntent");
        super.onNewIntent(intent);
        setIntent(intent);//must store the new intent unless getIntent() will return the old one
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            LoggerD.debugLog("showSearchFragment from onNewIntent query- " + query);
            showCloseButton(true);
            if (TextUtils.isEmpty(query)) {
                return;
            }
            showSearchFragment(query, true);
            setSearchQuery(query);
//            showOnlyMicButton();
            isVoiceButtonClicked = false;
            LoggerD.debugLog("search querey- " + query);
            return;
        }
        onHandleExternalIntent(intent);
    }

    private void showMicButton(boolean b) {
        if (mVoiceButton == null) {
            return;
        }
        if (b) {
            mVoiceButton.setVisibility(GONE);
            return;
        }
        mVoiceButton.setVisibility(GONE);
    }

    //     Hide Mic
//     Hide Close btn with parallely with search text change
    private void showCloseButton(boolean show) {
        if (mSearchCloseButton == null) {
            return;
        }

        /*if (b) {
            mSearchCloseButton.setEnabled(true);//  mSearchCloseButton.setEnabled(false);
            mSearchCloseButton.setImageDrawable(getResources().getDrawable(android.support.v7.appcompat.R.styleable.SearchView_closeIcon));
            return;
        }
        mSearchCloseButton.setEnabled(false);//  mSearchCloseButton.setEnabled(false);
        mSearchCloseButton.setImageDrawable(getResources().getDrawable(R.drawable.transparent));*/
        mSearchCloseButton.setVisibility(!show ? GONE : View.VISIBLE);
    }

    private void toggleSearchView(String query) {
        showCloseButton(true);
        if (query == null) {
            return;
        }
        if (TextUtils.isEmpty(query)) {
//            showCloseButton(false);
            showMicButton(false);
            return;
        }
        showMicButton(false);

    }

    private void setSearchQuery(String query) {
        if (mSearchView == null) {
            return;
        }
        toggleSearchView(query);
        mSearchView.setQuery(query, true);
    }


    public boolean showHelpScreenShown() {

        if (!ApplicationController.ENABLE_HELP_SCREEN) {
            return false;
        }
        boolean helpShown = PrefUtils.getInstance().getEpgHelpScreenPref();
        if (!helpShown) {

            InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null && imm.isAcceptingText()) {
                return false;
            }

            PrefUtils.getInstance().setEpgHelpScreenPref(true);

            helpLayout.postDelayed(new Runnable() {
                @Override
                public void run() {
                    helpLayout.setVisibility(View.VISIBLE);
                }
            }, 500);

            helpLayout.setOnTouchListener(new View.OnTouchListener() {

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    helpLayout.setVisibility(GONE);
                    return false;
                }

            });

        }
        return helpShown;

    }

    public void fetchUserId() {
        UserProfileRequest userProfileRequest = new UserProfileRequest(new APICallback<UserProfileResponseData>() {
            @Override
            public void onResponse(APIResponse<UserProfileResponseData> response) {
                if (response == null || response.body() == null) {
                    //Log.d(TAG, "fetchUserId null ");
                    return;
                }
                if (response.body().code == 402) {
                    PrefUtils.getInstance().setPrefLoginStatus("");
                    return;
                }
                if (response.body().code == 200) {
                    UserProfileResponseData responseData = response.body();
                    if (responseData != null
                            && responseData.result != null
                            && responseData.result.profile != null) {
                        Analytics.mixpanelIdentify();
                        PrefUtils.getInstance().setPrefUserId(responseData.result.profile._id);
                        Util.setUserIdInMyPlexEvents(mContext);
                        if (!TextUtils.isEmpty(responseData.result.profile.serviceName)) {
                            PrefUtils.getInstance().setServiceName(responseData.result.profile.serviceName);
                        }
                        if (responseData.result.profile.locations != null && responseData.result.profile.locations.size() > 0) {
                            if (responseData.result.profile.locations.get(0) != null
                                    && !TextUtils.isEmpty(responseData.result.profile.locations.get(0)))
                                PrefUtils.getInstance().setUSerCountry(responseData.result.profile.locations.get(0));
                        }

                        if (responseData.result.profile.state != null && !TextUtils.isEmpty(responseData.result.profile.state)) {
                            PrefUtils.getInstance().setUserState(responseData.result.profile.state);
                        }

                        if (responseData.result.profile.city != null && !TextUtils.isEmpty(responseData.result.profile.city)) {
                            PrefUtils.getInstance().setUserCity(responseData.result.profile.city);
                        }

                        if (mContext.getResources().getBoolean(R.bool.crashlytics_enable)) {
                            //Crashlytics.setUserIdentifier(responseData.result.profile._id + "");
                        }
//                        PlayUtils.initialization((Activity) mContext);
                        //Log.d(TAG, "fetchUserId profile._id " + responseData.result.profile._id);
                    }
                }
            }

            @Override
            public void onFailure(Throwable t, int errorCode) {
            }
        });
        APIService.getInstance().execute(userProfileRequest);
    }

    @Override
    public void onStop() {
        try {
            super.onStop();
           // isOpen = false;
            // To preserve battery life, the Mixpanel library will store
            // events rather than send them immediately. This means it
            // is important to call flush() to send any unsent events
            // before your application is taken out of memory.
            if (mDraggablePanel != null && mDraggablePanel.getVisibility() != View.VISIBLE) {
                if (mFragmentStack != null/* && mFragmentStack.size() < 1*/) {
                    sendPauseBroadCast();
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();

        }
    }

    private void showFragmentsFromNotification(String showAllLayoutType, int pageCount, String carouselName, String carouselTitle) {
        if (showAllLayoutType != null)
            LoggerD.debugLog("carouselData.showAllLayoutType- " + showAllLayoutType);
        if (APIConstants.LAYOUT_TYPE_BROWSE_LIST.equalsIgnoreCase(showAllLayoutType)) {
            showRelatedVODListFragment(showAllLayoutType, carouselName, pageCount, carouselTitle);
            return;
        } else if (APIConstants.LAYOUT_TYPE_BIG_BROWSE_LIST.equalsIgnoreCase(showAllLayoutType)
                || APIConstants.LAYOUT_TYPE_BIG_BROWSE_LIST_WITHOUT_FILTER.equalsIgnoreCase(showAllLayoutType)) {
            showCarouselViewAllFragment(showAllLayoutType, carouselName, pageCount, carouselTitle);
            return;
        } /*else if (APIConstants.LAYOUT_TYPE_BROWSE_SMALL_SQUARE_GRID.equalsIgnoreCase(showAllLayoutType)
                && APIConstants.LAYOUT_TYPE_NESTED_CAROUSEL.equalsIgnoreCase(carouselData.layoutType)) {
            Bundle args = new Bundle();
            ((MainActivity) mContext).pushFragment(SmallSquareItemsFragment.newInstance(args));
            return;
        }*/ else if (APIConstants.LAYOUT_TYPE_BROWSE_SMALL_SQUARE_GRID.equalsIgnoreCase(showAllLayoutType)) {
            showVODListFragment(showAllLayoutType, carouselName, pageCount, carouselTitle);
            return;
        } else if (showAllLayoutType != null
                && showAllLayoutType.contains(APIConstants.LAUNCH_TAB_HASH)) {
            try {
                ((MainActivity) mContext).redirectToPage(showAllLayoutType.split(APIConstants.PREFIX_HASH)[1]);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }

        showVODListFragment(showAllLayoutType, carouselName, pageCount, carouselTitle);
    }

    private void showVODListFragment(String showAllLayoutType, String carouselName, int count, String carouselTitle) {
        //TODO show VODListFragment from MainActivity with bundle

        Bundle args = new Bundle();

        args.putBoolean(ApplicationController.PARTNER_LOGO_BOTTOM_VISIBILTY, !APIConstants.LAYOUT_TYPE_HORIZONTAL_LIST_SMALL_ITEM.equalsIgnoreCase(showAllLayoutType));

        if (!APIConstants.LAYOUT_TYPE_BIG_BROWSE_LIST.equalsIgnoreCase(showAllLayoutType)) {
            args.putBoolean(FragmentCarouselViewAll.PARAM_FRAGMENT_TYPE_CAROUSEL_GRID, true);
        }
        args.putString(APIConstants.NOTIFICATION_PARAM_CAROUSEL_NAME, carouselName);
        args.putInt(APIConstants.NOTIFICATION_PARAM_PAGE_COUNT, count);
        args.putString(APIConstants.NOTIFICATION_PARAM_CAROUSEL_TITLE, carouselTitle);

        ((MainActivity) mContext).pushFragment(FragmentVODList.newInstance(args));
    }

    private void showCarouselViewAllFragment(String showAllLayoutType, String carouselName, int pageCount, String carouselTitle) {
        Bundle args = new Bundle();
        args.putString(APIConstants.NOTIFICATION_PARAM_CAROUSEL_NAME, carouselName);
        args.putInt(APIConstants.NOTIFICATION_PARAM_PAGE_COUNT, pageCount);
        args.putString(APIConstants.NOTIFICATION_PARAM_CAROUSEL_TITLE, carouselTitle);
        ((MainActivity) mContext).pushFragment(FragmentCarouselViewAll.newInstance(args));
    }


    private void showCarouselViewAllFragment(CarouselInfoData movieData, int pageCount) {
        CacheManager.setCarouselInfoData(movieData);
        Bundle args = new Bundle();
        args.putString(APIConstants.NOTIFICATION_PARAM_CAROUSEL_NAME, movieData.name);
        args.putInt(APIConstants.NOTIFICATION_PARAM_PAGE_COUNT, pageCount);
        args.putString(APIConstants.NOTIFICATION_PARAM_CAROUSEL_TITLE, movieData.title);
        ((MainActivity) mContext).pushFragment(FragmentCarouselViewAll.newInstance(args));
    }

    private void showRelatedVODListFragment(String showAllLayoutType, String carouselName, int pageCount, String carouselTitle) {
        //TODO show RelatedVodListFragment from main activity context

        Bundle args = new Bundle();
        args.putBoolean(FragmentCarouselViewAll.PARAM_FROM_VIEW_ALL, true);
        args.putString(APIConstants.NOTIFICATION_PARAM_CAROUSEL_NAME, carouselName);
        args.putString(APIConstants.NOTIFICATION_PARAM_CAROUSEL_TITLE, carouselTitle);
        args.putInt(APIConstants.NOTIFICATION_PARAM_PAGE_COUNT, pageCount);
        ((MainActivity) mContext).pushFragment(FragmentRelatedVODList.newInstance(args));


    }

    private void callContentFragment(String _id){
        CacheManager cacheManager = new CacheManager();
        cacheManager.getCardDetails(_id, false, new CacheManager.CacheManagerCallback() {
            @Override
            public void OnCacheResults(List<CardData> dataList) {
                Log.d(TAG, "OnCacheResults ");
                if (dataList == null
                        || dataList.isEmpty()) {
                    return;
                }
                final CardData cardData = dataList.get(0);
                inAppCardData = cardData;
                if (null == cardData) {
                    return;
                }

                if(showSubscriptionView){
                    fetchOfferAvailability(APIConstants.SOURCE_HOME,APIConstants.VALUE_SOURCE_IN_APP);
                    return;
                }

                CacheManager.setSelectedCardData(cardData);
//                        TODO show RelatedVOdListFragment
                final Bundle args = new Bundle();

                if (null != cardData.startDate
                        && null != cardData.endDate) {
                    Date startDate = Util.getDate(cardData.startDate);
                    Date endDate = Util.getDate(cardData.endDate);
                    Date currentDate = new Date();
                    if ((currentDate.after(startDate)
                            && currentDate.before(endDate))
                            || currentDate.after(endDate)) {
                        args.putBoolean(CardDetails
                                .PARAM_AUTO_PLAY, true);
                    }
                }

                if (cardData != null
                        && cardData.generalInfo != null) {
                    String contentId = cardData._id;
                    if (APIConstants.TYPE_PROGRAM.equalsIgnoreCase(cardData.generalInfo.type)
                            && cardData.globalServiceId != null) {
                        contentId = cardData.globalServiceId;
                    }
                    args.putString(CardDetails.PARAM_CARD_ID, contentId);
                    if (APIConstants.TYPE_VODCHANNEL.equalsIgnoreCase(cardData.generalInfo.type)
                            || APIConstants.TYPE_VODCATEGORY.equalsIgnoreCase(cardData.generalInfo.type)
                            || APIConstants.TYPE_VODYOUTUBECHANNEL.equalsIgnoreCase(cardData.generalInfo.type)
                            || APIConstants.TYPE_TVSEASON.equalsIgnoreCase(cardData.generalInfo.type)) {
                        //Launching ActivityRelatedVODList for vodcategory,vodchannel content type's
                        args.putSerializable(FragmentRelatedVODList.PARAM_SELECTED_VOD_DATA, cardData);
                        pushFragment(FragmentRelatedVODList.newInstance(args));
                        return;
                    }


                    args.putInt(CardDetails.PARAM_PARTNER_TYPE, Util.getPartnerTypeContent(cardData));
                    String partnerId = cardData == null || cardData.generalInfo == null || cardData.generalInfo.partnerId == null ? null : cardData.generalInfo.partnerId;
                    args.putString(CardDetails.PARAM_PARTNER_ID, partnerId);


                    if (mDraggablePanel != null) {
                        mDraggablePanel.post(new Runnable() {
                            @Override
                            public void run() {
                                showDetailsFragment(args, cardData);
                            }
                        });
                    }
                }
            }

            @Override
            public void OnOnlineResults(List<CardData> dataList) {
                Log.d(TAG, "OnOnlineResults ");
                if (dataList == null
                        || dataList.isEmpty()) {
                    return;
                }
                final CardData cardData = dataList.get(0);
                inAppCardData = cardData;
                if (null == cardData) {
                    return;
                }

                if(showSubscriptionView){
                    fetchOfferAvailability("Home","In_App_Messaging");
                    return;
                }

                CacheManager.setSelectedCardData(cardData);
//                        TODO show RelatedVOdListFragment
                final Bundle args = new Bundle();

                if (null != cardData.startDate
                        && null != cardData.endDate) {
                    Date startDate = Util.getDate(cardData.startDate);
                    Date endDate = Util.getDate(cardData.endDate);
                    Date currentDate = new Date();
                    if ((currentDate.after(startDate)
                            && currentDate.before(endDate))
                            || currentDate.after(endDate)) {
                        args.putBoolean(CardDetails
                                .PARAM_AUTO_PLAY, true);
                    }
                }

                if (cardData != null
                        && cardData.generalInfo != null) {
                    String contentId = cardData._id;
                    if (APIConstants.TYPE_PROGRAM.equalsIgnoreCase(cardData.generalInfo.type)
                            && cardData.globalServiceId != null) {
                        contentId = cardData.globalServiceId;
                    }
                    args.putString(CardDetails.PARAM_CARD_ID, contentId);
                    if (APIConstants.TYPE_VODCHANNEL.equalsIgnoreCase(cardData.generalInfo.type)
                            || APIConstants.TYPE_VODCATEGORY.equalsIgnoreCase(cardData.generalInfo.type)
                            || APIConstants.TYPE_VODYOUTUBECHANNEL.equalsIgnoreCase(cardData.generalInfo.type)
                            || APIConstants.TYPE_TVSEASON.equalsIgnoreCase(cardData.generalInfo.type)) {
                        //Launching ActivityRelatedVODList for vodcategory,vodchannel content type's
                        args.putSerializable(FragmentRelatedVODList.PARAM_SELECTED_VOD_DATA, cardData);
                        pushFragment(FragmentRelatedVODList.newInstance(args));
                        return;
                    }
                    args.putInt(CardDetails.PARAM_PARTNER_TYPE, Util.getPartnerTypeContent(cardData));
                    String partnerId = cardData == null || cardData.generalInfo == null || cardData.generalInfo.partnerId == null ? null : cardData.generalInfo.partnerId;
                    args.putString(CardDetails.PARAM_PARTNER_ID, partnerId);
                    String adProvider = null;
                    boolean adEnabled = false;
                    if (cardData != null
                            && cardData.content != null) {
                        if (!TextUtils.isEmpty(cardData.content.adProvider)) {
                            adProvider = cardData.content.adProvider;
                        }
                        adEnabled = cardData.content.adEnabled;
                    }
                    args.putString(CardDetails.PARAM_AD_PROVIDER, adProvider);
                    args.putBoolean(CardDetails.PARAM_AD_ENBLED, adEnabled);

                    args.putString(CleverTap.PROPERTY_TAB, getCurrentTabName());
                    args.putString(Analytics.PROPERTY_SOURCE, APIConstants.VALUE_SOURCE_IN_APP);
                    args.putString(Analytics.PROPERTY_SOURCE_DETAILS, APIConstants.VALUE_SOURCE_IN_APP);

                    if (mDraggablePanel != null) {
                        mDraggablePanel.post(new Runnable() {
                            @Override
                            public void run() {
                                showDetailsFragment(args, cardData);
                            }
                        });
                    }
                }
            }

            @Override
            public void OnOnlineError(Throwable error, int errorCode) {
                Log.d(TAG, "onOnlineError " + error);
                if (error != null) {
                    String errorMessage = error.getMessage();
                    if (errorMessage != null && errorMessage.contains(APIConstants.MESSAGE_ERROR_CONN_RESET) && !isRetryAlreadyDone) {
                        //Retry for data connection
                        isRetryAlreadyDone = true;
                        Log.d("Leaderboard", "MAinActicity : onHandleExternalIntent 5: ");
                        onHandleExternalIntent(getIntent());
                    }

                }

            }
        });
    }

    private void showAlertDialog(InAppMessage inAppMessage){
        AlertDialog.Builder builder = new AlertDialog.Builder(this,R.style.CustomDialog); // new AlertDialog.Builder(this,R.style.CustomDialog)  to make it transparent
        LayoutInflater factory = LayoutInflater.from(this);
        final View views = factory.inflate(R.layout.layout_in_app_notification, null);
        ImageView imageView = views.findViewById(R.id.image_view_app);
        // Get  Device Width
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels - 150;
        int tabWidth = displayMetrics.widthPixels - (displayMetrics.widthPixels/100 * 50);
        //set width for tab and mobile
        if(DeviceUtils.isTablet(mContext)){
            imageView.setLayoutParams(new FrameLayout.LayoutParams(tabWidth, ViewGroup.LayoutParams.WRAP_CONTENT));
        }else{
            imageView.setLayoutParams(new FrameLayout.LayoutParams(width, ViewGroup.LayoutParams.WRAP_CONTENT));
        }
        Button collapse = views.findViewById(R.id.collapse_button);
        collapse.setVisibility(GONE);
        Glide.with(this)
                .load(inAppMessage.getImageData().getImageUrl()).listener(new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                collapse.setVisibility(View.VISIBLE);
                return false;
            }
        })
                .fitCenter()
                .into(imageView);
        imageView.setImageResource(R.drawable.black);
        builder.setView(views);
        alert = builder.create();
        collapse.setOnClickListener(view -> {
            if(callbacks != null && inAppMessage != null && inAppMessage.getAction() != null)
                callbacks.messageDismissed(FirebaseInAppMessagingDisplayCallbacks.InAppMessagingDismissType.CLICK);
            alert.cancel();
            isInAppDisplaying = false;
           /* if (!PrefUtils.getInstance().getPrefMainCoachMark() && !isInAppDisplaying && !ApplicationController.navigateToDeeplink){
                showCoachMarkDialog();
            }*/
        });
        imageView.setOnClickListener(view -> {
            inAppMessageShow = true;
            if(callbacks != null && inAppMessage != null && inAppMessage.getAction() != null)
                callbacks.messageClicked(inAppMessage.getAction());

            handleInAppUrl(Uri.parse(inAppMessage.getAction().getActionUrl()));

            alert.cancel();
        });
        alert.show();
    }




    @Override
    public void onResume() {
        makeUserProfileRequest();
        updateNavMenuMyAccountSection();

        if(blurlayout_toolbar.getVisibility() == VISIBLE){
            setStatusBarGradiant(MainActivity.this, true);
        }else{
            setStatusBarGradiant(MainActivity.this, false);
        }
       // ((Activity) mContext).getWindow().setStatusBarColor(getResources().getColor(R.color.black_30));
        if (mDrawerLayout != null && mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.openDrawer(GravityCompat.START);
        }else{
            disableNavigation();
        }
        try {
            super.onResume();
            isOpen = true;
            // to load manager after getting Properties api response
            FirebaseInAppMessaging.getInstance().setAutomaticDataCollectionEnabled(true);
            FirebaseInAppMessaging.getInstance().setMessageDisplayComponent((inAppMessage, firebaseInAppMessagingDisplayCallbacks) -> {
                isInAppDisplaying = true;
                callbacks = firebaseInAppMessagingDisplayCallbacks;
                if(isLaunch && inAppMessage != null && inAppMessage.getImageData().getImageUrl() != null) {
                    if (inAppMessage.getData() != null && inAppMessage.getData().containsKey("status")){
                        if (!inAppMessage.getData().get("status").isEmpty() && inAppMessage.getData().get("status") != null) {
                            if(inAppMessage.getData().get("status").equals("promotion")){
                                showAlertDialog(inAppMessage);
                            }else if (PrefUtils.getInstance().getPrefLoginStatus().equalsIgnoreCase("success") /*&& !PrefUtils.getInstance().getPrefIsPremium()*/ && inAppMessage.getData().get("status").equals("unsubscribed") ){
                                showAlertDialog(inAppMessage);
//                            }else if (PrefUtils.getInstance().getPrefIsPremium() && inAppMessage.getData().get("status").equals("subscribed")){
//                                showAlertDialog(inAppMessage);
                            }/*else if (PrefUtils.getInstance().getPrefLoginStatus().equalsIgnoreCase("success") && inAppMessage.getData().get("status").equals("expired")){
                                if (!TextUtils.isEmpty(ApplicationController.isExpired) && ApplicationController.isExpired.equalsIgnoreCase("true") ){
                                    showAlertDialog(inAppMessage);
                                }else{
                                    isInAppDisplaying = false;
                                }
                            }*/else if (/*PrefUtils.getInstance().getPrefIsPremium() &&*/ inAppMessage.getData().get("status").equals("expiring")){
                                try {
                                }catch (Exception e){
                                    e.printStackTrace();
                                    isInAppDisplaying = false;
                                }

                            }else{
                                isInAppDisplaying = false;
                            }
                        }
                    }
                }
                isLaunch = false;
            });

            HungamaPartnerHandler.getInstance(this).destroy();
            ErosNowDownloadManager.getInstance(this).destroy();
//        HungamaPartnerHandler.getInstance(this).resumeAllDownloads();
            ErosNowDownloadManager.getInstance(this).initUnzipManagerListener(null);
            ErosNowDownloadManager.getInstance(this).checkAndCompleteUnzip();
            LoggerD.debugLog("MainActivity: onResume");
            updateNavigationBarAndToolbar();
            if (displayManager == null) {
                displayManager = ((DisplayManager) this.getSystemService(Context.DISPLAY_SERVICE));
            }
            Display[] displays = displayManager.getDisplays();
            if (displays != null) {
                Log.e("ScreenMirror", "" + displays.length);
                if (displays.length > 1) {
                    isScreenMirrorInProgress = true;
                    Toast.makeText(mContext, "Screen Mirroring in Progress", Toast.LENGTH_SHORT).show();
                } else {
                    isScreenMirrorInProgress = false;
                }
            }
            if (mSearchView != null
                    && !mSearchView.isIconified()) {
                mSearchView.requestFocus();
                if (mSearchView.getQuery() != null
                        && !TextUtils.isEmpty(mSearchView.getQuery())) {
                    toggleSearchView(mSearchView.getQuery().toString());
                }
            }
            showChromCastButton();
            if (mDraggablePanel != null && mDraggablePanel.getVisibility() != View.VISIBLE) {
                if (mFragmentStack != null /*&& mFragmentStack.size() < 1*/) {
                    sendResuemBroadCast();
                    if(((MainActivity) mContext)!=null && ((MainActivity) mContext).mFragmentCardDetailsPlayer.mPlayer!=null &&  !((MainActivity) mContext).mFragmentCardDetailsPlayer.mPlayer.isMinimized())
                        sendMiniPlayerDisableddBroadCast();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkStoragePermissionForDownload() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
                requestPermissions(permissions, PERMISSION_REQUEST_CODE);
            } else {
                startDownloadContentPlayback();
            }
        }
    }

    private void showChromCastButton() {
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mCastManager == null) {
                    //Log.d(TAG, "MainActivity: onResume mCastManager == null");
                    initGoogleChromeCast();
                }
             /*   //Log.d(TAG, "MainActivity: onResume mCastManager.isConnected()- " + isConnected()
                        + " or mCastManager.isConnecting()- " + isConnected());*/
                if (ApplicationController.ENABLE_CHROME_CAST
                        && mCastManager != null
                        && isConnected()
                        && ConnectivityUtil.isConnectedWifi(mContext)) {

                    checkAndEnableChromeCast();
                    invalidateOptionsMenu();
                    if (mMediaRouteMenuItem != null) {
                        supportInvalidateOptionsMenu();
                        mMediaRouteMenuItem.setVisible(true);
                    }
                    MediaRouteActionProvider mediaRouteActionProvider =
                            (MediaRouteActionProvider) MenuItemCompat.getActionProvider(mMediaRouteMenuItem);
                    mediaRouteActionProvider.setAlwaysVisible(true);
                    //Log.d(TAG, "MainActivity: R.id.media_route_menu_item");
                }
            }
        }, 5000);
    }

    private void checkAppVersionUpgrade() {
        APIConstants.versionDataPath = APIConstants.getVersionDataPath(mContext);
        VersionData latestVersionData = (VersionData) SDKUtils.loadObject(APIConstants.versionDataPath);
        if (latestVersionData != null) {
            VersionUpdateUtil versionUpdateUtil = new VersionUpdateUtil
                    (mContext, latestVersionData, new VersionUpdateUtil.VersionUpdateCallbackListener() {
                        @Override
                        public boolean showUpgradeDialog() {
                            return SDKUtils.getCardExplorerData().cardDataToSubscribe == null;
                        }

                        @Override
                        public void triggerInAppUpdate(boolean isMandatory) {
                            checkPlayStoreUpdate(isMandatory);
                        }
                    });
            versionUpdateUtil.setOnUpdateClickedListener(new VersionUpdateUtil.OnUpdateClickedListener() {
                @Override
                public void onUpdateClicked() {
                    shouldCheckAppVersionUpgrade = true;
                }
            });
            versionUpdateUtil.checkIfUpgradeAvailable();
        }
    }

    private boolean shouldCheckAppVersionUpgrade = false;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isOpen = false;
        //Log.v(TAG, "onDestroy()");
        unregisterNetworkChanges();
        APIService.getInstance().destroy();
        /*if (vmaxInterStitialAdView != null) {
            vmaxInterStitialAdView.onDestroy();
        }
        if (vmaxBannerAdView != null) {
            vmaxBannerAdView.onDestroy();
        }*/
    }

    @Override
    public void pushFragment(BaseFragment fragment) {
//        showSystemUI();
        if (isFinishing() || fragment == null) {
            SDKLogger.debug("isFinishing " + isFinishing() + "or fragment is null " + fragment);
            return;
        }
        if((isMediaPlaying() ||(mFragmentCardDetailsPlayer!=null && mFragmentCardDetailsPlayer.mPlayer!=null && (mFragmentCardDetailsPlayer.mPlayer.mPlayerState==WAIT_FORRETRY || mFragmentCardDetailsPlayer.mPlayer.mPlayerState==PLAYER_STOPPED)) /* || !(PrefUtils.getInstance().getSubscriptionStatusString()!=null && PrefUtils.getInstance().getSubscriptionStatusString().equalsIgnoreCase(APIConstants.USER_NOT_SUBSCRIBED))) && mFragmentCardDetailsPlayer!=null*/)){
//            mFragmentCardDetailsPlayer.mPlayer.onPause();
            mFragmentCardDetailsPlayer.minimizePlayer();
        }
      //  sendMiniPlayerEnabledBroadCast();
        try {
            super.pushFragment(fragment);
            FragmentManager fragmentManager = getSupportFragmentManager();
//            removeFragment(mCurrentFragment);
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            minimizePlayerAtTabPageIndicator();
            hideTabIndicator();
            if (fragment instanceof MyWatchlistFavouritesFragment) {
                ((MyWatchlistFavouritesFragment) fragment).setTabName(getCurrentDescForWatchList());
            }
            if (fragment instanceof FragmentChannelEpg) {
                findViewById(R.id.content_channel_epg).setVisibility(View.VISIBLE);
                transaction.replace(R.id.content_channel_epg, fragment);
            } else if (fragment instanceof FragmentVODList) {
                sendPauseBroadCast();
                findViewById(R.id.fragment_vodlist).setVisibility(View.VISIBLE);
                if(mCurrentFragment instanceof FragmentLanguageCarouselInfo){
                    transaction.add(R.id.fragment_related_vodlist_or_carousel_view_all, fragment);
                }else {
                    transaction.add(R.id.fragment_vodlist, fragment);
                }
            } else if (fragment instanceof FragmentCarouselViewAll) {
            findViewById(R.id.fragment_vodlist).setVisibility(View.VISIBLE);
            if(mCurrentFragment instanceof FragmentLanguageCarouselInfo){
                transaction.add(R.id.fragment_related_vodlist_or_carousel_view_all, fragment);
            }else {
                transaction.replace(R.id.fragment_vodlist, fragment);
            }
        } else if (fragment instanceof FragmentRelatedVODList
                    || fragment instanceof FragmentCarouselViewAll || fragment instanceof FragmentLanguageCarouselInfo) {
                findViewById(R.id.fragment_related_vodlist_or_carousel_view_all).setVisibility(View.VISIBLE);
                transaction.replace(R.id.fragment_related_vodlist_or_carousel_view_all, fragment);
                if(isMediaPlaying()){
                    mFragmentCardDetailsPlayer.mPlayer.onResume();
                }
            } else if (fragment instanceof CardDetails) {
                findViewById(R.id.content_detail).setVisibility(View.VISIBLE);
                transaction.replace(R.id.content_detail, fragment);
            } else if (fragment instanceof SearchSuggestions) {
                findViewById(R.id.content_searchview).setVisibility(View.VISIBLE);
                transaction.replace(R.id.content_searchview, fragment);
            } else if (fragment instanceof SearchSuggestionsWithFilter) {
                findViewById(R.id.content_searchview).setVisibility(View.VISIBLE);
                transaction.replace(R.id.content_searchview, fragment);
            } else if (fragment instanceof FilterFragment) {
                FrameLayout frameLayout = findViewById(R.id.filterFrameLayout);
                frameLayout.setVisibility(View.VISIBLE);
                if (mCurrentFragment == null)
                    showAppBar();
                /*if (mCurrentFragment != null) {
                    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                    params.addRule(RelativeLayout.BELOW, mToolbar.getId());
                    frameLayout.setLayoutParams(params);
                }*/
                transaction.replace(R.id.filterFrameLayout, fragment);
                setAllowScrollBar(false);
            } else if (fragment instanceof SmallSquareItemsFragment) {
                findViewById(R.id.fragment_genres).setVisibility(View.VISIBLE);
                transaction.replace(R.id.fragment_genres, fragment);
            } else if (fragment instanceof FragmentLanguageInfo || fragment instanceof FragmentCarouselInfo) {
                findViewById(R.id.fragment_language_carousel).setVisibility(View.VISIBLE);
                transaction.replace(R.id.fragment_language_carousel, fragment);
            } else if (fragment instanceof ArtistProfileFragment) {
                findViewById(R.id.fragment_artist).setVisibility(View.VISIBLE);
                transaction.replace(R.id.fragment_artist, fragment);
            } else {
                if (carouselInfoData == null) {
                    findViewById(R.id.other_content).setVisibility(View.VISIBLE);
                    if (fragment != null && fragment.isAdded()) {
                        return;
                    }
                    transaction.add(R.id.other_content, fragment);
                } else {
                    findViewById(R.id.other_content_kids).setVisibility(View.VISIBLE);
                    if (fragment != null && fragment.isAdded()) {
                        return;
                    }
                    transaction.add(R.id.other_content_kids, fragment);
                }
            }
            mCurrentFragment = fragment;
            mFragmentStack.push(fragment);
            mCurrentFragment.setBaseActivity(this);
            mCurrentFragment.setContext(this);
            transaction.addToBackStack(null);
            transaction.commitAllowingStateLoss();
            isPushedFragment = true;
        } catch (Throwable e) {
            e.printStackTrace();
//            Crashlytics.logException(e);
        }
    }

    @Override
    public void showDetailsFragment(Bundle args, CardData cardData) {
        String actionType = "";
        String actionURL = "";
        if (cardData != null && cardData.content != null) {
            actionType = cardData.content.actionType;
            actionURL = cardData.content.actionURL;
        } else if (args != null) {
            actionType = args.getString(APIConstants.ACTION_TYPE);
            actionURL = args.getString(APIConstants.ACTION_URL);
        }
        sendPageChangeListener();

     /*   if(cardData != null && cardData.generalInfo !=null && cardData.generalInfo.contentRights != null && cardData.generalInfo.contentRights.size()>0 && cardData.generalInfo.contentRights.get(0)!= null) {
            if(cardData.generalInfo.contentRights.get(0).equalsIgnoreCase(APIConstants.CONTENT_RIGHTS_TVOD)) {
                showHomePopUpPromotion();
                return;
            }
        }*/
        if (!TextUtils.isEmpty(actionType)) {
            if (APIConstants.ACTION_TYPE_DEEPLINK.equalsIgnoreCase(actionType)) {
                if (actionURL.contains("/viewAll")) {

                    String[] split = actionURL.split("\\?");
                    String[] split1 = new String[0];
                    if (split.length == 2) {
                        split1 = split[1].split("&");
                    }

                    String[] split2 = new String[0];
                    if (split1.length > 0) {
                        split2 = split1[0].split("=");
                    }
                    String[] split3 = new String[0];
                    if (split1.length > 1) {
                        split3 = split1[1].split("=");
                    }
                    String[] split4 = new String[0];
                    if (split1.length > 2) {
                        split4 = split1[2].split("=");
                    }

                    String[] split5 = new String[0];
                    if (split1.length > 3) {
                        split5 = split1[3].split("=");
                    }

                    String c_name = "";
                    if (split2.length == 2) {
                        c_name = split2[1];
                    }
                    String c_title = "";
                    if (split3.length == 2) {
                        c_title = split3[1];
                    }
                    String c_count = "";
                    if (split4.length == 2) {
                        c_count = split4[1];
                    }

                    String showAllLayoutType = "";
                    if (split5.length == 2) {
                        showAllLayoutType = split5[1];
                    }


                    if (!TextUtils.isEmpty(c_name) && !TextUtils.isEmpty(c_title)) {
                        CarouselInfoData carouselData = new CarouselInfoData();
                        carouselData.name = c_name;
                        carouselData.title = c_title;
                        if (!TextUtils.isEmpty(c_count))
                            carouselData.pageSize = Integer.parseInt(c_count);

                        if (!TextUtils.isEmpty(showAllLayoutType))
                            carouselData.showAllLayoutType = showAllLayoutType;

                        CleverTap.eventPageViewed(CleverTap.PAGE_VIEW_ALL);
                        if (carouselData.name == null) {
                            return;
                        }
                        if (mCurrentFragment != null) {
                            removeFragment(mCurrentFragment);
                        }
                        String mMenuGroup = carouselData.name;
                        String mPageTitle = carouselData.title;
                        int carouselPosition = -1;
                        if (mListCarouselInfo != null && mListCarouselInfo.size() > 0) {
                            for (int i = 0; i < mListCarouselInfo.size(); i++) {
                                if (mListCarouselInfo.get(i) != null && !TextUtils.isEmpty(mListCarouselInfo.get(i).name) && mListCarouselInfo.get(i).name.equalsIgnoreCase(carouselData.name))
                                    carouselPosition = i;
                            }
                        }

                        if (APIConstants.MENU_TYPE_GROUP_ANDROID_TVSHOW.equalsIgnoreCase(mMenuGroup)) {
                            Analytics.browseViewAllEvent(Analytics.EVENT_BROWSED_TVSHOWS, carouselData);
                            AppsFlyerTracker.eventBrowseTabWithSectionViewAll(APIConstants.TYPE_PAGE_TV_SHOWS, carouselData.title, true);
                        } else if (APIConstants.MENU_TYPE_GROUP_ANDROID_VIDEOS.equalsIgnoreCase(mMenuGroup)) {
                            Analytics.browseViewAllEvent(Analytics.EVENT_BROWSED_VIDEOS, carouselData);
                            AppsFlyerTracker.eventBrowseTabWithSectionViewAll(APIConstants.TYPE_PAGE_VIDEOS, carouselData.title, true);
                        } else if (APIConstants.MENU_TYPE_GROUP_ANDROID_MUSIC_VIDEOS.equalsIgnoreCase(mMenuGroup)) {
                            Analytics.browseViewAllEvent(Analytics.EVENT_BROWSED_MUSIC_VIDEOS, carouselData);
                            AppsFlyerTracker.eventBrowseTabWithSectionViewAll(APIConstants.TYPE_PAGE_MUSIC_VIDEOS, carouselData.title, true);
                        } else if (APIConstants.MENU_TYPE_GROUP_ANDROID_KIDS.equalsIgnoreCase(mMenuGroup) || APIConstants.MENU_TYPE_GROUP_ANDROID_KIDS_MENU.equalsIgnoreCase(mMenuGroup)) {
                            Analytics.browseViewAllEvent(Analytics.EVENT_BROWSED_KIDS, carouselData);
                            AppsFlyerTracker.eventBrowseTabWithSectionViewAll(HomePagerAdapter.getPageKids(), carouselData.title, true);
                        } else if (mPageTitle != null) {
                            Analytics.browseViewAllEvent("browsed " + carouselData.title.toLowerCase(), carouselData);
                            AppsFlyerTracker.eventBrowseTabWithSectionViewAll(mPageTitle.toLowerCase(), carouselData.title, true);
                        }

                        LoggerD.debugLog("carouselData.showAllLayoutType- " + carouselData.showAllLayoutType);

                        if (APIConstants.LAYOUT_TYPE_BROWSE_LIST.equalsIgnoreCase(carouselData.showAllLayoutType)) {
                            showRelatedVODListFragment(carouselData.showAllLayoutType, carouselData.name, carouselPosition, carouselData.title);
                            return;
                        } else if (APIConstants.LAYOUT_TYPE_BIG_BROWSE_LIST.equalsIgnoreCase(carouselData.showAllLayoutType)
                                || APIConstants.LAYOUT_TYPE_BIG_BROWSE_LIST_WITHOUT_FILTER.equalsIgnoreCase(carouselData.showAllLayoutType)) {
                            showCarouselViewAllFragment(carouselData, carouselPosition);
                            return;
                        } else if (APIConstants.LAYOUT_TYPE_BROWSE_SMALL_SQUARE_GRID.equalsIgnoreCase(carouselData.showAllLayoutType)
                                && APIConstants.LAYOUT_TYPE_NESTED_CAROUSEL.equalsIgnoreCase(carouselData.layoutType)) {
                            Bundle bundle = new Bundle();
                            bundle.putSerializable(SmallSquareItemsFragment.PARAM_CAROUSEL_INFO_DATA, carouselData);
                            bundle.putInt(CleverTap.PROPERTY_CAROUSEL_POSITION, carouselPosition);
                            ((MainActivity) mContext).pushFragment(SmallSquareItemsFragment.newInstance(bundle));
                            return;
                        } else if (APIConstants.LAYOUT_TYPE_BROWSE_SMALL_SQUARE_GRID.equalsIgnoreCase(carouselData.showAllLayoutType)) {
                            showVODListFragment(carouselData.showAllLayoutType, carouselData.name, carouselPosition, carouselData.title);
                            return;
                        } else if (carouselData.showAllLayoutType != null
                                && carouselData.showAllLayoutType.contains(APIConstants.LAUNCH_TAB_HASH)) {
                            try {
                                ((MainActivity) mContext).redirectToPage(carouselData.showAllLayoutType.split(APIConstants.PREFIX_HASH)[1]);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            return;
                        }

                        showVODListFragment(carouselData.showAllLayoutType, carouselData.name, carouselPosition, carouselData.title);
                    } else {
                        navigateToDetails(args, cardData);
                    }
                }
            } else if (APIConstants.LAUNCH_WEB_CHROME.equalsIgnoreCase(actionType)) {
                if (actionURL != null) {
                    ChromeTabUtils.openUrl(mContext, actionURL);
                }
            } else if (APIConstants.LAUNCH_WEB_PAGE.equalsIgnoreCase(actionType)) {
                //startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(actionURL)));
                if (actionURL != null) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(actionURL)));
                }
            } else if (APIConstants.LAUNCH_SUBSCRIBE.equalsIgnoreCase(actionType)) {
                if (!Util.checkUserLoginStatus()) {
                    launchLoginActivity("", "");
                    return;
                }
                if (Util.isPremiumUser()) {
                    startActivity(new Intent(mContext, ActivityMyPacks.class));
                    return;
                }
                Intent ip = new Intent(mContext, SubscriptionWebActivity.class);
                ip.putExtra(SubscriptionWebActivity.IS_FROM_PREMIUM, true);
                if (actionURL != null) {
                    ip.putExtra(SubscriptionWebActivity.ACTION_URL, actionURL);
                }
                startActivity(ip);
            } else if (APIConstants.LAYOUT_TYPE_MENU.equalsIgnoreCase(actionType)) {
                CarouselInfoData carouselData = new CarouselInfoData();
                carouselData.name = actionURL;
                carouselData.title = cardData.generalInfo.title;
                carouselData.bgColor = getCurrentTabBgColor();
                Bundle bundle = new Bundle();
                bundle.putSerializable(FragmentCarouselInfo.PARAM_APP_FRAG_TYPE, actionURL);
                bundle.putSerializable(FragmentCarouselInfo.PARAM_CAROUSAL, carouselData);
                bundle.putSerializable(FragmentCarouselInfo.PARAM_SHOW_TOOLBAR, true);
                ((MainActivity) mContext).pushFragment(FragmentCarouselInfo.newInstance(bundle));
            }

        } else {
            nonceImplementation();
            navigateToDetails(args, cardData);
        }
    }

    private void navigateToDetails(Bundle args, CardData cardData) {
//       showSystemUI();
        if (cardData != null && cardData.localFilePath != null) {
            if (shouldAskForStoragePermission(cardData)) {
                mOldDownloadContentCardData = cardData;
                mOldDownloadContentBundle = args;
                checkStoragePermissionForDownload();
                return;
            }
        }

        if (mFragmentCardDetailsPlayer == null
                || mFragmentCardDetailsDescription == null
                || !mFragmentCardDetailsPlayer.isAdded()
                || !mFragmentCardDetailsDescription.isAdded()) {
            hookDraggablePanelListeners();
            initializeDraggablePanel();
            LoggerD.debugLog("mFragmentCardDetailsPlayer is not initialized");
            return;
        }
        if (args == null
                || cardData == null
                || mDraggablePanel == null) {
            return;
        }
        mArgumentsOfPlayer = args;
        mCardData = cardData;
        isLoginCheckInProgress = false;
        CleverTap.eventVideoDetailsViewed((CardData) args.getSerializable(CardDetails.PARAM_RELATED_CARD_DATA),
                mCardData, args.getString(Analytics.PROPERTY_SOURCE), args.getString(Analytics.PROPERTY_SOURCE_DETAILS),
                Util.checkUserLoginStatus(), getCurrentTabName(), args.getInt(CleverTap.PROPERTY_CAROUSEL_POSITION, -1),
                args.getInt(CleverTap.PROPERTY_CONTENT_POSITION, -1));
         if (!Util.checkUserLoginStatus()) {
            initLogin(args.getString(Analytics.PROPERTY_SOURCE), args.getString(Analytics.PROPERTY_SOURCE_DETAILS));
            return;
        }
        String adProvider = null;
        boolean adEnabled = false;
        if (cardData != null
                && cardData.content != null) {
            if (!TextUtils.isEmpty(cardData.content.adProvider)) {
                adProvider = cardData.content.adProvider;
            }
            adEnabled = cardData.content.adEnabled;
        }
        args.putString(CardDetails.PARAM_AD_PROVIDER, adProvider);
        args.putBoolean(CardDetails.PARAM_AD_ENBLED, adEnabled);
        args.putString(CleverTap.PROPERTY_TAB, getCurrentTabName());
        args.putString(APIConstants.TAB_NAME, getCurrentTabName());
        if (PrefUtils.getInstance().getIsLightThemeEnabled()) {
            if (cardData.isNewsContent() || cardData.isLive()) {
                args.putString(APIConstants.BG_COLOR, "#FFFFFF");
            } else {
                args.putString(APIConstants.BG_COLOR, getBgColor());
            }
        }
        if (cardData.generalInfo != null
                && !TextUtils.isEmpty(cardData.generalInfo.partnerId)) {
            args.putString(CardDetails.PARAM_PARTNER_ID, cardData.generalInfo.partnerId);
            args.putInt(CardDetails.PARAM_PARTNER_TYPE, Util.getPartnerTypeContent(cardData));
        }
        disableNavigation();
        if (cardData != null & cardData.generalInfo != null && !TextUtils.isEmpty(cardData.generalInfo.type)) {
            type = cardData.generalInfo.type;
        }
        if (!TextUtils.isEmpty(type) && (type.equalsIgnoreCase("person") || type.equalsIgnoreCase("actor"))) {

            Bundle args2 = new Bundle();
            if (cardData != null) {
                CardDataGeneralInfo generalInfo = cardData.generalInfo;
                if (generalInfo != null) {
                    args2.putString("ID", cardData.generalInfo._id);
                    args2.putString("NAME", generalInfo.title);
                    args2.putString("DESCRIPTION", type);
                    args2.putString("FULL_DESCRIPTION", generalInfo.description);
                    args2.putSerializable("CARD_DATA", cardData);
                    if (DeviceUtils.isTablet(mContext)) {
                        args2.putString("IMAGE_URL", Util.getSquareImageLink(cardData, true));
                    } else {
                        args2.putString("IMAGE_URL", Util.getSquareImageLink(cardData, false));
                    }
                }
            }
            ((BaseActivity) mContext).pushFragment(ArtistProfileFragment.newInstance(args2));
            return;
        }

        int adClicks = PrefUtils.getInstance().getInterstrialAdClicks() + 1;
        PrefUtils.getInstance().setInterstrialAdClicks(adClicks);
        /*if (mCardData!=null&&mCardData.isMovie()){
            mCardData.playFullScreen=true;
        }*/
        mFragmentCardDetailsPlayer.updateData(args);
        hideTabIndicator();
        mDraggablePanel.setVisibility(View.VISIBLE);
       // sendMiniPlayerEnabledBroadCast();
        // un commented due to opening player as doc
        mDraggablePanel.maximize();
    }

    public void getEPGData( String contentId){
        ChannelListEPG.Params params = new ChannelListEPG.Params(contentId, "", false, false);
        ChannelListEPG channelListEPG = new ChannelListEPG(params, new APICallback<ChannelsEPGResponseData>() {
            @Override
            public void onResponse(APIResponse<ChannelsEPGResponseData> response) {
                if (response != null && response.body() != null && response.body().getResults() != null && response.body().getResults().size() > 0) {
                    for (int i = 0; i < response.body().getResults().size(); i++) {
                        List<CardData> cardDataList = response.body().getResults().get(i).getPrograms();
                        if(cardDataList.size() >0){
                            CardData carouselData = cardDataList.get(0);
                            //showDetailsFragment(args, cardDataList.get(0));
                            CacheManager.setSelectedCardData(carouselData);
                            Bundle args = new Bundle();
                            args.putString(CardDetails.PARAM_CARD_ID, carouselData._id);
                            args.putBoolean(CardDetails.PARAM_AUTO_PLAY, true);
                            if (carouselData.generalInfo != null
                                    && carouselData.generalInfo.type != null
                                    && (APIConstants.TYPE_VODCATEGORY.equalsIgnoreCase(carouselData.generalInfo.type)
                                    || APIConstants.TYPE_VODCHANNEL.equalsIgnoreCase(carouselData.generalInfo.type)
                                    || APIConstants.TYPE_VODYOUTUBECHANNEL.equalsIgnoreCase(carouselData.generalInfo.type)
                                    || APIConstants.TYPE_TVSEASON.equalsIgnoreCase(carouselData.generalInfo.type)
                                    || APIConstants.TYPE_TVSERIES.equalsIgnoreCase(carouselData.generalInfo.type))) {
                                //Log.d(TAG, "type: " + carouselData.generalInfo.type + " title: " + carouselData.generalInfo.title);
                                args.putSerializable(CardDetails.PARAM_RELATED_CARD_DATA, carouselData);
                            }
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

                           String partnerId = carouselData == null || carouselData.generalInfo == null || carouselData.generalInfo.partnerId == null ? null : carouselData.generalInfo.partnerId;
                            args.putString(CardDetails.PARAM_PARTNER_ID, partnerId);
                            args.putInt(CardDetails.PARAM_PARTNER_TYPE, Util.getPartnerTypeContent(carouselData));
                            args.putString(Analytics.PROPERTY_SOURCE, Analytics.VALUE_SOURCE_CAROUSEL);
                         //   args.putString(Analytics.PROPERTY_SOURCE_DETAILS, carouselInfoData.title);
                           // args.putInt(CleverTap.PROPERTY_CAROUSEL_POSITION, parentPosition);
                            args.putSerializable(CardDetails.PARAM_SELECTED_CARD_DATA, carouselData);
                            ((BaseActivity) mContext).showDetailsFragment(args, carouselData);
                        }
                    }
                }
            }

            @Override
            public void onFailure(Throwable t, int errorCode) {

            }
        });
        APIService.getInstance().execute(channelListEPG);
    }


    private boolean shouldAskForStoragePermission(CardData cardData) {
        if (cardData != null && cardData.isDownloadDataOnExternalStorage) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return ActivityCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED;
            }
        }
        return false;
    }

    private String getCurrentTabName() {
        String currentTab = "";
        try {
            if (mViewPager != null
                    && mListCarouselInfo != null
                    && mListCarouselInfo.size() > 0) {
                currentTab = mListCarouselInfo.get(mCurrentSelectedPagePosition).title;

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return currentTab;
    }

    private String getNavigationDownloadItemTitle() {
        String title = "";
        try {
            for (int i = 0; i < mListCarouselInfoDrawer.size(); i++) {
                if (mListCarouselInfoDrawer.get(i).title.contains(APIConstants.NOTIFICATION_PARAM_DOWNLOAD)) {
                    return mListCarouselInfoDrawer.get(i).altTitle;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return title;
        }
        return title;

    }

    private String getCurrentTabForVmax() {
        String currentTab = "";
        try {
            if (mViewPager != null
                    && mListCarouselInfo != null
                    && mListCarouselInfo.size() > 0) {
                currentTab = mListCarouselInfo.get(mCurrentSelectedPagePosition).name;

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return currentTab;
    }

    private String getCurrentDesc() {
        String currentTab = "";
        try {
            if (mViewPager != null
                    && homePagerAdapterDynamicMenu != null
                    && homePagerAdapterDynamicMenu.getCount() > 0) {
                int pos = mViewPager.getCurrentItem();
                currentTab = tabListData.get(pos).name;

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return currentTab;
    }

    private String[] getCurrentDescForWatchList() {
        String[] currentTab = new String[homePagerAdapterDynamicMenu.getCount()];
        try {
            if (mViewPager != null
                    && homePagerAdapterDynamicMenu != null
                    && homePagerAdapterDynamicMenu.getCount() > 0) {
                for (int i = 0; i < homePagerAdapterDynamicMenu.getCount(); i++) {
                    currentTab[i] = tabListData.get(i).name;
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return currentTab;
    }

    public String getBgColor() {
        if (mListCarouselInfo == null) {
            return null;
        }
        int positionClicked = getPositionInCarousalWhenClickedBottomTab(mCurrentSelectedPagePositionTitle);
        CarouselInfoData carouselInfoData = mListCarouselInfo.get(positionClicked);
        return carouselInfoData.bgColor;
    }

    private void hideTabIndicator() {
        if (mTabPageIndicator == null) return;
        mTabPageIndicator.setVisibility(GONE);
        mTabPagerRootLayout.setVisibility(View.INVISIBLE);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        if (mDraggablePanel != null)
            mDraggablePanel.setTopFragmentMarginBottom(getResources().getDimensionPixelSize(R.dimen.margin_gap_16));

    }

    public void initLogin(String source, String sourceDetails) {
        /*if (getResources().getBoolean(R.bool.is_login_check_request_enabled) && ConnectivityUtil.isConnectedMobile(mContext)) {
            makeSignInCheck(source, sourceDetails);
            return;
        }*/
        launchLoginActivity(source, sourceDetails);

    }

    /**
     * Initialize and configure the DraggablePanel widget with two fragments and some attributes.
     */
    private void initializeDraggablePanel() {
        LoggerD.debugLog("initialize draggable panel");
        if (mFragmentCardDetailsPlayer == null
                || mFragmentCardDetailsDescription == null) {
            initializeFragmentDetailsPlayer();
        }
        mDraggablePanel.setFragmentManager(getSupportFragmentManager());
        mDraggablePanel.setTopFragment(mFragmentCardDetailsPlayer);
        mDraggablePanel.setBottomFragment(mFragmentCardDetailsDescription);
        mDraggablePanel.setClickToMaximizeEnabled(!DeviceUtils.isTablet(this));

        //mDraggablePanel.setTopViewHeight(500);
        TypedValue typedValue = new TypedValue();
        getResources().getValue(R.dimen.x_scale_factor, typedValue, true);
        float xScaleFactor = typedValue.getFloat();
        typedValue = new TypedValue();
        getResources().getValue(R.dimen.y_scale_factor, typedValue, true);
        float yScaleFactor = typedValue.getFloat();
//        mDraggablePanel.setTopViewHeight((ApplicationController.getApplicationConfig().screenWidth * 9) / 16);
        int softNavigationBarHeight = UiUtil.getSoftButtonsBarSizePort((Activity) mContext);
        if (DeviceUtils.isTablet(this)) {
            mDraggablePanel.setScreenWidth(getApplicationConfig().screenWidth > getApplicationConfig().screenHeight ? getApplicationConfig().screenHeight + softNavigationBarHeight : getApplicationConfig().screenWidth);
        } else {
            mDraggablePanel.setScreenWidth(getApplicationConfig().screenWidth);
        }
            mDraggablePanel.setXScaleFactor(xScaleFactor);
        mDraggablePanel.setYScaleFactor(yScaleFactor);
         mDraggablePanel.setTopFragmentMarginRight(getResources().getDimensionPixelSize(R.dimen._5sdp));
        mDraggablePanel.setTopFragmentMarginBottom(getResources().getDimensionPixelSize(R.dimen.margin_gap_70));
        mDraggablePanel.setEnableHorizontalAlphaEffect(false);
        //mDraggablePanel.setTopFragmentViewWidth(ApplicationController.getApplicationConfig().screenWidth);
         mDraggablePanel.topFragmentWidth = ((MainActivity) mContext).getWindowManager().getDefaultDisplay().getWidth()*2;
         mDraggablePanel.topFragmentMarginBottom = getResources().getDimensionPixelSize(R.dimen.margin_gap_70);
        mDraggablePanel.initializeView();
        if (DeviceUtils.isTablet(this)) {

        }
        // mDraggablePanel.setTopViewWidth(getApplicationConfig().screenWidth > getApplicationConfig().screenHeight ? getApplicationConfig().screenHeight + softNavigationBarHeight : getApplicationConfig().screenWidth);
        else
//            mDraggablePanel.setTopViewWidth(getApplicationConfig().screenWidth);
            mDraggablePanel.setTopFragmentResize(true);
        mDraggablePanel.setVisibility(View.INVISIBLE);
        mFragmentCardDetailsPlayer.setDraggablePanel(mDraggablePanel);
        mFragmentCardDetailsPlayer.enableDraggablePanel();
    }

    private void initializeFragmentDetailsPlayer() {
        LoggerD.debugLog("initialize fragments");
        mFragmentCardDetailsDescription = FragmentCardDetailsDescription.newInstance(null);
        mFragmentCardDetailsDescription.setContext(this);
        mFragmentCardDetailsDescription.setBaseActivity(this);

        mFragmentCardDetailsPlayer = FragmentCardDetailsPlayer.newInstance(null);
        mFragmentCardDetailsPlayer.setContext(this);
        mFragmentCardDetailsPlayer.setBaseActivity(this);

        mFragmentCardDetailsPlayer.setPlayerDescriptionListener(mFragmentCardDetailsDescription);
    }

    /**
     * Hook the DraggableListener to DraggablePanel to pause or resume the video when the
     * DragglabePanel is maximized or closed.
     */
    private void hookDraggablePanelListeners() {
        mDraggablePanel.setDraggableListener(mDraggableListener);
    }


    /**
     * Restore the DraggablePanel state.
     *
     * @param savedInstanceState bundle to get the Draggable state.
     */
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        try {
            recoverDraggablePanelState(savedInstanceState);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        try {
            saveDraggableState(outState);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Get the DraggablePanelState from the saved bundle, modify the DraggablePanel visibility to
     * GONE
     * and apply the
     * DraggablePanelState to recover the last graphic state.
     */
    private void recoverDraggablePanelState(Bundle savedInstanceState) {
        final DraggableState draggableState =
                (DraggableState) savedInstanceState.getSerializable(DRAGGABLE_PANEL_STATE);
        final CardData cardData =
                (CardData) savedInstanceState.getSerializable(FRAGMENT_PLAYER_CARD_DATA_STATE);
        final Bundle args =
                savedInstanceState.getBundle(FRAGMENT_PLAYER_ARGS_STATE);
        if (mDraggablePanel != null && draggableState == null) {
            mDraggablePanel.setVisibility(GONE);
            return;
        }
        if (cardData != null
                && args != null) {
            showDetailsFragment(args, cardData);
        }
//        updateDraggablePanelStateDelayed(draggableState);
    }

    /**
     * Return the view to the DraggablePanelState: minimized, maximized, closed to the right or
     * closed
     * to the left.
     *
     * @param draggableState to apply.
     */
    private void updateDraggablePanelStateDelayed(DraggableState draggableState) {
        Handler handler = new Handler();
        switch (draggableState) {
            case MAXIMIZED:
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mDraggablePanel.setVisibility(View.VISIBLE);
                        mDraggablePanel.maximize();
                    }
                }, DELAY_MILLIS);
                break;
            case MINIMIZED:
                mDraggablePanel.setVisibility(View.VISIBLE);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mDraggablePanel.minimize();
                    }
                }, DELAY_MILLIS);
                break;
            case CLOSED_AT_LEFT:
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mDraggablePanel.setVisibility(View.INVISIBLE);
                        mDraggablePanel.closeToLeft();
                    }
                }, DELAY_MILLIS);
                break;
            case CLOSED_AT_RIGHT:
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mDraggablePanel.setVisibility(View.INVISIBLE);
                        mDraggablePanel.closeToRight();
                    }
                }, DELAY_MILLIS);
                break;
        }
    }

    /**
     * Keep a reference of the last DraggablePanelState.
     *
     * @param outState Bundle used to store the DraggablePanelState.
     */
    private void saveDraggableState(Bundle outState) {
        DraggableState draggableState = null;
        if (mDraggablePanel == null) {
            return;
        }
        if (mDraggablePanel.isMaximized()) {
            draggableState = DraggableState.MAXIMIZED;
        } else if (mDraggablePanel.isMinimized()) {
            draggableState = DraggableState.MINIMIZED;
        } else if (mDraggablePanel.isClosedAtLeft()) {
            draggableState = DraggableState.CLOSED_AT_LEFT;
        } else if (mDraggablePanel.isClosedAtRight()) {
            draggableState = DraggableState.CLOSED_AT_RIGHT;
        }
        outState.putSerializable(DRAGGABLE_PANEL_STATE, draggableState);
        outState.putBundle(FRAGMENT_PLAYER_ARGS_STATE, mArgumentsOfPlayer);
        outState.putSerializable(FRAGMENT_PLAYER_CARD_DATA_STATE, mCardData);
    }

    public void closeDraggablePanel() {
        if (mDraggablePanel == null) {
            return;
        }
        if(mCurrentFragment!=null && mCurrentFragment.isAdded() ){
          //  ((Activity) mContext).getWindow().setStatusBarColor(getResources().getColor(R.color.app_bkg));
            setStatusBarGradiant(MainActivity.this, false);
        }
        mDraggablePanel.setVisibility(View.GONE);
        showTabIndicator();
    }

    private void showTabIndicator() {
        if (mTabPageIndicator == null || mCurrentFragment != null) {
            return;
        }
        mTabPagerRootLayout.setVisibility(View.VISIBLE);
        mTabPageIndicator.setVisibility(GONE);
    }

    public void setDraggableListener(DraggableListener draggableListener) {
        this.mDetailsDraggableListener = draggableListener;
    }

    public void closePlayerFragment() {
        if (mFragmentCardDetailsPlayer != null) {
            mFragmentCardDetailsPlayer.onCloseFragment();
        }
        if (mFragmentStack != null && mFragmentStack.size() <= 1) {
            sendMiniPlayerDisableddBroadCast();
        }
        closeDraggablePanel();
        checkInAppRatingPopUp();
    }


    private Handler mHandlerShowMediaController = new Handler();
    private static final int DEFAULT_MEDIACTROLLER_TIMEOUT = 10 * 1000;
    private RelativeLayout mRLayoutTimeShiftHelp;
    private Runnable mRunnableShowMediaController = new Runnable() {
        @Override
        public void run() {
            if (mFragmentCardDetailsPlayer != null) {
                mFragmentCardDetailsPlayer.showMediaController();
            }
            mHandlerShowMediaController.postDelayed(mRunnableShowMediaController, DEFAULT_MEDIACTROLLER_TIMEOUT);
        }
    };

    public void showTimeShiftHelpScreen() {
        if (mRLayoutTimeShiftHelp == null) {
            return;
        }
//        if (mContext != null) {
//            ((BaseActivity) mContext).setOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//        }
        if (mContext != null) {
            if (DeviceUtils.isTabletOrientationEnabled(mContext)) {
                ((BaseActivity) mContext).setOrientation(SCREEN_ORIENTATION_USER);
            } else {
                ((BaseActivity) mContext).setOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        }
        mFragmentCardDetailsPlayer.disableDraggablePanel();
        mRLayoutTimeShiftHelp.setVisibility(View.VISIBLE);
        mHandlerShowMediaController.postDelayed(mRunnableShowMediaController, DEFAULT_MEDIACTROLLER_TIMEOUT);
        mRLayoutTimeShiftHelp.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideTimeShiftHelpScreen();
                return false;
            }
        });
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        LoggerD.debugLog("onConfigurationChanged(): MainActiviy " + newConfig.orientation);
        if(url!=null && !url.equals("")){
            showHomePopUpPromotion(url);
        }
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            //Log.d(TAG, "dismiss pop up window");
            if (mDraggablePanel != null
                    && mSearchView != null
                    && !mSearchView.isIconified()) {
                LoggerD.debugLog("onConfigurationChanged(): hidesearchview");
                HideSearchView();
            }
//            hideSoftInputKeyBoard(mSearchView);
//            removeSearchFragment();
            if (mFilterMenuPopupWindow != null) {
                mFilterMenuPopupWindow.dismissPopupWindow();
            }

            if (mFragmentCardDetailsPlayer != null
                    && mFragmentCardDetailsPlayer.mPlayer != null
                    && (mFragmentCardDetailsPlayer.mPlayer.isMediaPlaying()
                    || mFragmentCardDetailsPlayer.mPlayer.isYouTubePlayerPlaying())) {
                disableNavigation();
            } else {
                enableNavigation();
            }
            if (drawerListAdapter != null) {
                closeDrawer();
                if (mDrawerRecycleView.getAdapter() == null) {
                    mDrawerRecycleView.setAdapter(drawerListAdapter);
                }
            }

        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            if (mFilterMenuPopupWindow != null) {
                mFilterMenuPopupWindow.dismissPopupWindow();
            }
            if (mFragmentCardDetailsPlayer != null
                    && mFragmentCardDetailsPlayer.mPlayer != null
                    && (mFragmentCardDetailsPlayer.mPlayer.isMediaPlaying()
                    || mFragmentCardDetailsPlayer.mPlayer.isYouTubePlayerPlaying())) {
                disableNavigation();
            } else {
                enableNavigation();
            }
            if (drawerListAdapter != null) {
                closeDrawer();
                if (mDrawerRecycleView.getAdapter() == null) {
                    mDrawerRecycleView.setAdapter(drawerListAdapter);
                }
            }
        }
//        Log.d(TAG, "onConfigurationChanged: mFragmentCardDetailsPlayer.mPlayer.isFullScreen() "+ mFragmentCardDetailsPlayer.mPlayer.isFullScreen());
//        Log.d(TAG, "onConfigurationChanged: mFragmentCardDetailsPlayer.mPlayer.isMinimized() "+ mFragmentCardDetailsPlayer.mPlayer.isMinimized());
//        Log.d(TAG, "onConfigurationChanged: newConfig.orientation == Configuration.ORIENTATION_PORTRAIT "+ (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT));
        try {

//            newConfig.orientation == Configuration.ORIENTATION_PORTRAIT &&
            if(DeviceUtils.isTablet(this) && mFragmentCardDetailsPlayer!=null && mFragmentCardDetailsPlayer.mPlayer!=null && mFragmentCardDetailsPlayer.mPlayer.isMinimized()){
                //            mFragmentCardDetailsPlayer.minimizePlayerControls();
                mDetailsDraggableListener.onMinimized();
//            mFragmentCardDetailsPlayer.mDraggablePanel.minimize();
                    updateBottomBar(true, 0);
                    if(mFragmentCardDetailsPlayer.mPlayer.channelName!=null){
                        mFragmentCardDetailsPlayer.mPlayer.channelName.setTextColor(getResources().getColor(R.color.white));
                        if(mCardData != null && mCardData.globalServiceName != null)
                            mFragmentCardDetailsPlayer.mPlayer.channelName.setText(mCardData.globalServiceName);
                        else {
                            if(mCardData != null){
                                if(mCardData.getParnterTitle(mContext) != null)
                                    mFragmentCardDetailsPlayer.mPlayer.channelName.setText(mCardData.getParnterTitle(mContext));
                            }
                        }
                    }

                if(mFragmentCardDetailsPlayer.mPlayer.programName!=null){
                    if(mCardData != null && mCardData.getTitle() != null) {
                        mFragmentCardDetailsPlayer.mPlayer.programName.setText(mCardData.getTitle());
                    }
                        mFragmentCardDetailsPlayer.mPlayer.programName.setTextColor(getResources().getColor(R.color.white));
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        super.onConfigurationChanged(newConfig);
    }

    public void hideTimeShiftHelpScreen() {
        if (mFragmentCardDetailsPlayer != null) {
            mFragmentCardDetailsPlayer.resumePreviousOrientaionTimer();
            mFragmentCardDetailsPlayer.enableDraggablePanel();
            mFragmentCardDetailsPlayer.setShowingHelpScreen(false);
        }
        if (mRLayoutTimeShiftHelp == null) {
            return;
        }
        mHandlerShowMediaController.removeCallbacks(mRunnableShowMediaController);
        mRLayoutTimeShiftHelp.setVisibility(GONE);
    }


    public enum PlaybackState {
        PLAYING, PAUSED, BUFFERING, IDLE
    }

    private void initCastConsumer() {
        try {
            mCastContext = CastContext.getSharedInstance(mContext);
        } catch (Exception e) {
            e.printStackTrace();
            checkPlayServices();
        }
        if (mCastContext == null) {
            return;
        }
        mCastManager = mCastContext.getSessionManager();
        mCastConsumer = new SessionManagerListener<CastSession>(

        ) {
            @Override
            public void onSessionStarting(CastSession castSession) {
                SDKLogger.debug("onSessionStarting");

            }


            @Override
            public void onSessionStarted(CastSession castSession, String s) {
                SDKLogger.debug("onSessionStarted");
                onApplicationConnected();

            }

            @Override
            public void onSessionStartFailed(CastSession castSession, int i) {
                SDKLogger.debug("onSessionStartFailed");
                onApplicationDisconnected();

            }

            @Override
            public void onSessionEnding(CastSession castSession) {
                SDKLogger.debug("onSessionEnding");

            }

            @Override
            public void onSessionEnded(CastSession castSession, int i) {
                SDKLogger.debug("onSessionEnded");
                onApplicationDisconnected();

            }

            @Override
            public void onSessionResuming(CastSession castSession, String s) {
                SDKLogger.debug("onSessionResuming");
            }

            @Override
            public void onSessionResumed(CastSession castSession, boolean b) {
                SDKLogger.debug("onSessionResumed");
                onApplicationConnected();
            }

            @Override
            public void onSessionResumeFailed(CastSession castSession, int i) {
                SDKLogger.debug("onSessionResumeFailed");
                onApplicationDisconnected();
            }

            @Override
            public void onSessionSuspended(CastSession castSession, int i) {
                SDKLogger.debug("onSessionSuspended");

            }

            private void onApplicationConnected() {
                //Log.w(TAG, "onApplicationLaunched() is reached");
                if (mPlaybackState == PlaybackState.PLAYING) {
                    try {
                        AlertDialogUtil.showToastNotification("Connected to Cast Device");
                        finish();
                    } catch (Exception e) {
                        Log.w("Load Error", e.toString());

                    }
                    return;
                } else {
                    mPlaybackState = PlaybackState.IDLE;
                    AlertDialogUtil.showToastNotification("Connected to Cast Device");
//                    Toast.makeText(MainActivity.this,getString(R.string.casting),Toast.LENGTH_SHORT).show();
                }
                if (mListCarouselInfo != null && !isChromeCastClickEventDone) {
                    isChromeCastClickEventDone = true;
                    CarouselInfoData carouselInfoData = mListCarouselInfo.get(mCurrentSelectedPagePosition);
                    CleverTap.eventClicked(carouselInfoData.title, CleverTap.ACTION_CHROMECAST);
                }
                invalidateOptionsMenu();
            }

            private void onApplicationDisconnected() {
                //Log.d(TAG, "onApplicationDisconnected() is reached ");
                showChromCastButton();
                isChromeCastClickEventDone = false;
            }
        };
        mCastManager.addSessionManagerListener(mCastConsumer,
                CastSession.class);


    }


    public void showProgressBar(boolean shouldUseProgressBar) {

        if (mContext == null) {
            return;
        }
        if (mProgressBar == null) {
            return;
        }
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
        if (mProgressBar != null && mProgressBar.getVisibility() == View.VISIBLE) {
            return;
        }
        if (shouldUseProgressBar) {
            if (mProgressBar != null) {
                mProgressBar.setVisibility(View.VISIBLE);
            }
        } else {
            mProgressDialog = ProgressDialog.show(mContext, "", "Loading...", true, true, null);
            mProgressDialog.setContentView(R.layout.layout_progress_dialog);
            ProgressBar mProgressBar = (ProgressBar) mProgressDialog.findViewById(R.id.imageView1);
            final int version = Build.VERSION.SDK_INT;
            if (version < 21) {
                mProgressBar.setIndeterminate(false);
                mProgressBar.getIndeterminateDrawable().setColorFilter(UiUtil.getColor(mContext, R.color.progressbar_color), PorterDuff.Mode.MULTIPLY);
            }
        }

    }

    public void dismissProgressBar(boolean shouldUseProgressBar) {
        try {
            if (isFinishing()) {
                return;
            }
            if (mProgressDialog != null && mProgressDialog.isShowing() && !shouldUseProgressBar) {
                mProgressDialog.dismiss();
            }
            if (mProgressBar != null
                    && mProgressBar.getVisibility() == View.VISIBLE
                    && shouldUseProgressBar) {
                mProgressBar.setVisibility(GONE);
            }

        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public String getSelectedPageName() {
        if (mListCarouselInfo != null) {
            CarouselInfoData carouselInfoData = mListCarouselInfo.get(mCurrentSelectedPagePosition);
            return carouselInfoData.title;
        }
        return null;
    }

    final int SEARCH_SPEECH = 1357;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        SDKLogger.debug("requestCode- " + requestCode + " resultCode- " + resultCode);
        if(APIConstants.NOTIFICATION_REQUEST == requestCode) {
         //   if(resultCode == RESULT_OK)
                loadNotification();
                APIConstants.IS_REFRESH_LIVETV=true;
        }
        if(resultCode==APIConstants.ONSUBSCRIPTIONDONE){
            APIConstants.IS_REFRESH_LIVETV1=false;
        }
        if (resultCode == APIConstants.SUBSCRIPTIONINPROGRESS
                || resultCode == APIConstants.SUBSCRIPTIONSUCCESS) {
            SDKLogger.debug("requestCode- " + resultCode);
            startOtpReader();
            isToReloadData = true;
            fetchMyPackages();
            if (isLoginCheckInProgress) {
//                if (mArgumentsOfPlayer != null && (resultCode == INTENT_RESPONSE_TYPE_SUCCESS_SUBSCRIPTION_FAILED)) {
//                    mArgumentsOfPlayer.putBoolean(CardDetails.PARAM_AUTO_PLAY, false);
//                }
                showDetailsFragment(mArgumentsOfPlayer, mCardData);
            }
        }
        if (requestCode == APP_UPDATE_REQUEST_CODE) {
            if (resultCode != RESULT_OK) {
                Log.d("MainActivity", "Update failed! code:" + resultCode);
                // If the update is cancelled or fails,
                // you can request to start the update again.
            } else if (resultCode == RESULT_CANCELED) {
                Log.e(TAG, "Result User Cancelled");
            } else if (resultCode == RESULT_FIRST_USER) {
                Log.e(TAG, "Result First User");
            }
        }

        if(data == null)
            return;
        if(EditProfileActivity.PERMISSION_REQUEST_CODE == requestCode) {
            if(resultCode == ProfileActivity.success) {
                String profileUrl = data.getStringExtra("profile_url");
                if(profileUrl != null && !profileUrl.isEmpty()) {
                    Glide.with(getApplicationContext())
                            .asBitmap()
                            .load(profileUrl)
                            .placeholder(R.drawable.nav_drawer_profile_thumbnail)
                            .error(R.drawable.nav_drawer_profile_thumbnail)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .dontAnimate()
                            .into(new SimpleTarget<Bitmap>() {

                                @Override
                                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                    profile_iv.setImageBitmap(resource);
                                }
                            });
                }
                else {
                    profile_iv.setImageResource(R.drawable.nav_drawer_profile_thumbnail);
                }

            }
        }
        if(requestCode == SEARCH_SPEECH){
            for (Fragment fragment : getSupportFragmentManager().getFragments()) {
                if(fragment instanceof SearchSuggestionsWithFilter) {
                    fragment.onActivityResult(requestCode, resultCode, data);
                    break;
                }
            }
        }else {
            if (mCurrentFragment != null) {
                mCurrentFragment.onActivityResult(requestCode, resultCode, data);
            }
//        if (requestCode == SubscriptionWebActivity.SUBSCRIPTION_REQUEST) {
            if (resultCode == ProfileActivity.success) {
                updateProfile();
            }

            if (requestCode == INTENT_REQUEST_TYPE_LOGIN) {
                if (resultCode == MainActivity.INTENT_RESPONSE_TYPE_SUCCESS
                        || resultCode == INTENT_RESPONSE_TYPE_SUCCESS_SUBSCRIPTION_FAILED) {
                    if (mArgumentsOfPlayer != null && (resultCode == INTENT_RESPONSE_TYPE_SUCCESS_SUBSCRIPTION_FAILED)) {
                        mArgumentsOfPlayer.putBoolean(CardDetails.PARAM_AUTO_PLAY, false);
                    }
                    myplexAPISDK.ENABLE_FORCE_CACHE = true;
                    MenuDataModel.clearCache();
                    isToReloadData = true;
                    fetchMyPackages();
                    updateNavMenuMyAccountSection();
                    //TODO: handle favorite request here
                /*if (!isFavoriteRequest)
                    showDetailsFragment(mArgumentsOfPlayer, mCardData);
                else {
                    FavouriteRequest.Params favouritesParams = new FavouriteRequest.Params(_id, type);
                    executeFavouriteRequest(favouritesParams);
                }*/

                    if (mFragmentCardDetailsDescription != null) {
                        mFragmentCardDetailsDescription.onActivityResult(requestCode, resultCode, data);
                    }
                    initVMAXAds();
                    if (resultCode == MainActivity.INTENT_RESPONSE_TYPE_SUCCESS)
                        updateProfile();
                }
            }

            if (resultCode == INTENT_RESPONSE_TYPE_EMAIL_UPDATE_SUCCESS) {
                if (mFragmentCardDetailsDescription != null) {
                    mFragmentCardDetailsDescription.onActivityResult(requestCode, resultCode, data);
                }
            }

            if (requestCode == PROFILE_UPDATE_REQUEST) {
                if (data != null && data.getExtras() != null) {
                    Bundle extras = data.getExtras();
                    if (extras.containsKey(IS_PROFILE_UPDATE_SUCCESS)) {
                        boolean isProfileUpdateSuccess = data.getBooleanExtra(extras.getString(IS_PROFILE_UPDATE_SUCCESS), false);
                        if (isProfileUpdateSuccess) {
                            myplexAPISDK.ENABLE_FORCE_CACHE = true;
                            MenuDataModel.clearCache();
                            isToReloadData = true;
                            fetchMyPackages();
                            updateNavMenuMyAccountSection();
                        }
                    }
                }
            }

            if (requestCode == ProfileActivity.edit_profile_code) {
                if (data != null && data.getExtras() != null) {
                    Bundle extras = data.getExtras();
                    if (extras.containsKey(ProfileActivity.IS_LOG_OUT_REQUEST)) {
                        boolean isLogoutRequestSuccess = data.getBooleanExtra(extras.getString(ProfileActivity.IS_LOG_OUT_REQUEST), true);
                        if (isLogoutRequestSuccess) {
                            makeSignOutRequest();
                        } else {
                            updateNavMenuMyAccountSection();
                        }
                    } else {
                        updateNavMenuMyAccountSection();
                    }
                } else {
                    updateNavMenuMyAccountSection();
                }
            }
        }
    }

    @Override
    public void onStateUpdate(InstallState installState) {
        if (installState.installStatus() == InstallStatus.DOWNLOADED) {
            popupSnackbarForCompleteUpdate();
        }

    }

    private void popupSnackbarForCompleteUpdate() {
        Snackbar snackbar =
                Snackbar.make(
                        findViewById(R.id.drawerLayout),
                        "An update has just been downloaded.",
                        Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction("RELAUNCH APP", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                appUpdateManager.completeUpdate();
            }
        });
        //snackbar.setAction("RESTART", view -> appUpdateManager.completeUpdate());
        snackbar.setActionTextColor(
                getResources().getColor(R.color.red1));
        snackbar.show();
    }

    private void startOtpReader() {
        if (mContext == null) {
            LoggerD.debugOTP("mCotext == null");
        }
        mOtpReader = OtpReader.getInstance(mContext, true);
        mOtpReader.start(ApplicationController.getAppContext(), MainActivity.SMS_CONFIRMATION_TIMEOUT);
        mOtpReader.setOtpListener(this);
    }

    private void stopOtpReader() {
        if (mOtpReader == null) {
            return;
        }
        mOtpReader.stop();
        mOtpReader.setOtpListener(null);
        mOtpReader = null;
    }

    public void onEventMainThread(SubscriptionsDataEvent event) {
        loadDataAndInitializeUI();
    }


    public void onEventMainThread(UpdateFilterDataEvent event) {
        updateFilterData(event.filteredValues);
    }

    public void reloadData() {
        SDKLogger.debug("reloadData");
        if (isFinishing()) {
            SDKLogger.debug("reloadData activity is finishing");
            return;
        }
        try {
            myplexAPISDK.ENABLE_FORCE_CACHE = true;
            tabListData = getTabListData();
            if (tabListData != null && tabListData.size() > 0 && tabListData.get(0) != null) {
                ApplicationController.FIRST_TAB_NAME = tabListData.get(0).name;
            }
            int currentItem = mViewPager.getCurrentItem();
            homePagerAdapterDynamicMenu = new HomePagerAdapterDynamicMenu(getSupportFragmentManager(), this, "", "", tabListData);
            homePagerAdapterDynamicMenu.setViewScrollListener(this);
            mViewPager.setAdapter(homePagerAdapterDynamicMenu);
            mViewPager.setPagingEnabled(false);
            mViewPager.setOffscreenPageLimit(5);
            int pageMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0, getResources().getDisplayMetrics());
            mViewPager.setPageMargin(pageMargin);
            mViewPager.setCurrentItem(currentItem);
            mTabPageIndicator.setViewPager(mViewPager);
            mViewPager.postDelayed(new Runnable() {
                @Override
                public void run() {
                    ScopedBus.getInstance().post(new MessageEvent());
                }
            }, 3000);

            // mTabPageIndicator.setOnPageChangeListener(mTabPageChangeListener);
            updateSelectedPageToolbar();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void launchLoginActivity(String source, String sourceDetails) {
        if (mContext == null) {
            return;
        }
        finish();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra("isFromSplash", false);
        startActivity(intent);
        //   ((Activity) mContext).startActivity(LoginActivity.createIntent(mContext, true, false, PARAM_SUBSCRIPTION_TYPE_NONE, source, sourceDetails));
//        SSO.activity().callback(new SSOcallback() {
//            @Override
//            public void AccessToken(String accessToken, String expiry, String idToken) {
//                if (accessToken != null && !TextUtils.isEmpty(accessToken)) {
//                    //Toast.makeText(mContext,"access token:: "+accessToken,Toast.LENGTH_LONG).show();
//                    makeSSOLoginRequest(accessToken, idToken, expiry);
//                } else {
//                    Toast.makeText(mContext, "Unable to fetch access token", Toast.LENGTH_LONG).show();
//                }
//            }
//
//            @Override
//            public void ErrorMessage(String errorMessage) {
//                Toast.makeText(mContext, "Error message:: " + errorMessage, Toast.LENGTH_LONG).show();
//            }
//        }).launchfromBackground(mContext);
    }

    public void makeSSOLoginRequest(String accessToken, String idToken, String expiry) {
        SSOLoginRequest.Params params = new SSOLoginRequest.Params(idToken, accessToken, expiry);
        SSOLoginRequest googleLogin = new SSOLoginRequest(new APICallback<BaseResponseData>() {
            @Override
            public void onResponse(APIResponse<BaseResponseData> response) {
                if (response != null && response.body() != null) {
                    if (response.body().code != 200 && response.body().code != 201) {
                        Toast.makeText(mContext, "Unable to login", Toast.LENGTH_LONG).show();
                        return;
                    }
                    if (response.body().code == 200 || response.body().code == 201) {
                        myplexAPI.clearCache(APIConstants.BASE_URL);
                        PropertiesHandler.clearCategoryScreenFilter();

                        if (!TextUtils.isEmpty(response.body().email)) {
                            PrefUtils.getInstance().setPrefEmailID(response.body().email);
                        }
                        PrefUtils.getInstance().setPrefLoginStatus("success");

                        Toast.makeText(mContext, "Login Successful", Toast.LENGTH_LONG).show();

                        try {
                            LoggerD.debugOTP("Info: msisdn login: " + "success" + " userid= " + response.body().userid);
                            PrefUtils.getInstance().setPrefUserId(Integer.parseInt(response.body().userid));
                            Util.setUserIdInMyPlexEvents(mContext);
                           /* if(!TextUtils.isEmpty(response.body().serviceName)) {
                                PrefUtils.getInstance().setServiceName(response.body().serviceName);
                            }*/
                            Analytics.mixpanelIdentify();
                            if (!TextUtils.isEmpty(response.body().email)) {
                                Analytics.setMixPanelEmail(response.body().email);
                            }

                            ComScoreAnalytics.getInstance().setEventLogin("NA", response.body().email, response.body().message, response.body().status);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        makeUserProfileRequest();

                    } else if (response.body().code == 401) {
                        if (response.body().message != null) {
                            Toast.makeText(mContext, response.body().message, Toast.LENGTH_LONG).show();
                        }
                    } else if (response.body().code == 423) {
                        if (response.body().message != null)
                            Toast.makeText(mContext, response.body().message, Toast.LENGTH_LONG).show();
                    } else if (response.body().code == 500) {
                        Toast.makeText(mContext, response.body().message, Toast.LENGTH_LONG).show();
                    } else if (response.body().code == 400) {
                        Toast.makeText(mContext, response.body().message, Toast.LENGTH_SHORT).show();
                    } else if (response.body().code == 403) {
                        DeviceUnRegRequest deviceUnregister = new DeviceUnRegRequest(new APICallback<BaseResponseData>() {
                            @Override
                            public void onResponse(APIResponse<BaseResponseData> response) {

                                if (response != null && response.body() != null) {
                                    if (response.body().code == 200) {
                                        makeSSOLoginRequest(accessToken, idToken, expiry);
                                    } else {

                                    }
                                }
                            }

                            @Override
                            public void onFailure(Throwable t, int errorCode) {

                            }
                        });
                        APIService.getInstance().execute(deviceUnregister);
                    } else {
                        Toast.makeText(mContext, "Unable to Login", Toast.LENGTH_LONG).show();
                    }
                } else {

                }
            }

            @Override
            public void onFailure(Throwable t, int errorCode) {

            }
        }, params);
        APIService.getInstance().execute(googleLogin);
    }

    public void loadNotification() {
        NotificationsListRequest mRequestFavourites = new NotificationsListRequest(
                new APICallback<NotificationList>() {
                    @Override
                    public void onResponse(APIResponse<NotificationList> response) {
                        if (response == null
                                || response.body() == null) {
                            return;
                        }
                        List<ResultNotification> notificationList = response.body().getResults();
                        int count = 0 ;
                        if(notificationList == null){
                            return;
                        }
                        if (notificationList.size() > 0) {
                            for(int i=0 ; i < notificationList.size() ;i++) {
                               if( notificationList.get(i).getStatus().equalsIgnoreCase("new") ) {
                                   count ++;
                               }
                            }
                        }
                        if(notifyMenuItem !=null) {
                            if (count != 0) {
                                notifyMenuItem.setIcon(buildCounterDrawable(count, R.drawable.ic_notification));
                            }
                            else {
                                notifyMenuItem.setIcon(R.drawable.ic_notification);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Throwable t, int errorCode) {
                        if (errorCode == APIRequest.ERR_NO_NETWORK) {
                            com.myplex.myplex.utils.AlertDialogUtil.showToastNotification(getString(R.string.network_error));
                            return;
                        }
                        com.myplex.myplex.utils.AlertDialogUtil.showToastNotification(getString(R.string.msg_fav_failed_update));
                    }
                });
        APIService.getInstance().execute(mRequestFavourites);

    }
    public void makeUserProfileRequest() {
        UserProfileRequest userProfileRequest = new UserProfileRequest(new APICallback<UserProfileResponseData>() {
            @Override
            public void onResponse(APIResponse<UserProfileResponseData> response) {
                if (response == null || response.body() == null) {
                    return;
                }
                if (response.body().code == 402) {
                    PrefUtils.getInstance().setPrefLoginStatus("");
                    return;
                }
                if (response.body().code == 200) {
                    UserProfileResponseData responseData = response.body();
                    if (responseData.result.profile.showForm) {
                        PrefUtils.getInstance().setIsToShowForm(true);
                        /*Intent ip=new Intent(mContext, MandatoryProfileActivity.class);
                        startActivityForResult(ip,PROFILE_UPDATE_REQUEST);*/
                    } else {
                        PrefUtils.getInstance().setIsToShowForm(false);
                    }
                    myplexAPISDK.ENABLE_FORCE_CACHE = true;
                    MenuDataModel.clearCache();
                    isToReloadData = true;
                    if (responseData.result.profile.first != null && !TextUtils.isEmpty(responseData.result.profile.first)) {
                        if (responseData.result.profile.last != null && !TextUtils.isEmpty(responseData.result.profile.last)) {
                            PrefUtils.getInstance().setPrefFullName(responseData.result.profile.first + " " + responseData.result.profile.last);
                        } else {
                            PrefUtils.getInstance().setPrefFullName(responseData.result.profile.first);
                        }
                    }
                    if (responseData.result.profile.mobile_no != null && !TextUtils.isEmpty(responseData.result.profile.mobile_no)) {
                        PrefUtils.getInstance().setPrefMobileNumber(responseData.result.profile.mobile_no);
                    }

                    if (responseData.result.profile.name != null && !TextUtils.isEmpty(responseData.result.profile.name)) {
                        PrefUtils.getInstance().setPrefFullName(responseData.result.profile.name);
                    }

                    if (responseData.result.profile.locations != null && responseData.result.profile.locations.size() > 0) {
                        if (responseData.result.profile.locations.get(0) != null
                                && !TextUtils.isEmpty(responseData.result.profile.locations.get(0)))
                            PrefUtils.getInstance().setUSerCountry(responseData.result.profile.locations.get(0));
                    }
                    if (responseData.result.profile.packageLanguages != null && responseData.result.profile.packageLanguages.size() > 0) {
                        if (responseData.result.profile.packageLanguages.get(0) != null
                                && !TextUtils.isEmpty(responseData.result.profile.packageLanguages.get(0)))
                            PrefUtils.getInstance().setSubscribedLanguage(responseData.result.profile.packageLanguages);
                        if(PrefUtils.getInstance().getAppLanguageToSendServer() == null || PrefUtils.getInstance().getAppLanguageToSendServer().isEmpty()) {
                            PrefUtils.getInstance().setAppLanguageToSendServer(responseData.result.profile.packageLanguages.get(0)/*"Tamil"*/);
                            PrefUtils.getInstance().setAppLanguageFirstTime("true");
                        }
                    } else {
                        if(PrefUtils.getInstance().getAppLanguageToSendServer() == null || PrefUtils.getInstance().getAppLanguageToSendServer().isEmpty()) {
                            PrefUtils.getInstance().setAppLanguageToSendServer("Tamil");
                            if(PrefUtils.getInstance().getAppLanguageFirstTime()==null || PrefUtils.getInstance().getAppLanguageFirstTime().isEmpty())
                                PrefUtils.getInstance().setAppLanguageFirstTime("true");
                        }
                          List<String> defaultPackage=new ArrayList<>();
                        defaultPackage.add("Tamil");
                        PrefUtils.getInstance().setSubscribedLanguage(defaultPackage);
                    }
                    if(responseData != null && responseData.result != null && responseData.result.profile != null && responseData.result.profile.packages != null) {
                        PrefUtils.getInstance().setPackages(responseData.result.profile.packages);
                    }

                    if (responseData.result.profile.state != null && !TextUtils.isEmpty(responseData.result.profile.state)) {
                        PrefUtils.getInstance().setUserState(responseData.result.profile.state);
                    }

                    if (responseData.result.profile.city != null && !TextUtils.isEmpty(responseData.result.profile.city)) {
                        PrefUtils.getInstance().setUserCity(responseData.result.profile.city);
                    }
                    if (responseData.result.profile.mobile_no != null && !TextUtils.isEmpty(responseData.result.profile.mobile_no)) {
                        PrefUtils.getInstance().setPrefMobileNumber(responseData.result.profile.mobile_no);
                    }
                    if (responseData.result.profile.smc_no != null && !TextUtils.isEmpty(responseData.result.profile.smc_no)) {
                        PrefUtils.getInstance().setPrefSmartCardNumber(responseData.result.profile.smc_no);
                    }

                    if (responseData.result.profile.dob != null && !TextUtils.isEmpty(responseData.result.profile.dob)) {
                        PrefUtils.getInstance().setUserDOB(responseData.result.profile.dob);
                    }

                    if (responseData.result.profile.gender != null && !TextUtils.isEmpty(responseData.result.profile.gender)) {
                        PrefUtils.getInstance().setUserGender(responseData.result.profile.gender);
                    }
                    if (responseData.result.profile.profile_image != null /*&& !TextUtils.isEmpty(responseData.result.profile.profile_image)*/) {
                        PrefUtils.getInstance().setString("PROFILE_IMAGE_URL", responseData.result.profile.profile_image);
                    }
                    if (responseData.result.profile.pendingSMCNumbers != null && responseData.result.profile.pendingSMCNumbers.size() > 0) {
                        if (responseData.result.profile.pendingSMCNumbers.get(0) != null
                                && !TextUtils.isEmpty(responseData.result.profile.pendingSMCNumbers.get(0)))
                            PrefUtils.getInstance().setPendingSMC(responseData.result.profile.pendingSMCNumbers);
                    }else{
                        PrefUtils.getInstance().setPendingSMC(new ArrayList<>());
                    }


                    if (responseData != null && responseData.result != null && responseData.result.profile != null &&
                            responseData.result.profile.mobile_no != null) {
                        FirebaseAnalytics.getInstance().setMobileNumberProperty(responseData.result.profile.mobile_no);
                    }

                    if (responseData != null && responseData.result != null && responseData.result.profile != null && responseData.result.profile.emails != null &&
                            !responseData.result.profile.emails.isEmpty() && responseData.result.profile.emails.size() > 0 && responseData.result.profile.emails.get(0).email != null) {
                        FirebaseAnalytics.getInstance().setEmailProperty(responseData.result.profile.emails.get(0).email);
                    }

                    enableNavigation();
                    updateNavMenuMyAccountSection();
                    //fetchMyPackages();
                    if(isShowHomePopUp) {
                        getHomePromotionPopUpBasedOnPackage();
                    }

                }
            }

            @Override
            public void onFailure(Throwable t, int errorCode) {
                SDKLogger.debug("Profile update onFailure");
            }
        });
        APIService.getInstance().execute(userProfileRequest);
    }

    private void showNewFilterMenuPopUp(View view) {
        mFilterMenuPopup = inflateFilterView(mContext);
//        Analytics.createScreenGA(Analytics.SCREEN_FILTER);
        FirebaseAnalytics.getInstance().createScreenFA(this, Analytics.SCREEN_FILTER);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int softButtonsHeight = UiUtil.getSoftButtonsBarSizePort(MainActivity.this)/* - UiUtil.SPACING_MOBILE_BETWEEN_CONTENT_LAYOUT_NAV_BAR*/;
        /*if (DeviceUtils.isTablet(this)) {
            softButtonsHeight = softButtonsHeight - UiUtil.SPACING_TABLET_BETWEEN_CONTENT_LAYOUT_NAV_BAR;
        }*/
        int popupHeight = dm.heightPixels - softButtonsHeight;

        mFilterMenuPopupWindow = new PopUpWindow(mFilterMenuPopup);
        if (UiUtil.hasSoftKeys(this)) {
            mFilterMenuPopupWindow = new PopUpWindow(mFilterMenuPopup, popupHeight, mToolbar);
        }
        mFilterMenuPopup.post(new Runnable() {
            @Override
            public void run() {
//                FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mFilterMenuPopup.getLayoutParams();
//                int softButtonsHeight = UiUtil.getSoftButtonsBarSizePort(MainActivity.this);
//                params.setMargins(0, 0, 0, 0);
//                params.bottomMargin = softButtonsHeight;
            }
        });
        mPopBlurredLayout = (RelativeLayout) mFilterMenuPopup.findViewById(R.id.filterLayout);
        mFilterLoadingTxt = (TextView) mFilterMenuPopup.findViewById(R.id.filter_loading_txt);
        mFilterMenuPopupWindow.attachPopupToView(view, new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                if (mToolbar != null) {
                    mToolbar.setVisibility(View.VISIBLE);
                }
                setFilterIcon(R.drawable.actionbar_filter_icon_default);
//                showSystemUI();
            }
        });
        mToolbar.hideOverflowMenu();
        fetchFilterData();
        try {
            setFilterIcon(R.drawable.actionbar_filter_icon_highlighted_icon);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private View inflateFilterView(Context mContext) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context
                .LAYOUT_INFLATER_SERVICE);
        if (inflater == null) {
            return null;
        }
        final View view = inflater.inflate(R.layout.popup_window_filters, null);
        tabLayout = view.findViewById(R.id.tab_layout);
        viewPager = view.findViewById(R.id.pager);
        btFilter = view.findViewById(R.id.btApply);
        tvReset = view.findViewById(R.id.tvReset);
        return view;
    }

    private List<FilterItem> mFilterLanguages;
    private List<FilterItem> mFilterGeners;
    private Button btFilter;
    private TextView tvReset;

    public void setData(List<FilterItem> mFilterLanguages, List<FilterItem> mFilterGeners) {
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        this.mFilterLanguages = mFilterLanguages;
        this.mFilterGeners = mFilterGeners;
        //TODO: hide loading textview
        adapter = new CustomPagerAdapter(mContext, mFilterLanguages, mFilterGeners, new CustomPagerAdapter.OnItemClickListener() {
            @Override
            public void onItemClicked(HashMap<Integer, ArrayList<String>> filterMap) {
                updateFilterData(filterMap);
            }
        });
        //TODO Added extra
        adapter.setFilterSectionType(mSectionType);
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
        adapter.notifyDataSetChanged();
        btFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (adapter != null && adapter instanceof CustomPagerAdapter) {
                    adapter.filterOnClickApply();
                    Analytics.mixpanelIncrementPeopleProperty(Analytics.MIXPANEL_PEOPLE_FILTERED_BY_CATEGORY);
                    closeFilterMenuPopup();
                }
            }
        });
        tvReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (adapter != null && adapter instanceof CustomPagerAdapter) {
                    adapter.reset();
                }
            }
        });
    }

    TabLayout tabLayout;
    ViewPager viewPager;
    CustomPagerAdapter adapter;

    private void makeSignInCheck(final String source, final String sourceDetails) {
        LoggerD.debugLog("checking for user sign in details");
        isLoginCheckInProgress = false;
        AlertDialogUtil.showProgressAlertDialog(MainActivity.this, "", getString(R.string.text_logging_in), true, false, null);

        MSISDNLoginEncrypted.Params msisdnParams = new MSISDNLoginEncrypted.Params(null, null, null, 20);

        MSISDNLoginEncrypted login = new MSISDNLoginEncrypted(msisdnParams,
                new APICallback<BaseResponseData>() {
                    @Override
                    public void onResponse(APIResponse<BaseResponseData> response) {
                        LoggerD.debugLog("response- " + response);
                        if (response == null
                                || response.body() == null) {
                            // login failed
                            //Log.d(TAG, "invalid response body");
                            String loginFailedMessage = PrefUtils.getInstance().getPrefAutoLoginFailedMessage();
                            if (TextUtils.isEmpty(loginFailedMessage))
                                AlertDialogUtil.showToastNotification(loginFailedMessage);
                            launchLoginActivity(source, sourceDetails);
                            AlertDialogUtil.dismissProgressAlertDialog();
                            return;
                        }
                        LoggerD.debugLog("success: msisdn login status : " + response.body().status + "code :" + response.body().code
                                + "message :" + response.body().message);
                        Map<String, String> params = new HashMap<>();
                        params.put(Analytics.ACCOUNT_TYPE, "");

                        if (response.body() != null && response.body().status != null && response.body().status.equalsIgnoreCase("SUCCESS")
                                && (response.body().code == 200
                                || response.body().code == 201)) {
                            if (!TextUtils.isEmpty(response.body().userid)) {
                                params.put(Analytics.USER_ID, response.body().userid);
                            }

                            CleverTap.eventRegistrationCompleted(response.body().email, response.body().mobile, Analytics.YES);
                            PrefUtils.getInstance().setPrefLoginStatus("success");
                            reloadData();
                            refreshNavigationDrawer();

                            if (ApplicationController.FLAG_ENABLE_OFFER_SUBSCRIPTION) {

                                if (response.body().mobile != null && !response.body().mobile.isEmpty()) {
                                    PrefUtils.getInstance().setPrefMsisdnNo(response.body().mobile);
                                    if (response.body().email != null)
                                        PrefUtils.getInstance().setPrefEmailID(response.body().email);
                                    MsisdnData msisdnData = new MsisdnData();
                                    msisdnData.operator = SubcriptionEngine.OPERATORS_NAME;
                                    msisdnData.msisdn = response.body().mobile;

                                    if (APIConstants.msisdnPath == null) {
                                        APIConstants.msisdnPath = mContext.getFilesDir() + "/" + "msisdn.bin";
                                    }
                                    SDKUtils.saveObject(msisdnData, APIConstants.msisdnPath);
                                }
                                //Log.d(TAG, "Info: msisdn login: " + "success and launching offer");
                                try {
                                    //Log.d(TAG, "Info: msisdn login: " + "success" + " userid= " + response.body().userid);
                                    PrefUtils.getInstance().setPrefUserId(Integer.parseInt(response.body().userid));
                                    Util.setUserIdInMyPlexEvents(mContext);
                                    Analytics.mixpanelIdentify();
                                    if (!TextUtils.isEmpty(response.body().email)) {
                                        Analytics.setMixPanelEmail(response.body().email);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                Analytics.mixpanelSetOncePeopleProperty(Analytics.ACCOUNT_TYPE, "");
                                Analytics.mixpanelSetOncePeopleProperty(Analytics.MIXPANEL_PEOPLE_JOINING_DATE, Util.getCurrentDate());
                                params.put(Analytics.JOINED_ON, Util.getCurrentDate());
                                Analytics.trackEvent(Analytics.EventPriority.HIGH, Analytics.EVENT_SIGN_UP_SUCCESS, params);
                                //fetchOfferAvailability(source, sourceDetails);
                                if (response.body().code == 201) {
                                    AppsFlyerTracker.eventLoginSuccessFirstTime(new HashMap<String, Object>());
                                } else if (response.body().code == 200) {
                                    AppsFlyerTracker.eventLoginSuccess(new HashMap<String, Object>());
                                }
                                AppsFlyerTracker.eventUserRegistrationCompleted(new HashMap<String, Object>());
                                // mBaseActivity.pushFragment(new OfferedPacksFragment());
                                return;
                            }
                            try {
                                //Log.d(TAG, "Info: msisdn login: " + "success" + " userid= " + response.body().userid);
                                PrefUtils.getInstance().setPrefUserId(Integer.parseInt(response.body().userid));
                                Util.setUserIdInMyPlexEvents(mContext);
                                Analytics.mixpanelIdentify();
                                if (!TextUtils.isEmpty(response.body().email)) {
                                    Analytics.setMixPanelEmail(response.body().email);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            params.put(Analytics.JOINED_ON, Util.getCurrentDate());
                            if (response.body().code == 201) {
                                AppsFlyerTracker.eventLoginSuccessFirstTime(new HashMap<String, Object>());
                            } else if (response.body().code == 200) {
                                AppsFlyerTracker.eventLoginSuccess(new HashMap<String, Object>());
                            }
                            AppsFlyerTracker.eventUserRegistrationCompleted(new HashMap<String, Object>());
                            Analytics.trackEvent(Analytics.EventPriority.HIGH, Analytics.EVENT_SIGN_UP_SUCCESS, params);
                            return;
                        }
                        AlertDialogUtil.dismissProgressAlertDialog();
                        launchLoginActivity(source, sourceDetails);
                        String loginFailedMessage = PrefUtils.getInstance().getPrefAutoLoginFailedMessage();
                        if (TextUtils.isEmpty(loginFailedMessage))
                            AlertDialogUtil.showToastNotification(loginFailedMessage);
                    }

                    @Override
                    public void onFailure(Throwable t, int errorCode) {
                        //Log.d(TAG, "Failed: " + t);
                        String loginFailedMessage = PrefUtils.getInstance().getPrefAutoLoginFailedMessage();
                        if (TextUtils.isEmpty(loginFailedMessage))
                            AlertDialogUtil.showToastNotification(loginFailedMessage);
                        AlertDialogUtil.dismissProgressAlertDialog();
                        launchLoginActivity(source, sourceDetails);
                    }
                });
        APIService.getInstance().execute(login);
    }

    private void fetchOfferAvailability(final String source, final String sourceDetails) {
//        showProgressBar();
        LoggerD.debugLog("check for offer availability");
        if (mCardData == null) {
            AlertDialogUtil.dismissProgressAlertDialog();
            launchLoginActivity(source, sourceDetails);
            return;
        }
        OfferedPacksRequest.Params params = new OfferedPacksRequest.Params(OfferedPacksRequest.TYPE_VERSION_5, APIConstants.PARAM_CONTENT_DETAIL, mCardData._id);
        final OfferedPacksRequest contentDetails = new OfferedPacksRequest(params, new APICallback<OfferResponseData>() {
            @Override
            public void onResponse(APIResponse<OfferResponseData> response) {
                LoggerD.debugLog("response- " + response);
                if (response == null || response.body() == null) {
                    AlertDialogUtil.dismissProgressAlertDialog();
                    Analytics.mixpanelEventUnableToFetchOffers(APIConstants.ERROR_RESPONSE_OR_RESPONSE_BODY_NULL, APIConstants.NOT_AVAILABLE);
                    checkAndLaunchPlayer(source, sourceDetails);
                    return;
                }

                if (!response.body().status.equalsIgnoreCase("SUCCESS")) {
                    Analytics.mixpanelEventUnableToFetchOffers("code: " + response.body().code + " message: " + response.body().message + " status: " + response.body().status, String.valueOf(response.body().code));
                    AlertDialogUtil.dismissProgressAlertDialog();
                    checkAndLaunchPlayer(source, sourceDetails);
                    return;
                }
                if (response.body().status.equalsIgnoreCase("SUCCESS")
                        && response.body().code == 216) {
//                    TODO GO TO DETAILS DESCRIPTION PAGE
                    AlertDialogUtil.dismissProgressAlertDialog();
                    checkAndLaunchPlayer(source, sourceDetails);
                    return;
                }

                if (response.body().status.equalsIgnoreCase("SUCCESS")) {
                    if (myplexAPISDK.ENABLE_WEB_SUBSCRIPTION_FLOW
                            && response.body().ui != null
                            && response.body().ui.action != null) {
                        switch (response.body().ui.action) {
                            case APIConstants.OFFER_ACTION_SHOW_MESSAGE:
                                PrefUtils.getInstance().setPrefOfferPackSubscriptionStatus(true);
                                if (!TextUtils.isEmpty(response.body().message)) {
                                    AlertDialogUtil.showNeutralAlertDialog(mContext, response.body().message, "", mContext.getString(R.string.dialog_ok), new AlertDialogUtil.NeutralDialogListener() {
                                        @Override
                                        public void onDialogClick(String buttonText) {
                                            checkAndLaunchPlayer(source, sourceDetails);
                                        }
                                    });
                                }
                                break;
                            case APIConstants.OFFER_ACTION_SHOW_TOAST:
                                PrefUtils.getInstance().setPrefOfferPackSubscriptionStatus(true);
                                if (!TextUtils.isEmpty(response.body().message)) {
                                    AlertDialogUtil.showToastNotification(response.body().message);
                                }
                                checkAndLaunchPlayer(source, sourceDetails);
                                break;
                            case APIConstants.APP_LAUNCH_WEB:
                                if (!TextUtils.isEmpty(response.body().ui.redirect)) {
//                                    startActivityForResult(SubscriptionWebActivity.createIntent(MainActivity.this, response.body().ui.redirect, SubscriptionWebActivity.PARAM_LAUNCH_NONE), SUBSCRIPTION_REQUEST);
                                }
                                break;
                            case APIConstants.APP_LAUNCH_NATIVE_OFFER:
                                launchPackagesScreen(PackagesFragment.PARAM_SUBSCRIPTION_TYPE_OFFER, source, sourceDetails);
                                break;
                            case APIConstants.APP_LAUNCH_NATIVE_SUBSCRIPTION:
                                launchPackagesScreen(PackagesFragment.PARAM_SUBSCRIPTION_TYPE_PACKAGES, source, sourceDetails);
                                break;
                            case APIConstants.APP_LAUNCH_HOME:
                                PrefUtils.getInstance().setPrefOfferPackSubscriptionStatus(true);
                                checkAndLaunchPlayer(source, sourceDetails);
                                break;
                            default:
                                break;
                        }
                        AlertDialogUtil.dismissProgressAlertDialog();
                        return;
                    }

                    boolean isOfferAvailable = false;
                    for (CardDataPackages offerPack : response.body().results) {
                        if (offerPack.subscribed) {
                            AlertDialogUtil.dismissProgressAlertDialog();
//                            TODO GOTO DETAILS PAGE
                            checkAndLaunchPlayer(source, sourceDetails);
                            return;
                        }
                        if (APIConstants.TYPE_OFFER.equalsIgnoreCase(offerPack.packageType)) {
                            isOfferAvailable = true;
                            break;
                        }
                    }
                    if (isOfferAvailable) {
                        AlertDialogUtil.dismissProgressAlertDialog();
                        launchPackagesScreen(PackagesFragment.PARAM_SUBSCRIPTION_TYPE_OFFER, source, sourceDetails);
                        return;
                    }
                    AlertDialogUtil.dismissProgressAlertDialog();
                    launchPackagesScreen(PackagesFragment.PARAM_SUBSCRIPTION_TYPE_PACKAGES, source, sourceDetails);
                }
            }

            @Override
            public void onFailure(Throwable t, int errorCode) {
                LoggerD.debugOTP("Failed: " + t);
                AlertDialogUtil.dismissProgressAlertDialog();
                launchLoginActivity(source, sourceDetails);
                if (errorCode == APIRequest.ERR_NO_NETWORK) {
                    Analytics.mixpanelEventUnableToFetchOffers(mContext.getString(R.string.network_error), APIConstants.NOT_AVAILABLE);
                    return;
                }
                Analytics.mixpanelEventUnableToFetchOffers(t == null || t.getMessage() == null ? APIConstants.NOT_AVAILABLE : t.getMessage(), APIConstants.NOT_AVAILABLE);
            }
        });

        APIService.getInstance().execute(contentDetails);
    }

    private void checkAndLaunchPlayer(String source, String sourceDetails) {
        if (Util.checkUserLoginStatus()) {
            if (mArgumentsOfPlayer != null) {
                mArgumentsOfPlayer.putBoolean(CardDetails.PARAM_AUTO_PLAY, false);
            }
            showDetailsFragment(mArgumentsOfPlayer, mCardData);
            return;
        }
        launchLoginActivity(source, sourceDetails);
    }

    private void launchPackagesScreen(int subscriptionType, String source, String sourceDetails) {
        if (mContext == null) {
            return;
        }
        //mContext.startActivity(LoginActivity.createIntent(mContext, true, true, subscriptionType, source, sourceDetails));
    }

    public boolean isMediaPlaying() {
        return mFragmentCardDetailsPlayer != null
                && mFragmentCardDetailsPlayer.mPlayer != null
                && mFragmentCardDetailsPlayer.mPlayer.isMediaPlaying();
    }


    /**
     * Method for adding Interstitial
     */

    public void cacheInterstitial() {
        //if (TextUtils.isEmpty(PrefUtils.getInstance().getPrefVmaxInterStitialAdId()) || !PrefUtils.getInstance().getPrefEnableVmaxInterStitialAd()) {
        if (TextUtils.isEmpty(PrefUtils.getInstance().getPrefVmaxInterStitialTabSwitchAdId()) || !PrefUtils.getInstance().getPrefEnableVmaxInterStitialTabswitchAd()) {
            SDKLogger.debug("Invalid AdSpotId or Interstitial Ads are disabled getPrefVmaxInterStitialTabswitchAdId- " + PrefUtils.getInstance().getPrefVmaxInterStitialTabSwitchAdId()
                    + " getPrefEnableVmaxInterStitialTabswitchAd- " + PrefUtils.getInstance().getPrefEnableVmaxInterStitialTabswitchAd());
            return;
        }

    }

    private void cacheBannerAds() {
        SDKLogger.debug("some thing is not right getPrefVmaxBannerAdId- " + PrefUtils.getInstance().getPrefVmaxBannerAdId()
                + " getPrefEnableVmaxFooterBannerAd- " + PrefUtils.getInstance().getPrefEnableVmaxFooterBannerAd());
        if (TextUtils.isEmpty(PrefUtils.getInstance().getPrefVmaxBannerAdId()) || !PrefUtils.getInstance().getPrefEnableVmaxFooterBannerAd()) {
            return;
        }

    }


    private void executeContentDetailRequest(FavouriteCheckRequest.Params contentDetailsParams) {

        final FavouriteCheckRequest contentDetails = new FavouriteCheckRequest(contentDetailsParams,
                new APICallback<FavouriteResponse>() {
                    String mAPIErrorMessage = null;

                    @Override
                    public void onResponse(APIResponse<FavouriteResponse> response) {
                        if (response == null || response.body() == null) {
                            onFailure(new Throwable(APIConstants.ERROR_RESPONSE_OR_RESPONSE_BODY_NULL), APIRequest.ERR_UN_KNOWN);
                            return;
                        }
                        //Log.d(TAG, "FavouriteRequest: onResponse: message - " + response.body().message);
                    }

                    @Override
                    public void onFailure(Throwable t, int errorCode) {
                    }
                });

        APIService.getInstance().execute(contentDetails);

    }


    private void postCheckFavoriteContent(final String contentId, final String type) {
        final FavouriteCheckRequest.Params contentDetailsParams = new FavouriteCheckRequest.Params(contentId, type);
        final FavouriteCheckRequest contentDetails = new FavouriteCheckRequest(contentDetailsParams,
                new APICallback<FavouriteResponse>() {
                    String mAPIErrorMessage = null;

                    @Override
                    public void onResponse(APIResponse<FavouriteResponse> response) {
                        if (response == null || response.body() == null) {
                            onFailure(new Throwable(APIConstants.ERROR_RESPONSE_OR_RESPONSE_BODY_NULL), APIRequest.ERR_UN_KNOWN);
                            return;
                        }
                        //Log.d(TAG, "FavouriteRequest: onResponse: message - " + response.body().message);
                        if (APIConstants.SUCCESS.equalsIgnoreCase(response.body().status)) {
                            if (response.body().favorite) {
                                AlertDialogUtil.showToastNotification("Already Added to Watchlist");
                                return;
                            }
                            FavouriteRequest.Params favouritesParams = new FavouriteRequest.Params(contentId, type);
                            executeFavouriteRequest(favouritesParams);
                        }
                    }

                    @Override
                    public void onFailure(Throwable t, int errorCode) {
                    }
                });

        APIService.getInstance().execute(contentDetails);

    }

    private void executeFavouriteRequest(final FavouriteRequest.Params favouritesParams) {

//                AlertDialogUtil.showToastNotification("Please wait while we update the data...");
        FavouriteRequest mRequestFavourites = new FavouriteRequest(favouritesParams,
                new APICallback<FavouriteResponse>() {
                    @Override
                    public void onResponse(APIResponse<FavouriteResponse> response) {
                        if (response == null
                                || response.body() == null) {
                            return;
                        }
                        Gson gson = new Gson();
                        Log.d("favouriteContent", "MainActivity" + gson.toJson(response.body()));
                        if (response.body().code == 402) {
                            PrefUtils.getInstance().setPrefLoginStatus("");
                            return;
                        }

                        //Log.d(TAG, "FavouriteRequest: onResponse: message - " + response.body().message);
                        if (APIConstants.SUCCESS.equalsIgnoreCase(response.body().status)) {
                            if (response.body().favorite) {
                                if (favouritesParams != null && favouritesParams.contentId != null && !TextUtils.isEmpty(favouritesParams.contentId)) {
                                    CleverTap.eventPromoVideoAddedToWatchList(APIConstants.NOT_AVAILABLE, favouritesParams.contentId, CleverTap.SOURCE_DETAILS_SCREEN);
                                }
                                AlertDialogUtil.showToastNotification("Added to Watchlist");
                            } else {
                                AlertDialogUtil.showToastNotification("Removed from Watchlist");
                            }
                        }
                    }

                    @Override
                    public void onFailure(Throwable t, int errorCode) {
                        //Log.d(TAG, "FavouriteRequest: onResponse: t- " + t);
                        if (errorCode == APIRequest.ERR_NO_NETWORK) {
                            AlertDialogUtil.showToastNotification(mContext.getString(R.string.network_error));
                            return;
                        }
                        AlertDialogUtil.showToastNotification(mContext.getString(R.string.msg_fav_failed_update));
                    }
                });
        APIService.getInstance().execute(mRequestFavourites);
    }

    private final void initScrollBehavior() {
        mAppBar.post(new Runnable() {
            @Override
            public void run() {
                if (mAppBar!=null && mAppBar.getLayoutParams() != null) {
                    CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) mAppBar.getLayoutParams();
                    AppBarLayout.Behavior behavior = (AppBarLayout.Behavior) params.getBehavior();
                    if (behavior == null) {
                        params.setBehavior(new AppBarLayout.Behavior());
                    }
                    if (behavior == null) {
                        mAppBar.post(this);
                        return;
                    }
                    behavior.setDragCallback(new AppBarLayout.Behavior.DragCallback() {
                        @Override
                        public boolean canDrag(@NonNull AppBarLayout appBarLayout) {
                            return getAllowScrollBar();
                        }
                    });
                }
            }
        });
    }

    private boolean getAllowScrollBar() {
        return allowAppBarScroll;
    }

    private void setAllowScrollBar(boolean allowAppBarScroll) {
        this.allowAppBarScroll = allowAppBarScroll;
    }

    private void minizePlayerAboveTabPageIndicator() {
        if (mDraggablePanel != null) {
            if(DeviceUtils.isTablet(mContext)){
                mDraggablePanel.setTopFragmentMarginBottom(getResources().getDimensionPixelSize(R.dimen.margin_gap_100));
            }else {
                mDraggablePanel.setTopFragmentMarginBottom(getResources().getDimensionPixelSize(R.dimen.margin_gap_70));
            }
//            if (mDraggablePanel.isMinimized())
          //  mDraggablePanel.minimize();
        }
    }

    private void minimizePlayerAtTabPageIndicator() {
        if (mDraggablePanel == null) return;
        mDraggablePanel.setTopFragmentMarginBottom(getResources().getDimensionPixelSize(R.dimen.margin_gap_8));
//        if (mDraggablePanel.isMinimized())
       // mDraggablePanel.minimize();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            //TODO: show Dialog
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    SDKLogger.debug("Don't ask again selected by user");
                    showDialog(true);
                } else {
                    showDialog(false);
                }
            }
            SDKLogger.debug("Mandatory permissions Not Accepted");
        } else {
            startDownloadContentPlayback();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void showDialog(final boolean shouldShowRationale) {
        android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle);
        if (!shouldShowRationale) {
            alertDialogBuilder.setMessage(getString(R.string.permission_warning_text));
            alertDialogBuilder.setTitle(getString(R.string.permission_text));
        } else {
            alertDialogBuilder.setMessage(getString(R.string.permission_warning_settings));
            alertDialogBuilder.setTitle(getString(R.string.permission_text));
        }
        alertDialogBuilder.setPositiveButton(shouldShowRationale ? getString(R.string.accept_from_settings) : getString(R.string.accept), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (shouldShowRationale) {
                    //TODO: Redirect to settings
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivityForResult(intent, REQUEST_PERMISSION_SETTING);
                } else
                    checkStoragePermissionForDownload();

            }
        });
        android.app.AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }

    @Override
    protected void onRestart() {
        isOpen = true;
        super.onRestart();
        if (shouldCheckAppVersionUpgrade) {
            checkAppVersionUpgrade();
            shouldCheckAppVersionUpgrade = false;
        }
    }

    RatingScreen ratingScreen;

    public void checkInAppRatingPopUp() {
        RatingScreen ratingScreen = PropertiesHandler.getRatingScreenConfig(mContext);
        if (ratingScreen != null) {
            if (!TextUtils.isEmpty(ratingScreen.rating_popup_enabled) && ratingScreen.rating_popup_enabled.equalsIgnoreCase("true")
                    && PrefUtils.getInstance().isUserNeedToGiveRating()) {
                if (Util.isNetworkAvailable(mContext)) {

                    if (ratingScreen.intervalas != null) {
                        ArrayList<String> myList = new ArrayList<>(Arrays.asList(ratingScreen.intervalas.split(",")));
                        if (myList != null) {

                            boolean isPopUPNeedTOBeShown = showPopUp(myList, Long.valueOf(PrefUtils.getInstance().getTotalMOU()), ratingScreen.recurring_interval);
                            if (isPopUPNeedTOBeShown) {
                                showRateTheAppPopUp();
                            }
                        }
                    }


                }
            }
        }
    }

    int rating;
    AdapterRatingFeedBackPopUP adapterRatingFeedBackPopUP;
    TextView textView;
    AlertDialog ratePopupDialog, ratePopupFeedBackDialog;

    //New method to Show App rating popUp
    private void showRateTheAppPopUp() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rateView;
        if (inflater != null) {
            rateView = inflater.inflate(R.layout.app_rating_pop_up, null);
        } else {
            return;
        }
        TextView notNowText = rateView.findViewById(R.id.not_now_text);
        TextView ratingMessage = rateView.findViewById(R.id.ratingMessage);
        TextView titleLabel = rateView.findViewById(R.id.titleLabel);
        final TextView rateAppText = rateView.findViewById(R.id.rate_app_text);
        final RatingbarCustom ratingBar = rateView.findViewById(R.id.ratingStars);
        final RatingScreen ratingScreen = PropertiesHandler.getRatingScreenConfig(mContext);
        if (ratingScreen != null) {
            if (ratingScreen.rating_scrn_text1 != null && !ratingScreen.rating_scrn_text1.isEmpty()) {
                titleLabel.setTextSize(16);
                titleLabel.setTextColor(mContext.getResources().getColor(R.color.color_222222));
                titleLabel.setText(ratingScreen.rating_scrn_text1);
            }
            if (ratingScreen.rating_scrn_text2 != null && !ratingScreen.rating_scrn_text2.isEmpty()) {
                ratingMessage.setTextSize(12);
                ratingMessage.setTextColor(mContext.getResources().getColor(R.color.color_222222));
                ratingMessage.setText(ratingScreen.rating_scrn_text2);
            }
            if (ratingScreen.rate_pstv_lbl != null && !ratingScreen.rate_pstv_lbl.isEmpty()) {
                rateAppText.setTextSize(16);
                rateAppText.setTextColor(mContext.getResources().getColor(R.color.color_222222));
                rateAppText.setText(ratingScreen.rate_pstv_lbl);
            }
            if (ratingScreen.rate_ngtv_lbl != null && !ratingScreen.rate_ngtv_lbl.isEmpty()) {
                notNowText.setTextSize(16);
                notNowText.setTextColor(mContext.getResources().getColor(R.color.color_222222));
                notNowText.setAlpha(0.6f);
                notNowText.setText(ratingScreen.rate_ngtv_lbl);
            }

            ComScoreAnalytics.getInstance().setEventPopUpShowed("NA", "rating", ratingScreen.rating_scrn_text2);

        } else {
            return;
        }
        builder.setView(rateView);

        ratePopupDialog = builder.create();
        if (ratePopupDialog == null) {
            return;
        }
        if (ratePopupDialog != null && ratePopupDialog.getWindow() != null) {
            ratePopupDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        ratePopupDialog.show();

        ratePopupDialog.setCancelable(false);

        notNowText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ratePopupDialog != null) {
                    ratePopupDialog.dismiss();
                    CleverTap.eventRatingPopUpShown(APIConstants.NOT_AVAILABLE, APIConstants.IGNORED);
                    ComScoreAnalytics.getInstance().setEventPopUpResponse("NA", "rating", "skipped", "NA");
                    PrefUtils.getInstance().setAppLaunchCount(0);
                }
                //Analytics.setMixPanelRatingPopUpResponse("1","rating", "skipped",  "not now");

            }
        });
        rateAppText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rating = (int) ratingBar.getRating();
                if (rating > 0) {
                    if (ratePopupDialog != null) {
                        ratePopupDialog.dismiss();
                        CleverTap.eventRatingPopUpShown(String.valueOf(rating), APIConstants.RATED);
                        PrefUtils.getInstance().setAppLaunchCount(0);
                        ComScoreAnalytics.getInstance().setEventPopUpResponse("NA", "rating", "rated", String.valueOf(rating));
                    }
                    //Analytics.setMixPanelRatingPopUpResponse("1","rating", "rated",  "rate the app");
                    if (rating >= ratingScreen.min_rating_for_store_page) {
                        PrefUtils.getInstance().setUserNeedToGiveRating(false);
                        takeToPlayStore();
                    } else {
                        PrefUtils.getInstance().setUserNeedToGiveRating(true);
                        showFeedBackPopUpAfterNegativeRating();
                    }
                } else {
                    if (ratingScreen != null && ratingScreen.rate_not_submitted != null && !ratingScreen.rate_not_submitted.isEmpty()) {
                        //Toast.makeText(mContext, ratingScreen.rate_not_submitted, Toast.LENGTH_SHORT).show();
                        AlertDialogUtil.showToastNotification(ratingScreen.rate_not_submitted);
                    }
                }
            }
        });

    }

    public void setEditTextMaxLength(EditText edt_text, int length) {
        //length = 10;
        InputFilter[] filterArray = new InputFilter[1];
        filterArray[0] = new InputFilter.LengthFilter(length);
        edt_text.setFilters(filterArray);
    }

    private final TextWatcher mTextEditorWatcher = new TextWatcher() {
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
            //This sets a textview to the current length
            if (textView != null) {
                String stringLength = String.valueOf(s.length());
                if (stringLength != null && !stringLength.isEmpty() && ratingScreen != null && ratingScreen.text_char_limit > 0) {
                    textView.setText(stringLength + "/" + ratingScreen.text_char_limit);
                } else {
                    textView.setVisibility(View.INVISIBLE);
                }

            }
        }

        public void afterTextChanged(Editable s) {
        }
    };

    private void showFeedBackPopUpAfterNegativeRating() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rateView = null;
        if (inflater != null) {
            rateView = inflater.inflate(R.layout.app_rating_pop_up_feedback, null);
        } else {
            return;
        }
        TextView skipLabel = rateView.findViewById(R.id.skip_text);
        TextView desc = rateView.findViewById(R.id.feedbackMessage);
        TextView titleLabelfeedback = rateView.findViewById(R.id.feedbackTitleLabel);
        TextView submit_text = rateView.findViewById(R.id.submit_text);
        final EditText feedbackBox = rateView.findViewById(R.id.multiAutoCompleteTextView);
        RecyclerView selectionRecycler = rateView.findViewById(R.id.selection_list);
        final RatingScreen ratingScreen = PropertiesHandler.getRatingScreenConfig(mContext);
        textView = rateView.findViewById(R.id.textCount);
        textView.setAlpha(0.6f);
        adapterRatingFeedBackPopUP = null;
        if (ratingScreen != null && ratingScreen.text_char_limit > 0) {
            setEditTextMaxLength(feedbackBox, ratingScreen.text_char_limit);
        }
        if (ratingScreen != null && ratingScreen.feedback_options != null) {
            adapterRatingFeedBackPopUP = new AdapterRatingFeedBackPopUP(mContext, ratingScreen.feedback_options);
            final CustomHorizontalScroll layoutManager = new CustomHorizontalScroll(mContext, LinearLayoutManager.HORIZONTAL, false);
            if (selectionRecycler != null) {
                selectionRecycler.setLayoutManager(layoutManager);
                selectionRecycler.setAdapter(adapterRatingFeedBackPopUP);
                HorizontalItemDecorator mHorizontalMoviesDivieder = new HorizontalItemDecorator(1);
                selectionRecycler.addItemDecoration(mHorizontalMoviesDivieder);
            }
        }
        if (ratingScreen == null) {
            return;
        }
        titleLabelfeedback.setTextSize(16);
        titleLabelfeedback.setTextColor(mContext.getResources().getColor(R.color.color_222222));
        if (titleLabelfeedback != null && !TextUtils.isEmpty(ratingScreen.feedback_screen_text1)) {
            titleLabelfeedback.setText(ratingScreen.feedback_screen_text1);
        }
        if (ratingScreen.feedback_screen_text2 != null && !ratingScreen.feedback_screen_text2.isEmpty()) {
            desc.setTextSize(12);
            desc.setTextColor(mContext.getResources().getColor(R.color.color_222222));
            desc.setText(ratingScreen.feedback_screen_text2);
        }
        if (ratingScreen.feedback_ngtv_lbl != null && !ratingScreen.feedback_ngtv_lbl.isEmpty()) {
            skipLabel.setTextSize(16);
            skipLabel.setTextColor(mContext.getResources().getColor(R.color.color_222222));
            skipLabel.setAlpha(0.6f);
            skipLabel.setText(ratingScreen.feedback_ngtv_lbl);
        }
        if (ratingScreen.fedback_pstv_lbl != null && !ratingScreen.fedback_pstv_lbl.isEmpty()) {
            submit_text.setTextSize(16);
            submit_text.setTextColor(mContext.getResources().getColor(R.color.color_222222));
            submit_text.setText(ratingScreen.fedback_pstv_lbl);
        }
        if (ratingScreen.feedback_txt_box_text != null && !ratingScreen.feedback_txt_box_text.isEmpty()) {
            feedbackBox.setHint(ratingScreen.feedback_txt_box_text);
            feedbackBox.addTextChangedListener(mTextEditorWatcher);
        }


        builder.setView(rateView);
        ratePopupFeedBackDialog = builder.create();
        /*if (ratePopupFeedBackDialog != null && ratePopupFeedBackDialog.getWindow() != null) {
            ratePopupDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }*/
        //PrefUtils.getInstance().setString(LAST_SAVED_DATE,Util.getStringFromDate(Util.getCurrentDate()));
        ratePopupFeedBackDialog.show();

        ratePopupFeedBackDialog.setCancelable(false);

        skipLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ratePopupFeedBackDialog != null) {
                    sendFeedbackToServer(ratingScreen.feedback_ngtv_lbl);
                    ratePopupFeedBackDialog.dismiss();

                }


            }
        });
        submit_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ratePopupFeedBackDialog != null) {
                    String selectedCategory = adapterRatingFeedBackPopUP.getSelectedFields();
                    RatingScreen ratingScreen1 = PropertiesHandler.getRatingScreenConfig(mContext);

                    if (selectedCategory != null && !selectedCategory.isEmpty()) {
                        String feedback = "";
                        if (feedbackBox.getText() != null) {
                            feedback = feedbackBox.getText().toString();
                            sendFeedbackToServer(feedback + " | " + selectedCategory);
                        } else {
                            sendFeedbackToServer(" | " + selectedCategory);
                        }
                        //TODO: change the toast message here from properties.
                        if (ratingScreen != null && !TextUtils.isEmpty(ratingScreen.feedback_submitted))
                            //Toast.makeText(mContext, ratingScreen.feedback_submitted, Toast.LENGTH_LONG).show();
                            AlertDialogUtil.showToastNotification(ratingScreen.feedback_submitted);
                        ratePopupFeedBackDialog.dismiss();
                    } else {
                        if (ratingScreen1 != null
                                && ratingScreen1.feedback_not_submitted != null && !ratingScreen1.feedback_not_submitted.isEmpty()) {
                            //Toast.makeText(mContext,ratingScreen1.feedback_not_submitted,Toast.LENGTH_SHORT).show();
                            AlertDialogUtil.showToastNotification(ratingScreen.feedback_not_submitted);
                        } else {
                            ratePopupFeedBackDialog.dismiss();
                        }

                    }
                }

            }
        });
    }

    private void sendFeedbackToServer(String feedback) {
        String contentId = PrefUtils.getInstance().getLastContentIDPlayed();
        if (!(contentId != null && !contentId.isEmpty())) {
            contentId = "0";
        }


        CommentsMessagePost.Params commentsPostParams = new CommentsMessagePost.Params(contentId, APIConstants.RATING, feedback, rating);

        CommentsMessagePost commentsPostRequest = new CommentsMessagePost
                (commentsPostParams, new APICallback<BaseResponseData>() {
                    @Override
                    public void onResponse(APIResponse<BaseResponseData> response) {


                    }

                    @Override
                    public void onFailure(Throwable t, int errorCode) {

                    }
                });

        APIService.getInstance().execute(commentsPostRequest);

    }


    private void takeToPlayStore() {
        PrefUtils.getInstance().setDidUserRateTheApp(true);
        final String appPackageName = getPackageName(); // package name of the app
        try {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=" + appPackageName)));
        } catch (android.content.ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
        }
    }

    private boolean showPopUp(ArrayList<String> intervals, Long mou, int recurringINterval) {
        if (intervals == null) {
            return false;
        }
        BigDecimal mouBig = new BigDecimal(mou);
        Log.e("INAPP", "#########");
        Log.e("INAPP", "PrefUtils.getInstance().getWhichElementToBeCompared()  " + PrefUtils.getInstance().getWhichElementToBeCompared());
        Log.e("INAPP", "mou  " + mou);
        if (PrefUtils.getInstance().getWhichElementToBeCompared() == 0) {
            int interval = PrefUtils.getInstance().getWhichElementToBeCompared();
            BigDecimal intervalBig = new BigDecimal(Integer.parseInt(intervals.get(PrefUtils.getInstance().getWhichElementToBeCompared())));
            Log.e("INAPP", "interval  " + interval);
            if (mouBig.compareTo(intervalBig) == -1) {
                return false;
            } else {
                long newMOU = mou + Integer.parseInt(intervals.get(PrefUtils.getInstance().getWhichElementToBeCompared() + 1));
                PrefUtils.getInstance().setBaseMOUForAppRating(newMOU);
                PrefUtils.getInstance().setWhichElementToBeCompared(1);
                Log.e("INAPP", "Increment for 0");
                Log.e("INAPP", "setBaseMOUForAppRating  " + PrefUtils.getInstance().getBaseMOUForAppRating());
                Log.e("INAPP", "getWhichElementToBeCompared  " + PrefUtils.getInstance().getWhichElementToBeCompared());
                return true;
            }
        }


        int intervalWhichNeedToBeAdded = 0;
        if (intervals.size() > PrefUtils.getInstance().getWhichElementToBeCompared()) {
            intervalWhichNeedToBeAdded = Integer.parseInt(intervals.get(PrefUtils.getInstance().getWhichElementToBeCompared()));
        } else {
            intervalWhichNeedToBeAdded = recurringINterval;
        }
        Log.e("INAPP", "PrefUtils.getInstance().getBaseMOUForAppRating()  " + PrefUtils.getInstance().getBaseMOUForAppRating());
        Log.e("INAPP", "intervalWhichNeedToBeAdded  " + intervalWhichNeedToBeAdded);
        if (mouBig.compareTo(new BigDecimal(PrefUtils.getInstance().getBaseMOUForAppRating())) == -1) {
            return false;
        } else {

            int newElementForComparison = PrefUtils.getInstance().getWhichElementToBeCompared() + 1;
            PrefUtils.getInstance().setWhichElementToBeCompared(newElementForComparison);
            long newbasaeMOU = mou + intervalWhichNeedToBeAdded;
            PrefUtils.getInstance().setBaseMOUForAppRating(newbasaeMOU);
            Log.e("INAPP", "Increment");
            Log.e("INAPP", "setBaseMOUForAppRating  " + PrefUtils.getInstance().getBaseMOUForAppRating());
            Log.e("INAPP", "getWhichElementToBeCompared  " + PrefUtils.getInstance().getWhichElementToBeCompared());
            return true;
        }
    }

    public void doLogout() {
        PrefUtils.getInstance().setPrefLoginStatus("");
        PrefUtils.getInstance().setPrefMsisdnNo("");
        PrefUtils.getInstance().setPrefUserId(0);
        PrefUtils.getInstance().setPrefFullName("");
        PrefUtils.getInstance().setIsToShowForm(false);
        PrefUtils.getInstance().setUserGenderRange("");
        PrefUtils.getInstance().setUserGender("");
        PrefUtils.getInstance().setUserAgeRange("");
        PrefUtils.getInstance().setAdVideoCount(1);
        PrefUtils.getInstance().setUSerCountry("");
        PrefUtils.getInstance().setUserState("");
        PrefUtils.getInstance().setUserCity("");
        PrefUtils.getInstance().setPopup(false);
        PrefUtils.getInstance().setString("PROFILE_IMAGE_URL","");
        PrefUtils.getInstance().setAppLanguageToSendServer("");
        PrefUtils.getInstance().setSubscribedLanguage(null);
        PrefUtils.getInstance().setPackages(null);
        clearAllarms();

        Util.setUserIdInMyPlexEvents(mContext);
//        ((MainActivity)mContext).reloadData();
//        if (isFacebookLoggedIn()) {
//            LoginManager.getInstance().logOut();
//        }
        if (isGmailSignedIn()) {
            makeGmailSignOut();
        }
        ApplicationController.clearPackagesList();
        //       SSO.getSsoConfig().LogOut(mContext);
        ///PrefUtils.getInstance().clearAllForLogout();
        updateNavMenuMyAccountSection();
        ComScoreAnalytics.getInstance().setEventLogout();
        myplexAPI.clearCache(APIConstants.BASE_URL);
        MenuDataModel.clearCache();
        PrefUtils.getInstance().setDefaultServiceName(getResources().getString(R.string.serviceName));
        enableNavigation();
        reloadData();
        refreshNavigationDrawer();
    }

//    private boolean isFacebookLoggedIn() {
//        AccessToken accessToken = AccessToken.getCurrentAccessToken();
//        return accessToken != null;
//    }

    private boolean isGmailSignedIn() {
        return GoogleSignIn.getLastSignedInAccount(mContext) != null;
    }

    private void makeGmailSignOut() {
        String serverClientId = mContext.getResources().getString(R.string.server_client_id);
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(serverClientId)
                .requestServerAuthCode(serverClientId)
                .requestEmail()
                .build();
        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(mContext, googleSignInOptions);
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull com.google.android.gms.tasks.Task<Void> task) {

                    }
                });
    }

    private void refreshNavigationDrawer() {
        if (mListCarouselInfo != null && mListCarouselInfo.size() > 0) {
            updateImagesForNavigationDrawer(mListCarouselInfo);
        } else {
            myplexAPISDK.ENABLE_FORCE_CACHE = true;
            new MenuDataModel().fetchMenuList(getString(R.string.MENU_TYPE_GROUP_ANDROID_NAV_MENU), 1, APIConstants.PARAM_CAROUSEL_API_VERSION, new MenuDataModel.MenuDataModelCallback() {
                @Override
                public void onCacheResults(List<CarouselInfoData> dataList) {
                }

                @Override
                public void onOnlineResults(List<CarouselInfoData> dataList) {
                    if (dataList == null) {
                        showRetryOption(true);
                        return;
                    }
                    mListCarouselInfo = dataList;
                    if (dataList.size() > 0) {
                        updateImagesForNavigationDrawer(mListCarouselInfo);

                    }
                }

                @Override
                public void onOnlineError(Throwable error, int errorCode) {
                }
            });
        }
    }

    private void updateImagesForNavigationDrawer(final List<CarouselInfoData> listCarouselInfo) {
        Util.setImageCount(0);
        handler = new Handler();
        saveMenuIconsOffline(listCarouselInfo);
        final int tabCount = listCarouselInfo.size();
        runnable = new Runnable() {
            @Override
            public void run() {
                if (isFinishing()) {
                    return;
                }
                if (Util.allImagesLoaded(getAllMenuImagesDataOnly(listCarouselInfo).size())) {
                    prepareDrawerRecycleView();
                } else {
                    updateImagesForNavigationDrawer(listCarouselInfo);
                }
            }
        };
        handler.postDelayed(runnable, 400);
    }

    private List<CarouselInfoData> getAllMenuImagesDataOnly(List<CarouselInfoData> tabsData) {
        List<CarouselInfoData> tabsList = new ArrayList<>();
        for (int p = 0; p < tabsData.size(); p++) {
            if (tabsData.get(p).images.size() != 0) {
                tabsList.add(tabsData.get(p));
            }
        }
        return tabsList;
    }

    public int getScreenOrientation() {
        Log.e("MINICARDVIDEO PLAYER::", "ORIENTATION METHOD");

        int orientation;

        DisplayMetrics dm = new DisplayMetrics();
        Display getOrient = ((Activity) mContext).getWindowManager().getDefaultDisplay();
        getOrient.getMetrics(dm);

        if (dm.widthPixels < dm.heightPixels) {
            if (DeviceUtils.isTablet(mContext)) {
                orientation = ActivityInfo.SCREEN_ORIENTATION_USER;
            } else {
                orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
            }
        } else {
            orientation = SCREEN_ORIENTATION_SENSOR_LANDSCAPE;
        }
        return orientation;

    }

    public int getPositionInBottomTab(String name) {
        int i = 0, z = 0;
        if (tabListData != null && tabListData.size() > 0) {
            for (; i < tabListData.size(); i++) {

                if (tabListData.get(i).title.equalsIgnoreCase(name)) {
                    return i;
                }

            }
        }

        return 0;

    }


    public int getPositionInCarousalWhenClickedBottomTab(String name) {
        int i = 0;
        if (mListCarouselInfoDrawer == null) {
            if (mListCarouselInfo != null && mListCarouselInfo.size() > 0) {
                //Filtering the carouselData based on state variable to show in NavDrawer
                mListCarouselInfoDrawer = getItemsToShowFromCarouselInfoData(mListCarouselInfo);
            }
        }
        if (mListCarouselInfo != null && mListCarouselInfo.size() > 0) {
            for (; i < mListCarouselInfo.size(); i++) {

                if (mListCarouselInfo.get(i).title.equalsIgnoreCase(name)) {
                    return i;
                }

            }
        }

        return 0;

    }

    public int getPositionInSideNavigaitonWhenClickedBottomTab(String name) {
        int i = 0;
        if (mListCarouselInfoDrawer != null && mListCarouselInfoDrawer.size() > 0) {
            for (; i < mListCarouselInfoDrawer.size(); i++) {

                if (mListCarouselInfoDrawer.get(i).title.equalsIgnoreCase(name)) {
                    return i;
                }

            }
        }
        return -1;
    }

    public boolean isConnected() {
        try {
            CastSession castSession = CastContext.getSharedInstance(mContext)
                    .getSessionManager()
                    .getCurrentCastSession();
            return (castSession != null && castSession.isConnected());
        } catch (Exception e) {
            e.printStackTrace();
            checkPlayServices();
            return false;
        }
    }


    Timer timer;
    TimerTask timerTask;

    public void startTimer() {
        //set a new Timer
        timer = new Timer();
        Log.e("Timer", "Started Timer");
        //initialize the TimerTask's job
        initializeTimerTask();
        //schedule the timer, after the SEARCH_TIMER_DELAY
        timer.schedule(timerTask, SEARCH_TIMER_DELAY); //
    }

    public void stoptimertask() {
        //stop the timer, if it's not already null
        Log.e("Timer", "Cancelled Timer");
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    public void initializeTimerTask() {

        timerTask = new TimerTask() {
            public void run() {

                //use a handler to run a toast that shows the current timestamp
                handler.post(new Runnable() {
                    public void run() {
                        //changeFrequency = 0;
                        LoggerD.debugLog("showSearchFragment from mSearchView.onQueryTextChange newText- " + mSearchQuery);
                        searchedQuery = mSearchQuery;
                        if ((mSearchSuggestionFrag != null && mSearchSuggestionFrag.isAdded()) || (mSearchSuggestionsWithFilterFrag != null && mSearchSuggestionsWithFilterFrag.isAdded())) {
                            //if ((mSearchSuggestionFrag != null && mCurrentFragment != null && mCurrentFragment instanceof SearchSuggestions) || (mSearchSuggestionsWithFilterFrag != null && mCurrentFragment != null && mCurrentFragment instanceof SearchSuggestionsWithFilter))
                            Log.e("Timer", "Query Sent");
                            showSearchFragment(mSearchQuery, true);
                            stoptimertask();
                        }


                    }
                });
            }
        };
    }


    //Called from FragmentCarouselInfo, When recyclerView is Scrolled
    //Mainly used for Retractable footer
    @Override
    public void onViewScrolledUp() {

        if (mTabPagerRootLayout != null && mTabPagerRootLayout.getVisibility() != View.VISIBLE && !isAnimating) {
            mTabPagerRootLayout.clearAnimation();
            Animation tabPagerShowAnim = AnimationUtils.loadAnimation(mContext, R.anim.slide_up_anim);
            tabPagerShowAnim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    isAnimating = true;
                }
                @Override
                public void onAnimationEnd(Animation animation) {
                    if(isPushedFragment)
                        return;
                    isAnimating = false;
                    mTabPagerRootLayout.setVisibility(View.VISIBLE);
                    minizePlayerAboveTabPageIndicator();
                    //Fixed the content playing in doc player in tab , when moving to portrait from landscape when coming back from account screen
//                    if(!DeviceUtils.isTablet(mContext)){
                        if(mDraggablePanel != null && mDraggablePanel.isMinimized()){
                            mDraggablePanel.minimize();
                        }
                   /* }else if(mDraggablePanel != null ){
                        mDraggablePanel.minimize();
                    }*/


                    if(mDraggablePanel != null && mDraggablePanel.isMaximized()) {
                        minimizePlayerAtTabPageIndicator();
                        mTabPagerRootLayout.setVisibility(GONE);
                    }

                }
                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            //ANIM for VMAX FOOTER_AD
            /*Animation footerADShowAnim = new Animation() {
                @Override
                protected void applyTransformation(float interpolatedTime, Transformation t) {

                    if (vmaxBannerAdFrameParent != null ) {
                        vmaxLayoutParams.bottomMargin = (int) (ORIGINAL_VMAX_PARENT_BOTTOM_PARAM * interpolatedTime);
                        vmaxBannerAdFrameParent.setLayoutParams(vmaxLayoutParams);
                    }
                }
            };
            footerADShowAnim.setDuration(200);*/
           /* if (vmaxBannerAdFrameParent != null && vmaxBannerAdFrameParent.getVisibility() == View.VISIBLE) {
                vmaxBannerAdFrameParent.startAnimation(footerADShowAnim);
            }*/
            if (mTabPagerRootLayout != null) {
                if (vmaxBannerAdFrameParent != null) {
                    vmaxLayoutParams.bottomMargin = ORIGINAL_VMAX_PARENT_BOTTOM_PARAM;

                    if (vmaxBannerAdFrameParent.getLayoutTransition() != null) {
                        vmaxBannerAdFrameParent.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
                    }
                    vmaxBannerAdFrameParent.setLayoutParams(vmaxLayoutParams);
                }
                mTabPagerRootLayout.startAnimation(tabPagerShowAnim);
               // mAppBar.startAnimation(tabPagerDismissAnim);
            }
        }
    }

    @Override
    public void onViewScrolledDown() {

        if (mTabPagerRootLayout != null && mTabPagerRootLayout.getVisibility() == View.VISIBLE && !isAnimating) {
            mTabPagerRootLayout.clearAnimation();
            Animation tabPagerDismissAnim = AnimationUtils.loadAnimation(mContext, R.anim.slide_down_anim);
            tabPagerDismissAnim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    isAnimating = true;
                }
                @Override
                public void onAnimationEnd(Animation animation) {
                    isAnimating = false;
                    mTabPagerRootLayout.setVisibility(GONE);
                    minimizePlayerAtTabPageIndicator();
                    if(mDraggablePanel != null)
                        mDraggablePanel.minimize();
                }
                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
            //Anim for VMAX FOOTER AD
          /*  Animation vmaxFooterDismissAnim = new Animation() {
                @Override
                protected void applyTransformation(float interpolatedTime, Transformation t) {
                    if (vmaxBannerAdFrameParent != null) {
                        vmaxLayoutParams.bottomMargin = (int)interpolatedTime;
                        vmaxBannerAdFrameParent.setLayoutParams(vmaxLayoutParams);
                    }
                }
            };
            vmaxFooterDismissAnim.setDuration(200);*/
           /* if (vmaxBannerAdFrameParent != null && vmaxBannerAdFrameParent.getVisibility() == View.VISIBLE) {
                vmaxBannerAdFrameParent.startAnimation(vmaxFooterDismissAnim);
            }*/
            if (mTabPagerRootLayout != null) {
                mTabPagerRootLayout.startAnimation(tabPagerDismissAnim);
                if (vmaxBannerAdFrameParent != null) {
                    vmaxLayoutParams.bottomMargin = 0;

                    if (vmaxBannerAdFrameParent.getLayoutTransition() != null) {
                        vmaxBannerAdFrameParent.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
                    }
                    vmaxBannerAdFrameParent.setLayoutParams(vmaxLayoutParams);

                }
            }
        }

    }


    @Override
    public void onScrolledToEnd() {
        if (mTabPagerRootLayout != null) {
            mTabPagerRootLayout.clearAnimation();
            Animation tabPagerShowAnim = AnimationUtils.loadAnimation(mContext, R.anim.slide_up_anim);
            tabPagerShowAnim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    isAnimating = true;

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    if(isPushedFragment)
                        return;
                    isAnimating = false;
                    mTabPagerRootLayout.setVisibility(View.VISIBLE);
                    minizePlayerAboveTabPageIndicator();
                    if(mDraggablePanel != null)
                        mDraggablePanel.minimize();
                   // mAppBar.setVisibility(GONE);

                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });

            //ANIM for VMAX FOOTER_AD
            /*Animation footerADShowAnim = new Animation() {
                @Override
                protected void applyTransformation(float interpolatedTime, Transformation t) {
                    if (vmaxBannerAdFrameParent != null ) {
                        vmaxLayoutParams.bottomMargin = (int) (ORIGINAL_VMAX_PARENT_BOTTOM_PARAM * interpolatedTime);
                        vmaxBannerAdFrameParent.setLayoutParams(vmaxLayoutParams);
                    }
                }
            };
            footerADShowAnim.setDuration(200);*/
           /* if (vmaxBannerAdFrameParent != null && vmaxBannerAdFrameParent.getVisibility() == View.VISIBLE) {
                vmaxBannerAdFrameParent.startAnimation(footerADShowAnim);
            }*/
            if (mTabPagerRootLayout != null) {
                if (vmaxBannerAdFrameParent != null) {
                    vmaxLayoutParams.bottomMargin = ORIGINAL_VMAX_PARENT_BOTTOM_PARAM;

                    if (vmaxBannerAdFrameParent != null && vmaxBannerAdFrameParent.getLayoutTransition() != null) {
                        vmaxBannerAdFrameParent.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
                    }
                    vmaxBannerAdFrameParent.setLayoutParams(vmaxLayoutParams);
                }
                mTabPagerRootLayout.startAnimation(tabPagerShowAnim);
               // mAppBar.startAnimation(tabPagerShowAnim);
            }
        }
    }

    /*
        Called from FragmentCarouselInfo, when recyclerView is Scrolled
        Used for hiding the VMAX footer ad if Portrait Banner is Visible
        */
    @Override
    public void onViewScrolled(float recoffset) {
        if (vmaxBannerAdFrameParent != null) {
            ApplicationConfig applicationConfig = new ApplicationConfig();
            int height = applicationConfig.screenHeight;
            float offset = height * 0.1f;
            if (recoffset > offset) {
                if (mTabPagerRootLayout != null
                        && mTabPagerRootLayout.getVisibility() != View.VISIBLE
                        && vmaxBannerAdFrameParent.getVisibility() != View.VISIBLE) {
                    /*
                    This is being checked cause if the TabIndicator is not
                    there we need to show the FOOTER_AD at the bottom of the page
                     */

                    vmaxLayoutParams.bottomMargin = 0;
                    vmaxBannerAdFrameParent.setLayoutParams(vmaxLayoutParams);
                }
                vmaxBannerAdFrameParent.setVisibility(View.VISIBLE);
            } else {
                vmaxBannerAdFrameParent.setVisibility(View.INVISIBLE);
            }
        }

    }

    private int getHomeCarouselPosition() {
        List<CarouselInfoData> carouselInfoData = getTabListData();
        if (carouselInfoData != null && carouselInfoData.size() > 0) {
            for (int i = 0; i < carouselInfoData.size(); i++) {
                ApplicationController.FIRST_TAB_NAME = carouselInfoData.get(i).name;
                return i;
            }
        }
        return 0;
    }


    //Method used to Start the DownloadContent Playback after user gives ExternalStorage Permission
    // Can be removed at a later time when we will be sure that there are no Downloads in the External Storage
    private void startDownloadContentPlayback() {
        if (mOldDownloadContentCardData != null && mOldDownloadContentBundle != null) {
            mOldDownloadContentCardData.isDownloadDataOnExternalStorage = false;
            showDetailsFragment(mOldDownloadContentBundle, mOldDownloadContentCardData);
        }
    }

    private void initGoogleAds() {
        if (!Util.checkUserLoginStatus()) {
            SDKLogger.debug("user is not logged in");
            return;
        }

        if (!TextUtils.isEmpty(PrefUtils.getInstance().getAdmobUnitId())) {
            // Initialize the Google Mobile Ads SDK
            MobileAds.initialize(this);
        }
    }


    private void updateExoDownloads() {
        CardDownloadedDataList downloadedData = ApplicationController.getDownloadData();
        Iterator it = null;
        if (downloadedData != null) {
            it = downloadedData.mDownloadedList.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry data = (Map.Entry) it.next();
                CardDownloadData cardDownloadData = downloadedData.mDownloadedList.get(data.getKey());
                if (DownloadManagerMaintainer.getInstance().getDownloadStatus(cardDownloadData._id) == DownloadManagerMaintainer.STATE_QUEUED) {
                    DownloadManagerMaintainer.getInstance().restartDownload(cardDownloadData._id);
                }
            }
        }
    }

    private String getCurrentTabBgColor() {
        String currentTab = "";
        try {
            if (mViewPager != null
                    && mListCarouselInfo != null
                    && mListCarouselInfo.size() > 0) {
                currentTab = mListCarouselInfo.get(mCurrentSelectedPagePosition).bgColor;

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return currentTab;
    }

    public enum AnalyticsPlayerState {
        PAUSE,
        RESUME,
        STOP
    }

    private void editProfileAlertDialog() {
        editProfileDialog = new Dialog(mContext);
        editProfileDialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        View view = getLayoutInflater().inflate(R.layout.alert_edit_profile, null);
        editProfileDialog.setContentView(view);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(editProfileDialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.CENTER;

        countrySpinner = view.findViewById(R.id.countrySpinner);
        stateSpinner = view.findViewById(R.id.stateSpinner);
        citySpinner = view.findViewById(R.id.citySpinner);
        cityEdit = view.findViewById(R.id.cityEdit);
        addressEt = view.findViewById(R.id.addressEt);
        pincodeEt = view.findViewById(R.id.pincodeEt);
        Button updateButton = view.findViewById(R.id.updateProfile);

        getProfileDetails();


        countrySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (countriesList != null && countriesList.size() != 0) {
                    country = countriesList.get(position).name;
                    countrySpinner.setSelection(getCountryIndex(country));
                    String code = getCountryCodeIndex(countriesList.get(position).name);
                    getStatesList(code);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        stateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (statesList != null && statesList.size() != 0) {
                    state = statesList.get(position).name;
                    stateSpinner.setSelection(getStateIndex(state));
                    String code = getStateCodeIndex(statesList.get(position).name);
                    getCitiesList(code);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        citySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (citiesList != null && citiesList.size() != 0) {
                    city = citiesList.get(position).name;
                    citySpinner.setSelection(getCityIndex(city));
                    //String code = getStateCodeIndex(statesList.get(position).name);
                    //getCitiesList(code);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        editProfileDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if( editProfileDialog!=null) {
                    editProfileDialog.dismiss();
                }
            }
        });
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String address = addressEt.getText().toString();
                String pincode = pincodeEt.getText().toString();
                if (TextUtils.isEmpty(country) || country.equalsIgnoreCase("Select Country")) {
                    Toast.makeText(mContext, "Please select Country", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(state) || state.equalsIgnoreCase("Select State")) {
                    Toast.makeText(mContext, "Please select State", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(city) || city.equalsIgnoreCase("Select City")) {
                    Toast.makeText(mContext, "Please select City", Toast.LENGTH_SHORT).show();
                    return;
                }

                updateUserData(citySpinner.getSelectedItem().toString(), stateSpinner.getSelectedItem().toString(), editDob,
                        country, addressEt.getText().toString(), pincodeEt.getText().toString(),"");

            }
        });

        editProfileDialog.show();
        editProfileDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        editProfileDialog.getWindow().setAttributes(lp);

    }

    private void updateUserData(String city, String state, String dob, String country, String address, String pincode,String language) {
        UpdateProfileRequest.Params params = new UpdateProfileRequest.Params(country, state, city, dob, address, pincode,language);
        UpdateProfileRequest updateProfileRequest = new UpdateProfileRequest(params, new APICallback<UserProfileResponseData>() {
            @Override
            public void onResponse(APIResponse<UserProfileResponseData> response) {
                if (response == null || response.body() == null) {
                    return;
                }
                if (response.body().code == 402) {
                    PrefUtils.getInstance().setPrefLoginStatus("");
                    return;
                }
                if (response.body().code == 200) {
                    if(editProfileDialog!=null) {
                        editProfileDialog.dismiss();
                    }
                    UserProfileResponseData responseData = response.body();
                    if (responseData.status != null && responseData.status.equals(APIConstants.SUCCESS)) {

                        if (dob != null) {
                            PrefUtils.getInstance().setUserDOB(dob);
                        }

                        if (!TextUtils.isEmpty(country)) {
                            PrefUtils.getInstance().setUSerCountry(country);
                        }

                        if (!TextUtils.isEmpty(state)) {
                            PrefUtils.getInstance().setUserState(state);
                        }

                        if (!TextUtils.isEmpty(city)) {
                            PrefUtils.getInstance().setUserCity(city);
                        }

                        AlertDialogUtil.showToastNotification(response.message());

                        Intent ip = new Intent(mContext, SubscriptionWebActivity.class);
                        ip.putExtra(SubscriptionWebActivity.IS_FROM_PREMIUM, true);
                        startActivity(ip);
                    } else {
                        AlertDialogUtil.showToastNotification(response.message());
                    }
                } else {
                    if (response.message() != null && !TextUtils.isEmpty(response.message())) {
                        AlertDialogUtil.showToastNotification(response.message());
                    } else {
                        AlertDialogUtil.showToastNotification(getResources().getString(R.string.default_profile_update_message));
                    }
                }
            }

            @Override
            public void onFailure(Throwable t, int errorCode) {

            }
        });
        APIService.getInstance().execute(updateProfileRequest);
    }

    private void getCountriesList() {
        CountriesListRequest countriesListRequest = new CountriesListRequest(new APICallback<CountriesResponse>() {
            @Override
            public void onResponse(APIResponse<CountriesResponse> response) {
                if (response == null || response.body() == null) {
                    return;
                }

                if (!response.body().status.equalsIgnoreCase("SUCCESS")) {
                    return;
                }

                if (response.body().countries != null && response.body().countries.size() != 0) {
                    countriesList.clear();
                    countriesList.add(new CountriesData("NA", "NA", "Select Country"));
                    countriesList.addAll(response.body().countries);
                    ArrayAdapter<String> countriesAdapter = new ArrayAdapter<String>(MainActivity.this, R.layout.spinner_item,
                            getCountriesListInString());
                    countrySpinner.setAdapter(countriesAdapter);
                    if (country != null && !TextUtils.isEmpty(country)) {
                        countrySpinner.setSelection(getCountryIndex(country));
                    }
                    getStatesList(getCountryCodeIndex(country));
                }
            }

            @Override
            public void onFailure(Throwable t, int errorCode) {

            }
        });
        APIService.getInstance().execute(countriesListRequest);
    }

    private void getStatesList(String code) {
        StatesListRequest.Params params = new StatesListRequest.Params(code);
        StatesListRequest statesListRequest = new StatesListRequest(params, new APICallback<CountriesResponse>() {
            @Override
            public void onResponse(APIResponse<CountriesResponse> response) {
                if (response == null || response.body() == null) {
                    return;
                }

                if (!response.body().status.equalsIgnoreCase("SUCCESS")) {
                    return;
                }

                if (response.body().states != null && response.body().states.size() != 0) {
                    statesList.clear();
                    statesList.add(new CountriesData("NA", "NA", "Select State"));
                    statesList.addAll(response.body().states);
                    ArrayAdapter<String> statesAdapter = new ArrayAdapter<String>(MainActivity.this,
                            R.layout.spinner_item,
                            getStatesListInString());
                    stateSpinner.setAdapter(statesAdapter);
                    if (state != null && !TextUtils.isEmpty(state)) {
                        stateSpinner.setSelection(getStateIndex(state));
                    } else {
                        stateSpinner.setSelection(0);
                        state = stateSpinner.getSelectedItem().toString();
                    }
                    getCitiesList(getStateCodeIndex(state));
                }
            }

            @Override
            public void onFailure(Throwable t, int errorCode) {

            }
        });
        APIService.getInstance().execute(statesListRequest);
    }


    private void getCitiesList(String code) {
        CityListRequest.Params params = new CityListRequest.Params(code);
        CityListRequest statesListRequest = new CityListRequest(params, new APICallback<CountriesResponse>() {
            @Override
            public void onResponse(APIResponse<CountriesResponse> response) {
                if (response == null || response.body() == null) {
                    return;
                }

                if (!response.body().status.equalsIgnoreCase("SUCCESS")) {
                    return;
                }

                if (response.body().cities != null && response.body().cities.size() != 0) {
                    citiesList.clear();
                    citiesList.add(new CountriesData("NA", "NA", "Select City"));
                    citiesList.addAll(response.body().cities);
                    ArrayAdapter<String> statesAdapter = new ArrayAdapter<String>(MainActivity.this, R.layout.spinner_item,
                            getCityListInString());
                    citySpinner.setAdapter(statesAdapter);
                    if (city != null && !TextUtils.isEmpty(city)) {
                        citySpinner.setSelection(getCityIndex(city));
                    } else {
                        citySpinner.setSelection(0);
                        city = citySpinner.getSelectedItem().toString();
                    }
                }
            }

            @Override
            public void onFailure(Throwable t, int errorCode) {

            }
        });
        APIService.getInstance().execute(statesListRequest);
    }

    private String getStateCodeIndex(String country) {
        String code = null;
        for (int p = 0; p < statesList.size(); p++) {
            if (statesList.get(p).name.equalsIgnoreCase(country)) {
                code = statesList.get(p).code;
            }
        }
        return code;
    }

    private int getCityIndex(String cityName) {
        int index = 0;
        for (int p = 0; p < citiesList.size(); p++) {
            if (cityName.equalsIgnoreCase(citiesList.get(p).name)) {
                index = p;
            }
        }
        return index;
    }

    private List<String> getCityListInString() {
        List<String> statesListNew = new ArrayList<>();
        for (int p = 0; p < citiesList.size(); p++) {
            statesListNew.add(citiesList.get(p).name);
        }
        return statesListNew;
    }

    private List<String> getCountriesListInString() {
        List<String> countriesListNew = new ArrayList<>();
        for (int p = 0; p < countriesList.size(); p++) {
            countriesListNew.add(countriesList.get(p).name);
        }
        return countriesListNew;
    }

    private List<String> getStatesListInString() {
        List<String> statesListNew = new ArrayList<>();
        for (int p = 0; p < statesList.size(); p++) {
            statesListNew.add(statesList.get(p).name);
        }
        //statesListNew.add(0,"Select State");
        return statesListNew;
    }

    private int getCountryIndex(String countryName) {
        int index = 0;
        for (int p = 0; p < countriesList.size(); p++) {
            if (countryName.equalsIgnoreCase(countriesList.get(p).name)) {
                index = p;
            }
        }
        return index;
    }

    private int getStateIndex(String stateName) {
        int index = 0;
        for (int p = 0; p < statesList.size(); p++) {
            if (stateName.equalsIgnoreCase(statesList.get(p).name)) {
                index = p;
            }
        }
        return index;
    }

    private String getCountryCodeIndex(String country) {
        String code = null;
        for (int p = 0; p < countriesList.size(); p++) {
            if (countriesList.get(p).name.equalsIgnoreCase(country)) {
                code = countriesList.get(p).indexCode;
            }
        }
        return code;
    }

    private void getProfileDetails() {
        UserProfileRequest userProfileRequest = new UserProfileRequest(new APICallback<UserProfileResponseData>() {
            @Override
            public void onResponse(APIResponse<UserProfileResponseData> response) {
                if (response == null || response.body() == null) {
                    return;
                }
                if (response.body().code == 402) {
                    PrefUtils.getInstance().setPrefLoginStatus("");
                    return;
                }
                if (response.body().code == 200) {
                    UserProfileResponseData responseData = response.body();
                    if (responseData.result != null
                            && responseData.result.profile != null) {
                        setData(responseData);
                    }
                }
            }

            @Override
            public void onFailure(Throwable t, int errorCode) {
                SDKLogger.debug("Profile update onFailure");
            }
        });
        APIService.getInstance().execute(userProfileRequest);
    }

    private void setData(UserProfileResponseData responseData) {

        if (responseData.result.profile.first != null && !TextUtils.isEmpty(responseData.result.profile.first)) {
            editUserName = responseData.result.profile.first;
        }
        if (responseData.result.profile.mobile_no != null && !TextUtils.isEmpty(responseData.result.profile.mobile_no)) {
            PrefUtils.getInstance().setPrefMobileNumber(responseData.result.profile.mobile_no);
        }
        if (responseData.result.profile.last != null && !TextUtils.isEmpty(responseData.result.profile.last)) {
            editLastName = responseData.result.profile.last;
        }
        if (responseData.result.profile.gender != null && !TextUtils.isEmpty(responseData.result.profile.gender)) {
            if (responseData.result.profile.gender.equalsIgnoreCase("M")) {
                editGender = "male";
            } else if (responseData.result.profile.gender.equalsIgnoreCase("F")) {
                editGender = "female";
            } else {
                editGender = "Select Gender";
            }
        }
        if (responseData.result.profile.mobile_no != null && !TextUtils.isEmpty(responseData.result.profile.mobile_no)) {
            editMobile = responseData.result.profile.mobile_no;
        }

        if (responseData.result.profile.age != null && !TextUtils.isEmpty(responseData.result.profile.age)) {
            editAge = responseData.result.profile.age;
        }

        if (responseData.result.profile.dob != null && !TextUtils.isEmpty(responseData.result.profile.dob)) {
            editDob = responseData.result.profile.dob;
        }

        if (responseData.result.profile.emails != null && responseData.result.profile.emails.size() > 0 && !TextUtils.isEmpty(responseData.result.profile.emails.get(0).email)) {
            editEmail = responseData.result.profile.emails.get(0).email;
        }

        if (responseData.result.profile.locations != null && responseData.result.profile.locations.size() > 0) {
            if (responseData.result.profile.locations.get(0) != null
                    && !TextUtils.isEmpty(responseData.result.profile.locations.get(0)))
                PrefUtils.getInstance().setUSerCountry(responseData.result.profile.locations.get(0));
        }

        if (responseData.result.profile.state != null && !TextUtils.isEmpty(responseData.result.profile.state)) {
            PrefUtils.getInstance().setUserState(responseData.result.profile.state);
        }

        if (responseData.result.profile.city != null && !TextUtils.isEmpty(responseData.result.profile.city)) {
            PrefUtils.getInstance().setUserCity(responseData.result.profile.city);
        }


        if (responseData.result.profile.city != null && !TextUtils.isEmpty(responseData.result.profile.city)) {
            cityEdit.setText(responseData.result.profile.city);
            city = responseData.result.profile.city;
        }

        if (responseData.result.profile.locations.size() != 0) {
            country = responseData.result.profile.locations.get(0);
        }
        state = responseData.result.profile.state;
        getCountriesList();
    }

    public void nonceImplementation() {
        //boolean isConsentToStorage = getConsentToStorage();
        boolean isConsentToStorage = true;
        consentSettings = ConsentSettings.builder()
                .allowStorage(isConsentToStorage)
                .build();
        nonceLoader = new NonceLoader(mContext, consentSettings);
        generateNonceForAdRequest();
    }

    public void generateNonceForAdRequest() {
        Set supportedApiFrameWorksSet = new HashSet();
        supportedApiFrameWorksSet.add(2);
        supportedApiFrameWorksSet.add(7);
        supportedApiFrameWorksSet.add(9);

        NonceRequest nonceRequest = NonceRequest.builder()
                //.descriptionURL("")
                .omidVersion("1.0.0")
                .omidPartnerVersion("6.2.1")
                //.omidPartnerName("Example Publisher")
                .playerType("Exo Player")
                .playerVersion("2_11_7")
                //.ppid("testPpid")
                //.sessionId("Sample SID")
                .supportedApiFrameworks(supportedApiFrameWorksSet)
                .videoPlayerHeight(480)
                .videoPlayerWidth(640)
                .willAdAutoPlay(true)
                .willAdPlayMuted(true)
                .build();
        NonceCallbackImpl callback = new NonceCallbackImpl();
        nonceLoader
                .loadNonceManager(nonceRequest)
                .addOnSuccessListener(callback)
                .addOnFailureListener(callback);
    }

    private class NonceCallbackImpl implements com.google.android.gms.tasks.OnSuccessListener, OnFailureListener {

        @Override
        public void onFailure(Exception error) {
            Log.e("PALSample", "Nonce generation failed: " + error.getMessage());
        }

        @Override
        public void onSuccess(@NonNull Object o) {
            nonceManager = (NonceManager) o;
            nonceString = nonceManager.getNonce();
            Log.i("PALSample", "Generated nonce: " + nonceString);
            // from here you would trigger your ad request and move on to initialize content
        }
    }

    public void setHomeTab() {
        setToolBarCollapsible(false, false);
        mViewPager.setCurrentItem(0);
        TabLayout.Tab tab = menuTabs.getTabAt(0);
        tab.select();
    }
    public void setAppsTab(String tabName) {
        setToolBarCollapsible(false, false);
        mViewPager.setCurrentItem(2);
        TabLayout.Tab tab = menuTabs.getTabAt(2);
        tab.select();
        if(homePagerAdapterDynamicMenu != null && homePagerAdapterDynamicMenu.appFragment != null &&
                homePagerAdapterDynamicMenu.appFragment instanceof FragmentAppCarouselInfo)
            ((FragmentAppCarouselInfo)homePagerAdapterDynamicMenu.appFragment).setTabPosition(tabName);
    }

    private TabLayout.OnTabSelectedListener bottomMenuTabListener = new TabLayout.OnTabSelectedListener() {

        @Override
        public void onTabSelected(TabLayout.Tab tab) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (tab != null && tab.getTag() != null) {
                        String tabCode = tab.getTag().toString();
                        int position = Integer.parseInt(tabCode);

                CarouselInfoData carouselInfoData = tabListData.get(position);
              /*  mToolbar.setVisibility(VISIBLE);
                if (carouselInfoData.title.equalsIgnoreCase(getResources().getString(R.string.search))) {
                    mToolbar.setVisibility(View.INVISIBLE);
                }*/
                setTabSelection(position);

                //tabIcon.setImageBitmap(Util.getBitmap(mContext, null, tabListData.get(i), false));
               // PicassoUtil.with(mContext).load(imageLink, ((ImageView) tab.getCustomView().findViewById(R.id.tab_icon)), R.drawable.ic_navigation_settings);

                if(carouselInfoData != null) {
                    switch (carouselInfoData.name) {
                        case MENU_HOME:
                            showToolbar();
                            setTopMargin(0);
                            isSearchScreenVisible=false;
//                            ApplicationController.IS_FROME_HOME=true;
                            setToolBarCollapsible(false, false);
                           // mToolbar.setBackgroundColor(getResources().getColor(R.color.tool_bar_color_tran));
                          /*  if(homePagerAdapterDynamicMenu != null && homePagerAdapterDynamicMenu.homeFragment != null
                                    && homePagerAdapterDynamicMenu.homeFragment instanceof  FragmentCarouselInfo)
                                ((FragmentCarouselInfo)homePagerAdapterDynamicMenu.homeFragment).startToScroll();*/
                            break;
                        case MENU_LIVE_TV:
                            setTopMargin((int) getResources().getDimension(R.dimen.tool_bar_height));
                            showToolbar();
                            isSearchScreenVisible=false;
                           /* showSystemUI();
                            ApplicationController.IS_FROME_HOME=false;*/
                            setToolBarCollapsible(false, false);
                          /*  if(homePagerAdapterDynamicMenu != null && homePagerAdapterDynamicMenu.fragment != null
                                    && homePagerAdapterDynamicMenu.fragment instanceof  LiveTVFragment)
                                ((LiveTVFragment)homePagerAdapterDynamicMenu.fragment).startToScroll();*/
                            break;
                        case MENU_VOD:
                            setTopMargin((int) getResources().getDimension(R.dimen.tool_bar_height));
                            isSearchScreenVisible=false;
                            showToolbar();
                           /* showSystemUI();
                            ApplicationController.IS_FROME_HOME=false;*/
                            setToolBarCollapsible(false, false);
                      /*      if(homePagerAdapterDynamicMenu != null && homePagerAdapterDynamicMenu.appFragment != null
                                    && homePagerAdapterDynamicMenu.appFragment instanceof  FragmentAppCarouselInfo)
                                 ((FragmentAppCarouselInfo)homePagerAdapterDynamicMenu.appFragment).startToScroll();*/
                            break;
                        case MENU_SEARCH:
//                            setTopMargin(0);
                            setTopMargin((int) getResources().getDimension(R.dimen.margin_20));
                            hideToolbar();
                            isSearchScreenVisible=true;
                            if(homePagerAdapterDynamicMenu != null && homePagerAdapterDynamicMenu.mSearchSuggestionFrag != null
                                    && homePagerAdapterDynamicMenu.mSearchSuggestionFrag instanceof  SearchSuggestionsWithFilter) {
                                ((SearchSuggestionsWithFilter) homePagerAdapterDynamicMenu.mSearchSuggestionFrag).clearText();
                                ((SearchSuggestionsWithFilter)homePagerAdapterDynamicMenu.mSearchSuggestionFrag).updateProfileImage();
                            }
                           /* showSystemUI();
                            ApplicationController.IS_FROME_HOME=false;*/
                                    //showAppBar();
                                    break;
                                default:
                                    showToolbar();
                                    break;
                            }
                        }
                        mViewPager.setCurrentItem(position, false);
                    }
                }
            }, 300);
        }

        @Override
        public void onTabUnselected(TabLayout.Tab tab) {
            if (tab != null && tab.getTag() != null) {
                String tabCode = tab.getTag().toString();
                int position = Integer.parseInt(tabCode);
                ((TextView) tab.getCustomView().findViewById(R.id.tabContent)).setTextColor(getResources().getColor(R.color.white));
                ((TextView) tab.getCustomView().findViewById(R.id.tabContent)).setVisibility(VISIBLE);
                ((ImageView) tab.getCustomView().findViewById(R.id.tab_icon)).setImageBitmap(Util.getBitmap(mContext, null, tabListData.get(position), false));
                ((ImageView) tab.getCustomView().findViewById(R.id.tab_icon)).setColorFilter(ContextCompat.getColor(mContext, R.color.white));

                CarouselInfoData carouselInfoData = tabListData.get(position);
                if(carouselInfoData != null) {
                    switch (carouselInfoData.name) {
                        case MENU_HOME:
//                            ApplicationController.IS_FROME_HOME=false;
                            if(homePagerAdapterDynamicMenu != null && homePagerAdapterDynamicMenu.homeFragment != null
                                    && homePagerAdapterDynamicMenu.homeFragment instanceof  FragmentCarouselInfo)
                                ((FragmentCarouselInfo)homePagerAdapterDynamicMenu.homeFragment).startToScroll();
                            updateBottomBar(true, 0);
                            blurlayout_toolbar.setVisibility(GONE);

                            break;
                        case MENU_LIVE_TV:
                            setTopMargin((int) getResources().getDimension(R.dimen.tool_bar_height));
                            showToolbar();
                           /* ApplicationController.IS_FROME_HOME=false;
                            showSystemUI();*/
                            APIConstants.IS_REFRESH_LIVETV1=true;
                            setToolBarCollapsible(false, false);
                            if(homePagerAdapterDynamicMenu != null && homePagerAdapterDynamicMenu.fragment != null
                                    && homePagerAdapterDynamicMenu.fragment instanceof  LiveTVFragment)
                                ((LiveTVFragment)homePagerAdapterDynamicMenu.fragment).startToScroll();
                            updateBottomBar(true, 0);
                            blurlayout_toolbar.setVisibility(GONE);
                            break;
                        case MENU_VOD:
                          /*  ApplicationController.IS_FROME_HOME=false;
                            showSystemUI();*/
                            if(homePagerAdapterDynamicMenu != null && homePagerAdapterDynamicMenu.appFragment != null
                                    && homePagerAdapterDynamicMenu.appFragment instanceof  FragmentAppCarouselInfo)
                                ((FragmentAppCarouselInfo)homePagerAdapterDynamicMenu.appFragment).startToScroll();
                            updateBottomBar(true, 0);
                            blurlayout_toolbar.setVisibility(GONE);
                            break;

                    }
                }
            }
            //  ((TextView) tab.getCustomView().findViewById(R.id.tabContent)).setVisibility(GONE);
        }

        @Override
        public void onTabReselected(TabLayout.Tab tab) {
            //CustomLog.e("onTabSelectListener1", "onTabReselected");
            if (tab != null && tab.getTag() != null) {
                String tabCode = tab.getTag().toString();
                int position = Integer.parseInt(tabCode);
               /* if(carouselInfoData != null && (carouselInfoData.name.equals(MENU_VOD))){
                    setToolBarCollapsible(true, false);
                    //  ((HomeCarouselInfo)homePagerAdapterDynamicMenu.homeFragment).startToScroll();
                }*/
                CarouselInfoData carouselInfoData = tabListData.get(position);
                if(carouselInfoData != null) {
                    switch (carouselInfoData.name) {
                        case MENU_HOME:
//                            ApplicationController.IS_FROME_HOME=true;
                            if(homePagerAdapterDynamicMenu != null && homePagerAdapterDynamicMenu.homeFragment != null
                                    && homePagerAdapterDynamicMenu.homeFragment instanceof  FragmentCarouselInfo)
                                ((FragmentCarouselInfo)homePagerAdapterDynamicMenu.homeFragment).startToScroll();
                            updateBottomBar(true, 0);
                            blurlayout_toolbar.setVisibility(GONE);
                            break;
                        case MENU_LIVE_TV:
                            setTopMargin((int) getResources().getDimension(R.dimen.tool_bar_height));
                            showToolbar();
                           /* ApplicationController.IS_FROME_HOME=false;
                            showSystemUI();*/
                            APIConstants.IS_REFRESH_LIVETV1=true;
                            setToolBarCollapsible(false, false);
                            if(homePagerAdapterDynamicMenu != null && homePagerAdapterDynamicMenu.fragment != null
                                    && homePagerAdapterDynamicMenu.fragment instanceof  LiveTVFragment)
                                ((LiveTVFragment)homePagerAdapterDynamicMenu.fragment).startToScroll();
                            updateBottomBar(true, 0);
                            blurlayout_toolbar.setVisibility(GONE);
                            break;
                        case MENU_VOD:
                          /*  ApplicationController.IS_FROME_HOME=false;
                            showSystemUI();*/
                            if(homePagerAdapterDynamicMenu != null && homePagerAdapterDynamicMenu.appFragment != null
                                    && homePagerAdapterDynamicMenu.appFragment instanceof  FragmentAppCarouselInfo)
                                ((FragmentAppCarouselInfo)homePagerAdapterDynamicMenu.appFragment).startToScroll();
                            updateBottomBar(true, 0);
                            blurlayout_toolbar.setVisibility(GONE);
                            break;
                    }
                }
            }
        }

    };
    public com.myplex.myplex.ui.fragment.epg.EPG getEpgData() {
        return epgData;
    }

    public void setTopMargin(int margin) {
        if(mFrameLL!=null) {
            CoordinatorLayout.LayoutParams mViewPagerLayoutParams = (CoordinatorLayout.LayoutParams) mFrameLL.getLayoutParams();
            //  mViewPagerLayoutParams.topMargin =  (int)getResources().getDimension(R.dimen.tool_bar_height) + statusBarHeight;
            if (margin != 0)
                mViewPagerLayoutParams.topMargin = margin + Util.getStatusBarHeight(mContext);
            else
                mViewPagerLayoutParams.topMargin = 0;
            mFrameLL.setLayoutParams(mViewPagerLayoutParams);
        }
    }

    public void showSystemUI() {
        // setToolBarCollapsible(false, false);
        // OttCLog.e("SystemUI", "SHow system UI");
        // Remove the FLAG_LAYOUT_NO_LIMITS for getting navigation bar and status bar
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        getWindow().setNavigationBarColor(getResources().getColor(R.color.black));
        // adding TRANSLUCENT_STATUS
        if (Build.VERSION.SDK_INT >= 19) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }

        if (Build.VERSION.SDK_INT >= 19 && Build.VERSION.SDK_INT < 21) {
            setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, true);
        }

        /*if (Build.VERSION.SDK_INT >= 21) {
            setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }*/
        //  Window window = getWindow();
        //window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        int color = getResources().getColor(R.color.tool_bar_color_tran);
        int color2 = R.color.tool_bar_color_tran;
        int color3 = evaluateColorAlpha(Math.max(0.0f, Math.min(1.0f, 2)), color, color2);
      //  getWindow().setStatusBarColor(getResources().getColor(R.color.black_30));
        setStatusBarGradiant(MainActivity.this, false);

//            window.setNavigationBarColor(getResources().getColor(android.R.color.transparent));
//            window.setBackgroundDrawableResource(R.drawable.gradient_status_bar);


        // Removing Fullscreen for status bar and navigation bar
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    private int evaluateColorAlpha(float f, int color1, int color2) {
        int c14 = color1 >>> 24;
        int c13 = (color1 >> 16) & 255;
        int c12 = (color1 >> 8) & 255;
        int c11 = color1 & 255;
        int c24 = color2 >>> 24;
        int c23 = (color2 >> 16) & 255;
        int c22 = (color2 >> 8) & 255;
        int c21 = color2 & 255;
        return (c11 + ((int) (((float) (c21 - c11)) * f)))
                | ((((c14 + ((int) (((float) (c24 - c14)) * f))) << 24)
                | ((c13 + ((int) (((float) (c23 - c13)) * f))) << 16))
                | ((((int) (((float) (c22 - c12)) * f)) + c12) << 8));
    }
    public void hideSystemUI() {
        // adding the FLAG_FULLSCREEN for remove navigation bar and status bar
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // adding the FLAG_LAYOUT_NO_LIMITS for remove navigation bar and status bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
           // getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            //getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        }
        // to hide the navigation bar
        if (!ViewConfiguration.get(this).hasPermanentMenuKey()) {
            int newUiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
            getWindow().getDecorView().setSystemUiVisibility(newUiOptions);
        }
    }



    public void setEpgData(com.myplex.myplex.ui.fragment.epg.EPG epgData) {
        this.epgData = epgData;
    }


    public HashMap<String, Boolean> getEpgDataTracker() {
        return epgDataTracker;
    }

    public void setEpgDataTracker(HashMap<String, Boolean> epgDataTracker) {
        this.epgDataTracker = epgDataTracker;
    }

/*
    public void updateBlurredState(boolean isShow){
        Log.d(TAG, "updateBlurredState: isShow "+ isShow);
        float radius = 15f;

        View decorView = getWindow().getDecorView();
        //ViewGroup you want to start blur from. Choose root as close to BlurView in hierarchy as possible.
        ViewGroup rootView = (ViewGroup) decorView.findViewById(android.R.id.content);
        //Set drawable to draw in the beginning of each blurred frame (Optional).
        //Can be used in case your layout has a lot of transparent space and your content
        //gets kinda lost after after blur is applied.
        Drawable windowBackground = decorView.getBackground();
//        ActionBar actionBar = getActionBar();

        if(isShow){
*/
/*            blurlayout_toolbar.setBackground(null);
            blurlayout_toolbar.setBlurEnabled(true);
            blurlayout_toolbar.setupWith(rootView)
                    .setFrameClearDrawable(windowBackground)
                    .setBlurAlgorithm(new RenderScriptBlur(this))
                    .setBlurRadius(radius)
                    .setBlurAutoUpdate(true)
                    .setHasFixedTransformationMatrix(false);*//*


            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.status_bar_color));
//            window.setNavigationBarColor(getResources().getColor(android.R.color.transparent));
//            window.setBackgroundDrawableResource(R.drawable.gradient_status_bar);

            mToolbar.setBackgroundResource(R.drawable.gradient_toolbar_bar);


        }else {
//            blurlayout_toolbar.setBackground(null);
//            blurlayout_toolbar.setBlurEnabled(false);

            mToolbar.setBackground(null);
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION,WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
            window.setNavigationBarColor(Color.TRANSPARENT);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        }

    }
*/

    public void updateToolbar(boolean isShow){
        try {
            if(isShow){
                mToolbar.setVisibility(VISIBLE);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            }else {
                mToolbar.setVisibility(GONE);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateBottomBar(boolean isShow, int resumecontent){
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int heights = displayMetrics.heightPixels;
        int widths = displayMetrics.widthPixels;
        // suresh tab height -1848
        // samsung tab height -1024
        // lenovo tab height -1024
       // Toast.makeText(this, heights+"-------"+widths, Toast.LENGTH_SHORT).show();
        try {
            int density = mContext.getResources().getDisplayMetrics().densityDpi;
            if(isShow){
               // IsScrolled1st = true;
                switch (density) {
                    case DisplayMetrics.DENSITY_LOW:
                    case DisplayMetrics.DENSITY_MEDIUM:
                    case DisplayMetrics.DENSITY_HIGH:
                    case DisplayMetrics.DENSITY_XHIGH:
                        //Commented all tablet conditions as getting error while scrolling up ,the doc player is disappearing but audio is coming
                         /*if (DeviceUtils.isTablet(mContext)){
                            if(heights >1800){
                            int displayWidth = ((MainActivity) mContext).getWindowManager().getDefaultDisplay().getWidth();
                            int height = ((MainActivity) mContext).getWindowManager().getDefaultDisplay().getHeight() -displayWidth-(displayWidth/3);
                         //   mDraggablePanel.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
                            mDraggablePanel.getLayoutParams().height = height+10;
                            // mDraggablePanel.getLayoutParams().height = 160;
                            mDraggablePanel.requestLayout();
                            ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) mDraggablePanel.getLayoutParams();
                            // layoutParams.setMargins(0, 0, 0, 150);
                            layoutParams.setMargins(0, 0, 0, 50);
                            mFragmentCardDetailsPlayer.enableDraggablePanel();
                            mDraggablePanel.setLayoutParams(layoutParams);
                           // mDraggablePanel.setBackgroundColor(getResources().getColor(R.color.green));
                            }else{
                                int displayWidth = ((MainActivity) mContext).getWindowManager().getDefaultDisplay().getWidth();
                              //  int height = ((MainActivity) mContext).getWindowManager().getDefaultDisplay().getHeight() -displayWidth-(displayWidth/5);
                                //   mDraggablePanel.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
                              //  mDraggablePanel.getLayoutParams().height = height;
                                mDraggablePanel.requestLayout();
                                ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) mDraggablePanel.getLayoutParams();
                                // layoutParams.setMargins(0, 0, 0, 150);
                                if (this.getResources().getConfiguration().orientation == (Configuration.ORIENTATION_LANDSCAPE)){
                                    if(widths >1800){
                                        mDraggablePanel.getLayoutParams().height = 250;
                                        mDraggablePanel.requestLayout();
                                        layoutParams = (ViewGroup.MarginLayoutParams) mDraggablePanel.getLayoutParams();
                                        layoutParams.setMargins(0, 0, 0, 55);
                                       // mDraggablePanel.setBackgroundColor(getResources().getColor(R.color.yellow));
                                    }else{
                                        mDraggablePanel.requestLayout();
                                        layoutParams = (ViewGroup.MarginLayoutParams) mDraggablePanel.getLayoutParams();
                                        if(resumecontent == 1){
                                            layoutParams.setMargins(0, 0, 0, 0);
                                        }else{
                                            layoutParams.setMargins(0, 0, 0, 30);
                                        }
                                       // layoutParams.setMargins(0, 0, 0, 30);
                                       // mDraggablePanel.setBackgroundColor(getResources().getColor(R.color.green));
                                    }
                                } else {
                                    if(resumecontent == 1){
                                        mDraggablePanel.getLayoutParams().height = heights;
                                        layoutParams.setMargins(0, 0, 0, 0);
                                    }else{
                                        mDraggablePanel.getLayoutParams().height = 160;
                                        layoutParams.setMargins(0, 0, 0, 40);
                                    }
                                    //layoutParams.setMargins(0, 0, 0, 40);
                                }
                                mFragmentCardDetailsPlayer.enableDraggablePanel();
                                mDraggablePanel.setLayoutParams(layoutParams);
                            }
                            }*/
                        break;
                    default:
                      /*  if (DeviceUtils.isTablet(mContext)) {
                            int displayWidth = ((MainActivity) mContext).getWindowManager().getDefaultDisplay().getWidth();
                            int height = ((MainActivity) mContext).getWindowManager().getDefaultDisplay().getHeight() -displayWidth-(displayWidth/3);
                           // mDraggablePanel.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
                            if (this.getResources().getConfiguration().orientation == (Configuration.ORIENTATION_LANDSCAPE)){
                                mDraggablePanel.getLayoutParams().height = 200;
                            }else{
                                mDraggablePanel.getLayoutParams().height = height+50;
                            }
                             //mDraggablePanel.getLayoutParams().height = 160;
                            //mDraggablePanel.getLayoutParams().height = height-50;
                            mDraggablePanel.requestLayout();
                            ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) mDraggablePanel.getLayoutParams();
                            layoutParams.setMargins(0, 0, 0, 40);
                            mDraggablePanel.setLayoutParams(layoutParams);
                            mFragmentCardDetailsPlayer.enableDraggablePanel();
                            //mFragmentCardDetailsPlayer.disableDraggablePanel();
                            //mDraggablePanel.setBackgroundColor(getResources().getColor(R.color.yellow));
                        }*/
                        break;
                }
                onViewScrolledUp();
              //  blurlayout_toolbar.setVisibility(GONE);
                setStatusBarGradiant(MainActivity.this,false);

            }else {
              //  IsScrolled1st = false;
                switch (density) {
                    case DisplayMetrics.DENSITY_LOW:
                    case DisplayMetrics.DENSITY_MEDIUM:
                    case DisplayMetrics.DENSITY_HIGH:
                    case DisplayMetrics.DENSITY_XHIGH:
                       /* if (DeviceUtils.isTablet(mContext)) {
                            if(heights >1800){
                                    int displayWidth = ((MainActivity) mContext).getWindowManager().getDefaultDisplay().getWidth();
                                    //int height = ((MainActivity) mContext).getWindowManager().getDefaultDisplay().getHeight() -(displayWidth-(displayWidth/2));
                                    // int height = ((MainActivity) mContext).getWindowManager().getDefaultDisplay().getHeight() -(((MainActivity) mContext).getWindowManager().getDefaultDisplay().getHeight()/2);
                                    //mDraggablePanel.getLayoutParams().height = height-80;
                                    int height = ((MainActivity) mContext).getWindowManager().getDefaultDisplay().getHeight() -displayWidth-(displayWidth/3);
                                    //mDraggablePanel.getLayoutParams().height = height+10;
                                    mDraggablePanel.getLayoutParams().height = height-80;
                                    mDraggablePanel.requestLayout();
                                    ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) mDraggablePanel.getLayoutParams();
                                    layoutParams.setMargins(0, 0, 0, 0);
                                    //mFragmentCardDetailsPlayer.disableDraggablePanel();
                                    mFragmentCardDetailsPlayer.enableDraggablePanel();
                                    mDraggablePanel.setLayoutParams(layoutParams);
                                   // mDraggablePanel.setBackgroundColor(getResources().getColor(R.color.white));
                            }else{
                                int displayWidth = ((MainActivity) mContext).getWindowManager().getDefaultDisplay().getWidth();
                                //int height = ((MainActivity) mContext).getWindowManager().getDefaultDisplay().getHeight() -(displayWidth-(displayWidth/2));
                                //mDraggablePanel.getLayoutParams().height = height-80;
                                int height = ((MainActivity) mContext).getWindowManager().getDefaultDisplay().getHeight() -displayWidth-(displayWidth/5);
                                //mDraggablePanel.getLayoutParams().height = height+10;
                                if (this.getResources().getConfiguration().orientation == (Configuration.ORIENTATION_LANDSCAPE)){
                                    if(widths >1800){
                                        mDraggablePanel.getLayoutParams().height = 160;
                                    }else{
                                       if(resumecontent == 2){
                                            mDraggablePanel.getLayoutParams().height = heights-10;
                                        }else{
                                            mDraggablePanel.getLayoutParams().height = 160;
                                        }
                                    }
                                    mDraggablePanel.requestLayout();
                                    ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) mDraggablePanel.getLayoutParams();
                                    layoutParams.setMargins(0, 0, 0, 0);
                                    mDraggablePanel.setLayoutParams(layoutParams);
                                 //   mDraggablePanel.setBackgroundColor(getResources().getColor(R.color.white));
                                } else {
                                    if(resumecontent == 1){
                                        mDraggablePanel.getLayoutParams().height = heights;
                                    }else if(resumecontent == 2){
                                        //IsScrolled1st = false;
                                        mDraggablePanel.getLayoutParams().height = heights-10;
                                    }else{
                                       // IsScrolled1st = true;
                                        mDraggablePanel.getLayoutParams().height = 130;
                                    }
                                    mDraggablePanel.requestLayout();
                                    ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) mDraggablePanel.getLayoutParams();
                                    layoutParams.setMargins(0, 0, 0, 0);
                                    mDraggablePanel.setLayoutParams(layoutParams);
                                  //  mDraggablePanel.setBackgroundColor(getResources().getColor(R.color.green));
                                }
                                //mFragmentCardDetailsPlayer.disableDraggablePanel();
                                mFragmentCardDetailsPlayer.enableDraggablePanel();
                            }
                        }*/
                        break;
                    default:
                       /* if (DeviceUtils.isTablet(mContext)) {
                            int displayWidth = ((MainActivity) mContext).getWindowManager().getDefaultDisplay().getWidth();
                            int height = ((MainActivity) mContext).getWindowManager().getDefaultDisplay().getHeight() -displayWidth-(displayWidth/3);
                            if (this.getResources().getConfiguration().orientation == (Configuration.ORIENTATION_LANDSCAPE)){
                                mDraggablePanel.getLayoutParams().height = 110;
                            }else{
                                mDraggablePanel.getLayoutParams().height = height-35;
                            }
                            mDraggablePanel.requestLayout();
                            ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) mDraggablePanel.getLayoutParams();
                            layoutParams.setMargins(0, 0, 0, 0);
                           // mFragmentCardDetailsPlayer.disableDraggablePanel();
                            mFragmentCardDetailsPlayer.enableDraggablePanel();
                            mDraggablePanel.setLayoutParams(layoutParams);
                           // mDraggablePanel.setBackgroundColor(getResources().getColor(R.color.blue));
                        }*/
                        break;
                }


                onViewScrolledDown();

                ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) blurlayout_toolbar.getLayoutParams();
                if(APIConstants.IS_BACK_FROM_FRAGMENT_VOD_LIST){
                    blurlayout_toolbar.setVisibility(VISIBLE);
                }else{
                    APIConstants.IS_BACK_FROM_FRAGMENT_VOD_LIST=true;
                }

                TypedValue tv = new TypedValue();
                if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
                    int actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data,getResources().getDisplayMetrics());
                   // int density = mContext.getResources().getDisplayMetrics().densityDpi;
                    switch (density) {
                        case DisplayMetrics.DENSITY_HIGH:
                            layoutParams.height = actionBarHeight+actionBarHeight-40;
                            blurlayout_toolbar.setLayoutParams(layoutParams);
                           // Toast.makeText(mContext, "hells=="+actionBarHeight, Toast.LENGTH_SHORT).show();
                            break;
                        case DisplayMetrics.DENSITY_XHIGH:
                            layoutParams.height = actionBarHeight+actionBarHeight-40;
                            blurlayout_toolbar.setLayoutParams(layoutParams);
                           // Toast.makeText(mContext, "hii=="+actionBarHeight, Toast.LENGTH_SHORT).show();
                            break;
                        default:
             /*               layoutParams.height = (actionBarHeight-actionBarHeight/3)-5;
                            blurlayout_toolbar.setLayoutParams(layoutParams);
 if(layoutParams.height == 248){
                                layoutParams.height = actionBarHeight+actionBarHeight-20;
                                blurlayout_toolbar.setLayoutParams(layoutParams);
                            }else{
                                layoutParams.height = actionBarHeight+actionBarHeight-60;
                                blurlayout_toolbar.setLayoutParams(layoutParams);
                            }
*/
//Toast.makeText(mContext, "hello=="+layoutParams.height, Toast.LENGTH_SHORT).show();
                            layoutParams.height = actionBarHeight+actionBarHeight-60;
                            blurlayout_toolbar.setLayoutParams(layoutParams);
                            break;
                    }
                }
                setStatusBarGradiant(MainActivity.this, true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void setStatusBarGradiant(Activity activity, boolean bottom) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            if (bottom){
               // window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(getResources().getColor(R.color.transparent));
                getWindow().setStatusBarColor(Color.TRANSPARENT);
                android.graphics.drawable.Drawable background = activity.getResources().getDrawable(R.drawable.gradient_bg);
             //   window.setBackgroundDrawable(background);
            }/*else if(!bottom){
                Drawable background = getResources().getDrawable(R.drawable.gradient_bg);
                window.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
                window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(getResources().getColor(R.color.transparent));
                WindowCompat.setDecorFitsSystemWindows(window, true);
                new WindowInsetsControllerCompat(window,homeLinearLayout).show(WindowInsetsCompat.Type.systemBars());
               // window.setBackgroundDrawable(background);
            }*/else{
//                Drawable background = getResources().getDrawable(R.drawable.gradient_bg);
                window.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
                window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(getResources().getColor(R.color.transparent));
                /*WindowCompat.setDecorFitsSystemWindows(window, true);
                new WindowInsetsControllerCompat(window,homeLinearLayout).show(WindowInsetsCompat.Type.systemBars());*/
               // window.setBackgroundDrawable(background);
            }
        }
    }

    // slide the view from below itself to the current position
    public void slideUp(View view){
        view.setVisibility(View.VISIBLE);
        TranslateAnimation animate = new TranslateAnimation(
                0,                 // fromXDelta
                0,                 // toXDelta
                view.getHeight(),  // fromYDelta
                0);                // toYDelta
        animate.setDuration(500);
        animate.setFillAfter(true);
        view.startAnimation(animate);
    }

    // slide the view from its current position to below itself
    public void slideDown(View view){
        TranslateAnimation animate = new TranslateAnimation(
                0,                 // fromXDelta
                0,                 // toXDelta
                0,                 // fromYDelta
                view.getHeight()); // toYDelta
        animate.setDuration(500);
        animate.setFillAfter(true);
        view.startAnimation(animate);
    }

    ConnectivityReceiver mNetworkReceiver;

    private void registerNetworkBroadcast() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            registerReceiver(mNetworkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }
    }

    protected void unregisterNetworkChanges() {
        try {
            unregisterReceiver(mNetworkReceiver);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    public void onEventMainThread(EventNetworkConnectionChange event) {
        Log.d(TAG, "onEventMainThread: event.isConnected "+ event.isConnected + " isRefreshScreen "+ isRefreshScreen);
        if(event.isConnected && MenuDataModel.isRefreshScreen){
            finish();
            MenuDataModel.isRefreshScreen = false;
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
        if(liveCardPlayerCallback != null && event.isConnected) {
            liveCardPlayerCallback.onInternetConnected();
        }
    }

    private void setTabSelection(int position){
        if(menuTabs!=null){

            for(int i = 0; i< menuTabs.getTabCount(); i++){

                if(Integer.parseInt(menuTabs.getTabAt(i).getTag().toString()) == position){
                    ((TextView) menuTabs.getTabAt(i).getCustomView().findViewById(R.id.tabContent)).setVisibility(VISIBLE);
                    ((TextView) menuTabs.getTabAt(i).getCustomView().findViewById(R.id.tabContent)).setTextColor(getResources().getColor(R.color.yellow));
                    ((ImageView) menuTabs.getTabAt(i).getCustomView().findViewById(R.id.tab_icon)).setImageBitmap(Util.getBitmap(mContext, null, tabListData.get(i), true));
                    ((ImageView) menuTabs.getTabAt(i).getCustomView().findViewById(R.id.tab_icon)).setColorFilter(ContextCompat.getColor(mContext, R.color.yellow));
                }else{
                    ((TextView) menuTabs.getTabAt(i).getCustomView().findViewById(R.id.tabContent)).setTextColor(getResources().getColor(R.color.white));
                    ((TextView) menuTabs.getTabAt(i).getCustomView().findViewById(R.id.tabContent)).setVisibility(VISIBLE);
                    ((ImageView) menuTabs.getTabAt(i).getCustomView().findViewById(R.id.tab_icon)).setImageBitmap(Util.getBitmap(mContext, null, tabListData.get(i), false));
                    ((ImageView) menuTabs.getTabAt(i).getCustomView().findViewById(R.id.tab_icon)).setColorFilter(ContextCompat.getColor(mContext, R.color.white));
                }

            }
        }

    }

    public void clearAllarms(){

        try {
            String allProgramStartTimes = PrefUtils.getInstance()
                    .getPrefAlreadySetReminderTimes();
            Gson gson = new Gson();
            AlarmsSetData alarmsSetData = gson.fromJson(allProgramStartTimes, AlarmsSetData.class);
            if (alarmsSetData != null && alarmsSetData.results != null) {
                for (AlarmData alarm : alarmsSetData.results) {
                    Util.cancelReminder(this, alarm.title, alarm._id, Util.getDate(alarm.startDate), "", "");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            //Log.d(TAG, "" + e.getMessage());
        }

        PrefUtils.getInstance().setPrefAlreadySetReminderTimes("");

    }

}