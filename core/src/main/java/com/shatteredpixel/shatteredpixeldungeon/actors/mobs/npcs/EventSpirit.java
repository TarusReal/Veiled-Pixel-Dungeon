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
import com.shatteredpixel.shatteredpixeldungeon.events.DungeonEvent;
import com.shatteredpixel.shatteredpixeldungeon.events.EventHost;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.GhostSprite;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndEvent;
import com.watabou.noosa.Game;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;

/**
 * Neutral NPC that carries a single {@link DungeonEvent}, then fades away.
 * Events themselves are not tied to it, this is just the default placeholder carrier.
 */
public class EventSpirit extends NPC implements EventHost {

	{
		spriteClass = GhostSprite.class;
	}

	private DungeonEvent event;

	//used by the debug floor to place a specific event without touching the run pool
	public void setEvent( DungeonEvent event ){
		this.event = event;
	}

	public DungeonEvent peekEvent(){
		return event;
	}

	@Override
	public int defenseSkill( Char enemy ) {
		return INFINITE_EVASION;
	}

	@Override
	public void damage( int dmg, Object src ) {
		//the spirit cannot be harmed
	}

	@Override
	public boolean interact( Char c ) {
		sprite.turnTo( pos, Dungeon.hero.pos );

		if (c != Dungeon.hero){
			return true;
		}

		if (event == null){
			event = DungeonEvent.createRandom();
		}
		if (event == null){
			yell( Messages.get( "events.ui.silent" ) );
			return true;
		}

		final DungeonEvent shown = event;
		Game.runOnRenderThread(new Callback() {
			@Override
			public void call() {
				GameScene.show( new WndEvent( EventSpirit.this, shown ) );
			}
		});
		return true;
	}

	@Override
	public void finished(){
		dismiss();
	}

	public void dismiss() {
		yell( Messages.get(this, "farewell") );
		destroy();
		if (sprite != null){
			sprite.killAndErase();
		}
	}

	@Override
	public boolean reset() {
		return true;
	}

	private static final String EVENT = "event";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		if (event != null){
			bundle.put( EVENT, event );
		}
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		if (bundle.contains( EVENT )){
			event = (DungeonEvent) bundle.get( EVENT );
		}
	}
}
