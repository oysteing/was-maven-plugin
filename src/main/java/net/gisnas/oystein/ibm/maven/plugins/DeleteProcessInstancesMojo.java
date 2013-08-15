package net.gisnas.oystein.ibm.maven.plugins;

import net.gisnas.oystein.ibm.BfmManager;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Delete BPC process instances by force (terminate if necessary)
 * 
 * Warning: not meant for use in production environments. Process instances
 * might be deleted unintentionally. Use versioning and/or process migration
 * with the normal {@link DeployMojo} goal instead. Intended for use in test
 * environments where developmentServer=false
 * 
 * Requires process administrator role 
 */
@Mojo(name = "delete-process-instances", requiresProject = false)
public class DeleteProcessInstancesMojo extends AbstractAppMojo {

	private static Logger log = LoggerFactory.getLogger(DeleteProcessInstancesMojo.class);

	/**
	 * Application name
	 */
	@Parameter(property = "was.applicationName", required=true)
	protected String applicationName;

	/**
	 * Endpoint URL to Business Flow Manager JAX-WS service
	 */
	@Parameter(property="was.bfmJaxwsEndpoint")
	protected String bfmJaxwsEndpoint;

	public void execute() throws MojoExecutionException, MojoFailureException {
		try {
			initialize();
			if (bfmJaxwsEndpoint == null) {
				bfmJaxwsEndpoint = "https://" + host + ":9443/BFMJAXWSAPI/BFMJAXWSService";
			}
			log.info("Deleting process instances for application {}", applicationName);
			BfmManager bfm = new BfmManager(bfmJaxwsEndpoint, username, password);
			bfm.forceDeleteAllProcessInstances(applicationName);
		} catch (RuntimeException e) {
			log.error("An error occured while deleting process instances for application {}", applicationName, e);
			throw e;
		}
	}

}
