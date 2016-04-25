package com.mgh.mtcmod;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.mgh.mghlibs.MghService;

public class MainActivity extends Activity implements View.OnClickListener {

    private EditText txtSend;

    private final static String TAG = "mgh-activity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtSend = (EditText) findViewById(R.id.txtSend);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){

            case R.id.btnSendKLD: {

                Log.v(TAG, "onClickKLD");
                try {
                    Intent intent = new Intent(MghService.INTENT_ACTION_SEND_KLD);
                    intent.putExtra(MghService.INTENT_EXTRA_SEND_KLD, txtSend.getText().toString());
                    sendBroadcast(intent);
                }catch (Throwable e){
                    Log.d(TAG, "error on intent", e);
                }
                Log.v(TAG, "end OnClick KLD");
                break;
            }
        }
    }
}
