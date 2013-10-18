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
package com.sun.lwuit.impl.midp;

import com.sun.lwuit.*;
import com.sun.lwuit.animations.CommonTransitions;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.geom.Rectangle;
import com.sun.lwuit.layouts.BorderLayout;
import com.sun.lwuit.layouts.BoxLayout;
import java.util.Hashtable;
import java.util.Vector;

/**
 * This class is a Light Weight Virtual Keyboard
 * 
 * @author Chen Fishbein
 */
public class VirtualKeyboard extends Dialog {

    /**
     * This keymap represents qwerty keyboard
     */
    public static final String[][] DEFAULT_QWERTY = new String[][]{
        {"q", "w", "e", "r", "t", "y", "u", "i", "o", "p"},
        {"a", "s", "d", "f", "g", "h", "j", "k", "l"},
        {"$Shift$", "z", "x", "c", "v", "b", "n", "m", "$Delete$"},
        {"$Mode$", "$T9$", "$Space$", "$OK$"}
    };
    /**
     * This keymap represents numbers keyboard
     */
    public static final String[][] DEFAULT_NUMBERS = new String[][]{
        {"1", "2", "3",},
        {"4", "5", "6",},
        {"7", "8", "9",},
        {"*", "0", "#",},
        {"$Mode$", "$Space$", "$Delete$", "$OK$"}
    };
    /**
     * This keymap represents numbers and symbols keyboard
     */
    public static final String[][] DEFAULT_NUMBERS_SYMBOLS = new String[][]{
        {"1", "2", "3", "4", "5", "6", "7", "8", "9", "0"},
        {"-", "/", ":", ";", "(", ")", "$", "&", "@"},
        {".", ",", "?", "!", "'", "\"", "$Delete$"},
        {"$Mode$", "$Space$", "$OK$"}
    };
    /**
     * This keymap represents symbols keyboard
     */
    public static final String[][] DEFAULT_SYMBOLS = new String[][]{
        {"[", "]", "{", "}", "#", "%", "^", "*", "+", "="},
        {"_", "\\", "|", "~", "<", ">", "\u00A3", "\u00A5"},
        {":-0", ";-)", ":-)", ":-(", ":P", ":D", "$Delete$"},
        {"$Mode$", "$Space$", "$OK$"}
    };
    /**
     * The String that represent the qwerty mode.
     */
    public static final String QWERTY_MODE = "ABC";
    /**
     * The String that represent the numbers mode.
     */
    public static final String NUMBERS_MODE = "123";
    /**
     * The String that represent the numbers sybols mode.
     */
    public static final String NUMBERS_SYMBOLS_MODE = ".,123";
    /**
     * The String that represent the symbols mode.
     */
    public static final String SYMBOLS_MODE = ".,?";
    private String currentMode = QWERTY_MODE;
    private Vector modesKeys = new Vector();
    private Hashtable modesMap = new Hashtable();
    private TextField inputField;
    private Container buttons = new Container(new BoxLayout(BoxLayout.Y_AXIS));
    private TextPainter txtPainter = new TextPainter();
    private boolean upperCase = false;
    private Button currentButton;
    private static final int INSERT_CHAR = 1;
    private static final int DELETE_CHAR = 2;
    private static final int CHANGE_MODE = 3;
    private static final int SHIFT = 4;
    private static final int OK = 5;
    private static final int SPACE = 6;
    private static final int T9 = 7;
    private Hashtable specialButtons = new Hashtable();
    private TextField field;
    private boolean finishedT9Edit = false;

    /**
     * Creates a new instance of VirtualKeyboard 
     * 
     * @param field The TextField to edit.
     */
    public VirtualKeyboard() {
        setLayout(new BorderLayout());
        getContentPane().setUIID("VKB");
        setAutoDispose(false);
        setDisposeWhenPointerOutOfBounds(true);
        setTransitionInAnimator(
                CommonTransitions.createSlide(CommonTransitions.SLIDE_VERTICAL, true, 500));
        setTransitionOutAnimator(
                CommonTransitions.createSlide(CommonTransitions.SLIDE_VERTICAL, false, 500));
        setGlassPane(txtPainter);
    }

