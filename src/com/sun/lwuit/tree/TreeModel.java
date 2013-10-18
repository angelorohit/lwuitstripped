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
package com.sun.lwuit.tree;

import java.util.Vector;

/**
 * Arranges tree node objects, a node can essentially be anything
 *
 * @author Shai Almog
 */
public interface TreeModel {
    /**
     * Returns the child objects representing the given parent, null should return
     * the root objects
     *
     * @param parent the parent object whose children should be returned, null would return the
     * tree roots
     * @return the children of the given node within the tree
     */
    public Vector getChildren(Object parent);

    /**
     * Is the node a leaf or a folder
     *
     * @param node a node within the tree
     * @return true if the node is a leaf that can't be expanded
     */
    public boolean isLeaf(Object node);
}
