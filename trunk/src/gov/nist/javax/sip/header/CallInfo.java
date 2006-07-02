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
/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/
package gov.nist.javax.sip.header;
import gov.nist.javax.sip.address.*;
import java.text.ParseException;

/**
 * CallInfo SIPHeader.
 *
 * 
 * @author "M. Ranganathan"  <br/>
 * @version 1.2 $Revision: 1.4 $ $Date: 2006-07-02 09:50:31 $
 * @since 1.1
 */
public final class CallInfo
	extends ParametersHeader
	implements javax.sip.header.CallInfoHeader {

	/**
	 * Comment for <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = -8179246487696752928L;
	
	protected GenericURI info;

	/**
	 * Default constructor
	 */
	public CallInfo() {
		super(CALL_INFO);
	}

	/**
	 * Return canonical representation.
	 * @return String 
	 */
	public String encodeBody() {
		StringBuffer encoding = new StringBuffer();

		encoding.append(LESS_THAN).append(info.toString()).append(GREATER_THAN);

		if (parameters != null && !parameters.isEmpty())
			encoding.append(SEMICOLON).append(parameters.encode());

		return encoding.toString();
	}

	/**
	 * get the purpose field
	 * @return String
	 */
	public String getPurpose() {
		return this.getParameter("purpose");
	}

	/**
	 * get the URI field
	 * @return URI
	 */
	public javax.sip.address.URI getInfo() {
		return info;
	}

	/**
	 * set the purpose field
	 * @param purpose is the purpose field.
	 */
	public void setPurpose(String purpose) {
		if (purpose == null)
			throw new NullPointerException("null arg");
		try {
			this.setParameter("purpose", purpose);
		} catch (ParseException ex) {
		}
	}

	/**
	 * set the URI field
	 * @param info is the URI to set.
	 */
	public void setInfo(javax.sip.address.URI info) {
		this.info = (GenericURI) info;
	}

	public Object clone() {
		CallInfo retval = (CallInfo) super.clone();
		if (this.info != null)
			retval.info = (GenericURI) this.info.clone();
		return retval;
	}
}
