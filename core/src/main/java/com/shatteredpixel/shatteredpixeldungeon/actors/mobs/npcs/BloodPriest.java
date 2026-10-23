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

package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ElmoParticle;
import com.shatteredpixel.shatteredpixeldungeon.events.BloodAltarEvent;
import com.shatteredpixel.shatteredpixeldungeon.events.DungeonEvent;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.BloodPriestSprite;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndBloodAltar;
import com.watabou.noosa.Game;
import com.watabou.utils.Callback;

/**
 * Event carrier for the blood altar. Shares all event handling with
 * {@link EventSpirit}, but wears a different face and uses an NPC-style
 * conversation window.
 */
public class BloodPriest extends EventSpirit {

	{
		spriteClass = BloodPriestSprite.class;
	}

	@Override
	public boolean interact( Char c ){
		sprite.turnTo( pos, Dungeon.hero.pos );

		if (c != Dungeon.hero){
			return true;
		}

		DungeonEvent event = peekEvent();
		if (!(event instanceof BloodAltarEvent)){
			return super.interact( c );
		}

		final BloodAltarEvent altarEvent = (BloodAltarEvent) event;
		Game.runOnRenderThread( new Callback() {
			@Override
			public void call(){
				GameScene.show( new WndBloodAltar( BloodPriest.this, altarEvent ) );
			}
		} );
		return true;
	}

	@Override
	public void dismiss(){
		super.dismiss();
		CellEmitter.get( pos ).burst( ElmoParticle.FACTORY, 6 );
	}
}
