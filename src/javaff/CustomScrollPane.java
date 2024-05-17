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

public  class CustomScrollPane extends JScrollPane {
    private static final Color BACKGROUND_COLOR = new Color(20, 20, 20);
    private static final int ARC_WIDTH = 15;
    private static final int ARC_HEIGHT = 15;
    private final Color borderColor = new Color(53, 53, 53); // Dark gray

    public CustomScrollPane(Component view) {
        super(view);
        setBackground(BACKGROUND_COLOR);
        setOpaque(true);
        getViewport().setBackground(BACKGROUND_COLOR);
        getViewport().setOpaque(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int width = getWidth() - 1; // Adjust for borders
        int height = getHeight() - 1;

        // Draw rounded border (white)
        g2d.setColor(borderColor);
        g2d.fillRoundRect(0, 0, width, height, ARC_WIDTH, ARC_HEIGHT);

        // Draw rounded background (slightly smaller)
        g2d.setColor(BACKGROUND_COLOR);
        g2d.fillRoundRect(1, 1, width - 2, height - 2, ARC_WIDTH, ARC_HEIGHT);

        g2d.dispose();
    }
}