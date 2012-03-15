package edu.killerud.kitchentimer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;

public class StopWatchFragment extends Fragment
{
	private PowerManager mPowerManager;
	private WakeLock mWakeLock;
	private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver()
	{

		@Override
		public void onReceive(Context context, Intent intent)
		{
			Log.i("SWFragment", "Some broadcast received received");
			if (intent == null)
			{
				return;
			}
			if (intent.getAction().equals("STOPWATCH_TICK"))
			{
				Log.i("SWFragment", "Tick received");
				mStopWatchView.updateTick(intent
						.getLongExtra("elapsedTime", 0l));
			}

		}

	};
	private StopWatchLayout mStopWatchView;
	private Context mContext;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState)
	{
		if (container == null)
		{
			return null;
		}
		mContext = container.getContext();
		registerBroadcastReceiver();

		mPowerManager = (PowerManager) container.getContext().getSystemService(
				Context.POWER_SERVICE);
		mWakeLock = mPowerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK,
				"OpenTimer StopWatch");
		mWakeLock.acquire();

		ScrollView stopWatchContent = (ScrollView) inflater.inflate(
				R.layout.f_stopwatch, container, false);
		mStopWatchView = new StopWatchLayout(container.getContext());
		stopWatchContent.addView(mStopWatchView.getLayout());
		stopWatchContent.bringToFront();
		return stopWatchContent;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);

	}

	@Override
	public void onSaveInstanceState(Bundle outState)
	{
		outState.putString("sw", "workaround");
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onResume()
	{
		super.onResume();
		if (!mWakeLock.isHeld())
		{
			mWakeLock.acquire();
		}
		registerBroadcastReceiver();
	}

	protected void registerBroadcastReceiver()
	{
		IntentFilter ifilter = new IntentFilter();
		ifilter.addAction("STOPWATCH_TICK");
		mContext.registerReceiver(broadcastReceiver, new IntentFilter(ifilter));
	}

	@Override
	public void onPause()
	{
		super.onPause();
		if (mWakeLock.isHeld())
		{
			mWakeLock.release();
		}
		mContext.unregisterReceiver(broadcastReceiver);

	}
}
