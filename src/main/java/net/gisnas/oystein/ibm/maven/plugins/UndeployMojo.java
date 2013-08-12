package net.gisnas.oystein.ibm.maven.plugins;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Undeploy JEE application to an WebSphere Application Server deployment
 * manager
 * 
 * If the application does not exist on the server, no action is made.
 * 
 * Requires administrator role Configurator, Deployer or Administrator
 */
@Mojo(name = "undeploy", requiresProject = false)
public class UndeployMojo extends AbstractAppMojo {

	private static Logger log = LoggerFactory.getLogger(UndeployMojo.class);

	public void execute() throws MojoExecutionException, MojoFailureException {
		try {
			initialize();
			initConnection();
			if (applicationName == null) {
				if (!earFile.canRead()) {
					throw new MojoFailureException("applicationName not set and EAR file not found: " + earFile);
				}
				log.info("Undeploying application {}", earFile);
				appManager.uninstallApplication(earFile);
			} else {
				log.info("Undeploying application {}", applicationName);
				appManager.undeploy(applicationName);
			}
		} catch (RuntimeException e) {
			log.error("An error occured while undeploying application {}", (applicationName == null ? earFile : applicationName), e);
			throw e;
		}
	}

}
