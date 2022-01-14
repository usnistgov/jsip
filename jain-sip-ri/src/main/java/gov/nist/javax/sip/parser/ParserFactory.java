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
package gov.nist.javax.sip.parser;
import gov.nist.core.InternalErrorHandler;
import gov.nist.javax.sip.header.SIPHeaderNamesCache;
import gov.nist.javax.sip.header.extensions.Join;
import gov.nist.javax.sip.header.extensions.MinSE;
import gov.nist.javax.sip.header.extensions.References;
import gov.nist.javax.sip.header.extensions.ReferredBy;
import gov.nist.javax.sip.header.extensions.Replaces;
import gov.nist.javax.sip.header.extensions.SessionExpires;
import gov.nist.javax.sip.header.ims.PAccessNetworkInfoHeader;
import gov.nist.javax.sip.header.ims.PAssertedIdentityHeader;
import gov.nist.javax.sip.header.ims.PAssertedServiceHeader;
import gov.nist.javax.sip.header.ims.PAssociatedURIHeader;
import gov.nist.javax.sip.header.ims.PCalledPartyIDHeader;
import gov.nist.javax.sip.header.ims.PChargingFunctionAddressesHeader;
import gov.nist.javax.sip.header.ims.PChargingVectorHeader;
import gov.nist.javax.sip.header.ims.PMediaAuthorizationHeader;
import gov.nist.javax.sip.header.ims.PPreferredIdentityHeader;
import gov.nist.javax.sip.header.ims.PPreferredServiceHeader;
import gov.nist.javax.sip.header.ims.PProfileKeyHeader;
import gov.nist.javax.sip.header.ims.PServedUserHeader;
import gov.nist.javax.sip.header.ims.PUserDatabaseHeader;
import gov.nist.javax.sip.header.ims.PVisitedNetworkIDHeader;
import gov.nist.javax.sip.header.ims.PathHeader;
import gov.nist.javax.sip.header.ims.PrivacyHeader;
import gov.nist.javax.sip.header.ims.SecurityClientHeader;
import gov.nist.javax.sip.header.ims.SecurityServerHeader;
import gov.nist.javax.sip.header.ims.SecurityVerifyHeader;
import gov.nist.javax.sip.header.ims.ServiceRouteHeader;
import gov.nist.javax.sip.parser.extensions.JoinParser;
import gov.nist.javax.sip.parser.extensions.MinSEParser;
import gov.nist.javax.sip.parser.extensions.ReferencesParser;
import gov.nist.javax.sip.parser.extensions.ReferredByParser;
import gov.nist.javax.sip.parser.extensions.ReplacesParser;
import gov.nist.javax.sip.parser.extensions.SessionExpiresParser;
import gov.nist.javax.sip.parser.ims.PAccessNetworkInfoParser;
import gov.nist.javax.sip.parser.ims.PAssertedIdentityParser;
import gov.nist.javax.sip.parser.ims.PAssertedServiceParser;
import gov.nist.javax.sip.parser.ims.PAssociatedURIParser;
import gov.nist.javax.sip.parser.ims.PCalledPartyIDParser;
import gov.nist.javax.sip.parser.ims.PChargingFunctionAddressesParser;
import gov.nist.javax.sip.parser.ims.PChargingVectorParser;
import gov.nist.javax.sip.parser.ims.PMediaAuthorizationParser;
import gov.nist.javax.sip.parser.ims.PPreferredIdentityParser;
import gov.nist.javax.sip.parser.ims.PPreferredServiceParser;
import gov.nist.javax.sip.parser.ims.PProfileKeyParser;
import gov.nist.javax.sip.parser.ims.PServedUserParser;
import gov.nist.javax.sip.parser.ims.PUserDatabaseParser;
import gov.nist.javax.sip.parser.ims.PVisitedNetworkIDParser;
import gov.nist.javax.sip.parser.ims.PathParser;
import gov.nist.javax.sip.parser.ims.PrivacyParser;
import gov.nist.javax.sip.parser.ims.SecurityClientParser;
import gov.nist.javax.sip.parser.ims.SecurityServerParser;
import gov.nist.javax.sip.parser.ims.SecurityVerifyParser;
import gov.nist.javax.sip.parser.ims.ServiceRouteParser;

