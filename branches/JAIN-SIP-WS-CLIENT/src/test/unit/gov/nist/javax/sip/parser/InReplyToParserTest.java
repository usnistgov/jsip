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
 *The JAIN-SIP project
 */
package test.unit.gov.nist.javax.sip.parser;

import gov.nist.javax.sip.parser.InReplyToParser;

/**
 *
 */
public class InReplyToParserTest extends ParserTestCase {

    /* (non-Javadoc)
     * @see test.unit.gov.nist.javax.sip.parser.ParserTestCase#testParser()
     */
    public void testParser() {

        String p[] = { "In-Reply-To: 2d809fa3f4ad434c760f23bc130d062b@::6555:2418:5736:aee4:a00:8, henxdhwhw445wdgh107020000@::2425:2418:5736:aee7:a00:8\n",
                "In-Reply-To: 70710@saturn.bell-tel.com, 17320@saturn.bell-tel.com\n",
                "In-Reply-To: 70710 \n"
                };

        super.testParser(InReplyToParser.class,p);
    }

}
