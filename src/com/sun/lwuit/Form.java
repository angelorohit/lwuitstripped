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

import com.sun.lwuit.animations.Animation;
import com.sun.lwuit.animations.CommonTransitions;
import com.sun.lwuit.geom.Rectangle;
import com.sun.lwuit.geom.Dimension;
import com.sun.lwuit.plaf.Style;
import com.sun.lwuit.animations.Transition;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.list.ListCellRenderer;
import com.sun.lwuit.layouts.BorderLayout;
import com.sun.lwuit.layouts.FlowLayout;
import com.sun.lwuit.layouts.GridLayout;
import com.sun.lwuit.layouts.Layout;
import com.sun.lwuit.plaf.LookAndFeel;
import com.sun.lwuit.plaf.UIManager;
import com.sun.lwuit.util.EventDispatcher;
import java.util.Hashtable;
import java.util.Vector;

/**
 * Top level component that serves as the root for the UI, this {@link Container}
 * handles the menus and title while placing content between them. By default a 
 * forms central content (the content pane) is scrollable.
 *
 * Form contains Title bar, MenuBar and a ContentPane.
 * Calling to addComponent on the Form is delegated to the contenPane.addComponent
 * 
 * *<pre>
 *
 *       **************************
 *       *         Title          *
 *       **************************
 *       *                        *
 *       *                        *
 *       *      ContentPane       *
 *       *                        *
 *       *                        *
 *       **************************
 *       *         MenuBar        *
 *       **************************
 *</pre> 
 * @author Chen Fishbein
 */
public class Form extends Container {
    Command selectMenuItem;
    Command cancelMenuItem;
    private Painter glassPane;
    private Container contentPane = new Container(new FlowLayout());
    private Label title = new Label("", "Title");
    private MenuBar menuBar = new MenuBar();
    private Command selectCommand;
    private Command defaultCommand;
    private Component dragged;
    /**
     * Indicates the command that is defined as the back command out of this form.
     * A back command can be used both to map to a hardware button (e.g. on the Sony Ericsson devices)
     * and by elements such as transitions etc. to change the behavior based on 
     * direction (e.g. slide to the left to enter screen and slide to the right to exit with back).
     */
    private Command backCommand;

    /**
     * Used by the combo box to block some default LWUIT behaviors
     */
    static boolean comboLock;

    /**
     * Indicates the command that is defined as the clear command out of this form similar
     * in spirit to the back command
     */
    private Command clearCommand;

    /**
     * Contains a list of components that would like to animate their state
     */
    private Vector animatableComponents;

    /**
     * Contains a list of components that would like to animate their state
     */
    private Vector internalAnimatableComponents;

    /**
     * This member holds the left soft key value
     */
    static int leftSK = -6;
    /**
     * This member holds the right soft key value
     */
    static int rightSK = -7;
    /**
     * This member holds the 2nd right soft key value
     * this is used for different BB devices
     */
    static int rightSK2 = -7;
    /**
     * This member holds the back command key value
     */
    static int backSK = -11;
    /**
     * This member holds the clear command key value
     */
    static int clearSK = -8;
    static int backspaceSK = -8;
    //private FormSwitcher formSwitcher;
    private Component focused;
    private Vector mediaComponents;
    /**
     * This member allows us to define an animation that will draw the transition for
     * entering this form. A transition is an animation that would occur when 
     * switching from one form to another.
     */
    private Transition transitionInAnimator;
    /**
     * This member allows us to define a an animation that will draw the transition for
     * exiting this form. A transition is an animation that would occur when 
     * switching from one form to another.
     */
    private Transition transitionOutAnimator;
    /**
     * a listener that is invoked when a command is clicked allowing multiple commands
     * to be handled by a single block
     */
    private EventDispatcher commandListener;

    private EventDispatcher pointerPressedListeners;
    private EventDispatcher pointerReleasedListeners;
    private EventDispatcher pointerDraggedListeners;

    /**
     * Relevant for modal forms where the previous form should be rendered underneath
     */
    private Form previousForm;
    /**
     * Indicates that this form should be tinted when painted
     */
    private boolean tint;
    /**
     * Default color for the screen tint when a dialog or a menu is shown
     */
    private int tintColor;
    /**
     * Allows us to cache the next focus component ordered from top to down, this
     * vector is guaranteed to have all focusable children in it.
     */
    private Vector focusDownSequence;
    /**
     * Allows us to cache the next focus component ordered from left to right, this
     * vector is guaranteed to have all focusable children in it.
     */
    private Vector focusRightSequence;
    /**
     * Listeners for key release events 
     */
    private Hashtable keyListeners;
    /**
     * Listeners for game key release events 
     */
    private Hashtable gameKeyListeners;
    /**
     * Indicates whether focus should cycle within the form
     */
    private boolean cyclicFocus = true;

    private int tactileTouchDuration;

    /**
     * Default constructor creates a simple form
     */
    public Form() {
        super(new BorderLayout());
        setUIID("Form");
        // forms/dialogs are not visible by default
        setVisible(false);
        Style formStyle = getStyle();
        int w = Display.getInstance().getDisplayWidth() - (formStyle.getMargin(isRTL(), Component.LEFT) + formStyle.getMargin(isRTL(), Component.RIGHT));
        int h = Display.getInstance().getDisplayHeight() - (formStyle.getMargin(false, Component.TOP) + formStyle.getMargin(false, Component.BOTTOM));

        setWidth(w);
        setHeight(h);
        setPreferredSize(new Dimension(w, h));

        title.setEndsWith3Points(false);
        super.addComponent(BorderLayout.NORTH, title);
        super.addComponent(BorderLayout.CENTER, contentPane);
        super.addComponent(BorderLayout.SOUTH, menuBar);
        contentPane.setUIID("ContentPane");
        contentPane.setScrollableY(true);
        LookAndFeel laf = UIManager.getInstance().getLookAndFeel();
        initLaf(laf);
        tintColor = laf.getDefaultFormTintColor();

        selectMenuItem = createMenuSelectCommand();
        cancelMenuItem = createMenuCancelCommand();
        
        // hardcoded, anything else is just pointless...
        formStyle.setBgTransparency(0xFF);
    }

    /**
     * Sets the style of the menu bar programmatically
     * 
     * @param s new style
     * @deprecated use setSoftButtonStyle instead
     */
    public void setMenuStyle(Style s) {
        menuBar.setStyle(s);
    }

    /**
     * Sets the style of the menu bar programmatically
     * 
     * @param s new style
     */
    public void setSoftButtonStyle(Style s) {
        menuBar.setUnSelectedStyle(s);
    }

    /**
     * Retrieves the style of the menu bar programmatically
     * 
     * @return the style of the softbutton
     */
    public Style getSoftButtonStyle() {
        return menuBar.getStyle();
    }

    /**
     * This method is only invoked when the underlying canvas for the form is hidden
     * this method isn't called for form based events and is generally usable for
     * suspend/resume based behavior
     */
    protected void hideNotify() {
    }

    /**
     * This method is only invoked when the underlying canvas for the form is shown
     * this method isn't called for form based events and is generally usable for
     * suspend/resume based behavior
     */
    protected void showNotify() {
    }


    /**
     * This method is only invoked when the underlying canvas for the form gets
     * a size changed event.
     * This method will trigger a relayout of the Form.
     * This method will get the callback only if this Form is the Current Form
     *
     * @param w the new width of the Form
     * @param h the new height of the Form
     */
    protected void sizeChanged(int w, int h) {
    }

    /**
     * This method is only invoked when the underlying canvas for the form gets
     * a size changed event.
     * This method will trigger a relayout of the Form.
     * This method will get the callback only if this Form is the Current Form
     * @param w the new width of the Form
     * @param h the new height of the Form
     */
    void sizeChangedInternal(int w, int h) {
        sizeChanged(w, h);
        setSize(new Dimension(w, h));
        setShouldCalcPreferredSize(true);
        doLayout();        
        repaint();
    }

    /**
     * Allows a developer that doesn't derive from the form to draw on top of the 
     * form regardless of underlying changes or animations. This is useful for
     * watermarks or special effects (such as tinting) it is also useful for generic
     * drawing of validation errors etc... A glass pane is generally 
     * transparent or translucent and allows the the UI bellow to be seen.
     * 
     * @param glassPane a new glass pane to install. It is generally recommended to
     * use a painter chain if more than one painter is required.
     */
    public void setGlassPane(Painter glassPane) {
        this.glassPane = glassPane;
        repaint();
    }

    /**
     * Allows a developer that doesn't derive from the form to draw on top of the 
     * form regardless of underlying changes or animations. This is useful for
     * watermarks or special effects (such as tinting) it is also useful for generic
     * drawing of validation errors etc... A glass pane is generally 
     * transparent or translucent and allows the the UI bellow to be seen.
     * 
     * @return the instance of the glass pane for this form
     * @see com.sun.lwuit.painter.PainterChain#installGlassPane(Form, com.sun.lwuit.Painter) 
     */
    public Painter getGlassPane() {
        return glassPane;
    }

    /**
     * Sets the style of the title programmatically
     * 
     * @param s new style
     */
    public void setTitleStyle(Style s) {
        title.setUnSelectedStyle(s);
    }

    /**
     * Allows modifying the title attributes beyond style (e.g. setting icon/alignment etc.)
     * 
     * @return the component representing the title for the form
     */
    public Label getTitleComponent() {
        return title;
    }

    /**
     * Allows replacing the title with a different title component, thus allowing
     * developers to create more elaborate title objects.
     *
     * @param title new title component
     */
    public void setTitleComponent(Label title) {
        super.replace(this.title, title);
        this.title = title;
    }

    /**
     * Allows replacing the title with a different title component, thus allowing
     * developers to create more elaborate title objects. This version of the
     * method allows special effects for title replacement such as transitions
     * for title entering
     *
     * @param title new title component
     * @param t transition for title replacement
     */
    public void setTitleComponent(Label title, Transition t) {
        super.replace(this.title, title);
        this.title = title;
    }

    /**
     * Add a key listener to the given keycode for a callback when the key is released
     * 
     * @param keyCode code on which to send the event
     * @param listener listener to invoke when the key code released.
     */
    public void addKeyListener(int keyCode, ActionListener listener) {
        if (keyListeners == null) {
            keyListeners = new Hashtable();
        }
        addKeyListener(keyCode, listener, keyListeners);
    }

    /**
     * Removes a key listener from the given keycode 
     * 
     * @param keyCode code on which the event is sent
     * @param listener listener instance to remove
     */
    public void removeKeyListener(int keyCode, ActionListener listener) {
        if (keyListeners == null) {
            return;
        }
        removeKeyListener(keyCode, listener, keyListeners);
    }

