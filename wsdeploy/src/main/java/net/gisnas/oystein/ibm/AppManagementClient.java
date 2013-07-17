package net.gisnas.oystein.ibm;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import javax.management.InstanceNotFoundException;
import javax.management.MalformedObjectNameException;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.websphere.management.AdminClient;
import com.ibm.websphere.management.application.AppConstants;
import com.ibm.websphere.management.application.AppManagement;
import com.ibm.websphere.management.application.AppManagementProxy;
import com.ibm.websphere.management.application.AppNotification;
import com.ibm.websphere.management.exception.AdminException;
import com.ibm.websphere.management.exception.ConnectorException;

/**
 * Low level management client for WebSphere applications
 * 
 * Convenience wrapper around {@link AppManagementProxy} Adds basic exception
 * handling and logging For higher level operations, see {@link AppManager}
 */
public class AppManagementClient implements NotificationListener {

	private static final Logger logger = LoggerFactory.getLogger(AdminClientConnectorProperties.class);
	private static final long MAX_WAIT_TIME = 86400000L;

	private AppManagement proxy;
	private AdminClient adminClient;

	private String appNotificationStatus;
	private String appNotificationTask;

	private String getAppNotificationStatus(String task) {
		while ((!task.equals(appNotificationTask) || !AppNotification.STATUS_COMPLETED.equals(appNotificationStatus))
				&& !AppNotification.STATUS_FAILED.equals(appNotificationStatus)) {
			try {
				wait(MAX_WAIT_TIME);
			} catch (InterruptedException e) {
				logger.trace("Thread was interrupted while waiting for appNotificationStatus");
			}
		}
		return appNotificationStatus;
	}

	private synchronized void setAppNotificationStatus(String appNotificationStatus, String taskName) {
		synchronized (this) {
			this.appNotificationStatus = appNotificationStatus;
			this.appNotificationTask = taskName;
			notify();
		}
	}

	public AppManagementClient(AdminClient adminClient) {
		this.adminClient = adminClient;
		try {
			proxy = AppManagementProxy.getJMXProxyForClient(adminClient);
		} catch (Exception e) {
			throw new RuntimeException("Could not obtain JMX proxy AppManagement", e);
		}
	}

	protected AppManagement getProxy() {
		return proxy;
	}

	private ObjectName getMBean() {
		try {
			ObjectName query = new ObjectName("WebSphere:type=AppManagement,*");
			Iterator<?> iter = adminClient.queryNames(query, null).iterator();
			if (!iter.hasNext()) {
				throw new RuntimeException("MBean not found with query " + query);
			}
			return (ObjectName) iter.next();
		} catch (MalformedObjectNameException e) {
			throw new RuntimeException("Could not query MBean", e);
		} catch (ConnectorException e) {
			throw new RuntimeException("An error occured in the communication with the deployment manager", e);
		}
	}

	private Set<?> getServers() {
		try {
			ObjectName query = new ObjectName("WebSphere:type=Server,*");
			Set<?> result = adminClient.queryNames(query, null);
			return result;
		} catch (MalformedObjectNameException e) {
			throw new RuntimeException("An error occured while querying servers", e);
		} catch (ConnectorException e) {
			throw new RuntimeException("An error occured in the communication with the deployment manager", e);
		}
	}

	private Set<?> getClusters() {
		try {
			ObjectName query = new ObjectName("WebSphere:type=Cluster,*");
			Set<?> result = adminClient.queryNames(query, null);
			return result;
		} catch (MalformedObjectNameException e) {
			throw new RuntimeException("An error occured while querying clusters", e);
		} catch (ConnectorException e) {
			throw new RuntimeException("An error occured in the communication with the deployment manager", e);
		}
	}

	/**
	 * Start application on all deployment targets
	 * 
	 * @param appName
	 * @see AppManagement#startApplication(String, java.util.Hashtable, String)
	 */
	public void startApplication(String appName) {
		try {
			String result = proxy.startApplication(appName, null, null);
			if (result == null) {
				throw new RuntimeException("Could not start application '" + appName + "'. Please consult server logs");
			}
			logger.debug("startApplication result: {}", result);
		} catch (AdminException e) {
			throw new RuntimeException("Could not start application", e);
		}
	}

	/**
	 * Stop application on all deployment targets
	 * 
	 * @param appName
	 * @see AppManagement#stopApplication(String, java.util.Hashtable, String)
	 */
	public void stopApplication(String appName) {
		try {
			String result = proxy.stopApplication(appName, null, null);
			if (result == null) {
				throw new RuntimeException("Could not stop application '" + appName + "'. Please consult server logs");
			}
			logger.debug("stopApplication result: {}", result);
		} catch (AdminException e) {
			throw new RuntimeException("Could not start application", e);
		}
	}

