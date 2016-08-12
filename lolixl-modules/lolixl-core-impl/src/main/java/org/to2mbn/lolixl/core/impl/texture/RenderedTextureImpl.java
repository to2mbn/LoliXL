package org.to2mbn.lolixl.core.impl.texture;

import org.to2mbn.jmccc.auth.yggdrasil.core.texture.SkinModel;
import org.to2mbn.lolixl.core.game.texture.RenderedTexture;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class RenderedTextureImpl implements RenderedTexture {

	/*
	 * The specification of skin can be found at https://github.com/minotar/skin-spec
	 */

	static final int head_front_x = 1;
	static final int head_front_y = 1;
	static final int head_front_w = 1;
	static final int head_front_h = 1;

	static final int helm_front_x = 5;
	static final int helm_front_y = 1;
	static final int helm_front_w = 1;
	static final int helm_front_h = 1;

	SkinModel model;
	int skinScale;
	Color[][] skinImg;

	@Override
	public void renderAvatar(GraphicsContext ctx, double width) {
		double helmBs = width / (1d * skinScale);
		double headBs = (width - 1d * helmBs) / (1d * skinScale);
		double headOffset = helmBs / 2d;

		// render head.front
		renderRange(ctx, skinImg, head_front_x * skinScale, head_front_y * skinScale, head_front_w * skinScale, head_front_h * skinScale, headOffset, headOffset, headBs);

		// render helm.front
		renderRange(ctx, skinImg, helm_front_x * skinScale, helm_front_y * skinScale, helm_front_w * skinScale, helm_front_h * skinScale, 0d, 0d, helmBs);
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
