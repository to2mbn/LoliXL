package org.to2mbn.lolixl.plugin.test;

import static org.junit.Assert.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import org.junit.Test;
import org.to2mbn.lolixl.plugin.impl.resolver.DependencyResolver;
import org.to2mbn.lolixl.plugin.maven.MavenArtifact;

public class DependencyResolverTest {

	@Test
	public void testMerge() {
		assertEquals(Collections.singleton(new MavenArtifact("org.to2mbn.lolixl", "test-artifact", "2.0")),
				DependencyResolver.merge(new HashSet<>(Arrays.asList(new MavenArtifact("org.to2mbn.lolixl", "test-artifact", "2.0"), new MavenArtifact("org.to2mbn.lolixl", "test-artifact", "1.0")))));
	}

}
