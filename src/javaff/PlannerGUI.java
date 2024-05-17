package javaff;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javaff.data.Plan;
import javaff.parser.ParseException;
// import javaff.search.BestFirstSearch;
import javaff.search.UnreachableGoalException;
import javaff.data.Action;
import javaff.PlannerGUI;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javaff.data.TotalOrderPlan;
import java.awt.geom.RoundRectangle2D;

import javaff.planning.NullFilter;
import javaff.planning.State;
import java.io.File;
import java.awt.image.BufferedImage;
import javax.swing.table.DefaultTableModel;
import javaff.CustomScrollPane;
import javaff.RoundPanel;
import javaff.PlanAlignmentCalculator;
import javaff.CustomButton;
import javaff.PDDLParser;
import java.util.HashMap;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.TreeSet;
import java.util.Hashtable;
import java.util.Collections;
import java.util.List;

// ==========================================
// ========= SETUP + ATTRIBUTES =============
// ==========================================
public class PlannerGUI {

    // filters
    public static boolean searchEHC = false;
    public static boolean searchUser = false;
    public static boolean searchHC = false;
    public static boolean searchBFS = false;
    public static int similarityModeFlag = 0;
    // goals
    public static float totalScore = 0;
    public static List<Integer> IndividualScores;
    public static List<Integer> IndividualScores2;
    public static StringBuilder[][] data = {
            { new StringBuilder("Plan"), new StringBuilder("Goal 1"), new StringBuilder("Goal 2"),
                    new StringBuilder("Goal 3"), new StringBuilder("Goal 4") },
            { new StringBuilder("Plan 1"), new StringBuilder(), new StringBuilder(), new StringBuilder(),
                    new StringBuilder() },
            { new StringBuilder("Plan 2"), new StringBuilder(), new StringBuilder(), new StringBuilder(),
                    new StringBuilder() } };

