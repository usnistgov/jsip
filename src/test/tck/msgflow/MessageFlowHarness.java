/*
* Conditions Of Use
*
* This software was developed by employees of the National Institute of
* Standards and Technology (NIST), and others.
* This software is has been contributed to the public domain.
* As a result, a formal license is not needed to use the software.
*
* This software is provided "AS IS."
* NIST MAKES NO WARRANTY OF ANY KIND, EXPRESS, IMPLIED
* OR STATUTORY, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTY OF
* MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, NON-INFRINGEMENT
* AND DATA ACCURACY.  NIST does not warrant or make any representations
* regarding the use of the software or the results thereof, including but
* not limited to the correctness, accuracy, reliability or usefulness of
* the software.
*
*
*/
package test.tck.msgflow;

import gov.nist.javax.sip.stack.SIPTransactionStack;

import java.text.ParseException;
import java.util.LinkedList;
import java.util.List;

import javax.sip.ListeningPoint;
import javax.sip.ObjectInUseException;
import javax.sip.SipProvider;
import javax.sip.SipStack;
import javax.sip.address.Address;
import javax.sip.address.AddressFactory;
import javax.sip.address.SipURI;
import javax.sip.header.CSeqHeader;
import javax.sip.header.CallIdHeader;
import javax.sip.header.ContactHeader;
import javax.sip.header.ContentTypeHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.Header;
import javax.sip.header.HeaderFactory;
import javax.sip.header.MaxForwardsHeader;
import javax.sip.header.ToHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;

import test.tck.TckInternalError;
import test.tck.TestHarness;
import test.tck.TiUnexpectedError;

/**
 * <p>
 * Title: TCK
 * </p>
 * <p>
 * Description: JAIN SIP 1.1 Technology Compatibility Kit
 * </p>
 *
 * @author Emil Ivov Network Research Team, Louis Pasteur University,
 *         Strasbourg, France
 * @author Ivelin Ivanov
 *
 * @version 1.0
 */

public class MessageFlowHarness extends TestHarness {
    protected static final String EXTENSION_HDR = "Status-Extension";

    protected static int counter;



    // timeout values depend on pc, mine is not that powerful :)
    protected static long MESSAGES_ARRIVE_FOR = 2500;

    // it is really important to delete as a failure messes up following tests
    // so let's try real hard - 10 is a good number
    protected static int RETRY_OBJECT_DELETES = 10;

    protected static long RETRY_OBJECT_DELETES_AFTER = 500;

    protected static long STACKS_START_FOR = 1000;

    protected static long STACKS_SHUT_DOWN_FOR = 500;

    protected static long TRANSACTION_TIMES_OUT_FOR = 38000;

    protected ListeningPoint riListeningPoint = null;

    protected ListeningPoint tiListeningPoint = null;

    protected SipProvider riSipProvider = null;

    protected SipProvider tiSipProvider = null;

    protected SipEventCollector eventCollector = new SipEventCollector();

    protected SipStack riSipStack;

    protected SipStack tiSipStack;

    public MessageFlowHarness(String name) {
        this(name,true);
    }

    protected MessageFlowHarness(String name, boolean autoDialog) {
        super(name, autoDialog);
        System.out.println("Initializing test " + name);

        try {
            if ( riFactory != null)
                riFactory.resetFactory();

            riSipStack = riFactory.createSipStack(getRiProperties(autoDialog));
            assertTrue( "RI must be gov.nist", riSipStack instanceof SIPTransactionStack );

            tiFactory.resetFactory();
            tiFactory.setPathName( getImplementationPath() );
            tiSipStack = tiFactory.createSipStack(getTiProperties());
            if (riSipStack == tiSipStack) {
                throw new TckInternalError("riSipStack should not the same as tiSipStack");
            }
        } catch (TckInternalError ex){
            throw ex;
        } catch (Exception ex) {
            fail("initialization failed");
        }
    }

