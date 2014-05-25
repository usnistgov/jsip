package gov.nist.core.net;

import gov.nist.core.CommonLogger;
import gov.nist.core.LogWriter;
import gov.nist.core.StackLogger;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.Security;
import java.util.Properties;

/**
 * Implement the default TLS security policy by loading kays specified in stack
 * properties or system -D settings.
 * 
 * @author Alexader Saveliev (Avistar)
 * @author Jose M Recio (josemrecio@gmail.com)
 *
 */
public class DefaultSecurityManagerProvider implements SecurityManagerProvider {

    private static final StackLogger logger = CommonLogger.getLogger(DefaultSecurityManagerProvider.class);

    private KeyManagerFactory keyManagerFactory;
    private TrustManagerFactory trustManagerFactory;

    public DefaultSecurityManagerProvider() {
    }

    public void init(Properties properties)
            throws GeneralSecurityException, IOException {
        // required, could use default keyStore, but it is better practice to explicitly specify
        final String keyStoreFilename = properties.getProperty("javax.net.ssl.keyStore");
        // required
        final String keyStorePassword = properties.getProperty("javax.net.ssl.keyStorePassword");
        // optional, uses default if not specified 
        String keyStoreType = properties.getProperty("javax.net.ssl.keyStoreType");
        if (keyStoreType == null) {
            keyStoreType = KeyStore.getDefaultType();
            logger.logWarning("Using default keystore type " + keyStoreType);
        }
        if (keyStoreFilename == null || keyStorePassword == null) {
            logger.logWarning("TLS server settings will be inactive - TLS key store will use JVM defaults"
                    + " keyStoreType=" +  keyStoreType
                    + " javax.net.ssl.keyStore=" + keyStoreFilename
                    + " javax.net.ssl.keyStorePassword=" + (keyStorePassword == null? null: "***"));
        }

        // required, could use default trustStore, but it is better practice to explicitly specify
        final String trustStoreFilename = properties.getProperty("javax.net.ssl.trustStore");
        // optional, if not specified using keyStorePassword
        String trustStorePassword = properties.getProperty("javax.net.ssl.trustStorePassword");
        if(trustStorePassword == null) {
        	logger.logInfo("javax.net.ssl.trustStorePassword is null, using the password passed through javax.net.ssl.keyStorePassword");
        	trustStorePassword = keyStorePassword;
        }
        // optional, uses default if not specified 
        String trustStoreType = properties.getProperty("javax.net.ssl.trustStoreType");
        if (trustStoreType == null) {
            trustStoreType = KeyStore.getDefaultType();
            logger.logWarning("Using default truststore type " + trustStoreType);
        }
        if (trustStoreFilename == null || trustStorePassword == null) {
            logger.logWarning("TLS trust settings will be inactive - TLS trust store will use JVM defaults."
                    + " trustStoreType=" +  trustStoreType
                    + " javax.net.ssl.trustStore=" +  trustStoreFilename
                    + " javax.net.ssl.trustStorePassword=" + (trustStorePassword == null? null: "***"));
        }

        String algorithm = Security.getProperty("ssl.KeyManagerFactory.algorithm");
        if (algorithm == null) {
            algorithm = "SunX509";
        }
        if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
            logger.logDebug("SecurityManagerProvider " + this.getClass().getCanonicalName() + " will use algorithm " + algorithm);
        }
        
        keyManagerFactory = KeyManagerFactory.getInstance(algorithm);
        if(keyStoreFilename != null) {
        	final KeyStore ks = KeyStore.getInstance(keyStoreType);
        	ks.load(new FileInputStream(new File(keyStoreFilename)), keyStorePassword.toCharArray());
        	
        	keyManagerFactory.init(ks, keyStorePassword.toCharArray());
        } else {
        	keyManagerFactory.init(null, null);
        }

        trustManagerFactory = TrustManagerFactory.getInstance(algorithm);
        if(trustStoreFilename != null) {
        	final KeyStore ts = KeyStore.getInstance(trustStoreType);
        	ts.load(new FileInputStream(new File(trustStoreFilename)), trustStorePassword.toCharArray());
        	
        	trustManagerFactory.init((KeyStore) ts);
        } else {
        	trustManagerFactory.init((KeyStore)null);
        }
        if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
        	logger.logDebug("TLS settings OK. SecurityManagerProvider " + this.getClass().getCanonicalName() + " initialized.");
        }
    }

    public KeyManager[] getKeyManagers(boolean client) {
        if(keyManagerFactory == null) return null;
        return keyManagerFactory.getKeyManagers();
    }

    public TrustManager[] getTrustManagers(boolean client) {
        if(trustManagerFactory == null) return null;
        return trustManagerFactory.getTrustManagers();
    }
}
