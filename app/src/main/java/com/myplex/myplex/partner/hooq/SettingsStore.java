/*
 * Copyright Quickplay Media Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of Quickplay Media Inc. and is protected by copyright
 * law. No license, implied or otherwise is granted by its use, unless licensed by Quickplay Media Inc.
 */
package com.myplex.myplex.partner.hooq;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static android.text.TextUtils.isEmpty;

/**
 * Centralized place we can set settings. This internally today using a Context SharedPreferences object.
 * This might be changed in the future.
 * Further, its always nice to have a centralized store in case any wrapping needs to be done in the future.
 * <br> Warning: the {@link SettingsStore#initialize(Context)}
 * needs to be called before using this class.
 */
public final class SettingsStore {

    /**
     * Default File Id for the Shared Preferences.
     */
    private static final String SHARED_PREFERENCE_FILE_ID = "DEBUG_PREFERENCES_ID";
    /**
     * Common prefix for the preferences keys.
     */
    private static final String DEBUG_PREFIX_ID = "DEBUG_VALUE_";

    /**
     * Bundle Delimiter
     */
    private static final String BUNDLE_DELIMITER = "|";

    /**
     * Context of the callee.
     */
    private Context mContext;

    /**
     * Custom File Id for the Shared Preferences.
     */
    private String mFileId = SHARED_PREFERENCE_FILE_ID;

    /**
     * the key prefix to identify the config file
     */
    private static final String CONFIG_PREFIX_KEY = "CONFIG_PREFIX_KEY";

    private String mConfigKeyPrefix = "";


    private static class Singleton {
        private static SettingsStore sInstance = null;
    }

    static public SettingsStore getInstance() {
        if (Singleton.sInstance == null) {
            throw new IllegalStateException(
                    "SettingsStore instance is null did you call SettingsStore#initialize(Context)?"
            );
        }
        return Singleton.sInstance;
    }

    /**
     * This initialize the singleton it must be called before using the class.
     *
     * @param context Context of the callee.
     */
    public static synchronized void initialize(final Context context) {
        Singleton.sInstance = new SettingsStore(context);
    }

    private SettingsStore(final Context context) {
        mContext = context;
    }

    /**
     * Returns Int Value for specified key with a default of 0
     * @param key The key
     * @return Int value or 0
     */
    public int getIntValue(final String key) {
        return getIntValue(key, 0);
    }

    /**
     * Returns Int Value for specified key with a default specified
     * @param key The key
     * @return Int value or default value
     */
    public int getIntValue(final String key, final int defaultValue) {
        return getPreferences().getInt(getKey(key), defaultValue);
    }

    /**
     * Sets a int value
     * @param key The key
     * @param value The value
     */
    public void setIntValue(final String key, final int value) {
        getPreferences().edit().putInt(getKey(key), value).apply();
    }

    /**
     * Returns a string value for the specified key or null if not found
     * @param key The key to lookup
     * @return String or null
     */
    public String getStringValue(final String key) {
        return getStringValue(key, null);
    }

    /**
     * Returns String Value for specified key with a default specified
     * @param key The key
     * @return String value or default value
     */
    public String getStringValue(final String key, final String defaultValue) {
        return getPreferences().getString(getKey(key), defaultValue);
    }

    /**
     * Sets a String value
     * @param key The key
     * @param value The value
     */
    public void setStringValue(final String key, final String value) {
        getPreferences().edit().putString(getKey(key), value).apply();
    }

    /**
     * Sets a default value to use for a string when the key cannot be found. This will NOT
     * overwrite an existing value if one exists. It only sets the value if there is no key.
     * @param key the key
     * @param value The default value to use
     */
    public void setDefaultStringValue(final String key, final String value) {
        String curr = getStringValue(key, null);
        if(curr == null) {
            setStringValue(key,value);
        }
    }

    /**
     * Returns Boolean Value for specified key with a default of 0
     * @param key The key
     * @return Bollean value or 0
     */
    public boolean getBooleanValue(final String key) {
        return getBooleanValue(key, false);
    }

    /**
     * Returns Boolean Value for specified key with a default specified
     * @param key The key for the value
     * @param defaultValue Default fallback value
     * @return The boolean value associated with the key or the default value
     */
    public boolean getBooleanValue(final String key, final boolean defaultValue) {
        return getPreferences().getBoolean(getKey(key), defaultValue);
    }

    /**
     * Sets a boolean balue with the specified key
     * @param key The key
     * @param value The default value
     */
    public void setBooleanValue(final String key, final boolean value) {
        getPreferences().edit().putBoolean(getKey(key), value).apply();
    }


    /**
     * Save a Bundle object.
     * @param key Key to store bundle under
     * @param bundle The bundle
     */
    public void setBundle(String key, Bundle bundle) {
        setBundleInternal(key, bundle);
        getPreferences().edit().apply();
    }

