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
import java.text.ParseException;

/** Parser for Repeat field.
*
*@version 1.2
*
*@author Olivier Deruelle
*@author M. Ranganathan   <br/>
*
*
*
 */
public class RepeatFieldParser extends SDPParser {

    /** Creates new RepeatFieldsParser */
    public RepeatFieldParser(String repeatField) {
        lexer = new Lexer("charLexer", repeatField);
    }

    /** Get the typed time.
     *
     * @param  tokenValue to convert into a typed time.
     * @return the typed time
     */
    public TypedTime getTypedTime(String tokenValue) {
        TypedTime typedTime = new TypedTime();

        if (tokenValue.endsWith("d")) {
            typedTime.setUnit("d");
            String t = tokenValue.replace('d', ' ');

            typedTime.setTime(Integer.parseInt(t.trim()));
        } else if (tokenValue.endsWith("h")) {
            typedTime.setUnit("h");
            String t = tokenValue.replace('h', ' ');
            typedTime.setTime(Integer.parseInt(t.trim()));
        } else if (tokenValue.endsWith("m")) {
            typedTime.setUnit("m");
            String t = tokenValue.replace('m', ' ');
            typedTime.setTime(Integer.parseInt(t.trim()));
        } else {
            typedTime.setUnit("s");
            if (tokenValue.endsWith("s")) {
                String t = tokenValue.replace('s', ' ');
                typedTime.setTime(Integer.parseInt(t.trim()));
            } else
                typedTime.setTime(Integer.parseInt(tokenValue.trim()));
        }
        return typedTime;
    }

    /** parse the field string
     * @return RepeatFields
     */
    public RepeatField repeatField() throws ParseException {
        try {

            this.lexer.match('r');
            this.lexer.SPorHT();
            this.lexer.match('=');
            this.lexer.SPorHT();

            RepeatField repeatField = new RepeatField();

            lexer.match(LexerCore.ID);
            Token repeatInterval = lexer.getNextToken();
            this.lexer.SPorHT();
            TypedTime typedTime = getTypedTime(repeatInterval.getTokenValue());
            repeatField.setRepeatInterval(typedTime);

            lexer.match(LexerCore.ID);
            Token activeDuration = lexer.getNextToken();
            this.lexer.SPorHT();
            typedTime = getTypedTime(activeDuration.getTokenValue());
            repeatField.setActiveDuration(typedTime);

            // The offsets list:
            /*Patch 117 */
            while (lexer.hasMoreChars()) {
                char la = lexer.lookAhead(0);
                if (la == '\n' || la == '\r')
                     break;
                lexer.match(LexerCore.ID);
                Token offsets = lexer.getNextToken();
                this.lexer.SPorHT();
                typedTime = getTypedTime(offsets.getTokenValue());
                repeatField.addOffset(typedTime);
            }

            return repeatField;
        } catch (Exception e) {
            throw lexer.createParseException();
        }
    }

    public SDPField parse() throws ParseException {
        return this.repeatField();
    }

    /**

        public static void main(String[] args) throws ParseException {
            String repeat[] = {
                            "r=604800s 3600s 0s 90000s\n",
                "r=7d 1h 0 25h\n",
                            "r=7 6 5 4 3 2 1 0 \n"
                    };

            for (int i = 0; i < repeat.length; i++) {
                RepeatFieldParser repeatFieldParser=new RepeatFieldParser(
                    repeat[i] );
                RepeatField repeatFields=repeatFieldParser.repeatField();
            System.out.println("toParse: " +repeat[i]);
            System.out.println("encoded: " +repeatFields.encode());
            }

        }
    **/
}
/*
 * $Log: not supported by cvs2svn $
 * Revision 1.6  2007/07/12 15:39:53  mranga
 * Issue number:  117
 * Obtained from:
 * Submitted by:  patrick_poglitsch (patrick_poglitsch)
 * Reviewed by:   mranga
 *
 * Revision 1.5  2006/08/23 00:00:22  mranga
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by: mranga
 *
 * javadoc fixups.
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
 * Revision 1.4  2006/07/13 09:02:39  mranga
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
