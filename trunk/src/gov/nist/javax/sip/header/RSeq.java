
package gov.nist.javax.sip.header;

import javax.sip.header.*;
import javax.sip.*;

/**
*
*@version  JAIN-SIP-1.1
*
*@author M. Ranganathan <mranga@nist.gov>  <br/>
*
*<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
*
*/

public class RSeq extends SIPHeader 
	implements javax.sip.header.RSeqHeader {
    protected int sequenceNumber;
    
    /** Creates a new instance of RSeq */
    public RSeq() {
        super(NAME);
    }
    
  
    
    /** Gets the sequence number of this RSeqHeader.
     *
     * @return the integer value of the Sequence number of the RSeqHeader
     */
    public int getSequenceNumber() {
        return this.sequenceNumber ;
    }
    
    
    
    /** Sets the sequence number value of the RSeqHeader of the provisional
     * response. The sequence number MUST be expressible as a 32-bit unsigned
     * integer and MUST be less than 2**31.
     *
     * @param sequenceNumber - the new Sequence number of this RSeqHeader
     * @throws InvalidArgumentException if supplied value is less than zero.
     */
    public void setSequenceNumber(int sequenceNumber) 
        throws InvalidArgumentException {
            if (sequenceNumber <= 0)
                    throw new InvalidArgumentException
                        ("Bad seq number " + sequenceNumber);
            this.sequenceNumber = sequenceNumber;
    }
    
    /** Encode the body of this header (the stuff that follows headerName).
     * A.K.A headerValue.
     */
    protected String encodeBody() {
        return new Integer(this.sequenceNumber).toString();
    }    
  
    
}
