package edu.killerud.kitchentimer;

import java.util.ArrayList;

import android.app.Activity;
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
	private final ArrayList<TimerView> mTimers = new ArrayList<TimerView>();
	private LinearLayout mLlTimePicker;
	private static NumberPicker npHours;
	private static NumberPicker npMinutes;
	private static NumberPicker npSeconds;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		Log.i("KitchenTimer", "Trolololol");

		mLlContentLayout = (LinearLayout) findViewById(R.id.llContentLayout);
		mLlTimePicker = (LinearLayout) findViewById(R.id.llTimePicker);

		/* Sets up three TimePicker widgets for time input */
		setupPickers();
		/* Sets up the add- and remove timer buttons */
		Button bAddTimer = (Button) findViewById(R.id.bAddTimer);
		Button bRemoveTimer = (Button) findViewById(R.id.bRemoveTimer);
		bAddTimer.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				mTimers.add(new TimerView(getApplicationContext()));
				mLlContentLayout.addView(mTimers.get(mTimers.size() - 1)
						.getmLlTimerLayout(),
						android.view.ViewGroup.LayoutParams.FILL_PARENT,
						android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
				mLlContentLayout.setPadding(4, 4, 4, 4);
			}
		});
		bRemoveTimer.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				if (mTimers.size() <= 0)
				{
					return;
				}
				mLlContentLayout.removeView(mTimers.get(mTimers.size() - 1)
						.getmLlTimerLayout());
				/*
				 * Calls the timer's remove() to stop the alarm and the
				 * countdown, as well as free up resources.
				 */
				mTimers.get(mTimers.size() - 1).remove();
				mTimers.remove(mTimers.size() - 1);
			}
		});

		try
		{

			for (int i = 0; i < savedInstanceState.size(); i++)
			{
				Log.i("KitchenTimer", "Trying to restore timer " + i);
				try
				{
					if (savedInstanceState.getParcelable("timer" + i)
							.toString().equals("TimerView"))
					{
						Log.i("KitchenTimer", "Actually restoring timer " + i);
						mTimers.add((TimerView) savedInstanceState
								.getParcelable("timer" + i));
						mLlContentLayout.addView(mTimers
								.get(mTimers.size() - 1).getmLlTimerLayout());
					}
				} catch (NullPointerException npe)
				{
					Log.i("KitchenTimer", "Added " + (i + 1)
							+ " timers from state");
					break;
				}
			}

		} catch (NullPointerException npe)
		{
			Log.i("KitchenTimer", "NPE on state, defaulting");

			/*
			 * Adds a few timers so the user has something to look at right off
			 * the bat.
			 */
			mTimers.add(new TimerView(getApplicationContext()));
			mLlContentLayout.addView(mTimers.get(mTimers.size() - 1)
					.getmLlTimerLayout());
			mTimers.add(new TimerView(getApplicationContext()));
			mLlContentLayout.addView(mTimers.get(mTimers.size() - 1)
					.getmLlTimerLayout());
			mTimers.add(new TimerView(getApplicationContext()));
			mLlContentLayout.addView(mTimers.get(mTimers.size() - 1)
					.getmLlTimerLayout());

		}
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

	@Override
	public void onSaveInstanceState(Bundle bundle)
	{
		super.onSaveInstanceState(bundle);
		for (int i = 0; i < mTimers.size(); i++)
		{
			bundle.putParcelable("timer" + i, mTimers.get(i));
			Log.i("KitchenTimer", "Saving timer " + i);
		}
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
			for (int i = 0; i < mTimers.size(); i++)
			{
				mTimers.get(i).stop();
			}
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onPause()
	{
		super.onPause();

	}

	/* Used by TimerView the currently chosen number */
	public static Integer getHours()
	{
		return npHours.getCurrent();
	}

	/* Used by TimerView the currently chosen number */
	public static Integer getMinutes()
	{
		return npMinutes.getCurrent();
	}

	/* Used by TimerView the currently chosen number */
	public static Integer getSeconds()
	{
		return npSeconds.getCurrent();
	}
}