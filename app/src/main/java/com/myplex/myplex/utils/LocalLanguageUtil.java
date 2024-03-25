package com.myplex.myplex.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Build;
import android.util.TypedValue;
import android.widget.TextView;



import com.myplex.myplex.data.Word;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Created by Apalya on 9/10/2014.
 */
public class LocalLanguageUtil {
	public enum LANGUAGE {
		HINDI, ENGLISH,TELUGU,MARATHI,GUJARATI,KANNADA,BENGALI,TAMIL,NATIVE
	};
	public static final String TAG = "LocalLanguageUtil";

	private static HashMap<String, Word> languagesLocalNames = new HashMap<String, Word>();

	public static String changedToLocalText(TextView textview, String from) {

		if (from == null || textview == null) {
			return null;
		}

		if (!languagesLocalNames.containsKey(from.toLowerCase())) {
			return null;
		}

		if (languagesLocalNames.get(from.toLowerCase()).localString.isEmpty()) {
			return null;
		}

		try {

			Word localWord = languagesLocalNames.get(from.toLowerCase()).localString
					.get(0);

			LANGUAGE localLanguage = LANGUAGE
					.valueOf(localWord.lan.toUpperCase());

			FontUtil.setLocalFont(textview, localLanguage);
//			textview.setText(localWord.text); 

			return localWord.text;
		} catch (Exception e) {
			return null;
		}
	}

	public static enum Languages {
		te, ta, hi, gu, bn, mr, kn, en, bengali, english, gujarati, hindi, kannada, marathi, tamil, telugu
	}


	public static boolean checkLanguageSupport(Context mContext, String lang) {
		lang = Character.toUpperCase(lang.charAt(0)) + lang.substring(1);

		if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
			return checkLangFont(lang) ? true : checkLocale(lang);
		}
		if (!checkLocale("Hindi")) {
			String[] locale = Resources.getSystem().getAssets().getLocales();
			for (int i = 0; i < locale.length; i++) {
				if (lang.equalsIgnoreCase(Languages.kannada.name())) {
					lang = "kn";
				} else if (lang.equalsIgnoreCase(Languages.marathi.name())) {
					lang = "mr";
				} else if (lang.equalsIgnoreCase(Languages.bengali.name())) {
					lang = "bn";
				}
				String twoChars = locale[i].substring(0,
						Math.min(locale[i].length(), 2));
				String loc_lang = lang.substring(0, Math.min(lang.length(), 2));
				if (twoChars.equalsIgnoreCase(loc_lang)) {
					return true;
				}
			}
			return false;
		}
		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR1) {
			if (lang.equalsIgnoreCase(Languages.gujarati.name())) {
				return checkLangFont(Languages.gujarati.name());
			}
		}
		return checkLocale(lang);
	}

	public static boolean checkLocale(String lang) {
		Locale[] locale = Locale.getAvailableLocales();
		for (Locale loc : locale) {
			String language = loc.getDisplayLanguage(Locale.getDefault());
			if (language.contains(lang)) {
				return true;
			}
		}
		return false;
	}

	@SuppressWarnings("rawtypes")
	public static boolean checkLangFont(String lang) {
		HashMap<String, String> lagsMap = FontManager.enumerateFonts();
		boolean isLangFound = false;
		// Get a set of the entries
		Set set = lagsMap.entrySet();
		// Get an iterator
		Iterator i = set.iterator();
		// Display elements
		while (i.hasNext()) {
			Map.Entry me = (Map.Entry) i.next();
			if (((String) me.getValue()).contains(lang)) {
				isLangFound = true;
				break;
			} else {
				isLangFound = false;
			}
		}
		if (isLangFound) {
			return true;
		} else {
			return false;
		}
	}

	

public static Bitmap getFontBitmap(Context mContext, String text,String fontName) {
		int fontSizePX = convertDiptoPix(16, mContext);
		int pad = (fontSizePX / 9);
		
		Paint paint = new Paint();
		Typeface typeface = Typeface
				.createFromAsset(mContext.getAssets(), fontName);
		paint.setAntiAlias(true);
		paint.setTypeface(typeface);
		paint.setColor(Color.WHITE);
		paint.setTextSize(fontSizePX);

		int textWidth = (int) (paint.measureText(text) + pad * 4);
		int height = (int) (fontSizePX / 0.60);
		
		Bitmap bitmap = Bitmap.createBitmap(textWidth, height,	Bitmap.Config.ARGB_4444);
		Canvas canvas = new Canvas(bitmap);
		float xOriginal = pad;		
		canvas.drawText(text, xOriginal, fontSizePX, paint);	
		return bitmap;
	}

	public static int convertDiptoPix(float dip,Context mContext) {

		int value = (int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, dip,mContext.getResources()
						.getDisplayMetrics());
		return value;
	}
	
}
