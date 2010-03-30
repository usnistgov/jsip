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

import gov.nist.javax.sip.parser.ViaParser;
import gov.nist.javax.sip.parser.WarningParser;

public class WarningParserTest extends ParserTestCase {

    public void testParser() {
        // TODO Auto-generated method stub
        String
          warning[] = { "Warning: 307 isi.edu \"Session parameter 'foo' not understood\"\n",
                "Warning: 301 isi.edu \"Incompatible network address type 'E.164'\"\n",
                "Warning: 312 ii.edu \"Soda\", "+ " 351 i.edu \"Inetwork address 'E.164'\" , 323 ii.edu \"Sodwea\"\n",
                "Warning: 399 foo.bar.com \"hold requested\"\n",
                "Warning: 392 192.168.89.71:5060 \"Noisy feedback tells: pid=936 req_src_ip=192.168.89.20 in_uri=sip:xxx@yyyy.org:5061 out_uri=sip:xxx@yyyy.org:5061 via_cnt==1\"\n"
                };

            super.testParser(WarningParser.class,warning);

    }



}
