/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/
package gov.nist.javax.sdp.fields;
import gov.nist.core.*;

/**
* Placeholder root class for SDP headers.
*
*@version  JAIN-SIP-1.1
*
*@author M. Ranganathan <mranga@nist.gov>  <br/>
*
*<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
*
*/

public abstract class SDPField extends SDPObject  {
	protected String fieldName;

	public abstract String encode();

	protected SDPField( String hname ) {
		fieldName = hname;
	}

	public String getFieldName() { return fieldName; }

       /** Returns the type character for the field.
       * @return the type character for the field.
       */
       public char getTypeChar() {
	if (fieldName == null) return  '\0';
	else return fieldName.charAt(0);
      }

	public SDPField() {}

	public String toString() { return this.encode(); }

} 
