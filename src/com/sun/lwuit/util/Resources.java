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

import com.sun.lwuit.Display;
import com.sun.lwuit.EncodedImage;
import com.sun.lwuit.Font;
import com.sun.lwuit.Image;
import com.sun.lwuit.StaticAnimation;
import com.sun.lwuit.plaf.Border;
import com.sun.lwuit.plaf.Style;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * Loads resources from the binary resource file generated during the build process or via the LWUIT Designer.
 * A resource is loaded entirely into memory since random file access is not supported
 * in Java ME, any other approach would be inefficient. This means that memory must
 * be made available to accommodate the resource file. 
 * 
 * @author Shai Almog
 */
public class Resources {
    /**
     * Magic numbers to prevent data corruption
     */
    static final byte MAGIC_THEME_LEGACY = (byte)0xF7;
    static final byte MAGIC_ANIMATION_LEGACY = (byte)0xF8;
    static final byte MAGIC_INDEXED_IMAGE_LEGACY = (byte)0xF4;
    static final byte MAGIC_FONT_LEGACY = (byte)0xF6;
    static final byte MAGIC_INDEXED_FONT_LEGACY = (byte)0xFB;
    static final byte MAGIC_IMAGE_LEGACY = (byte)0xF3;


    static final byte MAGIC_FONT = (byte)0xFC;
    static final byte MAGIC_IMAGE = (byte)0xFD;
    static final byte MAGIC_L10N = (byte)0xF9;
    static final byte MAGIC_DATA = (byte)0xFA;
    static final byte MAGIC_HEADER = (byte)0xFF;

    private short majorVersion;
    private short minorVersion;

    /**
     * Temporary member for compatibility with older versions, in future versions
     * this will superceed the MAGIC_THEME property
     */
    static final byte MAGIC_THEME = (byte)0xF2;

    static final int BORDER_TYPE_EMPTY = 0;
    static final int BORDER_TYPE_LINE = 1;
    static final int BORDER_TYPE_ROUNDED = 2;
    static final int BORDER_TYPE_ETCHED_LOWERED = 4;
    static final int BORDER_TYPE_ETCHED_RAISED = 5;
    static final int BORDER_TYPE_BEVEL_RAISED = 6;
    static final int BORDER_TYPE_BEVEL_LOWERED = 7;
    static final int BORDER_TYPE_IMAGE = 8;

    // for use by the resource editor
    private static Class classLoader = Resources.class;

    private String[] metaData;

    static void setClassLoader(Class cls) {
        classLoader = cls;
    }
    
    /**
     * Hashtable containing the mapping between element types and their names in the
     * resource hashtable
     */
    private Hashtable resourceTypes = new Hashtable();
    
    /**
     * A cache within the resource allowing us to preserve some resources in memory
     * so they can be utilized by a theme when it is loaded
     */
    private Hashtable resources = new Hashtable();
    
    private DataInputStream input; 
    
    // for internal use by the resource editor, creates an empty resource
    Resources() {
    }
    
    Resources(InputStream input) throws IOException {
        openFile(input);
    }
    
    void clear() {
        majorVersion = 0;
        minorVersion = 0;
        resourceTypes.clear();
        resources.clear();
        input = null;
    }
    
    /**
     * This method is used by the LWUIT Designer
     */
    void startingEntry(String id, byte magic) {
    }

