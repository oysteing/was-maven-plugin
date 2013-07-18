package net.gisnas.oystein.ibm.maven.plugins;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Deploy JEE application to an WebSphere Application Server deployment manager
 * 
 * Deploy consists of upload of artifact to deployment manager, install and start.
 * If an application with the same name already exists, redeploy will be performed.
 * 
 * @goal deploy
 * @requiresProject false
 */
public class DeployMojo extends AbstractAppMojo {

	protected String deploymentTarget;

	public void execute() throws MojoExecutionException, MojoFailureException {
		initConnection();
		getLog().info("Deploying application " + earFile);
		if (applicationName == null) {
			appManager.installApplication(earFile);
		} else {
			appManager.installApplication(earFile, applicationName);
		}
	}
	
}
