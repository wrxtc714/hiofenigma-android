package edu.killerud.kitchentimer;

import java.io.IOException;
import java.util.ArrayList;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.PowerManager;

public class CountdownService extends Service

{

	private ArrayList<Timer> mTimers;
	private long mMillisInFuture;

	@Override
	public void onCreate()
	{
		mTimers = new ArrayList<Timer>();
		mTimers.add(new Timer(0));
		mTimers.add(new Timer(1));
		mTimers.add(new Timer(2));
		mMillisInFuture = 0l;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startid)
	{
		if (intent == null || intent.getAction() == null)
		{
			return Service.START_STICKY;
		}
		if (intent.getAction().equals("ADD_TIMER"))
		{
			return addTimer();
		} else if (intent.getAction().equals("REMOVE_TIMER"))
		{
			return removeTimer(intent);
		} else if (intent.getAction().equals("STOP_TIMER"))
		{
			return stopTimer(intent);
		} else if (intent.getAction().equals("STOP_ALARM"))
		{
			return stopAlarm(intent);
		} else if (intent.getAction().equals("START_TIMER"))
		{
			return startTimer(intent);
		} else if (intent.getAction().equals("GET_TIMERS"))
		{
			return getNumberOfTimers();
		} else
		{
			return Service.START_STICKY;
		}

	}

	private int getNumberOfTimers()
	{
		Intent intent = new Intent();
		intent.setAction("NUMBER_OF_TIMERS");
		intent.putExtra("NUMBER_OF_TIMERS",
				mTimers.size() == 0 ? 3 : mTimers.size());
		sendBroadcast(intent);
		for (int i = 0; i < mTimers.size(); i++)
		{
			if (mTimers.get(i).isSounding)
			{
				Intent intentA = new Intent();
				intentA.setAction("ALARM_SOUNDING");
				intentA.putExtra("TIMER_ID", i);
				sendBroadcast(intentA);
			}
		}
		return Service.START_STICKY;
	}

	private int addTimer()
	{
		mTimers.add(new Timer(mTimers.size()));
		Intent added = new Intent();
		added.setAction("TIMER_ADDED");
		sendBroadcast(added);
		return Service.START_STICKY;
	}

	private int removeTimer(Intent intent)
	{
		try
		{
			mTimers.get(mTimers.size() - 1).stop();
			mTimers.remove(mTimers.size() - 1);
			Intent removed = new Intent();
			removed.setAction("TIMER_REMOVED");
			sendBroadcast(removed);
			return Service.START_STICKY;
		} catch (IndexOutOfBoundsException e)
		{
			return Service.START_STICKY;
		}

	}

	private int stopTimer(Intent intent)
	{
		try
		{
			mTimers.get(intent.getIntExtra("TIMER_ID", -1)).stop();
			Intent stopped = new Intent();
			stopped.setAction("TIMER_STOPPED");
			stopped.putExtra("TIMER_ID", intent.getIntExtra("TIMER_ID", -1));
			sendBroadcast(stopped);
			return Service.START_STICKY;
		} catch (IndexOutOfBoundsException e)
		{
			return Service.START_STICKY;
		}
	}

	private int stopAlarm(Intent intent)
	{
		try
		{
			mTimers.get(intent.getIntExtra("TIMER_ID", -1)).stopAlarm();
			Intent stopped = new Intent();
			stopped.setAction("TIMER_ALARM_STOPPED");
			stopped.putExtra("TIMER_ID", intent.getIntExtra("TIMER_ID", -1));
			sendBroadcast(stopped);
			if (allAreFinished())
			{
				return Service.START_NOT_STICKY;
			}
			return Service.START_STICKY;
		} catch (IndexOutOfBoundsException e)
		{
			return Service.START_STICKY;
		}
	}

	private boolean allAreFinished()
	{
		for (int i = 0; i < mTimers.size(); i++)
		{
			if (mTimers.get(i).isCounting)
			{
				return false;
			}
		}
		return true;
	}

	private int startTimer(Intent intent)
	{
		mMillisInFuture = intent.getLongExtra("MILLISINFUTURE", 0l);
		try
		{

			mTimers.get(intent.getIntExtra("TIMER_ID", -1)).startTimer(
					mMillisInFuture);
		} catch (IndexOutOfBoundsException e)
		{
			return Service.START_STICKY;
		}
		return Service.START_STICKY;
	}

	void showAppNotification()
	{
		/* Creates a notification manager to show our notification */
		NotificationManager nm = (NotificationManager) getSystemService(android.app.Activity.NOTIFICATION_SERVICE);

		/* Creates the notification itself */
		Notification notification = new Notification(
				android.R.drawable.stat_sys_warning,
				getString(R.string.notification), System.currentTimeMillis());

		Intent resumeActivity = new Intent(this, KlerudKitchenTimer.class);

		/*
		 * This line lets the user touch the notification to be taken to the
		 * KlerudKitchenTimer Activity. The flag FLAG_ACTIVITY_SINGLE_TOP makes
		 * sure that we don't start up a new instance of the activity, but
		 * rather go back to the one we started in the first place.
		 */
		resumeActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_SINGLE_TOP);