    /**
     * Removes a game key listener from the given game keycode 
     * 
     * @param keyCode code on which the event is sent
     * @param listener listener instance to remove
     */
    public void removeGameKeyListener(int keyCode, ActionListener listener) {
        if (gameKeyListeners == null) {
            return;
        }
        removeKeyListener(keyCode, listener, gameKeyListeners);
    }

    private void addKeyListener(int keyCode, ActionListener listener, Hashtable keyListeners) {
        if (keyListeners == null) {
            keyListeners = new Hashtable();
        }
        Integer code = new Integer(keyCode);
        Vector vec = (Vector) keyListeners.get(code);
        if (vec == null) {
            vec = new Vector();
            vec.addElement(listener);
            keyListeners.put(code, vec);
            return;
        }
        if (!vec.contains(listener)) {
            vec.addElement(listener);
        }
    }

    private void removeKeyListener(int keyCode, ActionListener listener, Hashtable keyListeners) {
        if (keyListeners == null) {
            return;
        }
        Integer code = new Integer(keyCode);
        Vector vec = (Vector) keyListeners.get(code);
        if (vec == null) {
            return;
        }
        vec.removeElement(listener);
        if (vec.size() == 0) {
            keyListeners.remove(code);
        }
    }

    /**
     * Add a game key listener to the given gamekey for a callback when the 
     * key is released
     * 
     * @param keyCode code on which to send the event
     * @param listener listener to invoke when the key code released.
     */
    public void addGameKeyListener(int keyCode, ActionListener listener) {
        if (gameKeyListeners == null) {
            gameKeyListeners = new Hashtable();
        }
        addKeyListener(keyCode, listener, gameKeyListeners);
    }

    /**
     * Returns the number of buttons on the menu bar for use with getSoftButton()
     * 
     * @return the number of softbuttons
     */
    public int getSoftButtonCount() {
        return menuBar.getSoftButtons().length;
    }

    /**
     * Returns the button representing the softbutton, this allows modifying softbutton
     * attributes and behavior programmatically rather than by using the command API.
     * Notice that this API behavior is fragile since the button mapped to a particular
     * offset might change based on the command API
     * 
     * @param offset the offest of the softbutton
     * @return a button that can be manipulated
     */
    public Button getSoftButton(int offset) {
        return menuBar.getSoftButtons()[offset];
    }

    /**
     * Returns the style of the menu
     * 
     * @return the style of the menu
     */
    public Style getMenuStyle() {
        return menuBar.getMenuStyle();
    }

    /**
     * Returns the style of the title
     * 
     * @return the style of the title
     */
    public Style getTitleStyle() {
        return title.getStyle();
    }

    /**
     * Allows the display to skip the menu dialog if that is the current form
     */
    Form getPreviousForm() {
        return previousForm;
    }

    /**
     * @inheritDoc
     */
    void initLaf(LookAndFeel laf) {
        transitionOutAnimator = laf.getDefaultFormTransitionOut();
        transitionInAnimator = laf.getDefaultFormTransitionIn();
    }

    /**
     * Resets the cache focus vectors, this is a good idea when we remove
     * or add an element to the layout.
     */
    void clearFocusVectors() {
        focusDownSequence = null;
        focusRightSequence = null;
    }

    /**
     * Sets the current dragged Component
     */
    void setDraggedComponent(Component dragged) {
        this.dragged = dragged;
    }

    synchronized void initFocusRight() {
        if (focusRightSequence == null) {
            focusRightSequence = new Vector();
            findAllFocusable(contentPane, focusRightSequence, true);
        }
    }

    synchronized void initFocusDown() {
        if (focusDownSequence == null) {
            focusDownSequence = new Vector();
            findAllFocusable(contentPane, focusDownSequence, false);
        }
    }

    /**
     * Adds a component to the vector in the appropriate location based on its
     * focus order
     */
    private void addSortedComponentRight(Vector components, Component c) {
        int componentCount = components.size();
        int componentX = c.getAbsoluteX();

        int bestSpot = 0;
        boolean rtl = isRTL();

        Component scrollableParent = findScrollableAncestor(c);

        // find components in the same row and add the component either at the end
        // of the line or at its start
        for (int iter = 0; iter < componentCount; iter++) {
            Component current = (Component) components.elementAt(iter);

            // this component is in the same row...
            Component currentScrollParent = findScrollableAncestor(current);
            if (currentScrollParent == scrollableParent) {
                if (isInSameRow(current, c)) {
                    int currentX = current.getAbsoluteX();
                    if (((!rtl) && (currentX > componentX)) ||
                    	((rtl) && (currentX < componentX))) {
                        continue;
                    }
                    bestSpot = iter + 1;
                    continue;
                }
            } else {
                Component tempScrollableParent = scrollableParent;
                if (scrollableParent == null) {
                    tempScrollableParent = c;
                }
                Component tempCurrentScrollParent = currentScrollParent;
                if (currentScrollParent == null) {
                    tempCurrentScrollParent = current;
                }
                if (((!rtl) && (tempCurrentScrollParent.getAbsoluteX() > tempScrollableParent.getAbsoluteX())) ||
                	((rtl) && (tempCurrentScrollParent.getAbsoluteX() < tempScrollableParent.getAbsoluteX()))) {
                    continue;
                }
                if (isInSameRow(tempCurrentScrollParent, tempScrollableParent)) {
                    bestSpot = iter + 1;
                    continue;
                }
            }
            if (current.getAbsoluteY() < c.getAbsoluteY()) {
                bestSpot = iter + 1;
            }
        }

        components.insertElementAt(c, bestSpot);
    }

    /**
     * Returns the first scrollable ancestor for this component or null if no
     * such ancestor exists
     */
    private Component findScrollableAncestor(Component c) {
        c = c.getParent();
        if (c == null || c.isScrollable()) {
            return c;
        }
        return findScrollableAncestor(c);
    }

    /**
    `     * Adds a component to the vector in the appropriate location based on its
     * focus order
     */
    private void addSortedComponentDown(Vector components, Component c) {
        int componentCount = components.size();
        int componentY = c.getAbsoluteY();

        int bestSpot = 0;
        boolean rtl = isRTL();

        Component scrollableParent = findScrollableAncestor(c);

        // find components in the same column and add the component either at the end
        // of the line or at its start
        for (int iter = 0; iter < componentCount; iter++) {
            Component current = (Component) components.elementAt(iter);

            // this component is in the same column...
            Component currentScrollParent = findScrollableAncestor(current);
            if (currentScrollParent == scrollableParent) {
                if (isInSameColumn(current, c)) {
                    int currentY = current.getAbsoluteY();
                    if (currentY > componentY) {
                        continue;
                    }
                    bestSpot = iter + 1;
                    continue;
                }
            } else {
                Component tempScrollableParent = scrollableParent;
                if (scrollableParent == null) {
                    tempScrollableParent = c;
                }
                Component tempCurrentScrollParent = currentScrollParent;
                if (currentScrollParent == null) {
                    tempCurrentScrollParent = current;
                }
                if (tempCurrentScrollParent.getAbsoluteY() > tempScrollableParent.getAbsoluteY()) {
                    continue;
                }
                if (isInSameColumn(tempCurrentScrollParent, tempScrollableParent)) {
                    bestSpot = iter + 1;
                    continue;
                }
            }
            if (((!rtl) && (current.getAbsoluteX() < c.getAbsoluteX())) ||
            	((rtl) && (current.getAbsoluteX() > c.getAbsoluteX()))) {
                bestSpot = iter + 1;
            }
        }

        components.insertElementAt(c, bestSpot);
    }

    /**
     * Returns true if the given dest component is in the column of the source component
     */
    private boolean isInSameColumn(Component source, Component dest) {
        return Rectangle.intersects(source.getAbsoluteX(), source.getAbsoluteY(),
                source.getWidth(), Integer.MAX_VALUE, dest.getAbsoluteX(), dest.getAbsoluteY(),
                dest.getWidth(), dest.getHeight());
    }

    /**
     * Returns true if the given dest component is in the row of the source component
     */
    private boolean isInSameRow(Component source, Component dest) {
        return Rectangle.intersects(source.getAbsoluteX(), source.getAbsoluteY(),
                Integer.MAX_VALUE, source.getHeight(), dest.getAbsoluteX(), dest.getAbsoluteY(),
                dest.getWidth(), dest.getHeight());
    }

    /**
     * Adds a component to the vector in the appropriate location based on its
     * focus order
     */
    private void addSortedComponent(Vector components, Component c, boolean toTheRight) {
        if (toTheRight) {
            addSortedComponentRight(components, c);
        } else {
            addSortedComponentDown(components, c);
        }
    }

    /**
     * Default command is invoked when a user presses fire, this functionality works
     * well in some situations but might collide with elements such as navigation
     * and combo boxes. Use with caution.
     * 
     * @param defaultCommand the command to treat as default
     */
    public void setDefaultCommand(Command defaultCommand) {
        this.defaultCommand = defaultCommand;
    }

    /**
     * Default command is invoked when a user presses fire, this functionality works
     * well in some situations but might collide with elements such as navigation
     * and combo boxes. Use with caution.
     * 
     * @return the command to treat as default
     */
    public Command getDefaultCommand() {
        if (selectCommand != null) {
            return selectCommand;
        }
        return defaultCommand;
    }

    /**
     * Indicates the command that is defined as the clear command in this form.
     * A clear command can be used both to map to a "clear" hardware button 
     * if such a button exists.
     * 
     * @param clearCommand the command to treat as the clear Command
     */
    public void setClearCommand(Command clearCommand) {
        this.clearCommand = clearCommand;
    }

    /**
     * Indicates the command that is defined as the clear command in this form.
     * A clear command can be used both to map to a "clear" hardware button 
     * if such a button exists.
     * 
     * @return the command to treat as the clear Command
     */
    public Command getClearCommand() {
        return clearCommand;
    }

    /**
     * Indicates the command that is defined as the back command out of this form.
     * A back command can be used both to map to a hardware button (e.g. on the Sony Ericsson devices)
     * and by elements such as transitions etc. to change the behavior based on 
     * direction (e.g. slide to the left to enter screen and slide to the right to exit with back).
     * 
     * @param backCommand the command to treat as the back Command
     */
    public void setBackCommand(Command backCommand) {
        this.backCommand = backCommand;
    }

