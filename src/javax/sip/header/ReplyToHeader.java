/**
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 * Unpublished - rights reserved under the Copyright Laws of the United States.
 * Copyright © 2003 Sun Microsystems, Inc. All rights reserved.
 *
 * U.S. Government Rights - Commercial software. Government users are subject 
 * to the Sun Microsystems, Inc. standard license agreement and applicable 
 * provisions of the FAR and its supplements.
 *
 * Use is subject to license terms.
 *
 * This distribution may include materials developed by third parties. Sun, 
 * Sun Microsystems, the Sun logo, Java, Jini and JAIN are trademarks or 
 * registered trademarks of Sun Microsystems, Inc. in the U.S. and other 
 * countries.
 *
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 *
 * Module Name   : JAIN SIP Specification
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

 * @version 1.1

 * @author Sun Microsystems

 *

 */



public interface ReplyToHeader extends HeaderAddress, Parameters, Header {



    /**

     * Name of ReplyToHeader

     */

    public final static String NAME = "Reply-To";

}

