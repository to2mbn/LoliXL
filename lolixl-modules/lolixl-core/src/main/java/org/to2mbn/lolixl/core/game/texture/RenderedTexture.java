package org.to2mbn.lolixl.core.game.texture;

import javafx.scene.canvas.GraphicsContext;

public interface RenderedTexture {

	// ==== 2D apis ====

	/**
	 * Renders the avatar to the given {@link GraphicsContext}.
	 * <p>
	 * The {@code height} is computed by {@code width}.
	 * 
	 * @param ctx
	 * @param width
	 */
	void renderAvatar(GraphicsContext ctx, double width);

	/**
	 * Renders the full-body shot to the given {@link GraphicsContext}.
	 * <p>
	 * The {@code height} is computed by {@code width}.
	 * 
	 * @param ctx
	 * @param width
	 */
	void renderFullBodyShot(GraphicsContext ctx, double width);

	// ==== 3D apis ====
	// TODO: Add 3d apis for RenderedTexture

}
