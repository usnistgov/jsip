package test.tck.msgflow;

import javax.sip.*;
import javax.sip.address.*;
import javax.sip.message.*;
import javax.sip.header.*;
import java.util.Properties;
import java.util.List;
import java.util.*;
import java.text.*;
import test.tck.*;

/**
 * <p>Title: TCK</p>
 * <p>Description: JAIN SIP 1.1 Technology Compatibility Kit</p>
 * @author Emil Ivov
 *         Network Research Team, Louis Pasteur University, Strasbourg, France
 * This  code is in the public domain.
 * @version 1.0
 */

public class MessageFlowHarness extends TestHarness {
	protected static final String EXTENSION_HDR = "Status-Extension";
	protected static int counter;
	// timeout values depend on pc, mine is not that powerful :)
	protected static long MESSAGES_ARRIVE_FOR = 2000l;
	//it is really important to delete as a failure messes up following tests
	//so let's try real hard - 10 is a good number
	protected static int RETRY_OBJECT_DELETES = 10;
	protected static long RETRY_OBJECT_DELETES_AFTER = 500;

	protected static long STACKS_START_FOR = 1000;
	protected static long STACKS_SHUT_DOWN_FOR = 500;

	protected SipFactory sipFactory = null;

	protected SipStack riSipStack = null;
	protected SipStack tiSipStack = null;

	/** Made these static in the super class - EI - breaks my code. will see why later .*/
	protected MessageFactory riMessageFactory = null;
	protected MessageFactory tiMessageFactory = null;

	protected HeaderFactory riHeaderFactory = null;
	protected HeaderFactory tiHeaderFactory = null;

	protected AddressFactory riAddressFactory = null;
	protected AddressFactory tiAddressFactory = null;
	/**/

	protected ListeningPoint riListeningPoint = null;
	protected ListeningPoint tiListeningPoint = null;

	protected SipProvider riSipProvider = null;
	protected SipProvider tiSipProvider = null;

	protected SipEventCollector eventCollector = new SipEventCollector();

	protected static boolean riRetransmissionFilterEnabled;

	protected static boolean tiRetransmissionFilterEnabled;

	public MessageFlowHarness(String name) {
		super(name);
	}

	//issue 17 on dev.java.net specify the headerFactory to use
	//report and fix thereof larryb@dev.java.net
	protected void addStatus(HeaderFactory headerFactory, Request request) {
		try {
			Header extension =
				headerFactory.createHeader(
					EXTENSION_HDR,
					new Integer(counter++).toString());
			request.addHeader(extension);
		} catch (ParseException ex) {
			//do nothing
		}
	}

	protected void addStatus(Request request, Response response) {
		Header extension = request.getHeader(EXTENSION_HDR);
		if (extension != null)
			response.addHeader(extension);
	}

	/**
	 * Initialises both RI and TI sip stacks and stack factories.
	 *
	 * @throws java.lang.Exception All Let all exceptions that come from the
	 * underlying stack to pass through and surface at JUnit Level.
	 */
	protected void setUp() throws java.lang.Exception {

		/** Made these static in the super class - EI - breaks my code. will see why later .*/
		//init the RI
		try {
			sipFactory = SipFactory.getInstance();
			sipFactory.resetFactory();
			sipFactory.setPathName("gov.nist");
			riAddressFactory = sipFactory.createAddressFactory();
			riMessageFactory = sipFactory.createMessageFactory();
			riHeaderFactory = sipFactory.createHeaderFactory();

			riSipStack = sipFactory.createSipStack(getRiProperties());

			riListeningPoint = riSipStack.createListeningPoint(6050, "udp");
			riSipProvider = riSipStack.createSipProvider(riListeningPoint);
		} catch (Throwable exc) {
			throw new TckInternalError("An exception occurred while initialising RI");
		}
		// super.getFactories();

		String tiPathName = System.getProperty(IMPLEMENTATION_PATH);
		if (tiPathName == null || tiPathName.trim().length() == 0)
			tiPathName = "gov.nist";

		//issue 15 on dev.java.net init factories after reseting sipFactory
		//report and fix thereof - larryb@dev.java.net
		sipFactory.resetFactory();
		sipFactory.setPathName(tiPathName);

		//init the TI
		tiAddressFactory = sipFactory.createAddressFactory();
		tiMessageFactory = sipFactory.createMessageFactory();
		tiHeaderFactory = sipFactory.createHeaderFactory();

		tiSipStack = sipFactory.createSipStack(getTiProperties());

		tiListeningPoint = tiSipStack.createListeningPoint(5060, "udp");
		tiSipProvider = tiSipStack.createSipProvider(tiListeningPoint);

		//If we don't wait for them to start first messages get lost and are
		//therefore reported as test failures.
		sleep(STACKS_START_FOR);
	}

