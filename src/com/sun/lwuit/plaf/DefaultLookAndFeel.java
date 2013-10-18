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
package com.sun.lwuit.plaf;

import com.sun.lwuit.geom.Dimension;
import com.sun.lwuit.Graphics;
import com.sun.lwuit.Image;
import com.sun.lwuit.Button;
import com.sun.lwuit.Component;
import com.sun.lwuit.Display;
import com.sun.lwuit.Label;
import com.sun.lwuit.List;
import com.sun.lwuit.TextArea;
import com.sun.lwuit.list.ListCellRenderer;
import com.sun.lwuit.list.ListModel;
import com.sun.lwuit.Font;
import com.sun.lwuit.TabbedPane;
import com.sun.lwuit.events.FocusListener;
import com.sun.lwuit.geom.Rectangle;

/**
 * Used to render the default look of LWUIT
 *
 * @author Chen Fishbein
 */
public class DefaultLookAndFeel extends LookAndFeel implements FocusListener {
    private Image[] chkBoxImages = null;
    private Image comboImage = null;
    private Image[] rButtonImages = null;
    private boolean tickWhenFocused = true;

    /** Creates a new instance of DefaultLookAndFeel */
    public DefaultLookAndFeel() {
    }

    /**
     * @inheritDoc
     */
    public void bind(Component cmp) {
        if (tickWhenFocused && cmp instanceof Label) {
            ((Label) cmp).addFocusListener(this);
        }
    }
    /**
     * This method allows to set all Labels, Buttons, CheckBoxes, RadioButtons
     * to start ticking when the text is too long. 
     * @param tickWhenFocused
     */
    public void setTickWhenFocused(boolean tickWhenFocused) {
        this.tickWhenFocused = tickWhenFocused;
    }

    /**
     * Sets images for checkbox checked/unchecked modes
     * 
     * @param checked the image to draw in order to represent a checked checkbox
     * @param unchecked the image to draw in order to represent an uncheck checkbox
     */
    public void setCheckBoxImages(Image checked, Image unchecked) {
        if (checked == null || unchecked == null) {
            chkBoxImages = null;
        } else {
            chkBoxImages = new Image[]{unchecked, checked};
        }
    }

    /**
     * Sets image for the combo box dropdown drawing
     * 
     * @param picker picker image
     */
    public void setComboBoxImage(Image picker) {
        comboImage = picker;
    }

    /**
     * Sets images for radio button selected/unselected modes
     * 
     * @param selected the image to draw in order to represent a selected radio button
     * @param unselected the image to draw in order to represent an unselected radio button
     */
    public void setRadioButtonImages(Image selected, Image unselected) {
        if (selected == null || unselected == null) {
            rButtonImages = null;
        } else {
            rButtonImages = new Image[]{unselected, selected};
        }
    }

    /**
     * Returns the images used to represent the radio button (selected followed by unselected).
     * 
     * @return images representing the radio button or null for using the default drawing
     */
    public Image[] getRadioButtonImages() {
        return rButtonImages;
    }

    /**
     * Returns the images used to represent the checkbox (selected followed by unselected).
     *
     * @return images representing the check box or null for using the default drawing
     */
    public Image[] getCheckBoxImages() {
        return chkBoxImages;
    }

    /**
     * @inheritDoc
     */
    public void drawButton(Graphics g, Button b) {
        drawComponent(g, b, b.getIconFromState(), null, 0);
    }

    /**
     * @inheritDoc
     */
    public void drawCheckBox(Graphics g, Button cb) {
        if (chkBoxImages != null) {
            drawComponent(g, cb, cb.getIconFromState(), chkBoxImages[cb.isSelected() ? 1 : 0], 0);
        } else {
            Style style = cb.getStyle();

            // checkbox square needs to be the height and width of the font height even
            // when no text is in the check box this is a good indication of phone DPI
            int height = cb.getStyle().getFont().getHeight();

            drawComponent(g, cb, cb.getIconFromState(), null, height);

            int gradientColor;
            g.setColor(style.getFgColor());
            gradientColor = style.getBgColor();

            int width = height;

            int rectWidth = scaleCoordinate(12f, 16, width);
            int tX = cb.getX();
            if (cb.isRTL()) {
            	tX = tX + cb.getWidth() - style.getPadding(cb.isRTL(), Component.LEFT) - rectWidth;
            } else {
            	tX += style.getPadding(cb.isRTL(), Component.LEFT);
            }

            int tY = cb.getY() + style.getPadding(false, Component.TOP) + (cb.getHeight() - style.getPadding(false, Component.TOP) - style.getPadding(false, Component.BOTTOM)) / 2 - height / 2;
            g.translate(tX, tY);
            int x = scaleCoordinate(1.04f, 16, width);
            int y = scaleCoordinate(4.0f, 16, height);
            int rectHeight = scaleCoordinate(12f, 16, height);

            // brighten or darken the color slightly
            int destColor = findDestColor(gradientColor);

            g.fillLinearGradient(gradientColor, destColor, x + 1, y + 1, rectWidth - 2, rectHeight - 1, false);
            g.drawRoundRect(x, y, rectWidth, rectHeight, 5, 5);

            if (cb.isSelected()) {
                int color = g.getColor();
                g.setColor(0x111111);
                g.translate(0, 1);
                fillCheckbox(g, width, height);
                g.setColor(color);
                g.translate(0, -1);
                fillCheckbox(g, width, height);
            }
            g.translate(-tX, -tY);
        }
    }

    private static void fillCheckbox(Graphics g, int width, int height) {
        int x1 = scaleCoordinate(2.0450495f, 16, width);
        int y1 = scaleCoordinate(9.4227722f, 16, height);
        int x2 = scaleCoordinate(5.8675725f, 16, width);
        int y2 = scaleCoordinate(13.921746f, 16, height);
        int x3 = scaleCoordinate(5.8675725f, 16, width);
        int y3 = scaleCoordinate(11f, 16, height);
        g.fillTriangle(x1, y1, x2, y2, x3, y3);

        x1 = scaleCoordinate(14.38995f, 16, width);
        y1 = scaleCoordinate(0, 16, height);
        g.fillTriangle(x1, y1, x2, y2, x3, y3);
    }

    private static int round(float x) {
        int rounded = (int) x;
        if (x - rounded > 0.5f) {
            return rounded + 1;
        }
        return rounded;
    }

    /**
     * Takes a floating point coordinate on a virtual axis and rusterizes it to 
     * a coordinate in the pixel surface. This is a very simple algorithm since
     * anti-aliasing isn't supported.
     * 
     * @param coordinate a position in a theoretical plain
     * @param plain the amount of space in the theoretical plain
     * @param pixelSize the amount of pixels available on the screen
     * @return the pixel which we should color
     */
    private static int scaleCoordinate(float coordinate, float plain, int pixelSize) {
        return round(coordinate / plain * pixelSize);
    }

