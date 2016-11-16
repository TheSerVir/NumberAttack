package com.virstyuk.zapominaika;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewManager;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RatingBar.OnRatingBarChangeListener;
import android.widget.TextView;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class MainActivity extends ActionBarActivity {
	
	DisplayMetrics metrics = new DisplayMetrics();
	LinearLayout mainLayout;
	TextView infoTextView;
	TextView scoresView;
	TextView errorsView;
	LinearLayout fieldStrings[];
	Button buttonsGrid[];
	
	SharedPreferences settings;
	final String SAVED_SIZE = "saved_size";
	final String SAVED_TIME = "saved_time";
	final String SAVED_COUNT = "saved_count";
	final String SAVED_VIBRO = "saved_vibro";
	final String RULES_SHOWED = "rules_showed";
	final String RATING = "rate_info";
	
	boolean canClick = false;
	
	OnClickListener gameClick;
	
	int field[];
	int coord[][];
	
	int now;
	int time;
	int size;
	int count;
	
	int rate;
	
	int scores = 0;
	int errors = 0;
	int gametime = 0;
	
	int refreshed = 0;
	
	final int vibrotime = 200;
	boolean vibro;
	boolean rules;
	
	boolean canceled = true;
	
	Timer gametimer_1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);
		getWindowManager().getDefaultDisplay().getMetrics(metrics); // Инициализация DisplayMetrics
		// Реклама
		//AdView mAdView = (AdView) findViewById(R.id.adView);
		//AdRequest adRequest = new AdRequest.Builder().build();
		//mAdView.loadAd(adRequest);
		// Извлекаем нужные view
		mainLayout = (LinearLayout) findViewById(R.id.mainLayout);
		infoTextView = (TextView) findViewById(R.id.infoView);
		scoresView = (TextView) findViewById(R.id.scoresView);
		errorsView = (TextView) findViewById(R.id.errorsView);
		// ---------------------
		// Объявляем Listener для кнопок
		gameClick = new OnClickListener() { 
        	Button s;
			@Override
			public void onClick(View v) {
				if(canClick)
					if(v.getBackground().getConstantState().equals(getResources().getDrawable(R.drawable.button_def).getConstantState()) | (Integer) v.getTag() >= now) {
					final Button butt = (Button) findViewById(v.getId());
					if(now==1 && canceled) gametimer_1 = GameTimer();
					if(now == (Integer)v.getTag()) {
						butt.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_true));
						butt.setText(Integer.toString(now));
						now++;
						scores++;

						if(now>count) {
							victory();
							if(!canceled) {
								gametimer_1.cancel();
								canceled = true;
							}
						}
					} else {
				        Timer timer = new Timer();
				        timer.schedule(new TimerTask() {
					        Button thisb = butt;
					        int f = 1;

					        @Override
					        public void run() {
					            MainActivity.this.runOnUiThread(new Runnable() {
					                public void run() {
					                	if(f==0 && thisb.getBackground().getConstantState().equals(getResources().getDrawable(R.drawable.button_false).getConstantState())) {
				                			thisb.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_def));
				                			cancel();
					                	}
					                	f = 0;
					                }
					            });
					        }
				        }, 0, 1000);
						butt.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_false));
						errors++;
						if(vibro) { 
							Vibrator vibro = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
							vibro.vibrate(vibrotime);
						}
					}
				}
				updateInfo();
			}
		};
		// ---------------------
		Button butt;
		for(int i=0; i<size; i++) { // строки
			for(int j=0; j<size; j++) { // столбцы
				butt = (Button) findViewById((i*size+j+1)*10);
				((ViewManager)butt.getParent()).removeView(butt);
			}
		}
		// Получение параметров
		settings = PreferenceManager.getDefaultSharedPreferences(this);
		size = Integer.parseInt(settings.getString(SAVED_SIZE, "4"));
		count = Integer.parseInt(settings.getString(SAVED_COUNT, "5"));
		time = Integer.parseInt(settings.getString(SAVED_TIME, "3"))*1000;
		vibro = settings.getBoolean(SAVED_VIBRO, true);
		// + тут работаем с окошками
		rules = settings.getBoolean(RULES_SHOWED, false); // правила
		rate = settings.getInt(RATING, 0); // прошу оценок
		if(!rules) {
			showRules();
		    Editor ed = settings.edit();
		    ed.putString(SAVED_SIZE, "4");
		    ed.commit();
		} else if(rate == 0) {
			showRateMe();
		} else if(rate>0) {
		    Editor ed = settings.edit();
		    ed.putInt(RATING, rate-1);
		    ed.commit();
		}
		// ---------------------
		startGame(); // Начинаем игру    
	}
	/*
	@Override
	public void onStart() {
		super.onStart();
		EasyTracker.getInstance(this).activityStart(this);
	}
	
	@Override
	public void onStop() {
		super.onStop();
		EasyTracker.getInstance(this).activityStop(this);
	}
	*/
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
		    Intent intent = new Intent(MainActivity.this, PrefActivity.class);
		    startActivityForResult(intent, 1);
		}
		if (id == R.id.action_refresh) {
			newSession();
		}
		return super.onOptionsItemSelected(item);
	}
	
  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Button butt;
		for(int i=0; i<size; i++) { // строки
			for(int j=0; j<size; j++) { // столбцы
				butt = (Button) findViewById((i*size+j+1)*10);
				((ViewManager)butt.getParent()).removeView(butt);
			}
		}
		// Получение параметров
		settings = PreferenceManager.getDefaultSharedPreferences(this);
		size = Integer.parseInt(settings.getString(SAVED_SIZE, "4"));
		count = Integer.parseInt(settings.getString(SAVED_COUNT, "5"));
		time = Integer.parseInt(settings.getString(SAVED_TIME, "3"))*1000;
		vibro = settings.getBoolean(SAVED_VIBRO, true);
		
		// ---------------------
		startGame(); // Начинаем игру    
  }
	
	private void startGame() {
		fieldStrings = new LinearLayout[size];
		buttonsGrid = new Button[size*size];
		Button butt;
		int pixels = metrics.widthPixels/size;
		// Инициализация поля
		for(int i=0; i<size; i++) { // строки
			fieldStrings[i] = new LinearLayout(this);
			fieldStrings[i].setOrientation(LinearLayout.HORIZONTAL);
			fieldStrings[i].setId(i*10000);
			fieldStrings[i].setGravity(Gravity.CENTER);
			mainLayout.addView(fieldStrings[i]);
			for(int j=0; j<size; j++) { // столбцы
				buttonsGrid[i+j] = new Button(this);
				buttonsGrid[i+j].setId((i*size+j+1)*10);
				fieldStrings[i].addView(buttonsGrid[i+j]);
				butt = (Button) findViewById((i*size+j+1)*10);
				butt.getLayoutParams().width = pixels;
				butt.getLayoutParams().height = pixels;
				butt.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.button_def));
				butt.setOnClickListener(gameClick);
				butt.setPadding(0, 0, 0, 0);
				butt.setTextSize((float) (pixels/size*0.8));
				//butt.setTextColor(Color.parseColor("#000000"));
			}
		}
		newSession();
	}
	
	private void newSession() {
		Button butt;
		int temp;
		
		scores = 0;
		errors = 0;
		now = 1;
		gametime = 0;
		updateInfo();
		RestoreAll();
		canClick = false;
		refreshed++;
		
		if(!canceled) {
			gametimer_1.cancel();
			canceled = true;
		}
		
		// Выводим параметры
		String t = Integer.toString(time/1000);
		String t_text;
		if(time/1000>=10) {
			if(time/1000%10 == 1) t_text = getResources().getString(R.string.sec1);
			else if(time/1000%10 < 5 && time/1000%10>1) t_text = getResources().getString(R.string.sec2);
			else t_text = getResources().getString(R.string.sec3);
		} else if(time/1000 <= 1) t_text = getResources().getString(R.string.sec1);
			   else if(time/1000 < 5) t_text = getResources().getString(R.string.sec2);
			   else t_text = getResources().getString(R.string.sec3);
		updateParams(t+" "+t_text+"\n "+getResources().getString(R.string.time_info)+" - 0 "+getResources().getString(R.string.s));
		// ---------------------
		
		coord = getRandomArray(count);
		for(int i = 0; i<size; i++) {
			for(int j = 0; j<size; j++) {
				butt = (Button) findViewById((i*size+j+1)*10);
				if(inArray(i*size+j,coord[1])) {
					temp = coord[0][findNumber(i*size+j,coord[1])];
					butt.setTag(temp);
					butt.setText(Integer.toString(temp));
				} else {
					butt.setTag(0);
				}
			}
		}
		Timer();
	}
	
	private void showRateMe() {
		final Dialog dialog = new Dialog(MainActivity.this);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.rateme);

		Button rem = (Button) dialog.findViewById(R.id.remember);
		// if button is clicked, close the custom dialog
		rem.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
			    SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
			    Editor ed = sPref.edit();
			    ed.putInt(RATING, 5);
			    ed.commit();
				dialog.dismiss();
			}
		});
		
		Button no = (Button) dialog.findViewById(R.id.no);
		// if button is clicked, close the custom dialog
		no.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
			    SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
			    Editor ed = sPref.edit();
			    ed.putInt(RATING, -1);
			    ed.commit();
				dialog.dismiss();
			}
		});
		
		RatingBar rate = (RatingBar) dialog.findViewById(R.id.ratingBar);
		rate.setOnRatingBarChangeListener(new OnRatingBarChangeListener() {

			@Override
			public void onRatingChanged(RatingBar ratingBar, float rating,
					boolean fromUser) {
				if(rating >= 4) {
					final String appPackageName = getPackageName();
					try {
					    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
					} catch (android.content.ActivityNotFoundException anfe) {
					    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
					}
				    SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
				    Editor ed = sPref.edit();
				    ed.putInt(RATING, -1);
				    ed.commit();
				} else {
				    SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
				    Editor ed = sPref.edit();
				    ed.putInt(RATING, -1);
				    ed.commit();				
				}		
				dialog.dismiss();
			}
			
		});
		
		dialog.show();		
	}
	
	private  void showRules() {
		final Dialog dialog = new Dialog(MainActivity.this);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.rulesdialog);
		dialog.setTitle(R.string.rules_title);

		Button okay = (Button) dialog.findViewById(R.id.okay_button);
		// if button is clicked, close the custom dialog
		okay.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
			    SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
			    Editor ed = sPref.edit();
			    ed.putBoolean(RULES_SHOWED, true);
			    ed.commit();
				dialog.dismiss();
			}
		});

		dialog.show();
	}
	
	private void victory() {
		canClick = false;
		int myscores = (int) count*10-errors*10;	
		int maximum = count*10;
		int time_bonus = (-(gametime/1000-count*2));
		int gametime_bonus = (count-time/1000);
		int count_bonus = count-size;
		if(count_bonus<0) count_bonus = 0;
		int result = myscores+time_bonus+gametime_bonus+count_bonus;
		if(myscores < 0) myscores = 0;
		
		final Dialog dialog = new Dialog(MainActivity.this);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.windialog);

		// set the custom dialog components - text, image and button
		TextView msc = (TextView) dialog.findViewById(R.id.myscores);
		msc.setText(Integer.toString(myscores));
		TextView max = (TextView) dialog.findViewById(R.id.maximum);
		max.setText(Integer.toString(maximum));
		TextView tm = (TextView) dialog.findViewById(R.id.timebonus);
		tm.setText(Integer.toString(time_bonus));
		TextView gtm = (TextView) dialog.findViewById(R.id.gtimebonus);
		gtm.setText(Integer.toString(gametime_bonus));
		TextView cnt = (TextView) dialog.findViewById(R.id.countbonus);
		cnt.setText(Integer.toString(count_bonus));
		TextView res = (TextView) dialog.findViewById(R.id.result);
		res.setText(Integer.toString(result));

		Button yes = (Button) dialog.findViewById(R.id.yes);
		// if button is clicked, close the custom dialog
		yes.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				newSession();
				dialog.dismiss();
			}
		});

		dialog.show();
	}
	
	private void RestoreAll() {
		Button butt;
		for(int i=0; i<size; i++) { // строки
			for(int j=0; j<size; j++) { // столбцы
				butt = (Button) findViewById((i*size+j+1)*10);
				butt.setText("");
				butt.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.button_def));
			}
		}
	}
	
	private void updateInfo() {
		scoresView.setText(Integer.toString(scores));
		errorsView.setText(Integer.toString(errors));
	}
	
	private void updateParams(String s) {
		infoTextView.setText(s);
	}
	
	private int[][] getRandomArray(int n) { // n - количество занятых полей
		if(size*size==n) {
			int[][] res = new int[2][n];
			for(int i = 0; i<count; i++) res[0][i] = i+1;
			mix(res[0]);			
			for(int i = 0; i<count; i++) res[1][i] = i;
			
			return res;
		} else if(n>size*size/2) {
			int[][] res = new int[2][n];
			for(int i = 0; i<count; i++) res[0][i] = i+1;
			mix(res[0]);
			n = size*size-count;
			Random rnd = new Random();
			int temp;
			int[] res_1 = new int[n];
			for(int i = 0; i<n; i++) {
				temp = rnd.nextInt(size*size-1);
				while(inArray(temp,res_1))
					temp = rnd.nextInt(size*size-1);
				res_1[i] = temp;
			}
			n = count;
			int j = 0;
			for(int i = 0; i<size*size; i++) {
				if(!inArray(i,res_1)) {
					res[1][j] = i;
					j++;
				}
			}
			return res;
		} else {
			int[][] res = new int[2][n];
			for(int i = 0; i<count; i++) res[0][i] = i+1;
			mix(res[0]);
			Random rnd = new Random();
			int temp;
			for(int i = 0; i<count; i++) {
				temp = rnd.nextInt(size*size-1);
				while(inArray(temp,res[1]))
					temp = rnd.nextInt(size*size-1);
				res[1][i] = temp;
			}
			return res;
		}
	}
	
	private void mix(int[] a) { // перемешать рандомно массив
        Random rnd = new Random();
        for (int i = 1; i < a.length; i++) {            
            int j = rnd.nextInt(i);
            int temp = a[i];
            a[i] = a[j];
            a[j] = temp;
        }
    }
	
	private boolean inArray(int n, int[] a) {
		for(int i = 0; i<a.length; i++)
			if(n == a[i]) return true;
		return false;
	}
	
	private int findNumber(int n, int[] a) {
		for(int i = 0; i<a.length; i++)
			if(a[i] == n) return i;
		return -1;
	}
	
	private Timer GameTimer() {
        Timer gtime = new Timer();
        gtime.schedule(new TimerTask() {
        
		String t = Integer.toString(time/1000);
		String t_text;

        @Override
        public void run() {
            MainActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                	if(gametime==0) getTime();
                	if(gametime/1000 > 300) {
                		updateParams(t+" "+t_text+"\n"+R.string.time_info+" - ∞");
                		canceled = true;
                		cancel();
                	} else {
	                	canceled = false;
	                	updateParams(t+" "+t_text+"\n"+getResources().getString(R.string.time_info)+" - "+(float)gametime/1000+" "+getResources().getString(R.string.s));
	                	gametime+=100;
                	}
                }
            });
        }
        
        public void getTime() {
    		if(time/1000>=10) {
    			if(time/1000%10 == 1) t_text = getResources().getString(R.string.sec1);
    			else if(time/1000%10 < 5 && time/1000%10>1) t_text = getResources().getString(R.string.sec2);
    			else t_text = getResources().getString(R.string.sec3);
    		} else if(time/1000 <= 1) t_text = getResources().getString(R.string.sec1);
    			   else if(time/1000 < 5) t_text = getResources().getString(R.string.sec2);
    			   else t_text = getResources().getString(R.string.sec3);
        }
        }, 0, 100);	
        
        return gtime;
	}
	
	private void Timer() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
        int iter = 0;
        int refreshed_local = refreshed;

        @Override
        public void run() {
            MainActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                	if(iter == 0) iter++;
                	else if(refreshed_local != refreshed) cancel();
                	else { 
                		RestoreAll();
                		canClick = true;
                		cancel();
                	}
                }
            });
        }
        }, 0, time);
    }
}
