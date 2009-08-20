package gov.nist.javax.sip.message;

import javax.sip.header.ContentTypeHeader;

public interface Content {

    public abstract void setContent(Object content);
    
    public abstract Object getContent();
 
    public abstract ContentTypeHeader getContentTypeHeader();

    /**
     * The default packing method. This packs the content to be appended to the
     * sip message.
     * 
     */
    public abstract String toString();

 
}
