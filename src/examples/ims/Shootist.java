package examples.ims;

import gov.nist.javax.sip.header.AllowList;
import gov.nist.javax.sip.header.HeaderFactoryImpl;
import gov.nist.javax.sip.header.RequireList;
import gov.nist.javax.sip.header.SupportedList;
import gov.nist.javax.sip.header.ims.*;

import javax.sip.*;
import javax.sip.address.*;
import javax.sip.header.*;
import javax.sip.message.*;

import java.util.*;

/**
 * <p>This class is a UAC template.</p>
 * <p>Exemplifies the creation and parsing of the SIP P-Headers for IMS</p>
 *
 * <p>based on examples.simplecallsetup, by M. Ranganathan</p>
 * <p>issued by Miguel Freitas (IT) PT-Inovacao</p>
 */


public class Shootist implements SipListener {

    private static SipProvider sipProvider;

    private static AddressFactory addressFactory;

    private static MessageFactory messageFactory;

    private static HeaderFactory headerFactory;

    private static SipStack sipStack;

    private ContactHeader contactHeader;

    private ListeningPoint udpListeningPoint;

    private ClientTransaction inviteTid;

    private Dialog dialog;

    private boolean byeTaskRunning;

    class ByeTask  extends TimerTask {
        Dialog dialog;
        public ByeTask(Dialog dialog)  {
            this.dialog = dialog;
        }
        public void run () {
            try {
               Request byeRequest = this.dialog.createRequest(Request.BYE);
               ClientTransaction ct = sipProvider.getNewClientTransaction(byeRequest);
               dialog.sendRequest(ct);
            } catch (Exception ex) {
                ex.printStackTrace();
                System.exit(0);
            }

        }

    }

    private static final String usageString = "java "
            + "examples.shootist.Shootist \n"
            + ">>>> is your class path set to the root?";

    private static void usage() {
        System.out.println(usageString);
        System.exit(0);

    }


    public void processRequest(RequestEvent requestReceivedEvent) {
        Request request = requestReceivedEvent.getRequest();
        ServerTransaction serverTransactionId = requestReceivedEvent
                .getServerTransaction();

        System.out.println("\n\nRequest " + request.getMethod()
                + " received at " + sipStack.getStackName()
                + " with server transaction id " + serverTransactionId);

        // We are the UAC so the only request we get is the BYE.
        if (request.getMethod().equals(Request.BYE))
            processBye(request, serverTransactionId);

    }

    public void processBye(Request request,
            ServerTransaction serverTransactionId) {
        try {
            System.out.println("shootist:  got a bye .");
            if (serverTransactionId == null) {
                System.out.println("shootist:  null TID.");
                return;
            }
            Dialog dialog = serverTransactionId.getDialog();
            System.out.println("Dialog State = " + dialog.getState());
            Response response = messageFactory.createResponse(200, request);
            serverTransactionId.sendResponse(response);
            System.out.println("shootist:  Sending OK.");
            System.out.println("Dialog State = " + dialog.getState());

        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(0);

        }
    }


