/*
 * TracesCanvas.java
 *
 * Created on April 16, 2002, 5:40 PM
 */

/* This class displays the trace logged by the server
 * Here is a description of the properties
 *
 *   -------------------------------------------------------  ^
 *   |                                                     |  |
 *   |       Actor 1          Actor 2         Actor 3      |  | 3
 *   |                           |               |
 *   |------------------------------------------------------  v ^
 *   |           |               |               |         |    | 4
 *   |           |-------------->|               |         |    v
 *   |           |               |               |         |
 *   |           |               |               |         |
 *   |           |               |-------------->|         |  ^
 *   |           |               |               |         |  | 5
 *   |           |               |               |         |  |
 *   |           |               |<--------------|         |  v
 *   |           |               |               |         |
 *   |           |               |               |         |
 *   |           |<--------------|               |         |  ^
 *   |           |               |               |         |  | 6
 *   -------------------------------------------------------  v
 *                                                    7
 *         1                             2       <--------->
 *   <----------->               <--------------->
 *
 *
 *  1 : FIRST_ACTOR_GAP
 *  2 : HORIZONTAL_GAP
 *  3 : ACTORS_STRIPE
 *  4 : FIRST_ARROW_GAP
 *  5 : VERTICAL_GAP
 *  6 : LAST_ARROW_GAP
 *  7 : LAST_ACTOR_GAP
 */

package tools.tracesviewer;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
/**
 *
 * @author  deruelle, chazeau
 * @version 1.0
 */
