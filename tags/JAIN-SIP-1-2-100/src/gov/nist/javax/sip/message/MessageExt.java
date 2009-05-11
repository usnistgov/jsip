package gov.nist.javax.sip.message;

/**
 * 
 * @author jean.deruelle@gmail.com
 *
 */
public interface MessageExt {

	 /**
     * This method allows applications to associate application context with 
     * the message. This specification does not define the format of this 
     * data, this the responsibility of the application and is dependent 
     * on the application.  
     * this application data is un-interpreted by the stack.
     * Beware : when you clone a message, the deepcopy does not apply to the application data 
     * (instead, we would just make a copy of the pointer).
     * 
     * @param applicationData - un-interpreted application data.
     * @since v2.0
     *
     */
    
    public void setApplicationData (Object applicationData);
    
    
    /**
     * Returns the application data associated with the transaction.This
     * specification does not define the format of this application specific
     * data. This is the responsibility of the application. 
     * 
     * @return application data associated with the message by the application.
     * @since v2.0
     *
     */
    public Object getApplicationData();
}
