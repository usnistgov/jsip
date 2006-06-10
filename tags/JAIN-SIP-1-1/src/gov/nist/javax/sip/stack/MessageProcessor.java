package gov.nist.javax.sip.stack;
import java.io.IOException;
import java.net.InetAddress;
import gov.nist.core.*;
import gov.nist.javax.sip.*;
import gov.nist.javax.sip.header.*;
import java.text.*;

/**
 * This is the Stack abstraction for the active object that waits
 * for messages to appear on the wire and processes these messages
 * by calling the MessageFactory interface to create a ServerRequest
 * or ServerResponse object. The main job of the message processor is
 * to instantiate message channels for the given transport.
 *
 * @version  JAIN-SIP-1.1 $Revision: 1.8 $ $Date: 2004-09-04 14:59:54 $
 *
 * @author M. Ranganathan <mranga@nist.gov>  <br/>
 * 
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a> 
 * <br/> Enhancements contributed by Jeff Keyser.
 */
public abstract class MessageProcessor implements Runnable {

	protected ListeningPointImpl listeningPoint;

	/** Transaction count.
	* Counter that tells if a new transaction is assigned to the message processor.
	*/
	protected int transactionCount;

	/**
	 * Get the transport string.
	 * @return A string that indicates the transport. 
	 * (i.e. "tcp" or "udp") 
	 */
	public abstract String getTransport();

	/**
	 * Get the port identifier.
	 * @return the port for this message processor. This is where you
	 * receive messages.
	 */
	public abstract int getPort();

	/**
	 * Get the SIP Stack.
	 * @return the sip stack.
	 */
	public abstract SIPMessageStack getSIPStack();

	/**
	 * Create a message channel for the specified host/port.
	 * @return New MessageChannel for this processor.
	 */
	public abstract MessageChannel createMessageChannel(HostPort targetHostPort)
		throws IOException;

	/**
	 * Create a message channel for the specified host/port.
	 * @return New MessageChannel for this processor.
	 */
	public abstract MessageChannel createMessageChannel(
		InetAddress targetHost,
		int port)
		throws IOException;

	/**
	* Get our thread. This is used for joining when 
	* message processors exit (requested by 
	* Mike Andrews).
	*/
	public abstract Thread getThread();

	/**
	 * Start our thread.
	 */
	public abstract void start() throws IOException;

	/**
	 * Stop method.
	 */
	public abstract void stop();

	/**
	 * Default target port used by this processor.  This is
	 * 5060 for TCP / UDP
	 */
	public abstract int getDefaultTargetPort();

	/**
	 * Flags whether this processor is secure or not.
	 */
	public abstract boolean isSecure();

	/**
	 * Maximum number of bytes that this processor can handle.
	 */
	public abstract int getMaximumMessageSize();

	/**
	 * Return true if there are pending messages to be processed
	 * (which prevents the message channel from being closed).
	 */
	public abstract boolean inUse();

	

	/**
	 * Get the Via header to assign for this message processor.
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

	/**
	 * Run method.
	 */
	public abstract void run();

	public ListeningPointImpl getListeningPoint() {
		return listeningPoint;
	}

	public void setListeningPoint(ListeningPointImpl lp) {
		this.listeningPoint = lp;
	}
}
/*
 * $Log: not supported by cvs2svn $
 * Revision 1.7  2004/06/21 04:59:50  mranga
 * Refactored code - no functional changes.
 *
 * Revision 1.6  2004/03/07 22:25:24  mranga
 * Reviewed by:   mranga
 * Added a new configuration parameter that instructs the stack to
 * drop a server connection after server transaction termination
 * set gov.nist.javax.sip.CACHE_SERVER_CONNECTIONS=false for this
 * Default behavior is true.
 *
 * Revision 1.5  2004/01/22 13:26:33  sverker
 * Issue number:
 * Obtained from:
 * Submitted by:  sverker
 * Reviewed by:   mranga
 *
 * Major reformat of code to conform with style guide. Resolved compiler and javadoc warnings. Added CVS tags.
 *
 * CVS: ----------------------------------------------------------------------
 * CVS: Issue number:
 * CVS:   If this change addresses one or more issues,
 * CVS:   then enter the issue number(s) here.
 * CVS: Obtained from:
 * CVS:   If this change has been taken from another system,
 * CVS:   then name the system in this line, otherwise delete it.
 * CVS: Submitted by:
 * CVS:   If this code has been contributed to the project by someone else; i.e.,
 * CVS:   they sent us a patch or a set of diffs, then include their name/email
 * CVS:   address here. If this is your work then delete this line.
 * CVS: Reviewed by:
 * CVS:   If we are doing pre-commit code reviews and someone else has
 * CVS:   reviewed your changes, include their name(s) here.
 * CVS:   If you have not had it reviewed then delete this line.
 *
 */
