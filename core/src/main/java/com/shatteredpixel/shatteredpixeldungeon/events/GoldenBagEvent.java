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
	public boolean canAppear(){
		return inRegion( 1, 2, 3, 4, 5 );
	}

	@Override
	public int choices( int stage ){
		return 2;
	}

	@Override
	public String label( int stage, int choice ){
		if (choice == 0){
			return txt( "choice0_1", goldAmount() );
		}
		return super.label( stage, choice );
	}

	@Override
	public void apply( Hero hero, int stage, int choice ){
		if (choice != 0) return;

		int amount = goldAmount();
		Dungeon.gold += amount;
		Statistics.goldCollected += amount;
		GLog.i( txt( "log", amount ) );

		if (Dungeon.level != null && Dungeon.level.spawnMob( 4 )){
			GLog.w( txt( "guard" ) );
		}
	}

	private int goldAmount(){
		return 10 + Dungeon.depth * 5;
	}
}
