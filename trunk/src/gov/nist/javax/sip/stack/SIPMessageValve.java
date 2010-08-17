package gov.nist.javax.sip.stack;

import gov.nist.javax.sip.message.SIPRequest;

/**
 * This interface has a callback that is notified for every SIP request arriving at the container.
 * The callback occurs before any significat long-lived resources are allocate for this call, thus
 * it give a chance to the application to pre-process the message and filter based on some
 * application-specific algorithm. Creating and sending a stateless responce is also allowed.
 * 
 * It is useful for congestion control or header re-writing.
 * 
 * @author Vladimir Ralev <vralev@redhat.com>
 *
 */
public interface SIPMessageValve {
	/**
	 * The callback method that is called for every request before any transaction/dialog mapping
	 * or allocation occur.
	 * 
	 * @param request
	 * @param messageChannel
	 * @return
	 */
	public boolean processRequest(SIPRequest request, MessageChannel messageChannel);
}
