package com.mgh.mghlibs;


import android.content.Context;
import android.util.Log;
import android.provider.Settings.System;

public class SysProps {

    private final static String TAG = "mgh-volumeObserver";

    private Context ctx;


    private static SysProps props;

    public static SysProps GetSysProps(Context ctx){

        if (props==null)
            props = new SysProps(ctx);

        return props;
    }

    private SysProps(Context ctx){
        this.ctx = ctx;
    }


    public int getVolume(){

        return getVolume(ctx);
    }

    public static int getVolume(Context ctx){
        try{
            final String KEY = "av_volume=";
            return System.getInt(ctx.getContentResolver(), KEY, 10);
        }catch (Throwable e){
            Log.e(TAG, "error on read volume", e);
        }

        return 0;
    }
    public int getBrightness(){

        return getBrightness(ctx);
    }

    private static final String KEY_BRIGHTNESS = "screen_brightness";
    public static int getBrightness(Context ctx){
        try{
            return System.getInt(ctx.getContentResolver(), KEY_BRIGHTNESS);
        }catch (Throwable e){
            Log.e(TAG, "error on read brightness", e);
        }

        return 0;
    }
    public void setBrightness(int value){
        if (value < 26)
            value = 26;
        if (value > 255)
            value = 255;
        try{
            Log.v(TAG, "set brightness: " + value);
            System.putInt(ctx.getContentResolver(), KEY_BRIGHTNESS, value);
        }catch (Throwable e){
            Log.e(TAG, "error on setting brightness", e);
        }

    }
}