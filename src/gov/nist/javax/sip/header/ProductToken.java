/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
* See ../../../../doc/uncopyright.html for conditions of use.                  *
* Author: M. Ranganathan (mranga@nist.gov)                                     *
* Modified By:  O. Deruelle (deruelle@nist.gov), added JAVADOC                 *                                                                                   
* Questions/Comments: nist-sip-dev@antd.nist.gov                               *
*******************************************************************************/

package gov.nist.javax.sip.header;

/** Product Token class
 */
public class ProductToken extends SIPObject {
    
        /** name field
         */    
	protected String name;
        
        /** version field
         */        
	protected String version;
	
        /**
         * Return canonical form.
         * @return String
         */
	public String encode() {
		if (version != null) return name + SLASH + version;
		else return name;
	}
        
        /**
         * Return the name field.
         * @return String
         */
	public String getName() {
            return name ;
        } 

	/**
         * Return the version field.
         * @return String
         */
	public String getVersion() {
            return version ;
        } 

	/**
         * Set the name member
         * @param n String to set
         */
	public void setName(String n) {
            name = n ;
        } 

	/**
         * Set the version member
         * @param v String to set
         */
	public void setVersion(String v) {
            version = v ;
        } 
	
}
