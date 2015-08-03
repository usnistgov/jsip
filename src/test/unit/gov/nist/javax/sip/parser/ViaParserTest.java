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

import gov.nist.javax.sip.parser.*;


public class ViaParserTest extends ParserTestCase {
    String via[] = {
            "Via: SIP/2.0/UDP 135.180.130.133\n",
            "Via: SIP/2.0/UDP 166.34.120.100;branch=0000045d-00000001"
                    + ",SIP/2.0/UDP 166.35.224.216:5000\n",
            "Via: SIP/2.0/UDP sip33.example.com,"
                    + " SIP/2.0/UDP sip32.example.com (oli),"
                    + "SIP/2.0/UDP sip31.example.com\n",
            "Via: SIP/2.0/UDP host.example.com;received=::133;"
                    + " branch=C1C3344E2710000000E299E568E7potato10potato0potato0\n",
            "Via: SIP/2.0/UDP host.example.com;received=135.180.130.133;"
                    + " branch=C1C3344E2710000000E299E568E7potato10potato0potato0\n",
            "Via: SIP/2.0/UDP company.com:5604 ( Hello )"
                    + ", SIP /  2.0  /  UDP 135.180.130.133\n",
            "Via: SIP/2.0/UDP 129.6.55.9:7060;received=stinkbug.antd.nist.gov\n",
            "Via: SIP/2.0/UDP ss2.wcom.com:5060;branch=721e418c4.1"
                    + ", SIP/2.0/UDP ss1.wcom.com:5060;branch=2d4790.1"
                    + " , SIP/2.0/UDP here.com:5060( Hello the big world) \n",
            "Via: SIP/2.0/UDP ss1.wcom.com:5060;branch=2d4790.1\n",
            "Via: SIP/2.0/UDP first.example.com:4000;ttl=16"
                    + ";maddr=224.2.0.1 ;branch=a7c6a8dlze.1 (Acme server)\n",
            "Via: SIP/2.0/UDP first.example_example.com:4000;ttl=16"
                    + ";maddr=224.2.0.1 ;branch=a7c6a8dlze.1 (Acme __server)\n"};


    public void testParser() {
        super.testParser(ViaParser.class,via);
    }

}
