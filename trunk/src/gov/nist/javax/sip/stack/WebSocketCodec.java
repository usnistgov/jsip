package gov.nist.javax.sip.stack;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import gov.nist.core.CommonLogger;
import gov.nist.core.LogLevels;
import gov.nist.core.StackLogger;

/**
 * 
 * Decodes a web socket frame from wire protocol version 8 format. This code was forked from <a
 * href="https://github.com/joewalnes/webbit">webbit</a> and modified.
 *
 * @author vladimirralev
 *
 */
public class WebSocketCodec {

	private static StackLogger logger = CommonLogger
			.getLogger(WebSocketCodec.class);
    private static final byte OPCODE_CONT = 0x0;
    private static final byte OPCODE_TEXT = 0x1;
    private static final byte OPCODE_BINARY = 0x2;
    private static final byte OPCODE_CLOSE = 0x8;
    private static final byte OPCODE_PING = 0x9;
    private static final byte OPCODE_PONG = 0xA;

    private int fragmentedFramesCount;

    private boolean frameFinalFlag;
    private int frameRsv;
    private int frameOpcode;
    private long framePayloadLength;
    private byte[] maskingKey = new byte[4];

    private final boolean allowExtensions;
    private final boolean maskedPayload;
    State state = null;
    public enum State {
        FRAME_START, MASKING_KEY, PAYLOAD, CORRUPT
    }

    public WebSocketCodec(boolean maskedPayload, boolean allowExtensions) {
        state = State.FRAME_START;
        this.maskedPayload = maskedPayload;
        this.allowExtensions = allowExtensions;
    }

    protected byte[] decode(InputStream is)
            throws Exception {

        switch (state) {
        case FRAME_START:
            framePayloadLength = -1;

            // FIN, RSV, OPCODE
            byte b = (byte) is.read();
            frameFinalFlag = (b & 0x80) != 0;
            frameRsv = (b & 0x70) >> 4;
            frameOpcode = b & 0x0F;

            logger.logDebug("Decoding WebSocket Frame opCode=" + frameOpcode);


            // MASK, PAYLOAD LEN 1
            b = (byte) is.read();
            boolean frameMasked = (b & 0x80) != 0;
            int framePayloadLen1 = b & 0x7F;

            if (frameRsv != 0 && !allowExtensions) {
                protocolViolation("RSV != 0 and no extension negotiated, RSV:" + frameRsv);
                return null;
            }

            if (maskedPayload && !frameMasked) {
                protocolViolation("unmasked client to server frame");
                return null;
            }
            if (frameOpcode > 7) { // control frame (have MSB in opcode set)

                // control frames MUST NOT be fragmented
                if (!frameFinalFlag) {
                    protocolViolation("fragmented control frame");
                    return null;
                }

                // control frames MUST have payload 125 octets or less
                if (framePayloadLen1 > 125) {
                    protocolViolation("control frame with payload length > 125 octets");
                    return null;
                }

                // check for reserved control frame opcodes
                if (!(frameOpcode == OPCODE_CLOSE || frameOpcode == OPCODE_PING || frameOpcode == OPCODE_PONG)) {
                    protocolViolation("control frame using reserved opcode " + frameOpcode);
                    return null;
                }

                // close frame : if there is a body, the first two bytes of the
                // body MUST be a 2-byte unsigned integer (in network byte
                // order) representing a status code
                if (frameOpcode == 8 && framePayloadLen1 == 1) {
                    protocolViolation("received close control frame with payload len 1");
                    return null;
                }
            } else { // data frame
                // check for reserved data frame opcodes
                if (!(frameOpcode == OPCODE_CONT || frameOpcode == OPCODE_TEXT || frameOpcode == OPCODE_BINARY)) {
                    protocolViolation("data frame using reserved opcode " + frameOpcode);
                    return null;
                }

                // check opcode vs message fragmentation state 1/2
                if (fragmentedFramesCount == 0 && frameOpcode == OPCODE_CONT) {
                    protocolViolation("received continuation data frame outside fragmented message");
                    return null;
                }

                // check opcode vs message fragmentation state 2/2
                if (fragmentedFramesCount != 0 && frameOpcode != OPCODE_CONT && frameOpcode != OPCODE_PING) {
                    protocolViolation("received non-continuation data frame while inside fragmented message");
                    return null;
                }
            }

            // Read frame payload length
            if (framePayloadLen1 == 126) {
            	int byte1 = is.read();
            	int byte2 = is.read();
            	int value = (byte1<<8) | byte2;
                framePayloadLength = value;
                //This check fails with chrome!!!
//                if (framePayloadLength < 126) {
//                    protocolViolation("invalid data frame length (not using minimal length encoding)");
//                    return null;
//                }
            } else if (framePayloadLen1 == 127) {
            	long value = 0;
            	for(int q=0;q<8;q++) {
            		value &= is.read()<<(7-q);
            	}
                framePayloadLength = value;

                if (framePayloadLength < 65536) {
                    protocolViolation("invalid data frame length (not using minimal length encoding)");
                    return null;
                }
            } else {
                framePayloadLength = framePayloadLen1;
            }

            if(logger.isLoggingEnabled(LogLevels.TRACE_DEBUG)) {
                logger.logDebug("Decoding WebSocket Frame length=" + framePayloadLength);
            }

            state = State.MASKING_KEY;
        case MASKING_KEY:
            if (maskedPayload) {
            	for(int q=0;q<4;q++)
            		maskingKey[q] = (byte) is.read();
            }
            state = State.PAYLOAD;
        case PAYLOAD:
            
        	payloadIndex += is.read(framePayload, payloadIndex, framePayload.length - payloadIndex);
        	if(payloadIndex < framePayloadLength) return null; // wait for more data
        	payloadIndex = 0;
            // Unmask data if needed
            if (maskedPayload) {
                unmask(framePayload);
            }
            
            byte[] msg = new byte[(int) framePayloadLength];
            System.arraycopy(framePayload, 0, msg, 0, (int) framePayloadLength);
            state = State.FRAME_START;
            return msg;
        case CORRUPT:
            return null;
        default:
            throw new Error("Shouldn't reach here.");
        }
    }
    
