package com.leucine.servlet;

import com.google.gson.Gson;
import com.leucine.model.Agent;
import com.leucine.wizard.model.WizardSession;
import com.leucine.wizard.model.WizardState;
import com.leucine.wizard.service.AgentCreationService;
import com.leucine.wizard.service.ObjectiveDecompositionService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Servlet for creating and managing agents through the wizard process.
 * This servlet handles the creation of agents based on objectives and manages
 * the state transitions of the wizard process.
 */
@WebServlet("/api/agent/create")
public class CreateAgentServlet extends HttpServlet {

    private final AgentCreationService agentCreationService;
    private final ObjectiveDecompositionService decompositionService;
    private final Gson gson;

    /**
     * Constructs a new CreateAgentServlet.
     */
    public CreateAgentServlet() {
        this.agentCreationService = AgentCreationService.getInstance();
        this.decompositionService = new ObjectiveDecompositionService();
        this.gson = new Gson();
    }

    /**
     * Handles POST requests to create or update agents.
     * 
     * @param request The HTTP request
     * @param response The HTTP response
     * @throws ServletException If an error occurs during servlet processing
     * @throws IOException If an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Read the request body
        BufferedReader reader = request.getReader();
        StringBuilder requestBody = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            requestBody.append(line);
        }
        
        // Parse the request body
        AgentRequest agentRequest = gson.fromJson(requestBody.toString(), AgentRequest.class);
        
        // Set response content type
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        
        try {
            // Process the request based on the state
            if (agentRequest.getState() == WizardState.INITIAL && agentRequest.getAgent() == null) {
                // Create a new agent session
                String sessionId = agentCreationService.startNewSession();
                WizardSession session;
                
                // If objective is provided, process it
                if (agentRequest.getObjective() != null && !agentRequest.getObjective().trim().isEmpty()) {
                    session = agentCreationService.processObjective(sessionId, agentRequest.getObjective());
                } else {
                    session = agentCreationService.getSession(sessionId);
                }
                
                // Return the session and agent
                AgentResponse agentResponse = new AgentResponse(
                    session.getSessionId(),
                    session.getState(),
                    session.getAgent(),
                    null
                );
                
                out.print(gson.toJson(agentResponse));
                
            } else if (agentRequest.getSessionId() != null) {
                // Get the existing session
                WizardSession session = agentCreationService.getSession(agentRequest.getSessionId());
                
                if (session == null) {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.print(gson.toJson(new ErrorResponse("Session not found")));
                    return;
                }
                
                // Process based on the requested state
                switch (agentRequest.getState()) {
                    case OBJECTIVE_ENTERED:
                        if (agentRequest.getObjective() != null && !agentRequest.getObjective().trim().isEmpty()) {
                            session = agentCreationService.processObjective(
                                agentRequest.getSessionId(), 
                                agentRequest.getObjective()
                            );
                        } else {
                            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                            out.print(gson.toJson(new ErrorResponse("Objective is required")));
                            return;
                        }
                        break;
                        
                    case AGENT_REVIEWED:
                        session = agentCreationService.reviewAgent(agentRequest.getSessionId());
                        break;
                        
                    case COMPLETED:
                        Agent agent = agentCreationService.completeAgentCreation(agentRequest.getSessionId());
                        AgentResponse agentResponse = new AgentResponse(
                            session.getSessionId(),
                            WizardState.COMPLETED,
                            agent,
                            null
                        );
                        out.print(gson.toJson(agentResponse));
                        return;
                        
                    default:
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        out.print(gson.toJson(new ErrorResponse("Invalid state transition")));
                        return;
                }
                
                // Return the updated session and agent
                AgentResponse agentResponse = new AgentResponse(
                    session.getSessionId(),
                    session.getState(),
                    session.getAgent(),
                    null
                );
                
                out.print(gson.toJson(agentResponse));
                
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(gson.toJson(new ErrorResponse("Invalid request")));
            }
            
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(gson.toJson(new ErrorResponse("Error processing request: " + e.getMessage())));
        }
    }
    
    /**
     * Handles GET requests to retrieve agent sessions.
     * 
     * @param request The HTTP request
     * @param response The HTTP response
     * @throws ServletException If an error occurs during servlet processing
     * @throws IOException If an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Get the session ID from the request parameter
        String sessionId = request.getParameter("sessionId");
        
        // Set response content type
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        
        if (sessionId != null && !sessionId.trim().isEmpty()) {
            // Get the session
            WizardSession session = agentCreationService.getSession(sessionId);
            
            if (session != null) {
                // Return the session and agent
                AgentResponse agentResponse = new AgentResponse(
                    session.getSessionId(),
                    session.getState(),
                    session.getAgent(),
                    null
                );
                
                out.print(gson.toJson(agentResponse));
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print(gson.toJson(new ErrorResponse("Session not found")));
            }
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print(gson.toJson(new ErrorResponse("Session ID is required")));
        }
    }
    
    /**
     * Request object for agent creation and management.
     */
    private static class AgentRequest {
        private String sessionId;
        private String objective;
        private WizardState state;
        private Agent agent;
        
        public String getSessionId() {
            return sessionId;
        }
        
        public String getObjective() {
            return objective;
        }
        
        public WizardState getState() {
            return state;
        }
        
        public Agent getAgent() {
            return agent;
        }
    }
    
    /**
     * Response object for agent creation and management.
     */
    private static class AgentResponse {
        private final String sessionId;
        private final WizardState state;
        private final Agent agent;
        private final String errorMessage;
        
        public AgentResponse(String sessionId, WizardState state, Agent agent, String errorMessage) {
            this.sessionId = sessionId;
            this.state = state;
            this.agent = agent;
            this.errorMessage = errorMessage;
        }
    }
    
    /**
     * Error response object.
     */
    private static class ErrorResponse {
        private final String error;
        
        public ErrorResponse(String error) {
            this.error = error;
        }
    }
}