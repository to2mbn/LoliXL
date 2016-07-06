package org.to2mbn.lolixl.plugin.provider;

import java.nio.channels.WritableByteChannel;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import org.to2mbn.lolixl.plugin.MavenArtifact;

public interface RemotePluginProvider {

	/**
	 * 尝试下载指定的构件。
	 * <p>
	 * 如果该Provider能下载指定的构件，则应调用output.get()打开一个Channel，并在将数据写入后关闭，Future返回void。
	 * Provider在下载失败的情况下可以自行决定进行重试：首先需要关闭原来的Channel，然后重新调用output.get()
	 * 打开一个Channel。<br>
	 * 如果无法找到该构件，则Future应该抛出一个{@link ArtifactNotFoundException}
	 * 。如果下载出现异常，并且Provider不再决定重试，Future也应该抛出异常。
	 * 
	 * @param artifact maven信息
	 * @param classifier 构件的classifier
	 * @param type 构件的type
	 * @param output 下载到的数据的目的地
	 * @return void
	 */
	CompletableFuture<Void> downloadArtifact(MavenArtifact artifact, String classifier, String type, Supplier<WritableByteChannel> output);

}
