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
package gov.nist.javax.sip.header.ims;

import gov.nist.javax.sip.header.SIPHeaderList;


/**
 * List of P-Access-Network-Info headers
 *
 * @author Dipesh Kumar Sahu (IT) HPE
 */

/*
 *  access-info            = cgi-3gpp / utran-cell-id-3gpp /
                               dsl-location / i-wlan-node-id /
                               ci-3gpp2 / eth-location /
                               ci-3gpp2-femto / fiber-location /
                               np / gstn-location /local-time-zone /
                               dvb-rcs2-node-id / operator-specific-GI /
                               utran-sai-3gpp / extension-access-info
      np                     = "network-provided"
      extension-access-info  = generic-param
 */

public class PAccessNetworkInfoList extends SIPHeaderList<PAccessNetworkInfo> {
	
	private static final long serialVersionUID = 563201040577795126L;
	
	/*
	 * Default Construction
	 */
	public PAccessNetworkInfoList() {
		super(PAccessNetworkInfo.class, PAccessNetworkInfoHeader.NAME);
	}
	
	public Object clone() {
		PAccessNetworkInfoList retval = new PAccessNetworkInfoList();
        return retval.clonehlist(this.hlist);
    }

}
