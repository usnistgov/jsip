package gov.nist.javax.sip;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Enumeration;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;
import java.util.Set;

public class MergedSystemProperties extends Properties {

    private Properties parent;

    public MergedSystemProperties(Properties props) {
        parent = props;
    }

    public void list(PrintStream out) {
        parent.list(out);
    }


    public void list(PrintWriter out) {
        parent.list(out);
    }


    public synchronized void load(InputStream inStream) throws IOException {

        parent.load(inStream);
    }


    public synchronized void load(Reader reader) throws IOException {

        throw new RuntimeException("Not implemented for Java 5 compatibility");
    }


    public synchronized void loadFromXML(InputStream in) throws IOException,
            InvalidPropertiesFormatException {

        parent.loadFromXML(in);
    }


    public Enumeration<?> propertyNames() {

        return parent.propertyNames();
    }


    public synchronized void save(OutputStream out, String comments) {

        parent.save(out, comments);
    }


    public synchronized Object setProperty(String key, String value) {

        return parent.setProperty(key, value);
    }


    public void store(OutputStream out, String comments) throws IOException {

        parent.store(out, comments);
    }


    public void store(Writer writer, String comments) throws IOException {

        throw new RuntimeException("Not implemented for Java 5 compatibility");
    }


    public synchronized void storeToXML(OutputStream os, String comment,
            String encoding) throws IOException {

        parent.storeToXML(os, comment, encoding);
    }


    public synchronized void storeToXML(OutputStream os, String comment)
            throws IOException {

        parent.storeToXML(os, comment);
    }


    public Set<String> stringPropertyNames() {

        throw new RuntimeException("Not implemented for Java 5 compatibility");
    }

    public String getProperty(String key, String defaultValue) {
        if(System.getProperty(key) != null)
            return System.getProperty(key);
        return parent.getProperty(key, defaultValue);
    }

    public String getProperty(String key) {
        if(System.getProperty(key) != null)
            return System.getProperty(key);
        return parent.getProperty(key);
    }
    
    @Override
    public Object get(Object key) {
        if(System.getProperty(key.toString()) != null)
            return System.getProperty(key.toString());
        return parent.getProperty(key.toString());
    }

    /**
     *
     */
    private static final long serialVersionUID = -7922854860297151103L;


    /**
     * Determines whether <tt>key</tt> is already present here. .
     *
     * @param key the key that we are looking for.
     *
     * @return <tt>true</tt> <tt>key</tt> is a known property and <tt>false</tt>
     * otherwise.
     */
    @Override
    public boolean containsKey(Object key){
        return parent.containsKey(key);
    }
    
    public String toString() {
    	return super.toString() + parent.toString();
    }
}
