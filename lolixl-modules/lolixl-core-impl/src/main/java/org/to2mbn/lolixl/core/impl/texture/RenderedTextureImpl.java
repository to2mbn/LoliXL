package org.to2mbn.lolixl.core.impl.texture;

import org.to2mbn.jmccc.auth.yggdrasil.core.texture.SkinModel;
import org.to2mbn.lolixl.core.game.texture.RenderedTexture;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class RenderedTextureImpl implements RenderedTexture {

	/*
	 * The specification of skin can be found at https://github.com/minotar/skin-spec
	 */

	SkinModel model;
	int skinScale;
	Color[][] skinImg;

	@Override
	public void renderAvatar(GraphicsContext ctx, double width) {
		// width/height = 1/1

		// render head.front
		renderRange(ctx, skinImg, 1 * skinScale, 1 * skinScale, 1 * skinScale, 1 * skinScale, 0, 0, width / (1 * skinScale));

		// render helm.front
		renderRange(ctx, skinImg, 5 * skinScale, 1 * skinScale, 1 * skinScale, 1 * skinScale, 0, 0, width / (1 * skinScale));
	}

	@Override
	public void renderFullBodyShot(GraphicsContext ctx, double width) {
		// TODO: implement renderFullBodyShot()
		throw new UnsupportedOperationException("renderFullBodyShot() is not implemented");
	}

	private void renderRange(GraphicsContext ctx, Color[][] img, int x0, int y0, int w, int h, double tx0, double ty0, double bs) {
		int x, y;
		double tx, ty;
		for (int dx = 0; dx < w; dx++) {
			for (int dy = 0; dy < h; dy++) {
				x = x0 + dx;
				y = y0 + dy;
				tx = tx0 + bs * dx;
				ty = ty0 + bs * dy;
				ctx.setFill(img[x][y]);
				ctx.fillRect(tx, ty, bs, bs);
			}
		}
	}

}
