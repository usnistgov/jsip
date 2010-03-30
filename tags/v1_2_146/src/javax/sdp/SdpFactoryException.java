/*
 * SdpFactoryException.java
 *
 * Created on January 9, 2002, 6:44 PM
 */

package javax.sdp;

/** The SdpFactoryException encapsulates the information thrown when a problem 
 * with configuration with the
 * SdpFactory exists.
 *
 * Please refer to IETF RFC 2327 for a description of SDP.
 *
 * @author deruelle
 * @version 1.0
 */
public class SdpFactoryException extends  SdpException {

    /** Chained exception.
     */    
    protected Exception ex;
    
    /** Creates new SdpFactoryException */
    public SdpFactoryException() {
        super();
    }

    /** Create a new FactoryConfigurationException with the String specified as 
     * an error message.
     * @param msg msg - the detail message
     */    
    public SdpFactoryException(String msg){
         super(msg);
    }
    
    /** Create a new FactoryConfigurationException with a given Exception base
     * cause of the error.
     * @param ex ex - the "chained" exception
     */    
    public SdpFactoryException(Exception ex){
       super(ex.getMessage());
       this.ex=ex;
    }
    
    /** Create a new FactoryConfigurationException with the given Exception base 
     * cause and detail message.
     * @param msg msg - the detail message
     * @param ex ex - the "chained" exception
     */    
    public SdpFactoryException(String msg,
    Exception ex){
         super(msg);
         this.ex=ex;
    }
    
    /** Return the message (if any) for this error. If there is no message for 
     * the exception and there is an encapsulated
     * exception then the message of that exception will be returned.
     * @return the error message
     */    
    public String getMessage(){
        if (super.getMessage() !=null) return super.getMessage();
        else 
            if (ex!=null) return ex.getMessage();
            else return null;
    }
    
    /** Return the actual exception (if any) that caused this exception to be thrown.
     * @return the encapsulated exception, or null if there is none
     */    
    public Exception getException(){
        return ex;
    }
}
