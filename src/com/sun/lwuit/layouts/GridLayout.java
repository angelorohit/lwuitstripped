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
import com.sun.lwuit.geom.Dimension;
import com.sun.lwuit.plaf.Style;
import com.sun.lwuit.plaf.UIManager;

/**
 * Components are arranged in an equally sized grid based on available space
 *
 * @author Chen Fishbein
 */
public class GridLayout extends Layout{
    
    private int rows;
    
    private int columns;
    
    /** 
     * Creates a new instance of GridLayout with the given rows and columns
     * 
     * @param rows - number of rows.
     * @param columns - number of columns.
     * @throws IllegalArgumentException if rows < 1 or columns < 1
     */
    public GridLayout(int rows, int columns) {
        this.rows = rows;
        this.columns = columns;
        if(rows < 1 || columns < 1){
            throw new IllegalArgumentException("rows and columns must be greater " +
                    "then zero");
        }
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
        if (rtl) {
        	x += parent.getSideGap();
        }

        int cmpWidth = (width)/columns;
        int cmpHeight;
        if (numOfcomponents > rows * columns) {
            cmpHeight  = (height)/(numOfcomponents/columns + (numOfcomponents%columns == 0 ? 0 : 1));//actual rows number
        } else {
            cmpHeight  = (height)/rows;  
        }
        int row = 0;        
        
        for(int i=0; i< numOfcomponents; i++){
            Component cmp = parent.getComponentAt(i);
            Style cmpStyle = cmp.getStyle();
            int marginLeft = cmpStyle.getMargin(parent.isRTL(), Component.LEFT);
            int marginTop = cmpStyle.getMargin(false, Component.TOP);
            cmp.setWidth(cmpWidth - marginLeft - cmpStyle.getMargin(parent.isRTL(), Component.RIGHT));
            cmp.setHeight(cmpHeight - marginTop - cmpStyle.getMargin(false, Component.BOTTOM));
            if (rtl) {
            	cmp.setX(x + (columns-1-(i%columns))*cmpWidth + marginLeft);
            } else {
            	cmp.setX(x + (i%columns)*cmpWidth + marginLeft);
            }
            cmp.setY(y + row*cmpHeight + marginTop);
            if((i + 1)%columns == 0){
                row++;
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
            width = Math.max(width, cmp.getPreferredW() + cmp.getStyle().getMargin(false, Component.LEFT)+ cmp.getStyle().getMargin(false, Component.RIGHT));
            height = Math.max(height, cmp.getPreferredH()+ cmp.getStyle().getMargin(false, Component.TOP)+ cmp.getStyle().getMargin(false, Component.BOTTOM));
        }

        if(columns > 1){
            width = width*columns;
        }
        
        if(rows > 1){
            if(numOfcomponents>rows*columns){ //if there are more components than planned
               height =  height * (numOfcomponents/columns + (numOfcomponents%columns == 0 ? 0 : 1));
            }else{
                height = height*rows;
            }
        }
        
        return new Dimension(width + parent.getStyle().getPadding(false, Component.LEFT)+ parent.getStyle().getPadding(false, Component.RIGHT),
            height + parent.getStyle().getPadding(false, Component.TOP)+ parent.getStyle().getPadding(false, Component.BOTTOM));
    }
    
}
