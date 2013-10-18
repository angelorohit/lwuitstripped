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
package com.sun.lwuit.plaf;

import com.sun.lwuit.*;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.util.EventDispatcher;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * Central point singleton managing the look of the application, this class allows us to
 * customize the styles (themes) as well as the look instance.
 *
 * @author Chen Fishbein
 */
public class UIManager {
    private LookAndFeel current = new DefaultLookAndFeel();
    
    private Hashtable styles = new Hashtable();
    private Hashtable selectedStyles = new Hashtable();
    
    private Hashtable themeProps;
    
    private static UIManager instance = new UIManager();
    
    private Style defaultStyle = new Style();
    private Style defaultSelectedStyle = new Style();

    /**
     * This member is used by the resource editor
     */
    static boolean accessible = true;
    
    /**
     * Useful for caching theme images so they are not loaded twice in case 
     * an image reference is used it two places in the theme (e.g. same background
     * to title and menu bar).
     */
    private Hashtable imageCache = new Hashtable();
    
    /**
     * The resource bundle allows us to implicitly localize the UI on the fly, once its
     * installed all internal application strings query the resource bundle and extract
     * their values from this table if applicable.
     */
    private Hashtable resourceBundle;
    
    /**
     * This EventDispatcher holds all listeners who would like to register to
     * Theme refreshed event
     */
    private EventDispatcher themelisteners;
    
    private UIManager(){
        resetThemeProps();
    }
    
    /**
     * Singleton instance method
     * 
     * @return Instance of the ui manager
     */
    public static UIManager getInstance(){
        return instance;
    }
    
    /**
     * Returns the currently installed look and feel
     * 
     * @return the currently installed look and feel
     */
    public LookAndFeel getLookAndFeel(){
        return current;
    }
    
    /**
     * Sets the currently installed look and feel
     * 
     * @param plaf the look and feel for the application
     */    
    public void setLookAndFeel(LookAndFeel plaf){
        current.uninstall();
        current = plaf;
    }
    
    /**
     * Allows a developer to programmatically install a style into the UI manager
     * 
     * @param id the component id matching the given style
     * @param style the style object to install
     */
    public void setComponentStyle(String id, Style style) {
        if(id == null || id.length() == 0){
            //if no id return the default style
            id = "";
        } else {
            id = id + ".";
        }
        
        styles.put(id, style);
    }
    
    /**
     * Allows a developer to programmatically install a style into the UI manager
     *
     * @param id the component id matching the given style
     * @param style the style object to install
     */
    public void setComponentSelectedStyle(String id, Style style) {
        if(id == null || id.length() == 0){
            //if no id return the default style
            id = "";
        } else {
            id = id + ".";
        }

        selectedStyles.put(id, style);
    }

    /**
     * Returns the style of the component with the given id or a <b>new instance</b> of the default
     * style.
     * This method will always return a new style instance to prevent modification of the global
     * style object.
     * 
     * @param id the component id whose style we want
     * @return the appropriate style (this method never returns null)
     */
    public Style getComponentStyle(String id){
        return getComponentStyleImpl(id, false, "");
    }
    
    /**
     * Returns the selected style of the component with the given id or a <b>new instance</b> of the default
     * style.
     * This method will always return a new style instance to prevent modification of the global
     * style object.
     *
     * @param id the component id whose selected style we want
     * @return the appropriate style (this method never returns null)
     */
    public Style getComponentSelectedStyle(String id){
        return getComponentStyleImpl(id, true, "sel#");
    }


    /**
     * Returns a custom style for the component with the given id, this method always returns a
     * new instance. Custom styles allow us to install application specific or component specific
     * style attributes such as pressed, disabled, hover etc.
     *
     * @param id the component id whose custom style we want
     * @param type the style type
     * @return the appropriate style (this method never returns null)
     */
    public Style getComponentCustomStyle(String id, String type){
        return getComponentStyleImpl(id, false, type + "#");
    }

    private Style getComponentStyleImpl(String id, boolean selected, String prefix){
        Style style = null;

        if(id == null || id.length() ==0){
            //if no id return the default style
            id = "";
        }else{
            id = id + ".";
        }

        if(selected) {
            style = (Style)selectedStyles.get(id);

            if(style == null){
                style = createStyle(id, prefix, true);
                selectedStyles.put(id, style);
            }
        } else {
            if(prefix.length() == 0) {
                style = (Style)styles.get(id);

                if(style == null) {
                    style = createStyle(id, prefix, false);
                    styles.put(id, style);
                }
            } else {
                return createStyle(id, prefix, false);
            }
        }

        return new Style(style);
    }

