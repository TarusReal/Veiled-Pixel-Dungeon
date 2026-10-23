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
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.AssassinsBlade;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.BattleAxe;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Cudgel;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Dagger;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Dirk;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Flail;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Gauntlet;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Glaive;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Gloves;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Greataxe;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Greatshield;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Greatsword;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.HandAxe;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Katana;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Longsword;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Mace;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Quarterstaff;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Rapier;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.RunicBlade;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Sai;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Scimitar;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Shortsword;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Sickle;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Spear;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Sword;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.WarHammer;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.WarScythe;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Whip;
import com.shatteredpixel.shatteredpixeldungeon.levels.RegionDefinition;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Pick one of two weapons, one of which may be secretly cursed with a strong curse.
 */
public class TwinWeaponsEvent extends DungeonEvent {

	//all melee weapon classes available in this event
	private static final Class<? extends MeleeWeapon>[] ALL_WEAPONS = new Class[]{
		AssassinsBlade.class, BattleAxe.class, Cudgel.class, Dagger.class, Dirk.class,
		Flail.class, Gauntlet.class, Glaive.class, Gloves.class, Greataxe.class,
		Greatshield.class, Greatsword.class, HandAxe.class, Katana.class, Longsword.class,
		Mace.class, Quarterstaff.class, Rapier.class, RunicBlade.class, Sai.class,
		Scimitar.class, Shortsword.class, Sickle.class, Spear.class, Sword.class,
		WarHammer.class, WarScythe.class, Whip.class
	};

	//the two weapon classes offered in this event (set during setup)
	private Class<? extends MeleeWeapon> firstClass;
	private Class<? extends MeleeWeapon> secondClass;
	private boolean firstCursed;
	private boolean secondCursed;
	private String firstName;
	private String secondName;

	@Override
	public String key(){
		return "twinweapons";
	}

	@Override
	public boolean canAppear(){
		return inRegion( 1, 2, 3, 4, 5 ) && Dungeon.depth >= 4;
	}

	@Override
	public int choices( int stage ){
		return 3;
	}

	@Override
	public String label( int stage, int choice ){
		if (firstClass == null){
			setupWeapons();
		}
		if (choice == 0){
			return txt( "choice0_1", firstName );
		} else if (choice == 1){
			return txt( "choice0_2", secondName );
		}
		return super.label( stage, choice );
	}

	@Override
	public void apply( Hero hero, int stage, int choice ){
		if (choice == 2) return; //leave both

		//setup weapons on first apply (choice 0 or 1)
		if (firstClass == null){
			setupWeapons();
		}

		Class<? extends MeleeWeapon> chosenClass = (choice == 0) ? firstClass : secondClass;
		boolean cursed = (choice == 0) ? firstCursed : secondCursed;

		MeleeWeapon weapon;
		try {
			weapon = (MeleeWeapon) Reflection.newInstance( chosenClass );
		} catch (Exception e){
			return;
		}

		//tier-based upgrades: higher tier -> fewer upgrades, lower tier -> more
		int region = (RegionDefinition.regionOf( Dungeon.depth ) != null)
				? RegionDefinition.regionOf( Dungeon.depth ).index : 1;
		int baseTier = Math.min( 5, Math.max( 1, region ) );
		int tierDiff = baseTier - weapon.tier;
		int upgrades = Math.max( 0, tierDiff + Random.Int( 2 ) );
		for (int i = 0; i < upgrades; i++){
			weapon.upgrade();
		}

		if (cursed){
			weapon.cursed = true;
			weapon.cursedKnown = true;
			if (weapon instanceof Weapon){
				((Weapon) weapon).curseCount = 2;
			}
		}

		//detach from backpack (in case it was there)
		weapon.detach( hero.belongings.backpack );

		//try direct equip; fallback to backpack if old weapon is cursed
		if (!weapon.doEquip( hero )){
			if (!weapon.doPickUp( hero )){
				Dungeon.level.drop( weapon, hero.pos ).sprite.drop();
			}
		}

		GLog.i( txt( "log" ) );
	}

	private void setupWeapons(){
		int region = (RegionDefinition.regionOf( Dungeon.depth ) != null)
				? RegionDefinition.regionOf( Dungeon.depth ).index : 1;
		int baseTier = Math.min( 5, Math.max( 1, region ) );

		//random tier variation ±1
		int tier = baseTier + Random.Int( -1, 2 );
		tier = Math.min( 5, Math.max( 1, tier ) );

		//collect weapons of that tier
		List<Class<? extends MeleeWeapon>> candidates = new ArrayList<>();
		for (Class<? extends MeleeWeapon> cls : ALL_WEAPONS){
			try {
				MeleeWeapon w = (MeleeWeapon) Reflection.newInstance( cls );
				if (w.tier == tier){
					candidates.add( cls );
				}
			} catch (Exception e){ }
		}

		//fallback: expand tier range
		if (candidates.size() < 2){
			for (int t = Math.max(1, tier-1); t <= Math.min(5, tier+1); t++){
				if (t == tier) continue;
				for (Class<? extends MeleeWeapon> cls : ALL_WEAPONS){
					try {
						MeleeWeapon w = (MeleeWeapon) Reflection.newInstance( cls );
						if (w.tier == t && !candidates.contains( cls )){
							candidates.add( cls );
						}
					} catch (Exception e){ }
				}
				if (candidates.size() >= 2) break;
			}
		}

		//pick two different weapons
		if (candidates.size() >= 2){
			int i1 = Random.Int( candidates.size() );
			firstClass = candidates.get( i1 );
			candidates.remove( i1 );
			secondClass = candidates.get( Random.Int( candidates.size() ) );
		} else if (candidates.size() == 1){
			firstClass = candidates.get( 0 );
			//pick a different one from all weapons
			List<Class<? extends MeleeWeapon>> all = new ArrayList<>( Arrays.asList( ALL_WEAPONS ) );
			all.remove( firstClass );
			secondClass = all.get( Random.Int( all.size() ) );
		} else {
			//fallback: any two weapons
			List<Class<? extends MeleeWeapon>> all = new ArrayList<>( Arrays.asList( ALL_WEAPONS ) );
			int i1 = Random.Int( all.size() );
			firstClass = all.get( i1 );
			all.remove( i1 );
			secondClass = all.get( Random.Int( all.size() ) );
		}

		//50% curse chance for each
		firstCursed = (Random.Int( 2 ) == 0);
		secondCursed = (Random.Int( 2 ) == 0);

		//store weapon names for label display
		try { firstName = ((MeleeWeapon) Reflection.newInstance( firstClass )).name(); } catch (Exception e){ firstName = "weapon"; }
		try { secondName = ((MeleeWeapon) Reflection.newInstance( secondClass )).name(); } catch (Exception e){ secondName = "weapon"; }
	}
}
