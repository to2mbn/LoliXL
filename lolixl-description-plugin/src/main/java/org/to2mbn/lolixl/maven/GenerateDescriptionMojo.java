package org.to2mbn.lolixl.maven;

import static java.util.stream.Collectors.*;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.Set;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.apache.maven.shared.utils.io.FileUtils;
import org.sonatype.plexus.build.incremental.BuildContext;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@Mojo(
		name = "generate-description",
		defaultPhase = LifecyclePhase.GENERATE_RESOURCES,
		requiresDependencyCollection = ResolutionScope.COMPILE,
		requiresDependencyResolution = ResolutionScope.COMPILE)
public class GenerateDescriptionMojo extends AbstractMojo {

	@Parameter(defaultValue = "${project}", readonly = true)
	private MavenProject project;

	@Parameter
	private Set<String> excludes;

	@Parameter(defaultValue = "${project.build.outputDirectory}/META-INF/lolixl/plugin.json")
	private File outputDescriptionFile;

	@Parameter(defaultValue = "META-INF/lolixl")
	private String outputTarget;

	@Parameter(defaultValue = "${project.build.directory}")
	private File artifactOutputDir;

	@Component
	private BuildContext buildContext;

	@Component
	private MavenProjectHelper projectHelper;

	private Gson gson = new GsonBuilder()
			.setPrettyPrinting()
			.create();

	@SuppressWarnings("unchecked")
	@Override
	public void execute() throws MojoExecutionException {
		getLog().info("Writing plugin description file to " + outputDescriptionFile);
		try {

			PluginDescription description = new PluginDescription(new MavenArtifact(project.getArtifact()),
					((Set<Artifact>) project.getArtifacts()).stream()
							.filter(artifact -> Artifact.SCOPE_COMPILE.equals(artifact.getScope()))
							.filter(artifact -> !artifact.isOptional())
							.filter(artifact -> excludes == null || !excludes.contains(artifact.getGroupId() + ":" + artifact.getArtifactId()))
							.map(MavenArtifact::new)
							.collect(toSet()));

			File outputDescriptionFileParent = outputDescriptionFile.getParentFile();
			if (!outputDescriptionFileParent.exists()) {
				outputDescriptionFileParent.mkdirs();
			}

			try (Writer writer = new OutputStreamWriter(new BufferedOutputStream(buildContext.newFileOutputStream(outputDescriptionFile)), "UTF-8")) {
				gson.toJson(description, writer);
			}
			Resource resource = new Resource();
			resource.setDirectory(outputDescriptionFile.getParentFile().getAbsolutePath());
			resource.setIncludes(Arrays.asList(outputDescriptionFile.getName()));
			resource.setTargetPath(outputTarget);
			project.addResource(resource);
			File descriptionArtifact = new File(artifactOutputDir, project.getArtifactId() + "-" + project.getVersion() + "-lolixl-plugin.json");
			FileUtils.copyFile(outputDescriptionFile, descriptionArtifact);
			projectHelper.attachArtifact(project, "json", "lolixl-plugin", descriptionArtifact);
		} catch (IOException e) {
			throw new MojoExecutionException("Couldn't generate plugin description file", e);
		}
	}

}
