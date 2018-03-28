package com.mgh.headunitmods;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import static com.mgh.headunitmods.LightMessageHandler.MSG_INIT;


public class LightService extends BroadcastReceiver {

    private final static String TAG = "mgh-lightSrv";

    public LightService() {
        Log.v(TAG, "LightService started");
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED") ||
                intent.getAction().equals("android.intent.action.QUICKBOOT_POWERON") ||
                intent.getAction().equals("android.intent.action.USB_DEVICE_ATTACHED") ) {

            Handler myHandler = LightMessageHandler.GetHandler(context);

            Log.v(TAG, "LightService start command");

            Message newMsg = myHandler.obtainMessage(MSG_INIT);
            myHandler.sendMessage(newMsg);
        }
    }



}
