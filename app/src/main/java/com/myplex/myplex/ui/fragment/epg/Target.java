package com.myplex.myplex.ui.fragment.epg;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Uday Kumar V on  16/04/22.
 */
public class Target implements Parcelable {
    @SerializedName("path")
    @Expose
    private String path;
    @SerializedName("pageAttributes")
    @Expose
    private Target.PageAttributes pageAttributes;
    @SerializedName("pageType")
    @Expose
    private String pageType;
    public static final Creator<Target> CREATOR = new Creator<Target>() {
        public Target createFromParcel(Parcel source) {
            return new Target(source);
        }

        public Target[] newArray(int size) {
            return new Target[size];
        }
    };

    public String getPath() {
        return this.path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Target.PageAttributes getPageAttributes() {
        return this.pageAttributes;
    }

    public void setPageAttributes(Target.PageAttributes pageAttributes) {
        this.pageAttributes = pageAttributes;
    }

    public String getPageType() {
        return this.pageType;
    }

    public void setPageType(String pageType) {
        this.pageType = pageType;
    }

    public Target() {
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.path);
        dest.writeParcelable(this.pageAttributes, flags);
        dest.writeString(this.pageType);
    }

    protected Target(Parcel in) {
        this.path = in.readString();
        this.pageAttributes = (Target.PageAttributes)in.readParcelable(Target.PageAttributes.class.getClassLoader());
        this.pageType = in.readString();
    }

    public static class PageAttributes implements Parcelable {
        @SerializedName("contentType")
        @Expose
        private String contentType;
        @SerializedName("isTransactional")
        @Expose
        private String isTransactional;
        @SerializedName("startTime")
        @Expose
        private String startTime;
        @SerializedName("endTime")
        @Expose
        private String endTime;
        @SerializedName("bannerPosition")
        @Expose
        private String bannerPosition;
        @SerializedName("isLive")
        @Expose
        private String isLive;
        @SerializedName("ClevertapContentType")
        @Expose
        private String ClevertapContentType;
        public static final Creator<Target.PageAttributes> CREATOR = new Creator<Target.PageAttributes>() {
            public Target.PageAttributes createFromParcel(Parcel source) {
                return new Target.PageAttributes(source);
            }

            public Target.PageAttributes[] newArray(int size) {
                return new Target.PageAttributes[size];
            }
        };

        public String getClevertapContentType() {
            return this.ClevertapContentType;
        }

        public void setClevertapContentType(String clevertapContentType) {
            this.ClevertapContentType = clevertapContentType;
        }

        public String getContentType() {
            return this.contentType;
        }

        public void setContentType(String contentType) {
            this.contentType = contentType;
        }

        public String getIsTransactional() {
            return this.isTransactional;
        }

        public void setIsTransactional(String isTransactional) {
            this.isTransactional = isTransactional;
        }

        public String getBannerPosition() {
            return this.bannerPosition;
        }

        public void setBannerPosition(String bannerPosition) {
            this.bannerPosition = bannerPosition;
        }

        public String getIsLive() {
            return this.isLive;
        }

        public void setIsLive(String isLive) {
            this.isLive = isLive;
        }

        public String getStartTime() {
            return this.startTime;
        }

        public void setStartTime(String startTime) {
            this.startTime = startTime;
        }

        public String getEndTime() {
            return this.endTime;
        }

        public void setEndTime(String endTime) {
            this.endTime = endTime;
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.contentType);
            dest.writeString(this.isTransactional);
            dest.writeString(this.bannerPosition);
            dest.writeString(this.isLive);
            dest.writeString(this.startTime);
            dest.writeString(this.endTime);
        }

        public PageAttributes() {
        }

        protected PageAttributes(Parcel in) {
            this.contentType = in.readString();
            this.isTransactional = in.readString();
            this.bannerPosition = in.readString();
            this.isLive = in.readString();
            this.startTime = in.readString();
            this.endTime = in.readString();
        }
    }
}
