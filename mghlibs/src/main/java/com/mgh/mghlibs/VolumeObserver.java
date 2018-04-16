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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;


class VolumeObserver extends ContentObserver {

    private final static String TAG = "mgh-volumeObserver";

    private List<IVolumeUpdateListener> listener = new ArrayList<>();
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

    private static class VolumeTwHandler extends Handler{
        private WeakReference<VolumeObserver> thisRef;

        public  VolumeTwHandler(VolumeObserver observer){
            super();
            thisRef = new WeakReference<>(observer);
        }

        @Override
        public void handleMessage(Message msg) {

            Log.v(TAG, "what: " + msg.what + " arg1:" + msg.arg1 + " arg2:" + msg.arg2);

            switch (msg.what){
                case TWUtil.VOLUME_EVENT:
                    if (msg.arg1 >= 0){
                        thisRef.get().lstVol = msg.arg1;
                        thisRef.get().lstVolChangedBroad = thisRef.get().lstVol;
                    } else {
                        // mute
                        thisRef.get().lstVolChangedBroad = 0;
                    }
                    for (IVolumeUpdateListener l: thisRef.get().listener) {
                        l.volChanged();
                    }

                    break;
            }
        }
    }

    //region Singleton
    private static VolumeObserver volObserver = null;
    public static VolumeObserver getVolumeObserver(Context ctx){
        if (volObserver == null)
            volObserver = new VolumeObserver(ctx);
        return volObserver;
    }
    //end region


    public void addListener(final IVolumeUpdateListener listener){
        this.listener.add(listener);
    }

    private VolumeObserver(Context ctx) {
        super(new Handler());


        this.ctx = ctx;
        props = SysProps.GetSysProps(ctx);

        am = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);
        ctx.getContentResolver().registerContentObserver(android.provider.Settings.System.CONTENT_URI, true, this );
        ctx.getContentResolver().registerContentObserver(System.getUriFor("screen_brightness_mode"), false, this);
        ctx.getContentResolver().registerContentObserver(System.getUriFor("screen_brightness"), false, this);
        ctx.getContentResolver().registerContentObserver(System.getUriFor("cfg_backlight="), false, this);

        TWUtil util = TWUtil.getInstance();
        if (util.open(new short[]{(short) TWUtil.VOLUME_EVENT}) == 0) {

            util.start();
            util.addHandler("test", new VolumeTwHandler(this));
            util.write(TWUtil.VOLUME_EVENT, 255);
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
                            fireVolChanged();
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
                    fireVolChanged();
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

    private void fireVolChanged(){
        Log.v(TAG, "update listener");
        for (IVolumeUpdateListener l: listener) {
            l.volChanged();
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
            fireVolChanged();
        }

        if (lstBrightness != currentBrightness) {
            Log.v(TAG, "update listener brightness: " + currentBrightness);
            for (IVolumeUpdateListener l: listener) {
                l.brightChanged();
            }

        }
        lstVol = currentVolume;
        lstBrightness = currentBrightness;
    }



    public int getVolume(){

        return lstVol;
    }

    boolean getMute(){
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