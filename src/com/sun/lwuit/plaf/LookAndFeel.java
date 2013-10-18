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
import com.sun.lwuit.*;
import com.sun.lwuit.animations.Transition;
import com.sun.lwuit.geom.Rectangle;
import com.sun.lwuit.list.*;

/**
 * Allows a UI developer to completely customize the look of the application by
 * overriding drawing/sizing methods appropriately.
 *
 * @author Chen Fishbein
 */
public abstract class LookAndFeel {
    private Component verticalScroll;
    private Component horizontalScroll;
    private Component verticalScrollThumb;
    private Component horizontalScrollThumb;

	/**
	 * Right-To-Left. Default false.
	 */
	private boolean rtl;

    private long tickerSpeed = 50;
    /**
     * Tint color is set when a form is partially covered be it by a menu or by a 
     * dialog. A look and feel can override this default value.
     */
    private int defaultFormTintColor = 0x77000000;
    /**
     * This color is used to paint disable mode.
     */
    private int disableColor = 0xcccccc;
    /**
     * This member allows us to define a default animation that will draw the transition for
     * entering a form
     */
    private Transition defaultFormTransitionIn;
    /**
     * This member allows us to define a default animation that will draw the transition for
     * exiting a form
     */
    private Transition defaultFormTransitionOut;
    /**
     * This member allows us to define a default animation that will draw the transition for
     * entering a menu
     */
    private Transition defaultMenuTransitionIn;
    /**
     * This member allows us to define a default animation that will draw the transition for
     * exiting a menu
     */
    private Transition defaultMenuTransitionOut;
    /**
     * This member allows us to define a default animation that will draw the transition for
     * entering a dialog
     */
    private Transition defaultDialogTransitionIn;
    /**
     * This member allows us to define a default animation that will draw the transition for
     * exiting a form
     */
    private Transition defaultDialogTransitionOut;
    /**
     * Indicates whether lists and containers should have smooth scrolling by default
     */
    private boolean defaultSmoothScrolling = true;
    /**
     * Indicates the default speed for smooth scrolling
     */
    private int defaultSmoothScrollingSpeed = 150;
    /**
     * Indicates whether softbuttons should be reversed from their default orientation
     */
    private boolean reverseSoftButtons;
    /**
     * This renderer is assigned to all Forms Menu's by default.
     */
    private ListCellRenderer menuRenderer;
    private Image[] menuIcons = new Image[3];

    /**
     * Indicates whether the menu UI should target a touch based device or a
     * standard cell phone
     */
    private boolean touchMenus;

    /**
     * Allows defining a tactile touch device that vibrates when the user presses a component
     * that should respond with tactile feedback on a touch device (e.g. vibrate).
     * Setting this to 0 disables tactile feedback completely
     */
    private int tactileTouchDuration = 0;


    /**
     * Every component binds itself to the look and feel thus allowing the look 
     * and feel to customize the component.  Binding occurs at the end of the
     * constructor when the component is in a valid state and ready to be used.
     * Notice that a component might be bound twice or more and it is the 
     * responsibility of the LookAndFeel to protect against that.
     * 
     * @param cmp component instance that may be customized by the look and feel
     */
    public void bind(Component cmp) {
    }

    /**
     * Invoked when a look and feel is removed, allows a look and feel to release 
     * resources related to binding components.
     * 
     * @see #bind(Component)
     */
    public void uninstall() {
    }

    /**
     * Invoked for drawing a button widget
     * 
     * @param g graphics context
     * @param b component to draw
     */
    public abstract void drawButton(Graphics g, Button b);

    /**
     * Invoked for drawing a checkbox widget
     * 
     * @param g graphics context
     * @param cb component to draw
     */
    public abstract void drawCheckBox(Graphics g, Button cb);

    /**
     * Invoked for drawing a combo box widget
     * 
     * @param g graphics context
     * @param cb component to draw
     */
    public abstract void drawComboBox(Graphics g, List cb);

    /**
     * Invoked for drawing a label widget
     * 
     * @param g graphics context
     * @param l component to draw
     */
    public abstract void drawLabel(Graphics g, Label l);

    /**
     * Invoked for drawing a list widget
     * 
     * @param g graphics context
     * @param l component to draw
     */
    public abstract void drawList(Graphics g, List l);

