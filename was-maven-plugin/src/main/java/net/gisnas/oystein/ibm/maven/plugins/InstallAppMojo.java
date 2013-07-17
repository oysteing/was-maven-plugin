package net.gisnas.oystein.ibm.maven.plugins;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Install JEE application to an WebSphere Application Server deployment manager
 * 
 * @goal installApp
 * @requiresProject false
 */
public class InstallAppMojo extends AbstractMojo {

	public void execute() throws MojoExecutionException, MojoFailureException {
		System.out.println("Hello, Maven");
	}

}
