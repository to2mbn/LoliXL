package org.to2mbn.lolixl.plugin.maven;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public interface LocalMavenRepository extends MavenRepository {

	// Notice: Do not write to these files directly
	Path getArtifactPath(MavenArtifact artifact, String classifier, String type);
	
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

	CompletableFuture<Void> install(MavenRepository from, MavenArtifact artifact, String classifier, String type);

}
