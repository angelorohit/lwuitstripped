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
import com.sun.lwuit.events.StyleListener;
import com.sun.lwuit.util.EventDispatcher;
import java.lang.ref.WeakReference;
import java.util.Vector;

/**
 * Represents the look of a given component: colors, fonts, transparency, margin and padding &amp; images.
 * <p>Each Component contains a Style Object and allows Style modification in Runtime
 * by Using {@code cmp.getStyle()}
 * The style is also used in Themeing, when a Theme is Changed the Styles Objects are been 
 * updated automatically.
 * <p>When changing a theme the elements changed manually in a style will not be updated
 * by the theme change by default. There are two ways to change that behavior:
 * <ol><li>Use the set method that accepts a second boolean argument and set it to true.
 * <li>Create a new style object and pass all the options in the constructor (without invoking setters manually).
 * </ol>
 * <p>
 * The Margin and Padding is inspired by <a href="http://www.w3.org/TR/REC-CSS2/box.html">W3 Box Model</a>
 *  
 *<pre>
 *
 *       **************************
 *       *         Margin         *
 *       *  ********************  *
 *       *  *      Padding     *  *
 *       *  *    ***********   *  *
 *       *  *    * Content *   *  *
 *       *  *    ***********   *  *
 *       *  *      Padding     *  *
 *       *  ********************  *
 *       *         Margin         *
 *       **************************
 *</pre> 
 * @author Chen Fishbein
 */
public class Style {
    /**
     * Indicates a special compatibilty mode for LWUIT allowing easy transition to
     * the new style model
     */
    private static boolean defaultStyleCompatibilityMode = true;


    /**
     * Background color attribute name for the theme hashtable 
     */
    public static final String BG_COLOR = "bgColor";

    /**
     * Foreground color attribute name for the theme hashtable 
     */
    public static final String FG_COLOR = "fgColor";

    /**
     * Background image attribute name for the theme hashtable 
     */
    public static final String BG_IMAGE = "bgImage";
    
    /**
     * Background attribute name for the theme hashtable
     */
    public static final String BACKGROUND_TYPE = "bgType";


    /**
     * Background attribute name for the theme hashtable
     */
    public static final String BACKGROUND_ALIGNMENT = "bgAlign";


    /**
     * Background attribute name for the theme hashtable
     */
    public static final String BACKGROUND_GRADIENT = "bgGradient";

    /**
     * Font attribute name for the theme hashtable 
     */
    public static final String FONT = "font";

    /**
     * Transparency attribute name for the theme hashtable 
     */
    public static final String TRANSPARENCY = "transparency";

    /**
     * Margin attribute name for the theme hashtable 
     */
    public static final String MARGIN = "margin";

    /**
     * Border attribute name for the theme hashtable 
     */
    public static final String BORDER = "border";

    /**
     * Padding attribute name for the theme hashtable 
     */
    public static final String PADDING = "padding";

    /**
     * Indicates the background for the style would use a scaled image
     */
    public static final byte BACKGROUND_IMAGE_SCALED = (byte)1;

    /**
     * Indicates the background for the style would use a tiled image on both axis
     */
    public static final byte BACKGROUND_IMAGE_TILE_BOTH = (byte)2;

    /**
     * Indicates the background for the style would use a vertical tiled image
     */
    public static final byte BACKGROUND_IMAGE_TILE_VERTICAL = (byte)3;

    /**
     * Indicates the background for the style would use a horizontal tiled image
     */
    public static final byte BACKGROUND_IMAGE_TILE_HORIZONTAL = (byte)4;

    /**
     * Indicates the background for the style would use an unscaled image with an alignment
     */
    public static final byte BACKGROUND_IMAGE_ALIGNED = (byte)5;

    /**
     * Indicates the background for the style would use a linear gradient
     */
    public static final byte BACKGROUND_GRADIENT_LINEAR_VERTICAL = (byte)6;

    /**
     * Indicates the background for the style would use a linear gradient
     */
    public static final byte BACKGROUND_GRADIENT_LINEAR_HORIZONTAL = (byte)7;

    /**
     * Indicates the background for the style would use a radial gradient
     */
    public static final byte BACKGROUND_GRADIENT_RADIAL = (byte)8;

    /**
     * Indicates the background alignment for use in tiling or aligned images
     */
    public static final byte BACKGROUND_IMAGE_ALIGN_TOP = (byte)0xa1;
    /**
     * Indicates the background alignment for use in tiling or aligned images
     */
    public static final byte BACKGROUND_IMAGE_ALIGN_BOTTOM = (byte)0xa2;
    /**
     * Indicates the background alignment for use in tiling or aligned images
     */
    public static final byte BACKGROUND_IMAGE_ALIGN_LEFT = (byte)0xa3;
    /**
     * Indicates the background alignment for use in tiling or aligned images
     */
    public static final byte BACKGROUND_IMAGE_ALIGN_RIGHT = (byte)0xa4;
    /**
     * Indicates the background alignment for use in tiling or aligned images
     */
    public static final byte BACKGROUND_IMAGE_ALIGN_CENTER = (byte)0xa5;

