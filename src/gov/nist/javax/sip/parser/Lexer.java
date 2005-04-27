package gov.nist.javax.sip.parser;

import gov.nist.core.*;
import javax.sip.header.*;
import java.util.Hashtable;

/**
 * Lexer class for the parser.
 *
 * @version  JAIN-SIP-1.1
 *
 * @author M. Ranganathan <mranga@nist.gov>  <br/>
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 */
public class Lexer extends LexerCore {
	/**
	 * get the header name of the line
	 * @return  the header name (stuff before the :)
	 * bug fix submitted by zvali@dev.java.net
	 */
	public static String getHeaderName(String line) {
		if (line == null)
			return null;
		String headerName = null;
		try {
			int begin = line.indexOf(":");
			headerName = null;
			if (begin >= 1)
				headerName = line.substring(0, begin).trim();
		} catch (IndexOutOfBoundsException e) {
			return null;
		}
		return headerName;
	}

	public Lexer(String lexerName, String buffer) {
		super(lexerName, buffer);
		this.selectLexer(lexerName);
	}

	/**
	 * get the header value of the line
	 * @return String
	 */
	public static String getHeaderValue(String line) {
		if (line == null)
			return null;
		String headerValue = null;
		try {
			int begin = line.indexOf(":");
			headerValue = line.substring(begin + 1);
		} catch (IndexOutOfBoundsException e) {
			return null;
		}
		return headerValue;
	}

