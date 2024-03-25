package com.myplex.myplex.partner.hooq;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


/**
 * Processes the Json Configuration file. This wraps the VSTB Library Configuration.
 * Note the VSTB Sample Configuration has various settings and options, depending upon the center.
 *
 * object for further details.
 */
public class ApplicationConfiguration {
    private static final String VSTB_SAMPLE_KEY = "vstb-sample";
    private static final String VSTB_SAMPLE_BUILTIN_CONTENT_KEY = "builtin-content";
    private static final String ANDROID_CUSTOM_EXTENSION_KEY = "android-custom-sample-app-extension";

    private final String mConfigFilePath;
    private final Context mContext;
    private JSONObject mConfig;
    private String hmac;
    private String sessionToken;
    private String sessionExpiryTime;


    public String getHmac() {
        return hmac;
    }

    public void setHmac(String hmac) {
        this.hmac = hmac;
    }

    public String getSessionToken() {
        return sessionToken;
    }

    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }

    public String getSessionExpiryTime() {
        return sessionExpiryTime;
    }

    public void setSessionExpiryTime(String sessionExpiryTime) {
        this.sessionExpiryTime = sessionExpiryTime;
    }


    public ApplicationConfiguration(final Context context, final String configFileNamePath) {
        super();

        mContext = context;
        mConfigFilePath = configFileNamePath;


    }



    /**
     * Returns Context in this Configuration
     * @return
     */
    public Context getContext() {
        return mContext;
    }


    /**
     * Returns JSON Config used to create this configuration.
     * @return
     */
    public JSONObject getRawJson() {
        return mConfig;
    }

    /**
     * Returns whether or not this configuration contains a OpenVideoService Configuration.
     *
     * @return {@code true} if configuration does contains OpenVideoService plugin, {@code false} otherwise.
     */




    /**
     * Return a JSONObject describing the properties with in the sample application
     * @return JSONObject of sample properties or null if none is specified
     */
    public JSONObject getSampleAppProperties() {
        return mConfig.optJSONObject(VSTB_SAMPLE_KEY);
    }

    /**
     * Returns a JSON Array of Sample App Items listed in the built in configuration area.
     * @param pluginId The id to check for built in items
     * @return JSONArray (even if not built in items, this will return empty array)
     */
    public JSONArray getBuiltinContentItems(String pluginId) {
        JSONArray array = null;
        JSONObject vstbSampleProperties = getSampleAppProperties();
        if(vstbSampleProperties != null) {
            JSONObject builtinItems = vstbSampleProperties.optJSONObject(VSTB_SAMPLE_BUILTIN_CONTENT_KEY);
            if(builtinItems != null) {
                array = builtinItems.optJSONArray(pluginId);
            }
        }
        if(array == null) {
            array = new JSONArray();
        }
        return array;
    }

    /**
     * All QuickPlay Applications are given a 'unique' application ID. Some plugins might require the
     * application ID prior to VSTB Starting. In this case, the App ID will be fetched from the JSON.
     * Depending upon how VSTB is configured, this could be in a few different places. So we need to
     * do a bit of searching.
     * @return Application ID if found or 0 if it cannot be found
     */



    private List<String> getRegisteredApplicationClassNames(JSONObject config) {
        Object customExtensionsJsonObject = getVstbSampleCustomExtensionJsonObject(config);
        List<String> appClasses = new ArrayList<>();

        if(customExtensionsJsonObject == null) {
            return appClasses;
        }

        if(customExtensionsJsonObject instanceof JSONArray) {
            JSONArray customExtensionsJsonArray = (JSONArray)customExtensionsJsonObject;
            for(int i = 0; i < customExtensionsJsonArray.length(); i++) {
                String str = customExtensionsJsonArray.optString(i);
                if(str != null) {
                    appClasses.add(str);
                }
            }
        } else if(customExtensionsJsonObject instanceof String) {
            appClasses.add((String)customExtensionsJsonObject);
        }

        return appClasses;
    }

    private Object getVstbSampleCustomExtensionJsonObject(JSONObject config) {
        JSONObject vstbSampleJson = config;
        if (config != null && config.has(VSTB_SAMPLE_KEY)) {
            vstbSampleJson = config.optJSONObject(VSTB_SAMPLE_KEY);
        }
        if(vstbSampleJson != null) {
            return vstbSampleJson.opt(ANDROID_CUSTOM_EXTENSION_KEY);
        }
        return null;
    }

    private List<String> getDefaultApplicationClassNames() {
        List<String> defaultApplicationClassNames = new ArrayList<>();
        defaultApplicationClassNames.add("com.quickplay.vstb.ref.plugin.chromecast.ChromecastUIPlugin");
        return defaultApplicationClassNames;
    }


}
