/*
 * SdpConstants.java
 *
 * Created on January 10, 2002, 10:34 AM
 */

package javax.sdp;

/** The SdpConstants class contains the RTP/AVP related constants.
 *  Please refer to IETF RFC 2327 for a description of SDP.
 * @author  deruelle
 * @version 1.0
 */
public interface SdpConstants {

    /** Constant used to translate between NTP time used in SDP and "native" Java
     * time. NTP time is defined as the number of
     * seconds relative to midnight, January 1, 1900 and Java time is measured in
     * number of milliseconds since midnight, January
     * 1, 1970 UTC (see System#currentTimeMillis()}).
     * The value of this constant is 2208988800L. It can be used to convert between
     * NTP and Java time using the following
     * formulas:
     * ntpTime = (javaTime/1000) + SdpConstants.NTP_CONST;
     * javaTime = (ntpTime - SdpConstants.NTP_CONST) * 1000;
     * The Network Time Protocol (NTP) is defined in RFC 1305.
     */
    public static final long NTP_CONST=2208988800L;

    /** Reserved Payload type.
     * An int greater than or equal to 0 and less than AVP_DEFINED_STATIC_MAX,
     * but has not been assigned a value.
     */
    public static final String RESERVED="0";

    /** Unassigned Payload type.
     * An int greater than or equal to AVP_DEFINED_STATIC_MAX and less than
     * AVP_DYNAMIC_MIN - currently
     * unassigned.
     */
    public static final String UNASSIGNED="35";

    /** Dynamic Payload type.
     * Any int less than 0 or greater than or equal to AVP_DYNAMIC_MIN
     */
     public static final String DYNAMIC="-35";

     /** RTP/AVP Protocol
      */
    public static final String RTP_AVP="RTP/AVP";

    /** RTP mapping attribute.
     *
     * SDP is case-sensitive; RFC2327 specifies 'rtpmap' (all smallcap)
     */
    public static final String RTPMAP="rtpmap";

    /** RTP mapping attribute.
     */
    public static final String FMTP="FMTP";

    /** Static RTP/AVP payload type for the PCMU audio codec.
     */
    public static final int PCMU=0;

    /** Static RTP/AVP payload type for the TENSIXTEEN audio codec.
     */
    public static final int TENSIXTEEN=1;

    /** Static RTP/AVP payload type for the G726_32 audio codec.
     */
    public static final int G726_32=2;

    /** Static RTP/AVP payload type for the GSM audio codec.
     */
    public static final int GSM=3;

    /** Static RTP/AVP payload type for the G723 audio codec.
     */
    public static final int G723=4;

    /** Static RTP/AVP payload type for the DVI4_8000 audio codec
     */
    public static final int DVI4_8000=5;

    /** Static RTP/AVP payload type for the DVI4_16000 audio codec.
     */
    public static final int DVI4_16000=6;

    /** Static RTP/AVP payload type for the LPC audio codec
     */
    public static final int LPC=7;

    /** Static RTP/AVP payload type for the PCMA audio codec.
     */
    public static final int PCMA=8;

    /** Static RTP/AVP payload type for the G722 audio codec.
     */
    public static final int G722=9;

    /** Static RTP/AVP payload type for the L16_2CH audio codec.
     */
    public static final int L16_2CH=10;

    /** Static RTP/AVP payload type for the L16_1CH audio codec.
     */
    public static final int L16_1CH=11;

    /** Static RTP/AVP payload type for QCELP audio codec
     */
    public static final int QCELP=12;

    /** Static RTP/AVP payload type for the CN audio codec.
     */
    public static final int CN=13;

    /** Static RTP/AVP payload type for the MPA audio codec.
     */
    public static final int MPA=14;

    /** Static RTP/AVP payload type for the G728 audio codec.
     */
    public static final int G728=15;

    /** Static RTP/AVP payload type for the DVI4_11025 audio codec
     */
    public static final int DVI4_11025=16;

    /** Static RTP/AVP payload type for the DVI4_22050 audio codec.
     */
    public static final int DVI4_22050=17;

    /** Static RTP/AVP payload type for the G729 audio codec.
     */
    public static final int G729=18;

    /** Static RTP/AVP payload type for the CN audio codec.
     */
    public static final int CN_DEPRECATED=19;

    /** Static RTP/AVP payload type for the CELB video codec.
     */
    public static final int CELB=25;

    /** Static RTP/AVP payload type for the JPEG video codec.
     */
    public static final int JPEG=26;

    /** Static RTP/AVP payload type for the NV video codec
     */
    public static final int NV=28;

    /** Static RTP/AVP payload type for the H261 video codec.
     */
    public static final int H261=31;

    /** Static RTP/AVP payload type for the MPV video codec.
     */
    public static final int MPV=32;

    /** Static RTP/AVP payload type for the MP2T video codec.
     */
    public static final int MP2T=33;

    /** Static RTP/AVP payload type for the H263 video codec.
     */
    public static final int H263=34;

    /** Highest defined static payload type. This is (currently) 35.
     */
    public static final int AVP_DEFINED_STATIC_MAX=35;

    /** The minimum defined dynamic format value
     */
    public static final int AVP_DYNAMIC_MIN=-35;

    /** Names of AVP (Audio-Video Profile) payload types indexed on their static
     * payload types.
     */
    public static final String[] avpTypeNames={ "PCMU",
                                                "1016",
                                                "G721",
                                                "GSM",
                                                "G723",
                                                "DVI4_8000",
                                                "DVI4_16000",
                                                "LPC",
                                                "PCMA",
                                                "G722",
                                                "L16_2CH",
                                                "L16_1CH",
                                                "QCELP",
                                                "CN",
                                                "MPA",
                                                "G728",
                                                "DVI4_11025",
                                                "DVI4_22050",
                                                "G729",
                                                "CN_DEPRECATED",
                                                //"H263",
                                                "UNASSIGNED",
                                                "UNASSIGNED",
                                                "UNASSIGNED",
                                                "UNASSIGNED",
                                                "UNASSIGNED",
                                                "CelB",
                                                "JPEG",
                                                "UNASSIGNED",
                                                "nv",
                                                "UNASSIGNED",
                                                "UNASSIGNED",
                                                "H261",
                                                "MPV",
                                                "MP2T",
                                                "H263",
                                                };

    /** Clock rates for various AVP payload types indexed by their static payload
     * types.
     */
    public static final int[] avpClockRates={   8000,
                                                8000,
                                                8000,
                                                8000,
                                                8000,
                                                8000,
                                                16000,
                                                8000,
                                                8000,
                                                8000,
                                                44100,
                                                44100,
                                                -1,
                                                -1,
                                                90000,
                                                8000,
                                                -1,
                                                -1,
                                                -1,
                                                -1,
                                                -1,
                                                -1,
                                                -1,
                                                -1,
                                                -1,
                                                90000,
                                                90000,
                                                -1,
                                                90000,
                                                -1,
                                                -1,
                                                90000,
                                                90000,
                                                90000,
                                                -1
                                                };

    /** Channels per static type.
     */
     public static final int[] avpChannels={    1,
                                                1,
                                                1,
                                                1,
                                                1,
                                                1,
                                                1,
                                                1,
                                                1,
                                                1,
                                                2,
                                                1,
                                                -1,
                                                -1,
                                                1,
                                                1,
                                                -1,
                                                -1,
                                                -1,
                                                -1,
                                                -1,
                                                -1,
                                                -1,
                                                -1,
                                                -1,
                                                -1,
                                                -1,
                                                -1,
                                                -1,
                                                -1,
                                                -1,
                                                -1,
                                                -1,
                                               -1,
                                                };
}

