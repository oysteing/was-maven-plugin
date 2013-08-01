package net.gisnas.oystein.ibm.maven.plugins;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * Undeploy JEE application to an WebSphere Application Server deployment manager
 * 
 * If the application does not exist on the server, no action is made.
 */
@Mojo(name = "undeploy", requiresProject = false)
public class UndeployMojo extends AbstractAppMojo {

	public void execute() throws MojoExecutionException, MojoFailureException {
		initialize();
		initConnection();
		getLog().info("Undeploying application " + earFile);
		if (applicationName == null) {
			appManager.uninstallApplication(earFile);
		} else {
			appManager.undeploy(applicationName);
		}
	}
	
}
