package net.gisnas.oystein.ibm.maven.plugins;

import java.io.File;

import net.gisnas.oystein.ibm.AdminClientConnectorProperties;
import net.gisnas.oystein.ibm.AppManager;
import net.gisnas.oystein.security.TrustStoreHelper;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

import com.ibm.websphere.management.AdminClient;

public abstract class AbstractAppMojo extends AbstractMojo {

	private static org.slf4j.Logger log = LoggerFactory.getLogger(AbstractAppMojo.class);

	/**
	 * EAR file to deploy
	 */
	@Parameter(property = "was.earFile", defaultValue = "${project.build.directory}/${project.build.finalName}.ear")
	protected File earFile;

	/**
	 * Application name (overrides display-name in application.xml)
	 */
	@Parameter(property = "was.applicationName")
	protected String applicationName;

	/**
	 * Hostname of the deployment manager to connect to (SOAP_CONNECTOR_ADDRESS
	 * host)
	 */
	@Parameter(property = "was.host", defaultValue = "localhost")
	protected String host;

	/**
	 * SOAP port of the deployment manager to connect to (SOAP_CONNECTOR_ADDRESS
	 * port)
	 */
	@Parameter(property = "was.port", defaultValue = "8879")
	protected Integer port;

	/**
	 * Server id in settings.xml to retrieve username and password from
	 */
	@Parameter(property = "was.serverId", defaultValue = "${was.host}")
	protected String serverId;

	/**
	 * Username for server authentication. It is recommended to specify
	 * username/password in settings.xml instead
	 */
	@Parameter(property = "was.username")
	protected String username;

	/**
	 * Password for server authentication. It is recommended to specify
	 * username/password in settings.xml instead
	 */
	@Parameter(property = "was.password")
	protected String password;

	/**
	 * JKS trust store with CA certificates used to verify server connection
	 */
	@Parameter(property = "was.trustStore")
	protected File trustStore;

	/**
	 * Output file for trace logging. Trace logging is enabled when set.
	 */
	@Parameter(property = "was.traceFile")
	protected File traceFile;

	/**
	 * Trace according to <a href=
	 * "http://pic.dhe.ibm.com/infocenter/wasinfo/v8r5/topic/com.ibm.websphere.base.doc/ae/utrb_loglevel.html"
	 * >specification</a>
	 * 
	 * For example "*=finest" for the most verbose output. This setting only
	 * takes effect if {@link AbstractAppMojo#traceFile} is set. When not
	 * specified, trace level "*=info" is used.
	 */
	@Parameter(property = "was.traceString")
	protected String traceString;

	@Parameter(property = "debug")
	protected boolean debug;

	@Component
	private Settings settings;

	protected AppManager appManager;

	protected AdminClient adminClient;

	/**
	 * Hook for plugins to override
	 */
	protected void initialize() {
		if (debug) {
			((Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME)).setLevel(Level.DEBUG);
		}
		initializeTrustStore();
		initializeCredentials();
	}

	private void initializeTrustStore() {
		// Use trust store from classpath if it exists and there is no explicit
		// trust store
		if (trustStore == null) {
			trustStore = TrustStoreHelper.classpathTrustStore();
		}
		if (trustStore != null) {
			if (!trustStore.exists()) {
				throw new RuntimeException("Trust store " + trustStore + " doesn't exist");
			}
			System.setProperty("javax.net.ssl.trustStore", trustStore.getPath());
		}
	}

	private void initializeCredentials() {
		if (serverId != null) {
			Server server = settings.getServer(serverId);
			if (server != null) {
				username = server.getUsername();
				password = server.getPassword();
			}
		}
	}

	public final void initConnection() {
		log.info("Connecting to {}:{} using {}SOAP", host, port, (username != null ? "secured " : ""));
		AdminClientConnectorProperties properties;
		if (username != null) {
			properties = new AdminClientConnectorProperties(host, port, username, password);
		} else {
			properties = new AdminClientConnectorProperties(host, port);
		}
		adminClient = AdminClientConnectorProperties.createAdminClient(properties, traceFile, traceString);
		appManager = new AppManager(adminClient);
	}

}