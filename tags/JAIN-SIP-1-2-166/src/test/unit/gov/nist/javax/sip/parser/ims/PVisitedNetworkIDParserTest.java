
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
/************************************************************************************************
 * PRODUCT OF PT INOVACAO - EST DEPARTMENT and Telecommunications Institute (Aveiro, Portugal)  *
 ************************************************************************************************/

package test.unit.gov.nist.javax.sip.parser.ims;

import gov.nist.javax.sip.parser.ims.PVisitedNetworkIDParser;
import test.unit.gov.nist.javax.sip.parser.ParserTestCase;

public class PVisitedNetworkIDParserTest extends ParserTestCase
{
    public void testParser() {
        // TODO Auto-generated method stub

        String[] visitedNet =  {
         "P-Visited-Network-ID: \"visited network 1\"\n",

         "P-Visited-Network-ID: \"visited network 1\", \"visited network 2\"\n",

         "P-Visited-Network-ID: ptinovacao, \"visited network 1\"\n"

        };

        super.testParser(PVisitedNetworkIDParser.class,visitedNet);
    }

}
