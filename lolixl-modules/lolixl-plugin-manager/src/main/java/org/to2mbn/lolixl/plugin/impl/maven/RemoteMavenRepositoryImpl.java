package org.to2mbn.lolixl.plugin.impl.maven;

import java.io.IOException;
import java.nio.channels.WritableByteChannel;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Logger;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.component.ComponentContext;
import org.osgi.util.tracker.ServiceTracker;
import org.to2mbn.lolixl.plugin.maven.ArtifactNotFoundException;
import org.to2mbn.lolixl.plugin.maven.ArtifactVersioning;
import org.to2mbn.lolixl.plugin.maven.MavenArtifact;
import org.to2mbn.lolixl.plugin.maven.MavenRepository;
import static java.lang.String.format;

@Component
@Service({ MavenRepository.class })
@Properties({
		@Property(name = "m2repository.type", value = "remote")
})
public class RemoteMavenRepositoryImpl implements MavenRepository {

	private static final Logger LOGGER = Logger.getLogger(RemoteMavenRepositoryImpl.class.getCanonicalName());

	private ServiceTracker<MavenRepository, MavenRepository> tracker;

	@Activate
	public void active(ComponentContext compCtx) throws InvalidSyntaxException {
		tracker = new ServiceTracker<>(compCtx.getBundleContext(), FrameworkUtil.createFilter("(m2repository.chain=remote)"), null);
		tracker.open(true);
	}

	@Deactivate
	public void deactive() {
		tracker.close();
	}

	@Override
	public CompletableFuture<Void> downloadArtifact(MavenArtifact artifact, String classifier, String type, Supplier<WritableByteChannel> output) {
		Objects.requireNonNull(artifact);
		Objects.requireNonNull(output);

		return asyncInvokeChain(repo -> repo.downloadArtifact(artifact, classifier, type, output));
	}

	@Override
	public CompletableFuture<ArtifactVersioning> getVersioning(String groupId, String artifactId) {
		Objects.requireNonNull(groupId);
		Objects.requireNonNull(artifactId);

		return asyncInvokeChain(repo -> repo.getVersioning(groupId, artifactId));
	}

	private <T> CompletableFuture<T> asyncInvokeChain(Function<MavenRepository, CompletableFuture<T>> operation) {
		MavenRepository[] repositories = tracker.getServices(new MavenRepository[0]);
		CompletableFuture<T> future = new CompletableFuture<>();
		asyncInvokeChain0(operation, repositories, future, 0, null);
		return future;
	}

	private <T> void asyncInvokeChain0(Function<MavenRepository, CompletableFuture<T>> operation, MavenRepository[] repositories, CompletableFuture<T> future, int idx, Throwable lastEx) {
		if (idx >= repositories.length) {
			Throwable noProvidersEx = new ArtifactNotFoundException("${org.to2mbn.lolixl.m2.repo.noMoreToTry}");
			if (lastEx != null) {
				noProvidersEx.addSuppressed(lastEx);
			}
			future.completeExceptionally(noProvidersEx);
			return;
		}
		LOGGER.finer(() -> format("Trying repository[%d]: %s", idx, repositories[idx]));
		CompletableFuture<T> thisStage;
		try {
			thisStage = operation.apply(repositories[idx]);
		} catch (Throwable currentEx) {
			if (lastEx != null && currentEx != lastEx) {
				currentEx.addSuppressed(lastEx);
			}
			future.completeExceptionally(currentEx);
			return;
		}

		thisStage.whenComplete((result, currentEx) -> {
			if (currentEx != null) {
				// exceptionally
				if (lastEx != null && currentEx != lastEx) {
					currentEx.addSuppressed(lastEx);
				}
				if (currentEx instanceof ArtifactNotFoundException ||
						currentEx instanceof IOException) {
					// continue
					asyncInvokeChain0(operation, repositories, future, idx + 1, currentEx);
				} else {
					// fatal
					future.completeExceptionally(currentEx);
				}
			} else {
				// normally
				future.complete(result);
			}
		});
	}

}
