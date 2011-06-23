/*
 * SdpException.java
 *
 * Created on December 18, 2001, 11:08 AM
 */

package javax.sdp;

import gov.nist.core.Separators;

/** The SdpException defines a general exception for the SDP classes to throw when they encounter a difficulty.
 *
 * @author deruelle
 * @version 1.0
 */
public class SdpException extends Exception {

   
    /** Creates new SdpException
     */
    public SdpException() {
         super();
    }

    /** Constructs a new SdpException with the message you specify.
     * @param message a String specifying the text of the exception message
     */    
    public SdpException(String message){
         super(message);
    }
    
    /** Constructs a new SdpException when the Codelet needs to throw an 
     * exception and include a message about another exception that interfered
     * with its normal operation.
     * @param message a String specifying the text of the exception message
     * @param rootCause the Throwable exception that interfered with the 
     * Codelet's normal operation, making this Codelet exception necessary
     */    
    public SdpException(String message,
    Throwable rootCause){
        super(rootCause.getMessage()+ Separators.SEMICOLON +message);
    }
    
    /** Constructs a new SdpException as a result of a system exception and uses
     * the localized system exception message.
     * @param rootCause the system exception that makes this SdpException necessary
     */    
    public SdpException(Throwable rootCause){
        super(rootCause.getLocalizedMessage());
    }
    
    /** Returns the Throwable system exception that makes this SdpException necessary.
     * @return Throwable
     */    
    public Throwable getRootCause(){
        return fillInStackTrace(); 
    }
    
}
