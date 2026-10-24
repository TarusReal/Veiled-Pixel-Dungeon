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

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.GoldenBagGuardian;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

/**
 * Take a pile of gold, but risk waking guardians somewhere on the floor.
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

	public boolean taken = false;

	@Override
	public void apply( Hero hero, int stage, int choice ){
		if (choice != 0){
			return;
		}

		taken = true;

		int full = goldAmount();
		int gold;

		// 60% chance: guardians wake and the full gold is taken.
		if (Random.Float() < 0.6f){
			gold = full;

			// alarm effect, just like GuardianTrap
			for (Mob mob : Dungeon.level.mobs){
				mob.beckon( Dungeon.hero.pos );
			}
			GLog.w( txt( "guard" ) );
			CellEmitter.center( Dungeon.hero.pos ).start( Speck.factory( Speck.SCREAM ), 0.3f, 3 );
			Sample.INSTANCE.play( Assets.Sounds.ALERT );

			// spawn guardians, scaled like the trap, but at least one
			int count = Math.max( 1, (Dungeon.scalingDepth() - 5) / 5 );
			int spawned = 0;
			for (int i = 0; i < count; i++){
				GoldenBagGuardian guardian = new GoldenBagGuardian();
				guardian.createWeapon( false );
				guardian.state = guardian.WANDERING;
				guardian.pos = Dungeon.level.randomRespawnCell( guardian );
				if (guardian.pos != -1){
					GameScene.add( guardian );
					guardian.beckon( Dungeon.hero.pos );
					spawned++;
				}
			}
			// ensure at least one guardian actually spawns
			if (spawned == 0){
				GoldenBagGuardian guardian = new GoldenBagGuardian();
				guardian.createWeapon( false );
				guardian.state = guardian.WANDERING;
				guardian.pos = Dungeon.level.randomRespawnCell( guardian );
				if (guardian.pos != -1){
					GameScene.add( guardian );
					guardian.beckon( Dungeon.hero.pos );
				}
			}
		} else {
			// no guardian: less gold (20-50% less)
			int reduction = Random.Int( 20, 51 ); // 20..50
			gold = Math.round( full * (100 - reduction) / 100f );
		}

		Dungeon.gold += gold;
		Statistics.goldCollected += gold;
		GLog.i( txt( "log", gold ) );
	}

	private int goldAmount(){
		return 290 +Random.Int(0,35) + Dungeon.depth * 31;
	}

	private static final String TAKEN = "taken";

	@Override
	public void storeInBundle( Bundle bundle ){
		super.storeInBundle( bundle );
		bundle.put( TAKEN, taken );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ){
		super.restoreFromBundle( bundle );
		taken = bundle.getBoolean( TAKEN );
	}
}
