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

import com.sun.lwuit.Font;
import com.sun.lwuit.TextField;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.plaf.Border;
import com.sun.lwuit.plaf.Style;
import com.sun.lwuit.plaf.UIManager;
import java.util.Hashtable;

/**
 * An implementation of LWUIT that features a Light Weight Virtual Keyboard.
 *
 * @author Chen Fishbein
 */
public class VKBImplementation extends GameCanvasImplementation{
    
    private static Class vkbClass = VirtualKeyboard.class;
    
    private VirtualKeyboard vkb;

    public void init(Object m) {
        super.init(m);         
        //installs a listener on the UIManager that updates the default styles
        //for the Virtual Keyboard
        UIManager.getInstance().addThemeRefreshListener(new ActionListener() {

            public void actionPerformed(ActionEvent evt) {
                Hashtable themeProps = new Hashtable();
                themeProps.put("VKB.bgColor", "666666");
                themeProps.put("VKBtooltip.padding", "8,8,8,8");
                themeProps.put("VKBtooltip.font",
                        Font.createSystemFont(Font.FACE_SYSTEM,
                        Font.STYLE_BOLD, Font.SIZE_LARGE));
                themeProps.put("VKBtooltip.bgColor", "FFFFFF");
                themeProps.put("VKBtooltip.fgColor", "0");
                themeProps.put("VKBtooltip.border", Border.createRoundBorder(8, 8));

                themeProps.put("VKBButton.fgColor", "FFFFFF");
                themeProps.put("VKBButton.bgColor", "0");
                themeProps.put("VKBButton.sel#fgColor", "FFFFFF");
                themeProps.put("VKBButton.sel#bgColor", "0");
                themeProps.put("VKBButton.press#fgColor", "FFFFFF");
                themeProps.put("VKBButton.press#bgColor", "0");
                themeProps.put("VKBButton.border", Border.createRoundBorder(8, 8));
                themeProps.put("VKBButton.sel#border", Border.createRoundBorder(8, 8));
                themeProps.put("VKBButton.press#border", Border.createRoundBorder(8, 8));
//                themeProps.put("VKBButton.bgType", new Byte(Style.BACKGROUND_GRADIENT_LINEAR_VERTICAL));
                themeProps.put("VKBButton.sel#bgType", new Byte(Style.BACKGROUND_GRADIENT_LINEAR_VERTICAL));
                themeProps.put("VKBButton.press#bgType", new Byte(Style.BACKGROUND_GRADIENT_LINEAR_VERTICAL));
//                themeProps.put("VKBButton.bgGradient", new Object[]{new Integer(0x666666),
//                            new Integer(0), new Float(0), new Float(0), new Float(0)
//                        });
                themeProps.put("VKBButton.sel#bgGradient", new Object[]{new Integer(0x666666),
                            new Integer(0), new Float(0), new Float(0), new Float(0)
                        });
                themeProps.put("VKBButton.press#bgGradient", new Object[]{new Integer(0),
                            new Integer(0x666666), new Float(0), new Float(0), new Float(0)
                        });
                themeProps.put("VKBButton.margin", "2,2,1,1");
                themeProps.put("VKBButton.sel#margin", "2,2,1,1");
                themeProps.put("VKBButton.press#margin", "2,2,1,1");
                themeProps.put("VKBButton.padding", "6,6,4,4");
                themeProps.put("VKBButton.sel#padding", "6,6,4,4");
                themeProps.put("VKBButton.press#padding", "6,6,4,4");
                themeProps.put("VKBButton.font",
                        Font.createSystemFont(Font.FACE_SYSTEM,
                        Font.STYLE_BOLD, Font.SIZE_MEDIUM));
                themeProps.put("VKBButton.sel#font",
                        Font.createSystemFont(Font.FACE_SYSTEM,
                        Font.STYLE_BOLD, Font.SIZE_MEDIUM));
                themeProps.put("VKBButton.press#font",
                        Font.createSystemFont(Font.FACE_SYSTEM,
                        Font.STYLE_BOLD, Font.SIZE_MEDIUM));

                themeProps.put("VKBSpecialButton.fgColor", "FFFFFF");
                themeProps.put("VKBSpecialButton.bgColor", "0");
                themeProps.put("VKBSpecialButton.sel#fgColor", "FFFFFF");
                themeProps.put("VKBSpecialButton.sel#bgColor", "0");
                themeProps.put("VKBSpecialButton.press#fgColor", "FFFFFF");
                themeProps.put("VKBSpecialButton.press#bgColor", "0");
                themeProps.put("VKBSpecialButton.border", Border.createRoundBorder(8, 8));
                themeProps.put("VKBSpecialButton.sel#border", Border.createRoundBorder(8, 8));
                themeProps.put("VKBSpecialButton.press#border", Border.createRoundBorder(8, 8));
                themeProps.put("VKBSpecialButton.bgType", new Byte(Style.BACKGROUND_GRADIENT_LINEAR_VERTICAL));
                themeProps.put("VKBSpecialButton.sel#bgType", new Byte(Style.BACKGROUND_GRADIENT_LINEAR_VERTICAL));
                themeProps.put("VKBSpecialButton.press#bgType", new Byte(Style.BACKGROUND_GRADIENT_LINEAR_VERTICAL));

                themeProps.put("VKBSpecialButton.bgGradient", new Object[]{new Integer(0xcccccc),
                            new Integer(0x666666), new Float(0), new Float(0), new Float(0)
                        });
                themeProps.put("VKBSpecialButton.sel#bgGradient", new Object[]{new Integer(0xcccccc),
                            new Integer(0x666666), new Float(0), new Float(0), new Float(0)
                        });
                themeProps.put("VKBSpecialButton.press#bgGradient", new Object[]{new Integer(0x666666),
                            new Integer(0xcccccc), new Float(0), new Float(0), new Float(0)
                        });


                themeProps.put("VKBSpecialButton.margin", "2,2,1,1");
                themeProps.put("VKBSpecialButton.sel#margin", "2,2,1,1");
                themeProps.put("VKBSpecialButton.press#margin", "2,2,1,1");
                themeProps.put("VKBSpecialButton.padding", "6,6,4,4");
                themeProps.put("VKBSpecialButton.sel#padding", "6,6,4,4");
                themeProps.put("VKBSpecialButton.press#padding", "6,6,4,4");
                themeProps.put("VKBSpecialButton.font",
                        Font.createSystemFont(Font.FACE_SYSTEM,
                        Font.STYLE_BOLD, Font.SIZE_MEDIUM));
                themeProps.put("VKBSpecialButton.sel#font",
                        Font.createSystemFont(Font.FACE_SYSTEM,
                        Font.STYLE_BOLD, Font.SIZE_MEDIUM));
                themeProps.put("VKBSpecialButton.press#font",
                        Font.createSystemFont(Font.FACE_SYSTEM,
                        Font.STYLE_BOLD, Font.SIZE_MEDIUM));

                themeProps.put("VKBTextInput.sel#fgColor", "FFFFFF");
                themeProps.put("VKBTextInput.sel#bgColor", "0");
                themeProps.put("VKBTextInput.sel#font",
                        Font.createSystemFont(Font.FACE_SYSTEM,
                        Font.STYLE_BOLD, Font.SIZE_MEDIUM));
                themeProps.put("VKBTextInput.sel#border",
                        Border.getDefaultBorder());
                UIManager.getInstance().addThemeProps(themeProps);
            }
        });
    }

    
    /**
     * @inheritDoc
     */
    public boolean isVirtualKeyboardShowing() {
        return vkb != null && vkb.isVisible();
    }

    /**
     * @inheritDoc
     */
    public boolean isVirtualKeyboardShowingSupported() {
        return true;
    }

    /**
     * @inheritDoc
     */
    public void setShowVirtualKeyboard(boolean show) {
        if (show) {
            TextField txtCmp = (TextField) getCurrentForm().getFocused();
            if (txtCmp != null) {
                vkb = createVirtualKeyboard();
                vkb.setTextField(txtCmp);
                vkb.showDialog();
            }
        }
    }

    /**
     * Sets the virtual keyboard class.
     * 
     * @param vkbClazz this class must extend VirtualKeyboard.
     */
    static void setVirtualKeyboardClass(Class vkbClazz){
        vkbClass = vkbClazz;
    }
    
    private VirtualKeyboard createVirtualKeyboard() {
        try {
            return (VirtualKeyboard) vkbClass.newInstance();
        } catch (Exception ex) {
            ex.printStackTrace();
            return new VirtualKeyboard();
        } 
    }
    
    
}
