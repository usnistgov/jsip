package tools.tracesviewer;

import java.awt.*;
import java.util.*;

/**
 * Lays out components within a Container such that each component takes a fixed percentage of the size.
 *
 * Each Component added to the Container must have a Constraint object that specifies what proportion
 * of the container it will fill. The Component will be stretched to fill exactly that percentage.
 *
 * @see Constraint
 */
public class PercentLayout implements LayoutManager2 {
    public void addLayoutComponent(Component component, Object constraint) {
        if (constraint instanceof PercentLayoutConstraint) {
            hash.put(component, constraint);
        } else {
            throw new IllegalArgumentException("Invalid constraint");

        }
    }
    public void addLayoutComponent(String constraint, Component comp) {
        throw new IllegalArgumentException("Invalid constraint");
    }
    public void removeLayoutComponent(Component component) {
        hash.remove(component);
    }

    public Dimension preferredLayoutSize(Container p1) {
        int prefx = 0;
        int prefy = 0;

        Enumeration keys = hash.keys();
        while (keys.hasMoreElements()) {
            Component comp = (Component) keys.nextElement();
            PercentLayoutConstraint constraint =
                (PercentLayoutConstraint) hash.get(comp);
            Dimension pref = comp.getPreferredSize();
            prefx += pref.width * 100 / constraint.width;
            prefy += pref.height * 100 / constraint.height;
        }
        int n = hash.size();
        return new Dimension(prefx / n, prefy / n);
    }
    public Dimension minimumLayoutSize(Container p1) {
        int minx = 0;
        int miny = 0;

        Enumeration keys = hash.keys();
        while (keys.hasMoreElements()) {
            Component comp = (Component) keys.nextElement();
            PercentLayoutConstraint constraint =
                (PercentLayoutConstraint) hash.get(comp);
            Dimension min = comp.getMinimumSize();
            int mx = (int) (min.width * 100 / constraint.width);
            int my = (int) (min.height * 100 / constraint.height);
            if (mx > minx)
                minx = mx;
            if (my > miny)
                miny = my;
        }
        return new Dimension(minx, miny);
    }
    public Dimension maximumLayoutSize(Container p1) {
        int maxx = Integer.MAX_VALUE;
        int maxy = Integer.MAX_VALUE;

        Enumeration keys = hash.keys();
        while (keys.hasMoreElements()) {
            Component comp = (Component) keys.nextElement();
            PercentLayoutConstraint constraint =
                (PercentLayoutConstraint) hash.get(comp);
            Dimension max = comp.getMaximumSize();
            int mx =
                max.width == Integer.MAX_VALUE
                    ? max.width
                    : (int) (max.width * 100 / constraint.width);
            int my =
                max.height == Integer.MAX_VALUE
                    ? max.height
                    : (int) (max.height * 100 / constraint.height);
            if (mx < maxx)
                maxx = mx;
            if (my < maxy)
                maxy = my;
        }
        return new Dimension(maxx, maxy);
    }
    public void layoutContainer(Container p1) {
        Dimension size = p1.getSize();
        Enumeration keys = hash.keys();
        while (keys.hasMoreElements()) {
            Component comp = (Component) keys.nextElement();
            PercentLayoutConstraint constraint =
                (PercentLayoutConstraint) hash.get(comp);
            int x = (int) (size.width * constraint.x / 100);
            int y = (int) (size.height * constraint.y / 100);
            int width = (int) (size.width * constraint.width / 100);
            int height = (int) (size.height * constraint.height / 100);
            comp.setBounds(x, y, width, height);
        }
    }
    public void invalidateLayout(Container p1) {
    }
    public float getLayoutAlignmentY(Container p1) {
        return 0.5f;
    }
    public float getLayoutAlignmentX(Container p1) {
        return 0.5f;
    }

    private Hashtable hash = new Hashtable();

}
