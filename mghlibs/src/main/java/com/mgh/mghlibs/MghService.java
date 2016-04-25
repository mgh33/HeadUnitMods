package com.mgh.mghlibs;

import android.Manifest;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.util.Log;


public class MghService extends Service implements LocationListener, VolumeObserver.IVolumeUpdateListener {

    private final static String TAG = "mgh-service";

    public final static String INTENT_EXTRA_SPEED = MghService.class.getCanonicalName() + ".speed";
    public final static String INTENT_EXTRA_VOLUME = MghService.class.getCanonicalName() + ".vol";
    public final static String INTENT_EXTRA_MUTE = MghService.class.getCanonicalName() + ".mute";
    public final static String INTENT_EXTRA_SEND_KLD = MghService.class.getCanonicalName() + ".SEND_STR";

    public final static String INTENT_ACTION_UPD_SPEED = MghService.class.getCanonicalName() + ".update_speed";
    public final static String INTENT_ACTION_UPD_VOLUME = MghService.class.getCanonicalName() + ".update_volume";
    public final static String INTENT_ACTION_SEND_KLD = MghService.class.getCanonicalName() + ".SEND_KLD";

    VolumeObserver volumeObserver;


    //region Overrides
    @Override
    public void onCreate(){
        super.onCreate();

        volumeObserver =  new VolumeObserver(this, this);

        // fill the fields with current values
        changed();

        //region Register location manager

        try {
            Log.i(TAG, "try retrieve location-manager");
            final LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

            BroadcastReceiver rec = new BroadcastReceiver() {

                @Override
                public void onReceive(Context context, Intent intent) {

                    //Log.v(TAG, "onReceive provider changed");

                    try {

                        if (ActivityCompat.checkSelfPermission(MghService.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                                //Log.d(TAG, "provider is enabled");
                                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, MghService.this);
                            }//else
                                //Log.d(TAG, "provider is disabled");

                        } else {
                            Log.w(TAG, "location updates not registered (permissions not granted");
                            // Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            //return;
                        }

                    }catch (Throwable e){
                        Log.e(TAG, "error on registering location-manager", e);
                    }
                }
            };

            IntentFilter filter = new IntentFilter();
            filter.addAction(LocationManager.PROVIDERS_CHANGED_ACTION);
            registerReceiver(rec, filter);

        }catch (Throwable e){
            Log.e(TAG, "error on retrieving locationManager", e);
        }

        //endregion
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private long lstSpeed;


    @Override
    public void onLocationChanged(Location location) {

        //Log.v(TAG, "onlocationchanged");
        try {
            //if (!Settings.get(this).getSpeedEnable()) return; // Speed control is disabled
            if (!location.hasSpeed()) return; // Speed control is disabled

            Log.v(TAG, "accuracy: " + location.getAccuracy()); //todo: tst accuracy

            double speed = location.getSpeed();
            speed = speed * 3.6; // m/s => km/h


            int spd = (int) Math.round(speed);
            if (spd != lstSpeed) {
                Intent intent = new Intent(INTENT_ACTION_UPD_SPEED);
                intent.putExtra(INTENT_EXTRA_SPEED, spd);
                sendBroadcast(intent);
            }
            lstSpeed = spd;
        }catch (Throwable e){
            Log.v(TAG, "location err", e);

        }

    }


    @Override
    public void changed() {
        //Log.v(TAG, "volume changed ");
        Intent intent = new Intent(INTENT_ACTION_UPD_VOLUME);
        intent.putExtra(INTENT_EXTRA_VOLUME, volumeObserver.getVolume());
        intent.putExtra(INTENT_EXTRA_MUTE, volumeObserver.getMute());
        sendBroadcast(intent);
    }

    //region empty Overrides
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.v(TAG, "GPS status changed: " + status);
    }

    @Override
    public void onProviderEnabled(String provider) {
        //Log.v(TAG, "GPS provider enabled");
    }

    @Override
    public void onProviderDisabled(String provider) {
        //Log.v(TAG, "GPS provider disabled");
    }
    //endregion
    //endregion

}
