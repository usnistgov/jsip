package gov.nist.javax.sip.parser;
import  java.io.*;
import java.util.*;

/** Input class for the pipelined parser.
*/

public class Pipeline extends  InputStream {
	private LinkedList buffList;
	private Buffer currentBuffer;
	private boolean isClosed;
        private Timer timer;
        private InputStream pipe;
	private int readTimeout;
	private TimerTask myTimerTask;
        
        class MyTimer extends TimerTask {
            Pipeline pipeline;
	    private boolean isCancelled;
            protected MyTimer(Pipeline pipeline) {
                this.pipeline = pipeline;
            }
            
            public void run() {
		if (this.isCancelled) return;
                pipeline.close();
                try {
                    pipeline.pipe.close();
                } catch (IOException ex) {}
            }
	    public boolean cancel() {
		boolean retval = super.cancel();
		this.isCancelled = true;
		return retval;
	    }
            
        }

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
		    int retval =   bytes[ptr ++ ] & 0xFF;
		    return retval;
		}
			
	}


        public void startTimer() {
            if (this.readTimeout == -1) return;
            //TODO make this a tunable number. For now 4 seconds
            // between reads seems reasonable upper limit.
	    this.myTimerTask = new MyTimer(this);
            this.timer.schedule(this.myTimerTask, this.readTimeout);
        }
        
        public void stopTimer() {
            if (this.readTimeout == -1) return;
	    if (this.myTimerTask != null) this.myTimerTask.cancel();
        }

	public Pipeline (InputStream pipe,int readTimeout, Timer timer) {
                // pipe is the Socket stream 
                // this is recorded here to implement a timeout.
		this.timer = timer;
                this.pipe = pipe;
		buffList = new LinkedList();
		this.readTimeout = readTimeout;
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

	public int read() throws IOException {
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
			  throw new IOException(ex.getMessage());
		     } catch (NoSuchElementException ex ) {
			ex.printStackTrace();
		        throw new IOException(ex.getMessage());
		     }
		}
	}


}
