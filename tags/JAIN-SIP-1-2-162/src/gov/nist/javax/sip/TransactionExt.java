
package gov.nist.javax.sip;

import java.security.cert.Certificate;
import java.util.List;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.sip.SipProvider;
import javax.sip.Transaction;

public interface TransactionExt extends Transaction {

    /**
     * Get the Sip Provider associated with this transaction
     */
    public SipProvider getSipProvider();

    /**
     * Returns the IP address of the upstream/downstream hop from which this message was initially received
     * @return the IP address of the upstream/downstream hop from which this message was initially received
     * @since 2.0
     */
    public String getPeerAddress();
    /**
     * Returns the port of the upstream/downstream hop from which this message was initially received
     * @return the port of the upstream/downstream hop from which this message was initially received
     * @since 2.0
     */
    public int getPeerPort();
    /**
     * Returns the name of the protocol with which this message was initially received
     * @return the name of the protocol with which this message was initially received
     * @since 2.0
     */
    public String getTransport();

    /**
     * return the ip address on which this message was initially received
     * @return the ip address on which this message was initially received
     */
    public String getHost();
    /**
     * return the port on which this message was initially received
     * @return the port on which this message was initially received
     */
    public int getPort();
    
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
   
   /**
    * Extract identities from certificates exchanged over TLS, based on guidelines
    * from draft-ietf-sip-domain-certs-04.
    * @return list of authenticated identities
    */
   public List<String> extractCertIdentities() throws SSLPeerUnverifiedException;

   /**
    * retrieve the value of release references to know if the stack performs optimizations
    * on cleanup to save on memory
    * @return release references value
    * 
    * @since 2.0
    */
   public boolean isReleaseReferences();
   
   /**
    * If set to true it will release all references that it no longer needs. This will include the reference to the
    * Request, Response, Dialogs, Any unused timers etc. This will significantly reduce memory
    * consumption under high load
    * @param releaseReferences 
    * 
    * @since 2.0
    */
   public void setReleaseReferences(boolean releaseReferences);     
   
   /**
    * Retrieve the value of Timer T2 (in ms)
    * @return T2 value (in ms)
    * 
    * @since 2.0
    */
   public int getTimerT2();
   /**
    * Sets the value of Timer T2 (in ms)
    * @param interval value of Timer T2 (in ms)
    * 
    * @since 2.0
    */
   public void setTimerT2(int interval);      
   /**
    * Retrieve the value of Timer T4 (in ms)
    * @return T4 value (in ms)
    * 
    * @since 2.0
    */
   public int getTimerT4();
   /**
    * Sets the value of Timer T4 (in ms)
    * @param interval value of Timer T4 (in ms)
    * 
    * @since 2.0
    */
   public void setTimerT4(int interval);
   
   /**
    * Sets the value of Timer D (in ms)
    * @param interval value of Timer D (in ms)
    * 
    * @since 2.0
    */
   public int getTimerD();
   /**
    * Sets the value of Timer D (in ms)
    * @param interval value of Timer D (in ms)
    * 
    * @since 2.0
    */
   public void setTimerD(int interval);
}
