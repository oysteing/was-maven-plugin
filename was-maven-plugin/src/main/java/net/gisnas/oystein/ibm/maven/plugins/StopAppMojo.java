package net.gisnas.oystein.ibm.maven.plugins;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * Stop JEE application on all deployment targets through an
 * WebSphere Application Server deployment manager
 * 
 * If the application is not running, no action is performed.
 */
@Mojo(name = "stopApp", requiresProject = false)
public class StopAppMojo extends AbstractAppMojo {

	public void execute() throws MojoExecutionException, MojoFailureException {
		initialize();
		initConnection();
		getLog().info("Stopping application " + earFile);
		if (applicationName == null) {
			appManager.stopApp(earFile);
		} else {
			appManager.stopApp(applicationName);
		}
	}
	
}
