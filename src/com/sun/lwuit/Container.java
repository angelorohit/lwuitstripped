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
import com.sun.lwuit.animations.Transition;
import com.sun.lwuit.layouts.BorderLayout;
import com.sun.lwuit.layouts.FlowLayout;
import com.sun.lwuit.layouts.Layout;
import com.sun.lwuit.plaf.UIManager;
import com.sun.lwuit.geom.Dimension;
import com.sun.lwuit.geom.Rectangle;
import com.sun.lwuit.plaf.LookAndFeel;
import java.util.Vector;

/**
 * A composite pattern with {@link Component}, allows nesting and arranging multiple
 * components using a pluggable layout manager architecture. Containers can be nested
 * one within the other to form elaborate UI's.
 *
 * @see com.sun.lwuit.layouts
 * @see Component
 * @author Chen Fishbein
 */
public class Container extends Component {

    private Layout layout;
    private java.util.Vector components = new java.util.Vector();
    private boolean shouldLayout = true;
    private boolean scrollableX;
    private boolean scrollableY;
    private java.util.Vector cmpTransitions;
    private int scrollIncrement = 20;
    
    /**
     * Constructs a new Container with a new layout manager.
     * 
     * @param layout the specified layout manager
     */
    public Container(Layout layout) {
        super();
        setUIID("Container");
        this.layout = layout;
        setFocusable(false);
        LookAndFeel laf = UIManager.getInstance().getLookAndFeel();
        setSmoothScrolling(laf.isDefaultSmoothScrolling());
    }

    /** 
     * Constructs a new Container, with a {@link FlowLayout}. 
     */
    public Container() {
        this(new FlowLayout());
    }

    /**
     * Returns the layout manager responsible for arranging this container
     * 
     * @return the container layout manager
     */
    public Layout getLayout() {
        return layout;
    }

    /**
     * Sets the layout manager responsible for arranging this container
     * 
     * @param layout the specified layout manager
     */
    public void setLayout(Layout layout) {
        this.layout = layout;
    }

    /**
     * Same as setShouldCalcPreferredSize(true) but made accessible for 
     * layout managers
     */
    public void invalidate() {
        setShouldCalcPreferredSize(true);
    }

    /**
     * @inheritDoc
     */
    protected void setShouldCalcPreferredSize(boolean shouldCalcPreferredSize) {
        super.setShouldCalcPreferredSize(shouldCalcPreferredSize);
        shouldLayout = shouldCalcPreferredSize;
        if (shouldLayout) {
            int size = components.size();
            for(int iter = 0 ; iter < size ; iter++) {
                Component cmp = (Component) components.elementAt(iter);
                if (cmp instanceof Container) {
                    ((Container) cmp).setShouldCalcPreferredSize(shouldCalcPreferredSize);
                }
            }
        }
        Form f = getComponentForm();
        if (f != null) {
            f.clearFocusVectors();
        }
    }

    /**
     * Returns the width for layout manager purposes, this takes scrolling
     * into consideration unlike the getWidth method.
     * 
     * @return the layout width
     */
    public int getLayoutWidth() {
        if (isScrollableX()) {
            return Math.max(getWidth(), getPreferredW());
        } else {
            Container parent = getScrollableParent();
            if (parent != null && parent.isScrollableX()) {
                return Math.max(getWidth(), getPreferredW());
            }
            int width = getWidth();
            if (width <= 0) {
                return getPreferredW();
            }
            return width;
        }
    }

    /**
     * Invokes apply/setRTL recursively on all the children components of this container
     * 
     * @param rtl right to left bidi indication
     * @see Component#setRTL(boolean) 
     */
    public void applyRTL(boolean rtl) {
        setRTL(rtl);
        int c = getComponentCount();
        for(int iter = 0 ; iter < c ; iter++) {
            Component current = getComponentAt(iter);
            if(current instanceof Container) {
                ((Container)current).applyRTL(rtl);
            } else {
                current.setRTL(rtl);
            }
        }
    }

