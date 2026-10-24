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
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.BloodPriest;
import com.shatteredpixel.shatteredpixeldungeon.events.BloodAltarEvent;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;

/**
 * NPC-style conversation for the blood altar, modelled on the wandmaker and
 * shopkeeper windows: portrait and name at the top, then the event text and
 * its choices.
 */
public class WndBloodAltar extends WndCompactOptions {

	private final BloodPriest priest;
	private final BloodAltarEvent event;

	public WndBloodAltar( BloodPriest priest, BloodAltarEvent event ){
		super( priest.sprite(), Messages.titleCase( priest.name() ), event.text( event.stage() ), labels( event ) );
		this.priest = priest;
		this.event = event;
	}

	private static String[] labels( BloodAltarEvent event ){
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

		final String result = event.result( stage, index );
		final int next = event.nextStage( stage, index );

		GameScene.show( new WndQuest( priest, result ){
			@Override
			public void hide(){
				super.hide();
				if (next < 0){
					//the whole conversation is closed now - only then hand out the reward
					event.grantReward( Dungeon.hero );
					priest.finished();
				} else {
					event.setStage( next );
					GameScene.show( new WndBloodAltar( priest, event ) );
				}
			}
		} );
	}
}
