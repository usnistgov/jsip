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
 * File Name     : ReferredByHeader.java
 * Author        : Peter Musgrave (modified code by Phelim O'Doherty - ReferToHeader)
 *
 *  HISTORY
 *  Version   Date      Author              Comments
 *  1.0     24/08/05  Peter Musgrave    Initial version, 
 *~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 */
package gov.nist.javax.sip.header.extensions;

import javax.sip.header.*;

/**
 * This header is described in a draft RFC which has expired. HOwever it appears to be
 * in wide use. 
 * 
 * This interface represents the ReferredBy SIP header, as defined by 
 * <a href = "http://www.ietf.org/rfc/rfc3515.txt">RFC3892</a>, this header is 
 * not part of RFC3261.
 * <p> 
 * A ReferredByHeader only appears in a REFER request. It provides a URL to 
 * reference. The ReferredByHeader field MAY be encrypted as part of end-to-end 
 * encryption. The resource identified by the Refer-To URI is contacted using 
 * the normal mechanisms for that URI type. 
 *
 * @since v1.1
 * @author Peter Musgrave (modified Sun code)
 */
public interface ReferredByHeader extends HeaderAddress, Parameters, Header {
    
    /**
     * Name of ReferToHeader
     */
    public final static String NAME = "Referred-By";

}


