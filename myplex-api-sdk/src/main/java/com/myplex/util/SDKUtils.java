package com.myplex.util;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.myplex.api.APICallback;
import com.myplex.api.APIConstants;
import com.myplex.api.APIResponse;
import com.myplex.api.APIService;
import com.myplex.api.request.user.GenerateKeyRequest;
import com.myplex.model.CardExplorerData;
import com.myplex.model.DeviceRegData;
import com.myplex.sdk.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Created by Apalya on 10/27/2015.
 */
public class SDKUtils {

    public static String downloadStoragePath="/sdcard/Android/data/com.sundirect.android/files/";

    public static String getDrmProxy()
    {
        return APIConstants.SCHEME + APIConstants.BASE_URL+ "/" + "licenseproxy/v2/license";
        //return "https://api-beta.myplex.in/licenseproxy/v2/license"
    }

    public static String getIMEINumber(Context ctx) {
        return UUID.randomUUID().toString();
    }

   /* public static String getInternetConnectivity(Context context) {
        String network_type = "Unknown";
        if(context == null){
            return network_type;
        }
        ConnectivityManager manager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo active_network = manager.getActiveNetworkInfo();

        if (active_network == null) {
            return network_type;
        }
        if (active_network.getType() == ConnectivityManager.TYPE_WIFI) {
            network_type = "wifi";
        } else {
            network_type = "mobile";
        }
        return network_type;
        *//*switch (active_network.getType()) {
            case ConnectivityManager.TYPE_WIFI:
                network_type = "wifi";
                break;
            case ConnectivityManager.TYPE_MOBILE:

                switch (active_network.getSubtype()) {
                    case TelephonyManager.NETWORK_TYPE_GPRS:
                    case TelephonyManager.NETWORK_TYPE_EDGE:
                    case TelephonyManager.NETWORK_TYPE_CDMA:
                    case TelephonyManager.NETWORK_TYPE_1xRTT:
                    case TelephonyManager.NETWORK_TYPE_IDEN:

                        network_type = "2G";
                        break;
                    case TelephonyManager.NETWORK_TYPE_UMTS:
                    case TelephonyManager.NETWORK_TYPE_EVDO_0:
                    case TelephonyManager.NETWORK_TYPE_EVDO_A:
                    case TelephonyManager.NETWORK_TYPE_HSDPA:
                    case TelephonyManager.NETWORK_TYPE_HSUPA:
                    case TelephonyManager.NETWORK_TYPE_HSPA:
                    case TelephonyManager.NETWORK_TYPE_EVDO_B:
                    case TelephonyManager.NETWORK_TYPE_EHRPD:
                    case TelephonyManager.NETWORK_TYPE_HSPAP:
                        network_type = "3G";
                        break;
                    case TelephonyManager.NETWORK_TYPE_LTE:
                        network_type = "4G";
                        break;

                }
        }*//*

    }*/

    public static String getInternetConnectivity(Context context) {
        String network_type = "Unknown";
        if(context == null){
            return network_type;
        }
        ConnectivityManager manager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        @SuppressLint("MissingPermission") NetworkInfo active_network = manager.getActiveNetworkInfo();

        if (active_network == null) {
            return network_type;
        }
        switch (active_network.getType()) {
            case ConnectivityManager.TYPE_WIFI:
                network_type = "wifi";
                break;
            case ConnectivityManager.TYPE_MOBILE:
                if (active_network.getSubtype() > TelephonyManager.NETWORK_TYPE_EDGE) {
                    network_type = "3G";
                }

                switch (active_network.getSubtype()) {
                    case TelephonyManager.NETWORK_TYPE_GPRS:

                    case TelephonyManager.NETWORK_TYPE_EDGE:

                        network_type = "2G";
                        break;

                    case TelephonyManager.NETWORK_TYPE_LTE:
                        network_type = "4G";
                        break;
                        case TelephonyManager.NETWORK_TYPE_NR:
                        network_type = "5G";
                        break;

                }
        }
        return network_type;

    }