    /**
     * @return the name of the current theme for theme switching UI's
     */
    public String getThemeName(){
        if(themeProps != null){
            return (String)themeProps.get("name");
        }
        return null;
    }
    
    /**
     * Initializes the theme properties with the current "defaults"
     */
    private void resetThemeProps() {
        themeProps = new Hashtable();
        themeProps.put("Button.border", Border.getDefaultBorder());
        themeProps.put("TouchCommand.border", Border.getDefaultBorder());

        // default pressed border for button
        themeProps.put("Button.press#border", Border.getDefaultBorder().createPressedVersion());
        themeProps.put("Button.press#padding", "4,4,4,4");
        themeProps.put("TouchCommand.press#border", Border.getDefaultBorder().createPressedVersion());
        themeProps.put("TouchCommand.press#padding", "6,6,6,6");

        themeProps.put("TextArea.border", Border.getDefaultBorder());
        themeProps.put("TextField.border", Border.getDefaultBorder());
        themeProps.put("ComboBox.border", Border.getDefaultBorder());
        themeProps.put("ComboBoxPopup.border", Border.getDefaultBorder());
        themeProps.put("Title.margin", "0,0,0,0");
        themeProps.put("CommandList.margin", "0,0,0,0");
        themeProps.put("CommandList.padding", "0,0,0,0");
        themeProps.put("CommandList.transparency", "0");
        themeProps.put("ComboBoxList.margin", "0,0,0,0");
        themeProps.put("ComboBoxList.padding", "0,0,0,0");
        themeProps.put("ComboBoxList.transparency", "0");
        themeProps.put("TableCell.transparency", "0");
        themeProps.put("TableHeader.transparency", "0");
        themeProps.put("Menu.padding", "0,0,0,0");
        themeProps.put("Command.margin", "0,0,0,0");
        themeProps.put("ComboBoxItem.margin", "0,0,0,0");
        themeProps.put("Container.transparency", "0");
        themeProps.put("ContentPane.transparency", "0");
        themeProps.put("List.transparency", "0");
        themeProps.put("List.margin", "0,0,0,0");
        themeProps.put("SoftButton.transparency", "255");
        themeProps.put("SoftButton.margin", "0,0,0,0");
        themeProps.put("SoftButton.padding", "2,2,2,2");
        themeProps.put("Button.padding", "4,4,4,4");
        themeProps.put("TouchCommand.padding", "6,6,6,6");
        themeProps.put("TouchCommand.margin", "0,0,0,0");
        themeProps.put("Container.margin", "0,0,0,0");
        themeProps.put("Container.padding", "0,0,0,0");
        themeProps.put("ContentPane.margin", "0,0,0,0");
        themeProps.put("ContentPane.padding", "0,0,0,0");
        themeProps.put("Title.transparency", "255");
        themeProps.put("TabbedPane.margin", "0,0,0,0");
        themeProps.put("TabbedPane.padding", "0,0,0,0");
        themeProps.put("TabbedPane.transparency", "0");
        themeProps.put("ScrollThumb.padding", "0,0,0,0");
        themeProps.put("ScrollThumb.margin", "0,0,0,0");
        themeProps.put("ScrollThumb.bgColor", "0");
        themeProps.put("Scroll.margin", "0,0,0,0");
        themeProps.put("Scroll.padding", "1,1,1,1");
        themeProps.put("HorizontalScrollThumb.padding", "0,0,0,0");
        themeProps.put("HorizontalScrollThumb.bgColor", "0");
        themeProps.put("HorizontalScrollThumb.margin", "0,0,0,0");
        themeProps.put("HorizontalScroll.margin", "0,0,0,0");
        themeProps.put("HorizontalScroll.padding", "1,1,1,1");
        themeProps.put("Form.padding", "0,0,0,0");
        themeProps.put("Form.margin", "0,0,0,0");
        themeProps.put("ListRenderer.transparency", "0");
        themeProps.put("Command.transparency", "0");
        themeProps.put("ComboBoxItem.transparency", "0");

        
        themeProps.put("Command.sel#transparency", "0");
        themeProps.put("ComboBoxItem.sel#transparency", "0");
        themeProps.put("ListRenderer.sel#transparency", "100");
        themeProps.put("Button.sel#border", Border.getDefaultBorder());
        themeProps.put("TouchCommand.sel#border", Border.getDefaultBorder());
        themeProps.put("TextArea.sel#border", Border.getDefaultBorder());
        themeProps.put("TextField.sel#border", Border.getDefaultBorder());
        themeProps.put("ComboBox.sel#border", Border.getDefaultBorder());
        themeProps.put("ComboBoxPopup.sel#border", Border.getDefaultBorder());
        themeProps.put("Title.sel#margin", "0,0,0,0");
        themeProps.put("CommandList.sel#margin", "0,0,0,0");
        themeProps.put("CommandList.sel#padding", "0,0,0,0");
        themeProps.put("CommandList.sel#transparency", "0");
        themeProps.put("Menu.sel#padding", "0,0,0,0");
        themeProps.put("Command.sel#margin", "0,0,0,0");
        themeProps.put("ComboBoxItem.sel#margin", "0,0,0,0");

        themeProps.put("Container.sel#transparency", "0");
        themeProps.put("ContentPane.sel#transparency", "0");
        themeProps.put("List.sel#transparency", "0");
        themeProps.put("SoftButton.sel#transparency", "255");
        themeProps.put("List.sel#margin", "0,0,0,0");
        themeProps.put("SoftButton.sel#margin", "0,0,0,0");
        themeProps.put("SoftButton.sel#padding", "2,2,2,2");
        themeProps.put("Button.sel#padding", "4,4,4,4");
        themeProps.put("TouchCommand.sel#padding", "6,6,6,6");
        themeProps.put("TouchCommand.sel#margin", "0,0,0,0");
        themeProps.put("Container.sel#margin", "0,0,0,0");
        themeProps.put("Container.sel#padding", "0,0,0,0");
        themeProps.put("ContentPane.sel#margin", "0,0,0,0");
        themeProps.put("ContentPane.sel#padding", "0,0,0,0");
        themeProps.put("Title.sel#transparency", "255");
        themeProps.put("TabbedPane.sel#margin", "0,0,0,0");
        themeProps.put("TabbedPane.sel#padding", "0,0,0,0");
        themeProps.put("Form.sel#padding", "0,0,0,0");
        themeProps.put("Form.sel#margin", "0,0,0,0");
        themeProps.put("sel#transparency", "255");
        themeProps.put("TableCell.sel#transparency", "0");
        themeProps.put("TableHeader.sel#transparency", "0");
    }
    