    private int fgColor = 0x000000;
    private int bgColor = 0xFFFFFF;
    private Font font = Font.getDefaultFont();
    private Image bgImage;
    private int[] padding = new int[4];
    private int[] margin = new int[4];
    private byte transparency = (byte) 0xFF; //no transparency
    private Painter bgPainter;

    private byte backgroundType = BACKGROUND_IMAGE_SCALED;
    private byte backgroundAlignment = BACKGROUND_IMAGE_ALIGN_TOP;
    private Object[] backgroundGradient;

    private Border border = null;

    /**
     * The modified flag indicates which portions of the style have changed using
     * bitmask values
     */
    private short modifiedFlag;
    /**
     * Used for modified flag
     */
    private static final short FG_COLOR_MODIFIED = 1;
    /**
     * Used for modified flag
     */
    private static final short BG_COLOR_MODIFIED = 2;

    /**
     * Used for modified flag
     */
    private static final short FONT_MODIFIED = 16;
    /**
     * Used for modified flag
     */
    private static final short BG_IMAGE_MODIFIED = 32;
    /**
     * Used for modified flag
     */
    //private static final short SCALE_IMAGE_MODIFIED = 64;
    /**
     * Used for modified flag
     */
    private static final short TRANSPARENCY_MODIFIED = 128;
    /**
     * Used for modified flag
     */
    private static final short PADDING_MODIFIED = 256;
    /**
     * Used for modified flag
     */
    private static final short MARGIN_MODIFIED = 512;

    /**
     * Used for modified flag
     */
    private static final short BORDER_MODIFIED = 1024;

    private static final short BACKGROUND_TYPE_MODIFIED = 2048;

    private static final short BACKGROUND_ALIGNMENT_MODIFIED = 4096;

    private static final short BACKGROUND_GRADIENT_MODIFIED = 8192;


    private EventDispatcher listeners;

    WeakReference roundRectCache;

    /**
     * Each component when it draw itself uses this Object 
     * to determine in what colors it should use.
     * When a Component is generated it construct a default Style Object.
     * The Default values for each Component can be changed by using the UIManager class
     */
    public Style() {
        setPadding(3, 3, 3, 3);
        setMargin(2, 2, 2, 2);
        modifiedFlag = 0;
    }

    /**
     * Creates a full copy of the given style. Notice that if the original style was modified 
     * manually (by invoking setters on it) it would not chnage when changing a theme/look and feel,
     * however this newly created style would change in such a case.
     * 
     * @param style the style to copy
     */
    public Style(Style style) {
        this(style.getFgColor(), style.getBgColor(), 0, 0, style.getFont(), style.getBgTransparency(),
                style.getBgImage(), true);
        setPadding(style.padding[Component.TOP],
                style.padding[Component.BOTTOM],
                style.padding[Component.LEFT],
                style.padding[Component.RIGHT]);
        setMargin(style.margin[Component.TOP],
                style.margin[Component.BOTTOM],
                style.margin[Component.LEFT],
                style.margin[Component.RIGHT]);
        setBorder(style.getBorder());
        modifiedFlag = 0;
        backgroundType = style.backgroundType;
        backgroundAlignment = style.backgroundAlignment;
        if(style.backgroundGradient != null) {
            backgroundGradient = new Object[style.backgroundGradient.length];
            System.arraycopy(style.backgroundGradient, 0, backgroundGradient, 0, backgroundGradient.length);
        }
    }

    /**
     * Creates a new style with the given attributes
     * 
     * @param fgColor foreground color
     * @param bgColor background color
     * @param fgSelectionColor foreground selection color
     * @param bgSelectionColor background selection color
     * @param f font
     * @param transparency transparency value
     */
    private Style(int fgColor, int bgColor, int fgSelectionColor, int bgSelectionColor, Font f, byte transparency) {
        this(fgColor, bgColor, fgSelectionColor, bgSelectionColor, f, transparency, null, false);
    }

    /**
     * Creates a new style with the given attributes
     *
     * @param fgColor foreground color
     * @param bgColor background color
     * @param f font
     * @param transparency transparency value
     */
    public Style(int fgColor, int bgColor, Font f, byte transparency) {
        this(fgColor, bgColor, f, transparency, null, BACKGROUND_IMAGE_SCALED);
    }

    /**
     * Creates a new style with the given attributes
     * 
     * @param fgColor foreground color
     * @param bgColor background color
     * @param fgSelectionColor foreground selection color
     * @param bgSelectionColor background selection color
     * @param f font
     * @param transparency transparency value
     * @param im background image
     * @param scaledImage whether the image should be scaled or tiled
     */
    private Style(int fgColor, int bgColor, int fgSelectionColor, int bgSelectionColor, Font f, byte transparency, Image im, boolean scaledImage) {
        this();
        this.fgColor = fgColor;
        this.bgColor = bgColor;
        this.font = f;
        this.transparency = transparency;
        this.bgImage = im;        
    }


