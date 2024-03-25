package com.myplex.util;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.myplex.api.APIConstants;
import com.myplex.api.myplexAPISDK;
import com.myplex.model.LocationInfo;

import java.io.IOException;
import java.util.List;
import java.util.Locale;


/**
 * This class is a singleton class for accessing Location as well as city name
 * <br>The sample call will be like <br> <b>
 * LocationUtil  lUtil =   LocationUtil.getInstance();</b> <br>
 * for getting Location <br> <b>lUtil.getLocation();</b>
 * <p>Don't forget to close the connection. close the connection by  lUtil.close();
 */
public class LocationUtil {
    public static final String TAG = "LocationUtil";
    private GoogleApiClient client;
    private Location location = null;
    private Context context;
    List<Address> addresses = null;
    private static LocationUtil locationInstance;
    private LocationInfo locationInfo;
    private boolean isDemo = true;

    private LocationUtil(Context context) {
        this.context = context;
        if (isLocationEnabled(context)) {
            client = new GoogleApiClient.Builder(context)
                    .addConnectionCallbacks(new ConnectionCallbacks())
                    .addOnConnectionFailedListener(new ConnectionFailedCallBack())
                    .addApi(LocationServices.API)
                    .build();
            client.connect();
        } else {
            // Location service is not enabled.
//			Util.showToast("Location access is not enabled. Please enable it.", context);
        }
    }

    public static LocationUtil getInstance(Context context) {
        if (locationInstance == null) {
            locationInstance = new LocationUtil(context);
        }
        return locationInstance;
    }

    public void init() {
        if (locationInfo == null) {
            getAddressParams();
        }
    }

    public LocationInfo getLocationInfo() {
        return locationInfo;
    }

    /**
     * @return the String as params  and <b>""(Empty String or in case of )</b> if location service is not enabled.
     */
    public LocationInfo getAddressParams() {
        /*if(isDemo){
            locationInfo = new LocationInfo();
            locationInfo.area = "Hyderabad";
            locationInfo.country = "IN";
            locationInfo.postalCode = "500001";
            return locationInfo;
        }*/
        if (!isLocationEnabled(context)) {
            if (!TextUtils.isEmpty(PrefUtils.getInstance().getPrefLocation())) {
                locationInfo = (LocationInfo) SDKUtils.loadObject(APIConstants.locationPath);
                close();
            }
            return locationInfo;
        }
        getLocation();
        if (location != null) {
            try {
                new GetAddressTask().execute(new Location[]{location});
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (locationInfo == null) {
                new GetAddressTask().execute(new Location[]{location});
            }
        }
        return locationInfo;
    }

    public Location getLocation() {
        if (client != null) {
            if (client.isConnected()) {
                location = LocationServices.FusedLocationApi.getLastLocation(client);
                close();
            }
        }
        return location;
    }

    /**
     * Disconnect and close the connection for fetching the location. To be called when you are no more interested to fetch the location.
     * If you are not calling this method the location will be fetched although you are not using it.
     */
    public void close() {
        if (client != null && (client.isConnected()
                || client.isConnecting()))
            client.disconnect();
    }

    class ConnectionCallbacks implements GoogleApiClient.ConnectionCallbacks {
        @Override
        public void onConnected(Bundle arg0) {
            // Gets the best and most recent location currently available, which may be null
            // in rare cases when a location is not available.
            location = LocationServices.FusedLocationApi.getLastLocation(client);
            init();
            Log.d("GoogleApiClient", "onConnected");
        }

        @Override
        public void onConnectionSuspended(int i) {
            Log.d("GoogleApiClient", "onConnectionSuspended");

        }
    }

    ;

    class ConnectionFailedCallBack implements GoogleApiClient.OnConnectionFailedListener {
        @Override
        public void onConnectionFailed(ConnectionResult cResult) {
            /**
             * There is a connection error.so we have to handle the connection problems here.
             */
            Log.d("GoogleApiClient", "onConnectionFailed");

        }
    }


    private class GetAddressTask extends AsyncTask<Location, Void, LocationInfo> {

        @Override
        protected LocationInfo doInBackground(Location... param) {
            Geocoder geocoder =
                    new Geocoder(context, Locale.getDefault());
            Location loc = param[0];
            String state = null;
            try {

                locationInfo = new LocationInfo();
                addresses = geocoder.getFromLocation(loc.getLatitude(),
                        loc.getLongitude(), 1);
                if (addresses != null && addresses.size() > 0) {
                    Address address = addresses.get(0);
                    Log.d("GetAddressTask", "addresses- " + address);
                    if (address.getCountryCode() != null)
                        locationInfo.country = address.getCountryCode().replaceAll(" ", "%20");
                    if (address.getAdminArea() != null)
                        locationInfo.state = address.getAdminArea().replaceAll(" ", "%20");
                    if (address.getPostalCode() != null)
                        locationInfo.postalCode = address.getPostalCode().replaceAll(" ", "%20");
                    if (address.getLocality() != null)
                        locationInfo.area = address.getLocality().replaceAll(" ", "%20");
                    locationInfo.longitude = String.valueOf(loc.getLongitude());
                    locationInfo.latitude = String.valueOf(loc.getLatitude());
                    Locale[] locales = Locale.getAvailableLocales();

                    for (Locale localeIn : locales) {
                        if (address.getCountryCode().equalsIgnoreCase(localeIn.getCountry())) {
                            locationInfo.languageCode = localeIn.getLanguage(); //This is language code, ex : en
                            locationInfo.languageName = localeIn.getDisplayLanguage();// This is language name, ex : English

                            break;
                        }
                    }
                    state = address.getAdminArea();
                }
                Log.d("GetAddressTask", "locationData.area - " + locationInfo.area);
            } catch (IOException e1) {
                Log.e("GetAddressTask",
                        "IO Exception in getFromLocation()");
                e1.printStackTrace();
                Log.d("GetAddressTask", "Exception- " + e1);
                return null;
            } catch (IllegalArgumentException e2) {
                // Error message to post in the log
                String errorString = "Illegal arguments " +
                        loc.getLatitude() +
                        " , " +
                        loc.getLongitude() +
                        " passed to address service";
                Log.e("GetAddressTask", errorString);
                e2.printStackTrace();
                return null;
            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (state != null && state.length() > 0) {
                //Log.d(TAG, "state =" + state);
//			SharedPrefUtils.writeToSharedPref(context, context.getString(R.string.pref_state),state);
            }
            if (APIConstants.locationPath == null) {
                APIConstants.locationPath = myplexAPISDK.getApplicationContext().getFilesDir() + "/" + "location.bin";
            }
            SDKUtils.saveObject(locationInfo, APIConstants.locationPath);
            return locationInfo;
        }

    }

    private boolean isLocationEnabled(Context context) {
        boolean isLocationEnabled = false;
        LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        /*if(manager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            isLocationEnabled = true;
		}else */
        if (manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            isLocationEnabled = true;
        }
        SDKLogger.debug("isLocationEnabled- " + isLocationEnabled);
        return isLocationEnabled;
    }

}
