package gov.nist.javax.sip.parser;
import  java.io.*;
import java.util.*;

/** Input class for the pipelined parser.
*/

public class Pipeline extends  InputStream {
	private LinkedList buffList;
	private Buffer currentBuffer;
	private boolean isClosed;

	class Buffer {
		byte[] bytes;
		int length ;
		int ptr;
		public Buffer (byte[] bytes, int length) {
			ptr = 0;
			this.length  = length;
			this.bytes = bytes;
		}
		public int getNextByte() {
		    int retval =  (int) bytes[ptr ++ ];
		    return retval;
		}
			
	}



	public Pipeline () {
		buffList = new LinkedList();
	}

	public void write (byte [] bytes, int start, int length) 
		throws IOException {
		if (this.isClosed) 
			throw new IOException ("Closed!!");
		Buffer buff = new Buffer(bytes,length);
		buff.ptr = start;
		synchronized (this.buffList) {
			buffList.add(buff);
			buffList.notifyAll();
		}
	}
	public void write (byte [] bytes) throws IOException {
		if (this.isClosed) 
			throw new IOException ("Closed!!");
		Buffer buff = new Buffer(bytes,bytes.length);
		synchronized (this.buffList) {
			buffList.add(buff);
			buffList.notifyAll();
		}
	}

	public void close() {
		this.isClosed = true;
		synchronized(this.buffList) {
			this.buffList.notifyAll();
		}
	}

	public int read() {
		if (this.isClosed) return -1;
		synchronized (this.buffList) {
		    if (currentBuffer != null && currentBuffer.ptr <
			currentBuffer.length)  {
			int retval =   currentBuffer.getNextByte();
		        if (currentBuffer.ptr == currentBuffer.length )
				this.currentBuffer = null;
			return retval;
		     }
		     try {
			// wait till something is posted.
			while (this.buffList.isEmpty()) {
				this.buffList.wait();
				if (this.isClosed) return -1;
			}
			currentBuffer = (Buffer) this.buffList.removeFirst();
			int retval =   currentBuffer.getNextByte();
		        if (currentBuffer.ptr == currentBuffer.length )
				this.currentBuffer = null;
			return retval;
		     } catch (InterruptedException ex) {
			return -1;
		     } catch (NoSuchElementException ex ) {
			ex.printStackTrace();
			return -1;
		     }
		}
	}


}