    private void setBundleInternal(String key, Bundle bundle) {
        final String prefKeyPrefix = key + BUNDLE_DELIMITER;
        Set<String> keys = bundle.keySet();
        Iterator<String> keyIterator = keys.iterator();
        SharedPreferences.Editor editor = getPreferences().edit();
        while (keyIterator.hasNext()){
            String bundleKey = keyIterator.next();
            Object value = bundle.get(bundleKey);
            if (value == null){
                editor.remove(prefKeyPrefix + bundleKey);
            } else if (value instanceof Integer){
                editor.putInt(prefKeyPrefix + bundleKey, (Integer) value);
            } else if (value instanceof Long){
                editor.putLong(prefKeyPrefix + bundleKey, (Long) value);
            } else if (value instanceof Boolean){
                editor.putBoolean(prefKeyPrefix + bundleKey, (Boolean) value);
            } else if (value instanceof CharSequence){
                editor.putString(prefKeyPrefix + bundleKey, ((CharSequence) value).toString());
            } else if (value instanceof Bundle){
                setBundle(prefKeyPrefix + bundleKey, ((Bundle) value));
            }
        }
    }

    /**
     * Will build a bundle stored with {@link #setBundle(String, Bundle)} or null if one cannot be found.
     * @param key The key.
     */
    public Bundle getBundle(String key) {
        final String prefKeyPrefix = key + BUNDLE_DELIMITER;
        Bundle bundle = new Bundle();
        Map<String, ?> all = getPreferences().getAll();
        Iterator<String> it = all.keySet().iterator();
        Set<String> subBundleKeys = new HashSet<>();
        while (it.hasNext()) {
            String prefKey = it.next();
            if (prefKey.startsWith(prefKeyPrefix)) {
                String bundleKey = removeStart(prefKey, prefKeyPrefix);
                if (!bundleKey.contains(BUNDLE_DELIMITER)) {
                    Object o = all.get(prefKey);
                    if (o == null) {
                        // Ignore null keys
                    } else if (o instanceof Integer) {
                        bundle.putInt(bundleKey, (Integer) o);
                    } else if (o instanceof Long) {
                        bundle.putLong(bundleKey, (Long) o);
                    } else if (o instanceof Boolean) {
                        bundle.putBoolean(bundleKey, (Boolean) o);
                    } else if (o instanceof CharSequence) {
                        bundle.putString(bundleKey, ((CharSequence) o).toString());
                    }
                }  else {
                    // Key is for a sub bundle
                    String subBundleKey = substringBefore(bundleKey, BUNDLE_DELIMITER);
                    subBundleKeys.add(subBundleKey);
                }
            }
        }

        // Recursively process the sub-bundles
        for (String subBundleKey : subBundleKeys) {
            Bundle subBundle = getBundle(prefKeyPrefix + subBundleKey);
            bundle.putBundle(subBundleKey, subBundle);
        }

        if(bundle.keySet().size() == 0) {
            return null;
        }
        return bundle;
    }

    public static String substringBefore(String str, String separator) {
        if (!isEmpty(str) && separator != null) {
            if (separator.length() == 0) {
                return "";
            } else {
                int pos = str.indexOf(separator);
                return pos == -1 ? str : str.substring(0, pos);
            }
        } else {
            return str;
        }
    }

    public static String removeStart(String str, String remove) {
        if (!isEmpty(str) && !isEmpty(remove)) {
            return str.startsWith(remove) ? str.substring(remove.length()) : str;
        } else {
            return str;
        }
    }

    /**
     * Prefix to use when define a single key of the {@link android.preference.Preference}.
     * If the name is unknown, an empty string will be used.
     *
     * @return Prefix to use when define a single key of the {@link android.preference.Preference} or an empty string.
     */
    public String getConfigKeyPrefix() {
        if (mConfigKeyPrefix == null || mConfigKeyPrefix.isEmpty()) {
            mConfigKeyPrefix = SettingsStore.getInstance().getStringValue(CONFIG_PREFIX_KEY);
        }
        return mConfigKeyPrefix;
    }

    /**
     * This returns the given key prefixed with default value. Current default value is {@link #getConfigKeyPrefix()}
     *
     * @param key The key to prefix
     * @return The prefixed key
     */
    public String getDefaultPrefixedKey(final String key) {
        return String.format("%s_%s", getConfigKeyPrefix(), key);
    }

    /**
     * set the config prefix
     * @param prefix
     */
    public void setConfigKeyPrefix(String prefix) {
        mConfigKeyPrefix = prefix;
        SettingsStore.getInstance().setStringValue(CONFIG_PREFIX_KEY, prefix);
    }


    /**
     * Sets the File Id associated with the concrete Shared Preferences.
     *
     * @param value The File Id.
     */
    protected void setFileId(final String value) {
        mFileId = value;
    }

    /**
     * Gets the File Id associated with the concrete Shared Preferences.
     *
     * @return The File Id.
     */
    private String getFileId() {
        return mFileId;
    }

    /**
     * Creates and returns the key suitable for the preferences based on the provided value.
     *
     * @param key provided value of the key.
     *
     * @return The key suitable for the preferences.
     */
    private static String getKey(final String key) {
        return DEBUG_PREFIX_ID + key;
    }

    /**
     * Retrieve and hold the contents of the preferences file 'name',
     * returning a SharedPreferences through which you can retrieve and modify its values.
     * Only one instance of the SharedPreferences object is returned to any callers for the same name,
     * meaning they will see each other's edits as soon as they are made.
     *
     * @return A SharedPreferences.
     */
    private SharedPreferences getPreferences() {
        return mContext.getSharedPreferences(getFileId(), Context.MODE_PRIVATE);
    }
}
