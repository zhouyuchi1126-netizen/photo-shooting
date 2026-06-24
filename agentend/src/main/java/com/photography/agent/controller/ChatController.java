package com.photography.agent.controller;

import com.photography.agent.model.ChatRequest;
import com.photography.agent.model.ChatResponse;
import com.photography.agent.service.AgentService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.UUID;

@RestController
@RequestMapping("/api/agent")
public class ChatController {

    private final AgentService agentService;

    public ChatController(AgentService agentService) {
        this.agentService = agentService;
    }

    /**
     * 普通对话：发送消息给摄影 Agent，获取回复
     */
    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        String sessionId = resolveSessionId(request);
        String reply = agentService.chat(request.getMessage(), sessionId);
        return ResponseEntity.ok(new ChatResponse(reply, sessionId));
    }

    /**
     * 流式对话：SSE 方式逐字返回 Agent 回复
     */
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatStream(@RequestBody ChatRequest request) {
        String sessionId = resolveSessionId(request);
        return agentService.chatStream(request.getMessage(), sessionId);
    }

    /**
     * 获取会话历史
     */
    @GetMapping("/history/{sessionId}")
    public ResponseEntity<?> getHistory(@PathVariable String sessionId) {
        return ResponseEntity.ok(agentService.getHistory(sessionId));
    }

    /**
     * 清除会话
     */
    @DeleteMapping("/history/{sessionId}")
    public ResponseEntity<Void> clearHistory(@PathVariable String sessionId) {
        agentService.clearHistory(sessionId);
        return ResponseEntity.noContent().build();
    }

    private String resolveSessionId(ChatRequest request) {
        return request.getSessionId() != null
                ? request.getSessionId()
                : UUID.randomUUID().toString();
    }
}
