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
 * File Name     : ReferToHeader.java
 * Author        : Phelim O'Doherty
 *
 *  HISTORY
 *  Version   Date      Author              Comments
 *  1.1     15/04/2003  Phelim O'Doherty    Initial version, extension header to 
 *                                          support RFC3515
 *~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 */
package javax.sip.header;

/**
 * This interface represents the ReferTo SIP header, as defined by 
 * <a href = "http://www.ietf.org/rfc/rfc3515.txt">RFC3515</a>, this header is 
 * not part of RFC3261.
 * <p> 
 * A ReferToHeader only appears in a REFER request. It provides a URL to 
 * reference. The ReferToHeader field MAY be encrypted as part of end-to-end 
 * encryption. The resource identified by the Refer-To URI is contacted using 
 * the normal mechanisms for that URI type. 
 *
 * @since v1.1
 * @author Sun Microsystems
 */
public interface ReferToHeader extends HeaderAddress, Parameters, Header {
    
    /**
     * Name of ReferToHeader
     */
    public final static String NAME = "Refer-To";

}

