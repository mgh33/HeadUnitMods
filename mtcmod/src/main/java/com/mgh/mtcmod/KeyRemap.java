package com.mgh.mtcmod;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

import java.lang.reflect.Method;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;


public class KeyRemap implements IXposedHookLoadPackage {

    private static final String TAG = "mgh-keyRemap";

    private static final String serverPgk = "android.microntek.service";
    private static final String serverClsString = serverPgk + ".MicrontekServer";

    private static Class serverCls;


    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {

        if (!loadPackageParam.packageName.equals(serverPgk)) return;

        try{
            serverCls = Class.forName(serverClsString, false, loadPackageParam.classLoader);
        } catch (Throwable e){
            Log.e(KeyRemap.TAG, "error on resolving class for " + serverClsString, e);
            return;
        }

        try {
            XposedHelpers.findAndHookConstructor(serverCls, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);

                    BroadcastReceiver rec;
                    try {
                        rec = (BroadcastReceiver) XposedHelpers.getObjectField(param.thisObject, "CarkeyProc");
                    }catch (Throwable e){
                        Log.e(KeyRemap.TAG, "error on finding CarkeyProc", e);
                        return;
                    }

                    try {
                        XposedHelpers.findAndHookMethod(rec.getClass(), "onReceive", Context.class, Intent.class, new onReceiveHook());
                    }catch (Throwable e){
                        Log.e(KeyRemap.TAG, "error on hooking oncreate", e);
                        return;
                    }

                }
            });
        }catch (Throwable e){
            Log.e(TAG, "error hooking constructor", e);
        }

        try {
            XposedHelpers.findAndHookMethod(serverCls, "cmdProc", byte[].class, int.class, int.class , new cmdProcHook());
        }catch (Throwable e){
            Log.e(TAG, "error hooking cmdProc", e);
        }

    }

    private class onReceiveHook extends XC_MethodHook{
        @Override
        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {

            Intent intent;
            try {
                intent = (Intent) param.args[1];
            }catch (Throwable e){
                Log.e(TAG, "parameter error", e);
                return;
            }

            try {
                String action = intent.getAction();
                //Log.v(TAG, "onreceive action: " + action);
                //if (intent.getAction().equals("com.microntek.irkeyDown"))
                    //Log.v(TAG, "  keycode: " + intent.getIntExtra("keyCode", -1));
            }catch (Throwable e){
                Log.e(TAG, "error action/ keycode ", e);
            }
        }
    }

    public static class MtcCodes {

        public static final int NOP  = -1;

        public static final int EVENT_KEY_DOWN  = 142;
        public static final int EVENT_KEY_UP  = 143;

        public static final int KEY_MUTE = 4;
        public static final int KEY_START_RADIO = 43;
        public static final int KEY_START_DVD = 42;
        public static final int KEY_NEXT = 24;
        public static final int KEY_PREVIOUS = 22;
    }

    private long lstKeyDownTime;
    private int lstKeyDown;
    private static Service mThis;

    private class cmdProcHook extends XC_MethodHook{

        private Object[] oldArgs;

        public final static int CALL_NATIVE = 0;
        public final static int SEND_BROADCAST = 1;

        private Handler handler = new  Handler(){

            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                switch (msg.what) {
                    case CALL_NATIVE:
                        try {
                            Log.v(KeyRemap.TAG, "handler: call native");
                            //callNativeMethod((Object[]) msg.obj);
                        } catch (Throwable e) {
                            Log.e(KeyRemap.TAG, "error on handling msg", e);
                        }
                        break;
                    case SEND_BROADCAST:
                        try {
                            mThis.sendBroadcast((Intent)msg.obj);
                        } catch (Throwable e) {
                            Log.e(KeyRemap.TAG, "error on sending broadcast msg", e);
                        }
                        break;
                }
                msg.recycle();
            }
        };



        @Override
        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
            Log.v(KeyRemap.TAG, "!!!!!!!!!!!!!!!!!!!!!!!!!");
            try {
                mThis = (Service) param.thisObject;
            } catch (Throwable e){
                Log.e(KeyRemap.TAG, "error on casting", e);
                return;
            }

            byte[] bArr;
            int i;
            int i2;
            try {
                bArr = (byte[]) param.args[0];
                i = (Integer) param.args[1];
                i2 = (Integer) param.args[2];
            }catch (Throwable e){
                Log.e(TAG, "error converting parameters cmdProc ", e);
                return;
            }

            int val;
            int key = -1;
            try {
                val = (bArr[i + 1] & 255);

                switch (val){
                    case MtcCodes.EVENT_KEY_DOWN:
                    case MtcCodes.EVENT_KEY_UP:
                        key = getInt(bArr, i + 3, 1) + 1;
                        Log.v(TAG, "val: " + val + ",  key: " + key);
                        break;
                }

            }catch (Throwable e){
                Log.e(TAG, "error eval proc para", e);
                return;
            }

            // disable native implementation
            param.setResult(null);