    /**
     * Indicates the command that is defined as the back command out of this form.
     * A back command can be used both to map to a hardware button (e.g. on the Sony Ericsson devices)
     * and by elements such as transitions etc. to change the behavior based on 
     * direction (e.g. slide to the left to enter screen and slide to the right to exit with back).
     * 
     * @return the command to treat as the back Command
     */
    public Command getBackCommand() {
        return backCommand;
    }

    /**
     * Finds all focusable components in the hierarchy 
     */
    private void findAllFocusable(Container c, Vector v, boolean toTheRight) {
        int size = c.getComponentCount();

        for (int iter = 0; iter < size; iter++) {
            Component current = c.getComponentAt(iter);
            if (current instanceof Container) {
                findAllFocusable((Container) current, v, toTheRight);
            }
            if (current.isFocusable()) {
                addSortedComponent(v, current, toTheRight);
            }
        }
    }

    /**
     * Sets the title after invoking the constructor
     * 
     * @param title the form title
     */
    public Form(String title) {
        this();
        this.title.setText(title);
    }

    /**
     * This method returns the Content pane instance
     * 
     * @return a content pane instance
     */
    public Container getContentPane() {
        return contentPane;
    }

    /**
     * Removes all Components from the Content Pane
     */
    public void removeAll() {
        contentPane.removeAll();
    }

    /**
     * Sets the background image to show behind the form
     * 
     * @param bgImage the background image
     */
    public void setBgImage(Image bgImage) {
        getStyle().setBgImage(bgImage);
    }

    /**
     * @inheritDoc
     */
    public void setLayout(Layout layout) {
        contentPane.setLayout(layout);
    }

    /**
     * Sets the Form title to the given text
     * 
     * @param title the form title
     */
    public void setTitle(String title) {
        this.title.setText(title);
        if(isInitialized() && this.title.isTickerEnabled()) {
            if(this.title.shouldTickerStart()) {
                this.title.startTicker(UIManager.getInstance().getLookAndFeel().getTickerSpeed(), true);
            } else {
                if(this.title.isTickerRunning()) {
                    this.title.stopTicker();
                }
            }
        }
    }

    /**
     * Returns the Form title text
     * 
     * @return returns the form title
     */
    public String getTitle() {
        return title.getText();
    }

    /**
     * Adds Component to the Form's Content Pane
     * 
     * @param cmp the added param
     */
    public void addComponent(Component cmp) {
        contentPane.addComponent(cmp);
    }

    /**
     * @inheritDoc
     */
    public void addComponent(Object constraints, Component cmp) {
        contentPane.addComponent(constraints, cmp);
    }

    /**
     * @inheritDoc
     */
    public void addComponent(int index, Object constraints, Component cmp) {
        contentPane.addComponent(index, constraints, cmp);
    }

    /**
     * Adds Component to the Form's Content Pane
     * 
     * @param cmp the added param
     */
    public void addComponent(int index, Component cmp) {
        contentPane.addComponent(index, cmp);
    }

    /**
     * @inheritDoc
     */
    public void replace(Component current, Component next, Transition t) {
        contentPane.replace(current, next, t);
    }

    /**
     * @inheritDoc
     */
    public void replaceAndWait(Component current, Component next, Transition t) {
        contentPane.replaceAndWait(current, next, t);
    }

    /**
     * Removes a component from the Form's Content Pane
     * 
     * @param cmp the component to be removed
     */
    public void removeComponent(Component cmp) {
        contentPane.removeComponent(cmp);
    }

    /**
     * Registering media component to this Form, that like to receive 
     * animation events
     * 
     * @param mediaCmp the Form media component to be registered
     */
    void registerMediaComponent(Component mediaCmp) {
        if (mediaComponents == null) {
            mediaComponents = new Vector();
        }
        if (!mediaComponents.contains(mediaCmp)) {
            mediaComponents.addElement(mediaCmp);
        }
    }

    /**
     * Used by the implementation to prevent flickering when flushing the double buffer
     * 
     * @return true if the form has media components within it
     */
    public final boolean hasMedia() {
        return mediaComponents != null && mediaComponents.size() > 0;
    }

    /**
     * Indicate that cmp would no longer like to receive animation events
     * 
     * @param cmp component that would no longer receive animation events
     */
    void deregisterMediaComponent(Component mediaCmp) {
        mediaComponents.removeElement(mediaCmp);
    }

    /**
     * The given component is interested in animating its appearance and will start
     * receiving callbacks when it is visible in the form allowing it to animate
     * its appearance. This method would not register a compnent instance more than once
     * 
     * @param cmp component that would be animated
     */
    public void registerAnimated(Animation cmp) {
        if (animatableComponents == null) {
            animatableComponents = new Vector();
        }
        if (!animatableComponents.contains(cmp)) {
            animatableComponents.addElement(cmp);
        }
        Display.getInstance().notifyDisplay();
    }


    /**
     * Identical to the none-internal version, the difference between the internal/none-internal
     * is that it references a different vector that is unaffected by the user actions.
     * That is why we can dynamically register/deregister without interfearing with user interaction.
     */
    void registerAnimatedInternal(Animation cmp) {
        if (internalAnimatableComponents == null) {
            internalAnimatableComponents = new Vector();
        }
        if (!internalAnimatableComponents.contains(cmp)) {
            internalAnimatableComponents.addElement(cmp);
        }
        Display.getInstance().notifyDisplay();
    }

    /**
     * Identical to the none-internal version, the difference between the internal/none-internal
     * is that it references a different vector that is unaffected by the user actions.
     * That is why we can dynamically register/deregister without interfearing with user interaction.
     */
    void deregisterAnimatedInternal(Animation cmp) {
        if (internalAnimatableComponents != null) {
            internalAnimatableComponents.removeElement(cmp);
        }
    }

    /**
     * Indicate that cmp would no longer like to receive animation events
     * 
     * @param cmp component that would no longer receive animation events
     */
    public void deregisterAnimated(Animation cmp) {
        if (animatableComponents != null) {
            animatableComponents.removeElement(cmp);
        }
    }

    /**
     * Returns the offset of the component within the up/down focus sequence
     * 
     * @return offset between 0 and number of components or -1 for an error
     */
    int getFocusPosition(Component c) {
        initFocusDown();
        return focusDownSequence.indexOf(c);
    }

    /**
     * Makes sure all animations are repainted so they would be rendered in every
     * frame
     */
    void repaintAnimations() {
        if (animatableComponents != null) {
            loopAnimations(animatableComponents, null);
        }
        if (internalAnimatableComponents != null) {
            loopAnimations(internalAnimatableComponents, animatableComponents);
        }
    }

    private void loopAnimations(Vector v, Vector notIn) {
        // we don't save size() in a varible since the animate method may deregister
        // the animation thus invalidating the size
        for (int iter = 0; iter < v.size(); iter++) {
            Animation c = (Animation) v.elementAt(iter);
            if(notIn != null && notIn.contains(c)) {
                continue;
            }
            if (c.animate()) {
                if (c instanceof Component) {
                    Rectangle rect = ((Component) c).getDirtyRegion();
                    if (rect != null) {
                        ((Component) c).repaint(rect.getX(), rect.getY(), rect.getSize().getWidth(), rect.getSize().getHeight());
                    } else {
                        ((Component) c).repaint();
                    }
                } else {
                    Display.getInstance().repaint(c);
                }
            }
        }
    }

    /**
     * If this method returns true the EDT won't go to sleep indefinitely
     * 
     * @return true is form has animation; otherwise false
     */
    boolean hasAnimations() {
        return (animatableComponents != null && animatableComponents.size() > 0)
                || (internalAnimatableComponents != null && internalAnimatableComponents.size() > 0);
    }

    /**
     * @inheritDoc
     */
    public void refreshTheme() {
        // when changing the theme when a title/menu bar is not visible the refresh
        // won't apply to them. We need to protect against this occurance.
        if (menuBar != null) {
            menuBar.refreshTheme();
        }
        if (title != null) {
            title.refreshTheme();
        }
        super.refreshTheme();
    }

    /**
     * Exposing the background painting for the benefit of animations
     * 
     * @param g the form graphics
     */
    public void paintBackground(Graphics g) {
        super.paintBackground(g);
    }

    /**
     * This property allows us to define a an animation that will draw the transition for
     * entering this form. A transition is an animation that would occur when 
     * switching from one form to another.
     * 
     * @return the Form in transition
     */
    public Transition getTransitionInAnimator() {
        return transitionInAnimator;
    }

    /**
     * This property allows us to define a an animation that will draw the transition for
     * entering this form. A transition is an animation that would occur when 
     * switching from one form to another.
     * 
     * @param transitionInAnimator the Form in transition
     */
    public void setTransitionInAnimator(Transition transitionInAnimator) {
        this.transitionInAnimator = transitionInAnimator;
    }

    /**
     * This property allows us to define a an animation that will draw the transition for
     * exiting this form. A transition is an animation that would occur when 
     * switching from one form to another.
     * 
     * @return the Form out transition
     */
    public Transition getTransitionOutAnimator() {
        return transitionOutAnimator;
    }

    /**
     * This property allows us to define a an animation that will draw the transition for
     * exiting this form. A transition is an animation that would occur when 
     * switching from one form to another.
     * 
     * @param transitionOutAnimator the Form out transition
     */
    public void setTransitionOutAnimator(Transition transitionOutAnimator) {
        this.transitionOutAnimator = transitionOutAnimator;
    }

    /**
     * A listener that is invoked when a command is clicked allowing multiple commands
     * to be handled by a single block
     * 
     * @param commandListener the command action listener
     * @deprecated use add/removeCommandListener instead
     */
    public void setCommandListener(ActionListener commandListener) {
        if(commandListener == null) {
            this.commandListener = null;
            return;
        }
        addCommandListener(commandListener);
    }

    /**
     * A listener that is invoked when a command is clicked allowing multiple commands
     * to be handled by a single block
     *
     * @param l the command action listener
     */
    public void addCommandListener(ActionListener l) {
        if(commandListener == null) {
            commandListener = new EventDispatcher();
        }
        commandListener.addListener(l);
    }

    /**
     * A listener that is invoked when a command is clicked allowing multiple commands
     * to be handled by a single block
     *
     * @param l the command action listener
     */
    public void removeCommandListener(ActionListener l) {
        commandListener.removeListener(l);
    }

    /**
     * Invoked to allow subclasses of form to handle a command from one point
     * rather than implementing many command instances. All commands selected 
     * on the form will trigger this method implicitly.
     * 
     * @param cmd the form commmand object
     */
    protected void actionCommand(Command cmd) {
    }

