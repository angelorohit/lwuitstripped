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

import com.sun.lwuit.animations.CommonTransitions;
import com.sun.lwuit.geom.Dimension;
import com.sun.lwuit.geom.Rectangle;
import com.sun.lwuit.plaf.Style;
import com.sun.lwuit.layouts.BorderLayout;
import com.sun.lwuit.list.DefaultListCellRenderer;
import com.sun.lwuit.list.DefaultListModel;
import com.sun.lwuit.list.ListCellRenderer;
import com.sun.lwuit.list.ListModel;
import com.sun.lwuit.plaf.UIManager;
import java.util.Vector;

/**
 * A combo box is a list that allows only one selection at a time, when a user clicks
 * the combo box a popup button with the full list of elements allows the selection of
 * a single element. The combo box is driven by the list model and allows all the renderer
 * features of the List as well. 
 * 
 * @see List
 * @author Chen Fishbein
 */
public class ComboBox extends List {

    /** 
     * Creates a new instance of ComboBox 
     * 
     * @param items set of items placed into the combo box model
     */
    public ComboBox(Vector items) {
        this(new DefaultListModel(items));
    }

    /** 
     * Creates a new instance of ComboBox 
     * 
     * @param items set of items placed into the combo box model
     */
    public ComboBox(Object[] items) {
        this(new DefaultListModel(items));
    }

    /** 
     * Constructs an empty combo box
     */
    public ComboBox() {
        this(new DefaultListModel());
    }

    /**
     * Creates a new instance of ComboBox 
     * 
     * @param model the model for the combo box elements and selection
     */
    public ComboBox(ListModel model) {
        super(model);
        setUIID("ComboBox");
        ((DefaultListCellRenderer) super.getRenderer()).setShowNumbers(false);
        setInputOnFocus(false);
        setIsScrollVisible(false);
        setFixedSelection(FIXED_NONE_CYCLIC);
        ListCellRenderer r = getRenderer();
        if(r instanceof Component) {
            Component c = (Component) getRenderer();
            c.setUIID("ComboBoxItem");
        }
        Component c = getRenderer().getListFocusComponent(this);
        if(c != null){
            c.setUIID("ComboBoxFocus");
        }
    }

    /**
     * @inheritDoc
     */
    public int getBaseline(int width, int height) {
        Component selected;
        if (getRenderingPrototype() != null) {
            selected = getRenderer().getListCellRendererComponent(this, getRenderingPrototype(), 0, true);
        }
        if (getModel().getSize() > 0) {
            selected = getRenderer().getListCellRendererComponent(this, getModel().getItemAt(0), 0, true);
        } else {
            selected = getRenderer().getListCellRendererComponent(this, "XXXXXXXXXXX", 0, true);
        }
        return getHeight() - getStyle().getPadding(false, BOTTOM) - selected.getStyle().getPadding(false, BOTTOM);
    }

    /**
     * @inheritDoc
     */
    protected void laidOut() {
    }

    /**
     * @inheritDoc
     */
    protected Rectangle getVisibleBounds() {
        return getBounds();
    }
    
    /**
     * @inheritDoc
     */
    public void setSelectedIndex(int selection) {
        super.setSelectedIndex(selection, false);
    }

    /**
     * @inheritDoc
     */
    public void setSelectedIndex(int selection, boolean scroll) {
        super.setSelectedIndex(selection, false);
    }

    /**
     * @inheritDoc
     */
    public void pointerHover(int[] x, int[] y) {
    }

    /**
     * @inheritDoc
     */
    public void pointerHoverReleased(int[] x, int[] y) {
    }

