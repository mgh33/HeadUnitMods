package com.mgh.displaylight;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class SensorStartActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent i = new Intent(this, LightService.class);
        startService(i);

        finish();
    }
}
