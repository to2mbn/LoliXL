package org.to2mbn.lolixl.plugin.maven;

import java.nio.channels.WritableByteChannel;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public interface MavenRepository {

	/**
	 * 尝试下载指定的版本的构件。
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
	 */
	CompletableFuture<Void> downloadArtifact(MavenArtifact artifact, String classifier, String type, Supplier<WritableByteChannel> output);

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

}
