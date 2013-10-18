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

import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.events.DataChangedListener;
import com.sun.lwuit.events.FocusListener;
import com.sun.lwuit.plaf.Style;
import com.sun.lwuit.layouts.BorderLayout;
import com.sun.lwuit.layouts.BoxLayout;
import com.sun.lwuit.layouts.FlowLayout;
import com.sun.lwuit.layouts.GridLayout;
import com.sun.lwuit.plaf.UIManager;
import com.sun.lwuit.util.EventDispatcher;
import java.util.Date;
import java.util.Hashtable;
import java.util.TimeZone;

/**
 * Date widget for selecting a date/time value. 
 * <p>To localize strings for month names
 * use the values "Calendar.Month" in the resource localization e.g. "Calendar.Jan", "Calendar.Feb" etc...
 *
 * @author Iddo Ari, Shai Almog
 */
public class Calendar extends Container {

    private Label month;
    private Label year;
    private MonthView mv;
    private static final String[] MONTHS = new String[]{"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
    private static final String[] DAYS = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
    private static final String[] LABELS = {"Su", "M", "Tu", "W", "Th", "F", "Sa"};
    static final long MINUTE = 1000 * 60;
    static final long HOUR = MINUTE * 60;
    static final long DAY = HOUR * 24;
    static final long WEEK = DAY * 7;
    private EventDispatcher dispatcher = new EventDispatcher();
    private EventDispatcher dataChangeListeners = new EventDispatcher();
    private long[] dates = new long[42];
    private Button incrementMonth;
    private Button decrementMonth;

    /**
     * Creates a new instance of Calendar set to the given date based on time
     * since epoch (the java.util.Date convention)
     * 
     * @param time time since epoch
     */
    public Calendar(long time) {
        super(new BorderLayout());
        setUIID("Calendar");
        Container upper = new Container(new FlowLayout(Component.CENTER));
        incrementMonth = createIncrementButton();
        decrementMonth = createDecrementButton();
        mv = new MonthView(time);
        incrementMonth.addActionListener(mv);
        decrementMonth.addActionListener(mv);

        month = new Label(getLocalizedMonth(mv.getMonth()));
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTime(new java.util.Date(time));
        month.getStyle().setBgTransparency(0);
        int y = cal.get(java.util.Calendar.YEAR);
        year = new Label("" + y);
        year.getStyle().setBgTransparency(0);

        Container cnt = new Container(new BoxLayout(BoxLayout.X_AXIS));
        cnt.setRTL(false);
        cnt.addComponent(decrementMonth);

        Container dateCnt = new Container(new BoxLayout(BoxLayout.X_AXIS));
        dateCnt.setUIID("CalendarDate");
        dateCnt.addComponent(month);
        dateCnt.addComponent(year);
        cnt.addComponent(dateCnt);
        cnt.addComponent(incrementMonth);
        upper.addComponent(cnt);

        addComponent(BorderLayout.NORTH, upper);
        addComponent(BorderLayout.CENTER, mv);
    }

    /**
     * Constructs a calendar with the current date and time
     */
    public Calendar() {
        this(System.currentTimeMillis());
    }
    
    /**
     * Returns the time for the current calendar.
     * 
     * @return the time for the current calendar.
     */
    public long getSelectedDay() {
        return mv.getSelectedDay();
    }

    private String getLocalizedMonth(int i) {
        Hashtable t = UIManager.getInstance().getResourceBundle();
        String text = MONTHS[i];
        if (t != null) {
            Object o = t.get("Calendar." + text);
            if (o != null) {
                text = (String) o;
            }
        }
        return text;
    }

    void componentChanged() {
        java.util.Calendar cal = java.util.Calendar.getInstance();

        cal.set(java.util.Calendar.YEAR, mv.getYear());
        cal.set(java.util.Calendar.MONTH, mv.getMonth());
        cal.set(java.util.Calendar.DAY_OF_MONTH, mv.getDayOfMonth());
        month.setText(getLocalizedMonth(mv.getMonth()));
        year.setText("" + mv.getYear());
        month.getParent().revalidate();
    }

    /**
     * Return the date object matching the current selection
     * 
     * @return the date object matching the current selection
     */
    public Date getDate() {
        return new Date(mv.getSelectedDay());
    }

    /**
     * Sets the current date in the view
     * 
     * @param d new date
     */
    public void setDate(Date d) {
        mv.setSelectedDay(d.getTime());
        componentChanged();
    }

    /**
     * @inheritDoc
     */
    public void paint(Graphics g) {
        super.paint(g);
    }

    /**
     * Sets the selected style of the month view component within the calendar
     * 
     * @param s style for the month view 
     */
    public void setMonthViewSelectedStyle(Style s) {
        mv.setSelectedStyle(s);
    }

    /**
     * Sets the un selected style of the month view component within the calendar
     * 
     * @param s style for the month view 
     */
    public void setMonthViewUnSelectedStyle(Style s) {
        mv.setUnSelectedStyle(s);
    }

    /**
     * Gets the selected style of the month view component within the calendar
     * 
     * @return the style of the month view
     */
    public Style getMonthViewSelectedStyle() {
        return mv.getSelectedStyle();
    }

    /**
     * Gets the un selected style of the month view component within the calendar
     * 
     * @return the style of the month view
     */
    public Style getMonthViewUnSelectedStyle() {
        return mv.getUnselectedStyle();
    }

    /**
     * Fires when a change is made to the month view of this component
     * 
     * @param l listener to add
     */
    public void addActionListener(ActionListener l) {
        mv.addActionListener(l);
    }

    /**
     * Fires when a change is made to the month view of this component
     * 
     * @param l listener to remove
     */
    public void removeActionListener(ActionListener l) {
        mv.removeActionListener(l);
    }

    /**
     * Allows tracking selection changes in the calendar in real time
     * 
     * @param l listener to add
     */
    public void addDataChangeListener(DataChangedListener l) {
        mv.addDataChangeListener(l);
    }

    /**
     * Allows tracking selection changes in the calendar in real time
     * 
     * @param l listener to remove
     */
    public void removeDataChangeListener(DataChangedListener l) {
        mv.removeDataChangeListener(l);
    }

    /**
     * This method creates the Day Button Component for the Month View
     * 
     * @return a Button that corresponds to the Days Components
     */
    protected Button createDay() {
        Button day = new Button();
        day.setAlignment(CENTER);
        day.setUIID("CalendarDay");
        return day;
    }

    /**
     * This method creates the Day title Component for the Month View
     * 
     * @param day the relevant day values are 0-6 where 0 is sunday.
     * @return a Label that corresponds to the relevant Day
     */
    protected Label createDayTitle(int day) {
        String value = UIManager.getInstance().localize(DAYS[day], LABELS[day]);
        return new Label(value, "CalendarTitle");
    }

    /**
     * This method creates the increment month button
     * 
     * @return the Button that increase the Calendar months 
     */
    protected Button createIncrementButton() {
        Button btn = new Button(">>");
        btn.setUIID("CalendarNavigation");
        return btn;
    }

    /**
     * This method creates the decrement month button
     * 
     * @return the Button that decrease the Calendar months 
     */
    protected Button createDecrementButton() {
        Button btn = new Button("<<");
        btn.setUIID("CalendarNavigation");
        return btn;
    }

    class MonthView extends Container implements ActionListener, FocusListener {

        private long selectedDay;
        private Button[] buttons = new Button[42];

        public MonthView(long time) {
            super(new GridLayout(7, 7));
            setUIID("MonthView");
            for (int iter = 0; iter < DAYS.length; iter++) {
                addComponent(createDayTitle(iter));
            }
            for (int iter = 0; iter < buttons.length; iter++) {
                buttons[iter] = createDay();
                addComponent(buttons[iter]);
                if (iter <= 3) {
                    buttons[iter].setNextFocusUp(decrementMonth);
                } else if (iter <= 7) {
                    buttons[iter].setNextFocusUp(incrementMonth);
                }
                buttons[iter].addActionListener(this);
                buttons[iter].addFocusListener(this);

            }
            setSelectedDay(time);
        }

        protected void initComponent() {
            super.initComponent();
            focusOnSelected();
        }

        private void focusOnSelected() {
            for (int i = 0; i < dates.length; i++) {
                if (selectedDay == dates[i]) {
                    buttons[i].requestFocus();
                }
            }
        }

        public void setSelectedDay(long day) {
            repaint();
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.setTime(new Date(selectedDay));
            int yearOld = cal.get(java.util.Calendar.YEAR);
            int monthOld = cal.get(java.util.Calendar.MONTH);
            int dayOld = cal.get(java.util.Calendar.DAY_OF_MONTH);

            cal.setTime(new Date(day));
            int yearNew = cal.get(java.util.Calendar.YEAR);
            int monthNew = cal.get(java.util.Calendar.MONTH);
            int dayNew = cal.get(java.util.Calendar.DAY_OF_MONTH);

            if (yearNew != yearOld || monthNew != monthOld || dayNew != dayOld) {
                selectedDay = day;

                int month = cal.get(java.util.Calendar.MONTH);
                cal.set(java.util.Calendar.DAY_OF_MONTH, 1);
                long startDate = cal.getTime().getTime();
                int dow = cal.get(java.util.Calendar.DAY_OF_WEEK);
                cal.setTime(new Date(cal.getTime().getTime() - DAY));
                int lastDay = cal.get(java.util.Calendar.DAY_OF_MONTH);
                int i = 0;
                
                if(dow > java.util.Calendar.SUNDAY){
                    //last day of previous month

                    while (dow > java.util.Calendar.SUNDAY) {
                        cal.setTime(new Date(cal.getTime().getTime() - DAY));
                        dow = cal.get(java.util.Calendar.DAY_OF_WEEK);
                    }
                    int previousMonthSunday = cal.get(java.util.Calendar.DAY_OF_MONTH);
                    for (; i <= lastDay - previousMonthSunday; i++) {
                        buttons[i].setEnabled(false);
                        buttons[i].setText("" + (previousMonthSunday + i));
                    }
                }
                //last day of current month
                cal.set(java.util.Calendar.MONTH, (month + 1) % 12);
                cal.set(java.util.Calendar.DAY_OF_MONTH, 1);
                cal.setTime(new Date(cal.getTime().getTime() - DAY));

                lastDay = cal.get(java.util.Calendar.DAY_OF_MONTH);

                int j = i;
                for (; j < buttons.length && (j - i + 1) <= lastDay; j++) {
                    buttons[j].setEnabled(true);
                    buttons[j].setText("" + (j - i + 1));
                    dates[j] = startDate;
                    startDate += DAY;
                }
                int d = 1;
                for (; j < buttons.length; j++) {
                    buttons[j].setEnabled(false);
                    buttons[j].setText("" + d++);
                }
            }
        }

        public int getDayOfMonth() {
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.setTime(new Date(selectedDay));
            return cal.get(java.util.Calendar.DAY_OF_MONTH);
        }

        public int getMonth() {
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.setTime(new Date(selectedDay));
            return cal.get(java.util.Calendar.MONTH);
        }

        public void incrementMonth() {
            int month = getMonth();
            month++;
            int year = getYear();
            if (month > java.util.Calendar.DECEMBER) {
                month = java.util.Calendar.JANUARY;
                year++;
            }
            setMonth(year, month);
        }

        private long getSelectedDay() {
            return selectedDay;
        }

        private void setMonth(int year, int month) {
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.setTimeZone(TimeZone.getDefault());
            cal.set(java.util.Calendar.MONTH, month);
            cal.set(java.util.Calendar.DAY_OF_MONTH, 1);
            cal.set(java.util.Calendar.YEAR, year);

            Date date = cal.getTime();
            long d = date.getTime();

            // if this is past the last day of the month (e.g. going from January 31st
            // to Febuary) we need to decrement the day until the month is correct
            while (cal.get(java.util.Calendar.MONTH) != month) {
                d -= DAY;
                cal.setTime(new Date(d));
            }
            setSelectedDay(d);
        }

        public void decrementMonth() {
            int month = getMonth();
            month--;
            int year = getYear();
            if (month < java.util.Calendar.JANUARY) {
                month = java.util.Calendar.DECEMBER;
                year--;
            }
            setMonth(year, month);
        }

        public int getYear() {
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.setTime(new Date(selectedDay));
            return cal.get(java.util.Calendar.YEAR);
        }

        public void addActionListener(ActionListener l) {
            dispatcher.addListener(l);
        }

        public void removeActionListener(ActionListener l) {
            dispatcher.removeListener(l);
        }

        /**
         * Allows tracking selection changes in the calendar in real time
         * 
         * @param l listener to add
         */
        public void addDataChangeListener(DataChangedListener l) {
            dataChangeListeners.addListener(l);
        }

        /**
         * Allows tracking selection changes in the calendar in real time
         * 
         * @param l listener to remove
         */
        public void removeDataChangeListener(DataChangedListener l) {
            dataChangeListeners.removeListener(l);
        }

        protected void fireActionEvent() {
            componentChanged();
            super.fireActionEvent();
            dispatcher.fireActionEvent(new ActionEvent(Calendar.this));
        }

        public void actionPerformed(ActionEvent evt) {
            Object src = evt.getSource();
            if (src == incrementMonth) {
                incrementMonth();
                dataChangeListeners.fireDataChangeEvent(-1, DataChangedListener.CHANGED);
                componentChanged();
                return;
            }
            if (src == decrementMonth) {
                decrementMonth();
                dataChangeListeners.fireDataChangeEvent(-1, DataChangedListener.CHANGED);
                componentChanged();
                return;
            }
            for (int iter = 0; iter < buttons.length; iter++) {
                if (src == buttons[iter]) {
                    selectedDay = dates[iter];
                    fireActionEvent();
                    if (!getComponentForm().isSingleFocusMode()) {
                        setHandlesInput(false);
                    }
                    return;
                }
            }
        }

        public void focusGained(Component cmp) {
            for (int iter = 0; iter < buttons.length; iter++) {
                if (cmp == buttons[iter]) {
                    selectedDay = dates[iter];
                    dataChangeListeners.fireDataChangeEvent(iter, DataChangedListener.CHANGED);
                    return;
                }
            }
        }

        public void focusLost(Component cmp) {
        }
    }
}
