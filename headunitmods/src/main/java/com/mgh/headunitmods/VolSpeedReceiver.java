package com.mgh.headunitmods;

import android.app.IntentService;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;

import com.mgh.mghlibs.MghService;
import com.mgh.mghlibs.SysProps;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by heiss on 23.03.2018.
 */

public class VolSpeedReceiver extends BroadcastReceiver {

    private final static String TAG = "mgh-volSpeedReceiver";


    @Override
    public void onReceive(Context context, Intent intent) {
        if (MghService.INTENT_ACTION_UPD_SPEED.equals(intent.getAction())) {

            double speed = intent.getDoubleExtra(MghService.INTENT_EXTRA_SPEED_DBL, Double.NaN);
            double oldSpeed = intent.getDoubleExtra(MghService.INTENT_EXTRA_SPEED_OLD_DBL, Double.NaN);

            speedChanged(speed, oldSpeed, context);
        }

    }

    public static void speedChanged(double speed, double oldSpeed, Context context) {


        SettingsHelper settings = SettingsHelper.getHelper(context);

        if (!settings.GetVolumeAdaptionEnabled()) return; // Speed control is disabled

        int vol = SysProps.getVolume(context);
        Log.v(TAG, "speedChange: new=" + speed + " old=" + oldSpeed + " vol=" + vol);
        if (0 == vol) {
            // Skip volume change on Volume == 0
            Log.d(TAG, "Set volume skipped - volume == 0");
            return;
        }

        List<Integer> speed_steps = settings.getSpeedValues();
        speed_steps.add(0, Integer.MIN_VALUE);
        speed_steps.add(Integer.MAX_VALUE);
        int tol = settings.getSpeedTolerance();
        int volNew = vol;
        int volChange = settings.getSpeedChangeValue();

        if (Double.isNaN(oldSpeed) || Double.isNaN(speed))
            return;

        if (oldSpeed < speed){
            // we got faster
            for (int i=0; i < speed_steps.size();i++) {
                int step = speed_steps.get(i) + tol;
                if (speed > step &&
                        oldSpeed <= step) {
                    volNew += volChange;
                    Log.v(TAG, " -- i:" + i + " s:" + step + "/" + speed_steps.get(i) + " +" +volNew);
                }
            }
        }else if (oldSpeed > speed){
            // we got slower
            for (int i=0; i < speed_steps.size();i++) {
                int step = speed_steps.get(i) - tol;
                if (speed < step &&
                        oldSpeed >= step) {
                    volNew -= volChange;
                    Log.v(TAG, " -- i:" + i + " s:" + step + "/" + speed_steps.get(i) + " -" +volNew);
                }
            }

        }


        if (volNew > 0 && volNew != vol) {
            // Change it!
            Log.v(TAG, "setVol: " + volNew);
            SysProps.setVolume(context, volNew);
        }
    }



}
