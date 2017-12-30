package com.mgh.displaylight;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.ListView;

import com.mgh.mghlibs.SysProps;
import com.yoctopuce.YoctoAPI.YAPI;
import com.yoctopuce.YoctoAPI.YAPI_Exception;
import com.yoctopuce.YoctoAPI.YLed;
import com.yoctopuce.YoctoAPI.YLightSensor;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class LightService extends Service {

    private final static String TAG = "mgh-lightSrv";

    private final static int MSG_INIT = 0;
    private final static int MSG_READ_VAL = 1;

    private SysProps props;
    private Map<Double, Integer> pts;

    private SharedPreferences settings;

    private boolean isEnabled(){
        if (settings == null)
            return false;
        return settings.getBoolean("enabled", false);
    }
    public LightService() {

        Log.v(TAG, "LightService started");

        props = new SysProps(this);
        pts = new LinkedHashMap<>();
        pts.put(1.0,0);
        pts.put(5.0,25);               // 10%
        pts.put(10.0,51);               // 20%
        pts.put(20.0,77);               // 30%
        pts.put(50.0,102);             // 40%
        pts.put(100.0,127);             // 50%
        pts.put(200.0,153);            // 60%
        pts.put(500.0,179);            // 70%
        pts.put(1000.0,204);            // 80%
        pts.put(2000.0,230);           // 90%
        pts.put(5000.0,255);           // 100%
    }

    private Handler myHandler = new Handler(){

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            if (! isEnabled()) {
                // do nothing but keep checking
                Message newMsg = myHandler.obtainMessage(MSG_INIT);
                myHandler.sendMessageDelayed(newMsg, 10000);
                return;
            }

            switch (msg.what){
                case MSG_INIT:
                    if (init()) {
                        // error
                        Message newMsg = myHandler.obtainMessage(MSG_INIT);
                        myHandler.sendMessageDelayed(newMsg, 10000);
                        break;
                    }
                    myHandler.removeMessages(MSG_INIT);
                    // no break here; go on with read val
                case MSG_READ_VAL:{
                    if (sensor != null) {
                        if (sensor.isOnline()) {
                            try {
                                double val =  sensor.get_currentValue();
                                MainActivity.log("current value: " + val);
                                MainActivity.updValue(val);
                                Message newMsg = myHandler.obtainMessage(MSG_READ_VAL);
                                setBrightness(val);
                                myHandler.sendMessageDelayed(newMsg, 1000);
                            }catch (Throwable e){
                                Log.e(TAG, "error on retrieving value: " + e);
                                MainActivity.log("error on retrieving value ");
                                Message newMsg = myHandler.obtainMessage(MSG_INIT);
                                myHandler.sendMessageDelayed(newMsg, 10000);
                            }
                        }else{
                            Log.e(TAG, "sensor not online " + sensor.get_errorMessage());
                            MainActivity.log("sensor not online ");
                            Message newMsg = myHandler.obtainMessage(MSG_INIT);
                            myHandler.sendMessageDelayed(newMsg, 10000);
                        }
                    }else{
                        Message newMsg = myHandler.obtainMessage(MSG_INIT);
                        myHandler.sendMessageDelayed(newMsg, 10000);
                    }
                    break;
                }
            }

        }
    };


    YLightSensor sensor = null;


    private void setBrightness(double brightness){

        int newVal = 0;
        for (Map.Entry<Double, Integer> e: pts.entrySet()){
            if (brightness > e.getKey())
                newVal = e.getValue();
        }

        props.setBrightness(newVal);

    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int ret = super.onStartCommand(intent, flags, startId);

        settings = PreferenceManager.getDefaultSharedPreferences(this);


        Log.v(TAG, "LightService start command");

        Message newMsg = myHandler.obtainMessage(MSG_INIT);
        myHandler.sendMessage(newMsg);

        return ret;
    }

    private boolean init() {
        MainActivity.log("LightService init");
        try {
            YAPI.EnableUSBHost(this);
        } catch (YAPI_Exception e) {
            Log.e(TAG, "error on init yapi",e);
            MainActivity.log("error on init yapi");
            return true;
        }

        try {
            YAPI.RegisterHub("usb");
        } catch (YAPI_Exception e) {
            Log.e(TAG, "error on init yapi",e);
            MainActivity.log("error on init yapi2");
            return true;
        }

        try{
            YAPI.UpdateDeviceList();
        } catch (YAPI_Exception e) {
            Log.e(TAG, "error on update devicelist",e);
            MainActivity.log("error on update devicelist");
            return true;
        }

        sensor = YLightSensor.FirstLightSensor();
        if (sensor != null){
            Log.d(TAG, "sensor found");
            MainActivity.log("Sensor found");
            myHandler.sendEmptyMessageDelayed(0, 1000);
        }

        return false;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
