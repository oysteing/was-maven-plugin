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
 * 
 * Requires administrator role Operator, Deployer or Administrator
 */
@Mojo(name = "stopApp", requiresProject = false)
public class StopAppMojo extends AbstractAppMojo {

	private static Logger log = LoggerFactory.getLogger(StopAppMojo.class);

	public void execute() throws MojoExecutionException, MojoFailureException {
		try {
			initialize();
			initConnection();
			if (applicationName == null) {
				if (!earFile.canRead()) {
					throw new MojoFailureException("applicationName not set and EAR file not found: " + earFile);
				}
				log.info("Stopping application {}", earFile);
				appManager.stopApp(earFile);
			} else {
				log.info("Stopping application {}", applicationName);
				appManager.stopApp(applicationName);
			}
		} catch (RuntimeException e) {
			log.error("An error occured while stopping application {}", (applicationName == null ? earFile : applicationName), e);
			throw e;
		}
	}

}