    /**
     * @inheritDoc
     */
    public void drawLabel(Graphics g, Label l) {
        drawComponent(g, l, l.getIcon(), null, 0);
    }

    /**
     * @inheritDoc
     */
    public void drawRadioButton(Graphics g, Button rb) {
        if (rButtonImages != null) {
            drawComponent(g, rb, rb.getIconFromState(), rButtonImages[rb.isSelected() ? 1 : 0], 0);
        } else {
            Style style = rb.getStyle();

            // radio button radius needs to be of the size of the font height even
            // when no text is in the radio button this is a good indication of phone DPI
            int height = rb.getStyle().getFont().getHeight();

            drawComponent(g, rb, rb.getIconFromState(), null, height + rb.getGap());
            g.setColor(style.getFgColor());
            int x = rb.getX();
            if (rb.isRTL()) {
            	x = x + rb.getWidth() - style.getPadding(rb.isRTL(), Component.LEFT) - height;
            } else {
            	x += style.getPadding(rb.isRTL(), Component.LEFT);
            }

            int y = rb.getY();

            // center the RadioButton
            y += Math.max(0, rb.getHeight() / 2 - height / 2);

            g.drawArc(x, y, height, height, 0, 360);
            if (rb.isSelected()) {
                int color = g.getColor();
                int destColor = findDestColor(color);
                g.fillRadialGradient(color, destColor, x + 3, y + 3, height - 5, height - 5);
            }
        }
    }

    /**
     * @inheritDoc
     */
    public void drawComboBox(Graphics g, List cb) {
        int border = 2;
        Style style = cb.getStyle();
        int leftPadding = style.getPadding(cb.isRTL(), Component.LEFT);
        int rightPadding = style.getPadding(cb.isRTL(), Component.RIGHT);

        setFG(g, cb);

        ListModel model = cb.getModel();
        ListCellRenderer renderer = cb.getRenderer();
        Object value = model.getItemAt(model.getSelectedIndex());
        int comboImageWidth;
        if (comboImage != null) {
            comboImageWidth = comboImage.getWidth();
        } else {
            comboImageWidth = style.getFont().getHeight();
        }
        
        int cellX = cb.getX() + style.getPadding(false, Component.TOP);
        if(cb.isRTL()){
            cellX += comboImageWidth;
        }
        
        if (model.getSize() > 0) {
            Component cmp = renderer.getListCellRendererComponent(cb, value, model.getSelectedIndex(), cb.hasFocus());
            cmp.setX(cellX);
            cmp.setY(cb.getY() + style.getPadding(false, Component.TOP));
            cmp.setWidth(cb.getWidth() - comboImageWidth - 2 * rightPadding - leftPadding);
            cmp.setHeight(cb.getHeight() - style.getPadding(false, Component.TOP) - style.getPadding(false, Component.BOTTOM));
            cmp.paint(g);
        }

        g.setColor(style.getBgColor());
        int y = cb.getY();
        int height = cb.getHeight();
        int width = comboImageWidth + border;
        int x = cb.getX();
        if (cb.isRTL()) {
        	x += leftPadding;
        } else {
        	x += cb.getWidth() - comboImageWidth - rightPadding - border;
        }

        if (comboImage != null) {
            g.drawImage(comboImage, x, y + height / 2 - comboImage.getHeight() / 2);
        } else {
            int color = g.getColor();

            // brighten or darken the color slightly
            int destColor = findDestColor(color);

            g.fillLinearGradient(g.getColor(), destColor, x, y, width, height, false);
            g.setColor(color);
            g.drawRect(x, y, width, height - 1);

            width--;
            height--;

            //g.drawRect(x, y, width, height);
            g.translate(x + 1, y + 1);
            g.setColor(0x111111);
            int x1 = scaleCoordinate(2.5652081f, 16, width);
            int y1 = scaleCoordinate(4.4753664f, 16, height);
            int x2 = scaleCoordinate(8.2872691f, 16, width);
            int y2 = scaleCoordinate(10f, 16, height);
            int x3 = scaleCoordinate(13.516078f, 16, width);
            int y3 = y1;
            g.fillTriangle(x1, y1, x2, y2, x3, y3);
            g.translate(-1, -1);
            g.setColor(style.getFgColor());
            g.fillTriangle(x1, y1, x2, y2, x3, y3);
            //g.setColor(style.getFgColor());
            //g.fillTriangle(x1 + 2, y1 + 2, x2, y2 - 2, x3 - 2, y3 + 2);

            g.translate(-x, -y);
        }

    }

    /**
     * Finds a suitable destination color for gradient values
     */
    private int findDestColor(int color) {
        // brighten or darken the color slightly
        int sourceR = color >> 16 & 0xff;
        int sourceG = color >> 8 & 0xff;
        int sourceB = color & 0xff;
        if (sourceR > 128 && sourceG > 128 && sourceB > 128) {
            // darken
            sourceR = Math.max(sourceR >> 1, 0);
            sourceG = Math.max(sourceG >> 1, 0);
            sourceB = Math.max(sourceB >> 1, 0);
        } else {
            // special case for black, since all channels are 0 it can't be brightened properly...
            if(color == 0) {
                return 0x222222;
            }
            
            // brighten
            sourceR = Math.min(sourceR << 1, 0xff);
            sourceG = Math.min(sourceG << 1, 0xff);
            sourceB = Math.min(sourceB << 1, 0xff);
        }
        return ((sourceR << 16) & 0xff0000) | ((sourceG << 8) & 0xff00) | (sourceB & 0xff);
    }

    /**
     * @inheritDoc
     */
    public void drawList(Graphics g, List l) {
    }

    /**
     * @inheritDoc
     */
    public void drawTextArea(Graphics g, TextArea ta) {
        setFG(g, ta);
        int line = ta.getLines();
        int oX = g.getClipX();
        int oY = g.getClipY();
        int oWidth = g.getClipWidth();
        int oHeight = g.getClipHeight();
        Font f = ta.getStyle().getFont();
        int fontHeight = f.getHeight();

        int align = ta.getAbsoluteAlignment();

        int leftPadding = ta.getStyle().getPadding(ta.isRTL(), Component.LEFT);
        int rightPadding = ta.getStyle().getPadding(ta.isRTL(), Component.RIGHT);
        int topPadding = ta.getStyle().getPadding(false, Component.TOP);
        boolean shouldBreak = false;
        
        for (int i = 0; i < line; i++) {
            int x = ta.getX() + leftPadding;
            int y = ta.getY() +  topPadding +
                    (ta.getRowsGap() + fontHeight) * i;
            if(Rectangle.intersects(x, y, ta.getWidth(), fontHeight, oX, oY, oWidth, oHeight)) {
                
                String rowText = (String) ta.getTextAt(i);
                //display ******** if it is a password field
                String displayText = "";
                if ((ta.getConstraint() & TextArea.PASSWORD) != 0) {
                    for (int j = 0; j < rowText.length(); j++) {
                        displayText += "*";
                    }
                } else {
                    displayText = rowText;
                }

                switch(align) {
                    case Component.RIGHT:
                		x = ta.getX() + ta.getWidth() - rightPadding - f.stringWidth(displayText);
                        break;
                    case Component.CENTER:
                        x+= (ta.getWidth()-leftPadding-rightPadding-f.stringWidth(displayText))/2;
                        break;
                }
            
                g.drawString(displayText, x, y);
                shouldBreak = true;
            }else{
                if(shouldBreak){
                    break;
                }
            }
        }
    }