	public void selectLexer(String lexerName) {
	     synchronized( lexerTables ) {
		// Synchronization Bug fix by Robert Rosen.
		currentLexer = (Hashtable) lexerTables.get(lexerName);
		this.currentLexerName = lexerName;
		if (currentLexer == null) {
			addLexer(lexerName);
			if (lexerName.equals("method_keywordLexer")) {
				addKeyword(
					TokenNames.REGISTER.toUpperCase(),
					TokenTypes.REGISTER);
				addKeyword(TokenNames.ACK.toUpperCase(), TokenTypes.ACK);
				addKeyword(
					TokenNames.OPTIONS.toUpperCase(),
 					TokenTypes.OPTIONS);
				addKeyword(TokenNames.BYE.toUpperCase(), TokenTypes.BYE);
				addKeyword(TokenNames.INVITE.toUpperCase(), TokenTypes.INVITE);
				addKeyword(TokenNames.SIP.toUpperCase(), TokenTypes.SIP);
				addKeyword(
					TokenNames.SUBSCRIBE.toUpperCase(),
					TokenTypes.SUBSCRIBE);
				addKeyword(TokenNames.NOTIFY.toUpperCase(), TokenTypes.NOTIFY);
			} else if (lexerName.equals("command_keywordLexer")) {
				addKeyword(
					ErrorInfoHeader.NAME.toUpperCase(),
					TokenTypes.ERROR_INFO);
				addKeyword(
					AllowEventsHeader.NAME.toUpperCase(),
					TokenTypes.ALLOW_EVENTS);
				addKeyword(
					AuthenticationInfoHeader.NAME.toUpperCase(),
					TokenTypes.AUTHENTICATION_INFO);
				addKeyword(EventHeader.NAME.toUpperCase(), TokenTypes.EVENT);
				addKeyword(
					MinExpiresHeader.NAME.toUpperCase(),
					TokenTypes.MIN_EXPIRES);
				addKeyword(RSeqHeader.NAME.toUpperCase(), TokenTypes.RSEQ);
				addKeyword(RAckHeader.NAME.toUpperCase(), TokenTypes.RACK);
				addKeyword(ReasonHeader.NAME.toUpperCase(), TokenTypes.REASON);
				addKeyword(
					ReplyToHeader.NAME.toUpperCase(),
					TokenTypes.REPLY_TO);
				addKeyword(
					SubscriptionStateHeader.NAME.toUpperCase(),
					TokenTypes.SUBSCRIPTION_STATE);
				addKeyword(
					TimeStampHeader.NAME.toUpperCase(),
					TokenTypes.TIMESTAMP);
				addKeyword(
					InReplyToHeader.NAME.toUpperCase(),
					TokenTypes.IN_REPLY_TO);
				addKeyword(
					MimeVersionHeader.NAME.toUpperCase(),
					TokenTypes.MIME_VERSION);
				addKeyword(
					AlertInfoHeader.NAME.toUpperCase(),
					TokenTypes.ALERT_INFO);
				addKeyword(FromHeader.NAME.toUpperCase(), TokenTypes.FROM);
				addKeyword(ToHeader.NAME.toUpperCase(), TokenTypes.TO);
				addKeyword(
					ReferToHeader.NAME.toUpperCase(),
					TokenTypes.REFER_TO);
				addKeyword(ViaHeader.NAME.toUpperCase(), TokenTypes.VIA);
				addKeyword(
					UserAgentHeader.NAME.toUpperCase(),
					TokenTypes.USER_AGENT);
				addKeyword(ServerHeader.NAME.toUpperCase(), TokenTypes.SERVER);
				addKeyword(
					AcceptEncodingHeader.NAME.toUpperCase(),
					TokenTypes.ACCEPT_ENCODING);
				addKeyword(AcceptHeader.NAME.toUpperCase(), TokenTypes.ACCEPT);
				addKeyword(AllowHeader.NAME.toUpperCase(), TokenTypes.ALLOW);
				addKeyword(RouteHeader.NAME.toUpperCase(), TokenTypes.ROUTE);
				addKeyword(
					AuthorizationHeader.NAME.toUpperCase(),
					TokenTypes.AUTHORIZATION);
				addKeyword(
					ProxyAuthorizationHeader.NAME.toUpperCase(),
					TokenTypes.PROXY_AUTHORIZATION);
				addKeyword(
					RetryAfterHeader.NAME.toUpperCase(),
					TokenTypes.RETRY_AFTER);
				addKeyword(
					ProxyRequireHeader.NAME.toUpperCase(),
					TokenTypes.PROXY_REQUIRE);
				addKeyword(
					ContentLanguageHeader.NAME.toUpperCase(),
					TokenTypes.CONTENT_LANGUAGE);
				addKeyword(
					UnsupportedHeader.NAME.toUpperCase(),
					TokenTypes.UNSUPPORTED);
				addKeyword(
					SupportedHeader.NAME.toUpperCase(),
					TokenTypes.SUPPORTED);
				addKeyword(
					WarningHeader.NAME.toUpperCase(),
					TokenTypes.WARNING);
				addKeyword(
					MaxForwardsHeader.NAME.toUpperCase(),
					TokenTypes.MAX_FORWARDS);
				addKeyword(DateHeader.NAME.toUpperCase(), TokenTypes.DATE);
				addKeyword(
					PriorityHeader.NAME.toUpperCase(),
					TokenTypes.PRIORITY);
				addKeyword(
					ProxyAuthenticateHeader.NAME.toUpperCase(),
					TokenTypes.PROXY_AUTHENTICATE);
				addKeyword(
					ContentEncodingHeader.NAME.toUpperCase(),
					TokenTypes.CONTENT_ENCODING);
				addKeyword(
					ContentLengthHeader.NAME.toUpperCase(),
					TokenTypes.CONTENT_LENGTH);
				addKeyword(
					SubjectHeader.NAME.toUpperCase(),
					TokenTypes.SUBJECT);
				addKeyword(
					ContentTypeHeader.NAME.toUpperCase(),
					TokenTypes.CONTENT_TYPE);
				addKeyword(
					ContactHeader.NAME.toUpperCase(),
					TokenTypes.CONTACT);
				addKeyword(CallIdHeader.NAME.toUpperCase(), TokenTypes.CALL_ID);
				addKeyword(
					RequireHeader.NAME.toUpperCase(),
					TokenTypes.REQUIRE);
				addKeyword(
					ExpiresHeader.NAME.toUpperCase(),
					TokenTypes.EXPIRES);
				addKeyword(
					RecordRouteHeader.NAME.toUpperCase(),
					TokenTypes.RECORD_ROUTE);
				addKeyword(
					OrganizationHeader.NAME.toUpperCase(),
					TokenTypes.ORGANIZATION);
				addKeyword(CSeqHeader.NAME.toUpperCase(), TokenTypes.CSEQ);
				addKeyword(
					AcceptLanguageHeader.NAME.toUpperCase(),
					TokenTypes.ACCEPT_LANGUAGE);
				addKeyword(
					WWWAuthenticateHeader.NAME.toUpperCase(),
					TokenTypes.WWW_AUTHENTICATE);
				addKeyword(
					CallInfoHeader.NAME.toUpperCase(),
					TokenTypes.CALL_INFO);
				addKeyword(
					ContentDispositionHeader.NAME.toUpperCase(),
					TokenTypes.CONTENT_DISPOSITION);
				// And now the dreaded short forms....
				addKeyword(TokenNames.K.toUpperCase(), TokenTypes.SUPPORTED);
				addKeyword(TokenNames.C.toUpperCase(), TokenTypes.CONTENT_TYPE);
				addKeyword(
					TokenNames.E.toUpperCase(),
					TokenTypes.CONTENT_ENCODING);
				addKeyword(TokenNames.F.toUpperCase(), TokenTypes.FROM);
				addKeyword(TokenNames.I.toUpperCase(), TokenTypes.CALL_ID);
				addKeyword(TokenNames.M.toUpperCase(), TokenTypes.CONTACT);
				addKeyword(
					TokenNames.L.toUpperCase(),
					TokenTypes.CONTENT_LENGTH);
				addKeyword(TokenNames.S.toUpperCase(), TokenTypes.SUBJECT);
				addKeyword(TokenNames.T.toUpperCase(), TokenTypes.TO);
				addKeyword(TokenNames.V.toUpperCase(), TokenTypes.VIA);
				addKeyword(TokenNames.R.toUpperCase(), TokenTypes.REFER_TO);
			        addKeyword(TokenNames.O.toUpperCase(), TokenTypes.EVENT); // Bug fix by Mario Mantak
			} else if (lexerName.equals("status_lineLexer")) {
				addKeyword(TokenNames.SIP.toUpperCase(), TokenTypes.SIP);
			} else if (lexerName.equals("request_lineLexer")) {
				addKeyword(TokenNames.SIP.toUpperCase(), TokenTypes.SIP);
			} else if (lexerName.equals("sip_urlLexer")) {
				addKeyword(TokenNames.TEL.toUpperCase(), TokenTypes.TEL);
				addKeyword(TokenNames.SIP.toUpperCase(), TokenTypes.SIP);
			}
		}
	      }
	}
}
/*
 * $Log: not supported by cvs2svn $
 * Revision 1.5  2005/03/22 18:34:56  mranga
 * Submitted by:  Brian Rosen
 * Reviewed by:   mranga
 *
 * Applied fix suggested by Brian Rosen
 *
 * Revision 1.4  2004/01/22 13:26:31  sverker
 * Issue number:
 * Obtained from:
 * Submitted by:  sverker
 * Reviewed by:   mranga
 *
 * Major reformat of code to conform with style guide. Resolved compiler and javadoc warnings. Added CVS tags.
 *
 * CVS: ----------------------------------------------------------------------
 * CVS: Issue number:
 * CVS:   If this change addresses one or more issues,
 * CVS:   then enter the issue number(s) here.
 * CVS: Obtained from:
 * CVS:   If this change has been taken from another system,
 * CVS:   then name the system in this line, otherwise delete it.
 * CVS: Submitted by:
 * CVS:   If this code has been contributed to the project by someone else; i.e.,
 * CVS:   they sent us a patch or a set of diffs, then include their name/email
 * CVS:   address here. If this is your work then delete this line.
 * CVS: Reviewed by:
 * CVS:   If we are doing pre-commit code reviews and someone else has
 * CVS:   reviewed your changes, include their name(s) here.
 * CVS:   If you have not had it reviewed then delete this line.
 *
 */
