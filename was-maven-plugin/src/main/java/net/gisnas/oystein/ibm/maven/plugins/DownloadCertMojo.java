package net.gisnas.oystein.ibm.maven.plugins;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Retrieve SSL certificate from the deployment manager and add it to trust
 * store as a trusted CA
 * 
 * @goal downloadCert
 * @requiresProject false
 */
public class DownloadCertMojo extends AbstractAppMojo {

	public void execute() throws MojoExecutionException, MojoFailureException {
		if (trustStore == null) {
			throw new RuntimeException("The property trustStore must be set. Exampke: mvn was:downloadCert -Dwas.trustStore=trustStore.jks");
		}
		getLog().info("Retrieving certificate from " + host + ":" + port);
		X509Certificate certificate = retrieveCAFromSSLHandshake();
		addCAToTrustStore(certificate);
	}

	private X509Certificate retrieveCAFromSSLHandshake() {
		SSLSocket socket = null;
		try {
			SSLContext context = SSLContext.getInstance("TLS");
			SavingTrustManager tm = new SavingTrustManager();
			context.init(null, new TrustManager[] { tm }, null);
			socket = (SSLSocket) context.getSocketFactory().createSocket(host, port);
			try {
				socket.startHandshake();
			} catch (IOException e) {
				getLog().debug("Server certificate is not trusted, will try to add CA to keystore");
			}

			// Choose last cert in chain and hope that this is root CA
			return tm.chain[tm.chain.length - 1];
		} catch (IOException | NoSuchAlgorithmException | KeyManagementException e) {
			throw new RuntimeException("An error occured while trying to establish SSL connectioni with " + host, e);
		} finally {
			if (socket != null) {
				try {
					socket.close();
				} catch (IOException e) {
					getLog().debug("Ignore error when closing socket", e);
				}
			}
		}
	}

	private void addCAToTrustStore(X509Certificate certificate) {
		try {
			KeyStore keyStore = KeyStore.getInstance("JKS");
			if (trustStore.exists()) {
				FileInputStream fis = null;
				try {
					fis = new FileInputStream(trustStore);
					keyStore.load(fis, null);
				} finally {
					if (fis != null) {
						fis.close();
					}
				}
			} else {
				keyStore.load(null, null);
			}

			keyStore.setCertificateEntry(String.valueOf(keyStore.size()), certificate);
			getLog().info("Added certificate (" + certificate.getSubjectX500Principal() + ") to " + trustStore + " with alias " + keyStore.size());
			FileOutputStream fos = null;
			try {
				fos = new FileOutputStream(trustStore);
				keyStore.store(fos, "Test1234".toCharArray());
			} finally {
				if (fos != null) {
					fos.close();
				}
			}
		} catch (NoSuchAlgorithmException | CertificateException | IOException | KeyStoreException e) {
			throw new RuntimeException("An error occured while adding certificate to keystore " + trustStore, e);
		}
	}

	private static class SavingTrustManager implements X509TrustManager {

		private X509Certificate[] chain;

		@Override
		public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
			throw new UnsupportedOperationException();
		}

		@Override
		public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
			this.chain = chain;
		}

		@Override
		public X509Certificate[] getAcceptedIssuers() {
			throw new UnsupportedOperationException();
		}
	}

}
