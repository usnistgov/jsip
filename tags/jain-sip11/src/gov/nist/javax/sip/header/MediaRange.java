/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
* See ../../../../doc/uncopyright.html for conditions of use.                  *
* Author: M. Ranganathan (mranga@nist.gov)                                     *
* Modified By:  O. Deruelle (deruelle@nist.gov)                                * 
* Questions/Comments: nist-sip-dev@antd.nist.gov                               *
*******************************************************************************/
package gov.nist.javax.sip.header;

import gov.nist.core.*;

import java.util.*;
/**
*   Media Range 
* @see Accept
* @since 0.9
* @version 1.0
* <pre>
* Revisions:
*
* Version 1.0
*    1. Added encode method.
*
* media-range    = ( "STAR/STAR"
*                        | ( type "/" STAR )
*                        | ( type "/" subtype )
*                        ) *( ";" parameter )       
* 
* HTTP RFC 2616 Section 14.1
* </pre>
*/
public class MediaRange  extends SIPObject  {
    
        /** type field
         */    
	protected String  type;
        
        /** subtype field
         */        
	protected String  subtype;


        /** Default constructor
         */        
	public  MediaRange() {
	}

        /** get type field
         * @return String
         */        
	public String getType() {
            return type ;
        }
            
        /** get the subType field.
         * @return String
         */                
	public String getSubtype() {
            return subtype ;
        } 
   
        
	/**
         * Set the type member
         * @param t String to set
         */
	public void setType(String t) {
            type = t ;
        }
        
	/**
         * Set the subtype member
         * @param s String to set
         */
	public void setSubtype(String s) {
            subtype = s ;
        }
        

	/**
         * Encode the object.
         * @return String
         */
	public String encode() {
		String encoding = type + SLASH + subtype;
		return encoding;
	}
       
       
        
}
