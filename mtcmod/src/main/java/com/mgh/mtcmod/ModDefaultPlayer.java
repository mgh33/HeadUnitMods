package com.mgh.mtcmod;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.UserHandle;
import android.util.Log;


import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;


public class ModDefaultPlayer implements IXposedHookZygoteInit, IXposedHookLoadPackage {

    private final static String TAG = "mgh-modDefaultPlayer";

    private String mod_path = null;



    public void initZygote(StartupParam startupParam) throws Throwable {
        mod_path = startupParam.modulePath;

    }


    private final String pkgMTCServer = "android.microntek.service";
    private final String appMTCServer = pkgMTCServer + ".MicrontekServer";


    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {

        if (loadPackageParam.packageName.equals(pkgMTCServer)){

            final Class cls;
            try{
                cls = Class.forName(appMTCServer, false, loadPackageParam.classLoader);
            }catch (Throwable e){
                Log.e(ModDefaultPlayer.TAG, "error on find class " + appMTCServer, e);
                return;
            }

            try {
                Log.v(ModDefaultPlayer.TAG, "try to hook startMusic");



                XposedHelpers.findAndHookMethod(cls, "startMusic", String.class, int.class , new XC_MethodReplacement() {


                    @Override
                    protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {

                        Log.v(ModDefaultPlayer.TAG, "startMusic");

                        String str;
                        int i;
                        boolean gps_isfront;
                        Service service;

                        try {
                            service = (Service) param.thisObject;

                            str = (String) param.args[0];
                            i = (Integer) param.args[1];

                            gps_isfront = false;//XposedHelpers.getStaticBooleanField(cls, "gps_isfront");
                        }catch (Throwable e){
                            Log.e(ModDefaultPlayer.TAG, "error on converting parameters", e);
                            return 0;
                        }

                        int i2 = 1;
                        PackageManager pm = service.getPackageManager();
                        Intent intent = pm.getLaunchIntentForPackage("com.mgh.vlc");

                        if (str != null) {
                            intent.putExtra("dev", str);
                        }
                        if ((i == 1 && gps_isfront) || i == 2) {
                            intent.putExtra("start", 1);
                        } else {
                            i2 = 0;
                        }
                        intent.addFlags(intent.getFlags() | 807600128);
                        try {
                            Log.v(ModDefaultPlayer.TAG, "try to start Prog");
                            //service.startActivityAsUser(intent, UserHandle.CURRENT_OR_SELF);
                            UserHandle handle = (UserHandle) XposedHelpers.getStaticObjectField(UserHandle.class, "CURRENT_OR_SELF");
                            //Object res = XposedHelpers.callMethod(service, "startActivityAsUser", intent, handle);
                            //Log.v(ModDefaultPlayer.TAG, "res from call: " + res);

                            service.startActivity(intent);
                        } catch (Exception e) {
                            Log.e(ModDefaultPlayer.TAG, "error on invoking startActivityAsUser", e);
                            try{
                                service.startActivity(intent);
                            } catch (Exception e1) {
                                Log.e(ModDefaultPlayer.TAG, "error on invoking startActivityAsUser2", e1);
                            }
                        }

                        try {
                            //this.mAppMode = 2;
                            XposedHelpers.setIntField(param.thisObject, "mAppMode", 2);
                        }catch (Throwable e){
                            Log.e(ModDefaultPlayer.TAG, "error on setting mAppMode", e);
                        }

                        Log.v(ModDefaultPlayer.TAG, "start Music end");

                        return i2;

                    }
                });

            }catch (Throwable e) {
                Log.e(ModDefaultPlayer.TAG, "error on hooking field", e);
            }

        }
    }


    }