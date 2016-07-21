package org.to2mbn.lolixl.utils;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public final class PathUtils {

	private PathUtils() {}

	public static void tryMkdirsParent(Path path) throws IOException {
		Path p = path.getParent();
		if (p != null && !Files.isDirectory(p)) {
			Files.createDirectories(p);
		}
	}

	public static void deleteRecursively(Path path) throws IOException {
		if (Files.isDirectory(path)) {
			try {
				Files.walkFileTree(path, new SimpleFileVisitor<Path>() {

					@Override
					public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
						Files.deleteIfExists(file);
						return FileVisitResult.CONTINUE;
					}

					@Override
					public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
						Files.deleteIfExists(dir);
						return FileVisitResult.CONTINUE;
					}

				});
			} catch (NoSuchFileException e) {}
		} else if (Files.isRegularFile(path)) {
			Files.deleteIfExists(path);
		}
	}

}
