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
 * File Name     : MediaType.java
 * Author        : Phelim O'Doherty
 *
 *  HISTORY
 *  Version   Date      Author              Comments
 *  1.1     08/10/2002  Phelim O'Doherty    
 *~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 */
package javax.sip.header;

import java.text.ParseException;

/**
 * This interface represents media type methods for any header that contain
 * content type and content sub-type values. 
 *
 * @see AcceptHeader
 * @see ContentTypeHeader
 *
 * @author BEA Systems, NIST
 * @version 1.2
 */

public interface MediaType {

    /**
     * Sets value of media type of Header with Content Type.
     *
     * @param contentType - the new string value of the content type
     * @throws ParseException which signals that an error has been reached
     * unexpectedly while parsing the contentType value.
     */
    public void setContentType(String contentType) throws ParseException;

    /**
     * Gets media type of Header with Content type.
     *
     * @return media type of Header with Content type.
     */
    public String getContentType();

    /**
     * Sets value of media subtype of Header with Content sub-type.
     *
     * @param contentSubType - the new string value of the content sub-type.
     * @throws ParseException which signals that an error has been reached
     * unexpectedly while parsing the contentSubType value.
     */
    public void setContentSubType(String contentSubType) throws ParseException;

    /**
     * Gets media sub-type of Header with Content sub-type.
     *
     * @return media sub-type of Header with Content sub-type.
     */
    public String getContentSubType();

}

