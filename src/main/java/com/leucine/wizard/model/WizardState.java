package com.leucine.wizard.model;

/**
 * Enum representing the different states of the agent creation wizard.
 * These states track the progress of the wizard through the agent creation process.
 */
public enum WizardState {
    /**
     * Initial state when the wizard is started
     */
    INITIAL,
    
    /**
     * State after the objective has been entered
     */
    OBJECTIVE_ENTERED,
    
    /**
     * State after the objective has been decomposed into goals and subgoals
     */
    OBJECTIVE_DECOMPOSED,
    
    /**
     * State after the agent has been reviewed and confirmed
     */
    AGENT_REVIEWED,
    
    /**
     * Final state when the agent creation is complete
     */
    COMPLETED,
    
    /**
     * State when an error has occurred during the wizard process
     */
    ERROR
}