    public void processInviteOK(Response ok, ClientTransaction ct)
    {

        HeaderFactoryImpl headerFactoryImpl =
            (HeaderFactoryImpl) headerFactory;

        try
        {

            RequireHeader require = null;
            String requireOptionTags = new String();
            ListIterator li = ok.getHeaders(RequireHeader.NAME);
            if (li != null) {
                try {
                    while(li.hasNext())
                    {
                        require = (RequireHeader) li.next();
                        requireOptionTags = requireOptionTags
                            .concat( require.getOptionTag())
                            .concat(" ");
                    }
                }
                catch (Exception ex)
                {
                    System.out.println("\n(!) Exception getting Require header! - " + ex);
                }
            }


            // this is only to illustrate the usage of this headers
            // send Security-Verify (based on Security-Server) if Require: sec-agree
            SecurityVerifyList secVerifyList = null;
            if (requireOptionTags.indexOf("sec-agree") != -1)
            {
                ListIterator secServerReceived =
                    ok.getHeaders(SecurityServerHeader.NAME);
                if (secServerReceived != null && secServerReceived.hasNext())
                {
                    System.out.println(".: Security-Server received: ");

                     while (secServerReceived.hasNext())
                    {
                        SecurityServerHeader security = null;
                        try {
                            security = (SecurityServerHeader) secServerReceived.next();
                        }
                        catch (Exception ex)
                        {
                            System.out.println("(!) Exception getting Security-Server header : " + ex);
                        }

                        try {
                            Iterator parameters = security.getParameterNames();
                            SecurityVerifyHeader newSecVerify = headerFactoryImpl.createSecurityVerifyHeader();
                            newSecVerify.setSecurityMechanism(security.getSecurityMechanism());
                            while (parameters.hasNext())
                            {
                                String paramName = (String)parameters.next();
                                newSecVerify.setParameter(paramName,security.getParameter(paramName));
                            }

                            System.out.println("   - " + security.toString());

                        }
                        catch (Exception ex)
                        {
                            System.out.println("(!) Exception setting the security agreement!" + ex);
                            ex.getStackTrace();
                        }

                    }
                }
                System.out.println(".: Security-Verify built and added to response...");
            }

            CSeqHeader cseq = (CSeqHeader) ok.getHeader(CSeqHeader.NAME);
            ackRequest = dialog.createAck( cseq.getSeqNumber() );

            if (secVerifyList != null && !secVerifyList.isEmpty())
            {
                RequireHeader requireSecAgree = headerFactory.createRequireHeader("sec-agree");
                ackRequest.setHeader(requireSecAgree);

                ackRequest.setHeader(secVerifyList);
            }

            System.out.println("Sending ACK");
            dialog.sendAck(ackRequest);

        }
        catch (Exception ex)
        {
            System.out.println("(!) Exception sending ACK to 200 OK " +
                    "response to INVITE : " + ex);
        }
    }



       // Save the created ACK request, to respond to retransmitted 2xx
       private Request ackRequest;

