package com.myplex.myplex.utils;

import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.widget.TextView;


public class LangUtil {

    //Language codes for Audio_Tracks
    public static final String LANGUAGE_CODE_TE = "te";
    public static final String LANGUAGE_CODE_HI = "hi";
    public static final String LANGUAGE_CODE_EN = "en";
    public static final String LANGUAGE_CODE_TA = "ta";
    public static final String LANGUAGE_CODE_ML = "ml";
    public static final String LANGUAGE_CODE_FR = "fr";
    public static final String LANGUAGE_CODE_ES = "es";
    public static final String LANGUAGE_CODE_MR = "mr";
    public static final String LANGUAGE_CODE_BN = "bn";
    public static final String LANGUAGE_CODE_KN = "kn";
    public static final String LANGUAGE_CODE_GU = "gu";
    public static final String LANGUAGE_CODE_OD = "odi";
    public static final String LANGUAGE_CODE_UR = "ur";

    public static final String LANGUAGE_CODE_TELUGU = "Telugu";
    public static final String LANGUAGE_CODE_HINDI = "Hindi";
    public static final String LANGUAGE_CODE_ENGLISH = "English";
    public static final String LANGUAGE_CODE_TAMIL = "Tamil";
    public static final String LANGUAGE_CODE_FRENCH = "French";
    public static final String LANGUAGE_CODE_SPANISH = "Spanish";
    public static final String LANGUAGE_CODE_MALAYALAM = "Malayalam";
    public static final String LANGUAGE_CODE_KANNADA = "Kannada";
    public static final String LANGUAGE_CODE_GUJARATI = "Gujarati";
    public static final String LANGUAGE_CODE_BENGALI = "Bengali";
    public static final String LANGUAGE_CODE_MARATHI = "Marathi";
    public static final String LANGUAGE_CODE_ODIA = "Odia";
    public static final String LANGUAGE_CODE_URDU = "Urdu";


    public static String getSubtitleTrackName(String languageCode) {
        switch (languageCode) {
            case LANGUAGE_CODE_TE:
                return LANGUAGE_CODE_TELUGU;
            case LANGUAGE_CODE_HI:
                return LANGUAGE_CODE_HINDI;
            case LANGUAGE_CODE_TA:
                return LANGUAGE_CODE_TAMIL;
            case LANGUAGE_CODE_EN:
                return LANGUAGE_CODE_ENGLISH;
            case LANGUAGE_CODE_FR:
                return LANGUAGE_CODE_FRENCH;
            case LANGUAGE_CODE_ML:
                return LANGUAGE_CODE_MALAYALAM;
            case LANGUAGE_CODE_ES:
                return LANGUAGE_CODE_SPANISH;
            case LANGUAGE_CODE_MR:
                return LANGUAGE_CODE_MARATHI;
            case LANGUAGE_CODE_BN:
                return LANGUAGE_CODE_BENGALI;
            case LANGUAGE_CODE_KN:
                return LANGUAGE_CODE_KANNADA;
            case LANGUAGE_CODE_GU:
                return LANGUAGE_CODE_GUJARATI;
                case LANGUAGE_CODE_OD:
                return LANGUAGE_CODE_ODIA;
                case LANGUAGE_CODE_UR:
                return LANGUAGE_CODE_URDU;

            default:
                return languageCode;

        }

    }


}