    /**
     * Allows manual theme loading from a hashtable of key/value pairs
     * 
     * @param themeProps the properties of the given theme
     */
    public void setThemeProps(Hashtable themeProps) {
        if(accessible) {
            setThemePropsImpl(themeProps);
        }
    }
    
    /**
     * Adds the given theme properties on top of the existing properties without
     * clearing the existing theme first
     *
     * @param themeProps the properties of the given theme
     */
    public void addThemeProps(Hashtable themeProps) {
        if(accessible) {
            buildTheme(themeProps);
        }
    }

    void setThemePropsImpl(Hashtable themeProps) {
        resetThemeProps();
        styles.clear();
        selectedStyles.clear();
        imageCache.clear();
        if(themelisteners != null){
            themelisteners.fireActionEvent(new ActionEvent(themeProps));
        }
        buildTheme(themeProps);
        current.refreshTheme();
    }

    private void buildTheme(Hashtable themeProps) {
        Enumeration e = themeProps.keys();
        while(e.hasMoreElements()) {
            Object key = e.nextElement();
            this.themeProps.put(key, themeProps.get(key));
        }
        
        // necessary to clear up the style so we don't get resedue from the previous UI
        defaultStyle = new Style();
        
        //create's the default style
        defaultStyle = createStyle("", "", false);
        defaultSelectedStyle = new Style(defaultStyle);
        defaultSelectedStyle = createStyle("", "sel#", true);    
    }

