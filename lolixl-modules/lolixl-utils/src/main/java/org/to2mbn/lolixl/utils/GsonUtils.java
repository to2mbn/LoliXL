package org.to2mbn.lolixl.utils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import org.to2mbn.lolixl.utils.internal.CommonExecutors;
import org.to2mbn.lolixl.utils.internal.GsonFactory;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public final class GsonUtils {

	public static <T> T fromJson(Path file, Class<T> type) throws JsonSyntaxException, IOException {
		try (Reader reader = new InputStreamReader(Files.newInputStream(file), "UTF-8")) {
			return instance().fromJson(reader, type);
		}
	}

	public static void toJson(Path file, Object obj) throws JsonSyntaxException, IOException {
		PathUtils.tryMkdirsParent(file);
		try (Writer writer = new OutputStreamWriter(Files.newOutputStream(file), "UTF-8")) {
			instance().toJson(obj, writer);
		}
	}

	public static <T> CompletableFuture<T> asynFromJson(Path file, Class<T> type) {
		return AsyncUtils.asyncRun(() -> fromJson(file, type), getIOPool());
	}

	public static CompletableFuture<Void> asynToJson(Path file, Object obj) {
		return AsyncUtils.asyncRun(() -> {
			toJson(file, obj);
			return null;
		}, getIOPool());
	}

	public static Gson instance() {
		Gson instance = GsonFactory.instance;
		if (instance == null) {
			throw new IllegalStateException("No Gson is available");
		}
		return instance;
	}

	private static ExecutorService getIOPool() {
		return CommonExecutors.getExecutorService("local_io");
	}

}
