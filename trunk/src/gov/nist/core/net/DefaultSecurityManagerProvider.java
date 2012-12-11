package gov.nist.core.net;

import gov.nist.core.CommonLogger;
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
import java.util.Properties;

/**
 * Implement the default TLS security policy by loading kays specified in stack
 * properties or system -D settings.
 * 
 * @author Alexader Saveliev (Avistar)
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
        String passphraseString = properties.getProperty("javax.net.ssl.keyStorePassword");
        String keyStoreType = properties.getProperty("javax.net.ssl.keyStoreType");
        if (passphraseString == null)
            passphraseString = System.getProperty("javax.net.ssl.keyStorePassword");
        if (keyStoreType == null)
            keyStoreType = System.getProperty("javax.net.ssl.keyStoreType");

        KeyStore ks = KeyStore.getInstance(keyStoreType);
        KeyStore ts = KeyStore.getInstance(keyStoreType);

        char[] passphrase = passphraseString.toCharArray();

        String keyStoreFilename = properties.getProperty("javax.net.ssl.keyStore");
        String trustStoreFilename = properties.getProperty("javax.net.ssl.trustStore");

        if (keyStoreFilename == null)
            keyStoreFilename = System.getProperty("javax.net.ssl.keyStore");
        if (trustStoreFilename == null)
            trustStoreFilename = System.getProperty("javax.net.ssl.trustStore");

        if (keyStoreFilename != null && trustStoreFilename != null) {
            ks.load(new FileInputStream(new File(keyStoreFilename)), passphrase);
            ts.load(new FileInputStream(new File(trustStoreFilename)), passphrase);
            keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
            keyManagerFactory.init(ks, passphrase);

            trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
            trustManagerFactory.init(ts);
        } else {
            logger.logWarning("TLS key and trust stores are not configured. javax.net.ssl.keyStore="
                    + keyStoreFilename + " javax.net.ssl.trustStore=" +
                    trustStoreFilename);

        }
    }

    public KeyManager[] getKeyManagers() {
        return keyManagerFactory.getKeyManagers();
    }

    public TrustManager[] getTrustManagers() {
        return trustManagerFactory.getTrustManagers();
    }
}
