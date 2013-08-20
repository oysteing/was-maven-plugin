package net.gisnas.oystein.ibm.maven.plugins;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import net.gisnas.oystein.ibm.ImportEndpoint;
import net.gisnas.oystein.ibm.ScaUtil;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Deploy JEE application to a WebSphere Application Server deployment manager
 * 
 * Deploy consists of upload of artifact to deployment manager, install and
 * start. If an application with the same name already exists, redeploy will be
 * performed.
 * 
 * Has options specific to IBM SCA service applications
 * 
 * Requires administrator role Deployer or Administrator
 */
@Mojo(name = "deploy-artifact", requiresProject = true, requiresDependencyResolution = ResolutionScope.RUNTIME)
public class DeployArtifactMojo extends DeployMojo {

	private static Logger log = LoggerFactory.getLogger(DeployArtifactMojo.class);

	/**
	 * GroupId of artifact to deploy. Must be among project dependencies.
	 */
	@Parameter(property = "was.groupId", required = true)
	protected String groupId;

	/**
	 * ArtifactId of artifact to deploy. Must be among project dependencies.
	 */
	@Parameter(property = "was.artifactId", required = true)
	protected String artifactId;

	/**
	 * List of SCA imports and endpoints to override. Only relevant for SCA
	 * service applications
	 */
	@Parameter(property = "was.importEndpoints")
	protected ImportEndpoint[] importEndpoints;

	/**
	 * earFile will be copied to this file before making changes (modify import
	 * endpoints) Defaults to ${project.build.directory}/${dependency
	 * filename}-deploy.ear, where ${dependency filename} is the filename of the
	 * dependency to deploy
	 */
	@Parameter(property = "was.targetFile")
	protected File targetFile;

	@Component
	protected MavenProject project;

	public void execute() throws MojoExecutionException, MojoFailureException {
		try {
			initialize();
			getLog().debug("Looking for dependency " + groupId + ":" + artifactId);
			earFile = getDependency(groupId, artifactId);
			if (importEndpoints.length > 0) {
				try {
					if (targetFile == null) {
						targetFile = new File(project.getBuild().getDirectory(), earFile.getName() + "-deploy.ear");
						new File(project.getBuild().getDirectory()).mkdir();
					}
					ScaUtil.modifyWsImports(importEndpoints, earFile, targetFile);
					earFile = targetFile;
				} catch (IOException e) {
					throw new RuntimeException("Unable to modify import in " + targetFile, e);
				}
			}
			initConnection();
			log.info("Deploying application {}", earFile);
			appManager.deploy(earFile, applicationName, cluster);
		} catch (RuntimeException e) {
			log.error("An error occured while deploying artifact {}", artifactId, e);
			throw e;
		}
	}

	private File getDependency(String groupId, String artifactId) throws MojoFailureException {
		@SuppressWarnings("unchecked")
		Set<Artifact> dependencies = project.getArtifacts();
		Artifact artifact = null;
		for (final Artifact a : dependencies) {
			if (a.getArtifactId().equals(artifactId) && a.getGroupId().equals(groupId)) {
				artifact = a;
				break;
			}
		}
		if (artifact == null) {
			throw new RuntimeException(String.format("Could not resolve artifact to deploy %s:%s", groupId, artifactId));
		}
		return artifact.getFile();
	}

}
