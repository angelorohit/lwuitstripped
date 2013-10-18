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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;

/**
 * An image that only keeps the binary data of the source file used to load it
 * in permanent memory. This allows the bitmap to get collected while the binary
 * data remains, a weak reference is used for caching.
 *
 * @author Shai Almog
 */
public class EncodedImage extends Image {
    private byte[] imageData;
    private int width = -1;
    private int height = -1;
    private boolean opaqueChecked = false;
    private boolean opaque = false;
    private WeakReference cache;
    
    private EncodedImage(byte[] imageData) {
        super(null);
        this.imageData = imageData;
    }

    /**
     * Returns the byte array data backing the image allowing the image to be stored
     * and discarded completely from RAM.
     * 
     * @return byte array used to create the image, e.g. encoded PNG, JPEG etc.
     */
    public byte[] getImageData() {
        return imageData;
    }

    /**
     * Creates an image from the given byte array
     * 
     * @param data the data of the image
     * @return newly created encoded image
     */
    public static EncodedImage create(byte[] data) {
        if(data == null) {
            throw new NullPointerException();
        }
        return new EncodedImage(data);
    }

    /**
     * Creates an image from the input stream 
     * 
     * @param i the input stream
     * @return newly created encoded image
     * @throws java.io.IOException if thrown by the input stream
     */
    public static EncodedImage create(InputStream i) throws IOException {
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int size = i.read(buffer);
        while(size > -1) {
            bo.write(buffer, 0, size);
            size = i.read(buffer);
        }
        bo.close();
        return new EncodedImage(bo.toByteArray());
    }
    
    private Image getInternal() {
        if(cache != null) {
            Image i = (Image)cache.get();
            if(i != null) {
                return i;
            }
        }
        Image i = Image.createImage(imageData, 0, imageData.length);
        cache = new WeakReference(i);
        return i;
    }

    /**
     * Creates an image from the input stream 
     * 
     * @param i the resource
     * @return newly created encoded image
     * @throws java.io.IOException if thrown by the input stream
     */
    public static EncodedImage create(String i) throws IOException {
        return create(Display.getInstance().getResourceAsStream(EncodedImage.class, i));
    }

    /**
     * @inheritDoc
     */
    public Image subImage(int x, int y, int width, int height, boolean processAlpha)  {
        return getInternal().subImage(x, y, width, height, processAlpha);
    }

    /**
     * @inheritDoc
     */
    public Image rotate(int degrees) {
        return getInternal().rotate(degrees);
    }
    
    /**
     * @inheritDoc
     */
    public Image modifyAlpha(byte alpha) {
        return getInternal().modifyAlpha(alpha);
    }
    
    /**
     * @inheritDoc
     */
    public Image modifyAlpha(byte alpha, int removeColor) {
        return getInternal().modifyAlpha(alpha, removeColor);
    }

    /**
     * @inheritDoc
     */
    public Graphics getGraphics() {        
        return null;
    }

    /**
     * @inheritDoc
     */
    public int getWidth() {
        if(width > -1) {
            return width;
        }
        width = getInternal().getWidth();
        return width;
    }

    /**
     * @inheritDoc
     */
    public int getHeight() {
        if(height > -1) {
            return height;
        }
        height = getInternal().getHeight();
        return height;
    }

    /**
     * @inheritDoc
     */
    protected void drawImage(Graphics g, Object nativeGraphics, int x, int y) {
        getInternal().drawImage(g, nativeGraphics, x, y);
    }

    /**
     * @inheritDoc
     */
    void getRGB(int[] rgbData,
            int offset,
            int x,
            int y,
            int width,
            int height) {
        getInternal().getRGB(rgbData, offset, x, y, width, height);
    }

    /**
     * @inheritDoc
     */
    public void toRGB(RGBImage image,
            int destX,
            int destY,
            int x,
            int y,
            int width,
            int height) {
        getInternal().toRGB(image, destX, destY, x, y, width, height);
    }

    /**
     * @inheritDoc
     */
    public Image scaledWidth(int width) {
        return getInternal().scaledWidth(width);
    }

    /**
     * @inheritDoc
     */
    public Image scaledHeight(int height) {
        return getInternal().scaledHeight(height);
    }

    /**
     * @inheritDoc
     */
    public Image scaledSmallerRatio(int width, int height) {
        return getInternal().scaledSmallerRatio(width, height);
    }

    /**
     * @inheritDoc
     */
    public Image scaled(int width, int height) {
        return getInternal().scaled(width, height);
    }

    /**
     * @inheritDoc
     */
    public void scale(int width, int height) {
        getInternal().scale(width, height);
    }

    /**
     * @inheritDoc
     */
    public boolean isAnimation() {
        return false;
    }

    /**
     * @inheritDoc
     */
    public boolean isOpaque() {
        if(opaqueChecked) {
            return opaque;
        }
        opaque = getInternal().isOpaque();
        return opaque;
    }
}
