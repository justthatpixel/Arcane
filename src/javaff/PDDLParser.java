package javaff;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import java.nio.file.Path;
import java.nio.file.Paths;
import javaff.CustomButton;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.IOException;

public class PDDLParser {
    public static String filePath;

    public static class RoundTextField extends JTextField {
        private static final int ARC_WIDTH = 15;
        private static final int ARC_HEIGHT = 15;
        private final Color borderColor = new Color(53, 53, 53); // Dark gray

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g); // Keep this for basic functionality
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int width = getWidth();
            int height = getHeight();

            g2.setColor(new Color(27, 27, 27, 90)); // Your color, adjust alpha as needed
            g2.fillRoundRect(0, 0, width - 1, height - 1, ARC_WIDTH, ARC_HEIGHT);

            g2.dispose();
        }

    }

    private static JFrame frame;
    private static JTextArea nubOutputTextArea;
    private static RoundTextField nubInputTextField;

    private static String promptForEditedActionFromGUI() {
        // You can use nubInputTextField.getText() to get the user input from the GUI
        // For simplicity, I'm returning a dummy edited action here.
        return "(:edited-action ...)";
    }

    public static class RoundPanel extends JPanel {
        private static final int ARC_WIDTH = 15;
        private static final int ARC_HEIGHT = 15;
        private final Color borderColor = new Color(53, 53, 53); // Dark gray

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int width = getWidth();
            int height = getHeight();

            int inset = 10; // Control the inset size

            // Draw the white rounded border as before
            g2.setColor(Color.black);
            g2.fillRoundRect(0, 0, width - 1, height - 1, ARC_WIDTH, ARC_HEIGHT);

            // Draw the background with inset
            g2.setColor(getBackground());
            g2.fillRoundRect(inset, inset, width - inset * 2, height - inset * 2, ARC_WIDTH, ARC_HEIGHT);

            g2.dispose();
        }
    }

    public static void createGUI() {

        filePath = "src\\javaff\\problems\\depots\\domain.pddl";

        String backupFilePath = "src\\javaff\\problems\\depots\\temp.pddl";

        // try {
        // copyFile(filePath, backupFilePath);
        // System.out.println("File copied successfully.");
        // } catch (IOException e) {
        // System.err.println("Error copying file: " + e.getMessage());
        // }

        frame = new JFrame("Gui User Panel");
        frame.setSize(1200, 800);

        // Set dark theme colors
        Color darkBackground = new Color(43, 43, 43); // Dark gray
        Color buttonForeground = Color.WHITE;
        Color buttonBackground = new Color(27, 27, 27); // Darker gray
        Color textForeground = Color.WHITE;

        // Create text area
        nubOutputTextArea = new JTextArea();
        nubOutputTextArea.setEditable(true);
        nubOutputTextArea.setFont(new Font("Monocraft", Font.PLAIN, 18));
        nubOutputTextArea.setForeground(textForeground);
        nubOutputTextArea.setBackground(darkBackground);
        nubOutputTextArea.setBorder(BorderFactory.createEmptyBorder());

        Font customFont = new Font("Monocraft", Font.PLAIN, 18);
        nubOutputTextArea.setFont(customFont);
        UIManager.put("Button.font", customFont);
        UIManager.put("Label.font", customFont);
        UIManager.put("TextField.font", customFont);

        nubInputTextField = new RoundTextField();
        nubInputTextField.setForeground(textForeground);
        nubInputTextField.setBackground(darkBackground);
        nubInputTextField.setBorder(BorderFactory.createEmptyBorder());

        JPanel nubPanel = new RoundPanel();
        nubPanel.setLayout(new BorderLayout());
        JScrollPane nubScrollPane = new JScrollPane(nubOutputTextArea);
        nubPanel.add(nubScrollPane, BorderLayout.CENTER);
        nubPanel.add(nubInputTextField, BorderLayout.SOUTH);

        RoundPanel textAreaContainer = new RoundPanel();
        textAreaContainer.setLayout(new BorderLayout());
        textAreaContainer.setBackground(new Color(0, 0, 0, 0)); // Fully transparent background
        nubScrollPane.setBorder(BorderFactory.createLineBorder(darkBackground));
        // Add the output text area to the text area container
        textAreaContainer.add(nubScrollPane, BorderLayout.CENTER);

        // Add the text area container to the nub panel
        nubPanel.add(textAreaContainer, BorderLayout.CENTER);
        if (MainMenu.selectedFile != null) {
            filePath = MainMenu.selectedFile.getAbsolutePath();
        }

        CustomButton createPlanButton = new CustomButton("Show all");
        createPlanButton.addActionListener(e -> {
            nubScrollPane.setBorder(BorderFactory.createLineBorder(darkBackground));
            try {

                // save(filePath, backupFilePath);
                String domainContent = readDomainFile(filePath);

                System.out.println("Parsing and analyzing PDDL domain...");

                // Show Types
                nubOutputTextArea.append(displayFormattedSection(domainContent, "\\(:types[\\s\\S]*?\\)", "Type"));

                // Show Predicates
                nubOutputTextArea.append(displayFormattedPredicates(domainContent));

                // Show Actions
                nubOutputTextArea.append("Actions:\n");
                nubOutputTextArea.append(displayFormattedActions(domainContent, "\\(:action[\\s\\S]*?\\)"));
                // displayFormattedActions(domainContent);

                revert(filePath, backupFilePath);

            } catch (IOException er) {
                System.out.println("Error reading the domain file: " + er.getMessage());
            }

        });

        CustomButton revertButton = new CustomButton("Revert To Original");
        revertButton.addActionListener(e -> {
            try {
                revert(filePath, backupFilePath);
            } catch (IOException er) {
                System.out.println("Error reading the domain file: " + er.getMessage());

            }

        });

        CustomButton saveButton = new CustomButton("Save File");
        saveButton.addActionListener(e -> {
            // Handle "Run search algorithm" button action here
            try {
                save(filePath, backupFilePath);
            } catch (IOException er) {
                System.out.println("Error reading the domain file: " + er.getMessage());

            }
        });

        CustomButton showActionsButton = new CustomButton("Show Actions");
        showActionsButton.addActionListener(e -> {
            try {
                String domainContent = readDomainFile(filePath);
                nubOutputTextArea.setText("");
                nubOutputTextArea.append(displayFormattedActions(domainContent, "\\(:action[\\s\\S]*?\\)"));
            } catch (IOException er) {
                System.out.println("Error reading the domain file: " + er.getMessage());
            }
        });

        CustomButton showPredicatesButton = new CustomButton("Show Predicates");
        showPredicatesButton.addActionListener(e -> {
            try {
                String domainContent = readDomainFile(filePath);
                nubOutputTextArea.setText("");
                nubOutputTextArea.append(displayFormattedPredicates(domainContent));
            } catch (IOException er) {
                System.out.println("Error reading the domain file: " + er.getMessage());
            }
        });

        CustomButton showTypesButton = new CustomButton("Show Types");
        showTypesButton.addActionListener(e -> {
            try {
                String domainContent = readDomainFile(filePath);
                nubOutputTextArea.setText("");
                nubOutputTextArea.append(displayFormattedSection(domainContent, "\\(:types[\\s\\S]*?\\)", "Type"));
            } catch (IOException er) {
                System.out.println("Error reading the domain file: " + er.getMessage());
            }
        });

        CustomButton clearScreenButton = new CustomButton("Clear Screen");
        clearScreenButton.addActionListener(e -> {
            nubOutputTextArea.setText("");
        });

        CustomButton editPredicatesButton = new CustomButton("Edit Predicates");

        // ==========================================================================
        // =========== PREDICATE BUTTON ================================================
        // ==========================================================================

        editPredicatesButton.addActionListener(e -> {
            try {
                save(filePath, backupFilePath);
                String domainContent = readDomainFile(filePath);

                // Show Predicates
                // nubOutputTextArea
                // .append(displayFormattedSection(domainContent, "\\(:predicates[\\s\\S]*?\\)",
                // "Predicate"));

                nubOutputTextArea.append(displayFormattedPredicates(domainContent));

                List<String> predicates = getFormattedPredicates(domainContent);
                System.out.println(predicates.size());

                // Prompt user to choose a predicate
                nubOutputTextArea.append("Choose a predicate to edit:\n");

                // Add an event listener to nubInputTextField
                nubInputTextField.addActionListener(actionEvent -> {
                    // Get the chosen index from user input
                    int chosenIndex;
                    try {
                        chosenIndex = Integer.parseInt(nubInputTextField.getText());
                    } catch (NumberFormatException ex) {
                        return;
                    }

                    if (chosenIndex >= 1 && chosenIndex <= predicates.size()) {
                        // Adjust index to match list index (0-based)
                        int adjustedIndex = chosenIndex;

                        // Get the chosen predicate
                        String chosenPredicate = predicates.get(adjustedIndex);
                        nubInputTextField.setText("");
                        // Display the chosen predicate
                        nubOutputTextArea.append("Selected Predicate: " + chosenPredicate + "\n");

                        // Add a new ActionListener for subsequent Enter presses
                        nubInputTextField.addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                // Get the edited predicate from the text field
                                String editedPredicate = nubInputTextField.getText();
                                String updatedContent = domainContent.replace(chosenPredicate, editedPredicate);

                                try {
                                    Path path = Paths.get(filePath);

                                    // Create a backup of the original file
                                    String backupFilePath = filePath + ".bak";
                                    Files.copy(path, Paths.get(backupFilePath), StandardCopyOption.REPLACE_EXISTING);

                                    // Write the updated content to the file
                                    Files.write(path, updatedContent.getBytes());

                                    String directory = "src\\javaff\\VAL";
                                    String command = "src//javaff//VAL//validate domain.pddl";

                                    int correctness = executeCommand(command, directory);
                                    if (correctness == -1) {
                                        // Restore the original file
                                        Files.copy(Paths.get(backupFilePath), path,
                                                StandardCopyOption.REPLACE_EXISTING);
                                        nubOutputTextArea.append("Error in PDDL format, Type a valid PDDL action.");
                                    } else {
                                        nubOutputTextArea.append("Predicate updated and saved to file.");
                                    }
                                } catch (IOException er) {
                                    System.out.println("Error reading or writing the domain file: " + er.getMessage());
                                } catch (InterruptedException er) {
                                    System.out.println("Error executing the command: " + er.getMessage());
                                }

                                nubInputTextField.removeActionListener(this);
                            }
                        });
                    } else {
                        nubOutputTextArea.append("Invalid choice. Exiting.\n");
                    }

                });
            } catch (IOException er) {
                System.out.println("Error reading the domain file: " + er.getMessage());
            }
        });

        // ==========================================================================
        // =========== TYPE BUTTON ================================================
        // ==========================================================================

        CustomButton editTypesButton = new CustomButton("Edit Types");
        editTypesButton.addActionListener(e -> {
            try {
                // save(filePath, backupFilePath);
                String domainContent = readDomainFile(filePath);

                // Show Types
                nubOutputTextArea.append(displayFormattedSection(domainContent, "\\(:types[\\s\\S]*?\\)", "Type"));

                List<String> types = getFormattedTypes(domainContent);
                System.out.println(types);

                // Prompt user to choose a type
                nubOutputTextArea.append("Choose a type to edit:\n");

                // Add an event listener to nubInputTextField
                nubInputTextField.addActionListener(actionEvent -> {
                    // Get the chosen index from user input
                    int chosenIndex;
                    try {
                        chosenIndex = Integer.parseInt(nubInputTextField.getText());
                    } catch (NumberFormatException ex) {
                        return;
                    }

                    if (chosenIndex >= 1 && chosenIndex <= types.size()) {
                        // Adjust index to match list index (0-based)
                        int adjustedIndex = chosenIndex - 1;

                        // Get the chosen type
                        String chosenType = types.get(adjustedIndex);
                        nubInputTextField.setText("");
                        // Display the chosen type
                        nubOutputTextArea.append("Selected Type: " + chosenType + "\n");

                        // Add a new ActionListener for subsequent Enter presses
                        nubInputTextField.addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                // Get the edited type from the text field
                                String editedType = nubInputTextField.getText();
                                String updatedContent = domainContent.replace(chosenType, editedType);

                                try {
                                    Path path = Paths.get(filePath);

                                    // Create a backup of the original file
                                    String backupFilePath = filePath + ".bak";
                                    Files.copy(path, Paths.get(backupFilePath), StandardCopyOption.REPLACE_EXISTING);

                                    // Write the updated content to the file
                                    Files.write(path, updatedContent.getBytes());

                               
                                    String directory = "src\\javaff\\VAL";
                                    String command = "src//javaff//VAL//validate domain.pddl";

                                    int correctness = executeCommand(command, directory);
                                    if (correctness == -1) {
                                        // Restore the original file
                                        Files.copy(Paths.get(backupFilePath), path,
                                                StandardCopyOption.REPLACE_EXISTING);
                                        nubOutputTextArea.append("Error in PDDL format, Type a valid PDDL action.");
                                    } else {
                                        nubOutputTextArea.append("Type updated and saved to file.");
                                    }
                                } catch (IOException er) {
                                    System.out.println("Error reading or writing the domain file: " + er.getMessage());
                                } catch (InterruptedException er) {
                                    System.out.println("Error executing the command: " + er.getMessage());
                                }
                                nubInputTextField.removeActionListener(this);
                            }
                        });
                    } else {
                        nubOutputTextArea.append("Invalid choice. Exiting.\n");
                    }
                });
            } catch (IOException er) {
                System.out.println("Error reading the domain file: " + er.getMessage());
            }
        });

        // ==========================================================================
        // =========== SPLICE BUTTON ================================================
        // ==========================================================================

        CustomButton editActionsButton = new CustomButton("Edit Actions");
        editActionsButton.addActionListener(e -> {
            try {
                // save(filePath, backupFilePath);
                String domainContent = readDomainFile(filePath);

                // Show Actions
                nubOutputTextArea.append(displayFormattedActions(domainContent, "\\(:action[\\s\\S]*?\\)"));

                List<String> actions = getFormattedActions(domainContent);
                // System.out.println(actions.size());

                // Prompt user to choose an action
                nubOutputTextArea.append("Choose an action to edit:\n");

                // Remove the previous ActionListener

                // Add an event listener to nubInputTextField
                nubInputTextField.addActionListener(actionEvent -> {
                    // Get the chosen index from user input
                    int chosenIndex;
                    try {
                        chosenIndex = Integer.parseInt(nubInputTextField.getText());
                    } catch (NumberFormatException ex) {

                        return;
                    }

                    if (chosenIndex >= 1 && chosenIndex <= actions.size()) {
                        // Adjust index to match list index (0-based)
                        int adjustedIndex = chosenIndex - 1;

                        // Get the chosen action
                        String chosenAction = actions.get(adjustedIndex);
                        nubInputTextField.setText("");
                        // Display the chosen action
                        nubOutputTextArea.append("Selected Action: " + chosenAction + "\n");

                        // Add a new ActionListener for subsequent Enter presses
                        nubInputTextField.addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                // Get the edited action from the text field
                                String editedAction = nubInputTextField.getText();
                                String updatedContent = domainContent.replace(chosenAction, editedAction);
                                System.out.println(updatedContent);
                                System.out.println(editedAction);
                                System.out.println(editedAction);
                                try {
                                    Path path = Paths.get(filePath);

                                    // Create a backup of the original file
                                    String backupFilePath = filePath + ".bak";
                                    Files.copy(path, Paths.get(backupFilePath), StandardCopyOption.REPLACE_EXISTING);

                                    // Write the updated content to the file
                                    Files.write(path, updatedContent.getBytes());

                             
                                    String directory = "src\\javaff\\VAL";
                                    String command = "src//javaff//VAL//validate domain.pddl";

                                    int correctness = executeCommand(command, directory);
                                    if (correctness == -1) {
                                        // Restore the original file
                                        Files.copy(Paths.get(backupFilePath), path,
                                                StandardCopyOption.REPLACE_EXISTING);
                                        nubOutputTextArea.append("Error in PDDL format, Type a valid PDDL action.");
                                    } else {
                                        nubOutputTextArea.append("Actions updated and saved to file.");
                                    }
                                } catch (IOException er) {
                                    System.out.println("Error reading or writing the domain file: " + er.getMessage());
                                } catch (InterruptedException er) {
                                    System.out.println("Error executing the command: " + er.getMessage());
                                }

                                // You can continue processing the edited action here
                                nubInputTextField.removeActionListener(this);
                            }
                        });

                    } else {
                        nubOutputTextArea.append("Invalid choice. Exiting.\n");
                    }
                });

            } catch (IOException er) {
                System.out.println("Error reading the domain file: " + er.getMessage());
            }
        });

        JPanel buttonPanel = new JPanel(new GridBagLayout());
        buttonPanel.setBackground(darkBackground);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST; // Align components to the left
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(10, 10, 10, 10);

        buttonPanel.add(saveButton, gbc);
        gbc.gridy++;

        buttonPanel.add(createPlanButton, gbc);
        gbc.gridy++;

        buttonPanel.add(revertButton, gbc);
        gbc.gridy++;

        buttonPanel.add(editActionsButton, gbc);
        gbc.gridy++;

        buttonPanel.add(editPredicatesButton, gbc);
        gbc.gridy++;

        buttonPanel.add(editTypesButton, gbc);
        gbc.gridy++;

        buttonPanel.add(clearScreenButton, gbc);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buttonPanel, nubPanel);
        splitPane.setDividerLocation(400); // Set the initial divider location
        splitPane.setDividerSize(1);
        frame.add(splitPane);

        // frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    // private static final String FILE_PATH = "C:\\Arcane\\javaff\\problems\\depots\\domain.pddl";
    // String backupFilePath = "C:\\javaff\\src\\problems\\depots\\domain.pddl";

    private static List<String> getFormattedActions(String domainContent) {
        List<String> actions = new ArrayList<>();
        Pattern pattern = Pattern.compile("\\(\\:action[\\s\\S]*?\\)\\)\\)");
        Matcher matcher = pattern.matcher(domainContent);

        while (matcher.find()) {
            String actionSection = matcher.group();
            actions.add(actionSection);
        }

        return actions;
    }

    private static List<String> getFormattedPredicates(String domainContent) {
        List<String> predicates = new ArrayList<>();
        Pattern pattern = Pattern.compile("\\(\\:predicates[\\s\\S]*?\\)\\)");
        Matcher matcher = pattern.matcher(domainContent);

        while (matcher.find()) {
            String predicatesSection = matcher.group();

            // Split the predicates section into individual predicates
            String[] individualPredicates = predicatesSection.split("\\(");

            // Iterate through each predicate and add it to the list
            for (int i = 1; i < individualPredicates.length; i++) {
                String predicate = individualPredicates[i].trim();

                // Format the predicate with a number
                String formattedPredicate = "(" + i + ") " + predicate;

                // Add the formatted predicate to the list
                predicates.add(formattedPredicate);
            }
        }

        return predicates;
    }

    private static List<String> getFormattedTypes(String domainContent) {
        List<String> types = new ArrayList<>();
        Pattern pattern = Pattern.compile("\\(\\:types[\\s\\S]*?\\)");
        Matcher matcher = pattern.matcher(domainContent);

        while (matcher.find()) {
            String typesSection = matcher.group();

            // Split the types section into individual lines
            String[] lines = typesSection.split("\n");

            // Iterate through each line starting from the second one (skipping the section
            // declaration)
            for (int i = 1; i < lines.length; i++) {
                String line = lines[i].trim();
                if (!line.isEmpty()) {
                    types.add(line);
                }
            }
        }

        return types;
    }

    private static List<String> loadActionsFromFile(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        return Files.readAllLines(path);
    }

    private static void displayActions(List<String> actions) {
        System.out.println("Available Actions:");
        for (int i = 0; i < actions.size(); i++) {
            System.out.println((i + 1) + ". " + actions.get(i));
        }
    }

    private static int promptForActionIndex() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the index of the action to edit: ");
        return scanner.nextInt();
    }

    private static String promptForEditedAction() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the edited action: ");
        return scanner.nextLine();
    }

    private static void saveActionsToFile(String content, String filePath) throws IOException {
        Path path = Paths.get(filePath);
        Files.write(path, content.getBytes());
        System.out.println("Actions updated and saved to file.");

    }

    private static void createAndShowGUI() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                createGUI();
            }
        });
    }

    public static void main(String[] args) {


        createAndShowGUI();

    }

    private static void save(String filePath, String backupFilePath) throws IOException {
        // Create a backup of the original file
        Files.copy(Path.of(filePath), Path.of(backupFilePath), StandardCopyOption.REPLACE_EXISTING);
        System.out.println("Original file saved as: " + backupFilePath);
    }

    private static void revert(String filePath, String backupFilePath) throws IOException {
        // Revert to the original file
        Files.copy(Path.of(backupFilePath), Path.of(filePath), StandardCopyOption.REPLACE_EXISTING);
        System.out.println("File reverted to the original state.");
    }

    public static String readDomainFile(String filePath) throws IOException {
        StringBuilder content = new StringBuilder();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                content.append(line).append("\n");
            }
        }

        return content.toString();
    }

    public static String displayFormattedSection(String content, String pattern, String sectionType) {
        Pattern regexPattern = Pattern.compile(pattern);
        Matcher matcher = regexPattern.matcher(content);

        StringBuilder formattedSections = new StringBuilder();

        while (matcher.find()) {
            String match = matcher.group();
            if (sectionType.equals("Predicate")) {
                formattedSections.append(displayFormatted(match, sectionType));
            } else {
                formattedSections.append(displayFormatted(match, sectionType));
            }
        }

        return formattedSections.toString();
    }

    private static String displayFormatted(String section, String sectionType) {
        StringBuilder formattedSection = new StringBuilder();

        String[] lines = section.split("\n");
        String sectionName = lines[0].trim().replace("(:", "").replace(")", "").trim();

        formattedSection.append(sectionType).append(" ").append(sectionName).append(" :\n");

        int count = 1;
        for (int i = 1; i < lines.length; i++) {
            String line = lines[i].trim();
            if (!line.isEmpty()) {
                // String formattedLine = formatType(line, count++);
                // System.out.println(formattedLine);
                String formatedLine = formatType(line, count++);
                formattedSection.append(formatedLine).append("\n");
            }
        }

        formattedSection.append("\n");
        return formattedSection.toString();
    }

    // ==========================================
    // =========== ACTIONS FORMATTED ==========
    // ==========================================

    private static void displayFormattedActions(String domainContent) {
        Pattern pattern = Pattern.compile("\\(\\:action[\\s\\S]*?\\)\\)\\)");
        Matcher matcher = pattern.matcher(domainContent);

        while (matcher.find()) {
            String actionSection = matcher.group();
            System.out.println("Actions:");

            // Output the entire actionSection
            System.out.println(actionSection);

            System.out.println(); // Separate action blocks with an empty line
        }
    }

    public static String displayFormattedActions(String content, String pattern) {
        StringBuilder formattedActions = new StringBuilder();
        Pattern regexPattern = Pattern.compile(pattern);
        Matcher matcher = regexPattern.matcher(content);
        int actionCount = 1;

        while (matcher.find()) {
            String match = matcher.group();
            formattedActions.append(displayFormattedAction(match, actionCount++));
        }

        return formattedActions.toString();
    }

    private static String displayFormattedAction(String actionSection, int actionCount) {
        String[] lines = actionSection.split("\n");
        String actionName = lines[0].trim().replace("(:action", "").replace(")", "").trim();

        StringBuilder formattedAction = new StringBuilder("(" + actionCount + ") " + actionName + " ");

        for (int i = 1; i < lines.length; i++) {
            String line = lines[i].trim();
            if (!line.isEmpty()) {
                formattedAction.append(formatParameters(line)).append(" ");
            }
        }

        // Add a newline before displaying preconditions and effects
        formattedAction.append("\n");

        // Identify and append preconditions and effects
        boolean isInPreconditionBlock = false;
        boolean isInEffectBlock = false;

        for (String line : lines) {
            line = line.trim();

            if (line.startsWith(":precondition")) {
                isInPreconditionBlock = true;
                formattedAction.append("- Preconditions: ");
                continue; // Skip the line starting with :precondition
            } else if (line.startsWith(":effect")) {
                isInPreconditionBlock = false;
                isInEffectBlock = true;
                formattedAction.append("- Effects: ");
                continue; // Skip the line starting with :effect
            }

            if (isInPreconditionBlock || isInEffectBlock) {
                formattedAction.append(line).append("\n");
            }
        }

        return formattedAction.toString().trim() + "\n";
    }

    private static String formatParameters(String line) {
        return line.replace("(", "").replace(")", "").replace("?", "").replace(" - ", ":");
    }

    // ==========================================
    // =========== PREDICATE FORMATTED ==========
    // ==========================================

    public static String displayFormattedPredicates(String domainContent) {
        StringBuilder formattedPredicates = new StringBuilder();

        Pattern pattern = Pattern.compile("\\(\\:predicates[\\s\\S]*?\\)\\)");
        Matcher matcher = pattern.matcher(domainContent);

        while (matcher.find()) {
            String predicatesSection = matcher.group();
            formattedPredicates.append("Predicates:\n");

            // Remove leading and trailing parentheses
            predicatesSection = predicatesSection.substring(1, predicatesSection.length() - 2);

            // Split the content into individual lines
            String[] lines = predicatesSection.split("\n");

            int predicateCount = 1;

            for (String line : lines) {
                line = line.trim();
                if (!line.isEmpty()) {
                    // Format each predicate with a number
                    String formattedLine = "(" + predicateCount++ + ") " + formatPredicate(line);
                    formattedPredicates.append(formattedLine).append("\n");
                }
            }

            formattedPredicates.append("\n"); // Separate matches with an empty line
        }

        return formattedPredicates.toString();
    }

    private static String formatPredicate(String line) {
        return line.replace("(", "").replace(")", "").replace("?", "").replace(" - ", ":");
    }

    private static String formatType(String line, int count) {
        String[] components = line.split("-");
        if (components.length > 1) {
            // It's a subtype, include reference to the parent type
            String typeName = components[0].trim();
            String parentType = components[1].trim();
            return "(" + count + ") " + typeName + " (" + parentType + ")";
        } else {
            // It's a standalone type
            return "(" + count + ") " + line.trim();
        }
    }

    // ==========================================
    // ============ HELPER METHODS =============
    // ==========================================

    public static void copyFile(String sourcePath, String destinationPath) throws IOException {
        Path source = Paths.get(sourcePath);
        Path destination = Paths.get(destinationPath);
        Files.copy(source, destination);
    }

    // Custom Button Class

    public String DFP(String domainContent) {
        StringBuilder formattedPredicates = new StringBuilder();

        Pattern pattern = Pattern.compile("\\(\\:predicates[\\s\\S]*?\\)\\)");
        Matcher matcher = pattern.matcher(domainContent);

        while (matcher.find()) {
            String predicatesSection = matcher.group();
            formattedPredicates.append("Predicates:\n");

            // Remove leading and trailing parentheses
            predicatesSection = predicatesSection.substring(1, predicatesSection.length() - 2);

            // Split the content into individual lines
            String[] lines = predicatesSection.split("\n");

            int predicateCount = 1;

            for (String line : lines) {
                line = line.trim();
                if (!line.isEmpty()) {
                    // Format each predicate with a number
                    String formattedLine = "(" + predicateCount++ + ") " + formatPredicate(line);
                    formattedPredicates.append(formattedLine).append("\n");
                }
            }

            formattedPredicates.append("\n"); // Separate matches with an empty line
        }

        return formattedPredicates.toString();
    }

    public static int executeCommand(String command, String directory) throws IOException, InterruptedException {
        // String directory = "src\\javaff\\VAL";
        command = "src//javaff//VAL//validate src//javaff//problems//depots//domain.pddl";
           
        // String directory = "src\\javaff\\VAL";
    

        ProcessBuilder processBuilder = new ProcessBuilder(command.split(" "));
        processBuilder.directory(new File(directory));
        Process process = processBuilder.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
        }

        int exitCode = process.waitFor();
        System.out.println("Exited with error code : " + exitCode);
        return exitCode;
    }

    private static String formatType(String type) {
        // Your formatting logic goes here
        // For now, simply return the type as is
        return type;
    }

    public static String DFT(String domainContent) {
        StringBuilder formattedTypes = new StringBuilder();

        Pattern pattern = Pattern.compile("\\(\\:types[\\s\\S]*?\\)");
        Matcher matcher = pattern.matcher(domainContent);

        while (matcher.find()) {
            String typesSection = matcher.group();
            formattedTypes.append("Types:\n");

            // Remove leading and trailing parentheses
            typesSection = typesSection.substring(8, typesSection.length() - 1);

            // Split the content into individual types
            String[] types = typesSection.split("\\n");

            int typeCount = 1;

            for (String type : types) {
                type = type.trim();
                if (!type.isEmpty()) {
                    // Format each type with a number
                    String formattedLine = "(" + typeCount++ + ") " + type;
                    formattedTypes.append(formattedLine).append("\n");
                }
            }

            formattedTypes.append("\n"); // Separate matches with an empty line
        }

        return formattedTypes.toString();
    }

}
