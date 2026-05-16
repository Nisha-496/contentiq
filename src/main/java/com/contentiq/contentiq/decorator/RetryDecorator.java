package com.contentiq.contentiq.decorator;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RetryDecorator implements AIService {

    private final AIService delegate;
    private final int maxAttempts;
    private final long backoffMillis;

    public RetryDecorator(AIService delegate, int maxAttempts, long backoffMillis) {
        this.delegate = delegate;
        this.maxAttempts = Math.max(1, maxAttempts);
        this.backoffMillis = Math.max(0, backoffMillis);
    }

    @Override
    public String generate(String systemPrompt, String userPrompt) {
        RuntimeException lastError = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return delegate.generate(systemPrompt, userPrompt);
            } catch (RuntimeException ex) {
                lastError = ex;
                log.warn("[AI] Attempt {}/{} failed: {}", attempt, maxAttempts, ex.getMessage());
                if (attempt < maxAttempts) {
                    sleep(backoffMillis * attempt);
                }
            }
        }
        throw lastError != null ? lastError
                : new RuntimeException("RetryDecorator exhausted attempts without exception");
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
