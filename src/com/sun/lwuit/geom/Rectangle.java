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
package com.sun.lwuit.geom;

/**
 * Represents a Rectangle position (x, y) and {@link Dimension} (width, height),
 * this is useful for measuring coordinates within the application.
 * 
 * @author Chen Fishbein
 */
public class Rectangle {

    private int x;
    private int y;
    private Dimension size;

    /** 
     * Creates a new instance of Rectangle 
     */
    public Rectangle() {
        size = new Dimension();
    }

    /**
     * Creates a new instance of Rectangle at position (x, y) and with 
     * predefine dimension
     * 
     * @param x the x coordinate of the rectangle
     * @param y the y coordinate of the rectangle
     * @param size the {@link Dimension} of the rectangle
     */
    public Rectangle(int x, int y, Dimension size) {
        this.x = x;
        this.y = y;
        this.size = size;
    }

    /**
     * Creates a new instance of Rectangle at position (x, y) and with 
     * predefine width and height
     * 
     * @param x the x coordinate of the rectangle
     * @param y the y coordinate of the rectangle
     * @param w the width of the rectangle
     * @param h the height of the rectangle
     */
    public Rectangle(int x, int y, int w, int h) {
        this.x = x;
        this.y = y;
        this.size = new Dimension(w, h);
    }

    /** 
     * A copy Constructor

     * @param rect the Rectangle to copy
     */
    public Rectangle(Rectangle rect) {
        this(rect.getX(), rect.getY(),
                rect.getSize().getWidth(), rect.getSize().getHeight());
    }

    /**
     * Return the dimension of the rectangle
     * 
     * @return the size of the rectangle
     */
    public Dimension getSize() {
        return size;
    }

    /**
     * Return the x coordinate of the rectangle
     * 
     * @return the x coordinate of the rectangle
     */
    public int getX() {
        return x;
    }

    /**
     * Return the y coordinate of the rectangle
     * 
     * @return the y coordinate of the rectangle
     */
    public int getY() {
        return y;
    }

    /**
     * @inheritDoc
     */
    public String toString() {
        return "x = " + x + " y = " + y + " size = " + size;
    }

    /**
     * Sets the x position of the rectangle
     * 
     * @param x the x coordinate of the rectangle
     */
    public void setX(int x) {
        this.x = x;
    }

    /**
     * Sets the y position of the rectangle
     * 
     * @param y the y coordinate of the rectangle
     */
    public void setY(int y) {
        this.y = y;
    }

    /**
     * Checks whether or not this Rectangle entirely contains the specified 
     * Rectangle.
     * 
     * @param rect the specified Rectangle 
     * @return true if the Rectangle is contained entirely inside this 
     * Rectangle; false otherwise
     */
    public boolean contains(Rectangle rect) {
        return contains(rect.x, rect.y, rect.size.getWidth(), rect.size.getHeight());
    }

    /**
     * Checks whether this Rectangle entirely contains the Rectangle 
     * at the specified location (rX, rY) with the specified 
     * dimensions (rWidth, rHeight).
     * 
     * @param rX the specified x coordinate
     * @param rY the specified y coordinate
     * @param rWidth the width of the Rectangle
     * @param rHeight the height of the Rectangle
     * @return true if the Rectangle specified by (rX, rY, rWidth, rHeight) 
     * is entirely enclosed inside this Rectangle; false otherwise.
     */
    public boolean contains(int rX, int rY, int rWidth, int rHeight) {
        return x <= rX && y <= rY && x + size.getWidth() >= rX + rWidth &&
                y + size.getHeight() >= rY + rHeight;
    }

    /**
     * Checks whether or not this Rectangle contains the point at the specified 
     * location (rX, rY).
     * 
     * @param rX the specified x coordinate
     * @param rY the specified y coordinate
     * @return true if the point (rX, rY) is inside this Rectangle; 
     * false otherwise.
     */
    public boolean contains(int rX, int rY) {
        return x <= rX && y <= rY && x + size.getWidth() >= rX &&
                y + size.getHeight() >= rY;
    }