    void openFile(InputStream input) throws IOException {
        clear();
        this.input = new DataInputStream(input);
        int resourceCount = this.input.readShort();
        if(resourceCount < 0) {
            throw new IOException("Invalid resource file!");
        }
        for(int iter = 0 ; iter < resourceCount ; iter++) {
            byte magic = this.input.readByte();
            String id = this.input.readUTF();
            startingEntry(id, magic);
            switch(magic) {
                case MAGIC_HEADER:
                    readHeader();
                    continue;
                case MAGIC_THEME:
                    setResource(id, MAGIC_THEME, loadTheme(id, magic == MAGIC_THEME));
                    continue;
                case MAGIC_IMAGE:
                    setResource(id, magic, createImage());
                    continue;
                case MAGIC_FONT:
                    setResource(id, magic, loadFont(this.input, id, false));
                    continue;
                case MAGIC_DATA:
                    setResource(id, magic, createData());
                    continue;
                case MAGIC_L10N:
                    setResource(id, magic, loadL10N());
                    continue;

                // legacy file support to be removed
                case MAGIC_IMAGE_LEGACY:
                    setResource(id, MAGIC_IMAGE, createImage());
                    continue;
                case MAGIC_INDEXED_IMAGE_LEGACY:
                    setResource(id, MAGIC_IMAGE, createPackedImage8());
                    continue;
                case MAGIC_THEME_LEGACY:
                    setResource(id, MAGIC_THEME, loadTheme(id, magic == MAGIC_THEME));
                    continue;
                case MAGIC_FONT_LEGACY:
                    setResource(id, MAGIC_FONT, loadFont(this.input, id, false));
                    continue;
                case MAGIC_INDEXED_FONT_LEGACY:
                    setResource(id, MAGIC_FONT, loadFont(this.input, id, true));
                    continue;
                case MAGIC_ANIMATION_LEGACY:
                    setResource(id, MAGIC_IMAGE, loadAnimation(this.input));
                    continue;
                default:
                    throw new IOException("Corrupt theme file unrecognized magic number: " + Integer.toHexString(magic & 0xff));
            }
        }
    }
    
    /**
     * Reads the header of the resource file
     */
    private void readHeader() throws IOException {
        int size = input.readShort();
        majorVersion = input.readShort();
        minorVersion = input.readShort();
        
        metaData = new String[input.readShort()];
        for(int iter = 0 ; iter < metaData.length ; iter++) {
            metaData[iter] = input.readUTF();
        }
    }

    /**
     * Returns the version number for this resource file. 
     * This value relates to the value from the header defined by the resource file
     * specification. 0 is returned for legacy versions of the resource file format.
     * 
     * @return major version number for the resource file
     */
    public int getMajorVersion() {
        return majorVersion;
    }

    /**
     * Returns the minor version number for this resource file
     * This value relates to the value from the header defined by the resource file
     * specification. 
     *
     * @return minor version number for the resource file
     */
    public int getMinorVersion() {
        return minorVersion;
    }

    /**
     * Returns optional meta-data associated with the resource file
     *
     * @return optional meta-data associated with the file
     */
    public String[] getMetaData() {
        return metaData;
    }

    /**
     * Returns the names of the resources within this bundle
     * 
     * @return array of names of all the resources in this bundle
     */
    public String[] getResourceNames() {
        String[] arr = new String[resourceTypes.size()];
        Enumeration e = resourceTypes.keys();
        for(int iter = 0 ; iter < arr.length ; iter++) {
            arr[iter] = (String)e.nextElement();
        }
        return arr;
    }

    /**
     * Returns the names of the data resources within this bundle
     * 
     * @return array of names of the data resources in this bundle
     */
    public String[] getDataResourceNames() {
        return getResourceTypeNames(MAGIC_DATA);
    }

    /**
     * For internal use only
     */
    void setResource(String id, byte type, Object value) {
        if(value == null) {
            resources.remove(id);
            resourceTypes.remove(id);
        } else {
            resources.put(id, value);
            resourceTypes.put(id, new Byte(type));
        }
    }
    

    /**
     * Returns the names of the localization bundles within this bundle
     * 
     * @return array of names of the localization resources in this bundle
     */
    public String[] getL10NResourceNames() {
        return getResourceTypeNames(MAGIC_L10N);
    }

    /**
     * Returns the names of the fonts within this bundle
     * 
     * @return array of names of the font resources in this bundle
     */
    public String[] getFontResourceNames() {
        Vector vec = new Vector();
        Enumeration e = resourceTypes.keys();
        while(e.hasMoreElements()) {
            String c = (String)e.nextElement();
            if(isFont(c)) {
                vec.addElement(c);
            }
        }
        return toStringArray(vec);
    }

    /**
     * Returns the names of the images within this bundle
     * 
     * @return array of names of the image resources in this bundle
     */
    public String[] getThemeResourceNames() {
        Vector vec = new Vector();
        Enumeration e = resourceTypes.keys();
        while(e.hasMoreElements()) {
            String c = (String)e.nextElement();
            if(isTheme(c)) {
                vec.addElement(c);
            }
        }
        return toStringArray(vec);
    }

