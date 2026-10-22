package com.shatteredpixel.shatteredpixeldungeon.test;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.EventSpirit;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Shopkeeper;
import com.shatteredpixel.shatteredpixeldungeon.events.DungeonEvent;
import com.shatteredpixel.shatteredpixeldungeon.events.EventRunState;
import com.shatteredpixel.shatteredpixeldungeon.events.MirrorEvent;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Sword;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Region0Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.SewerLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition;
import com.shatteredpixel.shatteredpixeldungeon.messages.Languages;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;

import org.junit.jupiter.api.Test;

import com.watabou.utils.Bundle;

import java.util.HashSet;
import java.util.Set;

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
 * regional event conditions, multi-stage state and bundle persistence.
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

		Set<Class<?>> assigned = new HashSet<>();
		int spirits = 0;
		for (Mob mob : level.mobs) {
			if (mob instanceof EventSpirit) {
				spirits++;
				DungeonEvent event = ((EventSpirit) mob).peekEvent();
				assertNotNull(event, "every debug spirit must carry an event");
				assigned.add(event.getClass());
			}
		}
		assertEquals(DungeonEvent.ALL.length, spirits, "level 0 should hold one spirit per event");
		assertEquals(DungeonEvent.ALL.length, assigned.size(), "each event must be placed exactly once");

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
	void poolOnlyOffersEventsThatFitTheFloor() {
		Dungeon.depth = 1;
		DungeonEvent.initForRun();

		int offered = 0;
		DungeonEvent event;
		while ((event = DungeonEvent.createRandom()) != null) {
			assertTrue(event.canAppear(), event.key() + " was offered on a floor it does not fit");
			assertNotEquals("forge", event.key(), "a caves event must not appear in the sewers");
			offered++;
		}

		assertTrue(offered > 0, "at least one event must be available on depth 1");
		assertEquals(DungeonEvent.ALL.length - offered, DungeonEvent.remaining.size(),
				"exactly the offered events leave the pool");
	}

	@Test
	void poolNeverOffersTheSameEventTwice() {
		Dungeon.depth = 25; //halls, the deepest region
		Dungeon.gold = 100;
		DungeonEvent.initForRun();

		Set<Class<?>> seen = new HashSet<>();
		DungeonEvent event;
		while ((event = DungeonEvent.createRandom()) != null) {
			assertTrue(seen.add(event.getClass()), "event offered twice: " + event.key());
		}

		assertFalse(seen.isEmpty(), "at least one event must be available on depth 25");
		assertFalse(DungeonEvent.anyApplicable(), "an exhausted floor pool must not report events");
		assertNull(DungeonEvent.createRandom(), "an exhausted pool must not hand out events");
	}

	@Test
	void eventPoolSurvivesSaveAndLoad() {
		Dungeon.depth = 1;
		DungeonEvent.initForRun();
		DungeonEvent.createRandom(); //one event already offered this run
		int remaining = DungeonEvent.remaining.size();

		Bundle bundle = new Bundle();
		DungeonEvent.storePoolInBundle(bundle);

		DungeonEvent.initForRun(); //simulate a fresh JVM before loading
		DungeonEvent.restorePoolFromBundle(bundle);
		assertEquals(remaining, DungeonEvent.remaining.size(),
				"loading a save must keep the already offered events out of the pool");

		//saves from before the feature have no event data, the pool starts fresh
		DungeonEvent.restorePoolFromBundle(new Bundle());
		assertEquals(DungeonEvent.ALL.length, DungeonEvent.remaining.size(),
				"an older save without event data must restore a full pool");
	}

	@Test
	void mirrorEventAdvancesToASecondStage() {
		MirrorEvent event = new MirrorEvent();

		assertEquals(1, event.nextStage(0, 0), "looking into the mirror opens the next stage");
		assertEquals(-1, event.nextStage(0, 1), "shattering the mirror ends the event");

		event.setStage(1);
		assertEquals(2, event.choices(1), "the second stage offers two choices");
	}

	@Test
	void eventStageSurvivesSaveAndLoad() {
		MirrorEvent event = new MirrorEvent();
		event.setStage(1);

		Bundle bundle = new Bundle();
		event.storeInBundle(bundle);

		MirrorEvent loaded = new MirrorEvent();
		loaded.restoreFromBundle(bundle);

		assertEquals(1, loaded.stage(), "a multi-stage event must keep its stage in a save");
	}

	@Test
	void runStateSurvivesSaveAndResetsWithTheRun() {
		EventRunState.reset();
		EventRunState.raiseShopPrices(1.25f);
		EventRunState.addExtraMobPerFloor();

		Bundle bundle = new Bundle();
		EventRunState.storeInBundle(bundle);

		EventRunState.reset();
		assertEquals(1f, EventRunState.shopPriceMultiplier(), 0.0001f);
		assertEquals(0, EventRunState.extraMobsPerFloor());

		EventRunState.restoreFromBundle(bundle);
		assertEquals(1.25f, EventRunState.shopPriceMultiplier(), 0.0001f);
		assertEquals(1, EventRunState.extraMobsPerFloor());
	}

	@Test
	void allEventTextsResolve() {
		for (Class<?> type : DungeonEvent.ALL) {
			DungeonEvent event = (DungeonEvent) com.watabou.utils.Reflection.newInstance(type);
			assertNotEquals(Messages.NO_TEXT_FOUND, event.title(), event.key() + " title");
			assertTextsResolve(event, 0);
		}
	}

	@Test
	void allEventTextsResolveInGerman() {
		try {
			Messages.setup(Languages.GERMAN);
			assertEquals("DER BLUTIGE ALTAR", Messages.get("events.bloodaltar.title"),
					"the german event bundle must actually be loaded");
			for (Class<?> type : DungeonEvent.ALL) {
				DungeonEvent event = (DungeonEvent) com.watabou.utils.Reflection.newInstance(type);
				assertNotEquals(Messages.NO_TEXT_FOUND, event.title(), event.key() + " title (de)");
				assertTextsResolve(event, 0);
			}
		} finally {
			Messages.setup(Languages.ENGLISH);
		}
	}

	@Test
	void pactRaisesShopPrices() {
		EventRunState.reset();
		int base = Shopkeeper.sellPrice(new Sword());

		EventRunState.raiseShopPrices(1.25f);
		int raised = Shopkeeper.sellPrice(new Sword());

		assertTrue(raised > base, "the pact must make shop prices go up");
		EventRunState.reset();
	}

	private void assertTextsResolve(DungeonEvent event, int stage) {
		assertNotEquals(Messages.NO_TEXT_FOUND, event.text(stage),
				event.key() + " text of stage " + stage);
		assertTrue(event.choices(stage) >= 2,
				event.key() + " needs at least two choices in stage " + stage);

		for (int i = 0; i < event.choices(stage); i++) {
			assertNotEquals(Messages.NO_TEXT_FOUND, event.label(stage, i),
					event.key() + " label " + stage + "/" + i);
			assertNotEquals(Messages.NO_TEXT_FOUND, event.result(stage, i),
					event.key() + " result " + stage + "/" + i);

			int next = event.nextStage(stage, i);
			if (next >= 0) {
				assertTextsResolve(event, next);
			}
		}
	}
}
