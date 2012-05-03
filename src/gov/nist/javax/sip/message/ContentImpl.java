/*
 * Conditions Of Use 
 * 
 * This software was developed by employees of the National Institute of
 * Standards and Technology (NIST), an agency of the Federal Government.
 * Pursuant to title 15 Untied States Code Section 105, works of NIST
 * employees are not subject to copyright protection in the United States
 * and are considered to be in the public domain.  As a result, a formal
 * license is not needed to use the software.
 * 
 * This software is provided by NIST as a service and is expressly
 * provided "AS IS."  NIST MAKES NO WARRANTY OF ANY KIND, EXPRESS, IMPLIED
 * OR STATUTORY, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTY OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, NON-INFRINGEMENT
 * AND DATA ACCURACY.  NIST does not warrant or make any representations
 * regarding the use of the software or the results thereof, including but
 * not limited to the correctness, accuracy, reliability or usefulness of
 * the software.
 * 
 * Permission to use this software is contingent upon your acceptance
 * of the terms of this agreement
 *  
 * .
 * 
 */
/*******************************************************************************
 * Product of NIST/ITL Advanced Networking Technologies Division (ANTD)         *
 *******************************************************************************/
package gov.nist.javax.sip.message;

import gov.nist.core.Separators;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.sip.header.ContentDispositionHeader;
import javax.sip.header.ContentTypeHeader;
import javax.sip.header.Header;

/**
 * Implementation of multipart content type.
 * <b>
 * This is an implementation class.
 * WARNING do not directly use the methods of this class. Instead use the methods of
 * the interface that is implemented by the class.
 * </b>
 */

public class ContentImpl implements Content {

  /*
   * The content type header for this chunk of content.
   */

  private Object content;

  private ContentTypeHeader contentTypeHeader;

  private ContentDispositionHeader contentDispositionHeader;

  private List<Header> extensionHeaders = new ArrayList<Header>();

  public ContentImpl(String content) {
    this.content = content;
  }

  /*
   * (non-Javadoc)
   * 
   * @see gov.nist.javax.sip.message.ContentExt#setContent(java.lang.String)
   */
  public void setContent(Object content) {
    this.content = content;
  }

  /*
   * (non-Javadoc)
   * 
   * @see gov.nist.javax.sip.message.ContentExt#getContentTypeHeader()
   */
  public ContentTypeHeader getContentTypeHeader() {
    return contentTypeHeader;
  }

  /*
   * (non-Javadoc)
   * 
   * @see gov.nist.javax.sip.message.Content#getContent()
   */
  public Object getContent() {
    return this.content;
  }

  /**
   * @see Content#toString()
   */
  public String toString() {
    StringBuilder result = new StringBuilder();
    if (contentTypeHeader != null) {
      result.append(contentTypeHeader.toString());
    }

    if (contentDispositionHeader != null) {
      result.append(contentDispositionHeader.toString());
    }
    for (Header header : extensionHeaders) {
      result.append(header);
    }
    result.append(Separators.NEWLINE);
    result.append(content.toString());
    return result.toString();
  }

  /**
   * @param contentDispositionHeader the contentDispositionHeader to set
   */
  public void setContentDispositionHeader(ContentDispositionHeader contentDispositionHeader) {
    this.contentDispositionHeader = contentDispositionHeader;
  }

  /**
   * @return the contentDispositionHeader
   */
  public ContentDispositionHeader getContentDispositionHeader() {
    return contentDispositionHeader;
  }

  /**
   * @param contentTypeHeader the contentTypeHeader to set
   */
  public void setContentTypeHeader(ContentTypeHeader contentTypeHeader) {
    this.contentTypeHeader = contentTypeHeader;
  }

  public void addExtensionHeader(Header header) {
    this.extensionHeaders.add(header);
  }

  public Iterator<Header> getExtensionHeaders() {
    return extensionHeaders.iterator();
  }
}
