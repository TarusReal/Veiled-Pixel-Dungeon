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

package com.shatteredpixel.shatteredpixeldungeon.levels;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Bones;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition;
import com.watabou.utils.Random;

import java.util.ArrayList;

/**
 * The Oberfläche ("surface"): where the hero arrives after insisting on climbing the
 * depth 1 surface stairs ("Go anyway" / "Doch" on the surface dialog). Lives on its own
 * branch (2, depth 0) so it cannot collide with the normal depths or the depth 0 debug
 * floor (branch 0).
 *
 * <p>Atmosphere level: hand-carved single room, no mobs, no respawning, no traps.
 * Modeled on {@link Region0Level}/{@link DeadEndLevel}. Arrival falls back to the
 * level's only transition, its exit, which leads back down to depth 1 (branch 0).
 *
 * <p>Layout and naming are provisional: the tileset currently reuses the crystal caves
 * art (see tilesTex) until this level gets its dedicated surface environment.
 */
public class SurfaceLevel extends Level {

	//identity for Dungeon.newLevel(), transitions and save files: branch 2, depth 0
	public static final int DEPTH = 0;
	public static final int BRANCH = 2;

	private static final int SIZE = 21;

	{
		color1 = 0x3d6b55;
		color2 = 0x8ad0b5;
	}

	@Override
	public String tilesTex() {
		return Assets.Environment.TILES_CAVES_CRYSTAL;
	}

	@Override
	public String waterTex() {
		return Assets.Environment.WATER_CAVES;
	}

	@Override
	protected boolean build() {

		setSize( SIZE, SIZE );

		//outer walls
		for (int x = 0; x < SIZE; x++) {
			for (int y = 0; y < SIZE; y++) {
				boolean border = x == 0 || x == SIZE - 1 || y == 0 || y == SIZE - 1;
				map[y * width() + x] = border ? Terrain.WALL : Terrain.GRASS;
			}
		}

		//dirt path ring around the middle clearing
		for (int x = 7; x <= 13; x++) {
			for (int y = 8; y <= 12; y++) {
				if (x == 7 || x == 13 || y == 8 || y == 12) {
					map[y * width() + x] = Terrain.EMPTY_SP;
				}
			}
		}

		//a small pond in the middle
		for (int x = 9; x <= 11; x++) {
			for (int y = 9; y <= 11; y++) {
				map[y * width() + x] = Terrain.WATER;
			}
		}

		//silent watchers in the four corners of the clearing
		int[] statues = { 5 + 5 * width(), 15 + 5 * width(), 5 + 15 * width(), 15 + 15 * width() };
		for (int cell : statues) {
			map[cell] = Terrain.STATUE;
		}

		//dense growth in the outer corners
		for (int x = 2; x <= 4; x++) {
			for (int y = 2; y <= 4; y++) {
				map[y * width() + x] = Terrain.HIGH_GRASS;
			}
			for (int y = 16; y <= 18; y++) {
				map[y * width() + x] = Terrain.HIGH_GRASS;
			}
		}
		for (int x = 16; x <= 18; x++) {
			for (int y = 2; y <= 4; y++) {
				map[y * width() + x] = Terrain.HIGH_GRASS;
			}
			for (int y = 16; y <= 18; y++) {
				map[y * width() + x] = Terrain.HIGH_GRASS;
			}
		}

		//the only way out: back down to depth 1 on the main branch.
		//arrival from the "Go anyway" transition falls back to this same exit cell
		int exit = 10 + 19 * width();
		map[exit] = Terrain.EXIT;
		transitions.add( new LevelTransition( this, exit, LevelTransition.Type.REGULAR_EXIT,
				1, 0, LevelTransition.Type.REGULAR_ENTRANCE ) );

		return true;
	}

	@Override
	protected void createMobs() {
		//atmosphere level - no mobs
	}

	@Override
	public Actor addRespawner() {
		//atmosphere level - no respawning
		return null;
	}

	@Override
	protected void createItems() {
		Random.pushGenerator( Random.Long() );
			ArrayList<Item> bonesItems = Bones.get();
			if (bonesItems != null) {
				for (Item i : bonesItems) {
					drop( i, exit() ).setHauntedIfCursed().type = Heap.Type.REMAINS;
				}
			}
		Random.popGenerator();
	}

}
