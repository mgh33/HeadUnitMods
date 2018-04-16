package com.mgh.headunitmods;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.multidex.MultiDex;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.mgh.mghlibs.SysProps;
import com.mgh.mghlibs.MghService;

import java.security.Permission;
import java.security.Permissions;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends Activity implements View.OnClickListener{


    private final static String TAG = "mgh-lightMain";

    private final static int WHAT_LOG = 1;
    private final static int WHAT_UPD = 2;


    private static Handler logHandler;

    private static class LogHandler extends Handler{

        private TextView txtCurValue = null;
        private ArrayAdapter<String> adapter;

        LogHandler(MainActivity activity){

            ListView log = activity.findViewById(R.id.listLog);
            adapter = new ArrayAdapter<>(activity, android.R.layout.simple_list_item_1);
            log.setAdapter(adapter);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch ( msg.what){
                case WHAT_LOG:
                    if (adapter != null) {
                        adapter.insert(msg.obj.toString(), 0);
                        if (adapter.getCount() > 50){
                            adapter.remove(adapter.getItem(50));
                        }
                    }
                    break;
                case WHAT_UPD:
                    if (txtCurValue != null) {
                        txtCurValue.setText(msg.obj.toString());
                    }
                    break;
            }

        }
    }


    public static void log(String str){

        if (logHandler == null) return;
        Message msg = logHandler.obtainMessage(WHAT_LOG);
        msg.obj = str;
        logHandler.sendMessage(msg);

    }

    public static void updValue(double value){

        Message msg = logHandler.obtainMessage(WHAT_UPD);
        msg.obj = value;
        logHandler.sendMessage(msg);

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MultiDex.install(this);
        setContentView(R.layout.activity_main);

        // start service
        Intent intent1 = new Intent(this, MghService.class);
        startService(intent1);

        logHandler = new LogHandler(this);

        log("log init");

        //txtCurValue = (TextView) findViewById(R.id.txtValue);

        //Intent i = new Intent(this, LightService.class);
        //startService(i);
        SysProps.GetSysProps(MainActivity.this).setBrightness(255);
        try {

            if (!android.provider.Settings.System.canWrite(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:com.mgh.headunitmods"));
                startActivity(intent);
            }

            PackageInfo info = getPackageManager().getPackageInfo(this.getPackageName(), PackageManager.GET_PERMISSIONS);
            if (info.requestedPermissions != null) {
                List<String> reqPerm = new ArrayList<>();
                for (String p : info.requestedPermissions) {
                    if ( !Manifest.permission.WRITE_SETTINGS.equals(p) &&
                            ContextCompat.checkSelfPermission( this, p ) != PackageManager.PERMISSION_GRANTED ) {
                        reqPerm.add(p);
                    }
                }
                if (reqPerm.size() > 0)
                    ActivityCompat.requestPermissions( this, reqPerm.toArray(new String[0]), 1);
            }
        }catch (Exception e){
            Log.e(TAG, "error on request permissions", e);
        }


        final SeekBar sk= findViewById(R.id.seekBar1);
        sk.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {

                SysProps.GetSysProps(MainActivity.this).setBrightness(seekBar.getProgress());
                //t1.setTextSize(progress);
                //Toast.makeText(getApplicationContext(), String.valueOf(progress),Toast.LENGTH_LONG).show();

            }
        });

        final SeekBar skVol= findViewById(R.id.seekBarVol);
        skVol.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {

                SysProps.setVolume(MainActivity.this, seekBar.getProgress());

            }
        });

        final SeekBar skSpeedSim= findViewById(R.id.seekBarSpeedSim);
        final TextView txtSpeed = findViewById(R.id.txtSpeedSim);
        skSpeedSim.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            double oldSpeed = Double.NaN;
            @Override
            public void onProgressChanged(SeekBar seekBar, int speed, boolean fromUser) {

                txtSpeed.setText(String.format("%s", speed));


                Intent intent = new Intent(MghService.INTENT_ACTION_UPD_SPEED);
                intent.putExtra(MghService.INTENT_EXTRA_SPEED, "" +  Math.round(speed));
                intent.putExtra(MghService.INTENT_EXTRA_SPEED_DBL, (double) Math.round(speed));
                intent.putExtra(MghService.INTENT_EXTRA_SPEED_OLD_DBL, oldSpeed);
                sendBroadcast(intent);
                oldSpeed = speed;
            }
        });
    }


    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.btnSettings:
                Intent i = new Intent(this, SettingsActivity.class);
                startActivity(i);
                break;
        }
    }



















}
