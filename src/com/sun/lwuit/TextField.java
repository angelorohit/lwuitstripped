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
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.events.DataChangedListener;
import com.sun.lwuit.geom.Dimension;
import com.sun.lwuit.layouts.GridLayout;
import com.sun.lwuit.plaf.UIManager;
import java.util.Hashtable;
import java.util.Vector;

/**
 * Allows in place editing using a lightweight API without necessarily moving to
 * the external native text box. The main drawback in this approach is that editing
 * can't support features such as T9 and might not have the same keymapping or
 * behavior of the native text input.
 * <p>Notice that due to limitations of text area and text field input modes in
 * text area aren't properly supported since they won't work properly across devices.
 * To limit input modes please use the setInputModeOrder method. All constants 
 * declated in TextArea are ignored with the exception of PASSWORD.
 * 
 * @author Shai Almog
 */
public class TextField extends TextArea {
    private static boolean replaceMenuDefault = true;
    private long cursorBlinkTime = System.currentTimeMillis();
    private boolean drawCursor = true;
    private int cursorCharPosition = -1;
    private boolean pressedAndNotReleased;
    private long pressTime;
    private boolean useSoftkeys = true;
    private long releaseTime;
    private String previousText;
    private int commitTimeout = 1000;
    private boolean pendingCommit;
    private int pressCount = 0;
    private int lastKeyCode;
    private int pressedKeyCode;
    private static String clearText = "Clear";
    private static String t9Text = "T9";
    private boolean longClick;
    private Command originalClearCommand;
    private static Hashtable inputModes;    
    private static String[] defaultInputModeOrder = {"Abc", "ABC", "abc", "123"};
    private String inputMode = defaultInputModeOrder[0];
    private String[] inputModeOrder = defaultInputModeOrder;
    private static Vector firstUppercaseInputMode = new Vector();
    private int blinkOnTime = 800;
    private int blinkOffTime = 200;
    private static boolean qwertyAutoDetect = true;
    private boolean qwertyInitialized;
    private static boolean qwertyDevice;
    private boolean qwerty = qwertyDevice;
    private boolean replaceMenu = replaceMenuDefault;
    private Command[] originalCommands;
    private EventDispatcher listeners = new EventDispatcher();
    private boolean overwriteMode;
    private boolean enableInputScroll = true;

    private int keyFwd = Display.GAME_RIGHT;
    private int keyBack = Display.GAME_LEFT;
    
    /**
     * Indicates whether the left/right keys will trigger editing, this is true by default.
     * Left and right key edit trigger might be disabled for cases such as text field
     * positioned horizontally one next to the other.
     */
    private boolean leftAndRightEditingTrigger = true;

    /**
     * Key to change the input mode on the device
     */
    private static int defaultChangeInputModeKey = '#';
    
    /**
     * The default key for poping open the symbol dialog
     */
    private static int defaultSymbolDialogKey = '*';

    private Command selectCommand;
    
    /**
     * Set the text that should appear on the clear softkey
     * 
     * @param text localized text for the clear softbutton
     */
    public static void setClearText(String text) {
        clearText = text;
    }


    /**
     * Set the text that should appear on the T9 softkey
     * 
     * @param text text for the T9 softbutton
     */
    public static void setT9Text(String text) {
        t9Text = text;
    }

    /**
     * @inheritDoc
     */
    public boolean isEnableInputScroll() {
        return enableInputScroll;
    }

    /**
     * Indicates whether text field input should scroll to the right side when no
     * more room for the input is present.
     *
     * @param enableInputScroll true to enable scrolling to the side
     */
    public void setEnableInputScroll(boolean enableInputScroll) {
        this.enableInputScroll = enableInputScroll;
    }

    class CommandHandler extends Command {
        CommandHandler(String title, int id) {
            super(title, id);
        }

        public void actionPerformed(ActionEvent ev) {
            ev.consume();
            switch(getId()) {
                case 1:
                    // key press for activating clear causes keyRelease never to be 
                    // called triggering a long click, this code disables the long click
                    releaseTime = System.currentTimeMillis();
                    pressedAndNotReleased = false;
                    longClick = false;
                    deleteChar();
                    break;
                case 2:
                    ev.consume();
                    editString();
                    break;
            }
        }
    }

    private Command DELETE_COMMAND = new CommandHandler(clearText, 1);
    private Command T9_COMMAND = new CommandHandler(t9Text, 2);

    private static final char[] DEFAULT_SYMBOL_TABLE = new char[] {
        '.', ',', '?', '!', '$', '@', '\'', '-',
        '_', ')', '(', ':', ';', '&', '/', '~',
        '\\', '%', '*', '#', '+', '>', '=', '<',
        '"'
    };
    
    private static char[] symbolTable = DEFAULT_SYMBOL_TABLE;
    
    private static final String[] DEFAULT_KEY_CODES = {
        // 0
        " 0",
        // 1
        ".,?!'\"1-()@/:_",
        // 2
        "ABC2",
        // 3
        "DEF3",
        // 4
        "GHI4",
        // 5
        "JKL5",
        // 6
        "MNO6",
        // 7
        "PQRS7",
        // 8
        "TUV8",
        // 9
        "WXYZ9",
    };

