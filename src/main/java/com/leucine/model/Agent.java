package com.leucine.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents an autonomous agent for deviation investigation.
 */
public class Agent {
    
    private String id;
    private String name;
    private String objective;    // The high-level objective of the agent
    private String standardProcedure; // The standard procedure for achieving the objective
    private List<Goal> goals;    // List of goals
    
    /**
     * Creates a new agent with default settings.
     */
    public Agent() {
        this.id = UUID.randomUUID().toString();
        this.goals = new ArrayList<>();
    }
    
    /**
     * Creates a new agent with the specified name.
     * 
     * @param name The name of the agent
     */
    public Agent(String name) {
        this.id = UUID.randomUUID().toString();
        this.goals = new ArrayList<>();
        this.name = name;
    }
    
    /**
     * Creates a new agent with the specified ID, name, and objective.
     * 
     * @param id The ID of the agent
     * @param name The name of the agent
     * @param objective The high-level objective of the agent
     */
    public Agent(String id, String name, String objective) {
        this.id = id;
        this.goals = new ArrayList<>();
        this.name = name;
        this.objective = objective;
    }

    /**
     * Creates a new agent with the specified ID, name, objective, and standard procedure.
     * 
     * @param id The ID of the agent
     * @param name The name of the agent
     * @param objective The high-level objective of the agent
     * @param standardProcedure The standard procedure for achieving the objective
     */
    public Agent(String id, String name, String objective, String standardProcedure) {
        this(id, name, objective);
        this.standardProcedure = standardProcedure;
    }
    
    /**
     * Adds a goal to the agent's goal list.
     * 
     * @param goal The goal to add
     * @return This agent instance for method chaining
     */
    public Agent addGoal(Goal goal) {
        this.goals.add(goal);
        return this;
    }
    
    // Getters and setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getObjective() {
        return objective;
    }
    
    public void setObjective(String objective) {
        this.objective = objective;
    }
    
    public String getStandardProcedure() {
        return standardProcedure;
    }
    
    public void setStandardProcedure(String standardProcedure) {
        this.standardProcedure = standardProcedure;
    }
    
    public List<Goal> getGoals() {
        return goals;
    }
    
    public void setGoals(List<Goal> goals) {
        this.goals = goals;
    }
    
    @Override
    public String toString() {
        return "Agent{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", objective='" + objective + '\'' +
                ", standardProcedure='" + (standardProcedure != null ? "present" : "null") + '\'' +
                ", goals=" + goals +
                '}';
    }
}