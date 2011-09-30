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
/*
 * SdpEncoderImpl.java
 *
 * Created on January 14, 2002, 11:40 AM
 */

package gov.nist.javax.sdp;
import javax.sdp.*;
import java.io.*;

/** Implementation of SDP encoder.
*
*@version  JSR141-PUBLIC-REVIEW (Subject to change).
*
*@author M. Ranganathan   <br/>
*
*
*
*/
public class SdpEncoderImpl {

    /** Creates new SdpEncoderImpl */
    public SdpEncoderImpl() {
    }

    /** Specifies the character set to be used to display the session name and
     * information data. By default, the ISO-10646
     *     character set in UTF-8 encoding is used. If a more compact representation
     * is required, other character sets may be used
     *     such as ISO-8859-1 for Northern European languages.
     * @param enc enc - name of character encoding to use for session name and
     * information data
     * @throws UnsupportedEncodingException if the named encoding is not supported
     */
    public void setEncoding(String enc) throws UnsupportedEncodingException {
        throw new UnsupportedEncodingException("Method not supported");
    }

    /** Specifies whether to try to write "typed-time" fields instead of raw integer
     * values. This
     *     makes the session description more readable but may have an adverse effect on
     *     serialization time.
     *
     *     Ordinarily time values are given in numbers of seconds, but for readability
     * they may be
     *     specified using logical units. From RFC 2327:
     *
     *          To make announcements more compact, times may also be given in units of
     *          days, hours or minutes. The syntax for these is a number immediately
     *          followed by a single case-sensitive character. Fractional units are not
     *          allowed - a smaller unit should be used instead. The following unit
     *          specification characters are allowed:
     *
     *             d - days (86400 seconds)
     *             h - minutes (3600 seconds)
     *             m - minutes (60 seconds)
     *             s - seconds (allowed for completeness but not recommended)
     *
     * @param flag  if true this Outputter should emit "typed" time specs in preference to
     *          untyped times.
     */
    public void setTypedTime(boolean flag) {

    }

    /** Specifies whether to generate "a=rtpmap:" attributes for static RTP/AVP format strings.
     *     This is recommended but makes messages bigger. The default is not to write such
     *     attributes.
     * @param flag  if true "a=rtpmap:" attributes are generated for all "RTP/AVP" formats
     */
    public void setRtpmapAttribute(boolean flag) {

    }

    /** Write the specified SessionDescription to the output stream using the current
     *     settings.
     * @param sd SessionDescription to serialize
     * @param out OutputStream to write serialized SessionDescription to
     * @throws IOException
     */
    public void output(SessionDescription sd, OutputStream out)
        throws IOException {
        if (out instanceof ObjectOutputStream) {
            ObjectOutputStream output = (ObjectOutputStream) out;
            if (sd != null)
                output.writeObject(sd);
            else
                throw new IOException("The parameter is null");
        } else
            throw new IOException("The output stream has to be an instance of ObjectOutputStream");
    }

}