    /**
     * Returns the height for layout manager purposes, this takes scrolling
     * into consideration unlike the getWidth method.
     * 
     * @return the layout height
     */
    public int getLayoutHeight() {
        if (isScrollableY()) {
            return Math.max(getHeight(), getPreferredH());
        } else {
            Container parent = getScrollableParent();
            if (parent != null && parent.isScrollableY()) {
                return Math.max(getHeight(), getPreferredH());
            }
            int height = getHeight();
            if (height <= 1) {
                return getPreferredH();
            }
            return height;
        }
    }

    /**
     * Returns a parent container that is scrollable or null if no parent is 
     * scrollable.
     * 
     * @return a parent container that is scrollable or null if no parent is 
     * scrollable.
     */
    private Container getScrollableParent() {
        Container parent = getParent();
        while (parent != null) {
            if (parent.isScrollable()) {
                return parent;
            }
            parent = parent.getParent();
        }
        return null;
    }

    /**
     * Adds a Component to the Container
     * 
     * @param cmp the component to be added
     */
    public void addComponent(Component cmp) {
        layout.addLayoutComponent(null, cmp, this);
        insertComponentAt(components.size(), cmp);
    }

    /**
     * Adds a Component to the Container
     * 
     * @param constraints this method is useful when the Layout requires a constraint
     * such as the BorderLayout.
     * In this case you need to specify an additional data when you add a Component,
     * such as "CENTER", "NORTH"...
     *
     * @param cmp component to add
     */
    public void addComponent(Object constraints, Component cmp) {
        layout.addLayoutComponent(constraints, cmp, this);
        insertComponentAt(components.size(), cmp);
    }


    /**
     * Adds a Component to the Container
     * 
     * @param index location to insert the Component
     * @param constraints this method is useful when the Layout requires a constraint
     * such as the BorderLayout.
     * In this case you need to specify an additional data when you add a Component,
     * such as "CENTER", "NORTH"...
     * @param cmp component to add
     */
    public void addComponent(int index, Object constraints, Component cmp) {
        layout.addLayoutComponent(constraints, cmp, this);
        insertComponentAt(index, cmp);
    }

    private void insertComponentAt(int index, Component cmp) {
        if (cmp.getParent() != null) {
            throw new IllegalArgumentException("Component is already contained in Container: " + cmp.getParent());
        }
        if (cmp instanceof Form) {
            throw new IllegalArgumentException("A form cannot be added to a container");
        }
        cmp.setParent(this);
        components.insertElementAt(cmp, index);
        setShouldCalcPreferredSize(true);
        if (isInitialized()) {
            cmp.initComponentImpl();
        }
        Form f = getComponentForm();
        if (f != null) {
            f.clearFocusVectors();
        }
    //repaint();
    }

    /**
     * This method adds the Component at a specific index location in the Container
     * Components array.
     * 
     * @param index location to insert the Component
     * @param cmp the Component to add
     * @throws ArrayIndexOutOfBoundsException if index is out of bounds
     * @throws IllegalArgumentException if Component is already contained or
     * the cmp is a Form Component
     */
    public void addComponent(int index, Component cmp) {
        insertComponentAt(index, cmp);
    }

    /**
     * This method replaces the current Component with the next Component.
     * Current Component must be contained in this Container.
     * This method returns when transition has finished.
     * 
     * @param current a Component to remove from the Container
     * @param next a Component that replaces the current Component
     * @param t a Transition between the add and removal of the Components
     *  a Transition can be null
     */
    public void replaceAndWait(final Component current, final Component next, final Transition t) {
        replaceComponents(current, next, t, true);
    }

    /**
     * This method replaces the current Component with the next Component.
     * Current Component must be contained in this Container.
     * This method return immediately.
     * 
     * @param current a Component to remove from the Container
     * @param next a Component that replaces the current Component
     * @param t a Transition between the add and removal of the Components
     *  a Transition can be null
     */
    public void replace(final Component current, final Component next, final Transition t) {
        replaceComponents(current, next, t, false);
    }

