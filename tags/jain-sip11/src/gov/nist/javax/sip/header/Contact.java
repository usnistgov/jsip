/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/
package gov.nist.javax.sip.header;

import gov.nist.core.*;
import gov.nist.javax.sip.address.*;

import java.util.Date ;
import java.util.*;
import javax.sip.header.*;
import javax.sip.address.*;
import java.text.ParseException;
import javax.sip.InvalidArgumentException;
/**
* Contact Item. 
*
*@see gov.nist.javax.sip.header.ContactList
*
*@author M. Ranganathan <mranga@nist.gov> <br/>
*
*<a href="${docRoot}/uncopyright.html">This code is in the public domain.</a>
*
*Bug reports contributed by Joao Paulo, Stephen Jones, 
*John Zeng and Alstair Cole.
*
*@version JAIN-SIP-1.1
*
*/
public final class Contact 
extends AddressParametersHeader implements javax.sip.header.ContactHeader {
        public static final String ACTION=ParameterNames.ACTION;
        public static final String PROXY=ParameterNames.PROXY;
        public static final String REDIRECT=ParameterNames.REDIRECT;
	public static final String EXPIRES = ParameterNames.EXPIRES;
        public static final String Q=ParameterNames.Q;
    

	// This must be private or the toString will go for a loop! 
	private ContactList  contactList;
   	
        /** wildCardFlag field.
         */        
        protected boolean wildCardFlag;
   	
   
        /** Default constructor.
         */        
	public Contact() {
		super(NAME);
	}

	/** Set a parameter.
	*/
	public void setParameter(String name, String value) 
	throws ParseException {
	   NameValue nv = parameters.getNameValue(name);
	   if (nv != null) {
		 nv.setValue(value);
	   } else {
              nv  = new NameValue(name,value);
	      if (name.equalsIgnoreCase("methods"))
		nv.setQuotedValue();
              this.parameters.set(nv);
	   }
	}
        
	/**
         * Encode body of the header into a cannonical String.
         * @return string encoding of the header value.
         */
	protected String encodeBody() {
		StringBuffer encoding = new StringBuffer();
		if (wildCardFlag)  {
			return encoding.append("*").toString();
		}
		// Bug report by Joao Paulo
		if (address.getAddressType() == 
				AddressImpl.NAME_ADDR) {
		    encoding.append(address.encode());
		} else  {
		    // Encoding in canonical form must have <> around address.
		    encoding.append("<" ).
			append(address.encode()).append(">");
		}
		if (!parameters.isEmpty()) {
		    encoding.append(SEMICOLON).append(parameters.encode());
		} 

		return encoding.toString();
	}
		
        /** get the Contact list.
         * @return ContactList
         */        
	public  ContactList getContactList() {
		return contactList;
	}


        /** get the WildCardFlag field
         * @return boolean
         */        
	public boolean getWildCardFlag() {
            return wildCardFlag;
        } 
        
        /** get the address field.
         * @return Address
         */        
	public javax.sip.address.Address getAddress() {
	    // JAIN-SIP stores the wild card as an address!
	    return address ;
        } 

        /** get the parameters List
         * @return NameValueList
         */        
	public NameValueList getContactParms() { 
            return parameters ;
        } 
		
	
	
        /** get Expires parameter.
         * @return the Expires parameter.
         */        
	public int getExpires() {
		return getParameterAsInt(EXPIRES);
	}
	
	/** Set the expiry time in seconds.
	*@param expiryDeltaSeconds exipry time.
	*/
	
	public void setExpires(int expiryDeltaSeconds){
		Integer deltaSeconds = new Integer(expiryDeltaSeconds);
		this.parameters.set(EXPIRES, deltaSeconds) ;
	}
        
        /** get the Q-value
         * @return float
         */        
        public float getQValue(){
		return getParameterAsFloat(Q);
	}
	
     
	
        
        /** set the Contact List
         * @param cl ContactList to set
         */        
	public void setContactList( ContactList cl ) {
		contactList = cl;
	}
        
	
	/**
         * Set the wildCardFlag member
         * @param w boolean to set
         */
	public void setWildCardFlag(boolean w) {
		this.wildCardFlag = true;
		this.address = new AddressImpl();
		this.address.setWildCardFlag();
        }
        
	/**
         * Set the address member
         *
         * @param address Address to set
         */
	public void setAddress(javax.sip.address.Address address) {
	    // Canonical form must have <> around the address.
	    if (address == null) 
		throw new  NullPointerException("null address");
            this.address = (AddressImpl)address ;
	    this.wildCardFlag = false;
        }
        
        
		
        
        /** set the Q-value parameter
         * @param qValue float to set
         */        
	public void setQValue(float qValue) throws InvalidArgumentException{
		if (qValue!=-1 && (qValue<0||qValue>1))	throw new 
		InvalidArgumentException
		("JAIN-SIP Exception, Contact, setQValue(), "+
		"the qValue is not between 0 and 1");
		this.parameters.set(Q, new Float(qValue)) ;
    }
        
        
        
}
