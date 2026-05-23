package com.medicore.hms.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${anthropic.api-key}")
    private String anthropicApiKey;

    @Value("${anthropic.api-url}")
    private String anthropicApiUrl;

    @Bean
    public WebClient anthropicWebClient() {
        // Increase buffer for large AI responses (up to 2MB)
        ExchangeStrategies strategies = ExchangeStrategies.builder()
            .codecs(config -> config.defaultCodecs().maxInMemorySize(2 * 1024 * 1024))
            .build();

        return WebClient.builder()
            .baseUrl(anthropicApiUrl)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .defaultHeader("x-api-key", anthropicApiKey)
            .defaultHeader("anthropic-version", "2023-06-01")
            .exchangeStrategies(strategies)
            .build();
    }

    @Bean
    public WebClient genericWebClient() {
        return WebClient.builder().build();
    }
}
