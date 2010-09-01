package gov.nist.javax.sip.stack;

import javax.sip.SipStack;
import javax.sip.message.Request;
import javax.sip.message.Response;

import gov.nist.javax.sip.message.SIPRequest;

/**
 * This interface has callbacks that are notified for every SIP message arriving at the container.
 * The callbacks occurs before any significant long-lived resources are allocated for this call, thus
 * it gives a chance to the application to pre-process the message and filter based on some
 * application-specific algorithm. Creating and sending a stateless response is also allowed.
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
	
	/**
	 * The callback method that is called for every response before any transaction/dialog mapping
	 * or allocation occur.
	 * 
	 * @param response
	 * @param messageChannel
	 * @return
	 */
	public boolean processResponse(Response response, MessageChannel messageChannel);
	
	/**
	 * This method is called when the valve is initialized. You can perform any initialization here.
	 * 
	 * @param stack
	 */
	public void init(SipStack stack);
	
	/**
	 * This method is called when the valve is about to be destroyed. You can perform any cleanup here.
	 */
	public void destroy();
}
