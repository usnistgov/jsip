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
package gov.nist.javax.sdp.parser;

import gov.nist.javax.sdp.fields.*;
import gov.nist.core.*;
import java.text.*;

/**
 * @author  deruelle
 * @version JAIN-SDP-PUBLIC-RELEASE $Revision: 1.4 $ $Date: 2006-07-02 09:52:54 $
 */
public class OriginFieldParser extends SDPParser {

	/** Creates new OriginFieldParser */
	public OriginFieldParser(String originField) {
		lexer = new Lexer("charLexer", originField);
	}

	public OriginField originField() throws ParseException {
		try {
			OriginField originField = new OriginField();

			lexer.match('o');
			lexer.SPorHT();
			lexer.match('=');
			lexer.SPorHT();

			lexer.match(LexerCore.SAFE);
			Token userName = lexer.getNextToken();
			originField.setUsername(userName.getTokenValue());
			this.lexer.SPorHT();

			lexer.match(LexerCore.ID);
			Token sessionId = lexer.getNextToken();
			originField.setSessId(Long.parseLong(sessionId.getTokenValue()));
			this.lexer.SPorHT();

			lexer.match(LexerCore.ID);
			Token sessionVersion = lexer.getNextToken();
			originField.setSessVersion(
				Long.parseLong(sessionVersion.getTokenValue()));
			this.lexer.SPorHT();

			lexer.match(LexerCore.ID);
			Token networkType = lexer.getNextToken();
			originField.setNettype(networkType.getTokenValue());
			this.lexer.SPorHT();

			lexer.match(LexerCore.ID);
			Token addressType = lexer.getNextToken();
			originField.setAddrtype(addressType.getTokenValue());
			this.lexer.SPorHT();

			String host = lexer.getRest();
			HostNameParser hostNameParser = new HostNameParser(host);
			Host h = hostNameParser.host();
			originField.setAddress(h);

			return originField;
		} catch (Exception e) {
			throw new ParseException(lexer.getBuffer(), lexer.getPtr());
		}
	}

	public SDPField parse() throws ParseException {
		return this.originField();
	}

	public static void main(String[] args) throws ParseException {
		String origin[] =
			{   "o=- 4322650003578 0 IN IP4 192.53.18.122\r\n",
				"o=4855 13760799956958020 13760799956958020 IN IP4 166.35.224.216\n",
				"o=mh/andley 2890844526 2890842807 IN IP4 126.16.64.4\n",
				"o=UserB 2890844527 2890844527 IN IP4 everywhere.com\n",
				"o=UserA 2890844526 2890844526 IN IP4 here.com\n",
				"o=IFAXTERMINAL01 2890844527 2890844527 IN IP4 ift.here.com\n",
				"o=GATEWAY1 2890844527 2890844527 IN IP4 gatewayone.wcom.com\n",
				"o=- 2890844527 2890844527 IN IP4 gatewayone.wcom.com\n" };

		for (int i = 0; i < origin.length; i++) {
			OriginFieldParser originFieldParser =
				new OriginFieldParser(origin[i]);
			OriginField originField = originFieldParser.originField();
			System.out.println("toParse :" + origin[i]);
			System.out.println("encoded: " + originField.encode());
		}

	}
}
/*
 * $Log: not supported by cvs2svn $
 * Revision 1.4  2006/06/19 06:47:26  mranga
 * javadoc fixups
 *
 * Revision 1.3  2006/06/16 15:26:28  mranga
 * Added NIST disclaimer to all public domain files. Clean up some javadoc. Fixed a leak
 *
 * Revision 1.2  2006/04/01 04:52:22  mranga
 * *** empty log message ***
 *
 * Revision 1.1.1.1  2005/10/04 17:12:34  mranga
 *
 * Import
 *
 *
 * Revision 1.3  2004/10/21 14:57:17  mranga
 * Reviewed by:   mranga
 *
 * Fixed origin field parser for sdp.
 *
 * Revision 1.2  2004/01/22 13:26:28  sverker
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
