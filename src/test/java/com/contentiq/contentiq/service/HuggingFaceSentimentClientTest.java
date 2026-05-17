package com.contentiq.contentiq.service;

import com.contentiq.contentiq.service.HuggingFaceSentimentClient.SentimentResult;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HuggingFaceSentimentClientTest {

    private final HuggingFaceSentimentClient client = new HuggingFaceSentimentClient(
            "test-token",
            "https://router.huggingface.co/hf-inference/models",
            "cardiffnlp/twitter-roberta-base-sentiment-latest",
            20
    );

    @Test
    void parsesBatchShapeWithOneEntryPerInput() {
        String response = "[[" +
                "{\"label\":\"positive\",\"score\":0.97}," +
                "{\"label\":\"negative\",\"score\":0.88}," +
                "{\"label\":\"neutral\",\"score\":0.55}" +
                "]]";

        List<SentimentResult> results = client.parse(response, 3);

        assertThat(results).hasSize(3);
        assertThat(results.get(0).label()).isEqualTo("POSITIVE");
        assertThat(results.get(0).score()).isEqualTo(0.97);
        assertThat(results.get(1).label()).isEqualTo("NEGATIVE");
        assertThat(results.get(2).label()).isEqualTo("NEUTRAL");
    }

    @Test
    void parsesSingleInputShapeAsFullDistribution() {
        String response = "[[" +
                "{\"label\":\"positive\",\"score\":0.98}," +
                "{\"label\":\"neutral\",\"score\":0.015}," +
                "{\"label\":\"negative\",\"score\":0.005}" +
                "]]";

        List<SentimentResult> results = client.parse(response, 1);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).label()).isEqualTo("POSITIVE");
        assertThat(results.get(0).score()).isEqualTo(0.98);
    }

    @Test
    void parsesLegacyOuterArrayOfPerInputDistributions() {
        String response = "[" +
                "[{\"label\":\"positive\",\"score\":0.91},{\"label\":\"neutral\",\"score\":0.05},{\"label\":\"negative\",\"score\":0.04}]," +
                "[{\"label\":\"negative\",\"score\":0.80},{\"label\":\"neutral\",\"score\":0.15},{\"label\":\"positive\",\"score\":0.05}]" +
                "]";

        List<SentimentResult> results = client.parse(response, 2);

        assertThat(results).hasSize(2);
        assertThat(results.get(0).label()).isEqualTo("POSITIVE");
        assertThat(results.get(1).label()).isEqualTo("NEGATIVE");
    }

    @Test
    void fallsBackToNeutralOnErrorResponse() {
        String response = "{\"error\":\"Model is currently loading\"}";

        List<SentimentResult> results = client.parse(response, 3);

        assertThat(results).hasSize(3);
        assertThat(results).allMatch(r -> r.label().equals("NEUTRAL"));
        assertThat(results).allMatch(r -> r.score() == 0.0);
    }

    @Test
    void padsShortResponsesWithNeutral() {
        String response = "[[{\"label\":\"positive\",\"score\":0.99}]]";

        List<SentimentResult> results = client.parse(response, 3);

        assertThat(results).hasSize(3);
        assertThat(results.get(0).label()).isEqualTo("POSITIVE");
        assertThat(results.get(1).label()).isEqualTo("NEUTRAL");
        assertThat(results.get(2).label()).isEqualTo("NEUTRAL");
    }

    @Test
    void normalizesLowercaseLabels() {
        assertThat(client.normalize("positive")).isEqualTo("POSITIVE");
        assertThat(client.normalize("NEGATIVE")).isEqualTo("NEGATIVE");
        assertThat(client.normalize("Neutral")).isEqualTo("NEUTRAL");
    }

    @Test
    void normalizesLabelIndices() {
        assertThat(client.normalize("LABEL_0")).isEqualTo("NEGATIVE");
        assertThat(client.normalize("LABEL_2")).isEqualTo("POSITIVE");
        assertThat(client.normalize("LABEL_1")).isEqualTo("NEUTRAL");
    }

    @Test
    void normalizesUnknownLabelToNeutral() {
        assertThat(client.normalize("weird-thing")).isEqualTo("NEUTRAL");
        assertThat(client.normalize(null)).isEqualTo("NEUTRAL");
    }

    @Test
    void handlesMalformedResponseGracefully() {
        List<SentimentResult> results = client.parse("not json", 2);

        assertThat(results).hasSize(2);
        assertThat(results).allMatch(r -> r.label().equals("NEUTRAL"));
    }
}
