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
 * File Name     : RequireHeader.java
 * Author        : Phelim O'Doherty
 *
 *  HISTORY
 *  Version   Date      Author              Comments
 *  1.1     08/10/2002  Phelim O'Doherty    
 *~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 */
package javax.sip.header;



/**

 * The Require header field is used by UACs to tell UASs about options that

 * the UAC expects the UAS to support in order to process the request.

 * Although an optional header field, the Require MUST NOT be ignored if it

 * is present.

 * <p>

 * The Require header field contains a list of option tags. Each option tag

 * defines a SIP extension that MUST be understood to process the request.

 * Frequently, this is used to indicate that a specific set of extension

 * header fields need to be understood. A UAC compliant to this specification

 * MUST only include option tags corresponding to standards-track RFCs.

 * <p>

 * If a server does not understand the option, it must respond by returning a

 * BAD_EXTENSION Response and list those options it does not understand in

 * the UnsupportedHeader.

 * <p>

 * Proxy and redirect servers must ignore features that are not understood. If

 * a particular extension requires that intermediate devices support it, the

 * extension must be tagged in the ProxyRequireHeader as well.

 * <p>

 * For Example:<br>

 * <code>Require: 100rel</code>

 *

 * @see ProxyRequireHeader

 * @see OptionTag
 * @author BEA Systems, NIST
 * @version 1.2

 */



public interface RequireHeader extends OptionTag, Header {



    /**

     * Name of RequireHeader

     */

    public final static String NAME = "Require";

}

