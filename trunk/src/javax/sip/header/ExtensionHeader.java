/**
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 * Unpublished - rights reserved under the Copyright Laws of the United States.
 * Copyright © 2003 Sun Microsystems, Inc. All rights reserved.
 *
 * U.S. Government Rights - Commercial software. Government users are subject 
 * to the Sun Microsystems, Inc. standard license agreement and applicable 
 * provisions of the FAR and its supplements.
 *
 * Use is subject to license terms.
 *
 * This distribution may include materials developed by third parties. Sun, 
 * Sun Microsystems, the Sun logo, Java, Jini and JAIN are trademarks or 
 * registered trademarks of Sun Microsystems, Inc. in the U.S. and other 
 * countries.
 *
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 *
 * Module Name   : JAIN SIP Specification
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
 * This interface represents an Extension SIP header that is not currently 
 * defined by JAIN SIP. Extension Headers can be added as required by extending 
 * this interface assuming other endpoints understand the Header. Any Header that
 * extends this class must define a "NAME" String constant identifying the name 
 * of the extension Header. A server must ignore Headers that it does not 
 * understand. A proxy must not remove or modify Headers that it does not 
 * understand.
 *
 * @version 1.1
 * @author Sun Microsystems
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
