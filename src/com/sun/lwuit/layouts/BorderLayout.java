/*
 * Copyright 2008 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */
package com.sun.lwuit.layouts;

import com.sun.lwuit.Component;
import com.sun.lwuit.Container;
import com.sun.lwuit.geom.*;
import com.sun.lwuit.plaf.Style;
import com.sun.lwuit.plaf.UIManager;

/**
 * A border layout lays out a container, arranging and resizing its 
 * components to fit in five regions: north, south, east, west, and center. 
 * Each region may contain no more than one component, and is identified by a 
 * corresponding constant: NORTH, SOUTH, EAST, WEST, and CENTER. 
 * When adding a component to a container with a border layout, use one of 
 * these five constants.
 *
 * @author Nir
 */
public class BorderLayout extends Layout {

    private Component north;
    private Component south;
    private Component center;
    private Component west;
    private Component east;
    /**
     * The north layout constraint (top of container).
     */
    public static final String NORTH = "North";
    /**
     * The south layout constraint (bottom of container).
     */
    public static final String SOUTH = "South";
    /**
     * The center layout constraint (middle of container)
     */
    public static final String CENTER = "Center";
    /**
     * The west layout constraint (left of container).
     */
    public static final String WEST = "West";
    /**
     * The east layout constraint (right of container).
     */
    public static final String EAST = "East";

    /** 
     * Creates a new instance of BorderLayout 
     */
    public BorderLayout() {
    }

    /**
     * @inheritDoc
     */
    public void addLayoutComponent(Object name, Component comp, Container c) {
        // helper check for a common mistake...
        if (name == null) {
            throw new IllegalArgumentException("Cannot add component to BorderLayout Container without constraint parameter");
        }

        Component previous = null;

        /* Assign the component to one of the known regions of the layout.
         */
        if (CENTER.equals(name)) {
            previous = center;
            center = comp;
        } else if (NORTH.equals(name)) {
            previous = north;
            north = comp;
        } else if (SOUTH.equals(name)) {
            previous = south;
            south = comp;
        } else if (EAST.equals(name)) {
            previous = east;
            east = comp;
        } else if (WEST.equals(name)) {
            previous = west;
            west = comp;
        } else {
            throw new IllegalArgumentException("cannot add to layout: unknown constraint: " + name);
        }
        if (previous != null && previous != comp) {
            c.removeComponent(previous);
        }
    }

    /**
     * @inheritDoc
     */
    public void removeLayoutComponent(Component comp) {
        if (comp == center) {
            center = null;
        } else if (comp == north) {
            north = null;
        } else if (comp == south) {
            south = null;
        } else if (comp == east) {
            east = null;
        } else if (comp == west) {
            west = null;
        }
    }

    /**
     * Returns the component constraint
     * 
     * @param comp the component whose constraint is queried
     * @return one of the constraints defined in this class
     */
    public Object getComponentConstraint(Component comp) {
        if (comp == center) {
            return CENTER;
        } else if (comp == north) {
            return NORTH;
        } else if (comp == south) {
            return SOUTH;
        } else if (comp == east) {
            return EAST;
        } else {
            return WEST;
        }
    }

    /**
     * @inheritDoc
     */
    public void layoutContainer(Container target) {
        Style s = target.getStyle();
        int top = s.getPadding(false, Component.TOP);
        int bottom = target.getLayoutHeight() - target.getBottomGap() - s.getPadding(false, Component.BOTTOM);
        int left = s.getPadding(target.isRTL(), Component.LEFT);
        int right = target.getLayoutWidth() - target.getSideGap() - s.getPadding(target.isRTL(), Component.RIGHT);
        int targetWidth = target.getWidth();
        int targetHeight = target.getHeight();

        boolean rtl = target.isRTL();
        if (rtl) {
        	left+=target.getSideGap();
        }

        if (north != null) {
            Component c = north;
            positionTopBottom(target, c, right, left, targetHeight);
            c.setY(top + c.getStyle().getMargin(false, Component.TOP));
            top += (c.getHeight() + c.getStyle().getMargin(false, Component.TOP) + c.getStyle().getMargin(false, Component.BOTTOM));
        }
        if (south != null) {
            Component c = south;
            positionTopBottom(target, c, right, left, targetHeight);
            c.setY(bottom - c.getHeight() - c.getStyle().getMargin(false, Component.TOP));
            bottom -= (c.getHeight() + c.getStyle().getMargin(false, Component.TOP) + c.getStyle().getMargin(false, Component.BOTTOM));
        }

        Component realEast = east;
        Component realWest = west;

        if (rtl) {
        	realEast = west;
        	realWest = east;
        }


        if (realEast != null) {
            Component c = realEast;
            positionLeftRight(realEast, targetWidth, bottom, top);
            c.setX(right - c.getWidth() - c.getStyle().getMargin(target.isRTL(), Component.LEFT));
            right -= (c.getWidth() + c.getStyle().getMargin(false, Component.LEFT) + c.getStyle().getMargin(false, Component.RIGHT));
        }
        if (realWest != null) {
            Component c = realWest;
            positionLeftRight(realWest, targetWidth, bottom, top);
            c.setX(left + c.getStyle().getMargin(target.isRTL(), Component.LEFT));
            left += (c.getWidth() + c.getStyle().getMargin(false, Component.LEFT) + c.getStyle().getMargin(false, Component.RIGHT));
        }
        if (center != null) {
            Component c = center;
            c.setWidth(right - left - c.getStyle().getMargin(false, Component.LEFT) - c.getStyle().getMargin(false, Component.RIGHT));
            c.setHeight(bottom - top - c.getStyle().getMargin(false, Component.TOP) - c.getStyle().getMargin(false, Component.BOTTOM)); //verify I want to use the remaining size
            c.setX(left + c.getStyle().getMargin(target.isRTL(), Component.LEFT));
            c.setY(top + c.getStyle().getMargin(false, Component.TOP));
        }
    }

