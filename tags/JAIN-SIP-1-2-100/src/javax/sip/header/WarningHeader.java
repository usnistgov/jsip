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
 * File Name     : WarningHeader.java
 * Author        : Phelim O'Doherty
 *
 *  HISTORY
 *  Version   Date      Author              Comments
 *  1.1     08/10/2002  Phelim O'Doherty    
 *~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 */
package javax.sip.header;

import java.text.ParseException;
import javax.sip.InvalidArgumentException;

/**
 * The Warning header field is used to carry additional information about the
 * status of a response. Warning header field values are sent with responses
 * and contain a three-digit warning code, agent name, and warning text.
 * <ul>
 * <li>Warning Text: The "warn-text" should be in a natural language that is
 * most likely to be intelligible to the human user receiving the response.
 * This decision can be based on any available knowledge, such as the location
 * of the user, the Accept-Language field in a request, or the Content-Language
 * field in a response.
 * <li>Warning Code: The currently-defined "warn-code"s have a recommended
 * warn-text in English and a description of their meaning. These warnings
 * describe failures induced by the session description. The first digit of
 * warning codes beginning with "3" indicates warnings specific to SIP. Warnings
 * 300 through 329 are reserved for indicating problems with keywords in the
 * session description, 330 through 339 are warnings related to basic network
 * services requested in the session description, 370 through 379 are warnings
 * related to quantitative QoS parameters requested in the session description,
 * and 390 through 399 are miscellaneous warnings that do not fall into one of
 * the above categories. Additional "warn-code"s can be defined.
 * </ul>
 * Any server may add WarningHeaders to a Response. Proxy servers must place
 * additional WarningHeaders before any AuthorizationHeaders. Within that
 * constraint, WarningHeaders must be added after any existing WarningHeaders
 * not covered by a signature. A proxy server must not delete any WarningHeader
 * that it received with a Response.
 * <p>
 * When multiple WarningHeaders are attached to a Response, the user agent
 * should display as many of them as possible, in the order that they appear
 * in the Response. If it is not possible to display all of the warnings, the
 * user agent first displays warnings that appear early in the Response.
 * <p>
 * Examples of using Warning Headers are as follows:
 * <ul>
 * <li>A UAS rejecting an offer contained in an INVITE SHOULD return a 488 (Not
 * Acceptable Here) response.  Such a response SHOULD include a Warning header
 * field value explaining why the offer was rejected.
 * <li>If the new session description is not acceptable, the UAS can reject it
 * by returning a 488 (Not Acceptable Here) response for the re-INVITE. This
 * response SHOULD include a Warning header field.
 * <li>A 606 (Not Acceptable) response means that the user wishes to communicate,
 * but cannot adequately support the session described. The 606 (Not Acceptable)
 * response MAY contain a list of reasons in a Warning header field describing
 * why the session described cannot be supported.
 * <li>Contact header fields MAY be present in a 200 (OK) response and have the
 * same semantics as in a 3xx response. That is, they may list a set of
 * alternative names and methods of reaching the user. A Warning header field
 * MAY be present.
 * </ul>
 * For Example:<br>
 * <code>Warning: 307 isi.edu "Session parameter 'foo' not understood"</code>
 *
 * @author BEA Systems, NIST
 * @version 1.2
 */


public interface WarningHeader extends Header {

    /**
     * Gets the agent of the server that created this WarningHeader.
     *
     * @return the agent of the WarningHeader
     */
    public String getAgent();

    /**
     * Sets the agent value of the WarningHeader to the new value passed to the
     * method.
     *
     * @param agent - the new agent value of WarningHeader
     * @throws ParseException which signals that an error has been reached
     * unexpectedly while parsing the agent value.
     */
    public void setAgent(String agent) throws ParseException;


    /**
     * Gets text of WarningHeader.
     *
     * @return the string text value of the WarningHeader.
     */
    public String getText();

    /**
     * Sets the text of WarningHeader to the newly supplied text value.
     *
     * @param text - the new text value of the Warning Header.
     * @throws ParseException which signals that an error has been reached
     * unexpectedly while parsing the text value.
     */
    public void setText(String text) throws ParseException;

    /**
     * Sets the code of the WarningHeader. The standard RFC3261 codes are
     * defined as constants in this class.
     *
     * @param code - the new code that defines the warning code.
     * @throws InvalidArgumentException if an invalid integer code is given for
     * the WarningHeader. 
     */
    public void setCode(int code) throws InvalidArgumentException;

    /**
     * Gets the code of the WarningHeader.
     *
     * @return the integer code value of the WarningHeader
     */
    public int getCode();

    /**
     * Name of WarningHeader
     */
    public final static String NAME = "Warning";

// Constants

    /**
     * One or more network protocols contained in the session description
     * are not available.
     */
    public final static int INCOMPATIBLE_NETWORK_PROTOCOL = 300;

    /**
     * One or more network address formats contained in the session
     * description are not available.
     */
    public final static int INCOMPATIBLE_NETWORK_ADDRESS_FORMATS = 301;

    /**
     * One or more transport protocols described in the session description
     * are not available.
     */
    public final static int INCOMPATIBLE_TRANSPORT_PROTOCOL = 302;

    /**
     * One or more bandwidth measurement units contained in the session
     * description were not understood.
     */
    public final static int INCOMPATIBLE_BANDWIDTH_UNITS = 303;

    /**
     * One or more media types contained in the session description are
     * not available.
     */
    public final static int MEDIA_TYPE_NOT_AVAILABLE = 304;

    /**
     * One or more media formats contained in the session description are
     * not available.
     */
    public final static int INCOMPATIBLE_MEDIA_FORMAT = 305;

    /**
     * One or more of the media attributes in the session description are
     * not supported.
     */
    public final static int ATTRIBUTE_NOT_UNDERSTOOD = 306;

    /**
     * A parameter other than those listed above was not understood.
     */
    public final static int SESSION_DESCRIPTION_PARAMETER_NOT_UNDERSTOOD = 307;

    /**
     * The site where the user is located does not support multicast.
     */
    public final static int MULTICAST_NOT_AVAILABLE = 330;

    /**
     * The site where the user is located does not support unicast
     * communication (usually due to the presence of a firewall).
     */
    public final static int UNICAST_NOT_AVAILABLE = 331;

    /**
     * The bandwidth specified in the session description or defined by the
     * media exceeds that known to be available.
     */
    public final static int INSUFFICIENT_BANDWIDTH = 370;

    /**
     * The warning text can include arbitrary information to be presented to
     * a human user, or logged. A system receiving this warning MUST NOT
     * take any automated action.
     */
    public final static int MISCELLANEOUS_WARNING = 399;

}
