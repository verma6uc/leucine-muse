package main.java.com.leucine.utils;

import com.google.gson.annotations.SerializedName;
import com.google.gson.Gson;
import okhttp3.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class ClaudeClient {
    private static final String BASE_URL = "https://api.anthropic.com/v1/messages";
    private static final MediaType JSON = MediaType.parse("application/json");
    private static final String DEFAULT_MODEL = "claude-3-7-sonnet-latest";
    
    private static final int MAX_RETRIES = 5;  // Increased from 3 to 5
    private static final long INITIAL_RETRY_DELAY_MS = 10000; // 10 seconds (increased from 5)
    private static final long MAX_RETRY_DELAY_MS = 120000; // 120 seconds (increased from 60)
    
    private final String apiKey;
    private final OkHttpClient httpClient;
    private final Gson gson;
    private final String model;
    private final int maxTokens;
    private final double temperature;
    private final String systemPrompt;
    private boolean debugMode = true;

    private ClaudeClient(Builder builder) {
        this.apiKey = builder.apiKey;
        this.model = builder.model;
        this.maxTokens = builder.maxTokens;
        this.temperature = builder.temperature;
        this.systemPrompt = builder.systemPrompt;
        this.gson = new Gson();
        this.httpClient = new OkHttpClient.Builder()
            .connectTimeout(builder.connectTimeout, TimeUnit.SECONDS)
            .readTimeout(builder.readTimeout, TimeUnit.SECONDS)
            .writeTimeout(builder.writeTimeout, TimeUnit.SECONDS)
            .build();
        this.debugMode = builder.debugMode;
    }

    public static class Message {
        @SerializedName("role")
        private String role;
        @SerializedName("content")
        private String content;

        public Message() {}

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }

        public String getRole() { return role; }
        public String getContent() { return content; }
        public void setRole(String role) { this.role = role; }
        public void setContent(String content) { this.content = content; }
    }

    public static class ContentBlock {
        @SerializedName("type")
        private String type;
        @SerializedName("text")
        private String text;

        public ContentBlock() {}

        public String getType() { return type; }
        public String getText() { return text; }
        public void setType(String type) { this.type = type; }
        public void setText(String text) { this.text = text; }
    }

    public static class ClaudeResponse {
        @SerializedName("id")
        private String id;
        @SerializedName("type")
        private String type;
        @SerializedName("role")
        private String role;
        @SerializedName("model")
        private String model;
        @SerializedName("content")
        private List<ContentBlock> content;
        @SerializedName("stop_reason")
        private String stopReason;
        @SerializedName("error")
        private ClaudeError error;

        public ClaudeResponse() {}

        public String getId() { return id; }
        public String getType() { return type; }
        public String getRole() { return role; }
        public String getModel() { return model; }
        public List<ContentBlock> getContent() { return content; }
        public String getStopReason() { return stopReason; }
        public ClaudeError getError() { return error; }

        public void setId(String id) { this.id = id; }
        public void setType(String type) { this.type = type; }
        public void setRole(String role) { this.role = role; }
        public void setModel(String model) { this.model = model; }
        public void setContent(List<ContentBlock> content) { this.content = content; }
        public void setStopReason(String stopReason) { this.stopReason = stopReason; }
        public void setError(ClaudeError error) { this.error = error; }

        public String getTextContent() {
            if (content != null && !content.isEmpty()) {
                // Get all text blocks and join them
                String text = content.stream()
                    .filter(block -> "text".equals(block.getType()))
                    .map(ContentBlock::getText)
                    .collect(Collectors.joining("\n"));
                
                // Clean up markdown formatting
                return text.replaceAll("```json", "")
                         .replaceAll("```markdown", "")
                         .replaceAll("```", "")
                         .trim();
            }
            return "";
        }
    }

    public static class ClaudeError {
        @SerializedName("type")
        private String type;
        @SerializedName("message")
        private String message;

        public String getType() { return type; }
        public String getMessage() { return message; }
        public void setType(String type) { this.type = type; }
        public void setMessage(String message) { this.message = message; }
    }

    public static class ClaudeException extends IOException {
        private final String type;
        private final int statusCode;

        public ClaudeException(String message, String type, int statusCode) {
            super(message);
            this.type = type;
            this.statusCode = statusCode;
        }

        public String getType() { return type; }
        public int getStatusCode() { return statusCode; }
    }

    public ClaudeResponse sendMessage(String message) throws IOException {
        List<Message> messages = new ArrayList<>();
        if (systemPrompt != null && !systemPrompt.trim().isEmpty()) {
            messages.add(new Message("system", systemPrompt));
        }
        messages.add(new Message("user", message));
        return sendMessages(messages);
    }

    public ClaudeResponse sendMessageWithSystem(String systemPrompt, String message) throws IOException {
        List<Message> messages = new ArrayList<>();
        messages.add(new Message("system", systemPrompt));
        messages.add(new Message("user", message));
        return sendMessages(messages);
    }

    private void log(String message) {
        if (debugMode) {
            System.out.printf("[%s] ClaudeClient: %s%n", 
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                message);
        }
    }

    private void logError(String message, Exception e) {
        System.err.printf("[%s] ClaudeClient ERROR: %s%n", 
            LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            message);
        if (e != null) {
            System.err.printf("Exception: %s%n", e.getMessage());
            e.printStackTrace();
        }
    }

    private void logRequest(Request request, String body) {
        if (debugMode) {
            log("Request URL: " + request.url());
            // Don't log request body or headers in debug mode
        }
    }

    private void logResponse(Response response, String body) {
        if (debugMode && response != null) {
            log("Response Code: " + response.code());
            log("Response Message: " + response.message());
            log("Response Body: " + (body != null ? body : "null"));
        } else if (debugMode) {
            log("Response: null");
        }
    }

    private boolean isRateLimitError(Response response, ClaudeResponse errorResponse) {
        // Check HTTP status code first
        if (response != null) {
            if (response.code() == 429) {
                return true;
            }
            
            // Check response headers for rate limit info
            String rateLimitHeader = response.header("x-ratelimit-remaining");
            if (rateLimitHeader != null && rateLimitHeader.equals("0")) {
                return true;
            }
        }
        
        // Then check error message content
        if (errorResponse != null && errorResponse.getError() != null) {
            String errorMessage = errorResponse.getError().getMessage();
            String errorType = errorResponse.getError().getType();
            
            if (errorMessage != null) {
                errorMessage = errorMessage.toLowerCase();
                return errorMessage.contains("rate limit") || 
                       errorMessage.contains("too many requests") ||
                       errorMessage.contains("would exceed your organization's rate limit") ||
                       errorMessage.contains("requests per minute") ||
                       errorMessage.contains("token limit") ||
                       errorMessage.contains("try again later");
            }
            
            if (errorType != null) {
                return errorType.equals("rate_limit_error") ||
                       errorType.equals("tokens_exceeded") ||
                       errorType.equals("quota_exceeded");
            }
        }
        return false;
    }

    private long getRetryDelay(int retryCount, Response response) {
        // Get retry delay from response headers if available
        if (response != null) {
            String retryAfter = response.header("Retry-After");
            if (retryAfter != null) {
                try {
                    return Long.parseLong(retryAfter) * 1000L; // Convert seconds to milliseconds
                } catch (NumberFormatException e) {
                    // Ignore parse error and use default delay calculation
                }
            }
        }
        
        // Exponential backoff with jitter
        long baseDelay = INITIAL_RETRY_DELAY_MS * (long) Math.pow(2, retryCount);
        long jitter = (long) (Math.random() * INITIAL_RETRY_DELAY_MS);
        return Math.min(baseDelay + jitter, MAX_RETRY_DELAY_MS);
    }

    public ClaudeResponse sendMessages(List<Message> messages) throws IOException {
        int retryCount = 0;
        while (true) {
            Map<String, Object> requestBody = new HashMap<>();
            String responseBody = null;
            try {
                requestBody.put("model", model);
                requestBody.put("max_tokens", maxTokens);
                requestBody.put("temperature", temperature);
                requestBody.put("messages", messages);

                String requestJson = gson.toJson(requestBody);
                
                Request request = new Request.Builder()
                    .url(BASE_URL)
                    .post(RequestBody.create(requestJson, JSON))
                    .addHeader("x-api-key", apiKey)
                    .addHeader("anthropic-version", "2023-06-01")
                    .addHeader("anthropic-beta", "output-128k-2025-02-19")
                    .addHeader("content-type", "application/json")
                    .build();

                logRequest(request, requestJson);

                try (Response response = httpClient.newCall(request).execute()) {
                     responseBody = response.body().string();
                    logResponse(response, responseBody);

                    if (!response.isSuccessful()) {
                        ClaudeResponse errorResponse = gson.fromJson(responseBody, ClaudeResponse.class);
                        
                        // Handle rate limit errors
                        if (isRateLimitError(response, errorResponse) && retryCount < MAX_RETRIES) {
                            long retryDelay = getRetryDelay(retryCount, response);
                            log("Rate limit exceeded, retrying in " + retryDelay + "ms (attempt " + (retryCount + 1) + " of " + MAX_RETRIES + ")");
                            Thread.sleep(retryDelay);
                            retryCount++;
                            continue;
                        }

                        if (errorResponse != null && errorResponse.getError() != null) {
                            throw new ClaudeException(
                                errorResponse.getError().getMessage(),
                                errorResponse.getError().getType(),
                                response.code()
                            );
                        }
                        throw new IOException("API call failed: " + response.code() + 
                            " - " + response.message() + "\nBody: " + responseBody);
                    }
                    
                    return gson.fromJson(responseBody, ClaudeResponse.class);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
				throw new IOException("Request interrupted during retry delay", e);
            } catch (Exception e) {
                log("The following json was not parsed.: "+ gson.toJson(responseBody));

            	if (retryCount < MAX_RETRIES && (e instanceof IOException || e instanceof ClaudeException)) {
                    long retryDelay = getRetryDelay(retryCount, null);
                    log("Request failed, retrying in " + retryDelay + "ms (attempt " + (retryCount + 1) + " of " + MAX_RETRIES + ")");
                    try {
                        Thread.sleep(retryDelay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new IOException("Request interrupted during retry delay", ie);
                    }
                    retryCount++;
                    continue;
                }
                logError("Error sending messages to Claude API", e);
                throw e;
            }
        }
    }

    public static class Builder {
        private String apiKey = AIConfig.getClaudeApiKey();
        private String model = DEFAULT_MODEL;
        private int maxTokens = 81920;
        private double temperature = 0.9;
        private String systemPrompt;
        private int connectTimeout = 18000;
        private int readTimeout = 18000;
        private int writeTimeout = 18000;
        private boolean debugMode = true;

        public Builder withModel(String model) {
            this.model = model;
            return this;
        }

        public Builder withMaxTokens(int maxTokens) {
            this.maxTokens = maxTokens;
            return this;
        }

        public Builder withTemperature(double temperature) {
            this.temperature = temperature;
            return this;
        }

        public Builder withSystemPrompt(String systemPrompt) {
            this.systemPrompt = systemPrompt;
            return this;
        }

        public Builder withConnectTimeout(int seconds) {
            this.connectTimeout = seconds;
            return this;
        }

        public Builder withReadTimeout(int seconds) {
            this.readTimeout = seconds;
            return this;
        }

        public Builder withWriteTimeout(int seconds) {
            this.writeTimeout = seconds;
            return this;
        }

        public Builder withDebugMode(boolean debugMode) {
            this.debugMode = debugMode;
            return this;
        }

        public ClaudeClient build() {
            if (apiKey == null || apiKey.trim().isEmpty()) {
                throw new IllegalArgumentException("API key must be provided");
            }
            return new ClaudeClient(this);
        }
    }
}
