package com.myplex.myplex.media;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.BatteryManager;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;

import com.myplex.model.ErrorManagerData;

import org.apache.http.conn.util.InetAddressUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ErrorManager {
	private List<ErrorManagerData> queue = new ArrayList<ErrorManagerData>();
	private Context mLocalContext;
	private boolean registered = true;
	private MyPhoneStateListener mNetworkStrengthListener = new MyPhoneStateListener();

	public ErrorManager(Context context) {
		mLocalContext = context;
	}

	public void addQueue(ErrorManagerData data) {
		queue.add(data);
		prepareRealData();
	}

	private void sendErrorMsg() {/*
		try {
			ErrorManagerData data = queue.get(0);
			queue.remove(0);
			AptvHttpEngineImpl engine = new AptvHttpEngineImpl(mLocalContext);
			HashMap<String, String> postdata = new HashMap<String, String>();
			postdata.put("servicename", data.serviceName);
			postdata.put("contentname", data.contentName);
			postdata.put("url", data.url);
			postdata.put("apn", (String) sessionData.getInstance().objectHolder.get(Common.DefaultAccessPoint));
			postdata.put("devicemodel", Build.MODEL + " " + Build.MANUFACTURER);
			postdata.put("deviceosversion", "" + Build.VERSION.RELEASE);
			postdata.put("deviceip",""+ (String) sessionData.getInstance().objectHolder.get(Common.IPAddress));
			postdata.put("networkmode","" + AptvEngineUtils.networkMode(mLocalContext));
			postdata.put("networkstrength",""+ (String) sessionData.getInstance().objectHolder.get(Common.NetworkStrength));
			postdata.put("networkstrengthlevel",""+ (String) sessionData.getInstance().objectHolder.get(Common.NetworkStrengthLevel));
			postdata.put("downloadspeed",""+ (String) sessionData.getInstance().objectHolder.get(Common.DownloadSpeed));
			postdata.put("batterystrength",""+ (String) sessionData.getInstance().objectHolder.get(Common.BatteryStrength));
			postdata.put("userid", ""+ (String) sessionData.getInstance().msisdn);
			postdata.put("playposition", data.playposition);
			engine.setBody(postdata);

			String url = "http://"
					+ mLocalContext.getResources().getString(R.string.sgduip)
					+ "/WEB-3/errorLog";
			engine.doPost(url, new AptvHttpEngineListener() {

				@Override
				public void connectionResponse(int arg0) {

				}

				@Override
				public void PayLoad(InputStream arg0) {
					if (queue.size() != 0) {
						prepareRealData();
					}
				}

				@Override
				public void Header(HashMap<String, String> arg0) {

				}
			});
		} catch (Exception e) {
		}

	*/}

	private void calulateDataSpeed(){/*
		AptvHttpEngineImpl engine = new AptvHttpEngineImpl(mLocalContext);
		String url = "http://"+mLocalContext.getResources().getString(R.string.sgduip)+"/aptv3-downloads/download/speedtest.txt";
		final long timebefore = System.currentTimeMillis();
		sessionData.getInstance().objectHolder.put(Common.DownloadSpeed,"");
		engine.doGet(url,new AptvHttpEngineListener() {
			
			@Override
			public void connectionResponse(int responseCode) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void PayLoad(InputStream data) {
				if(data != null){
					try {
						int length = toByteArray(data).length;
						long downloadtimeinsecs = (System.currentTimeMillis() - timebefore)/1000;
						double numberbytespersecond = length / downloadtimeinsecs; 
						double kilobytepersecond = 1024/numberbytespersecond;
						
						sessionData.getInstance().objectHolder.put(Common.DownloadSpeed,""+roundToDecimals(kilobytepersecond,3)+"Kbps");
					} catch (Exception e) {
						// TODO: handle exception
					}
				}
				sendErrorMsg();
			}
			
			@Override
			public void Header(HashMap<String, String> header_key_values) {
				
			}
		});
		
	*/}
	public static double roundToDecimals(double d, int c){   
	   int temp = (int)(d * Math.pow(10 , c));  
	   return ((double)temp)/Math.pow(10 , c);  
	}
	public static byte[] toByteArray(final InputStream input)
			throws IOException {
		final ByteArrayOutputStream output = new ByteArrayOutputStream();
		copy(input, output);
		return output.toByteArray();
	}

	private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;
	private static final int EOF = -1;

	public static int copy(final InputStream input, final OutputStream output)
			throws IOException {
		final long count = copyLarge(input, output);
		if (count > Integer.MAX_VALUE) {
			return -1;
		}
		return (int) count;
	}

	public static long copyLarge(final InputStream input,
			final OutputStream output) throws IOException {
		return copy(input, output, DEFAULT_BUFFER_SIZE);
	}

	public static long copy(final InputStream input, final OutputStream output,
			final int bufferSize) throws IOException {
		return copyLarge(input, output, new byte[bufferSize]);
	}

	public static long copyLarge(final InputStream input,
			final OutputStream output, final byte[] buffer) throws IOException {
		long count = 0;
		int n = 0;
		while (EOF != (n = input.read(buffer))) {
			output.write(buffer, 0, n);
			count += n;
		}
		return count;
	}

	private void prepareRealData() {
		startListeningNetworkStrength();
	}

	private void foundNetworkStrength() {
		stopListeningNetworkStrength();
		startListeningBatteryStrength();
	}

	private void foundBatteryStrength() {
		stopListeningBatteryStrength();
		fillDefaultAccessPoint();
		fillIPAddress(true);
		calulateDataSpeed();
	}

	private void startListeningNetworkStrength() {
		TelephonyManager Tel = (TelephonyManager) mLocalContext
				.getSystemService(Context.TELEPHONY_SERVICE);
		Tel.listen(mNetworkStrengthListener,
				PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
	}

	private void stopListeningNetworkStrength() {
		TelephonyManager Tel = (TelephonyManager) mLocalContext
				.getSystemService(Context.TELEPHONY_SERVICE);
		Tel.listen(mNetworkStrengthListener, PhoneStateListener.LISTEN_NONE);
	}

	private void startListeningBatteryStrength() {
		try {
			registered = true;
			IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
			mLocalContext.registerReceiver(batteryReceiver, filter);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	private void stopListeningBatteryStrength() {
		try {
			registered = false;
			mLocalContext.unregisterReceiver(batteryReceiver);		
		} catch (Exception e) {
			e.printStackTrace();
		}
	
	}

	private class MyPhoneStateListener extends PhoneStateListener {
		@Override
		public void onSignalStrengthsChanged(SignalStrength signalStrength) {
			super.onSignalStrengthsChanged(signalStrength);
			try {
				String signalStrengthraw = String.valueOf(signalStrength
						.getGsmSignalStrength()) + " asu";
				String signalStrengthLevel = getGsmLevel(signalStrength
						.getGsmSignalStrength());
//				sessionData.getInstance().objectHolder.put(Common.NetworkStrength, signalStrengthraw);
//				sessionData.getInstance().objectHolder.put(Common.NetworkStrengthLevel, signalStrengthLevel);
				// Toast.makeText(getApplicationContext(),
				// "Go to Firstdroid!!! GSM Cinr = "+
				// String.valueOf(signalStrength.getGsmSignalStrength()),
				// Toast.LENGTH_SHORT).show();
			} catch (Exception e) {
				e.printStackTrace();
			}
			foundNetworkStrength();
		}
	};

	public String getGsmLevel(int strength) {
		String level = "UNKNOWN_OR_NONE";
		// ASU ranges from 0 to 31 - TS 27.007 Sec 8.5
		// asu = 0 (-113dB or less) is very weak
		// signal, its better to show 0 bars to the user in such cases.
		// asu = 99 is a special case, where the signal strength is unknown.
		int asu = strength;
		if (asu <= 2 || asu == 99)
			level = "UNKNOWN_OR_NONE";
		else if (asu >= 12)
			level = "GREAT";
		else if (asu >= 8)
			level = "GOOD";
		else if (asu >= 5)
			level = "MODERATE";
		else
			level = "POOR";
		;
		return level;
	}

	private void fillDefaultAccessPoint() {
		try {
			// path to preffered APNs
			final Uri PREFERRED_APN_URI = Uri
					.parse("content://telephony/carriers/preferapn");

			// receiving cursor to preffered APN table
			Cursor c = mLocalContext.getContentResolver().query(
					PREFERRED_APN_URI, null, null, null, null);

			// moving the cursor to beggining of the table
			c.moveToFirst();

			// now the cursor points to the first preffered APN and we can get
			// some
			// information about it
			// for example first preffered APN id
			int index = c.getColumnIndex("_id"); // getting index of required
													// column
			Short id = c.getShort(index); // getting APN's id from

			String accesspointName = new String();
			// we can get APN name by the same way
			index = c.getColumnIndex("name");
			String name = c.getString(index);
			accesspointName = name;

			index = c.getColumnIndex("proxy");
			String proxy = c.getString(index);

			if (proxy != null && proxy.length() > 0) {
				accesspointName += "::proxy:" + proxy;
			}

			index = c.getColumnIndex("port");
			String port = c.getString(index);
			if (port != null && port.length() > 0) {
				accesspointName += "::port" + port;
			}

//			sessionData.getInstance().objectHolder.put(Common.DefaultAccessPoint, accesspointName);
			// and any other APN properties: numeric, mcc, mnc, apn, user,
			// server,
			// password, proxy, port, mmsproxy, mmsport, mmsc, type, current
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void fillIPAddress(boolean useIPv4) {
		try {
			List<NetworkInterface> interfaces = Collections
					.list(NetworkInterface.getNetworkInterfaces());
			for (NetworkInterface intf : interfaces) {
				List<InetAddress> addrs = Collections.list(intf
						.getInetAddresses());
				for (InetAddress addr : addrs) {
					if (!addr.isLoopbackAddress()) {
						String sAddr = addr.getHostAddress().toUpperCase();
						boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
						if (useIPv4) {
							if (isIPv4)
//								sessionData.getInstance().objectHolder.put(Common.IPAddress, sAddr);
							return;
						} else {
							if (!isIPv4) {
								int delim = sAddr.indexOf('%'); // drop ip6 port
																// suffix

								if (delim < 0) {
//									sessionData.getInstance().objectHolder.put(Common.IPAddress, sAddr);
								} else {
//									sessionData.getInstance().objectHolder.put(Common.IPAddress,sAddr.substring(0, delim));
								}
								return;
							}
						}
					}
				}
			}
		} catch (Exception ex) {
		} // for now eat exceptions
//		sessionData.getInstance().objectHolder.put(Common.IPAddress, "");
	}

	private BroadcastReceiver batteryReceiver = new BroadcastReceiver() {
		int scale = -1;
		int level = -1;

		@Override
		public void onReceive(Context context, Intent intent) {
			try {
				level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
				scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
				// Toast.makeText(getApplicationContext(),
				// "level is "+level+"/"+scale, Toast.LENGTH_SHORT).show();
//				sessionData.getInstance().objectHolder.put(Common.BatteryStrength, level + "/" + scale);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if(registered){
				foundBatteryStrength();
			}
		}
	};
}
