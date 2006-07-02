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

import gov.nist.core.LexerCore;
import gov.nist.javax.sip.parser.TokenNames;
import gov.nist.javax.sip.parser.TokenTypes;

import java.util.Hashtable;

import gov.nist.javax.sip.header.ims.*;


/**
 * @author ALEXANDRE MIGUEL SILVA SANTOS - Nú 10045401
 */

public class LexerIms extends LexerCore {
	
	public LexerIms() {
		
		this.selectLexerIms("command_keywordLexer");
	}
	
	
	public void selectLexerIms(String lexerName) {
	    
		currentLexer = (Hashtable) lexerTables.get(lexerName);
		this.currentLexerName = lexerName;
		
		addKeyword(PathHeader.NAME.toUpperCase(), TokenTypesIms.PATH);
		addKeyword(ServiceRouteHeader.NAME.toUpperCase(), TokenTypesIms.SERVICE_ROUTE);
		addKeyword(AssertedIdentityHeader.NAME.toUpperCase(), TokenTypesIms.P_ASSERTED_IDENTITY);
		addKeyword(TokenNames.TEL.toUpperCase(), TokenTypes.TEL);
		addKeyword(PreferredIdentityHeader.NAME.toUpperCase(), TokenTypesIms.P_PREFERRED_IDENTITY);
		
		
		// issued by jmf
		addKeyword(CalledPartyIDHeader.NAME.toUpperCase(), TokenTypesIms.P_CALLED_PARTY_ID);
		addKeyword(AssociatedURIHeader.NAME.toUpperCase(), TokenTypesIms.P_ASSOCIATED_URI);
		addKeyword(VisitedNetworkIDHeader.NAME.toUpperCase(), TokenTypesIms.P_VISITED_NETWORK_ID);
		addKeyword(ChargingFunctionAddressesHeader.NAME.toUpperCase(), TokenTypesIms.P_CHARGING_FUNCTION_ADDRESSES);
		addKeyword(ChargingVectorHeader.NAME.toUpperCase(), TokenTypesIms.P_VECTOR_CHARGING);
		addKeyword(AccessNetworkInfoHeader.NAME.toUpperCase(), TokenTypesIms.P_ACCESS_NETWORK_INFO);
		addKeyword(MediaAuthorizationHeader.NAME.toUpperCase(), TokenTypesIms.P_MEDIA_AUTHORIZATION);
		
		
	}
	
	
}
