package test.unit.gov.nist.javax.sip.parser.ims;
/*
* Conditions Of Use
*
* This software was developed by employees of the National Institute of
* Standards and Technology (NIST), an agency of the Federal Government.
* Pursuant to title 15 Untied States Code Section 105, works of NIST
* employees are not subject to copyright protection in the United States
* and are considered to be in the public domain.  As a result, a formal
* license is not needed to use the software.
*
* This software is provided by NIST as a service and is expressly
* provided "AS IS."  NIST MAKES NO WARRANTY OF ANY KIND, EXPRESS, IMPLIED
* OR STATUTORY, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTY OF
* MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, NON-INFRINGEMENT
* AND DATA ACCURACY.  NIST does not warrant or make any representations
* regarding the use of the software or the results thereof, including but
* not limited to the correctness, accuracy, reliability or usefulness of
* the software.
*
* Permission to use this software is contingent upon your acceptance
* of the terms of this agreement
*
* .
*
*/
import java.text.ParseException;
import javax.sip.address.AddressFactory;
import gov.nist.javax.sip.address.AddressFactoryImpl;
import gov.nist.javax.sip.header.HeaderFactoryImpl;
import gov.nist.javax.sip.header.ims.PProfileKeyHeader;
import gov.nist.javax.sip.parser.ims.PProfileKeyParser;
import test.unit.gov.nist.javax.sip.parser.ParserTestCase;
/**
 *
 * @author aayush.bhatnagar
 * Rancore Technologies Pvt Ltd, Mumbai India.
 *
 */
public class PProfileKeyParserTest extends ParserTestCase{

    @Override
    public void testParser() {

        System.out.println("*****************************************************");
        System.out.println("parsie parsie.....");
        System.out.println("*****************************************************");

        String p_prof_key[] = {"P-Profile-Key: <sip:chatroom-12@rancore.com>\n",
                               "P-Profile-Key: <sip:chatroom-!.*!@rancore.com>\n",
                               "P-Profile-Key: <sip:chatroom-19A@rancore.com>\n"};

        super.testParser(PProfileKeyParser.class, p_prof_key);

        System.out.println("*****************************************************");
        System.out.println("From the perspective of the application, let us test");
        System.out.println("The creation of this header.....");
        System.out.println("*****************************************************");

        HeaderFactoryImpl himpl = new HeaderFactoryImpl();
        AddressFactory addFactory = new AddressFactoryImpl();

        try {
            PProfileKeyHeader ppkey = himpl.createPProfileKeyHeader
                (addFactory.createAddress("aayush's room",
                         addFactory.createSipURI("aayushzChatRoom-19", "rancoremumbai.com")));

            System.out.println("The newly encoded header is---> "+ppkey.toString());
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
