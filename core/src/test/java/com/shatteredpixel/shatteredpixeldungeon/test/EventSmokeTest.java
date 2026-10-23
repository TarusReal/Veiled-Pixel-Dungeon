package com.shatteredpixel.shatteredpixeldungeon.test;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.BloodPriest;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.EventSpirit;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Shopkeeper;
import com.shatteredpixel.shatteredpixeldungeon.events.BloodAltarEvent;
import com.shatteredpixel.shatteredpixeldungeon.events.DungeonEvent;
import com.shatteredpixel.shatteredpixeldungeon.events.EventRunState;
import com.shatteredpixel.shatteredpixeldungeon.events.MirrorEvent;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Sword;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Region0Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.SewerLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.EventRoom;
import com.shatteredpixel.shatteredpixeldungeon.messages.Languages;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.tiles.CustomTilemap;

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
			DungeonEvent.initForRun(); //each attempt simulates a fresh run, so event rooms keep spawning
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
	void bloodAltarEventGetsItsOwnRitualRoom() {
		//the altar only appears once the hero has a real amount of health to sacrifice
		Dungeon.hero.lvl = 5;
		Dungeon.hero.updateHT( false );

		Level level = null;
		BloodPriest priest = null;

		//look for a blood altar event room, keep the pool fresh each attempt
		for (int i = 0; i < 60 && priest == null; i++) {
			Dungeon.seed = testSeed() + i;
			Dungeon.depth = 1;
			DungeonEvent.initForRun();
			level = Dungeon.newLevel();
			Dungeon.level = level;
			for (Mob mob : level.mobs) {
				if (mob instanceof BloodPriest) {
					priest = (BloodPriest) mob;
				}
			}
		}

		assertNotNull(priest, "no blood altar room within 60 generations");
		assertNotNull(priest.peekEvent(), "the blood priest must carry an event");
		assertEquals("bloodaltar", priest.peekEvent().key(),
				"the blood priest must only offer the blood altar event");

		EventRoom.BloodRitualFloor floor = null;
		EventRoom.BloodAltarVis altar = null;
		for (CustomTilemap tile : level.customTiles) {
			if (tile instanceof EventRoom.BloodRitualFloor) floor = (EventRoom.BloodRitualFloor) tile;
			if (tile instanceof EventRoom.BloodAltarVis) altar = (EventRoom.BloodAltarVis) tile;
		}

		assertNotNull(floor, "the ritual room needs its custom floor");
		assertNotNull(altar, "the ritual room needs its altar visual");
		assertEquals(5, floor.tileW, "the ritual floor covers 5x5 tiles");
		assertEquals(5, floor.tileH, "the ritual floor covers 5x5 tiles");

		int altarCell = altar.tileX + altar.tileY * level.width();
		assertEquals(Terrain.CUSTOM_DECO, level.map[altarCell], "the altar must stand on a blocking decoration tile");
		assertFalse(level.passable[altarCell], "the altar must block movement");
		assertTrue(level.passable[priest.pos], "the blood priest must stand on walkable ground");

		assertEquals(altar.tileX - 2, floor.tileX, "the ritual floor must be centered on the altar");
		assertEquals(altar.tileY - 2, floor.tileY, "the ritual floor must be centered on the altar");
	}

	@Test
	void debugLevelZeroAlsoHoldsTheBloodAltarRoom() {
		Dungeon.depth = 0;
		Dungeon.branch = 0;
		Level level = Dungeon.newLevel();
		Dungeon.level = level;

		EventRoom.BloodRitualFloor floor = null;
		EventRoom.BloodAltarVis altar = null;
		for (CustomTilemap tile : level.customTiles) {
			if (tile instanceof EventRoom.BloodRitualFloor) floor = (EventRoom.BloodRitualFloor) tile;
			if (tile instanceof EventRoom.BloodAltarVis) altar = (EventRoom.BloodAltarVis) tile;
		}

		assertNotNull(floor, "level 0 needs the ritual floor");
		assertNotNull(altar, "level 0 needs the altar visual");
		assertEquals(5, floor.tileW);
		assertEquals(5, floor.tileH);
		assertEquals(altar.tileX - 2, floor.tileX);
		assertEquals(altar.tileY - 2, floor.tileY);

		int altarCell = altar.tileX + altar.tileY * level.width();
		assertEquals(Terrain.CUSTOM_DECO, level.map[altarCell]);
		assertFalse(level.passable[altarCell], "the altar must block movement");

		int doorCell = (altar.tileY) * level.width() + (altar.tileX - 3);
		assertEquals(Terrain.DOOR, level.map[doorCell], "the chamber must connect to the event hall");

		BloodPriest priest = null;
		for (Mob mob : level.mobs) {
			if (mob instanceof BloodPriest) priest = (BloodPriest) mob;
		}
		assertNotNull(priest, "level 0 needs the blood priest in the chamber");
		assertEquals(altarCell - level.width(), priest.pos, "the priest stands behind the altar");
		assertTrue(level.passable[priest.pos]);
		assertEquals("bloodaltar", priest.peekEvent().key());
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
		// the golden bag event is placed as an item on the ground, not as a spirit
		for (Heap heap : level.heaps.values()) {
			for (Item item : heap.items) {
				if (item instanceof com.shatteredpixel.shatteredpixeldungeon.items.GoldenBagItem) {
					DungeonEvent event = ((com.shatteredpixel.shatteredpixeldungeon.items.GoldenBagItem) item).peekEvent();
					assertNotNull(event, "the golden bag item must carry an event");
					assigned.add(event.getClass());
				}
			}
		}
		assertEquals(DungeonEvent.ALL.length - 1, spirits, "level 0 should hold one spirit per non-item event");
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

	@Test
	void bloodAltarAppliesAFivePercentSeal() {
		com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero hero = Dungeon.hero;
		hero.lvl = 5;
		hero.updateHT( false ); //theoretical max: 40
		hero.HP = hero.HT;

		BloodAltarEvent event = new BloodAltarEvent();
		event.apply( hero, 0, 0 );

		assertTrue(hero.bloodSeal, "the blood altar must seal part of the hero's health");
		assertEquals(40, hero.theoreticalHT(), "the seal must not lower the theoretical max");
		assertEquals(38, hero.HT, "5% of the theoretical max must be locked away");
		assertEquals(2, hero.sealedHT());
		assertEquals(38, hero.HP, "current health must be clamped to the sealed max");
	}

	@Test
	void bloodSealLocksFivePercentAndScalesWithMaxHealthGains() {
		com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero hero = Dungeon.hero;

		assertTrue(hero.theoreticalHT() >= 0);
		assertFalse(hero.bloodSeal);

		//the lock rounds up: 5% of 21 are 2, so only 19 remain available
		hero.bloodSeal = true;
		hero.HTBoost = 1;
		hero.updateHT( false );
		assertEquals(21, hero.theoreticalHT());
		assertEquals(2, hero.sealedHT());
		assertEquals(19, hero.HT);
		hero.HTBoost = 0;

		hero.lvl = 5;
		hero.updateHT( false );
		assertEquals(40, hero.theoreticalHT());
		assertEquals(38, hero.HT);
		assertEquals(2, hero.sealedHT());

		hero.HP = hero.HT;
		hero.updateHT( false );

		assertEquals(38, hero.HP);

		//the lock stays at 5% when the hero later gains max health
		hero.lvl = 10;
		hero.updateHT( true );
		assertEquals(65, hero.theoreticalHT());
		assertEquals(61, hero.HT);
		assertEquals(4, hero.sealedHT());

		//and it survives save and load
		Bundle bundle = new Bundle();
		hero.storeInBundle( bundle );

		com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero loaded =
				new com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero();
		loaded.restoreFromBundle( bundle );

		assertTrue(loaded.bloodSeal, "the sealed state must survive save and load");
		assertEquals(65, loaded.theoreticalHT());
		assertEquals(61, loaded.HT);
	}

	@Test
	void bloodAltarTilesAreExaminable() {
		Class<?> floor = EventRoom.BloodRitualFloor.class;
		Class<?> altar = EventRoom.BloodAltarVis.class;

		for (Class<?> type : new Class<?>[]{floor, altar}) {
			assertNotEquals(Messages.NO_TEXT_FOUND, Messages.get(type, "name"), type.getSimpleName() + " name");
			assertNotEquals(Messages.NO_TEXT_FOUND, Messages.get(type, "desc"), type.getSimpleName() + " desc");
		}

		try {
			Messages.setup(Languages.GERMAN);
			for (Class<?> type : new Class<?>[]{floor, altar}) {
				assertNotEquals(Messages.NO_TEXT_FOUND, Messages.get(type, "name"), type.getSimpleName() + " name (de)");
				assertNotEquals(Messages.NO_TEXT_FOUND, Messages.get(type, "desc"), type.getSimpleName() + " desc (de)");
			}
		} finally {
			Messages.setup(Languages.ENGLISH);
		}
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
