/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/
package gov.nist.javax.sip.header;
import gov.nist.core.*;

/**
*  Protocol name and version.
*
*@version  JAIN-SIP-1.1
*
*@author M. Ranganathan <mranga@nist.gov>  <br/>
*
*<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
*
*/
public class Protocol  extends SIPObject {
    
        /** protocolName field
         */    
	protected String protocolName;
        
        /** protocolVersion field
         */        
	protected String protocolVersion;
        
        /** transport field
         */        
	protected String transport;
        
        /**
         * Compare two To headers for equality.
         * @return true if the two headers are the same.
         * @param other Object to set
         */   
        public boolean equals(Object other) {
            if (! other.getClass().equals(this.getClass())) {
                return false;
            }
            Protocol that = (Protocol) other;
            if (this.protocolName.compareToIgnoreCase(that.protocolName) != 0) {
                return false;
            }
            if (this.transport.compareToIgnoreCase(that.transport) != 0) {
                return false;
            }
            if (this.protocolVersion.compareTo(that.protocolVersion) != 0) {
                return false;
            }
            return true;
        }
        
        /**
         * Return canonical form.
         * @return String
         */  
        public String encode() {
            return protocolName.toUpperCase() + SLASH + 
		protocolVersion +
                SLASH + transport.toUpperCase();
        }

        /** get the protocol name
         * @return String
         */        
	public String getProtocolName() {
            return protocolName ;
        }
            
        /** get the protocol version
         * @return String
         */        
	public String getProtocolVersion() {
            return protocolVersion ;
        }
        
        /** get the transport
         * @return String
         */        
	public String getTransport() {
            return transport ;
        }
        
	/**
         * Set the protocolName member
         * @param p String to set
         */
	public void setProtocolName(String p) {
            protocolName = p ;
        }
        
	/**
         * Set the protocolVersion member
         * @param p String to set
         */
	public void setProtocolVersion(String p) {
            protocolVersion = p ;
        }
        
	/**
         * Set the transport member
         * @param t String to set
         */
	public void setTransport(String t) {
            transport = t ;
        }


	/** 
	* Default constructor.
	*/
	public Protocol() {
		protocolName = "SIP";
		protocolVersion = "2.0";
		transport = "UDP";	
	}
        
}