    /**
     * Returns the names of the images within this bundle
     * 
     * @return array of names of the image resources in this bundle
     */
    public String[] getImageResourceNames() {
        Vector vec = new Vector();
        Enumeration e = resourceTypes.keys();
        while(e.hasMoreElements()) {
            String c = (String)e.nextElement();
            if(isImage(c)) {
                vec.addElement(c);
            }
        }
        return toStringArray(vec);
    }

    /**
     * Returns the names of the animations within this bundle
     * 
     * @return array of names of the animation resources in this bundle
     * @deprecated use getImageResourceNames instead
     */
    public String[] getAnimationResourceNames() {
        return getResourceTypeNames(MAGIC_ANIMATION_LEGACY);
    }
    
    /**
     * For internal use only
     */
    byte getResourceType(String name) {
        return ((Byte)resourceTypes.get(name)).byteValue();
    }
    
    private String[] getResourceTypeNames(byte b) {
        Vector vec = new Vector();
        Enumeration e = resourceTypes.keys();
        while(e.hasMoreElements()) {
            String c = (String)e.nextElement();
            if(((Byte)resourceTypes.get(c)).byteValue() == b) {
                vec.addElement(c);
            }
        }
        return toStringArray(vec);
    }

    private static String[] toStringArray(Vector v) {
        String[] s = new String[v.size()];
        for(int iter = 0 ; iter < s.length ; iter++) {
            s[iter] = (String)v.elementAt(iter);
        }
        return s;
    }

    /**
     * Returns true if this is a generic data resource
     * 
     * @param name the name of the resource
     * @return true if the resource is a data resource
     * @throws NullPointerException if the resource doesn't exist
     */
    public boolean isL10N(String name) {
        byte b = ((Byte)resourceTypes.get(name)).byteValue();
        return b == MAGIC_L10N;
    }
    
    /**
     * Returns true if this is a theme resource
     * 
     * @param name the name of the resource
     * @return true if the resource is a theme
     * @throws NullPointerException if the resource doesn't exist
     */
    public boolean isTheme(String name) {
        byte b = ((Byte)resourceTypes.get(name)).byteValue();
        return b == MAGIC_THEME_LEGACY || b == MAGIC_THEME;
    }

    /**
     * Returns true if this is a font resource
     * 
     * @param name the name of the resource
     * @return true if the resource is a font
     * @throws NullPointerException if the resource doesn't exist
     */
    public boolean isFont(String name) {
        byte b = ((Byte)resourceTypes.get(name)).byteValue();
        return b == MAGIC_FONT || b == MAGIC_FONT_LEGACY || b == MAGIC_INDEXED_FONT_LEGACY;
    }

    /**
     * Returns true if this is an animation resource
     * 
     * @param name the name of the resource
     * @return true if the resource is an animation
     * @throws NullPointerException if the resource doesn't exist
     * @deprecated animations are no longer distinguished from images in the resource file, use Image.isAnimation instead
     */
    public boolean isAnimation(String name) {
        byte b = ((Byte)resourceTypes.get(name)).byteValue();
        return b == MAGIC_ANIMATION_LEGACY;
    }

    /**
     * Returns true if this is a data resource
     * 
     * @param name the name of the resource
     * @return true if the resource is a data resource
     * @throws NullPointerException if the resource doesn't exist
     */
    public boolean isData(String name) {
        byte b = ((Byte)resourceTypes.get(name)).byteValue();
        return b == MAGIC_DATA;
    }

    /**
     * Returns true if this is an image resource
     * 
     * @param name the name of the resource
     * @return true if the resource is an image
     * @throws NullPointerException if the resource doesn't exist
     */
    public boolean isImage(String name) {
        byte b = ((Byte)resourceTypes.get(name)).byteValue();
        return b == MAGIC_IMAGE_LEGACY || b == MAGIC_ANIMATION_LEGACY || b == MAGIC_INDEXED_IMAGE_LEGACY || b == MAGIC_IMAGE;
    }
    
    /**
     * Creates a resource object from the local JAR resource identifier
     * 
     * @param resource a local reference to a resource using the syntax of Class.getResourceAsStream(String)
     * @return a resource object
     * @throws java.io.IOException if opening/reading the resource fails
     */
    public static Resources open(String resource) throws IOException {
        try {
            InputStream is = Display.getInstance().getResourceAsStream(classLoader, resource);
            Resources r = new Resources(is);
            is.close();
            return r;
        } catch(RuntimeException err) {
            // intercept exceptions since user code might not deal well with runtime exceptions 
            err.printStackTrace();
            throw new IOException(err.getMessage());
        }
    }

