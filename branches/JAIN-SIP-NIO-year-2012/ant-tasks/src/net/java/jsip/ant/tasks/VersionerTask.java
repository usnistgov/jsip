package net.java.jsip.ant.tasks;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

/**
 *
 * @author  baranowb@dev.java.net
 *
 */
public class VersionerTask extends Task {

    protected File cvsVersionFile = null;
    protected int version = 1111;
    protected File pomFile = null;
    protected File toVersion = null;
    protected boolean incrementCVSVersion = false;
    protected boolean parent = false;
    protected int mainVersionParts = 3;

    private boolean doPom = false;
    private boolean doToVersion = false;


    public void setMainVersionParts(int mainVersionParts) {
        this.mainVersionParts = mainVersionParts;
    }

    public void setParent(boolean parent) {
        this.parent = parent;
    }

    public void setCvsVersionFile(File cvsVersionFile) {
        this.cvsVersionFile = cvsVersionFile;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public void setPomFile(File pomFile) {
        this.pomFile = pomFile;
    }

    public void setToVersion(File toVersion) {
        this.toVersion = toVersion;
    }

    public void setIncrementCVSVersion(boolean incrementCVSVersion) {
        this.incrementCVSVersion = incrementCVSVersion;
    }

    @Override
    public void execute() throws BuildException {


        if (this.cvsVersionFile == null && this.version < 0) {
            throw new BuildException(
                    "Version file property file is not set and version property is not set - one of them must be used!!!");
        }
        if (toVersion == null) {

        } else {
            doToVersion = true;
        }

        if(pomFile==null)
        {}else
        {
            doPom=true;
        }

        // Check cvsVersionFile
        if ( this.version < 0 &&  ( cvsVersionFile==null ||!cvsVersionFile.canRead())) {
            throw new BuildException("Cant read from " + cvsVersionFile
                    + " - eiher set it or version property!!!");
        }

        if (incrementCVSVersion && !cvsVersionFile.canWrite()) {
            throw new BuildException("Cant write to " + cvsVersionFile + "!!!");
        }

        if ( doPom && (!pomFile.canRead() || !pomFile.canWrite())) {
            throw new BuildException("Cant read/write pom file "
                    + cvsVersionFile + "!!!");
        }

        if (doToVersion && !toVersion.canRead()) {
            throw new BuildException(
                    "Cant read file that is going to be versioned "
                            + cvsVersionFile + "!!!");
        }












        super.execute();

        int localVersion = 1111;
        if (cvsVersionFile != null) {
        	//ObjectInputStream ois = null;
        	BufferedReader br=null;
        	try {
        		//ois = new ObjectInputStream(new FileInputStream(cvsVersionFile));

        		String versionValue=null;
        		br=new BufferedReader(new FileReader(cvsVersionFile));
        		versionValue=br.readLine();
        		localVersion = Math.abs(Integer.valueOf(versionValue));

        	} catch (Exception e) {
        		log("Failed to fetch version from file!!");
        		localVersion = this.version;
        	} finally {
        		if (br != null)
        			try {

        				br.close();
        			} catch (Exception e) {
        				localVersion = this.version;
        			}
        	}
        	super.getProject().setProperty("svnversion", Integer.toString(localVersion));

        	
        	getProject().setProperty("jain-sip-ri-jar",
					"jain-sip-ri-1.2." + localVersion + ".jar");
			getProject().setProperty("jain-sip-sdp-jar",
					"jain-sip-sdp-1.2." + localVersion + ".jar");
			getProject().setProperty("jain-sip-src-tar",
					"jain-sip-src-1.2." + localVersion + ".tar.gz");

			getProject().setProperty("jain-sip-javadoc-tar",
					"jain-sip-javadoc-1.2." + localVersion + ".tar.gz");

			getProject().setProperty("jain-sip-all-tar",
					"jain-sip-1.2." + localVersion + ".tar.gz");
			getProject().setProperty("jain-sip-tck-jar",
					"jain-sip-tck-1.2." + localVersion + ".jar");
			getProject().setProperty("sdp_jar",
					"jain-sdp-1.0." + localVersion + ".jar");
			getProject().setProperty("sdp-src-jar",
					"jain-sdp-src-1.0." + localVersion + ".jar");
			getProject().setProperty("jain-sip-src-jar",
					"jain-sip-src-1.2." + localVersion + ".jar");

			getProject().setProperty("jain-sip-sctp-jar",
					"jain-sip-sctp-1.2." + localVersion + ".jar");
			getProject().setProperty("unit_test_jar",
					"jain-sip-unit-test-1.2." + localVersion + ".jar");
			
			String cwd = System.getProperty("user.dir");
			getProject().setProperty("top",new File(cwd).getName());
        		
        	
        	
        } else {
            localVersion = this.version;
        }

        if(doPom)
        try {
            SAXBuilder saxb = new SAXBuilder();
            Document doc = saxb.build(pomFile);
            Element root = doc.getRootElement();
            // dont be supripsed - jdom and other work weird -
            // Element.getChild(Element.getName)==null even though child element
            // exists...
            if (parent) {
                for (Object o : root.getChildren()) {
                    Element current = (Element) o;

                    if (current.getName().equals("parent")) {
                        doVersionChange(current, localVersion);
                    } else {
                        continue;
                    }

                    break;
                }
            } else {
                doVersionChange(root, localVersion);
            }

            XMLOutputter outputter = new XMLOutputter();
            outputter.output(doc, new FileOutputStream(pomFile));

        } catch (Exception e) {
            throw new BuildException(e);
        }

        try{
        if(doToVersion)
        {
            String name=toVersion.toString();
            name=name.substring(0, name.lastIndexOf("."))+localVersion+name.substring( name.lastIndexOf("."));
            File outputFile=new File(name);
            toVersion.renameTo(outputFile);
        }
        }catch(Exception e)
        {
            log("Failed to rename file!!",e,0);
        }


    }

    private void doVersionChange(Element root, int ver) {
        log("Doing version injection - version parts:"+this.mainVersionParts);
        for (Object o : root.getChildren()) {
            Element current = (Element) o;

            if (current.getName().equals("version")) {
                String[] currentVersion = current.getText().split("\\.");
                log("Version from file:"+current.getText());
                String nextV = "";
                for (int i = 0; i < currentVersion.length
                        && i < this.mainVersionParts; i++) {
                    nextV = nextV + currentVersion[i] + ".";
                }
                nextV = nextV + ver;
                log("Resulting version:"+nextV);
                current.setText(nextV);
            } else {
                continue;
            }

            break;
        }
    }

    @Override
    public void init() throws BuildException {

        super.init();

    }

}
