package com.leucine.config;

import io.github.cdimascio.dotenv.Dotenv;

/**
 * Configuration class for AI API keys
 * Manages API keys for Claude and OpenAI services loaded from .env file
 */
public class AIConfig {
    private static Dotenv dotenv;
    private static String claudeApiKey;
    private static String openAiApiKey;
    
    static {
        try {
            // Load environment variables from .env file
            dotenv = Dotenv.configure().ignoreIfMissing().load();
        } catch (Exception e) {
            System.err.println("Warning: Failed to load .env file: " + e.getMessage());
            dotenv = null;
        }
    }

    /**
     * Initialize API keys
     * 
     * @param claudeKey The API key for Claude AI service
     * @param openAiKey The API key for OpenAI service
     */
    public static void init(String claudeKey, String openAiKey) {
        claudeApiKey = claudeKey;
        openAiApiKey = openAiKey;
    }

    /**
     * Get the Claude API key
     * If not explicitly set, uses a default key
     * 
     * @return The Claude API key
     * @throws IllegalStateException if the key is not available
     */
    public static String getClaudeApiKey() {
        if (claudeApiKey == null) {
            // Try to get the API key from the .env file first
            if (dotenv != null) {
                claudeApiKey = dotenv.get("CLAUDE_API_KEY");
            }
            // Fall back to system environment variables if not found in .env
            if (claudeApiKey == null) {
                claudeApiKey = System.getenv("CLAUDE_API_KEY");
            }
            if (claudeApiKey == null) {
                throw new IllegalStateException("Claude API key not set. Call AIConfig.init() or set CLAUDE_API_KEY environment variable.");
            }
        }
        return claudeApiKey;
    }

    /**
     * Get the OpenAI API key
     * If not explicitly set, uses a default key
     * 
     * @return The OpenAI API key
     * @throws IllegalStateException if the key is not available
     */
    public static String getOpenAiApiKey() {
        if (openAiApiKey == null) {
            // Try to get the API key from the .env file first
            if (dotenv != null) {
                openAiApiKey = dotenv.get("OPENAI_API_KEY");
            }
            // Fall back to system environment variables if not found in .env
            if (openAiApiKey == null) {
                openAiApiKey = System.getenv("OPENAI_API_KEY");
            }
            if (openAiApiKey == null) {
                throw new IllegalStateException("OpenAI API key not set. Call AIConfig.init() or set OPENAI_API_KEY environment variable.");
            }
        }
        return openAiApiKey;
    }
}