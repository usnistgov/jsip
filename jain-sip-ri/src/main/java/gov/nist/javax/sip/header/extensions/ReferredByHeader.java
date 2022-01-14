/*
 * Conditions Of Use
 *
 * This software was developed by employees of the National Institute of
 * Standards and Technology (NIST), an agency of the Federal Government.
 * Pursuant to title 15 Untied States Code Section 105, works of NIST
 * employees are not subject to copyright protection in the United States
 * and are considered to be in the public domain.  As a result, a formal
 * license is not needed to use the software.
 *
 * This software is provided by NIST as a service and is expressly
 * provided "AS IS."  NIST MAKES NO WARRANTY OF ANY KIND, EXPRESS, IMPLIED
 * OR STATUTORY, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTY OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, NON-INFRINGEMENT
 * AND DATA ACCURACY.  NIST does not warrant or make any representations
 * regarding the use of the software or the results thereof, including but
 * not limited to the correctness, accuracy, reliability or usefulness of
 * the software.
 *
 * Permission to use this software is contingent upon your acceptance
 * of the terms of this agreement
 *
 * .
 *
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


