package com.contentiq.contentiq.config;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.youtube.YouTube;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class YouTubeConfig {

    @Bean
    @Scope("singleton")
    public YouTube youTubeClient(@Value("${youtube.api.application-name:contentiq}") String applicationName)
            throws Exception {
        return new YouTube.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                request -> { })
                .setApplicationName(applicationName)
                .build();
    }
}