    private void replaceComponents(final Component current, final Component next, final Transition t, boolean wait) {
        if (!contains(current)) {
            throw new IllegalArgumentException("Component " + current + " is not contained in this Container");
        }
        if (t == null || !isVisible() || getComponentForm() == null) {
            replace(current, next);
            return;
        }

        next.setX(current.getX());
        next.setY(current.getY());
        next.setWidth(current.getWidth());
        next.setHeight(current.getHeight());
        next.setParent(this);
        if (next instanceof Container) {
            ((Container) next).layoutContainer();
        }

        final Anim anim = new Anim(this, current, next, t);

        // register the transition animation
        getComponentForm().registerAnimatedInternal(anim);
        //wait until animation has finished
        if (wait) {
            Display.getInstance().invokeAndBlock(anim);
        }
    }

    private boolean isParentOf(Component c) {
        c = c.getParent();
        if (c == null || c instanceof Form) {
            return false;
        }
        return (c == this) || isParentOf(c);
    }

    private boolean requestFocusChild() {
        for (int iter = 0; iter < getComponentCount(); iter++) {
            Component c = getComponentAt(iter);
            if (c.isFocusable()) {
                c.requestFocus();
                return true;
            }
            if (c instanceof Container && ((Container) c).requestFocusChild()) {
                return true;
            }
        }
        return false;
    }

    void replace(final Component current, final Component next) {
        int index = components.indexOf(current);
        boolean currentFocused = false;
        if (current.getComponentForm() != null) {
            Component currentF = current.getComponentForm().getFocused();
            currentFocused = currentF == current;
            if (!currentFocused && current instanceof Container && currentF != null && ((Container) current).isParentOf(currentF)) {
                currentFocused = true;
            }
        }
        if (layout instanceof BorderLayout) {
            Object constraint = layout.getComponentConstraint(current);
            removeComponent(current);
            layout.addLayoutComponent(constraint, next, Container.this);
        } else {
            removeComponent(current);
        }
        next.setParent(null);
        if (index < 0) {
            index = 0;
        }
        insertComponentAt(index, next);
        if (currentFocused) {
            if (next.isFocusable()) {
                next.requestFocus();
            } else {
                if (next instanceof Container) {
                    ((Container) next).requestFocusChild();
                }
            }
        }
    }

    /**
     * @inheritDoc
     */
    void initComponentImpl() {
        if (!isInitialized()) {
            super.initComponentImpl();
        }
        int size = components.size();
        for(int iter = 0 ; iter < size ; iter++) {
            ((Component) components.elementAt(iter)).initComponentImpl();
        }
    }

    /**
     * removes a Component from the Container
     * 
     * @param cmp the removed component
     */
    public void removeComponent(Component cmp) {
        Form parentForm = cmp.getComponentForm();
        layout.removeLayoutComponent(cmp);
        cmp.deinitializeImpl();
        components.removeElement(cmp);
        cmp.setParent(null);
        cmp.setShouldCalcPreferredSize(true);
        if (parentForm != null) {
            if (parentForm.getFocused() == cmp || cmp instanceof Container && ((Container) cmp).contains(parentForm.getFocused())) {
                parentForm.setFocused(null);
            }
            parentForm.clearFocusVectors();
            if (cmp.isSmoothScrolling()) {
                parentForm.deregisterAnimatedInternal(cmp);
            }
        }
        setShouldCalcPreferredSize(true);
    }

    /**
     * Cleansup the initialization flags in the hierachy
     */
    void deinitializeImpl() {
        super.deinitializeImpl();
        int size = components.size();
        for (int iter = 0; iter < size; iter++) {
            ((Component) components.elementAt(iter)).deinitializeImpl();
        }
        flushReplace();
    }

