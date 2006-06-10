/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/
package gov.nist.javax.sdp.fields;
import java.util.ListIterator;

/**
* A list of SDP Fields.
*
*@version  JAIN-SIP-1.1
*
*@author M. Ranganathan <mranga@nist.gov>  <br/>
*
*<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
*
*/

public abstract class SDPFieldList extends SDPField {
	protected SDPObjectList sdpFields;

	public SDPFieldList() {
	}

	/** Return a list iterator for the embedded field list.
	*/
	public ListIterator listIterator() {
		return sdpFields.listIterator();
	}

	public SDPFieldList(String fieldName) {
		super(fieldName);
		sdpFields = new SDPObjectList(fieldName);
	}

	public SDPFieldList(String fieldName, String classname) {
		super(fieldName);
		sdpFields = new SDPObjectList(fieldName, classname);
	}

	/**
	*add a SDP Field to the list of headers that we maintain.
	*@param h is the sdp field to add to our list.
	*/
	public void add(SDPField h) {
		sdpFields.add(h);
	}

	public SDPObject first() {
		return (SDPObject) sdpFields.first();
	}

	public SDPObject next() {
		return (SDPObject) sdpFields.next();
	}

	/**
	* Encode into a canonical string.
	*/
	public String encode() {
		StringBuffer retval = new StringBuffer();
		ListIterator li = sdpFields.listIterator();
		while (li.hasNext()) {
			SDPField sdphdr = (SDPField) li.next();
			retval.append(sdphdr.encode());
		}
		return retval.toString();
	}
	/**
	 * convert to a string representation (for printing).
	 * @param indentation int to set
	 * @return String string representation of object (for printing).
	 */
	public String debugDump(int indentation) {
		stringRepresentation = "";
		String indent = new Indentation(indentation).getIndentation();
		;
		String className = this.getClass().getName();
		sprint(indent + className);
		sprint(indent + "{");
		sprint(indent + sdpFields.debugDump(indentation));
		sprint(indent + "}");
		return stringRepresentation;
	}

	/** convert to a string representation
	 * @return String
	 */
	public String debugDump() {
		return debugDump(0);
	}

	/** Return a string representation.
	*
	*@return String representation.
	*/
	public String toString() {
		return encode();
	}

	/**
	* Equality checking predicate.
	*@param other is the other object to compare ourselves against.
	*/
	public boolean equals(Object other) {
		if (!this.getClass().equals(other.getClass()))
			return false;
		SDPFieldList that = (SDPFieldList) other;
		if (sdpFields == null)
			return that.sdpFields == null;
		return this.sdpFields.equals(that.sdpFields);
	}

	/**
	* Do a template match of fields.
	*@param template is the template to match against.
	*/
	public boolean match(Object template) {
		if (template == null)
			return true;
		if (!template.getClass().equals(this.getClass()))
			return false;
		SDPFieldList other = (SDPFieldList) template;
		if (sdpFields == other.sdpFields)
			return true;
		else if (sdpFields == null)
			return false;
		return sdpFields.match(other.sdpFields);
	}

	public Object clone() {
		SDPFieldList retval = (SDPFieldList) super.clone();
		if (this.sdpFields != null)
			retval.sdpFields = (SDPObjectList) this.sdpFields.clone();
		return retval;
	}
}
/*
 * $Log: not supported by cvs2svn $
 * Revision 1.3  2005/04/04 10:01:28  dmuresan
 * Used StringBuffer instead of String += for concatenation in
 * various encode() methods in javax.sdp.
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
