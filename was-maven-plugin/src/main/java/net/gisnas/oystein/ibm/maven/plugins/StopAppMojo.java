package net.gisnas.oystein.ibm.maven.plugins;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Stop JEE application on all deployment targets through an WebSphere
 * Application Server deployment manager
 * 
 * If the application is not running, no action is performed.
 */
@Mojo(name = "stopApp", requiresProject = false)
public class StopAppMojo extends AbstractAppMojo {

	private static Logger log = LoggerFactory.getLogger(StopAppMojo.class);

	public void execute() throws MojoExecutionException, MojoFailureException {
		try {
			initialize();
			initConnection();
			log.info("Stopping application {}", earFile);
			if (applicationName == null) {
				appManager.stopApp(earFile);
			} else {
				appManager.stopApp(applicationName);
			}
		} catch (RuntimeException e) {
			log.error("An error occured while stopping application {}", earFile, e);
			throw e;
		}
	}

}
