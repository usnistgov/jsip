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
 * Created on Jul 31, 2004
 *
 *The JAIN-SIP project
 */
package test.unit.gov.nist.javax.sip.parser;

/**
 *
 */
public class FromParserTest extends ParserTestCase {

    /* (non-Javadoc)
     * @see test.unit.gov.nist.javax.sip.parser.ParserTestCase#testParser()
     */
    public void testParser() {

            String from[] = {
            "From: +00123456 <sip:+00123456;cpc=ordinary@192.168.3.35;user=phone>;tag=0082-000001c3-025d\n",
            "From: foobar at com<sip:4855@166.34.120.100 >;tag=1024181795\n",
            "From: sip:user@company.com\n",
            "From: sip:caller@university.edu\n",
                "From: sip:localhost\n",
                "From: \"A. G. Bell\" <sip:agb@bell-telephone.com> ;tag=a48s\n"
                 };

            super.testParser(gov.nist.javax.sip.parser.FromParser.class,from);

    }

}
