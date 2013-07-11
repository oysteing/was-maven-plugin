package net.gisnas.oystein.ibm;

import java.util.Set;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.websphere.management.AdminClient;
import com.ibm.websphere.management.exception.ConnectorException;

/**
 * Management client for WebSphere applications
 */
public class AppManager {

	private static final Logger logger = LoggerFactory
			.getLogger(AdminClientConnectorProperties.class);

	private AppManagementClient am;
	private AdminClient adminClient;

	public AppManager(AdminClient adminClient) {
		am = new AppManagementClient(adminClient);
		this.adminClient = adminClient;
	}

	public boolean isStarted(String appName) {
		ObjectName query;
		try {
			query = new ObjectName ("WebSphere:type=Application,name=" + appName + ",*");
		} catch (MalformedObjectNameException e) {
			throw new RuntimeException("Could not query MBean", e);
		}
		try {
			Set<?> result = adminClient.queryNames(query, null);
			if (result.size() == 1) {
				return true;
			} else if (result.size() == 0) {
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
		if (isStarted(appName)) {
			logger.debug("Application " + appName + " is already started. Doing nothing.");
			return;
		}
		logger.debug("Starting application " + appName);
		am.startApplication(appName);
		logger.debug("Application " + appName + " started");
	}

}