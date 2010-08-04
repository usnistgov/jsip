package performance.b2bua;

import gov.nist.javax.sip.ListeningPointImpl;
import gov.nist.javax.sip.header.Route;
import gov.nist.javax.sip.header.RouteList;
import gov.nist.javax.sip.header.Via;
import gov.nist.javax.sip.header.ViaList;
import gov.nist.javax.sip.message.SIPRequest;

import java.text.ParseException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Set;

import javax.sip.ClientTransaction;
import javax.sip.Dialog;
import javax.sip.DialogState;
import javax.sip.InvalidArgumentException;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.ServerTransaction;
import javax.sip.SipException;
import javax.sip.SipProvider;
import javax.sip.address.SipURI;
import javax.sip.address.URI;
import javax.sip.header.CSeqHeader;
import javax.sip.header.CallIdHeader;
import javax.sip.header.ContactHeader;
import javax.sip.header.ContentLengthHeader;
import javax.sip.header.ContentTypeHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.Header;
import javax.sip.header.HeaderFactory;
import javax.sip.header.RecordRouteHeader;
import javax.sip.header.RouteHeader;
import javax.sip.header.ToHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;

public class TestCall {
	
	private String localTag;
	private CallIdHeader outgoingDialogCallId;
	
	private SipProvider sipProvider;
	private MessageFactory messageFactory;
	private HeaderFactory headerFactory;
	private Dialog incomingDialog;
	private Dialog outgoingDialog;
	private ServerTransaction serverTransaction;
	
	public TestCall(String localTag, SipProvider sipProvider, HeaderFactory headerFactory, MessageFactory messageFactory) {
		this.localTag = localTag;
		this.sipProvider = sipProvider;
		this.messageFactory = messageFactory;
		this.headerFactory = headerFactory;
	}
	
	/**
	 * @return the incomingDialog
	 */
	public Dialog getIncomingDialog() {
		return incomingDialog;
	}
	
	/**
	 * @return the outgoingDialog
	 */
	public Dialog getOutgoingDialog() {
		return outgoingDialog;
	}
	
