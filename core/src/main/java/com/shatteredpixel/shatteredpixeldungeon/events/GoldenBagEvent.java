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
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

/**
 * Take a pile of gold, but risk waking a guardian somewhere on the floor.
 */
public class GoldenBagEvent extends DungeonEvent {

	@Override
	public String key(){
		return "goldenbag";
	}

	@Override
	public String[] labels(){
		return new String[]{ txt("c1"), txt("c2") };
	}

	@Override
	public void select( Hero hero, int index ){
		if (index != 0) return;

		int amount = 10 + Dungeon.depth * 5;
		Dungeon.gold += amount;
		Statistics.goldCollected += amount;
		GLog.i( txt("log", amount) );

		if (Dungeon.level.spawnMob( 4 )){
			GLog.w( txt("guard") );
		}
	}
}
