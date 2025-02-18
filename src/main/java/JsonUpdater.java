import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
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

public class JsonUpdater {
    private JFrame frame;
    private JTextField baseNameField;
    private JTextField directoryPathField;
    private JTextArea logArea;
    private JSpinner rangeSpinner;

    public JsonUpdater() {
        createAndShowGUI();
    }

    private void createAndShowGUI() {
        frame = new JFrame("JSON File Updater");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);
        frame.setLayout(new BorderLayout(10, 10));

        // Create panels
        JPanel inputPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Directory selection
        gbc.gridx = 0;
        gbc.gridy = 0;
        inputPanel.add(new JLabel("Directory:"), gbc);

        directoryPathField = new JTextField(20);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        inputPanel.add(directoryPathField, gbc);

        JButton browseButton = new JButton("Browse");
        gbc.gridx = 2;
        gbc.weightx = 0.0;
        inputPanel.add(browseButton, gbc);

        // Base name input
        gbc.gridx = 0;
        gbc.gridy = 1;
        inputPanel.add(new JLabel("Base Name:"), gbc);

        baseNameField = new JTextField(20);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        inputPanel.add(baseNameField, gbc);

        // Process button
        JButton processButton = new JButton("Process Files");
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        inputPanel.add(processButton, gbc);

        // Add Range Spinner
        gbc.gridx = 0;
        gbc.gridy = 4;
        inputPanel.add(new JLabel("Range (units):"), gbc);

        SpinnerNumberModel spinnerModel = new SpinnerNumberModel(40, 1, 1000, 1);
        rangeSpinner = new JSpinner(spinnerModel);
        gbc.gridx = 1;
        inputPanel.add(rangeSpinner, gbc);

        // Add Check Range button
        JButton checkRangeButton = new JButton("Check Position Range");
        gbc.gridx = 1;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        inputPanel.add(checkRangeButton, gbc);

        // Add Check Duplicates button
        JButton checkDuplicatesButton = new JButton("Check Duplicate Positions");
        gbc.gridx = 1;
        gbc.gridy = 6;  // Put it after the range checker
        gbc.gridwidth = 2;
        inputPanel.add(checkDuplicatesButton, gbc);

        // Log area
        logArea = new JTextArea();
        logArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setPreferredSize(new Dimension(500, 200));

        frame.add(inputPanel, BorderLayout.NORTH);
        frame.add(scrollPane, BorderLayout.CENTER);

        // Add button listeners
        browseButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new JsonUpdater());
    }
}