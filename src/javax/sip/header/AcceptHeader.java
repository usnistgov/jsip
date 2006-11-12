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
 * File Name     : AcceptHeader.java
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

 * This interface represents an Accept request-header. It can be used in to

 * specify media-ranges which are acceptable for the response. AcceptHeaders

 * can be used to indicate that the request is specifically limited to a small

 * set of desired types. The specification of the acceptable media

 * is split into type and subtype.

 * <p>

 * An AcceptHeader may be followed by one or more parameters applicable to the

 * media-range. q-values allow the user to indicate the relative degree of

 * preference for that media-range, using the qvalue scale from 0 to 1. (If no

 * q-value is present, the media-range should be treated as having a q-value of

 * 1.)

 * <p>

 * If no AcceptHeader is present in a Request, the server SHOULD assume a media 
 * of type "application" and subType

 * "sdp". If an AcceptHeader is present, and if the server cannot send a

 * response which is acceptable according to the combined Accept field value,

 * then the server should send a Response message with a NOT_ACCEPTABLE

 * status code.

 * <p>
 * For example:<br>
 * <code>Accept: application/sdp;level=1, application/x-private, text/html</code>
 *
* @author BEA Systems, NIST
 * @version 1.2
 *
 */
public interface AcceptHeader extends MediaType, Parameters, Header {

    /**
     * Sets q-value for media-range in AcceptHeader. Q-values allow the user to
     * indicate the relative degree of preference for that media-range, using

     * the qvalue scale from 0 to 1. If no q-value is present, the media-range

     * should be treated as having a q-value of 1.

     *

     * @param qValue - the new float value of the q-value, a value of -1 resets

     * the qValue.

     * @throws InvalidArgumentException if the q parameter value is not

     * <code>-1</code> or between <code>0 and 1</code>.

     */

    public void setQValue(float qValue) throws InvalidArgumentException;



    /**

     * Gets q-value of media-range in AcceptHeader. A value of <code>-1</code>

     * indicates the<code>q-value</code> is not set.

     *

     * @return q-value of media-range, -1 if q-value is not set.

     */

    public float getQValue();



    /**

     * Gets boolean value to indicate if the AcceptHeader allows all media

     * sub-types, that is the content sub-type is "*".

     *

     * @return true if all content sub-types are allowed, false otherwise.

     */

    public boolean allowsAllContentSubTypes();



    /**

     * Gets boolean value to indicate if the AcceptHeader allows all media

     * types, that is the content type is "*".

     *

     * @return true if all contenet types are allowed, false otherwise.

     */

    public boolean allowsAllContentTypes();



    /**

     * Name of AcceptHeader

     */

    public final static String NAME = "Accept";



}

