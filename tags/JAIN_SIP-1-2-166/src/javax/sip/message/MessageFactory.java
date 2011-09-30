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
 * File Name     : MessageFactory.java
 * Author        : Phelim O'Doherty
 *
 *  HISTORY
 *  Version   Date      Author              Comments
 *  1.1     08/10/2002  Phelim O'Doherty    Initial version
 *  1.2     11/15/2004  M. Ranganathan      Null argument for createSipRequest creates am empty Sip Request
 *                                          Added new method to create a response from a String
 *~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 */
package javax.sip.message;

import java.text.ParseException;
import javax.sip.header.*;
import javax.sip.address.URI;
import java.util.List;

/**
 * This interface provides factory methods that allow an application to create
 * Request and Response messages from a particular implementation of JAIN SIP.
 * This class is a singleton and can be retrieved from the
 * {@link javax.sip.SipFactory#createMessageFactory()}.
 *
 * @author BEA Systems, NIST
 * @version 1.2
 */
public interface MessageFactory {

// Standard Request Creation methods

    /**
     * Creates a new Request message of type specified by the method paramater,
     * containing the URI of the Request, the mandatory headers of the message
     * with a body in the form of a Java object and the body content type.
     *
     * @param requestURI the new URI object of the requestURI value of this Message.
     * @param method the new string of the method value of this Message.
     * @param callId the new CallIdHeader object of the callId value of this Message.
     * @param cSeq the new CSeqHeader object of the cSeq value of this Message.
     * @param from the new FromHeader object of the from value of this Message.
     * @param to the new ToHeader object of the to value of this Message.
     * @param via the new List object of the ViaHeaders of this Message.
     * @param contentType the new ContentTypeHeader object of the content type
     * value of this Message.
     * @param content the new Object of the body content value of this Message.
     * @return the newly created Request object.
     * @throws ParseException which signals that an error has been reached
     * unexpectedly while parsing the method or the body.
     */
    public Request createRequest(URI requestURI, String method, CallIdHeader
            callId, CSeqHeader cSeq, FromHeader from, ToHeader to, List via,
                MaxForwardsHeader maxForwards, ContentTypeHeader contentType,
                Object content) throws ParseException;

    /**
     * Creates a new Request message of type specified by the method paramater,
     * containing the URI of the Request, the mandatory headers of the message
     * with a body in the form of a byte array and body content type.
     *
     * @param requestURI the new URI object of the requestURI value of this Message.
     * @param method the new string of the method value of this Message.
     * @param callId the new CallIdHeader object of the callId value of this Message.
     * @param cSeq the new CSeqHeader object of the cSeq value of this Message.
     * @param from the new FromHeader object of the from value of this Message.
     * @param to the new ToHeader object of the to value of this Message.
     * @param via the new List object of the ViaHeaders of this Message.
     * @param contentType the new ContentTypeHeader object of the content type
     * value of this Message.
     * @param content the new byte array of the body content value of this Message.
     * @return the newly created Request object.
     * @throws ParseException which signals that an error has been reached
     * unexpectedly while parsing the method or the body.
     */
    public Request createRequest(URI requestURI, String method, CallIdHeader
            callId, CSeqHeader cSeq, FromHeader from, ToHeader to, List via,
                MaxForwardsHeader maxForwards, ContentTypeHeader contentType,
                byte[] content) throws ParseException;

    /**
     * Creates a new Request message of type specified by the method paramater,
     * containing the URI of the Request, the mandatory headers of the message.
     * This new Request does not contain a body.
     *
     * @param requestURI the new URI object of the requestURI value of this Message.
     * @param method the new string of the method value of this Message.
     * @param callId the new CallIdHeader object of the callId value of this Message.
     * @param cSeq the new CSeqHeader object of the cSeq value of this Message.
     * @param from the new FromHeader object of the from value of this Message.
     * @param to the new ToHeader object of the to value of this Message.
     * @param via the new List object of the ViaHeaders of this Message.
     * @return the newly created Request object.
     * @throws ParseException which signals that an error has been reached
     * unexpectedly while parsing the method.
     */
    public Request createRequest(URI requestURI, String method, CallIdHeader
            callId, CSeqHeader cSeq, FromHeader from, ToHeader to, List via,
                MaxForwardsHeader maxForwards) throws ParseException;


// generic create message method

    /**
     * Create a new SIP Request object based on a specific string value. This
     * method parses the supplied string into a SIP Request. The request
     * string should only consist of the SIP portion of the Request and not
     * the content.  Supplying a null argument creates an empty SIP Request
     * which may be used to end out "keep alive" messages for a connection.
     *
     * @param request the new string value of the Request.
     * @return the newly created Request object.
     * @throws ParseException which signals that an error has been reached
     * unexpectedly while parsing the request.
     */
    public Request createRequest(String request) throws ParseException;


// Standard Response Creation methods

