import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.Properties;

public class JsonUpdater {
    private JFrame frame;
    private JTextField baseNameField;
    private JTextField directoryPathField;
    private JTextArea logArea;
    private JSpinner rangeSpinner;
    private boolean darkMode = false;
    private Color lightBackground = new Color(240, 240, 240);
    private Color darkBackground = new Color(43, 43, 43);
    private Color lightText = new Color(0, 0, 0);
    private Color darkText = new Color(255, 255, 255);
    private Color lightButtonBg = new Color(70, 130, 180);
    private Color darkButtonBg = new Color(100, 100, 100);
    private Color lightButtonHover = new Color(100, 149, 237);
    private Color darkButtonHover = new Color(130, 130, 130);
    private Color lightBorder = new Color(100, 100, 100);
    private Color darkBorder = new Color(70, 70, 70);
    private Color lightLogArea = new Color(250, 250, 250);
    private Color darkLogArea = new Color(30, 30, 30);
    private static final String SETTINGS_FILE = "jsonupdater.properties";
    private Properties settings;

    public JsonUpdater() {
        loadSettings();
        createAndShowGUI();
    }

    private void loadSettings() {
        settings = new Properties();
        try {
            File settingsFile = new File(SETTINGS_FILE);
            if (settingsFile.exists()) {
                try (FileInputStream in = new FileInputStream(settingsFile)) {
                    settings.load(in);
                    darkMode = Boolean.parseBoolean(settings.getProperty("darkMode", "false"));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveSettings() {
        try {
            settings.setProperty("darkMode", String.valueOf(darkMode));
            try (FileOutputStream out = new FileOutputStream(SETTINGS_FILE)) {
                settings.store(out, "JSON Updater Settings");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createAndShowGUI() {
        frame = new JFrame("JSON File Updater");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLayout(new BorderLayout(10, 10));
        updateTheme(frame.getContentPane());

        // Create main panel with padding
        JPanel mainPanel = new JPanel(new BorderLayout(20, 20));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        updateTheme(mainPanel);

        // Create panels
        JPanel inputPanel = new JPanel(new GridBagLayout());
        updateTheme(inputPanel);
        inputPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(getCurrentBorderColor(), 1),
                "Settings",
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                new Font("Arial", Font.BOLD, 12),
                getCurrentTextColor()
            ),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Theme toggle button
        JButton themeButton = createStyledButton("Toggle Dark Mode");
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        inputPanel.add(themeButton, gbc);
        gbc.gridwidth = 1;

        // Create a specific panel for directory selection with GridBagLayout
        JPanel directoryPanel = new JPanel(new GridBagLayout());
        updateTheme(directoryPanel); // Initial theme
        GridBagConstraints dirGbc = new GridBagConstraints();
        dirGbc.insets = new Insets(2, 2, 2, 2);
        
        // Directory Label
        dirGbc.gridx = 0;
        dirGbc.gridy = 0;
        dirGbc.weightx = 0.0;
        dirGbc.fill = GridBagConstraints.NONE;
        dirGbc.anchor = GridBagConstraints.WEST;
        JLabel dirLabel = new JLabel("Directory:");
        dirLabel.setFont(new Font("Arial", Font.BOLD, 12));
        dirLabel.setForeground(getCurrentTextColor());
        directoryPanel.add(dirLabel, dirGbc);

        // Directory Text Field
        dirGbc.gridx = 1;
        dirGbc.weightx = 1.0;
        dirGbc.fill = GridBagConstraints.HORIZONTAL;
        dirGbc.insets = new Insets(2, 5, 2, 5);
        directoryPathField = new JTextField();
        directoryPathField.setPreferredSize(new Dimension(0, 25));
        updateTextFieldTheme(directoryPathField);
        directoryPanel.add(directoryPathField, dirGbc);

        // Browse Button
        dirGbc.gridx = 2;
        dirGbc.weightx = 0.0;
        dirGbc.fill = GridBagConstraints.NONE;
        dirGbc.insets = new Insets(2, 2, 2, 2);
        JButton browseButton = createStyledButton("Browse");
        browseButton.setPreferredSize(new Dimension(80, 25));
        directoryPanel.add(browseButton, dirGbc);

        // Add directory panel to input panel
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        inputPanel.add(directoryPanel, gbc);
        gbc.gridwidth = 1;

        // Reset weightx for other components
        gbc.weightx = 0.0;

        // Base name input
        gbc.gridx = 0;
        gbc.gridy = 2;
        JLabel baseLabel = new JLabel("Base Name:");
        baseLabel.setFont(new Font("Arial", Font.BOLD, 12));
        baseLabel.setForeground(getCurrentTextColor());
        inputPanel.add(baseLabel, gbc);

        baseNameField = new JTextField(20);
        baseNameField.setPreferredSize(new Dimension(0, 30));
        updateTextFieldTheme(baseNameField);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        inputPanel.add(baseNameField, gbc);

        // Add Range Spinner
        gbc.gridx = 0;
        gbc.gridy = 3;
        JLabel rangeLabel = new JLabel("Range (units):");
        rangeLabel.setFont(new Font("Arial", Font.BOLD, 12));
        rangeLabel.setForeground(getCurrentTextColor());
        inputPanel.add(rangeLabel, gbc);

        SpinnerNumberModel spinnerModel = new SpinnerNumberModel(40, 1, 1000, 1);
        rangeSpinner = new JSpinner(spinnerModel);
        rangeSpinner.setPreferredSize(new Dimension(0, 30));
        updateSpinnerTheme(rangeSpinner);
        gbc.gridx = 1;
        inputPanel.add(rangeSpinner, gbc);

        // Button panel
        JPanel buttonPanel = new JPanel(new GridLayout(3, 1, 0, 10));
        updateTheme(buttonPanel);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        JButton processButton = createStyledButton("Process Files");
        JButton checkRangeButton = createStyledButton("Check Position Range");
        JButton checkDuplicatesButton = createStyledButton("Check Duplicate Positions");

        buttonPanel.add(processButton);
        buttonPanel.add(checkRangeButton);
        buttonPanel.add(checkDuplicatesButton);

        // Log area with title
        JPanel logPanel = new JPanel(new BorderLayout(5, 5));
        updateTheme(logPanel);
        logPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(getCurrentBorderColor(), 1),
                "Log",
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                new Font("Arial", Font.BOLD, 12),
                getCurrentTextColor()
            ),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        updateLogAreaTheme(logArea);
        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setPreferredSize(new Dimension(0, 300));
        logPanel.add(scrollPane, BorderLayout.CENTER);

        // Add all components to main panel
        mainPanel.add(inputPanel, BorderLayout.NORTH);
        mainPanel.add(buttonPanel, BorderLayout.CENTER);
        mainPanel.add(logPanel, BorderLayout.SOUTH);

        frame.add(mainPanel);

        // Theme toggle listener
        themeButton.addActionListener(e -> {
            darkMode = !darkMode;
            updateAllThemes(mainPanel);
            // Explicitly update directory panel components
            updateTheme(directoryPanel);
            dirLabel.setForeground(getCurrentTextColor());
            updateTextFieldTheme(directoryPathField);
            updateButtonTheme(browseButton);
            frame.repaint();
            saveSettings();
        });

        // Add button listeners
        browseButton.addActionListener(e -> {
            JFileChooser fileChooser = createStyledFileChooser();
            
            String currentPath = directoryPathField.getText();
            if (!currentPath.isEmpty()) {
                File currentDir = new File(currentPath);
                if (currentDir.exists()) {
                    fileChooser.setCurrentDirectory(currentDir);
                }
            }
            
            if (fileChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
                directoryPathField.setText(fileChooser.getSelectedFile().getAbsolutePath());
            }
        });

        processButton.addActionListener(e -> {
            String directoryPath = directoryPathField.getText();
            String baseName = baseNameField.getText();
            
            if (directoryPath.isEmpty() || baseName.isEmpty()) {
                JOptionPane.showMessageDialog(frame, 
                    "Please select a directory and enter a base name",
                    "Input Error",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            processButton.setEnabled(false);
            new Thread(() -> {
                processJsonFiles(directoryPath, baseName);
                SwingUtilities.invokeLater(() -> processButton.setEnabled(true));
            }).start();
        });

        checkRangeButton.addActionListener(e -> {
            String directoryPath = directoryPathField.getText();
            
            if (directoryPath.isEmpty()) {
                JOptionPane.showMessageDialog(frame, 
                    "Please select a directory",
                    "Input Error",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            checkRangeButton.setEnabled(false);
            new Thread(() -> {
                checkPositionRange(directoryPath, (Integer) rangeSpinner.getValue());
                SwingUtilities.invokeLater(() -> checkRangeButton.setEnabled(true));
            }).start();
        });

        checkDuplicatesButton.addActionListener(e -> {
            String directoryPath = directoryPathField.getText();
            
            if (directoryPath.isEmpty()) {
                JOptionPane.showMessageDialog(frame, 
                    "Please select a directory",
                    "Input Error",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            checkDuplicatesButton.setEnabled(false);
            new Thread(() -> {
                checkDuplicatePositions(directoryPath);
                SwingUtilities.invokeLater(() -> checkDuplicatesButton.setEnabled(true));
            }).start();
        });

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void updateAllThemes(Container container) {
        updateTheme(container);
        for (Component comp : container.getComponents()) {
            if (comp instanceof JLabel) {
                ((JLabel) comp).setForeground(getCurrentTextColor());
            } else if (comp instanceof JTextField) {
                updateTextFieldTheme((JTextField) comp);
            } else if (comp instanceof JSpinner) {
                updateSpinnerTheme((JSpinner) comp);
            } else if (comp instanceof JButton) {
                updateButtonTheme((JButton) comp);
            } else if (comp instanceof JTextArea) {
                updateLogAreaTheme((JTextArea) comp);
            } else if (comp instanceof JPanel) {
                updateTheme((JPanel)comp);
                updateAllThemes((Container) comp);
            } else if (comp instanceof Container) {
                updateAllThemes((Container) comp);
            }
            
            if (comp instanceof JComponent) {
                JComponent jcomp = (JComponent) comp;
                if (jcomp.getBorder() instanceof TitledBorder) {
                    TitledBorder border = (TitledBorder) jcomp.getBorder();
                    border.setTitleColor(getCurrentTextColor());
                }
            }
        }
    }

    private void updateTheme(Container container) {
        container.setBackground(darkMode ? darkBackground : lightBackground);
    }

    private void updateTextFieldTheme(JTextField textField) {
        textField.setBackground(darkMode ? darkLogArea : lightLogArea);
        textField.setForeground(darkMode ? darkText : lightText);
        textField.setCaretColor(darkMode ? darkText : lightText);
    }

    private void updateSpinnerTheme(JSpinner spinner) {
        spinner.getEditor().getComponent(0).setBackground(darkMode ? darkLogArea : lightLogArea);
        spinner.getEditor().getComponent(0).setForeground(darkMode ? darkText : lightText);
    }

    private void updateButtonTheme(JButton button) {
        button.setBackground(darkMode ? darkButtonBg : lightButtonBg);
        button.setForeground(darkMode ? darkText : Color.WHITE);
    }

    private void updateLogAreaTheme(JTextArea logArea) {
        logArea.setBackground(darkMode ? darkLogArea : lightLogArea);
        logArea.setForeground(darkMode ? darkText : lightText);
        logArea.setCaretColor(darkMode ? darkText : lightText);
    }

    private Color getCurrentTextColor() {
        return darkMode ? darkText : lightText;
    }

    private Color getCurrentBorderColor() {
        return darkMode ? darkBorder : lightBorder;
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(0, 35));
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setFocusPainted(false);
        updateButtonTheme(button);
        button.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(darkMode ? darkButtonHover : lightButtonHover);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(darkMode ? darkButtonBg : lightButtonBg);
            }
        });
        
        return button;
    }

    private void processJsonFiles(String directoryPath, String baseFileName) {
        ObjectMapper mapper = new ObjectMapper();
        final int[] counter = {100}; // Start from 100

        try {
            // First, find the highest existing number
            Files.list(Paths.get(directoryPath))
                 .filter(Files::isRegularFile)
                 .filter(path -> path.toString().endsWith(".json"))
                 .filter(path -> path.getFileName().toString().startsWith(baseFileName))
                 .forEach(path -> {
                     try {
                         String fileName = path.getFileName().toString();
                         String numberStr = fileName.substring(baseFileName.length() + 1, 
                                                            fileName.length() - 5); // remove .json
                         int number = Integer.parseInt(numberStr);
                         counter[0] = Math.max(counter[0], number + 1);
                     } catch (Exception e) {
                         // Skip files that don't match the exact pattern
                     }
                 });

            // Then process only non-renamed files
            Files.list(Paths.get(directoryPath))
                 .filter(Files::isRegularFile)
                 .filter(path -> path.toString().endsWith(".json"))
                 .filter(path -> !path.getFileName().toString().startsWith(baseFileName)) // Only process non-renamed files
                 .sorted()
                 .forEach(path -> {
                     try {
                         JsonNode rootNode = mapper.readTree(path.toFile());
                         
                         if (rootNode instanceof ObjectNode) {
                             ObjectNode objectNode = (ObjectNode) rootNode;
                             
                             String newName = String.format("%s_%d", baseFileName, counter[0]);
                             
                             objectNode.put("description", "new_description");
                             objectNode.put("name", newName);
                             
                             String newFileName = newName + ".json";
                             Path newPath = path.resolveSibling(newFileName);
                             
                             mapper.writerWithDefaultPrettyPrinter()
                                  .writeValue(newPath.toFile(), rootNode);
                             
                             Files.delete(path);
                             
                             log("Updated and renamed file: " + newPath);
                             counter[0]++;
                         }
                     } catch (IOException e) {
                         log("Error processing file: " + path);
                         e.printStackTrace();
                     }
                 });
            log("Processing complete!");
        } catch (IOException e) {
            log("Error accessing directory");
            e.printStackTrace();
        }
    }

    private void checkPositionRange(String directoryPath, double range) {
        ObjectMapper mapper = new ObjectMapper();
        List<PositionEntry> positions = new ArrayList<>();
        
        try {
            // First pass: collect all positions from ALL files
            Files.list(Paths.get(directoryPath))
                 .filter(Files::isRegularFile)
                 .filter(path -> path.toString().endsWith(".json"))
                 // Removed the filter that was skipping renamed files
                 .forEach(path -> {
                     try {
                         JsonNode rootNode = mapper.readTree(path.toFile());
                         if (rootNode.has("position")) {
                             JsonNode posNode = rootNode.get("position");
                             double[] pos = new double[] {
                                 posNode.get(0).asDouble(),
                                 posNode.get(1).asDouble(),
                                 posNode.get(2).asDouble()
                             };
                             positions.add(new PositionEntry(path, pos));
                         }
                     } catch (IOException e) {
                         log("Error reading file: " + path);
                         e.printStackTrace();
                     }
                 });

            log("\n=== Position Range Check Results ===");
            log("Checking for positions within " + range + " units of each other");
            log("Comparing only X and Z coordinates (ignoring Y/height)");
            
            Set<Path> filesToDelete = new HashSet<>();
            
            // Compare each position with every other position
            for (int i = 0; i < positions.size(); i++) {
                for (int j = i + 1; j < positions.size(); j++) {
                    PositionEntry p1 = positions.get(i);
                    PositionEntry p2 = positions.get(j);
                    
                    double distance = calculateXZDistance(p1.position, p2.position);
                    
                    if (distance < range) {
                        // Keep the first file, mark the second for deletion
                        if (!filesToDelete.contains(p1.path)) {
                            filesToDelete.add(p2.path);
                            log("\nFound close positions (XZ distance: " + String.format("%.2f", distance) + "):");
                            log(String.format("Keeping: %s [%.2f, %.2f, %.2f]", 
                                p1.path.getFileName(), p1.position[0], p1.position[1], p1.position[2]));
                            log(String.format("Will delete: %s [%.2f, %.2f, %.2f]", 
                                p2.path.getFileName(), p2.position[0], p2.position[1], p2.position[2]));
                        }
                    }
                }
            }
            
            // Delete marked files
            if (!filesToDelete.isEmpty()) {
                log("\nDeleting files:");
                for (Path path : filesToDelete) {
                    try {
                        Files.delete(path);
                        log("  - Deleted: " + path.getFileName());
                    } catch (IOException e) {
                        log("  - Error deleting " + path.getFileName());
                        e.printStackTrace();
                    }
                }
                log("\nDeleted " + filesToDelete.size() + " files");
            } else {
                log("\nNo files found within " + range + " units of each other (XZ plane)");
            }
            
            log("\nTotal files checked: " + positions.size());
            
        } catch (IOException e) {
            log("Error accessing directory");
            e.printStackTrace();
        }
    }

    private double calculateXZDistance(double[] pos1, double[] pos2) {
        double dx = pos1[0] - pos2[0];  // X coordinate
        double dz = pos1[2] - pos2[2];  // Z coordinate (last number)
        return Math.sqrt(dx*dx + dz*dz); // Ignoring Y (height) in the distance calculation
    }

    private void checkDuplicatePositions(String directoryPath) {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, List<Path>> positionToFiles = new HashMap<>();
        
        try {
            log("\n=== Starting Duplicate Position Check ===");
            
            // First pass: collect all positions from ALL files
            Files.list(Paths.get(directoryPath))
                 .filter(Files::isRegularFile)
                 .filter(path -> path.toString().endsWith(".json"))
                 // Removed the filter that was skipping renamed files
                 .forEach(path -> {
                     try {
                         JsonNode rootNode = mapper.readTree(path.toFile());
                         if (rootNode.has("position")) {
                             JsonNode posNode = rootNode.get("position");
                             String positionKey = String.format("%.2f,%.2f,%.2f",
                                 posNode.get(0).asDouble(),
                                 posNode.get(1).asDouble(),
                                 posNode.get(2).asDouble());
                             
                             log("File: " + path.getFileName() + " Position: " + positionKey);
                             
                             positionToFiles.computeIfAbsent(positionKey, k -> new ArrayList<>())
                                          .add(path);
                         } else {
                             log("Warning: No position found in " + path.getFileName());
                         }
                     } catch (IOException e) {
                         log("Error reading file: " + path);
                         e.printStackTrace();
                     }
                 });

            log("\n=== Exact Duplicate Position Check Results ===");
            
            boolean duplicatesFound = false;
            
            // Check for duplicates and delete duplicate files
            for (Map.Entry<String, List<Path>> entry : positionToFiles.entrySet()) {
                List<Path> files = entry.getValue();
                if (files.size() > 1) {
                    duplicatesFound = true;
                    log("\nDuplicate position found: " + entry.getKey());
                    log("Files with this position:");
                    for (Path p : files) {
                        log("  - " + p.getFileName());
                    }
                    log("\nKeeping file: " + files.get(0).getFileName());
                    log("Deleting duplicates:");
                    
                    // Start from index 1 to keep the first file
                    for (int i = 1; i < files.size(); i++) {
                        Path duplicatePath = files.get(i);
                        try {
                            Files.delete(duplicatePath);
                            log("  - Deleted: " + duplicatePath.getFileName());
                        } catch (IOException e) {
                            log("  - Error deleting " + duplicatePath.getFileName());
                            e.printStackTrace();
                        }
                    }
                }
            }

            if (!duplicatesFound) {
                log("\nNo exact duplicate positions found!");
                log("Positions found:");
                for (String pos : positionToFiles.keySet()) {
                    log("  " + pos + " : " + positionToFiles.get(pos).get(0).getFileName());
                }
            }
            
            log("\nTotal files checked: " + positionToFiles.values()
                .stream()
                .mapToInt(List::size)
                .sum());
            
        } catch (IOException e) {
            log("Error accessing directory");
            e.printStackTrace();
        }
    }

    private static class PositionEntry {
        Path path;
        double[] position;
        
        PositionEntry(Path path, double[] position) {
            this.path = path;
            this.position = position;
        }
    }

    private void log(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    private JFileChooser createStyledFileChooser() {
        JFileChooser fileChooser = new JFileChooser() {
            @Override
            protected JDialog createDialog(Component parent) {
                JDialog dialog = super.createDialog(parent);
                dialog.setSize(800, 600);
                
                // Style the dialog and all its components
                SwingUtilities.invokeLater(() -> {
                    styleFileChooserDialog(dialog);
                });
                
                return dialog;
            }
        };
        
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setDialogTitle("Select Directory");
        
        return fileChooser;
    }

    private void styleFileChooserDialog(Container container) {
        for (Component c : container.getComponents()) {
            if (c instanceof JPanel || c instanceof JDialog) {
                c.setBackground(darkMode ? darkBackground : lightBackground);
            }
            
            if (c instanceof JTextField) {
                JTextField tf = (JTextField) c;
                tf.setBackground(darkMode ? darkLogArea : lightLogArea);
                tf.setForeground(darkMode ? darkText : lightText);
                tf.setCaretColor(darkMode ? darkText : lightText);
            }
            
            if (c instanceof JList) {
                JList<?> list = (JList<?>) c;
                list.setBackground(darkMode ? darkLogArea : lightLogArea);
                list.setForeground(darkMode ? darkText : lightText);
            }
            
            if (c instanceof JTable) {
                JTable table = (JTable) c;
                table.setBackground(darkMode ? darkLogArea : lightLogArea);
                table.setForeground(darkMode ? darkText : lightText);
                table.setSelectionBackground(darkMode ? darkButtonHover : lightButtonHover);
                table.setSelectionForeground(darkMode ? darkText : lightText);
                table.setGridColor(darkMode ? darkBorder : lightBorder);
            }
            
            if (c instanceof JScrollPane) {
                JScrollPane scrollPane = (JScrollPane) c;
                scrollPane.getViewport().setBackground(darkMode ? darkLogArea : lightLogArea);
                // Style the scrollbar
                JScrollBar vScroll = scrollPane.getVerticalScrollBar();
                JScrollBar hScroll = scrollPane.getHorizontalScrollBar();
                vScroll.setBackground(darkMode ? darkBackground : lightBackground);
                hScroll.setBackground(darkMode ? darkBackground : lightBackground);
            }
            
            if (c instanceof JButton) {
                JButton button = (JButton) c;
                button.setBackground(darkMode ? darkButtonBg : lightButtonBg);
                button.setForeground(darkMode ? darkText : Color.WHITE);
                button.setFocusPainted(false);
                
                // Add hover effect
                button.addMouseListener(new java.awt.event.MouseAdapter() {
                    public void mouseEntered(java.awt.event.MouseEvent evt) {
                        button.setBackground(darkMode ? darkButtonHover : lightButtonHover);
                    }

                    public void mouseExited(java.awt.event.MouseEvent evt) {
                        button.setBackground(darkMode ? darkButtonBg : lightButtonBg);
                    }
                });
            }
            
            if (c instanceof JComboBox) {
                JComboBox<?> combo = (JComboBox<?>) c;
                combo.setBackground(darkMode ? darkLogArea : lightLogArea);
                combo.setForeground(darkMode ? darkText : lightText);
            }
            
            if (c instanceof JLabel) {
                ((JLabel) c).setForeground(darkMode ? darkText : lightText);
            }
            
            // Recursively style any sub-containers
            if (c instanceof Container) {
                styleFileChooserDialog((Container) c);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new JsonUpdater());
    }
}