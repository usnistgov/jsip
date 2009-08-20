package gov.nist.javax.sip.message;

import java.util.Iterator;
import java.util.List;

import javax.sip.header.ContentTypeHeader;

public interface MultipartMimeContent {

    public abstract boolean add(Content content);

    /**
     * Return the Content type header to assign to the outgoing sip meassage.
     * 
     * @return
     */
    public abstract ContentTypeHeader getContentTypeHeader();

    public abstract String toString();

    /**
     * Set the content by its type.
     * 
     * @param content
     */
    public abstract void addContent( Content content);
    
    /**
     * Retrieve the list of Content that is part of this MultitypeMime content.
     * 
     * @return - the content list. Returns an empty list if no content list present.
     */
    public Iterator<Content> getContents();

}
