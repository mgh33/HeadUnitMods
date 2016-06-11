package com.mgh.displaylight;

import android.app.Activity;
import android.content.Intent;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;


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

        log = (ListView) findViewById(R.id.listLog);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        log.setAdapter(adapter);
        log("log init");

        //txtCurValue = (TextView) findViewById(R.id.txtValue);

        Intent i = new Intent(this, LightService.class);
        startService(i);
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