    // Sometimes, the payload may not be delivered in 1 nice packet
    // We need to accumulate the data until we have it all
    byte[] framePayload = new byte[66000];
    int payloadIndex = 0;
    
    protected static byte[] encode(byte[] msg, int rsv, boolean fin) throws Exception {

    	boolean maskPayload = false;
    	ByteArrayOutputStream frame = new ByteArrayOutputStream();

    	byte opcode;
    	opcode = OPCODE_TEXT;


    	int length = msg.length;

    	if(logger.isLoggingEnabled(LogLevels.TRACE_DEBUG)) {
    		logger.logDebug("Encoding WebSocket Frame opCode=" + opcode + " length=" + length);
    	}

    	int b0 = 0;
    	if (fin) {
    		b0 |= 1 << 7;
    	}
    	b0 |= rsv % 8 << 4;
    	b0 |= opcode % 128;


    	if (length <= 125) {
    		frame.write(b0);
    		byte b = (byte) (maskPayload ? 0x80 | (byte) length : (byte) length);
    		frame.write(b);
    	} else if (length <= 0xFFFF) {
    		frame.write(b0);
    		frame.write(maskPayload ? 0xFE : 126);
    		frame.write(length >>> 8 & 0xFF);
    		frame.write(length & 0xFF);
    	} else {
    		frame.write(b0);
    		frame.write(maskPayload ? 0xFF : 127);
    		for(int q=0;q<8;q++) {
    			frame.write((0xFF)&(length>>q));
    		}
    	}

    	frame.write(msg);
    	return frame.toByteArray();

    }

    private void unmask(byte[] frame) {
        for (int i = 0; i < frame.length; i++) {
            frame[i] = (byte) (frame[i] ^ maskingKey[i % 4]);
        }
    }
    
    public byte readByte(InputStream is) throws IOException {
    	byte value = (byte) (0xFF&is.read());
    	return value;
    }

    private void protocolViolation(String reason)  {
        state = State.CORRUPT;
     
        throw new RuntimeException(reason);
    }


}
