/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/
package gov.nist.core;
import java.net.*;

/**
* Holds the hostname:port.
*
*@version  JAIN-SIP-1.1
*
*@author M. Ranganathan <mranga@nist.gov>  <br/>
*
*<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
*
*/
public final class HostPort  extends GenericObject {
    
	// host / ipv4/ ipv6/ 
        /** host field
         */    
	protected Host  host;

        /** port field
         *
         */        
        protected Integer    port;
	     
        /** Default constructor
         */        
	public HostPort() {
           
		host = null;
		port = null; // marker for not set.
	}
        
        /**
         * Encode this hostport into its string representation.
         * Note that this could be different from the string that has
         * been parsed if something has been edited.
         * @return String
         */
	public String encode() {
		StringBuffer retval = new StringBuffer();
		retval.append(host.encode());
		if (port != null) 
		    retval.append(COLON).append(port.toString());
		return retval.toString();
	}
        
        /** returns true if the two objects are equals, false otherwise.
         * @param other Object to set
         * @return boolean
         */        
        public boolean equals(Object other) {
            if (! this.getClass().equals(other.getClass())) {
                return false;
            }
            HostPort that = (HostPort) other;
	    if ( (this.port == null && that.port != null) ||
		 (this.port != null && that.port == null) ) return false;
	    else if (this.port == that.port && this.host.equals(that.host)) 
		return true;
	    else 
              return this.host.equals(that.host) && this.port.equals(that.port);
        }
   
        /** get the Host field
         * @return host field
         */        
	public	 Host getHost() { 
            return host ;
        } 
                
        /** get the port field
         * @return int
         */        
	public	 int getPort() {
	    if (port == null) {
		return -1; 
	    } else {
		return port.intValue();
	    }
        }
        
       /**
        * Returns boolean value indicating if Header has port
        * @return boolean value indicating if Header has port
        */
        public boolean hasPort() {
            return  port!= null;
        }
        
        /** remove port.
         */        
        public void removePort() {
            port=null;
        }
        
	/**
         * Set the host member
         * @param h Host to set
         */
	public void setHost(Host h) {
            host = h ;
        }
        
	/**
         * Set the port member
         * @param p int to set
         */
	public void setPort(int p) {
	   // -1 is same as remove port.
	    if (p == -1) port = null;
            else port = new Integer(p);
        } 
	
        /** Return the internet address corresponding to the host.
         *@throws java.net.UnkownHostException if host name cannot be resolved.
         *@return the inet address for the host.
         */
        public InetAddress getInetAddress() 
            throws java.net.UnknownHostException{
            if (host == null) return null;
            else return host.getInetAddress();
        }


	public Object clone() {
		HostPort retval = new HostPort();
		if (this.host != null) retval.host = (Host)this.host.clone();
		if (this.port != null) retval.port = new Integer
							(this.port.intValue());
		return retval;
	}
}
