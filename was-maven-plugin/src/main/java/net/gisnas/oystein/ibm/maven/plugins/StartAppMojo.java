package net.gisnas.oystein.ibm.maven.plugins;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Start JEE application on all deployment targets through an
 * WebSphere Application Server deployment manager
 * 
 * If the application is already running, no action is performed.
 * 
 * @goal startApp
 * @requiresProject false
 */
public class StartAppMojo extends AbstractAppMojo {

	public void execute() throws MojoExecutionException, MojoFailureException {
		initConnection();
		getLog().info("Starting application " + earFile);
		if (applicationName == null) {
			appManager.startApp(earFile);
		} else {
			appManager.startApp(applicationName);
		}
	}
	
}