    /**
     * Creates a new style with the given attributes
     *
     * @param fgColor foreground color
     * @param bgColor background color
     * @param f font
     * @param transparency transparency value
     * @param im background image
     * @param backgroundType one of:
     * BACKGROUND_IMAGE_SCALED, BACKGROUND_IMAGE_TILE_BOTH,
     * BACKGROUND_IMAGE_TILE_VERTICAL, BACKGROUND_IMAGE_TILE_HORIZONTAL,
     * BACKGROUND_IMAGE_ALIGNED, BACKGROUND_GRADIENT_LINEAR_HORIZONTAL,
     * BACKGROUND_GRADIENT_LINEAR_VERTICAL, BACKGROUND_GRADIENT_RADIAL 
     */
    public Style(int fgColor, int bgColor, Font f, byte transparency, Image im, byte backgroundType) {
        this();
        this.fgColor = fgColor;
        this.bgColor = bgColor;
        this.font = f;
        this.transparency = transparency;
        this.backgroundType = backgroundType;
        this.bgImage = im;
    }

    /**
     * Merges the new style with the current style without changing the elements that
     * were modified.
     * 
     * @param style new values of styles from the current theme
     */
    public void merge(Style style) {
        short tmp = modifiedFlag;

        if ((modifiedFlag & FG_COLOR_MODIFIED) == 0) {
            setFgColor(style.getFgColor());
        }
        if ((modifiedFlag & BG_COLOR_MODIFIED) == 0) {
            setBgColor(style.getBgColor());
        }
        if ((modifiedFlag & BG_IMAGE_MODIFIED) == 0) {
            setBgImage(style.getBgImage());
        }
        if ((modifiedFlag & BACKGROUND_TYPE_MODIFIED) == 0) {
            setBackgroundType(style.getBackgroundType());
        }
        if ((modifiedFlag & BACKGROUND_ALIGNMENT_MODIFIED) == 0) {
            setBackgroundAlignment(style.getBackgroundAlignment());
        }
        if ((modifiedFlag & BACKGROUND_GRADIENT_MODIFIED) == 0) {
            setBackgroundGradientStartColor(style.getBackgroundGradientStartColor());
            setBackgroundGradientEndColor(style.getBackgroundGradientEndColor());
            setBackgroundGradientRelativeX(style.getBackgroundGradientRelativeX());
            setBackgroundGradientRelativeY(style.getBackgroundGradientRelativeY());
            setBackgroundGradientRelativeSize(style.getBackgroundGradientRelativeSize());
        }
        if ((modifiedFlag & FONT_MODIFIED) == 0) {
            setFont(style.getFont());
        }

        if ((modifiedFlag & TRANSPARENCY_MODIFIED) == 0) {
            setBgTransparency(style.getBgTransparency());
        }

        if ((modifiedFlag & PADDING_MODIFIED) == 0) {
            setPadding(style.padding[Component.TOP],
                    style.padding[Component.BOTTOM],
                    style.padding[Component.LEFT],
                    style.padding[Component.RIGHT]);
        }

        if ((modifiedFlag & MARGIN_MODIFIED) == 0) {
            setMargin(style.margin[Component.TOP],
                    style.margin[Component.BOTTOM],
                    style.margin[Component.LEFT],
                    style.margin[Component.RIGHT]);
        }
        
        if ((modifiedFlag & BORDER_MODIFIED) == 0) {
            setBorder(style.getBorder());
        }

        modifiedFlag = tmp;
    }

    /**
     * Returns true if the style was modified manually after it was created by the
     * look and feel. If the style was modified manually (by one of the set methods)
     * then it should be merged rather than overwritten.
     * 
     * @return true if the style was modified
     */
    public boolean isModified() {
        return modifiedFlag != 0;
    }

    /**
     * Background color for the component
     *
     * @return the background color for the component
     */
    public int getBgColor() {
        return bgColor;
    }

    /**
     * Background image for the component
     *
     * @return the background image for the component
     */
    public Image getBgImage() {
        return bgImage;
    }

    /**
     * The type of the background defaults to BACKGROUND_IMAGE_SCALED
     * 
     * @return one of: 
     * BACKGROUND_IMAGE_SCALED, BACKGROUND_IMAGE_TILE_BOTH, 
     * BACKGROUND_IMAGE_TILE_VERTICAL, BACKGROUND_IMAGE_TILE_HORIZONTAL, 
     * BACKGROUND_IMAGE_ALIGNED, BACKGROUND_GRADIENT_LINEAR_HORIZONTAL, 
     * BACKGROUND_GRADIENT_LINEAR_VERTICAL, BACKGROUND_GRADIENT_RADIAL 
     */
    public byte getBackgroundType() {
        return backgroundType;
    }

