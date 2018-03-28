package com.mgh.mghlibs;

import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import android.os.Handler;

/**
 * Created by heiss on 22.03.2018.
 */

public class TWUtil {

    private Object mObj;
    private final static String TAG = "mgh-twutil";

    //    public final static int MUTE_EVENT = 517;
    public final static int VOLUME_EVENT = 515;

    private static TWUtil instance = null;

    public static TWUtil getInstance(){

        if (instance == null)
            instance = new TWUtil();

        return instance;
    }


    private TWUtil() {

        Class c = null;
        try {
            c = Class.forName("android.tw.john.TWUtil");
        } catch (ClassNotFoundException e) {
            //e.printStackTrace();
            Log.e(TAG, "class not found", e);
            return;
        }

        try {
            mObj = c.newInstance();
        } catch (InstantiationException e) {
            //e.printStackTrace();
            Log.e(TAG, "error init call", e);
            return;
        } catch (IllegalAccessException e) {
            //e.printStackTrace();
            Log.e(TAG, "error init call2", e);
            return;
        }

    }

    public void addHandler(String name, Handler handler){

        if (mObj == null) return;

        Method m = null;
        try {
            m = mObj.getClass().getMethod("addHandler", String.class, Handler.class);
        } catch (NoSuchMethodException e) {
            //e.printStackTrace();
            Log.e(TAG, "error get method addHandler", e);
            return;
        }

        try {
            m.invoke(mObj, name, handler );
        } catch (IllegalAccessException e) {
            //e.printStackTrace();
            Log.e(TAG, "error call addHandler", e);
            return;
        } catch (InvocationTargetException e) {
            //e.printStackTrace();
            Log.e(TAG, "error call addHandler2", e);
            return;
        }

    }

    public void start(){

        if (mObj == null) return;

        Method m = null;
        try {
            m = mObj.getClass().getMethod("start");
        } catch (NoSuchMethodException e) {
            //e.printStackTrace();
            Log.e(TAG, "error get method start", e);
            return;
        }

        try {
            m.invoke(mObj);
        } catch (IllegalAccessException e) {
            //e.printStackTrace();
            Log.e(TAG, "error call start", e);
            return;
        } catch (InvocationTargetException e) {
            //e.printStackTrace();
            Log.e(TAG, "error call start2", e);
            return;
        }

    }

    public void close(){

        if (mObj == null) return;

        Method m = null;
        try {
            m = mObj.getClass().getMethod("close");
        } catch (NoSuchMethodException e) {
            //e.printStackTrace();
            Log.e(TAG, "error get method close", e);
            return;
        }

        try {
            m.invoke(mObj);
        } catch (IllegalAccessException e) {
            //e.printStackTrace();
            Log.e(TAG, "error call close", e);
            return;
        } catch (InvocationTargetException e) {
            //e.printStackTrace();
            Log.e(TAG, "error call close2", e);
            return;
        }

    }

    public int open(short[] eventIds){

        if (mObj == null) return -1;

        Method m = null;
        try {
            m = mObj.getClass().getMethod("open", short[].class);
        } catch (NoSuchMethodException e) {
            //e.printStackTrace();
            Log.e(TAG, "error get method open", e);
            return -1;
        }

        try {
            Object o = m.invoke(mObj, eventIds);
            return (Integer) o;
        } catch (IllegalAccessException e) {
            //e.printStackTrace();
            Log.e(TAG, "error call open", e);
            return -1;
        } catch (InvocationTargetException e) {
            //e.printStackTrace();
            Log.e(TAG, "error call open2", e);
            return -1;
        }

    }

    public void write(int p1, int p2, int p3){

        if (mObj == null) return;

        Method m = null;
        try {
            Object o = mObj.getClass().getMethods();
            m = mObj.getClass().getMethod("write", int.class, int.class, int.class);
        } catch (NoSuchMethodException e) {
            //e.printStackTrace();
            Log.e(TAG, "error get method write(3)", e);
            return;
        }

        try {
            m.invoke(mObj, p1, p2, p3 );
        } catch (IllegalAccessException e) {
            //e.printStackTrace();
            Log.e(TAG, "error call write(3)", e);
            return;
        } catch (InvocationTargetException e) {
            //e.printStackTrace();
            Log.e(TAG, "error call write(3)2", e);
            return;
        }

    }

}
