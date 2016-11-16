package com.virstyuk.zapominaika;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class StartScreenActivity extends Activity {
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.startscreen);
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
        	
        int f = 1;

        @Override
        public void run() {
        	StartScreenActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                	if(f == 0) {
                		startMain();
                		cancel();
                	} else f = 0;
                }
            });
        }
        }, 0, 1000);
	}
	
	public void startMain() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
	}
}
