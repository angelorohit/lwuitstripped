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

import com.sun.lwuit.util.EventDispatcher;
import com.sun.lwuit.geom.Dimension;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.plaf.Border;
import com.sun.lwuit.plaf.Style;
import com.sun.lwuit.plaf.UIManager;


/**
 * Button is the base class for several UI widgets allowing clickability.
 * It has 3 states: rollover, pressed and the default state it 
 * can also have ActionListeners that react when the Button is clicked.
 * 
 * @author Chen Fishbein
 */
public class Button extends Label {
    /**
     * Indicates the rollover state of a button which is equivalent to focused for
     * most uses
     */
    public static final int STATE_ROLLOVER = 0;
    
    /**
     * Indicates the pressed state of a button 
     */
    public static final int STATE_PRESSED = 1;
    
    /**
     * Indicates the default state of a button which is neither pressed nor focused
     */
    public static final int STATE_DEFAULT = 2;
    
    private EventDispatcher dispatcher = new EventDispatcher();
    
    private int state = STATE_DEFAULT;
    
    private Image pressedIcon;
    
    private Image rolloverIcon;
  
    private Command cmd;

    private Style pressedStyle;

    
    /** 
     * Constructs a button with an empty string for its text.
     */
    public Button() {
        this("");
    }
    
    /**
     * Constructs a button with the specified text.
     * 
     * @param text label appearing on the button
     */
    public Button(String text) {
        this(text, null);
    }
    
    /**
     * Allows binding a command to a button for ease of use
     * 
     * @param cmd command whose text would be used for the button and would recive action events
     * from the button
     */
    public Button(Command cmd) {
        this(cmd.getCommandName(), cmd.getIcon());
        addActionListener(cmd);
        this.cmd = cmd;
    }
    
    /**
     * Constructs a button with the specified image.
     * 
     * @param icon appearing on the button
     */
    public Button(Image icon) {
        this("", icon);
    }
    
    /**
     * Constructor a button with text and image
     * 
     * @param text label appearing on the button
     * @param icon image appearing on the button
     */
    public Button(String text, Image icon) {
        super(text);
        setUIID("Button");
        setFocusable(true);
        setIcon(icon);
        this.pressedIcon = icon;
        this.rolloverIcon = icon;
    }

    /**
     * @inheritDoc
     */
    void focusGainedInternal() {
        super.focusGainedInternal();
        state = STATE_ROLLOVER;
    }
    
    /**
     * @inheritDoc
     */
    void focusLostInternal() {
        super.focusLostInternal();
        state = STATE_DEFAULT;
    }
    
    /**
     * Returns the button state
     * 
     * @return One of STATE_ROLLOVER, STATE_DEAFULT, STATE_PRESSED
     */
    public int getState() {
        return state;
    }
    
    /**
     * Indicates the icon that is displayed on the button when the button is in 
     * pressed state
     * 
     * @return icon used
     * @see #STATE_PRESSED
     */
    public Image getPressedIcon() {
        return pressedIcon;
    }
    
    /**
     * Indicates the icon that is displayed on the button when the button is in 
     * rolled over state
     * 
     * @return icon used
     * @see #STATE_ROLLOVER
     */
    public Image getRolloverIcon() {
        return rolloverIcon;
    }
    
    /**
     * Indicates the icon that is displayed on the button when the button is in 
     * rolled over state
     * 
     * @param rolloverIcon icon to use
     * @see #STATE_ROLLOVER
     */
    public void setRolloverIcon(Image rolloverIcon) {
        this.rolloverIcon = rolloverIcon;
        setShouldCalcPreferredSize(true);
        checkAnimation();
        repaint();        
    }
    
    /**
     * Indicates the icon that is displayed on the button when the button is in 
     * pressed state
     * 
     * @param pressedIcon icon used
     * @see #STATE_PRESSED
     */
    public void setPressedIcon(Image pressedIcon) {
        this.pressedIcon = pressedIcon;
        setShouldCalcPreferredSize(true);
        checkAnimation();
        repaint();
    }

    void checkAnimation() {
        super.checkAnimation();
        if((pressedIcon != null && pressedIcon.isAnimation()) || 
            (rolloverIcon != null && rolloverIcon.isAnimation())) {
            Form parent = getComponentForm();
            if(parent != null) {
                // animations are always running so the internal animation isn't
                // good enough. We never want to stop this sort of animation
                parent.registerAnimated(this);
            }
        }
    }
    
    /**
     * Adds a listener to the button which will cause an event to dispatch on click
     * 
     * @param l implementation of the action listener interface
     */
    public void addActionListener(ActionListener l){
        dispatcher.addListener(l);
    }
    
    /**
     * Removes the given action listener from the button
     * 
     * @param l implementation of the action listener interface
     */
    public void removeActionListener(ActionListener l){
        dispatcher.removeListener(l);
    }

