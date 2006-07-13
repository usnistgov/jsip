/*
* Conditions Of Use 
* 
* This software was developed by employees of the National Institute of
* Standards and Technology (NIST), an agency of the Federal Government.
* Pursuant to title 15 Untied States Code Section 105, works of NIST
* employees are not subject to copyright protection in the United States
* and are considered to be in the public domain.  As a result, a formal
* license is not needed to use the software.
* 
* This software is provided by NIST as a service and is expressly
* provided "AS IS."  NIST MAKES NO WARRANTY OF ANY KIND, EXPRESS, IMPLIED
* OR STATUTORY, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTY OF
* MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, NON-INFRINGEMENT
* AND DATA ACCURACY.  NIST does not warrant or make any representations
* regarding the use of the software or the results thereof, including but
* not limited to the correctness, accuracy, reliability or usefulness of
* the software.
* 
* Permission to use this software is contingent upon your acceptance
* of the terms of this agreement
*  
* .
* 
*/
package gov.nist.javax.sip.parser;

import gov.nist.core.*;

/**
 * @version 1.2 $Revision: 1.6 $ $Date: 2006-07-13 09:01:55 $
 */
public interface TokenTypes {

	public static final int START = LexerCore.START;
	// Everything under this is reserved
	public static final int END = LexerCore.END;
	// End markder.

	public static final int SIP = START + 3;
	public static final int REGISTER = START + 4;
	public static final int INVITE = START + 5;
	public static final int ACK = START + 6;
	public static final int BYE = START + 7;
	public static final int OPTIONS = START + 8;
	public static final int CANCEL = START + 9;
	public static final int ERROR_INFO = START + 10;
	public static final int IN_REPLY_TO = START + 11;
	public static final int MIME_VERSION = START + 12;
	public static final int ALERT_INFO = START + 13;
	public static final int FROM = START + 14;
	public static final int TO = START + 15;
	public static final int VIA = START + 16;
	public static final int USER_AGENT = START + 17;
	public static final int SERVER = START + 18;
	public static final int ACCEPT_ENCODING = START + 19;
	public static final int ACCEPT = START + 20;
	public static final int ALLOW = START + 21;
	public static final int ROUTE = START + 22;
	public static final int AUTHORIZATION = START + 23;
	public static final int PROXY_AUTHORIZATION = START + 24;
	public static final int RETRY_AFTER = START + 25;
	public static final int PROXY_REQUIRE = START + 26;
	public static final int CONTENT_LANGUAGE = START + 27;
	public static final int UNSUPPORTED = START + 28;
	public static final int SUPPORTED = START + 20;
	public static final int WARNING = START + 30;
	public static final int MAX_FORWARDS = START + 31;
	public static final int DATE = START + 32;
	public static final int PRIORITY = START + 33;
	public static final int PROXY_AUTHENTICATE = START + 34;
	public static final int CONTENT_ENCODING = START + 35;
	public static final int CONTENT_LENGTH = START + 36;
	public static final int SUBJECT = START + 37;
	public static final int CONTENT_TYPE = START + 38;
	public static final int CONTACT = START + 39;
	public static final int CALL_ID = START + 40;
	public static final int REQUIRE = START + 41;
	public static final int EXPIRES = START + 42;
	public static final int ENCRYPTION = START + 43;
	public static final int RECORD_ROUTE = START + 44;
	public static final int ORGANIZATION = START + 45;
	public static final int CSEQ = START + 46;
	public static final int ACCEPT_LANGUAGE = START + 47;
	public static final int WWW_AUTHENTICATE = START + 48;
	public static final int RESPONSE_KEY = START + 49;
	public static final int HIDE = START + 50;
	public static final int CALL_INFO = START + 51;
	public static final int CONTENT_DISPOSITION = START + 52;
	public static final int SUBSCRIBE = START + 53;
	public static final int NOTIFY = START + 54;
	public static final int TIMESTAMP = START + 55;
	public static final int SUBSCRIPTION_STATE = START + 56;
	public static final int TEL = START + 57;
	public static final int REPLY_TO = START + 58;
	public static final int REASON = START + 59;
	public static final int RSEQ = START + 60;
	public static final int RACK = START + 61;
	public static final int MIN_EXPIRES = START + 62;
	public static final int EVENT = START + 63;
	public static final int AUTHENTICATION_INFO = START + 64;
	public static final int ALLOW_EVENTS = START + 65;
	public static final int REFER_TO = START + 66;
	
	// JvB: added to support RFC3903
	public static final int PUBLISH = START + 67;
	public static final int SIP_ETAG = START + 68;
	public static final int SIP_IF_MATCH = START + 69;	
	
	public static final int ALPHA = LexerCore.ALPHA;
	public static final int DIGIT = LexerCore.DIGIT;
	public static final int ID = LexerCore.ID;
	public static final int WHITESPACE = LexerCore.WHITESPACE;
	public static final int BACKSLASH = LexerCore.BACKSLASH;
	public static final int QUOTE = LexerCore.QUOTE;
	public static final int AT = LexerCore.AT;
	public static final int SP = LexerCore.SP;
	public static final int HT = LexerCore.HT;
	public static final int COLON = LexerCore.COLON;
	public static final int STAR = LexerCore.STAR;
	public static final int DOLLAR = LexerCore.DOLLAR;
	public static final int PLUS = LexerCore.PLUS;
	public static final int POUND = LexerCore.POUND;
	public static final int MINUS = LexerCore.MINUS;
	public static final int DOUBLEQUOTE = LexerCore.DOUBLEQUOTE;
	public static final int TILDE = LexerCore.TILDE;
	public static final int BACK_QUOTE = LexerCore.BACK_QUOTE;
	public static final int NULL = LexerCore.NULL;
	public static final int EQUALS = (int) '=';
	public static final int SEMICOLON = (int) ';';
	public static final int SLASH = (int) '/';
	public static final int L_SQUARE_BRACKET = (int) '[';
	public static final int R_SQUARE_BRACKET = (int) ']';
	public static final int R_CURLY = (int) '}';
	public static final int L_CURLY = (int) '{';
	public static final int HAT = (int) '^';
	public static final int BAR = (int) '|';
	public static final int DOT = (int) '.';
	public static final int EXCLAMATION = (int) '!';
	public static final int LPAREN = (int) '(';
	public static final int RPAREN = (int) ')';
	public static final int GREATER_THAN = (int) '>';
	public static final int LESS_THAN = (int) '<';
	public static final int PERCENT = (int) '%';
	public static final int QUESTION = (int) '?';
	public static final int AND = (int) '&';
	public static final int UNDERSCORE = (int) '_';

}
/*
 * $Log: not supported by cvs2svn $
 * Revision 1.4  2006/06/19 06:47:27  mranga
 * javadoc fixups
 *
 * Revision 1.3  2006/06/16 15:26:28  mranga
 * Added NIST disclaimer to all public domain files. Clean up some javadoc. Fixed a leak
 *
 * Revision 1.2  2005/10/27 20:49:00  jeroen
 * added support for RFC3903 PUBLISH
 *
 * Revision 1.1.1.1  2005/10/04 17:12:36  mranga
 *
 * Import
 *
 *
 * Revision 1.4  2004/01/22 13:26:32  sverker
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