    public static Object loadObject(String path) {
        try {
            File f = new File(path);
            if (!f.exists()) {
                return null;
            }
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f));
            Object o = ois.readObject();
            ois.close();
            return o;
        } catch (Exception ex) {
            if(ex != null){
//				Log.v("Util", ex.getMessage());
                ex.printStackTrace();
            }
        }
        return null;
    }

    public static void saveObject(final Object obj, final String path) {
        SDKLogger.debug("saving obj- " + obj + " path- " + path);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File(path)));
                    oos.writeObject(obj);
                    oos.flush();
                    oos.close();
                } catch (Exception ex) {
                    if (ex != null) {
//				Log.v("Util", ex.getMessage());
                        ex.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public static CardExplorerData sCardExplorerData;
    public static CardExplorerData getCardExplorerData(){
        if(sCardExplorerData==null)
            sCardExplorerData=new CardExplorerData();

        return sCardExplorerData;
    }

    public static void launchBrowserIntent(Context context, String redirectLink) {
        try {
            if(TextUtils.isEmpty(redirectLink) || context == null){
                return;
            }
            Log.d("SDKUtils", "launchBrowser: redirectLink- " + redirectLink);
            Uri appStoreLink = Uri.parse(redirectLink);
            context.startActivity(new Intent(Intent.ACTION_VIEW, appStoreLink));
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static String getMCCAndMNCValues(Context mContext){
        String codes ="";
        if(mContext == null){
            return codes;
        }
        TelephonyManager tel = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        String networkOperator = tel.getNetworkOperator();
        if (networkOperator != null && !networkOperator.isEmpty()) {
            try{
                int mcc = Integer.parseInt(networkOperator.substring(0, 3));
                int mnc = Integer.parseInt(networkOperator.substring(3));
                codes = mcc+","+mnc;
            }catch(NumberFormatException e){
                e.printStackTrace();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return codes;
    }

    public static int getColor(Context mContext, int id) {
        final int version = Build.VERSION.SDK_INT;
        if (version >= 23) {
            return ContextCompat.getColor(mContext, id);
        } else {
            return mContext.getResources().getColor(id);
        }
    }

    public interface RegenerateKeyRequestCallback {
        void onSuccess();

        void onFailed(String msg);
    }

    public static void makeReGenerateKeyRequest(final Context mContext, final RegenerateKeyRequestCallback regenerateKeyRequestCallback) {
        GenerateKeyRequest generateKeyRequest = new GenerateKeyRequest(new APICallback<DeviceRegData>() {
            @Override
            public void onResponse(APIResponse<DeviceRegData> response) {

                if (response == null || response.body() == null) {
                    SDKLogger.debug("response: clientKey re-reg: response is null");
                    if (regenerateKeyRequestCallback != null) {
                        regenerateKeyRequestCallback.onFailed("response: clientKey re-gen: response is null");
                    }
                }
                SDKLogger.debug("response: clientKey re-reg: response.body().status: " + response.body().status);

                DeviceRegData devRegResponse = response.body();

                if (!devRegResponse.status.equalsIgnoreCase("SUCCESS")) {
                    SDKLogger.debug("response: clientKey re-reg: response.body().status: " + response.body().status);
                    if (regenerateKeyRequestCallback != null) {
                        regenerateKeyRequestCallback.onFailed(devRegResponse.message);
                    }
                    return;
                }

                if (devRegResponse.status.equalsIgnoreCase("SUCCESS")) {
                    if(null != response.body()
                            && null != response.body().clientKey
                            && null != response.body().deviceId
                            && null != response.body().expiresAt){
                        PrefUtils.getInstance().setPrefClientkey( response.body().clientKey);
                        PrefUtils.getInstance().setPrefDeviceid(response.body().deviceId);
                        PrefUtils.getInstance().setPrefClientkeyExpiry(response.body().expiresAt);
                    }
                    if (regenerateKeyRequestCallback != null) {
                        regenerateKeyRequestCallback.onSuccess();
                    }
                }
            }

            @Override
            public void onFailure(Throwable t, int errorCode) {
                SDKLogger.debug("response: clientKey re-reg: Failed: " + t);
                String reason = null;
                if (mContext != null) {
                    reason = mContext.getString(R.string.network_error);
                }
                if (regenerateKeyRequestCallback != null) {
                    regenerateKeyRequestCallback.onFailed(reason);
                }
            }
        });
        APIService.getInstance().execute(generateKeyRequest);
    }

    public static boolean isTablet(@NonNull Context context){
        return context.getResources().getBoolean(R.bool.isTablet);
    }

    public static String getLogFolderLocation(Context mContext) {
        return mContext.getExternalFilesDir(null) + "/.vfdumplogs";
    }

    public static void deleteRecursive(final File fileOrDirectory) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (fileOrDirectory == null) {
                    return;
                }
                if (fileOrDirectory.isDirectory()) {
                    for (File child : fileOrDirectory.listFiles()) {
                        deleteRecursive(child);
                    }
                }
                fileOrDirectory.delete();
            }
        }).start();
    }


    public static void deleteLogFile(Context mContext) {
        String logDir = getLogFolderLocation(mContext);
        deleteRecursive(new File(logDir));
    }


    public static void captureLogsToSDCard(Context mContext) {
        File appDirectory = new File(SDKUtils.getLogFolderLocation(mContext));
        File logFile = new File(appDirectory, "logcat" + System.currentTimeMillis() + ".txt");

        // create app folder
        if (!appDirectory.exists()) {
            appDirectory.mkdir();
        }

        // clear the previous logcat and then write the new one to the file
        try {
            fetch(logFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static final String COMMAND = "logcat -d -v time";


    public static void fetch(OutputStream out, boolean close) throws IOException {
        byte[] log = new byte[1024 * 2];
        InputStream in = null;
        try {
            Process proc = Runtime.getRuntime().exec(COMMAND);
            in = proc.getInputStream();
            int read = in.read(log);
            while (-1 != read) {
                out.write(log, 0, read);
                read = in.read(log);
            }
        }
        finally {
            if (null != in) {
                try {
                    in.close();
                }
                catch (IOException e) {
                    // ignore
                }
            }

            if (null != out) {
                try {
                    out.flush();
                    if (close)
                        out.close();
                }
                catch (IOException e) {
                    // ignore
                }
            }
        }
    }

    public static void fetch(File file) throws IOException {
        FileOutputStream fos = new FileOutputStream(file);
        fetch(fos, true);
    }
}