    /**
     * Return the alignment for the image or tiled image
     *
     * @return one of:
     * BACKGROUND_IMAGE_ALIGN_TOP, BACKGROUND_IMAGE_ALIGN_BOTTOM,
     * BACKGROUND_IMAGE_ALIGN_LEFT, BACKGROUND_IMAGE_ALIGN_RIGHT,
     * BACKGROUND_IMAGE_ALIGN_CENTER
     */
    public byte getBackgroundAlignment() {
        return backgroundAlignment;
    }

    /**
     * Start color for the radial/linear gradient
     *
     * @return the start color for the radial/linear gradient
     */
    public int getBackgroundGradientStartColor() {
        if(backgroundGradient != null && backgroundGradient.length > 1) {
            return ((Integer)backgroundGradient[0]).intValue();
        }
        return 0xffffff;
    }

    /**
     * End color for the radial/linear gradient
     *
     * @return the end color for the radial/linear gradient
     */
    public int getBackgroundGradientEndColor() {
        if(backgroundGradient != null && backgroundGradient.length > 1) {
            return ((Integer)backgroundGradient[1]).intValue();
        }
        return 0;
    }

    /**
     * Background radial gradient relative center position X
     *
     * @return value between 0 and 1 with 0.5 representing the center of the component
     */
    public float getBackgroundGradientRelativeX() {
        if(backgroundGradient != null && backgroundGradient.length > 2) {
            return ((Float)backgroundGradient[2]).floatValue();
        }
        return 0.5f;
    }

    /**
     * Background radial gradient relative center position Y
     *
     * @return value between 0 and 1 with 0.5 representing the center of the component
     */
    public float getBackgroundGradientRelativeY() {
        if(backgroundGradient != null && backgroundGradient.length > 3) {
            return ((Float)backgroundGradient[3]).floatValue();
        }
        return 0.5f;
    }

    /**
     * Background radial gradient relative size
     *
     * @return value representing the relative size of the gradient
     */
    public float getBackgroundGradientRelativeSize() {
        if(backgroundGradient != null && backgroundGradient.length > 4) {
            return ((Float)backgroundGradient[4]).floatValue();
        }
        return 1f;
    }

    /**
     * Foreground color for the component
     *
     * @return the foreground color for the component
     */
    public int getFgColor() {
        return fgColor;
    }

    /**
     * Font for the component
     *
     * @return the font for the component
     */
    public Font getFont() {
        return font;
    }

    /**
     * Sets the background color for the component
     * 
     * @param bgColor RRGGBB color that ignors the alpha component
     */
    public void setBgColor(int bgColor) {
        setBgColor(bgColor, false);
    }

    /**
     * Sets the background image for the component
     * 
     * @param bgImage background image
     */
    public void setBgImage(Image bgImage) {
        setBgImage(bgImage, false);
    }

    /**
     * Sets the background type for the component
     *
     * @param backgroundType one of BACKGROUND_IMAGE_SCALED, BACKGROUND_IMAGE_TILE_BOTH,
     * BACKGROUND_IMAGE_TILE_VERTICAL, BACKGROUND_IMAGE_TILE_HORIZONTAL,
     * BACKGROUND_IMAGE_ALIGNED, BACKGROUND_GRADIENT_LINEAR_HORIZONTAL,
     * BACKGROUND_GRADIENT_LINEAR_VERTICAL, BACKGROUND_GRADIENT_RADIAL
     */
    public void setBackgroundType(byte backgroundType) {
        setBackgroundType(backgroundType, false);
    }


    /**
     * Sets the background alignment for the component
     *
     * @param backgroundAlignment one of:
     * BACKGROUND_IMAGE_ALIGN_TOP, BACKGROUND_IMAGE_ALIGN_BOTTOM,
     * BACKGROUND_IMAGE_ALIGN_LEFT, BACKGROUND_IMAGE_ALIGN_RIGHT,
     * BACKGROUND_IMAGE_ALIGN_CENTER
     */
    public void setBackgroundAlignment(byte backgroundAlignment) {
        setBackgroundAlignment(backgroundAlignment, false);
    }


    /**
     * Sets the background color for the component
     *
     * @param backgroundGradientStartColor start color for the linear/radial gradient
     */
    public void setBackgroundGradientStartColor(int backgroundGradientStartColor) {
        setBackgroundGradientStartColor(backgroundGradientStartColor, false);
    }

    /**
     * Sets the background color for the component
     *
     * @param backgroundGradientEndColor end color for the linear/radial gradient
     */
    public void setBackgroundGradientEndColor(int backgroundGradientEndColor) {
        setBackgroundGradientEndColor(backgroundGradientEndColor, false);
    }


