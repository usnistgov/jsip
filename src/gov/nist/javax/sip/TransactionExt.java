
package gov.nist.javax.sip;

import javax.sip.SipProvider;

public interface TransactionExt {
    
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
}
