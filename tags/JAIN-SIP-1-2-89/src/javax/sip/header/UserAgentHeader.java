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
 * File Name     : UserAgentHeader.java
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
 * The User-Agent header field contains information about the UAC originating
 * the request. This is for statistical purposes, the tracing of protocol
 * violations, and automated recognition of user agents for the sake of
 * tailoring Responses to avoid particular user agent limitations. However
 * revealing the specific software version of the user agent might allow the
 * user agent to become more vulnerable to attacks against software that is
 * known to contain security holes. Implementers SHOULD make the User-Agent
 * header field a configurable option.
 * <p>
 * For Example:<br>
 * <code>User-Agent: Softphone Beta1.5</code>
 *
 * @see ServerHeader
 * @see ViaHeader
 *
 * @author BEA Systems, NIST
 * @version 1.2
 */
public interface UserAgentHeader extends Header {

    /**
     * Returns the List of product values.
     *
     * @return the List of strings identifying the software of this ServerHeader
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
     * Name of UserAgentHeader
     */
    public final static String NAME = "User-Agent";

}

