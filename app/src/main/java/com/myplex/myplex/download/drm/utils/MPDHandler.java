package com.myplex.myplex.download.drm.utils;


import android.util.Log;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;

import static com.myplex.myplex.ui.fragment.CardDetails.Partners.APALYA;

/**
 * Created by Apalya on 11/18/2016.
 */

public class MPDHandler extends DefaultHandler {

    private AdaptionSet currentaAdaptionSet;
    private RepresentationData currentRepresentationData;

    public void setPartnerType(int partnerType) {
        this.partnerType = partnerType;
    }

    int partnerType = APALYA;
    MPD mpd;
    boolean isVideo;
    String tempVal;

    public MPD getMpd() {
        return mpd;
    }

    public MPDHandler(){
        mpd = new MPD();
    }


    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);

        if (qName.equalsIgnoreCase("AdaptationSet")) {
            if (attributes.getValue("contentType") != null
                    && attributes.getValue("contentType").contains("video")
                    || (attributes.getValue("mimeType") != null
                    && attributes.getValue("mimeType").contains("video"))) {
                if (mpd.adaptionSetList == null)
                    mpd.adaptionSetList = new ArrayList<>();
                currentaAdaptionSet = new AdaptionSet();
                currentaAdaptionSet.type = AdaptionSet.TYPE_VIDEO;
            } else if (attributes.getValue("contentType") != null
                    && attributes.getValue("contentType").contains("audio")
                    || (attributes.getValue("mimeType") != null
                    && attributes.getValue("mimeType").contains("audio"))) {
                if (mpd.adaptionSetList == null)
                    mpd.adaptionSetList = new ArrayList<>();
                currentaAdaptionSet = new AdaptionSet();
                currentaAdaptionSet.type = AdaptionSet.TYPE_AUDIO;
            } else {
                if (mpd.adaptionSetList == null)
                    mpd.adaptionSetList = new ArrayList<>();
                currentaAdaptionSet = new AdaptionSet();
                currentaAdaptionSet.type = AdaptionSet.TYPE_OTHER;
            }
        }

        if (qName.equalsIgnoreCase("Representation")) {
            currentRepresentationData = new RepresentationData();
            try {
                if (attributes.getValue("id") != null) {
                    currentRepresentationData.id = Integer.parseInt(attributes.getValue("id"));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                if (attributes.getValue("bandwidth") != null) {
                    currentRepresentationData.bandwidth = Long.parseLong(attributes.getValue("bandwidth"));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (currentaAdaptionSet.listRepresentations == null)
                currentaAdaptionSet.listRepresentations = new ArrayList<>();
            currentaAdaptionSet.listRepresentations.add(currentRepresentationData);
        }
    }

    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        if (qName.equalsIgnoreCase("BaseURL")) {
//                mpd.setURL(tempVal);
//                mpd.setURL("LSD_movie_282cb999_video_track_0.mp4");
            currentRepresentationData.setUrl(tempVal);
            Log.d("Download", "Download URL- " + tempVal);
            Log.d("Download", "Download currentaAdaptionSet.type- " + currentaAdaptionSet.type);
        }
        if (qName.equalsIgnoreCase("AdaptationSet")) {
            if (mpd.adaptionSetList == null)
                mpd.adaptionSetList = new ArrayList<>();
            mpd.adaptionSetList.add(currentaAdaptionSet);
        }
        if (qName.equalsIgnoreCase("BaseURL")) {
            if ( currentaAdaptionSet.type == AdaptionSet.TYPE_VIDEO) {
                mpd.setVideoURL(tempVal);
            } else if ( currentaAdaptionSet.type == AdaptionSet.TYPE_AUDIO) {
                mpd.setAudioURL(tempVal);
            }
        }

    }


    public void characters(char[] ch, int start, int length)
            throws SAXException {
        tempVal = new String(ch, start, length);
        Log.d("Download","Download tempVal- " + tempVal);
    }

}
