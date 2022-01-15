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
 * Created on Jul 25, 2004
 *
 *The Open SLEE project
 */
package gov.nist.javax.sip.parser;

import gov.nist.javax.sip.parser.*;

/**
 *
 */
public class AllowEventsParserTest extends ParserTestCase {



    public void testParser() {
        String r[] = { "Allow-Events: pack1.pack2, pack3 , pack4\n",
                "Allow-Events: pack1\n" /* empty not allowed */ };
        super.testParser(AllowEventsParser.class,r);
    }

}
