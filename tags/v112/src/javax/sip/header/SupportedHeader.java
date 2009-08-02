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
 * File Name     : SupportedHeader.java
 * Author        : Phelim O'Doherty
 *
 *  HISTORY
 *  Version   Date      Author              Comments
 *  1.1     08/10/2002  Phelim O'Doherty    Initial Version
 *~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 */
package javax.sip.header;



/**

 * The Supported header field enumerates all the extensions supported by

 * the UAC or UAS. The Supported header field contains a list of option tags,

 * that are understood by the UAC or UAS. A User Agent compliant to this specification

 * MUST only include option tags corresponding to standards-track RFCs. If

 * empty, it means that no extensions are supported.

 * <p>

 * For Example:<br>

 * <code>Supported: 100rel</code>

 *

 * @see OptionTag

 * @see UnsupportedHeader
 * @author BEA Systems, NIST
 * @version 1.2

 */



public interface SupportedHeader extends OptionTag, Header {



    /**

     * Name of SupportedHeader

     */

    public final static String NAME = "Supported";

}