    /**
     * Dispatches a command via the standard form mechanism of firing a command event
     * 
     * @param cmd The command to dispatch
     * @param ev the event to dispatch 
     */
    public void dispatchCommand(Command cmd, ActionEvent ev) {
        cmd.actionPerformed(ev);
        if(!ev.isConsumed()) {
            actionCommandImpl(cmd, ev);
        }
    }

    /**
     * Invoked to allow subclasses of form to handle a command from one point
     * rather than implementing many command instances
     */
    void actionCommandImpl(Command cmd) {
        actionCommandImpl(cmd, new ActionEvent(cmd));
    }

    /**
     * Invoked to allow subclasses of form to handle a command from one point
     * rather than implementing many command instances
     */
    void actionCommandImpl(Command cmd, ActionEvent ev) {
        if (cmd == null) {
            return;
        }

        if (cmd != selectCommand && (!comboLock)) {
            if (commandListener != null) {
                commandListener.fireActionEvent(ev);
                if(ev.isConsumed()) {
                    return;
                }
            }
            actionCommand(cmd);
        } else {
            Component c = getFocused();
            if (c != null) {
                c.fireClicked();
            }
        }
    }

    void initFocused() {
        if (focused == null) {
            setFocused(findFirstFocusable(contentPane));
            layoutContainer();
            initFocusDown();
            if(focusDownSequence == null) {
                initFocusDown();
                if (focusDownSequence.size() > 0) {
                    setFocused((Component) focusDownSequence.elementAt(0));
                }
            } else {
                if (focusDownSequence.size() > 0) {
                    setFocused((Component) focusDownSequence.elementAt(0));
                }
            }
        }
    }

    /**
     * Displays the current form on the screen
     */
    public void show() {
        show(false);
    }

    /**
     * Displays the current form on the screen, this version of the method is
     * useful for "back" navigation since it reverses the direction of the transition.
     */
    public void showBack() {
        show(true);
    }

    /**
     * Displays the current form on the screen
     */
    private void show(boolean reverse) {
        if (transitionOutAnimator == null && transitionInAnimator == null) {
            initLaf(UIManager.getInstance().getLookAndFeel());
        }
        initFocused();
        onShow();
        tint = false;
        com.sun.lwuit.Display.getInstance().setCurrent(this, reverse);
    }

    /**
     * @inheritDoc
     */
    void initComponentImpl() {
        super.initComponentImpl();
        tactileTouchDuration = UIManager.getInstance().getLookAndFeel().getTactileTouchDuration();
        if(title.getText() != null && title.shouldTickerStart()) {
            title.startTicker(UIManager.getInstance().getLookAndFeel().getTickerSpeed(), true);
        }
    }

    /**
     * @inheritDoc
     */
    public void setSmoothScrolling(boolean smoothScrolling) {
        // invoked by the constructor for component
        if (contentPane != null) {
            contentPane.setSmoothScrolling(smoothScrolling);
        }
    }

    /**
     * @inheritDoc
     */
    public boolean isSmoothScrolling() {
        return contentPane.isSmoothScrolling();
    }

    /**
     * @inheritDoc
     */
    public int getScrollAnimationSpeed() {
        return contentPane.getScrollAnimationSpeed();
    }

    /**
     * @inheritDoc
     */
    public void setScrollAnimationSpeed(int animationSpeed) {
        contentPane.setScrollAnimationSpeed(animationSpeed);
    }

    /**
     * Allows subclasses to bind functionality that occurs when
     * a specific form or dialog appears on the screen
     */
    protected void onShow() {
    }

    /**
     * Allows subclasses to bind functionality that occurs when
     * a specific form or dialog is "really" showing hence when
     * the transition is totally complete (unlike onShow which is called
     * on intent). The necessity for this is for special cases like
     * media that might cause artifacts if played during a transition.
     */
    protected void onShowCompleted() {
    }

    /**
     * This method shows the form as a modal alert allowing us to produce a behavior
     * of an alert/dialog box. This method will block the calling thread even if the
     * calling thread is the EDT. Notice that this method will not release the block
     * until dispose is called even if show() from another form is called!
     * <p>Modal dialogs Allow the forms "content" to "hang in mid air" this is especially useful for
     * dialogs where you would want the underlying form to "peek" from behind the 
     * form. 
     * 
     * @param top space in pixels between the top of the screen and the form
     * @param bottom space in pixels between the bottom of the screen and the form
     * @param left space in pixels between the left of the screen and the form
     * @param right space in pixels between the right of the screen and the form
     * @param includeTitle whether the title should hang in the top of the screen or
     * be glued onto the content pane
     * @param modal indictes if this is a modal or modeless dialog true for modal dialogs
     */
    void showModal(int top, int bottom, int left, int right, boolean includeTitle, boolean modal, boolean reverse) {
        Display.getInstance().flushEdt();
        if (previousForm == null){
            previousForm = Display.getInstance().getCurrent();
            // special case for application opening with a dialog before any form is shown
            if (previousForm == null) {
                previousForm = new Form();
                previousForm.show();
            } else {
                if(previousForm instanceof Dialog) {
                    Dialog previousDialog = (Dialog)previousForm;
                    if(previousDialog.isDisposed()) {
                        previousForm = Display.getInstance().getCurrentUpcoming();
                    }
                }
            }

            previousForm.tint = true;
        }
        Painter p = getStyle().getBgPainter();
        if (top > 0 || bottom > 0 || left > 0 || right > 0) {
            Style titleStyle = title.getStyle();
            Style contentStyle = contentPane.getUnselectedStyle();
            if (includeTitle) {
                titleStyle.setMargin(Component.TOP, top, true);
                titleStyle.setMargin(Component.BOTTOM, 0, true);
                titleStyle.setMargin(Component.LEFT, left, true);
                titleStyle.setMargin(Component.RIGHT, right, true);

                contentStyle.setMargin(Component.TOP, 0, true);
                contentStyle.setMargin(Component.BOTTOM, bottom, true);
                contentStyle.setMargin(Component.LEFT, left, true);
                contentStyle.setMargin(Component.RIGHT, right, true);
            } else {
                titleStyle.setMargin(Component.TOP, 0, true);
                titleStyle.setMargin(Component.BOTTOM, 0, true);
                titleStyle.setMargin(Component.LEFT, 0, true);
                titleStyle.setMargin(Component.RIGHT, 0, true);

                contentStyle.setMargin(Component.TOP, top, true);
                contentStyle.setMargin(Component.BOTTOM, bottom, true);
                contentStyle.setMargin(Component.LEFT, left, true);
                contentStyle.setMargin(Component.RIGHT, right, true);
            }
            if (p instanceof BGPainter && ((BGPainter) p).getPreviousForm() != null) {
                ((BGPainter) p).setPreviousForm(previousForm);
                ((BGPainter) p).setParent(this);
            } else {
                BGPainter b = new BGPainter(this, p);
                b.setIgnorCoordinates(true);
                getStyle().setBgPainter(b);
                b.setPreviousForm(previousForm);
            }
            revalidate();
        }

        initFocused();
        if (getTransitionOutAnimator() == null && getTransitionInAnimator() == null) {
            initLaf(UIManager.getInstance().getLookAndFeel());
        }
        
        initComponentImpl();
        Display.getInstance().setCurrent(this, reverse);
        onShow();

        if (modal) {
            // called to display a dialog and wait for modality  
            Display.getInstance().invokeAndBlock(new RunnableWrapper(this, p, reverse));

            // if the virtual keyboard was opend by the dialog close it
            if(Display.getInstance().isVirtualKeyboardShowingSupported()) {
                Display.getInstance().setShowVirtualKeyboard(false);
            }
        }
    }

    /**
     * The default version of show modal shows the dialog occupying the center portion
     * of the screen.
     */
    void showModal(boolean reverse) {
        showDialog(true, reverse);
    }

    /**
     * The default version of show dialog shows the dialog occupying the center portion
     * of the screen.
     */
    void showDialog(boolean modal, boolean reverse) {
        int h = Display.getInstance().getDisplayHeight() - menuBar.getPreferredH() - title.getPreferredH();
        int w = Display.getInstance().getDisplayWidth();
        int topSpace = h / 100 * 20;
        int bottomSpace = h / 100 * 10;
        int sideSpace = w / 100 * 20;
        showModal(topSpace, bottomSpace, sideSpace, sideSpace, true, modal, reverse);
    }

    /**
     * Works only for modal forms by returning to the previous form
     */
    void dispose() {
        disposeImpl();
    }

    boolean isDisposed() {
        return false;
    }

    /**
     * Works only for modal forms by returning to the previous form
     */
    void disposeImpl() {
        if (previousForm != null) {
            previousForm.tint = false;

            if (previousForm instanceof Dialog) {
                if (!((Dialog) previousForm).isDisposed()) {
                    Display.getInstance().setCurrent(previousForm, false);
                }
            } else {
                Display.getInstance().setCurrent(previousForm, false);
            }

            // enable GC to cleanup the previous form if no longer referenced
            previousForm = null;
        }
    }

    boolean isMenu() {
        return false;
    }

    /**
     * @inheritDoc
     */
    void repaint(Component cmp) {
        if (isVisible()) {
            Display.getInstance().repaint(cmp);
        }
    }

    /**
     * @inheritDoc
     */
    public final Form getComponentForm() {
        return this;
    }

    /**
     * Invoked by display to hide the menu during transition
     * 
     * @see restoreMenu
     */
    void hideMenu() {
        super.removeComponent(menuBar);
    }

    /**
     * Invoked by display to restore the menu after transition
     * 
     * @see hideMenu
     */
    void restoreMenu() {
        if (menuBar.getParent() == null) {
            super.addComponent(BorderLayout.SOUTH, menuBar);
        }
    }

    /**
     * Sets the focused component and fires the appropriate events to make it so
     * 
     * @param focused the newly focused component or null for no focus
     */
    public void setFocused(Component focused) {
        if (this.focused == focused && focused != null) {
            this.focused.repaint();
            return;
        }
        Component oldFocus = this.focused;
        this.focused = focused;
        boolean triggerRevalidate = false;
        if (oldFocus != null) {
            triggerRevalidate = changeFocusState(oldFocus, false);
            //if we need to revalidate no need to repaint the Component, it will
            //be painted from the Form
            if (!triggerRevalidate && oldFocus.getParent() != null) {
                oldFocus.repaint();
            }
        }
        // a listener might trigger a focus change event essentially
        // invalidating focus so we shouldn't break that 
        if (focused != null && this.focused == focused) {
            triggerRevalidate = changeFocusState(focused, true) || triggerRevalidate;
            //if we need to revalidate no need to repaint the Component, it will
            //be painted from the Form
            if(!triggerRevalidate){
                focused.repaint();
            }
        }
        if(triggerRevalidate){
            revalidate();
        }
    }