    /**
     * Background radial gradient relative center position X
     *
     * @param backgroundGradientRelativeX x position of the radial gradient center
     */
    public void setBackgroundGradientRelativeX(float backgroundGradientRelativeX) {
        setBackgroundGradientRelativeX(backgroundGradientRelativeX, false);
    }

    /**
     * Background radial gradient relative center position Y
     *
     * @param backgroundGradientRelativeY y position of the radial gradient center
     */
    public void setBackgroundGradientRelativeY(float backgroundGradientRelativeY) {
        setBackgroundGradientRelativeY(backgroundGradientRelativeY, false);
    }

    /**
     * Background radial gradient relative size
     *
     * @param backgroundGradientRelativeSize the size of the radial gradient
     */
    public void setBackgroundGradientRelativeSize(float backgroundGradientRelativeSize) {
        setBackgroundGradientRelativeSize(backgroundGradientRelativeSize, false);
    }

    /**
     * Sets the foreground color for the component
     * 
     * @param fgColor foreground color
     */
    public void setFgColor(int fgColor) {
        setFgColor(fgColor, false);
    }

    /**
     * Sets the font for the component
     * 
     * @param font the font
     */
    public void setFont(Font font) {
        setFont(font, false);
    }


    private Style getSelectedStyle() {
        if(listeners != null) {
            Vector l = listeners.getListenerVector();
            int size = l.size();
            if(size > 0) {
                Object o = l.elementAt(0);
                if(o instanceof Component) {
                    return ((Component)o).getSelectedStyle();
                }
            }
        }
        return null;
    }

    /**
     * Returns the transparency (opacity) level of the Component, zero indicates fully
     * transparent and FF indicates fully opaque. 
     * 
     * @return  the transparency level of the Component
     */
    public byte getBgTransparency() {
        if(bgImage != null && (bgImage.isAnimation() || bgImage.isOpaque())) {
            return (byte)0xff;
        }
        return transparency;
    }

    /**
     * Sets the Component transparency (opacity) level of the Component, zero indicates fully
     * transparent and FF indicates fully opaque. 
     * 
     * @param transparency transparency level as byte
     */
    public void setBgTransparency(byte transparency) {
        setBgTransparency(transparency & 0xFF, false);
    }

    /**
     * Sets the Component transparency level. Valid values should be a 
     * number between 0-255
     * @param transparency int value between 0-255
     */
    public void setBgTransparency(int transparency) {
        setBgTransparency(transparency, false);
    }

    /**
     * Sets the Style Padding
     *  
     * @param top number of pixels to padd
     * @param bottom number of pixels to padd
     * @param left number of pixels to padd
     * @param right number of pixels to padd
     */
    public void setPadding(int top, int bottom, int left, int right) {
        if (top < 0 || left < 0 || right < 0 || bottom < 0) {
            throw new IllegalArgumentException("padding cannot be negative");
        }
        if (padding[Component.TOP] != top ||
                padding[Component.BOTTOM] != bottom ||
                padding[Component.LEFT] != left ||
                padding[Component.RIGHT] != right) {
            padding[Component.TOP] = top;
            padding[Component.BOTTOM] = bottom;
            padding[Component.LEFT] = left;
            padding[Component.RIGHT] = right;

            modifiedFlag |= PADDING_MODIFIED;
            firePropertyChanged(PADDING);
        }
    }

    /**
     * Sets the Style Padding
     * 
     * @param orientation one of: Component.TOP, Component.BOTTOM, Component.LEFT, Component.RIGHT
     * @param gap number of pixels to padd
     */
    public void setPadding(int orientation, int gap) {
        setPadding(orientation, gap, false);
    }

    /**
     * Sets the Style Margin
     *  
     * @param top number of margin pixels
     * @param bottom number of margin pixels
     * @param left number of margin pixels
     * @param right number of margin pixels
     */
    public void setMargin(int top, int bottom, int left, int right) {
        if (top < 0 || left < 0 || right < 0 || bottom < 0) {
            throw new IllegalArgumentException("margin cannot be negative");
        }
        if (margin[Component.TOP] != top ||
                margin[Component.BOTTOM] != bottom ||
                margin[Component.LEFT] != left ||
                margin[Component.RIGHT] != right) {
            margin[Component.TOP] = top;
            margin[Component.BOTTOM] = bottom;
            margin[Component.LEFT] = left;
            margin[Component.RIGHT] = right;

            modifiedFlag |= MARGIN_MODIFIED;
            firePropertyChanged(MARGIN);
        }
    }

    /**
     * Sets the Style Margin
     * 
     * @param orientation one of: Component.TOP, Component.BOTTOM, Component.LEFT, Component.RIGHT
     * @param gap number of margin pixels
     */
    public void setMargin(int orientation, int gap) {
        setMargin(orientation, gap, false);
    }