    /**
     * @inheritDoc
     */
    public Dimension getButtonPreferredSize(Button b) {
        return getPreferredSize(b, new Image[]{b.getIcon(), b.getRolloverIcon(), b.getPressedIcon()}, null);
    }

    /**
     * @inheritDoc
     */
    public Dimension getCheckBoxPreferredSize(Button cb) {
        if (chkBoxImages != null) {
            return getPreferredSize(cb, new Image[]{cb.getIcon(), cb.getRolloverIcon(), cb.getPressedIcon()}, chkBoxImages[0]);
        }
        Dimension d = getPreferredSize(cb, new Image[]{cb.getIcon(), cb.getRolloverIcon(), cb.getPressedIcon()}, null);

        // checkbox square needs to be the height and width of the font height even
        // when no text is in the check box this is a good indication of phone DPI
        int checkBoxSquareSize = cb.getStyle().getFont().getHeight();

        // allow for checkboxes without a string within them
        d.setHeight(Math.max(checkBoxSquareSize, d.getHeight()));

        d.setWidth(d.getWidth() + checkBoxSquareSize + cb.getGap());
        return d;
    }

    /**
     * @inheritDoc
     */
    public Dimension getLabelPreferredSize(Label l) {
        return getPreferredSize(l, new Image[]{l.getIcon()}, null);
    }

    /**
     * @inheritDoc
     */
    private Dimension getPreferredSize(Label l, Image[] icons, Image stateImage) {
        int prefW = 0;
        int prefH = 0;

        Style style = l.getStyle();
        int gap = l.getGap();
        for (int i = 0; i < icons.length; i++) {
            Image icon = icons[i];
            if (icon != null) {
                prefW = Math.max(prefW, icon.getWidth());
                prefH = Math.max(prefH, icon.getHeight());
            }
        }
        String text = l.getText();
        Font font = style.getFont();
        if (font == null) {
            System.out.println("Missing font for " + l);
            font = Font.getDefaultFont();
        }
        if (text != null && text.length() > 0) {
            //add the text size
            switch (l.getTextPosition()) {
                case Label.LEFT:
                case Label.RIGHT:
                    prefW += font.stringWidth(text);
                    prefH = Math.max(prefH, font.getHeight());
                    break;
                case Label.BOTTOM:
                case Label.TOP:
                    prefW = Math.max(prefW, font.stringWidth(text));
                    prefH += font.getHeight();
                    break;
            }
        }
        //add the state image(relevant for CheckBox\RadioButton)
        if (stateImage != null) {
            prefW += (stateImage.getWidth() + gap);
            prefH = Math.max(prefH, stateImage.getHeight());
        }


        if (icons[0] != null && text != null && text.length() > 0) {
            switch (l.getTextPosition()) {
                case Label.LEFT:
                case Label.RIGHT:
                    prefW += gap;
                    break;
                case Label.BOTTOM:
                case Label.TOP:
                    prefH += gap;
                    break;
            }
        }

        if (prefH != 0) {
            prefH += (style.getPadding(false, Component.TOP) + style.getPadding(false, Component.BOTTOM));
        }
        if (prefW != 0) {
            prefW += (style.getPadding(l.isRTL(), Component.RIGHT) + style.getPadding(l.isRTL(), Component.LEFT));
        }
        return new Dimension(prefW, prefH);
    }

    /**
     * @inheritDoc
     */
    public Dimension getListPreferredSize(List l) {
        int width = 0;
        int height = 0;
        int selectedHeight;
        int selectedWidth;
        ListModel model = l.getModel();
        int numOfcomponents = Math.max(model.getSize(), l.getMinElementHeight());
        Object prototype = l.getRenderingPrototype();

        Style unselectedEntryStyle = null;
        Style selectedEntryStyle;
        if(prototype != null) {
            ListCellRenderer renderer = l.getRenderer();
            Component cmp = renderer.getListCellRendererComponent(l, prototype, 0, false);
            height = cmp.getPreferredH();
            width = cmp.getPreferredW();
            unselectedEntryStyle = cmp.getStyle();
            cmp = renderer.getListCellRendererComponent(l, prototype, 0, true);

            selectedEntryStyle = cmp.getStyle();
            selectedHeight = Math.max(height, cmp.getPreferredH());
            selectedWidth = Math.max(width, cmp.getPreferredW());
        } else {
            int hightCalcComponents = Math.min(5, numOfcomponents);
            Object dummyProto = l.getRenderingPrototype();
            if(model.getSize() > 0 && dummyProto == null) {
                dummyProto = model.getItemAt(0);
            }
            ListCellRenderer renderer = l.getRenderer();
            for (int i = 0; i < hightCalcComponents; i++) {
                Object value;
                if(i < model.getSize()) {
                    value = model.getItemAt(i);
                } else {
                    value = dummyProto;
                }
                Component cmp = renderer.getListCellRendererComponent(l, value, i, false);
                unselectedEntryStyle = cmp.getStyle();

                height = Math.max(height, cmp.getPreferredH());
                width = Math.max(width, cmp.getPreferredW());
            }
            selectedEntryStyle = unselectedEntryStyle;
            selectedHeight = height;
            selectedWidth = width;
            if (model.getSize() > 0) {
                Object value = model.getItemAt(0);
                Component cmp = renderer.getListCellRendererComponent(l, value, 0, true);

                selectedHeight = Math.max(height, cmp.getPreferredH());
                selectedWidth = Math.max(width, cmp.getPreferredW());
                selectedEntryStyle = cmp.getStyle();
            }
        }
        if(unselectedEntryStyle != null) {
            selectedWidth += selectedEntryStyle.getMargin(false, Component.LEFT) + selectedEntryStyle.getMargin(false, Component.RIGHT);
            selectedHeight += selectedEntryStyle.getMargin(false, Component.TOP) + selectedEntryStyle.getMargin(false, Component.BOTTOM);
            width += unselectedEntryStyle.getMargin(false, Component.LEFT) + unselectedEntryStyle.getMargin(false, Component.RIGHT);
            height += unselectedEntryStyle.getMargin(false, Component.TOP) + unselectedEntryStyle.getMargin(false, Component.BOTTOM);
        }

        Style lStyle = l.getStyle();
        int verticalPadding = lStyle.getPadding(false, Component.TOP) + lStyle.getPadding(false, Component.BOTTOM);
        int horizontalPadding = lStyle.getPadding(false, Component.RIGHT) + lStyle.getPadding(false, Component.LEFT);

        if (numOfcomponents == 0) {
            return new Dimension(horizontalPadding, verticalPadding);
        }

        // If combobox without ever importing the ComboBox class dependency
        if(l.getOrientation() > List.HORIZONTAL) {
            int boxWidth = l.getStyle().getFont().getHeight() + 2;
            return new Dimension(boxWidth + selectedWidth + horizontalPadding, selectedHeight + verticalPadding);
        } else {
            if (l.getOrientation() == List.VERTICAL) {
                return new Dimension(selectedWidth + horizontalPadding, selectedHeight + (height + l.getItemGap()) * (numOfcomponents - 1) + verticalPadding);
            } else {
                return new Dimension(selectedWidth + (width + l.getItemGap()) * (numOfcomponents - 1) + horizontalPadding, selectedHeight + verticalPadding);
            }
        }
    }
    
