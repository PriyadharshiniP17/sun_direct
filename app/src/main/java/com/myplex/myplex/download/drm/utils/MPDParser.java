package com.myplex.myplex.download.drm.utils;

import android.os.AsyncTask;
import android.util.Log;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.parsers.SAXParserFactory;

import static com.myplex.myplex.ui.fragment.CardDetails.Partners.APALYA;

/**
 * Created by Apalya on 11/18/2016.
 */

public class MPDParser extends AsyncTask<String, Void, MPD> {

    String URL;

    public void setPartnerType(int partnerType) {
        this.partnerType = partnerType;
    }

    int partnerType = APALYA;
    public enum Progress{ FETCHING, ANALYSING, FINISHED}

    public void addParserListerner(MDPParserListerner mdpParserListerner) {
        this.mdpParserListerner = mdpParserListerner;
    }

    MDPParserListerner mdpParserListerner;

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if(mdpParserListerner != null){
            mdpParserListerner.OnParseProgress(Progress.FETCHING);
        }
    }

    @Override
    protected MPD doInBackground(String... params) {
        URL = params[0];
        if (URL != null
                && !URL.contains("http:")
                && !URL.contains("https:")) {
            try {
                InputStream stream = new FileInputStream(URL);
                if (mdpParserListerner != null) {
                    mdpParserListerner.OnParseProgress(Progress.ANALYSING);
                }
                return parse(stream);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            return null;
        }
        String xml = getXmlFromUrl(URL);

        InputStream stream = new ByteArrayInputStream(xml.getBytes());
        if(mdpParserListerner != null){
            mdpParserListerner.OnParseProgress(Progress.ANALYSING);
        }
        return parse(stream);
    }

    @Override
    protected void onPostExecute(MPD mpd) {
        super.onPostExecute(mpd);
        if(mdpParserListerner != null){
            mdpParserListerner.OnParseProgress(Progress.FINISHED);
        }
        if (mpd == null) {
            if(mdpParserListerner != null){
                mdpParserListerner.OnParseFailed();
            }
        } else {
//            mpd.setPartnerType(partnerType);
            mpd.setStreamURL(URL);
            if(mdpParserListerner != null){
                mdpParserListerner.OnParseSuccess(mpd);
            }
        }
    }

    public interface MDPParserListerner {
        public void OnParseFailed();

        public void OnParseSuccess(MPD MPD);

        public void OnParseProgress(Progress update);
    }

    private String getXmlFromUrl(String urlString) {
        StringBuffer output = new StringBuffer("");
        try {
            InputStream stream = null;
            java.net.URL url = new URL(urlString);
            URLConnection connection = url.openConnection();

            HttpURLConnection httpConnection = (HttpURLConnection) connection;
            httpConnection.setRequestMethod("GET");
            httpConnection.connect();

            if (httpConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                stream = httpConnection.getInputStream();

                BufferedReader buffer = new BufferedReader(
                        new InputStreamReader(stream));
                String s = "";
                while ((s = buffer.readLine()) != null)
                    output.append(s);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        Log.d("MDP", "setStreamURL: " + output.toString());
        return output.toString();
    }

    public MPD parse(InputStream is) {
        try {
            XMLReader xmlReader = SAXParserFactory.newInstance().newSAXParser()
                    .getXMLReader();
            MPDHandler mpdHandler = new MPDHandler();
            xmlReader.setContentHandler(mpdHandler);
            xmlReader.parse(new InputSource(is));
            return mpdHandler.getMpd();
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
