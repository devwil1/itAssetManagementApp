import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UpdateAssetForm extends javax.swing.JFrame {
    private Asset parentForm;
    private int assetId;
    
    // Form components
    private JTextField nameField;
    private JTextField categoryField;
    private JComboBox<String> statusCombo;
    private JComboBox<String> conditionCombo;
    private JTextField locationField;
    private JLabel deviceIdLabel;  // Changed to JLabel since it's read-only

    public UpdateAssetForm(Asset parent, int assetId) {
        this.parentForm = parent;
        this.assetId = assetId;
        initComponents();
        loadAssetData();
        setLocationRelativeTo(parent);
        setTitle("Update Asset");
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
        deviceIdLabel = new JLabel();  // Display-only field for device ID

        // Add fields to panel
        panel.add(new JLabel("Property ID:"));
        panel.add(deviceIdLabel);  // Show as label instead of text field
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

    private void loadAssetData() {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT * FROM asset WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, assetId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                // Get deviceid as String instead of int
                String deviceid = rs.getString("deviceid");
                String name = rs.getString("name");
                String category = rs.getString("category");
                String status = rs.getString("status");
                String condition = rs.getString("condition");
                String location = rs.getString("location");

                // Set values to form
                deviceIdLabel.setText(deviceid);  // Set as text to label
                nameField.setText(name);
                categoryField.setText(category);
                statusCombo.setSelectedItem(status);
                conditionCombo.setSelectedItem(condition);
                locationField.setText(location);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, 
                "Error loading asset data: " + ex.getMessage(), 
                "Database Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveAsset() {
        // Validate required fields
        if (nameField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Asset name is required!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            String sql = "UPDATE asset SET name=?, category=?, status=?, `condition`=?, location=? WHERE id=?";
            
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                stmt.setString(1, nameField.getText().trim());
                stmt.setString(2, categoryField.getText().trim());
                stmt.setString(3, (String) statusCombo.getSelectedItem());
                stmt.setString(4, (String) conditionCombo.getSelectedItem());
                stmt.setString(5, locationField.getText().trim());
                stmt.setInt(6, assetId);
                
                stmt.executeUpdate();
                
                JOptionPane.showMessageDialog(this, "Asset updated successfully!");
                parentForm.refreshTable();
                this.dispose();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, 
                "Error updating asset: " + ex.getMessage(), 
                "Database Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
}