/*
 * ReferToParserTest.java
 * 
 * Created on Mar 27, 2005
 * 
 * Created by: M. Ranganathan
 *
 * The JAIN-SIP Project
 * 
 */

package test.unit.gov.nist.javax.sip.parser;

import gov.nist.javax.sip.parser.ReferToParser;

/**
 *
 */
public class ReferToParserTest extends ParserTestCase {
   
    

    	/* (non-Javadoc)
    	 * @see test.unit.gov.nist.javax.sip.parser.ParserTestCase#testParser()
    	 */
    	public void testParser() {
    		
    		String p[] = {
    		    "Refer-To: <sip:dave@denver.example.org?" +
					"Replaces=12345%40192.168.118.3%3Bto-tag%3D12345%3Bfrom-tag%3D5FFE-3994>\n",
				"Refer-To: <sip:+1-650-555-2222@ss1.wcom.com;user=phone>;tag=5617\n",
				"Refer-To: T. A. Watson <sip:watson@bell-telephone.com>\n",
				"Refer-To: LittleGuy <sip:UserB@there.com>\n",
				"Refer-To: sip:mranga@120.6.55.9\n",
				"Refer-To: sip:mranga@129.6.55.9 ; tag=696928473514.129.6.55.9\n"
    	    };
    			
    		super.testParser(ReferToParser.class,p);
    	}



}

