package com.mgh.mghlibs;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.os.Handler;
import android.util.Log;
import android.provider.Settings.System;



class VolumeObserver extends ContentObserver {

    private final static String TAG = "mgh-volumeObserver";

    private IVolumeUpdateListener listener;
    private int lstVol;
    private int lstVolChangedBroad = 15;
    private Context ctx;
    private AudioManager am;

    public interface IVolumeUpdateListener{
        void changed();
    }

    public VolumeObserver(Context ctx, final IVolumeUpdateListener listener) {
        super(new Handler());

        this.listener = listener;
        this.ctx = ctx;

        am = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);
        ctx.getContentResolver().registerContentObserver(android.provider.Settings.System.CONTENT_URI, true, this );


        BroadcastReceiver receiver = new BroadcastReceiver(){

            @Override
            public void onReceive(Context context, Intent intent) {

                if (intent.getAction().equals("com.microntek.irkeyDown") ||
                    intent.getAction().equals("com.microntek.irkeyUp")){
                    try{
                        if (intent.getIntExtra("keyCode", -1) == 4){
                            // key for Mute is pressed
                            //Log.v(TAG, "mute received");
                            listener.changed();
                        }
                    }catch (Throwable e){
                        Log.e(TAG, "error on handling extra of mute", e);
                    }
                } else if (intent.getAction().equals("com.microntek.VOLUME_CHANGED")){
                    // Volume changed broadcast
                    //Log.v(TAG, "Volume changed broadcast");
                    try{
                        // in case of mute the volume send in this extra will be zero
                        lstVolChangedBroad = intent.getIntExtra("volume", 15);
                    }catch (Throwable e){
                        Log.e(TAG, "error on handling extra of VOLUME_CHANGED", e);
                    }
                    listener.changed();
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

        int currentVolume = getVolume();

        if (lstVol != currentVolume) {
            //Log.v(TAG, "update listener");
            listener.changed();
        }

        lstVol = currentVolume;
    }

    public int getVolume(){
        try{
            final String KEY = "av_volume=";
            return System.getInt(ctx.getContentResolver(), KEY, 10);
        }catch (Throwable e){
            Log.e(TAG, "error on read volume", e);
        }

        return 0;
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