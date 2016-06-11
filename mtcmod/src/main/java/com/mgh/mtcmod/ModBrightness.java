package com.mgh.mtcmod;

import android.content.Context;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.util.Log;
import android.widget.ImageView;

import com.mgh.mghlibs.MghService;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;


public class ModBrightness  extends ModBase implements IXposedHookLoadPackage {

    static private final String TAG = "mgh-bright";

    private final String appBigSlider = "in.jmkl.dcsms.statusbargreper.SlideBrightness";
    private final String appSysSlider = "com.android.systemui.settings.BrightnessController";

    private Object instance = null;
    private Object instBrightCntrl = null;


    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {

        if (!loadPackageParam.packageName.equals(pkgSysUI))
            return;

        if (!enableBrightnessMod()) {
            //Log.i(ModBrightness.TAG, "!!!!disabled");
            return;
        }

        //region retrieve method "setProgress"


        Class clsBigSlider;
        try{
            clsBigSlider = Class.forName(appBigSlider, false, loadPackageParam.classLoader);
        }catch (Throwable e){
            Log.e(ModBrightness.TAG, "error on find class " + appBigSlider, e);
            return;
        }

        Class clsSysSlider;
        try{
            clsSysSlider = Class.forName(appSysSlider, false, loadPackageParam.classLoader);
        }catch (Throwable e){
            Log.e(ModBrightness.TAG, "error on find class " + appSysSlider, e);
            return;
        }


        try{
            XposedHelpers.findAndHookMethod(clsBigSlider, "init", new XC_MethodHook() {
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

        try{

            Class tglSlider = Class.forName("com.android.systemui.settings.ToggleSlider", false, loadPackageParam.classLoader);
            XposedHelpers.findAndHookConstructor(clsSysSlider, Context.class, ImageView.class, tglSlider, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    // only to retrieve the instance
                    Log.v(ModBrightness.TAG, "instance for brightness control");

                    instBrightCntrl = param.thisObject;

                    if (lstBrightness >= 0)
                        updSysSlider(lstBrightness);

                }
            });
        }catch (Throwable e){
            Log.e(ModBrightness.TAG, "error on hook constructor ", e);
            return;
        }

        //endregion


    }

    private int lstBrightness = -1;
    public void updateBrightness(int brightness){

        lstBrightness = brightness;
        if (instance != null) {
            try {
                Log.v(TAG, "setProgress");
                XposedHelpers.callMethod(instance, "setProgress", brightness);
            }catch (Throwable e){
                Log.e(ModBrightness.TAG, "error on calling 'setProgress'", e);
                return;
            }
            if (instBrightCntrl != null) {
                updSysSlider(brightness);
            }
        }
    }

    private void updSysSlider(int brightness) {
        try {
            Log.v(TAG, "update Slider");

            AudioManager am = (AudioManager) XposedHelpers.getObjectField(instBrightCntrl, "am");
            am.setParameters("cfg_backlight=" + brightness);
            XposedHelpers.callMethod(instBrightCntrl, "updateSlider");
        } catch (Throwable e) {
            Log.e(ModBrightness.TAG, "error on calling 'updateSlider'", e);
            return;
        }
    }
}
