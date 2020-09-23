/**
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 * Unpublished - rights reserved under the Copyright Laws of the United States.
 * Copyright © 2003 Sun Microsystems, Inc. All rights reserved.
 * Copyright © 2005 BEA Systems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
 *
 * This distribution may include materials developed by third parties.
 *
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 *
 * Module Name   : JSIP Specification
 * File Name     : HeaderFactory.java
 * Author        : Phelim O'Doherty
 *
 *  HISTORY
 *  Version   Date      Author              Comments
 *  1.1     08/10/2002  Phelim O'Doherty
 *  1.2     20/12/2005    Jereon Van Bemmel Added create methods for PUBLISH
 *                                          headers
 *  1.2     20/12/2006    Phelim O'Doherty  Added new createCseqHeader with long
 *                                          sequence number
 *~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 */
package javax.sip.header;

import javax.sip.address.*;
import java.text.ParseException;
import javax.sip.InvalidArgumentException;
import java.util.*;

/**
 * This interface provides factory methods that allow an application to create
 * Header object from a particular implementation of JAIN SIP. This class is a
 * singleton and can be retrieved from the
 * {@link javax.sip.SipFactory#createHeaderFactory()}.
 *
 * @author BEA Systems, NIST
 * @version 1.2
 */
public interface HeaderFactory {

    /**
     * Creates a new AcceptEncodingHeader based on the newly supplied encoding
     * value.
     *
     * @param encoding the new string containing the encoding value.
     * @throws ParseException which signals that an error has been reached
     * unexpectedly while parsing the encoding value.
     * @return the newly created AcceptEncodingHeader object.
     */
    public AcceptEncodingHeader createAcceptEncodingHeader(String encoding)
                         throws ParseException;

    /**
     * Creates a new AcceptHeader based on the newly supplied contentType and
     * contentSubType values.
     *
     * @param contentType the new string content type value.
     * @param contentSubType the new string content sub-type value.
     * @throws ParseException which signals that an error has been reached
     * unexpectedly while parsing the content type or content subtype value.
     * @return the newly created AcceptHeader object.
     */
    public AcceptHeader createAcceptHeader(String contentType, String contentSubType)
                                    throws ParseException;


    /**
     * Creates a new AcceptLanguageHeader based on the newly supplied
     * language value.
     *
     * @param language the new Locale value of the language
     * @return the newly created AcceptLanguageHeader object.
     */
    public AcceptLanguageHeader createAcceptLanguageHeader(Locale language);

    /**
     * Creates a new AlertInfoHeader based on the newly supplied alertInfo value.
     *
     * @param alertInfo the new URI value of the alertInfo
     * @return the newly created AlertInfoHeader object.
     */
    public AlertInfoHeader createAlertInfoHeader(URI alertInfo);


    /**
     * Creates a new AllowEventsHeader based on the newly supplied event type
     * value.
     *
     * @param eventType the new string containing the eventType value.
     * @throws ParseException which signals that an error has been reached
     * unexpectedly while parsing the eventType value.
     * @return the newly created AllowEventsHeader object.
     */
    public AllowEventsHeader createAllowEventsHeader(String eventType)
                                    throws ParseException;

    /**
     * Creates a new AllowHeader based on the newly supplied method value.
     *
     * @param method the new string containing the method value.
     * @throws ParseException which signals that an error has been reached
     * unexpectedly while parsing the method value.
     * @return the newly created AllowHeader object.
     */
    public AllowHeader createAllowHeader(String method)
                                    throws ParseException;


    /**
     * Creates a new AuthenticationInfoHeader based on the newly supplied
     * response value.
     *
     * @param response the new string value of the response.
     * @throws ParseException which signals that an error has been reached
     * unexpectedly while parsing the response value.
     * @return the newly created AuthenticationInfoHeader object.
     */
    public AuthenticationInfoHeader createAuthenticationInfoHeader(String response)
                                    throws ParseException;



