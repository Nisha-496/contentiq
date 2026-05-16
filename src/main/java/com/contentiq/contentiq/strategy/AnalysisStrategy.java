package com.contentiq.contentiq.strategy;

public interface AnalysisStrategy<I, O> {

    String getName();

    O analyze(I input);
}
