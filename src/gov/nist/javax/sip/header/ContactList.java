/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/
package gov.nist.javax.sip.header;

import javax.sip.header.*;
import java.util.ListIterator;

/**
 * List of contact headers.ContactLists are also maintained in a hashtable
 * for quick lookup.
 * @author M. Ranganathan <mranga@nist.gov>  <br/>
 * @version JAIN-SIP-1.1 $Revision: 1.3 $ $Date: 2005-04-16 20:38:49 $
 *
 *<a href="${docRoot}/uncopyright.html">This code is in the public domain.</a>
 */
public class ContactList extends SIPHeaderList {

	/**
	 * Constructor
	 * @param hl SIPObjectList
	 */
	public ContactList(SIPObjectList hl) {
		super(hl, CONTACT);
	}

	/**
	 * Constructor. 
	 */
	public ContactList() {
		super(Contact.class, ContactHeader.NAME);
		// Set the headerlist field in our superclass.
	}

	/**
	 * add a new contact header. Store it in the hashtable also
	 * @param contact -- contact to add to this list.
	 * @throws IllegalArgumentException if Duplicate Contact for same addr
	 */
	protected void add(Contact contact) throws IllegalArgumentException {
		// Concatenate my lists.
		super.add(contact);
	}

	/**
	 * Get an array of contact addresses.
	 *
	 * @return  array of contacts.
	 */
	public Contact[] getContacts() {
		Contact[] retval = new Contact[this.size()];

		ListIterator li = this.listIterator();
		int i = 0;
		while (li.hasNext()) {
			Contact nextContact = (Contact) li.next();
			retval[i] = nextContact;
			i++;
		}
		return retval;
	}
}
/*
 * $Log: not supported by cvs2svn $
 * Revision 1.2  2004/01/22 13:26:29  sverker
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