    /**
     * Invoked for drawing the radio button widget
     * 
     * @param g graphics context
     * @param rb component to draw
     */
    public abstract void drawRadioButton(Graphics g, Button rb);

    /**
     * Draw the given text area
     * 
     * @param g graphics context
     * @param ta component to draw
     */
    public abstract void drawTextArea(Graphics g, TextArea ta);

    /**
     * Draws the text field without its cursor which is drawn in a separate method
     * input mode indication can also be drawn using this method.
     * 
     * @param g graphics context
     * @param ta component to draw
     */
    public abstract void drawTextField(Graphics g, TextArea ta);

    /**
     * Draws the cursor of the text field, blinking is handled simply by avoiding
     * a call to this method.
     * 
     * @param g graphics context
     * @param ta component to draw
     */
    public abstract void drawTextFieldCursor(Graphics g, TextArea ta);

    /**
     * Invoked for drawing the Tab Pane widget
     * 
     * @param g graphics context
     * @param tp component to draw
     */
    public abstract void drawTabbedPane(Graphics g, TabbedPane tp);

    /**
     * Calculate the preferred size of the component
     * 
     * @param b component whose size should be calculated
     * @return the preferred size for the button
     */
    public abstract Dimension getButtonPreferredSize(Button b);

    /**
     * Calculate the preferred size of the component
     * 
     * @param cb component whose size should be calculated
     * @return the preferred size for the component
     */
    public abstract Dimension getCheckBoxPreferredSize(Button cb);

    /**
     * Calculate the preferred size of the component
     * 
     * @param l component whose size should be calculated
     * @return the preferred size for the component
     */
    public abstract Dimension getLabelPreferredSize(Label l);

    /**
     * Calculate the preferred size of the component
     * 
     * @param l component whose size should be calculated
     * @return the preferred size for the component
     */
    public abstract Dimension getListPreferredSize(List l);

    /**
     * Calculate the preferred size of the component
     * 
     * @param rb component whose size should be calculated
     * @return the preferred size for the component
     */
    public abstract Dimension getRadioButtonPreferredSize(Button rb);

    //public abstract Dimension getSpinnerPreferredSize(Spinner sp);
    /**
     * Calculate the preferred size of the component
     * 
     * @param ta component whose size should be calculated
     * @param pref indicates whether preferred or scroll size should be returned
     * @return the preferred size for the component
     */
    public abstract Dimension getTextAreaSize(TextArea ta, boolean pref);

    /**
     * Calculate the preferred size of the component
     * 
     * @param ta component whose size should be calculated
     * @return the preferred size for the component
     */
    public abstract Dimension getTextFieldPreferredSize(TextArea ta);

    /**
     * Calculate the preferred size of the component
     * 
     * @param box component whose size should be calculated
     * @return the preferred size for the component
     */
    public abstract Dimension getComboBoxPreferredSize(List box);

    /**
     * Draws a vertical scroll bar in the given component
     * 
     * @param g graphics context
     * @param c component to draw on
     * @param offsetRatio ratio of the scroll bar from 0 to 1
     * @param blockSizeRatio block size for the scroll from 0 to 1
     */
    public void drawVerticalScroll(Graphics g, Component c, float offsetRatio, float blockSizeRatio) {
        if(verticalScroll == null) {
            initScroll();
        }
        int x = c.getX();
        if(!c.isRTL()) {
            x += c.getWidth() - getVerticalScrollWidth();
        }
        int y = c.getY();
        int height = c.getHeight();
        int width = getVerticalScrollWidth();
        drawScroll(g, c, offsetRatio, blockSizeRatio, true, x, y, width, height, verticalScroll, verticalScrollThumb);
    }

    /**
     * Draws a horizontal scroll bar in the given component
     * 
     * @param g graphics context
     * @param c component to draw on
     * @param offsetRatio ratio of the scroll bar from 0 to 1
     * @param blockSizeRatio block size for the scroll from 0 to 1
     */
    public void drawHorizontalScroll(Graphics g, Component c, float offsetRatio, float blockSizeRatio) {
        if(horizontalScroll == null) {
            initScroll();
        }
        int x = c.getX();
        int y = c.getY() + c.getHeight() - getHorizontalScrollHeight();
        int width = c.getWidth();
        int height = getHorizontalScrollHeight();
        drawScroll(g, c, offsetRatio, blockSizeRatio, false, x, y, width, height, horizontalScroll, horizontalScrollThumb);
    }

