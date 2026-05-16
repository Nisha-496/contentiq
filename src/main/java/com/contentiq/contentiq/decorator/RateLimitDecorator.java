package com.contentiq.contentiq.decorator;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Semaphore;

@Slf4j
public class RateLimitDecorator implements AIService {

    private final AIService delegate;
    private final Semaphore semaphore;
    private final long minIntervalMillis;
    private volatile long lastCallEpochMillis = 0L;
    private final Object intervalLock = new Object();

    public RateLimitDecorator(AIService delegate, int maxConcurrent, long minIntervalMillis) {
        this.delegate = delegate;
        this.semaphore = new Semaphore(Math.max(1, maxConcurrent));
        this.minIntervalMillis = Math.max(0, minIntervalMillis);
    }

    @Override
    public String generate(String systemPrompt, String userPrompt) {
        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("RateLimitDecorator interrupted while waiting", e);
        }
        try {
            throttleIfNeeded();
            return delegate.generate(systemPrompt, userPrompt);
        } finally {
            semaphore.release();
        }
    }

    private void throttleIfNeeded() {
        if (minIntervalMillis <= 0) {
            return;
        }
        synchronized (intervalLock) {
            long now = System.currentTimeMillis();
            long sinceLast = now - lastCallEpochMillis;
            if (sinceLast < minIntervalMillis) {
                long sleepMs = minIntervalMillis - sinceLast;
                try {
                    Thread.sleep(sleepMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            lastCallEpochMillis = System.currentTimeMillis();
        }
    }
}
