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
 * File Name     : AlertInfoHeader.java
 * Author        : Phelim O'Doherty
 *
 *  HISTORY
 *  Version   Date      Author              Comments
 *  1.1     08/10/2002  Phelim O'Doherty   
 *~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 */
package javax.sip.header;

import javax.sip.address.URI;

/**
 * When present in an INVITE request, the Alert-Info header field
 * specifies an alternative ring tone to the UAS.  When present in a 180
 * (Ringing) response, the Alert-Info header field specifies an
 * alternative ringback tone to the UAC.  A typical usage is for a proxy
 * to insert this header field to provide a distinctive ring feature.
 * <p>
 * The Alert-Info header field can introduce security risks, which are
 * identical to the Call-Info header field risk, see section 20.9 of
 * <a href = "http://www.ietf.org/rfc/rfc3261.txt">RFC3261</a>.
 * In addition, a user SHOULD be able to disable this feature selectively.
 * This helps prevent disruptions that could result from the use of this
 * header field by untrusted elements.
 * <p>
 * For Example:<br>
 * <code>Alert-Info: <http://jcp.org/yeeha.wav></code>
 *
 * @version 1.1
 * @author Sun Microsystems
 */

public interface AlertInfoHeader extends Parameters, Header {

    /**
     * Sets the AlertInfo of the AlertInfoHeader to the <var>alertInfo</var>
     * parameter value.
     *
     * @param alertInfo the new Alert Info URI of this AlertInfoHeader.
     * @since v1.1
     */
    public void setAlertInfo(URI alertInfo);

    /**
     * Returns the AlertInfo value of this AlertInfoHeader.
     *
     * @return the URI representing the AlertInfo.
     * @since v1.1
     */
    public URI getAlertInfo();
    
    /**
     * Name of the AlertInfoHeader
     */
    public final static String NAME = "Alert-Info";

}

