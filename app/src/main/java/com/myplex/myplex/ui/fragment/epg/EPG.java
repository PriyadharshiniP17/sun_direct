package com.myplex.myplex.ui.fragment.epg;

import android.net.NetworkInfo;
import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.myplex.model.CardData;

import java.util.Date;
import java.util.List;

/**
 * Created by Uday Kumar V on  16/04/22.
 */
public class EPG {
    @SerializedName("tiltle")
    @Expose
    private String tiltle;
    @SerializedName("tabs")
    @Expose
    private List<EPGTab> tabs = null;
    @SerializedName("data")
    @Expose
    private List<EPG.EPGData> data = null;

    public EPG() {
    }

    public String getTiltle() {
        return this.tiltle;
    }

    public void setTiltle(String tiltle) {
        this.tiltle = tiltle;
    }

    public List<EPG.EPGTab> getTabs() {
        return this.tabs;
    }

    public void setTabs(List<EPG.EPGTab> tabs) {
        this.tabs = tabs;
    }

    public List<EPG.EPGData> getData() {
        return this.data;
    }

    public void setData(List<EPG.EPGData> data) {
        this.data = data;
    }

    public static class PosterDisplayChannel implements Parcelable {
        @SerializedName("markers")
        @Expose
        private List<EPG.PosterDisplayChannel.Marker> markers = null;
        @SerializedName("title")
        @Expose
        private String title;
        @SerializedName("imageUrl")
        @Expose
        private String imageUrl;
        public static final Creator<EPG.PosterDisplayChannel> CREATOR = new Creator<EPG.PosterDisplayChannel>() {
            public EPG.PosterDisplayChannel createFromParcel(Parcel source) {
                return new EPG.PosterDisplayChannel(source);
            }

            public EPG.PosterDisplayChannel[] newArray(int size) {
                return new EPG.PosterDisplayChannel[size];
            }
        };

        public List<EPG.PosterDisplayChannel.Marker> getMarkers() {
            return this.markers;
        }

        public void setMarkers(List<EPG.PosterDisplayChannel.Marker> markers) {
            this.markers = markers;
        }

        public String getTitle() {
            return this.title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getImageUrl() {
            return this.imageUrl;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }

        public PosterDisplayChannel() {
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeTypedList(this.markers);
            dest.writeString(this.imageUrl);
        }

        protected PosterDisplayChannel(Parcel in) {
            this.markers = in.createTypedArrayList(EPG.PosterDisplayChannel.Marker.CREATOR);
            this.imageUrl = in.readString();
        }

        public static class Marker implements Parcelable {
            @SerializedName("markerType")
            @Expose
            private String markerType;
            @SerializedName("value")
            @Expose
            private String value;
            public static final Creator<EPG.PosterDisplayChannel.Marker> CREATOR = new Creator<EPG.PosterDisplayChannel.Marker>() {
                public EPG.PosterDisplayChannel.Marker createFromParcel(Parcel source) {
                    return new EPG.PosterDisplayChannel.Marker(source);
                }

                public EPG.PosterDisplayChannel.Marker[] newArray(int size) {
                    return new EPG.PosterDisplayChannel.Marker[size];
                }
            };

            public String getMarkerType() {
                return this.markerType;
            }

            public void setMarkerType(String markerType) {
                this.markerType = markerType;
            }

            public String getValue() {
                return this.value;
            }

            public void setValue(String value) {
                this.value = value;
            }

            public int describeContents() {
                return 0;
            }

            public void writeToParcel(Parcel dest, int flags) {
                dest.writeString(this.markerType);
                dest.writeString(this.value);
            }

            public Marker() {
            }

            protected Marker(Parcel in) {
                this.markerType = in.readString();
                this.value = in.readString();
            }
        }
    }

    public static class PosterDisplay implements Parcelable {
        @SerializedName("subtitle2")
        @Expose
        private String subtitle2;
        @SerializedName("markers")
        @Expose
        private List<EPG.PosterDisplay.Marker> markers = null;
        @SerializedName("parentName")
        @Expose
        private String parentName;
        @SerializedName("subtitle1")
        @Expose
        private String subtitle1;
        @SerializedName("title")
        @Expose
        private String title;
        @SerializedName("imageUrl")
        @Expose
        private String imageUrl;
        @SerializedName("subtitle5")
        @Expose
        private String subtitle5;
        @SerializedName("subtitle3")
        @Expose
        private String subtitle3;
        @SerializedName("language")
        @Expose
        private String language;
        @SerializedName("payType")
        @Expose
        private String payType;
        public static final Creator<EPG.PosterDisplay> CREATOR = new Creator<EPG.PosterDisplay>() {
            public EPG.PosterDisplay createFromParcel(Parcel source) {
                return new EPG.PosterDisplay(source);
            }

            public EPG.PosterDisplay[] newArray(int size) {
                return new EPG.PosterDisplay[size];
            }
        };