    public void setTextField(TextField field) {
        this.field = field;
        removeAll();
        field.setUseSoftkeys(false);
        inputField = new TextField() {

            public boolean hasFocus() {
                return true;
            }

            public String getUIID() {
                return "VKBTextInput";
            }
        };
        inputField.setText(field.getText());
        inputField.setConstraint(field.getConstraint());
        inputField.setInputModeOrder(new String[]{"ABC"});
        initModes();
        initSpecialButtons();
        addComponent(BorderLayout.NORTH, inputField);
        buttons.getStyle().setPadding(0, 0, 0, 0);
        addComponent(BorderLayout.CENTER, buttons);
        initInputButtons(upperCase);
        inputField.setUseSoftkeys(false);
        applyRTL(false);
    }

    /**
     * @inheritDoc
     */
    public void show() {
        super.showPacked(BorderLayout.SOUTH, true);
    }

    /**
     * @inheritDoc
     */
    protected void sizeChanged(int w, int h) {
        if (!finishedT9Edit) {
            setTransitionOutAnimator(CommonTransitions.createEmpty());
            dispose();
        } else {
            finishedT9Edit = false;
        }

        super.sizeChanged(w, h);
    }


    /**
     * init all virtual keyboard modes, such as QWERTY_MODE, NUMBERS_SYMBOLS_MODE...
     * to add an addtitional mode a developer needs to override this method and
     * add a mode by calling addInputMode method
     */
    protected void initModes() {
        modesKeys.removeAllElements();
        modesMap.clear();
        if (inputField.getConstraint() == TextField.NUMERIC) {
            setCurrentMode(NUMBERS_SYMBOLS_MODE);
            addInputMode(NUMBERS_SYMBOLS_MODE, DEFAULT_NUMBERS_SYMBOLS);
            addInputMode(NUMBERS_MODE, DEFAULT_NUMBERS);
        } else {
            setCurrentMode(QWERTY_MODE);
            addInputMode(QWERTY_MODE, DEFAULT_QWERTY);
            addInputMode(NUMBERS_SYMBOLS_MODE, DEFAULT_NUMBERS_SYMBOLS);
            addInputMode(SYMBOLS_MODE, DEFAULT_SYMBOLS);
            addInputMode(NUMBERS_MODE, DEFAULT_NUMBERS);
        }
    }

    /**
     * Sets the current virtual keyboard mode.
     * 
     * @param mode the String that represents the mode(QWERTY_MODE, 
     * SYMBOLS_MODE, ...)
     */
    protected void setCurrentMode(String mode) {
        this.currentMode = mode;
    }

    /**
     * Gets the current mode.
     * 
     * @return the String that represents the current mode(QWERTY_MODE, 
     * SYMBOLS_MODE, ...)
     */
    protected String getCurrentMode() {
        return currentMode;
    }

