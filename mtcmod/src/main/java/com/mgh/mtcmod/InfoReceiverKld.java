package com.mgh.mtcmod;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.mgh.mghlibs.MghService;

/**
 * Only because ModSysUI can't be a BroadcastReceiver itself (due to Xposed)
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
                long time = intent.getLongExtra(MghService.INTENT_EXTRA_SEND_TIME, -1);



                str = str.replace(' ', '&');

                // fill the empty places until the digits with &
                for (int i = str.length(); i < 5; i++){
                    str += "&";
                }

                if (time >= 0) {
                    int posSec = (int) time / 1000;  // [pos] = s
                    int posMin = posSec / 60; // [posMin] = min
                    posSec = posSec % 60;
                    if (posMin > 99)
                        posMin = 99;

                    if (posMin >= 10)
                        str += posMin;
                    else
                        str += "0" + posMin;
                    str += ".";
                    if (posSec >= 10)
                        str += posSec;
                    else
                        str += "0" + posSec;

                    str += "&";
                }else{
                    str += "&&&&&&";
                }

                str += "-000000000";

                //Log.v(TAG, "write KLD: " + str);
                listener.writePortKLD(str);
            }catch (Throwable e){
                Log.e(TAG, "error on intent " + MghService.INTENT_ACTION_SEND_KLD, e);
            }
        }
    }


}
