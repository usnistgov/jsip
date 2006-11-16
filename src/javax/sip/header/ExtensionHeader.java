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
 * File Name     : ExtensionHeader.java
 * Author        : Phelim O'Doherty
 *
 *  HISTORY
 *  Version   Date      Author              Comments
 *  1.1     08/10/2002  Phelim O'Doherty    Initial version
 *~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 */
package javax.sip.header;

import java.text.ParseException;

/**
 * This interface represents an Extension SIP header that was not defined at the
 * baseline of this specification. Extension Headers can be added as 
 * required by extending this interface assuming other endpoints understand the Header. 
 * Any Header that extends this class must define a "NAME" String constant identifying the name 
 * of the extension Header. A server must ignore Headers that it does not 
 * understand. A proxy must not remove or modify Headers that it does not 
 * understand. Implementation note : The implementation of any new headers 
 * added since version 1.1 of this specification
 * <b>SHOULD</b> implement ExtensionHeader for backwards compatibility (even if these
 * headers are defined in the current version of this specification).
 * Headers that are not part of the current version of this specification 
 * <b>MUST</b> implement ExtensionHeader.
 *
 * @author BEA Systems, NIST 
 * @version 1.2
 */

public interface ExtensionHeader extends Header {
  
    /**
     * Sets the value parameter of the ExtensionHeader. 
     *
     * @param value - the new value of the ExtensionHeader
     * @throws ParseException which signals that an error has been reached
     * unexpectedly while parsing the value parameter.
     */
    public void setValue(String value) throws ParseException;


    /**
     * Gets the value of the ExtensionHeader. 
     *
     * @return the string of the value parameter.
     */
    public String getValue();    

}