    private void drawScroll(Graphics g, Component c, float offsetRatio,
            float blockSizeRatio, boolean isVertical, int x, int y, int width, int height,
            Component scroll, Component scrollThumb) {
        Style scrollStyle = scroll.getUnselectedStyle();
        Style scrollThumbStyle = scrollThumb.getUnselectedStyle();

        // take margin into consideration when positioning the scroll
        int marginLeft = scrollStyle.getMargin(c.isRTL(), Component.LEFT);
        int marginTop = scrollStyle.getMargin(false, Component.TOP);
        x += marginLeft;
        width -= (marginLeft + scrollStyle.getMargin(c.isRTL(), Component.RIGHT));
        y += marginTop;
        height -= (marginTop + scrollStyle.getMargin(false, Component.BOTTOM));

        scroll.setX(x);
        scroll.setY(y);
        scroll.setWidth(width);
        scroll.setHeight(height);

        int cx = g.getClipX();
        int cy = g.getClipY();
        int cw = g.getClipWidth();
        int ch = g.getClipHeight();

        scroll.paintComponent(g);

        marginLeft = scrollThumbStyle.getMargin(c.isRTL(), Component.LEFT);
        marginTop = scrollThumbStyle.getMargin(false, Component.TOP);
        x += marginLeft;
        width -= (marginLeft + scrollThumbStyle.getMargin(c.isRTL(), Component.RIGHT));
        y += marginTop;
        height -= (marginTop + scrollThumbStyle.getMargin(false, Component.BOTTOM));

        int offset, blockSize;

        if (isVertical) {
            blockSize = (int) (c.getHeight() * blockSizeRatio) + 2;
            offset = (int) ((c.getHeight()) * offsetRatio);
        } else {
            blockSize = (int) (c.getWidth() * blockSizeRatio) + 2;
            offset = (int) ((c.getWidth()) * offsetRatio);
        }
        
        if (isVertical) {
            scrollThumb.setX(x);
            scrollThumb.setY(y + offset);
            scrollThumb.setWidth(width);
            scrollThumb.setHeight(blockSize);
        } else {
            scrollThumb.setX(x + offset);
            scrollThumb.setY(y);
            scrollThumb.setWidth(blockSize);
            scrollThumb.setHeight(height);
        }
        
        g.setClip(cx, cy, cw, ch);
        scrollThumb.paintComponent(g);
        g.setClip(cx, cy, cw, ch);
    }


    /**
     * Sets the foreground color and font for a generic component, reuse-able by most component
     * drawing code
     * 
     * @param g graphics context
     * @param c component from which fg styles should be set
     */
    public void setFG(Graphics g, Component c) {
        Style s = c.getStyle();
        g.setFont(s.getFont());
        if (c.isEnabled()) {
            g.setColor(s.getFgColor());
        } else {
            g.setColor(disableColor);
        }
    }

    /**
     * Returns the default width of a vertical scroll bar
     * 
     * @return default width of a vertical scroll bar
     */
    public int getVerticalScrollWidth() {
        if(verticalScroll == null) {
            initScroll();
        }
        Style scrollStyle = verticalScroll.getStyle();

        // bidi doesn't matter for width calculations
        return scrollStyle.getMargin(false, Component.LEFT) + scrollStyle.getMargin(false, Component.RIGHT) +
                scrollStyle.getPadding(false, Component.LEFT) + scrollStyle.getPadding(false, Component.RIGHT);
    }

    /**
     * Returns the default height of a horizontal scroll bar
     * 
     * @return default height of a horizontal scroll bar
     */
    public int getHorizontalScrollHeight() {
        if(horizontalScroll == null) {
            initScroll();
        }
        Style scrollStyle = horizontalScroll.getStyle();

        // bidi doesn't matter for height calculations
        return scrollStyle.getMargin(false, Component.TOP) + scrollStyle.getMargin(false, Component.BOTTOM) +
                scrollStyle.getPadding(false, Component.TOP) + scrollStyle.getPadding(false, Component.BOTTOM);
    }

