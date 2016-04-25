package com.mgh.mtcmod;

import android.app.AndroidAppHelper;
import android.app.Application;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.XModuleResources;
import android.content.res.XResources;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mgh.mghlibs.MghService;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_InitPackageResources.InitPackageResourcesParam;
import de.robv.android.xposed.callbacks.XC_LayoutInflated;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

//todo: mute

//todo: KLD

public class ModSys implements IXposedHookZygoteInit, IXposedHookInitPackageResources, IXposedHookLoadPackage {

    private final static String TAG = "mgh-modSys";

    private String mod_path = null;

    private ImageView imgVol;
    private ImageView imgSpd;
    private TextView txtVolume;
    private TextView txtSpeed;

    private int resIdMuteIcon;
    private int resIdVolIcon;
    private int resIdSpdIcon;
    private int resIdStatusDisp;

    private int lstVol;

    private XResources sysUiRes;

    public void updateSpeed(int speed){
        try {

            txtSpeed.setText(String.format(Locale.getDefault(),"%3d",speed));

        }catch (Throwable e){
            Log.e(TAG, "error on receiving volume", e);
        }

    }
    public void updateVol(int volume, boolean mute) {

        Log.i(TAG, "update txtField; vol: " + volume + ", mute: " + mute);

        if (txtVolume != null) {
            txtVolume.setText(String.format(Locale.getDefault(),"%2d",volume));
        }

        if (imgVol != null) {
            try {
                if (mute) {
                    //Log.i(TAG, "load mute icon");
                    imgVol.setImageDrawable(sysUiRes.getDrawable(resIdMuteIcon));
                } else {
                    //Log.i(TAG, "load speaker icon");
                    imgVol.setImageDrawable(sysUiRes.getDrawable(resIdVolIcon));
                }
            } catch (Throwable e) {
                Log.e(TAG, "error on setting image", e);
            }
        }

        lstVol = volume;
    }


    public void initZygote(StartupParam startupParam) throws Throwable {
        mod_path = startupParam.modulePath;
    }


    private final String pkgSysUI = "com.android.systemui";
    private final String appSysUI = pkgSysUI + ".SystemUIService";

    public void handleInitPackageResources(final InitPackageResourcesParam resparam) throws Throwable {
        if (!resparam.packageName.equals(pkgSysUI)) return;

        sysUiRes = resparam.res;
        XModuleResources localRes = XModuleResources.createInstance(mod_path, resparam.res);
        resIdMuteIcon = sysUiRes.addResource(localRes, R.drawable.mute_icon_light);
        resIdVolIcon = sysUiRes.addResource(localRes, R.drawable.speaker_icon_light);
        resIdSpdIcon = sysUiRes.addResource(localRes, R.drawable.speed_light);
        resIdStatusDisp = sysUiRes.addResource(localRes, R.layout.status_display);


        //region Hooklayout for SysUI StatusBar
        resparam.res.hookLayout(pkgSysUI, "layout", "status_bar", new XC_LayoutInflated() {
            @Override
            public void handleLayoutInflated(LayoutInflatedParam liparam) throws Throwable {

                Context ctx = liparam.view.getContext();

                try {

                    LinearLayout buttonLayout = (LinearLayout) liparam.view.findViewById(
                            liparam.res.getIdentifier("button_layout", "id", pkgSysUI));

                    // inflate the volume-display
                    LayoutInflater infl = LayoutInflater.from(ctx);
                    LinearLayout volDispLayout = (LinearLayout) infl.inflate(resIdStatusDisp, null);
                    LinearLayout spdDispLayout = (LinearLayout) infl.inflate(resIdStatusDisp, null);

                    txtVolume = (TextView) volDispLayout.getChildAt(1);
                    imgVol = (ImageView) volDispLayout.getChildAt(0);
                    imgVol.setImageResource(resIdMuteIcon);

                    txtSpeed = (TextView) spdDispLayout.getChildAt(1);
                    imgSpd = (ImageView) spdDispLayout.getChildAt(0);
                    imgSpd.setImageResource(resIdSpdIcon);


                    buttonLayout.addView(volDispLayout, 5);
                    buttonLayout.addView(spdDispLayout, 5);

                }catch(Throwable e){
                    Log.e(ModSys.TAG, "error in handler", e);
                }

                try{
                    InfoReceiver inf = new InfoReceiver(ModSys.this);
                    IntentFilter filter = new IntentFilter();
                    filter.addAction(MghService.INTENT_ACTION_UPD_SPEED);
                    filter.addAction(MghService.INTENT_ACTION_UPD_VOLUME);
                    ctx.registerReceiver(inf, filter);
                }catch (Throwable e){
                    Log.e(ModSys.TAG, "error in handler", e);

                }

            }
        });
        //endregion

    }



    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {

        if (loadPackageParam.packageName.equals(pkgSysUI)){

            // start the MghService together with SystemUI
            //region handle start SystemUI

            Class cls;
            try{
                cls = Class.forName(appSysUI, false, loadPackageParam.classLoader);
            }catch (Throwable e){
                Log.e(ModSys.TAG, "error on find class " + appSysUI, e);
                return;
            }

            try {
                Log.v(ModSys.TAG, "try to hook oncreate");

                final Intent intent = new Intent();

                try{
                    ComponentName name = new ComponentName(ModSys.class.getPackage().getName().toString(),
                            MghService.class.getCanonicalName());
                    //Log.d(ModSys.TAG, "compName: " + name.toString());
                    intent.setComponent(name);
                }catch (Throwable e){
                    Log.e(ModSys.TAG, "error on creating intent", e);
                }

                XposedHelpers.findAndHookMethod(cls, "onCreate", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {


                        Context appCtx = AndroidAppHelper.currentApplication();

                        try {
                            //Log.v(ModSys.TAG, "start mghService");
                            appCtx.startService(intent);

                        }catch (Throwable e){
                            Log.e(ModSys.TAG, "error on start service", e);
                        }
                    }
                });

            }catch (Throwable e) {
                Log.e(ModSys.TAG, "error on hooking field", e);
            }

        }
    }


    }