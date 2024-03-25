package com.myplex.myplex.utils;

import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.widget.TextView;


public class FontUtil {
	
	public static Typeface Roboto_Bold;
	public static Typeface Roboto_Medium;
	public static Typeface Roboto_Regular;
	public static Typeface Roboto_Light;
	public static Typeface ss_symbolicons_line;
	public static Typeface digital;
	private static Typeface hindi;
	private static Typeface telugu;
	private static Typeface marathi;
	private static Typeface tamil;
	private static Typeface kannada;
	private static Typeface bengali;
	private static Typeface gujarati;
	public static boolean isFontsLoaded = false;
	
	
	public static void loadFonts(AssetManager mgr){
		Roboto_Bold = createFromAsset(mgr, "fonts/Roboto-Bold.ttf");
		Roboto_Medium = createFromAsset(mgr, "fonts/Roboto-Medium.ttf");
		Roboto_Regular = createFromAsset(mgr, "fonts/Roboto-Regular.ttf");
		Roboto_Light = createFromAsset(mgr, "fonts/Roboto-Light.ttf");
		hindi = createFromAsset(mgr, "fonts/hindi.ttf");
		telugu = createFromAsset(mgr, "fonts/telugu.ttf");
		marathi = createFromAsset(mgr, "fonts/marathi.ttf");
		tamil = createFromAsset(mgr, "fonts/tamil.ttf");
		kannada = createFromAsset(mgr, "fonts/kannada.ttf");
		bengali = createFromAsset(mgr, "fonts/bengali.ttf");
		gujarati = createFromAsset(mgr, "fonts/gujarati.ttf");	
		isFontsLoaded = true;
	}

	private static Typeface createFromAsset(AssetManager mgr , String fontPath){

		 try {
			 return  Typeface.createFromAsset(mgr, fontPath);				 
		 }catch(Throwable e){
			 e.printStackTrace();
		 }

		return Typeface.DEFAULT;
	}
	
	public static void setLocalFont(TextView textView, LocalLanguageUtil.LANGUAGE language){
		
        if(textView == null || language == null ){
            return;
        }
        
		switch (language) {
		case HINDI:
			if (hindi != null) {
				textView.setTypeface(hindi);
			}
			break;
		case GUJARATI:
			if (gujarati != null) {
				textView.setTypeface(gujarati);
			}
			break;
		case TELUGU:
			if (telugu != null) {
				textView.setTypeface(telugu);
			}
			break;
		case MARATHI:
			if (marathi != null) {
				textView.setTypeface(marathi);
			}
			break;
		case KANNADA:
			if (kannada != null) {
				textView.setTypeface(kannada);
			}
			break;
		case BENGALI:
			if (bengali != null) {
				textView.setTypeface(bengali);
			}
			break;
		case TAMIL:
			if (tamil != null) {
				textView.setTypeface(tamil);
			}
			break;
		default:
			break;

		}
    }


}
