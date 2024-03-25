package com.myplex.myplex.download;

import android.content.Context;

import com.myplex.model.CardData;

import java.lang.reflect.Field;

/**
 * Created by Srikanth on 08-Sep-17.
 */
public class DownloadDataItem {
    public Context mContext;
    public CardData cardData;
    public String seasonName;
    public CardData tvShowData;
    public String aUrl;
    public String mVideoUrl;
    public String mAudioUrl;
    public String fileName;
    public String videoFileName;
    public String audioFileName;
    public String varientType;

    public DownloadDataItem() {

    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        String newLine = System.getProperty("line.separator");

        result.append(this.getClass().getName());
        result.append(" {");
        result.append(newLine);
        newLine = ",";
        //determine fields declared in this class only (no fields of superclass)
        Field[] fields = this.getClass().getDeclaredFields();

        //print field names paired with their values
        for (Field field : fields) {
            result.append("  ");
            try {
                result.append(field.getName());
                result.append(": ");
                //requires access to private field:
                result.append(field.get(this));
            } catch (IllegalAccessException ex) {
                System.out.println(ex);
            }
            result.append(newLine);
        }
        result.append("}");

        return result.toString();

    }
}
