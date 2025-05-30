import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AddAssetForm extends javax.swing.JFrame {
    private Asset parentForm;
    
    // Form components
    private JTextField nameField;
    private JTextField categoryField;
    private JComboBox<String> statusCombo;
    private JComboBox<String> conditionCombo;
    private JTextField locationField;
    private JLabel deviceIdLabel; // Changed to label since it's auto-generated

    public AddAssetForm(Asset parent) {
        this.parentForm = parent;
        initComponents();
        setLocationRelativeTo(parent);
        setTitle("Add New Asset");
        generatePropertyID(); // Generate ID on form open
    }

    private void initComponents() {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(400, 350);
        setResizable(false);
        
        JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Initialize form fields
        nameField = new JTextField();
        categoryField = new JTextField();
        statusCombo = new JComboBox<>(new String[]{"In Use", "In Storage", "Under Maintenance", "Retired"});
        conditionCombo = new JComboBox<>(new String[]{"Good", "Fair", "Poor", "Broken"});
        locationField = new JTextField();
        deviceIdLabel = new JLabel("Generating ID..."); // Placeholder text

        // Add fields to panel
        panel.add(new JLabel("Asset Name*:"));
        panel.add(nameField);
        panel.add(new JLabel("Category:"));
        panel.add(categoryField);
        panel.add(new JLabel("Status*:"));
        panel.add(statusCombo);
        panel.add(new JLabel("Condition*:"));
        panel.add(conditionCombo);
        panel.add(new JLabel("Location:"));
        panel.add(locationField);
        panel.add(new JLabel("Property ID:"));
        panel.add(deviceIdLabel); // Display generated ID

        // Buttons
        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> saveAsset());
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> this.dispose());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        // Main layout
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(panel, BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);
    }

    private void generatePropertyID() {
        try {
            // Format: yyyyMMdd (e.g., 20240530)
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
            String datePart = dateFormat.format(new Date());
            
            // Get next sequence number for today
            int nextSeq = getNextSequenceForDate(datePart);
            
            // Format: YYYYMMDD + sequence (3 digits)
            String propertyId = datePart + String.format("%03d", nextSeq);
            deviceIdLabel.setText(propertyId);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error generating ID: " + e.getMessage());
            deviceIdLabel.setText("ID-GEN-ERROR");
        }
    }

    private int getNextSequenceForDate(String datePart) throws SQLException {
        String sql = "SELECT MAX(CAST(SUBSTRING(deviceid, 9) AS UNSIGNED)) AS max_seq " +
                     "FROM asset WHERE deviceid LIKE ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, datePart + "%");
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                int maxSeq = rs.getInt("max_seq");
                return (rs.wasNull()) ? 1 : maxSeq + 1;
            }
            return 1; // First asset of the day
        }
    }

private void saveAsset() {
    // Validate required fields
    if (nameField.getText().trim().isEmpty()) {
        JOptionPane.showMessageDialog(this, "Asset name is required!", "Error", JOptionPane.ERROR_MESSAGE);
        return;
    }
    
    String deviceId = deviceIdLabel.getText();
    if (deviceId == null || deviceId.isEmpty() || deviceId.equals("Generating ID...")) {
        JOptionPane.showMessageDialog(this, "Property ID generation failed!", "Error", JOptionPane.ERROR_MESSAGE);
        return;
    }
    
    try {
        // CHANGE: Updated SQL to match column order
        String sql = "INSERT INTO asset (deviceid, name, category, status, `condition`, location) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            // CHANGE: Set parameters in correct order
            stmt.setString(1, deviceId);  // Device ID first
            stmt.setString(2, nameField.getText().trim());
            stmt.setString(3, categoryField.getText().trim());
            stmt.setString(4, (String) statusCombo.getSelectedItem());
            stmt.setString(5, (String) conditionCombo.getSelectedItem());
            stmt.setString(6, locationField.getText().trim());
            
            stmt.executeUpdate();
            
            JOptionPane.showMessageDialog(this, "Asset added successfully!");
            parentForm.refreshTable();
            this.dispose();
        }
    } catch (SQLException ex) {
        JOptionPane.showMessageDialog(this, 
            "Error saving asset: " + ex.getMessage(), 
            "Database Error", 
            JOptionPane.ERROR_MESSAGE);
    }
}}