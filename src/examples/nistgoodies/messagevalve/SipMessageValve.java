package examples.nistgoodies.messagevalve;

import examples.nistgoodies.threadaudit.ThreadAudit;
import gov.nist.javax.sip.SipStackImpl;
import gov.nist.javax.sip.message.SIPRequest;
import gov.nist.javax.sip.stack.MessageChannel;
import gov.nist.javax.sip.stack.SIPMessageValve;
import gov.nist.javax.sip.stack.SIPTransactionStack;

import java.io.IOException;
import java.util.Properties;

import javax.sip.DialogTerminatedEvent;
import javax.sip.IOExceptionEvent;
import javax.sip.ListeningPoint;
import javax.sip.PeerUnavailableException;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.SipFactory;
import javax.sip.SipListener;
import javax.sip.SipProvider;
import javax.sip.SipStack;
import javax.sip.TransactionTerminatedEvent;
import javax.sip.message.Response;


public class SipMessageValve implements SIPMessageValve {
	public static class Shootme implements SipListener {


        private SipStack sipStack;

        private SipProvider sipProvider;

        private static final int myPort = 5070;
        public void processRequest(RequestEvent requestEvent) {
            System.out.println("This method will never be called because we handle the messages in " +
            		"the SipMessageValve only!");
        }

        public void processResponse(ResponseEvent responseEvent) {
            System.out.println("This method will never be called because we handle the messages in " +
            		"the SipMessageValve only!");
        }

        public void processTimeout(javax.sip.TimeoutEvent timeoutEvent) {
  
        }

        public void init() {
            SipFactory sipFactory = null;
            sipStack = null;
            sipFactory = SipFactory.getInstance();
            sipFactory.setPathName("gov.nist");
            Properties properties = new Properties();
            properties.setProperty("javax.sip.STACK_NAME", "shootme");
            // You need 16 for logging traces. 32 for debug + traces.
            // Your code will limp at 32 but it is best for debugging.
            properties.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "32");
            properties.setProperty("gov.nist.javax.sip.DEBUG_LOG",
                    "shootmedebug.txt");
            properties.setProperty("gov.nist.javax.sip.SERVER_LOG",
                    "shootmelog.txt");
            properties.setProperty("gov.nist.javax.sip.AUTOMATIC_DIALOG_ERROR_HANDLING", "false");
            properties.setProperty("javax.sip.AUTOMATIC_DIALOG_SUPPORT", "off");
            properties.setProperty("gov.nist.javax.sip.SIP_MESSAGE_VALVE", SipMessageValve.class.getCanonicalName());

            try {
                // Create SipStack object
                sipStack = sipFactory.createSipStack(properties);
                System.out.println("sipStack = " + sipStack);
            } catch (PeerUnavailableException e) {
                // could not find
                // gov.nist.jain.protocol.ip.sip.SipStackImpl
                // in the classpath
                e.printStackTrace();
                System.err.println(e.getMessage());
                if (e.getCause() != null)
                    e.getCause().printStackTrace();
                System.exit(0);
            }

            try {
                ListeningPoint lp = sipStack.createListeningPoint("127.0.0.1",
                        myPort, "udp");

                Shootme listener = this;

                sipProvider = sipStack.createSipProvider(lp);
                System.out.println("udp provider " + sipProvider);
                sipProvider.addSipListener(listener);

            } catch (Exception ex) {
                ex.printStackTrace();
            }

        }

        public void processTransactionTerminated(
                TransactionTerminatedEvent transactionTerminatedEvent) {

        }

        public void processDialogTerminated(
                DialogTerminatedEvent dialogTerminatedEvent) {

        }

		public void processIOException(IOExceptionEvent exceptionEvent) {
			
		}

    }

	public boolean processRequest(SIPRequest request,
			MessageChannel messageChannel) {
		try {
			messageChannel.sendMessage(request.createResponse(603));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	public boolean processResponse(Response response,
			MessageChannel messageChannel) {
		// Just drop the all responses
		return false;
	}
	
	public static void main(String[] args) throws Exception {
        Shootme shootme = new Shootme();
        shootme.init();
        System.out.println("SIP stack started.");
        Thread.sleep(10000);
        System.out.println("Stopped.");
    }

	public void destroy() {
		// TODO Auto-generated method stub
		
	}

	public void init(SipStack stack) {
		SipStackImpl s = (SipStackImpl) stack;
		s.getConfigurationProperties();
		System.out.println("SIP Message Valve initialized");
	}
}
