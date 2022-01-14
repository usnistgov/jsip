package examples.ims;

import gov.nist.javax.sip.header.HeaderFactoryImpl;
import gov.nist.javax.sip.header.ims.*;

import javax.sip.*;
import javax.sip.address.*;
import javax.sip.header.*;
import javax.sip.message.*;
import java.util.*;

/**
 * <p>This class is a UAS template.</p>
 * <p>Exemplifies the creation and parsing of the SIP P-Headers for IMS</p>
 *
 * <p>based on examples.simplecallsetup, by M. Ranganathan</p>
 * <p>issued by Miguel Freitas (IT) PT-Inovacao</p>
 */

public class Shootme implements SipListener {

    private static AddressFactory addressFactory;

    private static MessageFactory messageFactory;

    private static HeaderFactory headerFactory;

    private static SipStack sipStack;

    private static final String myAddress = "127.0.0.1";

    private static final int myPort = 5070;

    protected ServerTransaction inviteTid;

    private Response okResponse;

    private Request inviteRequest;

    private Dialog dialog;

    public static final boolean callerSendsBye = true;

    class MyTimerTask extends TimerTask {
        Shootme shootme;

        public MyTimerTask(Shootme shootme) {
            this.shootme = shootme;

        }

        public void run() {
            shootme.sendInviteOK();
        }

    }

    protected static final String usageString = "java "
            + "examples.shootist.Shootist \n"
            + ">>>> is your class path set to the root?";

    private static void usage() {
        System.out.println(usageString);
        System.exit(0);

    }

    public void processRequest(RequestEvent requestEvent) {
        Request request = requestEvent.getRequest();
        ServerTransaction serverTransactionId = requestEvent
                .getServerTransaction();

        System.out.println("\n\nRequest " + request.getMethod()
                + " received at " + sipStack.getStackName()
                + " with server transaction id " + serverTransactionId);

        if (request.getMethod().equals(Request.INVITE)) {
            processInvite(requestEvent, serverTransactionId);
        } else if (request.getMethod().equals(Request.ACK)) {
            processAck(requestEvent, serverTransactionId);
        } else if (request.getMethod().equals(Request.BYE)) {
            processBye(requestEvent, serverTransactionId);
        } else if (request.getMethod().equals(Request.CANCEL)) {
            processCancel(requestEvent, serverTransactionId);
        }

    }

    public void processResponse(ResponseEvent responseEvent) {
    }