    /**
     * @inheritDoc
     */
    public Dimension getRadioButtonPreferredSize(Button rb) {
        if (rButtonImages != null) {
            return getPreferredSize(rb, new Image[]{rb.getIcon(), rb.getRolloverIcon(), rb.getPressedIcon()}, rButtonImages[0]);
        }
        Dimension d = getPreferredSize(rb, new Image[]{rb.getIcon(), rb.getRolloverIcon(), rb.getPressedIcon()}, null);

        // radio button radius needs to be of the size of the font height even
        // when no text is in the radio button this is a good indication of phone DPI
        int height = rb.getStyle().getFont().getHeight();

        // allow for radio buttons without a string within them
        d.setHeight(Math.max(height, d.getHeight()));

        d.setWidth(d.getWidth() + height + rb.getGap());
        return d;
    }

    /**
     * @inheritDoc
     */
    public Dimension getTextAreaSize(TextArea ta, boolean pref) {
        int prefW = 0;
        int prefH = 0;
        Font f = ta.getStyle().getFont();

        //if this is a text field the preferred size should be the text width
        if (ta.getRows() == 1) {
            prefW = f.stringWidth(ta.getText());
        } else {
            prefW = f.charWidth(TextArea.getWidestChar()) * ta.getColumns();
        }
        int rows;
        if(pref) {
            rows = ta.getActualRows();
        } else {
            rows = ta.getLines();
        }
        prefH = (f.getHeight() + 2) * rows;
        int columns = ta.getColumns();
        String str = "";
        for (int iter = 0; iter < columns; iter++) {
            str += TextArea.getWidestChar();
        }
        if(columns > 0) {
            prefW = Math.max(prefW, f.stringWidth(str));
        }
        prefH = Math.max(prefH, rows * f.getHeight());

        return new Dimension(prefW + ta.getStyle().getPadding(false, Component.RIGHT) + ta.getStyle().getPadding(false, Component.LEFT), 
                prefH + ta.getStyle().getPadding(false, Component.TOP) + ta.getStyle().getPadding(false, Component.BOTTOM));
    }

    /**
     * Reverses alignment in the case of bidi
     */
    private int reverseAlignForBidi(Component c, int align) {
        if(c.isRTL()) {
            switch(align) {
                case Component.RIGHT:
                    return Component.LEFT;
                case Component.LEFT:
                    return Component.RIGHT;
            }
        }
        return align;
    }

