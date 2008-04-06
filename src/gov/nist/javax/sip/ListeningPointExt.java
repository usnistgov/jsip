package gov.nist.javax.sip;

import javax.sip.header.ContactHeader;

public interface ListeningPointExt {
	
	/**
	 * Create a contact for this listening point.
	 * 
	 * @return a contact header corresponding to this listening point.
	 * 
	 * @since 2.0
	 * 
	 */
	
	ContactHeader createContactHeader() ;
	

}
