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

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.levels.RegionDefinition;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.watabou.utils.Bundle;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * A single optional encounter with choices, in the style of Slay the Spire events.
 *
 * An event can have several stages. Each stage shows one text and a set of choices.
 * A choice applies its effects, shows a result text and then either opens the next
 * stage or ends the event. Each event is offered at most once per run.
 *
 * Event text lives in the events bundle:
 * events.&lt;key&gt;.title, .text&lt;stage&gt;, .choice&lt;stage&gt;_&lt;n&gt;, .result&lt;stage&gt;_&lt;n&gt;
 */
public abstract class DungeonEvent implements Bundlable {

	//all events known to this run, those still waiting to be offered
	public static final Class<?>[] ALL = new Class<?>[]{
			BloodAltarEvent.class,
			GoldenBagEvent.class,
			TwinWeaponsEvent.class,
			BlackwellEvent.class,
			TraderEvent.class,
			ForgeEvent.class,
			ShrineEvent.class,
			MirrorEvent.class,
			PactEvent.class,
			EmberCoreEvent.class
	};

	public static ArrayList<Class<?>> remaining = new ArrayList<>();

	public static void initForRun(){
		remaining = new ArrayList<>( Arrays.asList( ALL ) );
	}

	public static boolean eventsRemain(){
		return remaining != null && !remaining.isEmpty();
	}

	//true if at least one unused event could be offered on the current floor
	public static boolean anyApplicable(){
		if (!eventsRemain()) return false;
		for (Class<?> type : remaining){
			if (instance( type ).canAppear()){
				return true;
			}
		}
		return false;
	}

	//takes an event out of the pool, so it can't be offered a second time this run
	public static DungeonEvent createRandom(){
		if (!eventsRemain()) return null;

		ArrayList<Class<?>> eligible = new ArrayList<>();
		for (Class<?> type : remaining){
			if (instance( type ).canAppear()){
				eligible.add( type );
			}
		}
		if (eligible.isEmpty()) return null;

		Class<?> type = eligible.get( Random.Int( eligible.size() ) );
		remaining.remove( type );
		return instance( type );
	}

	private static DungeonEvent instance( Class<?> type ){
		return (DungeonEvent) Reflection.newInstance( type );
	}

	//event definition

	public abstract String key();

	//false means the event is not offered on this floor at all
	public abstract boolean canAppear();

	public abstract int choices( int stage );

	public abstract void apply( Hero hero, int stage, int choice );

	//-1 ends the event after the result text, anything else opens that stage next
	public int nextStage( int stage, int choice ){
		return -1;
	}

	public String title(){
		return txt( "title" );
	}

	public String text( int stage ){
		return txt( "text" + stage );
	}

	public String label( int stage, int choice ){
		return txt( "choice" + stage + "_" + (choice + 1) );
	}

	public String result( int stage, int choice ){
		return txt( "result" + stage + "_" + (choice + 1) );
	}

	//free-form extra text, e.g. for effect log lines
	public String txt( String suffix, Object... args ){
		return Messages.get( "events." + key() + "." + suffix, args );
	}

	protected boolean inRegion( int... regions ){
		RegionDefinition region = RegionDefinition.regionOf( Dungeon.depth );
		if (region == null) return false;
		for (int r : regions){
			if (r == region.index) return true;
		}
		return false;
	}

	protected boolean hasHero(){
		return Dungeon.hero != null;
	}

	//current stage, persisted with the event so multi-stage events survive save/load
	protected int stage = 0;

	public int stage(){
		return stage;
	}

	public void setStage( int stage ){
		this.stage = stage;
	}

	//save/load

	private static final String STAGE = "stage";
	private static final String REMAINING = "event_remaining";

	@Override
	public void storeInBundle( Bundle bundle ){
		bundle.put( STAGE, stage );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ){
		stage = bundle.getInt( STAGE );
	}

	public static void storePoolInBundle( Bundle bundle ){
		bundle.put( REMAINING, remaining.toArray( new Class[0] ) );
	}

	public static void restorePoolFromBundle( Bundle bundle ){
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
