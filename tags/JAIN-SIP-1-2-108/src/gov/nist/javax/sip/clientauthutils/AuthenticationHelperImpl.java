package gov.nist.javax.sip.clientauthutils;

/*
 *
 * This code has been contributed with permission from:
 *
 * SIP Communicator, the OpenSource Java VoIP and Instant Messaging client but has been significantly changed.
 * It is donated to the JAIN-SIP project as it is common code that many sip clients
 * need to perform class and others will consitute a set of utility functions
 * that will implement common operations that ease the life of the developer.
 * 
 * Acknowledgements:
 * ----------------
 * 
 * Fredrik Wickstrom reported that dialog cseq counters are not incremented
 * when resending requests. He later uncovered additional problems and
 * proposed a way to fix them (his proposition was taken into account).	
 */

import gov.nist.core.InternalErrorHandler;
import gov.nist.javax.sip.SipStackImpl;
import gov.nist.javax.sip.address.AddressImpl;
import gov.nist.javax.sip.address.SipUri;
import gov.nist.javax.sip.header.Route;
import gov.nist.javax.sip.header.RouteList;
import gov.nist.javax.sip.message.SIPRequest;
import gov.nist.javax.sip.stack.SIPClientTransaction;
import gov.nist.javax.sip.stack.SIPTransactionStack;

import java.text.*;
import java.util.*;
import javax.sip.*;
import javax.sip.address.Address;
import javax.sip.address.Hop;
import javax.sip.address.SipURI;
import javax.sip.header.*;
import javax.sip.message.*;

import org.apache.log4j.Logger;

/**
 * The class handles authentication challenges, caches user credentials and takes care (through
 * the SecurityAuthority interface) about retrieving passwords.
 * 
 * 
 * @author Emil Ivov
 * @author Jeroen van Bemmel
 * @author M. Ranganathan
 * 
 * @since 2.0
 */

public class AuthenticationHelperImpl implements AuthenticationHelper {

    /**
     * Credentials cached so far.
     */
    private CredentialsCache cachedCredentials;

    /**
     * The account manager for the system. Stores user credentials.
     */
    private AccountManager accountManager = null;

    /*
     * Header factory for this security manager.
     */
    private HeaderFactory headerFactory;

    private SipStackImpl sipStack;

    Timer timer;

    /**
     * Default constructor for the security manager. There is one Account manager. There is one
     * SipSecurity manager for every user name,
     * 
     * @param sipStack -- our stack.
     * @param accountManger -- an implementation of the AccountManager interface.
     * @param headerFactory -- header factory.
     */
    public AuthenticationHelperImpl(SipStackImpl sipStack, AccountManager accountManager,
            HeaderFactory headerFactory) {
        this.accountManager = accountManager;
        this.headerFactory = headerFactory;
        this.sipStack = sipStack;

        this.cachedCredentials = new CredentialsCache(((SIPTransactionStack) sipStack).getTimer());
    }

