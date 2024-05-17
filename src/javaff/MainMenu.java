package javaff;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javaff.data.Plan;
import javaff.parser.ParseException;
import javaff.search.UnreachableGoalException;
import javaff.data.Action;
import javaff.PlannerGUI;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javaff.data.TotalOrderPlan;
import java.awt.geom.RoundRectangle2D;
import javaff.planning.State;
import java.io.File;
import java.awt.image.BufferedImage;
import javax.swing.table.DefaultTableModel;
import javaff.CustomScrollPane;
import javaff.RoundPanel;
import javaff.PlanAlignmentCalculator;
import javaff.PlannerGUI;
import javax.swing.*;
import java.io.File;

// ==========================================
// ============= PLANNER GUI ================
// ==========================================
/**********************************************
 * Builds and Displays the Planner GUI
 **********************************************
 * * Sets up JFrame (title, close behavior, size).
 * * Loads and scales an icon image.
 * * Customizes fonts for UI elements.
 * * Creates 'Nub User' button with action listener.
 * * Uses GridBagLayout to arrange components.
 * * Finalizes and displays the frame.
 */

public class MainMenu {
    public static File selectedFile;
    public static File selectedFile2;

    public static void main(String[] args) {
        MainMenu.createAndShowGUI();
    }

    public static void createAndShowGUI() {
        JFrame frame = new JFrame("Planner GUI");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1200, 800);

        // Set dark background color
        Color darkBackgroundColor = new Color(51, 51, 51);

        ImageIcon originalIcon = new ImageIcon("src\\javaff\\img.png");
        Image originalImage = originalIcon.getImage();

        // Apply rounding
        Image roundedImage = createRoundedImage(originalImage, 30); // Adjust cornerRadius as needed

        Image scaledImage = roundedImage.getScaledInstance(720, -100, Image.SCALE_SMOOTH);
        JLabel img = new JLabel(new ImageIcon(scaledImage));

        Font customFont = new Font("Monocraft", Font.PLAIN, 17);
        UIManager.put("Button.font", customFont);
        UIManager.put("Label.font", customFont);
        UIManager.put("TextField.font", customFont);

        CustomButton nubUserButton = new CustomButton("Planner");
        nubUserButton.setPreferredSize(new Dimension(300, 50));
        nubUserButton.addActionListener(e -> PlannerGUI.main(null));

        CustomButton otherButton = new CustomButton("File Editor");
        otherButton.setPreferredSize(new Dimension(300, 50));
        otherButton.addActionListener(e -> {
            PDDLParser.main(null); // Call the main method of PDDLParser
        });

