
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

package gov.nist.javax.sip.parser.ims;

import gov.nist.javax.sip.parser.ims.PAccessNetworkInfoParser;
import gov.nist.javax.sip.parser.ParserTestCase;

public class PAccessNetworkInfoParserTest extends ParserTestCase
{
    public void testParser() {
        // TODO Auto-generated method stub

        String[] accessNetworkInfo =  {

        		"P-Access-Network-Info: 3GPP-UTRAN-TDD; utran-cell-id-3gpp=23456789ABCDE; [123:4::abcd]; rand=l\n",
        		"P-Access-Network-Info: 3GPP-UTRAN-TDD; utran-cell-id-3gpp=23456789ABCDE; a-b.c1; rand=l\n",
        		"P-Access-Network-Info: 3GPP-UTRAN-TDD; utran-cell-id-3gpp=23456789ABCDE; 127.0.0.1; rand=l\n",
        		"P-Access-Network-Info: 3GPP-UTRAN-TDD; utran-cell-id-3gpp=23456789ABCDE;\"\"\n",
        		"P-Access-Network-Info: 3GPP-UTRAN-TDD; utran-cell-id-3gpp=23456789ABCDE;\";\"\n",
        		"P-Access-Network-Info: 3GPP-UTRAN-TDD; utran-cell-id-3gpp=23456789ABCDE;\"ip=123.123.123.123\"\n",
        		"P-Access-Network-Info: 3GPP-UTRAN-TDD; utran-cell-id-3gpp=23456789ABCDE; [123:4::abcd];rand=l\n",
        		"P-Access-Network-Info: 3GPP-UTRAN-TDD; utran-cell-id-3gpp=23456789ABCDE; [123:4::abcd]\n",
                "P-Access-Network-Info: IEEE-802.11\n",
                "P-Access-Network-Info: 3GPP-UTRAN-TDD; utran-cell-id-3gpp=23456789ABCDE\n"

        };

        super.testParser(PAccessNetworkInfoParser.class,accessNetworkInfo);
        
        //test one more
        String[] accessNetworkInfo_2 =  {
                "P-Access-Network-Info: IEEE-802.11\n",
                "P-Access-Network-Info: IEEE-802.11, 3GPP-UTRAN-TDD; utran-cell-id-3gpp=23456789ABCDE\n",
                "P-Access-Network-Info: IEEE-802.11, 3GPP-UTRAN-TDD; utran-cell-id-3gpp=23456789ABCDE, 3GPP-UTRAN-TDD; utran-cell-id-3gpp=23456789ABCDF\n",
                "P-Access-Network-Info: 3GPP-E-UTRAN; utran-cell-id-3gpp=262010063F423802;network-provided,3GPP-E-UTRAN-FDD; utran-cell-id-3gpp=262010063F423802\n",
                "P-Access-Network-Info: IEEE-802.11;i-wlan-node-id=74da38582ba4\n",
                "P-Access-Network-Info: 3GPP-E-UTRAN-FDD;utran-cell-id-3gpp=262010063f423802\n",
                "P-Access-Network-Info: 3GPP-E-UTRAN;utran-cell-id-3gpp=\"262010063F423802\";network-provided,3GPP-E-UTRAN-FDD;utran-cell-id-3gpp=262010063f423802\n"
        };
        super.testParser(PAccessNetworkInfoParser.class,accessNetworkInfo_2);
        String token = "A1-.%*_+`'~";
        String[] accessNetworkInfo_3 =  {
                "P-Access-Network-Info: IEEE-802.11; network-provided\n",
                 "P-Access-Network-Info: IEEE-802.11; ipv4=[2345:3456::]\n",
                 //token
                 "P-Access-Network-Info: "+token+"\n",
                 //hostname
                "P-Access-Network-Info: IEEE-802.11;AaB123=www.example-test.example.com\n",
                "P-Access-Network-Info: IEEE-802.11;AaB123=www.example-test.example.com.\n",
                //IPv4address 
                "P-Access-Network-Info: IEEE-802.11;AaB123=1.1.1.1\n",
                "P-Access-Network-Info: IEEE-802.11;AaB123=0.0.0.0\n",
                //IPv6reference
                "P-Access-Network-Info: IEEE-802.11;AaB123=[::]\n",
                "P-Access-Network-Info: IEEE-802.11;AaB123=[88A]\n",
                "P-Access-Network-Info: IEEE-802.11;AaB123=[88A::]\n",
                "P-Access-Network-Info: IEEE-802.11;AaB123=[88A::99B]\n",
                "P-Access-Network-Info: IEEE-802.11;AaB123=[88A:99B::]\n",
                "P-Access-Network-Info: IEEE-802.11;AaB123=[88A:99B::11C]\n",
                "P-Access-Network-Info: IEEE-802.11;AaB123=[88A:99B::11C:22D]\n",
                "P-Access-Network-Info: IEEE-802.11;AaB123=[::11C]\n",
                "P-Access-Network-Info: IEEE-802.11;AaB123=[11C::22D]\n",
                //hexpart [ ":" IPv4address ]
                "P-Access-Network-Info: IEEE-802.11;AaB123=[11C::12.04.02.99]\n",
                //IPv6reference quoted
                "P-Access-Network-Info: IEEE-802.11;AaB123=\"[11C::12.04.02.99]\"\n",
                //Multiple IP6
                "P-Access-Network-Info: IEEE-802.11;AaB123=[88A:99B::11C:22D];cc231=[11C::12.04.02.99]\n",
        };
        super.testParser(PAccessNetworkInfoParser.class,accessNetworkInfo_3);

    }


}
