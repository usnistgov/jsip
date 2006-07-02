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
/*******************************************
 * PRODUCT OF PT INOVAO - EST DEPARTMENT *
 *******************************************/

package gov.nist.javax.sip.header.ims;

import gov.nist.core.NameValue;
import java.text.ParseException;
import java.util.ListIterator;

import javax.sip.header.ExtensionHeader;

import gov.nist.javax.sip.header.ims.ChargingFunctionAddressesHeader;
import gov.nist.javax.sip.header.ims.ParameterNamesIms;


/**
 * @author ALEXANDRE MIGUEL SILVA SANTOS - Nú 10045401
 */

public class ChargingFunctionAddresses
	extends gov.nist.javax.sip.header.ParametersHeader
	implements ChargingFunctionAddressesHeader, SIPHeaderNamesIms , ExtensionHeader{
	
	public ChargingFunctionAddresses() {
		
		super(P_CHARGING_FUNCTION_ADDRESSES);
	}

	/* (non-Javadoc)
	 * @see gov.nist.javax.sip.header.ParametersHeader#encodeBody()
	 */
	protected String encodeBody() {
		
		StringBuffer encoding = new StringBuffer();
		
		// issued by jmf
		if (!parameters.isEmpty())
		{
			encoding.append(parameters.encode());
		}
		
		return encoding.toString();
		
	}

	
	public void setChargingCollectionFunctionAddress(String ccfAddress) throws ParseException {
		
		if (ccfAddress == null)
			throw new NullPointerException(
				"JAIN-SIP Exception, "
					+ "P-Charging-Function-Addresses, setChargingCollectionFunctionAddress(), the ccfAddress parameter is null.");

		setParameter(ParameterNamesIms.CCF, ccfAddress);
		
	}

	
	public void addChargingCollectionFunctionAddress(String ccfAddress) throws Exception {
		
		if (ccfAddress == null)
			throw new NullPointerException(
				"JAIN-SIP Exception, "
					+ "P-Charging-Function-Addresses, setChargingCollectionFunctionAddress(), the ccfAddress parameter is null.");
		
		this.parameters.add(ParameterNamesIms.CCF, ccfAddress);
		
	}

	
	public void removeChargingCollectionFunctionAddress(String ccfAddress) throws Exception {
		
		if (ccfAddress == null)
			throw new NullPointerException(
				"JAIN-SIP Exception, "
					+ "P-Charging-Function-Addresses, setChargingCollectionFunctionAddress(), the ccfAddress parameter is null.");
	
		if(!this.delete(ccfAddress, ParameterNamesIms.CCF)) {
			
			throw new Exception("CCF Address Not Removed");
			
		}
		
	}

	
	public ListIterator getChargingCollectionFunctionAddresses() {
	
		ListIterator li = this.parameters.listIterator();
		ListIterator ccfLIST = null;
		NameValue nv;
		boolean removed = false;
		while (li.hasNext()) {
			nv = (NameValue) li.next();
			if (nv.getName().equalsIgnoreCase(ParameterNamesIms.CCF)) {
				
				NameValue ccfNV = new NameValue();
				
				ccfNV.setName(nv.getName());
				ccfNV.setValue(nv.getValue());
				
				ccfLIST.add(ccfNV);
				
			}
		}
					
		return ccfLIST;
	}

	
	public void setEventChargingFunctionAddress(String ecfAddress) throws ParseException {
		
		if (ecfAddress == null)
			throw new NullPointerException(
				"JAIN-SIP Exception, "
					+ "P-Charging-Function-Addresses, setEventChargingFunctionAddress(), the ecfAddress parameter is null.");

		setParameter(ParameterNamesIms.ECF, ecfAddress);
		
	}

	
	public void addEventChargingFunctionAddress(String ecfAddress) throws ParseException {
		
		if (ecfAddress == null)
			throw new NullPointerException(
				"JAIN-SIP Exception, "
					+ "P-Charging-Function-Addresses, setEventChargingFunctionAddress(), the ecfAddress parameter is null.");
		
		this.parameters.add(ParameterNamesIms.ECF, ecfAddress);
		
	}

	
	public void removeEventChargingFunctionAddress(String ecfAddress) throws ParseException {
		
		if (ecfAddress == null)
			throw new NullPointerException(
				"JAIN-SIP Exception, "
					+ "P-Charging-Function-Addresses, setEventChargingFunctionAddress(), the ecfAddress parameter is null.");
	
		if(!this.delete(ecfAddress, ParameterNamesIms.ECF)) {
			
			throw new java.text.ParseException("CCF Address Not Removed",0);
			
		}
		
	}

	
	public ListIterator getEventChargingFunctionAddresses() {
		
		ListIterator li = this.parameters.listIterator();
		ListIterator ecfLIST = null;
		NameValue nv;
		boolean removed = false;
		while (li.hasNext()) {
			nv = (NameValue) li.next();
			if (nv.getName().equalsIgnoreCase(ParameterNamesIms.ECF)) {
				
				NameValue ecfNV = new NameValue();
				
				ecfNV.setName(nv.getName());
				ecfNV.setValue(nv.getValue());
				
				ecfLIST.add(ecfNV);
				
			}
		}
					
		return ecfLIST;
	}
	
	
	public boolean delete(String value, String name) {
		ListIterator li = this.parameters.listIterator();
		NameValue nv;
		boolean removed = false;
		while (li.hasNext()) {
			nv = (NameValue) li.next();
			if (((String) nv.getValue()).equalsIgnoreCase(value) && nv.getName().equalsIgnoreCase(name)) {
				li.remove();
				removed = true;
			}
		}
		
		return removed;
		
	}

	public void setValue(String value) throws ParseException {
		throw new ParseException ( value,0);
		
	}

}