    /**
     * Returns a rectangle that intersects the given rectangle with this rectangle
     *
     * @param rX rectangle to intersect with this rectangle
     * @param rY rectangle to intersect with this rectangle
     * @param rW rectangle to intersect with this rectangle
     * @param rH rectangle to intersect with this rectangle
     * @return the intersection
     */
    public Rectangle intersection(int rX, int rY, int rW, int rH) {
        int tx1 = this.x;
        int ty1 = this.y;
        int rx1 = rX;
        int ry1 = rY;
        long tx2 = tx1; tx2 += this.size.getWidth();
        long ty2 = ty1; ty2 += this.size.getHeight();
        long rx2 = rx1; rx2 += rW;
        long ry2 = ry1; ry2 += rH;
        if (tx1 < rx1) {
            tx1 = rx1;
        }
        if (ty1 < ry1) {
            ty1 = ry1;
        }
        if (tx2 > rx2) {
            tx2 = rx2;
        }
        if (ty2 > ry2) {
            ty2 = ry2;
        }
        tx2 -= tx1;
        ty2 -= ty1;
        // tx2,ty2 will never overflow (they will never be
        // larger than the smallest of the two source w,h)
        // they might underflow, though...
        if (tx2 < Integer.MIN_VALUE) {
            tx2 = Integer.MIN_VALUE;
        }
        if (ty2 < Integer.MIN_VALUE) {
            ty2 = Integer.MIN_VALUE;
        }
        return new Rectangle(tx1, ty1, (int) tx2, (int) ty2);
    }

    /**
     * Returns a rectangle that intersects the given rectangle with this rectangle
     * 
     * @param r rectangle to intersect with this rectangle
     * @return the intersection
     */
    public Rectangle intersection(Rectangle r) {
        return intersection(r.x, r.y, r.size.getWidth(), r.size.getHeight());
    }


    /**
     * Determines whether or not this Rectangle and the specified Rectangle 
     * location (x, y) with the specified dimensions (width, height),
     * intersect. Two rectangles intersect if their intersection is nonempty.
     * 
     * @param x the specified x coordinate
     * @param y the specified y coordinate
     * @param width the width of the Rectangle
     * @param height the height of the Rectangle
     * @return true if the specified Rectangle and this Rectangle intersect; 
     * false otherwise.
     */
    public boolean intersects(int x, int y, int width, int height) {
        int tw = size.getWidth();
        int th = size.getHeight();
        return intersects(this.x, this.y, tw, th, x, y, width, height);
    }

    /**
     * Determines whether or not this Rectangle and the specified Rectangle 
     * location (x, y) with the specified dimensions (width, height),
     * intersect. Two rectangles intersect if their intersection is nonempty.
     * 
     * @param rect the Rectangle to check intersection with
     * @return true if the specified Rectangle and this Rectangle intersect; 
     * false otherwise.
     */
    public boolean intersects(Rectangle rect) {
        return intersects(rect.getX(), rect.getY(),
                rect.getSize().getWidth(), rect.getSize().getHeight());
    }

    /**
     * Helper method allowing us to determine if two coordinate sets intersect. This saves
     * us the need of creating a rectangle object for a quick calculation
     * 
     * @param tx x of first rectangle
     * @param ty y of first rectangle
     * @param tw width of first rectangle
     * @param th height of first rectangle
     * @param x x of second rectangle
     * @param y y of second rectangle
     * @param width width of second rectangle
     * @param height height of second rectangle
     * @return true if the rectangles intersect
     */
    public static boolean intersects(int tx, int ty, int tw, int th, int x, int y, int width, int height) {
        int rw = width;
        int rh = height;
        if (rw <= 0 || rh <= 0 || tw <= 0 || th <= 0) {
            return false;
        }
        int rx = x;
        int ry = y;
        rw += rx;
        rh += ry;
        tw += tx;
        th += ty;
        return ((rw < rx || rw > tx) &&
                (rh < ry || rh > ty) &&
                (tw < tx || tw > rx) &&
                (th < ty || th > ry));

    }
}