    private Style createStyle(String id, String prefix, boolean selected) {
        Style style;
        String originalId = id;
        if(selected) {
            style = new Style(defaultSelectedStyle);
        } else {
            style = new Style(defaultStyle);
        }
        if (prefix != null && prefix.length() > 0) {
            id += prefix;
        }
        if(themeProps != null){
            String bgColor;
            String fgColor;
            Object border;
            
            bgColor = (String)themeProps.get(id + Style.BG_COLOR);
            fgColor = (String)themeProps.get(id + Style.FG_COLOR);
            border = themeProps.get(id + Style.BORDER);
            Object bgImage = themeProps.get(id + Style.BG_IMAGE);
            String transperency = (String)themeProps.get(id + Style.TRANSPARENCY);
            String margin = (String)themeProps.get(id + Style.MARGIN);
            String padding = (String)themeProps.get(id + Style.PADDING);
            Object font = themeProps.get(id + Style.FONT);
            
            Byte backgroundType = (Byte)themeProps.get(id + Style.BACKGROUND_TYPE);
            Byte backgroundAlignment = (Byte)themeProps.get(id + Style.BACKGROUND_ALIGNMENT);
            Object[] backgroundGradient = (Object[])themeProps.get(id + Style.BACKGROUND_GRADIENT);
            if(bgColor != null){
                style.setBgColor(Integer.valueOf(bgColor, 16).intValue());
            }
            if(fgColor != null){
                style.setFgColor(Integer.valueOf(fgColor, 16).intValue());
            }
            if(transperency != null){
                style.setBgTransparency(Integer.valueOf(transperency).intValue());
            } else {
                if(selected) {
                    transperency = (String)themeProps.get(originalId + Style.TRANSPARENCY);
                    if(transperency != null){
                        style.setBgTransparency(Integer.valueOf(transperency).intValue());
                    }
                }
            }
            if(margin != null){
                int [] marginArr = toIntArray(margin.trim());
                style.setMargin(marginArr[0], marginArr[1], marginArr[2], marginArr[3]);
            } 
            if(padding != null){
                int [] paddingArr = toIntArray(padding.trim());
                style.setPadding(paddingArr[0], paddingArr[1], paddingArr[2], paddingArr[3]);
            }
            if(backgroundType != null) {
                style.setBackgroundType(backgroundType.byteValue());
            }
            if(backgroundAlignment != null) {
                style.setBackgroundAlignment(backgroundAlignment.byteValue());
            }
            if(backgroundGradient != null) {
                if(backgroundGradient.length < 5) {
                    Object[] a = new Object[5];
                    System.arraycopy(backgroundGradient, 0, a, 0, backgroundGradient.length);
                    backgroundGradient = a;
                    backgroundGradient[4] = new Float(1);
                }
                style.setBackgroundGradient(backgroundGradient);
            }
            if(bgImage != null){
                Image im = null;
                if(bgImage instanceof String){
                    try {
                        String bgImageStr = (String)bgImage;
                        if(imageCache.contains(bgImageStr)) {
                            im = (Image)imageCache.get(bgImageStr);
                        } else { 
                            if(bgImageStr.startsWith("/")) {
                                im = Image.createImage(bgImageStr);
                            } else {
                                im = parseImage((String)bgImage);
                            }
                            imageCache.put(bgImageStr, im);
                        }
                        themeProps.put(id + Style.BG_IMAGE, im);
                    } catch (IOException ex) {
                        System.out.println("failed to parse image for id = "+id + Style.BG_IMAGE);
                    }
                }else{
                    im = (Image)bgImage;
                }
                // this code should not excute in the resource editor!
                if(id.indexOf("Form") > -1){
                    if((im.getWidth() != Display.getInstance().getDisplayWidth() || 
                       im.getHeight() != Display.getInstance().getDisplayHeight())
                       && style.getBackgroundType() == Style.BACKGROUND_IMAGE_SCALED && accessible) {
                       im.scale(Display.getInstance().getDisplayWidth(), 
                               Display.getInstance().getDisplayHeight());
                    }
                }
                style.setBgImage(im);
            }
            if(font != null){
                if(font instanceof String){
                    style.setFont(parseFont((String)font));
                }else{
                    style.setFont((com.sun.lwuit.Font)font);
                }
            }
            
            style.setBorder((Border)border);
            style.resetModifiedFlag();
        } 
        
        return style;
    }
    
    /**
     * This method is used to parse the margin and the padding
     * @param str
     * @return
     */
    private int [] toIntArray(String str){
        int [] retVal = new int[4];
        str  = str + ",";
        for(int i=0; i< retVal.length; i++){
            retVal[i] = Integer.parseInt(str.substring(0, str.indexOf(",")));
            str = str.substring(str.indexOf(",") + 1, str.length());
        }
        return retVal;
    }
    
    
    private static Image parseImage(String value) throws IOException {
        int index = 0;
        byte [] imageData = new byte[value.length()/2];
        while(index < value.length()){
            String byteStr = value.substring(index, index + 2);
            imageData[index/2] = Integer.valueOf(byteStr, 16).byteValue();
            index += 2;
        }
        ByteArrayInputStream in = new ByteArrayInputStream(imageData);
        Image image = Image.createImage(in);
        in.close();
        return image;
    }
        