    /**
     * Creates a new AuthorizationHeader based on the newly supplied
     * scheme value.
     *
     * @param scheme the new string value of the scheme.
     * @throws ParseException which signals that an error has been reached
     * unexpectedly while parsing the scheme value.
     * @return the newly created AuthorizationHeader object.
     */
    public AuthorizationHeader createAuthorizationHeader(String scheme)
                                    throws ParseException;

    /**
     * Creates a new CSeqHeader based on the newly supplied sequence number and
     * method values.
     *
     * @param sequenceNumber the new integer value of the sequence number.
     * @param method the new string value of the method.
     * @throws InvalidArgumentException if supplied sequence number is less
     * than zero.
     * @throws ParseException which signals that an error has been reached
     * unexpectedly while parsing the method value.
     * @return the newly created CSeqHeader object.
     *
     * @deprecated Since 1.2. Use {@link #createCSeqHeader(long, String)} method
     * with type long.
     */
    public CSeqHeader createCSeqHeader(int sequenceNumber, String method)
                             throws ParseException, InvalidArgumentException;

 /**
     * Creates a new CSeqHeader based on the newly supplied sequence number and
     * method values.
     *
     * @param sequenceNumber the new long value of the sequence number.
     * @param method the new string value of the method.
     * @throws InvalidArgumentException if supplied sequence number is less
     * than zero.
     * @throws ParseException which signals that an error has been reached
     * unexpectedly while parsing the method value.
     * @return the newly created CSeqHeader object.
     *
     * @since v1.2
     */
    public CSeqHeader createCSeqHeader(long sequenceNumber, String method)
                             throws ParseException, InvalidArgumentException;


    /**
     * Creates a new CallIdHeader based on the newly supplied callId value.
     *
     * @param callId the new string value of the call-id.
     * @throws ParseException which signals that an error has been reached
     * unexpectedly while parsing the callId value.
     * @return the newly created CallIdHeader object.
     */
    public CallIdHeader createCallIdHeader(String callId) throws ParseException;


    /**
     * Creates a new CallInfoHeader based on the newly supplied callInfo value.
     *
     * @param callInfo the new URI value of the callInfo.
     * @return the newly created CallInfoHeader object.
     */
    public CallInfoHeader createCallInfoHeader(URI callInfo);


    /**
     * Creates a new ContactHeader based on the newly supplied address value.
     *
     * @param address the new Address value of the address.
     * @return the newly created ContactHeader object.
     */
    public ContactHeader createContactHeader(Address address);

    /**
     * Creates a new wildcard ContactHeader. This is used in Register requests
     * to indicate to the server that it should remove all locations the
     * at which the user is currently available. This implies that the
     * following conditions are met:
     * <ul>
     * <li><code>ContactHeader.getAddress.getUserInfo() == *;</code>
     * <li><code>ContactHeader.getAddress.isWildCard() == true;</code>
     * <li><code>ContactHeader.getExpires() == 0;</code>
     * </ul>
     *
     * @return the newly created wildcard ContactHeader.
     */
    public ContactHeader createContactHeader();


    /**
     * Creates a new ContentDispositionHeader based on the newly supplied
     * contentDisposition value.
     *
     * @param contentDispositionType the new string value of the contentDisposition.
     * @throws ParseException which signals that an error has been reached
     * unexpectedly while parsing the contentDisposition value.
     * @return the newly created ContentDispositionHeader object.
     */
    public ContentDispositionHeader createContentDispositionHeader(String contentDispositionType)
                                  throws ParseException;

    /**
     * Creates a new ContentEncodingHeader based on the newly supplied encoding
     * value.
     *
     * @param encoding the new string containing the encoding value.
     * @throws ParseException which signals that an error has been reached
     * unexpectedly while parsing the encoding value.
     * @return the newly created ContentEncodingHeader object.
     */
    public ContentEncodingHeader createContentEncodingHeader(String encoding)
                                  throws ParseException;

    /**
     * Creates a new ContentLanguageHeader based on the newly supplied
     * contentLanguage value.
     *
     * @param contentLanguage the new Locale value of the contentLanguage.
     * @return the newly created ContentLanguageHeader object.
     */
    public ContentLanguageHeader createContentLanguageHeader(Locale contentLanguage);