    /**
     * Flushes ongoing replace operations to prevent two concurrent replace operations from colliding.
     * If there is no ongoing replace nothing will occur
     */
    public void flushReplace() {
        if (cmpTransitions != null) {
            int size = cmpTransitions.size();
            for (int iter = 0; iter < size; iter++) {
                ((Anim) cmpTransitions.elementAt(iter)).destroy();
            }
            cmpTransitions.removeAllElements();
            cmpTransitions = null;
        }
    }

    /**
     * remove all Components from container
     */
    public void removeAll() {
        Form parentForm = getComponentForm();
        if (parentForm != null) {
            Component focus = parentForm.getFocused();
            if (focus != null && contains(focus)) {
                parentForm.setFocused(null);
            }
        }
        Object[] arr = new Object[components.size()];
        components.copyInto(arr);

        for (int i = 0; i < arr.length; i++) {
            removeComponent((Component) arr[i]);
        }
    }

    /**
     * Re-layout the container, this is useful when we modify the container hierarchy and
     * need to redo the layout
     */
    public void revalidate() {
        setShouldCalcPreferredSize(true);
        Form root = getComponentForm();
        
        if (root != null) {
            root.layoutContainer();
            root.repaint();
        } else {
            layoutContainer();
            repaint();
        }
    }

    /**
     * @inheritDoc
     */
    public void paint(Graphics g) {
        layoutContainer();
        g.translate(getX(), getY());
        int size = components.size();
        for (int i = 0; i < size; i++) {
            Component cmp = (Component)components.elementAt(i);
            cmp.paintInternal(g, false);
        }
        g.translate(-getX(), -getY());
    }

    void paintIntersecting(Graphics g, Component cmp, int x, int y, int w, int h, boolean above) {

        if (layout.isOverlapSupported() && components.contains(cmp)) {
            int indexOfComponent = components.indexOf(cmp);
            
            int startIndex;
            int endIndex;
            if (above) {
                startIndex = indexOfComponent + 1;
                endIndex = components.size();
            } else {
                startIndex = 0;
                endIndex = indexOfComponent;
            }

            for (int i = startIndex; i < endIndex; i++) {
                Component cmp2 = (Component) components.elementAt(i);
                if(Rectangle.intersects(x, y, w, h,
                        cmp2.getAbsoluteX() + cmp2.getScrollX(),
                        cmp2.getAbsoluteY() + cmp2.getScrollY(),
                        cmp2.getBounds().getSize().getWidth(),
                        cmp2.getBounds().getSize().getHeight())){
                    cmp2.paintInternal(g, false);
                }
            }
        }
    }

    /**
     * Performs the layout of the container if a layout is necessary
     */
    public void layoutContainer() {
        //will compute the container + components and will layout the components.
        if (shouldLayout) {
            shouldLayout = false;
            doLayout();
        }
    }

    /**
     * Lays out the container
     */
    void doLayout() {
        layout.layoutContainer(this);
        int count = getComponentCount();
        for (int i = 0; i < count; i++) {
            Component c = getComponentAt(i);
            if (c instanceof Container) {
                ((Container) c).doLayout();
            }else{
                c.laidOut();
            }
        }
        laidOut();
    }

    /**
     * Returns the number of components
     * 
     * @return the Component count
     */
    public int getComponentCount() {
        return components.size();
    }

    /**
     * Returns the Component at a given index
     * 
     * @param index of the Component you wish to get
     * @return a Component
     * @throws ArrayIndexOutOfBoundsException if an invalid index was given.
     */
    public Component getComponentAt(
            int index) {
        return (Component) components.elementAt(index);
    }