	public void processInvite(RequestEvent requestEvent) {
		//System.out.println("Got invite: "+requestEvent.getRequest());
		try {
			serverTransaction = requestEvent.getServerTransaction();
			if (serverTransaction == null) {
				try {
					serverTransaction = sipProvider.getNewServerTransaction(requestEvent.getRequest());
				}
				catch (Exception e) {
					e.printStackTrace();
					return;
				}
			}
			//serverTransaction.sendResponse(messageFactory.createResponse(100, requestEvent.getRequest()));
			setupIncomingDialog();
			forwardInvite();			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	/**
	 * @param serverTransaction2
	 * @return
	 * @throws SipException 
	 */
	private void setupIncomingDialog() throws SipException {
		this.incomingDialog = sipProvider.getNewDialog(serverTransaction);
		this.incomingDialog.setApplicationData(this);
	}
	
	/**
	 * @param incomingDialog2
	 * @return
	 * @throws SipException 
	 */
	private void forwardInvite() throws SipException {
		this.outgoingDialogCallId = sipProvider.getNewCallId();
		Request request = createRequest(serverTransaction.getRequest());
		ClientTransaction ct = sipProvider.getNewClientTransaction(request);
		this.outgoingDialog = sipProvider.getNewDialog(ct);
		this.outgoingDialog.setApplicationData(this);
		ct.sendRequest();
	}

	@SuppressWarnings("unchecked")
	private Request createRequest(Request origRequest) throws SipException {

		final SIPRequest request = (SIPRequest) origRequest.clone();

		try {
			request.getFromHeader().setTag(localTag);
		} catch (ParseException e1) {
			throw new SipException("failed to set local tag", e1);
		}

		final String transport = request.getTopmostViaHeader().getTransport();
		final ListeningPointImpl listeningPointImpl = (ListeningPointImpl) sipProvider.getListeningPoint(transport);

		final ViaList viaList = new ViaList();
		viaList.add((Via) listeningPointImpl.createViaHeader());
		request.setVia(viaList);
		
		try {
			request.setHeader(headerFactory.createMaxForwardsHeader(70));
		} catch (InvalidArgumentException e) {
			throw new SipException("Failed to create max forwards header",e);
		}
		request.setHeader((Header) outgoingDialogCallId.clone());
		// note: cseq will be set by dialog when sending
		// set contact if the original response had it
		if (origRequest.getHeader(ContactHeader.NAME) != null) {
			request.setHeader(listeningPointImpl.createContactHeader());
		}

		/*
		 * Route header fields of the upstream request MAY be copied in the
		 * downstream request, except the topmost Route header if it is under
		 * the responsibility of the B2BUA. Additional Route header fields MAY
		 * also be added to the downstream request.
		 */
		if (outgoingDialog == null || outgoingDialog.getState() == null) {
			// first request, no route available
			final RouteList routeList = request.getRouteHeaders();
			if (routeList != null) {
				final RouteHeader topRoute = routeList.get(0);
				final URI topRouteURI = topRoute.getAddress().getURI();
				if (topRouteURI.isSipURI()) {
					final SipURI topRouteSipURI = (SipURI) topRouteURI;
					if (topRouteSipURI.getHost().equals(listeningPointImpl.getIPAddress())
							&& topRouteSipURI.getPort() == listeningPointImpl.getPort()) {
						if (routeList.size() > 1) {
							routeList.remove(0);
						}
						else {
							request.removeHeader(RouteHeader.NAME);
						}					
					}
				}
			}			
		}
		else {
			// replace route in orig request with the one in dialog
			request.removeHeader(RouteHeader.NAME);
			final RouteList routeList = new RouteList();
			for (Iterator<Route> it = outgoingDialog.getRouteSet(); it.hasNext();) {
				Route route = it.next();				
				routeList.add(route);								
			}
			if (!routeList.isEmpty()) {
				request.addHeader(routeList);
			}
		}
		
		/*
		 * Record-Route header fields of the upstream request are not copied in
		 * the new downstream request, as Record-Route is only meaningful for
		 * the upstream dialog.
		 */
		request.removeHeader(RecordRouteHeader.NAME);

		return request;		
	}

	public void processAck(RequestEvent requestEvent) {
		// ignore
	}

	public void processBye(RequestEvent requestEvent) {
		try {
			requestEvent.getServerTransaction().sendResponse(messageFactory.createResponse(200, requestEvent.getRequest()));
			final Request request = createRequest(requestEvent.getRequest());
			final ClientTransaction ct = sipProvider.getNewClientTransaction(request);
			outgoingDialog.sendRequest(ct);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void process180(ResponseEvent responseEvent) {
		try {
			forwardResponse(responseEvent.getResponse());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * @param responseEvent
	 * @throws InvalidArgumentException 
	 */
	@SuppressWarnings("unchecked")
	private void forwardResponse(Response receivedResponse) throws SipException, InvalidArgumentException {

		final ServerTransaction origServerTransaction = this.serverTransaction;	
		Response forgedResponse = null;
		
		try {
			forgedResponse = messageFactory.createResponse(receivedResponse.getStatusCode(), origServerTransaction.getRequest());
		} catch (ParseException e) {
			throw new SipException("Failed to forge message", e);
		}
		
		final DialogState dialogState = incomingDialog.getState();
		if ((dialogState == null || dialogState == DialogState.EARLY) && localTag != null && incomingDialog.isServer()) {
			// no tag set in the response, since the dialog creating transaction didn't had it
			try {
				((ToHeader)forgedResponse.getHeader(ToHeader.NAME)).setTag(localTag);
			} catch (ParseException e) {
				throw new SipException("Failed to set local tag", e);
			}
		}
		
		// copy headers 
		ListIterator<String> lit = receivedResponse.getHeaderNames();
		String headerName = null;
		ListIterator<Header> headersIterator = null;
		while (lit.hasNext()) {
			headerName = lit.next();
			if (TestCall.getHeadersToOmmitOnResponseCopy().contains(headerName)) {
				continue;
			} else {
				forgedResponse.removeHeader(headerName);
				headersIterator = receivedResponse.getHeaders(headerName);
				while (headersIterator.hasNext()) {
					forgedResponse.addLast((Header)headersIterator.next().clone());
				}
			}
		}
		
		// Copy content
		final byte[] rawOriginal = receivedResponse.getRawContent();
		if (rawOriginal != null && rawOriginal.length != 0) {
			final byte[] copy = new byte[rawOriginal.length];
			System.arraycopy(rawOriginal, 0, copy, 0, copy.length);
			try {
				forgedResponse.setContent(copy, (ContentTypeHeader) forgedResponse
						.getHeader(ContentTypeHeader.NAME));
			} catch (ParseException e) {
				throw new SipException("Failed to copy content.",e);
			}
		}
		
		// set contact if the received response had it
		if (receivedResponse.getHeader(ContactHeader.NAME) != null) {
			final String transport = ((ViaHeader) forgedResponse.getHeader(ViaHeader.NAME)).getTransport();
			forgedResponse.setHeader(((ListeningPointImpl)sipProvider.getListeningPoint(transport)).createContactHeader());
		}
		
		origServerTransaction.sendResponse(forgedResponse);
	}

	public void process200(ResponseEvent responseEvent) {
		try {
			final CSeqHeader cSeqHeader = (CSeqHeader) responseEvent.getResponse().getHeader(CSeqHeader.NAME); 			
			if (cSeqHeader.getMethod().equals(Request.INVITE)) {
				processInvite200(responseEvent,cSeqHeader);
			}
			else if (cSeqHeader.getMethod().equals(Request.BYE)) {
				processBye200(responseEvent);
			}
			else {
				System.err.println("Unexpected response: "+responseEvent.getResponse());
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * @param responseEvent
	 * @throws SipException 
	 * @throws InvalidArgumentException 
	 */
	private void processInvite200(ResponseEvent responseEvent,CSeqHeader cseq) throws InvalidArgumentException, SipException {
		// lets ack it ourselves to avoid UAS retransmissions due to
		// forwarding of this response and further UAC Ack
		// note that the app does not handles UAC ACKs
		final Request ack = responseEvent.getDialog().createAck(cseq.getSeqNumber());
		responseEvent.getDialog().sendAck(ack);
		forwardResponse(responseEvent.getResponse());			
	}
	
	/**
	 * @param responseEvent
	 */
	private void processBye200(ResponseEvent responseEvent) {
		// nothing to do
	}

	private static Set<String> HEADERS_TO_OMMIT_ON_RESPONSE_COPY;

	private static Set<String> getHeadersToOmmitOnResponseCopy() {
		if (HEADERS_TO_OMMIT_ON_RESPONSE_COPY == null) {
			final Set<String> set = new HashSet<String>();
			set.add(RouteHeader.NAME);
			set.add(RecordRouteHeader.NAME);
			set.add(ViaHeader.NAME);
			set.add(CallIdHeader.NAME);
			set.add(CSeqHeader.NAME);
			set.add(ContactHeader.NAME);
			set.add(FromHeader.NAME);
			set.add(ToHeader.NAME);
			set.add(ContentLengthHeader.NAME);
			HEADERS_TO_OMMIT_ON_RESPONSE_COPY = Collections.unmodifiableSet(set);
		}
		return HEADERS_TO_OMMIT_ON_RESPONSE_COPY;
	}
	
}
