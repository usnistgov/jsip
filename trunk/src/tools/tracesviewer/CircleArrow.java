package tools.tracesviewer;

import java.awt.*;

/**
*@version 1.2
*
*@author Olivier Deruelle   <br/>
*
*
*/

public class CircleArrow extends Arrow {

    public int x = 0;
    public int y = 0;
    public int diameter = 0;

    public CircleArrow(
        boolean selected,
        String arrowName,
        int x,
        int ymin,
        int ymax,
        int diameter,
        boolean flag,
        boolean info) {
        super(selected, arrowName, flag, x, x + diameter, ymin, ymax, info);
        this.x = xmin;
        this.y = (ymin + ymax) / 2;
        this.diameter = diameter;
    }

    public int xmin() {
        return x;
    }

    public int xmax() {
        return x + diameter;
    }

    public int ymin() {
        return ymin;
    }

    public int ymax() {
        return ymax;
    }

    public void draw(Graphics g) {
        // Set the color of this arrow:
        if (selected)
            g.setColor(Color.red);
        else
            g.setColor(color);
        /* Draw the circle */

        g.drawOval(x, y - (diameter / 2), diameter, diameter);
        g.drawOval(x - 1, y - (diameter / 2) - 1, diameter + 2, diameter + 2);

        /* Display the first line of the message */
        String timeString = "Time : " + tracesMessage.getTime() + " ms";

        int timeStringWidth =
            g.getFontMetrics(g.getFont()).stringWidth(timeString);
        int fistLineStringWidth =
            g.getFontMetrics(g.getFont()).stringWidth(
                tracesMessage.getFirstLine());

        g.drawString(
            tracesMessage.getFirstLine(),
            x
                + diameter
                + 5
                + tracesCanvas.HORIZONTAL_GAP / 2
                - fistLineStringWidth / 2,
            y - 5);

        g.drawString(
            timeString,
            x
                + diameter
                + 5
                + tracesCanvas.HORIZONTAL_GAP / 2
                - timeStringWidth / 2,
            y + g.getFontMetrics(g.getFont()).getHeight());

        /* Draw the head of the arrow */

        g.drawLine(x, y, x - 3, y + 10);
        g.drawLine(x, y, x + 7, y + 7);

        g.drawLine(x - 1, y, x - 4, y + 10);
        g.drawLine(x + 1, y, x + 8, y + 7);

        g.drawLine(x - 2, y, x - 5, y + 10);
        g.drawLine(x + 2, y, x + 9, y + 7);
    }
}
