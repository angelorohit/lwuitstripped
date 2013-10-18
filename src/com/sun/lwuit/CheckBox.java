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

package com.sun.lwuit;

import com.sun.lwuit.plaf.UIManager;
import com.sun.lwuit.geom.*;
import com.sun.lwuit.plaf.DefaultLookAndFeel;
import com.sun.lwuit.plaf.LookAndFeel;

/**
 * Checkbox is a button that can be selected or deselected, and which displays
 * its state to the user.
 * 
 * @author Chen Fishbein
 */
public class CheckBox extends Button {
    
    private boolean selected= false;
    
    /**
     * Constructs a checkbox with the given text
     * 
     * @param text to display next to the checkbox
     */
    public CheckBox(String text) {
        this(text, null);
    }

    /**
     * Constructs a checkbox with no text
     */
    public CheckBox() {
        this("");
    }
    
    /**
     * Constructs a checkbox with the given icon
     * 
     * @param icon icon to display next to the checkbox
     */
    public CheckBox(Image icon) {
        this("", icon);
    }

    /**
     * Constructs a checkbox with the given text and icon
     * 
     * @param text to display next to the checkbox
     * @param icon icon to display next to the text
     */
    public CheckBox(String text,Image icon) {
        super(text,icon);
        setUIID("CheckBox");
    }
    
    
    /**
     * Return true if the checkbox is selected
     * 
     * @return true if the checkbox is selected
     */
    public boolean isSelected() {
        return selected;
    }
    
    /**
     * Selects the current checkbox
     * 
     * @param selected value for selection
     */
    public void setSelected(boolean selected) {
        this.selected = selected;
        repaint();
    }
    
    /**
     * @inheritDoc
     */
    void released() {
        if(isEnabled()){
            selected = !isSelected();
        }
        super.released();
    }
    
    /**
     * @inheritDoc
     */
    public void paint(Graphics g) {
        UIManager.getInstance().getLookAndFeel().drawCheckBox(g, this);
    }

    /**
     * @inheritDoc
     */
    protected Dimension calcPreferredSize(){
        return UIManager.getInstance().getLookAndFeel().getCheckBoxPreferredSize(this);
    }

    /**
     * @inheritDoc
     */
    protected String paramString() {
        return super.paramString() + ", selected = " +selected;
    }

    int getAvaliableSpaceForText() {
        LookAndFeel l = UIManager.getInstance().getLookAndFeel();
        if(l instanceof DefaultLookAndFeel) {
            Image[] rButtonImages = ((DefaultLookAndFeel)l).getCheckBoxImages();
            if (rButtonImages != null) {
                int index = isSelected() ? 1 : 0;
                return super.getAvaliableSpaceForText() - rButtonImages[index].getWidth();
            }
        }
        return super.getAvaliableSpaceForText() - (getHeight() + getGap());
    }
}
