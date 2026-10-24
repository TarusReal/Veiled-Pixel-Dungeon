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

package com.shatteredpixel.shatteredpixeldungeon.items;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.events.DungeonEvent;
import com.shatteredpixel.shatteredpixeldungeon.events.GoldenBagEvent;
import com.shatteredpixel.shatteredpixeldungeon.events.EventHost;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.noosa.Game;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;

import java.util.ArrayList;

/**
 * A bag of gold lying open on the floor. Picking it up does not add it to the
 * inventory; instead it triggers the Golden Bag event dialog.
 */
public class GoldenBagItem extends Item implements EventHost {

	{
		image = ItemSpriteSheet.GOLD_BAG;
		stackable = false;
	}

	private static final String EVENT = "event";

	private DungeonEvent event;

	public void setEvent( DungeonEvent event ){
		this.event = event;
	}

	public DungeonEvent peekEvent(){
		return event;
	}

	@Override
	public ArrayList<String> actions( Hero hero ){
		return new ArrayList<>();
	}

	@Override
	public boolean doPickUp( Hero hero, int pos ){
		if (event == null){
			event = DungeonEvent.createRandom();
		}
		if (event == null){
			return false;
		}

		final DungeonEvent shown = event;
		Game.runOnRenderThread( new Callback(){
			@Override
			public void call(){
				GameScene.show( new com.shatteredpixel.shatteredpixeldungeon.windows.WndEvent( GoldenBagItem.this, shown ) );
			}
		} );

		hero.spendAndNext( pickupDelay() );
		return false;
	}

	@Override
	public void finished(){
		// only consume the bag if the player actually took the gold
		if (event instanceof GoldenBagEvent && ((GoldenBagEvent) event).taken && Dungeon.level != null){
			for (Heap h : Dungeon.level.heaps.valueList()){
				if (h != null && h.items.contains( this )){
					h.remove( this );
					break;
				}
			}
		}
	}

	@Override
	public void storeInBundle( Bundle bundle ){
		super.storeInBundle( bundle );
		if (event != null){
			bundle.put( EVENT, event );
		}
	}

	@Override
	public void restoreFromBundle( Bundle bundle ){
		super.restoreFromBundle( bundle );
		if (bundle.contains( EVENT )){
			event = (DungeonEvent) bundle.get( EVENT );
		}
	}
}
