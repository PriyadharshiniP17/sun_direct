package com.myplex.model;

import android.graphics.Bitmap;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.myplex.api.APIConstants;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CardDownloadData implements Serializable {

	public static final int TYPE_DOWNLOAD_NON_DRM = 0;
	public static final int TYPE_DOWNLOAD_DRM = 1;
	public static final int TYPE_DOWNLOAD_DRM_HOOQ = 2;

	public static final int STATUS_FAILED_TO_AQUIRE_DRM_LICENSE = 198;
	public static final int STATUS_FILE_NOT_ENOGH_SPACE = 199;

	public static final int STATUS_FILE_ZIPPED = 200;
	public static final int STATUS_FILE_UNZIPPING = 201;
	public static final int STATUS_FILE_UNZIPPED = 202;


	/**
	 * 
	 */
	private static final long serialVersionUID = -8475605841159873373L;
    public String downloadKey;
	public int videoTrackId;
	public int zipStatus;
	public int audioTrackId;
	public String audioFileName;
	public String videoFileName;
	public String duration;
	public String description;
	public String briefDescription;
	public String releaseDate;
	public String variantType;
	public String contentType;

    public CardDownloadData(){}

	public boolean mCompleted = false;
	public int mPercentage;
	// This is the Download ID of the Parent item, it can be wvm, mpd, or mp4.
	public long mDownloadId = -1;

	public long mVideoDownloadId = -1;
	public long mAudioDownloadId = -1;
	public long  mSubtitleDownloadId = -1;


	public double mDownloadedBytes=0;
	public double mDownloadTotalSize=0;
	// This will be the default path of the file that is supplied as the Url in offline mode, path to wvm, mp4 or mpd file.
	public String mDownloadPath;
	//Id of the card that is being downloaded;
	public String _id;
	public String fileName;
	public String title;
	public String genres;
	public String time_languages;
	//This is the url for the image that is shown on the Poster in UI of Downloads Section
	public String ImageUrl;
	public String audioFilePath;
	public String coverPosterImageUrl;
	public String ItemType;
	public String partnerName;
	public int downloadType;
	public String hooqCacheId;
	public int elapsedTime;
	public boolean isNotificationShown;

	public List<SeasonData> tvSeasonsList;
	public List<CardDownloadData> tvEpisodesList;
	public CardDataContent content;
	public boolean isStoredInternally;

	public boolean isFree;
	public float starRating;
	public boolean showWatermark;

	public int publishingHouseID;

	public String language;
	public String globalServiceID;
	public String contentPartner;
	public String categoryType;
	public String categoryName;

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		String newLine = System.getProperty("line.separator");

		result.append( this.getClass().getName() );
		result.append( " {" );
		result.append(newLine);
		newLine = ",";
		/*//determine fields declared in this class only (no fields of superclass)
		Field[] fields = this.getClass().getDeclaredFields();

		//print field names paired with their values
		for ( Field field : fields  ) {
			result.append("  ");
			try {
				result.append( field.getName() );
				result.append(": ");
				//requires access to private field:
				result.append( field.get(this) );
			} catch ( IllegalAccessException ex ) {
				System.out.println(ex);
			}
			result.append(newLine);
		}*/
		result.append("\ttitle- " + title);
		result.append("\tmPercentage- " + mPercentage);
		result.append("\tepisodes- " + tvEpisodesList);
		result.append("\t}");

		return result.toString();

	}

	public boolean isHooqContent() {
		return partnerName != null
				&& APIConstants.TYPE_HOOQ.equalsIgnoreCase(partnerName);
	}

	public boolean isHungamaContent() {
		return partnerName != null
				&& APIConstants.TYPE_HUNGAMA.equalsIgnoreCase(partnerName);
	}
}
