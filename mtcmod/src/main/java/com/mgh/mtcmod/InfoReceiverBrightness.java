package com.mgh.mtcmod;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.mgh.mghlibs.MghService;

public class InfoReceiverBrightness extends BroadcastReceiver {

    private final static String TAG = "mgh-infoReceiverBright";

    private ModBrightness listener;

    public InfoReceiverBrightness(ModBrightness listener) {
        this.listener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals(MghService.INTENT_ACTION_UPD_BRIGHTNESS)){
            // new brightness
            try {
                int val = intent.getIntExtra(MghService.INTENT_EXTRA_BRIGHTNESS, 0);
                listener.updateBrightness(val);
            }catch (Throwable e){
                Log.e(TAG, "error on update speed", e);
            }
        }
    }

}
