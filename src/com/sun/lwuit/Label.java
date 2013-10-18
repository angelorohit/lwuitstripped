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

import com.sun.lwuit.animations.Animation;
import com.sun.lwuit.geom.*;
import com.sun.lwuit.plaf.DefaultLookAndFeel;
import com.sun.lwuit.plaf.LookAndFeel;
import com.sun.lwuit.plaf.Style;
import com.sun.lwuit.plaf.UIManager;
import java.util.Hashtable;

/**
 * Allows displaying labels and images with different alignment options, this class
 * is a base class for several components allowing them to declare alignement/icon
 * look in a similar way.
 * 
 * @author Chen Fishbein
 */
public class Label extends Component {
    
    private String text = "";
    
    private Image icon;
    
    private int align = LEFT;
    private int valign = BOTTOM;

    private int textPosition = RIGHT;
    
    private int gap = 2;
    
    private int shiftText = 0;
    
    private boolean tickerRunning = false;
    
    private boolean tickerEnabled = true;
    
    private long tickerStartTime;
    
    private long tickerDelay;
    
    private boolean rightToLeft;
    
    private boolean endsWith3Points = true;
    
    /** 
     * Constructs a new label with the specified string of text, left justified.
     * 
     * @param text the string that the label presents.
     */
    public Label(String text) {
        setUIID("Label");
        this.text = text;
        localize();
        setFocusable(false);
    }

    Label(String text, String uiid) {
        this.text = text;
        localize();
        setFocusable(false);
        setAlignment(CENTER);
        setUIID(uiid);
    }
    
    /**
     * Construct an empty label
     */
    public Label() {
        this("");
    }

    /** 
     * Constructs a new label with the specified icon
     * 
     * @param icon the image that the label presents.
     */
    public Label(Image icon) {
        this("");
        this.icon = icon;
    }

    /**
     * @inheritDoc
     */
    public int getBaselineResizeBehavior() {
        switch(valign) {
        case TOP:
            return BRB_CONSTANT_ASCENT;
        case BOTTOM:
            return BRB_CONSTANT_DESCENT;
        case CENTER:
            return BRB_CENTER_OFFSET;
        }
        return BRB_OTHER;
    }

    /**
     * Sets the Label text
     * 
     * @param text the string that the label presents.
     */
    public void setText(String text){
        this.text = text;
        localize();
        setShouldCalcPreferredSize(true);
        repaint();
    }
    
    private void localize() {
        Hashtable t =  UIManager.getInstance().getResourceBundle();
        if(t != null && text != null) {
            Object o = t.get(text);
            if(o != null) {
                this.text = (String)o;
            }
        } 
    }
    
    /**
     * @inheritDoc
     */
    void initComponentImpl() {
        super.initComponentImpl();
        if(hasFocus()) {
            LookAndFeel lf = UIManager.getInstance().getLookAndFeel();
            if(lf instanceof DefaultLookAndFeel) {
                ((DefaultLookAndFeel)lf).focusGained(this);
            }
        }
    }
    
    /**
     * Returns the label text
     * 
     * @return the label text
     */
    public String getText(){
        return text;
    }
    
    /**
     * Sets the Label icon, if the icon is unmodified a repaint would not be triggered
     * 
     * @param icon the image that the label presents.
     */
    public void setIcon(Image icon){
        if(this.icon == icon) {
            return;
        }
        this.icon = icon;
        setShouldCalcPreferredSize(true);
        checkAnimation();
        repaint();
    }
    
    void checkAnimation() {
        super.checkAnimation();
        if(icon != null && icon.isAnimation()) {
            Form parent = getComponentForm();
            if(parent != null) {
                // animations are always running so the internal animation isn't
                // good enough. We never want to stop this sort of animation
                parent.registerAnimated(this);
            }
        }
    }
    
    /**
     * Returns the labels icon
     * 
     * @return the labels icon
     */
    public Image getIcon(){
        return icon;
    }
    
    /**
     * Sets the Alignment of the Label to one of: CENTER, LEFT, RIGHT
     * 
     * @param align alignment value
     * @see #CENTER
     * @see #LEFT
     * @see #RIGHT
     */
    public void setAlignment(int align){
        if(align != CENTER && align != RIGHT && align != LEFT){
            throw new IllegalArgumentException("Alignment can't be set to " + align);
        }
        this.align = align;
    }
    
    /**
     * Sets the vertical alignment of the Label to one of: CENTER, TOP, BOTTOM
     * 
     * @param valign alignment value
     * @see #CENTER
     * @see #TOP
     * @see #BOTTOM
     */
    public void setVerticalAlignment(int valign) {
        if(valign != CENTER && valign != TOP && valign != BOTTOM){
            throw new IllegalArgumentException("Alignment can't be set to " + valign);
        }
        this.valign = valign;
    }

    /**
     * Returns the vertical alignment of the Label, this will only work when the icon
     * is in the side of the text and not above or bellow it.
     * 
     * @return the vertical alignment of the Label one of: CENTER, TOP, BOTTOM
     * @see #CENTER
     * @see #TOP
     * @see #BOTTOM
     */
    public int getVerticalAlignment(){
        return valign;
    }
    
    /**
     * Returns the alignment of the Label
     * 
     * @return the alignment of the Label one of: CENTER, LEFT, RIGHT
     * @see #CENTER
     * @see #LEFT
     * @see #RIGHT
     */
    public int getAlignment(){
        return align;
    }