    /**
     * Creates a new ContentLengthHeader based on the newly supplied contentLength value.
     *
     * @param contentLength the new integer value of the contentLength.
     * @throws InvalidArgumentException if supplied contentLength is less
     * than zero.
     * @return the newly created ContentLengthHeader object.
     */
    public ContentLengthHeader createContentLengthHeader(int contentLength)
                                throws InvalidArgumentException;

    /**
     * Creates a new ContentTypeHeader based on the newly supplied contentType and
     * contentSubType values.
     *
     * @param contentType the new string content type value.
     * @param contentSubType the new string content sub-type value.
     * @throws ParseException which signals that an error has been reached
     * unexpectedly while parsing the content type or content subtype value.
     * @return the newly created ContentTypeHeader object.
     */
    public ContentTypeHeader createContentTypeHeader(String contentType, String contentSubType)
                              throws ParseException;

     /**
     * Creates a new DateHeader based on the newly supplied date value.
     *
     * @param date the new Calender value of the date.
     * @return the newly created DateHeader object.
     */
    public DateHeader createDateHeader(Calendar date);


    /**
     * Creates a new ErrorInfoHeader based on the newly supplied errorInfo value.
     *
     * @param errorInfo the new URI value of the errorInfo.
     * @return the newly created ErrorInfoHeader object.
     */
    public ErrorInfoHeader createErrorInfoHeader(URI errorInfo);

    /**
     * Creates a new EventHeader based on the newly supplied eventType value.
     *
     * @param eventType the new string value of the eventType.
     * @throws ParseException which signals that an error has been reached
     * unexpectedly while parsing the eventType value.
     * @return the newly created EventHeader object.
     */
    public EventHeader createEventHeader(String eventType) throws ParseException;

    /**
     * Creates a new ExpiresHeader based on the newly supplied expires value.
     *
     * @param expires the new integer value of the expires.
     * @throws InvalidArgumentException if supplied expires is less
     * than zero.
     * @return the newly created ExpiresHeader object.
     */
    public ExpiresHeader createExpiresHeader(int expires)
                                    throws InvalidArgumentException;

    /**
     * Creates a new Header based on the newly supplied name and value values.
     * This method can be used to create ExtensionHeaders.
     *
     * @param name the new string name of the Header value.
     * @param value the new string value of the Header.
     * @throws ParseException which signals that an error has been reached
     * unexpectedly while parsing the name or value values.
     * @return the newly created Header object.
     * @see ExtensionHeader
     */
    public Header createHeader(String name, String value) throws ParseException;

    /**
     * Creates a new List of Headers based on a supplied comma seperated
     * list of Header values for a single header name.
     * This method can be used only for SIP headers whose grammar is of the form
     * header = header-name HCOLON header-value *(COMMA header-value) that
     * allows for combining header fields of the same name into a
     * comma-separated list.  Note that the Contact header field allows a
     * comma-separated list  unless the header field
     * value is "*"
     * @param headers the new string comma seperated list of Header values.
     * @throws ParseException which signals that an error has been reached
     * unexpectedly while parsing the headers value or a List of that Header
     * type is not allowed.
     * @return the newly created List of Header objects.
     */
    public List createHeaders(String headers) throws ParseException;


    /**
     * Creates a new FromHeader based on the newly supplied address and
     * tag values.
     *
     * @param address the new Address object of the address.
     * @param tag the new string value of the tag.
     * @throws ParseException which signals that an error has been reached
     * unexpectedly while parsing the tag value.
     * @return the newly created FromHeader object.
     */
    public FromHeader createFromHeader(Address address, String tag)
                       throws ParseException;


    /**
     * Creates a new InReplyToHeader based on the newly supplied callId
     * value.
     *
     * @param callId the new string containing the callId value.
     * @throws ParseException which signals that an error has been reached
     * unexpectedly while parsing the callId value.
     * @return the newly created InReplyToHeader object.
     */
    public InReplyToHeader createInReplyToHeader(String callId)
                                  throws ParseException;
     /**
     * Creates a new MaxForwardsHeader based on the newly supplied maxForwards value.
     *
     * @param maxForwards the new integer value of the maxForwards.
     * @throws InvalidArgumentException if supplied maxForwards is less
     * than zero or greater than 255.
     * @return the newly created MaxForwardsHeader object.
     */
    public MaxForwardsHeader createMaxForwardsHeader(int maxForwards)
                              throws InvalidArgumentException;