    protected static boolean goalAcheived = true;
    // comparison
    protected static TotalOrderPlan previousPlan;
    public static TotalOrderPlan currentPlan;
    protected static TotalOrderPlan pl2;
    public static boolean used;
    private static CardLayout cardLayout;
    private static JPanel cardPanel;
    public static String domainContent;
    // plans
    public static String pl2String;
    public static String pl1String;
    // goal list for explanation
    public static List<Integer> goalList1;
    public static List<Integer> goalList2;
    public static List<Action> actionList;
    public static String userInputNumber;
    public static List<String> actionlistString = new ArrayList<>();
    public static List<Action> currentPlanManual = new ArrayList<>();
    public static Hashtable closed;
    public static TreeSet open;
    public static State start;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> createNubUserPanel());
    }

    // ==========================================
    // ========= PLANNER RUN METHOD =============
    // ==========================================
    /********************************************
     * Manages the Planner Execution Process
     ********************************************
     * * Configures the planner (search modes, similarity). Loads domain and problem
     * files. Executes the planner to generate a solution.
     * * Stores the generated plan, scores, and goal status Handles potential goal
     * reachability and parsing exceptions.
     */
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

    private static void plannerFunction() {
        System.out.println("Planner is running...");
        boolean useOutputFile = false;
        System.out.println("Looking for files in the following locations:");
        // Manually select your file from here if a domain and file not chosen
        // For testing purposes, so you dont have to select the domain and problem file
        // every time.
        String domainFilePath = "C:\\javaff\\src\\problems\\depots\\domain.pddl";
        String problemFilePath = "C:\\javaff\\src\\problems\\depots\\pfile02";

        try {
            domainContent = readDomainFile(domainFilePath);
        } catch (IOException er) {
            System.out.println("Error reading the domain file: " + er.getMessage());
        }

        File domainFile = new File(domainFilePath);
        File problemFile = new File(problemFilePath);

        if (MainMenu.selectedFile != null) {

            domainFile = MainMenu.selectedFile;
            problemFile = MainMenu.selectedFile2;
        }

        File solutionFile = null;
        // ATTEMPT TO RUN THE PLANNER
        try {
            JavaFF planner = new JavaFF(domainFile, solutionFile);
            planner.similarityMode = similarityModeFlag;

            TotalOrderPlan plan = null;
            if (pl2 == null) {
                if (currentPlan != null) {
                    planner.setPlan(currentPlan);
                    planner.setGoal(false);
                    goalAcheived = false;

                }
            } else {
                {

                    planner.setPlan(pl2);
                    planner.setGoal(false);
                    goalAcheived = false;

                }
            }
            if (searchUser == true) {
                planner.setUseUser(true);
                planner.setUseBFS(searchBFS);
            }
            if (searchEHC == true) {
                planner.setUseEHC(true);
                planner.setUseBFS(searchBFS);
            } else if (searchHC == true) {
                planner.setUseHC(true);
                planner.setUseBFS(searchBFS);
            }

            if (used == false) {
                Plan p = planner.plan(problemFile);
                currentPlan = planner.getPlan();

            } else {

                TotalOrderPlan copy = planner.getPlan();
                Plan p = planner.plan(problemFile);
                pl2 = planner.getPlan();
                // turn into a string
                // save to file and then pattern match for the resources and location
                pl1String = currentPlan.toString();
                pl2String = pl2.toString();
                planner.setPlan(copy);
            }
            if (IndividualScores != null) {
                IndividualScores2 = planner.getGoalSteps();
            } else {
                IndividualScores = planner.getGoalSteps();
            }

            if (goalList1 == null) {
                goalList1 = planner.stepWhenTrue;
            } else {
                goalList2 = planner.stepWhenTrue;
            }
            IndividualScores = List.of(29, 1, 21, 30);
            IndividualScores2 = List.of(55, 1, 93, 95);
            System.out.println(IndividualScores);
            System.out.println(IndividualScores2);
            totalScore = planner.getTotalScore();
            goalAcheived = planner.getGoal();

        } catch (UnreachableGoalException e) {
            System.out.println("Goal " + e.getUnreachables().toString() + " is unreachable");
        } catch (ParseException e) {
            System.out.println(e.getMessage());
        }
    }

    // ==========================================
    // ============= USER PANEL =================
    // ==========================================
    /*********************************************************
     * Builds the 'Nub User' Panel (User Interface)
     ********************************************************
     * * Sets up panel's JFrame (title, size).
     * * Creates UI elements (buttons, text areas, input fields).
     * * Defines actions for buttons (Search, Edit, Splice, etc.)
     */
    public static String[] splitString(String string, String delimiter) {
        return string.split(delimiter);
    }

    public static int countOccurrences(String string, String sub) {
        int count = 0;
        int lastIndex = 0;

        while (lastIndex != -1) {
            lastIndex = string.indexOf(sub, lastIndex);

            if (lastIndex != -1) {
                count++;
                lastIndex += sub.length();
            }
        }

        return count;
    }

    public static String getSecondWord(String text) {
        if (text == null || text.isEmpty()) {
            return null; // or throw an exception, depending on your requirements
        }

        // Trim leading and trailing spaces
        text = text.trim();

        // Find the index of the first space
        int firstSpaceIndex = text.indexOf(' ');

        // If no space is found, return null since there's no second word
        if (firstSpaceIndex == -1) {
            return null;
        }

        // Find the index of the second space by starting the search after the first
        // space
        int secondSpaceIndex = text.indexOf(' ', firstSpaceIndex + 1);

        // If no second space is found, return the substring starting from the character
        // after the first space
        if (secondSpaceIndex == -1) {
            return text.substring(firstSpaceIndex + 1);
        }

        return text.substring(firstSpaceIndex + 1, secondSpaceIndex);
    }

    public static List<String> splitLines(String multilineText) {
        List<String> lines = new ArrayList<>();
        if (multilineText == null || multilineText.isEmpty()) {
            return lines;
        }
        String[] splitText = multilineText.split("\\r?\\n"); // Split by newline character(s)
        for (String line : splitText) {
            lines.add(line);
        }
        return lines;
    }

    public static void updateOutputText(String text) {
        userInputNumber = text;
    }

    private static void createNubUserPanel() {
        class ValueHolder {
            float planOneGoal = -1;
            float planTwoGoal = -1;
            List<Integer> planOneGoalList;
            List<Integer> planTwoGoalList;
        }
        ValueHolder values = new ValueHolder();
        class similarityMode {
            boolean isSimilarityModeOn = false;
        }
        similarityMode simMode = new similarityMode();
        class NumberHolder {
            int value;
        }

        final NumberHolder spliced = new NumberHolder();

        // Dark theme colors
        Color darkBackground = new Color(43, 43, 43); // Dark gray
        Color buttonForeground = Color.WHITE;
        Color buttonBackground = new Color(27, 27, 27); // Darker gray
        Color textForeground = Color.WHITE;

        // Set dark theme for the frame
        JFrame nubFrame = new JFrame("User Panel");
        nubFrame.getContentPane().setBackground(darkBackground);
        nubFrame.setSize(1200, 800);
        nubFrame.setBackground(darkBackground);

        // Set dark theme for text components
        Font customFont = new Font("Monocraft", Font.PLAIN, 18);
        UIManager.put("Button.font", customFont);
        UIManager.put("Label.font", customFont);
        UIManager.put("TextField.font", customFont);
        UIManager.put("TextArea.font", customFont);
        UIManager.put("Button.foreground", buttonForeground);
        UIManager.put("Button.background", buttonBackground);
        UIManager.put("Button.opaque", true);
        UIManager.put("Panel.background", darkBackground);
        UIManager.put("TextArea.foreground", textForeground);
        UIManager.put("TextArea.background", darkBackground);
        UIManager.put("TextField.foreground", textForeground);
        UIManager.put("TextField.background", darkBackground);
        UIManager.put("SplitPane.background", darkBackground);
        UIManager.put("SplitPane.border", darkBackground);

        RoundPanel textAreaContainer = new RoundPanel();
        textAreaContainer.setLayout(new BorderLayout()); // Optional, but aligns with rest of your code
        textAreaContainer.setBackground(new Color(0, 0, 0, 0)); // Fully transparent background
        JPanel nubPanel = new RoundPanel();
        nubPanel.setLayout(new BorderLayout());
        JTextArea nubOutputTextArea = new JTextArea();
        JScrollPane nubScrollPane = new CustomScrollPane(nubOutputTextArea);
        nubPanel.add(textAreaContainer, BorderLayout.CENTER);
        JTextField nubInputTextField = new RoundTextField();
        nubInputTextField.setForeground(textForeground);
        nubInputTextField.setBackground(darkBackground);

        CustomButton explainButton = new CustomButton("Explain Button");
        CustomButton createPlanButton = new CustomButton("Manual search");
        CustomButton runSearchButton = new CustomButton("Run search algorithm");
        CustomButton editSearchButton = new CustomButton("Edit Search Algorithm");
        CustomButton spliceButton = new CustomButton("Splice from plan");
        CustomButton compareButton = new CustomButton("Compare");
        CustomButton similarityButton = new CustomButton("Similarity Mode");

        // Set dark theme for buttons
        JButton[] buttons = { createPlanButton, runSearchButton, editSearchButton, spliceButton, compareButton,
                similarityButton, explainButton };
        for (JButton button : buttons) {
            button.setForeground(buttonForeground);
            button.setBackground(buttonBackground);
        }
        // Add components to the nub panel
        nubPanel.add(nubScrollPane, BorderLayout.CENTER);
        nubPanel.add(nubInputTextField, BorderLayout.SOUTH);
        JPanel buttonPanel = new RoundPanel();
        buttonPanel.setLayout(new GridBagLayout());
        buttonPanel.setBackground(darkBackground);

        // Add the button panel and nub panel to the frame
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buttonPanel, nubPanel);
        splitPane.setDividerLocation(400); // Set the initial divider location
        nubFrame.getContentPane().add(splitPane);
        nubFrame.setLocationRelativeTo(null);
        nubFrame.setVisible(true);
        nubFrame.getContentPane().setBackground(darkBackground);
        splitPane.setDividerSize(1); // Increase divider size for visibilitysplitPane.setBackground(darkBackground);
        nubInputTextField.setBorder(BorderFactory.createLineBorder(darkBackground)); // Change to your desired dark
        nubFrame.getContentPane().setBackground(darkBackground);
        nubScrollPane.setBorder(BorderFactory.createLineBorder(darkBackground)); // Change to your desired dark color
        JFrame frame = new JFrame("Planner GUI");
        setButtonPreferredSize(runSearchButton, editSearchButton, spliceButton, compareButton, explainButton);
        updateButtonPanel(buttonPanel, createPlanButton, runSearchButton, editSearchButton, spliceButton, compareButton,
                similarityButton, explainButton);

        PDDLParser po = new PDDLParser();

        // Explain Button
        explainButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // First question
                TotalOrderPlan pl2 = new TotalOrderPlan(null);
                // String tempo = po.displayFormattedSection(domainContent,
                // "\\(:types[\\s\\S]*?\\)", "Type");
                String tempo = po.DFT(domainContent);
                List<String> lines = splitLines(tempo);

                String tempo2 = tempo;
                String[] words = tempo2.split("\\s+");

                for (String line : lines) {
                    System.out.println(line);
                }

                nubOutputTextArea.append(po.DFT(domainContent));
                nubOutputTextArea.append("\nWhich type do you define as a location?: \n");
                nubInputTextField.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        String userInput1 = nubInputTextField.getText();
                        nubOutputTextArea.append("User Input 1: " + userInput1 + "\n");
                        Integer locationInt = Integer.parseInt(userInput1);
                        nubInputTextField.removeActionListener(this);
                        nubOutputTextArea.append("\nWhich type do you define as agent? \n");
                        nubInputTextField.addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                String userInput2 = nubInputTextField.getText();
                                nubOutputTextArea.append("User Input 2: " + userInput2 + "\n");
                                // Save the second user input here
                                Integer agentInt = Integer.parseInt(userInput2);
                                // Remove the action listener to avoid repeated actions
                                nubInputTextField.removeActionListener(this);
                                // Third question
                                nubOutputTextArea.append("\nWhich type do you define as resources? \n");
                                nubInputTextField.addActionListener(new ActionListener() {
                                    @Override
                                    public void actionPerformed(ActionEvent e) {
                                        String userInput3 = nubInputTextField.getText();
                                        nubOutputTextArea.append("User Input 3: " + userInput3 + "\n");
                                        Integer resourceInt = Integer.parseInt(userInput3);
                                        nubInputTextField.removeActionListener(this);

                                        // Fourth question
                                        nubOutputTextArea.append(
                                                po.displayFormattedActions(domainContent, "\\(:action[\\s\\S]*?\\)"));
                                        nubOutputTextArea
                                                .append("\nWhich action allows your agent to move to another state? \n");
                                        nubInputTextField.addActionListener(new ActionListener() {
                                            @Override
                                            public void actionPerformed(ActionEvent e) {
                                                String userInput5 = nubInputTextField.getText();
                                                nubOutputTextArea.append("User Input 3: " + userInput5 + "\n");
                                                Integer actionInt = Integer.parseInt(userInput5);
                                                nubInputTextField.removeActionListener(this);

                                                // Fourth question
                                                nubOutputTextArea.append("Which action would you like to explain? \n");
                                                nubInputTextField.addActionListener(new ActionListener() {

                                                    @Override
                                                    public void actionPerformed(ActionEvent e) {
                                                        String userInput4 = nubInputTextField.getText();
                                                        nubOutputTextArea.append("User Input 4: " + userInput4 + "\n");
                                                        String location = lines.get(locationInt);
                                                        String agent = lines.get(agentInt);
                                                        String resource = lines.get(resourceInt);
                                                        String actionz = lines.get(actionInt);

                                                        System.out.println(getSecondWord(location));
                                                        System.out.println(getSecondWord(agent));
                                                        System.out.println(getSecondWord(resource));
                                                        System.out.println(getSecondWord(actionz));

                                                        // Clear text fields
                                                        nubOutputTextArea.setText("");
                                                        nubInputTextField.setText("");

                                                        Integer splice_number = Integer.parseInt(userInput4);
                                                        spliced.value = splice_number;
                                                        // Clear the input field
                                                        nubOutputTextArea.setText("");
                                                        nubInputTextField.setText("");
                                                        if (currentPlan != null) {
                                                            List<Action> temp = new ArrayList<>();
                                                            if (splice_number < currentPlan.getActionCount()) {
                                                                goalAcheived = false;
                                                            }

                                                            // nubOutputTextArea
                                                            // .append("mo" + currentPlan.getActionCount());
                                                            nubPanel.add(nubInputTextField, BorderLayout.SOUTH);
                                                            int counter = 1; // Counter for numbering actions
                                                            for (Action action : currentPlan) {
                                                                String actionString = counter + ". "
                                                                        + action.toString();
                                                                JLabel actionLabel = new JLabel(actionString);
                                                                actionLabel.setFont(customFont);
                                                                temp.add(action);

                                                                if (counter == splice_number) {
                                                                    used = true;
                                                                    pl2.setPlan(temp);
                                                                    plannerFunction();
                                                                    break;
                                                                }
                                                                counter++;
                                                            }
                                                            nubOutputTextArea.setCaretPosition(
                                                                    nubOutputTextArea.getDocument().getLength());

                                                        }

                                                        String action = "drive";
                                                        String loc = "depot";
                                                        // String action = getSecondWord(actionz);
                                                        // String loc = getSecondWord(location);
                                                        String resc = getSecondWord(resource);
                                                        // nubOutputTextArea.append(action + resc + loc);

                                                        // Create a HashMap to store word counts
                                                        int resourceCount = 0;
                                                        HashMap<String, Integer> wordCounts = new HashMap<>();
                                                        HashMap<String, Integer> wordCounts2 = new HashMap<>();
                                                        HashMap<String, Integer> resourceCounter = new HashMap<>();
                                                        HashMap<String, Integer> resourceCounter2 = new HashMap<>();

                                                        // Split the pl2String into rows
                                                        String[] rows = pl2String.split(",");
                                                        String[] rows2 = pl1String.split(",");

                                                        for (String str : rows2) {
                                                            System.out.println(str);
                                                        }

                                                        for (String row : rows) {
                                                            String[] parts = row.trim().split(" ");

                                                            if (parts.length >= 2 && row.contains(action)
                                                                    && row.contains(loc)) {
                                                                String lastWord = parts[parts.length - 1];
                                                                wordCounts.put(lastWord,
                                                                        wordCounts.getOrDefault(lastWord, 0) + 1);
                                                            }

                                                            if (parts.length >= 2 && row.contains(resc)) {
                                                                for (int i = 0; i < parts.length; i++) {
                                                                    if (parts[i].contains(resc)) {
                                                                        // Increment the count for the word "crate" in
                                                                        // resourceCounter hashmap
                                                                        resourceCounter.put(parts[i],
                                                                                resourceCounter.getOrDefault(parts[i],
                                                                                        0)
                                                                                        + 1);
                                                                    }
                                                                }
                                                            }
                                                        }
                                                        for (String row : rows2) {

                                                            String[] parts = row.trim().split(" ");

                                                            if (parts.length >= 2 && row.contains(action)
                                                                    && row.contains(loc)) {
                                                                String lastWord = parts[parts.length - 1];
                                                                wordCounts2.put(lastWord,
                                                                        wordCounts2.getOrDefault(lastWord, 0) + 1);
                                                            }

                                                            if (parts.length >= 2 && row.contains(resc)) {
                                                                for (int i = 0; i < parts.length; i++) {
                                                                    if (parts[i].contains(resc)) {
                                                                        // Increment the count for the word "crate" in
                                                                        // resourceCounter hashmap
                                                                        resourceCounter2.put(parts[i], resourceCounter2
                                                                                .getOrDefault(parts[i], 0) + 1);
                                                                    }
                                                                }
                                                            }
                                                        }

                                                        int visitCountDiff = 0;
                                                        int resourceCountDiff = 0;

                                                        for (String word : wordCounts.keySet()) {
                                                            int visitCount1 = wordCounts.getOrDefault(word, 0);
                                                            int visitCount2 = wordCounts2.getOrDefault(word, 0);
                                                            visitCountDiff += visitCount2 - visitCount1;
                                                        }

                                                        // Calculate the differences in resource interaction counts for
                                                        // each resource
                                                        for (String word : resourceCounter.keySet()) {
                                                            int resourceCount1 = resourceCounter.getOrDefault(word, 0);
                                                            int resourceCount2 = resourceCounter2.getOrDefault(word, 0);
                                                            resourceCountDiff += resourceCount2 - resourceCount1;
                                                        }

                                                        StringBuilder messageBuilder = new StringBuilder();
                                                        messageBuilder.append("Plan 1 is ");

                                                        if (visitCountDiff >= 0 && resourceCountDiff >= 0) {
                                                            messageBuilder.append("better ");
                                                        } else {
                                                            messageBuilder.append("as good as Plan 2 ");
                                                        }

                                                        messageBuilder.append("because it visited locations \n");

                                                        if (visitCountDiff > 0) {
                                                            messageBuilder.append(
                                                                    "less frequently compared to Plan 2,\n Plan 2 visted :\n");

                                                            for (String word : wordCounts2.keySet()) {
                                                                int visitDiff = wordCounts2.getOrDefault(word, 0)
                                                                        - wordCounts.getOrDefault(word, 0);
                                                                if (visitDiff > 0) {
                                                                    messageBuilder.append(word).append(": ")
                                                                            .append(visitDiff).append(" times more\n");
                                                                }
                                                            }

                                                            // Check for locations in Plan 2 that were not present in
                                                            // Plan 1
                                                            for (String word : wordCounts2.keySet()) {
                                                                if (!wordCounts.containsKey(word)) {
                                                                    int visitCount = wordCounts2.get(word);
                                                                    messageBuilder.append(word).append(": ")
                                                                            .append(visitCount)
                                                                            .append(" times in Plan 2\n");
                                                                }
                                                            }
                                                        }

                                                        if (resourceCountDiff > 0) {
                                                            messageBuilder.append("and interacted with resource \n ");
                                                            messageBuilder.append(
                                                                    "less frequently compared to Plan 2,\n including interacting with:\n");

                                                            for (String rsc : resourceCounter2.keySet()) {
                                                                int countDiff = resourceCounter2.getOrDefault(rsc, 0)
                                                                        - resourceCounter.getOrDefault(rsc, 0);
                                                                if (countDiff > 0) {
                                                                    messageBuilder.append(rsc).append(": ")
                                                                            .append(countDiff).append(" times more\n");
                                                                }
                                                            }

                                                            // Check for resources in Plan 2 that were not present in
                                                            // Plan 1
                                                            // for (String rsc : resourceCounter2.keySet()) {
                                                            // if (!resourceCounter.containsKey(rsc)) {
                                                            // int count = resourceCounter2.get(rsc);
                                                            // messageBuilder.append("- interacted with ")
                                                            // .append(rsc).append(": ").append(count)
                                                            // .append(" times in Plan 2\n");
                                                            // }
                                                            // }

                                                            for (String rsc : resourceCounter2.keySet()) {
                                                                if (!resourceCounter.containsKey("pallet")) {
                                                                    int count = resourceCounter2.get("pallet");
                                                                    messageBuilder.append("- interacted with ")
                                                                            .append(rsc).append(": ").append(count)
                                                                            .append(" times in Plan 2\n");
                                                                }
                                                            }
                                                        }

                                                        messageBuilder.append(
                                                                "and took more steps to complete certain goals, including:\n");
                                                        for (int i = 0; i < goalList1.size()
                                                                && i < goalList2.size(); i++) {
                                                            int stepsDiff = goalList2.get(i) - goalList1.get(i);
                                                            if (stepsDiff > 0) {
                                                                messageBuilder.append("- Goal ").append(i + 1)
                                                                        .append(": Plan 2 took ").append(stepsDiff)
                                                                        .append(" more steps\n");
                                                            }
                                                        }

                                                        String message = messageBuilder.toString();
                                                        System.out.println(message);
                                                        nubOutputTextArea.append(message);
                                                        nubPanel.revalidate();
                                                        nubPanel.repaint();

                                                    }

                                                });

                                            }

                                        });
                                        nubInputTextField.removeActionListener(this);
                                    }
                                });
                            }
                        });
                    }
                });
            }
        });

        // ==========================================
        // ============= RUN BUTTON =================
        // ==========================================
        runSearchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    boolean add = false;
                    if (goalAcheived == false) {
                        add = true;
                    }
                    plannerFunction();
                    searchEHC = false;
                    searchUser = false;
                    searchHC = true;
                    searchBFS = true;
                    JOptionPane.showMessageDialog(frame, "Planner executed successfully!", "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                    String temporary = nubOutputTextArea.getText();
                    nubOutputTextArea.setText("");
                    String temp = Integer.toString(spliced.value);
                    if (currentPlan != null) {
                        nubOutputTextArea.setForeground(new Color(255, 255, 255, 0));
                        int fadeDelay = 10;
                        int fadeStep = 5;
                        Timer fadeTimer = new Timer(fadeDelay, new ActionListener() {
                            float opacity = 0.0f;

                            @Override
                            public void actionPerformed(ActionEvent e) {
                                opacity += fadeStep / 100f;
                                Color textColor = new Color(255, 255, 255, (int) (opacity * 255));
                                nubOutputTextArea.setForeground(textColor);
                                if (opacity >= 1.0f) {
                                    ((Timer) e.getSource()).stop();
                                }
                            }
                        });
                        fadeTimer.start();
                        if (add == true) {
                            nubOutputTextArea.append("Extended version Partial Plan \n");
                            nubOutputTextArea.append(temporary);
                        }
                        nubPanel.add(nubInputTextField, BorderLayout.SOUTH);
                        int counter = Integer.valueOf(temp) + 1; // Counter for numbering actions/ splice variable
                        for (Action action : currentPlan) {
                            String actionString = counter + ". " + action.toString();
                            JLabel actionLabel = new JLabel(actionString);
                            actionLabel.setFont(customFont);
                            nubPanel.add(actionLabel);
                            nubOutputTextArea.append(actionString + "\n");
                            nubOutputTextArea.setFont(customFont);
                            counter++;
                        }
                        nubOutputTextArea.setCaretPosition(nubOutputTextArea.getDocument().getLength());
                        nubPanel.revalidate();
                        nubPanel.repaint();
                    }
                    // ==========================================
                    // ============= PERCENTAGE =================
                    // ==========================================
                    /***************************************************************
                     * Calculates Shared Action Percentage Between Plans
                     *************************************************************** 
                     * * Calculates the percentage of shared actions.
                     */
                    PlanAlignmentCalculator alignmentCalculator = new PlanAlignmentCalculator();
                    int alignmentScore = alignmentCalculator.calculateAlignmentScore(previousPlan, currentPlan);

                    if (previousPlan == null || currentPlan == null) {
                        previousPlan = currentPlan;
                    } else {
                        int sharedActionsCount = 0;
                        int totalActionsInPreviousPlan = previousPlan.getActions().size();
                        for (Action actionInPrevious : previousPlan.getActions()) {
                            for (Action actionInCurrent : currentPlan.getActions()) {
                                if (actionInPrevious.equals(actionInCurrent)) {
                                    sharedActionsCount++;
                                }
                            }
                        }
                        double sharedActionsPercentage = (sharedActionsCount / (double) totalActionsInPreviousPlan)
                                * 100;
                        // nubOutputTextArea.append("Percentage of shared actions compared to previous
                        // plan: "
                        // + sharedActionsPercentage + "%");
                        nubOutputTextArea
                                .append("\nAlignment score: " + alignmentScore + "\n");
                    }
                    // Final Plan
                    nubOutputTextArea.append("Final Plan Calculated !" + "\n");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, "Error executing planner: " + ex.getMessage(), "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // Define the listener outside of the ActionListener
        ActionListener listener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Button pressed!");
            }
        };

        // ==========================================
        // ============= MANUAL SEARCH =================
        // ==========================================
        createPlanButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Check if currentPlan is null

                if (currentPlan == null) {
                    searchUser = false;
                    runSearchButton.doClick();
                    nubOutputTextArea.setText("");
                    currentPlanManual = new ArrayList<>();
                }

                // Prompt the user to enter a number
                nubOutputTextArea.append("Please enter a number: \n");
                searchUser = true;
                plannerFunction();
                nubOutputTextArea.append(userInputNumber);

                // Split userInputNumber to get individual numbers
                String[] userInputNumbers = userInputNumber.split("\\r?\\n");

                // ActionListener for nubInputTextField
                nubInputTextField.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        String userInput = nubInputTextField.getText();

                        // Parse userInput to get the selected action index
                        int selectedActionIndex = Integer.parseInt(userInput);

                        // Ensure the selectedActionIndex is within bounds
                        if (selectedActionIndex >= 0 && selectedActionIndex < actionList.size()) {
                            // Add the selected action to currentPlanManual
                            currentPlanManual = currentPlan.getPlan(); // Get the plan
                            Action selectedAction = actionList.get(selectedActionIndex - 1);
                            currentPlanManual.add(selectedAction); // Add the action to the plan
                            actionlistString.add(userInputNumbers[selectedActionIndex - 1]); // string
                            start.addActionToPlan(actionList.get(selectedActionIndex - 1));

                            // Display the updated plan in the GUI
                            nubOutputTextArea.setText("");
                            int counter = 1;
                            for (String action : actionlistString) {
                                String actionString = counter + ". " + action;
                                JLabel actionLabel = new JLabel(actionString);
                                actionLabel.setFont(customFont);
                                nubPanel.add(actionLabel);
                                nubOutputTextArea.append(actionString + "\n");
                                nubOutputTextArea.setFont(customFont);
                                counter++;
                            }
                            nubOutputTextArea.setCaretPosition(nubOutputTextArea.getDocument().getLength());
                            nubPanel.revalidate();
                            nubPanel.repaint();

                            // Clear the input text field
                            nubInputTextField.setText("");
                        } else {
                            // Display error message for invalid action index
                            JOptionPane.showMessageDialog(null, "Invalid action index.", "Error",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    }
                });
            }
        });

        // Method to update the plan display in the GUI

        // Add an ActionListener to the nubInputTextField to handle user input
        // ==========================================
        // ================ COMPARE =================
        // ==========================================
        /**********************************************************
         * Handles 'Compare' Button Actions
         **********************************************************
         * * Checks if a plan was successfully generated.
         * * Stores and compares goal scores from two generated plans.
         */
        compareButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                nubOutputTextArea.append("Plan 1 saved" + "\n");
                if (goalAcheived == true) {
                    if (values.planOneGoal == -1) {
                        values.planOneGoal = totalScore;
                        values.planOneGoalList = IndividualScores;
                    } else {
                        values.planTwoGoal = totalScore;
                        values.planTwoGoalList = IndividualScores2;
                        List<Integer> greaterIndices = new ArrayList<>();
                        if (values.planOneGoal > values.planTwoGoal) {
                            for (int i = 0; i < values.planOneGoalList.size(); i++) {
                                int element = values.planOneGoalList.get(i);
                                int element2 = values.planTwoGoalList.get(i);
                                if (element > element2) {
                                    greaterIndices.add(i + 1);
                                }
                            }
                            nubOutputTextArea
                                    .append(" Plan 1 is better for the following Goals:" + greaterIndices + "\n");
                            // nubOutputTextArea.append(IndividualScores.toString());

                        } else {
                            // nubOutputTextArea.append(Float.toString(values.planOneGoal) + " Plan 2 is
                            // better" + "\n");
                            for (int i = 0; i < values.planOneGoalList.size(); i++) {
                                int element = values.planOneGoalList.get(i);
                                int element2 = values.planTwoGoalList.get(i);
                                if (element < element2) {
                                    greaterIndices.add(i + 1);
                                }
                            }
                            nubOutputTextArea
                                    .append(" Plan 2 is better for the following Goals:" + greaterIndices + "\n");
                            // nubOutputTextArea.append(IndividualScores.toString());
                        }
                    }
                }
            }
        });

        // ==========================================
        // ============ SPLICE BUTTON ==============
        // ==========================================
        spliceButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                nubOutputTextArea.append("Please input the number of actions you would like to save \n");
                nubInputTextField.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        // Get the text from the input field
                        String userInput = nubInputTextField.getText();
                        // Do something with the user input (e.g., print or save it)
                        nubOutputTextArea.append("User Input: " + userInput + "\n");
                        Integer splice_number = Integer.parseInt(userInput);
                        spliced.value = splice_number;
                        // Clear the input field
                        nubOutputTextArea.setText("");
                        nubInputTextField.setText("");
                        if (currentPlan != null) {
                            List<Action> temp = new ArrayList<>();
                            if (splice_number < currentPlan.getActionCount()) {
                                goalAcheived = false;
                            }
                            nubPanel.add(nubInputTextField, BorderLayout.SOUTH);
                            int counter = 1; // Counter for numbering actions
                            for (Action action : currentPlan) {
                                String actionString = counter + ". " + action.toString();
                                JLabel actionLabel = new JLabel(actionString);
                                actionLabel.setFont(customFont);
                                nubPanel.add(actionLabel);
                                nubOutputTextArea.append(actionString + "\n");
                                nubOutputTextArea.setFont(customFont);
                                temp.add(action);
                                if (counter == splice_number) {
                                    currentPlan.setPlan(temp);
                                    break;
                                }
                                counter++;
                            }
                            nubOutputTextArea.setCaretPosition(nubOutputTextArea.getDocument().getLength());
                            nubPanel.revalidate();
                            nubPanel.repaint();
                        }
                    }
                });
            }
        });
        // ==========================================
        // ============ SELECT FILTERS ==============
        // ==========================================
        /************************************************************
         * Configures UI Buttons: 'Select Algorithms' and 'Select Filter'
         ************************************************************
         * * Creates 'Select Algorithm' and 'Select Filter' buttons.
         */
        nubPanel.add(nubInputTextField, BorderLayout.SOUTH);
        splitPane.setDividerLocation(400); // Set the initial divider location
        nubFrame.getContentPane().add(splitPane);
        nubFrame.setLocationRelativeTo(null);
        nubFrame.setVisible(true);
        editSearchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                buttonPanel.removeAll();
                // Create and add new buttons as specified
                CustomButton selectAlgorithmButton = new CustomButton("Select algorithm");
                CustomButton selectFilterButton = new CustomButton("Select Filter");
                updateButtonPanel(buttonPanel, selectAlgorithmButton, selectFilterButton);
                setButtonPreferredSize(selectAlgorithmButton, selectFilterButton);
                selectAlgorithmButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        // Create and add new buttons as specified
                        CustomButton selectEHC = new CustomButton("EHC");
                        CustomButton selectHC = new CustomButton("HC");
                        CustomButton selectBFS = new CustomButton("BFS");
                        CustomButton back1 = new CustomButton("Go Back");
                        updateButtonPanel(buttonPanel, selectEHC, selectHC, selectBFS, back1);
                        // Create a single ActionListener implementation to handle all button actions
                        ActionListener algorithmSelectionListener = new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                JButton source = (JButton) e.getSource();
                                setFalse();
                                if (source == selectEHC) {
                                    searchEHC = true;
                                    nubOutputTextArea.append("EHC Algorithm selected.");
                                } else if (source == selectHC) {
                                    searchHC = true;
                                    nubOutputTextArea.append("HC Algorithm selected.");
                                } else if (source == selectBFS) {
                                    searchBFS = true;
                                    nubOutputTextArea.append("BFS Algorithm selected.");
                                }
                            }
                        };
                        selectEHC.addActionListener(algorithmSelectionListener);
                        selectHC.addActionListener(algorithmSelectionListener);
                        selectBFS.addActionListener(algorithmSelectionListener);
                        back1.addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                nubOutputTextArea.append("go back to previous.");
                                buttonPanel.removeAll();
                                buttonPanel.setLayout(new GridBagLayout());
                                updateButtonPanel(buttonPanel, createPlanButton, runSearchButton, editSearchButton,
                                        spliceButton, compareButton, similarityButton);
                                setButtonPreferredSize(createPlanButton, runSearchButton, editSearchButton,
                                        spliceButton, compareButton, explainButton);
                            }
                        });
                    }
                });
                selectFilterButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        // Create and add new buttons as specified
                        CustomButton selectNullFilter = new CustomButton("Null Filter");
                        CustomButton selectRandomFilter = new CustomButton("Random 3 Filter");
                        CustomButton selectHelpfulFilter = new CustomButton("Helpful Filter");
                        CustomButton back1 = new CustomButton("Go Back");
                        updateButtonPanel(buttonPanel, selectNullFilter, selectRandomFilter, selectHelpfulFilter,
                                back1);
                        back1.addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                nubOutputTextArea.append("go back to previous.");
                                updateButtonPanel(buttonPanel, createPlanButton, runSearchButton, editSearchButton,
                                        spliceButton, compareButton, similarityButton);
                                setButtonPreferredSize(createPlanButton, runSearchButton, editSearchButton,
                                        spliceButton, compareButton);
                            }
                        });
                    }
                });
            }
        });
        // ==========================================
        // ================ SIMILARITY ==============
        // ==========================================
        /***************************************************
         * Handles 'Similarity' Button Actions
         ***************************************************
         */
        similarityButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                nubOutputTextArea.append("Similarity mode selected" + "\n");
                // Create and add new buttons as specified
                CustomButton selectHC = new CustomButton("Low Similarity Plan");
                CustomButton selectBFS = new CustomButton("High Similarity Plan");
                CustomButton back1 = new CustomButton("Go Back");
                updateButtonPanel(buttonPanel, selectHC, selectBFS, back1);
                setButtonPreferredSize(selectHC, selectBFS, back1);
                back1.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        nubOutputTextArea.append("go back to previous.");
                        updateButtonPanel(buttonPanel, createPlanButton, runSearchButton, editSearchButton,
                                spliceButton, compareButton, similarityButton, explainButton);
                        setButtonPreferredSize(createPlanButton, runSearchButton, editSearchButton, spliceButton,
                                compareButton, explainButton);
                    }
                });
            }
        });
    }

    // ==========================================
    // ============ HELPER METHODS =============
    // ==========================================
    private static void setButtonPreferredSize(CustomButton... buttons) {
        Dimension buttonSize = new Dimension(800, 200);
        for (CustomButton button : buttons) {
            button.setPreferredSize(buttonSize);
        }
    }

    private static void updateButtonPanel(JPanel panel, Component... components) {
        panel.removeAll();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST; // Align components to the left
        gbc.insets = new Insets(10, 10, 10, 10); // Padding
        for (Component component : components) {
            panel.add(component, gbc);
            gbc.gridy++;
        }
        panel.revalidate();
        panel.repaint();
    }

    public static TotalOrderPlan getTotalOrderPlan() {
        return currentPlan;
    }

    public static void setFalse() {
        searchEHC = false;
        searchUser = false;
        searchHC = false;
        searchBFS = false;
    }

}