/*******************************************************************************
 * Product of NIST/ITL Advanced Networking Technologies Division (ANTD)        *
 ******************************************************************************/
package gov.nist.javax.sip.parser;
import java.util.Vector;
import java.util.NoSuchElementException;
import java.io.*;
import gov.nist.javax.sip.*;
import gov.nist.javax.sip.header.*;
import gov.nist.javax.sip.message.*;
import gov.nist.javax.sip.address.*;
import java.text.ParseException;
import gov.nist.core.*;

/**
 * Parse SIP message and parts of SIP messages such as URI's etc
 * from memory and return a structure.
 * Intended use:  UDP message processing.
 * This class is used when you have an entire SIP message or SIPHeader
 * or SIP URL in memory and you want to generate a parsed structure from
 * it. For SIP messages, the payload can be binary or String.
 * If you have a binary payload,
 * use parseSIPMessage(byte[]) else use parseSIPMessage(String)
 * The payload is accessible from the parsed message using the getContent and
 * getContentBytes methods provided by the SIPMessage class. If SDP parsing
 * is enabled using the parseContent method, then the SDP body is also parsed
 * and can be accessed from the message using the getSDPAnnounce method.
 * Currently only eager parsing of the message is supported (i.e. the
 * entire message is parsed in one feld swoop).
 *
 *
 * @version JAIN-SIP-1.1 $Revision: 1.8 $ $Date: 2004-02-18 14:33:02 $
 *
 * @author M. Ranganathan <mranga@nist.gov>  <br/>
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 */
public class StringMsgParser {

	protected boolean readBody;

	private String rawMessage;
	// Unprocessed message  (for error reporting)
	private String rawMessage1;
	// Unprocessed message  (for error reporting)
	private String currentMessage;
	// the message being parsed. (for error reporting)
	private ParseExceptionListener parseExceptionListener;

	private Vector messageHeaders; // Message headers

	private int bufferPointer;

	private boolean bodyIsString;

	private byte[] currentMessageBytes;

	protected int contentLength;

	private boolean debugFlag;

	private int currentLine;

	private String currentHeader;

	/**
	 * @since v0.9
	 */
	public StringMsgParser() {
		super();
		messageHeaders = new Vector(10, 10);
		bufferPointer = 0;
		currentLine = 0;
		readBody = true;
	}

	/**
	 * Constructor (given a parse exception handler).
	 * @since 1.0
	 * @param exhandler is the parse exception listener for the message parser.
	 */
	public StringMsgParser(ParseExceptionListener exhandler) {
		this();
		parseExceptionListener = exhandler;
	}

	/**
	 * Get the message body.
	 */
	protected String getMessageBody() {

		if (this.contentLength == 0) {
			return null;
		} else {
			int endIndex = bufferPointer + this.contentLength;
			String body;
			// guard against bad specifications.
			if (endIndex > currentMessage.length()) {
				endIndex = currentMessage.length();
				body = currentMessage.substring
					(bufferPointer, endIndex);
				bufferPointer = endIndex;
			} else {
				body = currentMessage.substring
					(bufferPointer, endIndex);
				bufferPointer = endIndex + 1;
			}
			this.contentLength = 0;
			return body;
		}

	}

	/**
	 * Get the message body as a byte array.
	 */
	protected byte[] getBodyAsBytes() {
		if (this.contentLength == 0) {
			return null;
		} else {
			int endIndex = bufferPointer + this.contentLength;
			// guard against bad specifications.
			if (endIndex > currentMessageBytes.length) {
				endIndex = currentMessageBytes.length;
			}
			byte[] body = new byte[endIndex - bufferPointer];
			System.arraycopy(
				currentMessageBytes,
				bufferPointer,
				body,
				0,
				body.length);
			//
			//            for (int i = bufferPointer, k = 0; i < endIndex; i++,k++) {
			//                body[k] = currentMessageBytes[i];
			//            }

			bufferPointer = endIndex;
			this.contentLength = 0;
			return body;
		}

	}

	/**
	 * Return the contents till the end of the buffer (this is useful when
	 * you encounter an error.
	 */
	protected String readToEnd() {
		String body = currentMessage.substring(bufferPointer);
		bufferPointer += body.length();
		return body;
	}

