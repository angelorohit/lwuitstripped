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
import com.sun.lwuit.animations.CommonTransitions;
import com.sun.lwuit.animations.Transition;
import com.sun.lwuit.geom.Dimension;
import com.sun.lwuit.impl.ImplementationFactory;
import com.sun.lwuit.impl.LWUITImplementation;
import java.io.InputStream;
import java.util.Vector;

/**
 * Central class for the API that manages rendering/events and is used to place top
 * level components ({@link Form}) on the "display". Before any Form is shown the Developer must
 * invoke Display.init(Object m) in order to register the current MIDlet.
 * <p>This class handles the main thread for the toolkit referenced here on as the EDT 
 * (Event Dispatch Thread) similar to the Swing EDT. This thread encapsulates the platform
 * specific event delivery and painting semantics and enables threading features such as
 * animations etc...
 * <p>The EDT should not be blocked since paint operations and events would also be blocked 
 * in much the same way as they would be in other platforms. In order to serialize calls back
 * into the EDT use the methods {@link Display#callSerially} &amp; {@link Display#callSeriallyAndWait}.
 * <p>Notice that all LWUIT calls occur on the EDT (events, painting, animations etc...), LWUIT 
 * should normally be manipulated on the EDT as well (hence the {@link Display#callSerially} &amp; 
 * {@link Display#callSeriallyAndWait} methods). Theoretically it should be possible to manipulate
 * some LWUIT features from other threads but this can't be guaranteed to work for all use cases.
 * 
 * @author Chen Fishbein, Shai Almog
 */
public final class Display {
    /**
     * Unknown keyboard type is the default indicating the software should try
     * to detect the keyboard type if necessary
     */
    public static final int KEYBOARD_TYPE_UNKNOWN = 0;

    /**
     * Numeric keypad keyboard type
     */
    public static final int KEYBOARD_TYPE_NUMERIC = 1;
    
    /**
     * Full QWERTY keypad keyboard type, even if a numeric keyboard also exists
     */
    public static final int KEYBOARD_TYPE_QWERTY = 2;

    /**
     * Touch device without a physical keyboard that should popup a keyboad
     */
    public static final int KEYBOARD_TYPE_VIRTUAL = 3;

    /**
     * Half QWERTY which needs software assistance for completion
     */
    public static final int KEYBOARD_TYPE_HALF_QWERTY = 4;
    
    private static final int POINTER_PRESSED = 1;
    private static final int POINTER_RELEASED = 2;
    private static final int POINTER_DRAGGED = 3;
    private static final int POINTER_HOVER = 8;
    private static final int POINTER_HOVER_RELEASED = 11;
    private static final int KEY_PRESSED = 4;
    private static final int KEY_RELEASED = 5;
    private static final int KEY_LONG_PRESSED = 6;
    private static final int SIZE_CHANGED = 7;
    private static final int HIDE_NOTIFY = 9;
    private static final int SHOW_NOTIFY = 10;
    
    
    private Graphics lwuitGraphics;

    /**
     * Indicates whether this is a touch device
     */
    private boolean touchScreen;

    /**
     * Indicates the maximum drawing speed of no more than 10 frames per second
     * by default (this can be increased or decreased) the advantage of limiting
     * framerate is to allow the CPU to perform other tasks besides drawing.
     * Notice that when no change is occurring on the screen no frame is drawn and
     * so a high/low FPS will have no effect then.
     */
    private int framerateLock = 20;
    
    /**
     * Light mode allows the UI to adapt and show less visual effects/lighter versions
     * of these visual effects to work properly on low end devices.
     */
    private boolean lightMode;
    
    /**
     * Game action for fire
     */
    public static final int GAME_FIRE = 8;

    /**
     * Game action for left key
     */
    public static final int GAME_LEFT = 2;

    /**
     * Game action for right key
     */
    public static final int GAME_RIGHT = 5;

    /**
     * Game action for UP key
     */
    public static final int GAME_UP = 1;

    /**
     * Game action for down key
     */
    public static final int GAME_DOWN = 6;
    
    /**
     * An attribute that encapsulates '#' int value.
     */
    public static final int KEY_POUND = '#';

    private static final Display INSTANCE = new Display();
    
    static int transitionDelay = -1;

    private LWUITImplementation impl;

    private boolean lwuitRunning = false;

    
    /**
     * Contains the call serially pending elements
     */
    private Vector pendingSerialCalls = new Vector();
    
    /**
     * This is the instance of the EDT used internally to indicate whether
     * we are executing on the EDT or some arbitrary thread
     */ 
    private Thread edt; 

    /**
     * Contains animations that must be played in full by the EDT before anything further
     * may be processed. This is useful for transitions/intro's etc... that animate without
     * user interaction.
     */
    private Vector animationQueue;

    /**
     * Indicates whether the 3rd softbutton should be supported on this device
     */
    private boolean thirdSoftButton = false;
    
    private boolean editingText;

    /**
     * Ignore all calls to show occurring during edit, they are discarded immediately
     */
    public static final int SHOW_DURING_EDIT_IGNORE = 1;

    /**
     * If show is called while editing text in the native text box an exception is thrown
     */
    public static final int SHOW_DURING_EDIT_EXCEPTION = 2;
    
    /**
     * Allow show to occur during edit and discard all user input at this moment
     */
    public static final int SHOW_DURING_EDIT_ALLOW_DISCARD = 3;

    /**
     * Allow show to occur during edit and save all user input at this moment
     */
    public static final int SHOW_DURING_EDIT_ALLOW_SAVE = 4;

    /**
     * Show will update the current form to which the OK button of the text box
     * will return
     */
    public static final int SHOW_DURING_EDIT_SET_AS_NEXT = 5;
     
    private int showDuringEdit;
    
    static final Object lock = new Object();
    
    /**
     * Events to broadcast on the EDT
     */
    private Vector inputEvents = new Vector();

    private boolean longPointerCharged;
    private int pointerX, pointerY;
    private boolean keyRepeatCharged;
    private boolean longPressCharged;
    private long longKeyPressTime;
    private int longPressInterval = 800;
    private long nextKeyRepeatEvent;
    private int keyRepeatValue;
    private int keyRepeatInitialIntervalTime = 800;
    private int keyRepeatNextIntervalTime = 10;
    private boolean keyReleasedSinceEdit;

