package gov.nist.javax.sip.stack;
import  java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import gov.nist.javax.sip.address.*;
import gov.nist.core.*;
import gov.nist.javax.sip.*;
import gov.nist.javax.sip.header.*;
import java.text.*;

/** This is the Stack abstraction for the active object that waits
* for messages to appear on the wire and processes these messages
* by calling the MessageFactory interface to create a ServerRequest
* or ServerResponse object. The main job of the message processor is
* to instantiate message channels for the given transport.
*
*@version  JAIN-SIP-1.1
*@author M. Ranganathan <mranga@nist.gov>  <br/>
*<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a> 
*<br/> Enhancements contributed by Jeff Keyser.
*
*/

public abstract class MessageProcessor  implements Runnable {
	

	protected ListeningPointImpl listeningPoint;
	/**
         * Get the transport string.
         * @return A string that indicates the transport. 
         * (i.e. "tcp" or "udp") 
          */
	public abstract String getTransport();

	/**
	* Get the port identifier.
	*@return the port for this message processor. This is where you
        * receive messages.
	*/
	public abstract int getPort();

	/**
	* Get the SIP Stack.
	*@return the sip stack.
	*/
	public abstract SIPStack getSIPStack();

	/**
	* Create a message channel for the specified host/port.
	*@return New MessageChannel for this processor.
	*/
	public abstract MessageChannel 
		createMessageChannel(HostPort targetHostPort) 
		throws IOException;
	/**
	* Create a message channel for the specified host/port.
	*@return New MessageChannel for this processor.
	*/
	public abstract 
	  MessageChannel createMessageChannel(InetAddress targetHost,
			int port) throws IOException;

	/** Start our thread.
	*/
	public abstract void start() throws IOException;

	/** Stop method.
	*/
	public abstract void stop();

	/** Default target port used by this processor.  This is
	 * 5060 for TCP / UDP
	 */
	public abstract int getDefaultTargetPort();

	/** Flags whether this processor is secure or not.
	 */
	public abstract boolean isSecure();

	/** Maximum number of bytes that this processor can handle.
	 */
	public abstract int getMaximumMessageSize();

	/** Return true if there are pending messages to be processed
	* (which prevents the message channel from being closed).
	*/
	public abstract boolean inUse();

	/** Get the Via header to assign for this message processor.
	*/
	public Via getViaHeader() {
	    try {
		Via via = new Via();
		Host host = new Host();
		host.setHostname(this.getSIPStack().getHostAddress());
		via.setHost(host);
		via.setPort(this.getPort());
		via.setTransport(this.getTransport());
		return via;
	    } catch (ParseException ex) {
			ex.printStackTrace();
			return null;
	    }
	}

	/** Run method.
	*/
	public abstract void run();

	public ListeningPointImpl getListeningPoint() { 
		return listeningPoint;
	}

	public void setListeningPoint(ListeningPointImpl lp) {
		this.listeningPoint = lp;
	}

}
