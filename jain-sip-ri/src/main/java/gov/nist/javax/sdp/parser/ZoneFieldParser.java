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

/**
 * Parser For the Zone field.
 *
 * @version JAIN-SDP-PUBLIC-RELEASE $Revision: 1.8 $ $Date: 2009-07-17 18:57:18 $
 *
 * @author Olivier Deruelle <deruelle@antd.nist.gov>
 * @author M. Ranganathan <mranga@antd.nist.gov> <br/>
 *
 *
 *
 */
public class ZoneFieldParser extends SDPParser {

    /** Creates new ZoneFieldParser */
    public ZoneFieldParser(String zoneField) {
        lexer = new Lexer("charLexer", zoneField);
    }

    /**
     * Get the sign of the offset.
     *
     * @param tokenValue
     *            to examine.
     * @return the sign.
     */
    public String getSign(String tokenValue) {
        if (tokenValue.startsWith("-"))
            return "-";
        else
            return "+";
    }

    /**
     * Get the typed time.
     *
     * @param tokenValue --
     *            token value to convert to typed time.
     * @return TypedTime -- the converted typed time value.
     */
    public TypedTime getTypedTime(String tokenValue) {
        TypedTime typedTime = new TypedTime();
        String offset = null;
        if (tokenValue.startsWith("-"))
            offset = tokenValue.replace('-', ' ');
        else if (tokenValue.startsWith("+"))
            offset = tokenValue.replace('+', ' ');
        else
            offset = tokenValue;

        if (offset.endsWith("d")) {
            typedTime.setUnit("d");
            String t = offset.replace('d', ' ');

            typedTime.setTime(Integer.parseInt(t.trim()));
        } else if (offset.endsWith("h")) {
            typedTime.setUnit("h");
            String t = offset.replace('h', ' ');
            typedTime.setTime(Integer.parseInt(t.trim()));
        } else if (offset.endsWith("m")) {
            typedTime.setUnit("m");
            String t = offset.replace('m', ' ');
            typedTime.setTime(Integer.parseInt(t.trim()));
        } else {
            typedTime.setUnit("s");
            if (offset.endsWith("s")) {
                String t = offset.replace('s', ' ');
                typedTime.setTime(Integer.parseInt(t.trim()));
            } else
                typedTime.setTime(Integer.parseInt(offset.trim()));
        }
        return typedTime;
    }

    /**
     * parse the Zone field string
     *
     * @return ZoneField
     */
    public ZoneField zoneField() throws ParseException {
        try {
            ZoneField zoneField = new ZoneField();

            this.lexer.match('z');
            this.lexer.SPorHT();
            this.lexer.match('=');
            this.lexer.SPorHT();

            // The zoneAdjustment list:
            // Patch 117
            while (lexer.hasMoreChars()) {
                char la = lexer.lookAhead(0);
                if (la == '\n' || la == '\r')
                    break;
                ZoneAdjustment zoneAdjustment = new ZoneAdjustment();

                lexer.match(LexerCore.ID);
                Token time = lexer.getNextToken();
                this.lexer.SPorHT();
                String timeValue = time.getTokenValue();
                if (timeValue.length() > 18)
                    timeValue = timeValue.substring(timeValue.length() - 18);

                zoneAdjustment.setTime(Long.parseLong(timeValue));

                lexer.match(LexerCore.ID);
                Token offset = lexer.getNextToken();
                this.lexer.SPorHT();
                String sign = getSign(offset.getTokenValue());
                TypedTime typedTime = getTypedTime(offset.getTokenValue());
                zoneAdjustment.setSign(sign);
                zoneAdjustment.setOffset(typedTime);

                zoneField.addZoneAdjustment(zoneAdjustment);
            }

            return zoneField;
        } catch (Exception e) {
            throw lexer.createParseException();
        }
    }

    public SDPField parse() throws ParseException {
        return this.zoneField();
    }

    /**
     * public static void main(String[] args) throws ParseException { String
     * zone[] = { "z=2882844526 -1h 2898848070 0\n", "z=2886 +1h 2898848070 10
     * 23423 -6s \n" };
     *
     * for (int i = 0; i <zone.length; i++) { ZoneFieldParser
     * zoneFieldParser=new ZoneFieldParser( zone[i] ); ZoneField
     * zoneField=zoneFieldParser.zoneField(); System.out.println("encoded: "
     * +zoneField.encode()); }
     *  }
     */

}
/*
 * $Log: not supported by cvs2svn $
 * Revision 1.7  2007/07/12 15:39:52  mranga
 * Issue number:  117
 * Obtained from:
 * Submitted by:  patrick_poglitsch (patrick_poglitsch)
 * Reviewed by:   mranga
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
 * Revision 1.5 2006/08/23 00:00:22 mranga Issue number: Obtained from:
 * Submitted by: Reviewed by: mranga
 *
 * javadoc fixups.
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
 * Revision 1.4 2006/07/13 09:02:37 mranga Issue number: Obtained from:
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
 * Revision 1.3 2006/06/19 06:47:26 mranga javadoc fixups
 *
 * Revision 1.2 2006/06/16 15:26:28 mranga Added NIST disclaimer to all public
 * domain files. Clean up some javadoc. Fixed a leak
 *
 * Revision 1.1.1.1 2005/10/04 17:12:34 mranga
 *
 * Import
 *
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
