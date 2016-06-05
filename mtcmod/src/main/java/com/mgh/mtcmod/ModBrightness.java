package com.mgh.mtcmod;

import android.content.Context;
import android.content.IntentFilter;
import android.util.Log;

import com.mgh.mghlibs.MghService;

import java.lang.reflect.Method;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;


public class ModBrightness  extends ModBase implements IXposedHookLoadPackage {

    static private final String TAG = "mgh-bright";

    private final String app = "in.jmkl.dcsms.statusbargreper.SlideBrightness";

    private Object instance = null;

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {

        if (!loadPackageParam.packageName.equals(pkgSysUI))
            return;

        if (!enableBrightnessMod()) {
            Log.i(ModBrightness.TAG, "!!!!disabled");
            return;
        }

        //region retrieve method "setProgress"


        Class cls;
        try{
            cls = Class.forName(app, false, loadPackageParam.classLoader);
        }catch (Throwable e){
            Log.e(ModBrightness.TAG, "error on find class " + app, e);
            return;
        }


        try{
            XposedHelpers.findAndHookMethod(cls, "init", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    // only to retrieve the instance
                    instance = param.thisObject;

                    try{
                        InfoReceiverBrightness inf = new InfoReceiverBrightness(ModBrightness.this);
                        IntentFilter filter = new IntentFilter();
                        filter.addAction(MghService.INTENT_ACTION_UPD_BRIGHTNESS);
                        Context ctx = (Context) XposedHelpers.getObjectField(instance, "mContext");
                        ctx.registerReceiver(inf, filter);
                    }catch (Throwable e){
                        Log.e(ModBrightness.TAG, "error on registering receiver", e);
                        return;
                    }

                }
            });
        }catch (Throwable e){
            Log.e(ModBrightness.TAG, "error on hook method 'init'", e);
            return;
        }

        //endregion


    }

    public void updateBrightness(int brightness){

        if (instance != null) {
            try {
                Log.v(TAG, "setProgress");
                XposedHelpers.callMethod(instance, "setProgress", brightness);
            }catch (Throwable e){
                Log.e(ModBrightness.TAG, "error on calling 'setProgress'", e);
                return;
            }
        }
    }
}
