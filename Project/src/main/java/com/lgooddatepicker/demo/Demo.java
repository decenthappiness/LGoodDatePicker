package com.lgooddatepicker.demo;

import com.lgooddatepicker.core.DatePicker;
import javax.swing.JButton;
import javax.swing.JFrame;
import com.jgoodies.forms.factories.CC;
import java.awt.event.ActionEvent;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.util.Locale;
import com.lgooddatepicker.core.DatePickerSettings;
import com.lgooddatepicker.policies.HighlightPolicy;
import com.lgooddatepicker.policies.VetoPolicy;

/**
 * Demo, This class contains a demonstration of various features of the DatePicker class.
 *
 * Optional features: Most of the DatePicker features that are shown in this demo are optional. The
 * simplest usage only requires creating a date picker instance and adding it to a panel or window.
 * The selected date can then be retrieved with the function datePicker.getDateOrNull().
 *
 * DatePicker Basic Usage Example:
 * <pre>
 * // Create a new date picker.
 * DatePicker datePicker = new DatePicker();
 *
 * // Add the date picker to a panel. (Or to another window container).
 * JPanel panel = new JPanel();
 * panel.add(datePicker);
 *
 * // Get the selected date.
 * LocalDate date = datePicker.getDateOrNull();
 * </pre>
 *
 * Running the demo: This is a runnable demonstration. To run the demo, click "run file" (or the
 * equivalent command) for the Demo class in your IDE.
 */
public class Demo {

    // This holds our main frame.
    static JFrame frame;
    // This holds our display panel.
    static DemoPanel panel;
    // These hold date pickers.
    static DatePicker datePicker1;
    static DatePicker datePicker2;
    static DatePicker datePicker3;
    static DatePicker datePicker4;

