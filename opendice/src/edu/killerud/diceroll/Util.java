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

import java.util.Random;

public class Util
{
	public static int getRandomNuberFromRange(int from, int to)
	{
		Random r = new Random();
		r.setSeed(System.currentTimeMillis());
		return from + r.nextInt(to - from);
	}

	public static int getRandomNumber()
	{
		Random r = new Random();
		r.setSeed(System.currentTimeMillis());
		return r.nextInt();
	}

	public static int getRandomNumberFromRangeCustomSeed(int from, int to,
			long seed)
	{
		Random r = new Random();
		r.setSeed(seed);
		return from + r.nextInt(to - from);
	}

	public static int getRandomNumberCustomSeed(long seed)
	{
		Random r = new Random();
		r.setSeed(seed);
		return r.nextInt();
	}
}