        public String getSubtitle3() {
            return this.subtitle3;
        }

        public void setSubtitle3(String subtitle3) {
            this.subtitle3 = subtitle3;
        }

        public String getLanguage() {
            return this.language;
        }

        public void setLanguage(String language) {
            this.language = language;
        }

        public String getPayType() {
            return this.payType;
        }

        public void setPayType(String payType) {
            this.payType = payType;
        }

        public String getSubtitle2() {
            return this.subtitle2;
        }

        public void setSubtitle2(String subtitle2) {
            this.subtitle2 = subtitle2;
        }

        public List<EPG.PosterDisplay.Marker> getMarkers() {
            return this.markers;
        }

        public void setMarkers(List<EPG.PosterDisplay.Marker> markers) {
            this.markers = markers;
        }

        public String getParentName() {
            return this.parentName;
        }

        public void setParentName(String parentName) {
            this.parentName = parentName;
        }

        public String getSubtitle1() {
            return this.subtitle1;
        }

        public void setSubtitle1(String subtitle1) {
            this.subtitle1 = subtitle1;
        }

        public String getTitle() {
            return this.title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getImageUrl() {
            return this.imageUrl;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }

        public String getSubtitle5() {
            return this.subtitle5;
        }

        public void setSubtitle5(String subtitle2) {
            this.subtitle5 = subtitle2;
        }

        public PosterDisplay() {
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.subtitle2);
            dest.writeTypedList(this.markers);
            dest.writeString(this.parentName);
            dest.writeString(this.subtitle1);
            dest.writeString(this.title);
            dest.writeString(this.imageUrl);
        }

        protected PosterDisplay(Parcel in) {
            this.subtitle2 = in.readString();
            this.markers = in.createTypedArrayList(EPG.PosterDisplay.Marker.CREATOR);
            this.parentName = in.readString();
            this.subtitle1 = in.readString();
            this.title = in.readString();
            this.imageUrl = in.readString();
        }

        public static class Marker implements Parcelable {
            @SerializedName("markerType")
            @Expose
            private String markerType;
            @SerializedName("value")
            @Expose
            private String value;
            public static final Creator<EPG.PosterDisplay.Marker> CREATOR = new Creator<EPG.PosterDisplay.Marker>() {
                public EPG.PosterDisplay.Marker createFromParcel(Parcel source) {
                    return new EPG.PosterDisplay.Marker(source);
                }

                public EPG.PosterDisplay.Marker[] newArray(int size) {
                    return new EPG.PosterDisplay.Marker[size];
                }
            };

            public String getMarkerType() {
                return this.markerType;
            }

            public void setMarkerType(String markerType) {
                this.markerType = markerType;
            }

            public String getValue() {
                return this.value;
            }

            public void setValue(String value) {
                this.value = value;
            }

            public int describeContents() {
                return 0;
            }

            public void writeToParcel(Parcel dest, int flags) {
                dest.writeString(this.markerType);
                dest.writeString(this.value);
            }

            public Marker() {
            }

            protected Marker(Parcel in) {
                this.markerType = in.readString();
                this.value = in.readString();
            }
        }
    }

    public static class EPGProgram {
        @SerializedName("display")
        @Expose
        private EPG.PosterDisplay display;
        @SerializedName("target")
        @Expose
        private Target target;
        @SerializedName("metadata")
        @Expose
        private EPG.EPGMetadata metadata;
        @SerializedName("template")
        @Expose
        private String template;
        @SerializedName("networkInfo")
        @Expose
        private List<NetworkInfo> mNetworkInfo;
        @SerializedName("channelNumber")
        @Expose
        private String channelNumber;

        private String catchup;

        public EPGProgram() {
        }

        public List<NetworkInfo> getmNetworkInfo() {
            return this.mNetworkInfo;
        }

        public void setmNetworkInfo(List<NetworkInfo> mNetworkInfo) {
            this.mNetworkInfo = mNetworkInfo;
        }

        public String getTemplate() {
            return this.template;
        }

        public void setTemplate(String template) {
            this.template = template;
        }

        public EPG.EPGMetadata getMetadata() {
            return this.metadata;
        }

        public void setMetadata(EPG.EPGMetadata metadata) {
            this.metadata = metadata;
        }

