package edu.killerud.kitchentimer;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class TimerView
{
	private final LinearLayout mLlTimerLayout;

	private int mTextSize;

	private TextView mTvHours;
	private TextView mTvMinutes;
	private TextView mTvSeconds;
	private TextView mTvHourMinuteSeparator;
	private TextView mTvMinuteSecondSeparator;

	private static Context mContext;
	private int mTimerViewId;

	boolean isCounting;
	boolean isSounding;

	public TimerView(Context context, int timerViewId)
	{
		mTextSize = 50;
		mContext = context;

		mTimerViewId = timerViewId;

		mLlTimerLayout = new LinearLayout(mContext);
		mLlTimerLayout.setClickable(true);
		mLlTimerLayout.setGravity(android.view.Gravity.CENTER);

		setupLayouts();

		/*
		 * The shortClickListener. Here we start the timer and set the alarm, as
		 * well as stop a sounding alarm.
		 */
		mLlTimerLayout.setOnClickListener(new OnClickListener()
		{

			public synchronized void onClick(View v)
			{
				if (isCounting)
				{
					Toast.makeText(mContext,
							"To stop the timer touch and hold.",
							Toast.LENGTH_SHORT).show();
				} else if (isSounding)
				{
					/*
					 * Here we release the wake lock we acquired further down in
					 * the code, in KitchenCountDownTimer.onFinish(). We also
					 * stop the annoying alarm. Then we reset the UI.
					 */

					Intent stopAlarm = new Intent(mContext,
							edu.killerud.kitchentimer.CountdownService.class);
					stopAlarm.setAction("STOP_ALARM");
					stopAlarm.putExtra("TIMER_ID", mTimerViewId);
					mContext.startService(stopAlarm);

					mTvHours.setTextColor(mContext.getResources().getColor(
							R.color.white));
					mTvMinutes.setTextColor(mContext.getResources().getColor(
							R.color.white));
					mTvSeconds.setTextColor(mContext.getResources().getColor(
							R.color.white));
					mTvHourMinuteSeparator.setTextColor(mContext.getResources()
							.getColor(R.color.white));
					mTvMinuteSecondSeparator.setTextColor(mContext
							.getResources().getColor(R.color.white));
				} else if (!isCounting && !isSounding)
				{

					/* Set a new alarm and start counting down! */
					Integer hours = KlerudKitchenTimer.getHours();
					Integer minutes = KlerudKitchenTimer.getMinutes();
					Integer seconds = KlerudKitchenTimer.getSeconds();

					Long millisInFuture = (long) ((seconds * 1000)
							+ (minutes * 60 * 1000) + (hours * 60 * 60 * 1000));

					if (millisInFuture < 1000)
					{
						Toast.makeText(
								mContext,
								"Please enter a higher number. Surely you can count to one yourself?",
								Toast.LENGTH_SHORT).show();
						return;
					}

					mTvHours.setTextColor(mContext.getResources().getColor(
							R.color.white));
					mTvMinutes.setTextColor(mContext.getResources().getColor(
							R.color.white));
					mTvSeconds.setTextColor(mContext.getResources().getColor(
							R.color.white));
					mTvHourMinuteSeparator.setTextColor(mContext.getResources()
							.getColor(R.color.white));
					mTvMinuteSecondSeparator.setTextColor(mContext
							.getResources().getColor(R.color.white));

					Intent stopAlarm = new Intent(mContext,
							edu.killerud.kitchentimer.CountdownService.class);
					stopAlarm.setAction("START_TIMER");
					stopAlarm.putExtra("TIMER_ID", mTimerViewId);
					stopAlarm.putExtra("MILLISINFUTURE", millisInFuture);
					mContext.startService(stopAlarm);

				}

			}

		});

		/*
		 * The longClickListener. Here we stop a countdown and alarm while it is
		 * in progress (hasn't sounded yet).
		 */
		mLlTimerLayout
				.setOnLongClickListener(new android.view.View.OnLongClickListener()
				{

					public synchronized boolean onLongClick(View v)
					{
						if (isCounting)
						{
							try
							{

								/* Reset the UI */
								resetUI();

								mTvHourMinuteSeparator
										.setTextColor(mContext.getResources()
												.getColor(R.color.white));
								mTvMinuteSecondSeparator
										.setTextColor(mContext.getResources()
												.getColor(R.color.white));

								Intent stopTimer = new Intent(
										mContext,
										edu.killerud.kitchentimer.CountdownService.class);
								stopTimer.setAction("STOP_TIMER");
								stopTimer.putExtra("TIMER_ID", mTimerViewId);
								mContext.startService(stopTimer);

							} catch (Exception e)
							{

							}
						}
						return true;

					}
				});
	}

	/* Sets up the Timer UI */
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

		resetUI();

		mLlTimerLayout.addView(mTvHours);
		mLlTimerLayout.addView(mTvHourMinuteSeparator);
		mLlTimerLayout.addView(mTvMinutes);
		mLlTimerLayout.addView(mTvMinuteSecondSeparator);
		mLlTimerLayout.addView(mTvSeconds);

		mLlTimerLayout
				.setBackgroundResource(R.drawable.status_border_grey_slim);
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

	public void updateTick(long timeLeft)
	{
		Long hours = timeLeft / (60 * 60 * 1000);
		Long minutes = (timeLeft / (60 * 1000)) - (hours * 60 * 60 * 1000);
		Long seconds = timeLeft / 1000 - (hours * 60 * 60 * 1000)
				- (minutes * 60 * 1000);
		mTvHours.setText((hours < 10) ? "0" + hours : "" + hours);
		mTvMinutes.setText((minutes < 10) ? "0" + minutes : "" + minutes);
		mTvSeconds.setText((seconds < 10) ? "0" + seconds : "" + seconds);
	}

	/* Used by the UI for object reference */
	public LinearLayout getTimerLayout()
	{
		return mLlTimerLayout;
	}

	/*
	 * Used by the UI to remove the timer. Stops the countdown and releases
	 * resources.
	 */
	public void remove()
	{
		if (mLlTimerLayout != null)
		{
			mLlTimerLayout.removeAllViews();
		}
	}

	@Override
	public String toString()
	{
		return "TimerView";
	}

	public void setSounding()
	{
		isSounding = true;
		mTvHours.setTextColor(mContext.getResources().getColor(R.color.red));
		mTvMinutes.setTextColor(mContext.getResources().getColor(R.color.red));
		mTvSeconds.setTextColor(mContext.getResources().getColor(R.color.red));
		mTvHourMinuteSeparator.setTextColor(mContext.getResources().getColor(
				R.color.red));
		mTvMinuteSecondSeparator.setTextColor(mContext.getResources().getColor(
				R.color.red));

		mTvHourMinuteSeparator.setText(":");
		mTvMinuteSecondSeparator.setText(":");
		mTvHours.setText("00");
		mTvMinutes.setText("00");
		mTvSeconds.setText("00");

	}

}