    /**
     * Process the ACK request. Send the bye and complete the call flow.
     */
    public void processAck(RequestEvent requestEvent,
            ServerTransaction serverTransaction) {
        try {
            System.out.println("shootme: got an ACK! ");
            System.out.println("Dialog State = " + dialog.getState());
            SipProvider provider = (SipProvider) requestEvent.getSource();
            if (!callerSendsBye) {
                Request byeRequest = dialog.createRequest(Request.BYE);
                ClientTransaction ct = provider
                        .getNewClientTransaction(byeRequest);
                dialog.sendRequest(ct);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    /**
     * Process the invite request.
     */
    public void processInvite(RequestEvent requestEvent,
            ServerTransaction serverTransaction) {
        SipProvider sipProvider = (SipProvider) requestEvent.getSource();
        Request request = requestEvent.getRequest();



        /* ++++++++++++++++++++++++++++++++++++++++++++
         *                IMS headers
         * ++++++++++++++++++++++++++++++++++++++++++++
         */

        // work-around for IMS headers
        //HeaderFactoryImpl headerFactoryImpl = new HeaderFactoryImpl();
        HeaderFactoryImpl headerFactoryImpl = (HeaderFactoryImpl)headerFactory;

        // check headers Allow, Require and Supported

        // Allow header
        ListIterator li = null;
        AllowHeader allow = null;
        String allowMethods = new String();         // all the methods in Allow Header
        li = request.getHeaders(AllowHeader.NAME);  // get all the allow methods
        // creates an Allow header for each method

        try {
            while(li.hasNext()) // concatenate the method of each Allow header in one string
            {
                allow = (AllowHeader) li.next();
                allowMethods = allowMethods.concat(allow.getMethod()).concat(" ");
            }
        }catch (Exception ex)
        {
            System.out.println("\n(!) Exception getting Allow header! - " + ex);
        }

        /*
        // Allow: PRACK ??
        if (allowMethods.indexOf(Request.PRACK) != -1)
            System.out.println("\n*** UAC allows PRACK method ");
        else
            System.out.println("\n*** UAC does NOT allow PRACK method ");
        */


        // Require header
        RequireHeader require = null;
        String requireOptionTags = new String();
        li = null;
        li = request.getHeaders(RequireHeader.NAME);
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

        /*
        // Require: 100rel ??
        if (requireOptionTags.indexOf("100rel") != -1)
            System.out.println("\n*** UAC requires \"100rel\"");
        else
            System.out.println("\n*** UAC does NOT require \"100rel\"");
        // Require: precondition ??
        if (requireOptionTags.indexOf("precondition") != -1)
            System.out.println("\n*** UAC requires \"precondition\"");
        else
            System.out.println("\n*** UAC does NOT require \"precondition\"");
        // Require: sec-agree ??
        if (requireOptionTags.indexOf("sec-agree") != -1)
            System.out.println("\n*** UAC requires \"sec-agree\"");
        else
            System.out.println("\n*** UAC does NOT require \"sec-agree\"");
        */


        // Supported header
        SupportedHeader supported = null;
        String supportedOptionTags = new String();
        li = request.getHeaders(SupportedHeader.NAME);
        try {
            while(li.hasNext())
            {
                supported = (SupportedHeader) li.next();
                supportedOptionTags = supportedOptionTags
                    .concat( supported.getOptionTag())
                    .concat(" ");
            }
        }
        catch (NullPointerException ex)
        {
            System.out.println("\n(!) Exception getting Supported header! - " + ex);
        }

        /*
        // Supported: 100rel ??
        if (supportedOptionTags.indexOf("100rel") != -1)
            System.out.println("\n*** UAC supports \"100rel\"");
        else
            System.out.println("\n*** UAC does NOT support \"100rel\"");

        // Supported: precondition ??
        if (supportedOptionTags.indexOf("precondition") != -1)
            System.out.println("\n*** UAC supports \"precondition\"");
        else
            System.out.println("\n*** UAC does NOT support \"precondition\"");
        */

        // check P-Headers

        // check P-Called-Party-ID
        PCalledPartyIDHeader calledParty;
        try {
            calledParty = (PCalledPartyIDHeader)
                request.getHeader(PCalledPartyIDHeader.NAME);

            if (calledParty != null)
            {
                System.out.println(".: P-Called-Party-ID = "
                    + calledParty.getAddress().toString());
            }
            else
                System.out.println(".: NOT received P-Called-Party-ID ! ");

        }
        catch (Exception ex)
        {
            System.out.println("(!) Exception getting P-Called-Party-ID header! - " + ex);
        }


        // check P-Associated-URI
        ListIterator associatedURIList;
        try {
            associatedURIList = request.getHeaders(PAssociatedURIHeader.NAME);
            if (associatedURIList != null)
            {
                System.out.print(".: P-Associated-URI = ");
                while (associatedURIList.hasNext())
                {
                    PAssociatedURIHeader associatedURI = (PAssociatedURIHeader) associatedURIList.next();

                    System.out.print(associatedURI.getAssociatedURI().toString());
                    if (associatedURIList.hasNext())
                        System.out.print(", ");

                }
            }
            else
                System.out.println(".: NOT received P-Associated-URI ! ");

            System.out.print("\n");
        }
        catch (Exception ex)
        {
            System.out.println("(!) Exception getting P-Associated-URI header! - " + ex);
        }


        // check P-Access-Network-Info
        PAccessNetworkInfoHeader accessInfo = null;
        try {
            accessInfo = (PAccessNetworkInfoHeader)
                request.getHeader(PAccessNetworkInfoHeader.NAME);
            if (accessInfo != null)
            {
                System.out.print(".: P-Access-Network-Info: Access Type = "
                    + accessInfo.getAccessType());

                if (accessInfo.getAccessType()
                        .equalsIgnoreCase(
                                PAccessNetworkInfoHeader.GGGPP_UTRAN_TDD)) // 3GPP-UTRAN-TDD
                    System.out.print(" - Cell ID = "
                            + accessInfo.getUtranCellID3GPP());
            }
            else
                System.out.println(".: NOT received P-Access-Network-Info ! ");

            System.out.println("");
        }
        catch (Exception ex)
        {
            System.out.println("(!) Exception getting P-Access-Network-Info header! - " + ex);
        }

        // check if .clone() and .equals() is working
        if (accessInfo != null)
        {
            PAccessNetworkInfo accessInfoClone =
                (PAccessNetworkInfo) accessInfo.clone();

            System.out.println("--> clone = " + accessInfoClone.toString());
            System.out.println("--> equals? " + accessInfoClone.equals(accessInfo));
        }


        // check P-Visited-Network-ID
        ListIterator visitedNetList;
        try {
            visitedNetList = request.getHeaders(PVisitedNetworkIDHeader.NAME);
            if (visitedNetList != null)
            {
                System.out.print(".: P-Visited-Network-ID = ");
                while (visitedNetList.hasNext())
                {
                    PVisitedNetworkIDHeader visitedID =
                        (PVisitedNetworkIDHeader) visitedNetList.next();
                    System.out.print(visitedID.getVisitedNetworkID());
                    if (visitedNetList.hasNext())
                        System.out.print(", ");
                }
                System.out.print("\n");
            }
            else
                System.out.print(".: NOT received P-Visited-Network-ID ! ");
        }
        catch (Exception ex)
        {
            System.out.println("(!) Exception getting P-Visited-Network-ID header! - " + ex);
        }


        // check Privacy
        ListIterator privacyList;
        try {
            privacyList = request.getHeaders(PrivacyHeader.NAME);
            if (privacyList != null && privacyList.hasNext())
            {
                System.out.print(".: Privacy = ");
                while (privacyList.hasNext())
                {
                    PrivacyHeader privacy =
                        (PrivacyHeader) privacyList.next();
                    System.out.print(privacy.getPrivacy());
                    if (privacyList.hasNext())
                        System.out.print("; ");
                }
                System.out.println("");
            }
            else
                System.out.println(".: NOT received Privacy ! ");
        }

        catch (Exception ex)
        {
            System.out.println("(!) Exception getting Privacy header! - " + ex);
        }

        // check P-Preferred-Identity
        PPreferredIdentityHeader preferredID;
        try {
            preferredID = (PPreferredIdentityHeader)
                request.getHeader(PPreferredIdentityHeader.NAME);
            if (preferredID != null)
            {
                System.out.println(".: P-Preferred-Identity = " + preferredID.getAddress().toString());
            }
            else
                System.out.println(".: NOT received P-Preferred-Identity ! ");
        }
        catch (Exception ex)
        {
            System.out.println("(!) Exception getting P-Preferred-Identity header! - " + ex);
        }


        /*
         * TEST
         */
        // this is only to illustrate the usage of this headers


        // P-Asserted-Identity
        ListIterator assertedIDList;
        try {
            assertedIDList =
                request.getHeaders(PAssertedIdentityHeader.NAME);
            if (assertedIDList != null && assertedIDList.hasNext())
            {
                System.out.print(".: P-Asserted-Identity = ");
                while (assertedIDList.hasNext())
                {
                    PAssertedIdentityHeader assertedID =
                        (PAssertedIdentityHeader) assertedIDList.next();
                    System.out.print(assertedID.getAddress().toString());
                    if (assertedIDList.hasNext())
                        System.out.print(", ");
                }
                System.out.println("");
            }
            else
                System.out.println(".: NOT received P-Asserted-Identity... ");
        }
        catch (Exception ex)
        {
            System.out.println("(!) Exception getting P-Asserted-Identity header! - " + ex);
        }



        // P-Charging-Function-Addresses
        PChargingFunctionAddressesHeader chargAddr;
        try {
            chargAddr = (PChargingFunctionAddressesHeader)
                request.getHeader(PChargingFunctionAddressesHeader.NAME);

            if (chargAddr != null)
            {
                Iterator param = chargAddr.getParameterNames();

                System.out.print(".: P-Charging-Function-Addresses = ");

                if (param != null) {
                    while (param.hasNext()) {
                        String paramName = (String)param.next();
                        System.out.print( paramName + "=" + chargAddr.getParameter(paramName));
                        if (param.hasNext())
                            System.out.print(", ");
                    }
                }
                System.out.println("");
            }
            else
                System.out.println(".: NOT containing P-Charging-Function-Addresses... ");
        }
        catch (Exception ex)
        {
            System.out.println("(!) Exception getting P-Charging-Function-Addresses header! - " + ex);
        }

        // P-Charging-Vector
        PChargingVectorHeader chargVect;
        try {
            chargVect = (PChargingVectorHeader)
                request.getHeader(PChargingVectorHeader.NAME);
            if (chargVect != null)
            {
                Iterator param = chargVect.getParameterNames();

                System.out.print(".: P-Charging-Vector = ");

                if (param != null && param.hasNext()) {
                    while (param.hasNext()) {
                        String paramName = (String)param.next();
                        System.out.print( paramName + "="
                                + chargVect.getParameter(paramName));
                        if (param.hasNext())
                            System.out.print(", ");
                    }
                }
                System.out.println("");
            }
            else
                System.out.println(".: NOT containing P-Charging-Vector... ");
        }
        catch (Exception ex)
        {
            System.out.println("(!) Exception getting P-Charging-Vector header! - " + ex);
        }


        // P-Media-Authorization
        ListIterator mediaAuthList;
        try {
            mediaAuthList = request.getHeaders(PMediaAuthorizationHeader.NAME);

            if (mediaAuthList != null)
            {
                System.out.print(".: P-Media-Authorization = ");
                while (mediaAuthList.hasNext())
                {
                    PMediaAuthorizationHeader mediaAuth =
                        (PMediaAuthorizationHeader) mediaAuthList.next();
                    System.out.print(mediaAuth.getToken());
                    if (mediaAuthList.hasNext())
                        System.out.print(", ");
                }
                System.out.println("");
            }
            else
                System.out.println(".: NOT containing P-Media-Authorization... ");
        }
        catch (Exception ex)
        {
            System.out.println("(!) Exception getting P-Media-Authorization header! - " + ex);
        }

        // Security-Client header
        ListIterator secClientList;
        try {
            secClientList = request.getHeaders(SecurityClientHeader.NAME);

            if (secClientList != null)
            {
                while (secClientList.hasNext())
                {
                    System.out.println(".: "
                            + ((SecurityClientHeader)secClientList.next()).toString());
                }
            }
            else
                System.out.println(".: NOT containing Security-Client header... ");
        }
        catch (Exception ex)
        {
            System.out.println("(!) Exception getting Security-Client header! - " + ex);
        }


        // this is only to illustrate the usage of this headers
        // send Security-Server if Require: sec-agree
        SecurityServerList secServerList = null;
        if (requireOptionTags.indexOf("sec-agree") != -1)
        {
            secServerList = new SecurityServerList();
            try {
                SecurityServerHeader secServer1 =
                    headerFactoryImpl.createSecurityClientHeader();
                secServer1.setSecurityMechanism("ipsec-3gpp");
                secServer1.setAlgorithm("hmac-md5-96");
                secServer1.setEncryptionAlgorithm("des-cbc");
                secServer1.setSPIClient(10000);
                secServer1.setSPIServer(10001);
                secServer1.setPortClient(5063);
                secServer1.setPortServer(4166);
                secServer1.setPreference(0.1f);

                SecurityServerHeader secServer2 =
                    headerFactoryImpl.createSecurityClientHeader();
                secServer2.setSecurityMechanism("ipsec-3gpp");
                secServer2.setAlgorithm("hmac-md5-96");
                secServer2.setEncryptionAlgorithm("des-cbc");
                secServer2.setSPIClient(20000);
                secServer2.setSPIServer(20001);
                secServer2.setPortClient(5073);
                secServer2.setPortServer(4286);
                secServer2.setPreference(0.5f);

                request.addHeader(secServer1);
                request.addHeader(secServer2);

            }
            catch (Exception ex)
            {
                System.out.println("(!) Exception adding Security-Server header : " + ex);
            }

        }

        // check Path header
        ListIterator<Header> pathList = (ListIterator<Header>)request.getHeaders(PathHeader.NAME);
        if (pathList != null && pathList.hasNext())
        {
            System.out.print(".: Path received : ");
            while (pathList.hasNext())
            {
                PathHeader path = (PathHeader)pathList.next();
                if (path != null)
                    System.out.print(path.getAddress().toString());
                if (pathList.hasNext())
                    System.out.print(", ");
            }
            System.out.println("");
        }

        /////////////////////////////////////////



        try {
            System.out.println("shootme: got an Invite sending Trying");
            // System.out.println("shootme: " + request);
            Response response = messageFactory.createResponse(Response.TRYING,
                    request);
            ServerTransaction st = requestEvent.getServerTransaction();

            if (st == null) {
                st = sipProvider.getNewServerTransaction(request);
            }
            dialog = st.getDialog();

            st.sendResponse(response);

            this.okResponse = messageFactory.createResponse(Response.OK,
                    request);
            Address address = addressFactory.createAddress("Shootme <sip:"
                    + myAddress + ":" + myPort + ">");
            ContactHeader contactHeader = headerFactory
                    .createContactHeader(address);
            response.addHeader(contactHeader);
            ToHeader toHeader = (ToHeader) okResponse.getHeader(ToHeader.NAME);
            toHeader.setTag("4321"); // Application is supposed to set.
            okResponse.addHeader(contactHeader);
            this.inviteTid = st;
            // Defer sending the OK to simulate the phone ringing.
            // Answered in 1 second ( this guy is fast at taking calls)
            this.inviteRequest = request;


            // add Security-Server header
            if (secServerList != null && !secServerList.isEmpty())
            {
                RequireHeader requireHeader = headerFactory.createRequireHeader("sec-agree");
                this.okResponse.setHeader(requireHeader);

                this.okResponse.setHeader(secServerList);
            }



            new Timer().schedule(new MyTimerTask(this), 1000);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(0);
        }
    }

    private void sendInviteOK() {
        try {
            if (inviteTid.getState() != TransactionState.COMPLETED) {
                System.out.println("shootme: Dialog state before 200: "
                        + inviteTid.getDialog().getState());
                inviteTid.sendResponse(okResponse);
                System.out.println("shootme: Dialog state after 200: "
                        + inviteTid.getDialog().getState());
            }
        } catch (SipException ex) {
            ex.printStackTrace();
        } catch (InvalidArgumentException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Process the bye request.
     */
    public void processBye(RequestEvent requestEvent,
            ServerTransaction serverTransactionId) {
        SipProvider sipProvider = (SipProvider) requestEvent.getSource();
        Request request = requestEvent.getRequest();
        Dialog dialog = requestEvent.getDialog();
        System.out.println("local party = " + dialog.getLocalParty());
        try {
            System.out.println("shootme:  got a bye sending OK.");
            Response response = messageFactory.createResponse(200, request);
            serverTransactionId.sendResponse(response);
            System.out.println("Dialog State is "
                    + serverTransactionId.getDialog().getState());

        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(0);

        }
    }

    public void processCancel(RequestEvent requestEvent,
            ServerTransaction serverTransactionId) {
        SipProvider sipProvider = (SipProvider) requestEvent.getSource();
        Request request = requestEvent.getRequest();
        try {
            System.out.println("shootme:  got a cancel.");
            if (serverTransactionId == null) {
                System.out.println("shootme:  null tid.");
                return;
            }
            Response response = messageFactory.createResponse(200, request);
            serverTransactionId.sendResponse(response);
            if (dialog.getState() != DialogState.CONFIRMED) {
                response = messageFactory.createResponse(
                        Response.REQUEST_TERMINATED, inviteRequest);
                inviteTid.sendResponse(response);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(0);

        }
    }

    public void processTimeout(javax.sip.TimeoutEvent timeoutEvent) {
        Transaction transaction;
        if (timeoutEvent.isServerTransaction()) {
            transaction = timeoutEvent.getServerTransaction();
        } else {
            transaction = timeoutEvent.getClientTransaction();
        }
        System.out.println("state = " + transaction.getState());
        System.out.println("dialog = " + transaction.getDialog());
        System.out.println("dialogState = "
                + transaction.getDialog().getState());
        System.out.println("Transaction Time out");
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
            headerFactory = sipFactory.createHeaderFactory();
            addressFactory = sipFactory.createAddressFactory();
            messageFactory = sipFactory.createMessageFactory();
            ListeningPoint lp = sipStack.createListeningPoint("127.0.0.1",
                    myPort, "udp");

            Shootme listener = this;

            SipProvider sipProvider = sipStack.createSipProvider(lp);
            System.out.println("udp provider " + sipProvider);
            sipProvider.addSipListener(listener);

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace();
            usage();
        }

    }

    public static void main(String args[]) {
        new Shootme().init();
    }

    public void processIOException(IOExceptionEvent exceptionEvent) {
        System.out.println("IOException");

    }

    public void processTransactionTerminated(
            TransactionTerminatedEvent transactionTerminatedEvent) {
        if (transactionTerminatedEvent.isServerTransaction())
            System.out.println("Transaction terminated event recieved"
                    + transactionTerminatedEvent.getServerTransaction());
        else
            System.out.println("Transaction terminated "
                    + transactionTerminatedEvent.getClientTransaction());

    }

    public void processDialogTerminated(
            DialogTerminatedEvent dialogTerminatedEvent) {
        System.out.println("Dialog terminated event recieved");
        Dialog d = dialogTerminatedEvent.getDialog();
        System.out.println("Local Party = " + d.getLocalParty());

    }

}
