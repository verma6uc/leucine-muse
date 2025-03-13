package com.leucine.wizard.service;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.leucine.model.Agent;
import com.leucine.model.Action;
import com.leucine.model.Goal;
import com.leucine.model.SubGoal;
import com.leucine.utils.ClaudeClient;
import com.leucine.wizard.prompts.StandardProcedurePrompt;
import com.leucine.wizard.prompts.ActionDecompositionPrompt;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service for decomposing high-level objectives into structured goals, subgoals, and actions
 * using Claude AI.
 */
public class ObjectiveDecompositionService {
    
    private final ClaudeClient claudeClient;
    private final Gson gson;
    private String currentStandardProcedure;
    
    /**
     * Constructs a new ObjectiveDecompositionService with default settings.
     */
    public ObjectiveDecompositionService() {
        this.claudeClient = new ClaudeClient.Builder()
                .withTemperature(0.7) // Lower temperature for more deterministic outputs
                .build();
        this.currentStandardProcedure = null;
        this.gson = new Gson();
    }
    
    /**
     * Constructs a new ObjectiveDecompositionService with a custom Claude client.
     * 
     * @param claudeClient The Claude client to use for API calls
     */
    public ObjectiveDecompositionService(ClaudeClient claudeClient) {
        this.claudeClient = claudeClient;
        this.currentStandardProcedure = null;
        this.gson = new Gson();
    }
    
    /**
     * Decomposes an objective into goals, subgoals, and actions using Claude AI.
     * First gets the standard procedure, then uses it to guide the decomposition.
     * 
     * @param objective The objective to decompose
     * @return An Agent object containing the decomposed goals, subgoals, and actions
     * @throws IOException If there's an error communicating with the Claude API
     * @throws JsonSyntaxException If the response cannot be parsed as valid JSON
     */
    public Agent decomposeObjective(String objective) throws IOException, JsonSyntaxException {
        // Step 1: Get the standard procedure
        String standardProcedure = getStandardProcedure(objective);
        
        // Step 2: Decompose the objective using the standard procedure
        return decomposeWithStandardProcedure(objective, standardProcedure);
    }
    
    /**
     * Gets the standard procedure for an objective using Claude AI.
     * 
     * @param objective The objective to get the standard procedure for
     * @return The standard procedure as a markdown string
     * @throws IOException If there's an error communicating with the Claude API
     */
    private String getStandardProcedure(String objective) throws IOException {
        // Format the user prompt with the objective
        String userPrompt = StandardProcedurePrompt.formatUserPrompt(objective);
        
        // Send the request to Claude
        ClaudeClient.ClaudeResponse response = claudeClient.sendMessage(userPrompt);
        
        // Extract the text content from the response
        this.currentStandardProcedure = response.getTextContent();
        return this.currentStandardProcedure;
    }
    
    /**
     * Decomposes an objective into goals, subgoals, and actions using the standard procedure.
     */
    private Agent decomposeWithStandardProcedure(String objective, String standardProcedure) throws IOException, JsonSyntaxException {
        // Format the user prompt with the objective and standard procedure
        String userPrompt = ActionDecompositionPrompt.formatUserPrompt(objective, standardProcedure);
        
        // Send the request to Claude and parse the response
        ClaudeClient.ClaudeResponse response = claudeClient.sendMessage(userPrompt);
        Agent agent = parseResponse(response.getTextContent(), objective, standardProcedure);
        return agent;
    }
    
    /**
     * Parses the JSON response from Claude into an Agent object with goals and subgoals.
     * 
     * @param responseContent The JSON response from Claude
     * @param originalObjective The original objective that was decomposed
     * @param standardProcedure The standard procedure for achieving the objective
     * @return An Agent object containing the decomposed goals and subgoals
     * @throws JsonSyntaxException If the response cannot be parsed as valid JSON
     */
    @SuppressWarnings("unchecked")
    private Agent parseResponse(String responseContent, String originalObjective, String standardProcedure) throws JsonSyntaxException {
        try {
            // Extract JSON from the response (in case it contains markdown or other text)
            String jsonContent = extractJsonFromResponse(responseContent);
            
            // Parse the JSON response
            Map<String, Object> jsonResponse = gson.fromJson(jsonContent, Map.class);
            
            // Get the objective from the response or use the original
            String responseObjective = (String) jsonResponse.get("objective");
            if (responseObjective == null || responseObjective.trim().isEmpty()) {
                responseObjective = originalObjective;
            }
            
            // Get the agent name from the response or use a default
            String agentName = (String) jsonResponse.get("agentName");
            if (agentName == null || agentName.trim().isEmpty()) {
                agentName = "Agent for " + originalObjective;
            }
            
            // Create a new agent with a UUID, name, and objective
            Agent agent = new Agent(UUID.randomUUID().toString(), agentName, responseObjective);
            
            // Set the standard procedure if available
            agent.setStandardProcedure(standardProcedure);
            
            // Process the goals
            List<Map<String, Object>> goalsList = (List<Map<String, Object>>) jsonResponse.get("goals");
            if (goalsList != null) {
                for (Map<String, Object> goalMap : goalsList) {
                    // Create a new goal
                    String goalDescription = (String) goalMap.get("description");
                    Goal goal = new Goal(goalDescription);
                    
                    // Process subgoals if they exist
                    List<Map<String, Object>> subgoalsList = (List<Map<String, Object>>) goalMap.get("subgoals");
                    if (subgoalsList != null) {
                        for (Map<String, Object> subgoalMap : subgoalsList) {
                            // Create a new subgoal
                            String subgoalDescription = (String) subgoalMap.get("description");
                            String subgoalName = (String) subgoalMap.get("name");
                            SubGoal subgoal = new SubGoal(subgoalDescription);
                            
                            // Set the name if available
                            if (subgoalName != null && !subgoalName.trim().isEmpty()) {
                                subgoal.setDescription(subgoalName + ": " + subgoalDescription);
                            }
                            
                            // Process actions if they exist
                            List<String> actionsList = (List<String>) subgoalMap.get("actions");
                            if (actionsList != null) {
                                for (String actionDescription : actionsList) {
                                    // Create a new action and add it to the subgoal
                                    Action action = new Action(actionDescription);
                                    subgoal.addAction(action);
                                }
                            }
                            
                            goal.addSubgoal(subgoal);
                        }
                    }
                    
                    // Add the goal to the agent
                    agent.addGoal(goal);
                }
            }
            
            return agent;
        } catch (JsonSyntaxException e) {
            throw new JsonSyntaxException("Failed to parse Claude response as JSON: " + responseContent, e);
        }
    }
    
    /**
     * Extracts JSON content from the response, which might contain markdown or other text.
     * 
     * @param response The response from Claude
     * @return The extracted JSON content
     */
    private String extractJsonFromResponse(String response) {
        // If the response is already valid JSON, return it as is
        if (response.trim().startsWith("{") && response.trim().endsWith("}")) {
            return response;
        }
        
        // Try to extract JSON between code blocks
        int startIndex = response.indexOf("{");
        int endIndex = response.lastIndexOf("}") + 1;
        
        if (startIndex >= 0 && endIndex > startIndex) {
            return response.substring(startIndex, endIndex);
        }
        
        // If no JSON is found, return the original response
        return response;
    }
}