    /*
     * (non-Javadoc)
     * 
     * @see gov.nist.javax.sip.clientauthutils.AuthenticationHelper#handleChallenge(javax.sip.message.Response,
     *      javax.sip.ClientTransaction, javax.sip.SipProvider)
     */
    public ClientTransaction handleChallenge(Response challenge,
            ClientTransaction challengedTransaction, SipProvider transactionCreator, int cacheTime)
            throws SipException, NullPointerException {
        try {
            if (sipStack.isLoggingEnabled()) {
                sipStack.getLogWriter().logDebug("handleChallenge: " + challenge);
            }

            SIPRequest challengedRequest = ((SIPRequest) challengedTransaction.getRequest());

            String callId = challengedRequest.getCallId().getCallId();

            Request reoriginatedRequest = (Request) challengedRequest.clone();

            // remove the branch id so that we could use the request in a new
            // transaction
            removeBranchID(reoriginatedRequest);

            if (challenge == null || reoriginatedRequest == null) {
                throw new NullPointerException("A null argument was passed to handle challenge.");
            }

            ListIterator authHeaders = null;

            if (challenge.getStatusCode() == Response.UNAUTHORIZED) {
                authHeaders = challenge.getHeaders(WWWAuthenticateHeader.NAME);
            } else if (challenge.getStatusCode() == Response.PROXY_AUTHENTICATION_REQUIRED) {
                authHeaders = challenge.getHeaders(ProxyAuthenticateHeader.NAME);
            } else {
                throw new IllegalArgumentException("Unexpected status code ");
            }

            if (authHeaders == null) {
                throw new IllegalArgumentException(
                        "Could not find WWWAuthenticate or ProxyAuthenticate headers");
            }

            // Remove all authorization headers from the request (we'll re-add them
            // from cache)
            reoriginatedRequest.removeHeader(AuthorizationHeader.NAME);
            reoriginatedRequest.removeHeader(ProxyAuthorizationHeader.NAME);

            // rfc 3261 says that the cseq header should be augmented for the new
            // request. do it here so that the new dialog (created together with
            // the new client transaction) takes it into account.
            // Bug report - Fredrik Wickstrom
            CSeqHeader cSeq = (CSeqHeader) reoriginatedRequest.getHeader((CSeqHeader.NAME));
            try {
                cSeq.setSeqNumber(cSeq.getSeqNumber() + 1l);
            } catch (InvalidArgumentException ex) {
                throw new SipException("Invalid CSeq -- could not increment : "
                        + cSeq.getSeqNumber());
            }
            
        
            /* Resolve this to the next hop based on the previous lookup. If we are not using
             * lose routing (RFC2543) then just attach hop as a maddr param.
             */
            if ( challengedRequest.getRouteHeaders() == null ) { 
                Hop hop   = ((SIPClientTransaction) challengedTransaction).getNextHop();
                SipURI sipUri = (SipURI) reoriginatedRequest.getRequestURI();
                sipUri.setMAddrParam(hop.getHost());
                if ( hop.getPort() != -1 ) sipUri.setPort(hop.getPort());
            }
            ClientTransaction retryTran = transactionCreator
            .getNewClientTransaction(reoriginatedRequest);
            
            WWWAuthenticateHeader authHeader = null;
            SipURI requestUri = (SipURI) challengedTransaction.getRequest().getRequestURI();
            while (authHeaders.hasNext()) {
                authHeader = (WWWAuthenticateHeader) authHeaders.next();
                String realm = authHeader.getRealm();

                UserCredentials userCreds = this.accountManager.getCredentials(challengedTransaction, realm);
                if (userCreds == null)
                    throw new SipException(
                            "Cannot find user creds for the given user name and realm");

                // we haven't yet authentified this realm since we were
                // started.

                AuthorizationHeader authorization = this.getAuthorization(reoriginatedRequest
                        .getMethod(), reoriginatedRequest.getRequestURI().toString(),
                        (reoriginatedRequest.getContent() == null) ? "" : new String(
                                reoriginatedRequest.getRawContent()), authHeader, userCreds);
                sipStack.getLogWriter().logDebug(
                        "Created authorization header: " + authorization.toString());

                if (cacheTime != 0)
                    cachedCredentials.cacheAuthorizationHeader(userCreds.getSipDomain(),
                            authorization, cacheTime);

                reoriginatedRequest.addHeader(authorization);
            }

            if (sipStack.isLoggingEnabled()) {
                sipStack.getLogWriter().logDebug(
                        "Returning authorization transaction." + retryTran);
            }
            return retryTran;
        } catch (SipException ex) {
            throw ex;
        } catch (Exception ex) {
            sipStack.getLogWriter().logError("Unexpected exception ", ex);
            throw new SipException("Unexpected exception ", ex);
        }
    }

