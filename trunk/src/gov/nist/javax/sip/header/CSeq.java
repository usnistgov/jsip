/******************************************************************************
 * Product of NIST/ITL Advanced Networking Technologies Division (ANTD).      *
 ******************************************************************************/
package gov.nist.javax.sip.header;
import javax.sip.InvalidArgumentException;
import java.text.ParseException;
/**
 *  CSeq SIP Header.
 *
 *@author M. Ranganathan <mranga@nist.gov>  NIST/ITL/ANTD <br/>
 *
 *<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 *@version JAIN-SIP-1.1
 *
 */

public  class CSeq extends 
    SIPHeader  implements javax.sip.header.CSeqHeader{
    
    /** seqno field
     */
    protected Integer seqno;
    
    /** method field
     */
    protected String method;
    
    /**
     *Constructor.
     */
    public CSeq() {
        super(CSEQ);
    }
    
    /**
     * Constructor given the sequence number and method.
     *
     *@param seqno is the sequence number to assign.
     *@param method is the method string.
     */
    public CSeq(  int seqno, String method) {
        this();
        this.seqno = new Integer(seqno);
        this.method = method;
    }
    
    /**
     * Compare two cseq headers for equality.
     * @param other Object to compare against.
     * @return true if the two cseq headers are equals, false
     * otherwise.
     */
    public boolean equals( Object other) {
	try {
        CSeq that = (CSeq) other;
        if (! this.seqno.equals(that.seqno)) {
            return false;
        }
        if (this.method.compareToIgnoreCase(that.method) != 0) {
            return false;
        }
        return true;
	} catch (ClassCastException ex) {
		return false;
	}
    }
    
    /**
     * Return canonical encoded header.
     * @return String with canonical encoded header.
     */
    public String encode() {
        return headerName + COLON + SP + encodeBody() + NEWLINE;
    }
    
    /**
     * Return canonical header content. (encoded header except headerName:)
     *
     * @return encoded string.
     */
    public String encodeBody() {
        return seqno + SP + method.toUpperCase();
    }
    

    
    /**
     * Get the method.
     * @return String the method.
     */
    public String getMethod() {
        return method ;
    }
    
    
    /** Sets the sequence number of this CSeqHeader. The sequence number
     * MUST be expressible as a 32-bit unsigned integer and MUST be less than
     * 2**31.
     *
     * @param sequenceNumber - the sequence number to set.
     * @throws InvalidArgumentException -- if the seq number is <= 0
     */
    public	 void setSequenceNumber(int sequenceNumber) 
        throws InvalidArgumentException{
        if (sequenceNumber< 0)	throw new 
		InvalidArgumentException
	     ("JAIN-SIP Exception, CSeq, setSequenceNumber(), "+
		"the sequence number parameter is < 0");
        seqno = new Integer(sequenceNumber) ;
    }
    
    /**
     * Set the method member
     *
     * @param meth -- String to set
     */
    public	 void setMethod(String meth) throws ParseException{
    	if (meth==null) throw new  
	NullPointerException("JAIN-SIP Exception, CSeq"+
    	", setMethod(), the meth parameter is null");
        method = meth ;
    }
    
    /** Gets the sequence number of this CSeqHeader.
     *
     * @return sequence number of the CSeqHeader 
     */
    
    public int getSequenceNumber() {
        if (this.seqno == null) return 0;
        else return this.seqno.intValue();
    }    
    
}
