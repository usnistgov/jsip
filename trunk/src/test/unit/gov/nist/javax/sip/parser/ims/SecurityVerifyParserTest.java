
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

import gov.nist.javax.sip.parser.ims.SecurityVerifyParser;
import test.unit.gov.nist.javax.sip.parser.ParserTestCase;

public class SecurityVerifyParserTest extends ParserTestCase
{
    public void testParser() {
        // TODO Auto-generated method stub

        String[] securityVerify =  {
         "Security-Verify: ipsec-3gpp; alg=hmac-sha-1-96; spi-c=23456789; spi-s=12345678; " +
             "port-c=2468; port-s=1357; q=0.1\n" ,

         // list
         "Security-Verify: ipsec-3gpp; alg=hmac-sha-1-96; spi-c=23456789; spi-s=12345678; " +
             "port-c=2468; port-s=1357; q=0.1, " +

             "ipsec-3gpp; alg=hmac-sha-1-96; spi-c=98765432; spi-s=87654321; " +
             "port-c=8642; port-s=7531; q=0.5 \n"


        };

        super.testParser(SecurityVerifyParser.class,securityVerify);
    }

}
