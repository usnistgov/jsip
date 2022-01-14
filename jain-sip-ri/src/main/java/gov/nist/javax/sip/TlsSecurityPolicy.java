package gov.nist.javax.sip;

/**
 * An implementation of this interface can be registered with the sip stack using the 
 * configuration property gov.nist.javax.sip.TLS_SECURITY_POLICY
 */
public interface TlsSecurityPolicy {

    /**
     * Enforce any application-specific security policy for TLS clients.
     * Called when establishing an outgoing TLS connection.
     * @param transaction -- the transaction context for the connection
     * @throws SecurityException -- if the certificates extracted from the client transaction are not acceptable.
     */

    public void enforceTlsPolicy(ClientTransactionExt transaction) throws SecurityException;


}
