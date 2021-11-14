/*
* Conditions Of Use
*
* This software was developed by employees of the National Institute of
* Standards and Technology (NIST), and others.
* This software is has been contributed to the public domain.
* As a result, a formal license is not needed to use the software.
*
* This software is provided "AS IS."
* NIST MAKES NO WARRANTY OF ANY KIND, EXPRESS, IMPLIED
* OR STATUTORY, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTY OF
* MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, NON-INFRINGEMENT
* AND DATA ACCURACY.  NIST does not warrant or make any representations
* regarding the use of the software or the results thereof, including but
* not limited to the correctness, accuracy, reliability or usefulness of
* the software.
*
*
*/
/*
 * Created on Jul 31, 2004
 *
 *The JAIN-SIP Project
 */
package gov.nist.javax.sip.parser;

import gov.nist.javax.sip.header.Supported;
import gov.nist.javax.sip.header.SupportedList;
import gov.nist.javax.sip.parser.SupportedParser;

/**
 *
 */
public class SupportedParserTest extends ParserTestCase {

    /* (non-Javadoc)
     * @see gov.nist.javax.sip.parser.ParserTestCase#testParser()
     */
    public void testParser() {

        String content[] = {
            "Supported: 100rel\n",
            "Supported:\n",
            "k:sessiontimer \n"
        };

        super.testParser(SupportedParser.class,content);

    }
    
    /*
     * Test for https://github.com/usnistgov/jsip/issues/53
     * Adding two Supported headers, first one "" causes malformed encoding output:
     * 
     * "Support: ,timer"
     */
    public void testMalformedConstruction() {
    	SupportedList supportedList = new SupportedList();
    	supportedList.add(new Supported(""));
    	supportedList.add(new Supported("timer"));
    	String encoded = supportedList.encode();
    	assertEquals("Supported: timer\r\n", encoded);
    }

}
