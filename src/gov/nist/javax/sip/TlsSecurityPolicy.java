package gov.nist.javax.sip;

import java.io.IOException;

import javax.sip.ClientTransaction;

/**
 * An implementation of this interface can be registered with the sip stack using the 
 * configuration property gov.nist.javax.sip.TLS_SECURITY_POLICY
 */
public interface TlsSecurityPolicy {

    /**
     * Enforce any application-specific security policy for TLS clients.
     * Called when establishing an outgoing TLS connection.
     * @param transaction -- the transaction context for the connection
     * @throws IOException -- if the certificates extracted from the client transacton are not acceptable.
     */
    void enforceTlsPolicy(ClientTransactionExt transaction) throws IOException;

}
