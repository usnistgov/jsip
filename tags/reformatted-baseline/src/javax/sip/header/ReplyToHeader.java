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
 * File Name     : ReplyToHeader.java
 * Author        : Phelim O'Doherty
 *
 *  HISTORY
 *  Version   Date      Author              Comments
 *  1.1     08/10/2002  Phelim O'Doherty    
 *~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 */
package javax.sip.header;



/**

 * The Reply-To header field contains a logical return URI that may be

 * different from the From header field.  For example, the URI MAY be used to

 * return missed calls or unestablished sessions. If the user wished to remain

 * anonymous, the header field SHOULD either be omitted from the request or

 * populated in such a way that does not reveal any private information.

 * <p>

 * For Example:<br>

 * <code>Reply-To: Bob sip:bob@biloxi.com</code>

 *

 * @see HeaderAddress

 * @see Parameters

 *
 * @author BEA Systems, NIST
 * @version 1.2

 *

 */



public interface ReplyToHeader extends HeaderAddress, Parameters, Header {



    /**

     * Name of ReplyToHeader

     */

    public final static String NAME = "Reply-To";

}

