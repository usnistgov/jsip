/*
 * Reason.java
 * Reason            =  "Reason" HCOLON reason-value *(COMMA reason-value)
 * reason-value      =  protocol *(SEMI reason-params)
 * protocol          =  "SIP" / "Q.850" / token
 * reason-params     =  protocol-cause / reason-text
 *                     / reason-extension
 * protocol-cause    =  "cause" EQUAL cause
 * cause             =  1*DIGIT
 * reason-text       =  "text" EQUAL quoted-string
 * reason-extension  =  generic-param
 */

package gov.nist.javax.sip.header;

import java.text.ParseException;
import javax.sip.*;
import gov.nist.core.*;



/** 
*Definition of the Reason SIP Header.
*
*@version  JAIN-SIP-1.1
*
*@author M. Ranganathan <mranga@nist.gov>  <br/>
*
*<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
*
 */
public class Reason extends ParametersHeader
    implements javax.sip.header.ReasonHeader {
        
        public final String TEXT = ParameterNames.TEXT;
	public final String CAUSE = ParameterNames.CAUSE;
        
        protected String protocol;
        
     /** Get the cause token.
      *@return the cause code.
      */
     public int getCause() {
	return	getParameterAsInt(CAUSE);
     }
     
     
     /**
      * Set the cause.
      *
      *@param cause - cause to set.
      */
     public void setCause( int cause ) 
        throws javax.sip.InvalidArgumentException {
         this.parameters.set( new NameValue("cause", new Integer(cause)));
     }
     
     
     /** Set the protocol
      *
      *@param protocol - protocol to set.
      */
     
     public void setProtocol(String protocol) throws ParseException {
         this.protocol = protocol;
     }
     
     
 
     /** Return the protocol.
      *
      *@return the protocol.
      */
     public String getProtocol() {
            return this.protocol;
     }
     
     /** Set the text.
      *
      *@param text -- string text to set.
      */
     public void setText(String text ) throws ParseException {
         this.parameters.set("text",text);
     }
     
     /** Get the text.
      *
      *@return text parameter.
      *
      */
     public String getText() {
         return this.parameters.getParameter("text");
     }
     
    
     /** Set the cause.
    
    /** Creates a new instance of Reason */
    public Reason() {
        super (NAME);
    }
    
    /** Gets the unique string name of this Header. A name constant is defined in
     * each individual Header identifying each Header.
     *
     * @return the name of this specific Header
     */
    public String getName() {
        return NAME;
        
    }
    
    /** Encode the body of this header (the stuff that follows headerName).
     * A.K.A headerValue.
     */
    protected String encodeBody() {
        StringBuffer s=new StringBuffer();
        s.append(protocol);
        if (parameters!=null && !parameters.isEmpty())
                s.append(SEMICOLON).
                append(parameters.encode());
        return s.toString();
    }
    
}
