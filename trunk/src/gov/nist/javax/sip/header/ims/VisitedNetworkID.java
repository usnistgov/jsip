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

import java.text.ParseException;

import javax.sip.header.ExtensionHeader;

import gov.nist.core.Token;

/**
 * @author ALEXANDRE MIGUEL SILVA SANTOS - Nú 10045401
 */



public class VisitedNetworkID
	extends gov.nist.javax.sip.header.ParametersHeader
	implements VisitedNetworkIDHeader, SIPHeaderNamesIms, ExtensionHeader {
	
	protected String networkID;

	
	public VisitedNetworkID() {
		
		super(P_VISITED_NETWORK_ID);
		
	}
	
	public VisitedNetworkID(String networkID) {
		
		super(P_VISITED_NETWORK_ID);
		setVisitedNetworkID(networkID);
		
	}
	
	public VisitedNetworkID(Token tok) {
		
		super(P_VISITED_NETWORK_ID);
		setVisitedNetworkID(tok.getTokenValue());
		
	}

	protected String encodeBody() {
		
		StringBuffer retval = new StringBuffer();
		
		if (getVisitedNetworkID() != null)
			retval.append(DOUBLE_QUOTE + getVisitedNetworkID() + DOUBLE_QUOTE);
		
		if (!parameters.isEmpty())
			retval.append(SEMICOLON + this.parameters.encode());
		
		return retval.toString();

	}

		
	public void setVisitedNetworkID(String networkID) {
		if (networkID == null)
			throw new NullPointerException(" the networkID parameter is null");
		
		this.networkID = networkID;
	}
	
	
	public void setVisitedNetworkID(Token networkID) {
		if (networkID == null)
			throw new NullPointerException(" the networkID parameter is null");
		
		this.networkID = networkID.getTokenValue();
	}
	
	
	public String getVisitedNetworkID() {
		return networkID;
	}

	public void setValue(String value) throws ParseException {
		throw new ParseException (value,0);
		
	}
	

}
