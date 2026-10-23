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

package com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.BloodPriest;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.EventSpirit;
import com.shatteredpixel.shatteredpixeldungeon.events.BloodAltarEvent;
import com.shatteredpixel.shatteredpixeldungeon.events.DungeonEvent;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.Painter;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.tiles.CustomTilemap;
import com.watabou.noosa.Image;
import com.watabou.noosa.Tilemap;
import com.watabou.utils.Point;

/**
 * A quiet room where an {@link EventSpirit} waits with an optional choice.
 * Spawns independently of the regular special room pool.
 *
 * The event is picked when the room is painted. The blood altar event gets its
 * own ritual chamber with custom floor art, an altar and a {@link BloodPriest}.
 */
public class EventRoom extends SpecialRoom {

	@Override
	public int minWidth() { return 7; }
	@Override
	public int minHeight() { return 7; }

	@Override
	public void paint( Level level ) {

		DungeonEvent event = DungeonEvent.createRandom();
		Point c = center();

		if (event instanceof BloodAltarEvent){
			paintBloodAltar( level, c );
		} else {
			paintDefault( level );
		}

		EventSpirit spirit = event instanceof BloodAltarEvent ? new BloodPriest() : new EventSpirit();
		if (event != null){
			spirit.setEvent( event );
		}
		Point spiritPoint = event instanceof BloodAltarEvent ? new Point( c.x, c.y - 1 ) : c;
		spirit.pos = level.pointToCell( spiritPoint );
		level.mobs.add( spirit );

		for (Door door : connected.values()) {
			door.set( Door.Type.REGULAR );
		}
	}

	private void paintDefault( Level level ){
		Painter.fill( level, this, Terrain.WALL );
		Painter.fill( level, this, 1, Terrain.EMPTY_SP );
	}

	private void paintBloodAltar( Level level, Point c ){
		Painter.fill( level, this, Terrain.WALL );
		Painter.fill( level, this, 1, Terrain.EMPTY_SP );

		furnishBloodAltar( level, c.x, c.y, top );
	}

	/**
	 * Places the ritual floor, the altar and the decorated back wall around a center cell.
	 * The caller is responsible for the room's own floor and walls.
	 */
	public static void furnishBloodAltar( Level level, int centerX, int centerY, int wallY ){

		//5x5 ritual floor, centered on the altar
		BloodRitualFloor floor = new BloodRitualFloor();
		floor.pos( centerX - 2, centerY - 2 );
		level.customTiles.add( floor );

		//CUSTOM_DECO is invisible and solid: the altar art sits on it and blocks movement
		Painter.set( level, centerX, centerY, Terrain.CUSTOM_DECO );
		BloodAltarVis altar = new BloodAltarVis();
		altar.pos( centerX, centerY );
		level.customTiles.add( altar );

		//a decorative wall face behind the altar, unless a door was carved there
		int decoCell = level.pointToCell( new Point( centerX, wallY ) );
		if (level.map[decoCell] == Terrain.WALL){
			Painter.set( level, decoCell, Terrain.WALL_DECO );
		}
	}

	public static class BloodRitualFloor extends CustomTilemap {

		{
			texture = Assets.Environment.BLOOD_ALTAR_FLOOR;

			tileW = 5;
			tileH = 5;
		}

		private static final int TEX_WIDTH = 80;

		@Override
		public Tilemap create() {
			Tilemap v = super.create();
			v.map( mapSimpleImage( 0, 0, TEX_WIDTH ), tileW );
			return v;
		}

		@Override
		public Image image( int tileX, int tileY ) {
			//the center cell reports itself as the altar, never as part of this floor
			if (tileX == 2 && tileY == 2){
				return null;
			}
			return super.image( tileX, tileY );
		}

		@Override
		public String name( int tileX, int tileY ) {
			return Messages.get( this, "name" );
		}

		@Override
		public String desc( int tileX, int tileY ) {
			return Messages.get( this, "desc" );
		}
	}

	public static class BloodAltarVis extends CustomTilemap {

		{
			texture = Assets.Environment.BLOOD_ALTAR;

			tileW = 1;
			tileH = 1;
		}

		private static final int TEX_WIDTH = 16;

		@Override
		public Tilemap create() {
			Tilemap v = super.create();
			v.map( mapSimpleImage( 0, 0, TEX_WIDTH ), tileW );
			//the gravestone art is bottom-anchored in its tile, nudge it up one pixel
			v.y -= 1;
			return v;
		}

		@Override
		public String name( int tileX, int tileY ) {
			return Messages.get( this, "name" );
		}

		@Override
		public String desc( int tileX, int tileY ) {
			return Messages.get( this, "desc" );
		}
	}
}
