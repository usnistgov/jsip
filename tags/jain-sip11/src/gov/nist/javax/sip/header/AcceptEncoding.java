/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/
package gov.nist.javax.sip.header;
import javax.sip.InvalidArgumentException;
import javax.sip.header.*;
import java.text.ParseException;
import gov.nist.core.*;
/**
* Accept-Encoding SIP (HTTP) Header.
*
*@author M. Ranganathan <mranga@nist.gov> 
*@author Olivier Deruelle <deruelle@nist.gov><br/>
*<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
*
* <pre>
* From HTTP RFC 2616
*
* 
*   The Accept-Encoding request-header field is similar to Accept, but
*   restricts the content-codings (section 3.5) that are acceptable in
*   the response.
*
* 
*       Accept-Encoding  = "Accept-Encoding" ":"
* 
* 
*                          1#( codings [ ";" "q" "=" qvalue ] )
*       codings          = ( content-coding | "*" )
* 
*   Examples of its use are:
* 
*       Accept-Encoding: compress, gzip
*       Accept-Encoding:
*       Accept-Encoding: *
*       Accept-Encoding: compress;q=0.5, gzip;q=1.0
*       Accept-Encoding: gzip;q=1.0, identity; q=0.5, *;q=0
* </pre>
* 
*/
public class AcceptEncoding extends ParametersHeader
implements AcceptEncodingHeader {
    
	
        /** contentEncoding field
         */        
        protected String contentCoding;
	
        /** default constructor
         */        
        public AcceptEncoding() {
		super(NAME);
	}
	

	/** Encode the value of this header.
	*@return the value of this header encoded into a string.
	*/
	protected String encodeBody() {
		StringBuffer encoding = new StringBuffer();
		if (contentCoding != null) {
		   encoding.append(contentCoding);
		 }
		if (parameters != null && ! parameters.isEmpty() ) {
			encoding.append(SEMICOLON).append(parameters.encode());
		}
		return encoding.toString();
	}        
        
        /** get QValue field
         * @return float
         */        
        public float getQValue() { 
	   return  getParameterAsFloat("q");
        }
	
        /** get ContentEncoding field
         * @return String
         */        
        public String getEncoding() { 
            return contentCoding;
        }
        
	/**
         * Set the qvalue member
         * @param q double to set
         */
	public	 void setQValue(float q) throws InvalidArgumentException{ 
		if (q < 0.0 || q > 1.0) 
			throw new InvalidArgumentException
				("qvalue out of range!");
		super.setParameter("q",q);
        }
        
    /**
     * Sets the encoding of an EncodingHeader.
     *
     * @param encoding - the new string value defining the encoding.
     * @throws ParseException which signals that an error has been reached
     * unexpectedly while parsing the encoding value.
     */

    public void setEncoding(String encoding) throws ParseException {
         if (encoding==null) 
	throw new  NullPointerException( " encoding parameter is null"); 
         contentCoding=encoding;
    }
        
}
