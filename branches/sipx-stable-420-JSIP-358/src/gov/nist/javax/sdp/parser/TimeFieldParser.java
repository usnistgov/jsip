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
 * TimeFieldParser.java
 *
 * Created on February 25, 2002, 9:58 AM
 */
package gov.nist.javax.sdp.parser;

import gov.nist.javax.sdp.fields.*;
import java.text.*;

/**
 * @author  deruelle
 * @version JAIN-SDP-PUBLIC-RELEASE $Revision: 1.7 $ $Date: 2009-07-17 18:57:18 $
 */
public class TimeFieldParser extends SDPParser {

    /** Creates new TimeFieldParser */
    public TimeFieldParser(String timeField) {
        lexer = new Lexer("charLexer", timeField);
    }

    /** Get the typed time.
     *
     * @param tokenValue to convert to typedTime.
     * @return TypedTime
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

    private long getTime() throws ParseException {
        try {
            String startTime = this.lexer.number();
            if ( startTime.length() > 18)
                startTime = startTime.substring( startTime.length() - 18);
            return Long.parseLong(startTime);
        } catch (NumberFormatException ex) {
            throw lexer.createParseException();
        }

    }

    /** parse the field string
      * @return TimeField
     */
    public TimeField timeField() throws ParseException {
        try {
            this.lexer.match('t');
            this.lexer.SPorHT();
            this.lexer.match('=');
            this.lexer.SPorHT();

            TimeField timeField = new TimeField();

            long st = this.getTime();
            timeField.setStartTime(st);
            this.lexer.SPorHT();

            st = this.getTime();
            timeField.setStopTime(st);

            return timeField;
        } catch (Exception e) {
            throw lexer.createParseException();
        }
    }

    public SDPField parse() throws ParseException {
        return this.timeField();
    }

    /**
        public static void main(String[] args) throws ParseException {
            String time[] = {
                            "t=3033434331 3042462419\n",
                "t=7d 1h \n",
                            "t=3149328700 0 \n",
                            "t=0 0\n"
                    };


            for (int i = 0; i < time.length; i++) {
                TimeFieldParser timeFieldParser=new TimeFieldParser(
                    time[i] );
                TimeField timeField=timeFieldParser.timeField();
            System.out.println("encoded: " +timeField.encode());
            }

        }
    **/
}
/*
 * $Log: not supported by cvs2svn $
 * Revision 1.6  2006/11/16 19:05:44  pmusgrave
 * Issue number:  82 - SDP with Origin/Time or Zone creater than 18 digits causes parse exception
 * Obtained from:
 * Submitted by:  pmusgrave@newheights.com
 * Reviewed by:
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
 * Revision 1.5  2006/08/23 00:00:23  mranga
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
 * Revision 1.4  2006/07/13 09:02:38  mranga
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
