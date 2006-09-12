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

import gov.nist.javax.sip.header.ims.ChargingVectorHeader;
import gov.nist.javax.sip.header.ims.ParameterNamesIms;



/**
 * Charging vector IMS header
 * 
 * @author ALEXANDRE MIGUEL SILVA SANTOS 
 */

public class ChargingVector
	extends gov.nist.javax.sip.header.ParametersHeader
	implements ChargingVectorHeader, SIPHeaderNamesIms , ExtensionHeader {
	
	public ChargingVector() {
		
		super(P_CHARGING_VECTOR);
	}

	/* (non-Javadoc)
	 * @see gov.nist.javax.sip.header.ParametersHeader#encodeBody()
	 */
	protected String encodeBody() {
		
		StringBuffer encoding = new StringBuffer();
		
		// bug issued by jmf
		if (!parameters.isEmpty()) {
			encoding.append(parameters.encode());
		}
		
		return encoding.toString();
	}

	
	public String getICID() {
		
		return getParameter(ParameterNamesIms.ICID_VALUE);
	}

	
	public void setICID(String icid) throws ParseException {
		
		if (icid == null)
			throw new NullPointerException(
				"JAIN-SIP Exception, "
					+ "P-Charging-Vector, setICID(), the icid parameter is null.");

		setParameter(ParameterNamesIms.ICID_VALUE, icid);

		
	}


	
	public String getICIDGeneratedAt() {
		
		
		return getParameter(ParameterNamesIms.ICID_GENERATED_AT);
		
	}


	public void setICIDGeneratedAt(String host) throws ParseException {
		
		
		if (host == null) 
			throw new NullPointerException(
			"JAIN-SIP Exception, "
					+ "P-Charging-Vector, setICIDGeneratedAt(), the host parameter is null.");

		setParameter(ParameterNamesIms.ICID_GENERATED_AT, host);
		
	}

	
	public String getOriginatingIOI() {

		return getParameter(ParameterNamesIms.ORIG_IOI);
	}

	
	public void setOriginatingIOI(String origIOI) throws ParseException {
		
		if (origIOI == null||origIOI.length()==0) {
			removeParameter(ParameterNamesIms.ORIG_IOI);
		} else
//			throw new NullPointerException(
//				"JAIN-SIP Exception, "
//					+ "P-Charging-Vector, setOriginatingIOI(), the origIOI parameter is null.");

		setParameter(ParameterNamesIms.ORIG_IOI, origIOI);	

	}


	
	
	
	public String getTerminatingIOI() {
		
		return getParameter(ParameterNamesIms.TERM_IOI);
	}

	
	
	public void setTerminatingIOI(String termIOI) throws ParseException {
		
		if (termIOI == null||termIOI.length()==0) {
			removeParameter(ParameterNamesIms.TERM_IOI);
		} else
//			throw new NullPointerException(
//				"JAIN-SIP Exception, "
//					+ "P-Charging-Vector, setTerminatingIOI(), the termIOI parameter is null.");
		setParameter(ParameterNamesIms.TERM_IOI, termIOI);


	}

	public void setValue(String value) throws ParseException {
		throw new ParseException (value,0);
		
	}

	

}
