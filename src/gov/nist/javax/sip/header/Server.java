/*******************************************************************************
 * Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
 *******************************************************************************/

package gov.nist.javax.sip.header;

import java.text.ParseException;
import javax.sip.header.*;
import java.util.*;

/**  
 * Supported SIP Header.
 *
 * @version JAIN-SIP-1.1 $Revision: 1.2 $ $Date: 2004-01-22 13:26:29 $
 *
 * @author M. Ranganathan <mranga@nist.gov>  <br/>
 * @author Olivier Deruelle <deruelle@nist.gov><br/>
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 */
public class Server extends SIPHeader implements ServerHeader {

	/** Product tokens.
	*/
	protected List productTokens;

	/**
	 * Return canonical form.
	 * @return String
	 */
	private String encodeProduct() {
		StringBuffer tokens = new StringBuffer();
		ListIterator it = productTokens.listIterator();

		while (it.hasNext()) {
			tokens.append((String) it.next());
			if (it.hasNext())
				tokens.append('/');
			else
				break;
		}
		return tokens.toString();
	}

	/** set the productToken field
	 * @param pt String to set
	 */
	public void addProductToken(String pt) {
		productTokens.add(pt);
	}

	/**
	 * Constructor.
	 */
	public Server() {
		super(NAME);
		productTokens = new LinkedList();
	}

	/** Encode only the body of this header.
	*@return encoded value of the header.
	*/
	public String encodeBody() {
		return encodeProduct();
	}

	/**
	* Returns the list value of the product parameter.
	*
	* @return the software of this UserAgentHeader
	*/
	public ListIterator getProduct() {
		if (productTokens == null || productTokens.isEmpty())
			return null;
		else
			return productTokens.listIterator();
	}

	/**
	 * Sets the product value of the UserAgentHeader.
	 *
	 * @param product - a List specifying the product value
	 * @throws ParseException which signals that an error has been reached
	 * unexpectedly while parsing the product value.
	 */
	public void setProduct(List product) throws ParseException {
		if (product == null)
			throw new NullPointerException(
				"JAIN-SIP Exception, UserAgent, "
					+ "setProduct(), the "
					+ " product parameter is null");
		productTokens = product;
	}
}
/*
 * $Log: not supported by cvs2svn $
 */
