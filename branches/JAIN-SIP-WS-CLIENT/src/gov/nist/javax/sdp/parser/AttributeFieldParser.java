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
 * AttributeFieldParser.java
 *
 * Created on February 19, 2002, 10:09 AM
 */

package gov.nist.javax.sdp.parser;
import gov.nist.javax.sdp.fields.*;
import gov.nist.core.*;
import java.text.*;

/**
 * @author  Olivier Deruelle
 * @version JAIN-SDP-PUBLIC-RELEASE $Revision: 1.6 $ $Date: 2009-07-17 18:57:15 $
 */
public class AttributeFieldParser extends SDPParser {

    /** Creates new AttributeFieldParser */
    public AttributeFieldParser(String attributeField) {

        this.lexer = new Lexer("charLexer", attributeField);
    }

    public AttributeField attributeField() throws ParseException {
        try {
            AttributeField attributeField = new AttributeField();

            this.lexer.match('a');

            this.lexer.SPorHT();
            this.lexer.match('=');

            this.lexer.SPorHT();

            NameValue nameValue = new NameValue();

            int ptr = this.lexer.markInputPosition();
            try {
                String name = lexer.getNextToken(':');
                this.lexer.consume(1);
                String value = lexer.getRest();
                nameValue = new NameValue(name.trim(), value.trim());
            } catch (ParseException ex) {
                this.lexer.rewindInputPosition(ptr);
                String rest = this.lexer.getRest();
                if (rest == null)
                    throw new ParseException(
                        this.lexer.getBuffer(),
                        this.lexer.getPtr());
                nameValue = new NameValue(rest.trim(), null);
            }
            attributeField.setAttribute(nameValue);

            this.lexer.SPorHT();

            return attributeField;
        } catch (Exception e) {
            e.printStackTrace();
            throw new ParseException(e.getMessage(), 0);
        }
    }

    public SDPField parse() throws ParseException {
        return this.attributeField();
    }

    /**
        public static void main(String[] args) throws ParseException {
            String attribute[] = {
                "a=rtpmap:0 PCMU/8000\n",
                "a=rtpmap:31 LPC\n",
                "a=rtpmap:0 PCMU/8000\n",
                            "a=rtpmap:4 G723/8000\n",
                            "a=rtpmap:18 G729A/8000\n",
                            "a=ptime:30\n",
                            "a=orient:landscape\n",
                            "a=recvonly\n",
                            "a=T38FaxVersion:0\n",
                            "a=T38maxBitRate:14400\n",
                            "a=T38FaxFillBitRemoval:0\n",
                            "a=T38FaxTranscodingMMR:0\n",
                            "a=T38FaxTranscodingJBIG:0\n",
                            "a=T38FaxRateManagement:transferredTCF\n",
                            "a=T38FaxMaxBuffer:72\n",
                            "a=T38FaxMaxDatagram:316\n",
                            "a=T38FaxUdpEC:t38UDPRedundancy\n"
                    };

            for (int i = 0; i < attribute.length; i++) {
                   AttributeFieldParser attributeFieldParser = new AttributeFieldParser(
                    attribute[i] );
            AttributeField attributeField= attributeFieldParser.attributeField();
            System.out.println("encoded: " + attributeField.encode());
            }

        }
    **/
}
/*
 * $Log: not supported by cvs2svn $
 * Revision 1.5  2006/07/13 09:02:35  mranga
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
