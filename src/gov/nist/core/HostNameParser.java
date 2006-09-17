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
/*
 *
 * IPv6 Support added by Emil Ivov (emil_ivov@yahoo.com)<br/>
 * Network Research Team (http://www-r2.u-strasbg.fr))<br/>
 * Louis Pasteur University - Strasbourg - France<br/>
 *
 *Bug fixes for corner cases were contributed by Thomas Froment.
 */
package gov.nist.core;

import java.text.ParseException;

/**
 * Parser for host names.
 *
 *@version 1.2
 *
 *@author M. Ranganathan  
 *
 *
 
 *
 */

public class HostNameParser extends ParserCore {

	public HostNameParser(String hname) {
		this.lexer = new LexerCore("charLexer", hname);
	}

	/** The lexer is initialized with the buffer.
	 */
	public HostNameParser(LexerCore lexer) {
		this.lexer = lexer;
		lexer.selectLexer("charLexer");
	}

	protected String domainLabel() throws ParseException {
		StringBuffer retval = new StringBuffer();
		if (debug)
			dbg_enter("domainLabel");
		try {
			while (lexer.hasMoreChars()) {
				char la = lexer.lookAhead(0);
				if (LexerCore.isAlpha(la)) {
					lexer.consume(1);
					retval.append(la);
				} else if (LexerCore.isDigit(la)) {
					lexer.consume(1);
					retval.append(la);
				} else if (la == '-') {
					lexer.consume(1);
					retval.append(la);
				} else
					break;
			}
			//Debug.println("returning " + retval.toString());
			return retval.toString();
		} finally {
			if (debug)
				dbg_leave("domainLabel");
		}
	}

	protected String ipv6Reference() throws ParseException {
		StringBuffer retval = new StringBuffer();
		if (debug)
			dbg_enter("ipv6Reference");
		try {
			while (lexer.hasMoreChars()) {
				char la = lexer.lookAhead(0);
				if (LexerCore.isHexDigit(la)) {
					lexer.consume(1);
					retval.append(la);
				} else if (la == '.' || la == ':' || la == '[') {
					lexer.consume(1);
					retval.append(la);
				} else if (la == ']') {
					lexer.consume(1);
					retval.append(la);
					return retval.toString();
				} else
					break;
			}

			throw new ParseException(
				lexer.getBuffer() + ": Illegal Host name ",
				lexer.getPtr());
		} finally {
			if (debug)
				dbg_leave("ipv6Reference");
		}
	}

	public Host host() throws ParseException {
		if (debug)
			dbg_enter("host");
		try {
			StringBuffer hname = new StringBuffer();

			//IPv6 referene
			if (lexer.lookAhead(0) == '[') {
				hname.append(ipv6Reference());
			}
			//IPv4 address or hostname
			else {
				String nextTok = domainLabel();
				hname.append(nextTok);
				// Bug reported by Stuart Woodsford (used to barf on
				// more than 4 components to the name).
				while (lexer.hasMoreChars()) {
					// Reached the end of the buffer.
					if (lexer.lookAhead(0) == '.') {
						lexer.consume(1);
						nextTok = domainLabel();
						hname.append(".");
						hname.append(nextTok);
					} else
						break;
				}
			}

			String hostname = hname.toString();
			if (hostname.equals(""))
				throw new ParseException(
					lexer.getBuffer() + ": Missing host name",
					lexer.getPtr());
			else
				return new Host(hostname);
		} finally {
			if (debug)
				dbg_leave("host");
		}
	}

	/**
	 * Parses a host:port string
	 * 
	 * @param allowWS - whether whitespace is allowed around ':', only true for Via headers
	 * @return
	 * @throws ParseException
	 */
	public HostPort hostPort( boolean allowWS ) throws ParseException {
		if (debug)
			dbg_enter("hostPort");
		try {
			Host host = this.host();
			HostPort hp = new HostPort();
			hp.setHost(host);
			// Has a port?
			if (allowWS) lexer.SPorHT(); // white space before ":port" should be accepted
			if (lexer.hasMoreChars()) {				
				switch (lexer.lookAhead(0))
				{ 
				case ':':
					lexer.consume(1);
					if (allowWS) lexer.SPorHT(); // white space before port number should be accepted
					try {
						String port = lexer.number();
						hp.setPort(Integer.parseInt(port));
					} catch (NumberFormatException nfe) {
						throw new ParseException(
							lexer.getBuffer() + " :Error parsing port ",
							lexer.getPtr());
					}
					break;
				
				case ';':	// OK, can appear in URIs (parameters)
				case '?':	// same, header parameters
				case '>':	// OK, can appear in headers
				case ' ':	// OK, allow whitespace
				case '\t':
				case '\r':
				case '\n':
					break;
					
				default:
					if (!allowWS) {
						throw new ParseException( lexer.getBuffer() + 
								" Illegal character in hostname:" + lexer.lookAhead(0), 
								lexer.getPtr() );
					}
				}
			}
			return hp;
		} finally {
			if (debug)
				dbg_leave("hostPort");
		}
	}

	public static void main(String args[]) throws ParseException {
		String hostNames[] =
			{
				"foo.bar.com:1234",
				"proxima.chaplin.bt.co.uk",
				"129.6.55.181:2345",
				":1234",
				"foo.bar.com:         1234",
				"foo.bar.com     :      1234   ",
				"MIK_S:1234"
			};
			
		for (int i = 0; i < hostNames.length; i++) {
			try {
				HostNameParser hnp = new HostNameParser(hostNames[i]);
				HostPort hp = hnp.hostPort(true);
				System.out.println("["+hp.encode()+"]");
			} catch (ParseException ex) {
				System.out.println("exception text = " + ex.getMessage());
			}
		}

	}

}