import java.lang.reflect.Constructor;
import java.text.ParseException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.sip.header.AcceptEncodingHeader;
import javax.sip.header.AcceptHeader;
import javax.sip.header.AcceptLanguageHeader;
import javax.sip.header.AlertInfoHeader;
import javax.sip.header.AllowEventsHeader;
import javax.sip.header.AllowHeader;
import javax.sip.header.AuthenticationInfoHeader;
import javax.sip.header.AuthorizationHeader;
import javax.sip.header.CSeqHeader;
import javax.sip.header.CallIdHeader;
import javax.sip.header.CallInfoHeader;
import javax.sip.header.ContactHeader;
import javax.sip.header.ContentDispositionHeader;
import javax.sip.header.ContentEncodingHeader;
import javax.sip.header.ContentLanguageHeader;
import javax.sip.header.ContentLengthHeader;
import javax.sip.header.ContentTypeHeader;
import javax.sip.header.DateHeader;
import javax.sip.header.ErrorInfoHeader;
import javax.sip.header.EventHeader;
import javax.sip.header.ExpiresHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.InReplyToHeader;
import javax.sip.header.MaxForwardsHeader;
import javax.sip.header.MimeVersionHeader;
import javax.sip.header.MinExpiresHeader;
import javax.sip.header.OrganizationHeader;
import javax.sip.header.PriorityHeader;
import javax.sip.header.ProxyAuthenticateHeader;
import javax.sip.header.ProxyAuthorizationHeader;
import javax.sip.header.ProxyRequireHeader;
import javax.sip.header.RAckHeader;
import javax.sip.header.RSeqHeader;
import javax.sip.header.ReasonHeader;
import javax.sip.header.RecordRouteHeader;
import javax.sip.header.ReferToHeader;
import javax.sip.header.ReplyToHeader;
import javax.sip.header.RequireHeader;
import javax.sip.header.RetryAfterHeader;
import javax.sip.header.RouteHeader;
import javax.sip.header.SIPETagHeader;
import javax.sip.header.SIPIfMatchHeader;
import javax.sip.header.ServerHeader;
import javax.sip.header.SubjectHeader;
import javax.sip.header.SubscriptionStateHeader;
import javax.sip.header.SupportedHeader;
import javax.sip.header.TimeStampHeader;
import javax.sip.header.ToHeader;
import javax.sip.header.UnsupportedHeader;
import javax.sip.header.UserAgentHeader;
import javax.sip.header.ViaHeader;
import javax.sip.header.WWWAuthenticateHeader;
import javax.sip.header.WarningHeader;

/**
 * A factory class that does a name lookup on a registered parser and
 * returns a header parser for the given name.
 *
 * @version 1.2 $Revision: 1.18 $ $Date: 2010-05-06 14:07:44 $
 *
 * @author M. Ranganathan   <br/>
 *
 *
 *
 */
public class ParserFactory {
	//jeand : moving to concurrent structures to avoid blocking witnessed during profiling
    private static Map<String,Class<? extends HeaderParser>> parserTable;
    private static Class[] constructorArgs;
    private static ConcurrentHashMap<Class, Constructor> parserConstructorCache;

