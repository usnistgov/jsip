/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/
package gov.nist.javax.sip;
import gov.nist.core.*;
import java.util.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
* A few utilities that are used in various places by the stack.
* This is used to convert byte arrays to hex strings etc. Generate
* tags and branch identifiers and odds and ends.
*/
public class Utils 
{
     /**
     * to hex converter
     */
    private static final char[] toHex = { '0', '1', '2', '3', '4', '5', '6',
                                          '7', '8', '9', 'a', 'b', 'c', 'd',
                                          'e', 'f' };
 
    /**
     * convert an array of bytes to an hexadecimal string
     * @return a string
     * @param b bytes array to convert to a hexadecimal
     * string
     */
 
    public static String toHexString(byte b[]) {
        int pos = 0;
        char[] c = new char[b.length*2];
        for (int i=0; i< b.length; i++) {
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
    public static String getQuotedString ( String str) {
	return  '"' + str + '"';
    }
    

    /**
    * Squeeze out all white space from a string and return the reduced string.
    *
    * @param input input string to sqeeze.
    * @return String a reduced string.	
    */
    protected static String reduceString( String input) {
	    String newString = input.toLowerCase();
	    int len = newString.length();
	    String retval = "";
	    for (int i = 0; i < len ; i++ ) { 
                if ( newString.charAt(i) == ' ' 
			|| newString.charAt(i) == '\t' ) 
		  	continue;
		  else retval += newString.charAt(i);
     	    }
	    return retval;
	}			  

        /** Generate a call  identifier. This is useful when we want
         * to generate a call identifier in advance of generating a message.
         */
        public static String  generateCallIdentifier(String address) {
            String date = (new Date()).toString() +
            new Double(Math.random()).toString();
            try {
                MessageDigest messageDigest = MessageDigest.getInstance("MD5");
                byte cid[] = messageDigest.digest(date.getBytes());
                String cidString = Utils.toHexString(cid);
                return cidString + "@" + address;
            } catch ( NoSuchAlgorithmException ex ) {
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
            return new Integer((int)(Math.random() * 10000)).toString();
	}

	/** Generate a cryptographically random identifier that can be used
	* to generate a branch identifier.
	*
	*@return a cryptographically random gloablly unique string that
	*	can be used as a branch identifier.
	*/
	public static String generateBranchId() {
          String b =  new Integer((int)(Math.random() * 10000)).toString() + 
		System.currentTimeMillis();
          try {
              MessageDigest messageDigest = MessageDigest.getInstance("MD5");
              byte bid[] = messageDigest.digest(b.getBytes());
		// cryptographically random string.
		// prepend with a magic cookie to indicate we
		// are bis09 compatible.
              return 	SIPConstants.BRANCH_MAGIC_COOKIE +
			Utils.toHexString(bid);
           } catch ( NoSuchAlgorithmException ex ) {
	      return null;
           }
	}
    

}
