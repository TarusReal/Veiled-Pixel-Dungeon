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

import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.watabou.noosa.Image;

/**
 * Variant of {@link WndOptions} with smaller button text and buttons that grow
 * only as tall as their wrapped label needs.
 */
class WndCompactOptions extends WndOptions {

	private static final int BUTTON_TEXT_SIZE = 7;
	private static final float BUTTON_HEIGHT = 14;

	WndCompactOptions( String title, String message, String... options ){
		super( title, message, options );
	}

	WndCompactOptions( Image icon, String title, String message, String... options ){
		super( icon, title, message, options );
	}

	@Override
	protected void layoutBody( float pos, String message, String... options ){
		int width = PixelScene.landscape() ? WIDTH_L : WIDTH_P;

		RenderedTextBlock tfMessage = PixelScene.renderTextBlock( 6 );
		tfMessage.text( message, width );
		tfMessage.setPos( 0, pos );
		add( tfMessage );

		pos = tfMessage.bottom() + 2*MARGIN;

		for (int i = 0; i < options.length; i++){
			final int index = i;
			CompactButton btn = new CompactButton( options[i] ){
				@Override
				protected void onClick(){
					hide();
					onSelect( index );
				}
			};
			btn.multiline = true;
			add( btn );

			btn.setRect( 0, pos, width, BUTTON_HEIGHT );
			float height = Math.max( BUTTON_HEIGHT, btn.contentHeight() + 4 );
			if (height > BUTTON_HEIGHT){
				btn.setRect( 0, pos, width, height );
			}

			pos += height + MARGIN;
		}

		resize( width, (int)(pos - MARGIN) );
	}

	private static class CompactButton extends RedButton {

		CompactButton( String label ){
			super( label, BUTTON_TEXT_SIZE );
		}

		float contentHeight(){
			return text == null ? 0 : text.height();
		}
	}
}
