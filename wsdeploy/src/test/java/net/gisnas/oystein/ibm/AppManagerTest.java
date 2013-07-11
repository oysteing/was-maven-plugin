package net.gisnas.oystein.ibm;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.ibm.websphere.management.AdminClient;

public class AppManagerTest {

	AppManager am;

	@Before
	public void setUp() {
		AdminClientConnectorProperties properties = new AdminClientConnectorProperties("10.0.0.6", 8880, "igor", "Test1234", "/home/oysteigi/src/ibmdeploy/wsdeploy/src/test/resources/trustStore.jks");
		AdminClient adminClient = AdminClientConnectorProperties.createAdminClient(properties);
		am = new AppManager(adminClient);
	}

	@Test
	public void nonExistentNotStarted() {
		Assert.assertFalse(am.isStarted("non_existent_app"));
	}
	
	@Test
	public void isStarted() {
		Assert.assertTrue(am.isStarted("echoear"));
	}

	@Test
	public void startApplication() {
		String appName = "echoear";
		am.startApplication(appName);
		Assert.assertTrue(am.isStarted(appName));
	}

}
