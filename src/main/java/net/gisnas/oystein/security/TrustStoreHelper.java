package net.gisnas.oystein.security;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import net.gisnas.oystein.ibm.maven.plugins.DeployMojo;

import org.codehaus.plexus.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TrustStoreHelper {

	private static Logger log = LoggerFactory.getLogger(TrustStoreHelper.class);

	/**
	 * Copy trust store from classpath to temp file
	 * 
	 * @return trustStore file to use. Null if there is no trust store on the classpath
	 */
	public static File classpathTrustStore() {
		try {
			URL defaultTrustStore = DeployMojo.class.getClassLoader().getResource("trustStore.jks");
			if (defaultTrustStore == null) {
				return null;
			} else {
				File tempTrustStore = File.createTempFile("trustStore", ".jks");
				log.debug("Copying truststore file from  {} to {}", defaultTrustStore, tempTrustStore);
				FileUtils.copyURLToFile(defaultTrustStore, tempTrustStore);
				tempTrustStore.deleteOnExit();
				return tempTrustStore;
			}
		} catch (IOException e) {
			throw new RuntimeException("Unable to copy truststore file to tempfile", e);
		}
	}

}
