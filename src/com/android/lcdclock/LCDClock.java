package com.android.lcdclock;

import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.widget.TextView;

public class LCDClock extends Activity {
	private PowerManager pm;
	private PowerManager.WakeLock wl;
	private TextView clockText;
	private TextView clockBack;
	private Timer mTimer;
	private boolean BulletsVisible = true;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	// android:background="@drawable/background"
    	// android:textColor="#1D67C5"
    	super.onCreate(savedInstanceState);
		/*
        // this can be replaced in manifest by:
        //	android:theme="@android:style/Theme.NoTitleBar.Fullscreen"
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        		WindowManager.LayoutParams.FLAG_FULLSCREEN);
        */
        setContentView(R.layout.main);
        
        clockText = (TextView)findViewById(R.id.TimeTextView);
        clockBack = (TextView)findViewById(R.id.TimeBackView);
        
        handleOrientation(getResources().getConfiguration().orientation);
		
        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/DS-DIGIT.TTF");
        clockText.setTypeface(typeface);
        clockBack.setTypeface(typeface);
        
        /*
        // Turn off buttons on Froyo
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.buttonBrightness = 0;
        */
        pm = (PowerManager) getSystemService(POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "My Tag");
        wl.acquire();
        
        /*
         * By calling SCREEN_DIM_WAKE_LOCK you will set the screen to dim and the keyboard to off.
         * Alternatively, you can call SCREEN_BRIGHT_WAKE_LOCK which sets the screen to bright and
         * the keyboard to off or PARTIAL_WAKE_LOCK which turns both the screen and the keyboard off.
         */
        
        // Включим пурпурный индикатор
		//setLedState("amber", 255);
        setLedState("blue", 255);
		
		// Сделаем дисплей темнее
		//setLedState("lcd-backlight", 0);
		
		// Выключим подсветку кнопок
        setLedState("button-backlight", 0);
		
		// Организуем фонарик средней яркости
		//setLedState("flashlight", 128);
        
        mTimer = new Timer("LCDClockTimer");
		mTimer.scheduleAtFixedRate(new SendMessageTask(), 0, 1000);
    }
    
    @Override
    public void onPause(){
    	//setLedState("lcd-backlight", 90);
    	setLedState("button-backlight", 255);
    	setLedState("blue", 0);
    	wl.release();
    	super.onPause();
    }
    
    @Override
    public void onResume(){
    	super.onResume();
    	//setLedState("lcd-backlight", 0);
    	setLedState("button-backlight", 0);
    	setLedState("blue", 255);
        wl.acquire();
    }
    
	@Override
	public void onConfigurationChanged(Configuration newConfig) { 
		handleOrientation(newConfig.orientation);
		super.onConfigurationChanged(newConfig); 
	}
	/*+ manifest:
	android:screenOrientation="landscape" (portrait, sensor)
	android:configChanges="orientation|keyboardHidden"
	*/

    public void handleOrientation(int orientation) {
		if (orientation == Configuration.ORIENTATION_PORTRAIT) {
			clockText.setTextSize(140);
			clockBack.setTextSize(140);
		} else {
			clockText.setTextSize(200);
			clockBack.setTextSize(200);
		}
    }
    
    public void setLedState(String name, int brightness) {
        try {
            FileWriter fw = new FileWriter("/sys/class/leds/" + name + "/brightness");
            fw.write(Integer.toString(brightness));
            fw.close();
        } catch (Exception e) {
            // Управление LED недоступно
        }
    }
    
	public class SendMessageTask extends TimerTask {
		@Override
		public void run() {
			Message m = new Message();
			LCDClock.this.updateClockHandler.sendMessage(m);
		}
	}
	
    Handler updateClockHandler = new Handler() {
    	public void handleMessage(Message msg) {
    		String DateMask;
    		if (BulletsVisible) {
    			DateMask = "HH:mm";
    		} else {
    			DateMask ="HH mm";
    		}
    		SimpleDateFormat formatter = new SimpleDateFormat(DateMask);
    		String timeString = formatter.format(new Date());
    		clockText.setText(timeString);
    		
    		BulletsVisible = !BulletsVisible;
    		
    		super.handleMessage(msg);
    	}
	};
	//	
}