	/**
	 * Sets all JAIN SIP objects to null and resets the SipFactory.
	 *
	 * @throws java.lang.Exception
	 */
	protected void tearDown() throws java.lang.Exception {
		//Delete RI SipProvider
		int tries = 0;
		for (tries = 0; tries < RETRY_OBJECT_DELETES; tries++) {
			try {
				riSipStack.deleteSipProvider(riSipProvider);
			} catch (ObjectInUseException ex) {
				// System.err.println("Retrying delete of riSipProvider!");
				sleep(RETRY_OBJECT_DELETES_AFTER);
				continue;
			}
			break;
		}
		if (tries >= RETRY_OBJECT_DELETES)
			throw new TckInternalError("Failed to delete riSipProvider!");

		//Delete RI ListeningPoint
		for (tries = 0; tries < RETRY_OBJECT_DELETES; tries++) {
			try {
				riSipStack.deleteListeningPoint(riListeningPoint);
			} catch (ObjectInUseException ex) {
				//System.err.println("Retrying delete of riListeningPoint!");
				sleep(RETRY_OBJECT_DELETES_AFTER);
				continue;
			}
			break;
		}
		if (tries >= RETRY_OBJECT_DELETES)
			throw new TckInternalError("Failed to delete riListeningPoint!");

		riSipStack.deleteSipProvider(riSipProvider);
		riSipStack.deleteListeningPoint(riListeningPoint);
		riSipProvider = null;
		riListeningPoint = null;
		riAddressFactory = null;
		riMessageFactory = null;
		riHeaderFactory = null;
		riSipStack = null;

		//Delete TI SipProvider
		for (tries = 0; tries < RETRY_OBJECT_DELETES; tries++) {
			try {
				tiSipStack.deleteSipProvider(tiSipProvider);
			} catch (ObjectInUseException ex) {
				// System.err.println("Retrying delete of tiSipProvider!");
				sleep(RETRY_OBJECT_DELETES_AFTER);
				continue;
			}
			break;
		}
		if (tries >= RETRY_OBJECT_DELETES)
			throw new TiUnexpectedError("Failed to delete tiSipProvider!");

		//Delete TI ListeningPoint
		for (tries = 0; tries < RETRY_OBJECT_DELETES; tries++) {
			try {
				tiSipStack.deleteListeningPoint(tiListeningPoint);
			} catch (ObjectInUseException ex) {
				//System.err.println("Retrying delete of tiListeningPoint!");
				sleep(RETRY_OBJECT_DELETES_AFTER);
				continue;
			}
			break;
		}

		if (tries >= RETRY_OBJECT_DELETES)
			throw new TiUnexpectedError("Failed to delete tiListeningPoint!");

		tiSipProvider = null;
		tiListeningPoint = null;
		tiAddressFactory = null;
		tiMessageFactory = null;
		tiHeaderFactory = null;
		tiSipStack = null;

		sipFactory.resetFactory();
		sipFactory = null;
		//Wait for stack threads to release resources (e.g. port)
		sleep(STACKS_SHUT_DOWN_FOR);
	}

