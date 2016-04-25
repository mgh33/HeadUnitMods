package com.mgh.mtcmod;

import android.app.Service;
import android.content.IntentFilter;
import android.text.format.Time;
import android.util.Log;

import com.mgh.mghlibs.MghService;

import java.lang.reflect.Method;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;


//todo: mic- test
//todo: KLD

public class ModKld implements IXposedHookZygoteInit, IXposedHookLoadPackage {

    private final static String TAG = "mgh-modKld";

    private String mod_path = null;

    public void initZygote(StartupParam startupParam) throws Throwable {
        mod_path = startupParam.modulePath;
    }



    private final String pkgKld = "android.microntek.canbus.service2";
    private final String appKld = pkgKld + ".CanBusServer2";

    private static Method mWriteKLD;

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {

        if (!loadPackageParam.packageName.equals("android.microntek.canbus"))
            return;

        //region replace messages to KLD display

        Log.i(ModKld.TAG, "this: " + this.toString());

        Class cls;
        try{
            cls = Class.forName(appKld, false, loadPackageParam.classLoader);
        }catch (Throwable e){
            Log.e(ModKld.TAG, "error on find class " + appKld, e);
            return;
        }

        try {
            //Log.v(ModSys.TAG, "try to hook writePort");
            XposedHelpers.findAndHookMethod(cls, "WritePortKLD", String.class, new HookKLD());
        }catch (Throwable e) {
            Log.e(ModKld.TAG, "error on hooking field", e);
        }


        try {
            mWriteKLD = XposedHelpers.findMethodBestMatch(cls, "WritePortKLD", String.class);
        }catch (Throwable e){
            Log.e(TAG, "Error on find WritePortKLD", e);
            return;
        }

        try {
            //Log.v(ModSys.TAG, "try to hook CanbusServer2 onCreate");
            XposedHelpers.findAndHookMethod(cls, "onCreate", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    try {
                        Log.i(ModKld.TAG, "this: " + ModKld.this.toString());
                        ModKld.this.canServer = (Service) param.thisObject;

                        InfoReceiverKld inf = new InfoReceiverKld(ModKld.this);
                        IntentFilter filter = new IntentFilter();
                        filter.addAction(MghService.INTENT_ACTION_SEND_KLD);
                        canServer.registerReceiver(inf, filter);

                    }catch (Throwable e){
                        Log.e("mgh-modSystem", "Error on resolving canserver object", e);
                    }
                }
            });
        }catch (Throwable e) {
            Log.e(ModKld.TAG, "error on hooking onCreate", e);
        }
        //endregion


    }

    private static Service canServer;


    //region class HookKLD
    private class HookKLD extends XC_MethodHook {

        private final static String TAG = "mgh-hookKLD";

        //@Override
        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {

            try {
                if (param.args.length > 0) {
                    //Log.d(HookKLD.TAG, "para=" + param.args[0].toString());
                    if (param.args[0].toString().startsWith("OFF")) {
                        // instead of OFF the time will be displayed
                        Log.d(HookKLD.TAG, "replace OFF");
                        Time t = new Time();
                        t.setToNow();
                        String s = t.format("%T");
                        s += "&&&-000000000";
                        param.args[0] = s;
                    }
                }
            }catch (Throwable e){
                Log.e(HookKLD.TAG, "error on edit KLD msg", e);
            }

        }
    }
    //endregion

    public void writePortKLD(String str){

        if (mWriteKLD == null){
            Log.i(TAG, "method not found");
            return;
        }

        if (canServer == null){
            Log.i(TAG, "instance not found");
            return;
        }
        try {
            mWriteKLD.invoke(canServer, str);
            Log.i(TAG, "sent " + str);
        }catch (Throwable exc) {
            Log.e(TAG, "Error on writeKLD", exc);
        }
    }
}