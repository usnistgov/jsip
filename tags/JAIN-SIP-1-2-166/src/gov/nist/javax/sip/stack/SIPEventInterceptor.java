package gov.nist.javax.sip.stack;

import javax.sip.SipStack;
import javax.sip.message.Message;
/**
 * This interface is the solution for https://jain-sip.dev.java.net/issues/show_bug.cgi?id=337
 * It allows to wrap the JSIP pipeline and execute custom analysis logic as SIP messages advance
 * through the pipeline checkpoints.
 * 
 * @author Vladimir Ralev
 *
 */
public interface SIPEventInterceptor {
	
	/**
	 * This method is called immediately after a SIP message has been parsed and before it is processed
	 * in the JAIN SIP pipeline.
	 * 
	 * @param message
	 */
	void beforeMessage(Message message);
	
	/**
	 * This message is called after the message has been processed by JAIN SIP
	 * @param message
	 */
	void afterMessage(Message message);
	
	/**
	 * This method is called when the interceptor is initialized. You can perform any initialization here.
	 * 
	 * @param stack
	 */
	public void init(SipStack stack);
	
	/**
	 * This method is called when the interceptor is about to be destroyed. You can perform any cleanup here.
	 */
	public void destroy();
}
