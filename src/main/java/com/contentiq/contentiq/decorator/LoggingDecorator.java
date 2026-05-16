package com.contentiq.contentiq.decorator;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LoggingDecorator implements AIService {

    private final AIService delegate;

    public LoggingDecorator(AIService delegate) {
        this.delegate = delegate;
    }

    @Override
    public String generate(String systemPrompt, String userPrompt) {
        long start = System.currentTimeMillis();
        log.info("[AI] Request - systemPromptLen={} userPromptLen={}",
                systemPrompt == null ? 0 : systemPrompt.length(),
                userPrompt == null ? 0 : userPrompt.length());
        try {
            String result = delegate.generate(systemPrompt, userPrompt);
            long elapsed = System.currentTimeMillis() - start;
            log.info("[AI] Response in {}ms responseLen={}", elapsed, result == null ? 0 : result.length());
            return result;
        } catch (RuntimeException ex) {
            long elapsed = System.currentTimeMillis() - start;
            log.error("[AI] Failure after {}ms: {}", elapsed, ex.getMessage());
            throw ex;
        }
    }
}
