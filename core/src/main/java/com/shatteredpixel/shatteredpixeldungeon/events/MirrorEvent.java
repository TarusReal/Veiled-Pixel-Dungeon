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
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bless;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Weakness;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Random;

/**
 * Two stage event: look into the mirror, then decide whether to touch it.
 */
public class MirrorEvent extends DungeonEvent {

	@Override
	public String key(){
		return "mirror";
	}

	@Override
	public boolean canAppear(){
		return inRegion( 1, 2, 3, 4 );
	}

	@Override
	public int choices( int stage ){
		return 2;
	}

	@Override
	public int nextStage( int stage, int choice ){
		if (stage == 0 && choice == 0) return 1;
		return -1;
	}

	@Override
	public void apply( Hero hero, int stage, int choice ){
		if (stage == 0){
			if (choice == 0) return;

			//shatter it: either gold or cuts
			if (Random.Int(2) == 0){
				Dungeon.gold += 30;
				Statistics.goldCollected += 30;
				GLog.p( txt( "gold" ) );
			} else {
				hero.damage( 8, this );
				GLog.w( txt( "cuts" ) );
			}
		} else {
			if (choice == 1) return;

			//take the hand: blessing or weakness
			if (Random.Int(2) == 0){
				Buff.affect( hero, Bless.class, Bless.DURATION );
				GLog.p( txt( "bless" ) );
			} else {
				Buff.affect( hero, Weakness.class, Weakness.DURATION );
				GLog.w( txt( "curse" ) );
			}
		}
	}
}