	/**
	 * Install application on deployment manager with one cluster or one server
	 * 
	 * If there are multiple servers and one clusters, the cluster will be
	 * chosen as the deployment target.
	 * 
	 * @param earFile
	 * @param redeploy
	 * @param appName
	 */
	public void installApplication(String earFile, boolean redeploy, String appName) {
		Set<?> servers = getServers();
		Set<?> clusters = getClusters();
		ObjectName deploymentTarget;
		if (clusters.size() == 1) {
			deploymentTarget = (ObjectName) clusters.iterator().next();
		} else {
			if (servers.size() == 1) {
				deploymentTarget = (ObjectName) servers.iterator().next();
			} else {
				throw new RuntimeException("Unambiguous server/cluster target, found " + servers.size() + " servers and " + clusters.size()
						+ " clusters. Please specifiy target");
			}
		}
		installApplication(earFile, redeploy, appName, deploymentTarget.toString());
	}

	public void installApplication(String earFile, boolean redeploy, String appName, String target) {
		try {
			Hashtable<String, String> module2server = new Hashtable<>();
			module2server.put("*", target);
			Hashtable<String, Object> props = new Hashtable<>();
			props.put(AppConstants.APPDEPL_ARCHIVE_UPLOAD, true);
			props.put(AppConstants.APPDEPL_MODULE_TO_SERVER, module2server);
			adminClient.addNotificationListener(getMBean(), this, null, null);
			appNotificationStatus = null;
			synchronized (this) {
				if (redeploy) {
					proxy.redeployApplication(earFile, appName, props, null);
				} else {
					proxy.installApplication(earFile, props, null);
				}
				switch (getAppNotificationStatus(AppNotification.INSTALL)) {
				case AppNotification.STATUS_COMPLETED:
					logger.debug("Installation of {} completed successfully", earFile);
					break;
				case AppNotification.STATUS_FAILED:
					throw new RuntimeException("Installation of " + earFile + " failed, see log messages for details");
				default:
					throw new RuntimeException("Received no conclusive status from application installation");
				}
			}
		} catch (AdminException e) {
			throw new RuntimeException("An error occured while installing the application " + earFile, e);
		} catch (InstanceNotFoundException e) {
			throw new RuntimeException("Could not find MBean " + getMBean(), e);
		} catch (ConnectorException e) {
			throw new RuntimeException("Communication with deployment manager failed", e);
		} finally {
			try {
				adminClient.removeNotificationListener(getMBean(), this);
			} catch (Exception e) {
				logger.warn("Unable to remove notification listener: {}", e);
			}
		}
	}

	public void uninstallApplication(String appName) {
		try {
			adminClient.addNotificationListener(getMBean(), this, null, null);
			synchronized (this) {
				proxy.uninstallApplication(appName, new Hashtable<String, Object>(), null);
				switch (getAppNotificationStatus(AppNotification.UNINSTALL)) {
				case AppNotification.STATUS_COMPLETED:
					logger.debug("Uninstallation of {} completed successfully", appName);
					break;
				case AppNotification.STATUS_FAILED:
					throw new RuntimeException("Uninstallation of " + appName + " failed, see log messages for details");
				default:
					throw new RuntimeException("Received no conclusive status from application uninstallation");
				}
			}
		} catch (AdminException e) {
			throw new RuntimeException("Uninstallation of application " + appName + " failed", e);
		} catch (InstanceNotFoundException e) {
			throw new RuntimeException("MBean not found", e);
		} catch (ConnectorException e) {
			throw new RuntimeException("An error occured in the communication with the deployment manager", e);
		} finally {
			try {
				adminClient.removeNotificationListener(getMBean(), this);
			} catch (Exception e) {
				logger.warn("Unable to remove notification listener: {}", e);
			}
		}
	}

	public void handleNotification(Notification notification, Object handback) {
		AppNotification appNotification = (AppNotification) notification.getUserData();
		logger.trace("AppNotification received: {}", appNotification);
		switch (appNotification.taskStatus) {
		case AppNotification.STATUS_INPROGRESS:
			logger.debug("{}", appNotification.message);
			break;
		case AppNotification.STATUS_COMPLETED:
			logger.debug("{}", appNotification.message);
			setAppNotificationStatus(appNotification.taskStatus, appNotification.taskName);
			break;
		case AppNotification.STATUS_WARNING:
			logger.warn("{}", appNotification.message);
			break;
		case AppNotification.STATUS_FAILED:
			logger.error("{}", appNotification.message);
			setAppNotificationStatus(appNotification.taskStatus, appNotification.taskName);
			break;
		default:
			logger.warn("Uknown status for AppNotification {}", appNotification);
		}
	}

	public boolean checkIfAppExists(String appName) {
		try {
			return proxy.checkIfAppExists(appName, null, null);
		} catch (AdminException e) {
			throw new RuntimeException("Unable to check if app " + appName + " exists", e);
		}
	}

}