/*            if (val == MtcCodes.EVENT_KEY_DOWN && false){
                // key down event

                if (lstKeyDown != MtcCodes.NOP){
                    // different keydown event
                    Log.v(KeyRemap.TAG, "different key");
                    // create the native keydown- event
                    callNativeMethod(oldArgs);
                    return;
                }
                Log.v(KeyRemap.TAG, "keydown");
                lstKeyDownTime = SystemClock.uptimeMillis();
                lstKeyDown = key;

                oldArgs = new Object[]{bArr, i, i2};

                // disable native implementation
                param.setResult(null);


            } else if (val == MtcCodes.EVENT_KEY_UP && false){
                // key up event

                Log.v(KeyRemap.TAG, "keyup");
                // disable native implementation
                param.setResult(null);

                long runtime = SystemClock.uptimeMillis() - lstKeyDownTime;

                if (lstKeyDown != key){
                    // keys do not match
                    lstKeyDown = MtcCodes.NOP;
                    executeNative(bArr, i, i2, runtime);
                    return;
                }

                lstKeyDown = MtcCodes.NOP;

                if (runtime < 600){
                    Log.v(KeyRemap.TAG, "short click: " + key);
                    switch (key){
                        case MtcCodes.KEY_START_RADIO:
                        case MtcCodes.KEY_START_DVD:
                            Intent intentDown = new Intent("com.microntek.irkeyDown");
                            Intent intentUp = new Intent("com.microntek.irkeyUp");
                            if (key == MtcCodes.KEY_START_RADIO) {
                                intentDown.putExtra("keyCode", MtcCodes.KEY_NEXT);
                                intentUp.putExtra("keyCode", MtcCodes.KEY_NEXT);
                            } else {
                                intentDown.putExtra("keyCode", MtcCodes.KEY_PREVIOUS);
                                intentUp.putExtra("keyCode", MtcCodes.KEY_PREVIOUS);
                            }
                            Log.v(KeyRemap.TAG, "broadcast down");
                            mThis.sendBroadcast(intentDown);
                            //mThis.sendBroadcastAsUser(intentDown, UserHandle.CURRENT_OR_SELF);

                            Log.v(KeyRemap.TAG, "broadcast up");
                            Message msg = handler.obtainMessage();
                            msg.what = SEND_BROADCAST;
                            msg.obj = intentUp;
                            //handler.sendMessageDelayed(msg, 200);
                            mThis.sendBroadcast(intentUp);
                            break;
                        default:
                            executeNative(bArr, i, i2, runtime);
                            break;
                    }
                }else{
                    Log.v(KeyRemap.TAG, "long click");
                    Intent intent = new Intent("com.microntek.app");
                    switch (key){
                        case MtcCodes.KEY_START_RADIO:
                            intent.putExtra("app", "radio");
                            break;
                        case MtcCodes.KEY_START_DVD:
                            intent.putExtra("app", "music");
                            break;
                        default:
                            executeNative(bArr, i, i2, runtime);
                            return;
                    }
                    mThis.sendBroadcast(intent);

                }

            }*/
        }

        private void executeNative(byte[] bArr, int i, int i2, long runtime) {
            Log.v(KeyRemap.TAG, "start native");
            // create the native keydown- event
            callNativeMethod(oldArgs);

            // create the native keyup- event
            Message msg = handler.obtainMessage();
            msg.what = CALL_NATIVE;
            msg.obj = new Object[]{bArr, i, i2};
            callNativeMethod((Object[]) msg.obj);
            //handler.sendMessageDelayed(msg, runtime);
            Log.v(KeyRemap.TAG, "end native");
        }

        private void callNativeMethod(Object[] args) {
            try {
                Log.v(KeyRemap.TAG, "call native");
                Method m = XposedHelpers.findMethodBestMatch(serverCls, "cmdProc", byte[].class, int.class, int.class);
                XposedBridge.invokeOriginalMethod(m, mThis, args);
            } catch (Throwable e){
                Log.e(KeyRemap.TAG, "error on invoking original method", e);
            }
        }


        private int getInt(byte[] serialBuff, int start, int size) {
            int dat = 0;
            for (int i = 0; i < size; i++) {
                dat = (dat << 8) + (serialBuff[start + i] & 255);
            }
            return dat;
        }

    }
}