	//========================= Utility Methods =========================
	/**
	 * Creates a SipRequest using the specified factories. The request has the
	 * specified method and is meant to be sent from srcProvider to dstProvider.
	 * This method is prefered to manual creation of requests as it helps avoid
	 * using RI objects instead of corresponding TI objects (or vice versa).
	 * @param method            the request's method
	 * @param addressFactory    the address factory to use when creating addresses
	 * @param headerFactory	    the header factory to use when creating headers
	 * @param messageFactory    the message factory to use when creating headers
	 * @param srcProvider       the provider that will eventually be used to send
	 * 	                        the request
	 * @param dstProvider       the provider that will eventually dispatch
	 *                          the request to a SipListener
	 * @param contentType       if the content parameter is not null then this is
	 *                          its content type.
	 * @param contentSubType    if the content parameter is not null then this is
	 *                          its sub content type.
	 * @param content           the content of the request. if null this parameter
	 *                          is ignored
	 * @return                  a request generated by the specified factories and destined to go
	 *                          from srcProvider to dstProvider
	 * @throws Exception if anything should go wrong. further exception handling
	 * 	is left to calling methods (or JUnit).
	 */
	protected Request createRequest(
		String method,
		AddressFactory addressFactory,
		HeaderFactory headerFactory,
		MessageFactory messageFactory,
		SipProvider srcProvider,
		SipProvider dstProvider,
		String contentType,
		String contentSubType,
		Object content)
		throws Exception {
		//Source SipUri
		SipURI srcSipURI =
			addressFactory.createSipURI(
				null,
				srcProvider.getSipStack().getIPAddress());
		srcSipURI.setPort(srcProvider.getListeningPoint().getPort());
		srcSipURI.setTransportParam(
			srcProvider.getListeningPoint().getTransport());

		//Destination SipURI
		SipURI dstSipURI =
			addressFactory.createSipURI(
				null,
				dstProvider.getSipStack().getIPAddress());
		dstSipURI.setPort(dstProvider.getListeningPoint().getPort());
		dstSipURI.setTransportParam(
			dstProvider.getListeningPoint().getTransport());
		//CallId
		CallIdHeader callId = srcProvider.getNewCallId();
		//CSeq
		CSeqHeader cSeq = headerFactory.createCSeqHeader(1, method);

		//From
		Address fromAddress = addressFactory.createAddress(srcSipURI);

		FromHeader from =
			headerFactory.createFromHeader(
				fromAddress,
				Integer.toString(srcProvider.hashCode()));
		//To
		Address toAddress = addressFactory.createAddress(dstSipURI);
		ToHeader to = headerFactory.createToHeader(toAddress, null);
		//Contact
		ContactHeader contact = headerFactory.createContactHeader(fromAddress);

		List via = new LinkedList();
		ViaHeader viaHeader =
			headerFactory
				.createViaHeader(
					srcProvider.getSipStack().getIPAddress(),
					srcProvider.getListeningPoint().getPort(),
					srcProvider.getListeningPoint().getTransport(),
					Long.toString(System.currentTimeMillis())
			//branch id
	);
		via.add(viaHeader);
		MaxForwardsHeader maxForwards =
			headerFactory.createMaxForwardsHeader(3);

		Request request =
			messageFactory.createRequest(
				dstSipURI,
				method,
				callId,
				cSeq,
				from,
				to,
				via,
				maxForwards);
		request.addHeader(contact);
		if (contentType != null && contentSubType != null && content != null) {
			ContentTypeHeader contentTypeHdr =
				headerFactory.createContentTypeHeader(
					contentType,
					contentSubType);
			request.setContent(content, contentTypeHdr);
		}
		//pass the headerFactory - issue17 by larryb@dev.java.net
		addStatus(headerFactory, request);
		return request;
	}