    private void initInputButtons(boolean upperCase) {
        buttons.removeAll();
        int largestLine = 0;
        String[][] currentKeyboardChars = (String[][]) modesMap.get(currentMode);
        for (int i = 1; i < currentKeyboardChars.length; i++) {
            if (currentKeyboardChars[i].length > currentKeyboardChars[largestLine].length) {
                largestLine = i;
            }
        }
        int length = currentKeyboardChars[largestLine].length;
        Button dummy = createButton(new Command("dummy"), 0);
        int buttonMargins = dummy.getUnselectedStyle().getMargin(dummy.isRTL(), LEFT) +
                dummy.getUnselectedStyle().getMargin(dummy.isRTL(), RIGHT);
        Container row = null;
        int rowW = (Display.getInstance().getDisplayWidth() -
                getContentPane().getUnselectedStyle().getPadding(false, LEFT) -
                getContentPane().getUnselectedStyle().getPadding(false, RIGHT) -
                getContentPane().getUnselectedStyle().getMargin(false, LEFT) -
                getContentPane().getUnselectedStyle().getMargin(false, RIGHT));
        int availableSpace = rowW - length * buttonMargins;
        int buttonSpace = (availableSpace) / length;
        for (int i = 0; i < currentKeyboardChars.length; i++) {
            int rowWidth = rowW;
            row = new Container(new BoxLayout(BoxLayout.X_AXIS));
            row.getUnselectedStyle().setMargin(0, 0, 0, 0);
            Vector specialsButtons = new Vector();
            for (int j = 0; j < currentKeyboardChars[i].length; j++) {
                String txt = currentKeyboardChars[i][j];
                Button b = null;
                if (txt.startsWith("$") && txt.endsWith("$") && txt.length() > 1) {
                    //add a special button
                    Button cmd = (Button) specialButtons.get(txt.substring(1, txt.length() - 1));
                    int prefW = 0;
                    int space = ((Integer) cmd.getClientProperty("space")).intValue();
                    if (space != -1) {
                        prefW = availableSpace * space / 100;
                    }
                    b = createButton(cmd.getCommand(), prefW, "VKBSpecialButton");
                    if (prefW != 0) {
                        rowWidth -= (b.getPreferredW() + buttonMargins);
                    } else {
                        //if we can't determind the size at this stage, wait until
                        //the loops ends and give the remains size to the special
                        //button
                        specialsButtons.addElement(b);
                    }
                } else {
                    if (upperCase) {
                        txt = txt.toUpperCase();
                    }
                    b = createInputButton(txt, buttonSpace);
                    rowWidth -= (b.getPreferredW() + buttonMargins);
                }
                if (currentButton != null) {
                    if (currentButton.getCommand().getId() == b.getCommand().getId()) {
                        currentButton = b;
                    }
                    if (currentButton.getText().equals(b.getText())) {
                        currentButton = b;
                    }
                }
                row.addComponent(b);
            }
            int emptySpace = Math.max(rowWidth, 0);
            //if we have special buttons on the keyboard give them the size or 
            //else give the remain size to the row margins
            if (specialsButtons.size() > 0) {
                int prefW = emptySpace / specialsButtons.size();
                for (int j = 0; j < specialsButtons.size(); j++) {
                    Button special = (Button) specialsButtons.elementAt(j);
                    special.setPreferredW(prefW);
                }

            } else {
                row.getUnselectedStyle().setPadding(Component.LEFT, 0);
                row.getUnselectedStyle().setPadding(Component.RIGHT, 0);
                row.getUnselectedStyle().setMargin(Component.LEFT, emptySpace / 2);
                row.getUnselectedStyle().setMargin(Component.RIGHT, emptySpace / 2);
            }
            buttons.addComponent(row);
        }
        applyRTL(false);
    }

    private Button createInputButton(String text, int prefSize) {
        Button b = createButton(new Command(text, INSERT_CHAR), prefSize);
        b.putClientProperty("glasspane", "true");
        return b;
    }

    private Button createButton(Command cmd, int prefSize) {
        return createButton(cmd, prefSize, "VKBButton");
    }