	/**
	 * Return tbe bytes to the end of the message.
	 * This is invoked when the parser is invoked with an array of bytes
	 * rather than with a string.
	 */
	protected byte[] readBytesToEnd() {
		byte[] body = new byte[currentMessageBytes.length - bufferPointer];
		int endIndex = currentMessageBytes.length;
		for (int i = bufferPointer, k = 0; i < endIndex; i++, k++) {
			body[k] = currentMessageBytes[i];
		}
		bufferPointer = endIndex;
		this.contentLength = 0;
		return body;
	}

	/**
	 * add a handler for header parsing errors.
	 * @param  pexhandler is a class
	 *  	that implements the ParseExceptionListener interface.
	 */
	public void setParseExceptionListener(ParseExceptionListener pexhandler) {
		parseExceptionListener = pexhandler;
	}

	/**
	 * Return true if the body is encoded as a string.
	 * If the parseSIPMessage(String) method is invoked then the body
	 * is assumed to be a string.
	 */
	protected boolean isBodyString() {
		return bodyIsString;
	}

	/**
	 * Parse a buffer containing a single SIP Message where the body
	 * is an array of un-interpreted bytes. This is intended for parsing
	 * the message from a memory buffer when the buffer.
	 * Incorporates a bug fix for a bug that was noted by Will Sullin of
	 * Callcast
	 * @param msgBuffer a byte buffer containing the messages to be parsed.
	 *   This can consist of multiple SIP Messages concatenated together.
	 * @return a SIPMessage[] structure (request or response)
	 * 			containing the parsed SIP message.
	 * @exception SIPIllegalMessageException is thrown when an
	 * 			illegal message has been encountered (and
	 *			the rest of the buffer is discarded).
	 * @see ParseExceptionListener
	 */
	public SIPMessage parseSIPMessage(byte[] msgBuffer) throws ParseException {
		bufferPointer = 0;
		bodyIsString = false;
		Vector retval = new Vector();
		currentMessageBytes = msgBuffer;
		int s;
		// Squeeze out leading CRLF
		// Squeeze out the leading nulls (otherwise the parser will crash)
		// Bug noted by Will Sullin of Callcast
		for (s = bufferPointer; s < msgBuffer.length; s++) {
			if ((char) msgBuffer[s] != '\r'
				&& (char) msgBuffer[s] != '\n'
				&& (char) msgBuffer[s] != '\0')
				break;
		}

		if (s == msgBuffer.length)
			return null;

		// Find the end of the SIP message.
		int f;
		for (f = s; f < msgBuffer.length - 4; f++) {
			if ((char) msgBuffer[f] == '\r'
				&& (char) msgBuffer[f + 1] == '\n'
				&& (char) msgBuffer[f + 2] == '\r'
				&& (char) msgBuffer[f + 3] == '\n') {
				break;
			}
		}
		if (f < msgBuffer.length)
			f += 4;
		else {
			// Could not find CRLFCRLF end of message so look for LFLF
			// Bug noticed here by Bruno Konik
			for (f = s; f < msgBuffer.length - 2; f++) {
				if ((char) msgBuffer[f] == '\n' 
					&& (char) msgBuffer[f+1] == '\n')
					break;
			}
			if (f < msgBuffer.length)
				f += 2;
			else
				throw new ParseException("Message not terminated", 0);
		}

		// Encode the body as a UTF-8 string.
		String messageString = null;
		try {
			messageString = new String(msgBuffer, s, f - s, "UTF-8");
		} catch (UnsupportedEncodingException ex) {
			throw new ParseException("Bad message encoding!", 0);
		}
		bufferPointer = f;
		StringBuffer message = new StringBuffer(messageString);
		int length = message.length();
		// Get rid of CR to make it uniform for the parser.
		for (int k = 0; k < length; k++) {
			if (message.charAt(k) == '\r') {
				message.deleteCharAt(k);
				length--;
			}
		}

		if (Parser.debug) {
			for (int k = 0; k < length; k++) {
				rawMessage1 = rawMessage1 + "[" + message.charAt(k) + "]";
			}
		}

		// The following can be written more efficiently in a single pass
		// but it is somewhat tricky.
		java.util.StringTokenizer tokenizer =
			new java.util.StringTokenizer(message.toString(), "\n", true);
		StringBuffer cooked_message = new StringBuffer();
		try {
			while (tokenizer.hasMoreElements()) {
				String nexttok = tokenizer.nextToken();
				// Ignore blank lines with leading spaces or tabs.
				if (nexttok.trim().equals(""))
					cooked_message.append("\n");
				else
					cooked_message.append(nexttok);
			}
		} catch (NoSuchElementException ex) {
		}

		String message1 = cooked_message.toString();
		length = message1.indexOf("\n\n") + 2;

		// Handle continuations - look for a space or a tab at the start
		// of the line and append it to the previous line.

		for (int k = 0; k < length - 1;) {
			if (cooked_message.charAt(k) == '\n') {
				if (cooked_message.charAt(k + 1) == '\t'
					|| cooked_message.charAt(k + 1) == ' ') {
					cooked_message.deleteCharAt(k);
					cooked_message.deleteCharAt(k);
					length--;
					length--;
					if (k == length)
						break;
					continue;
				}

				if (cooked_message.charAt(k + 1) == '\n') {
					cooked_message.insert(k, '\n');
					length++;
					k++;
				}
			}
			k++;
		}
		cooked_message.append("\n\n");

		// Separate the string out into substrings for
		// error reporting.
		currentMessage = cooked_message.toString();
		SIPMessage sipmsg = this.parseMessage(currentMessage);
		if (readBody
			&& sipmsg.getContentLength() != null
			&& sipmsg.getContentLength().getContentLength() != 0) {
			this.contentLength = sipmsg.getContentLength().getContentLength();
			byte body[] = getBodyAsBytes();
			sipmsg.setMessageContent(body);
		}
		// System.out.println("Parsed = " + sipmsg);
		return sipmsg;

	}

