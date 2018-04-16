package com.mgh.headunitmods;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by heiss on 26.03.2018.
 */

class SettingsHelper {

    private final static String TAG = "mgh-settingshelper";

    private static SettingsHelper helper;

    public static SettingsHelper getHelper(Context ctx){
        if (helper == null)
            helper = new SettingsHelper(ctx);
        return helper;
    }

    private SharedPreferences settings;
    private SettingsHelper(Context ctx){
        settings = PreferenceManager.getDefaultSharedPreferences(ctx);
    }

    public boolean BrightnessAdaptionEnabled() {
        return settings != null && settings.getBoolean("brightness.enable", false);
    }

    public boolean GetVolumeAdaptionEnabled() {
        return settings != null && settings.getBoolean("speed.enable", false);
    }

    public String VolumeSpeedRange() {
        if (settings == null) return "";
        return settings.getString("speed.speedrange", "");
    }

    public void SetVolumeSpeedRange(String value) {
        if (settings == null) return ;

        SharedPreferences.Editor editor = settings.edit();
        editor.putString("speed.speedrange", value);
        editor.apply();
    }

    private String _oldSpeed ="";
    private ArrayList<Integer> speedValues = new ArrayList<>();

    public List<Integer> getSpeedValues() {
        // Load speed values
        String newSpeed = VolumeSpeedRange();
        if (!_oldSpeed.equals(newSpeed) || speedValues == null || speedValues.size() <= 0) {
            // Try to calc it

            List<String> speedValsStr = Arrays.asList(newSpeed.split("\\s*,\\s*"));
            StringBuilder speedValsClr = new StringBuilder();
            for (String spdStep : speedValsStr) {
                Integer s;
                try {
                    s = Integer.valueOf(spdStep);
                } catch (Exception e) {
                    s = -1;
                }
                if (s > 0 && s < 500) {
                    if (speedValues.size() > 0)
                        speedValsClr.append(", ");
                    speedValsClr.append(s.toString());
                    speedValues.add(s);
                }
            }

            if (!newSpeed.equals(speedValsClr.toString())) {
                newSpeed = speedValsClr.toString();
                SetVolumeSpeedRange(newSpeed);
            }
        }
        _oldSpeed = newSpeed;

        return new ArrayList<>(speedValues);
    }

    public int getSpeedChangeValue() {
        if (settings == null) return 1;
        String str =  settings.getString("speed.speedvol", "1");
        try{
            return Integer.parseInt(str);
        } catch(NumberFormatException nfe) {
            Log.e(TAG,"error reading speed.speedvol", nfe);
            return 1;
        }
    }

    public int getSpeedTolerance() {
        if (settings == null) return 3;
        String str =  settings.getString("speed.tol", "3");
        try{
            return Integer.parseInt(str);
        } catch(NumberFormatException nfe) {
            Log.e(TAG,"error reading speed.tol", nfe);
            return 3;
        }
    }


}