    private void drawComponent(Graphics g, Label l, Image icon, Image stateIcon, int preserveSpaceForState) {
        setFG(g, l);

        int gap = l.getGap();
        int stateIconSize = 0;
        int stateIconYPosition = 0;
        String text = l.getText();
        Style style = l.getStyle();

        int leftPadding = style.getPadding(l.isRTL(), Component.LEFT);
        int rightPadding = style.getPadding(l.isRTL(), Component.RIGHT);
        
        Font font = style.getFont();
        int fontHeight = 0;
        if (text == null) {
            text = "";
        }
        if(text.length() > 0){
            fontHeight = font.getHeight();
        }
        
        if (stateIcon != null) {
            stateIconSize = stateIcon.getWidth(); //square image width == height
            stateIconYPosition = l.getY() + l.getStyle().getPadding(false, Component.TOP) +
                    (l.getHeight() - l.getStyle().getPadding(false, Component.TOP) -
                    l.getStyle().getPadding(false, Component.BOTTOM)) / 2 - stateIconSize / 2;
            preserveSpaceForState = stateIconSize + gap;
            int tX = l.getX();
            if (l.isRTL()) {
            	tX = tX + l.getWidth() - style.getPadding(l.isRTL(), Component.LEFT) - stateIcon.getWidth();
            } else {
            	tX += style.getPadding(l.isRTL(), Component.LEFT);
            }

            g.drawImage(stateIcon, tX, stateIconYPosition);
        }

        //default for bottom left alignment
        int x = l.getX() + l.getStyle().getPadding(l.isRTL(), Component.LEFT) + preserveSpaceForState;
        int y = l.getY() + l.getStyle().getPadding(false, Component.TOP);

        int align = reverseAlignForBidi(l, l.getAlignment());

        int textPos= reverseAlignForBidi(l, l.getTextPosition());

        //set initial x,y position according to the alignment and textPosition
        if (align == Component.LEFT) {
            switch (textPos) {
                case Label.LEFT:
                case Label.RIGHT:
                    y = y + (l.getHeight() - (l.getStyle().getPadding(false, Component.TOP) + l.getStyle().getPadding(false, Component.BOTTOM) + Math.max(((icon != null) ? icon.getHeight() : 0), fontHeight))) / 2;
                    break;
                case Label.BOTTOM:
                case Label.TOP:
                    y = y + (l.getHeight() - (l.getStyle().getPadding(false, Component.TOP) + l.getStyle().getPadding(false, Component.BOTTOM) + ((icon != null) ? icon.getHeight() + gap : 0) + fontHeight)) / 2;
                    break;
            }
        } else if (align == Component.CENTER) {
            switch (textPos) {
                case Label.LEFT:
                case Label.RIGHT:
                    x = x + (l.getWidth() - (preserveSpaceForState +
                            leftPadding +
                            rightPadding +
                            ((icon != null) ? icon.getWidth() : 0) +
                            font.stringWidth(text))) / 2;
                    x = Math.max(x, l.getX() + leftPadding + preserveSpaceForState);
                    y = y + (l.getHeight() - (style.getPadding(false, Component.TOP) +
                            style.getPadding(false, Component.BOTTOM) +
                            Math.max(((icon != null) ? icon.getHeight() : 0),
                            fontHeight))) / 2;
                    break;
                case Label.BOTTOM:
                case Label.TOP:
                    x = x + (l.getWidth() - (preserveSpaceForState + leftPadding +
                            rightPadding +
                            Math.max(((icon != null) ? icon.getWidth() : 0),
                            font.stringWidth(text)))) / 2;
                    x = Math.max(x, l.getX() + leftPadding + preserveSpaceForState);
                    y = y + (l.getHeight() - (style.getPadding(false, Component.TOP) +
                            style.getPadding(false, Component.BOTTOM) +
                            ((icon != null) ? icon.getHeight() + gap : 0) +
                            fontHeight)) / 2;
                    break;
            }
        } else if (align == Component.RIGHT) {
            switch (textPos) {
                case Label.LEFT:
                case Label.RIGHT:
                    x = l.getX() + l.getWidth() - rightPadding -
                            ( ((icon != null) ? (icon.getWidth() + gap) : 0) +
                            font.stringWidth(text));
                    if(l.isRTL()) {
                        x = Math.max(x - preserveSpaceForState, l.getX() + leftPadding);
                    } else {
                        x = Math.max(x, l.getX() + leftPadding + preserveSpaceForState);
                    }
                    y = y + (l.getHeight() - (style.getPadding(false, Component.TOP) +
                            style.getPadding(false, Component.BOTTOM) +
                            Math.max(((icon != null) ? icon.getHeight() : 0),
                            fontHeight))) / 2;
                    break;
                case Label.BOTTOM:
                case Label.TOP:
                    x = l.getX() + l.getWidth() - rightPadding -
                             (Math.max(((icon != null) ? (icon.getWidth()) : 0),
                            font.stringWidth(text)));
                    x = Math.max(x, l.getX() + leftPadding + preserveSpaceForState);
                    y = y + (l.getHeight() - (style.getPadding(false, Component.TOP) +
                            style.getPadding(false, Component.BOTTOM) +
                            ((icon != null) ? icon.getHeight() + gap : 0) + fontHeight)) / 2;
                    break;
            }
        }


        int textSpaceW = l.getWidth() - style.getPadding(false, Label.RIGHT) - style.getPadding(false, Label.LEFT);

        if (icon != null && (textPos == Label.RIGHT || textPos == Label.LEFT)) {
            textSpaceW = textSpaceW - icon.getWidth();
        }

        if (stateIcon != null) {
            textSpaceW = textSpaceW - stateIcon.getWidth();
        } else {
            textSpaceW = textSpaceW - preserveSpaceForState;
        }

        if (icon == null) { // no icon only string 
            drawLabelString(g, l, text, x, y, textSpaceW, preserveSpaceForState);
        } else {
            //int strHeight = l.getStyle().getFont().getHeight();
            int strWidth = l.getStyle().getFont().stringWidth(text);
            int iconWidth = icon.getWidth();
            int iconHeight = icon.getHeight();
            int iconStringWGap;
            int iconStringHGap;

            switch (textPos) {
                case Label.LEFT:
                    if (iconHeight > fontHeight) {
                        iconStringHGap = (iconHeight - fontHeight) / 2;
                        //drawLabelStringValign(g, l, text, x, y, iconStringHGap, iconHeight);
                        strWidth = drawLabelStringValign(g, l, text, x, y, iconStringHGap, iconHeight, textSpaceW, fontHeight, preserveSpaceForState);

                        g.drawImage(icon, x + strWidth + gap, y);
                    } else {
                        iconStringHGap = (fontHeight - iconHeight) / 2;
                        //drawLabelString(g, l, text, x, y);
                        strWidth = drawLabelString(g, l, text, x, y, textSpaceW, preserveSpaceForState);

                        g.drawImage(icon, x + strWidth + gap, y + iconStringHGap);
                    }
                    break;
                case Label.RIGHT:
                    if (iconHeight > fontHeight) {
                        iconStringHGap = (iconHeight - fontHeight) / 2;
                        g.drawImage(icon, x, y);
                        //drawLabelStringValign(g, l, text, x+ iconWidth + gap, y, iconStringHGap, iconHeight);
                        drawLabelStringValign(g, l, text, x + iconWidth + gap, y, iconStringHGap, iconHeight, textSpaceW, fontHeight, preserveSpaceForState);
                    } else {
                        iconStringHGap = (fontHeight - iconHeight) / 2;
                        g.drawImage(icon, x, y + iconStringHGap);
                        //drawLabelString(g, l, text, x + iconWidth + gap, y);
                        drawLabelString(g, l, text, x + iconWidth + gap, y, textSpaceW, preserveSpaceForState);
                    }
                    break;
                case Label.BOTTOM:
                    if (iconWidth > strWidth) { //center align the smaller

                        iconStringWGap = (iconWidth - strWidth) / 2;
                        g.drawImage(icon, x, y);
                        //drawLabelString(g, l, text, x + iconStringWGap, y + iconHeight + gap);
                        drawLabelString(g, l, text, x + iconStringWGap, y + iconHeight + gap, textSpaceW, preserveSpaceForState);
                    } else {
                        iconStringWGap = (Math.min(strWidth, textSpaceW) - iconWidth) / 2;
                        g.drawImage(icon, x + iconStringWGap, y);
                        
                        //drawLabelString(g, l, text, x, y + iconHeight + gap);
                        drawLabelString(g, l, text, x, y + iconHeight + gap, textSpaceW, preserveSpaceForState);
                    }
                    break;
                case Label.TOP:
                    if (iconWidth > strWidth) { //center align the smaller

                        iconStringWGap = (iconWidth - strWidth) / 2;
//                        drawLabelString(g, l, text, x + iconStringWGap, y);
                        drawLabelString(g, l, text, x + iconStringWGap, y, textSpaceW, preserveSpaceForState);
                        g.drawImage(icon, x, y + fontHeight + gap);
                    } else {
                        iconStringWGap = (Math.min(strWidth, textSpaceW) - iconWidth) / 2;
//                        drawLabelString(g, l, text, x, y);
                        drawLabelString(g, l, text, x, y, textSpaceW, preserveSpaceForState);
                        g.drawImage(icon, x + iconStringWGap, y + fontHeight + gap);
                    }
                    break;
            }
        }
    }

    /**
     * Implements the drawString for the text component and adjust the valign
     * assuming the icon is in one of the sides
     */
    private int drawLabelStringValign(Graphics g, Label l, String str, int x, int y,
            int iconStringHGap, int iconHeight, int textSpaceW, int fontHeight, int preserveSpaceForState) {
        switch (l.getVerticalAlignment()) {
            case Component.TOP:
                return drawLabelString(g, l, str, x, y, textSpaceW, preserveSpaceForState);
            case Component.CENTER:
                return drawLabelString(g, l, str, x, y + iconHeight / 2 - fontHeight / 2, textSpaceW, preserveSpaceForState);
            default:
                return drawLabelString(g, l, str, x, y + iconStringHGap, textSpaceW, preserveSpaceForState);
        }
    }