    static {
        parserTable = new ConcurrentHashMap<String,Class<? extends HeaderParser>>(90);
        parserConstructorCache = new ConcurrentHashMap<Class, Constructor>();
        constructorArgs = new Class[1];
        constructorArgs[0] = String.class;
        parserTable.put(ReplyToHeader.NAME.toLowerCase(), ReplyToParser.class);

        parserTable.put(
            InReplyToHeader.NAME.toLowerCase(),
            InReplyToParser.class);

        parserTable.put(
            AcceptEncodingHeader.NAME.toLowerCase(),
            AcceptEncodingParser.class);

        parserTable.put(
            AcceptLanguageHeader.NAME.toLowerCase(),
            AcceptLanguageParser.class);

        parserTable.put("t", ToParser.class);
        parserTable.put(ToHeader.NAME.toLowerCase(), ToParser.class);

        parserTable.put(FromHeader.NAME.toLowerCase(), FromParser.class);
        parserTable.put("f", FromParser.class);

        parserTable.put(CSeqHeader.NAME.toLowerCase(), CSeqParser.class);

        parserTable.put(ViaHeader.NAME.toLowerCase(), ViaParser.class);
        parserTable.put("v", ViaParser.class);

        parserTable.put(ContactHeader.NAME.toLowerCase(), ContactParser.class);
        parserTable.put("m", ContactParser.class);

        parserTable.put(
            ContentTypeHeader.NAME.toLowerCase(),
            ContentTypeParser.class);
        parserTable.put("c", ContentTypeParser.class);

        parserTable.put(
            ContentLengthHeader.NAME.toLowerCase(),
            ContentLengthParser.class);
        parserTable.put("l", ContentLengthParser.class);

        parserTable.put(
            AuthorizationHeader.NAME.toLowerCase(),
            AuthorizationParser.class);

        parserTable.put(
            WWWAuthenticateHeader.NAME.toLowerCase(),
            WWWAuthenticateParser.class);

        parserTable.put(CallIdHeader.NAME.toLowerCase(), CallIDParser.class);
        parserTable.put("i", CallIDParser.class);

        parserTable.put(RouteHeader.NAME.toLowerCase(), RouteParser.class);

        parserTable.put(
            RecordRouteHeader.NAME.toLowerCase(),
            RecordRouteParser.class);

        parserTable.put(DateHeader.NAME.toLowerCase(), DateParser.class);

        parserTable.put(
            ProxyAuthorizationHeader.NAME.toLowerCase(),
            ProxyAuthorizationParser.class);

        parserTable.put(
            ProxyAuthenticateHeader.NAME.toLowerCase(),
            ProxyAuthenticateParser.class);

        parserTable.put(
            RetryAfterHeader.NAME.toLowerCase(),
            RetryAfterParser.class);

        parserTable.put(RequireHeader.NAME.toLowerCase(), RequireParser.class);

        parserTable.put(
            ProxyRequireHeader.NAME.toLowerCase(),
            ProxyRequireParser.class);

        parserTable.put(
            TimeStampHeader.NAME.toLowerCase(),
            TimeStampParser.class);

        parserTable.put(
            UnsupportedHeader.NAME.toLowerCase(),
            UnsupportedParser.class);

        parserTable.put(
            UserAgentHeader.NAME.toLowerCase(),
            UserAgentParser.class);

        parserTable.put(
            SupportedHeader.NAME.toLowerCase(),
            SupportedParser.class);
        // bug fix by Steve Crosley
        parserTable.put("k", SupportedParser.class);

        parserTable.put(ServerHeader.NAME.toLowerCase(), ServerParser.class);

        parserTable.put(SubjectHeader.NAME.toLowerCase(), SubjectParser.class);
        parserTable.put( "s", SubjectParser.class); // JvB: added

        parserTable.put(
            SubscriptionStateHeader.NAME.toLowerCase(),
            SubscriptionStateParser.class);

        parserTable.put(
            MaxForwardsHeader.NAME.toLowerCase(),
            MaxForwardsParser.class);

        parserTable.put(
            MimeVersionHeader.NAME.toLowerCase(),
            MimeVersionParser.class);

        parserTable.put(
            MinExpiresHeader.NAME.toLowerCase(),
            MinExpiresParser.class);

        parserTable.put(
            OrganizationHeader.NAME.toLowerCase(),
            OrganizationParser.class);

        parserTable.put(
            PriorityHeader.NAME.toLowerCase(),
            PriorityParser.class);

        parserTable.put(RAckHeader.NAME.toLowerCase(), RAckParser.class);

        parserTable.put(RSeqHeader.NAME.toLowerCase(), RSeqParser.class);

        parserTable.put(ReasonHeader.NAME.toLowerCase(), ReasonParser.class);

        parserTable.put(WarningHeader.NAME.toLowerCase(), WarningParser.class);

        parserTable.put(ExpiresHeader.NAME.toLowerCase(), ExpiresParser.class);

        parserTable.put(EventHeader.NAME.toLowerCase(), EventParser.class);
        parserTable.put("o", EventParser.class);

        parserTable.put(
            ErrorInfoHeader.NAME.toLowerCase(),
            ErrorInfoParser.class);

        parserTable.put(
            ContentLanguageHeader.NAME.toLowerCase(),
            ContentLanguageParser.class);

        parserTable.put(
            ContentEncodingHeader.NAME.toLowerCase(),
            ContentEncodingParser.class);
        parserTable.put("e", ContentEncodingParser.class);

        parserTable.put(
            ContentDispositionHeader.NAME.toLowerCase(),
            ContentDispositionParser.class);

        parserTable.put(
            CallInfoHeader.NAME.toLowerCase(),
            CallInfoParser.class);

        parserTable.put(
            AuthenticationInfoHeader.NAME.toLowerCase(),
            AuthenticationInfoParser.class);

        parserTable.put(AllowHeader.NAME.toLowerCase(), AllowParser.class);

        parserTable.put(
            AllowEventsHeader.NAME.toLowerCase(),
            AllowEventsParser.class);
        parserTable.put("u", AllowEventsParser.class);

        parserTable.put(
            AlertInfoHeader.NAME.toLowerCase(),
            AlertInfoParser.class);

        parserTable.put(AcceptHeader.NAME.toLowerCase(), AcceptParser.class);

        parserTable.put(ReferToHeader.NAME.toLowerCase(), ReferToParser.class);
        // Was missing (bug noticed by Steve Crossley)
        parserTable.put("r", ReferToParser.class);

        // JvB: added to support RFC3903 PUBLISH
        parserTable.put(SIPETagHeader.NAME.toLowerCase(), SIPETagParser.class);
        parserTable.put(SIPIfMatchHeader.NAME.toLowerCase(), SIPIfMatchParser.class);

        //IMS headers
        parserTable.put(PAccessNetworkInfoHeader.NAME.toLowerCase(), PAccessNetworkInfoParser.class);
        parserTable.put(PAssertedIdentityHeader.NAME.toLowerCase(), PAssertedIdentityParser.class);
        parserTable.put(PPreferredIdentityHeader.NAME.toLowerCase(), PPreferredIdentityParser.class);
        parserTable.put(PChargingVectorHeader.NAME.toLowerCase(), PChargingVectorParser.class);
        parserTable.put(PChargingFunctionAddressesHeader.NAME.toLowerCase(), PChargingFunctionAddressesParser.class);
        parserTable.put(PMediaAuthorizationHeader.NAME.toLowerCase(), PMediaAuthorizationParser.class);
        parserTable.put(PathHeader.NAME.toLowerCase(), PathParser.class);
        parserTable.put(PrivacyHeader.NAME.toLowerCase(), PrivacyParser.class);
        parserTable.put(ServiceRouteHeader.NAME.toLowerCase(), ServiceRouteParser.class);
        parserTable.put(PVisitedNetworkIDHeader.NAME.toLowerCase(), PVisitedNetworkIDParser.class);
        
        // added for more P-header extensions for IMS :
        parserTable.put(PServedUserHeader.NAME.toLowerCase(), PServedUserParser.class);
        parserTable.put(PPreferredServiceHeader.NAME.toLowerCase(), PPreferredServiceParser.class);
        parserTable.put(PAssertedServiceHeader.NAME.toLowerCase(), PAssertedServiceParser.class);
        parserTable.put(PProfileKeyHeader.NAME.toLowerCase(), PProfileKeyParser.class);
        parserTable.put(PUserDatabaseHeader.NAME.toLowerCase(), PUserDatabaseParser.class);
        

        parserTable.put(PAssociatedURIHeader.NAME.toLowerCase(), PAssociatedURIParser.class);
        parserTable.put(PCalledPartyIDHeader.NAME.toLowerCase(), PCalledPartyIDParser.class);

        parserTable.put(SecurityServerHeader.NAME.toLowerCase(), SecurityServerParser.class);
        parserTable.put(SecurityClientHeader.NAME.toLowerCase(), SecurityClientParser.class);
        parserTable.put(SecurityVerifyHeader.NAME.toLowerCase(), SecurityVerifyParser.class);


        // Per RFC 3892 (pmusgrave)
        parserTable.put(ReferredBy.NAME.toLowerCase(), ReferredByParser.class);
        parserTable.put("b", ReferToParser.class);

        // Per RFC4028 Session Timers (pmusgrave)
        parserTable.put(SessionExpires.NAME.toLowerCase(), SessionExpiresParser.class);
        parserTable.put("x", SessionExpiresParser.class);
        parserTable.put(MinSE.NAME.toLowerCase(), MinSEParser.class);
        // (RFC4028 does not give a short form header for MinSE)

        // Per RFC3891 (pmusgrave)
        parserTable.put(Replaces.NAME.toLowerCase(), ReplacesParser.class);

        // Per RFC3911 (jean deruelle)
        parserTable.put(Join.NAME.toLowerCase(), JoinParser.class);
        
        //http://tools.ietf.org/html/draft-worley-references-05
        parserTable.put(References.NAME.toLowerCase(), ReferencesParser.class);
        
    }