	/**
	 * Parse a buffer containing one or more SIP Messages  
	 * and return an array of SIPMessage parsed structures. 
	 * @param sipMessages a String containing the messages to be parsed.
	 *   This can consist of multiple SIP Messages concatenated together.
	 * @return a SIPMessage structure (request or response)
	 * 			containing the parsed SIP message.
	 * @exception SIPIllegalMessageException is thrown when an
	 * 			illegal message has been encountered (and
	 *			the rest of the buffer is discarded).
	 * @see ParseExceptionListener
	 */
	public SIPMessage parseSIPMessage(String sipMessages)
		throws ParseException {
		// Handle line folding and evil DOS CR-LF sequences
		rawMessage = sipMessages;
		Vector retval = new Vector();
		String pmessage = sipMessages.trim();
		bodyIsString = true;

		this.contentLength = 0;
		if (pmessage.trim().equals(""))
			return null;

		pmessage += "\n\n";
		StringBuffer message = new StringBuffer(pmessage);
		// squeeze out the leading crlf sequences.
		while (message.charAt(0) == '\r' || message.charAt(0) == '\n') {
			bufferPointer++;
			message.deleteCharAt(0);
		}

		// squeeze out the crlf sequences and make them uniformly CR
		String message1 = message.toString();
		int length;
		length = message1.indexOf("\r\n\r\n");
		if (length > 0)
			length += 4;
		if (length == -1) {
			length = message1.indexOf("\n\n");
			if (length == -1)
				throw new ParseException("no trailing crlf", 0);
		} else
			length += 2;

		// Get rid of CR to make it uniform.
		for (int k = 0; k < length; k++) {
			if (message.charAt(k) == '\r') {
				message.deleteCharAt(k);
				length--;
			}
		}

		if (debugFlag) {
			for (int k = 0; k < length; k++) {
				rawMessage1 = rawMessage1 + "[" + message.charAt(k) + "]";
			}
		}

		// The following can be written more efficiently in a single pass
		// but it is somewhat tricky.
		java.util.StringTokenizer tokenizer =
			new java.util.StringTokenizer(message.toString(), "\n", true);
		StringBuffer cooked_message = new StringBuffer();
		try {
			while (tokenizer.hasMoreElements()) {
				String nexttok = tokenizer.nextToken();
				// Ignore blank lines with leading spaces or tabs.
				if (nexttok.trim().equals(""))
					cooked_message.append("\n");
				else
					cooked_message.append(nexttok);
			}
		} catch (NoSuchElementException ex) {
		}

		message1 = cooked_message.toString();
		length = message1.indexOf("\n\n") + 2;

		// Handle continuations - look for a space or a tab at the start
		// of the line and append it to the previous line.

		for (int k = 0; k < length - 1;) {
			if (cooked_message.charAt(k) == '\n') {
				if (cooked_message.charAt(k + 1) == '\t'
					|| cooked_message.charAt(k + 1) == ' ') {
					cooked_message.deleteCharAt(k);
					cooked_message.deleteCharAt(k);
					length--;
					length--;
					if (k == length)
						break;
					continue;
				}
				if (cooked_message.charAt(k + 1) == '\n') {
					cooked_message.insert(k, '\n');
					length++;
					k++;
				}
			}
			k++;
		}
		cooked_message.append("\n\n");

		// Separate the string out into substrings for
		// error reporting.

		currentMessage = cooked_message.toString();
		if (Parser.debug)
			Debug.println(currentMessage);
		bufferPointer = currentMessage.indexOf("\n\n") + 3;
		SIPMessage sipmsg = this.parseMessage(currentMessage);
		if (readBody
			&& sipmsg.getContentLength() != null
			&& sipmsg.getContentLength().getContentLength() != 0) {
			this.contentLength = sipmsg.getContentLength().getContentLength();
			String body = this.getMessageBody();
			sipmsg.setMessageContent(body);
		}
		return sipmsg;

	}

