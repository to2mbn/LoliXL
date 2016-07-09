package org.to2mbn.lolixl.plugin.maven;

import java.nio.channels.WritableByteChannel;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import org.to2mbn.lolixl.plugin.util.MavenUtils;

public interface MavenRepository {

	/**
	 * 尝试下载指定的release版本的构件。
	 * <p>
	 * 如果该MavenRepository能下载指定的构件，则应调用output.get()打开一个Channel，并在将数据写入后关闭，
	 * Future返回void。
	 * MavenRepository在下载失败的情况下可以自行决定进行重试：首先需要关闭原来的Channel，然后重新调用output.get()
	 * 打开一个Channel。 如果无法找到该构件，则Future应该抛出一个{@link ArtifactNotFoundException}
	 * 。如果下载出现异常，并且MavenRepository不再决定重试，Future也应该抛出异常。
	 * 
	 * @param artifact 构件信息
	 * @param classifier 构件的classifier
	 * @param type 构件的type
	 * @param output 下载到的数据的目的地
	 * @return void
	 * @throws IllegalVersionException 如果构件是一个snapshot却调用了该方法
	 */
	CompletableFuture<Void> downloadRelease(MavenArtifact artifact, String classifier, String type, Supplier<WritableByteChannel> output) throws IllegalVersionException;

	/**
	 * 尝试下载指定的snapshot版本的构件。
	 * <p>
	 * 大部分要求同{@link #downloadRelease(MavenArtifact, String, String, Supplier)}。
	 *
	 * @param artifact 构件信息
	 * @param snapshot 该snapshot的信息
	 * @param classifier 构件的classifier
	 * @param type 构件的type
	 * @param output 下载到的数据的目的地
	 * @return void
	 * @throws IllegalVersionException 如果构件是一个release却调用了该方法
	 * @see #downloadRelease(MavenArtifact, String, String, Supplier)
	 */
	CompletableFuture<Void> downloadSnapshot(MavenArtifact artifact, ArtifactSnapshot snapshot, String classifier, String type, Supplier<WritableByteChannel> output) throws IllegalVersionException;

	/**
	 * 尝试下载所给构件的versioning。
	 * <p>
	 * 如果该MavenRepository能下载指定构件的versioning，则Future应该返回此versioning。
	 * 如果无法找到该构件，则Future应该抛出一个{@link ArtifactNotFoundException}。
	 * 如果下载出现异常，并Future也应该抛出异常。返回值不应该为null。
	 * 
	 * @param groupId 构件的groupId
	 * @param artifactId 构件的artifactId
	 * @return versioning
	 */
	CompletableFuture<ArtifactVersioning> getVersioning(String groupId, String artifactId);

	/**
	 * 尝试下载所给构件的snapshot信息。
	 * <p>
	 * 大部分要求同{@link #getVersioning(String, String)}。
	 * 
	 * @param artifact 构件信息
	 * @return snapshot信息
	 * @throws IllegalVersionException 如果构件是一个release却调用了该方法
	 */
	CompletableFuture<ArtifactSnapshot> getSnapshot(MavenArtifact artifact) throws IllegalVersionException;

	default CompletableFuture<Void> downloadArtifact(MavenArtifact artifact, String classifier, String type, Supplier<WritableByteChannel> output) {
		Objects.requireNonNull(artifact);
		Objects.requireNonNull(output);
		if (MavenUtils.isSnapshot(artifact.getVersion())) {
			return getSnapshot(artifact)
					.thenCompose(snapshot -> downloadSnapshot(artifact, snapshot, classifier, type, output));
		} else {
			return downloadRelease(artifact, classifier, type, output);
		}
	}

}