    /** 
    * This method is added to support parser extensibility.
    */
    public static void addToParserTable(String headerName, Class<? extends HeaderParser> parserClass) {
       parserTable.put(headerName.toLowerCase(), parserClass);
    }

    /**
     * create a parser for a header. This is the parser factory.
     */
    public static HeaderParser createParser(String line)
        throws ParseException {
        String headerName = Lexer.getHeaderName(line);
        String headerValue = Lexer.getHeaderValue(line);
        if (headerName == null || headerValue == null)
            throw new ParseException("The header name or value is null", 0);

        Class parserClass = (Class) parserTable.get(SIPHeaderNamesCache.toLowerCase(headerName));
        if (parserClass != null) {
            try {
                Constructor cons = (Constructor) parserConstructorCache.get(parserClass);
                if (cons == null) {
                    cons = parserClass.getConstructor(constructorArgs);
                    parserConstructorCache.putIfAbsent(parserClass, cons);
                }
                Object[] args = new Object[1];
                args[0] = line;
                HeaderParser retval = (HeaderParser) cons.newInstance(args);
                return retval;

            } catch (Exception ex) {
                InternalErrorHandler.handleException(ex);
                return null; // to placate the compiler.
            }

        } else {
            // Just generate a generic SIPHeader. We define
            // parsers only for the above.
            return new HeaderParser(line);
        }
    }
}
/*
 * $Log: not supported by cvs2svn $
 * Revision 1.17  2010/01/12 00:05:25  mranga
 * Add support for References header draft-worley-references-05
 *
 * Revision 1.16  2009/07/17 18:58:01  emcho
 * Converts indentation tabs to spaces so that we have a uniform indentation policy in the whole project.
 *
 * Revision 1.15  2009/01/22 19:33:48  deruelle_jean
 * Add support for JOIN (RFC 3911)
 * Issue number:  186
 * Obtained from:
 * Submitted by:  Jean Deruelle
 * Reviewed by:   Ranga, The high priest and grand poobah of Jain-SIP
 *
 * Revision 1.14  2007/03/07 14:29:46  belangery
 * Yet another bunch of improvements in the parsing code.
 *
 * Revision 1.13  2007/02/23 14:56:06  belangery
 * Added performance improvement around header name lowercase conversion.
 *
 * Revision 1.12  2007/01/08 19:24:21  mranga
 * Issue number:
 * Obtained from:
 * Submitted by:  Miguel Freitas
 * Reviewed by:   mranga
 *
 * Miguel -- please implement a deep clone method for the IMS headers.
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
 * Revision 1.11  2006/10/12 11:57:54  pmusgrave
 * Issue number:  79, 80
 * Submitted by:  pmusgrave@newheights.com
 * Reviewed by:   mranga
 *
 * Revision 1.10  2006/09/29 19:40:50  jbemmel
 * fixed missing IMS header parsing plumbing
 *
 * Revision 1.9  2006/09/11 18:41:32  mranga
 * Issue number:
 * Obtained from:
 * Submitted by:  mranga
 * Reviewed by:
 * Tighter integration of IMS headers.
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
 * Revision 1.8  2006/08/15 21:44:50  mranga
 * Issue number:
 * Obtained from:
 * Submitted by:  mranga
 * Reviewed by:   mranga
 * Incorporating the latest API changes from Phelim
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
 * Revision 1.7  2006/07/13 09:02:06  mranga
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
 * Revision 1.5  2006/06/19 06:47:27  mranga
 * javadoc fixups
 *
 * Revision 1.4  2006/06/16 15:26:28  mranga
 * Added NIST disclaimer to all public domain files. Clean up some javadoc. Fixed a leak
 *
 * Revision 1.3  2005/10/27 20:49:00  jeroen
 * added support for RFC3903 PUBLISH
 *
 * Revision 1.2  2005/10/14 19:59:00  jeroen
 * bugfix: missing parser for shortform of Subject (s)
 *
 * Revision 1.1.1.1  2005/10/04 17:12:35  mranga
 *
 * Import
 *
 *
 * Revision 1.4  2005/04/04 09:29:03  dmuresan
 * Replaced new String().getClass() with String.class.
 *
 * Revision 1.3  2004/01/22 13:26:31  sverker
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
