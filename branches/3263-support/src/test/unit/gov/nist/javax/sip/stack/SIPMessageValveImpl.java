package test.unit.gov.nist.javax.sip.stack;
import gov.nist.javax.sip.SipStackImpl;
import gov.nist.javax.sip.message.SIPMessage;
import gov.nist.javax.sip.message.SIPRequest;
import gov.nist.javax.sip.stack.MessageChannel;
import gov.nist.javax.sip.stack.SIPMessageValve;

import java.io.IOException;

import javax.sip.SipStack;
import javax.sip.message.Response;

    public class SIPMessageValveImpl implements SIPMessageValve {
    	public static int lastResponseCode;
    	public static boolean inited;
    	public static boolean destroyed;

    	public boolean processRequest(SIPRequest request, MessageChannel messageChannel) {
    		try {
    			sendResponse(messageChannel, createErrorResponse(request, 603));
    			return false;
    		} catch (IOException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
    		return false;
    	}
    	
    	/**
    	 * Demonstrating how stateless response is created and sent
    	 * @param request
    	 * @param code
    	 * @return
    	 */
    	public SIPMessage createErrorResponse(SIPRequest request, int code) {
    		return request.createResponse(code);
    	}
    	
    	public void sendResponse(MessageChannel channel, SIPMessage response) throws IOException {
    		channel.sendMessage(response);
    	}

		public boolean processResponse(Response response,
				MessageChannel messageChannel) {
			lastResponseCode = response.getStatusCode();
			return true;
		}

		public void destroy() {
			destroyed = true;
		}

		public void init(SipStack stack) {
			SipStackImpl impl = (SipStackImpl) stack;
			impl.getConfigurationProperties().getProperty("keee");
			impl.getActiveClientTransactionCount();
			inited = true;
		}
    	
    	
    }