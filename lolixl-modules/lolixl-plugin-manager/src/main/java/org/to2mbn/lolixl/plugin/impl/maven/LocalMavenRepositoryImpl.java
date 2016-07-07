package org.to2mbn.lolixl.plugin.impl.maven;

import java.nio.channels.WritableByteChannel;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;
import org.apache.felix.scr.annotations.Reference;
import org.to2mbn.lolixl.plugin.MavenArtifact;
import org.to2mbn.lolixl.plugin.maven.ArtifactSnapshot;
import org.to2mbn.lolixl.plugin.maven.ArtifactVersioning;
import org.to2mbn.lolixl.plugin.maven.LocalMavenRepository;
import org.to2mbn.lolixl.plugin.util.MavenUtils;
import org.to2mbn.lolixl.plugin.util.PathUtils;
import org.to2mbn.lolixl.utils.AsyncUtils;

public class LocalMavenRepositoryImpl implements LocalMavenRepository {

	@Reference(target = "(usage=local_io)")
	private Executor localIOPool;

	private Path m2dir;

	public LocalMavenRepositoryImpl(Path m2dir) {
		this.m2dir = m2dir;
	}

	@Override
	public CompletableFuture<Void> downloadRelease(MavenArtifact artifact, String classifier, String type, Supplier<WritableByteChannel> output) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<Void> downloadSnapshot(MavenArtifact artifact, ArtifactSnapshot snapshot, String classifier, String type, Supplier<WritableByteChannel> output) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<ArtifactVersioning> getVersioning(String groupId, String artifactId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<ArtifactSnapshot> getSnapshot(MavenArtifact artifact) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Path getReleasePath(MavenArtifact artifact, String classifier, String type) {
		return getVersionDir(artifact).resolve(MavenUtils.getArtifactFileName(artifact, classifier, type));
	}

	@Override
	public Path getSnapshotPath(MavenArtifact artifact, ArtifactSnapshot snapshot, String classifier, String type) {
		return getVersionDir(artifact).resolve(MavenUtils.getArtifactFileName(artifact, snapshot, classifier, type));
	}

	@Override
	public CompletableFuture<Void> deleteArtifact(MavenArtifact artifact) {
		return AsyncUtils.asyncRun(() -> {
			PathUtils.deleteRecursively(getVersionDir(artifact));
			return null;
		}, localIOPool);
	}

	@Override
	public CompletableFuture<Void> deleteArtifactAllVersions(String groupId, String artifactId) {
		// TODO Auto-generated method stub
		return null;
	}

	private Path getArtifactDir(String groupId, String artifactId) {
		Path p = m2dir;
		for (String gid : groupId.split("\\."))
			p = p.resolve(gid);
		return p.resolve(artifactId);
	}

	private Path getVersionDir(MavenArtifact artifact) {
		return getArtifactDir(artifact.getGroupId(), artifact.getArtifactId())
				.resolve(artifact.getVersion());
	}

}
