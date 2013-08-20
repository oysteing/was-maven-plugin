package net.gisnas.oystein.ibm.maven.plugins;

import java.io.File;
import java.io.IOException;

import net.gisnas.oystein.ibm.ImportEndpoint;
import net.gisnas.oystein.ibm.ScaUtil;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
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
@Mojo(name = "deploy", requiresProject = true)
public class DeployMojo extends AbstractAppMojo {

	private static Logger log = LoggerFactory.getLogger(DeployMojo.class);

	/**
	 * Name of target cluster to deploy to
	 */
	@Parameter(property = "was.cluster")
	protected String cluster;

	/**
	 * List of SCA imports and endpoints to override. Only relevant for SCA service applications
	 */
	@Parameter(property = "was.importEndpoints")
	protected ImportEndpoint[] importEndpoints;

	/**
	 * earFile will be copied to this file before making changes (modify import endpoints)
	 */
	@Parameter(property = "was.targetFile", defaultValue="${project.build.directory}/${project.build.finalName}-deploy.ear")
	protected File targetFile;

	public void execute() throws MojoExecutionException, MojoFailureException {
		try {
			initialize();
			if (!earFile.canRead()) {
				throw new MojoFailureException("EAR file not found: " + earFile);
			}
			if (importEndpoints.length > 0) {
				try {
					if (!targetFile.canWrite()) {
						throw new MojoFailureException("Cannot write to " + targetFile);
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
			log.error("An error occured while deploying application {}", earFile, e);
			throw e;
		}
	}

}
