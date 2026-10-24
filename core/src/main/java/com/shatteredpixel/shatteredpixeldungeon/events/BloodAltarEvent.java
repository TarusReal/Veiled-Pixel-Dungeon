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
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfUpgrade;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

/**
 * Trade a slice of maximum health for a scroll of upgrade.
 */
public class BloodAltarEvent extends DungeonEvent {

	@Override
	public String key(){
		return "bloodaltar";
	}

	@Override
	public boolean canAppear(){
		return inRegion( 1, 2, 3, 4, 5 ) && hasHero()
				&& !Dungeon.hero.bloodSeal && Dungeon.hero.theoreticalHT() >= 25;
	}

	@Override
	public int choices( int stage ){
		return 2;
	}

	@Override
	public void apply( Hero hero, int stage, int choice ){
		if (choice != 0) return;

		if (hero.bloodSeal) return;
		
		hero.bloodSeal = true;
		hero.updateHT( true );
		hero.HP = Math.min( hero.HP, hero.HT );
	}

	//called when the last dialog window is closed, so the scroll only lands in the hero's
	//inventory (or on the floor) after the whole conversation was clicked through
	public void grantReward( Hero hero ){
		if (!hero.bloodSeal) return;

		Item scroll = new ScrollOfUpgrade();
		if (!scroll.doPickUp( hero )){
			Dungeon.level.drop( scroll, hero.pos ).sprite.drop();
		}
		GLog.p( txt( "log" ) );
	}
}
