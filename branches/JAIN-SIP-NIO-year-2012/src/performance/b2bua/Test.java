package performance.b2bua;

import java.text.ParseException;
import java.util.Properties;
import java.util.TooManyListenersException;
import java.util.concurrent.atomic.AtomicLong;

import javax.sip.Dialog;
import javax.sip.DialogTerminatedEvent;
import javax.sip.IOExceptionEvent;
import javax.sip.InvalidArgumentException;
import javax.sip.ListeningPoint;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.SipException;
import javax.sip.SipFactory;
import javax.sip.SipListener;
import javax.sip.SipProvider;
import javax.sip.SipStack;
import javax.sip.TimeoutEvent;
import javax.sip.TransactionTerminatedEvent;
import javax.sip.header.HeaderFactory;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;

public class Test implements SipListener {
 
	private static final String SIP_BIND_ADDRESS = "javax.sip.IP_ADDRESS";
	private static final String SIP_PORT_BIND = "javax.sip.PORT";
	private static final String TRANSPORTS_BIND = "javax.sip.TRANSPORT";
	//private static final String STACK_NAME_BIND = "javax.sip.STACK_NAME";
	
	private SipFactory sipFactory;
	private SipStack sipStack;
	private ListeningPoint listeningPoint;
	private SipProvider provider;
	private HeaderFactory headerFactory;
	private MessageFactory messageFactory;
		
	private static Properties properties = loadProperties();
	private static Properties loadProperties() {
		Properties props = new Properties();
		// Load default values
		try {
			props.load(Test.class.getResourceAsStream("test.properties"));
		} catch (java.io.IOException ex) {
			ex.printStackTrace();
		}
		return props;
	}
	
	public Test() throws NumberFormatException, SipException, TooManyListenersException, InvalidArgumentException, ParseException {
		initStack();
	}
	
	private void initStack() throws SipException, TooManyListenersException,
			NumberFormatException, InvalidArgumentException, ParseException {
		this.sipFactory = SipFactory.getInstance();
		this.sipFactory.setPathName("gov.nist");
		this.sipStack = this.sipFactory.createSipStack(Test.properties);
		this.sipStack.start();
		this.listeningPoint = this.sipStack.createListeningPoint(properties.getProperty(
				SIP_BIND_ADDRESS, "127.0.0.1"), Integer.valueOf(properties
				.getProperty(SIP_PORT_BIND, "5060")), properties.getProperty(
				TRANSPORTS_BIND, "udp"));
		this.provider = this.sipStack.createSipProvider(this.listeningPoint);
		this.provider.addSipListener(this);
		this.headerFactory = sipFactory.createHeaderFactory();
		this.messageFactory = sipFactory.createMessageFactory();
	}

	private AtomicLong counter = new AtomicLong();
	
	private String getNextCounter() {
		long l = counter.incrementAndGet();
		return Long.toString(l);
	}
	
	// XXX -- SipListenerMethods - here we process incoming data

	public void processIOException(IOExceptionEvent arg0) {}

	public void processRequest(RequestEvent requestEvent) {

		if (requestEvent.getRequest().getMethod().equals(Request.INVITE)) {
			TestCall call = new TestCall(getNextCounter(),provider,headerFactory,messageFactory);
			call.processInvite(requestEvent);
			
		}
		else if (requestEvent.getRequest().getMethod().equals(Request.BYE)) {
			Dialog dialog = requestEvent.getDialog();
			if (dialog != null) {
				((TestCall)dialog.getApplicationData()).processBye(requestEvent);
			}
		}
		else if (requestEvent.getRequest().getMethod().equals(Request.ACK)) {
			Dialog dialog = requestEvent.getDialog();
			if (dialog != null) {
				((TestCall)dialog.getApplicationData()).processAck(requestEvent);
			}
		}
		else {
			System.err.println("Received unexpected sip request: "+requestEvent.getRequest());
			Dialog dialog = requestEvent.getDialog();
			if (dialog != null) {
				dialog.setApplicationData(null);
			}
		}
	}

	public void processResponse(ResponseEvent responseEvent) {

		Dialog dialog = responseEvent.getDialog();
		if (dialog != null) {
			if (responseEvent.getClientTransaction() == null) {
				// retransmission, drop it
				return;
			}
			TestCall call = (TestCall) dialog.getApplicationData();
			if (call != null) {				
				switch (responseEvent.getResponse().getStatusCode()) {
				case 100:
					// ignore
					break;
				case 180:
					call.process180(responseEvent);
					break;
				case 200:
					call.process200(responseEvent);
					break;	
				default:
					System.err.println("Received unexpected sip response: "+responseEvent.getResponse());
					dialog.setApplicationData(null);
					break;
				}
			} else {
				System.err
						.println("Received response on dialog with id that does not matches a active call: "+responseEvent.getResponse());
			}
		} else {
			System.err.println("Received response without dialog: "+responseEvent.getResponse());
		}
	}

	public void processTimeout(TimeoutEvent arg0) {}

	public void processTransactionTerminated(
			TransactionTerminatedEvent txTerminatedEvent) {}

	public void processDialogTerminated(DialogTerminatedEvent dte) {
		dte.getDialog().setApplicationData(null);
	}
	
	public static void main(String[] args) {
		try {
			new Test();
			System.out.println("Test started");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
