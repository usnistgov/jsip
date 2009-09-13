
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

import gov.nist.javax.sip.parser.ims.PChargingFunctionAddressesParser;
import test.unit.gov.nist.javax.sip.parser.*;

/**
 * @author Miguel Freitas
 */

public class PChargingFunctionAddressesParserTest extends ParserTestCase
{
    public void testParser() {
        // TODO Auto-generated method stub

        String[] chargAddr =  {
         "P-Charging-Function-Addresses: ccf=test1.ims.test; ecf=testevent\n",

         "P-Charging-Function-Addresses: ccf=token; ecf=\"test quoted str\"\n",

         "P-Charging-Function-Addresses: ccf=192.1.1.1; ccf=192.1.1.2; ecf=192.1.1.3; ecf=192.1.1.4\n",
         
         "P-Charging-Function-Addresses: ccf=1.2.3.4; ccf=2.3.4.5;ecf=1.2.3.4; ecf=4.5.6.7\n"
         

         /*
         "P-Charging-Function-Addresses: ccf=[5555::b99:c88:d77:e66]; ccf=[5555::a55:b44:c33:d22]; " +
             "ecf=[5555::1ff:2ee:3dd:4cc]; ecf=[5555::6aa:7bb:8cc:9dd]\n"
         */

        };

        super.testParser(PChargingFunctionAddressesParser.class,chargAddr);
    }

}