    /**
     * Implements the drawString for the text component and adjust the valign
     * assuming the icon is in one of the sides
     */
    private int drawLabelString(Graphics g, Label l, String text, int x, int y, int textSpaceW, int preserveSpaceForState) {
        Style style = l.getStyle();

        int cx = g.getClipX();
        int cy = g.getClipY();
        int cw = g.getClipWidth();
        int ch = g.getClipHeight();

        g.clipRect(x, cy, textSpaceW, ch);

        if (l.isTickerRunning()) {
            if (l.getShiftText() > 0) {
                if (l.getShiftText() > textSpaceW) {
                    l.setShiftText(x - l.getX() - style.getFont().stringWidth(text));
                }
            } else if (l.getShiftText() + style.getFont().stringWidth(text) < 0) {
                l.setShiftText(textSpaceW);
            }
        }
        int drawnW = drawLabelText(g, l, text, x, y, textSpaceW);

        g.setClip(cx, cy, cw, ch);

        return drawnW;
    }

    /**
     * Draws the text of a label
     * 
     * @param g graphics context
     * @param l label component
     * @param text the text for the label
     * @param x position for the label
     * @param y position for the label
     * @param textSpaceW the width available for the component
     * @return the space used by the drawing
     */
    protected int drawLabelText(Graphics g, Label l, String text, int x, int y, int textSpaceW) {
        Font f = l.getStyle().getFont();
        if ((!l.isTickerRunning()) || l.isRTL()) {
            int txtW = f.stringWidth(text);
            
            //if there is no space to draw the text add ... at the end
            if (txtW > textSpaceW && textSpaceW > 0) {
            	// Handling of adding 3 points and in fact all text positioning when the text is bigger than
            	// the allowed space is handled differently in RTL, this is due to the reverse algorithm
            	// effects - i.e. when the text includes both Hebrew/Arabic and English/numbers then simply
            	// trimming characters from the end of the text (as done with LTR) won't do.
            	// Instead we simple reposition the text, and draw the 3 points, this is quite simple, but
            	// the downside is that a part of a letter may be shown here as well.

            	if (l.isRTL()) {
                	if ((!l.isTickerRunning()) && (l.isEndsWith3Points())) {
	            		String points = "...";
	                	int pointsW = f.stringWidth(points);
	            		g.drawString(points, l.getShiftText() + x, y);
	            		g.clipRect(pointsW+l.getShiftText() + x, y, textSpaceW - pointsW, f.getHeight());
                	}
            		x = x - txtW + textSpaceW;
                } else {
                    if (l.isEndsWith3Points()) {
                        String points = "...";
                        int index = 1;
                        String tmp = text.substring(0, index);
                        int pointsW = f.stringWidth(points);
                        while (f.stringWidth(tmp) + pointsW < textSpaceW) {
                            index++;
                            if (index >= text.length()) {
                                break;
                            }
                            tmp = text.substring(0, index);
                        }
                        tmp = text.substring(0, index - 1) + points;
                        text = tmp;
                    }
                }
            }
        }

        g.drawString(text, l.getShiftText() + x, y);
        return Math.min(f.stringWidth(text), textSpaceW);
    }

    /**
     * @inheritDoc
     */
    public Dimension getComboBoxPreferredSize(List cb) {
        return getListPreferredSize(cb);
    }

    /**
     * @inheritDoc
     */
    public void drawTabbedPane(Graphics g, TabbedPane tp) {
    }

    /**
     * @inheritDoc
     */
    public Component getTabbedPaneCell(final TabbedPane tp,
            final String text, final Image icon, final boolean isSelected,
            final boolean cellHasFocus, final Style cellStyle, final Style cellSelectedStyle,
            final Style tabbedPaneStyle, final int cellOffsetX,
            final int cellOffsetY, final Dimension cellsPreferredSize,
            final Dimension contentPaneSize) {
        Label cell = new Label(text) {
            public void paint(Graphics g) {
                int tPBorder = tp.getTabbedPaneBorderWidth();
                int focusMarkWidth = tPBorder * 2;
                int tabP = tp.getTabPlacement();
                // Initialize forground colors before calling to super.paint()
                if (isSelected && cellHasFocus) {
                    focusMarkWidth = tPBorder * 3;
                    this.getStyle().setFgColor(cellSelectedStyle.getFgColor());
                } else {
                    this.getStyle().setFgColor(cellStyle.getFgColor());
                }

                super.paint(g);

                if (!isSelected) {
                    g.setColor(0);
                    g.fillRect(getX(), getY(), getWidth(), getHeight(), (byte) 0x2f);
                }
                // coloring back the focus mark line
                g.setColor(getStyle().getFgColor());
                if (tabP == TabbedPane.TOP || tabP == TabbedPane.BOTTOM) {
                    if (tabP == TabbedPane.TOP) {
                        if (isSelected) {
                            g.fillRect(getX(), getY() + tPBorder, getWidth(), focusMarkWidth);// north

                        }
                        g.setColor(tabbedPaneStyle.getFgColor());
                        g.fillRect(getX(), getY(), getWidth(), tPBorder);//north line

                        g.fillRect(getX(), getY(), tPBorder, getHeight());// west line

                    } else {
                        if (isSelected) {
                            g.fillRect(getX(), getY() + getHeight() - focusMarkWidth, getWidth(), focusMarkWidth);// south

                        }
                        g.setColor(tabbedPaneStyle.getFgColor());
                        g.fillRect(getX(), getY() + getHeight() - tPBorder, getWidth(), tPBorder);//south line

                        g.fillRect(getX(), getY(), tPBorder, getHeight());// west line

                    }
                    int x = getX() - cellOffsetX + getWidth();
                    if (x == contentPaneSize.getWidth()) {
                        g.fillRect(x + cellOffsetX - tPBorder, getY(), tPBorder, getHeight());// east line

                    }
                    if (cellsPreferredSize.getWidth() < contentPaneSize.getWidth() && (getX() + getWidth() == cellsPreferredSize.getWidth())) {
                        g.fillRect(getX() + getWidth() - tPBorder, getY(), tPBorder, getHeight());
                    }
                } else { // LEFT or RIGHT

                    if (isSelected) {
                        g.fillRect(getX(), getY() + tPBorder, getWidth(), focusMarkWidth);// north

                    }
                    g.setColor(tabbedPaneStyle.getFgColor());
                    g.fillRect(getX(), getY(), getWidth(), tPBorder);
                    int y = getY() - cellOffsetY + getHeight();
                    if (y == contentPaneSize.getHeight()) {
                        g.fillRect(getX(), y + cellOffsetY - tPBorder, getWidth(), tPBorder);
                    }
                    if (cellsPreferredSize.getHeight() < contentPaneSize.getHeight() && (getY() + getHeight() == cellsPreferredSize.getHeight())) {
                        g.fillRect(getX(), getY() + getHeight() - tPBorder, getWidth(), tPBorder);//south line

                    }
                    if (!tp.isRTL() && tabP == TabbedPane.LEFT || 
                            tp.isRTL() && tabP == TabbedPane.RIGHT) {
                        g.fillRect(getX(), getY(), tPBorder, getHeight());// west border
                    } else {
                        g.fillRect(getX() + getWidth() - tPBorder, getY(), tPBorder, getHeight());// east border
                    }
                }
            }
        };
        cell.setUIID("Tab");
        cell.setCellRenderer(true);
        cell.getStyle().setBorder(null);
        cell.getStyle().setMargin(0, 0, 0, 0);
        cell.setIcon(icon);
        updateCellLook(tp, (Component) cell, isSelected);
        if (isSelected) {
            cellStyle.setBgColor(cellStyle.getBgColor());
        }

        cell.setAlignment(Label.CENTER);
        return cell;
    }

