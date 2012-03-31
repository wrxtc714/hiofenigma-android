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

public class Die
{
	private final DieType mDieType;
	private boolean mIsSaved;
	private int mValue;

	public Die(DieType type)
	{
		this.mDieType = type;
	}

	public int roll()
	{
		if (mIsSaved)
		{
			return mValue;
		}
		mValue = Util.getRandomNuberFromRange(1, mDieType.faces() + 1);
		Main.playRollSound();
		return mValue;
	}

	public int seededRoll(long seed)
	{
		if (mIsSaved)
		{
			return mValue;
		}
		mValue = Util.getRandomNumberFromRangeCustomSeed(1,
				mDieType.faces() + 1, seed);
		Main.playRollSound();
		return mValue;
	}

	public void save()
	{
		this.mIsSaved = true;
	}

	public void discard()
	{
		this.mIsSaved = false;
	}

	public boolean isSaved()
	{
		if (mIsSaved)
		{
			return true;
		} else
		{
			return false;
		}
	}
}