    private Button createButton(Command cmd, int prefSize, String uiid) {
        final Button b = new Button(cmd);
        b.setUIID(uiid);
        b.setEndsWith3Points(false);
        b.setAlignment(Component.CENTER);
        prefSize = Math.max(prefSize, b.getPreferredW());
        b.setPreferredW(prefSize);
        b.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent evt) {
                currentButton = b;
            }
        });

        return b;
    }

    /**
     * Add an input mode to the virtual keyboard 
     * 
     * @param mode a string that represents the identifier of the mode
     * @param inputChars 2 dimentional String array that contains buttons String
     * and special buttons (a special button is identified with $...$ marks 
     * e.g: "$Space$")
     */
    public void addInputMode(String mode, String[][] inputChars) {
        modesKeys.addElement(mode);
        modesMap.put(mode, inputChars);
    }

    /**
     * This method adds a special button to the virtual keyboard
     * 
     * @param key the string identifier from within the relevant input mode
     * @param cmd the Command to invoke when this button is invoked.
     */
    public void addSpecialButton(String key, Command cmd) {
        addSpecialButton(key, cmd, -1);
    }

    /**
     * This method adds a special button to the virtual keyboard
     * 
     * @param key the string identifier from within the relevant input mode
     * @param cmd the Command to invoke when this button is invoked.
     * @param space how much space in percentage from the overall row 
     * the special button should occupy
     */
    public void addSpecialButton(String key, Command cmd, int space) {
        Button b = new Button(cmd);
        b.putClientProperty("space", new Integer(space));
        specialButtons.put(key, b);
    }

    private String getNextMode(String current) {

        int index = modesKeys.indexOf(current);
        if (index == modesKeys.size() - 1) {
            return (String) modesKeys.elementAt(0);
        }
        return (String) modesKeys.elementAt(index + 1);
    }

    /**
     * @inheritDoc
     */
    public void pointerPressed(int x, int y) {
        super.pointerPressed(x, y);
        //txtPainter.clear();
        Component cmp = getComponentAt(x, y);
        if (cmp != null && cmp instanceof Button && cmp.getClientProperty("glasspane") != null) {
            txtPainter.showButtonOnGlasspane((Button) cmp);
        }
    }

    /**
     * @inheritDoc
     */
    public void pointerDragged(int x, int y) {
        super.pointerDragged(x, y);
        //txtPainter.clear();
        Component cmp = getComponentAt(x, y);
        if (cmp != null && cmp instanceof Button && cmp.getClientProperty("glasspane") != null) {
            txtPainter.showButtonOnGlasspane((Button) cmp);
        }
    }

    /**
     * @inheritDoc
     */
    public void pointerReleased(int x, int y) {
        txtPainter.clear();
        super.pointerReleased(x, y);
    }

    /**
     * This method initialize all the virtual keyboard special buttons.
     */
    protected void initSpecialButtons() {
        specialButtons.clear();
        addSpecialButton("Shift", new Command("SH", SHIFT), 15);
        addSpecialButton("Delete", new Command("Del", DELETE_CHAR), 15);
        addSpecialButton("T9", new Command("T9", T9), 15);
        addSpecialButton("Mode", new Command(getNextMode(currentMode), CHANGE_MODE));
        addSpecialButton("Space", new Command("Space", SPACE), 50);
        addSpecialButton("OK", new Command("Ok", OK));
    }

    class TextPainter implements Painter {

        private Label label = new Label();
        private boolean paint = true;

        public TextPainter() {
            label = new Label();
            label.setUIID("VKBtooltip");
        }

        public void showButtonOnGlasspane(Button button) {
            if(label.getText().equals(button.getText())){
                return;
            }
            paint = true;
            repaint(label.getAbsoluteX()-2,
                    label.getAbsoluteY()-2,
                    label.getWidth()+4,
                    label.getHeight()+4);
            label.setText(button.getText());
            label.setSize(label.getPreferredSize());
            label.setX(button.getAbsoluteX() + (button.getWidth() - label.getWidth()) / 2);
            label.setY(button.getAbsoluteY() - label.getPreferredH() * 4 / 3);
            repaint(label.getAbsoluteX()-2,
                    label.getAbsoluteY()-2,
                    label.getPreferredW()+4,
                    label.getPreferredH()+4);
        }

        public void paint(Graphics g, Rectangle rect) {
            if (paint) {
                label.paintComponent(g);
            }

        }

        private void clear() {
            paint = false;
            repaint();
        }
    }

    /**
     * @inheritDoc
     */
    protected void actionCommand(Command cmd) {
        super.actionCommand(cmd);

        switch (cmd.getId()) {
            case OK:
                field.setText(inputField.getText());
                field.setCursorPosition(field.getText().length());
                dispose();
                break;
            case INSERT_CHAR:
                Button btn = currentButton;
                String text = btn.getText();
                if (inputField.getText().length() == 0) {
                    inputField.setText(text);
                    inputField.setCursorPosition(text.length());
                } else {
                    inputField.insertChars(text);
                }

                break;
            case SPACE:
                if (inputField.getText().length() == 0) {
                    inputField.setText(" ");
                } else {
                    inputField.insertChars(" ");
                }
                break;
            case DELETE_CHAR:
                inputField.deleteChar();
                break;
            case CHANGE_MODE:
                currentMode = getNextMode(currentMode);

                Display.getInstance().callSerially(new Runnable() {

                    public void run() {
                        initInputButtons(upperCase);
                        currentButton.setText(getNextMode(currentMode));
                        setTransitionOutAnimator(CommonTransitions.createEmpty());
                        setTransitionInAnimator(CommonTransitions.createEmpty());
                        getTitleComponent().getStyle().setMargin(TOP, 0);
                        dispose();

                        show();
                        setTransitionOutAnimator(
                                CommonTransitions.createSlide(CommonTransitions.SLIDE_VERTICAL, false, 500));

                    }
                });
                return;
            case SHIFT:
                if (currentMode.equals(QWERTY_MODE)) {
                    upperCase = !upperCase;
                    Display.getInstance().callSerially(new Runnable() {

                        public void run() {
                            initInputButtons(upperCase);
                            revalidate();
                        }
                    });
                }
                return;
            case T9:
                Display.getInstance().editString(inputField, inputField.getMaxSize(), inputField.getConstraint(), inputField.getText());
                finishedT9Edit = true;
        }
    }

    /**
     * This method returns the Virtual Keyboard TextField.
     * 
     * @return the the Virtual Keyboard TextField.
     */
    protected TextField getInputField() {
        return inputField;
    }
}
