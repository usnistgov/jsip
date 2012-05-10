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
import gov.nist.javax.sip.header.HeaderFactoryExt;
import gov.nist.javax.sip.header.HeaderFactoryImpl;

import java.text.ParseException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import javax.sip.header.ContentDispositionHeader;
import javax.sip.header.ContentTypeHeader;
import javax.sip.header.Header;

/**
 * Content list for multipart mime content type.
 * <b>
 * WARNING -- do not directly cast to this this class.
 * Use the methods of the interface that it implements.
 * </b>
 * 
 * @author M. Ranganathan
 * 
 */
public class MultipartMimeContentImpl implements MultipartMimeContent {
  public static final String BOUNDARY = "boundary";
  private List<Content> contentList = new LinkedList<Content>();
  private HeaderFactoryExt headerFactory = new HeaderFactoryImpl();
  private ContentTypeHeader multipartMimeContentTypeHeader;
  private String boundary;

  /**
   * Creates a default content list.
   */
  public MultipartMimeContentImpl(ContentTypeHeader contentTypeHeader) {
    this.multipartMimeContentTypeHeader = contentTypeHeader;
    this.boundary = contentTypeHeader.getParameter(BOUNDARY);

  }

  /*
   * (non-Javadoc)
   * 
   * @see gov.nist.javax.sip.message.MultipartMimeContentExt#add(gov.nist.javax.sip.message.Content)
   */
  public boolean add(Content content) {
    return contentList.add((ContentImpl) content);
  }

  /*
   * (non-Javadoc)
   * 
   * @see gov.nist.javax.sip.message.MultipartMimeContentExt#getContentTypeHeader()
   */
  public ContentTypeHeader getContentTypeHeader() {
    return multipartMimeContentTypeHeader;
  }

  /*
   * (non-Javadoc)
   * 
   * @see gov.nist.javax.sip.message.MultipartMimeContentExt#toString()
   */
  @Override
  public String toString() {
    StringBuilder result = new StringBuilder();

    for (Content content : this.contentList) {
      result.append("--" + boundary + Separators.NEWLINE);
      result.append(content.toString());
      result.append(Separators.NEWLINE);
    }

    if (!contentList.isEmpty()) {
      result.append("--" + boundary + "--");
    }

    return result.toString();

  }

  /**
   * unpack a multipart mime packet and set a list of content packets.
   * 
   * @return -- an iterator of Content blocks.
   * 
   */
  public void createContentList(String body) throws ParseException {
    if (boundary != null) {
      Scanner scanner = new Scanner(body);
      // scanner.useDelimiter("--" + boundary + "(--)?\r?\n?");
      scanner.useDelimiter("\r?\n?--" + boundary + "(--)?\r?\n?");
      while (scanner.hasNext()) {
        String bodyPart = scanner.next();
        Content partContent = parseBodyPart(bodyPart);
        contentList.add(partContent);
      }
    } else {
      // No boundary had been set, we will consider the body as a single part
      ContentImpl content = parseBodyPart(body);
      content.setContentTypeHeader(this.getContentTypeHeader());
      this.contentList.add(content);
    }
  }

  private ContentImpl parseBodyPart(String bodyPart) throws ParseException {
    String[] nextPartSplit = bodyPart.split("\r?\n\r?\n");

    String headers[] = null;
    String bodyContent;
    if (nextPartSplit.length == 2) {
      headers = nextPartSplit[0].split("\r?\n");
      bodyContent = nextPartSplit[1];
    } else {
      bodyContent = bodyPart;
    }

    ContentImpl content = new ContentImpl(bodyContent);
    if (headers != null) {
      for (String partHeader : headers) {
        Header header = headerFactory.createHeader(partHeader);
        if (header instanceof ContentTypeHeader) {
          content.setContentTypeHeader((ContentTypeHeader) header);
        } else if (header instanceof ContentDispositionHeader) {
          content.setContentDispositionHeader((ContentDispositionHeader) header);
        } else {
          content.addExtensionHeader(header);
        }
      }
    }
    return content;
  }

  /*
   * (non-Javadoc)
   * 
   * @see gov.nist.javax.sip.message.MultipartMimeContentExt#setContent(java.lang.String,
   * java.lang.String, gov.nist.javax.sip.message.Content)
   */
  public void addContent(Content content) {
    this.add(content);
  }

  public Iterator<Content> getContents() {
    return this.contentList.iterator();
  }

  public int getContentCount() {
    return this.contentList.size();
  }

}
