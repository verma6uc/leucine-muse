package com.leucine.wizard.prompts;

/**
 * Provides prompts for retrieving standard procedures for a given objective.
 * This prompt is used to guide the LLM in providing detailed hierarchical analysis
 * of standard procedures for specific objectives, particularly in pharmaceutical manufacturing.
 */
public class StandardProcedurePrompt {
    
    /**
     * Returns the user prompt template for retrieving standard procedures.
     * This template includes placeholders that will be replaced with actual values.
     * 
     * @return The user prompt template as a String
     */
    public static String getUserPromptTemplate() {
        return """
               Objective: %s
               
               Provide a detailed hierarchical analysis of the current standard procedure for this objective in pharmaceutical manufacturing, including:
               
               1. All sequential phases of the process
               2. Sub-stages within each phase
               3. Specific tasks performed at each level
               4. Personnel responsible for each task
               5. Methodologies and tools employed
               6. Documentation requirements
               7. Decision points and escalation pathways
               8. Regulatory considerations
               9. Timeline expectations for each phase
               10. Cross-functional interactions and handoffs
               
               Format your response as a well-structured markdown document with clear headings, subheadings, bullet points, and numbered lists to represent the hierarchical nature of the procedure. Use markdown formatting features like headers (# for main phases, ## for sub-stages, ### for tasks), bullet points, numbered lists, tables, and emphasis where appropriate to make the information clear and easy to navigate.
               """;
    }
    
    /**
     * Formats the user prompt by replacing the placeholder with the actual objective.
     * 
     * @param objective The objective to get standard procedures for
     * @return The formatted user prompt as a String
     */
    public static String formatUserPrompt(String objective) {
        return String.format(getUserPromptTemplate(), objective);
    }
}