    /**
     * Returns the Padding
     *
     * @param rtl flag indicating whether the padding is for an RTL bidi component
     * @param orientation one of: Component.TOP, Component.BOTTOM, Component.LEFT, Component.RIGHT
     * @return number of padding pixels in the givven orientation
     */
    public int getPadding(boolean rtl, int orientation) {
        if (orientation < Component.TOP || orientation > Component.RIGHT) {
            throw new IllegalArgumentException("wrong orientation " + orientation);
        }

        if (rtl) {
        	switch(orientation) {
                case Component.LEFT:
                    orientation = Component.RIGHT;
                    break;
                case Component.RIGHT:
                    orientation = Component.LEFT;
                    break;
            }
        }

        return padding[orientation];
    }

    /**
     * Returns the Padding
     *
     * @param orientation one of: Component.TOP, Component.BOTTOM, Component.LEFT, Component.RIGHT
     * @return number of padding pixels in the givven orientation
     */
    public int getPadding(int orientation) {
        return getPadding(UIManager.getInstance().getLookAndFeel().isRTL(), orientation);
    }

    /**
     * Returns the Margin
     *
     * @param orientation one of: Component.TOP, Component.BOTTOM, Component.LEFT, Component.RIGHT
     * @return number of margin pixels in the givven orientation
     */
    public int getMargin(int orientation) {
        return getMargin(UIManager.getInstance().getLookAndFeel().isRTL(), orientation);
    }


    /**
     * Returns the Margin
     * 
     * @param rtl flag indicating whether the padding is for an RTL bidi component
     * @param orientation one of: Component.TOP, Component.BOTTOM, Component.LEFT, Component.RIGHT
     * @return number of margin pixels in the givven orientation
     */
    public int getMargin(boolean rtl, int orientation) {
        if (orientation < Component.TOP || orientation > Component.RIGHT) {
            throw new IllegalArgumentException("wrong orientation " + orientation);
        }
        if (rtl) {
        	switch(orientation) {
                case Component.LEFT:
                    orientation = Component.RIGHT;
                    break;
                case Component.RIGHT:
                    orientation = Component.LEFT;
                    break;
            }
        }
        return margin[orientation];
    }

    /**
     * Sets the background color for the component
     * 
     * @param bgColor RRGGBB color that ignors the alpha component
     * @param override If set to true allows the look and feel/theme to override 
     * the value in this attribute when changing a theme/look and feel
     */
    public void setBgColor(int bgColor, boolean override) {
        if (this.bgColor != bgColor) {
            this.bgColor = bgColor;
            if (!override) {
                modifiedFlag |= BG_COLOR_MODIFIED;
            }
            firePropertyChanged(BG_COLOR);
        }
    }

    /**
     * Sets the background image for the component
     * 
     * @param bgImage background image
     * @param override If set to true allows the look and feel/theme to override 
     * the value in this attribute when changing a theme/look and feel
     */
    public void setBgImage(Image bgImage, boolean override) {
        if (this.bgImage != bgImage) {
            this.bgImage = bgImage;
            if(!override){
                modifiedFlag |= BG_IMAGE_MODIFIED;
            }
            firePropertyChanged(BG_IMAGE);
        }
    }

    /**
     * Sets the background type for the component
     *
     * @param backgroundType one of BACKGROUND_IMAGE_SCALED, BACKGROUND_IMAGE_TILE_BOTH,
     * BACKGROUND_IMAGE_TILE_VERTICAL, BACKGROUND_IMAGE_TILE_HORIZONTAL,
     * BACKGROUND_IMAGE_ALIGNED, BACKGROUND_GRADIENT_LINEAR_HORIZONTAL,
     * BACKGROUND_GRADIENT_LINEAR_VERTICAL, BACKGROUND_GRADIENT_RADIAL
     * @param override If set to true allows the look and feel/theme to override
     * the value in this attribute when changing a theme/look and feel
     */
    public void setBackgroundType(byte backgroundType, boolean override) {
        if (this.backgroundType != backgroundType) {
            this.backgroundType = backgroundType;
            if(!override){
                modifiedFlag |= BACKGROUND_TYPE_MODIFIED;
            }
            firePropertyChanged(BACKGROUND_TYPE);
        }
    }


    /**
     * Sets the background alignment for the component
     *
     * @param backgroundAlignment one of:
     * BACKGROUND_IMAGE_ALIGN_TOP, BACKGROUND_IMAGE_ALIGN_BOTTOM,
     * BACKGROUND_IMAGE_ALIGN_LEFT, BACKGROUND_IMAGE_ALIGN_RIGHT,
     * BACKGROUND_IMAGE_ALIGN_CENTER
     * @param override If set to true allows the look and feel/theme to override
     * the value in this attribute when changing a theme/look and feel
     */
    public void setBackgroundAlignment(byte backgroundAlignment, boolean override) {
        if (this.backgroundAlignment != backgroundAlignment) {
            this.backgroundAlignment = backgroundAlignment;
            if(!override){
                modifiedFlag |= BACKGROUND_ALIGNMENT_MODIFIED;
            }
            firePropertyChanged(BACKGROUND_ALIGNMENT);
        }
    }