    // issue 17 on dev.java.net specify the headerFactory to use
    // report and fix thereof larryb@dev.java.net
    protected void addStatus(HeaderFactory headerFactory, Request request) {
        try {
            Header extension = headerFactory.createHeader(EXTENSION_HDR,
                    new Integer(counter++).toString());
            request.addHeader(extension);
        } catch (ParseException ex) {
            // do nothing
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
     * @throws java.lang.Exception
     *             All Let all exceptions that come from the underlying stack to
     *             pass through and surface at JUnit Level.
     */
    public void setUp() throws java.lang.Exception {

        riListeningPoint = riSipStack.createListeningPoint(LOCAL_ADDRESS, RI_PORT,
                "udp");
        riSipProvider = riSipStack.createSipProvider(riListeningPoint);

        tiListeningPoint = tiSipStack.createListeningPoint(LOCAL_ADDRESS, TI_PORT,
                "udp");
        tiSipProvider = tiSipStack.createSipProvider(tiListeningPoint);

        // JvB: don't forget to start them!
        riSipStack.start();
        tiSipStack.start();
        
        // If we don't wait for them to start first messages get lost and are
        // therefore reported as test failures.
        sleep(STACKS_START_FOR);
    }

    /**
     * Sets all JAIN SIP objects to null and resets the SipFactory.
     *
     * @throws java.lang.Exception
     */
    public void tearDown() throws java.lang.Exception {


        // Delete RI SipProvider
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

        // Delete RI ListeningPoint
        for (tries = 0; tries < RETRY_OBJECT_DELETES; tries++) {
            try {
                riSipStack.deleteListeningPoint(riListeningPoint);
            } catch (ObjectInUseException ex) {
                // System.err.println("Retrying delete of riListeningPoint!");
                sleep(RETRY_OBJECT_DELETES_AFTER);
                continue;
            }
            break;
        }
        if (tries >= RETRY_OBJECT_DELETES)
            throw new TckInternalError("Failed to delete riListeningPoint!");

        riSipProvider = null;
        riListeningPoint = null;

        // Delete TI SipProvider
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

        // Delete TI ListeningPoint
        for (tries = 0; tries < RETRY_OBJECT_DELETES; tries++) {
            try {
                tiSipStack.deleteListeningPoint(tiListeningPoint);
            } catch (ObjectInUseException ex) {
                // System.err.println("Retrying delete of tiListeningPoint!");
                sleep(RETRY_OBJECT_DELETES_AFTER);
                continue;
            }
            break;
        }

        if (tries >= RETRY_OBJECT_DELETES)
            throw new TiUnexpectedError("Failed to delete tiListeningPoint!");
        riSipStack.stop();

        tiSipStack.stop();


        tiSipProvider = null;
        tiListeningPoint = null;

        // Wait for stack threads to release resources (e.g. port)
        sleep(STACKS_SHUT_DOWN_FOR);
    }

    // ========================= Utility Methods =========================
    /**
     * Creates a SipRequest using the specified factories. The request has the
     * specified method and is meant to be sent from srcProvider to dstProvider.
     * This method is prefered to manual creation of requests as it helps avoid
     * using RI objects instead of corresponding TI objects (or vice versa).
     *
     * @param method
     *            the request's method
     * @param addressFactory
     *            the address factory to use when creating addresses
     * @param headerFactory
     *            the header factory to use when creating headers
     * @param messageFactory
     *            the message factory to use when creating headers
     * @param srcProvider
     *            the provider that will eventually be used to send the request
     * @param dstProvider
     *            the provider that will eventually dispatch the request to a
     *            SipListener
     * @param contentType
     *            if the content parameter is not null then this is its content
     *            type.
     * @param contentSubType
     *            if the content parameter is not null then this is its sub
     *            content type.
     * @param content
     *            the content of the request. if null this parameter is ignored
     * @return a request generated by the specified factories and destined to go
     *         from srcProvider to dstProvider
     * @throws Exception
     *             if anything should go wrong. further exception handling is
     *             left to calling methods (or JUnit).
     */
    protected Request createRequest(String method,
            AddressFactory addressFactory, HeaderFactory headerFactory,
            MessageFactory messageFactory, SipProvider srcProvider,
            SipProvider dstProvider, String contentType, String contentSubType,
            Object content) throws Exception {
        // Source SipUri
        ListeningPoint srclp = srcProvider.getListeningPoints()[0];
        SipURI srcSipURI = addressFactory.createSipURI(null, srclp
                .getIPAddress());
        srcSipURI.setPort(srclp.getPort());
        srcSipURI.setTransportParam(srclp.getTransport());

        // Destination SipURI
        ListeningPoint dstlp = dstProvider.getListeningPoints()[0];
        SipURI dstSipURI = addressFactory.createSipURI(null, dstlp
                .getIPAddress());
        dstSipURI.setPort(dstlp.getPort());
        dstSipURI.setTransportParam(dstlp.getTransport());
        // CallId
        CallIdHeader callId = srcProvider.getNewCallId();
        callId = headerFactory.createCallIdHeader( callId.getCallId() );

        // CSeq
        CSeqHeader cSeq = headerFactory.createCSeqHeader(1L, method);

        // From
        Address fromAddress = addressFactory.createAddress(srcSipURI);

        FromHeader from = headerFactory.createFromHeader(fromAddress, Integer
                .toString(srcProvider.hashCode()));
        // To
        Address toAddress = addressFactory.createAddress(dstSipURI);
        ToHeader to = headerFactory.createToHeader(toAddress, null);
        // Contact
        ContactHeader contact = headerFactory.createContactHeader(fromAddress);

        List via = new LinkedList();
        ViaHeader viaHeader = headerFactory.createViaHeader(srclp
                .getIPAddress(), srclp.getPort(), srclp.getTransport(),
        // BUG: Use proper RFC3261 branch ID
                "z9hG4bK" + Long.toString(System.currentTimeMillis())
        // branch id
                );
        via.add(viaHeader);
        MaxForwardsHeader maxForwards = headerFactory
                .createMaxForwardsHeader(3);

        Request request = messageFactory.createRequest(dstSipURI, method,
                callId, cSeq, from, to, via, maxForwards);
        request.addHeader(contact);
        if (contentType != null && contentSubType != null && content != null) {
            ContentTypeHeader contentTypeHdr = headerFactory
                    .createContentTypeHeader(contentType, contentSubType);
            request.setContent(content, contentTypeHdr);
        }
        // pass the headerFactory - issue17 by larryb@dev.java.net
        addStatus(headerFactory, request);
        return request;
    }

    /**
     * Creates an invite request object using the RI. This invite request is
     * meant to be sent to the TI
     *
     * @param contentType
     *            if the content parameter is not null then this is its content
     *            type.
     * @param contentSubType
     *            if the content parameter is not null then this is its content
     *            sub type.
     * @param content
     *            if the request is to have any content then this parameter is
     *            used to set it. Th content parameter is to be left to null if
     *            the request won't have any content.
     * @return an RI->TI invite request
     * @throws TckInternalError
     *             if anything should gou wrong.
     */
    protected Request createRiInviteRequest(String contentType,
            String contentSubType, Object content) throws TckInternalError {
        try {
            return createRequest(Request.INVITE, riAddressFactory,
                    riHeaderFactory, riMessageFactory, riSipProvider,
                    tiSipProvider, contentType, contentSubType, content);
        } catch (Throwable exc) {
            throw new TckInternalError(
                    "Failed to create an RI->TI invite request", exc);
        }
    }

    /**
     * Creates an invite request object using the TI. This invite request is
     * meant to be sent to the RI
     *
     * @param contentType
     *            if the content parameter is not null then this is its content
     *            type.
     * @param contentSubType
     *            if the content parameter is not null then this is its content
     *            sub type.
     * @param content
     *            if the request is to have any content then this parameter is
     *            used to set it. Th content parameter is to be left to null if
     *            the request won't have any content.
     * @return an TI->RI invite request
     * @throws TiUnexpectedError
     *             if anything should gou wrong.
     */
    protected Request createTiInviteRequest(String contentType,
            String contentSubType, Object content) throws TiUnexpectedError {
        try {
            return createRequest(Request.INVITE, tiAddressFactory,
                    tiHeaderFactory, tiMessageFactory, tiSipProvider,
                    riSipProvider, contentType, contentSubType, content);
        } catch (Throwable exc) {
            throw new TiUnexpectedError(
                    "Failed to create a TI->RI invite request", exc);
        }
    }

    /**
     * Creates a register request object using the RI. This register request is
     * meant to be sent to the TI
     *
     * @return an RI->TI register request
     * @throws TckInternalError
     *             if anything should gou wrong.
     */
    protected Request createRiRegisterRequest() throws TckInternalError {
        try {
            return createRequest(Request.REGISTER, riAddressFactory,
                    riHeaderFactory, riMessageFactory, riSipProvider,
                    tiSipProvider, null, null, null);
        } catch (Throwable exc) {
            throw new TckInternalError(
                    "Failed to create an RI->TI register request", exc);
        }
    }

    /**
     * Creates a register request object using the TI. This register request is
     * meant to be sent to the RI
     *
     * @return a TI->RI register request
     * @throws TiUnexpectedError
     *             if anything should gou wrong.
     */
    protected Request createTiRegisterRequest() throws TiUnexpectedError {
        try {
            return createRequest(Request.REGISTER, tiAddressFactory,
                    tiHeaderFactory, tiMessageFactory, tiSipProvider,
                    riSipProvider, null, null, null);

        } catch (Throwable exc) {
            throw new TiUnexpectedError(
                    "Failed to create a TI->RI register request", exc);
        }
    }

    public static void waitLongForMessage() {
        sleep(2*MESSAGES_ARRIVE_FOR);
    }
    /**
     * Waits during LISTEN_TIMEOUT milliseconds. This method is called after a
     * message has been sent so that it has the time to propagate though the
     * sending and receiving stack
     */
    public static void waitForMessage() {
        sleep(MESSAGES_ARRIVE_FOR);
    }

    /**
     * Wait till  a transaction times out.
     *
     */
    protected static void waitForTimeout() {
        sleep(TRANSACTION_TIMES_OUT_FOR);
    }
    /**
     * waits a good long time for messages.
     */
    protected static void waitShortForMessage() {
        sleep(MESSAGES_ARRIVE_FOR/2);
    }

    /**
     * Waits during _no_less_ than sleepFor milliseconds. Had to implement it on
     * top of Thread.sleep() to guarantee minimum sleep time.
     *
     * @param sleepFor
     *            the number of miliseconds to wait
     */
    protected static void sleep(long sleepFor) {
        long startTime = System.currentTimeMillis();
        long haveBeenSleeping = 0;
        while (haveBeenSleeping < sleepFor) {
            try {
                //Thread.sleep(sleepFor - haveBeenSleeping);
                if ( sleepFor - haveBeenSleeping < 750) {
                    Thread.sleep(sleepFor - haveBeenSleeping);
                } else {
                    Thread.sleep(750);
                    System.out.print(".");
                }
            } catch (InterruptedException ex) {
                // we-ll have to wait again!
            }
            haveBeenSleeping = (System.currentTimeMillis() - startTime);
        }

    }

    /**
     * Add a contact for the TI.
     */
    public ContactHeader createTiContact() throws Exception {
        try {
            ContactHeader contact = tiHeaderFactory.createContactHeader();

            // JvB: getIPAddress may return null!
            String ip = tiSipProvider.getSipStack().getIPAddress();
            if (ip == null) {
                ListeningPoint lp = (ListeningPoint) tiSipProvider
                        .getSipStack().getListeningPoints().next();
                ip = lp.getIPAddress();
            }

            SipURI srcSipURI = tiAddressFactory.createSipURI(null, ip);
            srcSipURI.setPort(tiSipProvider.getListeningPoint("udp").getPort());
            srcSipURI.setTransportParam("udp");
            Address address = tiAddressFactory.createAddress(srcSipURI);
            address.setDisplayName("TI Contact");
            contact.setAddress(address);
            return contact;
        } catch (Exception ex) {
            ex.printStackTrace();
            assertTrue(false);
            throw ex;
        }
    }

    /**
     * Add a contact for the TI.
     */
    public ContactHeader createRiContact() throws TckInternalError {
        try {
            ContactHeader contact = riHeaderFactory.createContactHeader();
            // BUG reported by Ben Evans (Open Cloud):
            // Should be using RI's address factory here, not TI's.

            ListeningPoint lp = riSipProvider.getListeningPoints()[0];
            SipURI srcSipURI = riAddressFactory.createSipURI(null, lp
                    .getIPAddress());
            srcSipURI.setPort(lp.getPort());
            srcSipURI.setTransportParam(lp.getTransport());
            Address address = riAddressFactory.createAddress(srcSipURI);
            address.setDisplayName("RI Contact");
            contact.setAddress(address);
            return contact;
        } catch (Exception ex) {
            throw new TckInternalError(ex.getMessage());
        }
    }

}
