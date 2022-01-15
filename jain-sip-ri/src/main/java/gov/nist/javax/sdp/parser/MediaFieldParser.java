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
import java.util.*;

/**
 * Parser for Media field.
 *
 * @version JAIN-SDP-PUBLIC-RELEASE $Revision: 1.6 $ $Date: 2009-07-17 18:57:16 $
 *
 * @author  Olivier Deruelle
 * @author M. Ranganathan   <br/>
 *
 *
 *
 */
public class MediaFieldParser extends SDPParser {

    /** Creates new MediaFieldParser */
    public MediaFieldParser(String mediaField) {
        lexer = new Lexer("charLexer", mediaField);
    }

    public MediaField mediaField() throws ParseException {
        if (Debug.parserDebug)
            dbg_enter("mediaField");
        try {
            MediaField mediaField = new MediaField();

            lexer.match('m');
            lexer.SPorHT();
            lexer.match('=');
            lexer.SPorHT();

            lexer.match(Lexer.ID);
            Token media = lexer.getNextToken();
            mediaField.setMedia(media.getTokenValue());
            this.lexer.SPorHT();

            lexer.match(Lexer.ID);
            Token port = lexer.getNextToken();
            mediaField.setPort(Integer.parseInt(port.getTokenValue()));

            this.lexer.SPorHT();

            // Some strange media formatting from Sun Ray systems with media
            // reported by Emil Ivov and Iain Macdonnell at Sun
            if (lexer.hasMoreChars() && lexer.lookAhead(1) == '\n')
                return mediaField;

            if (lexer.lookAhead(0) == '/') {
                // The number of ports is present:
                lexer.consume(1);
                lexer.match(Lexer.ID);
                Token portsNumber = lexer.getNextToken();
                mediaField.setNports(
                    Integer.parseInt(portsNumber.getTokenValue()));
                this.lexer.SPorHT();
            }
            // proto = token *("/" token)
            lexer.match(Lexer.ID);
            Token token = lexer.getNextToken();
            String transport = token.getTokenValue();
            while (lexer.lookAhead(0) == '/') {
                lexer.consume(1);
                lexer.match(Lexer.ID);
                Token transportTemp = lexer.getNextToken();
                transport = transport + "/" + transportTemp.getTokenValue();
            }
            mediaField.setProto(transport);
            this.lexer.SPorHT();

            // The formats list:
            Vector formatList = new Vector();
            while (lexer.hasMoreChars()) {
                char la = lexer.lookAhead(0);
                if (la == '\n' || la == '\r')
                    break;
                this.lexer.SPorHT();
                //while(lexer.lookAhead(0) == ' ') lexer.consume(1);
                lexer.match(Lexer.ID);
                Token tok = lexer.getNextToken();
                this.lexer.SPorHT();
                String format = tok.getTokenValue().trim();
                if (!format.equals(""))
                    formatList.add(format);
            }
            mediaField.setFormats(formatList);

            return mediaField;
        } catch (Exception e) {
            e.printStackTrace();
            //System.out.println(lexer.getPtr());
            //System.out.println("char = [" + lexer.lookAhead(0) +"]");
            throw new ParseException(lexer.getBuffer(), lexer.getPtr());
        } finally {
            dbg_leave("mediaField");
        }
    }

    public SDPField parse() throws ParseException {
        return this.mediaField();
    }

    /**
        public static void main(String[] args) throws ParseException {
            String media[] = {
                "m=video 0\r\n",
                "m=audio 50006 RTP/AVP 0 8 4 18\r\n",
                "m=video 49170/2 RTP/AVP 31\n",
                            "m=video 49232 RTP/AVP 0\n",
                            "m=audio 49230 RTP/AVP 96 97 98\n",
                            "m=application 32416 udp wb\n",
                            "m=control 49234 H323 mc\n",
                            "m=audio 50012 RTP/AVP 0 8 4 18\n",
                            "m=image 49172 udptl t38\n"
                    };

            for (int i = 0; i < media.length; i++) {
               MediaFieldParser mediaFieldParser=new MediaFieldParser(
                    media[i] );
            System.out.println("toParse: " + media[i]);
            MediaField mediaField=mediaFieldParser.mediaField();
            System.out.println("encoded: " +mediaField.encode());
            }

        }
    **/
}
/*
 * $Log: not supported by cvs2svn $
 * Revision 1.5  2007/02/06 16:40:00  belangery
 * Introduced simple code optimizations.
 *
 * Revision 1.4  2006/07/13 09:02:42  mranga
 * Issue number:
 * Obtained from:
 * Submitted by:  jeroen van bemmel
 * Reviewed by:   mranga
 * Moved some changes from jain-sip-1.2 to java.net
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
 * Revision 1.3  2006/06/19 06:47:26  mranga
 * javadoc fixups
 *
 * Revision 1.2  2006/06/16 15:26:28  mranga
 * Added NIST disclaimer to all public domain files. Clean up some javadoc. Fixed a leak
 *
 * Revision 1.1.1.1  2005/10/04 17:12:34  mranga
 *
 * Import
 *
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
