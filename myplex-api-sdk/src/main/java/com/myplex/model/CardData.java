package com.myplex.model;

import android.content.Context;
import android.text.TextUtils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.myplex.api.APIConstants;
import com.myplex.util.PropertiesHandler;
import com.myplex.util.SDKLogger;

import org.w3c.dom.Text;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import static com.myplex.model.ApplicationConfig.XHDPI;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CardData implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = -3114232287673074578L;
	public static final String TAG = "CardData";
	public static final int ESTDOWNLOADINPROGRESS = 1;
	public static final int ESTDOWNLOADCOMPLETE = 2;
	public static final int ESTDOWNLOADFAILED = 3;
	public static final String TYPE_LOADING = "loading";

	public static List<CardData> DUMMY_LIST = new ArrayList<>();
	static {
		for (int i = 0; i < 10; i++) {
			DUMMY_LIST.add(new CardData());
		}
	}

	public CardDataContent content;
	public PreviewData previews;
	public String categoryName;
	public CardDataUserReviews userReviews;
	public CardDataVideos videos;
	public CardDataRelatedCast relatedCast;
	public CardDataCurrentUserData currentUserData;
	public FingerPrintData fingerPrint;
	public CardDataRelatedContent relatedContent;
	public VideoInfo videoInfo ;
	public CardDataComments comments;
	public List<CardDataPackages> packages;
	public String mediaSessionToken;
	public PlayerConfig playerConfig;
	public String _expiresAt;
	public CardDataRelatedMultimedia relatedMultimedia;
	public String liveTv;
	public List<CardDataAwards> awards;
	public CardDataCriticReviews criticReviews;
	public String _lastModifiedAt;
	public int elapsedTime;
	public CardDataGeneralInfo generalInfo;
	public CardDataImages images;
	public CardDataSimilarContent similarContent;
	public String _id;
	public CardDownloadData downloadData;
	public MatchInfo matchInfo;
	public List<CardData> childs;
	public List<LanguageTitleData> altTitle;
	public String promoText;
	public String localFilePath;
	private String portraitImageLink;
	private String requestID;
	private String coverposterImageLink;
	private String bannerImageLink;
	private String thumbnailImageLink;
	private String partnerImageLink;
	public boolean isFavourite;
	public boolean playFullScreen;
	public String trackingID;
	public String thumbnailSeekPreview;
	public String isSupportCatchup;
	public SkipConfig skipConfig;

	//Used only to support Downloaded content on older App versions
	public boolean isDownloadDataOnExternalStorage;
	public Statistics stats;
	public String offline_link;
	public String offlinePlayerType;
	public CardDataTags tags;
	public boolean isExpand;

	public String getTitle() {
		return generalInfo == null ? null : generalInfo.title;
	}
	public String getGenre() {
		if(content==null || content.genre==null) {
			return null;
		}
		return content == null ? null : content.genre.get(0).name;
	}

	public boolean isProgram() {
		return generalInfo != null && APIConstants.TYPE_PROGRAM.equalsIgnoreCase(generalInfo.type);
	}

	public String getChannelIconUrl() {
		if (images == null || images.values == null || images.values.isEmpty()) {
			return null;
		}
		String[] imageTypes = new String[]{APIConstants.IMAGE_TYPE_THUMBNAIL,APIConstants.IMAGE_TYPE_THUMBNAIL};

		for (String imageType : imageTypes) {
			for (CardDataImagesItem imageItem : images.values) {
				if (imageType.equalsIgnoreCase(imageItem.type)
						&& ApplicationConfig.MDPI.equalsIgnoreCase(imageItem.profile)) {
					return imageItem.link;
				}
			}
		}

		return null;
	}

	public String getChannelName() {
		return globalServiceName;
	}


	public String getTimeHHMM(Date date) {
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
		sdf.setTimeZone(TimeZone.getDefault());
		return sdf.format(date);
	}

	public String getTimeHHMM_AM(Date date) {
		SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");
		sdf.setTimeZone(TimeZone.getDefault());
		return sdf.format(date);
	}

	public Date getStartDate() {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		Date date = null;
		try {
			date = format.parse(startDate);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return date;
	}


	public Date getEndDate() {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		Date date = null;
		try {
			date = format.parse(endDate);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return date;
	}

	public String getYYYY() {
		if (content == null || content.releaseDate == null) return null;
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
//        format.setTimeZone(TimeZone.getTimeZone("UTC"));
		Date date = null;
		try {
			date = format.parse(content.releaseDate);
		} catch (ParseException e) {
			e.printStackTrace();
			return "";
		}
		if (date != null) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
			sdf.setTimeZone(TimeZone.getDefault());
			return sdf.format(date).toString();
		}

		return "";

	}

	public String getDurationWithFormat() {
		if (content == null || content.duration == null) return null;

		String duration = content.duration.replace(":0:", ":00:");
		if (duration.endsWith(":0")) {
			duration = duration.replace(duration.substring(duration.length() - 1, duration.length()), ":00");
		}

		String[] splitValues = duration.split(":");
		if (splitValues[0].length() == 1) {
			duration = "0" + duration;
		}
		if (splitValues.length > 2) {
			if (splitValues[0].equalsIgnoreCase("0") || splitValues[0].equalsIgnoreCase("00")) {
				duration = splitValues[1] + ":" + splitValues[2] + " mins";
				if (splitValues[1].length() == 1) {
					duration = "0" + duration;
				}
			} else {
				duration = duration + " hrs";
			}
		} else if (splitValues.length == 2) {
			duration = duration + " mins";
			if (splitValues[0].length() == 1) {
				duration = "0" + duration;
			}
		}
		return duration;
	}

	public String getDDMMYYYY() {

		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
//        format.setTimeZone(TimeZone.getTimeZone("UTC"));
		Date date = null;
		try {
			date = format.parse(content.releaseDate);
		} catch (ParseException e) {
			e.printStackTrace();
			return "";
		}
		if (date != null) {
			SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy");
			sdf.setTimeZone(TimeZone.getDefault());
			return sdf.format(date).toString();
		}

		return "";

	}

	public String getDDMMYYYYUTC() {

		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		format.setTimeZone(TimeZone.getTimeZone("UTC"));
		Date date = null;
		try {
			date = format.parse(content.releaseDate);
		} catch (ParseException e) {
			e.printStackTrace();
			return "";
		}
		if (date != null) {
			SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy");
//            sdf.setTimeZone(TimeZone.getDefault());
			return sdf.format(date).toString();
		}

		return "";

	}
//APIConstants.TYPE_VODCHANNEL.equalsIgnoreCase(searchData.generalInfo.type)
//			||APIConstants.TYPE_TVSEASON.equalsIgnoreCase(searchData.generalInfo.type)
//			||APIConstants.TYPE_VODCATEGORY.equalsIgnoreCase(searchData.generalInfo.type)
//			||APIConstants.TYPE_TVSERIES.equalsIgnoreCase(searchData.generalInfo.type)

	public boolean isVODChannel() {
		return generalInfo != null && APIConstants.TYPE_VODCHANNEL.equalsIgnoreCase(generalInfo.type);
	}

	public boolean isVODCategory() {
		return generalInfo != null && APIConstants.TYPE_VODCATEGORY.equalsIgnoreCase(generalInfo.type);
	}

	public boolean isTVSeason() {
		return generalInfo != null && APIConstants.TYPE_TVSEASON.equalsIgnoreCase(generalInfo.type);
	}

	public boolean isTVSeries() {
		return generalInfo != null && APIConstants.TYPE_TVSERIES.equalsIgnoreCase(generalInfo.type);
	}

	public boolean isTVEpisode() {
		return generalInfo != null && APIConstants.TYPE_TVEPISODE.equalsIgnoreCase(generalInfo.type);
	}

	public boolean isVOD() {
		return generalInfo != null && APIConstants.TYPE_VOD.equalsIgnoreCase(generalInfo.type);
	}

	public boolean isYoutube() {
		return generalInfo != null && APIConstants.TYPE_YOUTUBE.equalsIgnoreCase(generalInfo.type);
	}

	public boolean isVODYoutubeChannel() {
		return generalInfo != null && APIConstants.TYPE_VODYOUTUBECHANNEL.equalsIgnoreCase(generalInfo.type);
	}

	public boolean isNewsContent() {
		return generalInfo != null && APIConstants.TYPE_NEWS.equalsIgnoreCase(generalInfo.type);
	}

	public CardDataImagesItem getImageItem() {
		if (images == null
				|| images.values== null) {
			return null;
		}
		String[] imageTypes = new String[]{APIConstants.IMAGE_TYPE_COVERPOSTER, APIConstants.IMAGE_TYPE_PORTRAIT_COVERPOSTER,};
		for (String imageType : imageTypes) {
//            LoggerD.debugHooqVstbLog("imageType- " + imageType);
			for (CardDataImagesItem imageItem : images.values) {
//                LoggerD.debugHooqVstbLog("imageItem: imageType- " + imageItem.type);
				if (imageType.equalsIgnoreCase(imageItem.type)) {
					if (APIConstants.IMAGE_TYPE_COVERPOSTER.equalsIgnoreCase(imageItem.type)
							&& ApplicationConfig.MDPI.equalsIgnoreCase(imageItem.profile)
							|| (APIConstants.IMAGE_TYPE_PORTRAIT_COVERPOSTER.equalsIgnoreCase(imageItem.type)
							&& XHDPI.equalsIgnoreCase(imageItem.profile))) {
						SDKLogger.debug("title- " + generalInfo.title + " imageUrl- " + imageItem);
						return imageItem;
					}
				}
			}
		}
		return null;
	}

	public boolean isLive() {
		return generalInfo != null && APIConstants.TYPE_LIVE.equalsIgnoreCase(generalInfo.type);
	}

	public String getDescription() {
		return generalInfo == null ? null : generalInfo.description;
	}

	public String getBriefDescription() {
		return generalInfo == null ? null : generalInfo.briefDescription;
	}

	public boolean isHooq() {
		return publishingHouse != null && APIConstants.TYPE_HOOQ.equalsIgnoreCase(publishingHouse.publishingHouseName);
	}

	public boolean isHungama() {
		return publishingHouse != null && APIConstants.TYPE_HUNGAMA.equalsIgnoreCase(publishingHouse.publishingHouseName);
	}

	public boolean isLoading() {
		return generalInfo != null && TYPE_LOADING.equalsIgnoreCase(generalInfo.type);
	}

	public int getPublishingHouseId() {
		return publishingHouse == null ? 0 : publishingHouse.publishingHouseId;
	}

	public boolean isMovie() {
		return generalInfo != null && APIConstants.TYPE_MOVIE.equalsIgnoreCase(generalInfo.type);
	}

	public String getPartnerId() {
		return generalInfo == null ? String.valueOf(0) : generalInfo.partnerId;
	}

	public boolean isMusicVideo() {
		return generalInfo != null && generalInfo.type != null && APIConstants.TYPE_MUSIC_VIDEO.equalsIgnoreCase(generalInfo.type);
	}

	public enum HTTP_SOURCE { CACHE , CACHE_REFRESH_NEEDED , ONLINE };
	public HTTP_SOURCE httpSource = HTTP_SOURCE.ONLINE;
	public String globalServiceId;
	public String _aid;
	public String nextProgram;
	public String startDate;
	public String endDate;
	public int pageIndex;
    public PublishingHouse publishingHouse;
	public String globalServiceName;
	public String contentProvider;
	public CardDataSubtitles subtitles;

	public CardData() {

	}


	@Override
	public String toString() {
		return "title- " + getTitle() + " type- " + getType();
	}

	public String getType() {
		return generalInfo == null ? "NA" : generalInfo.type;
	}


	public int getDurationInMints() {
		int durationInSeconds = 0;
		if (content == null || content.duration == null) return durationInSeconds;
		try {
			String[] hhmmss = content.duration.split(":");
			int hhtosecFactor = 60 * 60;
			int mmtosecFactor = 60;
			if (hhmmss.length > 2) {
				durationInSeconds = Integer.parseInt(hhmmss[0]) * hhtosecFactor + Integer.parseInt(hhmmss[1]) * mmtosecFactor + Integer.parseInt(hhmmss[2]);
			} else if (hhmmss.length > 1) {
				durationInSeconds = Integer.parseInt(hhmmss[0]) * mmtosecFactor + Integer.parseInt(hhmmss[1]);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return durationInSeconds / 60;
	}

	public boolean isAdType() {
		return generalInfo != null && (generalInfo.type.startsWith(APIConstants.TYPE_ADBANNER_IMAGE)
				||generalInfo.type.equalsIgnoreCase(APIConstants.TYPE_ADBANNER_IMAGE));
	}

	public final String getImageLink(String imageType) {
		if (images == null || images.values == null || images.values.isEmpty()) {
			return null;
		}
		String[] imageTypes = new String[]{APIConstants.IMAGE_TYPE_THUMBNAIL};
		String profile = ApplicationConfig.MDPI;
		if (APIConstants.IMAGE_TYPE_PORTRAIT_COVERPOSTER.equalsIgnoreCase(imageType)) {
			if (!TextUtils.isEmpty(portraitImageLink)) {
				return portraitImageLink;
			}
			imageTypes = new String[]{APIConstants.IMAGE_TYPE_PORTRAIT_COVERPOSTER, APIConstants.IMAGE_TYPE_COVERPOSTER};
			// profile = XHDPI;
		}
		if (APIConstants.IMAGE_TYPE_COVERPOSTER.equalsIgnoreCase(imageType)) {
			if (!TextUtils.isEmpty(coverposterImageLink)) {
				return coverposterImageLink;
			}
			imageTypes = new String[]{APIConstants.IMAGE_TYPE_COVERPOSTER, APIConstants.IMAGE_TYPE_BANNER, APIConstants.IMAGE_TYPE_PORTRAIT_COVERPOSTER};
		}
		if (APIConstants.IMAGE_TYPE_BANNER.equalsIgnoreCase(imageType)) {
			if (!TextUtils.isEmpty(bannerImageLink)) {
				return bannerImageLink;
			}
			imageTypes = new String[]{APIConstants.IMAGE_TYPE_BANNER, APIConstants.IMAGE_TYPE_COVERPOSTER, APIConstants.IMAGE_TYPE_PORTRAIT_COVERPOSTER};
		}
		if (APIConstants.IMAGE_TYPE_THUMBNAIL.equalsIgnoreCase(imageType)) {
			if (!TextUtils.isEmpty(thumbnailImageLink)) {
				return thumbnailImageLink;
			}
		}
		for (String type : imageTypes) {
			for (CardDataImagesItem imageItem : images.values) {
				if (type.equalsIgnoreCase(imageItem.type)
						&& profile.equalsIgnoreCase(imageItem.profile)) {
					if (APIConstants.IMAGE_TYPE_PORTRAIT_COVERPOSTER.equalsIgnoreCase(imageType)) {
						portraitImageLink = imageItem.link;
					}
					if (APIConstants.IMAGE_TYPE_COVERPOSTER.equalsIgnoreCase(imageType)) {
						coverposterImageLink = imageItem.link;
					}
					if (APIConstants.IMAGE_TYPE_BANNER.equalsIgnoreCase(imageType)) {
						bannerImageLink = imageItem.link;
					}
					if (APIConstants.IMAGE_TYPE_BANNER.equalsIgnoreCase(imageType)) {
						thumbnailImageLink = imageItem.link;
					}


					return imageItem.link;
				}
			}
		}
		return null;
	}

	public String getPartnerImageLink(Context mContext) {
		if (mContext != null) {
            if (!TextUtils.isEmpty(partnerImageLink)) {
                return partnerImageLink;
            }
			PartnerDetailsResponse partnerDetailsResponse = PropertiesHandler.getPartnerDetailsResponse(mContext);
			String partnerName = (this.publishingHouse != null && !TextUtils.isEmpty(this.publishingHouse.publishingHouseName)) ? this.publishingHouse.publishingHouseName : this.contentProvider;
			if (!TextUtils.isEmpty(partnerName) && partnerDetailsResponse != null && this != null && partnerDetailsResponse.partnerDetails != null) {
				for (int i = 0; i < partnerDetailsResponse.partnerDetails.size(); i++) {
					if (partnerDetailsResponse != null
							&& partnerDetailsResponse.partnerDetails != null
							&& partnerDetailsResponse.partnerDetails.get(i) != null
							&& !TextUtils.isEmpty(partnerDetailsResponse.partnerDetails.get(i).name)
							&& partnerDetailsResponse.partnerDetails.get(i).name.equalsIgnoreCase(partnerName)) {
						partnerImageLink = partnerDetailsResponse.partnerDetails.get(i).imageURL;
						return partnerDetailsResponse.partnerDetails.get(i).imageURL;
					}
				}
			}
		}
		return null;
	}
	public String getParnterTitle(Context mContext) {
		if (mContext != null) {
			PartnerDetailsResponse partnerDetailsResponse = PropertiesHandler.getPartnerDetailsResponse(mContext);
			String partnerName = (this.publishingHouse != null && !TextUtils.isEmpty(this.publishingHouse.publishingHouseName)) ? this.publishingHouse.publishingHouseName : this.contentProvider;
			if (!TextUtils.isEmpty(partnerName) && partnerDetailsResponse != null && this != null && partnerDetailsResponse.partnerDetails != null) {
				for (int i = 0; i < partnerDetailsResponse.partnerDetails.size(); i++) {
					if (partnerDetailsResponse != null
							&& partnerDetailsResponse.partnerDetails != null
							&& partnerDetailsResponse.partnerDetails.get(i) != null
							&& !TextUtils.isEmpty(partnerDetailsResponse.partnerDetails.get(i).name)
							&& partnerDetailsResponse.partnerDetails.get(i).name.equalsIgnoreCase(partnerName)) {
					//	partnerImageLink = partnerDetailsResponse.partnerDetails.get(i).imageURL;
						String name = partnerDetailsResponse.partnerDetails.get(i).name;
						return name.substring(0, 1).toUpperCase() + name.substring(1);
					}
				}
			}
		}
		return null;
	}

}