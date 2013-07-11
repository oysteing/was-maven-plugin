package net.gisnas.oystein.ibm;

import com.ibm.websphere.management.AdminClient;


public class Main {

	public static void main(String[] args) throws Exception {
		AdminClientConnectorProperties properties = new AdminClientConnectorProperties("10.0.0.6", 8880, "igor", "Test1234", "/home/oysteigi/src/ibmdeploy/wsdeploy/src/test/resources/trustStore.jks");
		AdminClient adminClient = AdminClientConnectorProperties.createAdminClient(properties);
		AppManager am = new AppManager(adminClient);
		String appName = "echoear";
		am.startApplication(appName);
	}

}