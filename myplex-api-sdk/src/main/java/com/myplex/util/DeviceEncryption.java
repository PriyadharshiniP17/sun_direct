package com.myplex.util;

import android.os.Build;

import com.myplex.api.APIConstants;

import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class DeviceEncryption {
    private final static String HEX = "0123456789ABCDEF";
    private static String KEY_ALGORITHM = "AES";
    private static String SECURE_RANDOM_ALGORITHM = "SHA1PRNG";
    private static String PROVIDER = "Crypto";

    public static String encrypt(String seed, String cleartext) throws Exception {
        if (Build.VERSION.SDK_INT >= 24) {
            SecretKey rawKey = getRawKey();
            byte[] result = encrypt(rawKey, cleartext.getBytes());
            return toHex(result);
        } else {
            byte[] rawKey = getRawKey(seed.getBytes());
            byte[] result = encrypt(rawKey, cleartext.getBytes());
            return toHex(result);
        }
    }

    public static String decrypt(String seed, String encrypted) throws Exception {
        if (Build.VERSION.SDK_INT >= 28 ||
        PrefUtils.getInstance().getBoolean(APIConstants.IS_PORTING_TO_SECURE_RANDOM_DONE,false)) {
            SecretKey rawKey = getRawKey();
            byte[] enc = toByte(encrypted);
            byte[] result = decrypt(rawKey, enc);
            return new String(result);
        } else {
            byte[] rawKey = getRawKey(seed.getBytes());
            byte[] enc = toByte(encrypted);
            byte[] result = decrypt(rawKey, enc);
            return new String(result);
        }
    }

    private static SecretKey key;

    private static SecretKey getRawKey() throws Exception {
        if (key == null)
            key = PrefUtils.getInstance().getPrefSecretKey();

        if (key == null) {
            final int outputKeyLength = 256;
            SecureRandom secureRandom = new SecureRandom();
            // Do not seed secureRandom! Automatically seeded from system entropy.
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(outputKeyLength, secureRandom);
            key = keyGenerator.generateKey();
            PrefUtils.getInstance().setPrefSecretKey(key);
        }
        return key;
    }

    private static byte[] getRawKey(byte[] seed) throws Exception {
        KeyGenerator kgen = KeyGenerator.getInstance(KEY_ALGORITHM);
        SecureRandom sr = SecureRandom.getInstance(SECURE_RANDOM_ALGORITHM,new CryptoProvider());
        sr.setSeed(seed);
        kgen.init(128, sr); // 192 and 256 bits may not be available
        SecretKey skey = kgen.generateKey();
        byte[] raw = skey.getEncoded();
        return raw;
    }

    private static byte[] encrypt(byte[] raw, byte[] clear) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(raw, KEY_ALGORITHM);
        Cipher cipher = Cipher.getInstance(KEY_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
        byte[] encrypted = cipher.doFinal(clear);
        return encrypted;
    }

    private static byte[] encrypt(SecretKey raw, byte[] clear) throws Exception {

        Cipher cipher = Cipher.getInstance(KEY_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, raw);
        byte[] encrypted = cipher.doFinal(clear);
        return encrypted;
    }

    private static byte[] decrypt(SecretKey raw, byte[] encrypted) throws Exception {
        Cipher cipher = Cipher.getInstance(KEY_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, raw);
        byte[] decrypted = cipher.doFinal(encrypted);
        return decrypted;
    }

    private static byte[] decrypt(byte[] raw, byte[] encrypted) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(raw, KEY_ALGORITHM);
        Cipher cipher = Cipher.getInstance(KEY_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, skeySpec);
        byte[] decrypted = cipher.doFinal(encrypted);
        return decrypted;
    }

    public static String toHex(String txt) {
        return toHex(txt.getBytes());
    }

    public static String fromHex(String hex) {
        return new String(toByte(hex));
    }

    public static byte[] toByte(String hexString) {
        int len = hexString.length() / 2;
        byte[] result = new byte[len];
        for (int i = 0; i < len; i++)
            result[i] = Integer.valueOf(hexString.substring(2 * i, 2 * i + 2), 16).byteValue();
        return result;
    }

    public static String toHex(byte[] buf) {
        if (buf == null)
            return "";
        StringBuffer result = new StringBuffer(2 * buf.length);
        for (int i = 0; i < buf.length; i++) {
            appendHex(result, buf[i]);
        }
        return result.toString();
    }

    private static void appendHex(StringBuffer sb, byte b) {
        sb.append(HEX.charAt((b >> 4) & 0x0f)).append(HEX.charAt(b & 0x0f));
    }


}