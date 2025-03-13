package com.leucine.wizard.model;

import com.leucine.model.Agent;
import java.time.LocalDateTime;

/**
 * Represents a session for the agent creation wizard.
 * Contains the current state of the wizard and the agent being created.
 */
public class WizardSession {
    
    private final String sessionId;
    private WizardState state;
    private Agent agent;
    private LocalDateTime createdAt;
    private LocalDateTime lastUpdatedAt;
    private String errorMessage;
    
    /**
     * Creates a new wizard session with the specified session ID.
     * 
     * @param sessionId The unique identifier for this session
     */
    public WizardSession(String sessionId) {
        this.sessionId = sessionId;
        this.state = WizardState.INITIAL;
        this.createdAt = LocalDateTime.now();
        this.lastUpdatedAt = this.createdAt;
    }
    
    /**
     * Creates a new wizard session with the specified session ID and agent.
     * 
     * @param sessionId The unique identifier for this session
     * @param agent The agent being created in this session
     */
    public WizardSession(String sessionId, Agent agent) {
        this(sessionId);
        this.agent = agent;
    }
    
    /**
     * Updates the state of the wizard session.
     * 
     * @param state The new state
     * @return This wizard session for method chaining
     */
    public WizardSession updateState(WizardState state) {
        this.state = state;
        this.lastUpdatedAt = LocalDateTime.now();
        return this;
    }
    
    /**
     * Sets the agent for this wizard session.
     * 
     * @param agent The agent
     * @return This wizard session for method chaining
     */
    public WizardSession setAgent(Agent agent) {
        this.agent = agent;
        this.lastUpdatedAt = LocalDateTime.now();
        return this;
    }
    
    /**
     * Sets an error message for this wizard session.
     * Also updates the state to ERROR.
     * 
     * @param errorMessage The error message
     * @return This wizard session for method chaining
     */
    public WizardSession setError(String errorMessage) {
        this.errorMessage = errorMessage;
        this.state = WizardState.ERROR;
        this.lastUpdatedAt = LocalDateTime.now();
        return this;
    }
    
    // Getters
    
    /**
     * Gets the session ID.
     * 
     * @return The session ID
     */
    public String getSessionId() {
        return sessionId;
    }
    
    /**
     * Gets the current state of the wizard.
     * 
     * @return The current state
     */
    public WizardState getState() {
        return state;
    }
    
    /**
     * Gets the agent being created in this session.
     * 
     * @return The agent
     */
    public Agent getAgent() {
        return agent;
    }
    
    /**
     * Gets the creation time of this session.
     * 
     * @return The creation time
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    /**
     * Gets the last update time of this session.
     * 
     * @return The last update time
     */
    public LocalDateTime getLastUpdatedAt() {
        return lastUpdatedAt;
    }
    
    /**
     * Gets the error message if an error occurred.
     * 
     * @return The error message, or null if no error occurred
     */
    public String getErrorMessage() {
        return errorMessage;
    }
    
    /**
     * Checks if this session has an error.
     * 
     * @return true if this session has an error, false otherwise
     */
    public boolean hasError() {
        return state == WizardState.ERROR;
    }
    
    /**
     * Checks if this session is completed.
     * 
     * @return true if this session is completed, false otherwise
     */
    public boolean isCompleted() {
        return state == WizardState.COMPLETED;
    }
}