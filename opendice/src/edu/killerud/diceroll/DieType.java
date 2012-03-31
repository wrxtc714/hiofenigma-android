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

/*
 * New die types must be added here with a name and a corresponding value. 
 * The new die name must also be added to the array DieTypes in res/values/arrays.xml. 
 * The values in the array is the name for the die the user will see.
 */
public enum DieType
{
	THREEFACED(3), FOURFACED(4), FIVEFACED(5), SIXFACED(6), SEVENFACED(7), EIGHTFACED(
			8), TENFACED(10), TWELVEFACED(12), FOURTEENFACED(14), SIXTEENFACED(
			16), EIGHTEENFACED(18), TWENTYFACED(20), TWENTYFOURFACED(24), THIRTYFACED(
			30), FIFTYFACED(50), ONEHUNDREDFACED(100);

	private int mFaces;

	private DieType(int faces)
	{
		this.mFaces = faces;
	}

	public int faces()
	{
		return this.mFaces;
	}
}
