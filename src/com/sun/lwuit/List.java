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
import com.sun.lwuit.animations.Motion;
import com.sun.lwuit.geom.Dimension;
import com.sun.lwuit.geom.Rectangle;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.events.DataChangedListener;
import com.sun.lwuit.events.SelectionListener;
import com.sun.lwuit.list.DefaultListCellRenderer;
import com.sun.lwuit.list.DefaultListModel;
import com.sun.lwuit.list.ListCellRenderer;
import com.sun.lwuit.list.ListModel;
import com.sun.lwuit.plaf.LookAndFeel;
import com.sun.lwuit.plaf.Style;
import com.sun.lwuit.plaf.UIManager;
import java.util.Vector;

/**
 * A set of elements that is rendered using a {@link com.sun.lwuit.list.ListCellRenderer}
 * and are extracted via the {@link com.sun.lwuit.list.ListModel}.
 * <p>A list can represent many UI concepts ranging from a carousel to a "todo" checklist, this
 * is made possible thanks to extensive use of Swing's style of MVC. Specifically a list
 * component is relatively simple, it invokes the model in order to extract the displayed/selected
 * information and shows it to the user by invoking the cell renderer.
 * <p>The list class itself is completely decoupled from everything, thus it allows us to extract its
 * content from any source (e.g. network, storage etc.) and display the information in any form
 * (e.g. checkboxed elemenents, icons etc.).
 *
 * @see com.sun.lwuit.list
 * @author Chen Fishbein
 */
public class List extends Component {

    /**
     * Indicates the list isn't fixed and that selection is movable
     */
    public static final int FIXED_NONE = 0;
    /**
     * Indicates that the list is not fixed in place but cycles its elements
     */
    public static final int FIXED_NONE_CYCLIC = 1;
    /**
     * Indicates the list selection will only reach the edge when there are no more
     * elements in the list.
     */
    public static final int FIXED_NONE_ONE_ELEMENT_MARGIN_FROM_EDGE = 2;
    /**
     * Allows to test for fixed none
     */
    private static final int FIXED_NONE_BOUNDRY = 9;
    /**
     * Indicates the list selection is fixed into place at the top of the list
     * or at the left of the list
     */
    public static final int FIXED_LEAD = 10;
    /**
     * Indicates the list selection is fixed into place at the bottom of the list
     * or at the right of the list
     */
    public static final int FIXED_TRAIL = 11;
    /**
     * Indicates the list selection is fixed into place at the center of the list
     */
    public static final int FIXED_CENTER = 12;
    /**
     * @see setRenderingPrototype
     */
    private Object renderingPrototype;
    /**
     * Indicates whether selection is fixable to place in which case all the
     * elements in the list move and selection stays in place. Fixed selection
     * can be one of: FIXED_NONE, FIXED_TRAIL, FIXED_LEAD, FIXED_CENTER
     */
    private int fixedSelection;
    private ListModel model;
    private ListCellRenderer renderer = new DefaultListCellRenderer();
    private int orientation = VERTICAL;
    /**
     * Indicates the list orientation is VERTICAL
     */
    public static final int VERTICAL = 0;
    /**
     * Indicates the list orientation is HORIZONTAL
     */
    public static final int HORIZONTAL = 1;
    static final int COMBO = 2;
    EventDispatcher dispatcher = new EventDispatcher();
    Object eventSource = this;
    private Dimension elemSize;
    private Dimension selectedElemSize;
    private boolean inputOnFocus = true;
    private boolean numericKeyActions = true;
    private boolean paintFocusBehindList = true;

    /**
     * Minimum number of elements shown in a list, this member is used to calculate
     * the list preferred size. If the number of elements in the model is smaller than
     * this then this value is used in the calculations.
     */
    private int minElementHeight = 0;

    /**
     * Indicates the gap between each item in the list
     */
    private int itemGap = 2;
    /**
     * Indicates the surrounding border gap
     */
    private int borderGap = 2;
    private Listeners listener;
    /**
     * Indicates the position within the current animation, 0 means no animation
     * is in progress
     */
    private int animationPosition;
    private int destination;
    private Motion listMotion;
    private boolean fireOnClick = true;

    /**
     * Initial x/y positions for the fixed mode drag
     */
    private int initialFixedDrag;
    private int initialFixedSelection;

    private boolean commandList;

    /**
     * Indicates whether the list should not paint the focus component if the list
     * itself has no focus.
     */
    private boolean ignoreFocusComponentWhenUnfocused;

    /**
     * Used internally by the combo box
     */
    boolean disposeDialogOnSelection;

    /**
     * Indicates that the background of a cell renderer might mutate between one entry and the next,
     * it is recommended that this flag remains false for performance reasons.
     */
    private boolean mutableRendererBackgrounds;

    /**
     * This flag indicates if the List should automatically scroll to the
     * selected element when it's been initialized.
     */
    private boolean scrollToSelected = true;

    /**
     * Creates a new instance of List
     *
     * @param items set of items placed into the list model
     */
    public List(Vector items) {
        this(new DefaultListModel(items));
    }

    /**
     * Creates a new instance of List
     *
     * @param items set of items placed into the list model
     */
    public List(Object[] items) {
        this(new DefaultListModel(items));
    }

    /**
     * Creates a new instance of List with an empty default model
     */
    public List() {
        this(new DefaultListModel());
    }

    /**
     * Creates a new instance of List with the given model
     *
     * @param model the model instance
     */
    public List(ListModel model) {
        setUIID("List");
        setModel(model);
        LookAndFeel laf = UIManager.getInstance().getLookAndFeel();
        setSmoothScrolling(laf.isDefaultSmoothScrolling());
    }

    /**
     * @inheritDoc
     */
    void initComponentImpl() {
        dataChanged(0, 0);
        // lazily bind listeners to prevent a memory leak in cases where models
        // are stored separately from view
        bindListeners();
        super.initComponentImpl();
        int index = model.getSelectedIndex();
        if(index >= 0){
            model.setSelectedIndex(index);
        }
    }

    /**
     * @inheritDoc
     */
    protected void laidOut() {
        super.laidOut();
        if (isScrollable() && isInitialized() && scrollToSelected) {
            int index = model.getSelectedIndex();
            if (index >= 0) {
                selectElement(index);
            }
        }
    }

    /**
     * @inheritDoc
     */
    void deinitializeImpl() {
        super.deinitializeImpl();

        // cleanup to allow garbage collection even if the user keeps the model in
        // memory which is a valid use case
        if (listener != null) {
            model.removeDataChangedListener(listener);
            model.removeSelectionListener(listener);
            listener = null;
        }
    }

    /**
     * Callback to allow subclasses to react to a change in the model
     *
     * @param status the type data change; REMOVED, ADDED or CHANGED
     * @param index item index in a list model
     */
    protected void modelChanged(int status, int index) {
    }