    /**
     * Creates a new MimeVersionHeader based on the newly supplied mimeVersion
     * values.
     *
     * @param majorVersion the new integer value of the majorVersion.
     * @param minorVersion the new integer value of the minorVersion.
     * @throws InvalidArgumentException if supplied majorVersion or minorVersion
     * is less than zero.
     * @return the newly created MimeVersionHeader object.
     */
    public MimeVersionHeader createMimeVersionHeader(int majorVersion, int minorVersion)
                              throws InvalidArgumentException;

    /**
     * Creates a new MinExpiresHeader based on the newly supplied minExpires value.
     *
     * @param minExpires the new integer value of the minExpires.
     * @throws InvalidArgumentException if supplied minExpires is less
     * than zero.
     * @return the newly created MinExpiresHeader object.
     */
    public MinExpiresHeader createMinExpiresHeader(int minExpires)
                              throws InvalidArgumentException;


    /**
     * Creates a new OrganizationHeader based on the newly supplied
     * organization value.
     *
     * @param organization the new string value of the organization.
     * @throws ParseException which signals that an error has been reached
     * unexpectedly while parsing the organization value.
     * @return the newly created OrganizationHeader object.
     */
    public OrganizationHeader createOrganizationHeader(String organization)
                               throws ParseException;

    /**
     * Creates a new PriorityHeader based on the newly supplied priority value.
     *
     * @param priority the new string value of the priority.
     * @throws ParseException which signals that an error has been reached
     * unexpectedly while parsing the priority value.
     * @return the newly created PriorityHeader object.
     */
    public PriorityHeader createPriorityHeader(String priority)
                           throws ParseException;

    /**
     * Creates a new ProxyAuthenticateHeader based on the newly supplied
     * scheme value.
     *
     * @param scheme the new string value of the scheme.
     * @throws ParseException which signals that an error has been reached
     * unexpectedly while parsing the scheme value.
     * @return the newly created ProxyAuthenticateHeader object.
     */
    public ProxyAuthenticateHeader createProxyAuthenticateHeader(String scheme)
                                    throws ParseException;

    /**
     * Creates a new ProxyAuthorizationHeader based on the newly supplied
     * scheme value.
     *
     * @param scheme the new string value of the scheme.
     * @throws ParseException which signals that an error has been reached
     * unexpectedly while parsing the scheme value.
     * @return the newly created ProxyAuthorizationHeader object.
     */
    public ProxyAuthorizationHeader createProxyAuthorizationHeader(String scheme)
                                    throws ParseException;

    /**
     * Creates a new ProxyRequireHeader based on the newly supplied optionTag
     * value.
     *
     * @param optionTag the new string OptionTag value.
     * @return the newly created ProxyRequireHeader object.
     * @throws ParseException which signals that an error has been reached
     * unexpectedly while parsing the optionTag value.
     */
    public ProxyRequireHeader createProxyRequireHeader(String optionTag)
                                    throws ParseException;


    /**
     * Creates a new RAckHeader based on the newly supplied rSeqNumber,
     * cSeqNumber and method values.
     *
     * @param rSeqNumber the new integer value of the rSeqNumber.
     * @param cSeqNumber the new integer value of the cSeqNumber.
     * @param method the new string value of the method.
     * @throws InvalidArgumentException if supplied rSeqNumber or cSeqNumber is
     * less than zero or greater than than 2**31-1.
     * @throws ParseException which signals that an error has been reached
     * unexpectedly while parsing the method value.
     * @return the newly created RAckHeader object.
     */
    public RAckHeader createRAckHeader(int rSeqNumber, int cSeqNumber, String method)
                             throws InvalidArgumentException, ParseException;

