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
 * File Name     : AcceptEncodingHeader.java
 * Author        : Phelim O'Doherty
 *
 *  HISTORY
 *  Version   Date      Author              Comments
 *  1.1     08/10/2002  Phelim O'Doherty    
 *~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 */
package javax.sip.header;

import javax.sip.InvalidArgumentException;

/**
 * This interface represents the Accept-Encoding request-header.
 * A client includes an AcceptEncodingHeader in a Request to tell the server
 * what coding schemes are acceptable in the Response e.g. compress, gzip.
 * <p>
 * If an AcceptEncodingHeader is present, and if the server cannot send a
 * Response which is acceptable according to the AcceptEncodingHeader, then
 * the server should return a Response with a status code of NOT_ACCEPTABLE.
 * <p>
 * An empty Accept-Encoding header field is permissible, it is equivalent to
 * <code>Accept-Encoding: identity</code>, meaning no encoding is permissible.
 * <p>
 * If no Accept-Encoding header field is present, the server SHOULD assume a
 * default value of identity.
 * <p>
 * For Example:<br>
 * <code>Accept-Encoding: gzip</code>
 *
 * @author BEA Systems, NIST
 * @version 1.2
 *
 */
public interface AcceptEncodingHeader extends Parameters, Encoding, Header {
   
    /**
     * Gets q-value of the encoding in this encoding value. A value of
     * <code>-1</code> indicates the<code>q-value</code> is not set.
     *
     * @return q-value of encoding value, -1 if q-value is not set.
     */
    public float getQValue();

    /**
     * Sets q-value for the encoding in this encoding value. Q-values allow the
     * user to indicate the relative degree of preference for that encoding,
     * using the qvalue scale from 0 to 1. If no q-value is present, the
     * encoding should be treated as having a q-value of 1.
     *
     * @param qValue - the new float value of the q-value, a value of -1 resets
     * the qValue.
     * @throws InvalidArgumentException if the q parameter value is not
     * <code>-1</code> or between <code>0 and 1</code>.
     */
    public void setQValue(float qValue) throws InvalidArgumentException;
    
    /**
     * Name of AcceptEncodingHeader
     */
    public final static String NAME = "Accept-Encoding";

}

