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
package com.sun.lwuit.animations;

import com.sun.lwuit.Graphics;

/**
 * Allows any object to react to events and draw an
 * animation at a fixed interval. All animation methods are executed on the EDT.
 * For simplicities sake all components are animatable, however no animation will
 * appear unless it is explicitly registered into the parent form. In order to 
 * stop animation callbacks the animation must be explicitly removed from the form
 * (notice that this differs from removing the component from the form!).
 * 
 * @author Shai Almog
 */
public interface Animation {
    /**
     * Allows the animation to reduce "repaint" calls when it returns false. It is
     * called once for every frame. Frames are defined by the {@link com.sun.lwuit.Display} class.
     * 
     * @return true if a repaint is desired or false if no repaint is necessary
     */
    public boolean animate();

    /**
     * Draws the animation, within a component the standard paint method would be
     * invoked since it bares the exact same signature.
     * 
     * @param g graphics context
     */
    public void paint(Graphics g);
    
}
