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
 * File Name     : ServerHeader.java
 * Author        : Phelim O'Doherty
 *
 *  HISTORY
 *  Version   Date      Author              Comments
 *  1.1     08/10/2002  Phelim O'Doherty    
 *~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 */
package javax.sip.header;

import java.text.ParseException;
import java.util.*;

/**
 * The Server header field contains information about the software used by
 * the UAS to handle the request. Revealing the specific software version of
 * the server might allow the server to become more vulnerable to attacks
 * against software that is known to contain security holes. Implementers
 * SHOULD make the Server header field a configurable option. If the Response
 * is being forwarded through a proxy, the proxy application must not modify
 * the ServerHeaders. Instead, it should include a ViaHeader.
 * <p>
 * For Example:<br>
 * <code>Server: HomeServer v2</code>
 *
 * @see ViaHeader
 * @see UserAgentHeader
 *
 * @author BEA Systems, NIST
 * @version 1.2
 */
public interface ServerHeader extends Header {

    /**
     * Returns a ListIterator over the List of product values.
     *
     * @return a ListIterator over the List of strings identifying the 
     * software of this ServerHeader
     */
    public ListIterator getProduct();

    /**
     * Sets the List of product values of the ServerHeader.
     *
     * @param product - a List of Strings specifying the product values
     * @throws ParseException which signals that an error has been reached
     * unexpectedly while parsing the List of product value strings.
     */
    public void setProduct(List product) throws ParseException;
    
    /**
     * Name of ServerHeader
     */
    public final static String NAME = "Server";

}

