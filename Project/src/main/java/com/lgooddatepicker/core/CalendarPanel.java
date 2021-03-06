package com.lgooddatepicker.core;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;
import java.text.DateFormatSymbols;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import com.lgooddatepicker.policies.HighlightPolicy;
import com.lgooddatepicker.policies.VetoPolicy;
import com.lgooddatepicker.utilities.TopWindowMovementListener;

/**
 * CalendarPanel, This implements the calendar panel which is displayed on the screen when the user
 * clicks the "toggle calendar" button on a date picker. The CalendarPanel class draws, controls,
 * and reacts to events, for the various GUI components of the date picker calendar.
 *
 * This class should not be instantiated directly, it is only intended to be used by the DatePicker
 * class.
 *
 * Life cycle: Each time that the user clicks the toggle calendar button on a date picker, a new
 * CalendarPanel instance is created and displayed, inside of a new instance of CustomPopup. The
 * calendar panel instance is closed and disposed each time that the date picker popup is closed.
 */
public class CalendarPanel extends JPanel {

    /**
     * dateLabels, This holds a list of all the date labels in the calendar, including ones that
     * currently have dates or ones that are blank. This should always have exactly 42 labels. Date
     * labels are reused when the currently displayed month or year is changed.
     */
    private ArrayList<JLabel> dateLabels;

    /**
     * displayedSelectedDate, This stores a date that will be highlighted in the calendar as the
     * "selected date", or it holds null if no date has been selected. This date is copied from the
     * date picker when the calendar is opened. This should not be confused with the "lastValidDate"
     * of a date picker object. This variable holds the selected date only for display purposes, not
     * for data storage purposes.
     */
    private LocalDate displayedSelectedDate = null;

    /**
     * displayedYearMonth, This stores the currently displayed year and month. This defaults to the
     * current year and month.
     */
    private YearMonth displayedYearMonth = YearMonth.now();

    /**
     * parentDatePicker, This holds a reference to the date picker that is the parent of this
     * calendar panel. A calendar panel always has a parent date picker. This will never be null
     * until the calendar is closed and disposed..
     */
    private DatePicker parentDatePicker;

    /**
     * weekdayLabels, This holds a list of all the weekday labels in the calendar. This should
     * always have exactly 7 labels. Weekday labels are reused when the currently displayed month or
     * year is changed.
     */
    private ArrayList<JLabel> weekdayLabels;

    /**
     * JFormDesigner GUI components, These variables are automatically generated by JFormDesigner.
     * This section should not be modified by hand, but only modified from within the JFormDesigner
     * program.
     */
    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	private JPanel headerControlsPanel;
	private JButton buttonPreviousYear;
	private JButton buttonPreviousMonth;
	private JPanel monthAndYearPanel;
	private JLabel labelMonthIndicator;
	private JLabel labelYearIndicator;
	private JButton buttonNextMonth;
	private JButton buttonNextYear;
	private JPanel weekDaysPanel;
	private JPanel datesPanel;
	private JPanel footerPanel;
	private JLabel labelSetDateToToday;
	private JLabel labelClearDate;
    // JFormDesigner - End of variables declaration  //GEN-END:variables