public class TracesCanvas
    extends Canvas
    implements MouseListener, MouseMotionListener {

    public static int FIRST_ACTOR_GAP = 100;
    public int HORIZONTAL_GAP = 350;
    public static int ACTORS_STRIPE = 120;
    public static int FIRST_ARROW_GAP = 50;
    public static int VERTICAL_GAP = 60;
    public static int LAST_ARROW_GAP = 30;
    public static int LAST_ACTOR_GAP = 100;

    // This color is the color of the current selected SIP message
    public Color SELECTED_COLOR = Color.red;
    public Color[] ARROWS_COLOR =
        {
            Color.blue,
            new Color(94, 151, 185),
            Color.green,
            new Color(55, 223, 131),
            Color.orange,
            new Color(209, 177, 69),
            Color.magenta,
            new Color(187, 91, 185),
            Color.cyan,
            new Color(199, 239, 39)};

    public String selectedArrowName;
    public Arrow newArrow;
    public Arrow oldArrow;

    public Image actorsImage;
    public Image backgroundImage;

    public TracesSession tracesSession = null;
    public TextArea messageContentTextArea = null;
    public String tracesOrigin;

    public Hashtable actors = new Hashtable();
    public Hashtable arrows = new Hashtable();
    public Hashtable arrowsColors = new Hashtable();

    public boolean isAnimated = false;
    public Arrow arrowTipTool = null;
    public TracesViewer tracesViewer;

    public TracesMessage debugTracesMessage;

    public Dimension getPreferredSize() {
        int width =
            FIRST_ACTOR_GAP
                + LAST_ACTOR_GAP
                + HORIZONTAL_GAP * (actors.size() - 1);
        int height =
            ACTORS_STRIPE
                + FIRST_ARROW_GAP
                + LAST_ARROW_GAP
                + VERTICAL_GAP * (arrows.size() - 1);

        return new Dimension(width, height);
    }

    public TracesCanvas(
        TracesSession tracesSession,
        TextArea messageContentTextArea,
        String trueName,
        TracesViewer tracesViewer) {
        this.tracesViewer = tracesViewer;
        backgroundImage = TracesViewer.backgroundImage;
        actorsImage = TracesViewer.actorsImage;

        this.tracesSession = tracesSession;
        this.messageContentTextArea = messageContentTextArea;

        refreshTracesCanvas(tracesSession, trueName);

        addMouseListener(this);
        addMouseMotionListener(this);

    }

    // Reserved constructor for the AboutFrame
    public TracesCanvas(
        TracesSession tracesSession,
        Image backgroundImage,
        String trueName,
        int horizontalGap,
        TracesViewer tracesViewer) {
        this.tracesViewer = tracesViewer;

        this.backgroundImage = backgroundImage;
        HORIZONTAL_GAP = horizontalGap;
        actorsImage = TracesViewer.actorsImage;

        this.tracesSession = tracesSession;
        this.messageContentTextArea = null;

        refreshTracesCanvas(tracesSession, trueName);

        addMouseListener(this);
        addMouseMotionListener(this);
    }

    public void refreshTracesCanvas(
        TracesSession tracesSession,
        String tracesOrigin) {

        this.tracesSession = tracesSession;
        // All of this has to called when the trace is refreshed or a new Session:
        // The actors are the hosts
        this.tracesOrigin = tracesOrigin;
        constructActors();

        // All the SIP messages from the session
        constructArrows();

        // We select the first message
        selectMessage(
            FIRST_ACTOR_GAP + HORIZONTAL_GAP / 2,
            ACTORS_STRIPE + FIRST_ARROW_GAP);
    }

    public void drawTop(Graphics g) {
        int widthCanvas = getSize().width;

        int heightCanvas = getSize().height;
        // System.out.println(widthCanvas+","+heightCanvas);
        // purple
        g.setColor(new Color(0, 0, 125));
        // light purple
        //g.setColor(new Color(102,102,153));
        // x, y, width, height
        g.fillRect(0, 0, widthCanvas, ACTORS_STRIPE - 5);

        // Draw the String using the current Font and color
        g.setColor(Color.white);
        // display the label
        Font f = g.getFont();
        Font newFont = new Font(f.getName(), Font.BOLD | Font.ITALIC, 17);
        g.setFont(newFont);
        // String, x,y
        g.drawString("Trace retrieved from " + tracesOrigin, 40, 25);

        // draw a separation line:
        g.setColor(Color.black);
        // x,y -> x,y
        g.drawLine(0, ACTORS_STRIPE, widthCanvas, ACTORS_STRIPE);

        // draw the actors above the separation line and their vertical line:
        Enumeration e = actors.keys();
        while (e.hasMoreElements()) {
            String origin = (String) e.nextElement();
            int positionX = ((Integer) actors.get(origin)).intValue();

            // if we have an image for the actors display it
            // otherwise just do nothing
            // Draw the name of the actor and its address below its icon:
            // String, x,y
            g.setColor(Color.white);
            //  if (origin.equals(TracesViewerLauncher.stackId) ){
            //       g.drawString(tracesOrigin,positionX - getFontMetrics(g.getFont()).stringWidth(tracesOrigin) / 2 ,
            // ACTORS_STRIPE -30) ;
            // }
            // else {
            //     g.drawString("user agent",positionX - getFontMetrics(g.getFont()).stringWidth("user agent") / 2 ,
            // ACTORS_STRIPE - 30) ;
            // }
            f = g.getFont();
            newFont = new Font(f.getName(), Font.BOLD | Font.ITALIC, 14);
            g.setFont(newFont);
            g.drawString(
                origin,
                positionX - getFontMetrics(g.getFont()).stringWidth(origin) / 2,
                ACTORS_STRIPE - 15);

            if (actorsImage != null) {

                g.drawImage(
                    actorsImage,
                    positionX - actorsImage.getWidth(this) / 2,
                    ACTORS_STRIPE / 3,
                    this);
            } else {
                //System.out.println("The actors icon is null!!");
            }

            // Vertical line
            g.setColor(Color.black);
            g.drawLine(positionX, ACTORS_STRIPE, positionX, heightCanvas);
        }
    }

    public void constructActors() {
        try {
            // We clean the table
            actors = new Hashtable();
            for (int i = 0; i < tracesSession.size(); i++) {
                TracesMessage tracesMessage =
                    (TracesMessage) tracesSession.elementAt(i);

                String from = tracesMessage.getFrom().trim();
                String to = tracesMessage.getTo().trim();
                //  System.out.println("consructorActors():"+tracesMessage.getMessageString() );
                //System.out.println("from:"+from);
                //System.out.println("to:"+from);
                //System.out.println();

                //fromActorName = getActorName(from) ;
                //to = getActorName(to) ;

                // We have to stock the actor and its position in the canvas
                int sizeActors = actors.size();
                if (actors.get(from) == null) {
                    actors.put(
                        from,
                        new Integer(
                            sizeActors * HORIZONTAL_GAP + FIRST_ACTOR_GAP));
                }
                sizeActors = actors.size();
                if (actors.get(to) == null) {
                    actors.put(
                        to,
                        new Integer(
                            sizeActors * HORIZONTAL_GAP + FIRST_ACTOR_GAP));
                }

            }
        } catch (Exception e) {
            System.out.println("Error in trying to construct the actors");
        }
    }

    public void constructArrows() {
        arrows = new Hashtable();
        String from = null;
        String to = null;

        // Assign different colors to each arrows
        assignColors();
        //System.out.println("Construct arrows");

        // Setup the first selected arrow:
        selectedArrowName = "arrow1";
        //System.out.println("tracesSession size:"+tracesSession.size());
        for (int i = 0; i < tracesSession.size(); i++) {
            TracesMessage tracesMessage =
                (TracesMessage) tracesSession.elementAt(i);

            from = tracesMessage.getFrom();
            to = tracesMessage.getTo();

            int positionXFrom = ((Integer) actors.get(from)).intValue();
            int positionXTo = ((Integer) actors.get(to)).intValue();
            int positionY = i * VERTICAL_GAP + ACTORS_STRIPE + FIRST_ARROW_GAP;
            boolean info = tracesMessage.getStatusInfo() != null;

            String arrowName = "arrow" + (i + 1);
            boolean selected = false;

            // Set up the color of this arrow
            String transactionId = tracesMessage.getTransactionId();

            Color color;
            if (transactionId != null) {
                color = (Color) arrowsColors.get(transactionId);
                if (color == null)
                    color = Color.black;
            } else
                color = Color.black;

            if (positionXFrom == positionXTo) {
                // This is a loop !!
                CircleArrow circleArrow =
                    new CircleArrow(
                        selected,
                        arrowName,
                        positionXFrom,
                        positionY - 20,
                        positionY + 20,
                        40,
                        true,
                        info);
                circleArrow.setColor(color);
                circleArrow.setTracesMessage(tracesMessage);
                circleArrow.setTracesCanvas(this);
                arrows.put(arrowName, circleArrow);
            } else {
                // This is a straight arrow
                //StraightArrow straightArrow=new StraightArrow(selected,arrowName,
                //positionXFrom,positionXTo,positionY-15, positionY+15, true,info);

                StraightArrow straightArrow =
                    new StraightArrow(
                        selected,
                        arrowName,
                        positionXFrom,
                        positionXTo,
                        positionY - 31,
                        positionY + 31,
                        true,
                        info);

                straightArrow.setColor(color);
                straightArrow.setTracesMessage(tracesMessage);
                straightArrow.setTracesCanvas(this);
                arrows.put(arrowName, straightArrow);
                // System.out.println("color:"+ straightArrow.color);
                // System.out.println("arrow name:"+straightArrow.arrowName);
                // System.out.println("transactionId:"+tracesMessage.getTransactionId());
                // System.out.println();

            }
        }
        // System.out.println("arrows size:"+arrows.size());
    }

    public void assignColors() {
        arrowsColors = new Hashtable();
        TracesMessage tracesMessage = null;
        int colorIndex = 0;

        for (int i = 0; i < tracesSession.size(); i++) {
            tracesMessage = (TracesMessage) tracesSession.elementAt(i);
            String transactionId = tracesMessage.getTransactionId();

            if (transactionId != null) {
                Color color = (Color) arrowsColors.get(transactionId);
                if (color == null) {
                    if (colorIndex >= 10)
                        color = Color.black;
                    else
                        color = ARROWS_COLOR[colorIndex];
                    arrowsColors.put(transactionId, color);
                    colorIndex++;
                } else {
                    // the Color is already attribuated with this transaction id.
                }
            }
        }
    }

    public Arrow getArrow(int x, int y) {
        //System.out.println("getArrow: x:"+x+" y:"+y);
        Enumeration e = arrows.keys();
        while (e.hasMoreElements()) {
            String arrowName = (String) e.nextElement();
            Arrow arrow = (Arrow) arrows.get(arrowName);
            if (arrow == null)
                return null;
            if (arrow.isCollisionArrow(x, y))
                return arrow;
        }
        return null;
    }

    public Arrow getArrowInfo(int x, int y) {
        //System.out.println("getArrow: x:"+x+" y:"+y);
        Enumeration e = arrows.keys();
        while (e.hasMoreElements()) {
            String arrowName = (String) e.nextElement();
            Arrow arrow = (Arrow) arrows.get(arrowName);
            if (arrow == null)
                return null;
            else if (arrow.statusInfo) {
                if (arrow.isCollisionInfo(x, y))
                    return arrow;
            }
        }
        return null;
    }

    public void selectMessage(int x, int y) {
        // x,y: position of the cursor
        newArrow = getArrow(x, y);
        if (newArrow == null) {
            // big problem
        } else {
            // We have to update the content:

            TracesMessage tracesMessage = newArrow.getTracesMessage();

            if (messageContentTextArea != null) {

                messageContentTextArea.setText(
                    tracesMessage.getMessageString());
                if (tracesMessage.debugLine != null
                    && !tracesMessage.debugLine.equals("")) {

                    tracesViewer.messageContentButton.setText(
                        "SIP message: (debug log line: "
                            + tracesMessage.debugLine
                            + " )");
                }
            }
            debugTracesMessage = tracesMessage;

            // We have to change the color of the old message and the new one:
            oldArrow = (Arrow) arrows.get(selectedArrowName);
            oldArrow.selected = false;
            newArrow.selected = true;
            selectedArrowName = newArrow.arrowName;
            repaint();
        }
    }

    public void showTipTool(int x, int y) {
        Arrow oldArrowTipTool = arrowTipTool;
        arrowTipTool = getArrow(x, y);
        if (oldArrowTipTool != null) {
            if (oldArrowTipTool.arrowName.equals(arrowTipTool.arrowName)) {
                // we do nothing because it is the same arrow
            } else {
                // A new arrow
                oldArrowTipTool.displayTipTool = false;
                arrowTipTool.displayTipTool = true;
                repaint();
            }
        } else {
            // The very first arrow selected!!!
            arrowTipTool.displayTipTool = true;
            repaint();
        }
    }

    public void unShowTipTool() {
        if (arrowTipTool != null) {
            arrowTipTool.displayTipTool = false;
            repaint();
            arrowTipTool = null;
        }
    }

    public void drawArrows(Graphics g) {
        // draw the arrows and information
        // Set up the Font

        Enumeration e = arrows.keys();
        while (e.hasMoreElements()) {
            String arrowName = (String) e.nextElement();
            Arrow arrow = (Arrow) arrows.get(arrowName);
            if (arrow.visible) {
                //System.out.println("arrow:"+arrow.ymin);
                arrow.draw(g);
            }
        }

        if (getParent() != null) {
            getParent().doLayout();
            getParent().validate();
        }
    }

    public void unvisibleAllArrows() {
        Enumeration e = arrows.keys();
        while (e.hasMoreElements()) {
            String arrowName = (String) e.nextElement();
            Arrow arrow = (Arrow) arrows.get(arrowName);
            arrow.visible = false;
        }
    }

    public void unselectAllArrows() {
        Enumeration e = arrows.keys();
        while (e.hasMoreElements()) {
            String arrowName = (String) e.nextElement();
            Arrow arrow = (Arrow) arrows.get(arrowName);
            arrow.selected = false;
        }
    }

    public boolean isOnArrow(int x, int y) {
        Arrow arrow = getArrow(x, y);
        if (arrow == null)
            return false;
        else
            return true;
    }

    public boolean isOnInfo(int x, int y) {
        Arrow arrow = getArrowInfo(x, y);
        if (arrow == null)
            return false;
        else
            return true;
    }

    public void displayInfo(int x, int y) {
        Arrow arrow = getArrow(x, y);
        arrow.displayInfo = true;
        repaint();
    }

    public void unDisplayInfo() {
        //System.out.println("getArrow: x:"+x+" y:"+y);
        Enumeration e = arrows.keys();
        boolean repaint = false;
        while (e.hasMoreElements()) {
            String arrowName = (String) e.nextElement();
            Arrow arrow = (Arrow) arrows.get(arrowName);
            if (arrow != null)
                if (arrow.displayInfo) {
                    arrow.displayInfo = false;
                    repaint = true;
                }
        }
        repaint();
    }

    public void setBackground(Graphics g) {
        // if we have a background image, fill the back with it
        // otherwise black by default

        if (backgroundImage != null
            && backgroundImage.getWidth(this) != -1
            && backgroundImage.getHeight(this) != -1) {
            int widthImage = backgroundImage.getWidth(this);
            int heightImage = backgroundImage.getHeight(this);

            int nbImagesX = getSize().width / widthImage + 1;
            int nbImagesY = getSize().height / heightImage + 1;

            // we don't draw the image above the top

            for (int i = 0; i < nbImagesX; i++)
                for (int j = 0; j < nbImagesY; j++)
                    g.drawImage(
                        backgroundImage,
                        i * widthImage,
                        j * heightImage + 95,
                        this);
        } else {

            g.setColor(Color.white);
            g.fillRect(0, 95, getSize().width, getSize().height);
        }

    }
    /*
     // Without this method, the canvas is clippping!!!!!!
     public void update(Graphics g){
         try{
             // Update is called only by selectMessage:
             //System.out.println("update method called");

             setBackground(g);
             drawTop(g);
             drawArrows(g);


         }
         catch(Exception exception) {}
     }*/

    public void paint(Graphics g) {
        try {
            // Paint is called each time:
            //  - resize of the window
            // System.out.println();
            // System.out.println("paint method called");
            // We draw the title and some decorations:
            setBackground(g);
            drawTop(g);
            drawArrows(g);
            /*
            Graphics2D g2 = (Graphics2D) g;

            // The width and height of the canvas
            int w = getSize().width;
            int h = getSize().height;
            // Create an ellipse and use it as the clipping path
            Ellipse2D e = new Ellipse2D.Float(w/4.0f,h/4.0f,
                               w/2.0f,h/2.0f);
            g2.setClip(e);

            // Fill the canvas. Only the area within the clip is rendered
            g2.setColor(Color.cyan);
            g2.fillRect(0,0,w,h);

            // Change the clipping path, setting it to the intersection of
            // the current clip and a new rectangle.
            Rectangle r = new Rectangle(w/4+10,h/4+10,w/2-20,h/2-20);
            g2.clip(r);

            // Fill the canvas. Only the area within the new clip
            // is rendered
            g2.setColor(Color.magenta);
            g2.fillRect(0,0,w,h);
            */
        } catch (Exception exception) {
        }
    }

    public void mousePressed(MouseEvent e) {
        // System.out.println("Mouse pressed in the canvas!!!");
        try {
            int x = e.getX();
            int y = e.getY();

            if ((e.getModifiers() & InputEvent.BUTTON3_MASK)
                == InputEvent.BUTTON3_MASK) {
                // The right button is pressed

                if (isOnArrow(x, y)) {

                    //unShowTipTool();
                    arrowTipTool.displayTipTool = false;
                    arrowTipTool = null;
                    displayInfo(x, y);
                }
            } else {
                // The left button is pressed:
                if (isOnArrow(x, y)) {
                    //System.out.println("click on Arrow!!!");
                    selectMessage(x, y);
                }
                //if (isOnInfo(x,y)){
                //    displayInfo(x,y);
                //}
            }
        } catch (Exception ex) {

        }
    }

    public void mouseReleased(MouseEvent p1) {
        unDisplayInfo();
    }

    public void mouseEntered(MouseEvent p1) {
    }

    public void mouseClicked(MouseEvent p1) {
    }

    public void mouseExited(MouseEvent p1) {
    }

    public void mouseDragged(MouseEvent p1) {
    }

    public void mouseMoved(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();

        if (isOnArrow(x, y)) {
            showTipTool(x, y);
        } else
            unShowTipTool();
    }

}
