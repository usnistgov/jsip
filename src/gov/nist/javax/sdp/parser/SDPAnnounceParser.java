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
	*@param sdpMessage message containing the sdp announce message.
	*
	*/
	public SDPAnnounceParser(String sdpAnnounce) {
		int start = 0;
		String line = null;
		sdpMessage = new Vector();
		// Return trivially if there is no sdp announce message
		// to be parsed. Bruno Konik noticed this bug.
		if (sdpAnnounce == null ) return;
		// Bug fix by Andreas Bystrom.
		while (start < sdpAnnounce.length()) {
			int add = 0;
			int index = sdpAnnounce.indexOf("\n", start);
			int index2 = sdpAnnounce.indexOf("\r", start);

			if (index > 0 && index2 < 0) {
				// there are only "\n" separators
				line = sdpAnnounce.substring(start, index);
				start = index + 1;
			} else if (index < 0 && index2 > 0) {
				//bug fix: there are only "\r" separators
				line = sdpAnnounce.substring(start, index2);
				start = index2 + 1;
			} else if (index > 0 && index2 > 0) {
				// there are "\r\n" or "\n\r" (if exists) separators
				if (index > index2) {
					line = sdpAnnounce.substring(start, index2);
					start = index + 1;
				} else {
					line = sdpAnnounce.substring(start, index);
					start = index2 + 1;
				}
			} else if (index < 0 && index2 < 0) // end
				break;
			sdpMessage.addElement(line);
		}
	}

	public SessionDescriptionImpl parse() throws ParseException {
		SessionDescriptionImpl retval = new SessionDescriptionImpl();
		for (int i = 0; i < sdpMessage.size(); i++) {
			String field = (String) sdpMessage.elementAt(i);
			SDPParser sdpParser = ParserFactory.createParser(field);
			SDPField sdpField = sdpParser.parse();
			retval.addField(sdpField);
		}
		return retval;

	}

}
/*
 * $Log: not supported by cvs2svn $
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