    StaticAnimation loadAnimation(DataInputStream input) throws IOException {
        return StaticAnimation.createAnimation(input);
    }
    
    /**
     * Creates a resource object from the given input stream
     * 
     * @param resource stream from which to read the resource
     * @return a resource object
     * @throws java.io.IOException if opening/reading the resource fails
     */
    public static Resources open(InputStream resource) throws IOException {
        return new Resources(resource);
    }

    /**
     * Returns the image resource from the file
     * 
     * @param id name of the image resource
     * @return cached image instance
     */
    public Image getImage(String id) {
        return (Image)resources.get(id);
    }

    /**
     * Returns the animation resource from the file
     * 
     * @param id name of the animation resource
     * @return cached image instance
     * @deprecated use getImage(String) instead
     */
    public StaticAnimation getAnimation(String id) {
        return (StaticAnimation)resources.get(id);
    }
    
    /**
     * Returns the data resource from the file
     * 
     * @param id name of the data resource
     * @return newly created input stream that allows reading the data of the resource
     */
    public InputStream getData(String id) {
        return new ByteArrayInputStream((byte[])resources.get(id));
    }
    
    /**
     * Returns a hashmap containing localized String key/value pairs for the given locale name
     * 
     * @param id the name of the locale resource
     * @param locale name of the locale resource
     * @return Hashtable containing key value pairs for localized data
     */
    public Hashtable getL10N(String id, String locale) {
        return (Hashtable)((Hashtable)resources.get(id)).get(locale);
    }

    /**
     * Returns an enumration of the locales supported by this resource id
     * 
     * @param id the name of the locale resource
     * @return enumeration of strings containing bundle names
     */
    public Enumeration listL10NLocales(String id) {
        return ((Hashtable)resources.get(id)).keys();
    }

    /**
     * Returns the font resource from the file
     * 
     * @param id name of the font resource
     * @return cached font instance
     */
    public Font getFont(String id) {
        return (Font)resources.get(id);
    }
    
    /**
     * Returns the theme resource from the file
     * 
     * @param id name of the theme resource
     * @return cached theme instance
     */
    public Hashtable getTheme(String id) {
        Hashtable h = (Hashtable)resources.get(id);
        
        // theme can be null in valid use cases such as the resource editor
        if(h != null && h.containsKey("uninitialized")) {
            Enumeration e = h.keys();
            while(e.hasMoreElements()) {
                String key = (String)e.nextElement();
                if(key.endsWith("font") || (key.endsWith("Image") && !key.endsWith("scaledImage"))) {
                    Object value = h.get(key);
                    if(value == null) {
                        throw new IllegalArgumentException("Couldn't find resource: " + key);
                    }
                    
                    // the resource was not already loaded when we loaded the theme
                    // it must be loaded now so we can resolve the temporary name
                    if(value instanceof String) {
                        Object o = resources.get(value);
                        if(o == null) {
                            throw new IllegalArgumentException("Theme entry for " + key + " could not be found: " + value);
                        }
                        h.put(key, o);
                    }
                }
                // if this is a border we might need to do additional work for older versions
                // of LWUIT and for the case of an image border where the images might not have
                // been loaded yet when the border was created
                if(key.endsWith("order")) {
                    Border b = confirmBorder(h, key);
                    if(majorVersion == 0 && minorVersion == 0) {
                        b.setPressedInstance(confirmBorder(h, key + "Pressed"));
                        b.setFocusedInstance(confirmBorder(h, key + "Focused"));
                        h.remove(key + "Pressed");
                        h.remove(key + "Focused");
                    }
                    h.put(key, b);
                }
            }
            h.remove("uninitialized");
        }
        return h;
    }
    
    private Border confirmBorder(Hashtable h, String key) {
        Object val = h.get(key);
        if(val == null) {
            return null;
        }
        if(!(val instanceof Border)) {
            String[] value = (String[])val;
            if(value == null) {
                throw new IllegalArgumentException("Couldn't find resource: " + key);
            }

            // the resource was not already loaded when we loaded the theme
            // it must be loaded now so we can resolve the temporary name
            Border imageBorder = createImageBorder(value);
            return imageBorder;
        }
        return (Border)val;
    }
    
