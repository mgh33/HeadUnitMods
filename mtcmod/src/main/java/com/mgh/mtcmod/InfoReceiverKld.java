package com.mgh.mtcmod;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.mgh.mghlibs.MghService;

/**
 * Only because ModSys can't be a BroadcastReceiver itself (due to Xposed)
 */
public class InfoReceiverKld extends BroadcastReceiver {

    private final static String TAG = "mgh-infoReceiverKld";

    private ModKld listener;

    public InfoReceiverKld(ModKld listener) {
        this.listener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals(MghService.INTENT_ACTION_SEND_KLD)){
            try {
                String str = intent.getStringExtra(MghService.INTENT_EXTRA_SEND_KLD);
                listener.writePortKLD(str);
            }catch (Throwable e){
                Log.e(TAG, "error on intent " + MghService.INTENT_ACTION_SEND_KLD, e);
            }
        }
    }


}
