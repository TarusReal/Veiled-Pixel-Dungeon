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

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndEvent;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * A single optional encounter with choices, in the style of Slay the Spire events.
 * Each event is offered at most once per run.
 */
public abstract class DungeonEvent {

	//all events known to this run, those still waiting to be offered
	private static final Class<?>[] ALL = new Class<?>[]{
			BloodAltarEvent.class, GoldenBagEvent.class, TwinWeaponsEvent.class
	};

	public static ArrayList<Class<?>> remaining = new ArrayList<>();

	public static void initForRun(){
		remaining = new ArrayList<>( Arrays.asList(ALL) );
	}

	public static boolean eventsRemain(){
		return remaining != null && !remaining.isEmpty();
	}

	//takes an event out of the pool, so it can't be offered a second time this run
	public static DungeonEvent createRandom(){
		if (!eventsRemain()) return null;
		Class<?> type = remaining.remove( Random.Int( remaining.size() ) );
		return (DungeonEvent) Reflection.newInstance( type );
	}

	//display

	public abstract String key();
	public abstract String[] labels();
	public abstract void select( Hero hero, int index );

	//all event text lives in the windows bundle, keyed by event
	public String txt( String suffix ){
		return txt( suffix, new Object[0] );
	}

	public String txt( String suffix, Object... args ){
		return Messages.get( WndEvent.class, key() + "_" + suffix, args );
	}

	//save/load

	private static final String REMAINING = "event_remaining";

	public static void storeInBundle( Bundle bundle ){
		bundle.put( REMAINING, remaining.toArray(new Class[0]) );
	}

	public static void restoreFromBundle( Bundle bundle ){
		if (bundle.contains( REMAINING )){
			remaining = new ArrayList<>();
			for (Class<?> type : bundle.getClassArray( REMAINING )){
				remaining.add( type );
			}
		} else {
			//older save, start the event pool fresh
			initForRun();
		}
	}
}
