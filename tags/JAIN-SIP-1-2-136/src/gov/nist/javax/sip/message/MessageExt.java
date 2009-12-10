package gov.nist.javax.sip.message;

import java.text.ParseException;

import javax.sip.message.Message;

/**
 *
 * @author jean.deruelle@gmail.com
 *
 */
public interface MessageExt extends Message {

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
    
    /**
     * Get the multipart mime content from a message. Builds a wrapper around the
     * content and breaks it into multiple sections. Returns these sections as
     * a multipart mime content list. If the content type is not multipart mime
     * then the list will have a single element in it. 
     * 
     * @since v2.0
     * @param Message message
     * @throws ParseException if the content type is multipart mime but the content
     *  is not properly encoded.
     *  
     */
    public MultipartMimeContent getMultipartMimeContent() throws ParseException;

}
