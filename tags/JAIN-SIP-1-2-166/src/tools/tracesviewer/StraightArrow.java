package tools.tracesviewer;

import java.awt.*;

public class StraightArrow extends Arrow {

    public Dimension dimensionInfo;

    public StraightArrow(
        boolean selected,
        String arrowName,
        int xmin,
        int xmax,
        int ymin,
        int ymax,
        boolean flag,
        boolean info) {
        super(selected, arrowName, flag, xmin, xmax, ymin, ymax, info);

    }

    public int xmin() {
        return Math.min(xmin, xmax);
    }

    public int xmax() {
        return Math.max(xmin, xmax);
    }

    public int ymin() {
        return Math.min(ymin, ymax);
    }

    public int ymax() {
        return Math.max(ymin, ymax);
    }

    public void draw(Graphics g) {
        // Set the color of this arrow:
        if (selected)
            g.setColor(Color.red);
        else
            g.setColor(color);

        Font font = g.getFont();
        Font newFont = new Font(font.getName(), Font.BOLD | Font.ITALIC, 14);
        g.setFont(newFont);

        int y = (ymin + ymax) / 2;
        if (tracesMessage.getStatusInfo() != null)
            if (tracesMessage.getStatusInfo().indexOf("Dropped") == -1) {
                g.drawLine(xmin, y, xmax, y);
                g.drawLine(
                    Math.min(xmin, xmax) + 2,
                    y - 1,
                    Math.max(xmin, xmax) - 2,
                    y - 1);
                g.drawLine(
                    Math.min(xmin, xmax) + 2,
                    y + 1,
                    Math.max(xmin, xmax) - 2,
                    y + 1);

            } else {

                if (xmin < xmax) {
                    int x = xmin;
                    while ((x + 8) < xmax) {
                        g.drawLine(x, y, x + 8, y);
                        g.drawLine(x, y - 1, x + 8, y - 1);
                        g.drawLine(x, y + 1, x + 8, y + 1);
                        x += 16;
                    }

                } else {
                    int x = xmin;
                    while ((x - 8) > xmax) {
                        g.drawLine(x, y, x - 8, y);
                        g.drawLine(x, y - 1, x - 8, y - 1);
                        g.drawLine(x, y + 1, x - 8, y + 1);
                        x -= 16;
                    }

                }

            }
        else {
            g.drawLine(xmin, y, xmax, y);
            g.drawLine(
                Math.min(xmin, xmax) + 2,
                y - 1,
                Math.max(xmin, xmax) - 2,
                y - 1);
            g.drawLine(
                Math.min(xmin, xmax) + 2,
                y + 1,
                Math.max(xmax, xmax) - 2,
                y + 1);

        }

        String timeString = "Time : " + tracesMessage.getTime() + " ms";

        int timeStringWidth =
            g.getFontMetrics(g.getFont()).stringWidth(timeString);
        int fistLineStringWidth =
            g.getFontMetrics(g.getFont()).stringWidth(
                tracesMessage.getFirstLine());

        if (xmax > xmin) {
            g.drawString(
                tracesMessage.getFirstLine(),
                xmin
                    + tracesCanvas.HORIZONTAL_GAP / 2
                    - fistLineStringWidth / 2,
                y - 5);

            g.drawString(
                timeString,
                xmin + tracesCanvas.HORIZONTAL_GAP / 2 - timeStringWidth / 2,
                y + g.getFontMetrics(g.getFont()).getHeight());

            g.drawLine(xmax, y, xmax - 10, y - 5);
            g.drawLine(xmax - 1, y, xmax - 11, y - 5);
            g.drawLine(xmax - 2, y, xmax - 12, y - 5);
            g.drawLine(xmax, y, xmax - 10, y + 5);
            g.drawLine(xmax - 1, y, xmax - 11, y + 5);
            g.drawLine(xmax - 2, y, xmax - 12, y + 5);

        } else {
            g.drawString(
                tracesMessage.getFirstLine(),
                xmax
                    + tracesCanvas.HORIZONTAL_GAP / 2
                    - fistLineStringWidth / 2,
                y - 2);

            g.drawString(
                timeString,
                xmax + tracesCanvas.HORIZONTAL_GAP / 2 - timeStringWidth / 2,
                y + 2 + g.getFontMetrics(g.getFont()).getHeight());

            g.drawLine(xmax, y, xmax + 10, y + 5);
            g.drawLine(xmax + 1, y, xmax + 11, y + 5);
            g.drawLine(xmax + 2, y, xmax + 12, y + 5);
            g.drawLine(xmax, y, xmax + 10, y - 5);
            g.drawLine(xmax + 1, y, xmax + 11, y - 5);
            g.drawLine(xmax + 2, y, xmax + 12, y - 5);
        } // else

        // draw the info sign if needed
        if (statusInfo) {
            //System.out.println("Display info sign ready");
            String statusString = tracesMessage.getStatusInfo();
            if (statusString == null || statusString.trim().equals("")) {
                //  System.out.println("No information: problem for displaying the info sign");
            } else {
                //g.setColor(new Color(0,0,125)) ;
                g.setColor(Color.yellow);
                //g.fillOval(xmax() - 20 , y - 20 , 15, 15) ;
                xminInfo = xmax() - 25;
                xmaxInfo = xmax();
                yminInfo = y - 30;
                ymaxInfo = y - 3;
                //x, y, width, height
                g.fillOval(xmax() - 25, y - 25, 20, 20);
                //g.setColor(Color.black) ;
                //g.drawOval(xmax() - 20 , y - 20 , 15, 15) ;
                g.setColor(Color.black);
                Font f = g.getFont();
                g.setFont(
                    new Font(
                        f.getName(),
                        Font.BOLD | Font.ITALIC,
                        f.getSize()));
                g.drawString("i", xmax() - 17, y - 11);
                g.setFont(f);
            }
        }
        if (displayInfo) {
            //System.out.println("Display info ready");
            String statusString = tracesMessage.getStatusInfo();
            if (statusString == null || statusString.trim().equals("")) {
                //   System.out.println("No information: problem for displaying info");
            } else {
                Font f = g.getFont();
                g.setFont(
                    new Font(
                        f.getName(),
                        Font.BOLD | Font.ITALIC,
                        f.getSize()));

                int statusStringWidth =
                    g.getFontMetrics(g.getFont()).stringWidth(statusString);
                int statusStringHeight =
                    g.getFontMetrics(g.getFont()).getHeight();

                int boxWidth =
                    Math.max(
                        tracesCanvas.HORIZONTAL_GAP,
                        statusStringWidth + 10);

                // shadow
                g.setColor(Color.gray);
                g.fillRoundRect(
                    xmin() + 15,
                    y - TracesCanvas.VERTICAL_GAP / 2 + 15,
                    boxWidth,
                    TracesCanvas.VERTICAL_GAP,
                    10,
                    10);
                // box
                g.setColor(Color.yellow);
                g.fillRoundRect(
                    xmin() + 10,
                    y - TracesCanvas.VERTICAL_GAP / 2 + 10,
                    boxWidth,
                    TracesCanvas.VERTICAL_GAP,
                    10,
                    10);

                // thick border
                g.setColor(Color.black);
                g.drawRoundRect(
                    xmin() + 10,
                    y - TracesCanvas.VERTICAL_GAP / 2 + 10,
                    boxWidth,
                    TracesCanvas.VERTICAL_GAP,
                    10,
                    10);
                g.drawRoundRect(
                    xmin() + 11,
                    y - TracesCanvas.VERTICAL_GAP / 2 + 11,
                    boxWidth - 2,
                    TracesCanvas.VERTICAL_GAP,
                    9,
                    9);

                // info string
                g.setColor(Color.black);

                if (boxWidth == tracesCanvas.HORIZONTAL_GAP)
                    g.drawString(
                        statusString,
                        xmin()
                            + 10
                            + 2
                            + tracesCanvas.HORIZONTAL_GAP / 2
                            - statusStringWidth / 2,
                        y + 10 + statusStringHeight / 2);

                else
                    g.drawString(
                        statusString,
                        xmin() + 10 + 5,
                        y + 10 + statusStringHeight / 2);
            }
        }
        if (displayTipTool) {
            //System.out.println("Display Tip tool ready");

            Font f = g.getFont();
            g.setFont(
                new Font(
                    f.getName(),
                    Font.BOLD | Font.ITALIC,
                    f.getSize() - 2));

            String text;
            if (statusInfo)
                text =
                    "Left click to select the message, Right click to display generated event.";
            else
                text = "Left click to select the message";

            int textWidth = g.getFontMetrics(g.getFont()).stringWidth(text);
            int textHeight = g.getFontMetrics(g.getFont()).getHeight();

            if (xmin < xmax) {
                // x,y,width,height

                // shadow
                g.setColor(Color.gray);
                g.fillRoundRect(
                    xmin() - 47,
                    y + 21,
                    textWidth + 5,
                    textHeight + 2,
                    10,
                    10);
                // box
                g.setColor(Color.yellow);
                g.fillRoundRect(
                    xmin() - 50,
                    y + 21,
                    textWidth + 4,
                    textHeight - 2,
                    10,
                    10);

                // thick border
                g.setColor(Color.black);
                g.drawRoundRect(
                    xmin() - 50,
                    y + 21,
                    textWidth + 5,
                    textHeight - 3,
                    10,
                    10);
                g.drawRoundRect(
                    xmin() - 50,
                    y + 21,
                    textWidth + 5,
                    textHeight - 3,
                    9,
                    9);

                // String, x,y
                g.setColor(Color.black);
                g.drawString(text, xmin - 46, y + 16 + textHeight);
            } else {
                // shadow
                g.setColor(Color.gray);
                g.fillRoundRect(
                    xmin() - 47,
                    y + 21,
                    textWidth + 5,
                    textHeight + 2,
                    10,
                    10);
                // box
                g.setColor(Color.yellow);
                g.fillRoundRect(
                    xmin() - 50,
                    y + 21,
                    textWidth + 4,
                    textHeight - 2,
                    10,
                    10);

                // thick border
                g.setColor(Color.black);
                g.drawRoundRect(
                    xmin() - 50,
                    y + 21,
                    textWidth + 5,
                    textHeight - 3,
                    10,
                    10);
                g.drawRoundRect(
                    xmin() - 50,
                    y + 21,
                    textWidth + 5,
                    textHeight - 3,
                    9,
                    9);

                // String, x,y
                g.setColor(Color.black);
                g.drawString(text, xmin() - 46, y + 16 + textHeight);
            }
        }
    }

}
