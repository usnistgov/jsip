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

