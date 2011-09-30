/*
 * TracesSocket.java
 *
 * Created on July 3, 2003, 2:59 PM
 */
package tools.tracesviewer;

import java.net.*;
import java.io.*;

/**
 *
 * @author  deruelle
 */
public class TracesSocket extends Thread {

    private String fileName;
    private String port;
    private InputStream is;
    private FileWriter fw;

    /** Creates a new instance of TracesSocket */
    public TracesSocket(String fileName, String port) throws Exception {
        this.fileName = fileName;

        this.port = port;

        System.out.println("Waiting for a connection on port: " + port);
        ServerSocket serverSocket =
            new ServerSocket(Integer.valueOf(port).intValue());
        Socket newsock = serverSocket.accept();
        is = newsock.getInputStream();
        fw = new FileWriter(fileName);

    }

    public void run() {
        try {
            while (true) {
                int i = is.read();
                fw.write(i);
                //System.out.print(i);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