    /**
     * Callback to allow subclasses to react to a selectio change in the model
     *
     * @param oldSelected the old selection value
     * @param newSelected the new selection value
     */
    protected void listSelectionChanged(int oldSelected, int newSelected) {
    }

    /**
     * @inheritDoc
     */
    public boolean isScrollableY() {
        return getScrollDimension().getHeight() > getHeight() && (fixedSelection < FIXED_NONE_BOUNDRY) &&
                orientation != HORIZONTAL;
    }

    /**
     * @inheritDoc
     */
    public boolean isScrollableX() {
        return getScrollDimension().getWidth() > getWidth() && (fixedSelection < FIXED_NONE_BOUNDRY) &&
                orientation == HORIZONTAL;
    }

    /**
     * Minimum number of elements shown in a list, this member is used to calculate
     * the list preferred size. If the number of elements in the model is smaller than
     * this then this value is used in the calculations.
     *
     * @return the minimum number of elements
     */
    public int getMinElementHeight() {
        return minElementHeight;
    }

    /**
     * Minimum number of elements shown in a list, this member is used to calculate
     * the list preferred size. If the number of elements in the model is smaller than
     * this then this value is used in the calculations.
     *
     * @param minElementHeight the minimum number of elements
     */
    public void setMinElementHeight(int minElementHeight) {
        this.minElementHeight = minElementHeight;
    }

    /**
     *
     * Returns the number of elements in the list, shorthand for
     * getModel().getSize()
     *
     * @return the number of elements in the list
     */
    public int size() {
        return model.getSize();
    }

    /**
     * Returns the current selected offset in the list
     *
     * @return the current selected offset in the list
     */
    public int getSelectedIndex() {
        return model.getSelectedIndex();
    }

    /**
     * Sets the current selected offset in the list, by default this implementation
     * will scroll the list to the selection if the selection is outside of the screen
     *
     * @param index the current selected offset in the list
     */
    public void setSelectedIndex(int index) {
        setSelectedIndex(index, true);
    }

    /**
     * @inheritDoc
     */
    protected Rectangle getVisibleBounds() {
        Rectangle pos = new Rectangle();
        Dimension rendererSize = getElementSize(false, true);
        Style style = getStyle();
        int width = getWidth() - style.getPadding(isRTL(), RIGHT) - style.getPadding(isRTL(), LEFT) - getSideGap();
        calculateComponentPosition(getSelectedIndex(), width, pos, rendererSize, getElementSize(true, true), true);
        pos.setX(pos.getX() + getX());
        pos.setY(pos.getY() + getY());
        return pos;
    }

    /**
     * Sets the current selected offset in the list
     *
     * @param index the current selected offset in the list
     * @param scrollToSelection indicates whether scrolling to selection should
     * occur if the selection is outside of view
     */
    public void setSelectedIndex(int index, boolean scrollToSelection) {
        if (index < 0) {
            throw new IllegalArgumentException("Selection index is negative:" + index);
        }
        model.setSelectedIndex(index);
        if (scrollToSelection && isInitialized()) {
            selectElement(index);
        }
    }

    /**
     * Returns the current selected item in the list or null for no selection
     *
     * @return the current selected item in the list
     */
    public Object getSelectedItem() {
        int idx = model.getSelectedIndex();
        if (idx < model.getSize() && idx > -1) {
            return model.getItemAt(idx);
        }
        return null;
    }

    /**
     * Sets the current selected item in the list
     *
     * @param item the current selected item in the list
     */
    public void setSelectedItem(Object item) {
        int size = model.getSize();
        for (int iter = 0; iter < size; iter++) {
            Object current = model.getItemAt(iter);
            if (current == item || (current != null && current.equals(item))) {
                model.setSelectedIndex(iter);
                break;
            }
        }
    }

    /**
     * Returns the model underlying the list
     *
     * @return the model underlying the list
     */
    public ListModel getModel() {
        return model;
    }

    /**
     * @inheritdoc
     */
    protected void setShouldCalcPreferredSize(boolean shouldCalcPreferredSize) {
        super.setShouldCalcPreferredSize(shouldCalcPreferredSize);
        elemSize = null;
        selectedElemSize = null;

        // we should try passing the should calcPreferredSize to the renderer so it can revalidate too
        if(shouldCalcPreferredSize) {
            ListCellRenderer r = getRenderer();
            Object val;
            if (renderingPrototype != null) {
                val = renderingPrototype;
            } else {
                if (getModel().getSize() > 0) {
                    val = getModel().getItemAt(0);
                } else {
                    return;
                }
            }
            Component c = r.getListCellRendererComponent(this, val, 0, false);
            c.setShouldCalcPreferredSize(shouldCalcPreferredSize);
            c = r.getListCellRendererComponent(this, val, 0, true);
            c.setShouldCalcPreferredSize(shouldCalcPreferredSize);
        }
    }


    void dataChanged(int status, int index) {
        setShouldCalcPreferredSize(true);
        if (getSelectedIndex() >= model.getSize()) {
            setSelectedIndex(Math.max(model.getSize() - 1, 0));
        }

        modelChanged(status, index);
        repaint();
    }

    private void bindListeners() {
        if (listener == null) {
            listener = new Listeners();
            model.addDataChangedListener(listener);
            model.addSelectionListener(listener);
        }
    }

    /**
     * Replaces/sets the model underlying the list
     *
     * @param model the new model underlying the list
     */
    public void setModel(ListModel model) {
        if (this.model != null) {
            setShouldCalcPreferredSize(true);
            this.model.removeDataChangedListener(listener);
            this.model.removeSelectionListener(listener);
            this.model = model;
            listener = null;

            // when replacing a model on a scrolled list reset the scrolling if necessary
            if (getScrollDimension().getHeight() < getScrollY() + getHeight()) {
                setScrollY(0);
            }
            if (getScrollDimension().getWidth() < getScrollX() + getWidth()) {
                setScrollX(0);
            }
        }
        this.model = model;
        if (isInitialized()) {
            bindListeners();
        }
        repaint();
    }

    /**
     * Indicate whether pressing the number keys should trigger an action
     *
     * @return true if pressing the number keys should trigger an action
     */
    public boolean isNumericKeyActions() {
        return numericKeyActions;
    }

    /**
     * Indicate whether pressing the number keys should trigger an action
     *
     * @param numericKeyActions true to trigger an action on number keys
     */
    public void setNumericKeyActions(boolean numericKeyActions) {
        this.numericKeyActions = numericKeyActions;
    }

    /**
     * Indicates that the list should be treated as a list of commands, if the
     * user "clicks" a command from the list its action performed method is invoked.
     *
     * @return true if the list is treated as a command list
     */
    public boolean isCommandList() {
        return commandList;
    }

