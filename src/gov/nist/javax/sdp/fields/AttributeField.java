/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/
package gov.nist.javax.sdp.fields;
import gov.nist.core.*;
import javax.sdp.*;

/**
* Attribute Field.
*
*@version  JSR141-PUBLIC-REVIEW (subject to change).
*
*@author Olivier Deruelle <deruelle@antd.nist.gov>
*@author M. Ranganathan <mranga@nist.gov>  <br/>
*
*<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
*
*/
public class AttributeField extends SDPField implements javax.sdp.Attribute {
	protected NameValue attribute;

	public NameValue getAttribute() {
		return attribute;
	}

	public AttributeField() {
		super(ATTRIBUTE_FIELD);
	}
	/**
	* Set the attribute member  
	*/
	public void setAttribute(NameValue a) {
		attribute = a;
		attribute.setSeparator(Separators.COLON);
	}

	/**
	*  Get the string encoded version of this object
	* @since v1.0
	*/
	public String encode() {
		String encoded_string = ATTRIBUTE_FIELD;
		if (attribute != null)
			encoded_string += attribute.encode();
		return encoded_string + Separators.NEWLINE;
	}

	public String toString() {
		return this.encode();
	}

	/** Returns the name of this attribute
	* @throws SdpParseException if the name is not well formatted.
	* @return a String identity or null.
	*/
	public String getName() throws SdpParseException {
		NameValue nameValue = getAttribute();
		if (nameValue == null)
			return null;
		else {
			String name = nameValue.getName();
			if (name == null)
				return null;
			else
				return name;
		}
	}

	/** Sets the id of this attribute.
	 * @param name  the string name/id of the attribute.
	 * @throws SdpException if the name is null
	 */
	public void setName(String name) throws SdpException {
		if (name == null)
			throw new SdpException("The name is null");
		else {
			NameValue nameValue = getAttribute();
			if (nameValue == null)
				nameValue = new NameValue();
			nameValue.setName(name);
			setAttribute(nameValue);
		}
	}

	/** Determines if this attribute has an associated value.
	 * @throws SdpParseException if the value is not well formatted.
	 * @return true if the attribute has a value.
	 */
	public boolean hasValue() throws SdpParseException {
		NameValue nameValue = getAttribute();
		if (nameValue == null)
			return false;
		else {
			Object value = nameValue.getValue();
			if (value == null || value == "")	// JvB: flag value now uses ""
				return false;
			else
				return true;
		}
	}

	/** Returns the value of this attribute.
	 * @throws SdpParseException if the value is not well formatted.
	 * @return the value; null if the attribute has no associated value.
	 */
	public String getValue() throws SdpParseException {
		NameValue nameValue = getAttribute();
		if (nameValue == null)
			return null;
		else {
			Object value = nameValue.getValue();
			if (value == null)
				return "";	// JvB: return empty string for flag attributes
			else if (value instanceof String)
				return (String) value;
			else
				return value.toString();
		}
	}

	/** Sets the value of this attribute.
	 * @param value the - attribute value
	 * @throws SdpException if the value is null.
	 */
	public void setValue(String value) throws SdpException {
		if (value == null)
			throw new SdpException("The value is null");
		else {
			NameValue nameValue = getAttribute();
			if (nameValue == null)
				nameValue = new NameValue();
			nameValue.setValue(value);
			setAttribute(nameValue);
		}
	}

	public Object clone() {
		AttributeField retval = (AttributeField) super.clone();
		if (this.attribute != null)
			retval.attribute = (NameValue) this.attribute.clone();
		return retval;
	}
}
/*
 * $Log: not supported by cvs2svn $
 * Revision 1.3  2005/04/16 20:38:43  dmuresan
 * Canonical clone() implementations for the GenericObject and GenericObjectList hierarchies
 *
 * Revision 1.2  2004/01/22 13:26:27  sverker
 * Issue number:
 * Obtained from:
 * Submitted by:  sverker
 * Reviewed by:   mranga
 *
 * Major reformat of code to conform with style guide. Resolved compiler and javadoc warnings. Added CVS tags.
 *
 * CVS: ----------------------------------------------------------------------
 * CVS: Issue number:
 * CVS:   If this change addresses one or more issues,
 * CVS:   then enter the issue number(s) here.
 * CVS: Obtained from:
 * CVS:   If this change has been taken from another system,
 * CVS:   then name the system in this line, otherwise delete it.
 * CVS: Submitted by:
 * CVS:   If this code has been contributed to the project by someone else; i.e.,
 * CVS:   they sent us a patch or a set of diffs, then include their name/email
 * CVS:   address here. If this is your work then delete this line.
 * CVS: Reviewed by:
 * CVS:   If we are doing pre-commit code reviews and someone else has
 * CVS:   reviewed your changes, include their name(s) here.
 * CVS:   If you have not had it reviewed then delete this line.
 *
 */
