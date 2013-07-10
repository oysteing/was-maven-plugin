package net.gisnas.oystein.ibm;

import java.util.logging.Logger;

import com.ibm.websphere.management.AdminClient;
import com.ibm.websphere.management.application.AppManagement;
import com.ibm.websphere.management.application.AppManagementProxy;
import com.ibm.websphere.management.exception.AdminException;

/**
 * Low level management client for WebSphere applications
 * 
 * Convenience wrapper around {@link AppManagementProxy}
 * Adds basic exception handling and logging
 * For higher level operations, see {@link AppManager}
 */
public class AppManagementClient {

	private static Logger LOGGER = Logger.getLogger(AppManagementClient.class.getName());
	
	private AppManagement proxy;

	public AppManagementClient(AdminClient adminClient) {
		try {
			proxy = AppManagementProxy.getJMXProxyForClient(adminClient);
		} catch (Exception e) {
			throw new RuntimeException("Could not obtain JMX proxy AppManagement", e);
		}
	}
	
	protected AppManagement getProxy() {
		return proxy;
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
			LOGGER.info("startApplication result: "+ result);
		} catch (AdminException e) {
			throw new RuntimeException("Could not start application", e);
		}
	}

//	public void registerNotificationListener() throws MalformedObjectNameException, ConnectorException, InstanceNotFoundException {
//		ObjectName on = new ObjectName ("WebSphere:type=AppManagement,*");
//		Iterator iter = adminClient.queryNames (on, null).iterator();
//		appmgmtON = (ObjectName)iter.next();
//		adminClient.addNotificationListener(appmgmtON, this, null, null);
//	}
//
//	@Override
//	public void handleNotification(Notification arg0, Object arg1) {
//		System.out.println("Hei!!!");
//	}
}