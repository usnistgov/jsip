package gov.nist.javax.sip;
import javax.sip.*;
import gov.nist.javax.sip.stack.*;
import java.io.*;

/** Implementation of the ListeningPoint interface
*
*@version  JAIN-SIP-1.1
*
*@author M. Ranganathan <mranga@nist.gov>  <br/>
*
*<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
*
*/
public class ListeningPointImpl implements javax.sip.ListeningPoint {
    
    protected String host;
    
    protected String transport;
    
    /** My port. (same thing as in the message processor) */

    int port;
   
    /** pointer to the imbedded mesage processor.
     */
    protected MessageProcessor messageProcessor;
    
    /** Provider back pointer
     */    
    protected SipProviderImpl sipProviderImpl;
   
    /** Our stack
     */    
    protected SipStackImpl sipStack;
    
   
    
    /** Construct a key to refer to this structure from the SIP stack
     * @param host host string
     * @param port port
     * @param transport transport
     * @return a string that is used as a key
     */    
    public static String makeKey(String host, int port, String transport) {
        return new StringBuffer(host).
                    append(":").
                    append(port).
                    append("/").
                    append(transport).
                    toString().
                    toLowerCase();
    }
    
    /** Get the key for this strucut
     * @return  get the host
     */    
    protected String getKey() {
        return makeKey(host,port,transport);
    }
    
    
    /** set the sip provider for this structure.
     * @param sipProviderImpl provider to set
     */    
    protected void setSipProvider(SipProviderImpl sipProviderImpl) {
        this.sipProviderImpl = sipProviderImpl;
    }
    
    /** remove the sip provider from this listening point.
     */    
    protected void removeSipProvider() { 
        this.sipProviderImpl = null;
    }
    
    /** Constructor
     * @param sipStack Our sip stack
     */    
    protected  ListeningPointImpl(SipStack sipStack) {
        this.sipStack = (SipStackImpl) sipStack;
	this.host = sipStack.getIPAddress();
    }
    
    
  
    
    /** clone this listening point. Note that a message Processor is not
     *started. The transport is set to null.
     * @return cloned listening point.
     */    
    public Object clone() {
        
        ListeningPointImpl lip = new ListeningPointImpl(this.sipStack);
        lip.sipStack = this.sipStack;
        lip.port  = this.port;
        lip.transport = null;
        return lip;
        
    }
    
    
    /** Gets host name of this ListeningPoint
     *
     * @return host of ListeningPoint
     */
    public String getHost() {
        return this.sipStack.getHostAddress();
    }
    
    /** Gets the port of the ListeningPoint. The default port of a ListeningPoint
     * is dependent on the scheme and transport.  For example:
     * <ul>
     * <li>The default port is 5060 if the transport UDP the scheme is <i>sip:</i>.
     * <li>The default port is 5060 if the transport is TCP the scheme is <i>sip:</i>.
     * <li>The default port is 5060 if the transport is SCTP the scheme is <i>sip:</i>.
     * <li>The default port is 5061 if the transport is TLS over TCP the scheme is <i>sip:</i>.
     * <li>The default port is 5061 if the transport is TCP the scheme is <i>sips:</i>.
     * </ul>
     *
     * @return port of ListeningPoint
     */
    public int getPort() {
        return messageProcessor.getPort();
    }
    
    /** Gets transport of the ListeningPoint.
     *
     * @return transport of ListeningPoint
     */
    public String getTransport() {
        return messageProcessor.getTransport();
    }

   /** Get the provider.
    *
    *@return the provider.
    */
    public SipProviderImpl getProvider() {
		return this.sipProviderImpl;
    }
    
    
}
