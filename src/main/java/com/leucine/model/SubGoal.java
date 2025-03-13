package com.leucine.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a subgoal within a goal.
 * Subgoals are simpler than goals and cannot have their own subgoals.
 */
public class SubGoal {
    private String id;
    private String description;
    private List<Action> actions;
    
    /**
     * Creates a new subgoal with the specified description.
     * 
     * @param description The description of the subgoal
     */
    public SubGoal(String description) {
        this.id = UUID.randomUUID().toString();
        this.description = description;
        this.actions = new ArrayList<>();
    }
    
    /**
     * Creates a new subgoal with the specified ID and description.
     * 
     * @param id The ID of the subgoal
     * @param description The description of the subgoal
     */
    public SubGoal(String id, String description) {
        this.id = id;
        this.description = description;
        this.actions = new ArrayList<>();
    }
    
    /**
     * Adds an action to this subgoal.
     * 
     * @param action The action to add
     * @return This subgoal instance for method chaining
     */
    public SubGoal addAction(Action action) {
        this.actions.add(action);
        return this;
    }
    
    // Getters and setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }

    public List<Action> getActions() {
        return actions;
    }
    
    public void setActions(List<Action> actions) {
        this.actions = Objects.requireNonNull(actions, "Actions cannot be null");
    }
    
    @Override
    public String toString() {
        return "SubGoal{" +
                "id='" + id + '\'' +
                ", description='" + description + '\'' +
                ", actions=" + actions +
                '}';
    }
}