    /**
     * Returns the background gradient array which includes the start/end color  
     * and optionally the x/y relative anchor for the radial gradient
     * 
     * @return the background gradient array which includes the start/end color  
     * and optionally the x/y relative anchor for the radial gradient
     */
    Object[] getBackgroundGradient() {
        if(backgroundGradient == null) {
            Float c = new Float(0.5f);
            backgroundGradient = new Object[] {new Integer(0xffffff), new Integer(0), c, c, new Float(1)};
        }
        return backgroundGradient;
    }

    /**
     * Internal use background gradient setter
     */
    void setBackgroundGradient(Object[] backgroundGradient) {
        this.backgroundGradient = backgroundGradient;
    }

    /**
     * Sets the background color for the component
     *
     * @param backgroundGradientStartColor start color for the linear/radial gradient
     * @param override If set to true allows the look and feel/theme to override
     * the value in this attribute when changing a theme/look and feel
     */
    public void setBackgroundGradientStartColor(int backgroundGradientStartColor, boolean override) {
        if (((Integer) getBackgroundGradient()[0]).intValue() != backgroundGradientStartColor) {
            getBackgroundGradient()[0] = new Integer(backgroundGradientStartColor);
            if (!override) {
                modifiedFlag |= BACKGROUND_GRADIENT_MODIFIED;
            }
            firePropertyChanged(BACKGROUND_GRADIENT);
        }
    }

    /**
     * Sets the background color for the component
     *
     * @param backgroundGradientEndColor end color for the linear/radial gradient
     * @param override If set to true allows the look and feel/theme to override
     * the value in this attribute when changing a theme/look and feel
     */
    public void setBackgroundGradientEndColor(int backgroundGradientEndColor, boolean override) {
        if (((Integer) getBackgroundGradient()[1]).intValue() != backgroundGradientEndColor) {
            getBackgroundGradient()[1] = new Integer(backgroundGradientEndColor);
            if (!override) {
                modifiedFlag |= BACKGROUND_GRADIENT_MODIFIED;
            }
            firePropertyChanged(BACKGROUND_GRADIENT);
        }
    }


    /**
     * Background radial gradient relative center position X
     *
     * @param backgroundGradientRelativeX x position of the radial gradient center
     * @param override If set to true allows the look and feel/theme to override
     * the value in this attribute when changing a theme/look and feel
     */
    public void setBackgroundGradientRelativeX(float backgroundGradientRelativeX, boolean override) {
        if (((Float) getBackgroundGradient()[2]).floatValue() != backgroundGradientRelativeX) {
            getBackgroundGradient()[2] = new Float(backgroundGradientRelativeX);
            if (!override) {
                modifiedFlag |= BACKGROUND_GRADIENT_MODIFIED;
            }
            firePropertyChanged(BACKGROUND_GRADIENT);
        }
    }

    /**
     * Background radial gradient relative center position Y
     *
     * @param backgroundGradientRelativeY y position of the radial gradient center
     * @param override If set to true allows the look and feel/theme to override
     * the value in this attribute when changing a theme/look and feel
     */
    public void setBackgroundGradientRelativeY(float backgroundGradientRelativeY, boolean override) {
        if (((Float) getBackgroundGradient()[3]).floatValue() != backgroundGradientRelativeY) {
            getBackgroundGradient()[3] = new Float(backgroundGradientRelativeY);
            if (!override) {
                modifiedFlag |= BACKGROUND_GRADIENT_MODIFIED;
            }
            firePropertyChanged(BACKGROUND_GRADIENT);
        }
    }

    /**
     * Background radial gradient relative size
     *
     * @param backgroundGradientRelativeSize the size of the radial gradient relative to the screens
     * larger dimension
     * @param override If set to true allows the look and feel/theme to override
     * the value in this attribute when changing a theme/look and feel
     */
    public void setBackgroundGradientRelativeSize(float backgroundGradientRelativeSize, boolean override) {
        if (((Float) getBackgroundGradient()[4]).floatValue() != backgroundGradientRelativeSize) {
            getBackgroundGradient()[4] = new Float(backgroundGradientRelativeSize);
            if (!override) {
                modifiedFlag |= BACKGROUND_GRADIENT_MODIFIED;
            }
            firePropertyChanged(BACKGROUND_GRADIENT);
        }
    }

    /**
     * Sets the foreground color for the component
     * 
     * @param fgColor foreground color
     * @param override If set to true allows the look and feel/theme to override 
     * the value in this attribute when changing a theme/look and feel
     */
    public void setFgColor(int fgColor, boolean override) {
        if (this.fgColor != fgColor) {
            this.fgColor = fgColor;
            if (!override) {
                modifiedFlag |= FG_COLOR_MODIFIED;
            }
            firePropertyChanged(FG_COLOR);
        }
    }

