package com.mgh.mtcmod;


import android.util.Log;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class ModHideVolumebar extends ModBase implements IXposedHookLoadPackage{


    private final static String TAG = "mgh-modHideVol";

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {

        if (!hideVolumeBar()) return;
        if (!loadPackageParam.packageName.equals(pkgMTCServer)) return;


        Class cls;
        try{
            cls = Class.forName(appMTCServer, false, loadPackageParam.classLoader);
        }catch (Throwable e){
            Log.e(ModHideVolumebar.TAG, "error on find class " + appMTCServer, e);
            return;
        }

        try {
            XposedHelpers.findAndHookMethod(cls, "ShowVolumeDalog", int.class, new XC_MethodReplacement() {

                @Override
                protected Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {

                    // just do nothing
                    //Log.e(ModHideVolumebar.TAG, "call show Volume");
                    return null;
                }

            });
        }catch (Throwable e){
            Log.e(ModHideVolumebar.TAG, "error on hooking ShowVolumeDalog" , e);
            return;
        }
    }


}