        public EPG.PosterDisplay getDisplay() {
            return this.display;
        }

        public void setDisplay(EPG.PosterDisplay display) {
            this.display = display;
        }

        public Target getTarget() {
            return this.target;
        }

        public void setTarget(Target target) {
            this.target = target;
        }

        public void setChannelNumber(String channelNumber) {
            this.channelNumber = channelNumber;
        }

        public String getChannelNumber() {
            return channelNumber;
        }

        public void setCatchup(String catchup) {
            this.catchup = catchup;
        }

        public String isCatchup() {
            return catchup;
        }
    }

    public static class EPGMetadata {
        @SerializedName("monochromeImage")
        @Expose
        private String  monochromeImage;
        @SerializedName("id")
        @Expose
        private String id;
        @SerializedName("channelNumber")
        @Expose
        private String channelNumber;

        public EPGMetadata() {
        }

        public String getMonochromeImage() {
            return this.monochromeImage;
        }

        public void setMonochromeImage(String monochromeImage) {
            this.monochromeImage = monochromeImage;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getChannelNumber() {
            return channelNumber;
        }

        public void setChannelNumber(String channelNumber) {
            this.channelNumber = channelNumber;
        }
    }

    public static class EPGChannel {
        @SerializedName("display")
        @Expose
        private EPG.PosterDisplayChannel display;
        @SerializedName("target")
        @Expose
        private Target target;
        @SerializedName("metadata")
        @Expose
        private EPG.EPGMetadata metadata;
        @SerializedName("networkInfo")
        @Expose
        private List<NetworkInfo> mNetworkInfo;

        public EPGChannel() {
        }

        public List<NetworkInfo> getmNetworkInfo() {
            return this.mNetworkInfo;
        }

        public void setmNetworkInfo(List<NetworkInfo> mNetworkInfo) {
            this.mNetworkInfo = mNetworkInfo;
        }

        public EPG.EPGMetadata getMetadata() {
            return this.metadata;
        }

        public void setMetadata(EPG.EPGMetadata metadata) {
            this.metadata = metadata;
        }

        public EPG.PosterDisplayChannel getDisplay() {
            return this.display;
        }

        public void setDisplay(EPG.PosterDisplayChannel display) {
            this.display = display;
        }

        public Target getTarget() {
            return this.target;
        }

        public void setTarget(Target target) {
            this.target = target;
        }
    }

    public static class EPGData {
        @SerializedName("channel")
        @Expose
        private EPG.EPGChannel channel;
        @SerializedName("programs")
        @Expose
        private List<EPG.EPGProgram> programs = null;

        private List<CardData> cardPrograms;



        public EPGData() {
        }

        public EPG.EPGChannel getChannel() {
            return this.channel;
        }

        public void setChannel(EPG.EPGChannel channel) {
            this.channel = channel;
        }

        public List<EPG.EPGProgram> getPrograms() {
            return this.programs;
        }

        public void setPrograms(List<EPG.EPGProgram> programs) {
            this.programs = programs;
        }

        public List<CardData> getCardPrograms() {
            return cardPrograms;
        }

        public void setCardPrograms(List<CardData> cardPrograms) {
            this.cardPrograms = cardPrograms;
        }
    }

    public static class EPGTab {
        @SerializedName("subtitle")
        @Expose
        private String subtitle;
        @SerializedName("startTime")
        @Expose
        private Long startTime;
        @SerializedName("endTime")
        @Expose
        private Long endTime;
        @SerializedName("isSelected")
        @Expose
        private Boolean isSelected;
        @SerializedName("title")
        @Expose
        private String title;

        private  String formatDate;
        private  String time;

        private Date date;

        public EPGTab() {
        }

        public String getSubtitle() {
            return this.subtitle;
        }

        public void setSubtitle(String subtitle) {
            this.subtitle = subtitle;
        }

        public Long getStartTime() {
            return this.startTime;
        }

        public void setStartTime(Long startTime) {
            this.startTime = startTime;
        }

        public Long getEndTime() {
            return this.endTime;
        }

        public void setEndTime(Long endTime) {
            this.endTime = endTime;
        }

        public Boolean getIsSelected() {
            return this.isSelected;
        }

        public void setIsSelected(Boolean isSelected) {
            this.isSelected = isSelected;
        }

        public String getTitle() {
            return this.title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getFormatDate() {
            return formatDate;
        }

        public void setFormatDate(String formatDate) {
            this.formatDate = formatDate;
        }

        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            this.date = date;
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }
    }
}
