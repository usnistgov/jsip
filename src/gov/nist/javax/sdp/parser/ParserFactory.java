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
package gov.nist.javax.sdp.parser;
import java.util.*;
import gov.nist.core.*;
import java.text.ParseException;
import java.lang.reflect.*;

/** Factory for creating parsers for the SDP stuff.
*
*@version 1.2
*
*@author M. Ranganathan   <br/>
*
*
*/
public class ParserFactory {
    private static Hashtable parserTable;
    private static Class[] constructorArgs;
    private static final String packageName =
        PackageNames.SDP_PACKAGE + ".parser";

    private static Class getParser(String parserClass) {
        try {
            return Class.forName(packageName + "." + parserClass);
        } catch (ClassNotFoundException ex) {
            System.out.println("Could not find class");
            ex.printStackTrace();
            System.exit(0);
            return null; // dummy
        }
    }

    static {
        constructorArgs = new Class[1];
        constructorArgs[0] = String.class;
        parserTable = new Hashtable();
        parserTable.put("a", getParser("AttributeFieldParser"));
        parserTable.put("b", getParser("BandwidthFieldParser"));
        parserTable.put("c", getParser("ConnectionFieldParser"));
        parserTable.put("e", getParser("EmailFieldParser"));
        parserTable.put("i", getParser("InformationFieldParser"));
        parserTable.put("k", getParser("KeyFieldParser"));
        parserTable.put("m", getParser("MediaFieldParser"));
        parserTable.put("o", getParser("OriginFieldParser"));
        parserTable.put("p", getParser("PhoneFieldParser"));
        parserTable.put("v", getParser("ProtoVersionFieldParser"));
        parserTable.put("r", getParser("RepeatFieldParser"));
        parserTable.put("s", getParser("SessionNameFieldParser"));
        parserTable.put("t", getParser("TimeFieldParser"));
        parserTable.put("u", getParser("URIFieldParser"));
        parserTable.put("z", getParser("ZoneFieldParser"));
    }

    public static SDPParser createParser(String field) throws ParseException {
        String fieldName = Lexer.getFieldName(field);
        if (fieldName == null)
            return null;
        Class parserClass = (Class) parserTable.get(fieldName.toLowerCase());

        if (parserClass != null) {
            try {

                Constructor cons = parserClass.getConstructor(constructorArgs);
                Object[] args = new Object[1];
                args[0] = field;
                SDPParser retval = (SDPParser) cons.newInstance(args);
                return retval;

            } catch (Exception ex) {
                InternalErrorHandler.handleException(ex);
                return null; // to placate the compiler.
            }
        } else
            throw new ParseException(
                "Could not find parser for " + fieldName,
                0);
    }

}
/*
 * $Log: not supported by cvs2svn $
 * Revision 1.5  2006/07/13 09:02:33  mranga
 * Issue number:
 * Obtained from:
 * Submitted by:  jeroen van bemmel
 * Reviewed by:   mranga
 * Moved some changes from jain-sip-1.2 to java.net
 *
 * CVS: ----------------------------------------------------------------------
 * CVS: Issue number:
 * CVS:   If this change addresses one or more issues,
 * CVS:   then enter the issue number(s) here.
 * CVS: Obtained from:
 * CVS:   If this change has been taken from another system,
 * CVS:   then name the system in this line, otherwise delete it.
 * CVS: Submitted by:
 * CVS:   If this code has been contributed to the project by someone else; i.e.,
 * CVS:   they sent us a patch or a set of diffs, then include their name/email
 * CVS:   address here. If this is your work then delete this line.
 * CVS: Reviewed by:
 * CVS:   If we are doing pre-commit code reviews and someone else has
 * CVS:   reviewed your changes, include their name(s) here.
 * CVS:   If you have not had it reviewed then delete this line.
 *
 * Revision 1.3  2006/06/19 06:47:26  mranga
 * javadoc fixups
 *
 * Revision 1.2  2006/06/16 15:26:28  mranga
 * Added NIST disclaimer to all public domain files. Clean up some javadoc. Fixed a leak
 *
 * Revision 1.1.1.1  2005/10/04 17:12:34  mranga
 *
 * Import
 *
 *
 * Revision 1.3  2005/04/04 09:29:02  dmuresan
 * Replaced new String().getClass() with String.class.
 *
 * Revision 1.2  2004/01/22 13:26:28  sverker
 * Issue number:
 * Obtained from:
 * Submitted by:  sverker
 * Reviewed by:   mranga
 *
 * Major reformat of code to conform with style guide. Resolved compiler and javadoc warnings. Added CVS tags.
 *
 * CVS: ----------------------------------------------------------------------
 * CVS: Issue number:
 * CVS:   If this change addresses one or more issues,
 * CVS:   then enter the issue number(s) here.
 * CVS: Obtained from:
 * CVS:   If this change has been taken from another system,
 * CVS:   then name the system in this line, otherwise delete it.
 * CVS: Submitted by:
 * CVS:   If this code has been contributed to the project by someone else; i.e.,
 * CVS:   they sent us a patch or a set of diffs, then include their name/email
 * CVS:   address here. If this is your work then delete this line.
 * CVS: Reviewed by:
 * CVS:   If we are doing pre-commit code reviews and someone else has
 * CVS:   reviewed your changes, include their name(s) here.
 * CVS:   If you have not had it reviewed then delete this line.
 *
 */
