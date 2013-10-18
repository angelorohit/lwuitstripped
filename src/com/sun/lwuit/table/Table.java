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
package com.sun.lwuit.table;

import com.sun.lwuit.Component;
import com.sun.lwuit.Container;
import com.sun.lwuit.Form;
import com.sun.lwuit.Graphics;
import com.sun.lwuit.Label;
import com.sun.lwuit.TextArea;
import com.sun.lwuit.TextField;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.events.DataChangedListener;
import com.sun.lwuit.plaf.Border;
import com.sun.lwuit.plaf.Style;

/**
 * The table class represents a grid of data that can be used for rendering a grid
 * of components/labels. The table reflects and updates the underlying model data.
 *
 * @author Shai Almog
 */
public class Table extends Container {
    private TableModel model;
    private Listener listener = new Listener();
    private boolean drawBorder = true;

    /**
     * Indicates the alignment of the title see label alignment for details
     * 
     * @see com.sun.lwuit.Label#setAlignment(int) 
     */
    private int titleAlignment = Label.CENTER;

    /**
     * Indicates the alignment of the cells see label alignment for details
     * 
     * @see com.sun.lwuit.Label#setAlignment(int)
     */
    private int cellAlignment = Label.LEFT;

    /**
     * Create a table with a new model
     *
     * @param model the model underlying this table
     */
    public Table(TableModel model) {
        this.model = model;
        updateModel();
        setUIID("Table");
    }

    private void updateModel() {
        int selectionRow = -1, selectionColumn = -1;
        Form f = getComponentForm();
        if(f != null) {
            Component c = f.getFocused();
            if(c != null) {
                selectionRow = getCellRow(c);
                selectionColumn = getCellColumn(c);
            }
        }
        removeAll();
        int columnCount = model.getColumnCount();

        // another row for the table header
        setLayout(new TableLayout(model.getRowCount() + 1, columnCount));
        for(int iter = 0 ; iter < columnCount ; iter++) {
            Component header = createCellImpl(model.getColumnName(iter), -1, columnCount, false);
            addComponent(null, header);
        }

        for(int r = 0 ; r < model.getRowCount() ; r++) {
            for(int c = 0 ; c < columnCount ; c++) {
                Object value = model.getValueAt(r, c);
                boolean e = model.isCellEditable(r, c);
                Component cell = createCellImpl(value, r, c, e);
                addComponent(null, cell);
                cell.setFocusable(true);
                if(r == selectionRow && c == selectionColumn) {
                    cell.requestFocus();
                }
            }
        }
    }

    private Component createCellImpl(Object value, final int row, final int column, boolean editable) {
        Component c = createCell(value, row, column, editable);
        c.putClientProperty("row", new Integer(row));
        c.putClientProperty("column", new Integer(column));
        
        // we do this here to allow subclasses to return a text area or its subclass
        if(c instanceof TextArea) {
            ((TextArea)c).addActionListener(listener);
        } 

        Style s = c.getSelectedStyle();
        s.setMargin(0, 0, 0, 0);
        if(drawBorder) {
            Border b = new Border() {
                public void paint(Graphics g, Component c) {
                    g.setColor(getUnselectedStyle().getFgColor());
                    if(row == getModel().getRowCount() - 1) {
                        if(column == getModel().getColumnCount() - 1) { 
                            g.drawRect(c.getX(), c.getY(), c.getWidth() - 1, c.getHeight() - 1);
                        } else {
                            g.drawRect(c.getX(), c.getY(), c.getWidth(), c.getHeight() - 1);
                        }
                    } else {
                        if(column == getModel().getColumnCount() - 1) {
                            g.drawRect(c.getX(), c.getY(), c.getWidth() - 1, c.getHeight());
                        } else {
                            g.drawRect(c.getX(), c.getY(), c.getWidth(), c.getHeight());
                        }
                    }
                }
            };
            s.setBorder(b);
            s = c.getUnselectedStyle();
            s.setBorder(b);
        } else {
            s = c.getUnselectedStyle();
        }
        s.setBgTransparency(0);
        s.setMargin(0, 0, 0, 0);
        return c;
    }