	/**
	 * This is called repeatedly by parseSIPMessage to parse
	 * the contents of a message buffer. This assumes the message
	 * already has continuations etc. taken care of.
	 * prior to its being called.
	 */
	private SIPMessage parseMessage(String currentMessage)
		throws ParseException {
		// position line counter at the end of the
		// sip messages.
		// System.out.println("parsing " + currentMessage);

		int sip_message_size = 0; // # of lines in the sip message
		SIPMessage sipmsg = null;
		java.util.StringTokenizer tokenizer =
			new java.util.StringTokenizer(currentMessage, "\n", true);
		messageHeaders = new Vector(); // A list of headers for error reporting
		try {
			while (tokenizer.hasMoreElements()) {
				String nexttok = tokenizer.nextToken();
				if (nexttok.equals("\n")) {
					String nextnexttok = tokenizer.nextToken();
					if (nextnexttok.equals("\n")) {
						break;
					} else
						messageHeaders.add(nextnexttok);
				} else
					messageHeaders.add(nexttok);
				sip_message_size++;
			}
		} catch (NoSuchElementException ex) {
		}
		currentLine = 0;
		currentHeader = (String) messageHeaders.elementAt(currentLine);
		String firstLine = currentHeader;
		// System.out.println("first Line " + firstLine);

		if (!firstLine.startsWith(SIPConstants.SIP_VERSION_STRING)) {
			sipmsg = new SIPRequest();
			try {
				RequestLine rl =
					new RequestLineParser(firstLine + "\n").parse();
				((SIPRequest) sipmsg).setRequestLine(rl);
			} catch (ParseException ex) {
				if (this.parseExceptionListener != null)
					this.parseExceptionListener.handleException(
						ex,
						sipmsg,
						RequestLine.class,
						firstLine,
						currentMessage);
				else
					throw ex;

			}
		} else {
			sipmsg = new SIPResponse();
			try {
				StatusLine sl = new StatusLineParser(firstLine + "\n").parse();
				((SIPResponse) sipmsg).setStatusLine(sl);
			} catch (ParseException ex) {
				if (this.parseExceptionListener != null) {
					this.parseExceptionListener.handleException(
						ex,
						sipmsg,
						StatusLine.class,
						firstLine,
						currentMessage);
				} else
					throw ex;

			}
		}

		for (int i = 1; i < messageHeaders.size(); i++) {
			String hdrstring = (String) messageHeaders.elementAt(i);
			if (hdrstring == null || hdrstring.trim().equals(""))
				continue;
			HeaderParser hdrParser = null;
			try {
				hdrParser = ParserFactory.createParser(hdrstring + "\n");
			} catch (ParseException ex) {
				this.parseExceptionListener.handleException(
					ex,
					sipmsg,
					null,
					hdrstring,
					rawMessage);
				continue;
			}
			try {
				SIPHeader sipHeader = hdrParser.parse();
				sipmsg.attachHeader(sipHeader, false);
			} catch (ParseException ex) {
				if (this.parseExceptionListener != null) {
					String hdrName = Lexer.getHeaderName(hdrstring);
					Class hdrClass = NameMap.getClassFromName(hdrName);
					try {
						if (hdrClass == null) {
							hdrClass =
								Class.forName(
									PackageNames.SIPHEADERS_PACKAGE
										+ ".ExtensionHeaderImpl");
						}
						this.parseExceptionListener.handleException(
							ex,
							sipmsg,
							hdrClass,
							hdrstring,
							rawMessage);
					} catch (ClassNotFoundException ex1) {
						InternalErrorHandler.handleException(ex1);
					}
				}
			}
		}
		return sipmsg;
	}

