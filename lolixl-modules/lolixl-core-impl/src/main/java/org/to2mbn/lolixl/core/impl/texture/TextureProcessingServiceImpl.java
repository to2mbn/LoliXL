package org.to2mbn.lolixl.core.impl.texture;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import javax.imageio.ImageIO;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.to2mbn.jmccc.auth.yggdrasil.core.texture.ByteArrayTexture;
import org.to2mbn.jmccc.auth.yggdrasil.core.texture.SkinModel;
import org.to2mbn.jmccc.auth.yggdrasil.core.texture.Texture;
import org.to2mbn.jmccc.auth.yggdrasil.core.texture.TextureType;
import org.to2mbn.lolixl.core.game.texture.RenderedTexture;
import org.to2mbn.lolixl.core.game.texture.TextureProcessingService;
import javafx.scene.paint.Color;

@Service({ TextureProcessingService.class })
@Component(immediate = true)
public class TextureProcessingServiceImpl implements TextureProcessingService {

	@Reference
	private TextureCachingService caching;

	@Reference(target = "(usage=cpu_compute)")
	private ExecutorService computingPool;

	@Override
	public CompletableFuture<RenderedTexture> render(Map<TextureType, Texture> texture) {
		Objects.requireNonNull(texture);
		if (!texture.containsKey(TextureType.SKIN)) {
			throw new IllegalArgumentException("SKIN is missing");
		}
		return caching.download(texture)
				.thenApplyAsync(this::process, computingPool);
	}

	private RenderedTexture process(Map<TextureType, ByteArrayTexture> textures) {
		RenderedTextureImpl rendered = new RenderedTextureImpl();

		for (Entry<TextureType, ByteArrayTexture> entry : textures.entrySet()) {
			TextureType textureType = entry.getKey();
			ByteArrayTexture texture = entry.getValue();

			BufferedImage image;
			try {
				image = ImageIO.read(texture.openStream());
			} catch (IOException e) {
				throw new IllegalTextureException("Couldn't load texture " + textureType, e);
			}

			if (textureType == TextureType.SKIN) {
				rendered.skinScale = image.getWidth() / 8;
				rendered.model = SkinModel.inferModel(texture);
				rendered.skinImg = toColorArray(image);
			}
		}

		return rendered;
	}

	private Color[][] toColorArray(BufferedImage img) {
		int w = img.getWidth();
		int h = img.getHeight();
		Color[][] result = new Color[w][h];
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				int c = img.getRGB(x, y);
				result[x][y] = Color.rgb((c >> 16) & 0xff, (c >> 8) & 0xff, c & 0xff, ((c >> 24) & 0xff) / 255.0);
			}
		}
		return result;
	}

}