    /**
     * Indicates that the list should be treated as a list of commands, if the
     * user "clicks" a command from the list its action performed method is invoked.
     *
     * @param commandList true for the list to be treated as a command list
     */
    public void setCommandList(boolean commandList) {
        this.commandList = commandList;
    }

    /**
     * Indicates whether the list should not paint the focus component if the list
     * itself has no focus.
     *
     * @return the ignoreFocusComponentWhenUnfocused
     */
    public boolean isIgnoreFocusComponentWhenUnfocused() {
        return ignoreFocusComponentWhenUnfocused;
    }

    /**
     * Indicates whether the list should not paint the focus component if the list
     * itself has no focus.
     *
     * @param ignoreFocusComponentWhenUnfocused true to ignore the focus component false otherwise
     */
    public void setIgnoreFocusComponentWhenUnfocused(boolean ignoreFocusComponentWhenUnfocused) {
        this.ignoreFocusComponentWhenUnfocused = ignoreFocusComponentWhenUnfocused;
    }

    /**
     * Indicates that the background of a cell renderer might mutate between one entry and the next,
     * it is recommended that this flag remains false for performance reasons.

     * @return the value of the flag
     */
    public boolean isMutableRendererBackgrounds() {
        return mutableRendererBackgrounds;
    }

    /**
     * Indicates that the background of a cell renderer might mutate between one entry and the next,
     * it is recommended that this flag remains false for performance reasons.

     * @param mutableRendererBackgrounds the new value for the flag
     */
    public void setMutableRendererBackgrounds(boolean mutableRendererBackgrounds) {
        this.mutableRendererBackgrounds = mutableRendererBackgrounds;
    }


    private class Listeners implements DataChangedListener, SelectionListener {

        public void dataChanged(int status, int index) {
            List.this.dataChanged(status, index);
        }

        public void selectionChanged(int oldSelected, int newSelected) {
            repaint();
            List.this.listSelectionChanged(oldSelected, newSelected);
        }
    }

    /**
     * Sets the renderer which is used to draw list elements
     *
     * @param renderer cell renderer instance
     */
    public void setListCellRenderer(ListCellRenderer renderer) {
        if (this.renderer != null) {
            //calculate the item list size and the list size.
            elemSize = null;
            selectedElemSize = null;
            setShouldCalcPreferredSize(true);
        }
        this.renderer = renderer;
    }

    /**
     * Returns the renderer which is used to draw list elements
     *
     * @return the renderer which is used to draw list elements
     */
    public ListCellRenderer getRenderer() {
        return renderer;
    }

    /**
     * Returns the list orientation
     *
     * @return the list orientation HORIZONTAL or VERTICAL
     * @see #HORIZONTAL
     * @see #VERTICAL
     */
    public int getOrientation() {
        return orientation;
    }

    /**
     * @inheritDoc
     */
    public void refreshTheme() {
        ListCellRenderer r = getRenderer();
        if (renderingPrototype != null) {
            r.getListCellRendererComponent(this, renderingPrototype, 0, false).refreshTheme();
        } else {
            if (getModel().getSize() > 0) {
                r.getListCellRendererComponent(this, getModel().getItemAt(0), 0, false).refreshTheme();
            } else {
                r.getListCellRendererComponent(this, null, 0, false).refreshTheme();
            }
        }
        Component focus = r.getListFocusComponent(this);
        if (focus != null) {
            focus.refreshTheme();
        }
        super.refreshTheme();
    }

    /**
     * Sets the list orientation HORIZONTAL or VERTICAL
     *
     * @param orientation the list orientation HORIZONTAL or VERTICAL
     * @see #HORIZONTAL
     * @see #VERTICAL
     */
    public void setOrientation(int orientation) {
        this.orientation = orientation;
    }

    /**
     * Makes sure the selected index is visible if it is not in the current view
     * rect the list will scroll so it fits within
     *
     * @param rect the rectangle area to scroll to
     */
    public void scrollRectToVisible(Rectangle rect) {
        if (fixedSelection < FIXED_NONE_BOUNDRY) {
            //Dimension elemSize = getElementSize();
            Rectangle toScroll;
            if (orientation != HORIZONTAL) {
                toScroll = new Rectangle(getScrollX(), rect.getY(), rect.getSize().getWidth(), rect.getSize().getHeight() + itemGap);
            } else {
                toScroll = new Rectangle(rect.getX(), getScrollY(), rect.getSize().getWidth() + itemGap, rect.getSize().getHeight());
            }
            super.scrollRectToVisible(toScroll, this);

        }
    }

    /**
     * @inheritDoc
     */
    public void setHandlesInput(boolean b) {
        Form f = getComponentForm();
        if (f != null) {
            // prevent the list from losing focus if its the only element
            // or when the user presses fire and there is no other component
            super.setHandlesInput(b || f.isSingleFocusMode());
        } else {
            super.setHandlesInput(b);
        }
    }

    void setHandlesInputParent(boolean b) {
        super.setHandlesInput(b);
    }

