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
 * File Name     : UnsupportedHeader.java
 * Author        : Phelim O'Doherty
 *
 *  HISTORY
 *  Version   Date      Author              Comments
 *  1.1     08/10/2002  Phelim O'Doherty    
 *~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 */
package javax.sip.header;

/**
 * The Unsupported header field lists the features not supported by the UAS.
 * If a server does not understand the option, it must respond by returning a
 * 420 (Bad Extension) Response and list those options it does not understand in
 * the UnsupportedHeader.
 *
 * @see SupportedHeader
 * @see RequireHeader
 * @author BEA Systems, NIST
 * @version 1.2
 */

public interface UnsupportedHeader extends OptionTag, Header {

    /**
     * Name of UnsupportedHeader
     */
    public final static String NAME = "Unsupported";

}

