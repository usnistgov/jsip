package tools.tracesviewer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
*
*@version 1.2
*
*@author Olivier Deruelle   <br/>
*
*
*
*/
public class ListenerTracesViewer {

    public TracesViewer tracesViewer;
    public TracesSessionsDisplayer tracesSessionsDisplayer;

    public boolean ANIMATION_STARTED;
    public AboutFrame aboutFrame;
    public HelpBox helpBox;
    public ScriptFrame scriptFrame;

    /** Creates new ListenerTraceViewer */
    public ListenerTracesViewer(TracesViewer tracesViewer) {
        this.tracesViewer = tracesViewer;
        ANIMATION_STARTED = false;
        aboutFrame = new AboutFrame();
        tracesSessionsDisplayer = new TracesSessionsDisplayer();
        helpBox = new HelpBox();
        scriptFrame = new ScriptFrame();
    }

    public void debugActionPerformed(ActionEvent evt) {
        TracesMessage debug = tracesViewer.tracesCanvas.debugTracesMessage;
        //System.out.println("******************BEGIN******************************");
        //System.out.println(debug.beforeDebug);
        //System.out.println(debug.afterDebug);
        //System.out.println("******************END********************************");
        if (debug == null)
            return;

        if (debug.beforeDebug != null
            && debug.beforeDebug != null
            && !debug.beforeDebug.trim().equals("")
            && !debug.afterDebug.trim().equals("")) {
            DebugWindow debugWindow =
                new DebugWindow(
                    debug.beforeDebug,
                    debug.afterDebug,
                    debug.debugLine);

            debugWindow.show();
        }
    }

    public void displayAllSessionsMouseEvent(MouseEvent evt) {
        tracesSessionsDisplayer.show(tracesViewer.getTracesSessions());
    }

    public void helpMenuMouseEvent(MouseEvent evt) {
        helpBox.show();
    }

    public void aboutMenuMouseEvent(MouseEvent evt) {
        aboutFrame.animationThread.start();
        aboutFrame.show();
    }

    public void animationActionPerformed(ActionEvent evt) {
        if (tracesViewer.tracesCanvas.arrows.size() == 0) {
            new AlertFrame(
                "Please hit Refresh, first!",
                JOptionPane.ERROR_MESSAGE);
        } else if (ANIMATION_STARTED) {

            tracesViewer.animationMenuItem.setBackground(Color.lightGray);
            tracesViewer.animationThread.stop();
            ANIMATION_STARTED = false;
        } else {

            tracesViewer.animationThread.start();

            tracesViewer.animationMenuItem.setBackground(Color.green);
            ANIMATION_STARTED = true;
        }
    }

    /*
    public void refreshActionPerformed(ActionEvent evt){
       if (ANIMATION_STARTED)
           new AlertFrame("You must stop the animation before refreshing the traces!",JOptionPane.ERROR_MESSAGE);
       else {
           TracesSessions tracesSessions=tracesViewerLauncher.refreshTracesSessions();
           tracesViewerLauncher.tracesSessionsList.setTracesSessions(tracesSessions);
           tracesViewerLauncher.tracesSessionsList.updateTracesCanvas();
           tracesViewerLauncher.initComboBox();
           if (tracesSessionsDisplayer.isVisible())
               tracesSessionsDisplayer.show(tracesSessions);
       }

    }
     */

    public void refreshActionPerformed(MouseEvent evt) {
        if (ANIMATION_STARTED)
            new AlertFrame(
                "You must stop the animation before refreshing the traces!",
                JOptionPane.ERROR_MESSAGE);
        else {
            TracesSessions tracesSessions =
                tracesViewer.refreshTracesSessions();
            tracesViewer.tracesSessionsList.setTracesSessions(tracesSessions);
            tracesViewer.tracesSessionsList.updateTracesCanvas();

            if (tracesSessionsDisplayer.isVisible())
                tracesSessionsDisplayer.show(tracesSessions);
        }

    }

    public void scriptActionPerformed(ActionEvent evt) {

    }

    public void tracesSessionsListStateChanged(ItemEvent e) {
        if (ANIMATION_STARTED) {
            tracesViewer.animationMenuItem.setBackground(Color.lightGray);
            tracesViewer.animationThread.stop();
            ANIMATION_STARTED = false;
        }
        tracesViewer.tracesSessionsList.updateTracesCanvas(e);
    }

}
