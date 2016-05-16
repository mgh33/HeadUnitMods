package com.mgh.mtcmod;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.mgh.mghlibs.MghService;

/**
 * Only because ModSysUI can't be a BroadcastReceiver itself (due to Xposed)
 */
public class InfoReceiver extends BroadcastReceiver {

    private final static String TAG = "mgh-infoReceiver";

    private ModSysUI listener;

    public InfoReceiver(ModSysUI listener) {
        this.listener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals(MghService.INTENT_ACTION_UPD_SPEED)){
            // new speed
            try {
                int val = intent.getIntExtra(MghService.INTENT_EXTRA_SPEED, 0);
                listener.updateSpeed(val);
            }catch (Throwable e){
                Log.e(TAG, "error on update speed", e);
            }
        }else if (intent.getAction().equals(MghService.INTENT_ACTION_UPD_VOLUME)){
            // new volume
            try {
                int val = intent.getIntExtra(MghService.INTENT_EXTRA_VOLUME, 0);
                boolean mute = intent.getBooleanExtra(MghService.INTENT_EXTRA_MUTE, false);
                listener.updateVol(val, mute);
            }catch (Throwable e){
                Log.e(TAG, "error on update volume", e);
            }
        }
    }


}