    /**
     * This method changes the cmp state to be focused/unfocused and fires the 
     * focus gained/lost events. 
     * @param cmp the Component to change the focus state
     * @param gained if true this Component needs to gain focus if false
     * it needs to lose focus
     * @return this method returns true if the state change needs to trigger a 
     * revalidate
     */
    private boolean changeFocusState(Component cmp, boolean gained){
        boolean trigger = false;
        Style selected = cmp.getSelectedStyle();
        Style unselected = cmp.getUnselectedStyle();
        //if selected style is different then unselected style there is a good 
        //chance we need to trigger a revalidate
        if(!selected.getFont().equals(unselected.getFont()) || 
                selected.getPadding(false, Component.TOP) != unselected.getPadding(false, Component.TOP) ||
                selected.getPadding(false, Component.BOTTOM) != unselected.getPadding(false, Component.BOTTOM) ||
                selected.getPadding(isRTL(), Component.RIGHT) != unselected.getPadding(isRTL(), Component.RIGHT) ||
                selected.getPadding(isRTL(), Component.LEFT) != unselected.getPadding(isRTL(), Component.LEFT) ||
                selected.getMargin(false, Component.TOP) != unselected.getMargin(false, Component.TOP) ||
                selected.getMargin(false, Component.BOTTOM) != unselected.getMargin(false, Component.BOTTOM) ||
                selected.getMargin(isRTL(), Component.RIGHT) != unselected.getMargin(isRTL(), Component.RIGHT) ||
                selected.getMargin(isRTL(), Component.LEFT) != unselected.getMargin(isRTL(), Component.LEFT)){
                trigger = true;
        }
        int prefW = 0;
        int prefH = 0;
        if(trigger){
            Dimension d = cmp.getPreferredSize();
            prefW = d.getWidth();
            prefH = d.getHeight();
        }            
        
        if (gained) {
            cmp.setFocus(true);
            cmp.fireFocusGained();
            fireFocusGained(cmp);
        } else {
            cmp.setFocus(false);
            cmp.fireFocusLost();
            fireFocusLost(cmp);
        }
        //if the styles are different there is a chance the preffered size is 
        //still the same therefore make sure there is a real need to preform 
        //a revalidate
        if(trigger){
            cmp.setShouldCalcPreferredSize(true);
            Dimension d = cmp.getPreferredSize();
            if(prefW != d.getWidth() || prefH != d.getHeight()){
                cmp.setShouldCalcPreferredSize(false);
                trigger = false;
            }
        }            

        return trigger;
    }
    
    /**
     * Find the first focusable Component
     * 
     * @param c a Container that holds potential Component
     * @return a focusable Component or null if not exists;
     */
    private Component findFirstFocusable(Container c) {
        int size = c.getComponentCount();

        for (int iter = 0; iter < size; iter++) {
            Component current = c.getComponentAt(iter);
            if(current.isFocusable()){
                return current;
            }
            if (current instanceof Container) {
                Component cmp = findFirstFocusable((Container)current);
                if(cmp != null){
                    return cmp;
                }
            }
        }        
        return null;
    }    
    
    /**
     * Returns the current focus component for this form
     * 
     * @return the current focus component for this form
     */
    public Component getFocused() {
        return focused;
    }

    /**
     * @inheritDoc
     */
    protected void longKeyPress(int keyCode) {
        if (focused != null) {
            if (focused.getComponentForm() == this) {
                focused.longKeyPress(keyCode);
            }
        }
    }

    /**
     * @inheritDoc
     */
    protected void longPointerPress(int x, int y){
        if (focused != null && focused.contains(x, y)) {
            if (focused.getComponentForm() == this) {
                focused.longPointerPress(x, y);
            }
        }
    }

    /**
     * @inheritDoc
     */
    public void keyPressed(int keyCode) {
        int game = Display.getInstance().getGameAction(keyCode);
        if (keyCode == leftSK || (keyCode == rightSK || keyCode == rightSK2) || keyCode == backSK || 
                (keyCode == clearSK && clearCommand != null) ||
                (keyCode == backspaceSK && clearCommand != null) ||
                (Display.getInstance().isThirdSoftButton() && game == Display.GAME_FIRE)) {
            menuBar.keyPressed(keyCode);
            return;
        }

        //Component focused = focusManager.getFocused();
        if (focused != null) {
            focused.keyPressed(keyCode);
            if(focused == null) {
                initFocused();
                return;
            }
            if (focused.handlesInput()) {
                return;
            }
            if (focused.getComponentForm() == this) {
                if (focused != null && focused.handlesInput()) {
                    return;
                }
                //if the arrow keys have been pressed update the focus.
                updateFocus(Display.getInstance().getGameAction(keyCode));
            } else {
                initFocused();
            }
        } else {
            initFocused();
        }

    }

    /**
     * @inheritDoc
     */
    public Layout getLayout() {
        return contentPane.getLayout();
    }

    /**
     * @inheritDoc
     */
    public void keyReleased(int keyCode) {
        int game = Display.getInstance().getGameAction(keyCode);
        if (keyCode == leftSK || (keyCode == rightSK || keyCode == rightSK2) || keyCode == backSK ||
                (keyCode == clearSK && clearCommand != null) ||
                (keyCode == backspaceSK && clearCommand != null) ||
                (Display.getInstance().isThirdSoftButton() && game == Display.GAME_FIRE)) {
            menuBar.keyReleased(keyCode);
            return;
        }

        //Component focused = focusManager.getFocused();
        if (focused != null) {
            if (focused.getComponentForm() == this) {
                focused.keyReleased(keyCode);
            }
        }

        // prevent the default action from stealing the behavior from the popup/combo box...
        if (game == Display.GAME_FIRE) {
            Command defaultCmd = getDefaultCommand();
            if (defaultCmd != null) {
                defaultCmd.actionPerformed(new ActionEvent(defaultCmd, keyCode));
                actionCommandImpl(defaultCmd);
            }
        }
        fireKeyEvent(keyListeners, keyCode);
        fireKeyEvent(gameKeyListeners, game);
    }

    private void fireKeyEvent(Hashtable keyListeners, int keyCode) {
        if (keyListeners != null) {
            Vector listeners = (Vector) keyListeners.get(new Integer(keyCode));
            if (listeners != null) {
                ActionEvent evt = new ActionEvent(this, keyCode);
                for (int iter = 0; iter < listeners.size(); iter++) {
                    ((ActionListener) listeners.elementAt(iter)).actionPerformed(evt);
                    if (evt.isConsumed()) {
                        return;
                    }
                }
            }
        }
    }

    /**
     * @inheritDoc
     */
    public void keyRepeated(int keyCode) {
        if (focused != null) {
            focused.keyRepeated(keyCode);

            int game = Display.getInstance().getGameAction(keyCode);
            // this has issues in the WTK
            if (!focused.handlesInput() && 
                    (game == Display.GAME_DOWN || game == Display.GAME_UP || game == Display.GAME_LEFT || game == Display.GAME_RIGHT)) {
                keyPressed(keyCode);
                keyReleased(keyCode);
            }
        } else {
            keyPressed(keyCode);
            keyReleased(keyCode);
        }
    }

    private void tactileTouchVibe(Component cmp) {
        if(tactileTouchDuration > 0 && cmp.isTactileTouch()) {
            Display.getInstance().vibrate(tactileTouchDuration);
        }
    }

    /**
     * @inheritDoc
     */
    public void pointerPressed(int x, int y) {
        if(pointerPressedListeners != null) {
            pointerPressedListeners.fireActionEvent(new ActionEvent(this, x, y));
        }
        //if there is no popup on the screen an click is relevant to the menu bar.
        if (menuBar.contains(x, y)) {
            Component cmp = menuBar.getComponentAt(x, y);
            if (cmp != null) {
                cmp.pointerPressed(x, y);
                tactileTouchVibe(cmp);
            }
            return;
        }

        Component cmp = contentPane.getComponentAt(x, y);
        if (cmp != null && cmp.isFocusable()) {
            setFocused(cmp);
            cmp.pointerPressed(x, y);
            cmp.repaint();
            tactileTouchVibe(cmp);
        }
    }

    /**
     * Adds a listener to the pointer event
     * 
     * @param l callback to receive pointer events
     */
    public void addPointerPressedListener(ActionListener l) {
        if(pointerPressedListeners == null) {
            pointerPressedListeners = new EventDispatcher();
        }
        pointerPressedListeners.addListener(l);
    }

    /**
     * Removes the listener from the pointer event
     *
     * @param l callback to remove
     */
    public void removePointerPressedListener(ActionListener l) {
        if(pointerPressedListeners != null) {
            pointerPressedListeners.removeListener(l);
        }
    }

    /**
     * Adds a listener to the pointer event
     *
     * @param l callback to receive pointer events
     */
    public void addPointerReleasedListener(ActionListener l) {
        if(pointerReleasedListeners == null) {
            pointerReleasedListeners = new EventDispatcher();
        }
        pointerReleasedListeners.addListener(l);
    }

    /**
     * Removes the listener from the pointer event
     *
     * @param l callback to remove
     */
    public void removePointerReleasedListener(ActionListener l) {
        if(pointerReleasedListeners != null) {
            pointerReleasedListeners.removeListener(l);
        }
    }

    /**
     * Adds a listener to the pointer event
     *
     * @param l callback to receive pointer events
     */
    public void addPointerDraggedListener(ActionListener l) {
        if(pointerDraggedListeners == null) {
            pointerDraggedListeners = new EventDispatcher();
        }
        pointerDraggedListeners.addListener(l);
    }

    /**
     * Removes the listener from the pointer event
     *
     * @param l callback to remove
     */
    public void removePointerDraggedListener(ActionListener l) {
        if(pointerDraggedListeners != null) {
            pointerDraggedListeners.removeListener(l);
        }
    }

    /**
     * @inheritDoc
     */
    public void pointerDragged(int x, int y) {
        if(pointerDraggedListeners != null) {
            pointerDraggedListeners.fireActionEvent(new ActionEvent(this, x, y));
        }

        if (dragged != null) {
            dragged.pointerDragged(x, y);
            return;
        }

        Component cmp = contentPane.getComponentAt(x, y);
        if (cmp != null) {
            if (cmp.isFocusable()) {
                setFocused(cmp);
            }
            cmp.pointerDragged(x, y);
            cmp.repaint();
        }
    }