    private void updateCellLook(TabbedPane tp, Component c, boolean selected) {
        c.getStyle().setFgColor(tp.getStyle().getFgColor());
        c.getStyle().setBgTransparency(tp.getStyle().getBgTransparency());
        c.getStyle().setFont(tp.getStyle().getFont());
    }

    /**
     * @inheritDoc
     */
    public void drawTabbedPaneContentPane(final TabbedPane tp,
            final Graphics g, final Rectangle rect,
            final Dimension cellsPreferredSize, final int numOfTabs,
            final int selectedTabIndex, final Dimension tabsSize,
            final int cellOffsetX, final int cellOffsetY) {
        int tPBorder = tp.getTabbedPaneBorderWidth();
        if(tPBorder < 1) {
            return;
        }
        int x = rect.getX();
        int y = rect.getY();
        int w = rect.getSize().getWidth();
        int h = rect.getSize().getHeight();
        int listPreferredW = cellsPreferredSize.getWidth();
        int listPreferredH = cellsPreferredSize.getHeight();
        int maxTabWidth = tabsSize.getWidth();
        int maxTabHeight = tabsSize.getHeight();

        g.setColor(tp.getStyle().getBgColor());
        g.fillRect(x, y, w, h, tp.getStyle().getBgTransparency());

        // paint borders for TOP tab placement
        g.setColor(tp.getStyle().getFgColor());
        int tabP = tp.getTabPlacement();

        if (tabP == TabbedPane.TOP || tabP == TabbedPane.BOTTOM) {
            g.fillRect(x, y, tPBorder, h);// west border

            g.fillRect(x + w - tPBorder, y, tPBorder, h);// east border

            int relativeY = y;
            if (tabP == TabbedPane.BOTTOM) {
                relativeY = y + h - tPBorder;
                g.fillRect(x, y, w, tPBorder);// north border

            } else {
                g.fillRect(x, y + h - tPBorder, w, tPBorder);// south border

            }
            
            if(tp.isRTL()){
                if (listPreferredW < w) {
                    g.fillRect(x, relativeY, w - listPreferredW, tPBorder);
                }
                for (int i = 0; i < numOfTabs; i++) {
                    if (i != selectedTabIndex) {
                        g.fillRect((x - cellOffsetX + w - (maxTabWidth * (i+1))), relativeY, maxTabWidth + tPBorder, tPBorder);
                    }
                }

            } else {
                if (listPreferredW < w) {
                    g.fillRect(listPreferredW - tPBorder, relativeY, w - listPreferredW, tPBorder);
                }
                for (int i = 0; i < numOfTabs; i++) {
                    if (i != selectedTabIndex) {
                        g.fillRect((x - cellOffsetX + (maxTabWidth * i)), relativeY, maxTabWidth + tPBorder, tPBorder);
                    }
                }
            }
        } else {//if (tabP == LEFT || tabP == RIGHT) {

            g.fillRect(x, y, w, tPBorder);// north border

            g.fillRect(x, y + h - tPBorder, w, tPBorder);// south border

            int relativeX = x;
            if (!tp.isRTL() && tabP == TabbedPane.RIGHT ||
                    tp.isRTL() && tabP == TabbedPane.LEFT) {
                g.fillRect(x, y, tPBorder, h);// west border

                relativeX = x + w - tPBorder;
            } else {
                g.fillRect(x + w - tPBorder, y, tPBorder, h);// east border

            }

            if (listPreferredH < h) {
                g.fillRect(relativeX, y + listPreferredH - tPBorder, tPBorder, h - listPreferredH + tPBorder);
            }
            for (int i = 0; i < numOfTabs; i++) {
                if (i != selectedTabIndex) {
                    g.fillRect(relativeX, (y - cellOffsetY + (maxTabHeight * i)), tPBorder, maxTabHeight + tPBorder);
                }
            }

        }
    }

    /**
     * Similar to getText() but works properly with password fields
     */
    protected String getTextFieldString(TextArea ta) {
        String text = (String) ta.getText();
        String displayText = "";
        if ((ta.getConstraint() & TextArea.PASSWORD) != 0) {
            // show the last character in a password field
            if (ta.isPendingCommit()) {
                if (text.length() > 0) {
                    for (int j = 0; j < text.length() - 1; j++) {
                        displayText += "*";
                    }
                    displayText += text.charAt(text.length() - 1);
                }
            } else {
                for (int j = 0; j < text.length(); j++) {
                    displayText += "*";
                }
            }
        } else {
            displayText = text;
        }
        return displayText;
    }
    
