/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/
package gov.nist.javax.sip.header;
import gov.nist.javax.sip.*;
import java.util.*;
import javax.sip.header.*;


/**
* Date Header.
*
*@version  JAIN-SIP-1.1
*
*@author M. Ranganathan <mranga@nist.gov>  <br/>
*@author Olivier Deruelle <deruelle@nist.gov><br/>
*<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
*
*/
public class SIPDateHeader extends SIPHeader
implements DateHeader{
    
        /** date field
         */    
	protected SIPDate date;

        /** Default constructor.
         */        
	public SIPDateHeader() { 
            super(DATE);
        }

        /** Encode the header into a String.
         * @return String
         */        
	public String encodeBody() {
		return date.encode();
	}
        
     

		/**
         * Set the date member
         * @param d SIPDate to set
         */
	public void setDate(SIPDate d) {
            date = d ;
            
    } 
        
    /**
     * Sets date of DateHeader. The date is repesented by the Calendar object.
     *
     * @param date the Calendar object date of this header.
     */
    public void setDate(Calendar dat) {
    	if (dat!=null)
	    	date=new SIPDate(dat.getTime().getTime() );
    }


    /**
     * Gets the date of DateHeader. The date is repesented by the Calender
     * object.
     *
     * @return the Calendar object representing the date of DateHeader
     */
    public Calendar getDate() {
    	if (date==null) return null;
    	return date.getJavaCal();
    }
   
}