    /**
     * Generates an authorisation header in response to wwwAuthHeader.
     * 
     * @param method method of the request being authenticated
     * @param uri digest-uri
     * @param requestBody the body of the request.
     * @param authHeader the challenge that we should respond to
     * @param userCredentials username and pass
     * 
     * @return an authorisation header in response to authHeader.
     * 
     * @throws OperationFailedException if auth header was malformated.
     */
    private AuthorizationHeader getAuthorization(String method, String uri, String requestBody,
            WWWAuthenticateHeader authHeader, UserCredentials userCredentials) {
        String response = null;

        // JvB: authHeader.getQop() is a quoted _list_ of qop values
        // (e.g. "auth,auth-int") Client is supposed to pick one
        String qopList = authHeader.getQop();
        String qop = (qopList != null) ? "auth" : null;
        String nc_value = "00000001";
        String cnonce = "xyz";

        response = MessageDigestAlgorithm.calculateResponse(authHeader.getAlgorithm(),
                userCredentials.getUserName(), authHeader.getRealm(), userCredentials
                        .getPassword(), authHeader.getNonce(), nc_value, // JvB added
                cnonce, // JvB added
                method, uri, requestBody, qop,sipStack.getLogWriter());// jvb changed

        AuthorizationHeader authorization = null;
        try {
            if (authHeader instanceof ProxyAuthenticateHeader) {
                authorization = headerFactory.createProxyAuthorizationHeader(authHeader
                        .getScheme());
            } else {
                authorization = headerFactory.createAuthorizationHeader(authHeader.getScheme());
            }

            authorization.setUsername(userCredentials.getUserName());
            authorization.setRealm(authHeader.getRealm());
            authorization.setNonce(authHeader.getNonce());
            authorization.setParameter("uri", uri);
            authorization.setResponse(response);
            if (authHeader.getAlgorithm() != null) {
                authorization.setAlgorithm(authHeader.getAlgorithm());
            }

            if (authHeader.getOpaque() != null) {
                authorization.setOpaque(authHeader.getOpaque());
            }

            // jvb added
            if (qop != null) {
                authorization.setQop(qop);
                authorization.setCNonce(cnonce);
                authorization.setNonceCount(Integer.parseInt(nc_value));
            }

            authorization.setResponse(response);

        } catch (ParseException ex) {
            throw new RuntimeException("Failed to create an authorization header!");
        }

        return authorization;
    }

    /**
     * Removes all via headers from <tt>request</tt> and replaces them with a new one, equal to
     * the one that was top most.
     * 
     * @param request the Request whose branchID we'd like to remove.
     * 
     */
    private void removeBranchID(Request request) {

        ViaHeader viaHeader = (ViaHeader) request.getHeader(ViaHeader.NAME);

        viaHeader.removeParameter("branch");

    }

    /*
     * (non-Javadoc)
     * 
     * @see gov.nist.javax.sip.clientauthutils.AuthenticationHelper#attachAuthenticationHeaders(javax.sip.message.Request)
     */
    public void setAuthenticationHeaders(Request request) {
        SIPRequest sipRequest = (SIPRequest) request;

        String callId = sipRequest.getCallId().getCallId();

        request.removeHeader(AuthorizationHeader.NAME);
        Collection<AuthorizationHeader> authHeaders = this.cachedCredentials
                .getCachedAuthorizationHeaders(callId);
        if (authHeaders == null) {
            sipStack.getLogWriter().logDebug(
                    "Could not find authentication headers for " + callId);
            return;
        }

        for (AuthorizationHeader authHeader : authHeaders) {
            request.addHeader(authHeader);
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see gov.nist.javax.sip.clientauthutils.AuthenticationHelper#removeCachedAuthenticationHeaders(java.lang.String)
     */
    public void removeCachedAuthenticationHeaders(String callId) {
        if (callId == null)
            throw new NullPointerException("Null callId argument ");
        this.cachedCredentials.removeAuthenticationHeader(callId);

    }

}
