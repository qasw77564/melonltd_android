package com.melonltd.naberc.model.service;

import android.util.Base64;

import com.melonltd.naberc.model.constant.NaberConstant;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;


public class Base64Service {
    private static final String TAG = Base64Service.class.getSimpleName();

    public static String encryptBASE64(String key) {
        try {
            if (NaberConstant.IS_DEBUG){
                return key;
            }else {
                return Base64.encodeToString(URLEncoder.encode(key, "utf-8").getBytes("UTF-8"), Base64.DEFAULT);
            }
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Encryption error !", e);
        }
    }

    public static String decryptBASE64(String key) {
        try {
            if (NaberConstant.IS_DEBUG){
                return key;
            }else {
                return URLDecoder.decode(new String(Base64.decode(key, Base64.DEFAULT),"utf-8"), "utf-8");
            }
        } catch (RuntimeException | UnsupportedEncodingException e) {
            throw new RuntimeException("Decryption error or The Cookie was tampered with !", e);
        }
    }


}