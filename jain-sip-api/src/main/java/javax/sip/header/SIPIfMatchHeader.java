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
 * File Name     : SIPIfMatchHeader.java
 * Author        : Jeroen van Bemmel
 *
 *  HISTORY
 *  Version   Date      Author              Comments
 *  1.1     27/10/2005  Jeroen van Bemmel   Initial version, header to support RFC3903.
 *~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 */
package javax.sip.header;

import java.text.ParseException;

import javax.sip.header.Header;

/**
 * This interface represents the SIP-If-Match header, as defined by
 * <a href = "http://www.ietf.org/rfc/rfc3903.txt">RFC3903</a>.
 * <p>
 * The SIP-If-Match header is used by a client (event state publisher) in
 * a PUBLISH request, to update previously published event state. The value is
 * obtained from the server in a {@link javax.sip.header.SIPETagHeader} in a
 * 2xx response to a previous PUBLISH.
 * <p>
 * Sample syntax:<br><code>SIP-If-Match: dx200xyz</code>
 *
 * <p>
 * A server must ignore Headers that it does not understand. A proxy must not
 * remove or modify Headers that it does not understand.
 *
 * @author BEA Systems, NIST
 * @since 1.2
 */
public interface SIPIfMatchHeader extends Header {

    /**
     * Name of this header (no short form.
     */
    public static final String NAME = "SIP-If-Match";

    /**
     * Returns the value of the entity-tag.
         *
     * @return the entity-tag
     */
    public String getETag();

    /**
     * Sets the entity-tag
     * @param etag the new value of the entity-tag.
     *
     * @throws ParseException if the ETag syntax is invalid (not a valid token)
     */
    public void setETag( String etag ) throws ParseException;
}
