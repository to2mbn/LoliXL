package org.to2mbn.lolixl.maven;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.Set;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
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
import org.w3c.dom.Document;
import org.w3c.dom.Element;

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

	@Parameter(defaultValue = "${project.build.outputDirectory}/META-INF/lolixl/plugin.xml")
	private File outputDescriptionFile;

	@Parameter(defaultValue = "META-INF/lolixl")
	private String outputTarget;

	@Parameter(defaultValue = "${project.build.directory}")
	private File artifactOutputDir;

	@Parameter
	private Set<String> languageFiles;

	@Component
	private BuildContext buildContext;

	@Component
	private MavenProjectHelper projectHelper;

	@SuppressWarnings("unchecked")
	@Override
	public void execute() throws MojoExecutionException {
		getLog().info("Writing plugin description file to " + outputDescriptionFile);
		try {

			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			Element elePlugin = doc.createElement("plugin");
			doc.appendChild(elePlugin);
			addGAVElement(doc, elePlugin, project.getArtifact());
			Element eleDependencies = doc.createElement("dependencies");
			elePlugin.appendChild(eleDependencies);
			((Set<Artifact>) project.getArtifacts()).stream()
					.filter(artifact -> Artifact.SCOPE_COMPILE.equals(artifact.getScope()))
					.filter(artifact -> !artifact.isOptional())
					.filter(artifact -> excludes == null || !excludes.contains(artifact.getGroupId() + ":" + artifact.getArtifactId()))
					.forEach(dependency -> {
						Element eleDependency = doc.createElement("dependency");
						addGAVElement(doc, eleDependency, dependency);
						eleDependencies.appendChild(eleDependency);
					});
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			File outputDescriptionFileParent = outputDescriptionFile.getParentFile();
			if (!outputDescriptionFileParent.exists()) {
				outputDescriptionFileParent.mkdirs();
			}

			if (languageFiles != null && !languageFiles.isEmpty()) {
				Element eleLanguageFiles = doc.createElement("languageFiles");
				languageFiles.forEach(lang -> {
					addTextElement(doc, eleLanguageFiles, "languageFile", lang);
				});
				elePlugin.appendChild(eleLanguageFiles);
			}

			try (Writer writer = new OutputStreamWriter(new BufferedOutputStream(buildContext.newFileOutputStream(outputDescriptionFile)), "UTF-8")) {
				transformer.transform(new DOMSource(doc), new StreamResult(writer));
			}
			Resource resource = new Resource();
			resource.setDirectory(outputDescriptionFile.getParentFile().getAbsolutePath());
			resource.setIncludes(Arrays.asList(outputDescriptionFile.getName()));
			resource.setTargetPath(outputTarget);
			project.addResource(resource);
			File descriptionArtifact = new File(artifactOutputDir, project.getArtifactId() + "-" + project.getVersion() + "-lolixl-plugin.xml");
			FileUtils.copyFile(outputDescriptionFile, descriptionArtifact);
			projectHelper.attachArtifact(project, "xml", "lolixl-plugin", descriptionArtifact);
		} catch (ParserConfigurationException | TransformerException | IOException e) {
			throw new MojoExecutionException("Couldn't generate plugin description file", e);
		}
	}

	private void addGAVElement(Document doc, Element ele, Artifact artifact) {
		addTextElement(doc, ele, "groupId", artifact.getGroupId());
		addTextElement(doc, ele, "artifactId", artifact.getArtifactId());
		addTextElement(doc, ele, "version", artifact.getVersion());
	}

	private void addTextElement(Document doc, Element ele, String k, String v) {
		Element eleText = doc.createElement(k);
		eleText.appendChild(doc.createTextNode(v));
		ele.appendChild(eleText);
	}

}