    /**
     * Default constructor
     */
    public TextField() {
        super(1, 20);
        setUIID("TextField");
    }
    
    /**
     * Construct a text field with space reserved for columns
     * 
     * @param columns - the number of columns
     */
    public TextField(int columns) {
        super(1, columns);
        setUIID("TextField");
    }
    
    /**
     * Construct text field 
     * 
     * @param text the text of the field
     */
    public TextField(String text) {
        super(text, 1, 20);
        setUIID("TextField");
    }
    
    /**
     * Performs a backspace operation
     */
    public void deleteChar() {
        String text = getText();

        if(text.length() > 0) {
            if(cursorCharPosition > 0) {
                cursorCharPosition--;

                if(!overwriteMode) {
                    text = text.substring(0, cursorCharPosition) +
                        text.substring(cursorCharPosition + 1, text.length());
                    super.setText(text);
                    commitChange();
                    fireDataChanged(DataChangedListener.REMOVED, cursorCharPosition);
                }
            }
        }
    }
    
    /**
     * Construct text field 
     * 
     * @param text the text of the field
     * @param columns - the number of columns
     */
    public TextField(String text, int columns) {
        super(text, 1, columns);
    }
    

    /**
     * Construct text field/area depending on whether native in place editing is supported 
     * 
     * @param text the text of the field
     * @param columns - the number of columns
     * @return a text field if native in place editing is unsupported and a text area if it is
     */
    public static TextArea create(String text, int columns) {
        if(Display.getInstance().getImplementation().isNativeInputSupported()) {
            return new TextArea(text, 1, columns);
        }
        return new TextField(text, columns);
    }
    
    
    /**
     * Default factory method
     * 
     * @return a text field if native in place editing is unsupported and a text area if it is
     */
    public static TextArea create() {
        return create(20);
    }
    
    /**
     * Construct text field/area depending on whether native in place editing is supported 
     * 
     * @param columns - the number of columns
     * @return a text field if native in place editing is unsupported and a text area if it is
     */
    public static TextArea create(int columns) {
        return create("", columns);
    }
    
    /**
     * Construct text field/area depending on whether native in place editing is supported 
     * 
     * @param text the text of the field
     * @return a text field if native in place editing is unsupported and a text area if it is
     */
    public static TextArea create(String text) {
        return create(text, 20);
    }
        
    /**
     * Commit the changes made to the text field as a complete edit operation. This
     * is used in a numeric keypad to allow the user to repeatedly press a number
     * to change values.
     */
    protected void commitChange() {
        pendingCommit = false;
        previousText = null;
        pressCount = 0;
    }

    /**
     * @inheritDoc
     */
    public boolean isPendingCommit() {
        return pendingCommit;
    }
    
    /**
     * The amount of time in milliseconds it will take for a change to get committed into
     * the field.
     * 
     * @param commitTimeout indicates the amount of time that should elapse for a commit
     * to automatically occur
     */
    public void setCommitTimeout(int commitTimeout) {
        this.commitTimeout = commitTimeout;
    }
    
    /**
     * The amount of time in milliseconds it will take for a change to get committed into
     * the field.
     * 
     * @return the time for a commit timeout
     */
    public int getCommitTimeout() {
        return commitTimeout;
    }
    
    /**
     * Sets the current selected input mode matching one of the existing input
     * modes
     * 
     * @param inputMode the display name of the input mode by default the following modes
     * are supported: Abc, ABC, abc, 123
     */
    public void setInputMode(String inputMode) {
        this.inputMode = inputMode;
        repaint();
    }
    
    /**
     * @inheritDoc
     */
    public String getInputMode() {
        return inputMode;
    }
    
    /**
     * Indicates whether the key changes the current input mode
     * 
     * @param keyCode the code
     * @return true for the hash (#) key code
     */
    protected boolean isChangeInputMode(int keyCode) {
        return keyCode == defaultChangeInputModeKey;
    }

    private static void initInputModes() {
        if(inputModes == null) {
            firstUppercaseInputMode.addElement("Abc");
            inputModes = new Hashtable();
            Hashtable upcase = new Hashtable();
            for(int iter = 0 ; iter < DEFAULT_KEY_CODES.length ; iter++) {
                upcase.put(new Integer('0' + iter), DEFAULT_KEY_CODES[iter]);
            }
            
            inputModes.put("ABC", upcase);

            Hashtable lowcase = new Hashtable();
            for(int iter = 0 ; iter < DEFAULT_KEY_CODES.length ; iter++) {
                lowcase.put(new Integer('0' + iter), DEFAULT_KEY_CODES[iter].toLowerCase());
            }
            inputModes.put("abc", lowcase);

            Hashtable numbers = new Hashtable();
            for(int iter = 0 ; iter < 10 ; iter++) {
                numbers.put(new Integer('0' + iter), "" + iter);
            }
            inputModes.put("123", numbers);
        }
    }
    