    /**
     * Returns the Component index in the Container
     * 
     * @param cmp the component to search for
     * @return the Component index in the Container or -1 if not found
     */
    public int getComponentIndex(Component cmp) {
        int count = getComponentCount();
        for (int i = 0; i <
                count; i++) {
            Component c = getComponentAt(i);
            if (c.equals(cmp)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Returns true if the given component is within the hierarchy of this container
     *
     * @param cmp a Component to check
     * @return true if this Component contains in this Container
     */
    public boolean contains(Component cmp) {
        boolean found = false;
        int count = getComponentCount();
        for (int i = 0; i < count; i++) {
            Component c = getComponentAt(i);
            if (c.equals(cmp)) {
                return true;
            }

            if (c instanceof Container) {
                found = ((Container) c).contains(cmp);
                if (found) {
                    return true;
                }

            }
        }
        return false;
    }

    /**
     * Makes sure the component is visible in the scroll if this container is 
     * scrollable
     * 
     * @param c the component that will be scrolling for visibility
     */
    protected void scrollComponentToVisible(Component c) {
        if (isScrollable()) {
            if (c != null) {
                if (c.getParent() != null) {
                    // special case for the first component to allow the user to scroll all the 
                    // way to the top
                    Form f = getComponentForm();
                    if (f != null && f.getFocusPosition(c) == 0) {
                        // support this use case only if the component doesn't explicitly declare visible bounds
                        Rectangle r = c.getVisibleBounds();
                        if(r == getBounds()) {
                            scrollRectToVisible(new Rectangle(0, 0, c.getX() + c.getWidth(), c.getY() + c.getHeight()), this);
                        } else {
                            scrollRectToVisible(r, c);
                        }
                        return;
                    }
                }
                scrollRectToVisible(c.getVisibleBounds(), c);
            }
        }
    }

    /**
     * This method scrolls the Container if Scrollable towards the given 
     * Component based on the given direction.
     * 
     * @param direction is true if forward 
     * @param c the Component to move the scroll towards.
     * 
     * @return true if c Component is now visible.
     */
    boolean moveScrollTowards(int direction, Component c) {
        if (isScrollable()) {
            if (c != null) {
                Component focus = null;
                Form f = getComponentForm();
                if(f != null){
                    focus = f.getFocused();
                }
                Rectangle move;
                boolean containsTheFocus = contains(focus);
                boolean forward = (direction == Display.GAME_DOWN || 
                        direction == Display.GAME_RIGHT);
                boolean movedOnY = false;
                if (isScrollableY()) {
                    //if the current focus Component is larger then the viewport
                    //and belongs to this scrollable Container
                    //scroll the scrollbar in scrollIncrement pixels
                    if ( containsTheFocus && focus.getHeight() > getHeight()) {
                        move = new Rectangle(focus.getBounds());
                        move.getSize().setHeight(scrollIncrement);
                        if (forward) {
                            if (focus.getAbsoluteY() + focus.getHeight() > getAbsoluteY() + getScrollY() + getHeight()) {
                                move.setY(getScrollY() + getHeight() + scrollIncrement);
                                scrollRectToVisible(move, focus);
                                return false;
                            }
                        } else {
                            if (focus.getAbsoluteY() < getAbsoluteY() + getScrollY()) {
                                move.setY(getScrollY() - scrollIncrement);
                                scrollRectToVisible(move, focus);
                                return false;
                            }
                        }
                    }
                    if(!isScrollableX() || (direction != Display.GAME_LEFT && direction != Display.GAME_RIGHT)){
                        //if c is larger then the viewport scroll only to the 
                        //begining of the Component
                        if (c.getHeight() > getHeight()) {
                            move = new Rectangle(c.getBounds());
                            if (!forward) {
                                move.setY(move.getY() + move.getSize().getHeight() - scrollIncrement);
                            }
                            move.getSize().setHeight(scrollIncrement);
                            scrollRectToVisible(move, c);
                        } else {
                            scrollRectToVisible(c.getX(), c.getY(), Math.min(getWidth(), c.getWidth()), c.getHeight(), c);
                        }
                        movedOnY = true;
                    }
                }
                
                if (isScrollableX() && !movedOnY) {
                    //if the current focus Component is larger then the viewport
                    //and belongs to this scrollable Container
                    //scroll the scrollbar in scrollIncrement pixels
                    if (containsTheFocus && focus.getWidth() > getWidth()&& (direction == Display.GAME_LEFT || direction == Display.GAME_RIGHT)) {
                        move = new Rectangle(focus.getBounds());
                        move.getSize().setWidth(scrollIncrement);
                        if (forward) {
                            if (focus.getAbsoluteX() + focus.getWidth() > getAbsoluteX() + getScrollX() + getWidth()) {
                                move.setX(getScrollX() + getWidth() + scrollIncrement);
                                scrollRectToVisible(move, focus);
                                return false;
                            }
                        } else {
                            if (focus.getAbsoluteX() < getAbsoluteX() + getScrollX()) {
                                move.setX(getScrollX() - scrollIncrement);
                                scrollRectToVisible(move, focus);
                                return false;
                            }
                        }
                    }
                    //if c is larger then the viewport scroll only to the 
                    //begining of the Component
                    if (c.getWidth() > getWidth()) {
                        move = new Rectangle(c.getBounds());
                        if (!forward) {
                            move.setX(move.getX() + move.getSize().getWidth() - scrollIncrement);
                        }
                        move.getSize().setWidth(scrollIncrement);
                        scrollRectToVisible(move, c);
                    } else {
                        scrollRectToVisible(c.getX(), c.getY(), c.getWidth(), Math.min(getHeight(), c.getHeight()), c);
                    }
                    
                }
            }
        }
        return true;
    }

    /**
     * Returns a Component that exists in the given x, y coordinates by traversing
     * component objects and invoking contains
     * 
     * @param x absolute screen location
     * @param y absolute screen location
     * @return a Component if found, null otherwise
     * @see Component#contains
     */
    public Component getComponentAt(int x, int y) {
        int count = getComponentCount();
        boolean overlaps = getLayout().isOverlapSupported();
        Component component = null;
        for (int i = count - 1; i >= 0; i--) {
            Component cmp = getComponentAt(i);
            if (cmp.contains(x, y)) {
                component = cmp;
                if (!overlaps && component.isFocusable()) {
                    return component;
                }
                if (cmp instanceof Container) {
                    component = ((Container) cmp).getComponentAt(x, y);
                }
                if (!overlaps || component.isFocusable()) {
                    return component;
                }
            }
        }
        if (component != null){
            return component;
        }
        if (contains(x, y)) {
            return this;
        }
        return null;
    }

    /**
     * @inheritDoc
     */
    public void pointerHover(int[] x, int[] y) {
        if(!isDragActivated()) {
            Component c = getComponentAt(x[0], y[0]);
            if(c != null && c.isFocusable()) {
                c.requestFocus();
            }
        }
        super.pointerDragged(x[0], y[0]);
    }
    
    /**
     * @inheritDoc
     */
    public void pointerPressed(int x, int y) {
        clearDrag();
        setDragActivated(false);
        Component cmp = getComponentAt(x, y);
        if (cmp == this) {
            super.pointerPressed(x, y);
        } else if (cmp != null) {
            cmp.pointerPressed(x, y);
        }
    }

    /**
     * @inheritDoc
     */
    protected Dimension calcPreferredSize() {
        Dimension d = layout.getPreferredSize(this);
        return d;
    }

    /**
     * @inheritDoc
     */
    protected String paramString() {
        String className = layout.getClass().getName();
        String layoutStr = className.substring(className.lastIndexOf('.') + 1);
        return super.paramString() + ", layout = " + layoutStr +
                ", scrollableX = " + scrollableX +
                ", scrollableY = " + scrollableY +
                ", components = " + getComponentsNames();
    }

    /**
     * Return the conatainer components objects as list of Strings
     * @return the conatainer components objects as list of Strings
     */
    private String getComponentsNames() {
        String ret = "[";
        int size = components.size();
        for(int iter = 0 ; iter < size ; iter++) {
            String className = components.elementAt(iter).getClass().getName();
            ret += className.substring(className.lastIndexOf('.') + 1) + ", ";
        }
        if (ret.length() > 1) {
            ret = ret.substring(0, ret.length() - 2);
        }
        ret = ret + "]";
        return ret;
    }

    /**
     * @inheritDoc
     */
    public void refreshTheme() {
        super.refreshTheme();
        int size = components.size();
        for(int iter = 0 ; iter < size ; iter++) {
            Component cmp = (Component) components.elementAt(iter);
            cmp.refreshTheme();
        }
    }

    /**
     * @inheritDoc
     */
    public boolean isScrollableX() {
        return scrollableX && getScrollDimension().getWidth() > getWidth();
    }

    /**
     * @inheritDoc
     */
    public boolean isScrollableY() {
        return scrollableY && getScrollDimension().getHeight() > getHeight();
    }

    /**
     * Sets whether the component should/could scroll on the X axis
     * 
     * @param scrollableX whether the component should/could scroll on the X axis
     */
    public void setScrollableX(boolean scrollableX) {
        this.scrollableX = scrollableX;
    }

    /**
     * Sets whether the component should/could scroll on the Y axis
     * 
     * @param scrollableY whether the component should/could scroll on the Y axis
     */
    public void setScrollableY(boolean scrollableY) {
        this.scrollableY = scrollableY;
    }

    /**
     * The equivalent of calling both setScrollableY and setScrollableX
     * 
     * @param scrollable whether the component should/could scroll on the 
     * X and Y axis
     */
    public void setScrollable(boolean scrollable) {
        setScrollableX(scrollable);
        setScrollableY(scrollable);
    }

    /**
     * @inheritDoc
     */
    public void setCellRenderer(boolean cellRenderer) {
        if (isCellRenderer() != cellRenderer) {
            super.setCellRenderer(cellRenderer);
            int size = getComponentCount();
            for (int iter = 0; iter <
                    size; iter++) {
                getComponentAt(iter).setCellRenderer(cellRenderer);
            }
        }
    }

    /**
     * Determines the scroll increment size of this Container.
     * This value is in use when the current foucs element within this Container
     * is larger then this Container size.
     *
     * @param scrollIncrement the size in pixels.
     */
    public void setScrollIncrement(int scrollIncrement) {
        this.scrollIncrement = scrollIncrement;
    }

    /**
     * Gets the Container scroll increment
     *
     * @return the scroll increment in pixels.
     */
    public int getScrollIncrement() {
        return scrollIncrement;
    }

    class Anim implements Animation, Runnable {
        private Transition t;
        private Component current;
        private Component next;
        private boolean started = false;
        private Container thisContainer;
        private boolean finished = false;
        private Form parent;
        
        public Anim(Container thisContainer, Component current, Component next, Transition t) {
            this.t = t;
            this.next = next;
            this.current = current;
            this.thisContainer = thisContainer;
            this.parent = thisContainer.getComponentForm();
        }

        public boolean animate() {
            if (!started) {
                t.init(current, next);
                t.initTransition();
                started = true;
                if (cmpTransitions == null) {
                    cmpTransitions = new Vector();
                }
                cmpTransitions.addElement(this);
            }
            boolean notFinished = t.animate();
            if (!notFinished) {
                cmpTransitions.removeElement(this);
                destroy();
            }
            return notFinished;
        }

        public void destroy() {
            parent.deregisterAnimatedInternal(this);
            next.setParent(null);
            thisContainer.replace(current, next);
            //release the events blocking
            t.cleanup();
            if(cmpTransitions.size() == 0){
                parent.revalidate();
            }
            finished = true;
        }

        public void paint(Graphics g) {
            t.paint(g);
        }

        public boolean isFinished() {
            return finished;
        }

        public void run() {
            while (!isFinished()) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
}

