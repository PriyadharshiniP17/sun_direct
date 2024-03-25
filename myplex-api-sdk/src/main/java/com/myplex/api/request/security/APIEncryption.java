package com.myplex.api.request.security;

import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import static com.myplex.api.myplexAPI.TAG;

/**
 * Created by apalya on 8/4/2016.
 */
public class APIEncryption {

    public static final String PART4="tifP1ioA";
    /*public static final String PART4="tifPNz0C";*/

    public static String encrypt(String keyValue, String data) throws Exception {
        Key key = generateKey(keyValue.getBytes());
        Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
        IvParameterSpec ivspec = new IvParameterSpec(new byte[16]);
        c.init(Cipher.ENCRYPT_MODE, key,ivspec);
        byte[] plainTextBytes = data.getBytes();
        byte[] decValue = c.doFinal(plainTextBytes);
        String encryptedValue = new String(Base64.encodeToString(decValue, Base64.NO_WRAP));
        //Log.d(TAG,"Excryption: TESTIV.encryptedData- " + encryptedValue);
        return encryptedValue;
    }

    public static String decrypt(String keyValue, String encryptedData) throws Exception {
        Key key = generateKey(keyValue.getBytes());
        Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
        IvParameterSpec ivspec = new IvParameterSpec(new byte[16]);
        c.init(Cipher.DECRYPT_MODE, key,ivspec);
        byte[] decodedValue = Base64.decode(encryptedData.getBytes(),Base64.NO_WRAP);
        byte[] decValue = c.doFinal(decodedValue);
        String decryptedValue = new String(decValue);
        return decryptedValue;
    }

    private static Key generateKey(byte[] keyValue) throws Exception {
        Key key = new SecretKeySpec(keyValue, "AES");
        return key;
    }

    public static String encryptBase64(String data, String seed) throws Exception {
        //Log.d(TAG," Excryption: encryptBase64:input data- " + data + " seed- " + seed);
        if (TextUtils.isEmpty(data)) {
            return null;
        }
        String encryptedData = encrypt(seed,data);
        //Log.d(TAG,"Excryption: encryptedData - " + encryptedData);
        return encryptedData;
    }

    public static String decryptBase64(String data, String seed) throws Exception {
        //Log.d(TAG, "Excryption: decryptBase64: input data- " + data + " seed- " + seed);
        if (TextUtils.isEmpty(data)) {
            return null;
        }
        String decryptedData = decrypt(seed, data);
        //Log.d(TAG, "Excryption: decryptedData- " + decryptedData);
        return decryptedData;
    }

}