    /**
     * Adds a new inputmode hashtable with the given name and set of values
     * 
     * @param name a unique display name for the input mode e.g. ABC, 123 etc...
     * @param values The key for the hashtable is an Integer keyCode and the value
     * is a String containing the characters to toggle between for the given keycode
     * @param firstUpcase indicates if this input mode in an input mode used for the special
     * case where the first letter is an upper case letter
     */
    public static void addInputMode(String name, Hashtable values, boolean firstUpcase) {
        initInputModes();
        inputModes.put(name, values);
        if(firstUpcase) {
            firstUppercaseInputMode.addElement(name);
        }
    }
    
    /**
     * @inheritDoc
     */
    public String[] getInputModeOrder() {
        return inputModeOrder;
    }
    
    /**
     * Sets the order in which input modes are toggled and allows disabling/hiding
     * an input mode
     * 
     * @param order the order for the input modes in this field
     */
    public void setInputModeOrder(String[] order) {
        inputModeOrder = order;
        inputMode = order[0];
    }
    
    /**
     * Returns the order in which input modes are toggled by default
     * 
     * @return the default order of the input mode
     */
    public static String[] getDefaultInputModeOrder() {
        return defaultInputModeOrder;
    }
    
    /**
     * Sets the order in which input modes are toggled by default and allows 
     * disabling/hiding an input mode
     * 
     * @param order the order for the input modes in all future created fields
     */
    public static void setDefaultInputModeOrder(String[] order) {
        defaultInputModeOrder = order;
    }
    
    /**
     * Used for the case of first sentence character should be upper case
     */
    private String pickLowerOrUpper(String inputMode) {
        // check the character before the cursor..
        int pos = cursorCharPosition - 1;

        // we have input which has moved the cursor position further
        if(pendingCommit) {
            pos--;
        }
        String text = getText();
        if(pos >= text.length()) {
            pos = text.length() - 1;
        }
        while(pos > -1) {
            if(text.charAt(pos) == '.') {
                return inputMode.toUpperCase();
            }
            if(text.charAt(pos) != ' ') {
                return inputMode.toLowerCase();
            }
            pos--;
        }
        return inputMode.toUpperCase();
    }
    
    /**
     * Returns the input mode for the ong click mode
     * 
     * @return returns 123 by default
     */
    protected String getLongClickInputMode() {
        return "123";
    }
    
    /**
     * Returns the character matching the given key code after the given amount
     * of user presses
     * 
     * @param pressCount number of times this keycode was pressed
     * @param keyCode the actual keycode input by the user
     * @param longClick does this click constitute a long click
     * @return the char mapping to this key or 0 if no appropriate char was found 
     * (navigation, input mode change etc...).
     */
    protected char getCharPerKeyCode(int pressCount, int keyCode, boolean longClick) {
        initInputModes();
        String input = inputMode;
        
        // if this is a first letter uppercase input mode then we need to pick either
        // the upper case mode or the lower case mode...
        if(longClick) {
            input = getLongClickInputMode();
        } else {
            if(firstUppercaseInputMode.contains(input)) {
                input = pickLowerOrUpper(input);
            }
        }
        
        Hashtable mode = (Hashtable)inputModes.get(input);
        String s = (String)mode.get(new Integer(keyCode));
        if(s != null) {
            pressCount = pressCount % s.length();
            return s.charAt(pressCount);
        }
        return 0;
    }
    
    /**
     * Blocks the text area from opening the native text box editing on touchscreen click
     */
    void onClick() {
    }

    /**
     * Set the position of the cursor
     * 
     * @param pos the cursor position
     */
    public void setCursorPosition(int pos) {
        if(pos < -1 || pos > getText().length()) {
            throw new IllegalArgumentException("Illegal cursor position: " + pos);
        }
        cursorCharPosition = pos;
    }
    
    /**
     * @inheritDoc
     */
    public int getCursorPosition() {
        if(getText() == null) {
            return 0;
        }
        return Math.min(getText().length(), cursorCharPosition);
    }
    
    /**
     * @inheritDoc
     */
    public void setText(String text) {
        super.setText(text);
        fireDataChanged(DataChangedListener.CHANGED, -1);
        if(cursorCharPosition < 0) {
            cursorCharPosition = text.length();
        } else {
            if(cursorCharPosition > text.length()) {
                cursorCharPosition = text.length();
            }
        }
    }
    
    
    /**
     * Cleares the text from the TextField
     */
    public void clear(){
        setText("");
        commitChange();
    }   
    
    /**
     * Invoked on a long click by the user
     */
    private void longClick(int keyCode) {
        longClick = true;
        keyReleaseOrLongClick(keyCode, true);
    }

    /**
     * Returns true if this is the clear key on the device, many devices don't contain
     * a clear key and even in those that contain it this might be an issue
     * 
     * @param keyCode the key code that might be the clear key
     * @return true if this is the clear key.
     */
    protected boolean isClearKey(int keyCode) {
        return keyCode == Form.clearSK || keyCode == Form.backspaceSK;
    }
    
    /**
     * @inheritDoc
     */
    public boolean isQwertyInput() {
        if(!qwertyInitialized) {
            qwertyInitialized = true;
            int type = Display.getInstance().getKeyboardType();
            qwerty = type == Display.KEYBOARD_TYPE_QWERTY ||
                    type == Display.KEYBOARD_TYPE_VIRTUAL;
            qwertyDevice = qwerty;
        }
        return qwerty;
    }

