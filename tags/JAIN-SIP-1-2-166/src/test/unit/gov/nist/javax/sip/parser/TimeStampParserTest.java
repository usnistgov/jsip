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
package test.unit.gov.nist.javax.sip.parser;

import gov.nist.javax.sip.parser.TimeStampParser;
import test.unit.gov.nist.javax.sip.parser.ParserTestCase;
public class TimeStampParserTest extends ParserTestCase {

    public void testParser() {
        // TODO Auto-generated method stub
         String timeStamp[] = {
                    "Timestamp: 54 \n",
                    "Timestamp: 52.34 34.5 \n"
                };
         super.testParser(TimeStampParser.class, timeStamp);


    }

}
