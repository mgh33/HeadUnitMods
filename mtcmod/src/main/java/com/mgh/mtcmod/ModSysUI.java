package com.mgh.mtcmod;

import android.app.AndroidAppHelper;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.XModuleResources;
import android.content.res.XResources;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mgh.mghlibs.MghService;

import java.util.Locale;

import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_InitPackageResources.InitPackageResourcesParam;
import de.robv.android.xposed.callbacks.XC_LayoutInflated;
import de.robv.android.xposed.callbacks.XC_LoadPackage;




public class ModSysUI extends ModBase implements IXposedHookInitPackageResources, IXposedHookLoadPackage {

    private final static String TAG = "mgh-modSysUI";

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

        //Log.i(TAG, "update txtField; vol: " + volume + ", mute: " + mute);

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




    public void handleInitPackageResources(final InitPackageResourcesParam resparam) throws Throwable {

        if (!resparam.packageName.equals(pkgSysUI)) return;
        if (!App.Settings().showSpeed() && !App.Settings().showVolume()) return;

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


                    if (App.Settings().showVolume())
                        buttonLayout.addView(volDispLayout, 5);
                    if (App.Settings().showSpeed())
                        buttonLayout.addView(spdDispLayout, 5);

                }catch(Throwable e){
                    Log.e(ModSysUI.TAG, "error in handler", e);
                }

                try{
                    InfoReceiver inf = new InfoReceiver(ModSysUI.this);
                    IntentFilter filter = new IntentFilter();
                    if (App.Settings().showSpeed())
                        filter.addAction(MghService.INTENT_ACTION_UPD_SPEED);
                    if (App.Settings().showVolume())
                        filter.addAction(MghService.INTENT_ACTION_UPD_VOLUME);
                    ctx.registerReceiver(inf, filter);
                }catch (Throwable e){
                    Log.e(ModSysUI.TAG, "error in handler", e);

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
                Log.e(ModSysUI.TAG, "error on find class " + appSysUI, e);
                return;
            }

            try {
                Log.v(ModSysUI.TAG, "try to hook oncreate");

                final Intent intent = new Intent();

                try{
                    ComponentName name = new ComponentName(ModSysUI.class.getPackage().getName().toString(),
                            MghService.class.getCanonicalName());
                    //Log.d(ModSysUI.TAG, "compName: " + name.toString());
                    intent.setComponent(name);
                }catch (Throwable e){
                    Log.e(ModSysUI.TAG, "error on creating intent", e);
                }

                XposedHelpers.findAndHookMethod(cls, "onCreate", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {


                        Context appCtx = AndroidAppHelper.currentApplication();

                        try {
                            //Log.v(ModSysUI.TAG, "start mghService");
                            appCtx.startService(intent);

                        }catch (Throwable e){
                            Log.e(ModSysUI.TAG, "error on start service", e);
                        }
                    }
                });

            }catch (Throwable e) {
                Log.e(ModSysUI.TAG, "error on hooking field", e);
            }

        }
    }


    }