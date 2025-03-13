package com.leucine.wizard.prompts;

/**
 * Provides prompts for decomposing objectives into goals, subgoals, and actions.
 * This prompt is used to guide the LLM in breaking down high-level objectives into
 * actionable components that can be executed autonomously by a system.
 */
public class ActionDecompositionPrompt {
    
    /**
     * Returns the user prompt template for objective decomposition with actions.
     * This template includes placeholders that will be replaced with actual values.
     * 
     * @return The user prompt template as a String
     */
    public static String getUserPromptTemplate() {
        return """
               Standard Procedure:
               ```%s```
               
               Now while keeping scope to the objective given below Objective: ```%s```
               I want to decompose this objective into goals, it's sub goals and their actions. Each action is a unit level work that the system can perform in order to progress further in the goal. The core idea is that an objective when broken down into meaningful goals can be executed autonomously by a system which also has LLM capability. It may have some checkpoints where it may require user approval before proceeding further. Ensure each action is detailed enough.
               
               Can you decompose my objective and give me in JSON:
               
               ```json
               {
                 "goals": [
                   {
                     "name": "Goal name",
                     "description": "Goal description",
                     "subgoals": [
                       {
                         "name": "Subgoal name",
                         "description": "Subgoal description",
                         "actions": [
                           "Detailed action 1",
                           "Detailed action 2",
                           "Detailed action 3"
                         ]
                       }
                     ]
                   }
                 ]
               }
               ```
               """;
    }
    
    /**
     * Formats the user prompt by replacing the placeholders with actual values.
     * 
     * @param objective The objective to decompose
     * @param standardProcedure The standard procedure for the objective
     * @return The formatted user prompt as a String
     */
    public static String formatUserPrompt(String objective, String standardProcedure) {
        return String.format(getUserPromptTemplate(), standardProcedure, objective);
    }
}