    private boolean processingSerialCalls;
    
    private int PATHLENGTH;
    private float[] dragPathX;
    private float[] dragPathY;
    private long[] dragPathTime;
    private int dragPathOffset = 0;
    private int dragPathLength = 0;
    
    /** 
     * Private constructor to prevent instanciation
     */
    private Display() {
    }

    Vector getAnimationQueue() {
        return animationQueue;
    }
    
    /**
     * This is the Display initialization method.
     * This method must be called before any Form is shown
     * 
     * @param m the main running MIDlet
     */
    public static void init(Object m) {
        if(INSTANCE.impl == null) {
            INSTANCE.lwuitRunning = true;
            INSTANCE.impl = ImplementationFactory.getInstance().createImplementation();
            INSTANCE.impl.setDisplayLock(lock);
            INSTANCE.impl.init(m);
            INSTANCE.lwuitGraphics = new Graphics(INSTANCE.impl.getNativeGraphics());
            INSTANCE.impl.setLWUITGraphics(INSTANCE.lwuitGraphics);
            
            // only enable but never disable the third softbutton
            if(INSTANCE.impl.isThirdSoftButton()) {
                INSTANCE.thirdSoftButton = true;
            }
            if(INSTANCE.impl.getSoftkeyCount() > 0) {
                Form.leftSK = INSTANCE.impl.getSoftkeyCode(0)[0];
                if(INSTANCE.impl.getSoftkeyCount() > 1) {
                    Form.rightSK = INSTANCE.impl.getSoftkeyCode(1)[0];
                    if(INSTANCE.impl.getSoftkeyCode(1).length > 1){
                        Form.rightSK2 = INSTANCE.impl.getSoftkeyCode(1)[1];
                    }
                }
                Form.backSK = INSTANCE.impl.getBackKeyCode();
                Form.backspaceSK = INSTANCE.impl.getBackspaceKeyCode();
                Form.clearSK = INSTANCE.impl.getClearKeyCode();
            }
            
            int width = INSTANCE.getDisplayWidth();
            int height = INSTANCE.getDisplayHeight();
            int colors = INSTANCE.numColors();


            INSTANCE.PATHLENGTH = INSTANCE.impl.getDragPathLength();
            INSTANCE.dragPathX = new float[INSTANCE.PATHLENGTH];
            INSTANCE.dragPathY = new float[INSTANCE.PATHLENGTH];
            INSTANCE.dragPathTime = new long[INSTANCE.PATHLENGTH];
            
            // if the resolution is very high and the amount of memory is very low while the device 
            // itself has many colors (requiring 32 bits per pixel) then we should concerve memory
            // by activating light mode.
            INSTANCE.lightMode = colors > 65536 && width * height * 30 > Runtime.getRuntime().totalMemory();

            // this can happen on some cases where an application was restarted etc...
            // generally its probably a bug but we can let it slide...
            if(INSTANCE.edt == null) {
                INSTANCE.touchScreen = INSTANCE.impl.isTouchDevice();

                // initialize the LWUIT EDT which from now on will take all responsibility
                // for the event delivery.
                INSTANCE.edt = new Thread(new RunnableWrapper(null, 3), "EDT");
                INSTANCE.edt.setPriority(Thread.NORM_PRIORITY + 1);
                INSTANCE.edt.start();
            }
        }
    }

    /**
     * Closes down the EDT and LWUIT, under normal conditions this method is completely unnecessary
     * since exiting the application will shut down LWUIT. However, if the application is minimized
     * and the user wishes to free all resources without exiting the application then this method can be used.
     * Once this method is used LWUIT will no longer work and Display.init(Object) should be invoked
     * again for any further LWUIT call!
     * Notice that minimize (being a LWUIT method) MUST be invoked before invoking this method!
     */
    public static void deinitialize() {
        INSTANCE.lwuitRunning = false;
        synchronized(lock) {
            lock.notifyAll();
        }
    }
    
    /**
     * Return the Display instance
     * 
     * @return the Display instance
     */
    public static Display getInstance(){
        return INSTANCE;
    }

    /**
     * This method allows us to manipulate the drag started detection logic.
     * If the pointer was dragged for more than this percentage of the display size it
     * is safe to assume that a drag is in progress.
     *
     * @return motion percentage
     */
    public int getDragStartPercentage() {
        return getImplementation().getDragStartPercentage();
    }

    /**
     * This method allows us to manipulate the drag started detection logic.
     * If the pointer was dragged for more than this percentage of the display size it
     * is safe to assume that a drag is in progress.
     *
     * @param dragStartPercentage percentage of the screen required to initiate drag
     */
    public void setDragStartPercentage(int dragStartPercentage) {
        getImplementation().setDragStartPercentage(dragStartPercentage);
    }

    LWUITImplementation getImplementation() {
        return impl;
    }
    
    /**
     * Indicates the maximum frames the API will try to draw every second
     * by default this is set to 10. The advantage of limiting
     * framerate is to allow the CPU to perform other tasks besides drawing.
     * Notice that when no change is occurring on the screen no frame is drawn and
     * so a high/low FPS will have no effect then.
     * 10FPS would be very reasonable for a business application.
     * 
     * @param rate the frame rate
     */
    public void setFramerate(int rate) {
        framerateLock = 1000 / rate;
    }
    
    /**
     * Vibrates the device for the given length of time
     * 
     * @param duration length of time to vibrate
     */
    public void vibrate(int duration) {
        impl.vibrate(duration);
    }
    
    /**
     * Flash the backlight of the device for the given length of time
     * 
     * @param duration length of time to flash the backlight
     */
    public void flashBacklight(int duration) {
        impl.flashBacklight(duration);
    }

    /**
     * Invoking the show() method of a form/dialog while the user is editing
     * text in the native text box can have several behaviors: SHOW_DURING_EDIT_IGNORE, 
     * SHOW_DURING_EDIT_EXCEPTION, SHOW_DURING_EDIT_ALLOW_DISCARD, 
     * SHOW_DURING_EDIT_ALLOW_SAVE, SHOW_DURING_EDIT_SET_AS_NEXT
     * 
     * @param showDuringEdit one of the following: SHOW_DURING_EDIT_IGNORE, 
     * SHOW_DURING_EDIT_EXCEPTION, SHOW_DURING_EDIT_ALLOW_DISCARD, 
     * SHOW_DURING_EDIT_ALLOW_SAVE, SHOW_DURING_EDIT_SET_AS_NEXT
     */
    public void setShowDuringEditBehavior(int showDuringEdit) {
        this.showDuringEdit = showDuringEdit;
    }