    private Border createImageBorder(String[] value) {
        Image[] images = new Image[value.length];
        for(int iter = 0 ; iter < value.length ; iter++) {
            images[iter] = (Image)resources.get(value[iter]);
        }
        switch(images.length) {
            case 2:
               return Border.createImageBorder(images[0], images[1], null); 
            case 3:
               return Border.createImageBorder(images[0], images[1], images[2]); 
            case 8:
               return Border.createImageBorder(images[0], images[1], images[2],
                   images[3], images[4], images[5], images[6], images[7], null); 
            default:
               return Border.createImageBorder(images[0], images[1], images[2],
                   images[3], images[4], images[5], images[6], images[7], images[8]); 
        }
    }
    
    Object getResourceObject(String res) {
        return resources.get(res);
    }
    
    Image createImage() throws IOException {
        if(majorVersion == 0 && minorVersion == 0) {
            byte[] data = new byte[input.readInt()];
            input.readFully(data, 0, data.length);
            return EncodedImage.create(data);
        } else {
            int type = input.readByte() & 0xff;
            switch(type) {
                // PNG file
                case 0xf1:

                // JPEG File
                case 0xf2:
                    byte[] data = new byte[input.readInt()];
                    input.readFully(data, 0, data.length);
                    return EncodedImage.create(data);

                // Indexed image
                case 0xf3:
                    return createPackedImage8();

                // animation
                case 0xf4:
                    return loadAnimation(input);

                // SVG
                case 0xf5:
                    int svgSize = input.readInt();
                    if(Image.isSVGSupported()) {
                        byte[] s = new byte[svgSize];
                        input.readFully(s);
                        String baseURL = input.readUTF();
                        boolean animated = input.readBoolean();
                        loadSVGRatios(input);
                        byte[] fallback = new byte[input.readInt()];
                        if(fallback.length > 0) {
                            input.readFully(fallback, 0, fallback.length);
                        }
                        return Image.createSVG(baseURL, animated, s);
                    } else {
                        svgSize -= input.skip(svgSize);
                        while(svgSize > 0) {
                            svgSize -= input.skip(svgSize);
                        }
                        // read the base url, the animated property and screen ratios to skip them as well...
                        input.readUTF();
                        input.readBoolean();
                        input.readFloat();
                        input.readFloat();

                        byte[] fallback = new byte[input.readInt()];
                        input.readFully(fallback, 0, fallback.length);
                        return EncodedImage.create(fallback);                      
                    }

                // Fail this is the wrong data type
                default:
                    throw new IOException("Illegal type while creating image: " + Integer.toHexString(type));
            }
        }
    }

    void loadSVGRatios(DataInputStream input) throws IOException {
        input.readFloat();
        input.readFloat();
    }

    private byte[] createData() throws IOException {
        byte[] data = new byte[input.readInt()];
        input.readFully(data);
        return data;
    }

    Font loadFont(DataInputStream input, String id, boolean packed) throws IOException {
        if(majorVersion == 0 && minorVersion == 0) {
            Image bitmap;
            if(packed) {
                bitmap = createPackedImage8();
            } else {
                bitmap = createImage();
            }
            int charCount = input.readShort();
            int[] cutOffsets = new int[charCount];
            int[] charWidth = new int[charCount];
            for(int iter = 0 ; iter < charCount ; iter++) {
                cutOffsets[iter] = input.readShort();
                charWidth[iter] = input.readByte();
            }
            String charset = input.readUTF();
            Font old = Font.getBitmapFont(id);
            if(old != null) {
                return old;
            }
            return Font.createBitmapFont(id, bitmap, cutOffsets, charWidth, charset);
        }

        // read a system font fallback
        int fallback = input.readByte() & 0xff;

        // do we have an emedded truetype font? Do we support embedded fonts?
        boolean trueTypeIncluded = input.readBoolean();
        Font font = null;
        if(trueTypeIncluded) {
            int size = input.readInt();
            if(Font.isTrueTypeFileSupported()) {
                font = Font.createTrueTypeFont(input);
            } else {
                while(size > 0) {
                    size -= input.skip(size);
                }
            }
        }
        boolean lookupIncluded = input.readBoolean();
        if(lookupIncluded) {
            String lookup = input.readUTF();
            if(font == null && Font.isCreationByStringSupported()) {
                font = Font.create(lookup);
            }
        }
        boolean bitmapIncluded = input.readBoolean();
        if(bitmapIncluded) {
            font = loadBitmapFont(input, id, font);
        }
        if(font != null) {
            return font;
        }
        return Font.createSystemFont(fallback & (Font.FACE_MONOSPACE | Font.FACE_PROPORTIONAL | Font.FACE_SYSTEM),
                fallback & (Font.STYLE_BOLD | Font.STYLE_ITALIC | Font.STYLE_PLAIN | Font.STYLE_UNDERLINED),
                fallback & (Font.SIZE_LARGE | Font.SIZE_MEDIUM| Font.SIZE_SMALL));
    }

