package com.mgh.mtcmod;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Settings {

    private static Settings instance = new Settings();
    private static SharedPreferences settings;

    private Settings(){

        settings = PreferenceManager.getDefaultSharedPreferences(App.getAppContext());
    }

    public static Settings getInstance(){
        return instance;
    }

    public boolean showSpeed(){
        return settings.getBoolean("show.speed", false);
    }

    public boolean showVolume(){
        return settings.getBoolean("show.volume", false);
    }


    public boolean hideVolumeBar(){
        return settings.getBoolean("hide.volumebar", false);
    }

    public boolean activateVlc(){
        return settings.getBoolean("vlcmod.active", false);
    }

    public boolean enableBrightnessMod(){
        return settings.getBoolean("brightness.active", false);
    }


}