    /**
     * Returns the status of the show during edit flag
     * 
     * @return one of the following: SHOW_DURING_EDIT_IGNORE, 
     * SHOW_DURING_EDIT_EXCEPTION, SHOW_DURING_EDIT_ALLOW_DISCARD, 
     * SHOW_DURING_EDIT_ALLOW_SAVE, SHOW_DURING_EDIT_SET_AS_NEXT
     */
    public int getShowDuringEditBehavior() {
        return showDuringEdit;
    }

    /**
     * Indicates the maximum frames the API will try to draw every second
     * 
     * @return the frame rate
     */
    public int getFrameRate() {
        return 1000 / framerateLock;
    }
        
    /**
     * Returns true if we are currently in the event dispatch thread.
     * This is useful for generic code that can be used both with the
     * EDT and outside of it.
     * 
     * @return true if we are currently in the event dispatch thread; 
     * otherwise false
     */
    public boolean isEdt() {
        return edt == Thread.currentThread();
    }
    
    /**
     * Plays sound for the dialog
     */
    void playDialogSound(final int type) {
        impl.playDialogSound(type);
    }
    
    /**
     * Causes the runnable to be invoked on the event dispatch thread. This method
     * returns immediately and will not wait for the serial call to occur 
     * 
     * @param r runnable (NOT A THREAD!) that will be invoked on the EDT serial to
     * the paint and key handling events 
     */
    public void callSerially(Runnable r){
        synchronized(lock) {
            pendingSerialCalls.addElement(r);
            lock.notify();
        }
    }
    
    
    /**
     * Identical to callSerially with the added benefit of waiting for the Runnable method to complete.
     * 
     * @param r runnable (NOT A THREAD!) that will be invoked on the EDT serial to
     * the paint and key handling events 
     * @throws IllegalStateException if this method is invoked on the event dispatch thread (e.g. during
     * paint or event handling).
     */
    public void callSeriallyAndWait(Runnable r){
        RunnableWrapper c = new RunnableWrapper(r, 0);
        callSerially(c);
        synchronized(lock) {
            while(!c.isDone()) {
                try {
                    // poll doneness to prevent potential race conditions
                    lock.wait(50);
                } catch(InterruptedException err) {}
            }
        }
    }
    
    /**
     * Allows us to "flush" the edt to allow any pending transitions and input to go
     * by before continuing with our other tasks.
     */
    void flushEdt() {
        if(!isEdt()){
            return;
        }
        while(!shouldEDTSleepNoFormAnimation()) {
            edtLoopImpl();
        }
        while(animationQueue != null && animationQueue.size() > 0){
            edtLoopImpl();
        }
    }

    /**
     * Restores the menu in the given form
     */
    private void restoreMenu(Form f) {
        if(f != null) {
            f.restoreMenu();
        }
    }
    

