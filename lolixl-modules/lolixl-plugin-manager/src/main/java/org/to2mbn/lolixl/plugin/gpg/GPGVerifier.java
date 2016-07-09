package org.to2mbn.lolixl.plugin.gpg;

import java.util.concurrent.CompletableFuture;

public interface GPGVerifier {

	/**
	 * 验证所给数据的GPG签名。
	 * <p>
	 * 签名验证包括验证签名有效性、检查公钥可信度等步骤，必要时会要求用户检查公钥。 所以该操作可能会耗费较长时间。
	 * 如果签名有效，则Future的返回值同{@code data}，否则Future抛出一个异常。
	 * 
	 * @param data 数据
	 * @param signature 签名
	 * @return 同{@code data}
	 */
	CompletableFuture<byte[]> verify(byte[] data, byte[] signature);

}
