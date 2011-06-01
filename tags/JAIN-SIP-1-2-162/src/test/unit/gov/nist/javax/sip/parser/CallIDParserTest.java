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
 * Created on Jul 26, 2004
 *
 *The Open SLEE project
 */
package test.unit.gov.nist.javax.sip.parser;
import gov.nist.javax.sip.parser.*;

/**
 *
 */
public class CallIDParserTest extends ParserTestCase {



    public void testParser() {
        String call[] = {
                "Call-ID: f0b40bcc-3485-49e7-ad1a-f1dfad2e39c9@10.5.0.53\n",
                "Call-ID: f81d4fae-7dec-11d0-a765-00a0c91e6bf6@foo.bar.com\n",
                "i:f81d4fae-7dec-11d0-a765-00a0c91e6bf6@foo.bar.com\n",
                "Call-ID: 1@10.0.0.1\n",
                "Call-ID: kl24ahsd546folnyt2vbak9sad98u23naodiunzds09a3bqw0sdfbsk34poouymnae0043nsed09mfkvc74bd0cuwnms05dknw87hjpobd76f\n",
                "Call-Id: 281794\n" };
        super.testParser(CallIDParser.class,call);
    }


}