    /**
     * @inheritDoc
     */
    protected void fireClicked() {
        boolean h = handlesInput();
        setHandlesInput(!h);
        if (h) {
            fireActionEvent();
        }
        repaint();
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
    public void keyReleased(int keyCode) {
        // other events are in keyReleased to prevent the next event from reaching the next form
        int gameAction = Display.getInstance().getGameAction(keyCode);
        if (gameAction == Display.GAME_FIRE) {
            boolean h = handlesInput();
            setHandlesInput(!h);
            if (h) {
                fireActionEvent();
            }
            repaint();
            return;
        }

        if (numericKeyActions && gameAction != Display.GAME_LEFT &&
                gameAction != Display.GAME_RIGHT && gameAction != Display.GAME_UP &&
                gameAction != Display.GAME_DOWN) {
            if (keyCode >= '1' && keyCode <= '9') {
                int offset = keyCode - '1';
                if (offset < getModel().getSize()) {
                    setSelectedIndex(offset);
                    fireActionEvent();
                }
            }
        }
    }

    /**
     * @inheritDoc
     */
    public void keyPressed(int keyCode) {
        // scrolling events are in keyPressed to provide immediate feedback
        if (!handlesInput()) {
            return;
        }

        int gameAction = Display.getInstance().getGameAction(keyCode);
        int keyFwd;
        int keyBck;
        if (getOrientation() != HORIZONTAL) {
            keyFwd = Display.GAME_DOWN;
            keyBck = Display.GAME_UP;
            if (gameAction == Display.GAME_LEFT || gameAction == Display.GAME_RIGHT) {
                setHandlesInput(false);
            }
        } else {
            if (isRTL()) {
	        	keyFwd = Display.GAME_LEFT;
	            keyBck = Display.GAME_RIGHT;
            } else {
	        	keyFwd = Display.GAME_RIGHT;
	            keyBck = Display.GAME_LEFT;
            }
            if (gameAction == Display.GAME_DOWN || gameAction == Display.GAME_UP) {
                setHandlesInput(false);
            }
        }

        int selectedIndex = model.getSelectedIndex();
        if (gameAction == keyBck) {
            selectedIndex--;
            if (selectedIndex < 0) {
                if (fixedSelection != FIXED_NONE && fixedSelection != FIXED_NONE_ONE_ELEMENT_MARGIN_FROM_EDGE) {
                    selectedIndex = size() - 1;
                } else {
                    selectedIndex = 0;
                    setHandlesInput(false);
                }
            }
        } else if (gameAction == keyFwd) {
            selectedIndex++;
            if (selectedIndex >= size()) {
                if (fixedSelection != FIXED_NONE && fixedSelection != FIXED_NONE_ONE_ELEMENT_MARGIN_FROM_EDGE) {
                    selectedIndex = 0;
                } else {
                    selectedIndex = size() - 1;
                    setHandlesInput(false);
                }
            }
        }

        if (selectedIndex != model.getSelectedIndex()) {
            model.setSelectedIndex(selectedIndex);
            int direction = (gameAction == keyFwd ? 1 : -1);
            if ((isRTL()) && (getOrientation() == HORIZONTAL)) {
            	direction = -direction;
            }
            updateAnimationPosition(direction);
            if (fixedSelection == FIXED_NONE || fixedSelection == FIXED_NONE_CYCLIC) {
                selectElement(selectedIndex);
            }
            if (fixedSelection == FIXED_NONE_ONE_ELEMENT_MARGIN_FROM_EDGE) {
                // are we going down?
                if (keyFwd == gameAction) {
                    selectElement(Math.min(selectedIndex + 1, getModel().getSize() - 1));
                } else {
                    selectElement(Math.max(selectedIndex - 1, 0));
                }
            }
        }
        repaint();
    }

    private void selectElement(int selectedIndex) {
        Dimension size = getElementSize(false, true);
        Rectangle rect;
        if (getOrientation() != HORIZONTAL) {
            rect = new Rectangle(getX(), (size.getHeight() + itemGap) * selectedIndex, getElementSize(true, true));
        } else {
            rect = new Rectangle((size.getWidth() + itemGap) * selectedIndex, getY(), getElementSize(true, true));
        }
        scrollRectToVisible(rect);
    }

    /**
     * Updates the animation constant to a new value based on a keypress
     *
     * @param direction direction of the animation 1 or -1
     */
    private void updateAnimationPosition(int direction) {
        if (animationPosition != 0) {
            return;
        }
        if (isSmoothScrolling()) {
            if (orientation != HORIZONTAL) {
                animationPosition += (direction * getElementSize(false, true).getHeight());
            } else {
                animationPosition += (direction * getElementSize(false, true).getWidth());
            }
            destination = Math.abs(animationPosition);
            initListMotion();
        }
    }

    private void initListMotion() {
        Form p = getComponentForm();
        if(p != null) {
            p.registerAnimatedInternal(this);
        }
        listMotion = Motion.createSplineMotion(0, destination, getScrollAnimationSpeed());
        listMotion.start();
    }

    /**
     * Calculates the desired bounds for the component and returns them within the
     * given rectangle.
     */
    private void calculateComponentPosition(int index, int defaultWidth, Rectangle rect, Dimension rendererSize, Dimension selectedSize, boolean beforeSelected) {
        Style style = getStyle();
        int initialY = style.getPadding(false, TOP);
        int initialX = style.getPadding(false, LEFT);

        boolean rtl = isRTL();
        if (rtl) {
            initialX += getSideGap();
        }

        int selection = getSelectedIndex();
        Dimension d = rect.getSize();
        int selectedDiff;

        // the algorithm illustrated here is very simple despite the "mess" of code...
        // The idea is that if we have a "fixed" element we just add up the amount of pixels
        // to get it into its place in the screen (nothing for top obviously).
        // In order to cause the list to be cyclic we just subtract the list size
        // which will cause the bottom elements to "return" from the top.
        if (orientation != HORIZONTAL) {
            int height = rendererSize.getHeight();
            selectedDiff = selectedSize.getHeight() - height;
            rect.setX(initialX);
            d.setHeight(height);
            d.setWidth(defaultWidth);
            int y = 0;
            int listHeight = getHeight() - style.getPadding(false, TOP) - style.getPadding(false, BOTTOM);
            int totalHeight = (height + itemGap) * getModel().getSize() + selectedDiff;
            switch (fixedSelection) {
                case FIXED_CENTER:
                    y = listHeight / 2 - (height + itemGap + selectedDiff) / 2 +
                            (index - selection) * (height + itemGap);
                    if (!beforeSelected) {
                        y += selectedDiff;
                    }
                    y = recalcOffset(y, totalHeight, listHeight, height + itemGap);
                    break;
                case FIXED_TRAIL:
                    y = listHeight - (height + itemGap + selectedDiff);
                case FIXED_LEAD:
                    y += (index - selection) * (height + itemGap);
                    if (index - selection > 0) {
                        y += selectedDiff;
                    }
                    y = recalcOffset(y, totalHeight, listHeight, height + itemGap);
                    break;
                default:
                    y = index * (height + itemGap);
                    if (!beforeSelected) {
                        y += selectedDiff;
                    }
                    break;
            }
            rect.setY(y + initialY);
            if (index == selection) {
                d.setHeight(d.getHeight() + selectedDiff);
            }

        } else {
            int width = rendererSize.getWidth();
            selectedDiff = selectedSize.getWidth() - width;
            rect.setY(initialY);
            d.setHeight(getHeight() - style.getPadding(false, TOP) - style.getPadding(false, BOTTOM));
            d.setWidth(width);
            int x = 0;
            int listWidth = getWidth() - style.getPadding(isRTL(), RIGHT) - style.getPadding(isRTL(), LEFT);
            int totalWidth = (width + itemGap) * getModel().getSize() + selectedDiff;
            switch (fixedSelection) {
                case FIXED_CENTER:
                    x = listWidth / 2 - (width + itemGap + selectedDiff) / 2 +
                            (index - selection) * (width + itemGap);
                    if (!beforeSelected) {
                        x += selectedDiff;
                    }
                    if(rtl) {
                    	x = listWidth - x - width;
                    }

                    x = recalcOffset(x, totalWidth, listWidth, width + itemGap);
                    break;
                case FIXED_TRAIL:
                    x = listWidth - (width + itemGap + selectedDiff);
                case FIXED_LEAD:
                    x += (index - selection) * (width + itemGap);
                    if (index - selection > 0) {
                        x += selectedDiff;
                    }
                    if (rtl) {
                    	x = listWidth - x - width;
                    }
                    x = recalcOffset(x, totalWidth, listWidth, width + itemGap);
                    break;
                default:
                    x = index * (width + itemGap);
                    if (!beforeSelected) {
                        x += selectedDiff;
                    }
                    break;
            }
            int rectX=initialX + x;
            if ((rtl) && (fixedSelection<FIXED_NONE_BOUNDRY)) {

            	rectX = initialX + totalWidth - (x - initialX) - (width + itemGap);
            	if(index == getSelectedIndex()) {
            		rectX -= selectedDiff;
                }
            	if(totalWidth < listWidth) {
            		rectX += (listWidth - totalWidth);
                }
            }
            rect.setX(rectX);
            if (index == selection) {
                d.setWidth(d.getWidth() + selectedDiff);
            }
        }
    }

    /**
     * Allows us to recalculate the bounds of a coordinate to make it "loop" back
     * into view
     *
     * @param offset either x or y coordinate
     * @param totalSize the total width or height of the list with all the elements (including scroll)
     * @param viewSize the size visible to the user
     * @param componentSize the size of the component
     * @return offset after manipulation if such manipulation was performed
     */
    private int recalcOffset(int offset, int totalSize, int viewSize, int componentSize) {
        if (offset + animationPosition % componentSize >= viewSize) {
            offset -= totalSize;
        } else {
            if (offset + animationPosition % componentSize < 1 - componentSize) {
                offset += totalSize;
            }
        }
        return offset;
    }

    /**
     * @inheritDoc
     */
    public void paint(Graphics g) {
        UIManager.getInstance().getLookAndFeel().drawList(g, this);

        Style style = getStyle();
        int width = getWidth() - style.getPadding(isRTL(), RIGHT) - style.getPadding(isRTL(), LEFT) - getSideGap();
        if (isScrollableX()) {
            width = Math.max(width, getScrollDimension().getWidth() - style.getPadding(isRTL(), RIGHT) - style.getPadding(isRTL(), LEFT) - getSideGap());
        }
        int numOfcomponents = model.getSize();
        if (numOfcomponents == 0) {
            return;
        }
        int xTranslate = getX();
        int yTranslate = getY();
        g.translate(xTranslate, yTranslate);
        Rectangle pos = new Rectangle();
        Dimension rendererSize = getElementSize(false, true);

        int selection = model.getSelectedIndex();

        if (animationPosition != 0 && fixedSelection > FIXED_NONE_BOUNDRY) {
            // selection should never move during animation of fixed elements
            selection = -1;

            if (orientation != HORIZONTAL) {
                yTranslate += animationPosition;
                g.translate(0, animationPosition);
            } else {
                xTranslate += animationPosition;
                g.translate(animationPosition, 0);
            }
        }

        int clipX = g.getClipX();
        int clipY = g.getClipY();
        int clipWidth = g.getClipWidth();
        int clipHeight = g.getClipHeight();

        // this flag is for preformance improvements
        // if we figured out that the list items are not visible anymore
        // we should break from the List loop
        boolean shouldBreak = false;

        // improve performance for browsing the end of a very large list
        int startingPoint = 0;
        if (fixedSelection < FIXED_NONE_BOUNDRY) {
            int startX = clipX + getAbsoluteX();
            if (isRTL()) {
                //In RTL the start of the list is not in the left side of the viewport, but rather the right side
            	startX += getWidth();
            }
            startingPoint = Math.max(0, pointerSelect(startX, clipY + getAbsoluteY()) - 1);
        }

        int startOffset = 0;
        int endOffset = numOfcomponents;

        if(mutableRendererBackgrounds) {
            for (int i = startingPoint; i < numOfcomponents; i++) {
                // skip on the selected
                if (i == getSelectedIndex() && animationPosition == 0) {
                    shouldBreak = true;
                    if(!shouldBreak) {
                        startOffset = i;
                    }
                    endOffset = i;
                    continue;
                }
                calculateComponentPosition(i, width, pos, rendererSize, getElementSize(true, true), i <= getSelectedIndex());

                // if the renderer is in the clipping region
                if (pos.intersects(clipX, clipY, clipWidth, clipHeight)) {
                    if(!shouldBreak) {
                        startOffset = i;
                    }
                    endOffset = i;
                    Dimension size = pos.getSize();
                    Component selectionCmp = renderer.getListCellRendererComponent(this, getModel().getItemAt(i), i, i == getSelectedIndex());
                    renderComponentBackground(g, selectionCmp, pos.getX(), pos.getY(), size.getWidth(), size.getHeight());
                    shouldBreak = true;
                } else {
                    //this is relevant only if the List is not fixed.
                    if (shouldBreak && (fixedSelection < FIXED_NONE_BOUNDRY)) {
                        break;
                    }
                }
            }
        } else {
            Object valueAt0 = getModel().getItemAt(0);
            Component selectionCmp = renderer.getListCellRendererComponent(this, valueAt0, 0, true);
            Component unselectedCmp = renderer.getListCellRendererComponent(this, valueAt0, 0, false);
            for (int i = startingPoint; i < numOfcomponents; i++) {
                // skip on the selected
                if (i == getSelectedIndex() && animationPosition == 0) {
                    shouldBreak = true;
                    if(!shouldBreak) {
                        startOffset = i;
                    }
                    endOffset = i;
                    continue;
                }
                calculateComponentPosition(i, width, pos, rendererSize, getElementSize(true, true), i <= getSelectedIndex());

                // if the renderer is in the clipping region
                if (pos.intersects(clipX, clipY, clipWidth, clipHeight)) {
                    if(!shouldBreak) {
                        startOffset = i;
                    }
                    endOffset = i;
                    if(i == getSelectedIndex()) {
                        Dimension size = pos.getSize();
                        renderComponentBackground(g, selectionCmp, pos.getX(), pos.getY(), size.getWidth(), size.getHeight());
                    } else {
                        Dimension size = pos.getSize();
                        renderComponentBackground(g, unselectedCmp, pos.getX(), pos.getY(), size.getWidth(), size.getHeight());
                    }
                    shouldBreak = true;
                } else {
                    //this is relevant only if the List is not fixed.
                    if (shouldBreak && (fixedSelection < FIXED_NONE_BOUNDRY)) {
                        break;
                    }
                }
            }
        }

        if (paintFocusBehindList) {
            paintFocus(g, width, pos, rendererSize);
        }

        for (int i = startOffset; i <= endOffset; i++) {
            // skip on the selected
            if (i == getSelectedIndex() && animationPosition == 0) {
                continue;
            }
            calculateComponentPosition(i, width, pos, rendererSize, getElementSize(true, true), i <= getSelectedIndex());

            Object value = model.getItemAt(i);
            Component cmp = renderer.getListCellRendererComponent(this, value, i, false);
            cmp.setCellRenderer(true);
            Dimension size = pos.getSize();
            renderComponent(g, cmp, pos.getX(), pos.getY(), size.getWidth(), size.getHeight());
        }
        calculateComponentPosition(getSelectedIndex(), width, pos, rendererSize, getElementSize(true, true), true);

        Dimension size = pos.getSize();
        //if the animation has finished draw the selected element
        if ((renderer.getListFocusComponent(this) == null && (fixedSelection < FIXED_NONE_BOUNDRY)) || animationPosition == 0 && model.getSize() > 0) {
            Component selected = renderer.getListCellRendererComponent(this, model.getItemAt(selection), selection, true);
            renderComponentBackground(g, selected, pos.getX(), pos.getY(), size.getWidth(), size.getHeight());
            renderComponent(g, selected, pos.getX(), pos.getY(), size.getWidth(), size.getHeight());
        }

        if (!paintFocusBehindList) {
            paintFocus(g, width, pos, rendererSize);
        }
        g.translate(-xTranslate, -yTranslate);
    }

    private void paintFocus(Graphics g, int width, Rectangle pos, Dimension rendererSize) {
        if(ignoreFocusComponentWhenUnfocused && !hasFocus()) {
            return;
        }
        calculateComponentPosition(getSelectedIndex(), width, pos, rendererSize, getElementSize(true, true), true);
        Dimension size = pos.getSize();

        Component cmp = renderer.getListFocusComponent(this);
        if (cmp != null) {
            cmp.setCellRenderer(true);
            int x = pos.getX();
            int y = pos.getY();

            // prevent focus animation from working during a drag operation
            if (orientation != HORIZONTAL) {
                y -= animationPosition;
            } else {
                x -= animationPosition;
            }
            renderComponentBackground(g, cmp, x, y, size.getWidth(), size.getHeight());
            renderComponent(g, cmp, x, y, size.getWidth(), size.getHeight());
        }

    }

    /**
     * Renders the current component on the screen
     */
    private void renderComponent(Graphics g, Component cmp, int x, int y, int width, int height) {
        Style s = cmp.getStyle();
        int left = s.getMargin(isRTL(), LEFT);
        int top =  s.getMargin(false, TOP);
        cmp.setWidth(width - left - s.getMargin(isRTL(), RIGHT));
        cmp.setHeight(height - top - s.getMargin(false, BOTTOM));
        cmp.setX(x + left);
        cmp.setY(y + top);

        int oX = g.getClipX();
        int oY = g.getClipY();
        int oWidth = g.getClipWidth();
        int oHeight = g.getClipHeight();

        g.clipRect(cmp.getX(), cmp.getY(), cmp.getWidth(), cmp.getHeight());

        cmp.paint(g);
        if (cmp.isBorderPainted()) {
            cmp.paintBorder(g);
        }
        g.setClip(oX, oY, oWidth, oHeight);
    }

    private void renderComponentBackground(Graphics g, Component cmp, int x, int y, int width, int height) {
        Style s = cmp.getStyle();
        int left = s.getMargin(isRTL(), LEFT);
        int top =  s.getMargin(false, TOP);
        cmp.setWidth(width - left - s.getMargin(isRTL(), RIGHT));
        cmp.setHeight(height - top - s.getMargin(false, BOTTOM));
        cmp.setX(x + left);
        cmp.setY(y + top);
        int cX = g.getClipX();
        int cY = g.getClipY();
        int cW = g.getClipWidth();
        int cH = g.getClipHeight();
        g.clipRect(cmp.getX(), cmp.getY(), cmp.getWidth(), cmp.getHeight());
        cmp.paintBackground(g);
        g.setClip(cX, cY, cW, cH);
    }

    /**
     * Invoked to indicate interest in future selection events
     *
     * @param l the selection listener to be added
     */
    public void addSelectionListener(SelectionListener l) {
        model.addSelectionListener(l);
    }

    /**
     * Invoked to indicate no further interest in future selection events
     *
     * @param l the selection listener to be removed
     */
    public void removeSelectionListener(SelectionListener l) {
        model.removeSelectionListener(l);
    }

    /**
     * Allows binding a listener to user selection actions
     *
     * @param l the action listener to be added
     */
    public void addActionListener(ActionListener l) {
        dispatcher.addListener(l);
    }

    /**
     * Allows binding a listener to user selection actions
     *
     * @param l the action listener to be removed
     */
    public void removeActionListener(ActionListener l) {
        dispatcher.removeListener(l);
    }

    /**
     * @inheritDoc
     */
    protected void fireActionEvent() {
        if(isEnabled()){
            if(disposeDialogOnSelection) {
                ((Dialog)getComponentForm()).dispose();
            }
            super.fireActionEvent();
            ActionEvent a = new ActionEvent(eventSource);
            dispatcher.fireActionEvent(a);
            if(isCommandList() && !a.isConsumed()) {
                Object i = getSelectedItem();
                if(i != null && i instanceof Command) {
                    ((Command)i).actionPerformed(a);
                    if(!a.isConsumed()) {
                        Form f = getComponentForm();
                        if(f != null) {
                            f.actionCommandImpl((Command)i);
                        }
                    }
                }
            }
        }
    }

    /**
     * A list can start handling input implicitly upon gaining focus, this can
     * make for a more intuitive UI when no other focus elements exist or when
     * their use case is infrequent. However, it might be odd in some cases
     * where the list "steals" focus.
     *
     * @param inputOnFocus true is a list can start handling input
     * implicitly upon gaining focus
     */
    public void setInputOnFocus(boolean inputOnFocus) {
        this.inputOnFocus = inputOnFocus;
    }

    /**
     * This method determines if the animated focus is drawn on top of the List
     * or behind the List when moving.
     *
     * @param paintFocusBehindList
     */
    public void setPaintFocusBehindList(boolean paintFocusBehindList) {
        this.paintFocusBehindList = paintFocusBehindList;
    }

    /**
     * @inheritDoc
     */
    void focusGainedInternal() {
        if (inputOnFocus) {
            setHandlesInput(true);
        }
    }

    /**
     * @inheritDoc
     */
    void focusLostInternal() {
    }

    /**
     * Returns the gap between items
     *
     * @return the gap between items
     */
    public int getItemGap() {
        return itemGap;
    }

    /**
     * Set the gap between items
     *
     * @param itemGap the gap between items
     */
    public void setItemGap(int itemGap) {
        this.itemGap = itemGap;
    }

    /**
     * The rendering prototype is optionally used in calculating the size of the
     * List and is recommended for performance reasons. You should invoke it with an object
     * representing a theoretical value in the list which will be used to calculate
     * the size required for each element in the list.
     * <p>This allows list size calculations to work across look and feels and allows
     * developers to predetermin size for list elements.
     * <p>e.g. For a list of Strings which you would like to always be 5 characters wide
     * you can use a prototype "XXXXX" which would use the preferred size of the XXXXX
     * String to determine the size of the list element. E.g. for a list of dates you can use
     * new Date(30, 12, 00) etc..
     *
     * @param renderingPrototype a value that can be passed to the renderer to indicate the preferred
     * size of a list component.
     */
    public void setRenderingPrototype(Object renderingPrototype) {
        this.renderingPrototype = renderingPrototype;
    }

    /**
     * See set rendering prototype
     *
     * @see #setRenderingPrototype(java.lang.Object)
     * @return the value of the rendering prototype
     */
    public Object getRenderingPrototype() {
        return renderingPrototype;
    }

    /**
     * Calculates the default size for elements on the list
     *
     * @return the default dimension for elements in a list
     */
    Dimension getElementSize(boolean selected, boolean addMargin) {
        if (selected) {
            if (selectedElemSize == null) {
                // don't keep element size if there are no elements and no prototype...
                if (renderingPrototype == null) {
                    if (model.getSize() == 0) {
                        // put a sensible value as default when there are no elements or rendering prototype
                        if(addMargin) {
                            return new Label("XXXXXX").getPreferredSizeWithMargin();
                        }
                        return new Label("XXXXXX").getPreferredSize();
                    }
                }
                selectedElemSize = calculateElementSize(true, addMargin);
            }
            return selectedElemSize;
        } else {
            if (elemSize == null) {
                // don't keep element size if there are no elements and no prototype...
                if (renderingPrototype == null) {
                    if (model.getSize() == 0) {
                        // put a sensible value as default when there are no elements or rendering prototype
                        Label l = new Label("XXXXXX");
                        if(addMargin) {
                            return l.getPreferredSizeWithMargin();
                        } else {
                            return l.getPreferredSize();
                        }
                    }
                }
                elemSize = calculateElementSize(false, addMargin);
            }
            return elemSize;
        }
    }

    /**
     * Calculates the size of an element based on a forumla or on rendering prototype
     */
    private Dimension calculateElementSize(boolean selected, boolean addMargin) {
        if (renderingPrototype != null) {
            Component unselected = renderer.getListCellRendererComponent(this, renderingPrototype, 0, selected);
            if(addMargin) {
                return unselected.getPreferredSizeWithMargin();
            } else {
                return unselected.getPreferredSize();
            }
        }
        int width = 0;
        int height = 0;
        int elements = Math.min(5, model.getSize());
        int marginY = 0;
        int marginX = 0;
        for (int iter = 0; iter < elements; iter++) {
            Component cmp = renderer.getListCellRendererComponent(this, model.getItemAt(iter), iter, selected);
            Dimension d = cmp.getPreferredSize();
            width = Math.max(width, d.getWidth());
            height = Math.max(height, d.getHeight());
            if(iter == 0) {
                Style s = cmp.getStyle();
                marginY = s.getMargin(TOP) + s.getMargin(BOTTOM);
                marginX = s.getMargin(LEFT) + s.getMargin(RIGHT);
            }
        }
        return new Dimension(width + marginX, height + marginY);
    }



    /**
     * @inheritDoc
     */
    protected void longPointerPress(int x, int y) {
        int s = pointerSelect(x, y);
        if(s > -1) {
            model.setSelectedIndex(s);
        }
    }

    /**
     * @inheritDoc
     */
    public void pointerPressed(int x, int y) {
        if(fixedSelection > FIXED_NONE_BOUNDRY) {
            // for a fixed list we need to store the initial drag position
            if(isSmoothScrolling()) {
                if(orientation != HORIZONTAL) {
                    initialFixedDrag = y;
                } else {
                    initialFixedDrag = x;
                }
                initialFixedSelection = getModel().getSelectedIndex();
            }
        }
        // prevent a hover event from activating the drag in case of a click screen,
        // this is essential for the Storm device
        setDragActivated(false);
        super.pointerPressed(x, y);
    }

    /**
     * @inheritDoc
     */
    public void pointerHover(int[] x, int[] y) {
        clearDrag();
        if(!isDragActivated()) {
            int selection = pointerSelect(x[0], y[0]);
            if (selection > -1) {
                model.setSelectedIndex(selection);
            }
        }
        pointerDraggedImpl(x[0], y[0]);
    }

    /**
     * @inheritDoc
     */
    public void pointerDragged(int x, int y) {
        pointerDraggedImpl(x, y);
    }

    private void pointerDraggedImpl(int x, int y) {
        if (isSmoothScrolling()) {
            if(fixedSelection < FIXED_NONE_BOUNDRY) {
                super.pointerDragged(x, y);
            } else {
                if(getModel().getSize() > 2) {
                    // a fixed list is not scrollable we need to implement dragging ourselves...
                    int offset;
                    int componentSize;

                    if(orientation != HORIZONTAL) {
                        offset = y - initialFixedDrag;
                        componentSize = getElementSize(false, true).getHeight();
                    } else {
                        offset = x - initialFixedDrag;
                        componentSize = getElementSize(false, true).getWidth();
                    }

                    if(!isDragActivated()) {
                        if(offset > 4) {
                            setDragActivated(true);
                        }
                    }
                    if(isDragActivated()) {

                        int elementSelection = initialFixedSelection + (offset / componentSize) % getModel().getSize();
                        if(elementSelection < 0) {
                            elementSelection += getModel().getSize();
                        }
                        getModel().setSelectedIndex(elementSelection);
                    }
                }


                int numOfcomponents = getModel().getSize();
                Style style = getStyle();
                int width = getWidth() - style.getPadding(false, RIGHT) - style.getPadding(false, LEFT) - getSideGap();
                if (isScrollableX()) {
                    width = Math.max(width, getScrollDimension().getWidth() - style.getPadding(false, RIGHT) - style.getPadding(false, LEFT) - getSideGap());
                }
                Dimension rendererSize = getElementSize(false, true);
                Dimension selectedSize = getElementSize(true, true);
                Rectangle pos = new Rectangle();
                for (int i = 0; i < numOfcomponents; i++) {
                    calculateComponentPosition(i, width, pos, rendererSize, selectedSize, true);
                    if (pos.contains(x, y)) {
                        model.setSelectedIndex(i);
                        break;
                    }
                }
            }
        } else {
            model.setSelectedIndex(pointerSelect(x, y));
        }
    }

    private int pointerSelect(int x, int y) {
        int selectedIndex = -1;
        int numOfcomponents = getModel().getSize();
        Style style = getStyle();

        Dimension rendererSize = getElementSize(false, true);
        Dimension selectedSize = getElementSize(true, true);

        Rectangle pos = new Rectangle();
        int width = getWidth() - style.getPadding(false, RIGHT) - style.getPadding(false, LEFT) - getSideGap();
        if (isScrollableX()) {
            width = Math.max(width, getScrollDimension().getWidth() - style.getPadding(false, RIGHT) - style.getPadding(false, LEFT) - getSideGap());
        }
        y = y - getAbsoluteY();
        x = x - getAbsoluteX();

        if (fixedSelection < FIXED_NONE_BOUNDRY) {
            calculateComponentPosition(getSelectedIndex(), width, pos, rendererSize, getElementSize(true, true), true);

            if (orientation != HORIZONTAL) {
                if(y < pos.getY()){
                    selectedIndex = y / (rendererSize.getHeight() + itemGap);
                }else{
                    int current = getSelectedIndex();
                    if(y < pos.getY() + selectedSize.getHeight()){
                        selectedIndex = current;
                    }else{
                        selectedIndex = (current+1) + (y - (pos.getY() + selectedSize.getHeight()))/(rendererSize.getHeight() + itemGap);
                    }
                }
            } else {
                if (isRTL()) {
                	if (x > pos.getX()+selectedSize.getWidth()) {
                		int delta=x-(pos.getX()+selectedSize.getWidth());
                		delta /= (rendererSize.getWidth() + itemGap);

                        // should have been -1-delta, but works better like this.
                		selectedIndex = getSelectedIndex()-delta;
                	} else {
	                    if(x >= pos.getX()){
	                        selectedIndex = getSelectedIndex();
	                    } else {
	                        int delta=pos.getX()-x;
	                        delta/=(rendererSize.getWidth() + itemGap);
	                        selectedIndex=getSelectedIndex()+1+delta;
	                    }
                	}
                } else {
                    if(x < pos.getX()){
                        selectedIndex = x / (rendererSize.getWidth() + itemGap);
                    }else{
                        int current = getSelectedIndex();
                        if(x < pos.getX() + selectedSize.getWidth()){
                            selectedIndex = current;
                        }else{
                            selectedIndex = (current+1) + (x - (pos.getX() + selectedSize.getWidth()))/(rendererSize.getWidth() + itemGap);
                        }
                    }
                }
            }
        } else {
            for (int i = 0; i < numOfcomponents; i++) {
                calculateComponentPosition(i, width, pos, rendererSize, selectedSize, true);
                if (pos.contains(x, y)) {
                    selectedIndex = i;
                    break;
                }
            }
        }

        if (selectedIndex < 0 || selectedIndex >= size()) {
            return -1;
        }
        return selectedIndex;
    }

    /**
     * This method determines if the List fires the action event when the pointer
     * was clicked on one of the items, or only if the item was the selected item
     * By default the value is true.
     *
     * @param fireOnClick
     */
    public void setFireOnClick(boolean fireOnClick){
        this.fireOnClick = fireOnClick;
    }

    /**
     * @inheritDoc
     */
    public void pointerHoverReleased(int[] x, int[] y) {
        pointerReleasedImpl(x[0], y[0], true);
    }

    private void pointerReleasedImpl(int x, int y, boolean isHover) {
        if (isDragActivated()) {
            if(fixedSelection < FIXED_NONE_BOUNDRY) {
                super.pointerReleased(x, y);
            } else {
                if(isScrollableY()){
                    destination += Display.getInstance().getDragSpeed(true);
                }else{
                    destination += Display.getInstance().getDragSpeed(false) * 1000;
                }
                if(destination < 0) {
                    destination += model.getSize();
                    if(destination >= model.getSize()) {
                        destination %= model.getSize();
                    }
                } else {
                    if(destination >= model.getSize()) {
                        destination -= model.getSize();
                        if(destination >= model.getSize()) {
                            destination %= model.getSize();
                        }
                    }
                }

                initListMotion();
                setDragActivated(false);
            }
            return;
        }
        int current = model.getSelectedIndex();
        int selection = pointerSelect(x, y);
        if (selection > -1) {
            model.setSelectedIndex(selection);
            if(!isHover) {
                if(fireOnClick){
                    fireActionEvent();
                }else{
                    if (current == selection) {
                        fireActionEvent();
                    }
                }
            }
        }
    }

    /**
     * @inheritDoc
     */
    public void pointerReleased(int x, int y) {
        pointerReleasedImpl(x, y, false);
    }

    /**
     * @inheritDoc
     */
    protected Dimension calcPreferredSize() {
        return UIManager.getInstance().getLookAndFeel().getListPreferredSize(this);
    }

    /**
     * Allows adding an element to a list if the underlying model supports this, notice that
     * it is an optional operation and if the model does not support it (default list model does)
     * then this operation may failed.
     *
     * @param item the item to be added to a list model
     */
    public void addItem(Object item) {
        model.addItem(item);
    }

    /**
     * Indicates whether selection is fixable to place in which case all the
     * elements in the list move and selection stays in place.
     *
     * @return one of: FIXED_NONE, FIXED_TRAIL, FIXED_LEAD, FIXED_CENTER, FIXED_NONE_CYCLIC
     */
    public int getFixedSelection() {
        return fixedSelection;
    }

    /**
     * Indicates whether selection is fixable to place in which case all the
     * elements in the list move and selection stays in place.
     *
     * @param fixedSelection one of: FIXED_NONE, FIXED_TRAIL, FIXED_LEAD,
     * FIXED_CENTER, FIXED_NONE_CYCLIC
     */
    public void setFixedSelection(int fixedSelection) {
        this.fixedSelection = fixedSelection;
    }

    /**
     * @inheritDoc
     */
    public boolean animate() {
        // parent is performing the animation we shouldn't do anything in this case
        // this is the scrolling animation which we don't want to interfear with
        boolean parentFinished = super.animate();
        if (animationPosition != 0) {
            if (animationPosition < 0) {
                animationPosition = Math.min(listMotion.getValue() - destination, 0);
            } else {
                animationPosition = Math.max(destination - listMotion.getValue(), 0);
            }
            if(animationPosition == 0) {
                Form p = getComponentForm();
                if(p != null) {
                    p.deregisterAnimatedInternal(this);
                }
            }
            return true;
        }
        return parentFinished;
    }

    /**
     * Setting the surrounding border gap
     *
     * @param borderGap number of pixels for the gap
     */
    public void setBorderGap(int borderGap) {
        this.borderGap = borderGap;
    }

    /**
     * Getting the surrounding border gap
     *
     * @return border gap in pixels
     */
    public int getBorderGap() {
        return borderGap;
    }

    /**
     * This flag indicates to the List if the List should scroll to the selected
     * element when it's been initialized.
     *
     * @param scrollToSelected if true the List scrolls to the selected element
     * when It's been initalized.
     */
    public void setScrollToSelected(boolean scrollToSelected) {
        this.scrollToSelected = scrollToSelected;
    }


    /**
     * @inheritDoc
     */
    protected String paramString() {
        String elemSizeStr = "element size = ";
        if (elemSize != null) {
            elemSizeStr += elemSize.toString();
        }
        return super.paramString() + ", " + elemSizeStr +
                ", itemGap = " + itemGap +
                ", orientation = " + orientation +
                ", selected index = " + getSelectedIndex() +
                ", size = " + size();
    }
}
