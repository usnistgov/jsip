package tools.tracesviewer;

import java.awt.*;

/**
*@version 1.2
*
*@author Olivier Deruelle   <br/>
*
*
*/

abstract class Arrow {

    public String arrowName;
    public TracesMessage tracesMessage;
    public TracesCanvas tracesCanvas;

    public boolean visible = true;
    public boolean selected;

    public Color color;

    public int xmin;
    public int xmax;
    public int ymin;
    public int ymax;

    public boolean statusInfo = false;
    public boolean displayInfo = false;
    public boolean displayTipTool = false;

    abstract int xmin();
    abstract int xmax();
    abstract int ymin();
    abstract int ymax();

    public int xminInfo;
    public int xmaxInfo;
    public int yminInfo;
    public int ymaxInfo;

    abstract void draw(Graphics g);

    public void setColor(Color color) {
        this.color = color;
    }

    public void setTracesMessage(TracesMessage tracesMessage) {
        this.tracesMessage = tracesMessage;
    }

    public TracesMessage getTracesMessage() {
        return tracesMessage;
    }

    public void setTracesCanvas(TracesCanvas tracesCanvas) {
        this.tracesCanvas = tracesCanvas;
    }

    public Arrow(
        boolean selected,
        String arrowName,
        boolean flag,
        int xmin,
        int xmax,
        int ymin,
        int ymax,
        boolean info) {
        this.arrowName = arrowName;
        this.xmin = xmin;
        this.xmax = xmax;
        this.ymin = ymin;
        this.ymax = ymax;
        this.selected = selected;
        visible = flag;
        statusInfo = info;
    }

    public boolean isCollisionArrow(int x, int y) {
        // Return true if the cursor is inside the rectangle delimited by
        // the arrow:
        //System.out.println("isCollision: xmin:"+xmin+" xmax:"+xmax+" ymin:"+ymin+" ymax:"+ymax);
        // We have to be careful to the negative distance:
        if (xmin <= xmax) {
            if (x < xmax && x > xmin)
                if (y < ymax && y > ymin)
                    return true;
                else
                    return false;
            else
                return false;
        } else {
            if (x < xmin && x > xmax)
                if (y < ymax && y > ymin)
                    return true;
                else
                    return false;
            else
                return false;
        }
    }

    public boolean isCollisionInfo(int x, int y) {
        // Return true if the cursor is inside the rectangle delimited by
        // the info:
        //System.out.println("isCollision x:"+x+" y:"+y+" xminInfo:"+xminInfo+" xmaxInfo:"+xmaxInfo+
        // " yminInfo:"+yminInfo+" ymaxInfo:"+ymaxInfo);
        // We have to be careful to the negative distance:
        if (x < xmaxInfo && x > xminInfo)
            if (y < ymaxInfo && y > yminInfo)
                return true;
            else
                return false;
        else
            return false;
    }

}
