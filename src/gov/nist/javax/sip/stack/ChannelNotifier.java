package gov.nist.javax.sip.stack;

/**
* An object that provides notification back to the application on new channel
* creates and deletes. This is useful for firewall implementation using 
* TCP where  such notifications can be used for firewall control.  
* The implmentation of this is provided by the application.
*
*@version  JAIN-SIP-1.1
*
*@author M. Ranganathan <mranga@nist.gov>  <br/>
*
*<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
*
*/

public interface ChannelNotifier {
 /**    
 * Notify application on channel open.
 * @param messageChannel MessageChannel that was opened
 */
	public void notifyOpen(MessageChannel messageChannel);
/** Notify application on channel close
 * @param messageChannel messageChannel that was closed.
 */        
	public void notifyClose(MessageChannel messageChannel);
}