    /**
     * Constructor, This creates a calendar panel and stores the parent date picker.
     */
    CalendarPanel(DatePicker parentDatePicker) {
        this.parentDatePicker = parentDatePicker;
        // Initialize the components.
        initComponents();

        // Create a TopWindowMovementListener.
        Window topWindow = SwingUtilities.getWindowAncestor(parentDatePicker);
        TopWindowMovementListener.addNewTopWindowMovementListener(parentDatePicker, topWindow);

        // Generate and add the date labels and weekday labels.
        addDateLabels();
        addWeekdayLabels();

        // Shrink the buttons for previous and next year and month.
        buttonPreviousYear.setMargin(new java.awt.Insets(1, 2, 1, 2));
        buttonNextYear.setMargin(new java.awt.Insets(1, 2, 1, 2));
        buttonPreviousMonth.setMargin(new java.awt.Insets(1, 2, 1, 2));
        buttonNextMonth.setMargin(new java.awt.Insets(1, 2, 1, 2));

        // Set the size of the month and year panel to be big enough to hold the largest month text.
        setSizeOfMonthYearPanel();

        // Set the calendar to show the current month and year by default.
        CalendarPanel.this.drawCalendar(YearMonth.now());

        // Close the calendar when the Escape key is pressed.
        String cancelName = "cancel";
        InputMap inputMap = this.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), cancelName);
        ActionMap actionMap = this.getActionMap();
        actionMap.put(cancelName, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                parentDatePicker.closePopup();
            }
        });
    }

    /**
     * addDateLabels, This adds a set of 42 date labels to the calendar, and ties each of those
     * labels to a mouse click event handler. The date labels are reused any time that the calendar
     * is redrawn.
     */
    private void addDateLabels() {
        dateLabels = new ArrayList<>();
        for (int i = 0; i < 42; ++i) {
            int dateLabelColumnX = ((i % 7) + 1);
            int dateLabelRowY = ((i / 7) + 2);
            JLabel dateLabel = new JLabel();
            dateLabel.setHorizontalAlignment(SwingConstants.CENTER);
            dateLabel.setVerticalAlignment(SwingConstants.CENTER);
            dateLabel.setBackground(Color.white);
            dateLabel.setBorder(null);
            dateLabel.setOpaque(true);
            dateLabel.setText("" + i);
            datesPanel.add(dateLabel, CC.xy(dateLabelColumnX, dateLabelRowY));
            dateLabels.add(dateLabel);
            // Add a mouse click listener for every date label, even the blank ones.
            dateLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    dateLabelMouseClicked(e);
                }
            });
        }
    }

    /**
     * addWeekdayLabels, This adds a set of 7 weekday labels to the calendar panel. The text of
     * these labels is set with locale sensitive weekday names each time that the calendar is
     * redrawn.
     */
    private void addWeekdayLabels() {
        weekdayLabels = new ArrayList<>();
        for (int i = 0; i < 7; ++i) {
            int weekdayLabelColumnX = (i + 1);
            int weekdayLabelRowY = 1;
            JLabel weekdayLabel = new JLabel();
            weekdayLabel.setHorizontalAlignment(SwingConstants.CENTER);
            weekdayLabel.setVerticalAlignment(SwingConstants.CENTER);
            weekdayLabel.setBackground(new Color(184, 207, 229));
            weekdayLabel.setOpaque(true);
            weekdayLabel.setText("wd" + i);
            weekDaysPanel.add(weekdayLabel, CC.xy(weekdayLabelColumnX, weekdayLabelRowY));
            weekdayLabels.add(weekdayLabel);
        }
    }

    /**
     * buttonNextMonthActionPerformed, This event is called when the next month button is pressed.
     * This sets the YearMonth of the calendar to the next month, and redraws the calendar.
     */
    private void buttonNextMonthActionPerformed(ActionEvent e) {
        drawCalendar(displayedYearMonth.plusMonths(1));
    }

    /**
     * buttonNextYearActionPerformed, This event is called when the next year button is pressed.
     * This sets the YearMonth of the calendar to the next year, and redraws the calendar.
     */
    private void buttonNextYearActionPerformed(ActionEvent e) {
        drawCalendar(displayedYearMonth.plusYears(1));
    }

    /**
     * buttonPreviousMonthActionPerformed, This event is called when the previous month button is
     * pressed. This sets the YearMonth of the calendar to the previous month, and redraws the
     * calendar.
     */
    private void buttonPreviousMonthActionPerformed(ActionEvent e) {
        drawCalendar(displayedYearMonth.minusMonths(1));
    }

    /**
     * buttonPreviousYearActionPerformed, This event is called when the previous year button is
     * pressed. This sets the YearMonth of the calendar to the previous year, and redraws the
     * calendar.
     */
    private void buttonPreviousYearActionPerformed(ActionEvent e) {
        drawCalendar(displayedYearMonth.minusYears(1));
    }

    /**
     * clearParent, This is called to remove the parent date picker reference from the calendar
     * panel. This is called at the same time that the parent date picker wants to close and dispose
     * its popup calendar panel.
     */
    void clearParent() {
        parentDatePicker = null;
    }

    /**
     * dateLabelMouseClicked, This event is called any time that the user clicks on a date label in
     * the calendar. This sets the date picker to the selected date, and closes the calendar panel.
     */
    private void dateLabelMouseClicked(MouseEvent e) {
        // Get the label that was clicked.
        JLabel label = (JLabel) e.getSource();
        // If the label is empty, do nothing and return.
        String labelText = label.getText();
        if ("".equals(labelText)) {
            return;
        }
        // We have a label with a specific date, so set the date and close the calendar.
        int dayOfMonth = Integer.parseInt(labelText);
        LocalDate clickedDate = LocalDate.of(
                displayedYearMonth.getYear(), displayedYearMonth.getMonth(), dayOfMonth);
        userSelectedADate(clickedDate);
    }

    /**
     * drawCalendar, This is called whenever the calendar needs to be drawn. This takes a year and a
     * month to indicate which month should be drawn in the calendar.
     */
    private void drawCalendar(int year, Month month) {
        drawCalendar(YearMonth.of(year, month));
    }

    /**
     * drawCalendar, This is called whenever the calendar needs to be drawn. This takes a year and a
     * month to indicate which month should be drawn in the calendar.
     */
    final void drawCalendar(YearMonth yearMonth) {
        // Save the displayed yearMonth.
        this.displayedYearMonth = yearMonth;
        // Get the displayed month and year.
        Month displayedMonth = yearMonth.getMonth();
        int displayedYear = yearMonth.getYear();
        // Get an instance of the calendar symbols for the current locale.
        DateFormatSymbols symbols = DateFormatSymbols.getInstance(getSettings().pickerLocale);
        // Get the days of the week in the local language.
        String localShortDaysOfWeek[] = symbols.getShortWeekdays();
        // Get the full name of the month in the current locale.
        String localizedFullMonth = symbols.getMonths()[displayedMonth.getValue() - 1];
        // Get the first day of the month, and the first day of week.
        LocalDate firstDayOfMonth = LocalDate.of(displayedYear, displayedMonth, 1);
        DayOfWeek firstDayOfWeekOfMonth = firstDayOfMonth.getDayOfWeek();
        // Get the last day of the month.
        int lastDateOfMonth = getLastDayOfMonth(displayedYearMonth);
        // Find out if we have a selected date that is inside the currently displayed month.
        boolean selectedDateIsInDisplayedMonth = (displayedSelectedDate != null)
                && (displayedSelectedDate.getYear() == displayedYear)
                && (displayedSelectedDate.getMonth() == displayedMonth);
        // Set the month and the year labels.
        labelMonthIndicator.setText(localizedFullMonth);
        labelYearIndicator.setText("" + displayedYear);
        // Set the days of the week labels, and create an array to represent the weekday positions.
        ArrayList<DayOfWeek> daysOfWeekAsDisplayed = new ArrayList<>();
        int isoFirstDayOfWeekValue = getSettings().firstDayOfWeek.getValue();
        int isoLastDayOfWeekOverflowed = isoFirstDayOfWeekValue + 6;
        int weekdayLabelArrayIndex = 0;
        for (int dayOfWeek = isoFirstDayOfWeekValue; dayOfWeek <= isoLastDayOfWeekOverflowed; dayOfWeek++) {
            int localShortDaysOfWeekArrayIndex = (dayOfWeek % 7) + 1;
            int isoDayOfWeek = (dayOfWeek > 7) ? (dayOfWeek - 7) : dayOfWeek;
            DayOfWeek currentDayOfWeek = DayOfWeek.of(isoDayOfWeek);
            daysOfWeekAsDisplayed.add(currentDayOfWeek);
            weekdayLabels.get(weekdayLabelArrayIndex).setText(localShortDaysOfWeek[localShortDaysOfWeekArrayIndex]);
            ++weekdayLabelArrayIndex;
        }
        // Set the dates of the month labels.
        // Also save the label for the selected date, if one is present in the current month.
        boolean insideValidRange = false;
        int dayOfMonth = 1;
        JLabel selectedDateLabel = null;
        for (int dateLabelArrayIndex = 0; dateLabelArrayIndex < dateLabels.size(); ++dateLabelArrayIndex) {
            // Get the current date label.
            JLabel dateLabel = dateLabels.get(dateLabelArrayIndex);
            // Reset the state of every label to a default state.
            dateLabel.setBackground(Color.white);
            dateLabel.setForeground(Color.black);
            dateLabel.setBorder(null);
            dateLabel.setEnabled(true);
            dateLabel.setToolTipText(null);
            // Calculate the index to use on the daysOfWeekAsDisplayed array.
            int daysOfWeekAsDisplayedArrayIndex = dateLabelArrayIndex % 7;
            // Check to see if we are inside the valid range for days of this month.
            if (daysOfWeekAsDisplayed.get(daysOfWeekAsDisplayedArrayIndex) == firstDayOfWeekOfMonth
                    && dateLabelArrayIndex < 7) {
                insideValidRange = true;
            }
            if (dayOfMonth > lastDateOfMonth) {
                insideValidRange = false;
            }
            // While we are inside the valid range, set the date labels with the day of the month.
            if (insideValidRange) {
                // Get a local date object for the current date.
                LocalDate currentDate = LocalDate.of(displayedYear, displayedMonth, dayOfMonth);
                VetoPolicy vetoPolicy = getSettings().vetoPolicy;
                HighlightPolicy highlightPolicy = getSettings().highlightPolicy;
                boolean dateIsVetoed = (vetoPolicy != null)
                        && (vetoPolicy.isDateVetoed(currentDate));
                String highlightStringOrNull = null;
                if (highlightPolicy != null) {
                    highlightStringOrNull = highlightPolicy.getHighlightStringOrNull(currentDate);
                }
                if (dateIsVetoed) {
                    dateLabel.setEnabled(false);
                    dateLabel.setBackground(getSettings().backgroundColorVetoed);
                }
                if ((!dateIsVetoed) && (highlightStringOrNull != null)) {
                    dateLabel.setBackground(getSettings().backgroundColorHighlighted);
                    if (!highlightStringOrNull.isEmpty()) {
                        dateLabel.setToolTipText(highlightStringOrNull);
                    }
                }
                // If needed, save the label for the selected date.
                if (selectedDateIsInDisplayedMonth && displayedSelectedDate != null
                        && displayedSelectedDate.getDayOfMonth() == dayOfMonth) {
                    selectedDateLabel = dateLabel;
                }
                // Set the text for the current date.
                dateLabel.setText("" + dayOfMonth);
                ++dayOfMonth;
            } else {
                // We are not inside the valid range, so set this label to an empty string.
                dateLabel.setText("");
            }
        }
        // If needed, change the color of the selected date.
        if (selectedDateLabel != null) {
            selectedDateLabel.setBackground(new Color(163, 184, 204));
            selectedDateLabel.setBorder(new LineBorder(new Color(99, 130, 191)));
        }
        // Set the label for the today button.
        String todayDateString = getSettings().todayFormatter.format(LocalDate.now());
        String todayLabel = getSettings().todayTranslation + ":  " + todayDateString;
        labelSetDateToToday.setText(todayLabel);
        // If today is vetoed, disable the today button.
        VetoPolicy vetoPolicy = getSettings().vetoPolicy;
        boolean todayIsVetoed = (vetoPolicy != null)
                && (vetoPolicy.isDateVetoed(LocalDate.now()));
        labelSetDateToToday.setEnabled(!todayIsVetoed);
        // Set the label for the clear button.
        labelClearDate.setText(getSettings().clearTranslation);
    }

    /**
     * getLastDayOfMonth, This returns the last day of the month for the specified year and month.
     *
     * Implementation notes: As of this writing, the below implementation is verified to work
     * correctly for negative years, as those years are to defined in the iso 8601 your format that
     * is used by java.time.YearMonth. This functionality can be tested by by checking to see if to
     * see if the year "-0004" is correctly displayed as a leap year. Leap years have 29 days in
     * February. There should be 29 days in the month of "February 1, -0004".
     */
    private int getLastDayOfMonth(YearMonth yearMonth) {
        LocalDate firstDayOfMonth = LocalDate.of(yearMonth.getYear(), yearMonth.getMonth(), 1);
        int lastDayOfMonth = firstDayOfMonth.lengthOfMonth();
        return lastDayOfMonth;
    }

    /**
     * getMonthOrYearMenuLocation, This calculates the position should be used to set the location
     * of the month or the year popup menus, relative to their source labels. These menus are used
     * to change the current month or current year from within the calendar panel.
     */
    private Point getMonthOrYearMenuLocation(JLabel sourceLabel, JPopupMenu filledPopupMenu) {
        Rectangle labelBounds = sourceLabel.getBounds();
        int menuHeight = filledPopupMenu.getPreferredSize().height;
        int popupX = labelBounds.x + labelBounds.width + 1;
        int popupY = labelBounds.y + (labelBounds.height / 2) - (menuHeight / 2);
        return new Point(popupX, popupY);
    }

    /**
     * getSettings, This is a convenience function to retrieve the date picker settings from the
     * parent date picker.
     */
    private DatePickerSettings getSettings() {
        return parentDatePicker.getSettings();
    }

    /**
     * labelClearDateMouseClicked, This event is called when the "Clear" label is clicked in a date
     * picker. This sets the date picker date to an empty date. (This sets the last valid date to
     * null.)
     */
    private void labelClearDateMouseClicked(MouseEvent e) {
        userSelectedADate(null);
    }

    /**
     * labelIndicatorMouseEntered, This event is called when the user move the mouse inside a
     * monitored label. This is used to generate mouse over effects for the calendar panel.
     */
    private void labelIndicatorMouseEntered(MouseEvent e) {
        JLabel label = ((JLabel) e.getSource());
        if (label == labelSetDateToToday) {
            VetoPolicy vetoPolicy = getSettings().vetoPolicy;
            boolean todayIsVetoed = (vetoPolicy != null)
                    && (vetoPolicy.isDateVetoed(LocalDate.now()));
            if (todayIsVetoed) {
                return;
            }
        }
        label.setBackground(new Color(184, 207, 229));
        label.setBorder(new CompoundBorder(
                new LineBorder(Color.GRAY), new EmptyBorder(0, 2, 0, 2)));
    }

    /**
     * labelIndicatorMouseExited, This event is called when the user move the mouse outside of a
     * monitored label. This is used to generate mouse over effects for the calendar panel.
     */
    private void labelIndicatorMouseExited(MouseEvent e) {
        JLabel label = ((JLabel) e.getSource());
        label.setBackground(null);
        label.setBorder(new CompoundBorder(
                new EmptyBorder(1, 1, 1, 1), new EmptyBorder(0, 2, 0, 2)));
    }

    /**
     * labelMonthIndicatorMousePressed, This event is called any time that the user clicks on the
     * month display label in the calendar. This opens a menu that the user can use to select a new
     * month in the same year.
     */
    private void labelMonthIndicatorMousePressed(MouseEvent e) {
        JPopupMenu monthPopupMenu = new JPopupMenu();
        DateFormatSymbols symbols = DateFormatSymbols.getInstance(getSettings().pickerLocale);
        String[] allLocalMonths = symbols.getMonths();
        for (int i = 0; i < allLocalMonths.length; ++i) {
            final String localMonth = allLocalMonths[i];
            final int localMonthZeroBasedIndexTemp = i;
            if (!localMonth.isEmpty()) {
                monthPopupMenu.add(new JMenuItem(new AbstractAction(localMonth) {
                    int localMonthZeroBasedIndex = localMonthZeroBasedIndexTemp;

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        drawCalendar(displayedYearMonth.getYear(),
                                Month.of(localMonthZeroBasedIndex + 1));
                    }
                }));
            }
        }
        Point menuLocation = getMonthOrYearMenuLocation(labelMonthIndicator, monthPopupMenu);
        monthPopupMenu.show(monthAndYearPanel, menuLocation.x, menuLocation.y);
    }

    /**
     * labelSetDateToTodayMouseClicked, This event is called when the "Today" label is clicked in a
     * date picker. This sets the date picker date to today.
     */
    private void labelSetDateToTodayMouseClicked(MouseEvent e) {
        userSelectedADate(LocalDate.now());
    }

    /**
     * labelYearIndicatorMousePressed, This event is called any time that the user clicks on the
     * year display label in the calendar. This opens a menu that the user can use to select a new
     * year within a chosen range of the previously displayed year.
     */
    private void labelYearIndicatorMousePressed(MouseEvent e) {
        int firstYearDifference = -11;
        int lastYearDifference = +11;
        JPopupMenu yearPopupMenu = new JPopupMenu();
        for (int yearDifference = firstYearDifference; yearDifference <= lastYearDifference;
                ++yearDifference) {
            // No special processing is required for the BC to AD transition in the 
            // ISO 8601 calendar system. Year zero does exist in this system.
            YearMonth choiceYearMonth = displayedYearMonth.plusYears(yearDifference);
            String choiceYearMonthString = "" + choiceYearMonth.getYear();
            yearPopupMenu.add(new JMenuItem(new AbstractAction(choiceYearMonthString) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String chosenMenuText = ((JMenuItem) e.getSource()).getText();
                    int chosenYear = Integer.parseInt(chosenMenuText);
                    drawCalendar(chosenYear, displayedYearMonth.getMonth());
                }
            }));
        }
        Point menuLocation = getMonthOrYearMenuLocation(labelYearIndicator, yearPopupMenu);
        yearPopupMenu.show(monthAndYearPanel, menuLocation.x, menuLocation.y);
    }

    /**
     * setDisplayedSelectedDate, This sets the date that will be marked as "selected" in the
     * calendar. Note that this function does -not- change the displayed YearMonth.
     */
    void setDisplayedSelectedDate(LocalDate selectedDate) {
        this.displayedSelectedDate = selectedDate;
    }

    /**
     * setSizeOfMonthYearPanel, This sets the size of the panel at the top of the calendar that
     * holds the month and the year label. The size is calculated from the largest month name (in
     * pixels), that exists in locale and language that is being used by the date picker.
     */
    private void setSizeOfMonthYearPanel() {
        // Get the font metrics object.
        Font font = labelMonthIndicator.getFont();
        Canvas canvas = new Canvas();
        FontMetrics metrics = canvas.getFontMetrics(font);
        // Get the height of a line of text in this font.
        int height = metrics.getHeight();
        // Get the length of the longest translated month string (in pixels).
        DateFormatSymbols symbols = DateFormatSymbols.getInstance(getSettings().pickerLocale);
        String[] allLocalMonths = symbols.getMonths();
        int longestMonthPixels = 0;
        for (String month : allLocalMonths) {
            int monthPixels = metrics.stringWidth(month);
            longestMonthPixels = (monthPixels > longestMonthPixels) ? monthPixels : longestMonthPixels;
        }
        int yearPixels = metrics.stringWidth("_2000");
        // Calculate the size of a box to hold the text with some padding.
        Dimension size = new Dimension(longestMonthPixels + yearPixels + 12, height + 2);
        // Set the monthAndYearPanel to the appropriate constant size.
        monthAndYearPanel.setMinimumSize(size);
        monthAndYearPanel.setMaximumSize(size);
        monthAndYearPanel.setPreferredSize(size);
        // Redraw the panel.
        this.doLayout();
        this.validate();
    }

    /**
     * userSelectedADate, This is called any time that the user makes a date selection on the
     * calendar panel, including choosing to clear the date. This will save the selected date and
     * close the calendar. The only time this will not be called during an exit event, is if the
     * user left focus of the component or pressed escape to cancel choosing a new date.
     */
    private void userSelectedADate(LocalDate selectedDate) {
        // If a date was selected and the date is vetoed, do nothing.
        if (selectedDate != null) {
            VetoPolicy vetoPolicy = getSettings().vetoPolicy;
            if (vetoPolicy != null && vetoPolicy.isDateVetoed(selectedDate)) {
                return;
            }
        }
        // Save the selected date.
        this.displayedSelectedDate = selectedDate;
        // Save the selected year and month, in case it is needed later.
        if (selectedDate != null) {
            YearMonth selectedDateYearMonth = YearMonth.from(selectedDate);
            this.displayedYearMonth = selectedDateYearMonth;
        } else {
            // The selected date was cleared, so set the displayed month and year to today's values.
            this.displayedYearMonth = YearMonth.now();
        }
        // We close the popup after the user selects a date.
        if (parentDatePicker != null) {
            parentDatePicker.setDate(selectedDate);
            parentDatePicker.closePopup();
        }
    }

    /**
     * initComponents, This initializes the GUI components in the calendar panel. This function is
     * automatically generated by JFormDesigner. This function should not be modified by hand, it
     * should only be modified from within JFormDesigner.
     */
    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		headerControlsPanel = new JPanel();
		buttonPreviousYear = new JButton();
		buttonPreviousMonth = new JButton();
		monthAndYearPanel = new JPanel();
		labelMonthIndicator = new JLabel();
		labelYearIndicator = new JLabel();
		buttonNextMonth = new JButton();
		buttonNextYear = new JButton();
		weekDaysPanel = new JPanel();
		datesPanel = new JPanel();
		footerPanel = new JPanel();
		labelSetDateToToday = new JLabel();
		labelClearDate = new JLabel();

		//======== this ========
		setLayout(new FormLayout(
			"0dlu, default, 0dlu",
			"0dlu, fill:default, $lgap, default, fill:default, $lgap, fill:default, 0dlu"));
		((FormLayout)getLayout()).setRowGroups(new int[][] {{2, 7}});

		//======== headerControlsPanel ========
		{
			headerControlsPanel.setLayout(new FormLayout(
				"2*(min), default, 2*(min)",
				"fill:default"));
			((FormLayout)headerControlsPanel.getLayout()).setColumnGroups(new int[][] {{1, 2, 4, 5}});

			//---- buttonPreviousYear ----
			buttonPreviousYear.setText("<<");
			buttonPreviousYear.setFocusable(false);
			buttonPreviousYear.setFocusPainted(false);
			buttonPreviousYear.addActionListener(e -> buttonPreviousYearActionPerformed(e));
			headerControlsPanel.add(buttonPreviousYear, CC.xy(1, 1));

			//---- buttonPreviousMonth ----
			buttonPreviousMonth.setText("<");
			buttonPreviousMonth.setFocusable(false);
			buttonPreviousMonth.setFocusPainted(false);
			buttonPreviousMonth.addActionListener(e -> buttonPreviousMonthActionPerformed(e));
			headerControlsPanel.add(buttonPreviousMonth, CC.xy(2, 1));

			//======== monthAndYearPanel ========
			{
				monthAndYearPanel.setLayout(new FormLayout(
					"default:grow, default, 1px, default, default:grow",
					"fill:default:grow"));

				//---- labelMonthIndicator ----
				labelMonthIndicator.setText("September");
				labelMonthIndicator.setHorizontalAlignment(SwingConstants.RIGHT);
				labelMonthIndicator.setOpaque(true);
				labelMonthIndicator.setBorder(new CompoundBorder(
					new EmptyBorder(1, 1, 1, 1),
					new EmptyBorder(0, 2, 0, 2)));
				labelMonthIndicator.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseEntered(MouseEvent e) {
						labelIndicatorMouseEntered(e);
					}
					@Override
					public void mouseExited(MouseEvent e) {
						labelIndicatorMouseExited(e);
					}
					@Override
					public void mousePressed(MouseEvent e) {
						labelMonthIndicatorMousePressed(e);
					}
				});
				monthAndYearPanel.add(labelMonthIndicator, CC.xy(2, 1));

				//---- labelYearIndicator ----
				labelYearIndicator.setText("2100");
				labelYearIndicator.setOpaque(true);
				labelYearIndicator.setBorder(new CompoundBorder(
					new EmptyBorder(1, 1, 1, 1),
					new EmptyBorder(0, 2, 0, 2)));
				labelYearIndicator.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseEntered(MouseEvent e) {
						labelIndicatorMouseEntered(e);
					}
					@Override
					public void mouseExited(MouseEvent e) {
						labelIndicatorMouseExited(e);
					}
					@Override
					public void mousePressed(MouseEvent e) {
						labelYearIndicatorMousePressed(e);
					}
				});
				monthAndYearPanel.add(labelYearIndicator, CC.xy(4, 1));
			}
			headerControlsPanel.add(monthAndYearPanel, CC.xy(3, 1));

			//---- buttonNextMonth ----
			buttonNextMonth.setText(">");
			buttonNextMonth.setFocusable(false);
			buttonNextMonth.setFocusPainted(false);
			buttonNextMonth.addActionListener(e -> buttonNextMonthActionPerformed(e));
			headerControlsPanel.add(buttonNextMonth, CC.xy(4, 1));

			//---- buttonNextYear ----
			buttonNextYear.setText(">>");
			buttonNextYear.setMargin(new Insets(0, 0, 0, 0));
			buttonNextYear.setFocusable(false);
			buttonNextYear.setFocusPainted(false);
			buttonNextYear.addActionListener(e -> buttonNextYearActionPerformed(e));
			headerControlsPanel.add(buttonNextYear, CC.xy(5, 1));
		}
		add(headerControlsPanel, CC.xy(2, 2));

		//======== weekDaysPanel ========
		{
			weekDaysPanel.setBorder(null);
			weekDaysPanel.setLayout(new FormLayout(
				"[27px,default]:grow, 6*(default:grow)",
				"fill:[22px,default]"));
			((FormLayout)weekDaysPanel.getLayout()).setColumnGroups(new int[][] {{1, 2, 3, 4, 5, 6, 7}});
		}
		add(weekDaysPanel, CC.xy(2, 4));

		//======== datesPanel ========
		{
			datesPanel.setBorder(new LineBorder(new Color(99, 130, 191)));
			datesPanel.setBackground(Color.white);
			datesPanel.setLayout(new FormLayout(
				"7*(pref:grow)",
				"2px, 6*(fill:[18px,default]:grow), 3px"));
			((FormLayout)datesPanel.getLayout()).setColumnGroups(new int[][] {{1, 2, 3, 4, 5, 6, 7}});
			((FormLayout)datesPanel.getLayout()).setRowGroups(new int[][] {{2, 3, 4, 5, 6, 7}});
		}
		add(datesPanel, CC.xy(2, 5));

		//======== footerPanel ========
		{
			footerPanel.setLayout(new FormLayout(
				"$rgap, default, default:grow, default, $rgap",
				"fill:default:grow"));

			//---- labelSetDateToToday ----
			labelSetDateToToday.setText("Today: Feb 12, 2016");
			labelSetDateToToday.setHorizontalAlignment(SwingConstants.CENTER);
			labelSetDateToToday.setOpaque(true);
			labelSetDateToToday.setBorder(new CompoundBorder(
				new EmptyBorder(1, 1, 1, 1),
				new EmptyBorder(0, 2, 0, 2)));
			labelSetDateToToday.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					labelSetDateToTodayMouseClicked(e);
				}
				@Override
				public void mouseEntered(MouseEvent e) {
					labelIndicatorMouseEntered(e);
				}
				@Override
				public void mouseExited(MouseEvent e) {
					labelIndicatorMouseExited(e);
				}
			});
			footerPanel.add(labelSetDateToToday, CC.xy(2, 1));

			//---- labelClearDate ----
			labelClearDate.setText("Clear");
			labelClearDate.setOpaque(true);
			labelClearDate.setBorder(new CompoundBorder(
				new EmptyBorder(1, 1, 1, 1),
				new EmptyBorder(0, 2, 0, 2)));
			labelClearDate.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					labelClearDateMouseClicked(e);
				}
				@Override
				public void mouseEntered(MouseEvent e) {
					labelIndicatorMouseEntered(e);
				}
				@Override
				public void mouseExited(MouseEvent e) {
					labelIndicatorMouseExited(e);
				}
			});
			footerPanel.add(labelClearDate, CC.xy(4, 1));
		}
		add(footerPanel, CC.xy(2, 7));
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

}
