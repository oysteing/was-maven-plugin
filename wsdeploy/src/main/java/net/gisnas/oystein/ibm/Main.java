package net.gisnas.oystein.ibm;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.ibm.websphere.management.AdminClient;


public class Main {

	private static Logger LOGGER = Logger.getLogger(Main.class.getName());
	
	public static void main(String[] args) throws Exception {
		System.out.println("Hei");
		LOGGER.setLevel(Level.FINE);

		AdminClient adminClient = AdminClientConnectorProperties.createAdminClient(new AdminClientConnectorProperties("igor", "Test1234"));
		AppManager am = new AppManager(adminClient);
		String appName = "echoear";
		am.startApplication(appName);
	}


}