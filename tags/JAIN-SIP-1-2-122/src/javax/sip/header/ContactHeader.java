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
 * File Name     : ContactHeader.java
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
 * A Contact header field value provides a URI whose meaning depends on
 * the type of request or response it is in. A Contact header field value
 * can contain a display name, a URI with URI parameters, and header
 * parameters.
 * <p>
 * The Contact header field provides a SIP or SIPS URI that can be used
 * to contact that specific instance of the User Agent for subsequent requests.
 * The Contact header field MUST be present and contain exactly one SIP
 * or SIPS URI in any request that can result in the establishment of a
 * dialog.  For the methods defined in this specification, that includes
 * only the INVITE request.  For these requests, the scope of the
 * Contact is global.  That is, the Contact header field value contains
 * the URI at which the User Agent would like to receive requests, and this URI
 * MUST be valid even if used in subsequent requests outside of any
 * dialogs.
 * <p>
 * If the Request-URI or top Route header field value contains a SIPS URI, the
 * Contact header field MUST contain a SIPS URI as well.
 * <p>
 * <b>Messages and Contact Headers</b>
 * <ul>
 * <li>Requests: A contact header is mandatory for INVITE's and optional for
 * ACK, OPTIONS and REGISTER requests. This allows the callee to send future
 * Requests, such as BYE Requests, directly to the caller instead of through a
 * series of proxies.
 * <li>Informational Responses - A contact header is optional in a Informational
 * Response to an INVITE request. It has the same semantics in an Informational
 * Response as a Success Response.
 * <li>Success Responses - A contact header is mandatory in response to INVITE's
 * and optional in response to OPTIONS and REGISTER requests. An user agent
 * server sending a Success Response to an INIVTE must insert a ContactHeader
 * in the Response indicating the SIP address under which it is reachable most
 * directly for future SIP Requests.
 * <li>Redirection Responses - A contact header is optional in response to
 * INVITE's, OPTIONS, REGISTER and BYE requests. A proxy may also delete the
 * contact header.
 * <li>Ambiguous Header: - A contact header is optional in response to
 * INVITE, OPTIONS, REGISTER and BYE requests.
 * </ul>
 *
 * The ContactHeader defines the Contact parameters "q" and "expires".
 * The <code>q-value</code> value is used to prioritize addresses in a
 * list of contact addresses. The <code>expires</code> value suggests an
 * expiration interval that indicates how long the client would like a
 * registration to be valid for a specific address. These parameters are only
 * used when the Contact is present in a:
 * <ul>
 * <li>REGISTER request
 * <li>REGISTER response
 * <li>3xx response
 * </ul>
 *
 * For Example:<br>
 * <code> Contact: "Mr. Watson" sip:watson@worcester.jcp.org;
 * q=0.7; expires=3600, "Mr. Watson" mailto:watson@jcp.org.com; q=0.1
 * </code>
 * @see HeaderAddress
 * @see Parameters
 *
 * @author BEA Systems, NIST
 * @version 1.2
 */



public interface ContactHeader extends HeaderAddress, Parameters, Header {

    /**
     * Sets the value of the <code>expires</code> parameter as delta-seconds.
     * When a client sends a REGISTER request, it MAY suggest an expiration
     * interval that indicates how long the client would like the registration
     * to be valid for a specific address. There are two ways in which a client
     * can suggest an expiration interval for a binding:
     * <ul>
     * <li>through an Expires header field
     * <li>an "expires" Contact header parameter.
     * </ul>
     * The latter allows expiration intervals to be suggested on a per-binding
     * basis when more than one binding is given in a single REGISTER request,
     * whereas the former suggests an expiration interval for all Contact
     * header field values that do not contain the "expires" parameter. If
     * neither mechanism for expressing a suggested expiration time is present
     * in a REGISTER, the client is indicating its desire for the server to
     * choose.
     * <p>
     * A User Agent requests the immediate removal of a binding by specifying an
     * expiration interval of "0" for that contact address in a REGISTER
     * request.  User Agents SHOULD support this mechanism so that bindings can be
     * removed before their expiration interval has passed. The
     * REGISTER-specific Contact header field value of "*" applies to all
     * registrations, but it MUST NOT be used unless the Expires header
     * field is present with a value of "0". The "*" value can be determined
     * if "this.getNameAddress().isWildcard() = = true".
     *
     * @param expires new relative value of the expires parameter.
     * 0 implies removal of Registration specified in Contact Header.
     * @throws InvalidArgumentException if supplied value is less than zero.
     */

    public void setExpires(int expires) throws InvalidArgumentException;



    /**
     * Returns the value of the <code>expires</code> parameter or -1 if no
     * expires parameter was specified or if the parameter value cannot be
     * parsed as an int.
     *
     * @return value of the <code>expires</code> parameter measured in
     * delta-seconds, O implies removal of Registration specified in Contact
     * Header.
     */

    public int getExpires();



    /**

     * Sets the <code>qValue</code> value of the Name Address. If more than

     * one Contact is sent in a REGISTER request, the registering UA intends

     * to associate all of the URIs in these Contact header field values with

     * the address-of-record present in the To field.  This list can be

     * prioritized with the "q" parameter in the Contact header field.  The "q"

     * parameter indicates a relative preference for the particular Contact

     * header field value compared to other bindings for this address-of-record.

     * A value of <code>-1</code> indicates the <code>qValue</code> paramater

     * is not set.
     *
     * @param qValue - the new float value of the q-value parameter.
     * @throws InvalidArgumentException if the q-value parameter value is not
     * <code>-1</code> or between <code>0 and 1</code>.
     */
    public void setQValue(float qValue) throws InvalidArgumentException;



    /**

     * Returns the value of the <code>q-value</code> parameter of this

     * ContactHeader. The <code>q-value</code> parameter indicates the relative

     * preference amongst a set of locations. <code>q-values</code> are

     * decimal numbers from 0 to 1, with higher values indicating higher

     * preference.

     *

     * @return the <code>q-value</code> parameter of this ContactHeader, -1 if

     * the q-value is not set.

     */

    public float getQValue();



      /**
       * Sets a wildcard on this contact address that is "*" is assigned to the
       * contact header so that the header will have the format of Contact: *.
       *
       * @since v1.2
       */
    public void setWildCard();

       /**
        * Returns a boolean value that indicates if the contact header
        * has the format of Contact: *.
        * @return true if this is a wildcard address, false otherwise.
        * @since v1.2
        */
    public boolean isWildCard();

    /**
     * Name of ContactHeader
     */
    public final static String NAME = "Contact";



}

