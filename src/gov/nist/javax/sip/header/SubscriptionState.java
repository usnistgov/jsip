/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/

package gov.nist.javax.sip.header;

import javax.sip.InvalidArgumentException;
import java.text.ParseException;
import javax.sip.header.*;
/**  
*SubscriptionState header
*
*@version  JAIN-SIP-1.1
*
*@author Olivier Deruelle <deruelle@nist.gov><br/>
*<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
*
*/
public class SubscriptionState extends ParametersHeader 
implements SubscriptionStateHeader{
    
    protected int expires;
    protected int retryAfter;
    protected String reasonCode;
    protected String state;
    
    /** Creates a new instance of SubscriptionState */
    public SubscriptionState() {
        super(SIPHeaderNames.SUBSCRIPTION_STATE);
        expires=-1;
        retryAfter=-1;
    }
    
      /**
     * Sets the relative expires value of the SubscriptionStateHeader. The 
     * expires value MUST be greater than zero and MUST be less than 2**31.
     *
     * @param expires - the new expires value of this SubscriptionStateHeader.
     * @throws InvalidArgumentException if supplied value is less than zero.
     */
    public void setExpires(int expires) throws InvalidArgumentException {
        if (expires<=0) throw new InvalidArgumentException("JAIN-SIP "+
        "Exception, SubscriptionState, setExpires(), the expires parameter is <=0");
        this.expires=expires;
    }

    /**
     * Gets the expires value of the SubscriptionStateHeader. This expires value is
     * relative time.
     *
     * @return the expires value of the SubscriptionStateHeader.
     */
    public int getExpires() {
         return expires;
    }
    
    /**
     * Sets the retry after value of the SubscriptionStateHeader. The retry after value
     * MUST be greater than zero and MUST be less than 2**31.
     *
     * @param retryAfter - the new retry after value of this SubscriptionStateHeader
     * @throws InvalidArgumentException if supplied value is less than zero.
     */
    public void setRetryAfter(int retryAfter) throws InvalidArgumentException {
        if (retryAfter<=0) throw new InvalidArgumentException("JAIN-SIP "+
        "Exception, SubscriptionState, setRetryAfter(), the retryAfter parameter is <=0");
        this.retryAfter=retryAfter;
    }

    /**
     * Gets the retry after value of the SubscriptionStateHeader. This retry after
     * value is relative time.
     *
     * @return the retry after value of the SubscriptionStateHeader.
     */
    public int getRetryAfter() {
        return retryAfter;
    }

    /**
     * Gets the reason code of SubscriptionStateHeader.
     *
     * @return the comment of this SubscriptionStateHeader, return null if no reason code
     * is available.
     */
    public String getReasonCode() {
        return reasonCode;
    }

    /**
     * Sets the reason code value of the SubscriptionStateHeader.
     *
     * @param reasonCode - the new reason code string value of the SubscriptionStateHeader.
     * @throws ParseException which signals that an error has been reached
     * unexpectedly while parsing the reason code.
     */
    public void setReasonCode(String reasonCode) throws ParseException {
        if (reasonCode==null) throw new  NullPointerException("JAIN-SIP "+
        "Exception, SubscriptionState, setReasonCode(), the reasonCode parameter is null");
        this.reasonCode=reasonCode;
    }
    
    /**
     * Gets the state of SubscriptionStateHeader.
     *
     * @return the state of this SubscriptionStateHeader.
     */
    public String getState() {
        return state;
    }

    /**
     * Sets the state value of the SubscriptionStateHeader.
     *
     * @param state - the new state string value of the SubscriptionStateHeader.
     * @throws ParseException which signals that an error has been reached
     * unexpectedly while parsing the state.
     */
    public void setState(String state) throws ParseException{
        if (state==null) throw new  NullPointerException("JAIN-SIP "+
        "Exception, SubscriptionState, setState(), the state parameter is null");   
        this.state=state;
    }
    
    
    /** Just the encoded body of the header.
     * @return the string encoded header body.
     */
    public String encodeBody() {
        String res="";
        if (state!=null) res+=state;
        if (reasonCode!=null) res+=";reason="+reasonCode;
	if (expires!=-1) res+=";expires="+expires;
        if (retryAfter!=-1) res+=";retry-after="+retryAfter;
        
        if (!parameters.isEmpty()) {
            res+=SEMICOLON+parameters.encode();
        }
        return res;
    }
        
    
}