    /**
     * @inheritDoc
     */
    public void drawTextField(Graphics g, TextArea ta) {
        setFG(g, ta);

        // display ******** if it is a password field
        String displayText = getTextFieldString(ta);

        Style style = ta.getStyle();
        int x = 0;
        int cursorCharPosition = ta.getCursorPosition();        
        Font f = ta.getStyle().getFont();
        int cursorX = 0;
        int xPos = 0;
        
        int align = ta.getAbsoluteAlignment();
        int displayX = 0;

        String inputMode = ta.getInputMode();
        int inputModeWidth = f.stringWidth(inputMode);

        // QWERTY devices don't quite have an input mode hide it also when we have a VK
        if(ta.isQwertyInput() || Display.getInstance().isVirtualKeyboardShowing()) {
            inputMode = "";
            inputModeWidth = 0;
        }

        // there is currently no support for CENTER aligned text fields
        if (align == Component.LEFT) {
            if (cursorCharPosition > 0) {
                xPos = f.stringWidth(displayText.substring(0, cursorCharPosition));
                cursorX = ta.getX() + style.getPadding(ta.isRTL(), Component.LEFT) + xPos;

                // no point in showing the input mode when there is only one input mode...
                if(inputModeWidth > 0 && ta.getInputModeOrder() != null && ta.getInputModeOrder().length == 1) {
                    inputModeWidth = 0;
                }
                if(ta.isEnableInputScroll()) {
                    if (ta.getWidth() > (f.getHeight() * 2) && cursorX >= ta.getWidth()-inputModeWidth - style.getPadding(ta.isRTL(), Component.LEFT)) {
                        while (x + xPos >= ta.getWidth() - inputModeWidth - style.getPadding(ta.isRTL(), Component.LEFT) * 2) {
                            x--;
                        }
                    }
                }
            }
            displayX = ta.getX() + x + style.getPadding(ta.isRTL(), Component.LEFT);
        } else {
    		x = 0;
    		cursorX = getTextFieldCursorX(ta);
    		int baseX = ta.getX() + style.getPadding(false, Component.LEFT) + inputModeWidth;
    		int endX = ta.getX() + ta.getWidth() - style.getPadding(false, Component.RIGHT);

    		if(cursorX<baseX) {
    			x = baseX - cursorX;
    		} else {
                if (cursorX > endX) {
        			x = endX - cursorX;
        		}
            }

    		displayX = ta.getX() + ta.getWidth() - style.getPadding(false, Component.RIGHT) - f.stringWidth(displayText)+x;
        }

        g.drawString(displayText, displayX, ta.getY() + style.getPadding(false, Component.TOP));

        // no point in showing the input mode when there is only one input mode...
        if(inputModeWidth > 0 && ta.getInputModeOrder() != null && ta.getInputModeOrder().length > 1) {
            
            if (ta.handlesInput() && ta.getWidth() / 2 > inputModeWidth) {
            	
                int drawXPos = ta.getX() + style.getPadding(ta.isRTL(), Component.LEFT);
                if((!ta.isRTL() && ta.getAlignment() == Component.LEFT) ||
                    (ta.isRTL() && ta.getAlignment() == Component.RIGHT)) {
                    drawXPos = drawXPos + ta.getWidth() - inputModeWidth - style.getPadding(false, Component.RIGHT) - style.getPadding(false, Component.LEFT);
                } 
                g.setColor(style.getFgColor());
                g.fillRect(drawXPos, ta.getY() + style.getPadding(false, Component.TOP), inputModeWidth,
                        ta.getHeight(), (byte) 140);
                g.setColor(style.getBgColor());
                g.drawString(inputMode, drawXPos, ta.getY() + style.getPadding(false, Component.TOP));
            }
        }
    }

    /**
     * Returns true if the given character is an RTL character or a space
     * character
     *
     * @param c character to test
     * @return true if bidi is active and this is a
     */
	private boolean isRTLOrWhitespace(char c) {
        return (Display.getInstance().isRTL(c)) || c == ' ';
    }

    /**
     * Calculates the position of the text field cursor within the string
     */
    private int getTextFieldCursorX(TextArea ta) {
        Style style = ta.getStyle();
        Font f = style.getFont();

        // display ******** if it is a password field
        String displayText = getTextFieldString(ta);
        String inputMode = ta.getInputMode();
        int inputModeWidth = f.stringWidth(inputMode);
        // QWERTY devices don't quite have an input mode hide it also when we have a VK
        if(ta.isQwertyInput() || Display.getInstance().isVirtualKeyboardShowing()) {
            inputMode = "";
            inputModeWidth = 0;
        }

        int xPos = 0;
        int cursorCharPosition = ta.getCursorPosition();
        int cursorX=0;
        int x = 0;

        if (ta.getAbsoluteAlignment() == Component.RIGHT) {
        	if (Display.getInstance().isBidiAlgorithm()) {
                //char[] dest = displayText.toCharArray();
                cursorCharPosition = Display.getInstance().getCharLocation(displayText, cursorCharPosition-1);

                if (cursorCharPosition==-1) {
                    xPos = f.stringWidth(displayText);
                } else {
                    displayText = Display.getInstance().convertBidiLogicalToVisual(displayText);
                    if (!isRTLOrWhitespace((displayText.charAt(cursorCharPosition)))) {
                        cursorCharPosition++;
                    }
                    xPos = f.stringWidth(displayText.substring(0, cursorCharPosition));
                } 
        	}
        	int displayX = ta.getX() + ta.getWidth() - style.getPadding(ta.isRTL(), Component.LEFT) - f.stringWidth(displayText);
        	cursorX = displayX + xPos;
        	x=0;
        } else {
            if (cursorCharPosition > 0) {
                xPos = f.stringWidth(displayText.substring(0, cursorCharPosition));
            }
            cursorX = ta.getX() + style.getPadding(ta.isRTL(), Component.LEFT) + xPos;

            if (ta.getWidth() > (f.getHeight() * 2) && cursorX >= ta.getWidth() - inputModeWidth  -style.getPadding(ta.isRTL(), Component.LEFT)) {
                while (x + xPos >= ta.getWidth() - inputModeWidth - style.getPadding(false, Component.LEFT) - style.getPadding(false, Component.RIGHT)) {
                    x--;
                }
            }
        }

        return cursorX+x;

    }

    /**
     * @inheritDoc
     */
    public Dimension getTextFieldPreferredSize(TextArea ta) {
        return getTextAreaSize(ta, true);
    }

    /**
     * @inheritDoc
     */
     public void drawTextFieldCursor(Graphics g, TextArea ta) {
         Style style = ta.getStyle();
         Font f = style.getFont();


    	int cursorY = ta.getY() + style.getPadding(false, Component.TOP);
    	int cursorX = getTextFieldCursorX(ta);

        int align=ta.getAbsoluteAlignment();
		int x=0;
        if (align==Component.RIGHT) {
            String inputMode = ta.getInputMode();
            int inputModeWidth = f.stringWidth(inputMode);

    		int baseX=ta.getX()+style.getPadding(false, Component.LEFT)+inputModeWidth;
    		if (cursorX<baseX) {
    			x=baseX-cursorX;
    		}
        }

        int oldColor = g.getColor();
        g.setColor(style.getFgColor());
        g.drawLine(cursorX + x, cursorY, cursorX + x, cursorY + f.getHeight());
        g.setColor(oldColor);
    }

    /**
     * @inheritDoc
     */
    public void focusGained(Component cmp) {
        if(cmp instanceof Label) {
            Label l = (Label) cmp;
            if (l.shouldTickerStart()) {
                ((Label) cmp).startTicker(getTickerSpeed(), true);
            }
        }
    }

    /**
     * @inheritDoc
     */
    public void focusLost(Component cmp) {
        if(cmp instanceof Label) {
            Label l = (Label) cmp;
            if (l.isTickerRunning()) {
                l.stopTicker();
            }
        }
    }
}
