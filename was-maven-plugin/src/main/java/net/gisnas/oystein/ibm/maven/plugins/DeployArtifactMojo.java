package net.gisnas.oystein.ibm.maven.plugins;

import java.io.File;
import java.util.Set;

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
 * Deploy JEE application to an WebSphere Application Server deployment manager
 * 
 * Deploy consists of upload of artifact to deployment manager, install and start.
 * If an application with the same name already exists, redeploy will be performed.
 */
@Mojo(name = "deploy-artifact", requiresProject = true, requiresDependencyResolution = ResolutionScope.RUNTIME)
public class DeployArtifactMojo extends DeployMojo {

	private static Logger log = LoggerFactory.getLogger(DeployArtifactMojo.class);
	
	/**
	 * GroupId of artifact to deploy. Must be among project dependencies.
	 */
	@Parameter(property="was.groupId")
	protected String groupId;

	/**
	 * ArtifactId of artifact to deploy. Must be among project dependencies.
	 */
	@Parameter(property="was.artifactId")
	protected String artifactId;

	@Component
	protected MavenProject project;
	
	public void execute() throws MojoExecutionException, MojoFailureException {
		try {
			initialize();
			initConnection();
			if (groupId != null && artifactId != null) {
				getLog().debug("Looking for dependency " + groupId + ":" + artifactId);
				earFile = getDependency(groupId, artifactId);
			}
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
            if (a.getArtifactId().equals(artifactId) &&
                    a.getGroupId().equals(groupId)) {
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
