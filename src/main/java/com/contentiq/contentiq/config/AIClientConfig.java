package com.contentiq.contentiq.config;

import com.contentiq.contentiq.decorator.AIService;
import com.contentiq.contentiq.decorator.CoreAIService;
import com.contentiq.contentiq.decorator.LoggingDecorator;
import com.contentiq.contentiq.decorator.RateLimitDecorator;
import com.contentiq.contentiq.decorator.RetryDecorator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class AIClientConfig {

    @Bean
    @Scope("singleton")
    public AIService aiService(
            @Value("${claude.api.key}") String apiKey,
            @Value("${claude.api.url:https://api.anthropic.com/v1/messages}") String apiUrl,
            @Value("${claude.api.model:claude-sonnet-4-20250514}") String model,
            @Value("${claude.api.max-tokens:4096}") int maxTokens,
            @Value("${claude.api.version:2023-06-01}") String anthropicVersion,
            @Value("${claude.retry.max-attempts:3}") int retryAttempts,
            @Value("${claude.retry.backoff-ms:1000}") long retryBackoff,
            @Value("${claude.rate-limit.max-concurrent:3}") int maxConcurrent,
            @Value("${claude.rate-limit.min-interval-ms:200}") long minInterval) {

        AIService core = new CoreAIService(apiKey, apiUrl, model, maxTokens, anthropicVersion);
        AIService retrying = new RetryDecorator(core, retryAttempts, retryBackoff);
        AIService limited = new RateLimitDecorator(retrying, maxConcurrent, minInterval);
        return new LoggingDecorator(limited);
    }
}