	/**
	 * Creates an invite request object using the RI. This invite request
	 * is meant to be sent to the TI
	 * @param contentType if the content parameter is not null then this is its
	 * 						content type.
	 * @param contentSubType if the content parameter is not null then this is its
	 * 						content sub type.
	 * @param content	if the request is to have any content then this parameter
	 * 					is used to set it. Th content parameter is to be left
	 * 					to null if the request won't have any content.
	 * @return an RI->TI invite request
	 * @throws TckInternalError if anything should gou wrong.
	 */
	protected Request createRiInviteRequest(
		String contentType,
		String contentSubType,
		Object content)
		throws TckInternalError {
		try {
			return createRequest(
				Request.INVITE,
				riAddressFactory,
				riHeaderFactory,
				riMessageFactory,
				riSipProvider,
				tiSipProvider,
				contentType,
				contentSubType,
				content);
		} catch (Throwable exc) {
			throw new TckInternalError(
				"Failed to create an RI->TI invite request",
				exc);
		}
	}

	/**
	 * Creates an invite request object using the TI. This invite request
	 * is meant to be sent to the RI
	 * @param contentType if the content parameter is not null then this is its
	 * 						content type.
	 * @param contentSubType if the content parameter is not null then this is its
	 * 						content sub type.
	 * @param content	if the request is to have any content then this parameter
	 * 					is used to set it. Th content parameter is to be left
	 * 					to null if the request won't have any content.
	 * @return an TI->RI invite request
	 * @throws TiUnexpectedError if anything should gou wrong.
	 */
	protected Request createTiInviteRequest(
		String contentType,
		String contentSubType,
		Object content)
		throws TiUnexpectedError {
		try {
			return createRequest(
				Request.INVITE,
				tiAddressFactory,
				tiHeaderFactory,
				tiMessageFactory,
				tiSipProvider,
				riSipProvider,
				contentType,
				contentSubType,
				content);
		} catch (Throwable exc) {
			throw new TiUnexpectedError(
				"Failed to create a TI->RI invite request",
				exc);
		}
	}

	/**
	 * Creates a register request object using the RI. This register request
	 * is meant to be sent to the TI
	 *
	 * @return an RI->TI register request
	 * @throws TckInternalError if anything should gou wrong.
	 */
	protected Request createRiRegisterRequest() throws TckInternalError {
		try {
			return createRequest(
				Request.REGISTER,
				riAddressFactory,
				riHeaderFactory,
				riMessageFactory,
				riSipProvider,
				tiSipProvider,
				null,
				null,
				null);
		} catch (Throwable exc) {
			throw new TckInternalError(
				"Failed to create an RI->TI register request",
				exc);
		}
	}

	/**
	 * Creates a register request object using the TI. This register request
	 * is meant to be sent to the RI
	 *
	 * @return a TI->RI register request
	 * @throws TiUnexpectedError if anything should gou wrong.
	 */
	protected Request createTiRegisterRequest() throws TiUnexpectedError {
		try {
			return createRequest(
				Request.REGISTER,
				tiAddressFactory,
				tiHeaderFactory,
				tiMessageFactory,
				tiSipProvider,
				riSipProvider,
				null,
				null,
				null);

		} catch (Throwable exc) {
			throw new TiUnexpectedError(
				"Failed to create a TI->RI register request",
				exc);
		}
	}

	/**
	 * Waits during LISTEN_TIMEOUT milliseconds. This method is called after
	 * a message has been sent so that it has the time to propagate though the
	 * sending and receiving stack
	 */
	protected static void waitForMessage() {
		sleep(MESSAGES_ARRIVE_FOR);
	}

