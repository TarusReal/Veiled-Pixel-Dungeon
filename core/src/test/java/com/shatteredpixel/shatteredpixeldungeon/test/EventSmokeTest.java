package com.shatteredpixel.shatteredpixeldungeon.test;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.EventSpirit;
import com.shatteredpixel.shatteredpixeldungeon.events.BloodAltarEvent;
import com.shatteredpixel.shatteredpixeldungeon.events.DungeonEvent;
import com.shatteredpixel.shatteredpixeldungeon.events.GoldenBagEvent;
import com.shatteredpixel.shatteredpixeldungeon.events.TwinWeaponsEvent;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Region0Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.SewerLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;

import org.junit.jupiter.api.Test;

import com.watabou.utils.Bundle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Smoke tests for the "free choice events" feature (Wahlfreie Ereignisse):
 * EventRoom generation on regular floors, the once-per-run event pool,
 * and bundle persistence of that pool.
 */
class EventSmokeTest extends GameTestBase {

	@Test
	void eventRoomSpawnsWithASpiritOnRegularFloors() {
		EventSpirit spirit = null;
		Level level = null;

		//event rooms have a 50% chance per regular floor while events remain.
		//Level generation seeds itself from Dungeon.seed + depth (seedForDepth), so a fixed
		//seed would rebuild the exact same level over and over - vary the seed per attempt
		for (int i = 0; i < 40 && spirit == null; i++) {
			Dungeon.seed = testSeed() + i;
			Dungeon.level = Dungeon.newLevel();
			for (Mob mob : Dungeon.level.mobs) {
				if (mob instanceof EventSpirit) {
					spirit = (EventSpirit) mob;
				}
			}
			level = Dungeon.level;
		}

		assertNotNull(spirit, "no event room within 40 depth-1 generations");

		int spiritCount = 0;
		for (Mob mob : level.mobs) {
			if (mob instanceof EventSpirit) spiritCount++;
		}
		assertEquals(1, spiritCount, "a level should never hold more than one event spirit");

		//the spirit must stand on walkable ground, i.e. the room was painted correctly
		assertTrue(level.passable[spirit.pos], "event spirit must stand on a passable cell");
	}

	@Test
	void debugLevelZeroHoldsOneSpiritPerEventAndLeadsBackToDepthOne() {
		Dungeon.depth = 0;
		Dungeon.branch = 0;
		Level level = Dungeon.newLevel();
		Dungeon.level = level;

		assertInstanceOf(Region0Level.class, level, "depth 0 must stay the Region 0 level");

		int spirits = 0;
		for (Mob mob : level.mobs) {
			if (mob instanceof EventSpirit) spirits++;
		}
		assertEquals(3, spirits, "level 0 should hold one spirit per current event");

		//arriving via the "Go anyway" transition falls back to the level's only transition, the exit
		LevelTransition arrival = level.getTransition(LevelTransition.Type.REGULAR_ENTRANCE);
		assertNotNull(arrival, "level 0 must offer an arrival cell");
		assertEquals(level.exit(), arrival.cell());

		//and the exit descends back to depth 1
		assertEquals(1, level.getTransition(LevelTransition.Type.REGULAR_EXIT).destDepth);

		//both dialog buttons must resolve in english and german bundles
		assertNotEquals(Messages.NO_TEXT_FOUND, Messages.get(SewerLevel.class, "leave_ok"));
		assertNotEquals(Messages.NO_TEXT_FOUND, Messages.get(SewerLevel.class, "leave_go"));
	}

	@Test
	void eachEventIsOfferedAtMostOncePerRun() {
		DungeonEvent.initForRun();

		int offered = 0;
		for (int i = 0; i < 10; i++) {
			DungeonEvent event = DungeonEvent.createRandom();
			if (event != null) offered++;
		}

		assertEquals(3, offered, "the pool should hand out each event exactly once");
		assertFalse(DungeonEvent.eventsRemain());
		assertNull(DungeonEvent.createRandom(), "an exhausted pool must not hand out events");
	}

	@Test
	void eventPoolSurvivesSaveAndLoad() {
		DungeonEvent.initForRun();
		DungeonEvent.createRandom(); //one event already offered this run

		Bundle bundle = new Bundle();
		DungeonEvent.storeInBundle(bundle);

		DungeonEvent.initForRun(); //simulate a fresh JVM before loading
		DungeonEvent.restoreFromBundle(bundle);
		assertEquals(2, DungeonEvent.remaining.size(),
				"loading a save must keep the already offered events out of the pool");

		//saves from before the feature have no event data, the pool starts fresh
		DungeonEvent.restoreFromBundle(new Bundle());
		assertEquals(3, DungeonEvent.remaining.size(),
				"an older save without event data must restore a full pool");
	}

	@Test
	void allEventTextsResolve() {
		DungeonEvent[] events = { new BloodAltarEvent(), new GoldenBagEvent(), new TwinWeaponsEvent() };

		for (DungeonEvent event : events) {
			assertNotEquals(Messages.NO_TEXT_FOUND, event.txt("title"), event.key() + "_title");
			assertNotEquals(Messages.NO_TEXT_FOUND, event.txt("text"), event.key() + "_text");
			assertTrue(event.labels().length >= 2, event.key() + " needs at least two choices");
			for (String label : event.labels()) {
				assertNotEquals(Messages.NO_TEXT_FOUND, label, event.key() + " choice label");
			}
		}
	}
}
