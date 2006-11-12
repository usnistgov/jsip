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
 * File Name     : CallInfoHeader.java
 * Author        : Phelim O'Doherty
 *
 *  HISTORY
 *  Version   Date      Author              Comments
 *  1.1     08/10/2002  Phelim O'Doherty    Initial version
 *~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 */
package javax.sip.header;

import java.text.ParseException;
import javax.sip.address.URI;

/**
 * The Call-Info header field provides additional information about the
 * caller or callee, depending on whether it is found in a request or
 * response.  The purpose of the URI is described by the "purpose"
 * parameter.  The "icon" purpose designates an image suitable as an
 * iconic representation of the caller or callee.  The "info" purpose
 * describes the caller or callee in general, for example, through a web
 * page.  The "card" purpose provides a business card, for example, in
 * vCard or LDIF formats.
 * <p>
 * Use of the Call-Info header field can pose a security risk.  If a
 * callee fetches the URIs provided by a malicious caller, the callee
 * may be at risk for displaying inappropriate or offensive content,
 * dangerous or illegal content, and so on.  Therefore, it is
 * RECOMMENDED that a User Agent only render the information in the Call-Info
 * header field if it can verify the authenticity of the element that
 * originated the header field and trusts that element.  This need not
 * be the peer User Agent; a proxy can insert this header field into requests.
 * <p>
 * For Example:<br>
 * <code>Call-Info: http://jcp.org/duke/photo.jpg;<br>
 * purpose=icon, http://jcp.org/duke/; purpose=info</code>
 *
 * @author BEA Systems, NIST
 * @version 1.2
 *
 */

public interface CallInfoHeader extends Parameters, Header {

    /**
     * Sets the Information parameter of this CallInfoHeader. The Information
     * describes the caller or callee.
     *
     * @param info the new URI value of the location of the information.
     */
    public void setInfo(URI info);

    /**
     * Gets the URI that represents the location of the info of the caller
     * or callee.
     *
     * @return the location of the info of this CallInfoHeader, returns null
     * if no info is present.
     */
    public URI getInfo();

    /**
     * Sets the purpose parameter of the info of this CallInfoHeader. 
     *
     * @param purpose - the new string value of the purpose of this info.
      * @throws ParseException which signals that an error has been reached
      * unexpectedly while parsing the purpose value.
     */
    public void setPurpose(String purpose) throws ParseException;

    /**
     * Gets the purpose of the information supplied in this CallInfoHeader.
     *
     * @return the sting value of the purpose of the info, returns null
     * if no purpose is present.
     */
    public String getPurpose();

    /**
     * Name of CallInfoHeader
     */
    public final static String NAME = "Call-Info";
}
