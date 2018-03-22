package com.mgh.displaylight;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;

import com.mgh.mghlibs.SysProps;
import com.yoctopuce.YoctoAPI.YAPI;
import com.yoctopuce.YoctoAPI.YAPI_Exception;
import com.yoctopuce.YoctoAPI.YLightSensor;
import android.content.SharedPreferences;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by heiss on 06.03.2018.
 */

public class LightMessageHandler extends Handler {

    private final static String TAG = "mgh-messagehandler";

    public final static int MSG_INIT = 0;
    public final static int MSG_READ_VAL = 1;

    private static LightMessageHandler handler;

    private SharedPreferences settings;
    private YLightSensor sensor = null;
    private SysProps props;
    private Map<Double, Integer> pts;
    private Context ctx;


    public static LightMessageHandler GetHandler(Context ctx){

        if (handler == null)
            handler = new LightMessageHandler(ctx);

        return handler;
    }

    private LightMessageHandler(Context ctx){

        this.ctx = ctx;
        settings = PreferenceManager.getDefaultSharedPreferences(ctx);

        props = SysProps.GetSysProps(ctx);

        props.setBrightness(200);

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

    private boolean isEnabled() {
        return settings != null && settings.getBoolean("enabled", false);
    }


    private boolean init() {
        MainActivity.log("LightService init");
        try {
            YAPI.EnableUSBHost(ctx);
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
            this.sendEmptyMessageDelayed(0, 1000);
        }

        return false;
    }

    private void setBrightness(double brightness){

        int newVal = 0;
        for (Map.Entry<Double, Integer> e: pts.entrySet()){
            if (brightness > e.getKey())
                newVal = e.getValue();
        }

        props.setBrightness(newVal);

    }

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);

        if (!isEnabled()) {
            // do nothing but keep checking
            Message newMsg = this.obtainMessage(MSG_INIT);
            this.sendMessageDelayed(newMsg, 10000);
            return;
        }

        switch (msg.what) {
            case MSG_INIT:
                if (init()) {
                    // error
                    Message newMsg = this.obtainMessage(MSG_INIT);
                    this.sendMessageDelayed(newMsg, 10000);
                    break;
                }
                this.removeMessages(MSG_INIT);
                // no break here; go on with read val
            case MSG_READ_VAL: {
                if (sensor != null) {
                    if (sensor.isOnline()) {
                        try {
                            double val = sensor.get_currentValue();
                            MainActivity.log("current value: " + val);
                            MainActivity.updValue(val);
                            Message newMsg = this.obtainMessage(MSG_READ_VAL);
                            setBrightness(val);
                            this.sendMessageDelayed(newMsg, 1000);
                        } catch (Throwable e) {
                            Log.e(TAG, "error on retrieving value: " + e);
                            MainActivity.log("error on retrieving value ");
                            Message newMsg = this.obtainMessage(MSG_INIT);
                            this.sendMessageDelayed(newMsg, 10000);
                        }
                    } else {
                        Log.e(TAG, "sensor not online " + sensor.get_errorMessage());
                        MainActivity.log("sensor not online ");
                        Message newMsg = this.obtainMessage(MSG_INIT);
                        this.sendMessageDelayed(newMsg, 10000);
                    }
                } else {
                    Message newMsg = this.obtainMessage(MSG_INIT);
                    this.sendMessageDelayed(newMsg, 10000);
                }
                break;
            }
        }

    }
}
