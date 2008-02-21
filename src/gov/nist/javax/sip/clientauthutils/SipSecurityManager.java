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

import java.text.*;
import java.util.*;
import javax.sip.*;
import javax.sip.address.SipURI;
import javax.sip.header.*;
import javax.sip.message.*;

import org.apache.log4j.Logger;

/**
* The class handles authentication challenges, caches user credentials and
* takes care (through the SecurityAuthority interface) about retrieving
* passwords. 
* 
* 
* @author Emil Ivov
* @author Jeroen van Bemmel
* @author M. Ranganathan
* @version 1.1
*/

public class SipSecurityManager {
	private static final Logger logger = Logger
			.getLogger(SipSecurityManager.class);

	/**
	 * Credentials cached so far.
	 */
	private CredentialsCache cachedCredentials = new CredentialsCache();

	/**
	 * The account manager for the system. Stores user credentials.
	 */
	private AccountManager accountManager = null;
	
	/*
	 * Header factory for this security manager.
	 */
	private HeaderFactory headerFactory;
	

	/**
	 * Default constructor for the security manager. There is one Account
	 * manager. There is one SipSecurity manager for every user name,
	 * 
	 * @param accountID
	 *            the id of the account that this security manager is going to
	 *            serve. We concatenate the user with the domain to get the
	 *            account id.
	 */
	public SipSecurityManager(AccountManager accountManager, HeaderFactory headerFactory) {
		this.accountManager = accountManager;
		this.headerFactory = headerFactory;

	}

	/**
	 * Uses securityAuthority to determinie a set of valid user credentials for
	 * the specified Response (Challenge) and appends it to the challenged
	 * request so that it could be retransmitted.
	 * 
	 * 
	 * 
	 * @param challenge
	 *            the 401/407 challenge response
	 * @param challengedTransaction
	 *            the transaction established by the challenged request
	 * @param transactionCreator
	 *            the JAIN SipProvider that we should use to create the new
	 *            transaction.
	 * 
	 * @return a transaction containing a reoriginated request with the
	 *         necessary authorization header.
	 * @throws SipException
	 *             if we get an exception white creating the new transaction
	 * @throws InvalidArgumentException
	 *             if we fail to create a new header containing user
	 *             credentials.
	 * @throws ParseException
	 *             if we fail to create a new header containing user
	 *             credentials.
	 * @throws NullPointerException
	 *             if an argument or a header is null.
	 * @throws OperationFailedException
	 *             if we fail to acquire a password from our security authority.
	 */
	public ClientTransaction handleChallenge(Response challenge,
			ClientTransaction challengedTransaction,
			SipProvider transactionCreator) throws SipException,
			InvalidArgumentException, ParseException, NullPointerException {
	
		Request challengedRequest = challengedTransaction.getRequest();

		Request reoriginatedRequest = (Request) challengedRequest.clone();

		// remove the branch id so that we could use the request in a new
		// transaction
		removeBranchID(reoriginatedRequest);

		if (challenge == null || reoriginatedRequest == null) {
			throw new NullPointerException(
					"A null argument was passed to handle challenge.");
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
		CSeqHeader cSeq = (CSeqHeader) reoriginatedRequest
				.getHeader((CSeqHeader.NAME));
		cSeq.setSeqNumber(cSeq.getSeqNumber() + 1l);

		ClientTransaction retryTran = transactionCreator
				.getNewClientTransaction(reoriginatedRequest);

		WWWAuthenticateHeader authHeader = null;
		while (authHeaders.hasNext()) {
			authHeader = (WWWAuthenticateHeader) authHeaders.next();
			String realm = authHeader.getRealm();

			UserCredentials userCreds = this.accountManager
					.getCredentials(realm);
			if (userCreds == null)
				throw new SipException(
						"Cannot find user creds for the given user name and realm");

			// we haven't yet authentified this realm since we were
			// started.

			AuthorizationHeader authorization = this.getAuthorization(
					reoriginatedRequest.getMethod(), reoriginatedRequest
							.getRequestURI().toString(), 
							(reoriginatedRequest.getContent() == null) ? "" : new String(reoriginatedRequest.getRawContent()),
							authHeader, userCreds);
			logger.debug("Created authorization header: "
					+ authorization.toString());

			cachedCredentials.cacheAuthorizationHeader(userCreds
					.getSipDomain(), authorization);

			reoriginatedRequest.addHeader(authorization);
		}

		logger.debug("Returning authorization transaction.");
		return retryTran;
	}

	/**
	 * Generates an authorisation header in response to wwwAuthHeader.
	 * 
	 * @param method
	 *            method of the request being authenticated
	 * @param uri
	 *            digest-uri
	 * @param requestBody
	 *            the body of the request.
	 * @param authHeader
	 *            the challenge that we should respond to
	 * @param userCredentials
	 *            username and pass
	 * 
	 * @return an authorisation header in response to authHeader.
	 * 
	 * @throws OperationFailedException
	 *             if auth header was malformated.
	 */
	private AuthorizationHeader getAuthorization(String method, String uri,
			String requestBody, WWWAuthenticateHeader authHeader,
			UserCredentials userCredentials) {
		String response = null;

		// JvB: authHeader.getQop() is a quoted _list_ of qop values
		// (e.g. "auth,auth-int") Client is supposed to pick one
		String qopList = authHeader.getQop();
		String qop = (qopList != null) ? "auth" : null;
		String nc_value = "00000001";
		String cnonce = "xyz";

		response = MessageDigestAlgorithm.calculateResponse(authHeader
				.getAlgorithm(), userCredentials.getUserName(), authHeader
				.getRealm(), userCredentials.getPassword(), authHeader
				.getNonce(), nc_value, // JvB added
				cnonce, // JvB added
				method, uri, requestBody, qop);// jvb changed

		AuthorizationHeader authorization = null;
		try {
			if (authHeader instanceof ProxyAuthenticateHeader) {
				authorization = headerFactory
						.createProxyAuthorizationHeader(authHeader.getScheme());
			} else {
				authorization = headerFactory
						.createAuthorizationHeader(authHeader.getScheme());
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
			throw new RuntimeException(
					"Failed to create an authorization header!");
		}

		return authorization;
	}

	/**
	 * Removes all via headers from <tt>request</tt> and replaces them with a
	 * new one, equal to the one that was top most.
	 * 
	 * @param request
	 *            the Request whose branchID we'd like to remove.
	 * 
	 * @throws ParseException
	 *             in case the host port or transport in the original request
	 *             were malformed
	 * @throws InvalidArgumentException
	 *             if the port in the original via header was invalid.
	 */
	private void removeBranchID(Request request) throws ParseException,
			InvalidArgumentException {
		ViaHeader viaHeader = (ViaHeader) request.getHeader(ViaHeader.NAME);

		viaHeader.removeParameter("branch");

		
	}

	/**
	 * Returns an authorization header cached against the specified
	 * <tt>userCredentials</tt> or <tt>null</tt> if no auth. header has been
	 * previously cached for this callID.
	 * 
	 * @return the <tt>AuthorizationHeader</tt> cached against the specified
	 *         call ID or null if no such header has been cached.
	 */
	public Collection<AuthorizationHeader> getCachedAuthorizationHeaders(
			UserCredentials accountInfo, String userName) {
		return this.cachedCredentials.getCachedAuthorizationHeaders(accountInfo
				.getSipDomain(), userName);
	}

}