    /**
     * True is this is a qwerty device or a device that is currently in
     * qwerty mode.
     * 
     * @param qwerty the value of qwerty mode
     */
    public void setQwertyInput(boolean qwerty) {
        this.qwerty = qwerty;
    }
    
    private boolean keyReleaseOrLongClick(int keyCode, boolean longClick) {
        // user pressed a different key, autocommit everything
        if(lastKeyCode != keyCode && pendingCommit) {
            commitChange();
        }
        lastKeyCode = keyCode;
        boolean isClearKey = isClearKey(keyCode);
        if(isQwertyInput() && !isClearKey) {
            if(keyCode > 0) {
                if(previousText == null) {
                    previousText = getText();
                }
                if(cursorCharPosition < 0) {
                    cursorCharPosition = 0;
                }
                
                insertChars("" + (char)keyCode);
                commitChange();
                fireDataChanged(DataChangedListener.ADDED, cursorCharPosition);
                return true;
            }
        } else {
            char c = getCharPerKeyCode(pressCount, keyCode, longClick);
            cursorCharPosition = Math.max(cursorCharPosition, 0);
            if (c != 0) {
                String text;
                if(previousText == null) {
                    previousText = getText();
                }
                if(!pendingCommit) {
                    insertChars("" + c);
                    pendingCommit = true;
                    pressCount++;
                } else {
                    if(overwriteMode) {
                        //neutralize incrementation within *insertChars*
                        cursorCharPosition--;

                        //overwrite the character of previous keystroke
                        insertChars("" + c);

                        pressCount++;
        		    } else {
                        text = previousText.substring(0, cursorCharPosition - 1) + c +
                            previousText.substring(cursorCharPosition - 1, previousText.length());
                        pendingCommit = true;
                        pressCount++;
                        super.setText(text);
                    }
                }

                if(inputMode.equals("123")) {
                    commitChange();
                    fireDataChanged(DataChangedListener.ADDED, cursorCharPosition);
                } else {
                    if(pressCount == 1) {
                        fireDataChanged(DataChangedListener.ADDED, cursorCharPosition);
                    } else {
                        fireDataChanged(DataChangedListener.CHANGED, cursorCharPosition);
                    }
                }
                return true;
            }
        }
        int game = Display.getInstance().getGameAction(keyCode);
        if(game == keyFwd) {
            cursorCharPosition++;
            if(cursorCharPosition > getText().length()) {
                if(isCursorPositionCycle()) {
                    cursorCharPosition = 0;
                } else {
                    cursorCharPosition = getText().length();
                }
            }
            repaint();
            return true;
        } else {
            if(game == keyBack) {
                cursorCharPosition--;
                if(cursorCharPosition < 0) {
                    if(isCursorPositionCycle()) {
                        cursorCharPosition = getText().length();
                    } else {
                        cursorCharPosition = 0;
                    }
                }
                repaint();
                return true;
            }
        }
        if(isChangeInputMode(keyCode)) {
            for(int iter = 0 ; iter < inputModeOrder.length ; iter++) {
                if(inputModeOrder[iter].equals(inputMode)) {
                    iter++;
                    if(iter < inputModeOrder.length) {
                        setInputMode(inputModeOrder[iter]);
                    } else {
                        setInputMode(inputModeOrder[0]);
                    }
                    return true;
                }
            }
            return true;
        }
        if(isClearKey) {
            if(longClick) {
                // long click on the clear key should erase the field entirely
                setText("");
                commitChange();
            } else {
                deleteChar();
            }
            return true;
        }
        if(isSymbolDialogKey(keyCode)) {
            showSymbolDialog();
            return true;
        }
        return false;
    }

    /**
     * This method is responsible for adding a character into the field and is 
     * the focal point for all input. It can be overriden to prevent a particular
     * char from insertion or provide a different behavior for char insertion.
     * It is the responsibility of this method to shift the cursor and invoke
     * setText...
     * <p>This method accepts a string for the more elaborate cases such as multi-char
     * input and paste.
     * 
     * @param c character for insertion
     */
    public void insertChars(String c) {
        String currentText = getText();
        //if the contraint is numeric only, don't insert a char that isn't 
        //numeric
        if ((getConstraint() & TextArea.NUMERIC) != 0) {
            if(c.charAt(0) < '0' || c.charAt(0) > '9'){
                return;
            }
        }
        cursorCharPosition++;
        if(overwriteMode && cursorCharPosition <= currentText.length()) {
            setText(currentText.substring(0, cursorCharPosition - 1) + c + 
                currentText.substring(cursorCharPosition, currentText.length()));
        } else {
            if(currentText.length() + c.length() > getMaxSize()) {
                cursorCharPosition--;
                return;
            }
            setText(currentText.substring(0, cursorCharPosition - 1) + c + 
                currentText.substring(cursorCharPosition - 1, currentText.length()));
        }
        if(c.length() > 1) {
            cursorCharPosition += c.length() - 1;
        }
    }
    