    private void paintTransitionAnimation() {
        Animation ani = (Animation) animationQueue.elementAt(0);
        if (!ani.animate()) {
            animationQueue.removeElementAt(0);
            if (ani instanceof Transition) {
                Form source = (Form) ((Transition)ani).getSource();
                restoreMenu(source);
                
                if (animationQueue.size() > 0) {
                    ani = (Animation) animationQueue.elementAt(0);
                    if (ani instanceof Transition) {
                        ((Transition) ani).initTransition();
                    }
                }else{
                    Form f = (Form) ((Transition)ani).getDestination();
                    restoreMenu(f);
                    if (source == null || source == impl.getCurrentForm() || source == getCurrent()) {
                        setCurrentForm(f);
                    }
                    ((Transition) ani).cleanup();
                }
                return;
            }
        }
        ani.paint(lwuitGraphics);
        impl.flushGraphics();

        if(transitionDelay > 0) {
            // yield for a fraction, some devices don't "properly" implement
            // flush and so require the painting thread to get CPU too.
            try {
                synchronized(lock){
                    lock.wait(transitionDelay);
                }
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }
    
    /**
     * This method represents the event thread for the UI library on which 
     * all events are carried out. It differs from the MIDP event thread to 
     * prevent blocking of actual input and drawing operations. This also
     * enables functionality such as "true" modal dialogs etc...
     */
    void mainEDTLoop() {
        try {
            synchronized(lock){
                // when there is no current form the EDT is useful only
                // for features such as call serially
                while(impl.getCurrentForm() == null) {
                    if(shouldEDTSleep()) {
                        lock.wait();
                    }

                    // paint transition or intro animations and don't do anything else if such
                    // animations are in progress...
                    if(animationQueue != null && animationQueue.size() > 0) {
                        paintTransitionAnimation();
                        continue;
                    }
                    processSerialCalls();
                }
            }
        } catch(Throwable err) {
            err.printStackTrace();
            if(!impl.handleEDTException(err)) {
                Dialog.show("Error", "An internal application error occurred: " + err.toString(), "OK", null);
            }
        }
        
        while(lwuitRunning) {
            try {
                // wait indefinetly but no more than the framerate if
                // there are no animations... If animations exist then
                // only wait for the framerate
                // Lock surrounds the should method to prevent serial calls from
                // getting "lost"
                 synchronized(lock){
                     if(shouldEDTSleep()) {
                         impl.edtIdle(true);
                         lock.wait();
                         impl.edtIdle(false);
                     }
                 } 

                edtLoopImpl();
            } catch(Throwable err) {
                err.printStackTrace();
                if(!impl.handleEDTException(err)) {
                    Dialog.show("Error", "An internal application error occurred: " + err.toString(), "OK", null);
                }
            }
        }
        INSTANCE.impl = null;
        INSTANCE.lwuitGraphics = null;
        INSTANCE.edt = null;
    }
    
    long time;
    
    /**
     * Implementation of the event dispatch loop content
     */
    void edtLoopImpl() {
        try {
            // transitions shouldn't be bound by framerate
            if(animationQueue == null || animationQueue.size() == 0) {
                // prevents us from waking up the EDT too much and 
                // thus exhausting the systems resources. The + 1
                // prevents us from ever waiting 0 milliseconds which
                // is the same as waiting with no time limit
                synchronized(lock){
                    lock.wait(Math.max(30, framerateLock - (time)));
                }
                
            } else {
                // paint transition or intro animations and don't do anything else if such
                // animations are in progress...
                paintTransitionAnimation();
                return;
            }
        } catch(Exception ignor) {
            ignor.printStackTrace();
        }

        long currentTime = System.currentTimeMillis();

        while(inputEvents.size() > 0) {
            int[] i = (int[])inputEvents.elementAt(0);
            inputEvents.removeElementAt(0);
            handleEvent(i);
        }

        lwuitGraphics.setGraphics(impl.getNativeGraphics());
        impl.paintDirty();

        // draw the animations
        Form current = impl.getCurrentForm();
        current.repaintAnimations();
        
        // check key repeat events
        long t = System.currentTimeMillis();
        if(keyRepeatCharged && nextKeyRepeatEvent <= t) {
            current.keyRepeated(keyRepeatValue);
            nextKeyRepeatEvent = t + keyRepeatNextIntervalTime;
        }
        if(longPressCharged && longPressInterval <= t - longKeyPressTime) {
            longPressCharged = false;
            current.longKeyPress(keyRepeatValue);
        }
        if(longPointerCharged && longPressInterval <= t - longKeyPressTime) {
            longPointerCharged = false;
            current.longPointerPress(pointerX, pointerY);
        }
        processSerialCalls();
        time = System.currentTimeMillis() - currentTime;
    }
    
    boolean hasNoSerialCallsPending() {
        return pendingSerialCalls.size() == 0;
    }
    
    /**
     * Called by the underlying implementation to indicate that editing in the native
     * system has completed and changes should propogate into LWUIT
     * 
     * @param c edited component
     * @param text new text for the component
     */
    public void onEditingComplete(Component c, String text) {
        c.onEditComplete(text);
        c.fireActionEvent();
    }
    
    /**
     * Used by the EDT to process all the calls submitted via call serially
     */
    void processSerialCalls() {
        processingSerialCalls = true;
        int size = pendingSerialCalls.size();
        if(size > 0) {
            Runnable[] array = new Runnable[size];

            // copy all elements to an array and remove them otherwise invokeAndBlock from
            // within a callSerially() can cause an infinite loop...
            for(int iter = 0 ; iter < size ; iter++) {
                array[iter] = (Runnable)pendingSerialCalls.elementAt(iter);
            }

            synchronized(lock) {
                if(size == pendingSerialCalls.size()) {
                    // this is faster
                    pendingSerialCalls.removeAllElements();
                } else {
                    // this can occur if an element was added during the loop
                    for(int iter = 0 ; iter < size ; iter++) {
                        pendingSerialCalls.removeElementAt(0);
                    }
                }
            }

            for(int iter = 0 ; iter < size ; iter++) {
                array[iter].run();
            }

            // after finishing an event cycle there might be serial calls waiting
            // to return.
            synchronized(lock){
                lock.notify();
            }
        }
        processingSerialCalls = false;
    }

    boolean isProcessingSerialCalls() {
        return processingSerialCalls;
    }
    
    void notifyDisplay(){
        synchronized (lock) {
            lock.notify();
        }
    }
    
    /**
     * Invokes runnable and blocks the current thread, if the current thread is the
     * edt it will still be blocked however a separate thread would be launched
     * to perform the duties of the EDT while it is blocked. Once blocking is finished
     * the EDT would be restored to its original position. This is very similar to the
     * "foxtrot" Swing toolkit and allows coding "simpler" logic that requires blocking
     * code in the middle of event sensitive areas.
     * 
     * @param r runnable (NOT A THREAD!) that will be invoked synchroniously by this method
     */
    public void invokeAndBlock(Runnable r){
        if(isEdt()) {
            // this class allows a runtime exception to propogate correctly out of the
            // internal thread
            RunnableWrapper w = new RunnableWrapper(r, 1);
            RunnableWrapper.pushToThreadPull(w);

            // loop over the EDT until the thread completes then return
            while(!w.isDone()) {
                try {
                    edtLoopImpl();
                    synchronized(lock) {
                        lock.wait(framerateLock);
                    }
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
            // if the thread thew an exception we need to throw it onwards
            if(w.getErr() != null) {
                throw w.getErr();
            }
        } else {
            r.run();
        }
    }

    /**
     * Indicates if this is a touch screen device that will return pen events,
     * defaults to true if the device has pen events but can be overriden by
     * the developer.
     * 
     * @return true if this device supports touch events
     */
    public boolean isTouchScreenDevice() {
        return touchScreen;
    }
    
    /**
     * Indicates if this is a touch screen device that will return pen events,
     * defaults to true if the device has pen events but can be overriden by
     * the developer.
     * 
     * @param touchScreen false if this is not a touch screen device
     */
    public void setTouchScreenDevice(boolean touchScreen) {
        this.touchScreen = touchScreen;
    }
    
    /**
     * Displays the given Form on the screen.
     * 
     * @param newForm the Form to Display
     */
    void setCurrent(final Form newForm, boolean reverse){
        if(edt == null) {
            throw new IllegalStateException("Initialize must be invoked before setCurrent!");
        }
        
        if(isVirtualKeyboardShowingSupported()) {
            setShowVirtualKeyboard(false);
        }
        
        if(editingText) {
            switch(showDuringEdit) {
                case SHOW_DURING_EDIT_ALLOW_DISCARD:
                    break;
                case SHOW_DURING_EDIT_ALLOW_SAVE:
                    impl.saveTextEditingState();
                    break;
                case SHOW_DURING_EDIT_EXCEPTION:
                    throw new IllegalStateException("Show during edit");
                case SHOW_DURING_EDIT_IGNORE:
                    return;
                case SHOW_DURING_EDIT_SET_AS_NEXT:
                    impl.setCurrentForm(newForm);
                    return;
            }
        }
        
        if(!isEdt()) {
            callSerially(new RunnableWrapper(newForm, null, reverse));
            return;
        }
        
        Form current = impl.getCurrentForm();
        if(current != null){
            if(current.isInitialized()) {
                current.deinitializeImpl();
            }
        }
        if(!newForm.isInitialized()) {
            newForm.initComponentImpl();
        }

        if(newForm.getWidth() != getDisplayWidth() || newForm.getHeight() != getDisplayHeight()) {
            newForm.setShouldCalcPreferredSize(true);
            newForm.layoutContainer();
        }

        synchronized(lock) {
            boolean transitionExists = false;
            if(animationQueue != null && animationQueue.size() > 0) {
                Object o = animationQueue.lastElement();
                if(o instanceof Transition) {
                    current = (Form)((Transition)o).getDestination();
                    impl.setCurrentForm(current);
                }
            }

            if(current != null) {
                // make sure the fold menu occurs as expected then set the current
                // to the correct parent!
                if(current instanceof Dialog && ((Dialog)current).isMenu()) {
                    Transition t = current.getTransitionOutAnimator();
                    if(t != null) {
                        // go back to the parent form first
                        if(((Dialog)current).getPreviousForm() != null) {
                            initTransition(t.copy(false), current, ((Dialog)current).getPreviousForm());
                        }
                    }
                    current = ((Dialog)current).getPreviousForm();
                    impl.setCurrentForm(current);
                }

                // prevent the transition from occurring from a form into itself
                if(newForm != current) {
                    if((current != null && current.getTransitionOutAnimator() != null) || newForm.getTransitionInAnimator() != null) {
                        if(animationQueue == null) {
                            animationQueue = new Vector();
                        }
                        // prevent form transitions from breaking our dialog based
                        // transitions which are a bit sensitive
                        if(current != null && (!(newForm instanceof Dialog))) {
                            Transition t = current.getTransitionOutAnimator();
                            if(current != null && t != null) {
                                initTransition(t.copy(reverse), current, newForm);
                                transitionExists = true;
                            }
                        }
                        if(current != null && !(current instanceof Dialog)) {
                            Transition t = newForm.getTransitionInAnimator();
                            if(t != null) {
                                initTransition(t.copy(reverse), current, newForm);
                                transitionExists = true;
                            }
                        }
                    }
                }
            }
            lock.notify();
            
            if(!transitionExists) {
                if(animationQueue == null || animationQueue.size() == 0) {
                    setCurrentForm(newForm);
                } else {
                    // we need to add an empty transition to "serialize" this
                    // screen change...
                    Transition t = CommonTransitions.createEmpty();
                    initTransition(t, current, newForm);
                }
            }
        }
    }
    
    /**
     * Initialize the transition and add it to the queue
     */
    private void initTransition(Transition transition, Form source, Form dest) {
        dest.setVisible(true);
        transition.init(source, dest);
        animationQueue.addElement(transition);
        
        if(animationQueue.size() == 1) {
            transition.initTransition();
        }
    }
    
    void setCurrentForm(Form newForm){
        Form current = impl.getCurrentForm();
        if(current != null){
            current.setVisible(false);
        }
        current = newForm;
        impl.setCurrentForm(current);
        current.setVisible(true);
        impl.confirmControlView();
        int w = current.getWidth();
        int h = current.getHeight();
        if(isEdt() && ( w != impl.getDisplayWidth() || h != impl.getDisplayHeight())){
           current.setSize(new Dimension(impl.getDisplayWidth(), impl.getDisplayHeight()));
           
           current.setShouldCalcPreferredSize(true);
           current.layoutContainer();
        }
        lastKeyPressed = 0;
        repaint(current);
        newForm.onShowCompleted();
    }
    
    /**
     * Indicate to the implementation whether the flush graphics bug exists on this
     * device. By default the flushGraphics bug is set to "true" and only disabled
     * on handsets known 100% to be safe
     * 
     * @param flushGraphicsBug true if the bug exists on this device (the safe choice)
     * false for slightly higher performance.
     * @deprecated this method is no longer supported use GameCanvasImplementation.setFlashGraphicsBug(f) instead
     */
    public void setFlashGraphicsBug(boolean flushGraphicsBug) {
    }
    
    /**
     * Indicates whether a delay should exist between calls to flush graphics during
     * transition. In some devices flushGraphics is asynchronious causing it to be
     * very slow with our background thread. The solution is to add a short wait allowing
     * the implementation time to paint the screen. This value is set automatically by default
     * but can be overriden for some devices.
     * 
     * @param transitionD -1 for no delay otherwise delay in milliseconds
     */
    public void setTransitionYield(int transitionD) {
        transitionDelay = transitionD;
    }

    /**
     * Encapsulates the editing code which is specific to the platform, some platforms
     * would allow "in place editing" MIDP does not.
     * 
     * @param cmp the {@link TextArea} component
     */
    public void editString(Component cmp, int maxSize, int constraint, String text) {
        editingText = true;
        keyRepeatCharged = false;
        longPressCharged = false;
        keyReleasedSinceEdit = false;
        lastKeyPressed = 0;
        impl.editString(cmp, maxSize, constraint, text);
        editingText = false;
    }

    /**
     * Minimizes the current application if minimization is supported by the platform (may fail).
     * Returns false if minimization failed.
     *
     * @return false if minimization failed true if it succeeded or seems to be successful
     */
    public boolean minimizeApplication() {
        return getImplementation().minimizeApplication();
    }

    /**
     * Indicates whether an application is minimized
     *
     * @return true if the application is minimized
     */
    public boolean isMinimized() {
        return getImplementation().isMinimized();
    }

    /**
     * Restore the minimized application if minimization is supported by the platform
     */
    public void restoreMinimizedApplication() {
        getImplementation().restoreMinimizedApplication();
    }

    private void addInputEvent(int[] ev) {
        synchronized(lock) {
            inputEvents.addElement(ev);
            lock.notify();
        }
    }

    /**
     * Creates a pointer event with the following properties
     */
    private int[] createPointerEvent(int[] x, int[] y, int eventType) {
        if(x.length == 1) {
            return new int[] {eventType, x[0], y[0]};
        }
        int[] arr = new int[1 + x.length * 2];
        arr[0] = eventType;
        int arrayOffset = 1;
        for(int iter = 0 ; iter < x.length ; iter++) {
            arr[arrayOffset] = x[iter];
            arrayOffset++;
            arr[arrayOffset] = y[iter];
            arrayOffset++;
        }
        return arr;
    }


    private int[] createKeyEvent(int keyCode, boolean pressed) {
        if(pressed) {
            return new int[] {KEY_PRESSED, keyCode};
        } else {
            return new int[] {KEY_RELEASED, keyCode};
        }
    }
    
    private int lastKeyPressed;
    
    /**
     * Pushes a key press event with the given keycode into LWUIT
     * 
     * @param keyCode keycode of the key event
     */
    public void keyPressed(final int keyCode){
        if(impl.getCurrentForm() == null){
            return;
        }
        addInputEvent(createKeyEvent(keyCode, true));

        // this solves a Sony Ericsson bug where on slider open/close someone "brilliant" chose
        // to send a keyPress with a -43/-44 keycode... Without ever sending a key release!
        keyRepeatCharged = (keyCode >= 0 || getGameAction(keyCode) > 0);
        longPressCharged = keyRepeatCharged;
        longKeyPressTime = System.currentTimeMillis();
        keyRepeatValue = keyCode;
        nextKeyRepeatEvent = System.currentTimeMillis() + keyRepeatInitialIntervalTime;
        lastKeyPressed = keyCode;
    }

    /**
     * Pushes a key release event with the given keycode into LWUIT
     * 
     * @param keyCode keycode of the key event
     */
    public void keyReleased(final int keyCode){
        keyRepeatCharged = false;
        longPressCharged = false;
        if(impl.getCurrentForm() == null){
            return;
        }
        // this can happen when traversing from the native form to the current form
        // caused by a keypress
        if((!keyReleasedSinceEdit) && keyCode != lastKeyPressed) {
            return;
        }
        keyReleasedSinceEdit = true;
        addInputEvent(createKeyEvent(keyCode, false));
    }

    void keyRepeatedInternal(final int keyCode){
    }
    
    /**
     * Pushes a pointer drag event with the given coordinates into LWUIT
     * 
     * @param x the x position of the pointer
     * @param y the y position of the pointer
     */
    public void pointerDragged(final int[] x, final int[] y){
        if(impl.getCurrentForm() == null){
            return;
        }
        longPointerCharged = false;
        addInputEvent(createPointerEvent(x, y, POINTER_DRAGGED));
    }

    /**
     * Pushes a pointer hover event with the given coordinates into LWUIT
     * 
     * @param x the x position of the pointer
     * @param y the y position of the pointer
     */
    public void pointerHover(final int[] x, final int[] y){
        if(impl.getCurrentForm() == null){
            return;
        }
        addInputEvent(createPointerEvent(x, y, POINTER_HOVER));
    }


    /**
     * Pushes a pointer hover release event with the given coordinates into LWUIT
     *
     * @param x the x position of the pointer
     * @param y the y position of the pointer
     */
    public void pointerHoverReleased(final int[] x, final int[] y){
        if(impl.getCurrentForm() == null){
            return;
        }
        addInputEvent(createPointerEvent(x, y, POINTER_HOVER_RELEASED));
    }

    /**
     * Pushes a pointer press event with the given coordinates into LWUIT
     * 
     * @param x the x position of the pointer
     * @param y the y position of the pointer
     */
    public void pointerPressed(final int[] x,final int[] y){
        if(impl.getCurrentForm() == null){
            return;
        }
        longPointerCharged = true;
        longKeyPressTime = System.currentTimeMillis();
        pointerX = x[0];
        pointerY = y[0];
        addInputEvent(createPointerEvent(x, y, POINTER_PRESSED));
    }
    
    /**
     * Pushes a pointer release event with the given coordinates into LWUIT
     * 
     * @param x the x position of the pointer
     * @param y the y position of the pointer
     */
    public void pointerReleased(final int[] x, final int[] y){
        longPointerCharged = false;
        if(impl.getCurrentForm() == null){
            return;
        }
        addInputEvent(createPointerEvent(x, y, POINTER_RELEASED));
    }

    /**
     * Notifies LWUIT of display size changes, this method is invoked by the implementation
     * class and is for internal use
     * 
     * @param w the width of the drawing surface
     * @param h the height of the drawing surface
     */
    public void sizeChanged(int w, int h){
        Form current = impl.getCurrentForm();
        if(current == null) {
            return;
        }
        if(w == current.getWidth() && h == current.getHeight()) {
            return;
        }
            
        addInputEvent(createSizeChangedEvent(w, h));
    }

    private int[] createSizeChangedEvent(int w, int h) {
        return new int[] {SIZE_CHANGED, w, h};
    }

    public void hideNotify(){
        keyRepeatCharged = false;
        longPressCharged = false;
        longPointerCharged = false;
        addInputEvent(new int[]{HIDE_NOTIFY});
    }

    public void showNotify(){
        addInputEvent(new int[]{SHOW_NOTIFY});        
    }
    
    
    /**
     * Used by the flush functionality which doesn't care much about component
     * animations
     */
    boolean shouldEDTSleepNoFormAnimation() {
        synchronized(lock){
            return inputEvents.size() == 0 &&
                    hasNoSerialCallsPending() && 
                    (!keyRepeatCharged || !longPressCharged);
        }
    }

    private void updateDragSpeedStatus(int[] ev) {
            //save dragging input to calculate the dragging speed later
            dragPathX[dragPathOffset] = pointerEvent(1, ev)[0];
            dragPathY[dragPathOffset] = pointerEvent(2, ev)[0];
            dragPathTime[dragPathOffset] = System.currentTimeMillis();
            if (dragPathLength < PATHLENGTH) {
                dragPathLength++;
            }
            dragPathOffset++;
            if (dragPathOffset >= PATHLENGTH) {
                dragPathOffset = 0;
            }
    }

    /**
     * Invoked on the EDT to propagate the event
     */
    private void handleEvent(int[] ev) {
        Form f = getCurrentUpcomingForm(true);
        
        switch(ev[0]) {
        case KEY_PRESSED:
            f.keyPressed(ev[1]);
            break;
        case KEY_RELEASED:
            f.keyReleased(ev[1]);
            break;
        case POINTER_PRESSED:
            f.pointerPressed(pointerEvent(1, ev), pointerEvent(2, ev));
            break;
        case POINTER_RELEASED:
            f.pointerReleased(pointerEvent(1, ev), pointerEvent(2, ev));
            break;
        case POINTER_DRAGGED:
            updateDragSpeedStatus(ev);
            f.pointerDragged(pointerEvent(1, ev), pointerEvent(2, ev));
            break;
        case POINTER_HOVER:
            updateDragSpeedStatus(ev);
            f.pointerHover(pointerEvent(1, ev), pointerEvent(2, ev));
            break;
        case POINTER_HOVER_RELEASED:
            f.pointerHoverReleased(pointerEvent(1, ev), pointerEvent(2, ev));
            break;
        case SIZE_CHANGED:
            f.sizeChangedInternal(ev[1], ev[2]);
            break;
        case HIDE_NOTIFY:
            f.hideNotify();
            break;
        case SHOW_NOTIFY:
            f.showNotify();
            break;    
        }
    }
    
    private int[] pointerEvent(int off, int[] event) {
        int[] peX = new int[event.length / 2];
        int offset = 0;
        for(int iter = off ; iter < event.length ; iter+=2 ) {
            peX[offset] = event[iter];
            offset++;
        }
        return peX;
    }

    /**
     * Returns true for a case where the EDT has nothing at all to do
     */
    boolean shouldEDTSleep() {
        Form current = impl.getCurrentForm();
        return (current == null || (!current.hasAnimations())) &&
                (animationQueue == null || animationQueue.size() == 0) &&
                inputEvents.size() == 0 &&
                (!impl.hasPendingPaints()) &&
                hasNoSerialCallsPending() && !keyRepeatCharged 
                && !longPointerCharged;
    }
    
    
   /**
    * Returns the video control for the media player
    * 
    * @param player the media player
    * @return the video control for the media player
    */
    Object getVideoControl(Object player) {
        return impl.getVideoControl(player);
    }
    
    Form getCurrentInternal() {
        return impl.getCurrentForm();
    }
        
    /**
     * Same as getCurrent with the added exception of looking into the future
     * transitions and returning the last current in the transition (the upcoming
     * value for current)
     * 
     * @return the form currently displayed on the screen or null if no form is
     * currently displayed
     */
    Form getCurrentUpcoming() {
        return getCurrentUpcomingForm(false);
    }

    private Form getCurrentUpcomingForm(boolean includeMenus) {
        Form upcoming = null;
        
        // we are in the middle of a transition so we should extract the next form
        if(animationQueue != null) {
            int size = animationQueue.size();
            for(int iter = 0 ; iter < size ; iter++) {
                Object o = animationQueue.elementAt(iter);
                if(o instanceof Transition) {
                    upcoming = (Form)((Transition)o).getDestination();
                }
            }
        }
        if(upcoming == null) {
            if(includeMenus){
                Form f = impl.getCurrentForm();
                if(f instanceof Dialog) {
                    if(((Dialog)f).isDisposed()) {
                        return getCurrent();
                    }
                }
                return f;
            }else{
                return getCurrent();
            }
        }
        return upcoming;
    }
    
    /**
     * Return the form currently displayed on the screen or null if no form is
     * currently displayed.
     * 
     * @return the form currently displayed on the screen or null if no form is
     * currently displayed
     */
    public Form getCurrent(){
        Form current = impl.getCurrentForm();
        if(current != null && current instanceof Dialog) {
            if(((Dialog)current).isMenu() || ((Dialog)current).isDisposed()) {
                Form p = current.getPreviousForm();
                if(p != null) {
                    return p;
                }

                // we are in the middle of a transition so we should extract the next form
                int size = animationQueue.size();
                for(int iter = 0 ; iter < size ; iter++) {
                    Object o = animationQueue.elementAt(iter);
                    if(o instanceof Transition) {
                        return (Form)((Transition)o).getDestination();
                    }
                }
            }
        }
        return current;
    }
    
    /**
     * Return the number of alpha levels supported by the implementation.
     * 
     * @return the number of alpha levels supported by the implementation
     */
    public int numAlphaLevels(){
        return impl.numAlphaLevels();
    }

    /**
     * Returns the number of colors applicable on the device, note that the API
     * does not support gray scale devices.
     * 
     * @return the number of colors applicable on the device
     */
    public int numColors() {
        return impl.numColors();
    }

    /**
     * Light mode allows the UI to adapt and show less visual effects/lighter versions
     * of these visual effects to work properly on low end devices.
     * 
     * @return true if this is light mode
     * @deprecated this method is no longer used, it was too unreliable
     */
    public boolean isLightMode() {
        return lightMode;
    }

    /**
     * Light mode allows the UI to adapt and show less visual effects/lighter versions
     * of these visual effects to work properly on low end devices.
     * 
     * @param lightMode true to activate light mode
     * @deprecated this method is no longer used, it was too unreliable
     */
    public void setLightMode(boolean lightMode) {
        this.lightMode = lightMode;
    }
    
    
    /**
     * Return the width of the display
     * 
     * @return the width of the display
     */
    public int getDisplayWidth(){
        return impl.getDisplayWidth();
    }
    
    /**
     * Return the height of the display
     * 
     * @return the height of the display
     */
    public int getDisplayHeight(){
        return impl.getDisplayHeight();
    }
    
    /**
     * Causes the given component to repaint, used internally by Form
     * 
     * @param cmp the given component to repaint
     */
    void repaint(final Animation cmp){
        impl.repaint(cmp);
    }
     
    /**
     * Returns the game action code matching the given key combination
     * 
     * @param keyCode key code received from the event
     * @return game action matching this keycode
     */
    public int getGameAction(int keyCode){
        return impl.getGameAction(keyCode);
    }
    
    /**
     * Returns the keycode matching the given game action constant (the opposite of getGameAction).
     * On some devices getKeyCode returns numeric keypad values for game actions,
     * this breaks the code since we filter these values (to prevent navigation on '2'). 
     * We pick unused negative values for game keys and assign them to game keys for 
     * getKeyCode so they will work with getGameAction.
     * 
     * @param gameAction game action constant from this class
     * @return keycode matching this constant
     * @deprecated this method doesn't work properly across device and is mocked up here
     * mostly for the case of unit testing. Do not use it for anything other than that! Do
     * not rely on getKeyCode(GAME_*) == keyCodeFromKeyEvent, this will never actually happen!
     */
    public int getKeyCode(int gameAction){
        return impl.getKeyCode(gameAction);
    }
     
    /**
     * Allows overriding the softkeys initialized by the software to a different value.
     * This method MUST be invoked after init() has completed. 
     * <p>In order to maintain the default value 0 can be passed as a value for a softkey
     * thus resulting in no effect e.g. setSoftkeyCodes(0, 0, 0, -8); will only affect the back key.
     * @param left the left softkey code
     * @param right the right softkey code
     * @param clear the clear softkey code
     * @param back the back softkey code
     * 
     * @deprecated the fix for this is to work with the LWUITImplementation classes and GameCanvasImplementation,
     * extend those classes to return the values you want or alternatively use the JAD file attributes
     * where appropriate
     */
    public void setSoftkeyCodes(int left, int right, int clear, int back) {
    }
    
    
    /**
     * Indicates whether the 3rd softbutton should be supported on this device
     * 
     * @return true if a third softbutton should be used
     */
    public boolean isThirdSoftButton() {
        return thirdSoftButton;
    }

    /**
     * Indicates whether the 3rd softbutton should be supported on this device
     * 
     * @param thirdSoftButton true if a third softbutton should be used
     */
    public void setThirdSoftButton(boolean thirdSoftButton) {
        this.thirdSoftButton = thirdSoftButton;
    }

    
    /**
     * Displays the virtual keyboard on devices that support manually poping up
     * the vitual keyboard
     * 
     * @param show toggles the virtual keyboards visibility
     */
    public void setShowVirtualKeyboard(boolean show) {
        impl.setShowVirtualKeyboard(show);
    }

    /**
     * Indicates if the virtual keyboard is currently showing or not
     *
     * @return true if the virtual keyboard is showing
     */
    public boolean isVirtualKeyboardShowing() {
        return impl.isVirtualKeyboardShowing();
    }
    
    /**
     * Indicates whether showing a virtual keyboard programmatically is supported 
     * 
     * @return false by default
     */
    public boolean isVirtualKeyboardShowingSupported() {
        return impl.isVirtualKeyboardShowingSupported();
    }
    
    /**
     * Returns the type of the input device one of:
     * KEYBOARD_TYPE_UNKNOWN, KEYBOARD_TYPE_NUMERIC, KEYBOARD_TYPE_QWERTY, 
     * KEYBOARD_TYPE_VIRTUAL, KEYBOARD_TYPE_HALF_QWERTY
     * 
     * @return KEYBOARD_TYPE_UNKNOWN
     */
    public int getKeyboardType() {
        return impl.getKeyboardType();
    }
    
    /**
     * Indicates whether the device supports native in place editing in which case
     * lightweight input logic shouldn't be used for input.
     * 
     * @return false by default
     */
    public boolean isNativeInputSupported() {
        return false;
    }
    
    /**
     * Indicates whether the device supports multi-touch events, this is only
     * relevant when touch events are supported
     * 
     * @return false by default
     */
    public boolean isMultiTouch() {
        return impl.isMultiTouch();
    }
    
    /**
     * Indicates whether the device has a double layer screen thus allowing two
     * stages to touch events: click and hover. This is true for devices such 
     * as the storm but can also be true for a PC with a mouse pointer floating 
     * on top.
     * <p>A click touch screen will also send pointer hover events to the underlying
     * software and will only send the standard pointer events on click.
     * 
     * @return false by default
     */
    public boolean isClickTouchScreen() {
        return impl.isClickTouchScreen();
    }

    /**
     * This method returns the dragging speed based on the latest dragged
     * events
     * @param yAxis indicates what axis speed is required
     * @return the dragging speed
     */
    float getDragSpeed(boolean yAxis){
        float speed;
        if(yAxis){
            speed = impl.getDragSpeed(dragPathY, dragPathTime, dragPathOffset, dragPathLength);
        }else{
            speed = impl.getDragSpeed(dragPathX, dragPathTime, dragPathOffset, dragPathLength);
        }
        dragPathLength = 0;
        return speed;
    }

    /**
     * Indicates whether LWUIT should consider the bidi RTL algorithm
     * when drawing text or navigating with the text field cursor.
     *
     * @return true if the bidi algorithm should be considered
     */
    public boolean isBidiAlgorithm() {
        return impl.isBidiAlgorithm();
    }

    /**
     * Indicates whether LWUIT should consider the bidi RTL algorithm
     * when drawing text or navigating with the text field cursor.
     *
     * @param activate set to true to activate the bidi algorithm, false to
     * disable it
     */
    public void setBidiAlgorithm(boolean activate) {
        impl.setBidiAlgorithm(activate);
    }

    /**
     * Converts the given string from logical bidi layout to visual bidi layout so
     * it can be rendered properly on the screen. This method is only necessary
     * for devices/platforms that don't have "built in" bidi support such as
     * Sony Ericsson devices.
     * See <a href="http://www.w3.org/International/articles/inline-bidi-markup/#visual">this</a>
     * for more on visual vs. logical ordering.
     *
     *
     * @param s a "logical" string with RTL characters
     * @return a "visual" renderable string
     */
    public String convertBidiLogicalToVisual(String s) {
        return impl.convertBidiLogicalToVisual(s);
    }

    /**
     * Returns the index of the given char within the source string, the actual
     * index isn't necessarily the same when bidi is involved
     * See <a href="http://www.w3.org/International/articles/inline-bidi-markup/#visual">this</a>
     * for more on visual vs. logical ordering.
     *
     * @param source the string in which we are looking for the position
     * @param index the "logical" location of the cursor
     * @return the "visual" location of the cursor
     */
	public int getCharLocation(String source, int index) {
        return impl.getCharLocation(source, index);
    }

    /**
     * Returns true if the given character is an RTL character
     *
     * @param c character to test
     * @return true if the charcter is an RTL character
     */
	public boolean isRTL(char c) {
        return impl.isRTL(c);
    }

    /**
     * This method is essentially equivalent to cls.getResourceAsStream(String)
     * however some platforms might define unique ways in which to load resources
     * within the implementation.
     *
     * @param cls class to load the resource from
     * @param resource relative/absolute URL based on the Java convention
     * @return input stream for the resource or null if not found
     */
    public InputStream getResourceAsStream(Class cls, String resource) {
        return impl.getResourceAsStream(cls, resource);
    }
}
