import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.io.IOException;

import java.math.BigDecimal;
import java.math.RoundingMode;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.DocumentFilter;
import javax.swing.text.PlainDocument;

import com.toedter.calendar.JCalendar;
import com.toedter.calendar.JDateChooser;

public class MainWindow {

  private JFrame frame;
  private JTabbedPane tabbedPane;
  private final FileOperations fileOperations = new FileOperations();
  private final String recurringPaymentFileName = "recurring_payments";
  private final int inputFieldLength = 20;
  private final int outputFieldLength = 35;
  private final Color green = new Color(46,176,59);
  private final Color red = Color.RED;
  private final Color blue = new Color(14,60,227);

  public MainWindow() {
    initialize();
  }

  public void show() {
    this.frame.setVisible(true);
  }

  private void initialize() {

    frame = new JFrame();
    tabbedPane = new JTabbedPane();

    frame.setTitle("Expense Tracker");
    frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    frame.setMinimumSize(new Dimension(1000, 700));
    frame.setLocationRelativeTo(null);

    /* Add Expense Panel
     * - allows users to add an expense, value = name + amount + dd-MM-yyyy
     * - data will be saved to file, file name = MM-yyyy 
     * - every file encompasses one month of expenses
    */
    initializeAddExpensePanel(tabbedPane);


    /*  Remove Expense Panel
     * - input MMM-yyyy
     * - will show list to users 
     * - enables a user to delete an expense from file
    */
    initializeRemoveExpensePanel(tabbedPane);

    
    /*  Add Recurring Payment
     * - value = name + amount + dd
     * - all recurring payments will be saved to one file
    */
    initializeAddRecurringPaymentPanel(tabbedPane);


    /*  Remove Recurring Payment Panel
     * - will show list to users 
     * - enables a user to delete a recurring payment from file
    */
    initializeRemoveRecurringPaymentPanel(tabbedPane);


    /*  Calculate Expenses Panel
     * - input MMM-yyyy plus total month's income
     * - displays expenses from file and recurring payments
     * - displays total spent and balance
     */
    initializeCalculateExpensesPanel(tabbedPane);

    frame.add(tabbedPane);
  }

