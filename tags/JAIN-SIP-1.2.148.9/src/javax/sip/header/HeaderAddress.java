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
 * File Name     : HeaderAddress.java
 * Author        : Phelim O'Doherty
 *
 *  HISTORY
 *  Version   Date      Author              Comments
 *  1.1     08/10/2002  Phelim O'Doherty    Initial version
 *~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 */
package javax.sip.header;

import javax.sip.address.Address;

/**
 * This interface represents methods for manipulating Address object
 * values for any header that contains a Address value. 
 * <p>
 * When the header field value contains a display name encapsulated in the
 * Address, the URI including all URI parameters is enclosed in "<" and ">".
 * If no "<" and ">" are present, all parameters after the URI are header
 * parameters, not URI parameters. The display name can be tokens, or a
 * quoted string, if a larger character set is desired.
 *
 * @see Address
 * @see ContactHeader
 * @see FromHeader
 * @see RecordRouteHeader
 * @see ReplyToHeader
 * @see RouteHeader
 * @see ToHeader
 *
 * @author BEA Systems, NIST
 * @version 1.2
 */
public interface HeaderAddress {

    /**
     * Sets the Address parameter of this Address.
     *
     * @param address - the Address object that represents the new
     *  address of this Address.
     */
    public void setAddress(Address address);

    /**
     * Gets the address parameter of this Address. 
     *
     * @return the Address of this Address
     */
    public Address getAddress();

}

