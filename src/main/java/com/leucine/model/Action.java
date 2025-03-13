package com.leucine.model;

import java.util.UUID;

/**
 * Represents an atomic action that can be performed by an autonomous system.
 * Actions are the lowest level of the goal hierarchy and represent specific,
 * executable tasks.
 */
public class Action {
    private String id;
    private String description;
    
    /**
     * Creates a new action with the specified description.
     * 
     * @param description The description of the action
     */
    public Action(String description) {
        this.id = UUID.randomUUID().toString();
        this.description = description;
    }
    
    /**
     * Creates a new action with the specified ID and description.
     * 
     * @param id The ID of the action
     * @param description The description of the action
     */
    public Action(String id, String description) {
        this.id = id;
        this.description = description;
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
    
    @Override
    public String toString() {
        return "Action{" +
                "id='" + id + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}