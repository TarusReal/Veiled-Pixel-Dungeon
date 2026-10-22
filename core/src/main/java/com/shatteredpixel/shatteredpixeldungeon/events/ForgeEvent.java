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
import com.shatteredpixel.shatteredpixeldungeon.items.KindOfWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Sword;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

/**
 * Pay health to upgrade the equipped weapon.
 */
public class ForgeEvent extends DungeonEvent {

	@Override
	public String key(){
		return "forge";
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

		hero.damage( Math.max( 1, hero.HP / 5 ), this );

		KindOfWeapon weapon = hero.belongings.weapon();
		if (weapon != null){
			weapon.upgrade();
		} else {
			Item fallback = new Sword().upgrade();
			if (!fallback.doPickUp( hero )){
				Dungeon.level.drop( fallback, hero.pos ).sprite.drop();
			}
		}
		GLog.p( txt( "log" ) );
	}
}
