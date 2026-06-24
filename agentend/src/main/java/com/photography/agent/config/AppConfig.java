package com.photography.agent.config;

import com.photography.agent.service.PhotographyTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Configuration
public class AppConfig {

    /**
     * 配置 Spring AI ChatClient，注入系统提示词和摄影工具
     */
    @Bean
    public ChatClient chatClient(ChatClient.Builder builder, PhotographyTools tools) {
        return builder
                .defaultSystem(readPrompt("prompts/system-prompt.st"))
                .defaultTools(tools)
                .build();
    }

    private String readPrompt(String path) {
        try {
            return new ClassPathResource(path).getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load prompt: " + path, e);
        }
    }
}