		notification.setLatestEventInfo(this,
				getString(R.string.notification_title),
				getString(R.string.notification),
				PendingIntent.getActivity(this, 0, resumeActivity, 0));

		/* Sets some of the notification properties */
		notification.defaults |= Notification.DEFAULT_VIBRATE;
		notification.defaults |= Notification.DEFAULT_LIGHTS;
		notification.defaults |= Notification.FLAG_INSISTENT;
		notification.defaults |= Notification.FLAG_AUTO_CANCEL;

		/* Shows the notification */
		nm.notify(1, notification);
	}

	@Override
	public IBinder onBind(Intent intent)
	{
		return null;
	}

	private class Timer
	{
		boolean isSounding;

		private KitchenCountdownTimer mTimer;

		protected MediaPlayer mMediaPlayer;
		AudioManager mAudioManager;

		PowerManager mPowerManager;
		protected PowerManager.WakeLock mWakeLock;

		private Object mPendingAlarmIntent;
		private AlarmManager mAlarmManager;

		private final int mTimerId;

		boolean isCounting;

		public Timer(int timerId)
		{

			mTimerId = timerId;
		}

		public synchronized void startTimer(long millisInFuture)
		{
			if (isCounting)
			{
				return;
			}
			Intent mAlarmIntent = new Intent(getApplicationContext(),
					KlerudKitchenTimer.class);
			mPendingAlarmIntent = PendingIntent.getBroadcast(
					getApplicationContext(), 1234, mAlarmIntent, 0);
			mAlarmManager = (AlarmManager) getApplicationContext()
					.getSystemService(Context.ALARM_SERVICE);
			mAlarmManager.set(AlarmManager.RTC_WAKEUP, millisInFuture,
					(PendingIntent) mPendingAlarmIntent);

			isCounting = true;

			mTimer = new KitchenCountdownTimer(millisInFuture, 1000);
			mTimer.start();
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
			isCounting = false;
			if (mWakeLock != null && mWakeLock.isHeld())
			{
				mWakeLock.release();
			}
		}

		public void stopAlarm()
		{
			if (mMediaPlayer != null)
			{
				mMediaPlayer.stop();
			}
			if (mWakeLock != null && mWakeLock.isHeld())
			{
				mWakeLock.release();
			}
			isSounding = false;
		}

		/* Our implementation of the CountDownTimer */
		private class KitchenCountdownTimer extends CountDownTimer

		{

			private Intent tick;

			public KitchenCountdownTimer(long millisInFuture,
					long countDownInterval)
			{
				super(millisInFuture, countDownInterval);

			}

			@Override
			public void onFinish()
			{

				isCounting = false;
				isSounding = true;
				/*
				 * Sets up the sound the app plays for an alarm, using the
				 * default alarm for the particular phone, and some backups just
				 * in case.
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
					mMediaPlayer.setDataSource(getApplicationContext(),
							alertSound);
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
				 * Finds the Audio Manager and checks the media volume. If it
				 * isn't zero (silent) we play the alarm.
				 */
				mAudioManager = (AudioManager) getApplicationContext()
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
				/*
				 * The PowerManager and its flags make sure the phone screen
				 * lights up if it has been locked down. This is called
				 * AQUIRE_CAUSES_WAKEUP, which basically means that when an app
				 * acquires a Wake Lock like below (wl.acquire()) the phone
				 * wakes up from its slumber. The wakeup requires that the flag
				 * FULL_WAKE_LOCK is also set. The wakelock is released when the
				 * user stops the alarm. It is important that the lock is
				 * released, or else the screen won't turn off automatically!
				 */
				mPowerManager = (PowerManager) getApplicationContext()
						.getSystemService(Context.POWER_SERVICE);
				mWakeLock = mPowerManager.newWakeLock(
						PowerManager.FULL_WAKE_LOCK
								| PowerManager.ACQUIRE_CAUSES_WAKEUP
								| PowerManager.ON_AFTER_RELEASE, "Time is up");

				if (!mWakeLock.isHeld())
				{
					mWakeLock.acquire();
				}

				Intent alarm = new Intent();
				alarm.setAction("ALARM_SOUNDING");
				alarm.putExtra("TIMER_ID", mTimerId);
				sendBroadcast(alarm);
				showAppNotification();
			}

			@Override
			public void onTick(long millisUntillFinished)
			{
				tick = new Intent();
				tick.setAction("TIMER_TICK");
				tick.putExtra("TIMER_ID", mTimerId);
				tick.putExtra("TIME_LEFT", millisUntillFinished);
				sendBroadcast(tick);
			}

		}

		@Override
		public String toString()
		{
			return "TimerView";
		}

	}
}