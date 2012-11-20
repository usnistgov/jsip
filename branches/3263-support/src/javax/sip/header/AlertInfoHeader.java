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
* @author BEA Systems, NIST
 * @version 1.2
 */

public interface AlertInfoHeader extends Parameters, Header {

    /**
     * Sets the AlertInfo of the AlertInfoHeader to the <var>alertInfo</var>
     * parameter value.
     *
     * @param alertInfo the new Alert Info URI of this AlertInfoHeader.
     */
    public void setAlertInfo(URI alertInfo);

    /**
     * Returns the AlertInfo value of this AlertInfoHeader.
     *
     * @return the URI representing the AlertInfo.
     */
    public URI getAlertInfo();
    
    /**
     * Name of the AlertInfoHeader
     */
    public final static String NAME = "Alert-Info";

}

