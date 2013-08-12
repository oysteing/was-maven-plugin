package net.gisnas.oystein.ibm.maven.plugins;

import net.gisnas.oystein.ibm.AppManager;
import net.gisnas.oystein.ibm.BpcManager;

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

	public void execute() throws MojoExecutionException, MojoFailureException {
		try {
			initialize();
			if (!earFile.canRead()) {
				throw new MojoFailureException("EAR file not found: " + earFile);
			}
			initConnection();
			BpcManager bpcManager = new BpcManager(adminClient);
			log.info("Deleting process instances and stopping process template for application {}", earFile);
			String appName = AppManager.extractAppName(earFile);
			bpcManager.stopProcessTemplates(appName);
			appManager.deploy(earFile, applicationName, cluster);
		} catch (RuntimeException e) {
			log.error("An error occured while deploying process {}", earFile, e);
			throw e;
		}
	}

}
