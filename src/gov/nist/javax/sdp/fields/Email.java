/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/
package gov.nist.javax.sdp.fields;
import gov.nist.core.*;
import javax.sdp.*;
/** Email address record.
*@version  JSR141-PUBLIC-REVIEW (subject to change).
*
*@author Olivier Deruelle <deruelle@antd.nist.gov>
*@author M. Ranganathan <mranga@nist.gov>  <br/>
*
*<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
*/
public class Email extends SDPObject {
	protected String userName;
	protected String hostName;
	public String getUserName() { return userName; }
	public String getHostName() { return hostName; }
	/**
	* Set the userName member  
	*/
	public	 void setUserName(String u) 
 	 	{ userName = u ; } 
	/**
	* Set the hostName member  
	*/
	public	 void setHostName(String h) 
 	 	{ hostName = h.trim() ; } 

      /**
       *  Get the string encoded version of this object
       * @since v1.0
       */
       public String encode() {
	return userName + Separators.AT + hostName;
      }
	

}
