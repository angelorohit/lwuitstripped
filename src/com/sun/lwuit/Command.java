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

import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;

/**
 * The action placed on the soft buttons and in the Menu on devices, similar to the
 * MIDP command abstraction and Swing's Actions. Unlike the MIDP abstraction this class 
 * can be derived to implement the right behavior
 * 
 * @author Nir Shabi
 */
public class Command implements ActionListener{
    private boolean disposesDialog = true;
    private Image icon;
    private String command;
    
    /**
     * Simplifies code dealing with commands allowing them to be used in switch statements
     * more easily
     */
    private int commandId;
    
    /**
     * Creates a new instance of Command
     * 
     * @param command the string that will be placed on the Soft buttons\Menu
     */
    public Command(String command) {
        this.command = command;
    }

    /**
     * Creates a new instance of Command
     * 
     * @param command the string that will be placed on the Soft buttons\Menu
     * @param icon the icon representing the command
     */
    public Command(String command, Image icon) {
        this.command = command;
        this.icon = icon;
    }


    /**
     * Creates a new instance of Command
     * 
     * @param command the string that will be placed on the Soft buttons\Menu
     * @param id user defined ID for a command simplifying switch statement code
     * working with a command
     */
    public Command(String command, int id) {
        this.command = command;
        this.commandId = id;
    }
    
    /**
     * Creates a new instance of Command
     * 
     * @param command the string that will be placed on the Soft buttons\Menu
     * @param icon the icon representing the command
     * @param id user defined ID for a command simplifying switch statement code
     * working with a command
     */
    public Command(String command, Image icon, int id) {
        this.command = command;
        this.commandId = id;
        this.icon = icon;
    }
    
    /**
     * Creates a new instance of Command
     * 
     * @param command the string that will be placed on the Soft buttons\Menu
     * @param id user defined ID for a command simplifying switch statement code
     * working with a command
     * @param defaultAction Indicates that this action should occur by default 
     * on fire action event
     * @deprecated this functionality is no longer supported use Form.setDefaultCommand() instead
     */
    public Command(String command, int id, boolean defaultAction) {
        this.command = command;
        this.commandId = id;
    }

    /**
     * Creates a new instance of Command
     * 
     * @param command the string that will be placed on the Soft buttons\Menu
     * @param defaultAction Indicates that this action should occur by default 
     * on fire action event
     * @deprecated this functionality is no longer supported use Form.setDefaultCommand() instead
     */
    public Command(String command, boolean defaultAction) {
        this.command = command;
    }

    /**
     * Return the command ID
     * 
     * @return the command ID
     */
    public int getId() {
        return commandId;
    }
    
    /**
     * gets the Command Name
     * 
     * @return the Command name
     */
    public String getCommandName() {
        return command;
    }

    void setCommandName(String command) {
        this.command = command;
    }
    
    /**
     * Returns the icon representing the command
     * 
     * @return an icon representing the command
     */
    public Image getIcon() {
        return icon;
    }
    
    /**
     * Returns a string representation of the object
     * 
     * @return Returns a string representation of the object
     */
    public String toString() {
        return command;
    }
    
    /**
     * compare two commands
     * 
     * @param obj a Command Object to compare
     * @return true if the obj has the same command name
     */
    public boolean equals(Object obj) {
        if(!(obj instanceof Command)) {
            return false;
        }
        if(((Command)obj).command == null) {
            return (obj != null) && obj.getClass() == getClass() && command == null &&
                ((Command)obj).icon == icon && ((Command)obj).commandId == commandId;
        } else {
            return (obj != null) && obj.getClass() == getClass() && ((Command)obj).command.equals(command) &&
                ((Command)obj).icon == icon && ((Command)obj).commandId == commandId;
        }
    }

    /**
     * Allows storing commands in a vector/hashtable
     * 
     * @return unique hashcode for the command class
     */
    public int hashCode() {
        return getClass().hashCode() + commandId;
    }
    
    /**
     * This method is called when the soft button/Menu item is clicked
     * 
     * @param evt the Event Object
     */
    public void actionPerformed(ActionEvent evt) {
    }

    /**
     * Indicates that this action should occur by default on fire action event
     * 
     * @return true if it is the default action event
     * @deprecated this functionality is no longer supported use Form.setDefaultCommand() instead
     */
    public boolean isDefaultAction() {
        return false;
    }

    /**
     * Setting the default action event
     * 
     * @param defaultAction the default action event
     * @deprecated this functionality is no longer supported use Form.setDefaultCommand() instead
     */
    public void setDefaultAction(boolean defaultAction) {
    }
    
    /**
     * Indicates whether this command causes the dialog to dispose implicitly
     */
    void setDisposesDialog(boolean disposesDialog) {
        this.disposesDialog = disposesDialog;
    }
    
    /**
     * Indicates whether this command causes the dialog to dispose implicitly
     */
    boolean isDisposesDialog() {
        return disposesDialog;
    }
}
