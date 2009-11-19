package gov.nist.javax.sip;

import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.sip.ClientTransaction;
import javax.sip.Timeout;
import javax.sip.address.Hop;

public interface ClientTransactionExt extends ClientTransaction, TransactionExt {

    /**
     * Notify on retransmission from the client transaction side. The listener will get a
     * notification on retransmission when this flag is set. When set the client transaction
     * listener will get a Timeout.RETRANSMIT event on each retransmission.
     * 
     * @param flag -- the flag that indicates whether or not notification is desired.
     * 
     * @since 2.0
     */
    public void setNotifyOnRetransmit(boolean flag);

    /**
     * Send a transaction timeout event to the application if Tx is still in Calling state in the
     * given time period ( in base timer interval count ) after sending request. The stack will
     * start a timer and alert the application if the client transaction does not transition out
     * of the Trying state by the given interval. This is a "one shot" alert.
     * 
     * @param count -- the number of base timer intervals after which an alert is issued.
     * 
     * 
     * @since 2.0
     */
    public void alertIfStillInCallingStateBy(int count);
    
    /**
     * Get the next hop that was computed by the routing layer.
     * when it sent out the request. This allows you to route requests
     * to the SAME destination if required ( for example if you get
     * an authentication challenge ).
     */
    public Hop getNextHop();
    
    /**
     * Return true if this Ctx is a secure transport.
     * 
     */
    public boolean isSecure();
    
    /**
     * Return the Cipher Suite that was used for the SSL handshake. 
     * 
     * @return     Returns the cipher suite in use by the session which was produced by the handshake.
     * @throw UnsupportedOperationException if this is not a secure client transaction.
     */
    public String getCipherSuite() throws UnsupportedOperationException;
    
    /**
     * Get the certificate(s) that were sent to the peer during handshaking.
     *@return the certificate(s) that were sent to the peer during handshaking.
     *@throw UnsupportedOperationException if this is not a secure client transaction.
     * 
     */
   Certificate[] getLocalCertificates() throws UnsupportedOperationException;
    
    /**
     * @return the identity of the peer which was identified as part of defining the session.
     * @throws SSLPeerUnverifiedException 
     * @throw UnsupportedOperationException if this is not a secure client transaction.
     */
   Certificate[]  getPeerCertificates() throws SSLPeerUnverifiedException;
   
   

}
