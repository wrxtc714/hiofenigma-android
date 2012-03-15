package edu.killerud.kitchentimer;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import coreygoldberg.StopWatch;

public class StopWatchView extends View
{

	private final LinearLayout mLlTimerLayout;
	private final Context mContext;
	private final OpenTimerService mService;
	private TextView mTvHours;
	private TextView mTvMinutes;
	private TextView mTvSeconds;
	private TextView mTvHourMinuteSeparator;
	private TextView mTvMinuteSecondSeparator;
	private float mTextSize;
	private StopWatch mStopWatch;

	public StopWatchView(Context context, OpenTimerService service)
	{
		super(context);
		mContext = context;
		mService = service;
		mStopWatch = new StopWatch();

		mLlTimerLayout = new LinearLayout(mContext);
		mLlTimerLayout.setClickable(true);
		mLlTimerLayout.setGravity(android.view.Gravity.CENTER);

		mLlTimerLayout.setOnClickListener(new OnClickListener()
		{

			public void onClick(View v)
			{
				if (mStopWatch.isRunning())
				{
					// TODO display a new line with the current time.

				} else
				{
					mStopWatch.start();
				}
			}

		});

		mLlTimerLayout
				.setOnLongClickListener(new android.view.View.OnLongClickListener()
				{
					public boolean onLongClick(View v)
					{
						if (mStopWatch.isRunning())
						{
							mStopWatch.stop();
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
	}

}
