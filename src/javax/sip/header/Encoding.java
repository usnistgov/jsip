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
 * File Name     : Encoding.java
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
 * This interface represents encoding methods for any header that contains an
 * encoding value. 
 *
 * @see AcceptEncodingHeader
 * @see ContentEncodingHeader
 *
 * @since 1.1
 * @author Sun Microsystems
 */
public interface Encoding {


    /**
     * Sets the encoding of an EncodingHeader.
     *
     * @param encoding - the new string value defining the encoding.
     * @throws ParseException which signals that an error has been reached
     * unexpectedly while parsing the encoding value.
     */

    public void setEncoding(String encoding) throws ParseException;

    /**
     * Gets the encoding of an EncodingHeader. Returns null if no 
     * encoding is defined in an EncodingHeader.
     *
     * @return the string value identifing the encoding
     */
    public String getEncoding();    

}