    /**
     * Position the east/west component variables
     */
    private void positionLeftRight(Component c, int targetWidth, int bottom, int top) {
        c.setWidth(Math.min(targetWidth, c.getPreferredW()));
        c.setHeight(bottom - top - c.getStyle().getMargin(false, Component.TOP) - c.getStyle().getMargin(false, Component.BOTTOM)); //verify I want to use tge prefered size
        c.setY(top + c.getStyle().getMargin(false, Component.TOP));
    }
    
    private void positionTopBottom(Component target, Component c, int right, int left, int targetHeight) {
        c.setWidth(right - left - c.getStyle().getMargin(false, Component.LEFT) - c.getStyle().getMargin(false, Component.RIGHT));
        c.setHeight(Math.min(targetHeight, c.getPreferredH())); //verify I want to use tge prefered size
        c.setX(left + c.getStyle().getMargin(target.isRTL(), Component.LEFT));
    }
    
    /**
     * @inheritDoc
     */
    public Dimension getPreferredSize(Container parent) {
        Dimension dim = new Dimension(0, 0);

        if (east != null) {
            dim.setWidth(east.getPreferredW() + east.getStyle().getMargin(false, Component.LEFT) + east.getStyle().getMargin(false, Component.RIGHT));
            dim.setHeight(Math.max(east.getPreferredH() + east.getStyle().getMargin(false, Component.TOP) + east.getStyle().getMargin(false, Component.BOTTOM), dim.getHeight()));
        }
        if (west != null) {
            dim.setWidth(dim.getWidth() + west.getPreferredW() + west.getStyle().getMargin(false, Component.LEFT) + west.getStyle().getMargin(false, Component.RIGHT));
            dim.setHeight(Math.max(west.getPreferredH() + west.getStyle().getMargin(false, Component.TOP) + west.getStyle().getMargin(false, Component.BOTTOM), dim.getHeight()));
        }
        if (center != null) {
            dim.setWidth(dim.getWidth() + center.getPreferredW() + center.getStyle().getMargin(false, Component.LEFT) + center.getStyle().getMargin(false, Component.RIGHT));
            dim.setHeight(Math.max(center.getPreferredH() + center.getStyle().getMargin(false, Component.TOP) + center.getStyle().getMargin(false, Component.BOTTOM), dim.getHeight()));
        }
        if (north != null) {
            dim.setWidth(Math.max(north.getPreferredW() + north.getStyle().getMargin(false, Component.LEFT) + north.getStyle().getMargin(false, Component.RIGHT), dim.getWidth()));
            dim.setHeight(dim.getHeight() + north.getPreferredH() + north.getStyle().getMargin(false, Component.TOP) + north.getStyle().getMargin(false, Component.BOTTOM));
        }

        if (south != null) {
            dim.setWidth(Math.max(south.getPreferredW() + south.getStyle().getMargin(false, Component.LEFT) + south.getStyle().getMargin(false, Component.RIGHT), dim.getWidth()));
            dim.setHeight(dim.getHeight() + south.getPreferredH() + south.getStyle().getMargin(false, Component.TOP) + south.getStyle().getMargin(false, Component.BOTTOM));
        }

        dim.setWidth(dim.getWidth() + parent.getStyle().getPadding(false, Component.LEFT) + parent.getStyle().getPadding(false, Component.RIGHT));
        dim.setHeight(dim.getHeight() + parent.getStyle().getPadding(false, Component.TOP) + parent.getStyle().getPadding(false, Component.BOTTOM));
        return dim;
    }

    /**
     * Returns the component in the south location
     * 
     * @return the component in the constraint
     */
    protected Component getSouth() {
        return south;
    }

    /**
     * Returns the component in the center location
     * 
     * @return the component in the constraint
     */
    protected Component getCenter() {
        return center;
    }

    /**
     * Returns the component in the north location
     * 
     * @return the component in the constraint
     */
    protected Component getNorth() {
        return north;
    }

    /**
     * Returns the component in the east location
     * 
     * @return the component in the constraint
     */
    protected Component getEast() {
        return east;
    }

    /**
     * Returns the component in the west location
     * 
     * @return the component in the constraint
     */
    protected Component getWest() {
        return west;
    }
}
