package com.leucine.demo;

import com.leucine.model.Agent;
import com.leucine.model.Action;
import com.leucine.model.Goal;
import com.leucine.model.SubGoal;
import com.leucine.wizard.model.WizardSession;
import com.leucine.wizard.service.AgentCreationService;
import com.leucine.wizard.service.ObjectiveDecompositionService;

import java.io.IOException;

/**
 * Demo class to demonstrate the usage of ObjectiveDecompositionService.
 * This class contains a main method that decomposes a hardcoded objective
 * into goals and subgoals using the Claude AI service.
 */
public class ObjectiveDecompositionDemo {

    /**
     * Main method to run the demo.
     * 
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        System.out.println("Starting Objective Decomposition Demo...");
        
        try {
            // Get the singleton instance of the agent creation service
            AgentCreationService service = AgentCreationService.getInstance();
            
            // Hardcoded objective for demonstration
            String objective = "In pharma manufacturing context, investigate a deviation given a deviation description and find its root cause";
            
            System.out.println("Decomposing objective: " + objective);
            System.out.println("This may take a few moments as it calls the Claude API...");
            
            // Start a new wizard session
            String sessionId = service.startNewSession();
            System.out.println("Started new wizard session with ID: " + sessionId);
            
            // Process the objective
            WizardSession session = service.processObjective(sessionId, objective);
            System.out.println("Processed objective, current state: " + session.getState());
            
            // Complete the agent creation process
            service.reviewAgent(sessionId);
            Agent agent = service.completeAgentCreation(sessionId);
            
            // Print the results
            System.out.println("\n=== Decomposition Results ===");
            System.out.println("Agent ID: " + agent.getId());
            System.out.println("Agent Name: " + agent.getName());
            System.out.println("Objective: " + agent.getObjective());
            System.out.println("\nGoals and Subgoals:");

            // Verify that the agent ID matches the session ID
            System.out.println("\nVerification: Agent ID matches Session ID: " + agent.getId().equals(sessionId));
            
            // Print all goals and their subgoals
            for (int i = 0; i < agent.getGoals().size(); i++) {
                Goal goal = agent.getGoals().get(i);
                System.out.println("\nGoal " + (i + 1) + ": " + goal.getDescription());
                
                // Print subgoals if any
                if (goal.getSubgoals() != null && !goal.getSubgoals().isEmpty()) {
                    for (int j = 0; j < goal.getSubgoals().size(); j++) {
                        SubGoal subgoal = goal.getSubgoals().get(j);
                        System.out.println("  Subgoal " + (i + 1) + "." + (j + 1) + ": " + subgoal.getDescription());
                        
                        // Print actions if any
                        if (subgoal.getActions() != null && !subgoal.getActions().isEmpty()) {
                            System.out.println("    Actions:");
                            for (int k = 0; k < subgoal.getActions().size(); k++) {
                                Action action = subgoal.getActions().get(k);
                                System.out.println("      " + (i + 1) + "." + (j + 1) + "." + (k + 1) + ": " + action.getDescription());
                            }
                        }
                    }
                }
            }
            
            System.out.println("\nDemo completed successfully!");
            
        } catch (IOException e) {
            System.err.println("Error communicating with Claude API: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}