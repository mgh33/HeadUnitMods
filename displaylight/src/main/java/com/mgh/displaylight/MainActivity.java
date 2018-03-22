package com.mgh.displaylight;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
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


public class MainActivity extends Activity implements View.OnClickListener{


    private final static String TAG = "mgh-lightMain";

    private final static int WHAT_LOG = 1;
    private final static int WHAT_UPD = 2;

    private static TextView txtCurValue = null;

    public static Handler logHandler = new Handler(){
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
    };

    private ListView log = null;
    private static ArrayAdapter<String> adapter;

    public static void log(String str){

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
        setContentView(R.layout.activity_main);

        // start service
        Intent intent1 = new Intent(this, MghService.class);
        startService(intent1);

        log = (ListView) findViewById(R.id.listLog);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        log.setAdapter(adapter);
        log("log init");

        //txtCurValue = (TextView) findViewById(R.id.txtValue);

        Intent i = new Intent(this, LightService.class);
        startService(i);
        SysProps.GetSysProps(MainActivity.this).setBrightness(255);
        try {

            if (!android.provider.Settings.System.canWrite(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:com.mgh.displaylight"));
                startActivity(intent);
            }

            PackageInfo info = getPackageManager().getPackageInfo(this.getPackageName(), PackageManager.GET_PERMISSIONS);
            if (info.requestedPermissions != null) {
                for (String p : info.requestedPermissions) {
                    if ( ContextCompat.checkSelfPermission( this, p ) != PackageManager.PERMISSION_GRANTED ) {
                        ActivityCompat.requestPermissions( this, new String[] {  p  }, 1);
                    }
                }
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
                // TODO Auto-generated method stub

                SysProps.GetSysProps(MainActivity.this).setBrightness(seekBar.getProgress());
                //t1.setTextSize(progress);
                //Toast.makeText(getApplicationContext(), String.valueOf(progress),Toast.LENGTH_LONG).show();

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
