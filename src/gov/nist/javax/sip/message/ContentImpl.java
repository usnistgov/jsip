package gov.nist.javax.sip.message;

import java.text.ParseException;

import javax.sip.header.ContentTypeHeader;

public class ContentImpl implements Content {
   
   
    /*
     * The content type header for this chunk of content.
     */
    private ContentTypeHeader contentTypeHeader;

    private Object content;

    private String boundary;

    

    public ContentImpl(ContentTypeHeader ctHeader, String content, String boundary ) {
        this.content = content;
        this.contentTypeHeader = ctHeader;
        this.boundary = boundary;
    }

    

    /* (non-Javadoc)
     * @see gov.nist.javax.sip.message.ContentExt#setContent(java.lang.String)
     */
    public void setContent(Object content) {
        this.content = content;
    }

    /* (non-Javadoc)
     * @see gov.nist.javax.sip.message.ContentExt#getContentTypeHeader()
     */
    public ContentTypeHeader getContentTypeHeader() {
        return contentTypeHeader;
    }

    /*
     * (non-Javadoc)
     * @see gov.nist.javax.sip.message.Content#getContent()
     */
    public Object getContent() {
        return this.content;
    }
    

    /* (non-Javadoc)
     * @see gov.nist.javax.sip.message.ContentExt#toString()
     */
    public String toString() {
        // This is not part of a multipart message.
        if (boundary == null) {
            return content.toString();
        } else {
            return "--" + boundary + "\r\n" + contentTypeHeader + "\r\n" + content.toString().trim()+"\r\n";
        }
    }


}
