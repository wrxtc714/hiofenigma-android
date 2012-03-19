package edu.killerud.kitchentimer;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import coreygoldberg.StopWatch;

public class StopWatchLayout
{

	private final LinearLayout mLlTimerLayout;
	private final Context mContext;

	private TextView mTvHours;
	private TextView mTvMinutes;
	private TextView mTvSeconds;
	private TextView mTvHourMinuteSeparator;
	private TextView mTvMinuteSecondSeparator;
	private final float mTextSize = 50;

	private StopWatchThread mStopWatchThread;

	public StopWatchLayout(Context context)
	{
		mContext = context;
		mStopWatchThread = new StopWatchThread(new StopWatch());
		mStopWatchThread.start();

		mLlTimerLayout = new LinearLayout(mContext);
		mLlTimerLayout.setClickable(true);
		mLlTimerLayout.setGravity(android.view.Gravity.CENTER);

		setupLayouts();

		mLlTimerLayout.setOnClickListener(new OnClickListener()
		{

			public void onClick(View v)
			{
				if (mStopWatchThread.isTiming())
				{
					storeAndDisplayTime(mStopWatchThread.getElapsedTime());

				} else
				{
					mStopWatchThread.beginStopWatch();
				}
			}

		});

		mLlTimerLayout
				.setOnLongClickListener(new android.view.View.OnLongClickListener()
				{
					public boolean onLongClick(View v)
					{
						if (mStopWatchThread.isTiming())
						{
							mStopWatchThread.resetStopWatch();
							resetUI();
						}
						return true;
					}
				});
	}

	/* Sets up the StopWatch UI */
	protected void setupLayouts()
	{

		mTvHours = new TextView(mLlTimerLayout.getContext());
		mTvMinutes = new TextView(mLlTimerLayout.getContext());
		mTvSeconds = new TextView(mLlTimerLayout.getContext());
		mTvHourMinuteSeparator = new TextView(mLlTimerLayout.getContext());
		mTvMinuteSecondSeparator = new TextView(mLlTimerLayout.getContext());

		mTvHours.setTextSize(mTextSize);
		mTvMinutes.setTextSize(mTextSize);
		mTvSeconds.setTextSize(mTextSize);
		mTvHourMinuteSeparator.setTextSize(mTextSize);
		mTvMinuteSecondSeparator.setTextSize(mTextSize);

		mLlTimerLayout.addView(mTvHours);
		mLlTimerLayout.addView(mTvHourMinuteSeparator);
		mLlTimerLayout.addView(mTvMinutes);
		mLlTimerLayout.addView(mTvMinuteSecondSeparator);
		mLlTimerLayout.addView(mTvSeconds);

		mLlTimerLayout
				.setBackgroundResource(R.drawable.status_border_grey_slim);

		resetUI();
	}

	protected void resetUI()
	{
		mTvHours.setTextColor(mContext.getResources().getColor(R.color.white));
		mTvMinutes
				.setTextColor(mContext.getResources().getColor(R.color.white));
		mTvSeconds
				.setTextColor(mContext.getResources().getColor(R.color.white));
		mTvHourMinuteSeparator.setTextColor(mContext.getResources().getColor(
				R.color.white));
		mTvMinuteSecondSeparator.setTextColor(mContext.getResources().getColor(
				R.color.white));

		mTvHourMinuteSeparator.setText(":");
		mTvMinuteSecondSeparator.setText(":");
		mTvHours.setText("00");
		mTvMinutes.setText("00");
		mTvSeconds.setText("00");
	}

	protected void updateTick(long elapsedTimeMillis)
	{
		Log.i("SWView", "Updatetick called with elapsed time "
				+ elapsedTimeMillis);

		int hours = (int) (elapsedTimeMillis / 3600000);
		int minutes = (int) (elapsedTimeMillis / 60000) - (hours * 60);
		int seconds = (int) (elapsedTimeMillis / 1000) - (hours * 60 * 60)
				- (minutes * 60);
		mTvHours.setText((hours < 10) ? "0" + hours : "" + hours);
		mTvMinutes.setText((minutes < 10) ? "0" + minutes : "" + minutes);
		mTvSeconds.setText((seconds < 10) ? "0" + seconds : "" + seconds);
	}

	protected void storeAndDisplayTime(long elapsedTimeMillis)
	{
		Log.i("StopWatch", "" + elapsedTimeMillis);
		// TODO need a TV for us to use here, and display a new line for each
		// call.
	}

	/* Used by the UI for object reference */
	public LinearLayout getLayout()
	{
		return mLlTimerLayout;
	}

	private class StopWatchThread extends Thread
	{
		private final StopWatch mStopWatch;
		private Intent mTick;

		public StopWatchThread(StopWatch stopWatch)
		{
			mStopWatch = stopWatch;
		}

		public void beginStopWatch()
		{
			mStopWatch.start();
		}

		public void resetStopWatch()
		{
			mStopWatch.reset();
		}

		public boolean isTiming()
		{
			return mStopWatch.isRunning();
		}

		public long getElapsedTime()
		{
			return mStopWatch.getElapsedTime();
		}

		@Override
		public void run()
		{
			while (true)
			{
				if (mStopWatch.isRunning()
						&& mStopWatch.getElapsedTime() % 1000 == 0)
				{
					mTick = new Intent();
					mTick.setAction("STOPWATCH_TICK");
					mTick.putExtra("elapsedTime", mStopWatch.getElapsedTime());
					mContext.sendBroadcast(mTick);
					Log.i("SWThread", "Tick broadcast");
				}
			}
		}

	}

}
