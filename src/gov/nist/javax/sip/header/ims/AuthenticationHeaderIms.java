/*
* Conditions Of Use 
* 
* This software was developed by employees of the National Institute of
* Standards and Technology (NIST), an agency of the Federal Government,
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
/*******************************************
 * PRODUCT OF PT INOVAO - EST DEPARTMENT *
 *******************************************/

package gov.nist.javax.sip.header.ims;

import java.text.ParseException;
import javax.sip.InvalidArgumentException;
import gov.nist.core.NameValue;
import gov.nist.core.Separators;
import gov.nist.javax.sip.header.AuthenticationHeader;
import javax.sip.header.ExtensionHeader;

/*
 * @author ALEXANDRE MIGUEL SILVA SANTOS - Nú 10045401
 */

public class AuthenticationHeaderIms extends AuthenticationHeader implements ExtensionHeader {
	
	public static final String IK = ParameterNamesIms.IK;
	public static final String CK = ParameterNamesIms.CK;
	public static final String INTEGRITY_PROTECTED = ParameterNamesIms.INTEGRITY_PROTECTED;
	
	public AuthenticationHeaderIms(String name) {
		
		super(name);
	}
	
	public AuthenticationHeaderIms() {
		
		super();
	}
	
	public void setIK(String ik) throws ParseException {
		if (ik == null)
			throw new NullPointerException(
				"JAIN-SIP Exception, "
					+ " AuthenticationHeader, setIk(), The auth-param IK parameter is null");
		setParameterIms(IK, ik);
	}
	
	public String getIK() {
		return getParameter(ParameterNamesIms.IK);
	}
	
	public void setCK(String ck) throws ParseException {
		if (ck == null)
			throw new NullPointerException(
				"JAIN-SIP Exception, "
					+ " AuthenticationHeader, setCk(), The auth-param CK parameter is null");
		setParameterIms(CK, ck);
	}
	
	public String getCK() {
		return getParameter(ParameterNamesIms.CK);
	}
	
	
	public void setIntegrityProtected(String integrityProtected) throws ParseException
	{
		if (integrityProtected == null)
			throw new NullPointerException(
				"JAIN-SIP Exception, "
					+ " AuthenticationHeader, setIntegrityProtected(), The integrity-protected parameter is null");
		
		setParameterIms(INTEGRITY_PROTECTED, integrityProtected);
	}
	
	
	
	public String getIntegrityProtected() {
		return getParameter(ParameterNamesIms.INTEGRITY_PROTECTED);
	}
	
	
	public void setParameterIms(String name, String value) throws ParseException {
		NameValue nv = super.parameters.getNameValue(name.toLowerCase());
		if (nv == null)
		{
			nv = new NameValue(name, value);
			if (name.equalsIgnoreCase(ParameterNamesIms.IK)	
					|| name.equalsIgnoreCase(ParameterNamesIms.CK) 
					|| name.equalsIgnoreCase(ParameterNamesIms.INTEGRITY_PROTECTED) ) 
			{
				nv.setQuotedValue();
				if (value == null)
					throw new NullPointerException("null value");
				if (value.startsWith(Separators.DOUBLE_QUOTE))
					throw new ParseException(
						value + " : Unexpected DOUBLE_QUOTE",
						0);
			}
			super.setParameter(nv);
		} 
		else
			nv.setValue(value);

	}

	public void setValue(String value) throws ParseException {
		throw new ParseException(value,0);
		
	}
}