    /**
     * @inheritDoc
     */
    public void pointerHoverReleased(int[] x, int[] y) {
        Component cmp = contentPane.getComponentAt(x[0], y[0]);
        if (cmp != null) {
            if (cmp.isFocusable()) {
                setFocused(cmp);
            }
            cmp.pointerHoverReleased(x, y);
            cmp.repaint();
        }
    }

    /**
     * @inheritDoc
     */
    public void pointerHover(int[] x, int[] y) {
        Component cmp = contentPane.getComponentAt(x[0], y[0]);
        if (cmp != null) {
            if (cmp.isFocusable()) {
                setFocused(cmp);
            }
            cmp.pointerHover(x, y);
            cmp.repaint();
        }
    }

    /**
     * Returns true if there is only one focusable member in this form. This is useful
     * so setHandlesInput would always be true for this case.
     * 
     * @return true if there is one focusable component in this form, false for 0 or more
     */
    public boolean isSingleFocusMode() {
        initFocusDown();
        return focusDownSequence.size() == 1;
    }

    /**
     * @inheritDoc
     */
    public void pointerReleased(int x, int y) {
        if(pointerReleasedListeners != null) {
            pointerReleasedListeners.fireActionEvent(new ActionEvent(this, x, y));
        }
        if (dragged == null) {
            //if the pointer was released on the menu invoke the appropriate
            //soft button.
            if (menuBar.contains(x, y)) {
                Component cmp = menuBar.getComponentAt(x, y);
                if (cmp != null) {
                    cmp.pointerReleased(x, y);
                }
                return;
            }

            Component cmp = contentPane.getComponentAt(x, y);
            if (cmp != null) {
                if (cmp.isFocusable()) {
                    setFocused(cmp);
                }

                cmp.pointerReleased(x, y);
                cmp.repaint();
            }
        } else {
            dragged.pointerReleased(x, y);
            dragged = null;
        }
    }

    /**
     * @inheritDoc
     */
    public void setScrollableY(boolean scrollableY) {
        getContentPane().setScrollableY(scrollableY);
    }

    /**
     * @inheritDoc
     */
    public void setScrollableX(boolean scrollableX) {
        getContentPane().setScrollableX(scrollableX);
    }

    /**
     * @inheritDoc
     */
    public int getComponentIndex(Component cmp) {
        return getContentPane().getComponentIndex(cmp);
    }

    /**
     * Adds a command to the menu bar softkeys or into the menu dialog, 
     * this version of add allows us to place a command in an arbitrary location.
     * This allows us to force a command into the softkeys when order of command
     * addition can't be changed.
     * 
     * @param cmd the Form command to be added
     * @param offset position in which the command is added
     */
    public void addCommand(Command cmd, int offset) {
        menuBar.addCommand(cmd, offset);
    }

    /**
     * A helper method to check the amount of commands within the form menu
     * 
     * @return the number of commands
     */
    public int getCommandCount() {
        return menuBar.getCommandCount();
    }

    /**
     * Returns the command occupying the given index
     * 
     * @param index offset of the command
     * @return the command at the given index
     */
    public Command getCommand(int index) {
        return menuBar.getCommand(index);
    }

    /**
     * Adds a command to the menu bar softkeys.
     * The Commands are placed in the order they are added.
     * If the Form has 1 Command it will be placed on the right.
     * If the Form has 2 Commands the first one that was added will be placed on
     * the right and the second one will be placed on the left.
     * If the Form has more then 2 Commands the first one will stay on the left
     * and a Menu will be added with all the remain Commands.
     * 
     * @param cmd the Form command to be added
     */
    public void addCommand(Command cmd) {
        menuBar.addCommand(cmd);
    }

    /**
     * Removes the command from the menu bar softkeys
     * 
     * @param cmd the Form command to be removed
     */
    public void removeCommand(Command cmd) {
        menuBar.removeCommand(cmd);
    }

    /**
     * Indicates whether focus should cycle within the form
     * 
     * @param cyclicFocus marks whether focus should cycle
     */
    public void setCyclicFocus(boolean cyclicFocus) {
        this.cyclicFocus = cyclicFocus;
    }

    /**
     * Indicates whether focus should cycle within the form
     * 
     * @return true if focus should cycle
     */
    public boolean isCyclicFocus() {
        return cyclicFocus;
    }

    private void updateFocus(int gameAction) {
        Component focused = getFocused();
        switch (gameAction) {
            case Display.GAME_DOWN: {
                Component down = focused.getNextFocusDown();
                if ( down != null && down.getComponentForm() == this) {
                    focused = down;
                } else {
                    initFocusDown();
                    int i = focusDownSequence.indexOf(focused) + 1;
                    if (focusDownSequence.size() > 0) {
                        if (i == focusDownSequence.size()) {
                            if (cyclicFocus) {
                                i = 0;
                            } else {
                                i = focusDownSequence.size() - 1;
                            }
                        }
                        focused = (Component) focusDownSequence.elementAt(i);
                    }
                }
                break;
            }
            case Display.GAME_UP: {
                Component up = focused.getNextFocusUp();
                if (up != null && up.getComponentForm() == this) {
                    focused = up;
                } else {
                    initFocusDown();
                    if (focusDownSequence.size() > 0) {
                        int i = focusDownSequence.indexOf(focused) - 1;
                        if (i < 0) {
                            if (cyclicFocus) {
                                i = focusDownSequence.size() - 1;
                            } else {
                                i = 0;
                            }
                        }
                        focused = (Component) focusDownSequence.elementAt(i);
                    }
                }
                break;
            }
            case Display.GAME_RIGHT: {
                Component right = focused.getNextFocusRight();
                if (right != null && right.getComponentForm() == this) {
                    focused = right;
                } else {
                    initFocusRight();
                    if (focusRightSequence.size() > 0) {
                        int i = focusRightSequence.indexOf(focused) + 1;
                        if (i == focusRightSequence.size()) {
                            if (cyclicFocus) {
                                i = 0;
                            } else {
                                i = focusRightSequence.size() - 1;
                            }
                        }
                        focused = (Component) focusRightSequence.elementAt(i);
                    }
                }
                break;
            }
            case Display.GAME_LEFT: {
                Component left = focused.getNextFocusLeft();
                if (left != null && left.getComponentForm() == this) {
                    focused = left;
                } else {
                    initFocusRight();
                    if (focusRightSequence.size() > 0) {
                        int i = focusRightSequence.indexOf(focused) - 1;
                        if (i < 0) {
                            if (cyclicFocus) {
                                i = focusRightSequence.size() - 1;
                            } else {
                                i = 0;
                            }
                        }
                        focused = (Component) focusRightSequence.elementAt(i);
                    }
                }
                break;
            }
            default:
                return;
        }
        boolean changeFocus = true;
        if(focused != null){
            changeFocus = moveScrollTowards(gameAction, focused);
        }
        //if focused is now visible we need to give it the focus.
        if(changeFocus){
            setFocused(focused);
        }
    }

    /**
     * @inheritDoc
     */
    boolean moveScrollTowards(int direction, Component c) {
        Container parent = c.getParent();
        while (parent != null) {
            if (parent.isScrollable()) {
                return parent.moveScrollTowards(direction, c);
            }
            parent = parent.getParent();
        }
        return true;
    }

    /**
     * Makes sure the component is visible in the scroll if this container 
     * is scrollable
     * 
     * @param c the componant to be visible
     */
    public void scrollComponentToVisible(Component c) {
        initFocused();
        Container parent = c.getParent();
        while (parent != null) {
            if (parent.isScrollable()) {
                parent.scrollComponentToVisible(c);
                return;
            }
            parent = parent.getParent();
        }
    }

    /**
     * Determine the cell renderer used to render menu elements for themeing the 
     * look of the menu options
     * 
     * @param menuCellRenderer the menu cell renderer
     */
    public void setMenuCellRenderer(ListCellRenderer menuCellRenderer) {
        menuBar.setMenuCellRenderer(menuCellRenderer);
    }

    /**
     * Clear menu commands from the menu bar
     */
    public void removeAllCommands() {
        menuBar.removeAllCommands();
    }

    /**
     * Request focus for a form child component
     * 
     * @param cmp the form child component
     */
    void requestFocus(Component cmp) {
        if (cmp.isFocusable() && contains(cmp)) {
            scrollComponentToVisible(cmp);
            setFocused(cmp);
        }
    }

    /**
     * Factory method that returns the Form select Command.
     * This Command is used when Display.getInstance().isThirdSoftButton() 
     * returns true.
     * This method can be overridden to customize the Command on the Form.
     * 
     * @return Command
     */
    protected Command createSelectCommand(){
        return new Command(UIManager.getInstance().localize("select", "Select"));
    }

    /**
     * Factory method that returns the Form Menu select Command.
     * This method can be overridden to customize the Command on the Form.
     * 
     * @return Command
     */
    protected Command createMenuSelectCommand(){
        LookAndFeel lf = UIManager.getInstance().getLookAndFeel();
        return new Command(UIManager.getInstance().localize("select", "Select"), lf.getMenuIcons()[0]);
    }
    
    /**
     * @inheritDoc
     */
    public void setRTL(boolean r) {
        super.setRTL(r);
        contentPane.setRTL(r);
    }

    /**
     * Factory method that returns the Form Menu cancel Command.
     * This method can be overridden to customize the Command on the Form.
     * 
     * @return Command
     */
    protected Command createMenuCancelCommand(){
        LookAndFeel lf = UIManager.getInstance().getLookAndFeel();
        return new Command(UIManager.getInstance().localize("cancel", "Cancel"), lf.getMenuIcons()[1]);
    }
    
    /**
     * @inheritDoc
     */
    public void paint(Graphics g) {
        paintBackground(g);
        super.paint(g);
        if (tint) {
            g.setColor(tintColor);
            g.fillRect(0, 0, getWidth(), getHeight(), (byte) ((tintColor >> 24) & 0xff));
        }
    }

    /**
     * @inheritDoc
     */
    public void setScrollable(boolean scrollable) {
        contentPane.setScrollable(scrollable);
    }

