package gov.nist.javax.sip.parser;

import gov.nist.javax.sip.header.*;
import gov.nist.core.*;
import java.text.ParseException;

/**
 * Parser for via headers.
 *
 * @version JAIN-SIP-1.1 $Revision: 1.5 $ $Date: 2005-01-28 00:23:37 $
 *
 * @author Olivier Deruelle 
 * @author M. Ranganathan 
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 */
public class ViaParser extends HeaderParser {

	public ViaParser(String via) {
		super(via);
	}

	public ViaParser(Lexer lexer) {
		super(lexer);
	}

	/**
	 * a parser for the essential part of the via header.
	 */
	private void parseVia(Via v) throws ParseException {
		// The protocol   
		lexer.match(TokenTypes.ID);
		Token protocolName = lexer.getNextToken();

		this.lexer.SPorHT();
		// consume the "/"
		lexer.match('/');
		this.lexer.SPorHT();
		lexer.match(TokenTypes.ID);
		this.lexer.SPorHT();
		Token protocolVersion = lexer.getNextToken();

		this.lexer.SPorHT();

		// We consume the "/"
		lexer.match('/');
		this.lexer.SPorHT();
		lexer.match(TokenTypes.ID);
		this.lexer.SPorHT();

		Token transport = lexer.getNextToken();
		this.lexer.SPorHT();

		Protocol protocol = new Protocol();
		protocol.setProtocolName(protocolName.getTokenValue());
		protocol.setProtocolVersion(protocolVersion.getTokenValue());
		protocol.setTransport(transport.getTokenValue());
		v.setSentProtocol(protocol);

		// sent-By  
		HostNameParser hnp = new HostNameParser(this.getLexer());
		HostPort hostPort = hnp.hostPort();
		v.setSentBy(hostPort);

		// Ignore blanks
		this.lexer.SPorHT();

		// parameters
		while (lexer.lookAhead(0) == ';') {
			this.lexer.match(';');
			this.lexer.SPorHT();
			NameValue nameValue = this.nameValue();
			String name = nameValue.getName();
			nameValue.setName(name.toLowerCase());
			v.setParameter(nameValue);
			this.lexer.SPorHT();
		}

		if (lexer.lookAhead(0) == '(') {
			this.lexer.selectLexer("charLexer");
			lexer.consume(1);
			StringBuffer comment = new StringBuffer();
			boolean cond = true;
			while (true) {
				char ch = lexer.lookAhead(0);
				if (ch == ')') {
					lexer.consume(1);
					break;
				} else if (ch == '\\') {
					// Escaped character
					Token tok = lexer.getNextToken();
					comment.append(tok.getTokenValue());
					lexer.consume(1);
					tok = lexer.getNextToken();
					comment.append(tok.getTokenValue());
					lexer.consume(1);
				} else if (ch == '\n') {
					break;
				} else {
					comment.append(ch);
					lexer.consume(1);
				}
			}
			v.setComment(comment.toString());
		}

	}

	/**
	 * Overrides the superclass nameValue parser because
	 * we have to tolerate IPV6 addresses in the received parameter.
	 */
	protected NameValue nameValue() throws ParseException {
		if (debug)
			dbg_enter("nameValue");
		try {

			lexer.match(LexerCore.ID);
			Token name = lexer.getNextToken();
			// eat white space.
			lexer.SPorHT();
			try {

				boolean quoted = false;

				char la = lexer.lookAhead(0);

				if (la == '=') {
					lexer.consume(1);
					lexer.SPorHT();
					String str = null;
					if (name.getTokenValue().compareToIgnoreCase(Via.RECEIVED)
						== 0) {
						// Allow for IPV6 Addresses.
						// these could have : in them!
						str = lexer.byteStringNoSemicolon();
					} else {
						if (lexer.lookAhead(0) == '\"') {
							str = lexer.quotedString();
							quoted = true;
						} else {
							lexer.match(LexerCore.ID);
							Token value = lexer.getNextToken();
							str = value.getTokenValue();
						}
					}
					NameValue nv = new NameValue(name.getTokenValue(), str);
					if (quoted)
						nv.setQuotedValue();
					return nv;
				} else {
					return new NameValue(name.getTokenValue(), null);
				}
			} catch (ParseException ex) {
				return new NameValue(name.getTokenValue(), null);
			}

		} finally {
			if (debug)
				dbg_leave("nameValue");
		}

	}

	public SIPHeader parse() throws ParseException {
		if (debug)
			dbg_enter("parse");
		try {
			ViaList viaList = new ViaList();
			// The first via header.
			this.lexer.match(TokenTypes.VIA);
			this.lexer.SPorHT(); // ignore blanks
			this.lexer.match(':'); // expect a colon.
			this.lexer.SPorHT(); // ingore blanks.

			while (true) {
				Via v = new Via();
				parseVia(v);
				viaList.add(v);
				this.lexer.SPorHT(); // eat whitespace.
				if (this.lexer.lookAhead(0) == ',') {
					this.lexer.consume(1); // Consume the comma
					this.lexer.SPorHT(); // Ignore space after.
				}
				if (this.lexer.lookAhead(0) == '\n')
					break;
			}
			this.lexer.match('\n');
			return viaList;
		} finally {
			if (debug)
				dbg_leave("parse");
		}

	}

/**

	        public static void main(String args[]) throws ParseException {
			String via[] = {
		"Via: SIP/2.0/UDP 135.180.130.133;branch=-12345\n",
		"Via: SIP/2.0/UDP 166.34.120.100;branch=0000045d-00000001"+
	                       ",SIP/2.0/UDP 166.35.224.216:5000\n",
		"Via: SIP/2.0/UDP sip33.example.com,"+
	         " SIP/2.0/UDP sip32.example.com (oli),"+
	         "SIP/2.0/UDP sip31.example.com\n",
	         "Via: SIP/2.0/UDP host.example.com;received=::133;"+
	         " branch=C1C3344E2710000000E299E568E7potato10potato0potato0\n",
	         "Via: SIP/2.0/UDP host.example.com;received=135.180.130.133;"+
	         " branch=C1C3344E2710000000E299E568E7potato10potato0potato0\n",
	         "Via: SIP/2.0/UDP company.com:5604 ( Hello )"+
	         ", SIP /  2.0  /  UDP 135.180.130.133\n",
	         "Via: SIP/2.0/UDP 129.6.55.9:7060;received=stinkbug.antd.nist.gov\n",
	
	          "Via: SIP/2.0/UDP ss2.wcom.com:5060;branch=721e418c4.1"+
	           ", SIP/2.0/UDP ss1.wcom.com:5060;branch=2d4790.1"+
	           " , SIP/2.0/UDP here.com:5060( Hello the big world) \n"
	            ,"Via: SIP/2.0/UDP ss1.wcom.com:5060;branch=2d4790.1\n",
	                        "Via: SIP/2.0/UDP first.example.com:4000;ttl=16"+
	                        ";maddr=224.2.0.1 ;branch=a7c6a8dlze.1 (Acme server)\n"
	                };
				
			for (int i = 0; i < via.length; i++ ) {
			    ViaParser vp = 
				  new ViaParser(via[i]);
			    System.out.println("toParse = " + via[i]);
			    ViaList vl = (ViaList) vp.parse();
			    System.out.println("encoded = " + vl.encode());
			}
				
		}

**/

}
/*
 * $Log: not supported by cvs2svn $
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
