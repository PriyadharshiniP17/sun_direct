/**
 * 
 */
package com.myplex.myplex.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.myplex.model.CardData;
import com.myplex.model.CardDataHolder;

/**
 * @author Apalya
 * 
 */
public class LocaleFontUtil {
	CardDataHolder dataHolder;
	Context mContext;
	OnClickListener mLocaleListener;
	public final static int NATIVE = 0;
	public final static int NONNATIVE = 1;

	public LocaleFontUtil(Context mContext, CardDataHolder dataHolder) {
		this.dataHolder = dataHolder;
		this.mContext = mContext;
	}

	public OnClickListener getLocaleListener() {
		return mLocaleListener = new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
//				Util.showFeedback(v);
				Integer isNative = null;
				if (v.getTag() instanceof Integer) {
					isNative = (Integer) v.getTag();
				} else {
					return;
				}
				switch (isNative) {
				case NATIVE:
					if (dataHolder.mDataObject.generalInfo != null
							&& dataHolder.mDataObject.generalInfo.title != null) {
						dataHolder.mTitle
								.setText(dataHolder.mDataObject.generalInfo.title);
//						dataHolder.mDelete.setText(mContext.getString(R.string.card_delete));
					}
					if (dataHolder.mDataObject.generalInfo.description != null) {
						dataHolder.mCardDescText
								.setText(dataHolder.mDataObject.generalInfo.description);
					}
					
					v.setTag(NONNATIVE);
					break;
				case NONNATIVE:
					v.setTag(NATIVE);
					if (!setLocalFontIfAvailable(mContext,dataHolder.mDataObject,
							dataHolder.mTitle)) {
						dataHolder.mTitle
								.setText(dataHolder.mDataObject.generalInfo.title);
						v.setTag(NONNATIVE);
					} else {
						dataHolder.mTitle
								.setText(dataHolder.mDataObject.generalInfo.altTitle
										.get(0).title);
					}
					if (!setLocalFontIfAvailable(mContext,dataHolder.mDataObject,
							dataHolder.mCardDescText)) {
						dataHolder.mCardDescText
								.setText(dataHolder.mDataObject.generalInfo.description);
						v.setTag(NONNATIVE);
					} else {
						dataHolder.mCardDescText
								.setText(dataHolder.mDataObject.generalInfo.altDescription
										.get(0).description);
						v.setTag(NATIVE);
					}
					break;
				default:
					break;
				}
			}

		};
	}

	public static void showLanConversionIcon(boolean show,
			CardDataHolder dataHolder) {
		// TODO Auto-generated method stub
		if (!show) {
			dataHolder.mFavLayout.setVisibility(View.GONE);
			return;
		}
		dataHolder.mFavourite.setVisibility(View.GONE);
		dataHolder.mFavProgressBar.setVisibility(View.GONE);
        dataHolder.mFavLayout.setVisibility(View.VISIBLE);
		dataHolder.mLangNoteIcon.setVisibility(View.VISIBLE);

	}
    public static enum Languages {
        te, ta, hi, gu, bn, mr, kn, en, bengali, english, gujarati, hindi, kannada, marathi, tamil, telugu
    }
	public static boolean setLocalFontIfAvailable(Context mContext, CardData data,
			TextView mTextView) {
		if (data != null && data.generalInfo != null && data.generalInfo.altTitle != null
				&& data.generalInfo.altTitle.size() > 0 ) {
			String localLanguage = data.generalInfo.altTitle.get(0).language;
            String localtitle = data.generalInfo.altTitle.get(0).title;
            if(localLanguage == null) return false;
            if(null == localtitle){return false;}
			if (Languages.hindi.name().equalsIgnoreCase(localLanguage)) {
//                mTextView.setTextAppearance(mContext, R.style.TextAppearance_FontHindi);
                mTextView.setTypeface(createFromAsset(mContext.getAssets(), "fonts/hindi.ttf"));
				return true;
			} else if (Languages.telugu.name().equalsIgnoreCase(localLanguage)) {
//                mTextView.setTextAppearance(mContext, R.style.TextAppearance_FontTelugu);
                mTextView.setTypeface(createFromAsset(mContext.getAssets(), "fonts/telugu.ttf"));
//                telugu = createFromAsset(mContext.getAssets(), "fonts/telugu.ttf");
                return true;
            } else if (Languages.gujarati.name().equalsIgnoreCase(localLanguage)) {
                mTextView.setTypeface(createFromAsset(mContext.getAssets(), "fonts/gujarati.ttf"));
//                mTextView.setTextAppearance(mContext, R.style.TextAppearance_FontGujarati);
                return true;
            } else if (Languages.marathi.name().equalsIgnoreCase(localLanguage)) {
//                mTextView.setTextAppearance(mContext, R.style.TextAppearance_FontMarathi);
                mTextView.setTypeface(createFromAsset(mContext.getAssets(), "fonts/marathi.ttf"));
                return true;
            } else if (Languages.kannada.name().equalsIgnoreCase(localLanguage)) {
//                mTextView.setTextAppearance(mContext, R.style.TextAppearance_FontKannada);
                mTextView.setTypeface(createFromAsset(mContext.getAssets(), "fonts/kannada.ttf"));
                return true;
            } else if (Languages.bengali.name().equalsIgnoreCase(localLanguage)) {
//                mTextView.setTextAppearance(mContext, R.style.TextAppearance_FontKannada);
                mTextView.setTypeface(createFromAsset(mContext.getAssets(), "fonts/bengali.ttf"));
                return true;
            } else if (Languages.tamil.name().equalsIgnoreCase(localLanguage)) {
//                mTextView.setTextAppearance(mContext, R.style.TextAppearance_FontTamil);
                mTextView.setTypeface(createFromAsset(mContext.getAssets(), "fonts/tamil.ttf"));
                return true;
            } else {
				return false;
			}
		}
		return false;
	}



    private static Typeface createFromAsset(AssetManager mgr , String fontPath){

        try {
            return  Typeface.createFromAsset(mgr, fontPath);
        }catch(Throwable e){
            e.printStackTrace();
        }

        return Typeface.DEFAULT;
    }

}