	/**
	 * Parse an address (nameaddr or address spec)  and return and address
	 * structure.
	 * @param address is a String containing the address to be parsed.
	 * @return a parsed address structure.
	 * @since v1.0
	 * @exception  ParseException when the address is badly formatted.
	 */
	public AddressImpl parseAddress(String address) throws ParseException {
		AddressParser addressParser = new AddressParser(address);
		return addressParser.address();
	}

	/**
	 * Parse a host:port and return a parsed structure.
	 * @param hostport is a String containing the host:port to be parsed
	 * @return a parsed address structure.
	 * @since v1.0
	 * @exception throws a ParseException when the address is badly formatted.
	 */
	public HostPort parseHostPort(String hostport) throws ParseException {
		Lexer lexer = new Lexer("charLexer", hostport);
		return new HostNameParser(lexer).hostPort();

	}

	/**
	 * Parse a host name and return a parsed structure.
	 * @param host is a String containing the host name to be parsed
	 * @return a parsed address structure.
	 * @since v1.0
	 * @exception throws a ParseException when the hostname is badly formatted.
	 */
	public Host parseHost(String host) throws ParseException {
		Lexer lexer = new Lexer("charLexer", host);
		return new HostNameParser(lexer).host();

	}

	/**
	 * Parse a telephone number return a parsed structure.
	 * @param telephone_number is a String containing 
	 * the telephone # to be parsed
	 * @return a parsed address structure.
	 * @since v1.0
	 * @exception throws a ParseException when the address is badly formatted.
	 */
	public TelephoneNumber parseTelephoneNumber(String telephone_number)
		throws ParseException {
		// Bug fix contributed by Will Scullin
		return new URLParser(telephone_number).parseTelephoneNumber();

	}

	/**
	 * Parse a  SIP url from a string and return a URI structure for it.
	 * @param url a String containing the URI structure to be parsed.
	 * @return A parsed URI structure
	 * @exception ParseException  if there was an error parsing the message.
	 */

	public SipUri parseSIPUrl(String url) throws ParseException {
		try {
			return (SipUri) new URLParser(url).parse();
		} catch (ClassCastException ex) {
			throw new ParseException(url + " Not a SIP URL ", 0);
		}
	}

	/**
	 * Parse a  uri from a string and return a URI structure for it.
	 * @param url a String containing the URI structure to be parsed.
	 * @return A parsed URI structure
	 * @exception ParseException  if there was an error parsing the message.
	 */

	public GenericURI parseUrl(String url) throws ParseException {
		return new URLParser(url).parse();
	}

	/**
	 * Parse an individual SIP message header from a string.
	 * @param header String containing the SIP header.
	 * @return a SIPHeader structure.
	 * @exception ParseException  if there was an error parsing the message.
	 */
	public SIPHeader parseSIPHeader(String header) throws ParseException {
		header += "\n\n";
		// Handle line folding.
		String nmessage = "";
		int counter = 0;
		// eat leading spaces and carriage returns (necessary??)
		int i = 0;
		while (header.charAt(i) == '\n'
			|| header.charAt(i) == '\t'
			|| header.charAt(i) == ' ')
			i++;
		for (; i < header.length(); i++) {
			if (i < header.length() - 1
				&& (header.charAt(i) == '\n'
					&& (header.charAt(i + 1) == '\t'
						|| header.charAt(i + 1) == ' '))) {
				nmessage += ' ';
				i++;
			} else {
				nmessage += header.charAt(i);
			}
		}

		nmessage += "\n";

		HeaderParser hp = ParserFactory.createParser(nmessage);
		if (hp == null)
			throw new ParseException("could not create parser", 0);
		return hp.parse();
	}

	/**
	 * Parse the SIP Request Line
	 * @param  requestLine a String  containing the request line to be parsed.
	 * @return  a RequestLine structure that has the parsed RequestLine
	 * @exception ParseException  if there was an error parsing the requestLine.
	 */

	public RequestLine parseSIPRequestLine(String requestLine)
		throws ParseException {
		requestLine += "\n";
		return new RequestLineParser(requestLine).parse();
	}

