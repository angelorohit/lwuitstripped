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

import com.sun.lwuit.geom.Dimension;
import com.sun.lwuit.plaf.DefaultLookAndFeel;
import com.sun.lwuit.plaf.LookAndFeel;
import com.sun.lwuit.plaf.UIManager;

/**
 * RadioButton is a {@link Button} that maintains a selection state exclusively
 * within a specific {@link ButtonGroup}
 * 
 * @author Chen Fishbein
 */
public class RadioButton extends Button {
    
    private boolean selected= false;
    
    /**
     * The group in which this button is a part
     */
    private ButtonGroup group;
    
    /**
     * Constructs a radio with the given text
     * 
     * @param text to display next to the button
     */
    public RadioButton(String text) {
        this(text, null);
    }
    
    /**
     * Creates an empty radio button
     */
    public RadioButton() {
        this("");
    }
    
    /**
     * Constructs a radio with the given icon
     * 
     * @param icon icon to show next to the button
     */
    public RadioButton(Image icon) {
        this("", icon);
    }

    /**
     * Constructs a radio with the given text and icon
     * 
     * @param text to display next to the button
     * @param icon icon to show next to the button
     */
    public RadioButton(String text,Image icon) {
        super(text,icon);
        setUIID("RadioButton");
    }
    
    /**
     * @inheritDoc
     */
    public String toString() {
        return "Radio Button " + getText();
    }

    int getAvaliableSpaceForText() {
        LookAndFeel l = UIManager.getInstance().getLookAndFeel();
        if(l instanceof DefaultLookAndFeel) {
            Image[] rButtonImages = ((DefaultLookAndFeel)l).getRadioButtonImages();
            if (rButtonImages != null) {
                int index = isSelected() ? 1 : 0;
                return super.getAvaliableSpaceForText() - rButtonImages[index].getWidth();
            }
        }
        return super.getAvaliableSpaceForText() - (getHeight() + getGap());
    }
    
    /**
     * Returns true if the radio button is selected
     * 
     * @return true if the radio button is selected
     */
    public boolean isSelected() {
        return selected;
    }

    void setSelectedImpl(boolean selected) {
        this.selected = selected;
        repaint();
    }
    
    /**
     * Selects the current radio button
     * 
     * @param selected value for selection
     */
    public void setSelected(boolean selected) {
        setSelectedImpl(selected);
        if(group != null && selected) {
            group.setSelected(this);
        }
    }
    
    /**
     * @inheritDoc
     */
    void released() {
        // prevent the radio button from being "turned off"
        if(!isSelected()) {
            setSelected(true);
        }
        super.released();
    }
    
    /**
     * @inheritDoc
     */
    public void paint(Graphics g) {
        UIManager.getInstance().getLookAndFeel().drawRadioButton(g, this);
    }
    
    /**
     * @inheritDoc
     */
    protected Dimension calcPreferredSize(){
        return UIManager.getInstance().getLookAndFeel().getRadioButtonPreferredSize(this);
    }
    
    /**
     * Setting a new button group
     * 
     * @param group a new button group
     */
    void setGroup(ButtonGroup group) {
        this.group = group;
    }

    /**
     * @inheritDoc
     */
    void fireActionEvent() {
        if(group != null) {
            group.setSelected(this);
        }
        super.fireActionEvent();
    }
}
