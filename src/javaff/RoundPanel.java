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

public  class RoundPanel extends JPanel {
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
        g2.setColor(borderColor);
        g2.fillRoundRect(0, 0, width - 1, height - 1, ARC_WIDTH, ARC_HEIGHT);

        // Draw the background with inset
        g2.setColor(getBackground());
        g2.fillRoundRect(inset, inset, width - inset * 2, height - inset * 2, ARC_WIDTH, ARC_HEIGHT);

        g2.dispose();
    }
}
