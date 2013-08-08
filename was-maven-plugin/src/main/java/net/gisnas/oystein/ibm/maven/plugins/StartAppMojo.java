package net.gisnas.oystein.ibm.maven.plugins;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Start JEE application on all deployment targets through an WebSphere
 * Application Server deployment manager
 * 
 * If the application is already running, no action is performed.
 * 
 * Requires administrator role Operator, Deployer or Administrator
 */
@Mojo(name = "startApp", requiresProject = false)
public class StartAppMojo extends AbstractAppMojo {

	private static Logger log = LoggerFactory.getLogger(StartAppMojo.class);

	public void execute() throws MojoExecutionException, MojoFailureException {
		try {
			initialize();
			initConnection();
			log.info("Starting application {}", earFile);
			if (applicationName == null) {
				appManager.startApp(earFile);
			} else {
				appManager.startApp(applicationName);
			}
		} catch (RuntimeException e) {
			log.error("An error occured while starting app {}", earFile, e);
			throw e;
		}
	}

}
