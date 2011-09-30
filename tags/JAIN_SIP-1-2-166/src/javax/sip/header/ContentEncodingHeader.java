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
 * File Name     : ContentEncodingHeader.java
 * Author        : Phelim O'Doherty
 *
 *  HISTORY
 *  Version   Date      Author              Comments
 *  1.1     08/10/2002  Phelim O'Doherty    
 *~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 */
package javax.sip.header;


/**
 * A ContentEncodingHeader is used as a modifier to the "media-type". When
 * present, its value indicates what additional content codings have been
 * applied to the entity-body, and thus what decoding mechanisms must be
 * applied in order to obtain the media-type referenced by the
 * ContentTypeHeader. The ContentEncodingHeader is primarily used to allow a
 * body to be compressed without losing the identity of its underlying media
 * type.
 * <p>
 * If multiple encodings have been applied to an entity, the ContentEncodings
 * must be listed in the order in which they were applied. All content-coding 
 * values are case-insensitive. Clients MAY apply content encodings to the body 
 * in requests. A server MAY
 * apply content encodings to the bodies in responses. The server MUST only
 * use encodings listed in the Accept-Encoding header field in the request.
 * If the server is not capable of decoding the body, or does not recognize any
 * of the content-coding values, it must send a UNSUPPORTED_MEDIA_TYPE
 * Response, listing acceptable encodings in an AcceptEncodingHeader.
 *
 * @see ContentDispositionHeader
 * @see ContentLengthHeader
 * @see ContentTypeHeader
 * @see ContentLanguageHeader
 *
 * @author BEA Systems, NIST
 * @version 1.2
 */

public interface ContentEncodingHeader extends Encoding, Header {
    

    /**
     * Name of ContentEncodingHeader
     */
    public final static String NAME = "Content-Encoding";

}

