package edu.killerud.kitchentimer;

import android.content.Context;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class StopWatchFragment extends Fragment
{
	private PowerManager mPowerManager;
	private WakeLock mWakeLock;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState)
	{
		if (container == null)
		{
			return null;
		}
		mPowerManager = (PowerManager) container.getContext().getSystemService(
				Context.POWER_SERVICE);
		mWakeLock = mPowerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK,
				"OpenTimer StopWatch");
		mWakeLock.acquire();

		return new LinearLayout(container.getContext());
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
		if (!mWakeLock.isHeld())
		{
			mWakeLock.acquire();
		}
	}

	@Override
	public void onPause()
	{
		super.onPause();
		if (mWakeLock.isHeld())
		{
			mWakeLock.release();
		}

	}
}
