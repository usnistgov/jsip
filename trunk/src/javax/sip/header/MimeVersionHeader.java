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
 * File Name     : MimeVersionHeader.java
 * Author        : Phelim O'Doherty
 *
 *  HISTORY
 *  Version   Date      Author              Comments
 *  1.1     08/10/2002  Phelim O'Doherty    Initial version
 *~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 */
package javax.sip.header;

import javax.sip.InvalidArgumentException;

/**
 * SIP messages MAY include a single MIME-Version general-header field to 
 * indicate what version of the MIME protocol was used to construct the 
 * message. Use of the MIME-Version header field indicates that the message is 
 * in full compliance with the MIME protocol as defined in 
 * <a href = "http://www.ietf.org/rfc/rfc2405.txt">RFC2045</a>. Proxies/gateways 
 * are responsible for ensuring full compliance (where possible) when exporting 
 * SIP messages to strict MIME environments.
 * <p>
 * For Example:<br>
 * <code>MIME-Version: 1.0</code>
 *
 * @author BEA Systems, NIST
 * @version 1.2
 */
public interface MimeVersionHeader extends Header {

    /**
     * Gets the Minor version value of this MimeVersionHeader.
     *
     * @return the Minor version of this MimeVersionHeader
     */
    public int getMinorVersion();

    /**
     * Sets the Minor-Version argument of this MimeVersionHeader to the supplied
     * minorVersion value.
     *
     * @param minorVersion - the new minor MIME version
     * @throws InvalidArgumentException if the supplied value is less than zero.
     */
    public void setMinorVersion(int minorVersion) throws InvalidArgumentException;
    
    /**
     * Gets the Major version value of this MimeVersionHeader.
     *
     * @return the Major version of this MimeVersionHeader
     */
    public int getMajorVersion();

    /**
     * Sets the Major-Version argument of this MimeVersionHeader to the supplied
     * majorVersion value.
     *
     * @param majorVersion - the new major MIME version
     * @throws InvalidArgumentException if the supplied version is less than zero.
     */
    public void setMajorVersion(int majorVersion) throws InvalidArgumentException;

    /**
     * Name of MimeVersionHeader
     */
    public final static String NAME = "MIME-Version";

}