    /**
     * @inheritDoc
     */
    protected void fireClicked() {
        Dialog popupDialog = new Dialog("ComboBoxPopup", "");
        popupDialog.setDisposeWhenPointerOutOfBounds(true);
        popupDialog.setTransitionInAnimator(CommonTransitions.createEmpty());
        popupDialog.setTransitionOutAnimator(CommonTransitions.createEmpty());
        popupDialog.setLayout(new BorderLayout());
        List l = createPopupList();
        l.dispatcher = dispatcher;
        l.eventSource = this;
        popupDialog.addComponent(BorderLayout.CENTER, l);
        Form parentForm = getComponentForm();
        l.getSelectedStyle().setBorder(null);
        Style popupDialogStyle = popupDialog.getContentPane().getStyle();
        int listW = Math.min(getWidth() + UIManager.getInstance().getLookAndFeel().getVerticalScrollWidth(), parentForm.getContentPane().getWidth());
        int listH = popupDialog.getContentPane().getPreferredH() 
                 + popupDialogStyle.getPadding(false, TOP) + popupDialogStyle.getPadding(false, BOTTOM);
        int bottom = 0;
        int top = getAbsoluteY();
        int formHeight = parentForm.getHeight();
        if(parentForm.getSoftButtonCount() > 1) {
            Component c = parentForm.getSoftButton(0).getParent();
            formHeight -= c.getHeight();
            Style s = c.getStyle();
            formHeight -= (s.getMargin(TOP) + s.getMargin(BOTTOM));
        }

        if(listH < formHeight) {
            // pop up or down?
            if(top > formHeight / 2) {
                bottom = formHeight - top;
                top = top - listH;
            } else {
                top +=  getHeight();
                bottom = formHeight - top - listH;
            }
        } else {
            top = 0;
        }

        int left = getAbsoluteX();
        int right = parentForm.getWidth() - left - listW;
        if(right < 0) {
            left += right;
            right = 0;
        }
        l.disposeDialogOnSelection = true;
        popupDialog.setBackCommand(popupDialog.cancelMenuItem);
        popupDialog.setScrollable(false);
        if (Display.getInstance().isThirdSoftButton()) {
            popupDialog.addCommand(popupDialog.selectMenuItem);
            popupDialog.addCommand(popupDialog.cancelMenuItem);
        } else {
            popupDialog.addCommand(popupDialog.cancelMenuItem);
            popupDialog.addCommand(popupDialog.selectMenuItem);
        }
        int originalSel = getSelectedIndex();
        int tint = parentForm.getTintColor();
        parentForm.setTintColor(0);
        Form.comboLock = true;
        Command result = popupDialog.show(Math.max(top, 0), 
                Math.max(bottom, 0), 
                Math.max(left, 0), 
                Math.max(right, 0), false, true);
        Form.comboLock = false;
        parentForm.setTintColor(tint);
        if(result == popupDialog.cancelMenuItem) {
            setSelectedIndex(originalSel);
        }
    }

    /**
     * Creates the list object used within the popup dialog. This method allows subclasses
     * to customize the list creation for the popup dialog shown when the combo box is pressed.
     * 
     * @return a newly created list object used when the user presses the combo box.
     */
    protected List createPopupList() {
        List l = new List(getModel());
        l.setSmoothScrolling(isSmoothScrolling());
        l.setFixedSelection(getFixedSelection());
        l.setListCellRenderer(getRenderer());
        l.setItemGap(getItemGap());
        l.setUIID("ComboBoxList");
        return l;
    }


    /**
     * @inheritDoc
     */
    public void keyReleased(int keyCode) {
        // other events are in keyReleased to prevent the next event from reaching the next form
        int gameAction = Display.getInstance().getGameAction(keyCode);
        if (gameAction == Display.GAME_FIRE) {
            fireClicked();
            return;
        }
        super.keyPressed(keyCode);
    }

    /**
     * Prevent the combo box from losing selection in some use cases
     */
    void selectElement(int selectedIndex) {
    }

    /**
     * @inheritDoc
     */
    public void pointerReleased(int x, int y) {
        if(isEnabled()) {
            fireClicked();
        }
    }

    /**
     * @inheritDoc
     */
    public void paint(Graphics g) {
        UIManager.getInstance().getLookAndFeel().drawComboBox(g, this);
    }

    /**
     * @inheritDoc
     */
    protected Dimension calcPreferredSize() {
        return UIManager.getInstance().getLookAndFeel().getComboBoxPreferredSize(this);
    }

    /**
     * @inheritDoc
     */
    public int getOrientation() {
        return COMBO;
    }
}
