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

package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.events.DungeonEvent;
import com.shatteredpixel.shatteredpixeldungeon.events.EventHost;
import com.shatteredpixel.shatteredpixeldungeon.events.GoldenBagEvent;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;

/**
 * Dialog for a {@link DungeonEvent}: shows the current stage, applies a choice,
 * then shows the result text before either opening the next stage or ending the event.
 */
public class WndEvent extends WndCompactOptions {

	private final EventHost host;
	private final DungeonEvent event;

	public WndEvent( EventHost host, DungeonEvent event ){
		super( event.title(), event.text( event.stage() ), labels( event ) );
		this.host = host;
		this.event = event;
	}

	private static String[] labels( DungeonEvent event ){
		int stage = event.stage();
		String[] labels = new String[event.choices( stage )];
		for (int i = 0; i < labels.length; i++){
			labels[i] = event.label( stage, i );
		}
		return labels;
	}

	@Override
	protected void onSelect( int index ){
		int stage = event.stage();
		event.apply( Dungeon.hero, stage, index );

		int next = event.nextStage( stage, index );

		//golden bag: leaving it does nothing, just close the dialog
		if (event instanceof GoldenBagEvent && index == 1){
			host.finished();
			return;
		}

		String result = event.result( stage, index );
		GameScene.show( new WndResult( host, event, result, next ) );
	}

	private static class WndResult extends WndCompactOptions {

		private final EventHost host;
		private final DungeonEvent event;
		private final int nextStage;

		WndResult( EventHost host, DungeonEvent event, String text, int nextStage ){
			super( event.title(), text, Messages.get( "events.ui.continue" ) );
			this.host = host;
			this.event = event;
			this.nextStage = nextStage;
		}

		@Override
		protected void onSelect( int index ){
			if (nextStage < 0){
				host.finished();
			} else {
				event.setStage( nextStage );
				GameScene.show( new WndEvent( host, event ) );
			}
		}
	}
}
