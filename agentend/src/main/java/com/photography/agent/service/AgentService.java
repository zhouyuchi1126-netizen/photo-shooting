package com.photography.agent.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AgentService {

    private final ChatClient chatClient;

    /** 简易会话历史存储（生产环境应替换为 Redis 或数据库） */
    private final Map<String, List<Map<String, String>>> sessions = new ConcurrentHashMap<>();

    public AgentService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    /**
     * 普通对话
     */
    public String chat(String message, String sessionId) {
        saveUserMessage(sessionId, message);

        String reply = chatClient.prompt()
                .user(message)
                .call()
                .content();

        saveAssistantMessage(sessionId, reply);
        return reply;
    }

    /**
     * 流式对话（SSE）
     */
    public Flux<String> chatStream(String message, String sessionId) {
        saveUserMessage(sessionId, message);

        StringBuilder fullReply = new StringBuilder();

        return chatClient.prompt()
                .user(message)
                .stream()
                .content()
                .doOnNext(fullReply::append)
                .doOnComplete(() -> saveAssistantMessage(sessionId, fullReply.toString()));
    }

    /**
     * 获取指定会话的历史记录
     */
    public List<Map<String, String>> getHistory(String sessionId) {
        return sessions.getOrDefault(sessionId, Collections.emptyList());
    }

    /**
     * 清除指定会话
     */
    public void clearHistory(String sessionId) {
        sessions.remove(sessionId);
    }

    private void saveUserMessage(String sessionId, String message) {
        sessions.computeIfAbsent(sessionId, k -> new ArrayList<>())
                .add(Map.of("role", "user", "content", message));
    }

    private void saveAssistantMessage(String sessionId, String message) {
        sessions.computeIfAbsent(sessionId, k -> new ArrayList<>())
                .add(Map.of("role", "assistant", "content", message));
    }
}
