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
/*******************************************
 * PRODUCT OF PT INOVAO - EST DEPARTMENT *
 *******************************************/

package gov.nist.javax.sip.header.ims;


/**
 * @author ALEXANDRE MIGUEL SILVA SANTOS - Nú 10045401
 */

public interface SIPHeaderNamesIms
	extends gov.nist.javax.sip.header.SIPHeaderNames {

	public static final String PATH = PathHeader.NAME;
	public static final String SERVICE_ROUTE = ServiceRouteHeader.NAME;
	public static final String P_ASSERTED_IDENTITY = AssertedIdentityHeader.NAME;
	public static final String P_PREFERRED_IDENTITY = PreferredIdentityHeader.NAME;
	public static final String CALLED_PARTY_ID = CalledPartyIDHeader.NAME;
	public static final String P_VISITED_NETWORK_ID = VisitedNetworkIDHeader.NAME;
	public static final String P_CHARGING_FUNCTION_ADDRESSES = ChargingFunctionAddressesHeader.NAME;
	public static final String P_CHARGING_VECTOR = ChargingVectorHeader.NAME;

	
	// issued by jmf
	public static final String PRIVACY = PrivacyHeader.NAME;
	public static final String P_ASSOCIATED_URI = AssociatedURIHeader.NAME;
	public static final String P_MEDIA_AUTHORIZATION = MediaAuthorizationHeader.NAME;
	public static final String P_ACCESS_NETWORK_INFO = AccessNetworkInfoHeader.NAME;
	
	
	
}