    void readRenderingHint(DataInputStream i) throws IOException {
        i.readByte();
    }

    Font loadBitmapFont(DataInputStream input, String id, com.sun.lwuit.Font font) throws IOException {
        Image bitmap = createImage();
        int charCount = input.readShort();
        int[] cutOffsets = new int[charCount];
        int[] charWidth = new int[charCount];
        for(int iter = 0 ; iter < charCount ; iter++) {
            cutOffsets[iter] = input.readShort();
        }
        for(int iter = 0 ; iter < charCount ; iter++) {
            charWidth[iter] = input.readByte();
        }
        String charset = input.readUTF();
        readRenderingHint(input);
        if(font == null) {
            if(Font.isBitmapFontEnabled()) {
                Font old = Font.getBitmapFont(id);
                if(old != null) {
                    // Returning bitmap font from cache, to prevent collision with an
                    // old resource file use Font.clearBitmapCache()
                    return old;
                }
                return Font.createBitmapFont(id, bitmap, cutOffsets, charWidth, charset);
            }
        }
        return font;
    }

    Hashtable loadTheme(String id, boolean newerVersion) throws IOException {
        Hashtable theme = new Hashtable();
        
        theme.put("name", id);
        
        // marks the theme as uninitialized so we can finish "wiring" cached resources
        theme.put("uninitialized", Boolean.TRUE);
        int size = input.readShort();
        for(int iter = 0 ; iter < size ; iter++) {
            String key = input.readUTF();

            // if this is a simple numeric value
            if(key.endsWith("Color")) {
                theme.put(key, Integer.toHexString(input.readInt()));
                continue;
            } 

            // if this is a short numeric value for transparency
            if(key.endsWith("ransparency")) {
                theme.put(key, "" + (input.readByte() & 0xff));
                continue;
            } 

            // if this is a padding or margin then we will have the 4 values as bytes
            if(key.endsWith("adding") || key.endsWith("argin")) {
                int p1 = input.readByte() & 0xff;
                int p2 = input.readByte() & 0xff;
                int p3 = input.readByte() & 0xff;
                int p4 = input.readByte() & 0xff;
                theme.put(key, "" + p1 + "," + p2 + "," + p3 + "," + p4);
                continue;
            }

            // border
            if(key.endsWith("order")) {
                if(majorVersion == 0 && minorVersion == 0) {
                    theme.put(key, createBorder(input, newerVersion));

                    if(newerVersion) {
                        if(input.readBoolean()) {
                            theme.put(key + "Pressed", createBorder(input, true));
                        }
                        if(input.readBoolean()) {
                            theme.put(key + "Focused", createBorder(input, true));
                        }
                    }
                } else {
                    int borderType = input.readShort() & 0xffff;
                    Object b = createBorder(input, borderType);
                    theme.put(key, b);
                }
                continue;
            }

            // if this is a font
            if(key.endsWith("ont")) {
                Font f;

                // is this a new font?
                if(input.readBoolean()) {
                    String fontId = input.readUTF();
                    f = (Font)resources.get(fontId);
                    
                    // if the font is not yet loaded
                    if(f == null) {
                        theme.put(key, fontId);
                        continue;
                    }
                } else {
                    f = Font.createSystemFont(input.readByte(), input.readByte(), input.readByte());
                }
                theme.put(key, f);
                continue;
            } 

            // the background property
            if(key.endsWith("ackground")) {
                int type = input.readByte() & 0xff;
                int pos = key.indexOf('.');
                if(pos > -1) {
                    key = key.substring(0, pos);
                } else {
                    key = "";
                }
                theme.put(key + Style.BACKGROUND_TYPE, new Byte((byte)type));

                switch(type) {
                    // Scaled Image
                    case 0xF1:
                    // Tiled Both Image
                    case 0xF4:
                        // the image name coupled with the type
                        theme.put(key + Style.BG_IMAGE, input.readUTF());
                        break;

                    // Aligned Image
                    case 0xF5:
                    // Tiled Vertically Image
                    case 0xF2:
                    // Tiled Horizontally Image
                    case 0xF3:
                        // the image name coupled with the type and with alignment information
                        String imageName = input.readUTF();
                        theme.put(key + Style.BG_IMAGE, imageName);
                        byte align = input.readByte();
                        theme.put(key + Style.BACKGROUND_ALIGNMENT, new Byte(align));
                        break;

                    // Horizontal Linear Gradient
                    case 0xF6:
                    // Vertical Linear Gradient
                    case 0xF7:
                        Float c =  new Float(0.5f);
                        theme.put(key + Style.BACKGROUND_GRADIENT, new Object[] {new Integer(input.readInt()), new Integer(input.readInt()),c, c, new Float(1)});
                        break;
                        
                    // Radial Gradient
                    case 0xF8:
                        int c1  = input.readInt();
                        int c2 = input.readInt();
                        float f1 = input.readFloat();
                        float f2 = input.readFloat();
                        float radialSize = 1;
                        if(minorVersion > 1) {
                            radialSize = input.readFloat();
                        }
                        theme.put(key + Style.BACKGROUND_GRADIENT, new Object[] {new Integer(c1),
                            new Integer(c2),
                            new Float(f1),
                            new Float(f2),
                            new Float(radialSize)});
                        break;
                }
                continue;
            }

            // if this is a background image bgImage
            if(key.endsWith("bgImage")) {
                String imageId = input.readUTF();
                Image i = (Image)resources.get(imageId);

                // if the font is not yet loaded
                if(i == null) {
                    theme.put(key, imageId);
                    continue;
                }
                theme.put(key, i);
                continue;
            } 

            if(key.endsWith("scaledImage")) {
                if(input.readBoolean()) {
                    theme.put(key, "true");
                } else {
                    theme.put(key, "false");
                }
                continue;
            }
            
            if(key.endsWith(Style.BACKGROUND_TYPE) || key.endsWith(Style.BACKGROUND_ALIGNMENT)) {
                theme.put(key, new Byte(input.readByte()));
                continue;
            }

            if(key.endsWith(Style.BACKGROUND_GRADIENT)) {
                if(minorVersion < 2) {
                    theme.put(key, new Object[] {
                        new Integer(input.readInt()),
                        new Integer(input.readInt()),
                        new Float(input.readFloat()),
                        new Float(input.readFloat())
                    });
                } else {
                    theme.put(key, new Object[] {
                        new Integer(input.readInt()),
                        new Integer(input.readInt()),
                        new Float(input.readFloat()),
                        new Float(input.readFloat()),
                        new Float(input.readFloat())
                    });
                }
                continue;
            }

            // thow an exception no idea what this is
            throw new IOException("Error while trying to read theme property: " + key);
        }
        return theme;
    }
    
