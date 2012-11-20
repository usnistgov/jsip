package gov.nist.javax.sip.message;

import java.util.Iterator;

import javax.sip.header.ContentDispositionHeader;
import javax.sip.header.ContentTypeHeader;
import javax.sip.header.Header;

public interface Content {

  public abstract void setContent(Object content);

  public abstract Object getContent();

  public abstract ContentTypeHeader getContentTypeHeader();

  public abstract ContentDispositionHeader getContentDispositionHeader();

  public abstract Iterator<Header> getExtensionHeaders();
  
  /**
   * The default packing method. This packs the content to be appended to the
   * sip message.
   * 
   */
  public abstract String toString();


}
