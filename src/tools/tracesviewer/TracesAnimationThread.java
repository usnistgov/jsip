package tools.tracesviewer;

import java.util.*;

public class TracesAnimationThread implements Runnable {

    public Thread tracesThread;
    TracesCanvas tracesCanvas;
    Hashtable arrows;
    int delay;

    public TracesAnimationThread(TracesCanvas tracesCanvas) {
        tracesThread = null;
        arrows = null;
        delay = 2000;
        this.tracesCanvas = tracesCanvas;
        arrows = tracesCanvas.arrows;
    }

    public void start() {
        try {
            tracesThread = new Thread(this);
            tracesThread.setPriority(1);
            tracesThread.start();
        } catch (Exception exception) {
        }
    }

    public void stop() {
        try {
            tracesThread = null;
            tracesCanvas.repaint();
        } catch (Exception exception) {
        }
    }

    public void run() {
        try {
            arrows = tracesCanvas.arrows;
            if (arrows.size() != 0) {

                while (tracesThread != null) {

                    tracesCanvas.unvisibleAllArrows();
                    for (int i = 0;
                        (i < arrows.size()) && (tracesThread != null);
                        i++) {

                        Arrow arrow = (Arrow) arrows.get("arrow" + (i + 1));
                        arrow.visible = true;
                        tracesCanvas.unselectAllArrows();
                        arrow.selected = true;
                        tracesCanvas.repaint();

                        if (tracesThread != null)
                            Thread.sleep(delay);

                    }
                }
            }
        } catch (Exception exception) {
        }
    }

    public void setDelay(int i) {
        delay = i * 1000;
    }

    public int getDelay() {
        return delay / 1000;
    }

}
