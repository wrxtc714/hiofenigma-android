package edu.killerud.kitchentimer;

import java.io.IOException;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.CountDownTimer;
import android.os.PowerManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class TimerView
{
	private boolean mIsCounting;
	private boolean mAlarmIsSounding;

	private final LinearLayout mLlTimerLayout;

	private final int mTextSize = 50;

	private TextView mTvHours;
	private TextView mTvMinutes;
	private TextView mTvSeconds;
	private TextView mTvHourMinuteSeparator;

	private final Context mContext;

	private KitchenCountdownTimer mTimer;

	private final TimerView mThisTimerViewObject;
	private TextView mTvMinuteSecondSeparator;

	private long mCurrentTime;

	protected MediaPlayer mMediaPlayer;
	public AudioManager mAudioManager;

	public PowerManager mPowerManager;
	protected PowerManager.WakeLock mWakeLock;

	private Intent mAlarmIntent;
	private Object mPendingAlarmIntent;
	private AlarmManager mAlarmManager;

	/*
	 * This is the big one, where the magic happens. Every timer in the UI is an
	 * instance of this class, and contains its own private CountDownTimer
	 * class, its own LinearLayout and child views, its own click-listeners, the
	 * whole deal.
	 */
	public TimerView(Context context)
	{
		mAlarmIsSounding = false;
		mIsCounting = false;

		mContext = context;
		mThisTimerViewObject = this;

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
				if (mTimer != null && mIsCounting)
				{
					Toast.makeText(mContext,
							"To stop the timer touch and hold.",
							Toast.LENGTH_SHORT).show();
				} else if (mAlarmIsSounding)
				{
					/*
					 * Here we release the wake lock we acquired further down in
					 * the code, in KitchenCountDownTimer.onFinish(). We also
					 * stop the annoying alarm. Then we reset the UI.
					 */
					mWakeLock.release();
					mMediaPlayer.stop();
					mAlarmIsSounding = false;

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
				} else if (!mIsCounting && !mAlarmIsSounding)
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

					mAlarmIntent = new Intent(mContext,
							KlerudKitchenTimer.class);
					mPendingAlarmIntent = PendingIntent.getBroadcast(mContext,
							1234, mAlarmIntent, 0);
					mAlarmManager = (AlarmManager) mContext
							.getSystemService(Context.ALARM_SERVICE);
					mAlarmManager.set(AlarmManager.RTC_WAKEUP, millisInFuture,
							(PendingIntent) mPendingAlarmIntent);

					mTimer = new KitchenCountdownTimer(mThisTimerViewObject,
							millisInFuture, 1000);
					mTimer.start();

					mIsCounting = true;
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
						if (mTimer != null && mIsCounting)
						{
							try
							{
								mAlarmManager
										.cancel((PendingIntent) mPendingAlarmIntent);
								mTimer.cancel();
								mIsCounting = false;
								mTimer = null;

								/* Reset the UI */
								resetTimers();

								mTvHourMinuteSeparator
										.setTextColor(mContext.getResources()
												.getColor(R.color.white));
								mTvMinuteSecondSeparator
										.setTextColor(mContext.getResources()
												.getColor(R.color.white));
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

		resetTimers();

		mLlTimerLayout.addView(mTvHours);
		mLlTimerLayout.addView(mTvHourMinuteSeparator);
		mLlTimerLayout.addView(mTvMinutes);
		mLlTimerLayout.addView(mTvMinuteSecondSeparator);
		mLlTimerLayout.addView(mTvSeconds);

		mLlTimerLayout
				.setBackgroundResource(R.drawable.status_border_grey_slim);
	}

	protected void resetTimers()
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

	/* Used by the UI for object reference */
	public LinearLayout getmLlTimerLayout()
	{
		return mLlTimerLayout;
	}

	/*
	 * Used by the UI to remove the timer. Stops the countdown and releases
	 * resources.
	 */
	public void remove()
	{
		if (mTimer != null)
		{
			mTimer.cancel();
			mTimer = null;
			mLlTimerLayout.removeAllViews();
		}
		if (mAlarmManager != null && mPendingAlarmIntent != null)
		{
			mAlarmManager.cancel((PendingIntent) mPendingAlarmIntent);
		}

	}

	public void stop()
	{
		if (mTimer != null)
		{
			mTimer.cancel();
			mTimer = null;
		}
		if (mAlarmManager != null && mPendingAlarmIntent != null)
		{
			mAlarmManager.cancel((PendingIntent) mPendingAlarmIntent);
		}
		resetTimers();
	}

	/* Our implementation of the CountDownTimer */
	private class KitchenCountdownTimer extends CountDownTimer
	{

		private final TimerView mParent;
		private final Context mContext;

		public KitchenCountdownTimer(TimerView parent, long millisInFuture,
				long countDownInterval)
		{
			super(millisInFuture, countDownInterval);
			mParent = parent;
			mContext = parent.mContext;

		}

		@Override
		public void onFinish()
		{
			/* Update the UI with a flare of urgency */
			mTvHours.setText("00");
			mTvMinutes.setText("00");
			mTvSeconds.setText("00");
			mTvHours.setTextColor(mContext.getResources().getColor(R.color.red));
			mTvMinutes.setTextColor(mContext.getResources().getColor(
					R.color.red));
			mTvSeconds.setTextColor(mContext.getResources().getColor(
					R.color.red));
			mTvHourMinuteSeparator.setTextColor(mContext.getResources()
					.getColor(R.color.red));
			mTvMinuteSecondSeparator.setTextColor(mContext.getResources()
					.getColor(R.color.red));
			mParent.mIsCounting = false;

			/*
			 * Sets up the sound the app plays for an alarm, using the default
			 * alarm for the particular phone, and some backups just in case.
			 */
			Uri alertSound = RingtoneManager
					.getDefaultUri(RingtoneManager.TYPE_ALARM);
			if (alertSound == null)
			{
				alertSound = RingtoneManager
						.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
				if (alertSound == null)
				{
					alertSound = RingtoneManager
							.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
				}
			}

			/* Sets up the MediaPlayer and points it to the sound. */
			mMediaPlayer = new MediaPlayer();
			try
			{
				mMediaPlayer.setDataSource(mContext, alertSound);
			} catch (IllegalArgumentException e1)
			{
				throw e1;
			} catch (SecurityException e1)
			{
				throw e1;
			} catch (IllegalStateException e1)
			{
				throw e1;
			} catch (IOException e1)
			{
				// Do nothing, as we vibrate as well
			}

			/*
			 * Finds the Audio Manager and checks the media volume. If it isn't
			 * zero (silent) we play the alarm.
			 */
			mAudioManager = (AudioManager) mContext
					.getSystemService(Context.AUDIO_SERVICE);
			if (mAudioManager.getStreamVolume(AudioManager.STREAM_ALARM) != 0)
			{
				mMediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
				/* Don't stop until the user has responded! */
				mMediaPlayer.setLooping(true);
				try
				{
					mMediaPlayer.prepare();
				} catch (IllegalStateException e)
				{
					throw e;
				} catch (IOException e)
				{
					// Do nothing, as we vibrate as well
				}
				mMediaPlayer.start();
			}
			mIsCounting = false;
			mAlarmIsSounding = true;

			/*
			 * The PowerManager and its flags make sure the phone screen lights
			 * up if it has been locked down. This is called
			 * AQUIRE_CAUSES_WAKEUP, which basically means that when an app
			 * acquires a Wake Lock like below (wl.acquire()) the phone wakes up
			 * from its slumber. The wakeup requires that the flag
			 * FULL_WAKE_LOCK is also set. The wakelock is released when the
			 * user stops the alarm. It is important that the lock is released,
			 * or else the screen won't turn off automatically!
			 */
			mPowerManager = (PowerManager) mContext
					.getSystemService(Context.POWER_SERVICE);
			mWakeLock = mPowerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK
					| PowerManager.ACQUIRE_CAUSES_WAKEUP
					| PowerManager.ON_AFTER_RELEASE, "Time is up");
			mWakeLock.acquire();

			/*
			 * This method is responsible for showing the notification that
			 * gives the user easy access to the KlerudKitchenTimer Activity
			 */
			showAppNotification();
		}

		@Override
		public void onTick(long millisUntillFinished)
		{
			/* Updates the user interface for every tick of the clock */
			mCurrentTime = millisUntillFinished;
			int hoursLeft = (int) (millisUntillFinished / 3600000);
			int minutesLeft = (int) (millisUntillFinished / 60000)
					- (hoursLeft * 60);
			int secondsLeft = (int) (millisUntillFinished / 1000)
					- (hoursLeft * 60 * 60) - (minutesLeft * 60);

			mParent.mTvHours.setText(""
					+ (hoursLeft > 9 ? hoursLeft : "0" + hoursLeft));
			mParent.mTvMinutes.setText(""
					+ (minutesLeft > 9 ? minutesLeft : "0" + minutesLeft));
			mParent.mTvSeconds.setText(""
					+ (secondsLeft > 9 ? secondsLeft : "0" + secondsLeft));
		}

	}

	void showAppNotification()
	{
		/* Creates a notification manager to show our notification */
		NotificationManager nm = (NotificationManager) mContext
				.getSystemService(android.app.Activity.NOTIFICATION_SERVICE);

		/* Creates the notification itself */
		Notification notification = new Notification(
				android.R.drawable.stat_sys_warning,
				"Time is up! Touch the timer to stop the alarm.",
				System.currentTimeMillis());

		Intent resumeActivity = new Intent(mContext,
				edu.killerud.kitchentimer.KlerudKitchenTimer.class);

		/*
		 * This line lets the user touch the notification to be taken to the
		 * KlerudKitchenTimer Activity. The flag FLAG_ACTIVITY_SINGLE_TOP makes
		 * sure that we don't start up a new instance of the activity, but
		 * rather go back to the one we started in the first place.
		 */
		resumeActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_SINGLE_TOP);

		notification.setLatestEventInfo(mContext, "Time is up!",
				"Time is up! Tap the timer to stop the alarm.",
				PendingIntent.getActivity(mContext, 0, resumeActivity, 0));

		/* Sets some of the notification properties */
		notification.defaults |= Notification.DEFAULT_VIBRATE;
		notification.defaults |= Notification.DEFAULT_LIGHTS;
		notification.defaults |= Notification.FLAG_INSISTENT;
		notification.defaults |= Notification.FLAG_AUTO_CANCEL;

		/* Shows the notification */
		nm.notify(1, notification);
	}

	public long getCurrentTime()
	{
		return mCurrentTime;
	}

}