    private static com.sun.lwuit.Font parseFont(String fontStr) {
        if(fontStr.startsWith("System")){
            int face = 0;
            int style = 0;
            int size = 0;
            String faceStr, styleStr, sizeStr;
            String sysFont = fontStr.substring(fontStr.indexOf("{") + 1, fontStr.indexOf("}"));
            faceStr = sysFont.substring(0, sysFont.indexOf(";"));
            sysFont = sysFont.substring(sysFont.indexOf(";") + 1, sysFont.length());
            styleStr = sysFont.substring(0, sysFont.indexOf(";"));
            sizeStr = sysFont.substring(sysFont.indexOf(";") + 1, sysFont.length());
            
            if(faceStr.indexOf("FACE_SYSTEM") > -1){
                face = Font.FACE_SYSTEM;
            }else if(faceStr.indexOf("FACE_MONOSPACE") > -1){
                face = Font.FACE_MONOSPACE;
            }else if(faceStr.indexOf("FACE_PROPORTIONAL") > -1){
                face = Font.FACE_PROPORTIONAL;
            }
            
            if(styleStr.indexOf("STYLE_PLAIN") > -1){
                style = Font.STYLE_PLAIN;
            }else{
                if(styleStr.indexOf("STYLE_BOLD") > -1){
                    style = Font.STYLE_BOLD;
                }
                if(styleStr.indexOf("STYLE_ITALIC") > -1){
                    style = style | Font.STYLE_ITALIC;
                }
                if(styleStr.indexOf("STYLE_UNDERLINED") > -1){
                    style = style | Font.STYLE_UNDERLINED;
                }
            }
            
            if(sizeStr.indexOf("SIZE_SMALL") > -1){
                size = Font.SIZE_SMALL;
            }else if(sizeStr.indexOf("SIZE_MEDIUM") > -1){
                size = Font.SIZE_MEDIUM;
            }else if(sizeStr.indexOf("SIZE_LARGE") > -1){
                size = Font.SIZE_LARGE;
            }
            
            
            return com.sun.lwuit.Font.createSystemFont(face, style, size);            
        } else {
            if(fontStr.toLowerCase().startsWith("bitmap")) {
                try {
                    String bitmapFont = fontStr.substring(fontStr.indexOf("{") + 1, fontStr.indexOf("}"));
                    String nameStr;
                    nameStr = bitmapFont.substring(0, bitmapFont.length());

                    
                    if(nameStr.toLowerCase().startsWith("highcontrast")) {
                        nameStr = nameStr.substring(nameStr.indexOf(";") + 1, nameStr.length());
                        com.sun.lwuit.Font f = com.sun.lwuit.Font.getBitmapFont(nameStr);
                        f.addContrast((byte)30);
                        return f;
                    }
                    
                    return com.sun.lwuit.Font.getBitmapFont(nameStr);
                } catch (Exception ex) {
                    // illegal argument exception?
                    ex.printStackTrace();
                }
            }
        }
        // illegal argument?
        return null;
    }
    
    /**
     * The resource bundle allows us to implicitly localize the UI on the fly, once its
     * installed all internal application strings query the resource bundle and extract
     * their values from this table if applicable.
     * 
     * @return the localization bundle
     */
    public Hashtable getResourceBundle() {
        return resourceBundle;
    }

    /**
     * The resource bundle allows us to implicitly localize the UI on the fly, once its
     * installed all internal application strings query the resource bundle and extract
     * their values from this table if applicable.
     * 
     * @param resourceBundle the localization bundle
     */
    public void setResourceBundle(Hashtable resourceBundle) {
        this.resourceBundle = resourceBundle;
    }
    
    /**
     * Localizes the given string from the resource bundle if such a String exists in the
     * resource bundle. If no key exists in the bundle then or a bundle is not installed
     * the default value is returned.
     * 
     * @param key The key used to lookup in the resource bundle
     * @param defaultValue the value returned if no such key exists
     * @return either default value or the appropriate value
     */
    public String localize(String key, String defaultValue) {
        if(resourceBundle != null) {
            Object o = resourceBundle.get(key);
            if(o != null) {
                return (String)o;
            }
        }
        return defaultValue;
    }
    

    /**
     * Adds a Theme refresh listener.
     * The listenres will get a callback when setThemeProps method is invoked.
     * 
     * @param l an ActionListener to be added
     */
    public void addThemeRefreshListener(ActionListener l) {

        if (themelisteners == null) {
            themelisteners = new EventDispatcher();
        }
        themelisteners.addListener(l);
    }
    
    /**
     * Removes a Theme refresh listener.
     * 
     * @param l an ActionListener to be removed
     */
    public void removeThemeRefreshListener(ActionListener l) {

        if (themelisteners == null) {
            return;
        }
        themelisteners.removeListener(l);
    }
}
