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

import com.sun.lwuit.Image;
import com.sun.lwuit.RGBImage;

/**
 * Static utility class useful for simple visual effects that don't quite fit 
 * anywhere else in the core API.
 *
 * @author Shai Almog
 */
public class Effects {
    private Effects() {}

    /**
     * Takes the given image and appends an effect of reflection bellow it that
     * is similar to the way elements appear in water beneath them. This method
     * shouldn't be used when numAlpha is very low.
     * 
     * @param source image to add the reflection effect to
     * @return new image with a reflection effect for the source image
     */
    public static Image reflectionImage(Image source) {
        return reflectionImage(source, 0.5f, 120);
    }
    
    /**
     * Takes the given image and appends an effect of reflection bellow it that
     * is similar to the way elements appear in water beneath them. This method
     * shouldn't be used when numAlpha is very low.
     * 
     * @param source image to add the reflection effect to
     * @param mirrorRatio generally less than 1, a mirror ration of 0.5f will create a mirror image half the
     * height of the image, 0.75f will create a 3 quarter height mirror etc.
     * @param alphaRatio starting point for the alpha value in the mirror, this should be a number between 0 - 255
     * (recommended larger than 0) indicating the opacity of the closest pixel. For a mirror thats completely
     * opaque use 255. A recommended value would be between 128 to 90.
     * @return new image with a reflection effect for the source image
     */
    public static Image reflectionImage(Image source, float mirrorRatio, int alphaRatio) {
        return reflectionImage(source, mirrorRatio, alphaRatio, 0);
    }

    /**
     * Takes the given image and appends an effect of reflection bellow it that
     * is similar to the way elements appear in water beneath them. This method
     * shouldn't be used when numAlpha is very low.
     *
     * @param source image to add the reflection effect to
     * @param mirrorRatio generally less than 1, a mirror ration of 0.5f will create a mirror image half the
     * height of the image, 0.75f will create a 3 quarter height mirror etc.
     * @param alphaRatio starting point for the alpha value in the mirror, this should be a number between 0 - 255
     * (recommended larger than 0) indicating the opacity of the closest pixel. For a mirror thats completely
     * opaque use 255. A recommended value would be between 128 to 90.
     * @param spacing the distance in pixels between the image and its reflection
     * @return new image with a reflection effect for the source image
     */
    public static Image reflectionImage(Image source, float mirrorRatio, int alphaRatio, int spacing) {
        int w = source.getWidth();
        int h = source.getHeight();
        int mirrorHeight = ((int)(h  * mirrorRatio)) * w;

        // create an array big enough to hold the mirror data
        RGBImage rgbImg = new RGBImage(new int[w * (h + spacing) + mirrorHeight], w, h + ((int)(h  * mirrorRatio) + spacing));
        source.toRGB(rgbImg, 0, 0, 0, 0, w, h);
        int[] imageData = rgbImg.getRGB();

        for(int iter = 0 ; iter < mirrorHeight ; iter++) {
            int sourcePos = w * h - iter - 1;
            int off = iter % w;
            off = w - off + iter - off;
            int mirrorPos = imageData.length - (mirrorHeight - off) + (spacing * w);
            int color = imageData[sourcePos];

            // if the color is not transparent
            if((color & 0xff000000) != 0 && mirrorPos < imageData.length) {
                int alpha = (int)(alphaRatio * ((float)mirrorHeight - iter) / ((float)mirrorHeight));
                imageData[mirrorPos] = (imageData[sourcePos] & 0xffffff) | ((alpha << 24) & 0xff000000);
            }
        }
        return rgbImg;
    }
}