    /**
     * Draws generic component border
     */
    void drawBorder(Graphics g, Component c, int color, int borderWidth) {
        drawBorder(g, c, color, color, borderWidth);
    }

    /**
     * Draws generic component border
     */
    void drawBorder(Graphics g, Component c, int topAndRightColor, int bottomAndLeftColor, int borderWidth) {
        g.setColor(topAndRightColor);     //Text Component upper border color

        g.fillRect(c.getX(), c.getY(), c.getWidth(), borderWidth);
        g.fillRect(c.getX(), c.getY(), borderWidth, c.getHeight());
        g.setColor(bottomAndLeftColor);     //Text Component lower border color

        g.fillRect(c.getX(), c.getY() + c.getHeight() - borderWidth, c.getWidth(), borderWidth);
        g.fillRect(c.getX() + c.getWidth() - borderWidth, c.getY(), borderWidth, c.getHeight());
    }

    /**
     * Draws and return the TabbedPane cell component (renderer)
     * according to each tab orientation, the borders are getting draws
     * 
     * @param tp the TabbedPane
     * @param text the cell text
     * @param icon the cell icon image
     * @param isSelected is the cell is the selected one
     * @param cellHasFocus is the cell has focus
     * @param cellStyle the cell Style object
     * @param cellSelectedStyle the selected style for the cell object
     * @param tabbedPaneStyle the TabbedPane Style object
     * @param cellOffsetX the offset when the cell is on TOP or BOTTOM orientation
     * @param cellOffsetY the offset when the cell is on LEFT or RIGHT orientation
     * @param cellsPreferredSize the total cells PreferredSize
     * @param contentPaneSize the contentPaneSize
     * @return A TabbedPane cell component
     */
    public abstract Component getTabbedPaneCell(final TabbedPane tp,
            final String text, final Image icon, final boolean isSelected,
            final boolean cellHasFocus, final Style cellStyle, Style cellSelectedStyle,
            final Style tabbedPaneStyle, final int cellOffsetX,
            final int cellOffsetY, final Dimension cellsPreferredSize,
            final Dimension contentPaneSize);

    /**
     * Draws and return the TabbedPane contentpane painter
     * 
     * @param tp the TabbedPane
     * @param g the content pane graphics
     * @param rect the content pane painting rectangle area
     * @param cellsPreferredSize the total cells PreferredSize
     * @param numOfTabs number of tabs
     * @param selectedTabIndex the selected tab index
     * @param tabsSize the tabs size
     * @param cellOffsetX the offset when the cell is on TOP or BOTTOM orientation
     * @param cellOffsetY the offset when the cell is on LEFT or RIGHT orientation
     */
    public abstract void drawTabbedPaneContentPane(final TabbedPane tp,
            final Graphics g, final Rectangle rect,
            final Dimension cellsPreferredSize, final int numOfTabs,
            final int selectedTabIndex, final Dimension tabsSize,
            final int cellOffsetX, final int cellOffsetY);

    /**
     * Allows us to define a default animation that will draw the transition for
     * entering a form
     * 
     * @return default transition
     */
    public Transition getDefaultFormTransitionIn() {
        return defaultFormTransitionIn;
    }

    /**
     * Allows us to define a default animation that will draw the transition for
     * entering a form
     * 
     * @param defaultFormTransitionIn the default transition
     */
    public void setDefaultFormTransitionIn(Transition defaultFormTransitionIn) {
        this.defaultFormTransitionIn = defaultFormTransitionIn;
    }

    /**
     * Allows us to define a default animation that will draw the transition for
     * exiting a form
     * 
     * @return default transition
     */
    public Transition getDefaultFormTransitionOut() {
        return defaultFormTransitionOut;
    }

    /**
     * Allows us to define a default animation that will draw the transition for
     * exiting a form
     * 
     * @param defaultFormTransitionOut the default transition
     */
    public void setDefaultFormTransitionOut(Transition defaultFormTransitionOut) {
        this.defaultFormTransitionOut = defaultFormTransitionOut;
    }

    /**
     * Allows us to define a default animation that will draw the transition for
     * entering a Menu
     * 
     * @return default transition
     */
    public Transition getDefaultMenuTransitionIn() {
        return defaultMenuTransitionIn;
    }