    /**
     * Creates a new RSeqHeader based on the newly supplied sequenceNumber value.
     *
     * @param sequenceNumber the new integer value of the sequenceNumber.
     * @throws InvalidArgumentException if supplied sequenceNumber is
     * less than zero or greater than than 2**31-1.
     * @return the newly created RSeqHeader object.
     */
    public RSeqHeader createRSeqHeader(int sequenceNumber)
                             throws InvalidArgumentException;

    /**
     * Creates a new ReasonHeader based on the newly supplied reason value.
     *
     * @param protocol the new string value of the protocol.
     * @param cause the new integer value of the cause.
     * @param text the new string value of the text.
     * @throws ParseException which signals that an error has been reached
     * unexpectedly while parsing the protocol or text value.
     * @throws InvalidArgumentException if supplied cause is
     * less than zero.
     * @return the newly created ReasonHeader object.
     */
    public ReasonHeader createReasonHeader(String protocol, int cause, String text)
                                    throws InvalidArgumentException, ParseException;

     /**
     * Creates a new RecordRouteHeader based on the newly supplied address value.
     *
     * @param address the new Address object of the address.
     * @return the newly created RecordRouteHeader object.
     */
    public RecordRouteHeader createRecordRouteHeader(Address address);

     /**
     * Creates a new ReplyToHeader based on the newly supplied address value.
     *
     * @param address the new Address object of the address.
     * @return the newly created ReplyToHeader object.
     */
    public ReplyToHeader createReplyToHeader(Address address);

     /**
     * Creates a new ReferToHeader based on the newly supplied address value.
     *
     * @param address the new Address object of the address.
     * @return the newly created ReferToHeader object.
     */
    public ReferToHeader createReferToHeader(Address address);

    /**
     * Creates a new RequireHeader based on the newly supplied optionTag
     * value.
     *
     * @param optionTag the new string value containing the optionTag value.
     * @throws ParseException which signals that an error has been reached
     * unexpectedly while parsing the List of optionTag value.
     * @return the newly created RequireHeader object.
     */
    public RequireHeader createRequireHeader(String optionTag)
                                    throws ParseException;

    /**
     * Creates a new RetryAfterHeader based on the newly supplied retryAfter
     * value.
     *
     * @param retryAfter the new integer value of the retryAfter.
     * @throws InvalidArgumentException if supplied retryAfter is less
     * than zero.
     * @return the newly created RetryAfterHeader object.
     */
    public RetryAfterHeader createRetryAfterHeader(int retryAfter)
                             throws InvalidArgumentException;


    /**
     * Creates a new RouteHeader based on the newly supplied address value.
     *
     * @param address the new Address object of the address.
     * @return the newly created RouteHeader object.
     */
    public RouteHeader createRouteHeader(Address address);

    /**
     * Creates a new ServerHeader based on the newly supplied List of product
     * values.
     *
     * @param product the new List of values of the product.
     * @throws ParseException which signals that an error has been reached
     * unexpectedly while parsing the List of product values.
     * @return the newly created ServerHeader object.
     */
    public ServerHeader createServerHeader(List product)
                                throws ParseException;

   /**
     * Creates a new SubjectHeader based on the newly supplied subject value.
     *
     * @param subject the new string value of the subject.
     * @throws ParseException which signals that an error has been reached
     * unexpectedly while parsing the subject value.
     * @return the newly created SubjectHeader object.
     */
    public SubjectHeader createSubjectHeader(String subject)
                                throws ParseException;

    /**
     * Creates a new SubscriptionStateHeader based on the newly supplied
     * subscriptionState value.
     *
     * @param subscriptionState the new string value of the subscriptionState.
     * @throws ParseException which signals that an error has been reached
     * unexpectedly while parsing the subscriptionState value.
     * @return the newly created SubscriptionStateHeader object.
     */
    public SubscriptionStateHeader createSubscriptionStateHeader(String subscriptionState)
                                        throws ParseException;


    /**
     * Creates a new SupportedHeader based on the newly supplied optionTag
     * value.
     *
     * @param optionTag the new string containing the optionTag value.
     * @throws ParseException which signals that an error has been reached
     * unexpectedly while parsing the optionTag value.
     * @return the newly created SupportedHeader object.
     */
    public SupportedHeader createSupportedHeader(String optionTag)
                                    throws ParseException;

