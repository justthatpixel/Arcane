/************************************************************************
 * Strathclyde Planning Group,
 * Department of Computer and Information Sciences,
 * University of Strathclyde, Glasgow, UK
 * http://planning.cis.strath.ac.uk/
 * 
 * Copyright 2007, Keith Halsey
 * Copyright 2008, Andrew Coles and Amanda Smith
 * Copyright 2015, David Pattison
 *
 * This file is part of JavaFF.
 * 
 * JavaFF is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 * 
 * JavaFF is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with JavaFF.  If not, see <http://www.gnu.org/licenses/>.
 * 
 ************************************************************************/

 package javaff;

 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 
 import javaff.data.Action;
 import javaff.data.Fact;
 import javaff.data.GroundFact;
 import javaff.data.metric.Metric;
 import javaff.data.metric.MetricType;
 import javaff.data.DomainRequirements;
 import javaff.data.Requirement;
 import javaff.data.UngroundProblem;
 import javaff.data.GroundProblem;
 import javaff.data.Plan;
 import javaff.data.TotalOrderPlan;
 import javaff.data.TimeStampedPlan;
 import javaff.data.metric.NumberFunction;
 import javaff.data.strips.InstantAction;
 import javaff.data.strips.Not;
 import javaff.data.temporal.DurativeAction;
 import javaff.parser.PDDL21parser;
 import javaff.parser.ParseException;
 import javaff.planning.HelpfulFilter;
 import javaff.planning.MetricState;
 import javaff.planning.STRIPSState;
 import javaff.planning.State;
 import javaff.planning.TemporalMetricState;
 import javaff.planning.NullFilter;
 import javaff.planning.RandomThreeFilter;
 import javaff.search.BFSoriginal;
 import javaff.search.UserSearch;
 import javaff.search.EnforcedHillClimbingSearch;
 import javaff.search.HillClimbingSearch;
 import javaff.search.Search;
 import javaff.search.UnreachableGoalException;
 
 import java.io.PrintStream;
 import java.io.PrintWriter;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.Comparator;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Random;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * An implementation of the FF planner in Java. The planner currently only
  * supports STRIPS/ADL style planning, but as it is a branch of the CRIKEY
  * planner,
  * the components for both Temporal and Metric planning exist, but are unused.
  * 
  * @version 2.2 - This version represents another major overhaul of the JavaFF
  *          code from 2.1, which had a massively
  *          modified framework structure. Version 2.2 focuses more on the
  *          functionality of the system rather than the
  *          code-base. This essentially means bringing JavaFF much closer to the
  *          original FF functionality, by adding
  *          "correct" helpful action usage, reachability analysis,
  *          goal-ordering, extended ADL support
  *          and speed/memory optimisations. Both temporal and metric planning
  *          have been disabled because of
  *          bugs arising from framework modifications, but will be re-introduced
  *          at a later date.
  * 
  * @author Keith Halsey, < 2007
  * @author Amanda Coles, 2008
  * @author Andrew Coles, 2008
  * @author David Pattison, 2008-2013
  */
 public class JavaFF {
	 protected static double Nanos = 1000000000;
	 /**
	  * This flag is used to determine whether the behaviour of JavaFF is
	  * deterministic. This essentially comes down to which action is selected
	  * during search. If false, the underlying order of the applicable actions
	  * is used, if true, actions are sorted based upon a {@link Comparator}
	  * defined elsewhere.
	  * 
	  * @deprecated Not so much deprecated as not implemented fully yet!
	  */
	 protected static boolean Deterministic = false;
 
	 /**
	  * Returns the hard-coded, static and final PDDL requirements which JavaFF
	  * supports.
	  * 
	  * @see Requirement
	  * @see DomainRequirements
	  */
	 public static final DomainRequirements PDDLRequirementsSupported = JavaFF.GetRequirementsSupported();
 
	 public static BigDecimal EPSILON = new BigDecimal(0.01);
	 public static BigDecimal MAX_DURATION = new BigDecimal("100000");
	 public static Random generator = new Random(1234); // FIXME reintroduce as CLI parameter!
 
	 public static PrintStream planOutput = System.out;
	 public static PrintStream parsingOutput = System.out;
	 public static PrintStream infoOutput = System.out;
	 public static PrintStream errorOutput = System.err;
 
	 protected File domainFile;
	 protected File useOutputFile;
	 protected boolean useEHC, useBFS, useHC, useUser;
	 protected TotalOrderPlan p;
	 protected boolean goalAcheived;
	 protected String goalsNeeded;
	 public List<Integer> stepWhenTrue; // number of actions to complete a goal
	 protected int similarityMode = 0;
 
	 protected float sscore;
 
	 protected JavaFF() {
		 this.domainFile = null;
		 this.useOutputFile = null;
		 this.useBFS = true;
 
	 }
 
	 public JavaFF(String domain) {
		 this(domain, null);
	 }
 
	 public JavaFF(File domain) {
		 this(domain, null);
	 }
 
	 /**
	  * Initialise JavaFF with the specified domain file.
	  * 
	  * @param domain       The domain file which will have an associated problem
	  *                     file provided at planning time.
	  * @param solutionFile A file to output any solution found to. May be null of no
	  *                     solution is wanted.
	  */
	 public JavaFF(String domain, File solutionFile) {
		 this();
 
		 this.domainFile = new File(domain);
		 this.useOutputFile = solutionFile;
	 }
 
	 /**
	  * Initialise JavaFF with the specified domain file.
	  * 
	  * @param domain       The domain file which will have an associated problem
	  *                     file provided at planning time.
	  * @param solutionFile A file to output any solution found to. May be null of no
	  *                     solution is wanted.
	  */
	 public JavaFF(File domain, File solutionFile) {
		 this();
 
		 this.domainFile = domain;
		 this.useOutputFile = solutionFile;
	 }
 
	 /**
	  * Constructs and returns a DomainRequirements object which contains flags
	  * for the functionality currently available in JavaFF.
	  * 
	  * @return
	  */
	 public static DomainRequirements GetRequirementsSupported() {
		 DomainRequirements req = new DomainRequirements();
		 req.addRequirement(Requirement.Typing);
		 req.addRequirement(Requirement.Strips);
		 req.addRequirement(Requirement.Equality);
		 req.addRequirement(Requirement.ADL);
		 req.addRequirement(Requirement.NegativePreconditions);
		 req.addRequirement(Requirement.QuantifiedPreconditions);
		 req.addRequirement(Requirement.ExistentialPreconditions);
		 req.addRequirement(Requirement.UniversalPreconditions);
		 req.addRequirement(Requirement.ActionCosts);
 
		 return req;
	 }
 
	 protected boolean checkRequirements(DomainRequirements problemRequirments) {
		 return JavaFF.PDDLRequirementsSupported.subsumes(problemRequirments);
	 }
 
	 public static void main(String args[]) {
		 EPSILON = EPSILON.setScale(2, BigDecimal.ROUND_HALF_EVEN);
		 MAX_DURATION = MAX_DURATION.setScale(2, BigDecimal.ROUND_HALF_EVEN);
		 boolean useOutputFile = false;
		 // TODO write a decent arg parser
		 String domainFilePath = "C:\\javaff\\src\\problems\\depots\\domain.pddl";
		 String problemFilePath = "C:\\javaff\\src\\problems\\depots\\pfile01";
 
		 File domainFile = new File(domainFilePath);
		 File problemFile = new File(problemFilePath);
		 File solutionFile = null;
 
		 if (args.length > 2) {
			 solutionFile = new File(args[2]);
			 useOutputFile = true;
 
			 for (int i = 3; i < args.length; i++) {
				 if (args[i].equals("--deterministic") || args[i].equals("-d")) {
					 JavaFF.Deterministic = true;
				 }
			 }
		 }
		 try {
			 JavaFF planner = new JavaFF(domainFile, solutionFile);
			 Plan p = planner.plan(problemFile);
		 } catch (UnreachableGoalException e) {
			 System.out.println("Goal " + e.getUnreachables().toString() + " is unreachable");
		 } catch (ParseException e) {
			 System.out.println(e.getMessage());
		 }
	 }
 
	 /**
	  * Constructs plans over several problem files.
	  * 
	  * @param path
	  *                       The path to the folder containing the problem files.
	  * @param filenamePrefix
	  *                       The prefix of each problem file, usually "pfile".
	  * @param pfileStart
	  *                       The start index which will be appended to the
	  *                       filenamePrefix.
	  * @param pfileEnd
	  *                       The index of the last problem file.
	  * @param usePDDLpostfix
	  *                       Whether to use ".pddl" at the end of the problem files.
	  *                       Domains are assumed to already have this.
	  * @return A totally ordered plan.
	  */
	 public List<Plan> plan(String path, String filenamePrefix, int pfileStart, int pfileEnd, boolean usePDDLpostfix)
			 throws UnreachableGoalException, ParseException {
		 List<Plan> plans = new ArrayList<Plan>(pfileEnd - pfileStart);
		 for (int i = pfileStart; i < pfileEnd; i++) {
			 String postfix = "" + i;
			 if (i < 10)
				 postfix = "0" + i;
			 if (usePDDLpostfix)
				 postfix = postfix + ".pddl";
 
			 File pfile = new File(path + "/" + filenamePrefix + postfix);
			 plans.add(this.plan(pfile));
		 }
 
		 return plans;
	 }
 
	 /**
	  * Construct a plan from the ground problem provided. This method foregoes
	  * any parsing required by plan(File).
	  * 
	  * @param gproblem
	  *                 A grounded problem.
	  * @return A totally ordered plan.
	  */
	 public Plan plan(GroundProblem gproblem) throws UnreachableGoalException {
		 return doPlan(gproblem);
	 }
 
	 /**
	  * Construct a plan for the single problem file provided. Obviously this
	  * problem must be intended for the domain associated with this object. @see
	  * JavaFF.getDomainFile(). Note- This method should only be called if there
	  * exists no UngroundProblem or GroundProblem instance in the program.
	  * Otherwise, use plan(GroundProblem, String).
	  * 
	  * @param pFile
	  *              The file to parse.
	  * @return A totally ordered plan.
	  */
	 public Plan plan(File pFile) throws UnreachableGoalException,
			 ParseException {
		 Plan plan = this.doFilePlan(pFile);
 
		 if (plan != null) {
			 if (useOutputFile != null) {
				 this.writePlanToFile(plan, useOutputFile);
				 System.out.println("Plan written to "
						 + useOutputFile.getAbsolutePath());
			 }
		 }
 
		 return plan;
	 }
 
	 protected List<GroundFact> getGoalOrderings(GroundFact goal) {
 
		 // TODO add the goal-ordering extraction as explained on page 21 of the FF paper
		 // To do this, need to find out what Jorg means by "the planner looks at all
		 // pairs of goals and decides
		 // heuristically whether there is an ordering constraint between them". Quite
		 // what "heuristically" means
		 // is anyones guess.
		 return new ArrayList<GroundFact>(0);
 
	 }
 
	 protected boolean isGoalValid(GroundProblem problem) {
		 Set<? extends Fact> facts = problem.getGoal().getFacts();
 
		 for (Fact f : facts) {
			 if (f instanceof Not) {
				 // if (problem.reachableFacts.contains(((Not)f).literal) ==
				 // false)
				 // return false;
				 if (problem.getReachableFacts().contains(((Not) f)) == false)
					 return false;
			 } else
			 // TODO checks for Quantified facts etc.
			 {
				 if (problem.getReachableFacts().contains(f) == false)
					 return false;
			 }
		 }
 
		 return true;
	 }
 
	 protected Plan doSTRIPSPlan(GroundProblem ground)
			 throws UnreachableGoalException {
		 STRIPSState initialState = ground.recomputeSTRIPSInitialState();
 
		 return this.performPlanning(ground, initialState);
	 }
 
	 protected TotalOrderPlan performPlanning(GroundProblem ground, State initialState) throws UnreachableGoalException {
		 // add goal weighting here
		 // ArrayList<String> goallist = ground.getGoal().toString();
		 goalsNeeded = ground.getGoal().toString();
 
		 String goalString = ground.getGoal().toString();
 
		 // Extract the substrings between the parentheses
		 Pattern pattern = Pattern.compile("\\((.*?)\\)");
		 Matcher matcher = pattern.matcher(goalString);
 
		 // Create an ArrayList to store the goal elements
		 ArrayList<String> goallist = new ArrayList<>();
 
		 // Add each element from the matcher to the ArrayList
		 while (matcher.find()) {
			 goallist.add(matcher.group(1).trim());
		 }
		
		 System.out.println(goallist);
		 Set goalWeights = new HashSet();
 
		 long startTime = System.nanoTime();
		 long afterBFSPlanning = 0, afterEHCPlanning = 0, afterHCPlanning = 0;
		 State originalInitState = (State) initialState.clone();
		 State goalState = null;
		 TotalOrderPlan plan = null;
		 System.out.println("Goal is: " + ground.getGoal().toString());
 
		 double planningEHCTime = 0;
		 double planningBFSTime = 0;
		 double planningHCTime = 0;
		 System.out.println(this.isUseUser());
 
		 if (this.isUseUser()) {
			 System.out.println("Running FF with  User...");
			 goalState = this.performUser(initialState);
			 afterHCPlanning = System.nanoTime();
			 planningHCTime = (afterHCPlanning - startTime) / JavaFF.Nanos;
 
		 }
		 else if (this.isUseEHC()) {
			 System.out.println("Running FF with EHC...");
			 goalState = this.performEHC(initialState, true);
			 afterEHCPlanning = System.nanoTime();
			 planningEHCTime = (afterEHCPlanning - startTime) / JavaFF.Nanos;
		 }
 
		 else if (this.isUseHC()) {
			 System.out.println("Running FF with HC...");
			 goalState = this.performHillClimbing(initialState);
			 afterHCPlanning = System.nanoTime();
			 planningHCTime = (afterHCPlanning - startTime) / JavaFF.Nanos;
		 }
 
		 
 
		 if (goalState != null) {
			 System.out.println("Found EHC plan: ");
			 plan = (TotalOrderPlan) goalState.getSolution();
		 } else if (this.isUseBFS()) {
			 initialState = (State) originalInitState.clone();
			 System.out.println("Running FF with BFS...");
			 goalState = this.performEHC(initialState, false);
			 afterBFSPlanning = System.nanoTime();
			 planningBFSTime = (afterBFSPlanning - afterEHCPlanning) / JavaFF.Nanos;
 
			 if (goalState != null) {
				 plan = (TotalOrderPlan) goalState.getSolution();
				 this.goalAcheived = true;
				 System.out.println("Found BFS plan: ");
 
			 }
 
		 }
		 TimeStampedPlan tsp = null;
		 if (plan != null) {
			 System.out.println("Final plan...");
			 // plan.print(planOutput);
			 // ***************0*****************
			 // Schedule a plan
			 // ********************************
			 int actionCounter = 1;
			 tsp = new TimeStampedPlan(ground.getGoal());
			 for (Object a : plan.getActions()) {
				 String actionString = ((Action) a).toString();
				 // System.out.println("hello world " + actionString);
 
				 // Extract the action name between the parentheses
 
				 tsp.addAction((Action) a, new BigDecimal(actionCounter++));
 
				 if (goallist.stream().anyMatch(goal -> actionString.contains(goal))
						 && !goalWeights.contains(actionString)) {// track the first occurrence of each goal in the plan
 
					 // goalWeights.add(actionString);
					 // System.out.println("goal found!");
					 // score += 10 / actionCounter;
 
				 }
			 }
			 double schedulingTime = 0;
			 if (tsp != null) {
 
				 tsp.print(planOutput);
				 System.out
						 .println("Final plan length is " + tsp.actions.size());
			 }
 
			 infoOutput.println("EHC Plan Time = " + planningEHCTime + "sec");
			 infoOutput.println("HC Plan Time = " + planningHCTime + "sec");
			 infoOutput.println("BFS Plan Time = " + planningBFSTime + "sec");
			 infoOutput.println("Scheduling Time = " + schedulingTime + "sec");
			 goalState.isGoalSatisfied();
			 float score = 0.0f;
			 for (Integer step : stepWhenTrue) {
				 score += (float) 10.0f / step;
			 }
			 sscore = score;
			 infoOutput.println("Score = " + score);
			 System.out.println(stepWhenTrue);
		 } else {
			 System.out.println("No plan found");
		 }
		 this.p = plan;
		 return plan;
	 }
 
	 protected Plan doMetricPlan(GroundProblem ground)
			 throws UnreachableGoalException {
		 // construct init
		 Set na = new HashSet();
		 Set ni = new HashSet();
		 Iterator ait = ground.getActions().iterator();
		 while (ait.hasNext()) {
			 Action act = (Action) ait.next();
			 if (act instanceof InstantAction) {
				 na.add(act);
				 ni.add(act);
			 } else if (act instanceof DurativeAction) {
				 DurativeAction dact = (DurativeAction) act;
				 na.add(dact.startAction);
				 na.add(dact.endAction);
				 ni.add(dact.startAction);
			 }
		 }
 
		 Metric metric;
		 if (ground.getMetric() == null)
			 metric = new Metric(MetricType.Minimize, new NumberFunction(0));
		 else
			 metric = ground.getMetric();
 
		 // MetricState ms = new MetricState(ni, ground.mstate.facts,
		 // ground.goal,
		 // ground.functionValues, metric);
		 // System.out.println("About to create gp");
		 // GroundProblem gp = new GroundProblem(na, ground.mstate.facts,
		 // ground.goal,
		 // ground.functionValues, metric);
		 // gp.getMetricInitialState();
		 // System.out.println("Creating RPG");
		 // ms.setRPG(new RelaxedMetricPlanningGraph(gp));
		 MetricState initialState = ground.recomputeMetricInitialState();
 
		 return this.performPlanning(ground, initialState);
	 }
 
	 protected Plan doTemporalPlan(GroundProblem ground)
			 throws UnreachableGoalException {
		 // construct init
		 Set na = new HashSet();
		 Set ni = new HashSet();
		 Iterator ait = ground.getActions().iterator();
		 while (ait.hasNext()) {
			 Action act = (Action) ait.next();
			 if (act instanceof InstantAction) {
				 na.add(act);
				 ni.add(act);
			 } else if (act instanceof DurativeAction) {
				 DurativeAction dact = (DurativeAction) act;
				 na.add(dact.startAction);
				 na.add(dact.endAction);
				 ni.add(dact.startAction);
			 }
		 }
 
		 Metric metric;
		 if (ground.getMetric() == null)
			 metric = new Metric(MetricType.Minimize, new NumberFunction(0));
		 else
			 metric = ground.getMetric();
 
		 System.out.println("About to create init tmstate");
		 // TemporalMetricState ts = new TemporalMetricState(ni,
		 // ground.tmstate.facts, ground.goal,
		 // ground.functionValues, metric);
		 // System.out.println("About to create gp");
		 // GroundProblem gp = new GroundProblem(na, ground.tmstate.facts,
		 // ground.goal,
		 // ground.functionValues, metric);
		 // gp.getTemporalMetricInitialState();
		 // System.out.println("Creating RPG");
		 // ts.setRPG(new RelaxedTemporalMetricPlanningGraph(gp));
		 TemporalMetricState initialState = ground
				 .recomputeTemporalMetricInitialState();
 
		 return this.performPlanning(ground, initialState);
	 }
 
	 protected Plan doPlan(GroundProblem ground) throws UnreachableGoalException {
		 if (ground.getRequirements().contains(Requirement.ADL)) {
			 System.out.println("Decompiling ADL...");
			 int previousActionCount = ground.getActions().size();
			 int naiveAdlActionCount = ground.decompileADL();
			 int adlActionCount = ground.getActions().size();
			 System.out.println(previousActionCount + " actions before ADL, "
					 + adlActionCount + " after (" + naiveAdlActionCount + " generated in total)");
		 }
 
		 // filtering has to be done after decompiling the ADL because the RPG method
		 // used only understands
		 // STRIPS facts
		 System.out.println("Performing RPG reachability analysis...");
		 ground.filterReachableFacts();
 
		 // Select the correct problem type to generate -- doing a STRIPS only domain
		 // using a Temporal approach will
		 // cause massive overheads which may stop the problem being solvable. This is
		 // because the type of RPG constructed
		 // is irectly linked to the type of the initial state. That is, STRIPS states
		 // will produce RPGs and further STRIPSStates.
		 // Metric states produce MetricRPGs and more Metric states, etc.
		 if (ground.isMetric() == false && ground.isTemporal() == false) {
			 return this.doSTRIPSPlan(ground);
		 } else if (ground.isMetric() == true && ground.isTemporal() == false) {
			 throw new IllegalArgumentException(
					 "Metric planning currently not supported");
			 // return this.doMetricPlan(ground);
		 } else
		 // temporal subsumes metric so no need to check for metric == false &&
		 // temporal == true
		 {
			 throw new IllegalArgumentException(
					 "Temporal planning currently not supported");
			 // return this.doTemporalPlan(ground);
		 }
	 }
 
	 protected Plan doFilePlan(File pFile) throws UnreachableGoalException,
			 ParseException {
		 // ********************************
		 // Parse and Ground the Problem
		 // ********************************
		 UngroundProblem unground = PDDL21parser.parseFiles(this.domainFile,
				 pFile);
 
		 if (unground == null) {
			 System.err.println("Parsing error - see console for details");
			 return null;
		 }
 
		 if (this.checkRequirements(unground.requirements) == false)
			 throw new ParseException(
					 "Domain contains unsupported PDDL requirements. JavaFF currently"
							 + " supports the following\n"
							 + JavaFF.PDDLRequirementsSupported.toString());
 
		 System.out.println("Grounding...");
		 GroundProblem ground = unground.ground();
		 System.out.println("Grounding complete");
 
		 return this.doPlan(ground);
	 }
 
	 
	 /**
	  * This performs the same job as Action.isApplicable() intuitively would --
	  * but now that ADL is supported, the isApplicable() method inside
	  * propositions and in particular, Nots, assumes that the state is valid in
	  * the context of the domain model. As we want to use the RPG, this is of no
	  * use, because relaxed states are not reachable.
	  * 
	  * @param curr
	  * @return
	  */
	 protected boolean isActionReachable(STRIPSState curr, Action a) {
		 for (Fact pc : a.getPreconditions()) {
			 if (pc instanceof Not) {
				 Not npc = (Not) pc;
				 if (npc.getLiteral() instanceof Not == false) {
					 // if (curr.getFalseFacts().contains(pc) == false)
					 // return false;
 
					 if (!curr.isTrue(pc) == false)
						 return false;
				 } else // PC is a Not, and its internal literal is also a Not
				 {
					 // only tests 2-nested Nots
					 if (curr.getTrueFacts().contains(
							 ((Not) ((Not) pc).getLiteral()).getLiteral()) == false)
						 return false;
				 }
			 } else {
				 if (curr.isTrue(pc) == false)
					 return false;
			 }
 
		 }
 
		 return true;
	 }
 
	 /**
	  * This performs the same job as Action.isApplicable() intuitively would --
	  * but now that ADL is supported, the isApplicable() method inside
	  * propositions and in particular, Nots, assumes that the state is valid in
	  * the context of the domain model. As we want to use the RPG, this is of no
	  * use, because relaxed states are not reachable.
	  * 
	  * @param curr
	  * @return
	  */
	 protected boolean isActionReachable(MetricState curr, Action a) {
		 if (this.isActionReachable((STRIPSState) curr, a) == false)
			 return false;
 
		 // TODO add check for functions
		 return true;
	 }
 
	 /**
	  * This performs the same job as Action.isApplicable() intuitively would --
	  * but now that ADL is supported, the isApplicable() method inside
	  * propositions and in particular, Nots, assumes that the state is valid in
	  * the context of the domain model. As we want to use the RPG, this is of no
	  * use, because relaxed states are not reachable.
	  * 
	  * @param curr
	  * @return
	  */
	 protected boolean isActionReachable(TemporalMetricState curr, Action a) {
		 if (this.isActionReachable((MetricState) curr, a) == false)
			 return false;
 
		 // TODO add check for temporal aspects
		 return true;
	 }
 
	 protected State performUser(State initialState) {
 
		 State goalState = null;
 
		 infoOutput.println("Performing search using User");
		 // create a Best-First Searcher
		 UserSearch BFS = new UserSearch(initialState);
		 BFS.setFilter(NullFilter.getInstance());
		 goalState = BFS.search();
 
		 if (goalState == null)
			 infoOutput.println("Failed to find solution using User");
		 // }
		 return goalState;
	 }
 
	 protected State performHillClimbing(State initialState) {
 
		 State goalState = null;
		 // System.out.println("INIT "+initialState.getTrueFacts() +
		 // "\nGOAL "+initialState.goal);
		 if (useHC) {
			 if (p != null) {
				 infoOutput.println("Extending");
				 Search HCS = new HillClimbingSearch(initialState, p, 0);
				 HCS.setFilter(RandomThreeFilter.getInstance());
				 goalState = HCS.search();
				 if (goalState != null) {
					 System.err.println("found");
					 return goalState;
				 } else
					 infoOutput.println("Failed to find solution using HC");
			 } else {
				 infoOutput.println("Performing search using HC with standard helpful action filter");
				 Search HCS = new HillClimbingSearch(initialState, 0);
				 HCS.setFilter(RandomThreeFilter.getInstance());
				 goalState = HCS.search();
				 if (goalState != null) {
					 System.err.println("found");
					 return goalState;
				 } else
					 infoOutput.println("Failed to find solution using HC");
			 }
 
		 } else {
 
			 if (p != null) {
				 infoOutput.println("Extending");
				 // create a Best-First Searcher
				 UserSearch BFS = new UserSearch(initialState, p);
				 BFS.setFilter(RandomThreeFilter.getInstance());
				 goalState = BFS.search();
 
				 if (goalState == null)
					 infoOutput.println("Failed to find solution using BFS");
			 } else {
 
				 infoOutput.println("Performing search using BFS");
				 // create a Best-First Searcher
				 UserSearch BFS = new UserSearch(initialState, p);
				 BFS.setFilter(RandomThreeFilter.getInstance());
				 goalState = BFS.search();
 
				 if (goalState == null)
					 infoOutput.println("Failed to find solution using BFS");
			 }
		 }
		 return goalState;
	 }
 
	 protected State performEHC(State initialState, boolean useEHC) {
 
		 State goalState = null;
		 // System.out.println("INIT "+initialState.getTrueFacts() +
		 // "\nGOAL "+initialState.goal);
		 if (useEHC) {
			 if (p != null) {
				 infoOutput.println("extending");
				 Search EHCS = new EnforcedHillClimbingSearch(initialState);
				 EHCS.setFilter(RandomThreeFilter.getInstance());
				 goalState = EHCS.search();
				 if (goalState != null)
					 return goalState;
				 else
					 infoOutput.println("Failed to find solution using EHC");
			 } else {
				 infoOutput.println("Performing search using EHC with standard helpful action filter");
				 Search EHCS = new EnforcedHillClimbingSearch(initialState);
				 EHCS.setFilter(RandomThreeFilter.getInstance());
				 goalState = EHCS.search();
				 if (goalState != null)
					 return goalState;
				 else
					 infoOutput.println("Failed to find solution using EHC");
			 }
 
		 }
 
		 else {
			 infoOutput.println("Performing search using BFS");
			 // create a Best-First Searcher
			 BFSoriginal BFS = new BFSoriginal(initialState, goalsNeeded);
			 BFS.similarity = similarityMode;
			 BFS.setFilter(NullFilter.getInstance());
			 goalState = BFS.search();
			 stepWhenTrue = BFS.getStepWhenTrue();
			 if (goalState == null)
				 infoOutput.println("Failed to find solution using BFS");
		 }
		 return goalState;
	 }
 
	 protected void writePlanToFile(Plan plan, File fileOut) {
		 try {
			 System.out.println("plan is " + plan + ", file is " + fileOut);
			 fileOut.delete();
			 fileOut.createNewFile();
 
			 FileOutputStream outputStream = new FileOutputStream(fileOut);
			 PrintWriter printWriter = new PrintWriter(outputStream);
			 plan.print(printWriter);
			 printWriter.close();
		 } catch (FileNotFoundException e) {
			 errorOutput.println(e);
			 e.printStackTrace();
		 } catch (IOException e) {
			 errorOutput.println(e);
			 e.printStackTrace();
		 }
 
	 }
 
	 public File getDomainFile() {
		 return domainFile;
	 }
 
	 public void setDomainFile(File domainFile) {
		 this.domainFile = domainFile;
	 }
 
	 public boolean isUseEHC() {
		 return useEHC;
	 }
 
	 public boolean isUseHC() {
		 return useHC;
	 }
 
	 public boolean isUseUser() {
		 return useUser;
	 }
 
	 public void setUseHC(boolean useHC) {
		 this.useHC = useHC;
	 }
 
	 public void setUseEHC(boolean useEHC) {
		 this.useEHC = useEHC;
	 }
 
	 public boolean isUseBFS() {
		 return useBFS;
	 }
 
	 public void setUseBFS(boolean useBFS) {
		 this.useBFS = useBFS;
	 }
 
	 public void setUseUser(boolean useUser) {
		 this.useUser = useUser;
	 }
 
	 public TotalOrderPlan getPlan() {
		 return this.p;
	 }
 
	 public void setPlan(TotalOrderPlan pl) {
		 this.p = pl;
	 }
 
	 public void setGoal(Boolean f) {
		 this.goalAcheived = false;
	 }
 
	 public Boolean getGoal() {
		 return this.goalAcheived;
	 }
 
	 // return sore for each individual goal
 
	 public List<Integer> getGoalSteps() {
 
		 return this.stepWhenTrue;
	 }
 
	 // return total score
 
	 public float getTotalScore() {
 
		 return this.sscore;
	 }
 
 }
 