    private Object createBorder(DataInputStream input, int type) throws IOException {
        switch(type) {
            // empty border
            case 0xff01:
                return Border.getEmpty();

            // Line border
            case 0xff02:
                // use theme colors?
                if(input.readBoolean()) {
                    return Border.createLineBorder(input.readByte());
                } else {
                    return Border.createLineBorder(input.readByte(), input.readInt());
                }

            // Rounded border
            case 0xff03:
                // use theme colors?
                if(input.readBoolean()) {
                    return Border.createRoundBorder(input.readByte(), input.readByte());
                } else {
                    return Border.createRoundBorder(input.readByte(), input.readByte(), input.readInt());
                }

            // Etched Lowered border
            case 0xff04:
                // use theme colors?
                if(input.readBoolean()) {
                    return Border.createEtchedLowered();
                } else {
                    return Border.createEtchedLowered(input.readInt(), input.readInt());
                }

            // Etched raised border
            case 0xff05:
                // use theme colors?
                if(input.readBoolean()) {
                    return Border.createEtchedRaised();
                } else {
                    return Border.createEtchedRaised(input.readInt(), input.readInt());
                }

            // Bevel raised
            case 0xff07:
                // use theme colors?
                if(input.readBoolean()) {
                    return Border.createBevelRaised();
                } else {
                    return Border.createBevelRaised(input.readInt(), input.readInt(), input.readInt(), input.readInt());
                }

            // Bevel lowered
            case 0xff06:
                // use theme colors?
                if(input.readBoolean()) {
                    return Border.createBevelLowered();
                } else {
                    return Border.createBevelLowered(input.readInt(), input.readInt(), input.readInt(), input.readInt());
                }

            // Image border
            case 0xff08:
                Object[] imageBorder = readImageBorder(input);
                return imageBorder;
        }
        return null;
    }

