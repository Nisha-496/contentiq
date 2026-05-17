package com.contentiq.contentiq.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VideoUrlParserTest {

    private static final String ID = "dQw4w9WgXcQ";

    @Test
    void extractsBareVideoId() {
        assertThat(VideoUrlParser.extractVideoId(ID)).isEqualTo(ID);
    }

    @Test
    void extractsFromShortYoutuBe() {
        assertThat(VideoUrlParser.extractVideoId("https://youtu.be/" + ID)).isEqualTo(ID);
    }

    @Test
    void extractsFromShortYoutuBeWithWww() {
        assertThat(VideoUrlParser.extractVideoId("https://www.youtu.be/" + ID)).isEqualTo(ID);
    }

    @Test
    void extractsFromWatchUrl() {
        assertThat(VideoUrlParser.extractVideoId("https://www.youtube.com/watch?v=" + ID))
                .isEqualTo(ID);
    }

    @Test
    void extractsFromWatchUrlWithExtraParams() {
        assertThat(VideoUrlParser.extractVideoId(
                "https://www.youtube.com/watch?v=" + ID + "&t=42&feature=share"))
                .isEqualTo(ID);
    }

    @Test
    void extractsFromMobileWatchUrl() {
        assertThat(VideoUrlParser.extractVideoId("https://m.youtube.com/watch?v=" + ID))
                .isEqualTo(ID);
    }

    @Test
    void extractsFromEmbedUrl() {
        assertThat(VideoUrlParser.extractVideoId("https://www.youtube.com/embed/" + ID))
                .isEqualTo(ID);
    }

    @Test
    void extractsFromVUrl() {
        assertThat(VideoUrlParser.extractVideoId("https://www.youtube.com/v/" + ID))
                .isEqualTo(ID);
    }

    @Test
    void extractsFromShortsUrl() {
        assertThat(VideoUrlParser.extractVideoId("https://www.youtube.com/shorts/" + ID))
                .isEqualTo(ID);
    }

    @Test
    void trimsWhitespace() {
        assertThat(VideoUrlParser.extractVideoId("  https://youtu.be/" + ID + "  "))
                .isEqualTo(ID);
    }

    @Test
    void rejectsBlankInput() {
        assertThatThrownBy(() -> VideoUrlParser.extractVideoId(""))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> VideoUrlParser.extractVideoId("   "))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> VideoUrlParser.extractVideoId(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsNonYouTubeHost() {
        assertThatThrownBy(() -> VideoUrlParser.extractVideoId("https://example.com/watch?v=" + ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Could not extract");
    }

    @Test
    void rejectsWatchUrlMissingVParam() {
        assertThatThrownBy(() ->
                VideoUrlParser.extractVideoId("https://www.youtube.com/watch?t=42"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsMalformedId() {
        assertThatThrownBy(() -> VideoUrlParser.extractVideoId("not-an-id"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
