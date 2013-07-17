package net.gisnas.oystein.ibm;

import java.util.Set;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.websphere.management.AdminClient;
import com.ibm.websphere.management.application.client.AppDeploymentException;
import com.ibm.websphere.management.exception.ConnectorException;
import com.ibm.ws.management.application.client.AppInstallHelper;

/**
 * Management client for WebSphere applications
 */
public class AppManager {

	private static final Logger logger = LoggerFactory.getLogger(AdminClientConnectorProperties.class);

	private AppManagementClient am;
	private AdminClient adminClient;

	public AppManager(AdminClient adminClient) {
		am = new AppManagementClient(adminClient);
		this.adminClient = adminClient;
	}

	public boolean isStarted(String appName) {
		logger.debug("Checking if application {} is started", appName);
		ObjectName query;
		try {
			query = new ObjectName("WebSphere:type=Application,name=" + appName + ",*");
		} catch (MalformedObjectNameException e) {
			throw new RuntimeException("Could not query MBean", e);
		}
		try {
			Set<?> result = adminClient.queryNames(query, null);
			if (result.size() == 1) {
				logger.debug("Application {} is started", appName);
				return true;
			} else if (result.size() == 0) {
				logger.debug("Application {} is not started", appName);
				return false;
			} else {
				throw new RuntimeException("JMX query '" + query + "' returned " + result.size() + " results");
			}
		} catch (ConnectorException e) {
			throw new RuntimeException("An error occured in the communication with the deployment manager", e);
		}
	}

	/**
	 * Start application on all deployment targets, if not already running
	 * 
	 * @param appName
	 */
	public void startApplication(String appName) {
		logger.debug("Attempting to start application {}", appName);
		if (isStarted(appName)) {
			logger.debug("Application {} is already started. Doing nothing.", appName);
			return;
		}
		logger.debug("Starting application {}", appName);
		am.startApplication(appName);
		logger.info("Application {} started", appName);
	}

	/**
	 * Stop application on all deployment targets, if not already stopped
	 * 
	 * @param appName
	 */
	public void stopApplication(String appName) {
		logger.debug("Attempting to stop application {}", appName);
		if (!isStarted(appName)) {
			logger.debug("Application {} is not running. Doing nothing.", appName);
			return;
		}
		logger.debug("Stopping application {}", appName);
		am.stopApplication(appName);
		logger.info("Application {} stopped", appName);
	}

	/**
	 * Install application
	 * 
	 * Will update if the application is already installed
	 * 
	 * @param earPath
	 */
	public void installApplication(String earPath) {
		logger.debug("Installation of {} started", earPath);
		try {
			String appName = AppInstallHelper.getAppDisplayName(AppInstallHelper.getEarFile(earPath, false, false, null), null);
			boolean appExists = am.checkIfAppExists(appName);
			am.installApplication(earPath, appExists, appName);
			if (!isStarted(appName)) {
				am.startApplication(appName);
			}
			logger.info("Application {} installed successfully", appName);
		} catch (AppDeploymentException e) {
			throw new RuntimeException("An error occured while reading EAR file " + earPath, e);
		}
	}

	/**
	 * Uninstall application
	 * 
	 * @param appName
	 */
	public void uninstallApplication(String appName) {
		logger.debug("Uninstallation of {} started", appName);
		boolean appExists = am.checkIfAppExists(appName);
		if (appExists) {
			am.uninstallApplication(appName);
		}
		logger.info("Application {} uninstalled successfully", appName);
	}
}