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
 * File Name     : ExpiresHeader.java
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

 * The Expires header field gives the relative time after which the message

 * (or content) expires. The precise meaning of this is method dependent.

 * The expiration time in an INVITE does not affect the duration of the

 * actual session that may result from the invitation.  Session description

 * protocols may offer the ability to express time limits on the session

 * duration, however.

 * The value of this field is an integral number of seconds (in decimal)

 * between 0 and (2**32)-1, measured from the receipt of the request. Malformed

 * values SHOULD be treated as equivalent to 3600.

 * <p>

 * This interface represents the Expires entity-header. The ExpiresHeader is

 * optional in both REGISTER and INVITE Requests.

 * <ul>

 * <li>REGISTER - When a client sends a REGISTER request, it MAY suggest an

 * expiration interval that indicates how long the client would like the

 * registration to be valid. There are two ways in which a client can suggest

 * an expiration interval for a binding: through an Expires header field or an

 * "expires" Contact header parameter.  The latter allows expiration intervals

 * to be suggested on a per-binding basis when more than one binding is given

 * in a single REGISTER request, whereas the former suggests an expiration

 * interval for all Contact header field values that do not contain the

 * "expires" parameter.

 * <li> INVITE - The UAC MAY add an Expires header field to limit the validity

 * of the invitation.  If the time indicated in the Expires header field is

 * reached and no final answer for the INVITE has been received, the UAC core

 * SHOULD generate a CANCEL request for the INVITE.

 * </ul>

 * Example:<br>

 * <code>Expires: 5</code>

 *
 * @author BEA Systems, NIST
 * @version 1.2

 */



public interface ExpiresHeader extends Header {

    /**
     * Sets the relative expires value of the ExpiresHeader in units of seconds. 
     * The expires value MUST be between zero and (2**31)-1.
     *
     * @param expires - the new expires value of this ExpiresHeader
     * @throws InvalidArgumentException if supplied value is less than zero.
     */

    public void setExpires(int expires) throws InvalidArgumentException;

    /**
     * Gets the expires value of the ExpiresHeader. This expires value is
     * relative time.
     *
     * @return the expires value of the ExpiresHeader.
     */

    public int getExpires();

    /**
     * Name of ExpiresHeader
     */
    public final static String NAME = "Expires";



}

