/*******************************************************************************
 * Product of NIST/ITL Advanced Networking Technologies Division (ANTD).       *
 *******************************************************************************/
package gov.nist.javax.sip;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * A few utilities that are used in various places by the stack.
 * This is used to convert byte arrays to hex strings etc. Generate
 * tags and branch identifiers and odds and ends.
 * 
 * @author mranga
 * @version JAIN-SIP-1.1 $Revision: 1.7 $ $Date: 2005-03-05 03:38:19 $
 */
public class Utils {

    private static MessageDigest digester = null;
    private static java.util.Random rand = new java.util.Random();
    private static long counter = 0;
    
	/**
	* to hex converter
	*/
	private static final char[] toHex =
		{
			'0',
			'1',
			'2',
			'3',
			'4',
			'5',
			'6',
			'7',
			'8',
			'9',
			'a',
			'b',
			'c',
			'd',
			'e',
			'f' };

	/**
	 * convert an array of bytes to an hexadecimal string
	 * @return a string
	 * @param b bytes array to convert to a hexadecimal
	 * string
	 */

	public static String toHexString(byte b[]) {
		int pos = 0;
		char[] c = new char[b.length * 2];
		for (int i = 0; i < b.length; i++) {
			c[pos++] = toHex[(b[i] >> 4) & 0x0F];
			c[pos++] = toHex[b[i] & 0x0f];
		}
		return new String(c);
	}

	/**
	 * Put quotes around a string and return it.
	 *
	 * @return a quoted string
	 * @param str string to be quoted
	 */
	public static String getQuotedString(String str) {
		return '"' + str + '"';
	}

	/**
	* Squeeze out all white space from a string and return the reduced string.
	*
	* @param input input string to sqeeze.
	* @return String a reduced string.	
	*/
	protected static String reduceString(String input) {
		String newString = input.toLowerCase();
		int len = newString.length();
		String retval = "";
		for (int i = 0; i < len; i++) {
			if (newString.charAt(i) == ' ' || newString.charAt(i) == '\t')
				continue;
			else
				retval += newString.charAt(i);
		}
		return retval;
	}

	/** Generate a call  identifier. This is useful when we want
	 * to generate a call identifier in advance of generating a message.
	 */
	public static String generateCallIdentifier(String address) {
		 String date =
		 new Long(System.currentTimeMillis()).toString() 
		 + new Double(Math.random()).toString();
		try {
			MessageDigest messageDigest = MessageDigest.getInstance("MD5");
			byte cid[] = messageDigest.digest(date.getBytes());
			String cidString = Utils.toHexString(cid);
			return cidString + "@" + address;
		} catch (NoSuchAlgorithmException ex) {
			return null;
		}

	}

	/** Generate a tag for a FROM header or TO header. Just return a
	* random 4 digit integer (should be enough to avoid any clashes!)
	* Tags only need to be unique within a call.
	*
	* @return a string that can be used as a tag parameter.
	*/
	public static String generateTag() {
		return new Integer((int) (Math.random() * 10000)).toString();
	}

	/** Generate a cryptographically random identifier that can be used
	* to generate a branch identifier.
	*
	*@return a cryptographically random gloablly unique string that
	*	can be used as a branch identifier.
	*/
    public static String generateBranchId() {		
	try {
	    if(null == digester) digester = MessageDigest.getInstance("MD5");
	    long num = ++counter * rand.nextLong() * System.currentTimeMillis();	    
	    byte bid[] = digester.digest( Long.toString(num).getBytes() );
	    // prepend with a magic cookie to indicate we are bis09 compatible.
	    return SIPConstants.BRANCH_MAGIC_COOKIE + Utils.toHexString(bid);
	} catch (NoSuchAlgorithmException ex) {
	    return null;
	}
    }
}
/*
 * $Log: not supported by cvs2svn $
 * Revision 1.6  2004/07/23 06:50:04  mranga
 * Submitted by:  mranga
 * Reviewed by:   mranga
 *
 * Clean up - Get rid of annoying eclipse warnings.
 *
 * Revision 1.5  2004/03/07 22:25:22  mranga
 * Reviewed by:   mranga
 * Added a new configuration parameter that instructs the stack to
 * drop a server connection after server transaction termination
 * set gov.nist.javax.sip.CACHE_SERVER_CONNECTIONS=false for this
 * Default behavior is true.
 *
 * Revision 1.4  2004/01/22 13:26:28  sverker
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