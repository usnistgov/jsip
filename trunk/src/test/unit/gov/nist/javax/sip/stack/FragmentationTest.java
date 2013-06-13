package test.unit.gov.nist.javax.sip.stack;

import java.io.ByteArrayInputStream;

import gov.nist.javax.sip.stack.WebSocketCodec;
import junit.framework.TestCase;

public class FragmentationTest extends TestCase {
	byte [] raw1 = new byte[] {-127};

	byte [] raw2 = new byte[] {-2, 1, };
	
	byte [] raw3 = new byte[] {119, 5, 49, -66, -31};
	
	byte [] raw4 = new byte[] {-31, 87, 116, -7, -88, 86, 101, -5, -77, 37, 66, -41, -111, 63, 67, -47, -106, 96, 95, -98, -78, 76, 97, -111, -45, 43, 1, -77, -21, 70, 80, -46, -115, 40, 120, -6, -37, 37, 0, -115, -42, 52, 1, -119, -39, 61, 0, -115, -40, 53, 4, -77, -21, 70, 98, -37, -112, 63, 17, -113, -63, 87, 116, -7, -88, 86, 101, -5, -77, 8, 59, -8, -109, 106, 92, -124, -63, 57, 66, -41, -111, 63, 67, -47, -106, 96, 95, -2, -125, 96, 95, -37, -121, 108, 69, -51, -49, 102, 94, -45, -33, 62, 69, -33, -122, 56, 0, -115, -42, 52, 1, -119, -39, 61, 0, -115, -40, 52, 3, -77, -21, 81, 94, -124, -63, 57, 66, -41, -111, 63, 67, -47, -106, 96, 95, -2, -125, 96, 95, -37, -121, 108, 69, -51, -49, 102, 94, -45, -33, 8, 59, -24, -120, 100, 11, -98, -78, 76, 97, -111, -45, 43, 1, -111, -74, 86, 17, -14, -42, 118, 99, -19, -47, 75, 69, -52, -116, 102, 75, -112, -120, 107, 71, -33, -115, 108, 85, -123, -125, 119, 80, -48, -126, 109, 12, -60, -40, 109, 118, -118, -125, 78, 28, -115, -45, 54, 9, -109, -39, 100, 7, -38, -124, 49, 83, -33, -121, 54, 84, -116, -40, 96, 84, -114, -42, 51, 84, -121, -126, 61, 9, -120, -47, 100, 85, -113, -48, 100, 85, -117, -38, 119, 65, -47, -109, 113, 60, -76, -84, 100, 73, -109, -89, 106, 67, -55, -128, 119, 85, -51, -37, 37, 6, -114, -20, 15, 100, -51, -124, 119, 28, -1, -122, 96, 95, -54, -37, 37, 67, -47, -106, 96, 95, -77, -21, 64, 73, -50, -120, 119, 84, -51, -37, 37, 2, -120, -47, 8, 59, -3, -114, 107, 69, -33, -126, 113, 11, -98, -35, 118, 88, -50, -37, 119, 94, -55, -124, 107, 113, -14, -42, 118, 99, -19, -47, 75, 69, -52, -116, 102, 75, -112, -120, 107, 71, -33, -115, 108, 85, -123, -107, 119, 80, -48, -110, 117, 94, -52, -107, 56, 70, -51, -33, 8, 59, -3, -114, 107, 69, -37, -113, 113, 28, -14, -124, 107, 86, -54, -119, 63, 17, -114, -20, 15, 60, -76,
			
			-127};
	
	public void testWebsocketCodecFragmentationInAllSectionsWithOverflow() throws Exception {
		ByteArrayInputStream bais1 = new ByteArrayInputStream(raw1);
		ByteArrayInputStream bais2 = new ByteArrayInputStream(raw2);
		ByteArrayInputStream bais3 = new ByteArrayInputStream(raw3);
		ByteArrayInputStream bais4 = new ByteArrayInputStream(raw4);
		ByteArrayInputStream bais5 = new ByteArrayInputStream(raw2);
		ByteArrayInputStream bais6 = new ByteArrayInputStream(raw3);
		ByteArrayInputStream bais7 = new ByteArrayInputStream(raw4);
		WebSocketCodec codec = new WebSocketCodec(true, false);
		assertNull(codec.decode(bais1));
		assertNull(codec.decode(bais2));
		assertNull(codec.decode(bais3));
		assertNotNull(codec.decode(bais4));
		assertNull(codec.decode(bais5));
		assertNull(codec.decode(bais6));
		assertNotNull(codec.decode(bais7));
	}
}
