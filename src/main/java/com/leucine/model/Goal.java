package com.leucine.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents a goal that the agent aims to achieve.
 */
public class Goal {
    private String id;
    private String description;
    private List<SubGoal> subgoals;
    
    /**
     * Creates a new goal with the specified description.
     * 
     * @param description The description of the goal
     */
    public Goal(String description) {
        this.id = UUID.randomUUID().toString();
        this.description = description;
        this.subgoals = new ArrayList<>();
    }
    
    /**
     * Adds a subgoal to this goal.
     * 
     * @param subgoal The subgoal to add
     * @return This goal instance for method chaining
     */
    public Goal addSubgoal(SubGoal subgoal) {
        this.subgoals.add(subgoal);
        return this;
    }
    
    // Getters and setters
    public String getId() {
        return id;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public List<SubGoal> getSubgoals() {
        return subgoals;
    }
    
    public void setSubgoals(List<SubGoal> subgoals) {
        this.subgoals = subgoals;
    }
    
    @Override
    public String toString() {
        return "Goal{" +
                "id='" + id + '\'' +
                ", description='" + description + '\'' +
                ", subgoals=" + subgoals +
                '}';
    }
}