package com.mgh.displaylight;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import static com.mgh.displaylight.LightMessageHandler.MSG_INIT;

public class LightService extends Service {

    private final static String TAG = "mgh-lightSrv";

    private Handler myHandler;


    public LightService() {
        Log.v(TAG, "LightService started");
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int ret = super.onStartCommand(intent, flags, startId);

        myHandler = LightMessageHandler.GetHandler(this);

        Log.v(TAG, "LightService start command");

        Message newMsg = myHandler.obtainMessage(MSG_INIT);
        myHandler.sendMessage(newMsg);

        return ret;
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
