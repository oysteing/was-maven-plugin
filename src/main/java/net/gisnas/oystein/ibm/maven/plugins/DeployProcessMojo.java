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
 * Deploy BPC process application and delete existing process instances
 * 
 * Warning: not meant for use in production environments. Process instances
 * might be deleted unintentionally. Use versioning and/or process migration
 * with the normal {@link DeployMojo} goal instead. Intended for use in test
 * environments where developmentServer=false
 * 
 * Requires administrative role Administrator
 */
@Mojo(name = "deploy-process", requiresProject = false)
public class DeployProcessMojo extends AbstractAppMojo {

	private static Logger log = LoggerFactory.getLogger(DeployProcessMojo.class);

	/**
	 * Name of target cluster to deploy to
	 */
	@Parameter(property = "was.cluster")
	private String cluster;

	/**
	 * List of imports and endpoints to override
	 */
	@Parameter(property = "was.importEndpoints")
	protected ImportEndpoint[] importEndpoints;
	
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
					ScaUtil.modifyWsImports(importEndpoints, earFile, targetFile);
				} catch (IOException e) {
					throw new RuntimeException("Unable to modify import in " + earFile, e);
				}
			}
//			initConnection();
//			BpcManager bpcManager = new BpcManager(adminClient);
//			log.info("Deleting process instances and stopping process template for application {}", earFile);
//			String appName = AppManager.extractAppName(earFile);
//			bpcManager.stopProcessTemplates(appName);
//			appManager.deploy(earFile, applicationName, cluster);
		} catch (RuntimeException e) {
			log.error("An error occured while deploying process {}", earFile, e);
			throw e;
		}
	}

}