    /**
     * Creates a new Response message of type specified by the statusCode
     * paramater, containing the mandatory headers of the message with a body
     * in the form of a Java object and the body content type.
     *
     * @param statusCode the new integer of the statusCode value of this Message.
     * @param callId the new CallIdHeader object of the callId value of this Message.
     * @param cSeq the new CSeqHeader object of the cSeq value of this Message.
     * @param from the new FromHeader object of the from value of this Message.
     * @param to the new ToHeader object of the to value of this Message.
     * @param via the new List object of the ViaHeaders of this Message.
     * @param contentType the new ContentTypeHeader object of the content type
     * value of this Message.
     * @param content the new Object of the body content value of this Message.
     * @return the newly created Response object.
     * @throws ParseException which signals that an error has been reached
     * unexpectedly while parsing the statusCode or the body.
     */
    public Response createResponse(int statusCode, CallIdHeader callId,
            CSeqHeader cSeq, FromHeader from, ToHeader to, List via,
                MaxForwardsHeader maxForwards, ContentTypeHeader contentType,
                Object content) throws ParseException;

    /**
     * Creates a new Response message of type specified by the statusCode
     * paramater, containing the mandatory headers of the message with a body
     * in the form of a byte array and the body content type.
     *
     * @param statusCode the new integer of the statusCode value of this Message.
     * @param callId the new CallIdHeader object of the callId value of this Message.
     * @param cSeq the new CSeqHeader object of the cSeq value of this Message.
     * @param from the new FromHeader object of the from value of this Message.
     * @param to the new ToHeader object of the to value of this Message.
     * @param via the new List object of the ViaHeaders of this Message.
     * @param contentType the new ContentTypeHeader object of the content type
     * value of this Message.
     * @param content the new byte array of the body content value of this Message.
     * @return the newly created Response object.
     * @throws ParseException which signals that an error has been reached
     * unexpectedly while parsing the statusCode or the body.
     */
    public Response createResponse(int statusCode, CallIdHeader callId,
            CSeqHeader cSeq, FromHeader from, ToHeader to, List via,
                MaxForwardsHeader maxForwards, ContentTypeHeader contentType,
                byte[] content) throws ParseException;


    /**
     * Creates a new Response message of type specified by the statusCode
     * paramater, containing the mandatory headers of the message. This new
     * Response does not contain a body.
     *
     * @param statusCode the new integer of the statusCode value of this Message.
     * @param callId the new CallIdHeader object of the callId value of this Message.
     * @param cSeq the new CSeqHeader object of the cSeq value of this Message.
     * @param from the new FromHeader object of the from value of this Message.
     * @param to the new ToHeader object of the to value of this Message.
     * @param via the new List object of the ViaHeaders of this Message.
     * @return the newly created Response object.
     * @throws ParseException which signals that an error has been reached
     * unexpectedly while parsing the statusCode.
     */
    public Response createResponse(int statusCode, CallIdHeader callId,
            CSeqHeader cSeq, FromHeader from, ToHeader to, List via,
                MaxForwardsHeader maxForwards) throws ParseException;

// Response Creation methods based on a Request

    /**
     * Creates a new Response message of type specified by the statusCode
     * paramater, based on a specific Request with a new body in the form of a
     * Java object and the body content type. Only the required headers are
     * copied from the Request.
     *
     * @param statusCode the new integer of the statusCode value of this Message.
     * @param request the received Reqest object upon which to base the Response.
     * @param contentType the new ContentTypeHeader object of the content type
     * value of this Message.
     * @param content the new Object of the body content value of this Message.
     * @return the newly created Response object.
     * @throws ParseException which signals that an error has been reached
     * unexpectedly while parsing the statusCode or the body.
     */
    public Response createResponse(int statusCode, Request request,
            ContentTypeHeader contentType, Object content) throws ParseException;

    /**
     * Creates a new Response message of type specified by the statusCode
     * paramater, based on a specific Request with a new body in the form of a
     * byte array and the body content type. Only the required headers are
     * copied from the Request.
     *
     * @param statusCode the new integer of the statusCode value of this Message.
     * @param request the received Reqest object upon which to base the Response.
     * @param contentType the new ContentTypeHeader object of the content type
     * value of this Message.
     * @param content the new byte array of the body content value of this Message.
     * @return the newly created Response object.
     * @throws ParseException which signals that an error has been reached
     * unexpectedly while parsing the statusCode or the body.
     */
    public Response createResponse(int statusCode, Request request,
            ContentTypeHeader contentType, byte[] content) throws ParseException;

    /**
     * Creates a new Response message of type specified by the statusCode
     * paramater, based on a specific Request message. This new Response does
     * not contain a body. Only the required headers are copied from the
     * Request.
     *
     * @param statusCode the new integer of the statusCode value of this Message.
     * @param request the received Reqest object upon which to base the Response.
     * @return the newly created Response object.
     * @throws ParseException which signals that an error has been reached
     * unexpectedly while parsing the statusCode.
     */
    public Response createResponse(int statusCode, Request request)
                                                throws ParseException;


    /**
     * Creates a Response from a String. This method parses the supplied string
     * into a SIP Response. The response string should only consist of the
     * SIP portion of the Response and not the content.
     *
     * @param response is a string representing the response. The argument should
     *  only contain the Sip Headers and not the body of the response.
     * @throws ParseException which signals an error has been reached unexpectedly
     * while parsing the response.
     * @since v1.2
     *
     */
    public Response createResponse( String response) throws ParseException;




}

