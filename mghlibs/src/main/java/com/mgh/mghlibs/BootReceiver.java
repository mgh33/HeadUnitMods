package com.mgh.mghlibs;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {

    private final static String TAG = "mgh-bootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {

        //Log.d(TAG, "onReceive");
        //if (intent.getAction() == null){
        //    Log.e(TAG, "boot onreceive action is null");
        //    return;
        //}
        try {
            //Log.v(TAG, "boot onreceive Boot complete");
            Intent intent1 = new Intent(context, MghService.class);
            context.startService(intent1);
            //Log.v(TAG, "started MghService");
        }catch (Throwable e) {
            Log.e(TAG, "error onreceive", e);
        }

    }


    final String app = "android.microntek.canbus.service2.CanBusServer2";
    Class<?> serverClass;

    private void doWork(){

        Log.d(TAG, "try find class");
        try {
            serverClass = Class.forName(app);
        }catch (ClassNotFoundException exc){
            Log.e(TAG,"class not found", exc);
            return;
        }

        Log.d(TAG, "try to hook writePort");

        Log.d(TAG, "CanBusServer2 WritePortKLD hook OK");
    }
}
