package edu.killerud.kitchentimer;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class IntervalFragment extends Fragment
{
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState)
	{
		if (container == null)
		{
			return null;
		}

		// TODO timepickers for setting the alarm interval
		// TODO set up a repeating alarm using the alarm service
		// TODO set up an equalivient to the CD for alarm sounding
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
		outState.putString("iv", "workaround");
		super.onSaveInstanceState(outState);
	}
}
