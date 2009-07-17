package tools.tracesviewer;

import java.awt.*;
import java.awt.event.*;

public class TracesSessionsList extends List {

    protected TracesSessions tracesSessions;
    protected TracesCanvas tracesCanvas;
    protected int index = 0;

    public TracesSessionsList() {
        //super(new DefaultListModel());
    }

    public void setTracesCanvas(TracesCanvas tracesCanvas) {
        this.tracesCanvas = tracesCanvas;
    }

    /**
    * Get the call identifier for a given trace session.
    *
    *@param name is the session name for the trace session.
    *
    */

    public String getCallId(String name) {
        try {
            int index = name.indexOf("//");
            int firstIndex = name.indexOf("/", index + 2);
            return name.substring(firstIndex + 1);
        } catch (Exception e) {
            return name;
        }
    }

    /**
    * Get the origin for a trace session.
    *
    *@param name is the name of the trace session.
    *
    */
    public String getOrigin(String name) {
        try {
            int firstIndex = name.indexOf("//");
            int secondIndex = name.indexOf("/", 2);
            String origin = name.substring(2, secondIndex);
            if (origin.equals(TracesViewer.stackId))
                return "the proxy";
            else
                return "a user agent (" + origin + ")";
        } catch (Exception e) {
            return "unknown";
        }
    }

    public void setTracesSessions(TracesSessions tracesSessions) {
        removeAll();
        //((DefaultListModel)getModel()).removeAllElements();
        this.tracesSessions = tracesSessions;
        for (int i = 0; i < tracesSessions.size(); i++) {
            TracesSession tracesSession =
                (TracesSession) tracesSessions.elementAt(i);
            String name = tracesSession.getName();
            String logDescription = tracesSession.getLogDescription();
            //System.out.println("logDesc1:"+logDescription);
            String callId = getCallId(name);
            String origin = getOrigin(name);
            if (name.equals("No available session, refresh")) {
                add(name);
            } else if (
                logDescription == null || logDescription.trim().equals("")) {
                add(
                    "Trace "
                        + (i + 1)
                        + " from "
                        + origin
                        + "; callId: "
                        + callId);
            } else {
                add(
                    "Trace "
                        + (i + 1)
                        + " from "
                        + logDescription
                        + "; callId: "
                        + callId);

            }
        }
        if (tracesSessions.size() != 0)
            select(0);
    }

    public void updateTracesCanvas() {
        if (tracesSessions == null || tracesSessions.isEmpty())
            return;
        // We take the first trace from the list
        TracesSession tracesSession =
            (TracesSession) tracesSessions.firstElement();

        String name = tracesSession.getName();
        String logDescription = tracesSession.getLogDescription();
        String callId = getCallId(name);
        String origin = getOrigin(name);
        if (name.equals("No available session, refresh")) {
            tracesCanvas.refreshTracesCanvas(tracesSession, "unknown");
        } else if (
            logDescription == null || logDescription.trim().equals("")) {
            tracesCanvas.refreshTracesCanvas(tracesSession, origin);
        } else {

            tracesCanvas.refreshTracesCanvas(tracesSession, logDescription);
        }
    }

    public void updateTracesCanvas(ItemEvent e) {
        if (tracesSessions == null || tracesSessions.isEmpty())
            return;

        index = ((Integer) e.getItem()).intValue();

        TracesSession tracesSession =
            (TracesSession) tracesSessions.elementAt(index);
        String name = tracesSession.getName();
        String logDescription = tracesSession.getLogDescription();
        String callId = getCallId(name);
        String origin = getOrigin(name);
        if (name.equals("No available session, refresh")) {
            tracesCanvas.refreshTracesCanvas(tracesSession, "unknown");
        } else if (
            logDescription == null || logDescription.trim().equals("")) {
            tracesCanvas.refreshTracesCanvas(tracesSession, origin);
        } else {
            System.out.println("logDesc33:" + logDescription);
            tracesCanvas.refreshTracesCanvas(tracesSession, logDescription);
        }
    }

}
