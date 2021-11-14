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
import java.util.*;
import gov.nist.core.*;
import gov.nist.javax.sdp.fields.*;
import gov.nist.javax.sdp.*;
import java.text.ParseException;
// Acknowledgement: this includes a bug fix submitted by
// Rafael Barriuso rbarriuso@dit.upm.es

/** Parser for SDP Announce messages.
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class SDPAnnounceParser extends ParserCore {

    protected Lexer lexer;
    protected Vector sdpMessage;

    /** Creates new SDPAnnounceParser
     * @param sdpMessage Vector of messages to parse.
     */
    public SDPAnnounceParser(Vector sdpMessage) {
        this.sdpMessage = sdpMessage;
    }

    /** Create a new SDPAnnounceParser.
    *@param message  string containing the sdp announce message.
    *
    */
	public SDPAnnounceParser(String message) {
        int start = 0;
        String line = null;
        // Return trivially if there is no sdp announce message
        // to be parsed. Bruno Konik noticed this bug.
        if (message == null) return;
        sdpMessage = new Vector();
        // Strip off leading and trailing junk.
        String sdpAnnounce = message.trim() + "\r\n";
        // Bug fix by Andreas Bystrom.
        while (start < sdpAnnounce.length()) {
            // Major re-write by Ricardo Borba.
            int lfPos = sdpAnnounce.indexOf("\n", start);
            int crPos = sdpAnnounce.indexOf("\r", start);

            if (lfPos >= 0 && crPos < 0) {
                // there are only "\n" separators
                line = sdpAnnounce.substring(start, lfPos);
                start = lfPos + 1;
            } else if (lfPos < 0 && crPos >= 0) {
                //bug fix: there are only "\r" separators
                line = sdpAnnounce.substring(start, crPos);
                start = crPos + 1;
            } else if (lfPos >= 0 && crPos >= 0) {
                // there are "\r\n" or "\n\r" (if exists) separators
                if (lfPos > crPos) {
                    // assume "\r\n" for now
                    line = sdpAnnounce.substring(start, crPos);
                    // Check if the "\r" and "\n" are close together
                    if (lfPos == crPos + 1) {
                        start = lfPos + 1; // "\r\n"
                    } else {
                        start = crPos + 1; // "\r" followed by the next record and a "\n" further away
                    }
                } else {
                    // assume "\n\r" for now
                    line = sdpAnnounce.substring(start, lfPos);
                    // Check if the "\n" and "\r" are close together
                    if (crPos == lfPos + 1) {
                        start = crPos + 1; // "\n\r"
                    } else {
                        start = lfPos + 1; // "\n" followed by the next record and a "\r" further away
                    }
                }
            } else if (lfPos < 0 && crPos < 0) { // end
                break;
            }
            sdpMessage.addElement(line);
        }
    }

    public SessionDescriptionImpl parse() throws ParseException {
        SessionDescriptionImpl retval = new SessionDescriptionImpl();
        for (int i = 0; i < sdpMessage.size(); i++) {
            String field = (String) sdpMessage.elementAt(i);
            SDPParser sdpParser = ParserFactory.createParser(field);
            SDPField sdpField = null;
            if (sdpParser != null)
            {
               sdpField = sdpParser.parse();
            }
            retval.addField(sdpField);
        }
        return retval;

    }



}
/*
 * $Log: not supported by cvs2svn $
 * Revision 1.10  2009/07/17 18:57:17  emcho
 * Converts indentation tabs to spaces so that we have a uniform indentation policy in the whole project.
 *
 * Revision 1.9  2008/01/18 02:18:24  mranga
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:   mranga
 * Moved out some test code.
 *
 * Revision 1.8  2006/11/22 04:08:57  rborba
 * Fixed a null pointer exception when an SDP is received with an empty session name ("s=").
 *
 * Revision 1.7  2006/07/13 09:02:37  mranga
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
 * Revision 1.5  2005/01/21 21:19:24  mranga
 * Reviewed by:   mranga
 * Strip leading whitespace while parsing Sdp Announce messages.
 *
 * Revision 1.4  2004/02/18 14:33:02  mranga
 * Submitted by:  Bruno Konik
 * Reviewed by:   mranga
 * Remove extraneous newline in encoding messages. Test for empty sdp announce
 * rather than die with null when null is passed to sdp announce parser.
 * Fixed bug in checking for \n\n when looking for message end.
 *
 * Revision 1.3  2004/01/22 13:26:28  sverker
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
