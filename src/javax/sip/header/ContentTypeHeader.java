/**
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 * Unpublished - rights reserved under the Copyright Laws of the United States.
 * Copyright © 2003 Sun Microsystems, Inc. All rights reserved.
 * Copyright © 2005 BEA Systems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
 *
 * This distribution may include materials developed by third parties. 
 *
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 *
 * Module Name   : JSIP Specification
 * File Name     : ContentTypeHeader.java
 * Author        : Phelim O'Doherty
 *
 *  HISTORY
 *  Version   Date      Author              Comments
 *  1.1     08/10/2002  Phelim O'Doherty    
 *~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 */
package javax.sip.header;

/**
 * The Content-Type header field indicates the media type of the message-body
 * sent to the recipient. The Content-Type header field MUST be present if the
 * body is not empty.  If the body is empty, and a Content-Type header field is
 * present, it indicates that the body of the specific type has zero length
 * (for example, an empty audio file).
 * <p>
 * For Example:<br>
 * <code>Content-Type: application/sdp</code>
 *
 * @see ContentDispositionHeader
 * @see ContentLengthHeader
 * @see ContentEncodingHeader
 * @see ContentLanguageHeader
 *
 * @author BEA Systems, NIST
 * @version 1.2
 */

public interface ContentTypeHeader extends MediaType, Parameters, Header {

    /**
     * Name of ContentTypeHeader
     */
    public final static String NAME = "Content-Type";

}