	/**
	 * Parse the SIP Response message status line
	 * @param statusLine a String containing the Status line to be parsed.
	 * @return StatusLine class corresponding to message
	 * @exception ParseException  if there was an error parsing
	 * @see StatusLine
	 */

	public StatusLine parseSIPStatusLine(String statusLine)
		throws ParseException {
		statusLine += "\n";
		return new StatusLineParser(statusLine).parse();
	}

	/**
	 * Get the current header.
	 */
	public String getCurrentHeader() {
		return currentHeader;
	}

	/**
	 * Get the current line number.
	 */
	public int getCurrentLineNumber() {
		return currentLine;
	}

/**
* Test code.
	public static void main(String[] args) throws ParseException {
		String messages[] =
			{
				"SIP/2.0 180 Ringing\r\n"
					+ "Via: SIP/2.0/UDP 172.18.1.29:5060;branch=z9hG4bK43fc10fb4446d55fc5c8f969607991f4\r\n"
					+ "To: \"0440\" <sip:0440@212.209.220.131>;tag=2600\r\n"
					+ "From: \"Andreas\" <sip:andreas@e-horizon.se>;tag=8524\r\n"
					+ "Call-ID: f51a1851c5f570606140f14c8eb64fd3@172.18.1.29\r\n"
					+ "CSeq: 1 INVITE\r\n"
					+ "Max-Forwards: 70\r\n"
					+ "Record-Route: <sip:212.209.220.131:5060>\r\n"
					+ "Content-Length: 0\r\n\r\n",
				"REGISTER sip:nist.gov SIP/2.0\r\n"
					+ "Via: SIP/2.0/UDP 129.6.55.182:14826\r\n"
					+ "Max-Forwards: 70\r\n"
					+ "From: <sip:mranga@nist.gov>;tag=6fcd5c7ace8b4a45acf0f0cd539b168b;epid=0d4c418ddf\r\n"
					+ "To: <sip:mranga@nist.gov>\r\n"
					+ "Call-ID: c5679907eb954a8da9f9dceb282d7230@129.6.55.182\r\n"
					+ "CSeq: 1 REGISTER\r\n"
					+ "Contact: <sip:129.6.55.182:14826>;methods=\"INVITE, MESSAGE, INFO, SUBSCRIBE, OPTIONS, BYE, CANCEL, NOTIFY, ACK, REFER\"\r\n"
					+ "User-Agent: RTC/(Microsoft RTC)\r\n"
					+ "Event:  registration\r\n"
					+ "Allow-Events: presence\r\n"
					+ "Content-Length: 0\r\n\r\n"
					+ "INVITE sip:littleguy@there.com:5060 SIP/2.0\r\n"
					+ "Via: SIP/2.0/UDP 65.243.118.100:5050\r\n"
					+ "From: M. Ranganathan  <sip:M.Ranganathan@sipbakeoff.com>;tag=1234\r\n"
					+ "To: \"littleguy@there.com\" <sip:littleguy@there.com:5060> \r\n"
					+ "Call-ID: Q2AboBsaGn9!?x6@sipbakeoff.com \r\n"
					+ "CSeq: 1 INVITE \r\n"
					+ "Content-Length: 247\r\n\r\n"
					+ "v=0\r\n"
					+ "o=4855 13760799956958020 13760799956958020 IN IP4  129.6.55.78\r\n"
					+ "s=mysession session\r\n"
					+ "p=+46 8 52018010\r\n"
					+ "c=IN IP4  129.6.55.78\r\n"
					+ "t=0 0\r\n"
					+ "m=audio 6022 RTP/AVP 0 4 18\r\n"
					+ "a=rtpmap:0 PCMU/8000\r\n"
					+ "a=rtpmap:4 G723/8000\r\n"
					+ "a=rtpmap:18 G729A/8000\r\n"
					+ "a=ptime:20\r\n" };

		for (int i = 0; i < messages.length; i++) {
			StringMsgParser smp = new StringMsgParser();
			SIPMessage sipMessage = smp.parseSIPMessage(messages[i]);
			System.out.println("encoded " + sipMessage.toString());
			System.out.println("dialog id = " + sipMessage.getDialogId(false));
		}
	}
**/
}
/*
 * $Log: not supported by cvs2svn $
 * Revision 1.7  2004/02/13 19:20:09  mranga
 * Reviewed by:   mranga
 * minor fix for error callback.
 *
 * Revision 1.6  2004/01/22 13:26:32  sverker
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
