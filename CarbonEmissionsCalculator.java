import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class CarbonEmissionsCalculator extends JFrame {
    private JComboBox<String> activityComboBox;
    private JTextField activityLevelTextField;
    private JTextArea emissionsHistoryTextArea; // JTextArea to display emissions history
    private JScrollPane scrollPane; // JScrollPane to allow scrolling in emissions history

    public CarbonEmissionsCalculator() {
        super("Carbon Emissions Calculator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Initialize UI components
        activityComboBox = new JComboBox<>();
        activityLevelTextField = new JTextField(10);
        emissionsHistoryTextArea = new JTextArea(10, 30);
        emissionsHistoryTextArea.setEditable(false); // Make it read-only
        scrollPane = new JScrollPane(emissionsHistoryTextArea); // Add the JTextArea to a scrollable pane

        JButton calculateButton = new JButton("Calculate Emissions");
        calculateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                calculateEmissions();
            }
        });

        // Set layout
        setLayout(new FlowLayout());

        // Add components to the frame
        add(new JLabel("Select Activity:"));
        add(activityComboBox);
        add(new JLabel("Enter Activity Level:"));
        add(activityLevelTextField);
        add(calculateButton);
        add(scrollPane);


        fetchActivities();

        pack();
        setLocationRelativeTo(null); // Center the frame
        setVisible(true);
    }

    private void fetchActivities() {
        // Update the connection URL, username, and password based on your MySQL configuration
        String url = "jdbc:mysql://localhost:3306/CEA";
        String username = "root";
        String password = "sarkar@3355";

        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            String query = "SELECT ActivityName FROM CarbonEmissionActivities";
            try (Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery(query)) {

                while (resultSet.next()) {
                    activityComboBox.addItem(resultSet.getString("ActivityName"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void calculateEmissions() {
        String selectedActivity = (String) activityComboBox.getSelectedItem();
        double activityLevel = Double.parseDouble(activityLevelTextField.getText());

        // Update the connection URL, username, and password based on your MySQL configuration
        String url = "jdbc:mysql://localhost:3306/CEA";
        String username = "root";
        String password = "sarkar@3355";

        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            String query = "SELECT EmissionFactor FROM CarbonEmissionActivities WHERE ActivityName = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, selectedActivity);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        double emissionFactor = resultSet.getDouble("EmissionFactor");
                        double calculatedEmissions = activityLevel * emissionFactor;

                        // Store the calculated emissions in the database
                        storeEmissionsInDatabase(selectedActivity, calculatedEmissions);

                        // Update the emissions history
                        updateEmissionsHistory(selectedActivity, calculatedEmissions);
                    } else {
                        JOptionPane.showMessageDialog(this, "Activity not found");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void storeEmissionsInDatabase(String activity, double emissions) {
        // Update the connection URL, username, and password based on your MySQL configuration
        String url = "jdbc:mysql://localhost:3306/CEA";
        String username = "root";
        String password = "sarkar@3355";

        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            String insertQuery = "INSERT INTO UserEntries (ActivityName, Emissions) VALUES (?, ?)";
            try (PreparedStatement insertStatement = connection.prepareStatement(insertQuery)) {
                insertStatement.setString(1, activity);
                insertStatement.setDouble(2, emissions);
                insertStatement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateEmissionsHistory(String activity, double emissions) {
        String currentHistory = emissionsHistoryTextArea.getText();
        String newEntry = "Activity: " + activity + ", Emissions: " + emissions + " CO2e\n";
        emissionsHistoryTextArea.setText(currentHistory + newEntry);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CarbonEmissionsCalculator());
    }
}
