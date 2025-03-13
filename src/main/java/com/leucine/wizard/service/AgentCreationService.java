package com.leucine.wizard.service;

import com.leucine.model.Agent;
import com.leucine.wizard.model.WizardSession;
import com.leucine.wizard.model.WizardState;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Singleton service for managing agent creation wizard sessions.
 * This service maintains a mapping of agent IDs to wizard sessions and
 * provides methods for creating and managing agents through the wizard process.
 */
public class AgentCreationService {
    
    private static AgentCreationService instance;
    
    // Map of agent IDs to wizard sessions
    private final Map<String, WizardSession> sessions;
    
    // Service for decomposing objectives
    private final ObjectiveDecompositionService decompositionService;
    
    /**
     * Private constructor to enforce singleton pattern.
     */
    private AgentCreationService() {
        this.sessions = new ConcurrentHashMap<>();
        this.decompositionService = new ObjectiveDecompositionService();
    }
    
    /**
     * Gets the singleton instance of the AgentCreationService.
     * 
     * @return The singleton instance
     */
    public static synchronized AgentCreationService getInstance() {
        if (instance == null) {
            instance = new AgentCreationService();
        }
        return instance;
    }
    
    /**
     * Starts a new wizard session for creating an agent.
     * 
     * @return The ID of the new session
     */
    public String startNewSession() {
        String sessionId = UUID.randomUUID().toString();
        WizardSession session = new WizardSession(sessionId);
        sessions.put(sessionId, session);
        return sessionId;
    }
    
    /**
     * Gets a wizard session by its ID.
     * 
     * @param sessionId The ID of the session to get
     * @return The wizard session, or null if no session exists with the given ID
     */
    public WizardSession getSession(String sessionId) {
        return sessions.get(sessionId);
    }
    
    /**
     * Processes an objective for a wizard session.
     * This will decompose the objective into goals and subgoals and update the session.
     * 
     * @param sessionId The ID of the session
     * @param objective The objective to process
     * @return The updated wizard session
     * @throws IllegalArgumentException If no session exists with the given ID
     * @throws IOException If there's an error communicating with the Claude API
     */
    public WizardSession processObjective(String sessionId, String objective) throws IOException {
        WizardSession session = getSession(sessionId);
        if (session == null) {
            throw new IllegalArgumentException("No session found with ID: " + sessionId);
        }
        
        try {
            // Update the session state
            session.updateState(WizardState.OBJECTIVE_ENTERED);
            
            // Decompose the objective
            Agent agent = decompositionService.decomposeObjective(objective);
            
            // Set the agent ID to match the session ID
            agent.setId(sessionId);
            
            
            // Update the session with the agent and new state
            session.setAgent(agent)
                  .updateState(WizardState.OBJECTIVE_DECOMPOSED);
            
            return session;
        } catch (Exception e) {
            // Handle any errors
            session.setError("Error processing objective: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Reviews and confirms an agent in a wizard session.
     * 
     * @param sessionId The ID of the session
     * @return The updated wizard session
     * @throws IllegalArgumentException If no session exists with the given ID or the session is not in the correct state
     */
    public WizardSession reviewAgent(String sessionId) {
        WizardSession session = getSession(sessionId);
        if (session == null) {
            throw new IllegalArgumentException("No session found with ID: " + sessionId);
        }
        
        if (session.getState() != WizardState.OBJECTIVE_DECOMPOSED) {
            throw new IllegalArgumentException("Session is not in the correct state for review. Current state: " + session.getState());
        }
        
        // Update the session state
        session.updateState(WizardState.AGENT_REVIEWED);
        
        return session;
    }
    
    /**
     * Completes the agent creation process for a wizard session.
     * 
     * @param sessionId The ID of the session
     * @return The created agent
     * @throws IllegalArgumentException If no session exists with the given ID or the session is not in the correct state
     */
    public Agent completeAgentCreation(String sessionId) {
        WizardSession session = getSession(sessionId);
        if (session == null) {
            throw new IllegalArgumentException("No session found with ID: " + sessionId);
        }
        
        if (session.getState() != WizardState.AGENT_REVIEWED) {
            throw new IllegalArgumentException("Session is not in the correct state for completion. Current state: " + session.getState());
        }
        
        // Update the session state
        session.updateState(WizardState.COMPLETED);
        
        return session.getAgent();
    }
    
    /**
     * Removes a wizard session.
     * 
     * @param sessionId The ID of the session to remove
     * @return true if the session was removed, false if no session existed with the given ID
     */
    public boolean removeSession(String sessionId) {
        return sessions.remove(sessionId) != null;
    }
    
    /**
     * Gets the number of active wizard sessions.
     * 
     * @return The number of active sessions
     */
    public int getActiveSessionCount() {
        return sessions.size();
    }
}