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
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

/**
 * Take the core for a ring, at the cost of one more enemy on every later floor.
 */
public class EmberCoreEvent extends DungeonEvent {

	@Override
	public String key(){
		return "embercore";
	}

	@Override
	public boolean canAppear(){
		return inRegion( 3, 4, 5 ) && Dungeon.depth >= 11;
	}

	@Override
	public int choices( int stage ){
		return 2;
	}

	@Override
	public void apply( Hero hero, int stage, int choice ){
		if (choice != 0) return;

		EventRunState.addExtraMobPerFloor();

		Item ring = Generator.random( Generator.Category.RING );
		if (!ring.doPickUp( hero )){
			Dungeon.level.drop( ring, hero.pos ).sprite.drop();
		}
		GLog.p( txt( "log" ) );
	}
}