    /**
     * Returns the icon for the button based on its current state
     *
     * @return the button icon based on its current state
     */
    public Image getIconFromState() {
        Image icon = null;
        switch (getState()) {
            case Button.STATE_DEFAULT:
                icon = getIcon();
                break;
            case Button.STATE_PRESSED:
                icon = getPressedIcon();
                if (icon == null) {
                    icon = getIcon();
                }
                break;
            case Button.STATE_ROLLOVER:
                icon = getRolloverIcon();
                if (icon == null) {
                    icon = getIcon();
                }
                break;
        }
        return icon;
    }

    /**
     * @inheritDoc
     */
    void fireActionEvent(){
        super.fireActionEvent();
        if(cmd != null) {
            ActionEvent ev = new ActionEvent(cmd);
            dispatcher.fireActionEvent(ev);
            if(!ev.isConsumed()) {
                Form f = getComponentForm();
                if(f != null) {
                    f.actionCommandImpl(cmd);
                }
            }
        } else {
            dispatcher.fireActionEvent(new ActionEvent(this));
        }
    }
    
    /**
     * Invoked to change the state of the button to the pressed state
     */
    void pressed(){
        if(isEnabled()) {
            state=STATE_PRESSED;
            repaint();
        }
    }
    
    /**
     * Invoked to change the state of the button to the released state
     */
    void released(){
        if(isEnabled()) {
            state=STATE_ROLLOVER;
            repaint();
            fireActionEvent();
        }
    }
    
    /**
     * @inheritDoc
     */
    public void keyPressed(int keyCode) {
        if (Display.getInstance().getGameAction(keyCode) == Display.GAME_FIRE){
            pressed();
        }
    }
    
    /**
     * @inheritDoc
     */
    public void keyReleased(int keyCode) {
        if (Display.getInstance().getGameAction(keyCode) == Display.GAME_FIRE){
            released();
        }
    }
    
    /**
     * @inheritDoc
     */
    public void keyRepeated(int keyCode) {
    }
    
    /**
     * @inheritDoc
     */
    protected void fireClicked() {
        pressed();
        released();
    }
    
    /**
     * @inheritDoc
     */
    protected boolean isSelectableInteraction() {
        return true;
    }

    /**
     * @inheritDoc
     */
    public void pointerHover(int[] x, int[] y) {
        requestFocus();
    }
    
    /**
     * @inheritDoc
     */
    public void pointerHoverReleased(int[] x, int[] y) {
        requestFocus();
    }

    /**
     * @inheritDoc
     */
    public void pointerPressed(int x, int y) {
        clearDrag();
        setDragActivated(false);
        pressed();
    }
    
    /**
     * @inheritDoc
     */
    public void pointerReleased(int x, int y) {
        released();
    }
    
    /**
     * @inheritDoc
     */
    public void paint(Graphics g) {
        UIManager.getInstance().getLookAndFeel().drawButton(g, this);
    }
    
    /**
     * @inheritDoc
     */
    protected Dimension calcPreferredSize(){
        return UIManager.getInstance().getLookAndFeel().getButtonPreferredSize(this);
    }
    
    /**
     * @inheritDoc
     */
    protected Border getBorder() {
        return getStyle().getBorder();
    }

    /**
     * Returns the Component Style for the pressed state allowing us to manipulate
     * the look of the component when it is pressed
     *
     * @return the component Style object
     */
    public Style getPressedStyle() {
        if (pressedStyle == null) {
            pressedStyle = UIManager.getInstance().getComponentCustomStyle(getUIID(), "press");
            pressedStyle.addStyleListener(this);
            if(pressedStyle.getBgPainter() == null){
                pressedStyle.setBgPainter(new BGPainter());
            }
        }
        return pressedStyle;
    }

    /**
     * Sets the Component Style for the pressed state allowing us to manipulate
     * the look of the component when it is pressed
     *
     * @param style the component Style object
     */
    public void setPressedStyle(Style style) {
        if (pressedStyle != null) {
            pressedStyle.removeStyleListener(this);
        }
        pressedStyle = style;
        pressedStyle.addStyleListener(this);
        if (pressedStyle.getBgPainter() == null) {
            pressedStyle.setBgPainter(new BGPainter());
        }
        setShouldCalcPreferredSize(true);
        checkAnimation();
    }

    /**
     * @inheritDoc
     */
    protected void refreshTheme(String id) {
        if(pressedStyle != null) {
            setPressedStyle(mergeStyle(pressedStyle, UIManager.getInstance().getComponentCustomStyle(id, "press")));
        }
        super.refreshTheme(id);
    }

    /**
     * @inheritDoc
     */
    public Style getStyle() {
        if(state == STATE_PRESSED) {
            return getPressedStyle();
        }
        return super.getStyle();
    }

    /**
     * This method return the Button Command if exists
     * 
     * @return Command Object or null if a Command not exists
     */
    public Command getCommand() {
        return cmd;
    }

    /**
     * Returns true if the button is selected for toggle buttons,
     * throws an exception if this is not a toggle button
     *
     * @return true if the button is selected
     */
    public boolean isSelected() {
        throw new RuntimeException();
    }
}
