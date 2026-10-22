/*
 * Veiled Pixel Dungeon
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.events;

import com.watabou.utils.Bundle;

/**
 * Lasting changes an event can leave behind for the rest of the run.
 * Kept separate from the event pool so it can be saved and reset on its own.
 */
public class EventRunState {

	private static final String SHOP_MULT = "event_shop_mult";
	private static final String EXTRA_MOBS = "event_extra_mobs";

	private static float shopPriceMultiplier = 1f;
	private static int extraMobsPerFloor = 0;

	public static float shopPriceMultiplier(){
		return shopPriceMultiplier;
	}

	public static int extraMobsPerFloor(){
		return extraMobsPerFloor;
	}

	public static void raiseShopPrices( float factor ){
		shopPriceMultiplier *= factor;
	}

	public static void addExtraMobPerFloor(){
		extraMobsPerFloor++;
	}

	public static void reset(){
		shopPriceMultiplier = 1f;
		extraMobsPerFloor = 0;
	}

	public static void storeInBundle( Bundle bundle ){
		bundle.put( SHOP_MULT, shopPriceMultiplier );
		bundle.put( EXTRA_MOBS, extraMobsPerFloor );
	}

	public static void restoreFromBundle( Bundle bundle ){
		shopPriceMultiplier = bundle.contains( SHOP_MULT ) ? bundle.getFloat( SHOP_MULT ) : 1f;
		extraMobsPerFloor = bundle.contains( EXTRA_MOBS ) ? bundle.getInt( EXTRA_MOBS ) : 0;
	}
}