    /**
     * Invoked to show the symbol dialog, this method can be overriden by subclasses to
     * manipulate the symbol table
     */
    protected void showSymbolDialog() {
        Command cancel = new Command("Cancel");
        Command r = Dialog.show(null, createSymbolTable(), new Command[] {cancel});
        if(r != null && r != cancel) {
            insertChars(r.getCommandName());
        }
    }

    /**
     * Creates a symbol table container used by the showSymbolDialog method.
     * This method is designed for subclases to override and customize.
     * 
     * @return container for the symbol table.
     */
    protected Container createSymbolTable() {
        char[] symbolArray = getSymbolTable();
        Container symbols = new Container(new GridLayout(symbolArray.length / 5, 5));
        for(int iter = 0 ; iter < symbolArray.length ; iter++) {
            Button button = new Button(new Command("" + symbolArray[iter]));
            button.setAlignment(CENTER);
            symbols.addComponent(button);
        }
        return symbols;
    }

    /**
     * @inheritDoc
     */
    public void keyReleased(int keyCode) {
        if(!isEditable()) {
            return;
        }
        pressedAndNotReleased = false;
        releaseTime = System.currentTimeMillis();
        if(!longClick) {
            if(keyReleaseOrLongClick(keyCode, false)) {
                return;
            }
        }
        longClick = false;
        super.keyReleased(keyCode);
    }
    
    /**
     * The amount of time considered as a "long click" causing the long click method
     * to be invoked.
     * 
     * @return currently defaults to 800
     */
    protected int getLongClickDuration() {
        return 800;
    }

    /**
     * Returns the symbol table for the device
     * 
     * @return the symbol table of the device for the symbol table input
     */
    public static char[] getSymbolTable() {
        return symbolTable;
    }

    /**
     * Sets the symbol table to show when the user clicks the symbol table key
     * 
     * @param table the symbol table of the device for the symbol table input
     */
    public static void setSymbolTable(char[] table) {
        symbolTable = table;;
    }
    
    /**
     * Returns true if the cursor should cycle to the beginning of the text when the
     * user navigates beyond the edge of the text and visa versa.
     * @return true by default
     */
    protected boolean isCursorPositionCycle() {
        return true;
    }
    
    /**
     * Returns true if this keycode is the one mapping to the symbol dialog popup
     * 
     * @param keyCode the keycode to check
     * @return true if this is the star symbol *
     */
    protected boolean isSymbolDialogKey(int keyCode) {
        return keyCode == defaultSymbolDialogKey;
    }

    /**
     * @inheritDoc
     */
    protected void deinitialize() {
        // if the text field is removed without restoring the commands we need to restore them
        if(handlesInput()) {
            if(useSoftkeys) {
                removeCommands(DELETE_COMMAND, T9_COMMAND, originalClearCommand);
            } else {
                Form f = getComponentForm();
                if(f != null) {
                    f.setClearCommand(originalClearCommand);
                }
                originalClearCommand = null;
            }
            pressedAndNotReleased = false;
        }
    }

    /**
     * @inheritDoc
     */
    public void setEditable(boolean b) {
        super.setEditable(b);
        if(!b && handlesInput()) {
            setHandlesInput(false);
            if(useSoftkeys) {
                removeCommands(DELETE_COMMAND, T9_COMMAND, originalClearCommand);
            } else {
                Form f = getComponentForm();
                f.setClearCommand(originalClearCommand);
                originalClearCommand = null;
            }
            pressedAndNotReleased = false;
        }
    }

    /**
     * @inheritDoc
     */
    public void keyRepeated(int keyCode) {
        // the default implementation interfears with long press
        if(isQwertyDevice()) {
            super.keyRepeated(keyCode);
        }
    }

    /**
     * @inheritDoc
     */
    public void keyPressed(int keyCode) {
        if(!isEditable()) {
            return;
        }
        pressedAndNotReleased = true;
        pressedKeyCode = keyCode;
        pressTime = System.currentTimeMillis();
        
        // try to autodetect a qwerty device
        if(qwertyAutoDetect) {
            if((!qwerty) && ((keyCode >= 'a' && keyCode <= 'z') || (keyCode >= 'A' && keyCode <= 'Z'))) {
                qwertyDevice = true;
                qwerty = true;
            }
        }
        
        if((!handlesInput()) && isEditingTrigger(keyCode)) {
            setHandlesInput(true);
            if(useSoftkeys) {
                T9_COMMAND.setDisposesDialog(false);
                DELETE_COMMAND.setDisposesDialog(false);
                originalClearCommand = installCommands(DELETE_COMMAND, T9_COMMAND);
            } else {
                Form f = getComponentForm();
                originalClearCommand = f.getClearCommand();
                f.setClearCommand(DELETE_COMMAND);
            }
            return;
        }
        if(handlesInput() && isEditingEndTrigger(keyCode)) {
            setHandlesInput(false);
            if(useSoftkeys) {
                removeCommands(DELETE_COMMAND, T9_COMMAND, originalClearCommand);
            } else {
                Form f = getComponentForm();
                f.setClearCommand(originalClearCommand);
                originalClearCommand = null;
            }
            fireActionEvent();
            return;
        } else {
            if(handlesInput()) {
                return;
            }
        }
        super.keyPressed(keyCode);
    }
    
