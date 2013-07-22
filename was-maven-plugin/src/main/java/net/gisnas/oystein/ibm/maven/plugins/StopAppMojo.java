package net.gisnas.oystein.ibm.maven.plugins;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Stop JEE application on all deployment targets through an
 * WebSphere Application Server deployment manager
 * 
 * If the application is not running, no action is performed.
 * 
 * @goal stopApp
 * @requiresProject false
 */
public class StopAppMojo extends AbstractAppMojo {

	public void execute() throws MojoExecutionException, MojoFailureException {
		initConnection();
		getLog().info("Stopping application " + earFile);
		if (applicationName == null) {
			appManager.stopApp(earFile);
		} else {
			appManager.stopApp(applicationName);
		}
	}
	
}
