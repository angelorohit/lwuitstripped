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
package com.sun.lwuit.util;

import com.sun.lwuit.*;
import com.sun.lwuit.events.*;
import com.sun.lwuit.plaf.Style;
import java.util.Vector;

/**
 * Handles event dispatching while guaranteeing that all events would
 * be fired properly on the EDT regardless of their source. This class handles listener
 * registration/removal in a safe and uniform way. 
 * 
 * @author Shai Almog
 */
public class EventDispatcher {
    private Vector listeners;
    private Object[] pending;
    private Object pendingEvent;
    private final Runnable callback = new Runnable() {
        /**
         * Do not invoke this method it handles the dispatching internally and serves
         * as an implementation detail
         */
        public final void run() {
            if(!Display.getInstance().isEdt()) {
                throw new IllegalStateException("This method should not be invoked by external code!");
            }

            if(pending instanceof ActionListener[]) {
                fireActionSync((ActionListener[])pending, (ActionEvent)pendingEvent);
                return;
            }

            if(pending instanceof FocusListener[]) {
                fireFocusSync((FocusListener[])pending, (Component)pendingEvent);
                return;
            }

            if(pending instanceof DataChangedListener[]) {
                fireDataChangeSync((DataChangedListener[])pending, ((int[])pendingEvent)[0], ((int[])pendingEvent)[1]);
                return;
            }
            
            if(pending instanceof SelectionListener[]) {
                fireSelectionSync((SelectionListener[])pending, ((int[])pendingEvent)[0], ((int[])pendingEvent)[1]);
                return;
            }

            if(pending instanceof StyleListener[]) {
                Object[] p = (Object[])pendingEvent;
                fireStyleChangeSync((StyleListener[])pending, (String)p[0], (Style)p[1]);
                return;
            }
        }
    };
    
    /**
     * Add a listener to the dispatcher that would receive the events when they occurs
     * 
     * @param listener a dispatcher listener to add
     */
    public synchronized void addListener(Object listener) {
        if(listeners == null) {
            listeners = new Vector();
        }
        listeners.addElement(listener);
    }
    
    /**
     * Returns the vector of the listeners
     * 
     * @return the vector of listeners attached to the event dispatcher
     */
    public Vector getListenerVector() {
        return listeners;
    }

    /**
     * Remove the listener from the dispatcher
     *
     * @param listener a dispatcher listener to remove
     */
    public synchronized void removeListener(Object listener) {
        if(listeners != null) {
            listeners.removeElement(listener);
        }
    }

    /**
     * Fires the event safely on the EDT without risk of concurrency errors
     * 
     * @param ev the ActionEvent to fire to the listeners
     */
    public void fireDataChangeEvent(int index, int type) {
        if(listeners == null || listeners.size() == 0) {
            return;
        }
        DataChangedListener[] array;
        synchronized(this) {
            array = new DataChangedListener[listeners.size()];
            for(int iter = 0 ; iter < array.length ; iter++) {
                array[iter] = (DataChangedListener)listeners.elementAt(iter);
            }
        }
        // if we already are on the EDT just fire the event
        if(Display.getInstance().isEdt()) {
            fireDataChangeSync(array, type, index);
        } else {
            pending = array;
            pendingEvent = new int[] {type, index};
            Display.getInstance().callSeriallyAndWait(callback);
        }
    }
    
    /**
     * Fires the style change even to the listeners
     *
     * @param property the property name for the event
     * @param source the style firing the event
     */
    public void fireStyleChangeEvent(String property, Style source) {
        if(listeners == null || listeners.size() == 0) {
            return;
        }
        StyleListener[] array;
        synchronized(this) {
            array = new StyleListener[listeners.size()];
            for(int iter = 0 ; iter < array.length ; iter++) {
                array[iter] = (StyleListener)listeners.elementAt(iter);
            }
        }
        // if we already are on the EDT just fire the event
        if(Display.getInstance().isEdt()) {
            fireStyleChangeSync(array, property, source);
        } else {
            pending = array;
            pendingEvent = new Object[] {property, source};
            Display.getInstance().callSerially(callback);
        }
    }

