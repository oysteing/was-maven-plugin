package net.gisnas.oystein.ibm.maven.plugins;

import java.io.File;

import net.gisnas.oystein.ibm.AdminClientConnectorProperties;
import net.gisnas.oystein.ibm.AppManager;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;

public abstract class AbstractAppMojo extends AbstractMojo {

	/**
	 * EAR file to deploy
	 * 
	 * @parameter expression="${was.earFile}" default-value=
	 *            "${project.build.directory}/${project.build.finalName}.ear"
	 */
	protected File earFile;

	/**
	 * Application name (overrides display-name in application.xml)
	 * 
	 * @parameter expression="${was.applicationName}"
	 */
	protected String applicationName;

	/**
	 * Hostname of the deployment manager to connect to (SOAP_CONNECTOR_ADDRESS
	 * host)
	 * 
	 * @parameter expression="${was.host}" default-value="localhost"
	 */
	protected String host;

	/**
	 * SOAP port of the deployment manager to connect to (SOAP_CONNECTOR_ADDRESS
	 * port)
	 * 
	 * @parameter expression="${was.port}" default-value="8879"
	 */
	protected Integer port;

	/**
	 * Server id in settings.xml to retrieve username and password from
	 * 
	 * @parameter expression="${was.serverId}" default-value="${was.host}"
	 */
	protected String serverId;

	/**
	 * Username for server authentication. It is recommended to specify
	 * username/password in settings.xml instead
	 * 
	 * @parameter expression="${was.username}
	 */
	protected String username;

	/**
	 * Password for server authentication. It is recommended to specify
	 * username/password in settings.xml instead
	 * 
	 * @parameter expression="${was.password}"
	 */
	protected String password;

	/**
	 * JKS trust store with CA certificates used to verify server connection
	 * 
	 * @parameter expression="${was.trustStore}"
	 */
	protected File trustStore;

	/**
	 * Output file for trace logging. Trace logging is enabled when set.
	 * 
	 * @parameter expression="${was.traceFile}"
	 */
	protected File traceFile;

	/**
	 * Trace specification according to {@link http://pic.dhe.ibm.com/infocenter/wasinfo/v8r5/topic/com.ibm.websphere.base.doc/ae/utrb_loglevel.html}
	 * 
	 * For example "*=finest" for the most verbose output. This setting only
	 * takes effect if {@link AbstractAppMojo#traceFile} is set. When not
	 * specified, trace level "*=info" is used.
	 * 
	 * @parameter expression="${was.traceString}"
	 */
	protected String traceString;

	/**
	 * @parameter expression="${settings}"
	 * @required
	 * @readonly
	 */
	private Settings settings;

	protected AppManager appManager;

	public final void initConnection() {
		if (serverId != null) {
			Server server = settings.getServer(serverId);
			if (server != null) {
				username = server.getUsername();
				password = server.getPassword();
			}
		}
		getLog().info("Connecting to " + host + ":" + port + " using " + (username != null ? "secured " : "") + "SOAP");
		AdminClientConnectorProperties properties;
		if (username != null) {
			properties = new AdminClientConnectorProperties(host, port, username, password, trustStore);
		} else {
			properties = new AdminClientConnectorProperties(host, port);
		}
		appManager = new AppManager(AdminClientConnectorProperties.createAdminClient(properties, traceFile, traceString));
	}

}