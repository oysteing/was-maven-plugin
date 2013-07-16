package net.gisnas.oystein.ibm;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.ibm.websphere.management.AdminClient;

public class AppManagementClientTest {

	private AppManagementClient amClient;

	@Before
	public void setUp() {
		AdminClientConnectorProperties properties = new AdminClientConnectorProperties("10.0.0.6", 8880, "igor", "Test1234", "/home/oysteigi/src/ibmdeploy/wsdeploy/src/test/resources/trustStore.jks");
		AdminClient adminClient = AdminClientConnectorProperties.createAdminClient(properties);
		amClient = new AppManagementClient(adminClient);
	}

	@Test(expected=RuntimeException.class)
	public void startNonExistentApplication() {
		amClient.startApplication("non_existent_app");
	}

	@Test
	public void testInstallApplication() {
//		amClient.installApplication("/home/oysteigi/src/ibmdeploy/wsdeploy/src/test/resources/echoear-0.0.1-SNAPSHOT.ear");
	}
	
	@Test
	public void uninstallApplication() {
		String appName = "echoear";
		amClient.uninstallApplication(appName);
	}

	@Test
	public void uninstallNonExistentApplication() throws InterruptedException {
		String appName = "non_existent_app";
		amClient.uninstallApplication(appName);
		Thread.sleep(20000);
		fail("Should fail");
	}

}