        CustomButton openButton = new CustomButton("Select Domain File");
        openButton.setPreferredSize(new Dimension(300, 50));
        openButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                int returnValue = fileChooser.showOpenDialog(frame);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    selectedFile = fileChooser.getSelectedFile();
                    // Do something with the selected file
                    System.out.println("Selected Domain file: " + selectedFile.getAbsolutePath());
                }
            }
        });

        CustomButton openButton2 = new CustomButton("Select Problem File");
        openButton2.setPreferredSize(new Dimension(300, 50));
        openButton2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                int returnValue = fileChooser.showOpenDialog(frame);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    selectedFile2 = fileChooser.getSelectedFile();
                    // Do something with the selected file
                    System.out.println("Selected Problem file: " + selectedFile.getAbsolutePath());
                }
            }
        });

        CustomButton infoPageButton = new CustomButton("Info Page");
        infoPageButton.setPreferredSize(new Dimension(300, 50));
        infoPageButton.addActionListener(e -> {
            JFrame infoFrame = new JFrame("Info Page");
            infoFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            infoFrame.setSize(1200, 800);
            infoFrame.setLocationRelativeTo(null); // Center the frame on the screen

            JPanel infoPanel = new JPanel(new BorderLayout());
            JTextArea infoTextArea = new JTextArea();
            infoTextArea.setEditable(false);
            infoTextArea.setLineWrap(true);
            infoTextArea.setWrapStyleWord(true);
            infoTextArea.setBackground(Color.black);
            infoTextArea.setForeground(Color.white);
            infoTextArea.setFont(new Font("Monocraft", Font.PLAIN, 14));
            // Font boldFont = new Font("Monocraft", Font.BOLD, 14);
            // Insert the beautifully formatted content
            infoTextArea.append(" Manual Search\n");
            infoTextArea.append(" -----------------\n");
            infoTextArea.append("\n");
            infoTextArea.append(" This shows a list of actions with your chosen filter\n\n");
            infoTextArea.append(" 1. Press the button\n");
            infoTextArea.append(" 2. You will see a list of actions, select one \n");
            infoTextArea.append(" 3. After selecting one, you should see your updated plan\n\n");
            infoTextArea.append("\n");

            infoTextArea.append(" Run search\n");
            infoTextArea.append(" -----------------\n");
            infoTextArea.append("\n");
            infoTextArea.append(" 1. Press the button\n");
            infoTextArea.append(" 2. If you don't have a plan already it will generate you a new plan\n");
            infoTextArea.append(" 3. If you do have a plan it will extend it for you\n\n");
            infoTextArea.append("\n");

            infoTextArea.append(" Edit Search\n");
            infoTextArea.append(" -----------------\n");
            infoTextArea.append("\n");
            infoTextArea.append(" 1. Open the menu\n");
            infoTextArea.append(" 2. Select from one of the algorithm or filters to guide your search\n\n");
            infoTextArea.append("\n");

            infoTextArea.append(" Splice The plan\n");
            infoTextArea.append(" -----------------\n");
            infoTextArea.append("\n");
            infoTextArea.append(" 1. You can only use this after you've generated a plan\n");
            infoTextArea.append(" 2. If you want to save a part of the plan, this is the place to do so\n\n");
            infoTextArea.append("\n");

            infoTextArea.append(" Compare\n");
            infoTextArea.append(" -----------------\n");
            infoTextArea.append("\n");
            infoTextArea.append(" 1. Generate a Plan\n");
            infoTextArea.append(
                    " 2. Press the compare button, this will tell you the first Plan for comparison has been saved\n");

            infoTextArea.append(" 3. Generate a different plan\n");
            infoTextArea.append(
                    " 4. If you press the button again, it will display which plan performed better and which goals it did better in as well\n\n");
            infoTextArea.append("\n");

            infoTextArea.append(" Similarity Mode\n");
            infoTextArea.append(" -----------------\n");
            infoTextArea.append("\n");
            infoTextArea.append(" 1. To turn on similarity mode, press low similarity or high similarity\n");
            infoTextArea.append(
                    " 2. On screen, we will display the Alignment score for you to compare how similar to plans are\n\n");
            infoTextArea.append("\n");

            infoTextArea.append(" Save File\n");
            infoTextArea.append(" -----------------\n");
            infoTextArea.append("\n");
            infoTextArea.append(" 1. This will save your current changes to the file\n\n");
            infoTextArea.append("\n");

            infoTextArea.append(" Clear Screen\n");
            infoTextArea.append(" -----------------\n");
            infoTextArea.append("\n");
            infoTextArea.append(" 1. This will wipe your screen for you\n\n");
            infoTextArea.append(" Revert To Original\n");
            infoTextArea.append(" -----------------\n");
            infoTextArea.append("\n");
            infoTextArea.append(
                    " 1. If you have made any changes you regret, this will allow you to revert back to the original file\n\n");
            infoTextArea.append("\n");
            infoTextArea.append(" Show All\n");
            infoTextArea.append(" -----------------\n");
            infoTextArea.append("\n");
            infoTextArea.append(" 1. You can see all the predicates/actions/types in the PDDL domain file\n\n");
            infoTextArea.append("\n");

            infoTextArea.append(" Edit Action/predicates/types\n");
            infoTextArea.append(" -----------------\n");
            infoTextArea.append("\n");
            infoTextArea.append(
                    " 1. If you press on one of these buttons you will be prompted to pick an element you want to edit\n");
            infoTextArea
                    .append(" 2. Select a valid element from one of the list by typing the number into the input box\n");
            infoTextArea.append(
                    " 3. You will then be prompted to type in the valid PDDL changes you would like to add to the file\n");
            infoTextArea.append(
                    " 4. If your input doesn't fit the correct PDDL format your changes will be rejected, however if your changes are valid it will be saved\n");

            JScrollPane scrollPane = new JScrollPane(infoTextArea);
            scrollPane.getViewport().setBackground(Color.BLACK);
            scrollPane.setBorder(BorderFactory.createEmptyBorder());

            RoundPanel roundedPanel = new RoundPanel();
            roundedPanel.setLayout(new BorderLayout());
            roundedPanel.setBackground(Color.BLACK);
            roundedPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            roundedPanel.add(scrollPane, BorderLayout.CENTER); // Ensure scrollPane is added

            infoFrame.add(roundedPanel);
            infoFrame.setVisible(true);
        });

        CustomButton quitButton = new CustomButton("Quit");
        quitButton.setPreferredSize(new Dimension(300, 50));
        quitButton.addActionListener(e -> System.exit(0)); // Exit the application when clicked

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(darkBackgroundColor); // Set background color

        GridBagConstraints gbc = new GridBagConstraints();

        // Add image to the left, taking 60% of the screen width
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridheight = 2;
        gbc.weightx = 0.6; // 60% width
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(10, 10, 10, 10);
        panel.add(img, gbc);

        // Add buttons on the right within the 40% width space
        JPanel buttonPanel = new JPanel(new GridBagLayout());
        buttonPanel.setBackground(darkBackgroundColor); // Set background color
        GridBagConstraints buttonGBC = new GridBagConstraints();
        buttonGBC.gridx = 0;
        buttonGBC.gridy = 0;
        buttonGBC.anchor = GridBagConstraints.CENTER;
        buttonPanel.add(nubUserButton, buttonGBC);

        buttonGBC.insets = new Insets(20, 0, 0, 0); // Padding between buttons
        buttonGBC.gridy = 1;
        buttonPanel.add(otherButton, buttonGBC);

        buttonGBC.gridy = 2;
        buttonPanel.add(infoPageButton, buttonGBC); // Add the info page button

        buttonGBC.gridy = 3;
        buttonPanel.add(openButton, buttonGBC); // Add the quit button

        buttonGBC.gridy = 4;
        buttonPanel.add(openButton2, buttonGBC); // Add the quit button

        buttonGBC.gridy = 5;
        buttonPanel.add(quitButton, buttonGBC); // Add the quit button

        gbc.gridx = 1;
        gbc.gridheight = 4; // Stack buttons vertically, so set grid height to 4
        gbc.weightx = 0.4; // 40% width for the button panel
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(buttonPanel, gbc);

        frame.getContentPane().add(panel);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static BufferedImage createRoundedImage(Image image, int cornerRadius) {
        int w = image.getWidth(null);
        int h = image.getHeight(null);
        BufferedImage output = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2 = output.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setClip(new RoundRectangle2D.Double(0, 0, w, h, cornerRadius, cornerRadius));
        g2.drawImage(image, 0, 0, null);
        g2.dispose();

        return output;
    }

    static class CustomButton extends JButton {
        private static final Color NORMAL_COLOR = new Color(145, 145, 145, 100); // Gray
        private static final Color HOVER_COLOR = new Color(105, 105, 105, 100); // Dark Gray
        private static final Color TEXT_COLOR = Color.WHITE;
        private static final int ARC_WIDTH = 15;
        private static final int ARC_HEIGHT = 15;

        public CustomButton(String text) {
            super(text);
            setForeground(TEXT_COLOR);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setBackground(NORMAL_COLOR);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    setBackground(HOVER_COLOR);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    setBackground(NORMAL_COLOR);
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (getModel().isPressed()) {
                g2.setColor(HOVER_COLOR);
            } else if (getModel().isRollover()) {
                g2.setColor(NORMAL_COLOR);
            } else {
                g2.setColor(NORMAL_COLOR);
            }

            g2.fillRoundRect(0, 0, getWidth(), getHeight(), ARC_WIDTH, ARC_HEIGHT);
            g2.setColor(TEXT_COLOR);
            super.paintComponent(g2);
            g2.dispose();
        }
    }

}
