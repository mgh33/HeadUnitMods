package com.mgh.mghlibs;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.provider.Settings.System;




class VolumeObserver extends ContentObserver {

    private final static String TAG = "mgh-volumeObserver";

    private IVolumeUpdateListener listener;
    private int lstVol;
    private int lstVolChangedBroad = 15;
    private int lstBrightness;

    private Context ctx;
    private AudioManager am;
    private SysProps props;

    public interface IVolumeUpdateListener{
        void volChanged();
        void brightChanged();
    }

    private class VolumeTwHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {

            Log.v(TAG, "what: " + msg.what + " arg1:" + msg.arg1 + " arg2:" + msg.arg2);

            switch (msg.what){
                case TWUtil.VOLUME_EVENT:
                    if (msg.arg1 >= 0){
                        lstVol = msg.arg1;
                        lstVolChangedBroad = lstVol;
                    } else {
                        // mute
                        lstVolChangedBroad = 0;
                    }
                    listener.volChanged();
                    break;
            }
        }
    }


    public VolumeObserver(Context ctx, final IVolumeUpdateListener listener) {
        super(new Handler());

        this.listener = listener;
        this.ctx = ctx;
        props = SysProps.GetSysProps(ctx);

        am = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);
        ctx.getContentResolver().registerContentObserver(android.provider.Settings.System.CONTENT_URI, true, this );
        ctx.getContentResolver().registerContentObserver(System.getUriFor("screen_brightness_mode"), false, this);
        ctx.getContentResolver().registerContentObserver(System.getUriFor("screen_brightness"), false, this);
        ctx.getContentResolver().registerContentObserver(System.getUriFor("cfg_backlight="), false, this);

        TWUtil util = new TWUtil();
        if (util.open(new short[]{(short) TWUtil.VOLUME_EVENT}) == 0) {

            util.start();
            util.addHandler("test", new VolumeTwHandler());
        }else{
            util.close();
        }

        BroadcastReceiver receiver = new BroadcastReceiver(){

            @Override
            public void onReceive(Context context, Intent intent) {

                if (intent.getAction().equals("com.microntek.irkeyDown") ||
                    intent.getAction().equals("com.microntek.irkeyUp")){
                    try{
                        if (intent.getIntExtra("keyCode", -1) == 4){
                            // key for Mute is pressed
                            //Log.v(TAG, "mute received");
                            listener.volChanged();
                        }
                    }catch (Throwable e){
                        Log.e(TAG, "error on handling extra of mute", e);
                    }
                } else if (intent.getAction().equals("com.microntek.VOLUME_CHANGED")){
                    // Volume volChanged broadcast
                    //Log.v(TAG, "Volume volChanged broadcast");
                    try{
                        // in case of mute the volume send in this extra will be zero
                        lstVolChangedBroad = intent.getIntExtra("volume", 15);
                    }catch (Throwable e){
                        Log.e(TAG, "error on handling extra of VOLUME_CHANGED", e);
                    }
                    listener.volChanged();
                }
            }
        };

        try {
            IntentFilter filter = new IntentFilter();
            filter.addAction("com.microntek.irkeyDown");
            filter.addAction("com.microntek.irkeyUp");
            filter.addAction("com.microntek.VOLUME_CHANGED");
            ctx.registerReceiver(receiver, filter);
        }catch (Throwable e){
            Log.e(TAG, "error on register Mute receiver", e);
        }

    }


    @Override
    public boolean deliverSelfNotifications() {
        return super.deliverSelfNotifications();
    }

    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);
        //Log.v(TAG, "Settings change detected");

        int currentVolume = props.getVolume();
        int currentBrightness = props.getBrightness();

        if (lstVol != currentVolume) {
            Log.v(TAG, "update listener");
            listener.volChanged();
        }

        if (lstBrightness != currentBrightness) {
            Log.v(TAG, "update listener brightness: " + currentBrightness);
            listener.brightChanged();
        }
        lstVol = currentVolume;
        lstBrightness = currentBrightness;
    }



    public int getVolume(){
        return lstVol;
    }

    public boolean getMute(){
        try{
            //final String KEY = "av_mute=";
            //String mute = am.getParameters(KEY);
            //return mute.equals("true");

            // this is a quite strange behaviour but it can happen that av_mute=false,
            // but the system is silent anyway
            return lstVolChangedBroad == 0;
        }catch (Throwable e){
            Log.e(TAG, "error on read mute", e);
        }
        return false;
    }

}