    /**
     * Installs the clear and t9 commands onto the parent form, this method can
     * be overriden to provide device specific placement for these commands
     * 
     * @param clear the clear command
     * @param t9 the t9 command
     * @param keyboard the keyboard command
     * @return clear command already installed in the form if applicable, none if no
     * clear command was installed before or not applicable.
     */
    protected Command installCommands(Command clear, Command t9) {
        Form f = getComponentForm();
        if(f != null) {
            Command original = f.getClearCommand();
            if(original instanceof CommandHandler) {
                original = originalClearCommand;
            }
            if(replaceMenu && originalCommands == null) {
                originalCommands = new Command[f.getCommandCount()];
                for(int iter = 0 ; iter < originalCommands.length ; iter++) {
                    originalCommands[iter] = f.getCommand(iter);
                }
                f.removeAllCommands();
                if(Display.getInstance().isThirdSoftButton()) {
                    getComponentForm().addSelectCommand(getSelectCommandText());
                    selectCommand = getComponentForm().getCommand(0);
                }
            }

            
            f.addCommand(clear, 0);
            f.addCommand(t9, 0);
            f.setClearCommand(clear);
            return original;
        }
        return null;
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
    protected void fireClicked() {
        if((!handlesInput())) {
            setHandlesInput(true);
            if(useSoftkeys) {
                T9_COMMAND.setDisposesDialog(false);
                DELETE_COMMAND.setDisposesDialog(false);
                originalClearCommand = installCommands(DELETE_COMMAND, T9_COMMAND);
            } else {
                Form f = getComponentForm();
                originalClearCommand = f.getClearCommand();
                f.setClearCommand(DELETE_COMMAND);
            }
            return;
        }
        if(handlesInput()) {
            setHandlesInput(false);
            if(useSoftkeys) {
                removeCommands(DELETE_COMMAND, T9_COMMAND, originalClearCommand);
            } else {
                Form f = getComponentForm();
                f.setClearCommand(originalClearCommand);
                originalClearCommand = null;
            }
            fireActionEvent();
            return;
        } else {
            if(handlesInput()) {
                return;
            }
        }
    }

    /**
     * Removes the clear and t9 commands from the parent form, this method can
     * be overriden to provide device specific placement for these commands
     * 
     * @param clear the clear command
     * @param t9 the t9 command
     * @param originalClear the command originally assigned as the clear command (or null if no command was assigned before)
     * @param keyboard toggle keyboard command
     */
    protected void removeCommands(Command clear, Command t9, Command originalClear) {
        Form f = getComponentForm();
        if(f != null) {
            f.removeCommand(clear);
            f.removeCommand(t9);
            if(selectCommand != null) {
                f.removeCommand(selectCommand);
            }
            f.setClearCommand(originalClearCommand);
            if(replaceMenu && originalCommands != null) {
                for(int iter = originalCommands.length - 1 ; iter >= 0 ; iter--) {
                    f.addCommand(originalCommands[iter]);
                }
                originalCommands = null;
            }
        }
    }
    
    void focusLostInternal() {
        if(handlesInput() || pressedAndNotReleased || pendingCommit) {
            setHandlesInput(false);
            Form f = getComponentForm();
            if(f != null) {
                if (useSoftkeys) {
                    removeCommands(DELETE_COMMAND, T9_COMMAND, originalClearCommand);
                } else {
                    f.setClearCommand(originalClearCommand);
                }
            }
            releaseTime = System.currentTimeMillis();
            commitChange();
            pressedAndNotReleased = false;
            longClick = false;
        }
        if(Display.getInstance().isVirtualKeyboardShowingSupported()) {
            Display.getInstance().setShowVirtualKeyboard(false);
        }
    }

    void focusGainedInternal() {
        releaseTime = System.currentTimeMillis();
        pressedAndNotReleased = false;
        longClick = false;
    }
    
    /**
     * Indicates whether the given key code should be ignored or should trigger
     * editing, by default fire or any numeric key should trigger editing implicitly.
     * This method is only called when handles input is false.
     * 
     * @param keyCode the keycode passed to the keyPressed method
     * @return true if this key code should cause a switch to editing mode.
     */
    protected boolean isEditingTrigger(int keyCode) {
        if(!isEditable()) {
            return false;
        }
        int gk = Display.getInstance().getGameAction(keyCode);
        if(isQwertyInput()) {
            return keyCode > 0 || (gk == Display.GAME_FIRE) || isClearKey(keyCode) || isEnterKey(keyCode)
                     || (leftAndRightEditingTrigger && ((gk == Display.GAME_LEFT) || (gk == Display.GAME_RIGHT)));
        }
        return (keyCode >= '0' && keyCode <= '9') || isClearKey(keyCode) ||
            (gk == Display.GAME_FIRE)
            || (leftAndRightEditingTrigger && ((gk == Display.GAME_LEFT) || (gk == Display.GAME_RIGHT)));
    }
    
    /**
     * Indicates whether the given key code should be ignored or should trigger
     * cause editing to end. By default the fire key, up or down will trigger
     * the end of editing.
     * 
     * @param keyCode the keycode passed to the keyPressed method
     * @return true if this key code should cause a switch to editing mode.
     */
    protected boolean isEditingEndTrigger(int keyCode) {
        int k =Display.getInstance().getGameAction(keyCode);
        if(isQwertyInput()) {
            return keyCode < 0 && (k == Display.GAME_FIRE || k == Display.GAME_UP || k == Display.GAME_DOWN || isEnterKey(keyCode));
        }
        return (k == Display.GAME_FIRE || k == Display.GAME_UP || k == Display.GAME_DOWN);
    }
    
    /**
     * @inheritDoc
     */
    public void paint(Graphics g) {
        UIManager.getInstance().getLookAndFeel().drawTextField(g, this);
        if (drawCursor && hasFocus() && isEditable()) {
            UIManager.getInstance().getLookAndFeel().drawTextFieldCursor(g, this);
        }
    }

    /**
     * @inheritDoc
     */
    protected Dimension calcPreferredSize() {
        return UIManager.getInstance().getLookAndFeel().getTextFieldPreferredSize(this);
    }
    
    /**
     * @inheritDoc
     */
    protected void initComponent() {
        boolean rtl = isRTL();
        keyFwd = rtl ? Display.GAME_LEFT : Display.GAME_RIGHT;
        keyBack = rtl ? Display.GAME_RIGHT : Display.GAME_LEFT;

        // text field relies too much on animation to use internal animations
        getComponentForm().registerAnimated(this);
    }
    
    /**
     * The amount of time in milliseconds in which the cursor is visible
     * 
     * @param time for the cursor to stay "on"
     */
    public void setCursorBlinkTimeOn(int time) {
        blinkOnTime = time;
    }

    /**
     * The amount of time in milliseconds in which the cursor is invisible
     * 
     * @param time for the cursor to stay "off"
     */
    public void setCursorBlinkTimeOff(int time) {
        blinkOffTime = time;
    }
    
    /**
     * The amount of time in milliseconds in which the cursor is visible
     * 
     * @return time for the cursor to stay "on"
     */
    public int getCursorBlinkTimeOn() {
        return blinkOnTime;
    }

    /**
     * The amount of time in milliseconds in which the cursor is invisible
     * 
     * @return time for the cursor to stay "off"
     */
    public int getCursorBlinkTimeOff() {
        return blinkOffTime;
    }

    /**
     * @inheritDoc
     */
    public boolean animate() {
        boolean ani = super.animate();
        if(hasFocus()) {
            long currentTime = System.currentTimeMillis();
            if (drawCursor) {
                if ((currentTime - cursorBlinkTime) > blinkOnTime) {
                    cursorBlinkTime = currentTime;
                    drawCursor = false;
                    return true;
                }
            } else {
                if ((currentTime - cursorBlinkTime) > blinkOffTime) {
                    cursorBlinkTime = currentTime;
                    drawCursor = true;
                    return true;
                }
            }
            if(pressedAndNotReleased) { 
                if(currentTime - pressTime >= getLongClickDuration()) {
                    longClick(pressedKeyCode);
                }
            } else {
                if(pendingCommit && currentTime - releaseTime > commitTimeout) {
                    commitChange();
                }
            }
        } else {
            drawCursor = false;
        }
        return ani;
    }
    
    /**
     * @inheritDoc
     */
    public void pointerReleased(int x, int y) {
        // unlike text area the text field supports shifting the cursor with the touch screen
        String text = getText();
        int textLength = text.length();
        int position = 0;
        Font f = getStyle().getFont();
        x -= getAbsoluteX();
        for(int iter = 0 ; iter < textLength ; iter++) {
            int width = f.substringWidth(text, 0, iter);
            if(x > width) {
                position = iter;
            } else {
                break;
            }
        }
        if(position == textLength - 1) {
            if(f.stringWidth(text) < x) {
                position = textLength;
            }
        }
        setCursorPosition(position);
        if(!isEditable()){
            return;
        }
        if(Display.getInstance().isVirtualKeyboardShowingSupported()) {
            Display.getInstance().setShowVirtualKeyboard(!Display.getInstance().isVirtualKeyboardShowing());
        } 
        if(!handlesInput()) {
            fireClicked();
        } else {
            repaint();
        }
    }

    /**
     * When set to true softkeys are used to enable delete functionality
     * 
     * @return true if softkeys should be used
     */
    public boolean isUseSoftkeys() {
        return useSoftkeys;
    }

    /**
     * When set to true softkeys are used to enable delete functionality
     * 
     * @param useSoftkeys true if softkeys should be used
     */
    public void setUseSoftkeys(boolean useSoftkeys) {
        this.useSoftkeys = useSoftkeys;
    }
    
    /**
     * Adds a listener for data change events it will be invoked for every change
     * made to the text field
     * 
     * @param d the listener
     */
    public void addDataChangeListener(DataChangedListener d) {
        listeners.addListener(d);
    }

    /**
     * Removes the listener for data change events 
     * 
     * @param d the listener
     */
    public void removeDataChangeListener(DataChangedListener d) {
        listeners.removeListener(d);
    }
    
    private void fireDataChanged(int type, int index) {
        if(listeners != null) {
            listeners.fireDataChangeEvent(index, type);
        }
    }
    
    /**
     * @inheritDoc
     */
    void onEditComplete(String text) {
        super.onEditComplete(text);
        setCursorPosition(text.length());
    }

    /**
     * Indicates whether the menu of the form should be replaced with the T9/Clear
     * commands for the duration of interactivity with the text field
     * 
     * @return true if the menu should be replaced
     */
    public boolean isReplaceMenu() {
        return replaceMenu;
    }

    /**
     * Indicates whether the menu of the form should be replaced with the T9/Clear
     * commands for the duration of interactivity with the text field
     * 
     * @param replaceMenu  true if the menu should be replaced
     */
    public void setReplaceMenu(boolean replaceMenu) {
        this.replaceMenu = replaceMenu;
    }


    /**
     * Indicates whether the menu of the form should be replaced with the T9/Clear
     * commands for the duration of interactivity with the text field
     * 
     * @return true if the menu should be replaced
     */
    public static boolean isReplaceMenuDefault() {
        return replaceMenuDefault;
    }

    /**
     * Indicates whether the menu of the form should be replaced with the T9/Clear
     * commands for the duration of interactivity with the text field
     * 
     * @param replaceMenu  true if the menu should be replaced
     */
    public static void setReplaceMenuDefault(boolean replaceMenu) {
        replaceMenuDefault = replaceMenu;
    }
    
    /**
     * Indicates whether the text field should try to auto detect qwerty and 
     * switch the qwerty device flag implicitly
     * 
     * @param v true for qwerty auto detection
     */
    public static void setQwertyAutoDetect(boolean v) {
        qwertyAutoDetect = v;
    }
    
    /**
     * The default value for the qwerty flag so it doesn't need setting for every
     * text field individually.
     * 
     * @param v true for qwerty device
     */
    public static void setQwertyDevice(boolean v) {
        qwertyDevice = v;
    }

    /**
     * Indicates whether the text field should try to auto detect qwerty and 
     * switch the qwerty device flag implicitly
     * 
     * @return true for qwerty auto detection
     */
    public static boolean isQwertyAutoDetect() {
        return qwertyAutoDetect;
    }
    
    /**
     * The default value for the qwerty flag so it doesn't need setting for every
     * text field individually.
     * 
     * @return true for qwerty devices
     */
    public static boolean isQwertyDevice() {
        return qwertyDevice;
    }

    /**
     * Key to change the input mode on the device
     * 
     * @param k key to change the input mode
     */
    public static void setDefaultChangeInputModeKey(int k) {
        defaultChangeInputModeKey = k;
    }
    
    /**
     * Key to change the input mode on the device
     * 
     * @return key to change the input mode
     */
    public static int getDefaultChangeInputModeKey() {
        return defaultChangeInputModeKey;
    }

    /**
     * The default key for poping open the symbol dialog
     *
     * @param d new key value
     */
    public static void setDefaultSymbolDialogKey(int d) {
        defaultSymbolDialogKey = d;
    }

    /**
     * The default key for poping open the symbol dialog
     *
     * @return the default key
     */
    public static int getDefaultSymbolDialogKey() {
        return defaultSymbolDialogKey;
    }

    /**
     * Indicates that this is the overwrite mode 
     * 
     * @param overwriteMode set to true if input with overwrite characters
     */
    public void setOverwriteMode(boolean overwriteMode) {
        this.overwriteMode = overwriteMode;
    }
    
    /**
     * Indicates that this is the overwrite mode 
     * 
     * @return true if input with overwrite characters
     */
    public boolean isOverwriteMode() {
        return overwriteMode;
    }

    /**
     * Indicates whether the left/right keys will trigger editing, this is true by default.
     * Left and right key edit trigger might be disabled for cases such as text field
     * positioned horizontally one next to the other.
     *
     * @param leftAndRightEditingTrigger Indicates whether the left/right keys will trigger editing
     */
    public void setLeftAndRightEditingTrigger(boolean leftAndRightEditingTrigger) {
        this.leftAndRightEditingTrigger = leftAndRightEditingTrigger;
    }


    /**
     * Indicates whether the left/right keys will trigger editing, this is true by default.
     * Left and right key edit trigger might be disabled for cases such as text field
     * positioned horizontally one next to the other.
     *
     * @return leftAndRightEditingTrigger Indicates whether the left/right keys will trigger editing
     */
    public boolean isLeftAndRightEditingTrigger() {
        return leftAndRightEditingTrigger;
    }

    /**
     * @inheritDoc
     */
    public boolean isSingleLineTextArea() {
        return true;
    }


    /**
     * @inheritDoc
     */
    public void setAlignment(int align) {
    	if(align == Component.CENTER) {
    		throw new IllegalArgumentException("CENTER alignment is not supported in TextField.");
    	} else {
    		super.setAlignment(align);
    	}
    }
}