	/**
	 * Waits during _no_less_ than sleepFor milliseconds.
	 * Had to implement it on top of Thread.sleep() to guarantee minimum
	 * sleep time.
	 *
	 * @param sleepFor the number of miliseconds to wait
	 */
	protected static void sleep(long sleepFor) {
		long startTime = System.currentTimeMillis();
		long haveBeenSleeping = 0;
		while (haveBeenSleeping < sleepFor) {
			try {
				Thread.sleep(sleepFor - haveBeenSleeping);
			} catch (InterruptedException ex) {
				//we-ll have to wait again!
			}
			haveBeenSleeping = (System.currentTimeMillis() - startTime);
		}

	}

	/** Add a contact for the TI.
	*/
	public ContactHeader createTiContact() throws Exception {
		try {
			ContactHeader contact = tiHeaderFactory.createContactHeader();
			SipURI srcSipURI =
				tiAddressFactory.createSipURI(
					null,
					tiSipProvider.getSipStack().getIPAddress());
			srcSipURI.setPort(tiSipProvider.getListeningPoint().getPort());
			srcSipURI.setTransportParam(
				tiSipProvider.getListeningPoint().getTransport());
			Address address = tiAddressFactory.createAddress(srcSipURI);
			address.setDisplayName("TI Contact");
			contact.setAddress(address);
			return contact;
		} catch (Exception ex) {
			assertTrue(false);
			throw ex;
		}
	}

	/** Add a contact for the TI.
	*/
	public ContactHeader createRiContact() throws TckInternalError {
		try {
			ContactHeader contact = riHeaderFactory.createContactHeader();
			SipURI srcSipURI =
				tiAddressFactory.createSipURI(
					null,
					riSipProvider.getSipStack().getIPAddress());
			srcSipURI.setPort(riSipProvider.getListeningPoint().getPort());
			srcSipURI.setTransportParam(
				riSipProvider.getListeningPoint().getTransport());
			Address address = riAddressFactory.createAddress(srcSipURI);
			address.setDisplayName("RI Contact");
			contact.setAddress(address);
			return contact;
		} catch (Exception ex) {
			throw new TckInternalError(ex.getMessage());
		}
	}

	/**
	 * Returns a properties object containing all RI settings. The result from
	 * this method is passed to the SipFactory when creating the RI Stack
	 */
	public Properties getRiProperties() {
		//TODO collect all system properties
		//prefixed javax.sip.tck.ri and add them to the local
		//properties object

		Properties properties = new Properties();
		properties.setProperty("javax.sip.IP_ADDRESS", "127.0.0.1");
		properties.setProperty("javax.sip.STACK_NAME", "RiStack");
		properties.setProperty("javax.sip.RETRANSMISSION_FILTER", "true");
		riRetransmissionFilterEnabled = true;

		properties.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "0");

		//properties.setProperty("gov.nist.javax.sip.TRACE_LEVEL","32");
		properties.setProperty(
			"gov.nist.javax.sip.DEBUG_LOG",
			"riDebugLog.txt");
		properties.setProperty(
			"gov.nist.javax.sip.SERVER_LOG",
			"riMessageLog.txt");

		return properties;
	}

	/**
	 * Returns a properties object containing all TI settings. The result from
	 * this method is passed to the SipFactory when creating the TI Stack
	 */
	public Properties getTiProperties() {
		//TODO collect all system properties
		//prefixed javax.sip.tck.ti and add them to the local
		//properties object

		Properties properties = new Properties();
		properties.setProperty("javax.sip.IP_ADDRESS", "127.0.0.1");
		properties.setProperty("javax.sip.STACK_NAME", "TiStack");
		properties.setProperty("javax.sip.RETRANSMISSION_FILTER", "true");
		tiRetransmissionFilterEnabled = true;
		//properties.setProperty("gov.nist.javax.sip.TRACE_LEVEL","0");

		properties.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "32");
		properties.setProperty(
			"gov.nist.javax.sip.DEBUG_LOG",
			"tiDebugLog.txt");
		properties.setProperty(
			"gov.nist.javax.sip.SERVER_LOG",
			"tiMessageLog.txt");

		return properties;
	}

}
