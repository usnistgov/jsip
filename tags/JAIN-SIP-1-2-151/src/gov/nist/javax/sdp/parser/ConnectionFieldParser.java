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
 * Parser for Connection Field.
 *
 * @version JAIN-SDP-PUBLIC-RELEASE $Revision: 1.5 $ $Date: 2009-07-17 18:57:15 $
 *
 * @author Olivier Deruelle
 * @author M. Ranganathan
 *
 *
 */
public class ConnectionFieldParser extends SDPParser {

    /** Creates new ConnectionFieldParser */
    public ConnectionFieldParser(String connectionField) {
        this.lexer = new Lexer("charLexer", connectionField);
    }

    public ConnectionAddress connectionAddress(String address) {
        ConnectionAddress connectionAddress = new ConnectionAddress();

        int begin = address.indexOf("/");

        if (begin != -1) {
            connectionAddress.setAddress(new Host(address.substring(0, begin)));

            int middle = address.indexOf("/", begin + 1);
            if (middle != -1) {
                String ttl = address.substring(begin + 1, middle);
                connectionAddress.setTtl(Integer.parseInt(ttl.trim()));

                String addressNumber = address.substring(middle + 1);
                connectionAddress.setPort(
                    Integer.parseInt(addressNumber.trim()));
            } else {
                String ttl = address.substring(begin + 1);
                connectionAddress.setTtl(Integer.parseInt(ttl.trim()));
            }
        } else
            connectionAddress.setAddress(new Host(address));

        return connectionAddress;
    }

    public ConnectionField connectionField() throws ParseException {
        try {
            this.lexer.match('c');
            this.lexer.SPorHT();
            this.lexer.match('=');
            this.lexer.SPorHT();

            ConnectionField connectionField = new ConnectionField();

            lexer.match(LexerCore.ID);
            this.lexer.SPorHT();
            Token token = lexer.getNextToken();
            connectionField.setNettype(token.getTokenValue());

            lexer.match(LexerCore.ID);
            this.lexer.SPorHT();
            token = lexer.getNextToken();
            connectionField.setAddressType(token.getTokenValue());
            this.lexer.SPorHT();
            String rest = lexer.getRest();
            ConnectionAddress connectionAddress =
                connectionAddress(rest.trim());

            connectionField.setAddress(connectionAddress);

            return connectionField;
        } catch (Exception e) {
            throw new ParseException(lexer.getBuffer(), lexer.getPtr());
        }
    }

    public SDPField parse() throws ParseException {
        return this.connectionField();
    }

    /**
        public static void main(String[] args) throws ParseException {
            String connection[] = {
                "c=IN IP4 224.2.17.12/127\n",
                "c=IN IP4 224.2.1.1/127/3 \n",
                "c=IN IP4 135.180.130.88\n"
                    };

            for (int i = 0; i < connection.length; i++) {
               ConnectionFieldParser connectionFieldParser=
                        new ConnectionFieldParser(
                                connection[i] );
            ConnectionField connectionField=connectionFieldParser.
                            connectionField();
            System.out.println("toParse: " +connection[i]);
            System.out.println("encoded: " +connectionField.encode());
            }

        }
    **/
}
/*
 * $Log: not supported by cvs2svn $
 * Revision 1.4  2006/07/13 09:02:43  mranga
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
