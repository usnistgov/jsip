package gov.nist.javax.sip.stack;
import java.util.EventListener;

/** 
* Event listener for timeout events. Register this with the stack if you want
* to handle your own transaction timeout events (i.e want control of 
* the transaction state machine.
*
*@version  JAIN-SIP-1.1
*
*@author M. Ranganathan <mranga@nist.gov>  <br/>
*
*<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
*
*/

public interface SIPTimerListener
	extends EventListener {
	/** 
	* Invoked for clients who want to handle their own transaction
	* state machine.
	*
	*@param timerEvent is the timer event.
	*
	*/
	public void timerEvent (
		SIPTimerEvent timerEvent
	);

}
