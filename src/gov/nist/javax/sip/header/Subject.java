/*******************************************************************************

* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *

*******************************************************************************/

package gov.nist.javax.sip.header;


import java.text.ParseException;
import javax.sip.header.*;
/**  

*Supported SIP Header.

*

*@version  JAIN-SIP-1.1

*

*@author M. Ranganathan <mranga@nist.gov>  <br/>

*@author Olivier Deruelle <deruelle@nist.gov><br/>

*

*<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>

*

*/
public class Subject extends SIPHeader 
implements SubjectHeader{

        /** subject field
         */    
	protected String subject;
        
        /** Default Constructor.
         */        
	public Subject() {
		super(SUBJECT);
	}
        
        /**
         * Generate the canonical form.
         * @return String.
         */
	public String encodeBody() {
		if (subject != null)  {
		  return subject;
		} else {
		  return  "";
		}
		
	}       
        
 /**

     * Sets the subject value of the SubjectHeader to the supplied string

     * subject value.

     *

     * @param subject - the new subject value of this header

     * @throws ParseException which signals that an error has been reached

     * unexpectedly while parsing the subject value.

     */

    public void setSubject(String subject) throws ParseException {
        if (subject==null) throw new  NullPointerException("JAIN-SIP Exception, "+
        " Subject, setSubject(), the subject parameter is null");
        this.subject=subject;
    }



    /**

     * Gets the subject value of SubjectHeader

     *

     * @return subject of SubjectHeader

     */

    public String getSubject() {
        return subject;
    }
    
}