    /**
     * Allows us to define a default animation that will draw the transition for
     * entering a Menu
     * 
     * @param defaultMenuTransitionIn the default transition
     */
    public void setDefaultMenuTransitionIn(Transition defaultMenuTransitionIn) {
        this.defaultMenuTransitionIn = defaultMenuTransitionIn;
    }

    /**
     * Allows us to define a default animation that will draw the transition for
     * exiting a Menu
     * 
     * @return default transition
     */
    public Transition getDefaultMenuTransitionOut() {
        return defaultMenuTransitionOut;
    }

    /**
     * Allows us to define a default animation that will draw the transition for
     * exiting a Menu
     * 
     * @param defaultMenuTransitionOut the default transition
     */
    public void setDefaultMenuTransitionOut(Transition defaultMenuTransitionOut) {
        this.defaultMenuTransitionOut = defaultMenuTransitionOut;
    }

    /**
     * Allows us to define a default animation that will draw the transition for
     * entering a dialog
     * 
     * @return default transition
     */
    public Transition getDefaultDialogTransitionIn() {
        return defaultDialogTransitionIn;
    }

    /**
     * Allows us to define a default animation that will draw the transition for
     * entering a dialog
     * 
     * @param defaultDialogTransitionIn the default transition
     */
    public void setDefaultDialogTransitionIn(Transition defaultDialogTransitionIn) {
        this.defaultDialogTransitionIn = defaultDialogTransitionIn;
    }

    /**
     * Allows us to define a default animation that will draw the transition for
     * exiting a dialog
     * 
     * @return default transition
     */
    public Transition getDefaultDialogTransitionOut() {
        return defaultDialogTransitionOut;
    }

    /**
     * Allows us to define a default animation that will draw the transition for
     * exiting a dialog
     * 
     * @param defaultDialogTransitionOut the default transition
     */
    public void setDefaultDialogTransitionOut(Transition defaultDialogTransitionOut) {
        this.defaultDialogTransitionOut = defaultDialogTransitionOut;
    }

    /**
     * Tint color is set when a form is partially covered be it by a menu or by a 
     * dialog. A look and feel can override this default value.
     * 
     * @return default tint color
     */
    public int getDefaultFormTintColor() {
        return defaultFormTintColor;
    }

    /**
     * Tint color is set when a form is partially covered be it by a menu or by a 
     * dialog. A look and feel can override this default value.
     * 
     * @param defaultFormTintColor the default tint color
     */
    public void setDefaultFormTintColor(int defaultFormTintColor) {
        this.defaultFormTintColor = defaultFormTintColor;
    }

    /**
     * This color is used to paint disable mode text color.
    
     * @return the color value
     */
    public int getDisableColor() {
        return disableColor;
    }

    /**
     * Simple setter to disable color
     * 
     * @param disableColor the disable color value
     */
    public void setDisableColor(int disableColor) {
        this.disableColor = disableColor;
    }

    /**
     * Indicates whether lists and containers should have smooth scrolling by default
     * 
     * @return true if smooth scrolling should be on by default
     */
    public boolean isDefaultSmoothScrolling() {
        return defaultSmoothScrolling;
    }

    /**
     * Indicates whether lists and containers should have smooth scrolling by default
     * 
     * @param defaultSmoothScrolling  true if smooth scrolling should be on by default
     */
    public void setDefaultSmoothScrolling(boolean defaultSmoothScrolling) {
        this.defaultSmoothScrolling = defaultSmoothScrolling;
    }

    /**
     * Indicates the default speed for smooth scrolling
     * 
     * @return speed for smooth scrollin
     */
    public int getDefaultSmoothScrollingSpeed() {
        return defaultSmoothScrollingSpeed;
    }

    /**
     * Indicates the default speed for smooth scrolling
     * 
     * @param defaultSmoothScrollingSpeed speed for smooth scrollin
     */
    public void setDefaultSmoothScrollingSpeed(int defaultSmoothScrollingSpeed) {
        this.defaultSmoothScrollingSpeed = defaultSmoothScrollingSpeed;
    }

    /**
     * Indicates whether softbuttons should be reversed from their default orientation
     * 
     * @return true if softbuttons should be reversed
     */
    public boolean isReverseSoftButtons() {
        return reverseSoftButtons;
    }

