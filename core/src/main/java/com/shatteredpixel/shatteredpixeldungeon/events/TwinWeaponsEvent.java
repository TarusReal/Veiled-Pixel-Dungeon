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
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Mace;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Sword;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Random;

/**
 * Pick one of two weapons, one of which is secretly cursed.
 */
public class TwinWeaponsEvent extends DungeonEvent {

	@Override
	public String key(){
		return "twinweapons";
	}

	@Override
	public String[] labels(){
		return new String[]{ txt("c1"), txt("c2"), txt("c3") };
	}

	@Override
	public void select( Hero hero, int index ){
		Weapon weapon;
		switch (index){
			case 0: default:
				weapon = new Sword();
				break;
			case 1:
				weapon = new Mace();
				break;
			case 2:
				return;
		}

		//one of the pair is cursed, so the chosen one has a 50% chance
		if (Random.Int(2) == 0){
			weapon.cursed = true;
		}

		if (!weapon.doPickUp( hero )){
			Dungeon.level.drop( weapon, hero.pos ).sprite.drop();
		} else {
			GLog.i( txt("log") );
		}
	}
}