    public void processResponse(ResponseEvent responseReceivedEvent) {
        System.out.println("Got a response");
        Response response = (Response) responseReceivedEvent.getResponse();
        ClientTransaction tid = responseReceivedEvent.getClientTransaction();
        CSeqHeader cseq = (CSeqHeader) response.getHeader(CSeqHeader.NAME);

        System.out.println("Response received : Status Code = "
                + response.getStatusCode() + " " + cseq);


        if (tid == null) {

            // RFC3261: MUST respond to every 2xx
            if (ackRequest!=null && dialog!=null) {
               System.out.println("re-sending ACK");
               try {
                  dialog.sendAck(ackRequest);
               } catch (SipException se) {
                  se.printStackTrace();
               }
            }
            return;
        }
        // If the caller is supposed to send the bye
        if ( Shootme.callerSendsBye && !byeTaskRunning) {
            byeTaskRunning = true;
            new Timer().schedule(new ByeTask(dialog), 2000) ;
        }
        System.out.println("transaction state is " + tid.getState());
        System.out.println("Dialog = " + tid.getDialog());
        System.out.println("Dialog State is " + tid.getDialog().getState());

        try {
            if (response.getStatusCode() == Response.OK) {
                if (cseq.getMethod().equals(Request.INVITE))
                {
                    processInviteOK(response, tid);

                    /*
                    ackRequest = dialog.createAck( cseq.getSeqNumber() );
                    System.out.println("Sending ACK");
                    dialog.sendAck(ackRequest);
                    */

                } else if (cseq.getMethod().equals(Request.CANCEL)) {
                    if (dialog.getState() == DialogState.CONFIRMED) {
                        // oops cancel went in too late. Need to hang up the
                        // dialog.
                        System.out
                                .println("Sending BYE -- cancel went in too late !!");
                        Request byeRequest = dialog.createRequest(Request.BYE);
                        ClientTransaction ct = sipProvider
                                .getNewClientTransaction(byeRequest);
                        dialog.sendRequest(ct);

                    }

                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(0);
        }

    }

    public void processTimeout(javax.sip.TimeoutEvent timeoutEvent) {

        System.out.println("Transaction Time out");
    }

    public void sendCancel() {
        try {
            System.out.println("Sending cancel");
            Request cancelRequest = inviteTid.createCancel();
            ClientTransaction cancelTid = sipProvider
                    .getNewClientTransaction(cancelRequest);
            cancelTid.sendRequest();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void init() {
        SipFactory sipFactory = null;
        sipStack = null;
        sipFactory = SipFactory.getInstance();
        sipFactory.setPathName("gov.nist");
        Properties properties = new Properties();
        // If you want to try TCP transport change the following to
        String transport = "udp";
        String peerHostPort = "127.0.0.1:5070";
        properties.setProperty("javax.sip.OUTBOUND_PROXY", peerHostPort + "/"
                + transport);
        // If you want to use UDP then uncomment this.
        properties.setProperty("javax.sip.STACK_NAME", "shootist");

        // The following properties are specific to nist-sip
        // and are not necessarily part of any other jain-sip
        // implementation.
        // You can set a max message size for tcp transport to
        // guard against denial of service attack.
        properties.setProperty("gov.nist.javax.sip.DEBUG_LOG",
                "shootistdebug.txt");
        properties.setProperty("gov.nist.javax.sip.SERVER_LOG",
                "shootistlog.txt");

        // Drop the client connection after we are done with the transaction.
        properties.setProperty("gov.nist.javax.sip.CACHE_CLIENT_CONNECTIONS",
                "false");
        // Set to 0 (or NONE) in your production code for max speed.
        // You need 16 (or TRACE) for logging traces. 32 (or DEBUG) for debug + traces.
        // Your code will limp at 32 but it is best for debugging.
        properties.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "TRACE");

        try {
            // Create SipStack object
            sipStack = sipFactory.createSipStack(properties);
            System.out.println("createSipStack " + sipStack);
        } catch (PeerUnavailableException e) {
            // could not find
            // gov.nist.jain.protocol.ip.sip.SipStackImpl
            // in the classpath
            e.printStackTrace();
            System.err.println(e.getMessage());
            System.exit(0);
        }

        try {
            headerFactory = sipFactory.createHeaderFactory();
            addressFactory = sipFactory.createAddressFactory();
            messageFactory = sipFactory.createMessageFactory();
            udpListeningPoint = sipStack.createListeningPoint("127.0.0.1",
                    5060, "udp");
            sipProvider = sipStack.createSipProvider(udpListeningPoint);
            Shootist listener = this;
            sipProvider.addSipListener(listener);

            String fromName = "BigGuy";
            String fromSipAddress = "here.com";
            String fromDisplayName = "The Master Blaster";

            String toSipAddress = "there.com";
            String toUser = "LittleGuy";
            String toDisplayName = "The Little Blister";

            // create >From Header
            SipURI fromAddress = addressFactory.createSipURI(fromName,
                    fromSipAddress);

            Address fromNameAddress = addressFactory.createAddress(fromAddress);
            fromNameAddress.setDisplayName(fromDisplayName);
            FromHeader fromHeader = headerFactory.createFromHeader(
                    fromNameAddress, "12345");

            // create To Header
            SipURI toAddress = addressFactory
                    .createSipURI(toUser, toSipAddress);
            Address toNameAddress = addressFactory.createAddress(toAddress);
            toNameAddress.setDisplayName(toDisplayName);
            ToHeader toHeader = headerFactory.createToHeader(toNameAddress,
                    null);

            // create Request URI
            SipURI requestURI = addressFactory.createSipURI(toUser,
                    peerHostPort);

            // Create ViaHeaders

            ArrayList viaHeaders = new ArrayList();
            ViaHeader viaHeader = headerFactory.createViaHeader("127.0.0.1",
                    sipProvider.getListeningPoint(transport).getPort(),
                    transport, null);

            // add via headers
            viaHeaders.add(viaHeader);

            // Create ContentTypeHeader
            ContentTypeHeader contentTypeHeader = headerFactory
                    .createContentTypeHeader("application", "sdp");

            // Create a new CallId header
            CallIdHeader callIdHeader = sipProvider.getNewCallId();

            // Create a new Cseq header
            CSeqHeader cSeqHeader = headerFactory.createCSeqHeader(1L,
                    Request.INVITE);

            // Create a new MaxForwardsHeader
            MaxForwardsHeader maxForwards = headerFactory
                    .createMaxForwardsHeader(70);

            // Create the request.
            Request request = messageFactory.createRequest(requestURI,
                    Request.INVITE, callIdHeader, cSeqHeader, fromHeader,
                    toHeader, viaHeaders, maxForwards);
            // Create contact headers
            String host = "127.0.0.1";

            SipURI contactUrl = addressFactory.createSipURI(fromName, host);
            contactUrl.setPort(udpListeningPoint.getPort());
            contactUrl.setLrParam();

            // Create the contact name address.
            SipURI contactURI = addressFactory.createSipURI(fromName, host);
            contactURI.setPort(sipProvider.getListeningPoint(transport)
                    .getPort());

            Address contactAddress = addressFactory.createAddress(contactURI);

            // Add the contact address.
            contactAddress.setDisplayName(fromName);

            contactHeader = headerFactory.createContactHeader(contactAddress);
            request.addHeader(contactHeader);

            // You can add extension headers of your own making
            // to the outgoing SIP request.
            // Add the extension header.
            Header extensionHeader = headerFactory.createHeader("My-Header",
                    "my header value");
            request.addHeader(extensionHeader);




            /* ++++++++++++++++++++++++++++++++++++++++++++
             *                IMS headers
             * ++++++++++++++++++++++++++++++++++++++++++++
             */

            // work-around for IMS headers
            HeaderFactoryImpl headerFactoryImpl = new HeaderFactoryImpl();

            // Allow header
            /*
            AllowHeader allowHeader =
                headerFactory.createAllowHeader(Request.INVITE + "," +
                        Request.PRACK + "," +
                        Request.UPDATE);
            request.addHeader(allowHeader);
            */
                AllowHeader allow1 =
                headerFactory.createAllowHeader(Request.INVITE);
            request.addHeader(allow1);
            AllowHeader allow2 =
                headerFactory.createAllowHeader(Request.PRACK);
            request.addHeader(allow2);
            AllowHeader allow3 =
                headerFactory.createAllowHeader(Request.UPDATE);
            request.addHeader(allow3);

            // Supported
            /*
            SupportedHeader supportedHeader =
                headerFactory.createSupportedHeader("100rel" + "," +
                        "precondition");
            request.addHeader(supportedHeader);
            */
            SupportedHeader supported1 =
                headerFactory.createSupportedHeader("100rel");
            request.addHeader(supported1);
            SupportedHeader supported2 =
                headerFactory.createSupportedHeader("preconditions");
            request.addHeader(supported2);
            SupportedHeader supported3 =
                headerFactory.createSupportedHeader("path");
            request.addHeader(supported3);



            // Require
            /*
            RequireHeader requireHeader =
                headerFactory.createRequireHeader("sec-agree"+ "," +
                "precondition");
            request.addHeader(requireHeader);
            */
            RequireHeader require1 =
                headerFactory.createRequireHeader("sec-agree");
            request.addHeader(require1);
            RequireHeader require2 =
                headerFactory.createRequireHeader("preconditions");
            request.addHeader(require2);


            // Security-Client
            SecurityClientHeader secClient =
                headerFactoryImpl.createSecurityClientHeader();
            secClient.setSecurityMechanism("ipsec-3gpp");
            secClient.setAlgorithm("hmac-md5-96");
            secClient.setEncryptionAlgorithm("des-cbc");
            secClient.setSPIClient(10000);
            secClient.setSPIServer(10001);
            secClient.setPortClient(5063);
            secClient.setPortServer(4166);
            request.addHeader(secClient);


            // P-Access-Network-Info
            PAccessNetworkInfoHeader accessInfo =
                headerFactoryImpl.createPAccessNetworkInfoHeader();
            accessInfo.setAccessType("3GPP-UTRAN-TDD");
            accessInfo.setUtranCellID3GPP("0123456789ABCDEF");
            request.addHeader(accessInfo);

            // Privacy
            PrivacyHeader privacy = headerFactoryImpl.createPrivacyHeader("header");
            request.addHeader(privacy);
            PrivacyHeader privacy2 = headerFactoryImpl.createPrivacyHeader("user");
            request.addHeader(privacy2);

            // P-Preferred-Identity
            PPreferredIdentityHeader preferredID =
                headerFactoryImpl.createPPreferredIdentityHeader(fromNameAddress);
            request.addHeader(preferredID);



            /*
             * TEST
             */
            // this is only to illustrate the usage of this headers


            // P-Called-Party-ID
            // only to test
            PCalledPartyIDHeader calledPartyID =
                headerFactoryImpl.createPCalledPartyIDHeader(toNameAddress);
            request.addHeader(calledPartyID);

            // P-Visited-Network-ID
            PVisitedNetworkIDHeader visitedNetworkID1 =
                headerFactoryImpl.createPVisitedNetworkIDHeader();
            visitedNetworkID1.setVisitedNetworkID(fromSipAddress
                    .substring(fromSipAddress.indexOf("@")+1));
            PVisitedNetworkIDHeader visitedNetworkID2 =
                headerFactoryImpl.createPVisitedNetworkIDHeader();
            visitedNetworkID2.setVisitedNetworkID(toSipAddress
                    .substring(toSipAddress.indexOf("@")+1));
            request.addHeader(visitedNetworkID1);
            request.addHeader(visitedNetworkID2);


            // P-Associated-URI
            PAssociatedURIHeader associatedURI1 =
                headerFactoryImpl.createPAssociatedURIHeader(toNameAddress);
            PAssociatedURIHeader associatedURI2 =
                headerFactoryImpl.createPAssociatedURIHeader(fromNameAddress);
            request.addHeader(associatedURI1);
            request.addHeader(associatedURI2);


            // P-Asserted-Identity
            PAssertedIdentityHeader assertedID =
                headerFactoryImpl.createPAssertedIdentityHeader(
                        addressFactory.createAddress(toAddress));
            request.addHeader(assertedID);

            TelURL tel = addressFactory.createTelURL("+1-201-555-0123");
            Address telAddress = addressFactory.createAddress(tel);
            toNameAddress.setDisplayName(toDisplayName);
            PAssertedIdentityHeader assertedID2 =
                headerFactoryImpl.createPAssertedIdentityHeader(telAddress);
            request.addHeader(assertedID2);


            // P-Charging-Function-Addresses
            PChargingFunctionAddressesHeader chargAddr =
                headerFactoryImpl.createPChargingFunctionAddressesHeader();
            chargAddr.addChargingCollectionFunctionAddress("test1.ims.test");
            chargAddr.addEventChargingFunctionAddress("testevent");
            request.addHeader(chargAddr);

            // P-Charging-Vector
            PChargingVectorHeader chargVect =
                headerFactoryImpl.createChargingVectorHeader("icid");
            chargVect.setICIDGeneratedAt("icidhost");
            chargVect.setOriginatingIOI("origIOI");
            chargVect.setTerminatingIOI("termIOI");
            request.addHeader(chargVect);

            // P-Media-Authorization
            PMediaAuthorizationHeader mediaAuth1 =
                headerFactoryImpl.createPMediaAuthorizationHeader("13579bdf");
            PMediaAuthorizationHeader mediaAuth2 =
                headerFactoryImpl.createPMediaAuthorizationHeader("02468ace");
            request.addHeader(mediaAuth1);
            request.addHeader(mediaAuth2);


            // Path header
            PathHeader path1 =
                headerFactoryImpl.createPathHeader(fromNameAddress);
            PathHeader path2 =
                headerFactoryImpl.createPathHeader(toNameAddress);
            request.addHeader(path1);
            request.addHeader(path2);



            /*
             * test clone() and equal()
             */
            /*
            SecurityClientHeader secClientClone =
                (SecurityClientHeader) secClient.clone();
            System.out.println(" --> Security-Client clone = "
                    + secClientClone.toString());
            System.out.println("    equals? "
                    + secClientClone.equals(secClient));

            PAccessNetworkInfo paniClone =
                (PAccessNetworkInfo) accessInfo.clone();
            System.out.println(" --> P-Access-Network-Info clone = "
                    + paniClone.toString());
            System.out.println("    equals? "
                    + paniClone.equals(accessInfo));

            Privacy privacyClone =
                (Privacy) privacy.clone();
            System.out.println(" --> Privacy clone = "
                    + privacyClone.toString());
            System.out.println("    equals? "
                    + privacyClone.equals(privacy));

            PPreferredIdentity preferredIDClone =
                (PPreferredIdentity) preferredID.clone();
            System.out.println(" --> P-Preferred-Identity clone = "
                    + preferredIDClone.toString());
            System.out.println("    equals? "
                    + preferredIDClone.equals(preferredID));

            PCalledPartyID calledPartyIDClone =
                (PCalledPartyID) calledPartyID.clone();
            System.out.println(" --> P-Called-Party-ID clone = "
                    + calledPartyIDClone.toString());
            System.out.println("    equals? "
                    + calledPartyIDClone.equals(calledPartyID));

            PVisitedNetworkIDList visNetListClone =
                (PVisitedNetworkIDList) visNetList.clone();
            System.out.println(" --> P-Visited-Network-ID list clone = "
                    + visNetListClone.toString());
            System.out.println("    equals? "
                    + visNetListClone.equals(visNetList));
            System.out.println("    equals? "
                    + visNetListClone.equals(visitedNetworkID1));

            PAssociatedURIList associatedListClone =
                (PAssociatedURIList) associatedList.clone();
            System.out.println(" --> P-Associated-URI list clone = "
                    + associatedListClone.toString());
            System.out.println("    equals? "
                    + associatedListClone.equals(associatedList));

            PAssertedIdentity assertedIDClone =
                (PAssertedIdentity) assertedID.clone();
            System.out.println(" --> P-Asserted-Identity clone = "
                    + assertedIDClone.toString());
            System.out.println("    equals? "
                    + assertedIDClone.equals(assertedID));

            PChargingFunctionAddresses chargAddrClone =
                (PChargingFunctionAddresses) chargAddr.clone();
            System.out.println(" --> P-Charging-Function-Addresses clone = "
                    + chargAddrClone.toString());
            System.out.println("    equals? "
                    + chargAddrClone.equals(chargAddr));

            PChargingVector chargVectClone =
                (PChargingVector) chargVect.clone();
            System.out.println(" --> P-Charging-Vector clone = "
                    + chargVectClone.toString());
            System.out.println("    equals? "
                    + chargVectClone.equals(chargVect));

            PMediaAuthorizationList mediaAuthListClone =
                (PMediaAuthorizationList) mediaAuthList.clone();
            System.out.println(" --> P-Media-Authorization list clone = "
                    + mediaAuthListClone.toString());
            System.out.println("    equals? "
                    + mediaAuthListClone.equals(mediaAuthList));

            PathList pathListClone =
                (PathList) pathList.clone();
            System.out.println(" --> Path list clone = "
                    + pathListClone.toString());
            System.out.println("    equals? "
                    + pathListClone.equals(pathList));
            System.out.println("    pathClone -> path1 equals? "
                    + pathListClone.equals(path1));
            System.out.println("    path1 -> path2 equals? "
                    + path1.equals(path2));

            */

            //////////////////////////////////////////////////



            String sdpData = "v=0\r\n"
                    + "o=4855 13760799956958020 13760799956958020"
                    + " IN IP4  129.6.55.78\r\n" + "s=mysession session\r\n"
                    + "p=+46 8 52018010\r\n" + "c=IN IP4  129.6.55.78\r\n"
                    + "t=0 0\r\n" + "m=audio 6022 RTP/AVP 0 4 18\r\n"

                    // bandwith
                    + "b=AS:25.4\r\n"
                    // precondition mechanism
                    + "a=curr:qos local none\r\n"
                    + "a=curr:qos remote none\r\n"
                    + "a=des:qos mandatory local sendrec\r\n"
                    + "a=des:qos none remote sendrec\r\n"


                    + "a=rtpmap:0 PCMU/8000\r\n" + "a=rtpmap:4 G723/8000\r\n"
                    + "a=rtpmap:18 G729A/8000\r\n" + "a=ptime:20\r\n";
            byte[] contents = sdpData.getBytes();

            request.setContent(contents, contentTypeHeader);
            // You can add as many extension headers as you
            // want.

            extensionHeader = headerFactory.createHeader("My-Other-Header",
                    "my new header value ");
            request.addHeader(extensionHeader);

            Header callInfoHeader = headerFactory.createHeader("Call-Info",
                    "<http://www.antd.nist.gov>");
            request.addHeader(callInfoHeader);

            // Create the client transaction.
            inviteTid = sipProvider.getNewClientTransaction(request);

            // send the request out.
            inviteTid.sendRequest();

            dialog = inviteTid.getDialog();

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace();
            usage();
        }
    }

    public static void main(String args[]) {
        new Shootist().init();

    }

    public void processIOException(IOExceptionEvent exceptionEvent) {
        System.out.println("IOException happened for "
                + exceptionEvent.getHost() + " port = "
                + exceptionEvent.getPort());

    }

    public void processTransactionTerminated(
            TransactionTerminatedEvent transactionTerminatedEvent) {
        System.out.println("Transaction terminated event recieved");
    }

    public void processDialogTerminated(
            DialogTerminatedEvent dialogTerminatedEvent) {
        System.out.println("dialogTerminatedEvent");

    }
}
