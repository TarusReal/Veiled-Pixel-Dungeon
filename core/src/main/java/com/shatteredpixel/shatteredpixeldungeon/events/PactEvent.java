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
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfStrength;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

/**
 * Take power now, pay at every shop for the rest of the run.
 */
public class PactEvent extends DungeonEvent {

	@Override
	public String key(){
		return "pact";
	}

	@Override
	public boolean canAppear(){
		return inRegion( 2, 3, 4 ) && Dungeon.depth >= 6;
	}

	@Override
	public int choices( int stage ){
		return 2;
	}

	@Override
	public void apply( Hero hero, int stage, int choice ){
		if (choice != 0) return;

		EventRunState.raiseShopPrices( 1.25f );

		Item potion = new PotionOfStrength();
		if (!potion.doPickUp( hero )){
			Dungeon.level.drop( potion, hero.pos ).sprite.drop();
		}
		GLog.p( txt( "log" ) );
	}
}
