package com.smartpay.common.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        // Spring AI auto-creates a ChatClient.Builder bean based on
        // your application.yml config (base-url, model name).
        // We just call .build() to get the actual usable ChatClient.
        return builder.build();
    }
}