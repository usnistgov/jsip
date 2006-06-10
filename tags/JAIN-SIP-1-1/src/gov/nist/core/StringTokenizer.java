package gov.nist.core;

import java.util.*;
import java.text.ParseException;

/** Base string token splitter.
*
*@version  JAIN-SIP-1.1
*
*@author M. Ranganathan <mranga@nist.gov>  <br/>
*
*<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
*
*/

public class StringTokenizer {

	protected String buffer;
	protected int ptr;
	protected int savedPtr;

	public StringTokenizer() {
	}

	public StringTokenizer(String buffer) {
		this.buffer = buffer;
		ptr = 0;
	}

	public String nextToken() {
		StringBuffer retval = new StringBuffer();

		while (ptr < buffer.length()) {
			if (buffer.charAt(ptr) == '\n') {
				retval.append(buffer.charAt(ptr));
				ptr++;
				break;
			} else {
				retval.append(buffer.charAt(ptr));
				ptr++;
			}
		}

		return retval.toString();
	}

	public boolean hasMoreChars() {
		return ptr < buffer.length();
	}

	public static boolean isHexDigit(char ch) {
		if (isDigit(ch))
			return true;
		else {
			char ch1 = Character.toUpperCase(ch);
			return ch1 == 'A'
				|| ch1 == 'B'
				|| ch1 == 'C'
				|| ch1 == 'D'
				|| ch1 == 'E'
				|| ch1 == 'F';
		}
	}

	public static boolean isAlpha(char ch) {
		boolean retval = Character.isUpperCase(ch) || Character.isLowerCase(ch);
		// Debug.println("isAlpha is returning " + retval  + " for " + ch);
		return retval;
	}

	public static boolean isDigit(char ch) {
		boolean retval = Character.isDigit(ch);
		// Debug.println("isDigit is returning " + retval + " for " + ch);
		return retval;
	}

	public String getLine() {
		StringBuffer retval = new StringBuffer();
		while (ptr < buffer.length() && buffer.charAt(ptr) != '\n') {
			retval.append(buffer.charAt(ptr));
			ptr++;
		}
		if (ptr < buffer.length() && buffer.charAt(ptr) == '\n') {
			retval.append('\n');
			ptr++;
		}
		return retval.toString();
	}

	public String peekLine() {
		int curPos = ptr;
		String retval = this.getLine();
		ptr = curPos;
		return retval;
	}

	public char lookAhead() throws ParseException {
		return lookAhead(0);
	}

	public char lookAhead(int k) throws ParseException {
		// Debug.out.println("ptr = " + ptr);
		if (ptr + k < buffer.length())
			return buffer.charAt(ptr + k);
		else
			return '\0';
	}

	public char getNextChar() throws ParseException {
		if (ptr >= buffer.length())
			throw new ParseException(
				buffer + " getNextChar: End of buffer",
				ptr);
		else
			return buffer.charAt(ptr++);
	}

	public void consume() {
		ptr = savedPtr;
	}

	public void consume(int k) {
		ptr += k;
	}

	/** Get a Vector of the buffer tokenized by lines
	 */
	public Vector getLines() {
		Vector result = new Vector();
		while (hasMoreChars()) {
			String line = getLine();
			result.addElement(line);
		}
		return result;
	}

	/** Get the next token from the buffer.
	*/
	public String getNextToken(char delim) throws ParseException {
		StringBuffer retval = new StringBuffer();
		while (true) {
			char la = lookAhead(0);
			
			//System.out.println("la = " + la);
			
			if (la == delim)
				break;
			else if (la == '\0')
				throw new ParseException("EOL reached", 0);
			retval.append(buffer.charAt(ptr));
			consume(1);
		}
		return retval.toString();
	}

	/** get the SDP field name of the line
	 *  @return String
	 */
	public static String getSDPFieldName(String line) {
		if (line == null)
			return null;
		String fieldName = null;
		try {
			int begin = line.indexOf("=");
			fieldName = line.substring(0, begin);
		} catch (IndexOutOfBoundsException e) {
			return null;
		}
		return fieldName;
	}

}

/*
 * $Log: not supported by cvs2svn $
 * Revision 1.4  2004/01/22 13:26:27  sverker
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
