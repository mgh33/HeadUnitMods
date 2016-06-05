package com.mgh.mtcmod;

import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XSharedPreferences;


public abstract class ModBase implements IXposedHookZygoteInit {

    protected String mod_path = null;

    protected final String pkgSysUI = "com.android.systemui";
    protected final String appSysUI = pkgSysUI + ".SystemUIService";

    protected final String pkgMTCServer = "android.microntek.service";
    protected final String appMTCServer = pkgMTCServer + ".MicrontekServer";


    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
        mod_path = startupParam.modulePath;

        settings = new XSharedPreferences(BuildConfig.APPLICATION_ID, "com.mgh.mtcmod_prefs");
    }



    private static XSharedPreferences settings;


    public boolean showSpeed(){
        //return true;
        return settings.getBoolean("show.speed", false);
    }

    public boolean showVolume(){
        //return true;
        return settings.getBoolean("show.volume", false);
    }


    public boolean hideVolumeBar(){
        //return true;
        return settings.getBoolean("hide.volumebar", false);
    }

    public boolean activateVlc(){
        //return true;
        return settings.getBoolean("vlcmod.active", false);
    }

    public boolean enableBrightnessMod(){
        //return true;
        return settings.getBoolean("brightness.active", false);
    }

}
