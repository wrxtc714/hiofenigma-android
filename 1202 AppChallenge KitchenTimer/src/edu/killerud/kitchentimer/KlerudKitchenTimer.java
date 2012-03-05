package edu.killerud.kitchentimer;

import java.util.ArrayList;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;

import com.quietlycoding.android.picker.NumberPicker;

/**
 * My take on the February App Challenge at Enigma, HIOF. The app has the option
 * of creating several separate timers, and a timer that reaches zero wakes up
 * the phone (if necessary), shows the user a notification, and sounds the
 * alarm.
 * 
 * Uses touch for starting and stopping the timers, rather than a separate
 * button.
 * 
 * @author William Killerud
 * 
 */
public class KlerudKitchenTimer extends Activity
{
	private LinearLayout mLlContentLayout;
	private LinearLayout mLlTimePicker;
	private static NumberPicker npHours;
	private static NumberPicker npMinutes;
	private static NumberPicker npSeconds;

	private ArrayList<TimerView> mTimerViews;

	private int mNumberOfTimers;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		mLlContentLayout = (LinearLayout) findViewById(R.id.llContentLayout);
		mLlTimePicker = (LinearLayout) findViewById(R.id.llTimePicker);
		mTimerViews = new ArrayList<TimerView>();

		/* Sets up three TimePicker widgets for time input */
		setupPickers();
		/* Sets up the add- and remove timer buttons */
		Button bAddTimer = (Button) findViewById(R.id.bAddTimer);
		Button bRemoveTimer = (Button) findViewById(R.id.bRemoveTimer);

		bAddTimer.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				addTimer();
			}

		});
		bRemoveTimer.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				removeTimer();
			}

		});

	}

	@Override
	public void onResume()
	{
		super.onResume();
		registerBroadcastReceiver();

		Intent getTimers = new Intent(getApplicationContext(),
				edu.killerud.kitchentimer.CountdownService.class);
		getTimers.setAction("GET_TIMERS");
		startService(getTimers);
	}

	@Override
	public void onPause()
	{
		super.onPause();
		unregisterReceiver(broadcastReceiver);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		case R.id.stopAll:
			Intent stopTimer;
			for (int i = 0; i < mTimerViews.size(); i++)
			{
				stopTimer = new Intent(getApplicationContext(),
						edu.killerud.kitchentimer.CountdownService.class);
				stopTimer.setAction("STOP_TIMER");
				stopTimer.putExtra("TIMER_ID", i);
				startService(stopTimer);
			}
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	protected void registerBroadcastReceiver()
	{
		IntentFilter ifilter = new IntentFilter();
		ifilter.addAction("TIMER_ADDED");
		ifilter.addAction("TIMER_REMOVED");
		ifilter.addAction("TIMER_STOPPED");
		ifilter.addAction("TIMER_ALARM_STOPPED");
		ifilter.addAction("TIMER_STARTED");
		ifilter.addAction("NUMBER_OF_TIMERS");
		ifilter.addAction("TIMER_TICK");
		ifilter.addAction("ALARM_SOUNDING");
		registerReceiver(broadcastReceiver, new IntentFilter(ifilter));
	}

	protected void addTimer()
	{
		Intent addTimer = new Intent(getApplicationContext(),
				edu.killerud.kitchentimer.CountdownService.class);
		addTimer.setAction("ADD_TIMER");
		addTimer.putExtra("TIMER_ID", mNumberOfTimers);

		startService(addTimer);
	}

	protected void addTimerView()
	{
		mTimerViews.add(new TimerView(getApplicationContext(), mTimerViews
				.size()));
		mLlContentLayout.addView(mTimerViews.get(mTimerViews.size() - 1)
				.getTimerLayout());
	}

	protected void removeTimer()
	{
		if (mTimerViews.size() > 0)
		{
			Intent removeTimer = new Intent(getApplicationContext(),
					edu.killerud.kitchentimer.CountdownService.class);
			removeTimer.setAction("REMOVE_TIMER");
			removeTimer.putExtra("TIMER_ID", mTimerViews.size() - 1);
			startService(removeTimer);
		}
	}

	protected void removeTimerView()
	{
		mLlContentLayout.removeView(mTimerViews.get(mTimerViews.size() - 1)
				.getTimerLayout());
		mTimerViews.remove(mTimerViews.size() - 1);
	}

	/* Sets up the TimePicker widgets */
	protected void setupPickers()
	{
		npHours = new NumberPicker(getApplicationContext());
		npMinutes = new NumberPicker(getApplicationContext());
		npSeconds = new NumberPicker(getApplicationContext());

		npHours.setFormatter(NumberPicker.TWO_DIGIT_FORMATTER);
		npMinutes.setFormatter(NumberPicker.TWO_DIGIT_FORMATTER);
		npSeconds.setFormatter(NumberPicker.TWO_DIGIT_FORMATTER);

		npHours.setCurrent(0);
		npMinutes.setCurrent(1);
		npSeconds.setCurrent(0);

		npHours.setRange(0, 24);
		npMinutes.setRange(0, 59);
		npSeconds.setRange(0, 59);

		mLlTimePicker.addView(npHours);
		mLlTimePicker.addView(npMinutes);
		mLlTimePicker.addView(npSeconds);
	}

	private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver()
	{

		@Override
		public void onReceive(Context context, Intent intent)
		{
			if (intent == null)
			{
				return;
			}

			if (intent.getAction().equals("TIMER_TICK"))
			{
				Log.i("TimerActivity", "Tick");
				try
				{
					mTimerViews.get(intent.getIntExtra("TIMER_ID", -1))
							.updateTick(intent.getLongExtra("TIME_LEFT", 0l));
				} catch (IndexOutOfBoundsException e)
				{
					// Something went wrong elsewhere
				}

			} else if (intent.getAction().equals("TIMER_REMOVED"))
			{
				removeTimerView();
			} else if (intent.getAction().equals("TIMER_STOPPED"))
			{
				try
				{
					mTimerViews.get(intent.getIntExtra("TIMER_ID", -1))
							.resetUI();
				} catch (IndexOutOfBoundsException e)
				{
					// Something went wrong elsewhere
				}

			} else if (intent.getAction().equals("ALARM_SOUNDING"))
			{
				try
				{
					mTimerViews.get(intent.getIntExtra("TIMER_ID", -1))
							.resetUI();
				} catch (IndexOutOfBoundsException e)
				{
					// Something went wrong elsewhere
				}
			} else if (intent.getAction().equals("TIMER_ALARM_STOPPED"))

			{
				try
				{
					mTimerViews.get(intent.getIntExtra("TIMER_ID", -1))
							.resetUI();
				} catch (IndexOutOfBoundsException e)
				{
					// Something went wrong elsewhere
				}
			} else if (intent.getAction().equals("TIMER_ADDED"))
			{
				addTimerView();
			} else if (intent.getAction().equals("NUMBER_OF_TIMERS"))
			{
				if (intent.getIntExtra("NUMBER_OF_TIMERS", 0) == mTimerViews
						.size())
				{
					return;
				}
				mTimerViews = new ArrayList<TimerView>();
				for (int i = 0; i < intent.getIntExtra("NUMBER_OF_TIMERS", 0); i++)
				{
					addTimerView();
				}
			}

		}

	};

	public static int getHours()
	{
		return npHours.getCurrent();
	}

	public static int getMinutes()
	{
		return npMinutes.getCurrent();
	}

	public static int getSeconds()
	{
		return npSeconds.getCurrent();
	}
}