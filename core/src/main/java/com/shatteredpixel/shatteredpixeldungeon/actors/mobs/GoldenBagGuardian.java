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

package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon.Enchantment;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.StatueSprite;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

/**
 * A stronger variant of the guardian, spawned only by the Golden Bag event.
 * Its weapon does not drop on death.
 */
public class GoldenBagGuardian extends Mob {

	{
		spriteClass = GoldenBagGuardianSprite.class;

		EXP = 0;
		state = PASSIVE;

		properties.add(Property.INORGANIC);
	}

	protected Weapon weapon;

	public boolean levelGenStatue = true;

	public GoldenBagGuardian() {
		super();

		HP = HT = (int) ((15 + Dungeon.depth * 5) * 1.3f);
		defenseSkill = (int) ((4 + Dungeon.depth) * 1.3f);
	}

	public void createWeapon( boolean useDecks ) {
		if (useDecks) {
			weapon = (MeleeWeapon) Generator.random( Generator.Category.WEAPON );
		} else {
			weapon = (MeleeWeapon) Generator.randomUsingDefaults( Generator.Category.WEAPON );
		}
		levelGenStatue = useDecks;
		weapon.cursed = false;
		weapon.enchant( Enchantment.random() );
		weapon.upgrade(); // +1 level, stronger
	}

	public Weapon weapon() {
		return weapon;
	}

	private static final String WEAPON = "weapon";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( WEAPON, weapon );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		weapon = (Weapon) bundle.get( WEAPON );
	}

	@Override
	public int damageRoll() {
		return weapon.damageRoll( this );
	}

	@Override
	public int attackSkill( Char target ) {
		return (int) ((9 + Dungeon.depth) * 1.5f * weapon.accuracyFactor( this, target ));
	}

	@Override
	public float attackDelay() {
		return super.attackDelay() * weapon.delayFactor( this );
	}

	@Override
	protected boolean canAttack( Char enemy ) {
		return super.canAttack( enemy ) || weapon.canReach( this, enemy.pos );
	}

	@Override
	public int drRoll() {
		return super.drRoll() + Random.NormalIntRange( 0, (int) ((Dungeon.depth + weapon.defenseFactor( this )) * 1.5f) );
	}

	@Override
	public void beckon( int cell ) {
		notice();
		if (state != HUNTING) {
			state = WANDERING;
		}
		target = cell;
	}

	@Override
	public String description() {
		String desc = Messages.get( this, "desc" );
		if (weapon != null) {
			desc += "\n\n" + Messages.get( this, "desc_weapon", weapon.name() );
		}
		return desc;
	}

	@Override
	public void die( Object cause ) {
		if (weapon != null) {
			weapon.identify( false );
		}
		// do not drop the weapon
		super.die( cause );
	}

	public static class GoldenBagGuardianSprite extends StatueSprite {

		public GoldenBagGuardianSprite() {
			super();
			tint( 0.85f, 0.68f, 0f, 0.35f );
		}

		@Override
		public void resetColor() {
			super.resetColor();
			tint( 0.85f, 0.68f, 0f, 0.35f );
		}
	}
}