    /**
     * Synchronious internal call for common code
     */
    private void fireDataChangeSync(DataChangedListener[] array, int type, int index) {
        for(int iter = 0 ; iter < array.length ; iter++) {
            array[iter].dataChanged(type, index);
        }
    }
    
    /**
     * Synchronious internal call for common code
     */
    private void fireStyleChangeSync(StyleListener[] array, String property, Style source) {
        for(int iter = 0 ; iter < array.length ; iter++) {
            array[iter].styleChanged(property, source);
        }
    }

    /**
     * Synchronious internal call for common code
     */
    private void fireSelectionSync(SelectionListener[] array, int oldSelection, int newSelection) {
        for(int iter = 0 ; iter < array.length ; iter++) {
            array[iter].selectionChanged(oldSelection, newSelection);
        }
    }
    
    /**
     * Fires the event safely on the EDT without risk of concurrency errors
     * 
     * @param ev the ActionEvent to fire to the listeners
     */
    public void fireActionEvent(ActionEvent ev) {
        if(listeners == null || listeners.size() == 0) {
            return;
        }
        ActionListener[] array;
        synchronized(this) {
            array = new ActionListener[listeners.size()];
            for(int iter = 0 ; iter < array.length ; iter++) {
                array[iter] = (ActionListener)listeners.elementAt(iter);
            }
        }
        // if we already are on the EDT just fire the event
        if(Display.getInstance().isEdt()) {
            fireActionSync(array, ev);
        } else {
            pending = array;
            pendingEvent = ev;
            Display.getInstance().callSeriallyAndWait(callback);
        }
    }


    /**
     * Fires the event safely on the EDT without risk of concurrency errors
     * 
     * @param oldSelection old selection
     * @param newSelection new selection
     */
    public void fireSelectionEvent(int oldSelection, int newSelection) {
        if(listeners == null || listeners.size() == 0) {
            return;
        }
        SelectionListener[] array;
        synchronized(this) {
            array = new SelectionListener[listeners.size()];
            for(int iter = 0 ; iter < array.length ; iter++) {
                array[iter] = (SelectionListener)listeners.elementAt(iter);
            }
        }
        // if we already are on the EDT just fire the event
        if(Display.getInstance().isEdt()) {
            fireSelectionSync(array, oldSelection, newSelection);
        } else {
            pending = array;
            pendingEvent = new int[] {oldSelection, newSelection};
            Display.getInstance().callSeriallyAndWait(callback);
        }
    }
    
    /**
     * Synchronious internal call for common code
     */
    private void fireActionSync(ActionListener[] array, ActionEvent ev) {
        for(int iter = 0 ; iter < array.length ; iter++) {
            if(!ev.isConsumed()) {
                array[iter].actionPerformed(ev);
            }
        }
    }
    
    /**
     * Fires the event safely on the EDT without risk of concurrency errors
     * 
     * @param c the Component that gets the focus event
     */
    public void fireFocus(Component c) {
        if(listeners == null || listeners.size() == 0) {
            return;
        }
        FocusListener[] array;
        synchronized(this) {
            array = new FocusListener[listeners.size()];
            for(int iter = 0 ; iter < array.length ; iter++) {
                array[iter] = (FocusListener)listeners.elementAt(iter);
            }
        }
        // if we already are on the EDT just fire the event
        if(Display.getInstance().isEdt()) {
            fireFocusSync(array, c);
        } else {
            pending = array;
            pendingEvent = c;
            Display.getInstance().callSeriallyAndWait(callback);
        }
    }
    
    /**
     * Synchronious internal call for common code
     */
    private void fireFocusSync(FocusListener[] array, Component c) {
        if(c.hasFocus()) {
            for(int iter = 0 ; iter < array.length ; iter++) {
                array[iter].focusGained(c);
            }
        } else {
            for(int iter = 0 ; iter < array.length ; iter++) {
                array[iter].focusLost(c);
            }
        }
    }
}
