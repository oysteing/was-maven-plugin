package net.gisnas.oystein.ibm.maven.plugins;

import net.gisnas.oystein.ibm.AppManager;
import net.gisnas.oystein.ibm.BpcManager;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Deploy BPC process application and delete existing process instances
 * 
 * Warning: not meant for use in production environments. Process instances
 * might be deleted unintentionally. Use versioning and/or process migration
 * with the normal {@link DeployMojo} goal instead. Intended for use in test
 * environments where developmentServer=false
 */
@Mojo(name = "deploy-process", requiresProject = false)
public class DeployProcessMojo extends AbstractAppMojo {

	/**
	 * Name of target cluster to deploy to
	 */
	@Parameter(property="was.cluster")
	private String cluster;

	public void execute() throws MojoExecutionException, MojoFailureException {
		initialize();
		initConnection();
		BpcManager bpcManager = new BpcManager(adminClient);
		getLog().info("Deleting process instances and stopping process template for application " + earFile);
		String appName = AppManager.extractAppName(earFile);
		bpcManager.stopProcessTemplates(appName);
		appManager.deploy(earFile, applicationName, cluster);
	}

}