    /**
     * Sets the position of the text relative to the icon if exists
     *
     * @param textPosition alignment value (LEFT, RIGHT, BOTTOM or TOP)
     * @see #LEFT
     * @see #RIGHT
     * @see #BOTTOM
     * @see #TOP
     */
    public void setTextPosition(int textPosition) {
        if (textPosition != LEFT && textPosition != RIGHT && textPosition != BOTTOM && textPosition != TOP) {
            throw new IllegalArgumentException("Text position can't be set to " + textPosition);
        }
        this.textPosition = textPosition;
    }

    
    /**
     * Returns The position of the text relative to the icon
     * 
     * @return The position of the text relative to the icon, one of: LEFT, RIGHT, BOTTOM, TOP
     * @see #LEFT
     * @see #RIGHT
     * @see #BOTTOM
     * @see #TOP
     */
    public int getTextPosition(){
        return textPosition;
    }
    
    /**
     * Set the gap in pixels between the icon/text to the Label boundaries
     * 
     * @param gap the gap in pixels
     */
    public void setGap(int gap) {
        this.gap = gap;
    }
    
    /**
     * Returns the gap in pixels between the icon/text to the Label boundaries
     * 
     * @return the gap in pixels between the icon/text to the Label boundaries
     */
    public int getGap() {
        return gap;
    }
    
    /**
     * @inheritDoc
     */
    protected String paramString() {
        return super.paramString() + ", text = " +getText() + ", gap = " + gap;
    }
    
    /**
     * @inheritDoc
     */
    public void paint(Graphics g) {
        UIManager.getInstance().getLookAndFeel().drawLabel(g, this);
    }

    /**
     * @inheritDoc
     */
    protected Dimension calcPreferredSize(){
        return UIManager.getInstance().getLookAndFeel().getLabelPreferredSize(this);
    }
    
    /**
     * Simple getter to return how many pixels to shift the text inside the Label

     * @return number of pixels to shift
     */
    public int getShiftText() {
        return shiftText;
    }

    /**
     * This method shifts the text from it's position in pixels.
     * The value can be positive/negative to move the text to the right/left
     * 
     * @param shiftText The number of pixels to move the text
     */
    public void setShiftText(int shiftText) {
        this.shiftText = shiftText;
    }
    
    /**
     * Returns true if a ticker should be started since there is no room to show
     * the text in the label.
     * 
     * @return true if a ticker should start running
     */
    public boolean shouldTickerStart() {
        Style style = getStyle();
        int txtW = style.getFont().stringWidth(getText());
        int textSpaceW = getAvaliableSpaceForText();
        return txtW > textSpaceW && textSpaceW > 0;
    }

    Image getIconFromState() {
        return getIcon();
    }

    int getAvaliableSpaceForText() {
        Style style = getStyle();
        int textSpaceW = getWidth() - style.getPadding(isRTL(), Label.RIGHT) - style.getPadding(isRTL(), Label.LEFT);
        Image icon = getIconFromState();

        if (icon != null && (getTextPosition() == Label.RIGHT || getTextPosition() == Label.LEFT)) {
            textSpaceW = textSpaceW - icon.getWidth();
        }
        int preserveSpaceForState = 0;

        textSpaceW = textSpaceW - preserveSpaceForState;
        return textSpaceW;
    }

    /**
     * This method will start the text ticker
     * 
     * @param delay the delay in millisecods between animation intervals
     * @param rightToLeft if true move the text to the left
     */
    public void startTicker(long delay, boolean rightToLeft){
        //return if ticker is not enabled
        if(!tickerEnabled){
            return;
        }
        if(!isCellRenderer()){
            Form parent = getComponentForm();
            if(parent != null) {
                parent.registerAnimatedInternal(this);
            }
        }
        tickerStartTime = System.currentTimeMillis();
        tickerDelay = delay;
        tickerRunning = true;
        this.rightToLeft = rightToLeft;
        if (isRTL()) {
        	this.rightToLeft = !this.rightToLeft;
        }
    }
    
    /**
     * Stops the text ticker
     */
    public void stopTicker(){
        tickerRunning = false;
        setShiftText(0);
        Form p = getComponentForm();
        if(p != null) {
            p.deregisterAnimatedInternal(this);
        }
    }

    /**
     * Returns true if the ticker is running
     * 
     * @return true if the ticker is running
     */
    public boolean isTickerRunning() {
        return tickerRunning;
    }

    /**
     * Sets the Label to allow ticking of the text.
     * By default is true
     * 
     * @param tickerEnabled
     */
    public void setTickerEnabled(boolean tickerEnabled) {
        this.tickerEnabled = tickerEnabled;
    }

    /**
     * This method return true if the ticker is enabled on this Label
     * 
     * @return tickerEnabled
     */
    public boolean isTickerEnabled() {
        return tickerEnabled;
    }
   
    /**
     * If the Label text is too long fit the text to the widget and add "..."
     * points at the end.
     * By default this is set to true
     * 
     * @param endsWith3Points true if text should add "..." at the end
     */
    public void setEndsWith3Points(boolean endsWith3Points){
        this.endsWith3Points = endsWith3Points;
    }

    /**
     * Simple getter
     * 
     * @return true if this Label adds "..." when the text is too long
     */
    public boolean isEndsWith3Points() {
        return endsWith3Points;
    }
    
    
    
    /**
     * @inheritDoc
     */
    public boolean animate() {
        boolean animateTicker = false;
        if(tickerRunning && tickerStartTime + tickerDelay < System.currentTimeMillis()){
            tickerStartTime = System.currentTimeMillis();
            if(rightToLeft){
                shiftText-=2;
            }else{
                shiftText+=2;
            }     
            animateTicker = true;
        }                
        // if we have an animated icon then just let it do its thing...
        boolean val = icon != null && icon.isAnimation() && ((Animation)icon).animate();
        boolean parent = super.animate();
        return  val || parent || animateTicker;
    }
}
