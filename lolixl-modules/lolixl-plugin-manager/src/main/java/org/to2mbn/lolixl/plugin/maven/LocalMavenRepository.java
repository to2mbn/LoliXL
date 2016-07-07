package org.to2mbn.lolixl.plugin.maven;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import org.to2mbn.lolixl.plugin.MavenArtifact;

public interface LocalMavenRepository extends MavenRepository {

	Path getReleasePath(MavenArtifact artifact, String classifier, String type);

	Path getSnapshotPath(MavenArtifact artifact, ArtifactSnapshot snapshot, String classifier, String type);
	
	/**
	 * 删除给定构件。
	 * <p>
	 * 删除失败则Future抛出{@link IOException}。
	 * 
	 * @param artifact 构件
	 * @return void
	 */
	CompletableFuture<Void> deleteArtifact(MavenArtifact artifact);
	
	/**
	 * 删除给定构件的所有版本。
	 * <p>
	 * 删除失败则Future抛出{@link IOException}。
	 * 
	 * @param groupId 构件的groupId
	 * @param artifactId 构件的artifactId
	 * @return void
	 */
	CompletableFuture<Void> deleteArtifactAllVersions(String groupId, String artifactId);

}
