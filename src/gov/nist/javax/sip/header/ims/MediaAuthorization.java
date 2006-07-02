/*
* Conditions Of Use 
* 
* This software was developed by employees of the National Institute of
* Standards and Technology (NIST), an agency of the Federal Government
* and others.
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
/*
 * Aveiro University - Portugal
 * DET - ECT
 * 
 */


package gov.nist.javax.sip.header.ims;


import java.text.ParseException;

import gov.nist.javax.sip.address.AddressImpl;
import gov.nist.javax.sip.header.SIPHeader;

import javax.sip.InvalidArgumentException;
import javax.sip.header.ExtensionHeader;


/**
 * MediaAuthorization Header.
 * 
 * @author Jose Miguel Freitas 
 */

public class MediaAuthorization
	extends SIPHeader
	implements MediaAuthorizationHeader, SIPHeaderNamesIms, ExtensionHeader
{
	/**
	 *  P-Media-Authorization Token
	 */
	protected String token;
	
	
	/**
	 * 
	 */
	public MediaAuthorization()
	{
		super(P_MEDIA_AUTHORIZATION);
	}
	
	
	public String getToken()
	{
		return token;
	}
	
	
	
	public void setMediaAuthorizationToken(String token) throws InvalidArgumentException
	{
		if (token == null || token == "")
			throw new InvalidArgumentException(" the Media-Authorization-Token parameter is null or empty");
		
		this.token = token;
	}

	
	protected String encodeBody()
	{
		
		return token;
		
	}


	public void setValue(String value) throws ParseException {
		throw new ParseException (value,0);
		
	}
	
	
}