    /**
     * Creates a cell based on the given value
     *
     * @param value the new value object
     * @param row row number, -1 for the header rows
     * @param column column number
     * @param editable true if the cell is editable
     * @return cell component instance
     */
    protected Component createCell(Object value, int row, int column, boolean editable) {
        if(row == -1) {
            Label header = new Label((String)value);
            header.setUIID("TableHeader");
            header.setAlignment(titleAlignment);
            return header;
        }
        if(editable) {
            TextField cell = new TextField(value.toString(), -1);
            cell.setLeftAndRightEditingTrigger(false);
            cell.setUIID("TableCell");
            return cell;
        }
        Label cell = new Label(value.toString());
        cell.setUIID("TableCell");
        cell.setAlignment(cellAlignment);
        return cell;
    }

    /**
     * @inheritDoc
     */
    public void initComponent() {
        model.addDataChangeListener(listener);
    }

    /**
     * @inheritDoc
     */
    public void deinitialize() {
        model.removeDataChangeListener(listener);
    }

    /**
     * Replaces the underlying model
     *
     * @param model the new model
     */
    public void setModel(TableModel model) {
        this.model = model;
        updateModel();
        revalidate();
    }

    /**
     * Returns the model instance
     *
     * @return the model instance
     */
    public TableModel getModel() {
        return model;
    }

    /**
     * @return the drawBorder
     */
    public boolean isDrawBorder() {
        return drawBorder;
    }

    /**
     * @param drawBorder the drawBorder to set
     */
    public void setDrawBorder(boolean drawBorder) {
        this.drawBorder = drawBorder;
        repaint();
    }

    /**
     * Indicates the alignment of the title see label alignment for details
     *
     * @return the title alignment
     * @see com.sun.lwuit.Label#setAlignment(int)
     */
    public int getTitleAlignment() {
        return titleAlignment;
    }

    /**
     * Indicates the alignment of the title see label alignment for details
     *
     * @param titleAlignment the title alignment
     * @see com.sun.lwuit.Label#setAlignment(int)
     */
    public void setTitleAlignment(int titleAlignment) {
        this.titleAlignment = titleAlignment;
        repaint();
    }


    /**
     * Returns the column in which the given cell is placed
     * 
     * @param cell the component representing the cell placed in the table
     * @return the column in which the cell was placed in the table
     */
    public int getCellColumn(Component cell) {
        Integer i = ((Integer)cell.getClientProperty("column"));
        if(i != null) {
            return i.intValue();
        }
        return -1;
    }

    /**
     * Returns the row in which the given cell is placed
     * 
     * @param cell the component representing the cell placed in the table
     * @return the row in which the cell was placed in the table
     */
    public int getCellRow(Component cell) {
        Integer i = ((Integer)cell.getClientProperty("row"));
        if(i != null) {
            return i.intValue();
        }
        return -1;
    }

    /**
     * Indicates the alignment of the cells see label alignment for details
     *
     * @see com.sun.lwuit.Label#setAlignment(int)
     * @return the cell alignment
     */
    public int getCellAlignment() {
        return cellAlignment;
    }

    /**
     * Indicates the alignment of the cells see label alignment for details
     *
     * @param cellAlignment the table cell alignment
     * @see com.sun.lwuit.Label#setAlignment(int)
     */
    public void setCellAlignment(int cellAlignment) {
        this.cellAlignment = cellAlignment;
        repaint();
    }


    class Listener implements DataChangedListener, ActionListener {
        /**
         * @inheritDoc
         */
        public final void dataChanged(int row, int column) {
            Object value = model.getValueAt(row, column);
            boolean e = model.isCellEditable(row, column);
            Component cell = createCellImpl(value, row, column, e);
            TableLayout t = (TableLayout)getLayout();
            removeComponent(t.getComponentAt(row + 1, column));
            addComponent(t.createConstraint(row + 1, column), cell);
            layoutContainer();
            cell.setFocusable(true);
            cell.requestFocus();
            revalidate();
        }

        public void actionPerformed(ActionEvent evt) {
            TextArea t = (TextArea)evt.getSource();
            int row = getCellRow(t);
            int column = getCellColumn(t);
            getModel().setValueAt(row, column, t.getText());
        }
    }
}
