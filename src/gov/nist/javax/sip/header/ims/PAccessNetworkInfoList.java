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
