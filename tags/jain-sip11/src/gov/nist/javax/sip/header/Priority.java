/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/
package gov.nist.javax.sip.header;


import javax.sip.header.*;
import java.text.ParseException;

/**
* the Priority header. 
*
*@author Olivier Deruelle <deruelle@nist.gov><br/>
*
*<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
*
*/
public class Priority extends SIPHeader 
implements PriorityHeader {
    
         /** constant EMERGENCY field
         */    
	public static final String EMERGENCY=ParameterNames.EMERGENCY;
        
        /** constant URGENT field
         */    
	public static final String URGENT= ParameterNames.URGENT;
        
        /** constant NORMAL field
         */    
	public static final String NORMAL= ParameterNames.NORMAL;
       
        /** constant NON_URGENT field
         */    
	public static final String NON_URGENT= ParameterNames.NON_URGENT;
        /** priority field
         */    
	protected String priority;

        /** Default constructor
         */        
        public Priority() { 
            super(NAME);
        }

        /**
         * Encode into canonical form.
         * @return String
         */
	public String encodeBody() {
		return  priority;
	}
        
	/**
         * get the priority value.
         * @return String
         */
	public String getPriority() {
		return priority;
	}

	/**
         * Set the priority member
         * @param p String to set
         */
	public void setPriority(String p)  throws ParseException{ 
            if (p==null) throw new  NullPointerException("JAIN-SIP Exception,"+
            "Priority, setPriority(), the priority parameter is null");
            priority = p ;
        } 

}
