/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/

package gov.nist.javax.sip.header;


import java.text.ParseException;
import javax.sip.header.*;

/**  
*Organization SIP Header.
*
*@version  JAIN-SIP-1.1
*
*@author M. Ranganathan <mranga@nist.gov>  <br/>
*@author Olivier Deruelle <deruelle@nist.gov><br/>
*
*<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
*
*/
public class Organization extends SIPHeader
implements OrganizationHeader {
    
        /** organization field
         */    
	protected String organization;

        /**
         * Return encoding of value of the header.
         * @return String
         */       
	public String encodeBody() {
		return organization ;
	}

        /** Default constructor
         */        
	public Organization() { 
		super(ORGANIZATION); 
	}
        
        /** get the organization field.
         * @return String
         */        
	public String getOrganization() {
		return organization;
	}
        
	/**
         * Set the organization member
         * @param o String to set
         */
	public void setOrganization(String o) throws ParseException{
            if (o==null) throw new  NullPointerException("JAIN-SIP Exception,"+
            " Organization, setOrganization(), the organization parameter is null");
            organization = o ;
        }
        
}
