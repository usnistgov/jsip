/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/
package gov.nist.javax.sip.header;
import javax.sip.header.*;
import java.text.ParseException;


/**
* Call ID SIPHeader.
*
*@author M. Ranganathan <mranga@nist.gov>  <br/>
*
*@version JAIN-SIP-1.1
*
*<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
*
*
*/

public class CallID extends SIPHeader implements javax.sip.header.CallIdHeader {
    
        /** callIdentifier field
         */    
	protected CallIdentifier callIdentifier;

        /** Default constructor
         */        
	public CallID () { 
            super(NAME);
        }
        
        /**
         * Compare two call ids for equality.
         * @param other Object to set
         * @return true if the two call ids are equals, false otherwise
         */
        public boolean equals(Object other) {
            if (! this.getClass().equals(other.getClass())) {
                return false;
            }
            CallID that = (CallID) other;
            return this.callIdentifier.equals(that.callIdentifier);    
        }
	
        
	/** Encode the body part of this header (i.e. leave out the hdrName).
	*@return String encoded body part of the header.
	*/
	public String encodeBody() {
		if (callIdentifier == null) return null;
		else return callIdentifier.encode();
	}
        
        /** get the CallId field. This does the same thing as
	 * encodeBody 
         * @return String the encoded body part of the 
         */        
	public String getCallId() { 
		return encodeBody();
	}

	/**
         * get the call Identifer member.
         * @return CallIdentifier
         */
	public CallIdentifier getCallIdentifer() {
            return callIdentifier;
        }
        
        /** set the CallId field
         * @param cid String to set. This is the body part of the Call-Id
	  *  header. It must have the form localId@host or localId.
         * @throws IllegalArgumentException if cid is null, not a token, or is 
         * not a token@token.
         */        
	public void setCallId( String cid ) throws ParseException  {
	    try {
		callIdentifier = new CallIdentifier(cid);
	    } catch (IllegalArgumentException ex) {
		throw new ParseException(cid,0);
	    }
	}

	/**
         * Set the callIdentifier member.
         * @param cid CallIdentifier to set (localId@host).
         */
	public void setCallIdentifier( CallIdentifier cid ) {
            callIdentifier = cid;
        }

	/** Constructor given the call Identifier.
	*@param callId string call identifier (should be localid@host)
	*@throws IllegalArgumentException if call identifier is bad.
	*/
	public CallID(String callId) throws IllegalArgumentException {
		super(NAME);
		this.callIdentifier = new CallIdentifier(callId);
	}

}
