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

public class CustomButton extends JButton {
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