package net.gisnas.oystein.ibm.maven.plugins;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Deploy JEE application to an WebSphere Application Server deployment manager
 * 
 * Deploy consists of upload of artifact to deployment manager, install and start.
 * If an application with the same name already exists, redeploy will be performed.
 */
@Mojo(name = "deploy", requiresProject = true)
public class DeployMojo extends AbstractAppMojo {

	private static Logger log = LoggerFactory.getLogger(DeployMojo.class);

	/**
	 * Name of target cluster to deploy to
	 */
	@Parameter(property = "was.cluster")
	protected String cluster;

	public void execute() throws MojoExecutionException, MojoFailureException {
		initialize();
		initConnection();
		log.info("Deploying application {}", earFile);
		appManager.deploy(earFile, applicationName, cluster);
	}

}
