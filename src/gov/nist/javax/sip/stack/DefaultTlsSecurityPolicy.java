package gov.nist.javax.sip.stack;

import gov.nist.javax.sip.ClientTransactionExt;
import gov.nist.javax.sip.TlsSecurityPolicy;

import java.io.IOException;

import javax.sip.ClientTransaction;

public class DefaultTlsSecurityPolicy implements TlsSecurityPolicy {

    /**
     * Enforce any application-specific security policy for TLS clients.
     * Called when establishing an outgoing TLS connection.
     * @param transaction -- the transaction context for the connection
     */
    public void enforceTlsPolicy(ClientTransactionExt transaction) throws IOException
    {
     
    }

}
