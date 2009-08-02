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
 *The JAIN-SIP project.
 */
package test.unit.gov.nist.javax.sip.parser;
import gov.nist.javax.sip.address.*;
import gov.nist.javax.sip.parser.*;
import junit.framework.TestCase;

/**
 *
 */
public class URLParserTest extends TestCase {

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        System.out.println("begin test " + getClass().getName());
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
        System.out.println("end test " + getClass().getName());
    }

    public void testURLParser() {
     String urls[] =
        {
             "sip:j.doe@big.com;lr",
           "sip:conference=1234@sip.convedia.com;xyz=pqd",
           "sip:herbivore.ncsl.nist.gov:5070;maddr=129.6.55.251;lc",
          "sip:1-301-975-3664@foo.bar.com;user=phone", "sip:129.6.55.181",
          "sip:herbivore.ncsl.nist.gov:5070;maddr=129.6.55.251?method=INVITE&contact=sip:foo.bar.com",
          "sip:j.doe@big.com",
          "sip:j.doe:secret@big.com;transport=tcp",
          "sip:j.doe@big.com?subject=project",
          "sip:+1-212-555-1212:1234@gateway.com;user=phone" ,
          "sip:1212@gateway.com",
          "sip:alice@10.1.2.3",
          "sip:alice@example.com",
          "sip:alice",
          "sip:alice@registrar.com;method=REGISTER",
          "sip:annc@10.10.30.186:6666;early=no;play=http://10.10.30.186:8080/examples/pin.vxml",
        "tel:+463-1701-4291" ,
        "tel:46317014291" ,
        "http://10.10.30.186:8080/examples/pin.vxml"
        };


                try {
                    for (int i = 0; i < urls.length; i++) {

                        String url = urls[i];
                        URLParser urlParser = new URLParser(url);
                        GenericURI uri = urlParser.parse();
                        assertEquals(uri,new URLParser(uri.encode()).parse());
                    }
                } catch (Exception ex) {
                    fail(getClass().getName());
                }

    }

}