    /**
     * Indicates whether softbuttons should be reversed from their default orientation
     * 
     * @param reverseSoftButtons  true if softbuttons should be reversed
     */
    public void setReverseSoftButtons(boolean reverseSoftButtons) {
        this.reverseSoftButtons = reverseSoftButtons;
    }

    /**
     * Returns the Menu default renderer
     * 
     * @return default renderer for the menu
     */
    public ListCellRenderer getMenuRenderer() {
        return menuRenderer;
    }

    /**
     * Sets the Menu default renderer
     * 
     * @param menuRenderer default renderer for the menu
     */
    public void setMenuRenderer(ListCellRenderer menuRenderer) {
        this.menuRenderer = menuRenderer;
    }

    /**
     * Sets globally the Menu icons
     * 
     * @param select select icon
     * @param cancel cancel icon
     * @param menu menu icon
     */
    public void setMenuIcons(Image select, Image cancel, Image menu) {
        menuIcons[0] = select;
        menuIcons[1] = cancel;
        menuIcons[2] = menu;

    }

    /**
     * Simple getter for the menu icons
     * 
     * @return an Image array at size of 3, where the first is the select image
     * the second is the cancel image and the last is the menu image.
     */
    public Image[] getMenuIcons() {
        return menuIcons;
    }

    /**
     * Gets the ticker speed
     *
     * @return ticker speed in milliseconds
     */
    public long getTickerSpeed() {
        return tickerSpeed;
    }

    /**
     * Sets the ticker speed
     *
     * @param tickerSpeed the speed in milliseconds
     */
    public void setTickerSpeed(long tickerSpeed) {
        this.tickerSpeed = tickerSpeed;
    }

    private void initScroll() {
        verticalScroll = new Label();
        verticalScroll.setUIID("Scroll");
        horizontalScroll = new Label();
        horizontalScroll.setUIID("HorizontalScroll");
        verticalScrollThumb = new Label();;
        verticalScrollThumb.setUIID("ScrollThumb");
        horizontalScrollThumb = new Label();
        horizontalScrollThumb.setUIID("HorizontalScrollThumb");
    }

    /**
     * This method is a callback to the LookAndFeel when a theme is being 
     * changed in the UIManager
     */
    public void refreshTheme() {
        initScroll();
        if(menuRenderer != null) {
            if(menuRenderer instanceof Component) {
                ((Component)menuRenderer).refreshTheme();
            }
        }
    }

    /**
     * Indicates whether the menu UI should target a touch based device or a
     * standard cell phone
     *
     * @return true for touch menus
     */
    public boolean isTouchMenus() {
        return touchMenus;
    }

    /**
     * Indicates whether the menu UI should target a touch based device or a
     * standard cell phone
     *
     * @param touchMenus true to enable touch menus false to disable
     */
    public void setTouchMenus(boolean touchMenus) {
        this.touchMenus = touchMenus;
    }

	/**
	 * Sets this LookAndFeel to operate in right-to-left mode.
	 *
	 * @param rtl - true if right-to-left, false if left-to-right
	 */
	public void setRTL(boolean rtl) {
		this.rtl = rtl;
        if(rtl) {
            Display.getInstance().setBidiAlgorithm(true);
        }
	}

	/**
	 * Use this to check if the LookAndFeel is in RTL mode
	 *
	 * @return true if the LookAndFeel is in right-to-left mode, false otherwise
	 */
	public boolean isRTL() {
		return rtl;
	}

    /**
     * Allows defining a tactile touch device that vibrates when the user presses a component
     * that should respond with tactile feedback on a touch device (e.g. vibrate).
     * Setting this to 0 disables tactile feedback completely

     * @return the tactileTouchDuration
     */
    public int getTactileTouchDuration() {
        return tactileTouchDuration;
    }

    /**
     * Allows defining a tactile touch device that vibrates when the user presses a component
     * that should respond with tactile feedback on a touch device (e.g. vibrate).
     * Setting this to 0 disables tactile feedback completely
     *
     * @param tactileTouchDuration the duration of vibration
     */
    public void setTactileTouchDuration(int tactileTouchDuration) {
        this.tactileTouchDuration = tactileTouchDuration;
    }
}
