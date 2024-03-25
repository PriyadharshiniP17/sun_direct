package com.myplex.model;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.myplex.api.APIConstants;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Apalya on 28-Mar-16.
 */
public class CarouselInfoData implements Serializable {
    public int weightage;
    public List<CardDataImagesItem> images;
    public String showAll;
    public String name;
    public String title;
    public int menuIcon;

    public String actionUrl;
    public String layoutType;
    public String appAction;
    public List<CardData> listCarouselData;
    public List<CarouselInfoData> listNestedCarouselInfoData;
    public int pageSize;
    public String bgColor;
    public String bgSectionColor;
    public boolean enableShowAll;
    public boolean showTitle;
    public String showAllLayoutType;
    public String shortDesc;
    public HashMap<Integer, ArrayList<String>> filteredData;
    public RequestState requestState = RequestState.NOT_LOADED;
    public GenreFilterData cachedFilterResponse;
    private String iconUrl;
    public String modified_on;
    public String userState;
    public String altTitle;
    public List<TextureItem> texture;
    public int adWidth;
    public int adHeight;
    public String adId;
    public boolean isSelected ;
    public String getLogoUrl(boolean isTablet, String screenDensity) {
        if (images == null || images.isEmpty()) {
            return null;
        }
        if (!TextUtils.isEmpty(iconUrl)) {
            return iconUrl;
        }
        String[] imageTypes = new String[]{APIConstants.IMAGE_TYPE_ICON};
        for (String imageType : imageTypes) {
            for (CardDataImagesItem imageItem : images) {
                if (isTablet) {
                    if (imageType.equalsIgnoreCase(imageItem.type)
                            && imageItem.profile.equalsIgnoreCase(screenDensity)) {
                        Log.e("SCREEN DENSITY ", screenDensity
                                + " IMAGE ITEM PROFILE " + imageItem.profile
                                + " IMAGE LINK " + imageItem.link);
                        iconUrl = imageItem.link;
                        return imageItem.link;
                    }
//                    if (imageType.equalsIgnoreCase(imageItem.type)
//                        && ApplicationConfig.XXHDPI.equalsIgnoreCase(imageItem.profile)) {
//                    return imageItem.link;
//                }
                } else {
                    if (imageType.equalsIgnoreCase(imageItem.type)
                            && ApplicationConfig.XXHDPI.equalsIgnoreCase(imageItem.profile)) {
                        Log.e("IMAGE ITEM PROFILE ", imageItem.profile
                                + " IMAGE LINK " + imageItem.link);
                        iconUrl = imageItem.link;
                        return imageItem.link;
                    }
                }
            }
        }

        return null;
    }

    public boolean isSideNavMenuSeperatorItem() {
        return layoutType != null && (APIConstants.LAYOUT_TYPE_NAVIGATION_SEPERATOR.equalsIgnoreCase(layoutType)|| APIConstants.LAYOUT_TYPE_NAVIGATION_SEPERATOR.equalsIgnoreCase(layoutType));
    }

    public boolean isSideNavMenuItem() {
        return layoutType != null && (APIConstants.LAYOUT_TYPE_SIDE_NAV_MENU.equalsIgnoreCase(layoutType)|| APIConstants.LAYOUT_TYPE_NAV_MENU.equalsIgnoreCase(layoutType));
    }
    public boolean isMenuItem() {
        return layoutType != null && (APIConstants.LAYOUT_TYPE_MENU.equalsIgnoreCase(layoutType)|| APIConstants.LAYOUT_TYPE_NAV_MENU.equalsIgnoreCase(layoutType));
    }

    public boolean isViewAllBigItemLayout() {
        return APIConstants.LAYOUT_TYPE_BIG_BROWSE_LIST.equalsIgnoreCase(showAllLayoutType);
    }

    public boolean isViewAllBigItemLayoutWithoutFilter() {
        return APIConstants.LAYOUT_TYPE_BIG_BROWSE_LIST_WITHOUT_FILTER.equalsIgnoreCase(showAllLayoutType);
    }

    public String getPromoUrl(boolean isTablet, String screenDensity) {
        if (images == null || images.isEmpty()) {
            return null;
        }
        String[] imageTypes = new String[]{APIConstants.IMAGE_TYPE_ICON};
        for (String imageType : imageTypes) {
            for (CardDataImagesItem imageItem : images) {
                if (isTablet) {
                    if (imageType.equalsIgnoreCase(imageItem.type)
                            && imageItem.profile.equalsIgnoreCase(ApplicationConfig.XXHDPI)) {
                        Log.e("SCREEN DENSITY ", screenDensity
                                + " IMAGE ITEM PROFILE " + imageItem.profile
                                + " IMAGE LINK " + imageItem.link);
                        return imageItem.link;
                    }
//                    if (imageType.equalsIgnoreCase(imageItem.type)
//                        && ApplicationConfig.XXHDPI.equalsIgnoreCase(imageItem.profile)) {
//                    return imageItem.link;
//                }
                } else {
                    if (imageType.equalsIgnoreCase(imageItem.type)
                            && ApplicationConfig.HDPI.equalsIgnoreCase(imageItem.profile)) {
                        Log.e("IMAGE ITEM PROFILE ", imageItem.profile
                                + " IMAGE LINK " + imageItem.link);
                        return imageItem.link;
                    }
                }
            }
        }

        return null;
    }
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        String newLine = System.getProperty("line.separator");

        result.append( this.getClass().getName() );
        result.append( CarouselInfoData.class.getSimpleName() + " {" );
        result.append(newLine);

        //determine fields declared in this class only (no fields of superclass)
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
        }
        result.append("logoImageUrl: " + getLogoUrl(false, null));
        result.append("}");

        return result.toString();
    }

    public String getPosterImageLink() {
        if (images == null) {
            return null;
        }
        String[] imageTypes = new String[]{APIConstants.IMAGE_TYPE_COVERPOSTER, APIConstants.IMAGE_TYPE_PORTRAIT_COVERPOSTER,};
        for (String imageType : imageTypes) {
//            LoggerD.debugHooqVstbLog("imageType- " + imageType);
            for (CardDataImagesItem imageItem : images) {
//                LoggerD.debugHooqVstbLog("imageItem: imageType- " + imageItem.type);
                if (imageType.equalsIgnoreCase(imageItem.type)) {
                    if (APIConstants.IMAGE_TYPE_COVERPOSTER.equalsIgnoreCase(imageItem.type)
                            && ApplicationConfig.XHDPI.equalsIgnoreCase(imageItem.profile)
                            || (APIConstants.IMAGE_TYPE_PORTRAIT_COVERPOSTER.equalsIgnoreCase(imageItem.type)
                            && ApplicationConfig.XHDPI.equalsIgnoreCase(imageItem.profile))) {

                        return imageItem.link;
                    }
                }
            }
        }
        return null;
    }

}
