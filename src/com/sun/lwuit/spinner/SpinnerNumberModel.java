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

package com.sun.lwuit.spinner;

import com.sun.lwuit.events.DataChangedListener;
import com.sun.lwuit.events.SelectionListener;
import com.sun.lwuit.list.ListModel;
import com.sun.lwuit.util.EventDispatcher;

/**
 * Represents a numeric model for the spinner
 *
 * @author Shai Almog
 */
class SpinnerNumberModel implements ListModel {
    private EventDispatcher dataListener = new EventDispatcher();
    private EventDispatcher selectionListener = new EventDispatcher();
    private double min;
    private double max;
    private double currentValue;
    private double step;
    boolean realValues;

    void setValue(Object value) {
        if(value instanceof Integer) {
            currentValue = ((Integer)value).doubleValue();
        } else {
            currentValue = ((Double)value).doubleValue();
        }
    }

    Object getValue() {
        if(realValues) {
            return new Double(currentValue);
        }
        return new Integer((int)currentValue);
    }

    /**
     * Indicates the range of the spinner
     * 
     * @param min lowest value allowed
     * @param max maximum value allowed
     * @param currentValue the starting value for the mode
     * @param step the value by which we increment the entries in the model
     */
    public SpinnerNumberModel(int min, int max, int currentValue, int step) {
        this.max = max;
        this.min = min;
        this.currentValue = currentValue;
        this.step = step;
    }

    /**
     * Indicates the range of the spinner
     *
     * @param min lowest value allowed
     * @param max maximum value allowed
     * @param currentValue the starting value for the mode
     * @param step the value by which we increment the entries in the model
     */
    public SpinnerNumberModel(double min, double max, double currentValue, double step) {
        this.max = max;
        this.min = min;
        this.currentValue = currentValue;
        this.step = step;
        realValues = true;
    }

    /**
     * @inheritDoc
     */
    public Object getItemAt(int index) {
        if(realValues) {
            return new Double(min + step * index);
        }
        return new Integer((int)(min + step * index));
    }


    /**
     * @inheritDoc
     */
    public int getSize() {
        return (int)((max - min) / step);
    }


    /**
     * @inheritDoc
     */
    public int getSelectedIndex() {
        int v = getSize() - (int)((max - currentValue) / step);
        return v;
    }


    /**
     * @inheritDoc
     */
    public void setSelectedIndex(int index) {
        int oldIndex = getSelectedIndex();
        currentValue = min + index * step;
        int newIndex = getSelectedIndex();
        selectionListener.fireSelectionEvent(oldIndex, newIndex);
    }

    /**
     * @inheritDoc
     */
    public void addDataChangedListener(DataChangedListener l) {
        dataListener.addListener(l);
    }

    /**
     * @inheritDoc
     */
    public void removeDataChangedListener(DataChangedListener l) {
        dataListener.removeListener(l);
    }

    /**
     * @inheritDoc
     */
    public void addSelectionListener(SelectionListener l) {
        selectionListener.addListener(l);
    }

    /**
     * @inheritDoc
     */
    public void removeSelectionListener(SelectionListener l) {
        selectionListener.removeListener(l);
    }

    /**
     * @inheritDoc
     */
    public void addItem(Object item) {
    }

    /**
     * @inheritDoc
     */
    public void removeItem(int index) {
    }
}
