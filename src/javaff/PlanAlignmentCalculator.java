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

public class PlanAlignmentCalculator {

    public static int smithWaterman(String sequence1, String sequence2) {
        int[][] dp = new int[sequence1.length() + 1][sequence2.length() + 1];
        int maxScore = 0;

        for (int i = 1; i <= sequence1.length(); i++) {
            for (int j = 1; j <= sequence2.length(); j++) {
                if (sequence1.charAt(i - 1) == sequence2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1] + 1;
                    maxScore = Math.max(maxScore, dp[i][j]);
                } else {
                    dp[i][j] = 0;
                }
            }
        }

        return maxScore;
    }

    public static int calculateAlignmentScore(Plan plan1, Plan plan2) {
        // Check for null plans
        if (plan1 == null || plan2 == null) {
            return 0; // or handle null case accordingly
        }

        // Convert plans to sequences of actions
        List<Action> actions1 = plan1.getActions();
        List<Action> actions2 = plan2.getActions();

        // Check for null actions
        if (actions1 == null || actions2 == null) {
            return 0; // or handle null case accordingly
        }

        // Convert sequences of actions to strings
        StringBuilder sequence1 = new StringBuilder();
        for (Action action : actions1) {
            sequence1.append(action.toString());
        }
        StringBuilder sequence2 = new StringBuilder();
        for (Action action : actions2) {
            sequence2.append(action.toString());
        }

        // Calculate alignment score using Smith-Waterman algorithm
        return smithWaterman(sequence1.toString(), sequence2.toString());
    }
}
