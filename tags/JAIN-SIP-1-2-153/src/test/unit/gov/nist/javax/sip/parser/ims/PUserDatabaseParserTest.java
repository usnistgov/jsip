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

import gov.nist.javax.sip.header.HeaderFactoryImpl;
import gov.nist.javax.sip.header.ims.PUserDatabaseHeader;
import gov.nist.javax.sip.parser.ims.PUserDatabaseParser;
import test.unit.gov.nist.javax.sip.parser.ParserTestCase;
/**
 *
 * @author aayush.bhatnagar
 * Rancore Technologies Pvt Ltd, Mumbai India.
 *
 */
public class PUserDatabaseParserTest extends ParserTestCase{

    @Override
    public void testParser() {

        System.out.println("************************************************************");
        System.out.println("parsie parsie here.....");
        System.out.println("************************************************************");

        String p_user_db[] = {"P-User-Database: <aaa://aayush.rancore.com;transport=tcp>\n",
                              "P-User-Database: <aaa://aayush.rancore.com>\n",
                              "P-User-Database: <aaa://10.124.25.210:8080>\n",
                              "P-User-Database: <aaa://10.124.25.210;transport=tcp>\n"};

        super.testParser(PUserDatabaseParser.class, p_user_db);

        System.out.println("************************************************************");
        System.out.println("Now from the perspective of an application (I-CSCF) built on");
        System.out.println("top of the JSIP stack. Let us test the marshaling");
        System.out.println("of the P-User-Database header.");
        System.out.println("************************************************************");

        HeaderFactoryImpl himpl = new HeaderFactoryImpl();
        PUserDatabaseHeader pud = himpl.createPUserDatabaseHeader("rancorehss.rancore.com:5555");
        try {
            pud.setParameter("transport", "tcp");
        } catch (ParseException e) {

            e.printStackTrace();
        }

        System.out.println("The encoded Database name is---> "+pud.getDatabaseName());

        System.out.println("The encoded URI parameter is---> "+pud.getParameter("transport"));

        System.out.println("The encoded header looks like---> "+pud.toString());

    }

}
