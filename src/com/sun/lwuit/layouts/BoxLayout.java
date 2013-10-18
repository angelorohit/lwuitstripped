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
import com.sun.lwuit.plaf.UIManager;

/**
 * Layout manager that places elements in a row or column according to 
 * box orientation
 *
 * @author Chen Fishbein
 */
public class BoxLayout extends Layout{
    
    /**
     * Horizontal layout where components are arranged from left to right
     */
    public static final int X_AXIS = 1;

    /**
     * Vertical layout where components are arranged from top to bottom
     */
    public static final int Y_AXIS = 2;
    
    private int axis;
    
    /** 
     * Creates a new instance of BoxLayout
     * 
     * @param axis the axis to lay out components along. 
     * Can be: BoxLayout.X_AXIS or BoxLayout.Y_AXIS
     */
    public BoxLayout(int axis) {
        this.axis = axis;
    }
    
    /**
     * @inheritDoc
     */
    public void layoutContainer(Container parent) {        
        int width = parent.getLayoutWidth() - parent.getSideGap() - parent.getStyle().getPadding(false, Component.RIGHT) - parent.getStyle().getPadding(false, Component.LEFT);
        int height = parent.getLayoutHeight() - parent.getBottomGap() - parent.getStyle().getPadding(false, Component.BOTTOM) - parent.getStyle().getPadding(false, Component.TOP);
        int x = parent.getStyle().getPadding(parent.isRTL(), Component.LEFT);
        int y = parent.getStyle().getPadding(false, Component.TOP);
        int numOfcomponents = parent.getComponentCount();
        
        boolean rtl = parent.isRTL();
        if(rtl) {
        	x += parent.getSideGap();
        }
        int initX = x;

        for(int i=0; i< numOfcomponents; i++){
            Component cmp = parent.getComponentAt(i);
            
            if(axis == Y_AXIS){
                int cmpBottom = height;
                int cmpH = cmp.getPreferredH();
                
                y += cmp.getStyle().getMargin(false, Component.TOP);
                
                if(y >= cmpBottom){
                    cmpH = 0;
                }else if(y + cmpH - parent.getStyle().getPadding(false, Component.TOP) > cmpBottom){
                    cmpH = cmpBottom - y - cmp.getStyle().getMargin(false, Component.BOTTOM);
                }
                cmp.setWidth(width - cmp.getStyle().getMargin(parent.isRTL(), Component.LEFT) - cmp.getStyle().getMargin(parent.isRTL(), Component.RIGHT));
                cmp.setHeight(cmpH);
                cmp.setX(x + cmp.getStyle().getMargin(parent.isRTL(), Component.LEFT));
                cmp.setY(y);
                y += cmp.getHeight() + cmp.getStyle().getMargin(false, Component.BOTTOM);
            }else{
                int cmpRight = width;
                int cmpW = cmp.getPreferredW();
                
                x += cmp.getStyle().getMargin(false, Component.LEFT);

                if(x >= cmpRight){
                    cmpW = 0;
                } else {
                    if(x + cmpW - parent.getStyle().getPadding(false, Component.LEFT) > cmpRight){
                        cmpW = cmpRight - x - cmp.getStyle().getMargin(false, Component.RIGHT);
                    }
                }
                cmp.setWidth(cmpW);
                cmp.setHeight(height- cmp.getStyle().getMargin(false, Component.TOP) - cmp.getStyle().getMargin(false, Component.BOTTOM));
                if(rtl) {
                	cmp.setX(width + initX - (x - initX) - cmpW);
                } else {
                	cmp.setX(x);
                }
                cmp.setY(y + cmp.getStyle().getMargin(false, Component.TOP));
                x += cmp.getWidth() + cmp.getStyle().getMargin(false, Component.RIGHT);
            }
        }
    }
    
    /**
     * @inheritDoc
     */
    public Dimension getPreferredSize(Container parent) {
        int width = 0;
        int height = 0;

        int numOfcomponents = parent.getComponentCount();
        for(int i=0; i< numOfcomponents; i++){
            Component cmp = parent.getComponentAt(i);
            
            if(axis == Y_AXIS){
                int cmpH = cmp.getPreferredH() + cmp.getStyle().getMargin(false, Component.TOP) + cmp.getStyle().getMargin(false, Component.BOTTOM);
                height += cmpH;
                width = Math.max(width , cmp.getPreferredW()+ cmp.getStyle().getMargin(false, Component.LEFT) + cmp.getStyle().getMargin(false, Component.RIGHT));
            }else{
                int cmpW = cmp.getPreferredW() + cmp.getStyle().getMargin(false, Component.LEFT) + cmp.getStyle().getMargin(false, Component.RIGHT);
                width += cmpW;
                height = Math.max(height, cmp.getPreferredH() + cmp.getStyle().getMargin(false, Component.TOP) + cmp.getStyle().getMargin(false, Component.BOTTOM));
            }
        }
        Dimension d = new Dimension(width + parent.getStyle().getPadding(false, Component.LEFT)+ parent.getStyle().getPadding(false, Component.RIGHT),
        height + parent.getStyle().getPadding(false, Component.TOP)+ parent.getStyle().getPadding(false, Component.BOTTOM));
        return d;
    }  
    
}