    /**
     * main, The application entry point.
     */
    public static void main(String[] args) {
        // Create a frame, a panel, and our demo buttons.
        frame = new JFrame();
        frame.setTitle("LGoodDatePicker Demo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        panel = new DemoPanel();
        frame.getContentPane().add(panel);
        createDemoButtons();

        // Create two date pickers.
        datePicker1 = new DatePicker();
        panel.add(datePicker1, CC.xy(4, 2));
        datePicker2 = new DatePicker();
        panel.add(datePicker2, CC.xy(4, 4));

        // Create a date picker with some customized settings.
        datePicker3 = createCustomizedDatePicker();
        panel.add(datePicker3, CC.xy(4, 6));

        // Create a date picker with a different locale (Russian).
        DatePickerSettings settings = new DatePickerSettings(new Locale("ru"));
        datePicker4 = new DatePicker(settings);
        panel.add(datePicker4, CC.xy(4, 8));

        // Display the frame.
        frame.pack();
        frame.validate();
        frame.setSize(980, 800);
        frame.setLocation(400, 200);
        frame.setVisible(true);
    }

    /**
     * createCustomizedDatePicker, This creates a date picker with customized settings. Only a few
     * of the available settings have been changed. This date picker has the first day of the week
     * as Monday, a custom veto policy, and a custom highlight policy.
     */
    private static DatePicker createCustomizedDatePicker() {
        DatePickerSettings settings = new DatePickerSettings();
        settings.firstDayOfWeek = DayOfWeek.MONDAY;
        settings.vetoPolicy = new SampleVetoPolicy();
        settings.highlightPolicy = new SampleHighlightPolicy();
        return new DatePicker(settings);
    }

    /**
     * setTwoWithY2KButtonClicked, This sets the date in date picker two, to New Years Day 2000.
     */
    private static void setTwoWithY2KButtonClicked(ActionEvent e) {
        // Set date picker date.
        LocalDate dateY2K = LocalDate.of(2000, Month.JANUARY, 1);
        datePicker2.setDate(dateY2K);
        // Display message.
        String dateString = datePicker2.getISODateStringOrNullString();
        String message = "The datePicker2 date was set to New Years 2000!\n\n";
        message += ("The datePicker2 date is currently set to: " + dateString + ".");
        panel.messageTextArea.setText(message);
    }

    /**
     * setOneWithTwoButtonClicked, This sets the date in date picker one, to whatever date is
     * currently set in date picker two.
     */
    private static void setOneWithTwoButtonClicked(ActionEvent e) {
        // Set date from date picker 2.
        LocalDate datePicker2Date = datePicker2.getDateOrNull();
        datePicker1.setDate(datePicker2Date);
        // Display message.
        String message = "The datePicker1 date was set using the datePicker2 date!\n\n";
        message += getDatePickerOneDateText();
        panel.messageTextArea.setText(message);
    }

    /**
     * setOneWithFeb31ButtonClicked, This sets the text in date picker one, to a nonexistent date
     * (February 31st). The last valid date in a date picker is always saved. This is a programmatic
     * demonstration of what happens when the user enters invalid text.
     */
    private static void setOneWithFeb31ButtonClicked(ActionEvent e) {
        // Set date picker text.
        datePicker1.setText("February 31, 1950");
        // Display message.
        String message = "The datePicker1 text was set to: \"" + datePicker1.getText() + "\".\n";
        message += "Note: The stored date (the last valid date), did not change because"
                + " February never has 31 days.\n\n";
        message += getDatePickerOneDateText();
        panel.messageTextArea.setText(message);
    }

    /**
     * getOneAndShowButtonClicked, This retrieves and displays whatever date is currently set in
     * date picker one.
     */
    private static void getOneAndShowButtonClicked(ActionEvent e) {
        // Get and display date picker text.
        panel.messageTextArea.setText(getDatePickerOneDateText());
    }

    /**
     * clearOneButtonClicked, This clears date picker one.
     */
    private static void clearOneButtonClicked(ActionEvent e) {
        // Clear date picker.
        datePicker1.clear();
        // Display message.
        String message = "The datePicker1 date was cleared!\n\n";
        message += getDatePickerOneDateText();
        panel.messageTextArea.setText(message);
    }

    /**
     * getDatePickerOneDateText, This returns a string indicating the current date stored in date
     * picker one.
     */
    private static String getDatePickerOneDateText() {
        // Create date string for date picker 1.
        String dateString = datePicker1.getISODateStringOrNullString();
        return ("The datePicker1 date is currently set to: " + dateString + ".");
    }

    /**
     * createDemoButtons, This creates the buttons for the demo, adds an action listener to each
     * button, and adds each button to the display panel.
     */
    private static void createDemoButtons() {
        // Create each demo button, and add it to the panel.
        // Add an action listener to link it to its appropriate function.
        int row = 0;
        JButton setTwoWithY2K = new JButton("Set DatePicker Two with New Years Day 2000");
        setTwoWithY2K.addActionListener(e -> setTwoWithY2KButtonClicked(e));
        panel.buttonPanel.add(setTwoWithY2K, CC.xy(1, (row += 2)));
        JButton setOneWithTwo = new JButton("Set DatePicker One with the date in Two");
        setOneWithTwo.addActionListener(e -> setOneWithTwoButtonClicked(e));
        panel.buttonPanel.add(setOneWithTwo, CC.xy(1, (row += 2)));
        JButton setOneWithFeb31 = new JButton("Set Text in DatePicker One to February 31, 1950");
        setOneWithFeb31.addActionListener(e -> setOneWithFeb31ButtonClicked(e));
        panel.buttonPanel.add(setOneWithFeb31, CC.xy(1, (row += 2)));
        JButton getOneAndShow = new JButton("Get and show the date in DatePicker One");
        getOneAndShow.addActionListener(e -> getOneAndShowButtonClicked(e));
        panel.buttonPanel.add(getOneAndShow, CC.xy(1, (row += 2)));
        JButton clearOne = new JButton("Clear DatePicker One.");
        clearOne.addActionListener(e -> clearOneButtonClicked(e));
        panel.buttonPanel.add(clearOne, CC.xy(1, (row += 2)));
    }

    /**
     * SampleVetoPolicy, A veto policy is a way to disallow certain dates from being selected in
     * calendar. A vetoed date cannot be selected by using the keyboard or the mouse.
     */
    private static class SampleVetoPolicy implements VetoPolicy {

        /**
         * isDateVetoed, Returns true if a date should be vetoed. Returns false to allow a date to
         * be selected.
         */
        @Override
        public boolean isDateVetoed(LocalDate date) {
            // Disallow every fifth day.
            if (date.getDayOfMonth() % 5 == 0) {
                return true;
            }
            // Allow all other days.
            return false;
        }
    }

    /**
     * SampleHighlightPolicy, A highlight policy is a way to visually highlight certain dates in the
     * calendar. These may be holidays, or weekends, or other significant dates.
     */
    private static class SampleHighlightPolicy implements HighlightPolicy {

        /**
         * getHighlightStringOrNull, This indicates if a date should be highlighted, or have a tool
         * tip in the calendar. Possible return values are: Return the desired tooltip text to give
         * a date a tooltip. Return an empty string to highlight a date without giving that date a
         * tooltip. Return null if a date should not be highlighted.
         */
        @Override
        public String getHighlightStringOrNull(LocalDate date) {
            // Highlight and give a tooltip to the 3rd.
            if (date.getDayOfMonth() == 3) {
                return "It's the 3rd!";
            }
            // Highlight all weekend days.
            if (date.getDayOfWeek() == DayOfWeek.SATURDAY) {
                return "It's the weekend.";
            }
            if (date.getDayOfWeek() == DayOfWeek.SUNDAY) {
                return "It's the weekend.";
            }
            // All other days should not be highlighted.
            return null;
        }
    }

}
