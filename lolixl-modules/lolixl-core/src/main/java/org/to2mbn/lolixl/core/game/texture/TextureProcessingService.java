package org.to2mbn.lolixl.core.game.texture;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.to2mbn.jmccc.auth.yggdrasil.core.texture.Texture;
import org.to2mbn.jmccc.auth.yggdrasil.core.texture.TextureType;

public interface TextureProcessingService {

	CompletableFuture<RenderedTexture> render(Map<TextureType, Texture> texture);

}
