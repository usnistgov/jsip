/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/
package gov.nist.javax.sip.header;
import javax.sip.header.*;
import javax.sip.InvalidArgumentException;
/**
* MaxForwards SIPHeader
*
*@version  JAIN-SIP-1.1
*
*@author M. Ranganathan <mranga@nist.gov>  <br/>
*@author Olivier Deruelle <deruelle@nist.gov><br/>
*<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
*
*/
public  class MaxForwards extends SIPHeader
implements MaxForwardsHeader {
    
        /** maxForwards field.
         */    
	protected int maxForwards;
        
        /** Default constructor.
         */        
	public MaxForwards() {
            super(NAME);
        }
        
        /** get the MaxForwards field.
         * @return the maxForwards member.
         */        
	public int getMaxForwards() {
            return maxForwards;
        }
	
	/**
         * Set the maxForwards member
         * @param maxForwards maxForwards parameter to set
         */
	public void setMaxForwards(int maxForwards)  
	  throws InvalidArgumentException{
            if (maxForwards<0 || maxForwards>255) 
	    throw new InvalidArgumentException
	    ("bad max forwards value " + maxForwards);
            this.maxForwards= maxForwards ;
        }
        
	/**
         * Encode into a string.
         * @return encoded string.
         *
         */	
         public String encodeBody() {
		return new Integer(maxForwards).toString() ;
	}
          
        /** Boolean function
         * @return true if MaxForwards field reached zero.
         */        
        public boolean hasReachedZero() {
            return maxForwards==0;
        }
        
        /** decrement MaxForwards field one by one.
         */        
        public void decrementMaxForwards()  {
	    if (maxForwards >= 0) maxForwards--;
        }
	
        
}
