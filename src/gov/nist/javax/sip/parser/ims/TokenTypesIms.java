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
package gov.nist.javax.sip.parser.ims;

/**
 * @author ALEXANDRE MIGUEL SILVA SANTOS - Nú 10045401
 */

public interface TokenTypesIms extends gov.nist.javax.sip.parser.TokenTypes {
	
	public static final int PATH = START + 67;
	public static final int SERVICE_ROUTE = START + 68;
	public static final int P_ASSERTED_IDENTITY = START + 69;
	public static final int P_PREFERRED_IDENTITY = START + 70;
	public static final int P_VISITED_NETWORK_ID = START + 71;
	public static final int P_CHARGING_FUNCTION_ADDRESSES = START + 72;	
	public static final int P_VECTOR_CHARGING = START + 73;
	
	
	
	// issued by jmf
	public static final int PRIVACY = START + 74;
	public static final int P_ACCESS_NETWORK_INFO = START + 75;
	public static final int P_CALLED_PARTY_ID = START + 76;
	public static final int P_ASSOCIATED_URI = START + 77;
	public static final int P_MEDIA_AUTHORIZATION = START + 78;
	public static final int P_MEDIA_AUTHORIZATION_TOKEN = START + 79;
	
	

}
