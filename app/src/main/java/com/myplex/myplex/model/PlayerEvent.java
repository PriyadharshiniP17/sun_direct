package com.myplex.myplex.model;

import java.lang.reflect.Field;

/**
 * Created by Srikanth on 09-Oct-17.
 */

public class PlayerEvent {
    public String _id;
    public String title;
    public String partnerId;
    public String partnerName;
    public String action;
    public String mediaUrl;
    public float bitrate;
    public String contentType;
    public String resolution;
    public long secDiff;

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        String newLine = System.getProperty("line.separator");

        result.append( this.getClass().getName() );
        result.append( " PlayerEvent {" );
        result.append(newLine);

        //determine fields declared in this class only (no fields of superclass)
        Field[] fields = this.getClass().getDeclaredFields();

        //print field names paired with their values
        for ( Field field : fields  ) {
            result.append("  ");
            try {
                result.append( field.getName() );
                result.append(": ");
                //requires access to private field:
                result.append( field.get(this) );
            } catch ( IllegalAccessException ex ) {
                System.out.println(ex);
            }
            result.append(newLine);
        }
        result.append("}");

        return result.toString();
    }
}