    private Object createBorder(DataInputStream input, boolean newerVersion) throws IOException {
        int type = input.readByte();
        switch(type) {
            case BORDER_TYPE_EMPTY:
                return Border.getEmpty();
            case BORDER_TYPE_LINE:
                // use theme colors?
                if(input.readBoolean()) {
                    return Border.createLineBorder(input.readByte());
                } else {
                    return Border.createLineBorder(input.readByte(), input.readInt());
                }
            case BORDER_TYPE_ROUNDED:
                // use theme colors?
                if(input.readBoolean()) {
                    return Border.createRoundBorder(input.readByte(), input.readByte());
                } else {
                    return Border.createRoundBorder(input.readByte(), input.readByte(), input.readInt());
                }
            case BORDER_TYPE_ETCHED_LOWERED:
                // use theme colors?
                if(input.readBoolean()) {
                    return Border.createEtchedLowered();
                } else {
                    return Border.createEtchedLowered(input.readInt(), input.readInt());
                }
            case BORDER_TYPE_ETCHED_RAISED:
                // use theme colors?
                if(input.readBoolean()) {
                    return Border.createEtchedRaised();
                } else {
                    return Border.createEtchedRaised(input.readInt(), input.readInt());
                }
            case BORDER_TYPE_BEVEL_RAISED:
                // use theme colors?
                if(input.readBoolean()) {
                    return Border.createBevelRaised();
                } else {
                    return Border.createBevelRaised(input.readInt(), input.readInt(), input.readInt(), input.readInt());
                }
            case BORDER_TYPE_BEVEL_LOWERED:
                // use theme colors?
                if(input.readBoolean()) {
                    return Border.createBevelLowered();
                } else {
                    return Border.createBevelLowered(input.readInt(), input.readInt(), input.readInt(), input.readInt());
                }
            case BORDER_TYPE_IMAGE:
                Object[] imageBorder = readImageBorder(input);
                
                if(!newerVersion) {
                    // legacy issue...
                    input.readBoolean();
                }
                
                return imageBorder;
        }
        return null;
    }
    
    private String[] readImageBorder(DataInputStream input) throws IOException {
        // Read number of images can be 2, 3, 8 or 9
        int size = input.readByte();
        String[] imageBorder = new String[size];
                
        for(int iter = 0 ; iter < size ; iter++) {
            imageBorder[iter] = input.readUTF();
        }
        return imageBorder;
    }
        
    private Hashtable loadL10N() throws IOException {
        Hashtable l10n = new Hashtable();

        int keys = input.readShort();
        int languages = input.readShort();
        String[] keyArray = new String[keys];
        for(int iter = 0 ; iter < keys ; iter++) {
            String key = input.readUTF();
            keyArray[iter] = key;
        }
        for(int iter = 0 ; iter < languages ; iter++) {        
            Hashtable currentLanguage = new Hashtable();
            String lang = input.readUTF();
            l10n.put(lang, currentLanguage);
            for(int valueIter =  0 ; valueIter < keys ; valueIter++) {
                currentLanguage.put(keyArray[valueIter], input.readUTF());
            }
        }
        return l10n;
    }
            
    /**
     * Creates a packed image from the input stream for an 8 bit packed image
     */
    private Image createPackedImage8() throws IOException {
        // read the length of the palette;
        int size = input.readByte() & 0xff;
        
        // 0 means the last bit overflowed, there is no sense for 0 sized palette
        if(size == 0) {
            size = 256;
        }
        int[] palette = new int[size];
        for(int iter = 0 ; iter < palette.length ; iter++) {
            palette[iter] = input.readInt();
        }
        int width = input.readShort();
        int height = input.readShort();
        byte[] data = new byte[width * height];
        input.readFully(data, 0, data.length);
        return Image.createIndexed(width, height, palette, data);
    }
}
