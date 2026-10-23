package com.shatteredpixel.shatteredpixeldungeon.test;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.SewerLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.SurfaceLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition;
import com.shatteredpixel.shatteredpixeldungeon.messages.Languages;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for the surface level reached by insisting on the depth 1 surface stairs
 * ("Go anyway" / "Doch"): generation, arrival/exit wiring back to depth 1, the
 * atmosphere-only rules, and the three surface dialog buttons (ok / go anyway / dev).
 */
class SurfaceLevelTest extends GameTestBase {

	@Test
	void surfaceLevelGeneratesWithArrivalAndExitBackToDepthOne() {
		Dungeon.depth = SurfaceLevel.DEPTH;
		Dungeon.branch = SurfaceLevel.BRANCH;
		Level level = Dungeon.newLevel();
		Dungeon.level = level;

		assertInstanceOf(SurfaceLevel.class, level, "branch 2 depth 0 must build the surface level");

		//arrival falls back to the level's only transition, the exit (same fallback as level 0)
		LevelTransition arrival = level.getTransition(LevelTransition.Type.REGULAR_ENTRANCE);
		assertNotNull(arrival, "the 'Go anyway' transition needs an arrival cell");
		assertEquals(level.exit(), arrival.cell());

		//and the exit descends back to depth 1 on the main branch, not to branch 2
		LevelTransition exit = level.getTransition(LevelTransition.Type.REGULAR_EXIT);
		assertEquals(1, exit.destDepth);
		assertEquals(0, exit.destBranch);
		assertEquals(1, level.transitions.size(), "the surface level must have exactly one transition");

		//atmosphere level: nothing hostile spawns, and the hero never arrives on a blocked cell
		assertEquals(0, level.mobs.size(), "the surface level must not spawn mobs");
		assertTrue(level.passable[level.exit()], "arrival must be walkable");
	}

	@Test
	void surfaceDialogButtonsResolve() {
		for (String key : new String[]{"leave_ok", "leave_go", "leave_dev"}) {
			assertNotEquals(Messages.NO_TEXT_FOUND, Messages.get(SewerLevel.class, key), key);
		}

		try {
			Messages.setup(Languages.GERMAN);
			assertNotEquals(Messages.NO_TEXT_FOUND, Messages.get(SewerLevel.class, "leave_ok"), "leave_ok (de)");
			assertNotEquals(Messages.NO_TEXT_FOUND, Messages.get(SewerLevel.class, "leave_go"), "leave_go (de)");
			assertEquals("Dev-Ebene", Messages.get(SewerLevel.class, "leave_dev"),
					"the third button must keep its german label");
		} finally {
			Messages.setup(Languages.ENGLISH);
		}
	}
}
