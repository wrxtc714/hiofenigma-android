/**
 *   Copyright William Killerud 2012
 *   
 *   This file is part of OpenDice.
 *
 *   OpenDice is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   any later version.
 *
 *   OpenDice is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with OpenDice.  If not, see <http://www.gnu.org/licenses/>.
 *   
 *   For questions contact William Killerud at william@killerud.com
 * 
 */

package edu.killerud.diceroll;

import java.util.ArrayList;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.graphics.PointF;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class Main extends Activity implements SensorEventListener
{
	private SensorManager mSensorManager;
	private Sensor mAccelerometer;
	private PowerManager mPowerManager;
	private WakeLock mWakeLock;

	private long mLastSensorEvent;

	private ArrayList<Die> mDice;

	private LinearLayout mAppWindow;
	private final int mTextSize = 50;
	private static MediaPlayer mMediaPlayer;

	private DieType mDieType = DieType.SIXFACED;

	private float mCurrentAccelleration;
	private PointF mPointOfImpact;
	private float mLastAccelleration;
	private float mAccelleration;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		Toast.makeText(getApplicationContext(), R.string.instructions,
				Toast.LENGTH_SHORT).show();

		/*
		 * This line allows us to control the Music volume form our app even if
		 * there is no music playing (in other words, outside of the two-second
		 * window when our dice roll sound plays!)
		 */
		setVolumeControlStream(AudioManager.STREAM_MUSIC);

		/* Sets up stuff we need */
		mLastSensorEvent = System.currentTimeMillis();
		mCurrentAccelleration = SensorManager.GRAVITY_EARTH;
		mAccelleration = 0.00f;
		mLastAccelleration = 0.00f;
		mAppWindow = (LinearLayout) findViewById(R.id.llDice);
		mDice = new ArrayList<Die>();
		mDieType = DieType.SIXFACED;
		addDie(this, mDieType);

		/*
		 * Sets up the navigation bar spinner. See also the private class
		 * ActionBarNavigationListener at the bottom of this class.
		 */
		ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

		/*
		 * Populates the spinner with the data from the array stored in
		 * res/values/arrays.xml with the ID DieTypes, with the layout
		 * simple_spinner_item. Also sets up a listener to make changes when the
		 * user selects something new
		 */
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				this, R.array.DieTypes, android.R.layout.simple_spinner_item);
		actionBar.setListNavigationCallbacks(adapter,
				new ActionBarNavigationListener());

		/* Sets the sixfaced die as default, as it is by far the most common */
		actionBar.setSelectedNavigationItem(3);

		/*
		 * Sets up the roll sound. Sound by Mike Koenig
		 * http://soundbible.com/182-Shake-And-Roll-Dice.html
		 */
		mMediaPlayer = MediaPlayer.create(getApplicationContext(),
				R.raw.dice_roll);

		/* Find the OS sensor manager, and the accelerometer sensor */
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		mAccelerometer = mSensorManager
				.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

		/*
		 * Find the power manager and get a wake lock to stop the screen from
		 * going black between dice rolls.
		 */
		mPowerManager = (PowerManager) getSystemService(POWER_SERVICE);
		mWakeLock = mPowerManager.newWakeLock(
				PowerManager.SCREEN_BRIGHT_WAKE_LOCK, getClass().getName());

		/*
		 * Sets up a touch listener, so the user can drag his finger across the
		 * screen to roll the dice
		 */
		mAppWindow.setOnTouchListener(new OnTouchListener()
		{

			public boolean onTouch(View view, MotionEvent event)
			{
				switch (event.getAction())
				{
				case MotionEvent.ACTION_DOWN:
					mPointOfImpact = new PointF(event.getX(), event.getY());
				case MotionEvent.ACTION_MOVE:
					if ((mLastSensorEvent + 2000) < System.currentTimeMillis())
					{
						final int dx = (int) (event.getX() - mPointOfImpact.x);
						final int dy = (int) (event.getY() - mPointOfImpact.y);
						if (dy > 5 || dx > 5)
						{
							mLastSensorEvent = System.currentTimeMillis();
							rollTheDice();
						}
					}
				}
				return true;
			}

		});
	}

	private void addDie(Context context, DieType type)
	{
		mDice.add(new Die(type));

		/* Styles the die output and adds it to the GUI */
		final TextView dieView = new TextView(context);
		dieView.setMinimumWidth(50);
		dieView.setText("Shake me!");
		dieView.setGravity(Gravity.CENTER);
		dieView.setTextSize(mTextSize);
		dieView.setBackgroundResource(R.drawable.status_border_white_slim);
		dieView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));

		/*
		 * Sets up the onClickListener so the user can save dice between rolls,
		 * for games like Yatzee.
		 */
		dieView.setOnClickListener(new OnClickListener()

		{

			public void onClick(View view)
			{

				Die dice = mDice.get(((ViewGroup) view.getParent())
						.indexOfChild(view));
				if (!dice.isSaved())
				{
					dice.save();
					dieView.setBackgroundResource(R.drawable.status_border_grey_slim);
				} else
				{
					dice.discard();
					dieView.setBackgroundResource(R.drawable.status_border_white_slim);
				}
			}

		});
		mAppWindow.addView(dieView);
	}

	private void removeDie()
	{
		if (mDice.size() > 0)
		{
			mAppWindow.removeViewAt(mDice.size() - 1);
			mDice.remove(mDice.size() - 1);
		}
	}

	@Override
	public void onResume()
	{
		super.onResume();
		/* Resume sensor sensing and wake-lock waking */
		mSensorManager.registerListener(this, mAccelerometer,
				SensorManager.SENSOR_DELAY_NORMAL);
		mWakeLock.acquire();
	}

	@Override
	public void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		outState.putInt("NUMBER_OF_DICE", mDice.size());
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState)
	{
		super.onRestoreInstanceState(savedInstanceState);
		restoreDice(savedInstanceState.getInt("NUMBER_OF_DICE", 1));
	}

	/* Restores the correct number of dice from the saved state */
	private void restoreDice(int numberOfDice)
	{
		if (mDice.size() < numberOfDice)
		{
			for (int i = mDice.size(); i < numberOfDice; i++)
			{
				addDie(getApplicationContext(), mDieType);
			}
		} else if (mDice.size() > numberOfDice)
		{
			for (int i = mDice.size(); i >= 0; i--)
			{
				removeDie();
			}
		}
	}

	@Override
	public void onPause()
	{
		super.onPause();
		/*
		 * From the official docs: "Always make sure to disable sensors you
		 * don't need, especially when your activity is paused. Failing to do so
		 * can drain the battery in just a few hours. Note that the system will
		 * not disable sensors automatically when the screen turns off."
		 */
		mSensorManager.unregisterListener(this);

		/*
		 * Release the wake-lock so that the screen can be turned off again
		 * automatically.
		 */
		mWakeLock.release();
	}

	public void onSensorChanged(SensorEvent event)
	{
		/*
		 * We want the user to be able to see the result before it disappears in
		 * a new "throw". We therefore ignore all events until two seconds have
		 * passed (the rolling sound stops).
		 */
		if ((mLastSensorEvent + 2000) < System.currentTimeMillis())
		{
			mLastSensorEvent = System.currentTimeMillis();

			if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
			{
				if (deviceWasShaken(event))
				{
					rollTheDice();
				}
			}
		}
	}

	private void rollTheDice()
	{
		for (int i = 0; i < mDice.size(); i++)
		{
			((TextView) mAppWindow.getChildAt(i)).setText(""
					+ mDice.get(i).roll());

			/* Animate the die text */
			Animation wobble = AnimationUtils.loadAnimation(
					getApplicationContext(), R.animator.wobble);
			((TextView) mAppWindow.getChildAt(i)).setAnimation(wobble);
			((TextView) mAppWindow.getChildAt(i)).startAnimation(wobble);
		}
	}

	private boolean deviceWasShaken(SensorEvent event)
	{
		// Not stirred

		/* Get the coordinate values from the event */
		float[] values = event.values;
		float x = values[0];
		float y = values[1];
		float z = values[2];

		mLastAccelleration = mCurrentAccelleration;
		mCurrentAccelleration = (float) Math.sqrt((x * x + y * y + z * z));
		float delta = mCurrentAccelleration - mLastAccelleration;
		mAccelleration = mAccelleration * 0.9f + delta;

		/* If the sensor has registered movement, return true. */
		if (delta > 2f)
		{
			return true;
		} else
		{
			return false;
		}
	}

	/* Required method, not needed this time */
	public void onAccuracyChanged(Sensor sensor, int accuracy)
	{
	}

	public static void playRollSound()
	{
		mMediaPlayer.start();
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
		case R.id.roll:
			rollTheDice();
			return true;
		case R.id.add:
			addDie(getApplicationContext(), mDieType);
			return true;
		case R.id.remove:
			removeDie();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/*
	 * This private class handles the use of the view (in our case, die type)
	 * changer in the Action Bar, allowing the user to choose from the list of
	 * different die types.
	 */
	private class ActionBarNavigationListener implements
			ActionBar.OnNavigationListener
	{

		public boolean onNavigationItemSelected(int itemPosition, long itemId)
		{
			int numberOfDice = mDice.size();
			mAppWindow.removeAllViews();
			mDice = new ArrayList<Die>();
			mDieType = DieType.values()[itemPosition];
			for (int i = 0; i < numberOfDice; i++)
			{
				addDie(getApplicationContext(), mDieType);
				Log.i("OpenDice", "Type of die now " + mDieType);
			}
			return true;
		}
	}

}