    /**
     * Creates a new TimeStampHeader based on the newly supplied timeStamp value.
     *
     * @param timeStamp the new float value of the timeStamp.
     * @throws InvalidArgumentException if supplied timeStamp is less
     * than zero.
     * @return the newly created TimeStampHeader object.
     */
    public TimeStampHeader createTimeStampHeader(float timeStamp)
                            throws InvalidArgumentException;

    /**
     * Creates a new ToHeader based on the newly supplied address and
     * tag values.
     *
     * @param address the new Address object of the address.
     * @param tag the new string value of the tag, this value may be null.
     * @throws ParseException which signals that an error has been reached
     * unexpectedly while parsing the tag value.
     * @return the newly created ToHeader object.
     */
    public ToHeader createToHeader(Address address, String tag) throws ParseException;

    /**
     * Creates a new UnsupportedHeader based on the newly supplied optionTag
     * value.
     *
     * @param optionTag the new string containing the optionTag value.
     * @throws ParseException which signals that an error has been reached
     * unexpectedly while parsing the List of optionTag value.
     * @return the newly created UnsupportedHeader object.
     */
    public UnsupportedHeader createUnsupportedHeader(String optionTag)
                              throws ParseException;

    /**
     * Creates a new UserAgentHeader based on the newly supplied List of product
     * values.
     *
     * @param product the new List of values of the product.
     * @throws ParseException which signals that an error has been reached
     * unexpectedly while parsing the List of product values.
     * @return the newly created UserAgentHeader object.
     */
    public UserAgentHeader createUserAgentHeader(List product)
                            throws ParseException;

    /**
     * Creates a new ViaHeader based on the newly supplied uri and branch values.
     *
     * @param host the new string value of the host.
     * @param port the new integer value of the port.
     * @param transport the new string value of the transport.
     * @param branch the new string value of the branch.
     * @throws ParseException which signals that an error has been reached
     * unexpectedly while parsing the host, transport or branch value.
     * @throws InvalidArgumentException if the supplied port is invalid.
     * @return the newly created ViaHeader object.
     */
    public ViaHeader createViaHeader(String host, int port, String transport,
                 String branch) throws ParseException, InvalidArgumentException;

    /**
     * Creates a new WWWAuthenticateHeader based on the newly supplied
     * scheme value.
     *
     * @param scheme the new string value of the scheme.
     * @throws ParseException which signals that an error has been reached
     * unexpectedly while parsing the scheme values.
     * @return the newly created WWWAuthenticateHeader object.
     */
    public WWWAuthenticateHeader createWWWAuthenticateHeader(String scheme)
                                throws ParseException;

    /**
     * Creates a new WarningHeader based on the newly supplied
     * agent, code and comment values.
     *
     * @param agent the new string value of the agent.
     * @param code the new boolean integer of the code.
     * @param comment the new string value of the comment.
     * @throws ParseException which signals that an error has been reached
     * unexpectedly while parsing the agent or comment values.
     * @throws InvalidArgumentException if an invalid integer code is given for
     * the WarningHeader.
     * @return the newly created WarningHeader object.
     */
    public WarningHeader createWarningHeader(String agent, int code, String comment)
                          throws InvalidArgumentException, ParseException;


    /**
     * Creates a new SIP-ETag header with the supplied tag value
     *
     * @param etag the new tag token
     * @return the newly created SIP-ETag header
     * @throws ParseException when an error occurs during parsing of the etag parameter
     *
     * @since 1.2
     */
    public SIPETagHeader createSIPETagHeader( String etag ) throws ParseException;

    /**
     * Creates a new SIP-If-Match header with the supplied tag value
     *
     * @param etag the new tag token
     * @return the newly created SIP-If-Match header
     * @throws ParseException when an error occurs during parsing of the etag parameter
     *
     * @since 1.2
     */
    public SIPIfMatchHeader createSIPIfMatchHeader( String etag ) throws ParseException;
}

