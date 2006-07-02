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

import javax.sip.header.Header;
import javax.sip.header.Parameters;



/**
 * P-Charging-Function-Addresses header
 * Private Header: RFC 3455. 
 * 
 * There is a need to inform each SIP proxy involved in a transaction about the common
 * charging functional entities to receive the generated charging records or charging events.
 * <ul>
 * <li>
 *   - CCF is used for off-line charging (e.g., for postpaid account charging).
 * <li>
 *   - ECF is used for on-line charging (e.g., for pre-paid account charging).
 * </ul>
 * Only one instance of the header MUST be present in a particular request or response.
 * 
 * <pre>
 * P-Charging-Addr = "P-Charging-Function-Addresses" HCOLON
 *			charge-addr-params
 *			*(SEMI charge-addr-params)
 * charge-addr-params	= ccf / ecf / generic-param
 * ccf 		        = "ccf" EQUAL gen-value
 * ecf 			= "ecf" EQUAL gen-value
 *
 *
 * example: 
 *  P-Charging-Function-Addresses: ccf=192.1.1.1; ccf=192.1.1.2; 
 *	ecf=192.1.1.3; ecf=192.1.1.4 
 * </pre>
 * 
 * @author ALEXANDRE MIGUEL SILVA SANTOS - Nú 10045401
 */



public interface ChargingFunctionAddressesHeader extends Parameters, Header {
	
	public final static String NAME = "P-Charging-Function-Addresses";
	
	public void setChargingCollectionFunctionAddress(String ccfAddress) throws Exception;
	
	public void addChargingCollectionFunctionAddress(String ccfAddress) throws Exception;
	
	public void removeChargingCollectionFunctionAddress(String ccfAddress) throws Exception;
	
	public java.util.ListIterator getChargingCollectionFunctionAddresses();
	
	public void setEventChargingFunctionAddress(String ecfAddress)throws Exception;
	
	public void addEventChargingFunctionAddress(String ecfAddress) throws Exception;
	
	public void removeEventChargingFunctionAddress(String ecfAddress) throws Exception;
	
	public java.util.ListIterator getEventChargingFunctionAddresses();

}