  private void initializeAddExpensePanel(JTabbedPane tabbedPane) {
    JPanel mainPanel = createPanel(new GridBagLayout());
    GridBagConstraints gBC = new GridBagConstraints();
    GridLayout gridLayout = new GridLayout(2, 1, 0, -5);

    JPanel nameWrapperPanel = createPanel(gridLayout);
    nameWrapperPanel.add(createLabel(" Expense Name:",null));
    JTextField nameField = createTextField(inputFieldLength);
    setTextFieldFilter(nameField, new CustomFilter("string"));
    nameWrapperPanel.add(nameField);

    gBC.gridx = 0;
    gBC.gridy = 0;
    mainPanel.add(nameWrapperPanel, gBC);
    
    JPanel amountWrapperPanel = createPanel(gridLayout);
    amountWrapperPanel.add(createLabel(" Amount:",null));  
    JTextField amountField = createTextField(inputFieldLength);
    setTextFieldFilter(amountField, new CustomFilter("double"));
    amountWrapperPanel.add(amountField);

    gBC.gridy = 1;
    mainPanel.add(amountWrapperPanel, gBC);

    JPanel dateWrapperPanel = createPanel(gridLayout);
    dateWrapperPanel.add(createLabel(" Date:", null));
    JDateChooser dateField = createDateChooser(null);
    dateWrapperPanel.add(dateField);

    gBC.gridy = 2;
    mainPanel.add(dateWrapperPanel, gBC);

    JPanel msgPanel = createPanel(null);
    JLabel msgSuccess = createLabel("Successfully Wrote To File", green);
    JLabel msgError = createLabel("Error Writing To File", red);
    gBC.gridy = 4;
    mainPanel.add(msgPanel, gBC);

    JPanel buttonWrapperPanel = createPanel(null);
    JButton button = createButton("Add Expense");
    button.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        msgPanel.removeAll();
        
        try {
          fileOperations.createAndWriteToFile(formatDate(dateField.getDate(),"MM-yyyy"), 
          new String[]{nameField.getText(), 
          String.valueOf(round(Double.parseDouble(amountField.getText()), 2)), 
          formatDate(dateField.getDate(),"dd-MM-yyyy")});
            
          msgPanel.add(msgSuccess);
          
          // needed to refresh page
          tabbedPane.setSelectedIndex(1);
          tabbedPane.setSelectedIndex(0);
        } catch (Exception e1) {
          msgPanel.add(msgError);

          // needed to refresh page
          tabbedPane.setSelectedIndex(1);
          tabbedPane.setSelectedIndex(0);
        }
      }
    });
    buttonWrapperPanel.add(button);
    gBC.gridx = 0;
    gBC.gridy = 3;
    mainPanel.add(buttonWrapperPanel, gBC);
    tabbedPane.addTab("Add Expense", mainPanel);
  }

  private void initializeRemoveExpensePanel(JTabbedPane tabbedPane) {
    JPanel mainPanel = createPanel(new BorderLayout());

    JPanel headerPanel = createPanel(null);
    JDateChooser dateField = createCustomMonthAndYearChooser("MMM yyyy");
    headerPanel.add(dateField);
    JButton button = createButton("Show Expenses");
    headerPanel.add(button);
    mainPanel.add(headerPanel, BorderLayout.NORTH);
  
    JPanel innerPanel = createPanel(new GridBagLayout()); 
    GridBagConstraints gBC = new GridBagConstraints();
    gBC.gridx = 0;

    button.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        innerPanel.removeAll();

        try {
          List<String> list = fileOperations.readFile(formatDate(dateField.getDate(), "MM-yyyy"));
          int counter = 0;
        
          for (String item: list) {
            JPanel entryPanel = createPanel(null);
            JTextField textField = createTextField(outputFieldLength);
            textField.setText(item);
            textField.setEditable(false);
            entryPanel.add(textField);
    
            JButton deleteButton = createButton("Delete");
            deleteButton.addActionListener(new ActionListener() {
              @Override
              public void actionPerformed(ActionEvent e) {
                try {
                  fileOperations.removeRecord(formatDate(dateField.getDate(), "MM-yyyy"), item);
                } catch (IOException e1) {

                }
                button.doClick(); 
              }
            });
            entryPanel.add(deleteButton);
            gBC.gridy = counter;
            innerPanel.add(entryPanel, gBC);
            counter++;
          }
        } catch (IOException e1) {
          JLabel msg = createLabel("File Not Found", red);
          innerPanel.add(msg);
        } 
        innerPanel.revalidate();

        // needed to refresh panel
        tabbedPane.setSelectedIndex(0);
        tabbedPane.setSelectedIndex(1);
      }
    });
    mainPanel.add(innerPanel);
    JScrollPane scrollPane = new JScrollPane(mainPanel); 
    tabbedPane.addTab("Remove Expense", scrollPane);
  }

  private void initializeAddRecurringPaymentPanel(JTabbedPane tabbedPane) {
    JPanel mainPanel = createPanel(new GridBagLayout());
    GridBagConstraints gBC = new GridBagConstraints();
    gBC.gridx = 0;
    GridLayout gridLayout = new GridLayout(2, 1, 0, -5);

    JPanel nameWrapperPanel = createPanel(gridLayout);
    nameWrapperPanel.add(createLabel(" Expense Name:", null));
    JTextField nameField = createTextField(inputFieldLength);
    setTextFieldFilter(nameField, new CustomFilter("string"));
    nameWrapperPanel.add(nameField);
    gBC.gridy = 0;
    mainPanel.add(nameWrapperPanel, gBC);
    
    JPanel amountWrapperPanel = createPanel(gridLayout);
    amountWrapperPanel.add(createLabel(" Amount:", null));  
    JTextField amountField = createTextField(inputFieldLength);
    setTextFieldFilter(amountField, new CustomFilter("double"));
    amountWrapperPanel.add(amountField);
    gBC.gridy = 1;
    mainPanel.add(amountWrapperPanel, gBC);

    JPanel dateWrapperPanel = createPanel(gridLayout);
    dateWrapperPanel.add(createLabel(" Date:", null));
    JDateChooser dateField = createCustomDayChooser("dd");
    dateWrapperPanel.add(dateField);
    gBC.gridy = 2;
    mainPanel.add(dateWrapperPanel, gBC);

    JPanel msgPanel = createPanel(null);
    JLabel msgSuccess = createLabel("Successfully Wrote To File", green);
    JLabel msgError = createLabel("Error Writing To File", red);
    gBC.gridy = 4;
    mainPanel.add(msgPanel, gBC);

    JPanel buttonWrapperPanel = createPanel(null);
    JButton button = createButton("Add Recurring Payment");
    button.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        msgPanel.removeAll();
        
        try {
          fileOperations.createAndWriteToFile(recurringPaymentFileName, 
          new String[]{nameField.getText(), 
            String.valueOf(round(Double.parseDouble(amountField.getText()), 2)), 
            formatDate(dateField.getDate(),"dd")});

            msgPanel.add(msgSuccess);

            // needed to refresh panel
            tabbedPane.setSelectedIndex(0);
            tabbedPane.setSelectedIndex(2);
        } catch (Exception e1) {
          msgPanel.add(msgError);

          // needed to refresh panel
          tabbedPane.setSelectedIndex(0);
          tabbedPane.setSelectedIndex(2);
        } 
      }
    });
    buttonWrapperPanel.add(button);
    gBC.gridy = 3;
    mainPanel.add(buttonWrapperPanel, gBC);
    tabbedPane.addTab("Add Recurring Payments", mainPanel);
  }

  private void initializeRemoveRecurringPaymentPanel(JTabbedPane tabbedPane) {
    JPanel mainPanel = createPanel(new GridBagLayout());
    GridBagConstraints gBC = new GridBagConstraints();
    gBC.gridx = 0;

    JScrollPane scrollPane = new JScrollPane(mainPanel);
    tabbedPane.addTab("Remove Recurring Payments", scrollPane);

    tabbedPane.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        if (tabbedPane.getSelectedIndex() == 3) {
          mainPanel.removeAll();

          try {
            List<String> list = fileOperations.readFile(recurringPaymentFileName);
            int counter = 0;

            for (String item: list) {
              JPanel entryPanel = createPanel(null);
              JTextField textField = createTextField(outputFieldLength);
              textField.setText(item);
              textField.setEditable(false);
              entryPanel.add(textField);
    
              JButton deleteButton = createButton("Delete");
              deleteButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                  try {
                    fileOperations.removeRecord(recurringPaymentFileName, item);
                  } catch (IOException e1) {
 
                  }
                  // needed to refresh panel
                  tabbedPane.setSelectedIndex(2);
                  tabbedPane.setSelectedIndex(3);
                }
              });
              entryPanel.add(deleteButton);
              gBC.gridy = counter;
              mainPanel.add(entryPanel, gBC);
              counter++;
            }
          } catch (IOException e1) {
            JLabel msg = createLabel("File Not Found", red);
            mainPanel.add(msg);
          }
        }
      }
    });
  }

  private void initializeCalculateExpensesPanel(JTabbedPane tabbedPane) {
    JPanel mainPanel = createPanel(new BorderLayout());

    JPanel headerPanel = createPanel(null);
    JDateChooser dateField = createCustomMonthAndYearChooser("MMM yyyy");
    headerPanel.add(dateField);
    headerPanel.add(createLabel("Income", null));
    JTextField incomeField = createTextField(15);
    setTextFieldFilter(incomeField, new CustomFilter("double"));
    headerPanel.add(incomeField);

    JPanel innerPanel = createPanel(new GridBagLayout());
    GridBagConstraints gBC = new GridBagConstraints();
    gBC.insets = new Insets(5, 0, 0, 5);
    gBC.anchor = GridBagConstraints.WEST;
    gBC.gridx = 0;

    JPanel msgPanel = createPanel(new GridLayout(2, 1));
    JLabel msgErrorR = createLabel("Recurring Payments File Not Found", red);
    JLabel msgErrorE = createLabel("Expenses File Not Found", red);

    JButton button = createButton("Calculate Expenses");
    button.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        innerPanel.removeAll();
        msgPanel.removeAll();

        int counter = 1;
        Double total = 0d;
        Double income;

        try {
          List<String> recurringList = fileOperations.readFile(recurringPaymentFileName);

          for (String item: recurringList) {
            JLabel label = createLabel(item,null);
            gBC.gridy = counter;
            innerPanel.add(label, gBC);
            counter++;
  
            total += getDoubleFromString(item);
          }
        } catch (IOException e1) {
          msgPanel.add(msgErrorR);
          gBC.gridy = counter;
          innerPanel.add(msgPanel, gBC);
          counter++;
        }
        
        try {
          List<String> expensesList = fileOperations.readFile(formatDate(dateField.getDate(), "MM-yyyy"));

          for (String item: expensesList) {
            JLabel label = createLabel(item, null);
            gBC.gridy = counter;
            innerPanel.add(label, gBC);
            counter++;
  
            total += getDoubleFromString(item);
          }
        } catch (IOException e1) {
          msgPanel.add(msgErrorE);
          gBC.gridy = counter;
          innerPanel.add(msgPanel, gBC);
        }

        try {
          income = round(Double.parseDouble(incomeField.getText()), 2);
        } catch (Exception ex) {
          income = 0d;
        }
        
        JPanel wrapper = createPanel(null);
        JLabel totalLabel = createLabel("Total Spent:",null);
        JLabel totalValue = createLabel(String.valueOf(round(total, 2)), blue);
        wrapper.add(totalLabel);
        wrapper.add(totalValue);

        Double balance = round(income - total, 2);
        JLabel balanceLabel = createLabel("Balance:",null);
        JLabel balanceValue = createLabel(balance.toString(), null);

        if (balance > 0) {
          balanceValue.setForeground(green);
        } else {
          balanceValue.setForeground(red);
        }

        wrapper.add(balanceLabel);
        wrapper.add(balanceValue);
        gBC.gridy = 0;
        innerPanel.add(wrapper, gBC);
        innerPanel.revalidate(); 

        // needed to refresh panel
        tabbedPane.setSelectedIndex(2);
        tabbedPane.setSelectedIndex(4);
      }
    });

    headerPanel.add(button);
    mainPanel.add(headerPanel, BorderLayout.NORTH);
    mainPanel.add(innerPanel);

    JScrollPane scrollPane = new JScrollPane(mainPanel); 

    tabbedPane.addTab("Calculate Expenses", scrollPane);
  }

  private JPanel createPanel(LayoutManager layout) {
    JPanel panel = new JPanel();
    if (layout != null) {
      panel.setLayout(layout);
    }

    return panel;
  }

  private JLabel createLabel(String text, Color color) {
    JLabel label = new JLabel(text);
    if (color != null) {
      label.setForeground(color);
    }

    return label;
  }

  private JTextField createTextField(int length) {
    JTextField textField = new JTextField(length);

    return textField;
  }

  private JButton createButton(String text) {
    JButton button = new JButton(text);
    button.setFocusable(false);

    return button;
  }

  private JDateChooser createDateChooser(String format) {
    JDateChooser dateChooser = new JDateChooser(new Date());
    dateChooser.setDateFormatString(format);
    dateChooser.setPreferredSize(new Dimension(230,28));

    return dateChooser;
  }

  private JDateChooser createCustomMonthAndYearChooser(String format) {
    JDateChooser dateChooser = createDateChooser(format);
    JCalendar calendar = dateChooser.getJCalendar();
    calendar.remove(calendar.getDayChooser());
    calendar.setPreferredSize(new Dimension(380, 27));

    calendar.addPropertyChangeListener(new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent e) {
        dateChooser.setDate(calendar.getDate());
      }
    });

    return dateChooser;
  }

  private JDateChooser createCustomDayChooser(String format) {
    JDateChooser dateChooser = createDateChooser(format);
    JCalendar calendar = dateChooser.getJCalendar();
    calendar.getMonthChooser().getComboBox().setEnabled(false);
    calendar.getMonthChooser().getSpinner().setEnabled(false);
    calendar.getYearChooser().getSpinner().setEnabled(false);
    
    return dateChooser;
  }

  private String formatDate(Date date, String formatString) {
    if (date == null) {
      date = new Date();
    }
    DateFormat dateFormat = new SimpleDateFormat(formatString);
    String formattedDate = dateFormat.format(date);

    return formattedDate;
  }

  private void setTextFieldFilter(JTextField textField, DocumentFilter filter) {
    PlainDocument doc = (PlainDocument) textField.getDocument();
    doc.setDocumentFilter(filter);
  }

  private Double getDoubleFromString(String item) {
    List<String> list = Arrays.asList(item.split(" "));
    int amountIndex = 0;
    for (String x : list) {
      if (x.equals("Amount:")) {
        amountIndex = list.indexOf(x) + 1;
      } 
    }

    return Double.parseDouble(list.get(amountIndex));
  }

  private double round(double value, int places) {
    BigDecimal bd = BigDecimal.valueOf(value);
    bd = bd.setScale(places, RoundingMode.HALF_UP);

    return bd.doubleValue();
  }
}