package org.to2mbn.lolixl.core.impl.texture;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.logging.Logger;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.osgi.service.component.ComponentContext;
import org.to2mbn.jmccc.auth.yggdrasil.core.texture.ByteArrayTexture;
import org.to2mbn.jmccc.auth.yggdrasil.core.texture.Texture;
import org.to2mbn.jmccc.auth.yggdrasil.core.texture.TextureType;
import org.to2mbn.jmccc.auth.yggdrasil.core.texture.URLTexture;
import org.to2mbn.jmccc.mcdownloader.download.Downloader;
import org.to2mbn.jmccc.mcdownloader.download.concurrent.DownloadCallbacks;
import org.to2mbn.jmccc.mcdownloader.download.tasks.MemoryDownloadTask;
import org.to2mbn.jmccc.util.IOUtils;
import org.to2mbn.lolixl.core.download.CompletableFutureAdapter;
import org.to2mbn.lolixl.utils.AsyncUtils;

@Service({ TextureCachingService.class })
@Component(immediate = true)
public class TextureCachingService {

	private static final Logger LOGGER = Logger.getLogger(TextureCachingService.class.getCanonicalName());

	@Reference
	private CacheManager cacheManager;

	@Reference
	private Downloader downloader;

	@Reference(target = "(usage=network_invocation_low_priority)")
	private ExecutorService bioDownloadExecutor;

	private Cache<String, byte[]> caches;

	@Activate
	public void active(ComponentContext compCtx) {
		caches = cacheManager.getCache("org.to2mbn.lolixl.core.impl.texture.binary", String.class, byte[].class);
	}

	public CompletableFuture<Map<TextureType, ByteArrayTexture>> download(Map<TextureType, Texture> textures) {
		List<CompletableFuture<?>> pendingTasks = new ArrayList<>();
		Map<TextureType, ByteArrayTexture> result = new ConcurrentHashMap<>();

		for (Entry<TextureType, Texture> entry : textures.entrySet()) {
			TextureType textureType = entry.getKey();
			Texture texture = entry.getValue();
			CompletableFuture<byte[]> pending = null;
			if (texture instanceof ByteArrayTexture) {
				result.put(textureType, (ByteArrayTexture) texture);

			} else if (texture instanceof URLTexture) {
				URL url = ((URLTexture) texture).getURL();
				byte[] cached = caches.get(url.toString());
				if (cached != null) {
					LOGGER.fine("Texture cache hit: " + texture);
					result.put(textureType, rebuildTexture(texture, cached));
					continue;
				}
				LOGGER.info("Downloading texture" + texture + " using jmccc-mcdownloader");
				URI uri;
				try {
					uri = url.toURI();
				} catch (URISyntaxException e) {
					throw new IllegalArgumentException(e);
				}
				CompletableFutureAdapter<byte[]> adapter = new CompletableFutureAdapter<>();
				adapter.setAdaptedCancelable(downloader.download(new MemoryDownloadTask(uri), DownloadCallbacks.fromCallback(adapter)));
				pending = adapter.toCompletableFuture()
						.thenApply(data -> {
							caches.put(url.toString(), data);
							return data;
						});

			} else {
				LOGGER.info("Downloading texture " + texture + " using BIO");
				pending = AsyncUtils.asyncRun(() -> {
					try (InputStream in = texture.openStream()) {
						return IOUtils.toByteArray(in);
					}
				}, bioDownloadExecutor);
			}

			if (pending != null) {
				pendingTasks.add(pending
						.thenAccept(data -> result.put(textureType, rebuildTexture(texture, data))));
			}
		}

		if (pendingTasks.isEmpty()) {
			return CompletableFuture.completedFuture(result);
		} else {
			return CompletableFuture.allOf(pendingTasks.toArray(new CompletableFuture[pendingTasks.size()]))
					.thenApply(dummy -> result);
		}
	}

	private ByteArrayTexture rebuildTexture(Texture texture, byte[] data) {
		return new ByteArrayTexture(data, texture.getMetadata());
	}

}
