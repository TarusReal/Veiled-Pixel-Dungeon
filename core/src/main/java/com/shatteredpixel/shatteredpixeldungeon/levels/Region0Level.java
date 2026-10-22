/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
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

package com.shatteredpixel.shatteredpixeldungeon.levels;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Bones;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.EventSpirit;
import com.shatteredpixel.shatteredpixeldungeon.events.DungeonEvent;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

import java.util.ArrayList;

/**
 * Region 0 ("surface") placeholder level - Fork-1 scope only (docs/depth0-findings.md /
 * docs/depth0-implementation.md): proves depth 0 can exist, be generated, entered and left
 * without crashing. Deliberately NOT a RegularLevel/StandardRoom composition (Entscheidung 2 in
 * docs/depth0-findings.md: Region 0 is a standalone hub, not "one more Sewers floor") - a single
 * hand-carved room, structurally modeled on {@link DeadEndLevel} (the only other non-RegularLevel,
 * hand-carved {@link Level} in this codebase).
 *
 * <p>No real surface content: no houses, NPCs, shops, or decorated item/mob placement - that's
 * still out of scope for this fork (Fork 2 territory). Instead, depth 0 now doubles as the
 * debug/test floor: it holds a hand-carved event hall with one {@link EventSpirit} per known
 * {@link DungeonEvent} (assigned deterministically, so every event can be tested in a single
 * visit), reached via the "Go anyway" choice on the depth 1 surface stairs. {@link #createMob()}
 * is intentionally left at its {@link Level} default (backed by {@code MobSpawner}/{@code
 * MobRegistry}, exactly like every other level) so a fight here is technically possible if
 * something ever calls {@code spawnMob()}/{@code createMob()} directly - but {@link
 * #addRespawner()} returns {@code null} so nothing spawns automatically beyond the hand-placed
 * spirits.
 */
public class Region0Level extends Level {

	private static final int SIZE = 13;

	//event hall: walls at column 7 and the map border, interior 8..11 x 2..10
	private static final int HALL_LEFT = 7;
	private static final int HALL_TOP = 1;
	private static final int HALL_RIGHT = SIZE - 1;
	private static final int HALL_BOTTOM = SIZE - 2;

	{
		color1 = 0x534f3e;
		color2 = 0xb9d661;
	}

	@Override
	public String tilesTex() {
		return Assets.Environment.TILES_SEWERS;
	}

	@Override
	public String waterTex() {
		return Assets.Environment.WATER_SEWERS;
	}

	@Override
	protected boolean build() {

		setSize(SIZE, SIZE);

		//main hall
		for (int i = 1; i <= 6; i++) {
			for (int j = 1; j < SIZE - 1; j++) {
				map[i * width() + j] = Terrain.EMPTY;
			}
		}

		//event hall, two columns of five spirits
		for (int i = HALL_TOP + 1; i < HALL_BOTTOM; i++) {
			for (int j = HALL_LEFT + 1; j < HALL_RIGHT; j++) {
				map[i * width() + j] = Terrain.EMPTY;
			}
		}

		//door between the main hall and the event hall
		map[6 * width() + HALL_LEFT] = Terrain.DOOR;

		for (int i = 0; i < DungeonEvent.ALL.length; i++) {
			DungeonEvent event = (DungeonEvent) Reflection.newInstance(DungeonEvent.ALL[i]);
			int cell = (2 + i % 5) * width() + (9 + i / 5);
			addEventSpirit(cell, event);
		}

		int exit = 6 * width() + 3;
		map[exit] = Terrain.EXIT;
		transitions.add(new LevelTransition(this, exit, LevelTransition.Type.REGULAR_EXIT));

		return true;
	}

	private void addEventSpirit( int cell, DungeonEvent event ) {
		EventSpirit spirit = new EventSpirit();
		spirit.pos = cell;
		spirit.setEvent( event );
		mobs.add( spirit );
	}

	@Override
	protected void createMobs() {
		//none configured yet - see class comment
	}

	@Override
	public Actor addRespawner() {
		//no ambient mob spawning yet - see class comment
		return null;
	}

	@Override
	protected void createItems() {
		Random.pushGenerator(Random.Long());
			ArrayList<Item> bonesItems = Bones.get();
			if (bonesItems != null) {
				for (Item i : bonesItems) {
					drop(i, exit()).setHauntedIfCursed().type = Heap.Type.REMAINS;
				}
			}
		Random.popGenerator();
	}

}
