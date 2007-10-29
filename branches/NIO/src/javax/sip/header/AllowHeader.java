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
 * File Name     : AllowHeader.java
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
 * The Allow header field lists the set of methods supported by the User Agent
 * generating the message. All methods, including ACK and CANCEL, understood 
 * by the User Agent MUST be included in the list of methods in the Allow header 
 * field, when present.
 * The absence of an Allow header field MUST NOT be interpreted to mean that
 * the User Agent sending the message supports no methods. Rather, it implies
 * that the User Agent is not providing any information on what methods it
 * supports. Supplying an Allow header field in responses to methods other than
 * OPTIONS reduces the number of messages needed.
 * <p>
 * For Example:<br>
 * <code>Allow: INVITE, ACK, OPTIONS, CANCEL, BYE</code>
 *
 * @author BEA Systems, NIST
 * @version 1.2
 */
public interface AllowHeader extends Header {

    /**
     * Sets the Allow header value. The argument may be a single method name 
     * (eg "ACK") or a comma delimited list of method names 
     * (eg "ACK, CANCEL, INVITE").
     *
     * @param method - the String defining the method supported
     * in this AllowHeader
     * @throws ParseException which signals that an error has been reached
     * unexpectedly while parsing the method supported.
     */
    public void setMethod(String method) throws ParseException;

    /**
     * Gets the method of the AllowHeader. Returns null if no method is
     * defined in this Allow Header.
     *
     * @return the string identifing the method of AllowHeader.
     */
    public String getMethod();

    /**
     * Name of AllowHeader
     */
    public final static String NAME = "Allow";

}

