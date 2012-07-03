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
 * @author deruelle
 * @version JAIN-SDP-PUBLIC-RELEASE $Revision: 1.8 $ $Date: 2009-07-17 18:57:16 $
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

            lexer.match(LexerCore.ID_NO_WHITESPACE);
            Token userName = lexer.getNextToken();
            originField.setUsername(userName.getTokenValue());
            this.lexer.SPorHT();

            lexer.match(LexerCore.ID);
            //lexer.ttokenSafe();
            Token sessionId = lexer.getNextToken();
            // guard against very long session IDs
            String sessId = sessionId.getTokenValue();
            if (sessId.length() > 18)
                sessId = sessId.substring(sessId.length() - 18);
            try {
                originField.setSessId(Long.parseLong(sessId));
            } catch (NumberFormatException ex) {
                originField.setSessionId(sessId);
            }
            this.lexer.SPorHT();

            lexer.match(LexerCore.ID);
            Token sessionVersion = lexer.getNextToken();
            // guard against very long session Verion
            String sessVer = sessionVersion.getTokenValue();
            if (sessVer.length() > 18)
                sessVer = sessVer.substring(sessVer.length() - 18);
            try {
                originField.setSessVersion(Long.parseLong(sessVer));
            } catch (NumberFormatException ex) {
                originField.setSessVersion(sessVer);

            }
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
            e.printStackTrace();
            throw new ParseException(lexer.getBuffer(), lexer.getPtr());
        }
    }

    public SDPField parse() throws ParseException {
        return this.originField();
    }

    public static void main(String[] args) throws ParseException {
        String origin[] = {
                "o=- 45ec4ba1.1 45ec4ba1 in ip4 10.1.80.200\r\n",
                "o=- 4322650003578 0 IN IP4 192.53.18.122\r\n",
                "o=4855 12345678901234567890 12345678901234567890 IN IP4 166.35.224.216\n",
                "o=mh/andley 2890844526 2890842807 IN IP4 126.16.64.4\n",
                "o=UserB 2890844527 2890844527 IN IP4 everywhere.com\n",
                "o=UserA 2890844526 2890844526 IN IP4 here.com\n",
                "o=IFAXTERMINAL01 2890844527 2890844527 IN IP4 ift.here.com\n",
                "o=GATEWAY1 2890844527 2890844527 IN IP4 gatewayone.wcom.com\n",
                "o=- 2890844527 2890844527 IN IP4 gatewayone.wcom.com\n" };

        for (int i = 0; i < origin.length; i++) {
            OriginFieldParser originFieldParser = new OriginFieldParser(
                    origin[i]);
            OriginField originField = originFieldParser.originField();
            System.out.println("toParse :" + origin[i]);
            System.out.println("encoded: " + originField.encode());
        }

    }
}
/*
 * $Log: not supported by cvs2svn $
 * Revision 1.7  2007/03/08 05:20:20  mranga
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by: mranga
 * Fix logging code. Losen up on Sdp parsing  (allow for extra long origin fields)
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
 * Revision 1.6 2006/11/16 19:05:44 pmusgrave
 * Issue number: 82 - SDP with Origin/Time or Zone creater than 18 digits causes
 * parse exception Obtained from: Submitted by: pmusgrave@newheights.com
 * Reviewed by: CVS:
 * ---------------------------------------------------------------------- CVS:
 * Issue number: CVS: If this change addresses one or more issues, CVS: then
 * enter the issue number(s) here. CVS: Obtained from: CVS: If this change has
 * been taken from another system, CVS: then name the system in this line,
 * otherwise delete it. CVS: Submitted by: CVS: If this code has been
 * contributed to the project by someone else; i.e., CVS: they sent us a patch
 * or a set of diffs, then include their name/email CVS: address here. If this
 * is your work then delete this line. CVS: Reviewed by: CVS: If we are doing
 * pre-commit code reviews and someone else has CVS: reviewed your changes,
 * include their name(s) here. CVS: If you have not had it reviewed then delete
 * this line.
 *
 * Revision 1.5 2006/07/13 09:02:36 mranga Issue number: Obtained from:
 * Submitted by: jeroen van bemmel Reviewed by: mranga Moved some changes from
 * jain-sip-1.2 to java.net
 *
 * CVS: ----------------------------------------------------------------------
 * CVS: Issue number: CVS: If this change addresses one or more issues, CVS:
 * then enter the issue number(s) here. CVS: Obtained from: CVS: If this change
 * has been taken from another system, CVS: then name the system in this line,
 * otherwise delete it. CVS: Submitted by: CVS: If this code has been
 * contributed to the project by someone else; i.e., CVS: they sent us a patch
 * or a set of diffs, then include their name/email CVS: address here. If this
 * is your work then delete this line. CVS: Reviewed by: CVS: If we are doing
 * pre-commit code reviews and someone else has CVS: reviewed your changes,
 * include their name(s) here. CVS: If you have not had it reviewed then delete
 * this line.
 *
 * Revision 1.4 2006/06/19 06:47:26 mranga javadoc fixups
 *
 * Revision 1.3 2006/06/16 15:26:28 mranga Added NIST disclaimer to all public
 * domain files. Clean up some javadoc. Fixed a leak
 *
 * Revision 1.2 2006/04/01 04:52:22 mranga *** empty log message ***
 *
 * Revision 1.1.1.1 2005/10/04 17:12:34 mranga
 *
 * Import
 *
 *
 * Revision 1.3 2004/10/21 14:57:17 mranga Reviewed by: mranga
 *
 * Fixed origin field parser for sdp.
 *
 * Revision 1.2 2004/01/22 13:26:28 sverker Issue number: Obtained from:
 * Submitted by: sverker Reviewed by: mranga
 *
 * Major reformat of code to conform with style guide. Resolved compiler and
 * javadoc warnings. Added CVS tags.
 *
 * CVS: ----------------------------------------------------------------------
 * CVS: Issue number: CVS: If this change addresses one or more issues, CVS:
 * then enter the issue number(s) here. CVS: Obtained from: CVS: If this change
 * has been taken from another system, CVS: then name the system in this line,
 * otherwise delete it. CVS: Submitted by: CVS: If this code has been
 * contributed to the project by someone else; i.e., CVS: they sent us a patch
 * or a set of diffs, then include their name/email CVS: address here. If this
 * is your work then delete this line. CVS: Reviewed by: CVS: If we are doing
 * pre-commit code reviews and someone else has CVS: reviewed your changes,
 * include their name(s) here. CVS: If you have not had it reviewed then delete
 * this line.
 *
 */