    /**
     * @inheritDoc
     */
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (mediaComponents != null) {
            int size = mediaComponents.size();
            for (int i = 0; i < size; i++) {
                Component mediaCmp = (Component) mediaComponents.elementAt(i);
                mediaCmp.setVisible(visible);
            }
        }
    }

    /**
     * Default color for the screen tint when a dialog or a menu is shown
     * 
     * @return the tint color when a dialog or a menu is shown
     */
    public int getTintColor() {
        return tintColor;
    }

    /**
     * Default color for the screen tint when a dialog or a menu is shown
     * 
     * @param tintColor the tint color when a dialog or a menu is shown
     */
    public void setTintColor(int tintColor) {
        this.tintColor = tintColor;
    }

    void addSelectCommand(String selectText) {
        if (Display.getInstance().isThirdSoftButton()) {
            if (selectCommand == null) {
                selectCommand = createSelectCommand();
            }
            selectCommand.setCommandName(selectText);
            addCommand(selectCommand);
        }
    }

    void removeSelectCommand() {
        if (Display.getInstance().isThirdSoftButton()) {
            removeCommand(selectCommand);
        }
    }

    /**
     * Sets the menu transitions for showing/hiding the menu, can be null...
     * 
     * @param transitionIn the transition that will play when the menu appears
     * @param transitionOut the transition that will play when the menu is folded
     */
    public void setMenuTransitions(Transition transitionIn, Transition transitionOut) {
        menuBar.setTransitions(transitionIn, transitionOut);
    }

    /**
     * @inheritDoc
     */
    protected String paramString() {
        return super.paramString() + ", title = " + title +
                ", visible = " + isVisible();
    }

    /**
     * A menu is implemented as a dialog, this method allows you to override dialog
     * display in order to customize the dialog menu in various ways
     * 
     * @param menu a dialog containing menu options that can be customized
     * @return the command selected by the user in the dialog (not menu) Select or Cancel
     */
    protected Command showMenuDialog(Dialog menu) {
        int marginLeft = (int) (Form.this.getWidth() * 0.25f);
        int marginRight = 0;
        boolean rtl = isRTL();

        if (isReverseSoftButtons()) {
            marginRight = marginLeft;
            marginLeft = 0;
        }
        int height = Form.this.getHeight() / 2;
        if(UIManager.getInstance().getLookAndFeel().isTouchMenus()) {
            marginLeft = 0;
            marginRight = 0;
        }
        return menu.show(height, 0, marginLeft, marginRight, true);
    }

    /**
     * Allows an individual form to reverse the layout direction of the softbuttons, this method is RTL
     * sensitive and might reverse the result based on RTL state
     * 
     * @return The value of UIManager.getInstance().getLookAndFeel().isReverseSoftButtons()
     */
    protected boolean isReverseSoftButtons() {
        LookAndFeel lf = UIManager.getInstance().getLookAndFeel();
        if(isRTL()) {
            return !lf.isReverseSoftButtons();
        }
        return lf.isReverseSoftButtons();
    }

    /**
     * Calculates the amount of columns to give to the touch commands within the grid
     * 
     * @param grid container that will be arranged in the grid containing the components
     * @return an integer representing the touch command grid size
     */
    protected int calculateTouchCommandGridColumns(Container grid) {
        int count = grid.getComponentCount();
        int maxWidth = 0;
        for(int iter = 0 ; iter < count ; iter++) {
            Component c = grid.getComponentAt(iter);
            Style s = c.getUnselectedStyle();
            
            // bidi doesn't matter since this is just a summary of width
            maxWidth = Math.max(maxWidth, c.getPreferredW() + s.getPadding(false, LEFT) + s.getPadding(false, RIGHT) +
                     s.getMargin(false, LEFT) + s.getMargin(false, RIGHT));
        }
        return Math.max(2, getWidth() / maxWidth);
    }

    /**
     * Creates a touch command for use as a touch menu item
     * 
     * @param c command to map into the returned button
     * @return a button that would fire the touch command appropriately
     */
    protected Button createTouchCommandButton(Command c) {
        Button b = new Button(c);
        b.setTactileTouch(true);
        b.setTextPosition(Label.BOTTOM);
        b.setAlignment(CENTER);
        b.setUIID("TouchCommand");
        return b;
    }

    /**
     * Creates the component containing the commands within the given vector
     * used for showing the menu dialog, this method calls the createCommandList
     * method by default however it allows more elaborate menu creation.
     *
     * @param commands list of command objects
     * @return Component that will result in the parent menu dialog recieving a command event
     */
    protected Component createCommandComponent(Vector commands) {
        // Create a touch based menu interface
        if(UIManager.getInstance().getLookAndFeel().isTouchMenus()) {
            Container menu = new Container();
            for(int iter = 0 ; iter < commands.size() ; iter++) {
                Command c = (Command)commands.elementAt(iter);
                menu.addComponent(createTouchCommandButton(c));
            }
            int cols = calculateTouchCommandGridColumns(menu);
            menu.setLayout(new GridLayout(Math.max(1, commands.size() / cols), cols));
            return menu;
        }
        return createCommandList(commands);
    }

    /**
     * Creates the list component containing the commands within the given vector
     * used for showing the menu dialog
     * 
     * @param commands list of command objects
     * @return List object
     */
    protected List createCommandList(Vector commands) {
        List l = new List(commands);
        l.setUIID("CommandList");
        Component c = (Component) l.getRenderer();
        c.setUIID("Command");
        c = l.getRenderer().getListFocusComponent(l);
        c.setUIID("CommandFocus");

        l.setFixedSelection(List.FIXED_NONE_CYCLIC);
        return l;
    }

    Command getComponentSelectedCommand(Component cmp) {
        if(cmp instanceof List) {
            List l = (List)cmp;
            return (Command) l.getSelectedItem();
        } else {
            cmp = cmp.getComponentForm().getFocused();
            if(cmp instanceof Button) {
                return ((Button)cmp).getCommand();
            }
        }
        // nothing to do for this case...
        return null;
    }

    MenuBar getMenuBar() {
        return menuBar;
    }
    
    class MenuBar extends Container implements ActionListener {

        private Command menuCommand;
        private Vector commands = new Vector();
        private Button[] soft;
        private Command[] softCommand;
        private Button left;
        private Button right;
        private Button main;
        private ListCellRenderer menuCellRenderer;
        private Transition transitionIn;
        private Transition transitionOut;
        private Component commandList;
        private Style menuStyle;
        private int topMargin, bottomMargin;
        
        public MenuBar() {
            LookAndFeel lf = UIManager.getInstance().getLookAndFeel();
            menuStyle = UIManager.getInstance().getComponentStyle("Menu");
            setUnSelectedStyle(UIManager.getInstance().getComponentStyle("SoftButton"));
            menuCommand = new Command(UIManager.getInstance().localize("menu", "Menu"), lf.getMenuIcons()[2]);
            // use the slide transition by default
            if (lf.getDefaultMenuTransitionIn() != null || lf.getDefaultMenuTransitionOut() != null) {
                transitionIn = lf.getDefaultMenuTransitionIn();
                transitionOut = lf.getDefaultMenuTransitionOut();
            } else {
                transitionIn = CommonTransitions.createSlide(CommonTransitions.SLIDE_VERTICAL, true, 300, true);
                transitionOut = CommonTransitions.createSlide(CommonTransitions.SLIDE_VERTICAL, false, 300, true);
            }
            menuCellRenderer = lf.getMenuRenderer();

            if (Display.getInstance().getImplementation().getSoftkeyCount() > 1) {
                if (Display.getInstance().isThirdSoftButton()) {
                    setLayout(new GridLayout(1, 3));
                    soft = new Button[]{createSoftButton(), createSoftButton(), createSoftButton()};
                    main = soft[0];
                    main.setAlignment(Label.CENTER);
                    left = soft[1];
                    right = soft[2];
                    if (Form.this.isRTL()) {
                    	addComponent(right);
                    	addComponent(main);
                    	addComponent(left);
                    } else {
                    	addComponent(left);
                    	addComponent(main);
                    	addComponent(right);
                    }
                    if (isReverseSoftButtons()) {
                        Button b = soft[1];
                        soft[1] = soft[2];
                        soft[2] = b;
                    }
                } else {
                    setLayout(new GridLayout(1, 2));
                    soft = new Button[]{createSoftButton(), createSoftButton()};
                    main = soft[0];
                    left = soft[0];
                    right = soft[1];
                   	if (Form.this.isRTL()) {
                   		addComponent(right);
                   		addComponent(left);
                   	} else {
                   		addComponent(left);
                   		addComponent(right);
                   	}
                    if (isReverseSoftButtons()) {
                        Button b = soft[0];
                        soft[0] = soft[1];
                        soft[1] = b;
                    }
                }
                // It doesn't make sense for softbuttons to have ... at the end
                for(int iter = 0 ; iter < soft.length ; iter++) {
                    soft[iter].setEndsWith3Points(false);
                }
            } else {
                // special case for touch screens we still want the 3 softbutton areas...
                if(Display.getInstance().isThirdSoftButton()) {
                    setLayout(new GridLayout(1, 3));
                    soft = new Button[]{createSoftButton(), createSoftButton(), createSoftButton()};
                    main = soft[0];
                    main.setAlignment(Label.CENTER);
                    left = soft[1];
                    right = soft[2];
                    addComponent(left);
                    addComponent(main);
                    addComponent(right);
                    if (isReverseSoftButtons()) {
                        Button b = soft[1];
                        soft[1] = soft[2];
                        soft[2] = b;
                    }
                } else {
                    soft = new Button[]{createSoftButton()};
                }
            }
            if (left != null) {
                if (Form.this.isRTL()) {
                	left.setAlignment(Label.RIGHT);
	                right.setAlignment(Label.LEFT);
                } else {
	            	left.setAlignment(Label.LEFT);
	                right.setAlignment(Label.RIGHT);
                }
            }

            softCommand = new Command[soft.length];
        }

        /**
         * Updates the command mapping to the softbuttons
         */
        private void updateCommands() {
            if (soft.length > 1) {
                soft[0].setText("");
                soft[1].setText("");
                soft[0].setIcon(null);
                soft[1].setIcon(null);
                int commandSize = commands.size();
                if (soft.length > 2) {
                    soft[2].setText("");
                    if (commandSize > 2) {
                        if (commandSize > 3) {
                            softCommand[2] = menuCommand;
                        } else {
                            softCommand[2] = (Command) commands.elementAt(commands.size() - 3);
                        }
                        soft[2].setText(softCommand[2].getCommandName());
                        soft[2].setIcon(softCommand[2].getIcon());
                    } else {
                        softCommand[2] = null;
                    }
                }
                if (commandSize > 0) {
                    softCommand[0] = (Command) commands.elementAt(commands.size() - 1);
                    soft[0].setText(softCommand[0].getCommandName());
                    soft[0].setIcon(softCommand[0].getIcon());
                    if (commandSize > 1) {
                        if (soft.length == 2 && commandSize > 2) {
                            softCommand[1] = menuCommand;
                        } else {
                            softCommand[1] = (Command) commands.elementAt(commands.size() - 2);
                        }
                        soft[1].setText(softCommand[1].getCommandName());
                        soft[1].setIcon(softCommand[1].getIcon());
                    } else {
                        softCommand[1] = null;
                    }
                } else {
                    softCommand[0] = null;
                    softCommand[1] = null;
                }

                // we need to add the menu bar to an already visible form
                if (commandSize == 1) {
                    if (Form.this.isVisible()) {
                        Form.this.revalidate();
                    }
                }
                repaint();
            }
        }

        /**
         * Invoked when a softbutton is pressed
         */
        public void actionPerformed(ActionEvent evt) {
            if (evt.isConsumed()) {
                return;
            }
            Object src = evt.getSource();
            if (commandList == null) {
                Button source = (Button) src;
                for (int iter = 0; iter < soft.length; iter++) {
                    if (source == soft[iter]) {
                        if (softCommand[iter] == menuCommand) {
                            showMenu();
                            return;
                        }
                        if (softCommand[iter] != null) {
                            ActionEvent e = new ActionEvent(softCommand[iter]);
                            softCommand[iter].actionPerformed(e);
                            if (!e.isConsumed()) {
                                actionCommandImpl(softCommand[iter]);
                            }
                        }
                        return;
                    }
                }
            } else {
                // the list for the menu sent the event
                if (src instanceof Button) {
                    for (int iter = 0; iter < soft.length; iter++) {
                        if (src == soft[iter]) {
                            Container parent = commandList.getParent();
                            while (parent != null) {
                                if (parent instanceof Dialog) {
                                    ((Dialog) parent).actionCommand(softCommand[iter]);
                                    return;
                                }
                                parent = parent.getParent();
                            }
                        }
                    }
                }
                Command c = getComponentSelectedCommand(commandList);
                Container parent = commandList.getParent();
                while (parent != null) {
                    if (parent instanceof Dialog) {
                        ((Dialog) parent).actionCommand(c);
                        return;
                    }
                    parent = parent.getParent();
                }
            }

        }

        private Button createSoftButton() {
            Button b = new Button();
            b.addActionListener(this);
            b.setFocusPainted(false);
            b.setFocusable(false);
            b.setTactileTouch(true);
            updateSoftButtonStyle(b);
            return b;
        }

        private void updateSoftButtonStyle(Button b) {
            Style s = new Style(getUnselectedStyle());
            b.setUnSelectedStyle(s);
            b.setPressedStyle(s);
            s.setBgImage(null);
            s.setBorder(null);
            s.setBackgroundType(Style.BACKGROUND_IMAGE_SCALED);
            s.setMargin(2, 2, 2, 2);
            s.setMargin(topMargin, bottomMargin, 2, 2);
            s.setBgTransparency(0);
            
        }

        public void setUnSelectedStyle(Style style) {
            topMargin = style.getMargin(false, Component.TOP);
            bottomMargin = style.getMargin(false, Component.BOTTOM);
            style.setMargin(Component.TOP, 0, true);
            style.setMargin(Component.BOTTOM, 0, true);
            super.setUnSelectedStyle(style);
            if(soft != null){
                for (int iter = 0; iter < soft.length; iter++) {
                    updateSoftButtonStyle(soft[iter]);
                }
            }
        }
        
        /**
         * Prevents scaling down of the menu when there is no text on the menu bar 
         */
        protected Dimension calcPreferredSize() {
            if (soft.length > 1) {
                Dimension d = super.calcPreferredSize();
                if ((soft[0].getText() == null || soft[0].getText().equals("")) &&
                        (soft[1].getText() == null || soft[1].getText().equals("")) &&
                        soft[0].getIcon() == null && soft[1].getIcon() == null) {
                    d.setHeight(0);
                }
                return d;
            }
            return super.calcPreferredSize();
        }

        /**
         * Sets the menu transitions for showing/hiding the menu, can be null...
         */
        public void setTransitions(Transition transitionIn, Transition transitionOut) {
            this.transitionIn = transitionIn;
            this.transitionOut = transitionOut;
        }

        private void showMenu() {
            final Dialog d = new Dialog();
            d.setDisposeWhenPointerOutOfBounds(true);
            d.setDialogStyle(new Style(menuStyle));
            d.setMenu(true);
            d.setSoftButtonStyle(new Style(getSoftButtonStyle()));
            menuStyle.removeStyleListener(d.getContentPane());

            d.setTransitionInAnimator(transitionIn);
            d.setTransitionOutAnimator(transitionOut);
            d.setLayout(new BorderLayout());
            d.setScrollable(false);
            ((Form) d).menuBar.commandList = createCommandComponent(commands);
            if (menuCellRenderer != null && ((Form) d).menuBar.commandList instanceof List) {
                ((List)((Form) d).menuBar.commandList).setListCellRenderer(menuCellRenderer);
            }
            d.getContentPane().getStyle().setMargin(0, 0, 0, 0);
            d.addComponent(BorderLayout.CENTER, ((Form) d).menuBar.commandList);
            if (Display.getInstance().isThirdSoftButton()) {
                d.addCommand(selectMenuItem);
                d.addCommand(cancelMenuItem);
            } else {
                d.addCommand(cancelMenuItem);
                if(soft.length > 1) {
                    d.addCommand(selectMenuItem);
                }
            }
            d.setClearCommand(cancelMenuItem);
            d.setBackCommand(cancelMenuItem);

            if(((Form) d).menuBar.commandList instanceof List) {
                ((List)((Form) d).menuBar.commandList).addActionListener(((Form) d).menuBar);
            }
            Command result = showMenuDialog(d);
            if (result != cancelMenuItem) {
                Command c = null;
                if (result == selectMenuItem) {
                    c = getComponentSelectedCommand(((Form) d).menuBar.commandList);
                    if (c != null) {
                        ActionEvent e = new ActionEvent(c);
                        c.actionPerformed(e);
                    }
                } else {
                    c = result;
                    // a touch menu will always send its commands on its own...
                    if(!UIManager.getInstance().getLookAndFeel().isTouchMenus()) {
                        c = result;
                        if (c != null) {
                            ActionEvent e = new ActionEvent(c);
                            c.actionPerformed(e);
                        }
                    }
                }
                // menu item was handled internally in a touch interface that is not a touch menu
                if (c != null) {
                    actionCommandImpl(c);
                }
            }
            if(((Form) d).menuBar.commandList instanceof List) {
                ((List)((Form) d).menuBar.commandList).removeActionListener(((Form) d).menuBar);
            }
            
            Form upcoming = Display.getInstance().getCurrentUpcoming();
            if (upcoming == Form.this) {
                d.disposeImpl();
            } else {
                Form.this.tint = (upcoming instanceof Dialog);
            }
        }

        public Button[] getSoftButtons() {
            return soft;
        }

        public String getUIID() {
            return "SoftButton";
        }

        public void addCommand(Command cmd) {
            // prevent duplicate commands which might happen in some edge cases
            // with the select command
            if (commands.contains(cmd)) {
                return;
            }
            // special case for default commands which are placed at the end and aren't overriden later
            if (soft.length > 2 && cmd == getDefaultCommand()) {
                commands.addElement(cmd);
            } else {
                commands.insertElementAt(cmd, 0);
            }
            updateCommands();
        }

        /**
         * Returns the command occupying the given index
         * 
         * @param index offset of the command
         * @return the command at the given index
         */
        public Command getCommand(int index) {
            return (Command) commands.elementAt(index);
        }

        public int getCommandCount() {
            return commands.size();
        }

        public void addCommand(Command cmd, int index) {
            // prevent duplicate commands which might happen in some edge cases
            // with the select command
            if (commands.contains(cmd)) {
                return;
            }
            commands.insertElementAt(cmd, index);
            updateCommands();
        }

        public void removeAllCommands() {
            commands.removeAllElements();
            updateCommands();
        }

        public void removeCommand(Command cmd) {
            commands.removeElement(cmd);
            updateCommands();
        }

        public void setMenuCellRenderer(ListCellRenderer menuCellRenderer) {
            this.menuCellRenderer = menuCellRenderer;
        }

        public Style getMenuStyle() {
            return menuStyle;
        }

        public void keyPressed(int keyCode) {
            if (commands.size() > 0) {
                if (keyCode == leftSK) {
                    if (left != null) {
                        left.pressed();
                    }
                } else {
                    // it might be a back command or the fire...
                    if ((keyCode == rightSK || keyCode == rightSK2)) {
                        if (right != null) {
                            right.pressed();
                        }
                    } else {
                        if (Display.getInstance().getGameAction(keyCode) == Display.GAME_FIRE) {
                            main.pressed();
                        }
                    }
                }
            }
        }

        public void keyReleased(int keyCode) {
            if (commands.size() > 0) {
                if (Display.getInstance().getImplementation().getSoftkeyCount() < 2 && keyCode == leftSK) {
                    if (commandList != null) {
                        Container parent = commandList.getParent();
                        while (parent != null) {
                            if (parent instanceof Dialog && ((Dialog) parent).isMenu()) {
                                return;
                            }
                            parent = parent.getParent();
                        }
                    }
                    showMenu();
                    return;
                } else {
                    if (keyCode == leftSK) {
                        if (left != null) {
                            left.released();
                        }
                        return;
                    } else {
                        // it might be a back command...
                        if ((keyCode == rightSK || keyCode == rightSK2)) {
                            if (right != null) {
                                right.released();
                            }
                            return;
                        } else {
                            if (Display.getInstance().getGameAction(keyCode) == Display.GAME_FIRE) {
                                main.released();
                                return;
                            }
                        }
                    }
                }
            }

            // allows a back/clear command to occur regardless of whether the
            // command was added to the form
            Command c = null;
            if (keyCode == backSK) {
                // the back command should be invoked
                c = getBackCommand();
            } else {
                if (keyCode == clearSK || keyCode == backspaceSK) {
                    c = getClearCommand();
                }
            }
            if (c != null) {
                ActionEvent ev = new ActionEvent(c, keyCode);
                c.actionPerformed(ev);
                if(!ev.isConsumed()) {
                    actionCommandImpl(c);
                }
            }
        }

        public void refreshTheme() {
            super.refreshTheme();
            if (menuStyle.isModified()) {
                menuStyle.merge(UIManager.getInstance().getComponentStyle("Menu"));
            } else {
                menuStyle = UIManager.getInstance().getComponentStyle("Menu");
            }
            if (menuCellRenderer != null) {
                List tmp = new List();
                tmp.setListCellRenderer(menuCellRenderer);
                tmp.refreshTheme();
            }
            for (int iter = 0; iter < soft.length; iter++) {
                updateSoftButtonStyle(soft[iter]);
            }
            
            revalidate();
        }
    }
}