    /**
     * Sets the font for the component
     * 
     * @param font the font
     * @param override If set to true allows the look and feel/theme to override 
     * the value in this attribute when changing a theme/look and feel
     */
    public void setFont(Font font, boolean override) {
        if (this.font == null && font != null ||
                (this.font != null && !this.font.equals(font))) {
            this.font = font;
            if (!override) {
                modifiedFlag |= FONT_MODIFIED;
            }
            firePropertyChanged(FONT);
        }
    }


    /**
     * Sets the Component transparency level. Valid values should be a 
     * number between 0-255
     * 
     * @param transparency int value between 0-255
     * @param override If set to true allows the look and feel/theme to override 
     * the value in this attribute when changing a theme/look and feel
     */
    public void setBgTransparency(int transparency, boolean override) {
        if (transparency < 0 || transparency > 255) {
            throw new IllegalArgumentException("valid values are between 0-255");
        }
        if (this.transparency != (byte) transparency) {
            this.transparency = (byte) transparency;

            if (!override) {
                modifiedFlag |= TRANSPARENCY_MODIFIED;
            }
            firePropertyChanged(TRANSPARENCY);
        }
    }


    /**
     * Sets the Style Padding
     * 
     * @param orientation one of: Component.TOP, Component.BOTTOM, Component.LEFT, Component.RIGHT
     * @param gap number of pixels to padd
     * @param override If set to true allows the look and feel/theme to override 
     * the value in this attribute when changing a theme/look and feel
     */
    public void setPadding(int orientation, int gap,boolean override) {
        if (orientation < Component.TOP || orientation > Component.RIGHT) {
            throw new IllegalArgumentException("wrong orientation " + orientation);
        }
        if (gap < 0) {
            throw new IllegalArgumentException("padding cannot be negative");
        }
        if (padding[orientation] != gap) {
            padding[orientation] = gap;

            if (!override) {
                modifiedFlag |= PADDING_MODIFIED;
            }
            firePropertyChanged(PADDING);
        }
    }


    /**
     * Sets the Style Margin
     * 
     * @param orientation one of: Component.TOP, Component.BOTTOM, Component.LEFT, Component.RIGHT
     * @param gap number of margin pixels
     * @param override If set to true allows the look and feel/theme to override 
     * the value in this attribute when changing a theme/look and feel
     */
    public void setMargin(int orientation, int gap, boolean override) {
        if (orientation < Component.TOP || orientation > Component.RIGHT) {
            throw new IllegalArgumentException("wrong orientation " + orientation);
        }
        if (gap < 0) {
            throw new IllegalArgumentException("margin cannot be negative");
        }
        if (margin[orientation] != gap) {
            margin[orientation] = gap;
            if (!override) {
                modifiedFlag |= MARGIN_MODIFIED;
            }
            firePropertyChanged(MARGIN);
        }
    }

    
    private void firePropertyChanged(String propertName) {
        roundRectCache = null;
        if (listeners == null) {
            return;
        }
        listeners.fireStyleChangeEvent(propertName, this);
    }

    /**
     * Adds a Style Listener to the Style Object.
     * 
     * @param l a style listener
     */
    public void addStyleListener(StyleListener l) {
        if (listeners == null) {
            listeners = new EventDispatcher();
        }
        listeners.addListener(l);
    }

    /**
     * Removes a Style Listener from the Style Object.
     * 
     * @param l a style listener
     */
    public void removeStyleListener(StyleListener l) {
        if (listeners != null) {
            listeners.removeListener(l);
        }
    }

    void resetModifiedFlag() {
        modifiedFlag = 0;
    }

    /**
     * Sets the border for the style
     * 
     * @param border new border object for the component
     */
    public void setBorder(Border border) {
        setBorder(border, false);
    }

    /**
     * Sets the border for the style
     * 
     * @param border new border object for the component
     * @param override If set to true allows the look and feel/theme to override 
     * the value in this attribute when changing a theme/look and feel
     */
    public void setBorder(Border border, boolean override) {
        if ((this.border == null && border != null) ||
                (this.border != null && !this.border.equals(border))) {
            this.border = border;
            if (!override) {
                modifiedFlag |= BORDER_MODIFIED;
            }
            firePropertyChanged(BORDER);
        }
    }
    
    /**
     * Returns the border for the style
     * 
     * @return the border
     */
    public Border getBorder() {
        return border;
    }
    
    /**
     * Return the background painter for this style, normally this would be 
     * the internal image/color painter but can be user defined 
     * 
     * @return the background painter
     */
    public Painter getBgPainter() {
        return bgPainter;
    }

    /**
     * Defines the background painter for this style, normally this would be 
     * the internal image/color painter but can be user defined 
     * 
     * @param bgPainter new painter to install into the style
     */
    public void setBgPainter(Painter bgPainter) {
        this.bgPainter = bgPainter;
    }
}
