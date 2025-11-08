package com.antonplakhotin.spring.springboot.router_service.controller;

import com.antonplakhotin.spring.springboot.router_service.dto.*;
import com.antonplakhotin.spring.springboot.router_service.service.RouterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/router")
public class RouterController {

    @Autowired
    private RouterService routerService;

    // Получить все чаты пользователя
    @GetMapping("/chats")
    public ResponseEntity<List<ChatRes>> getAllChats(@RequestHeader("Authorization") String authHeader) {
        String userId = extractUserIdFromHeader(authHeader);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<ChatRes> chats = routerService.getAllChats(userId);
        return ResponseEntity.ok(chats);
    }

    // Получить конкретный чат
    @GetMapping("/chat/{chatId}")
    public ResponseEntity<ChatRes> getChat(@PathVariable long chatId,
                                           @RequestHeader("Authorization") String authHeader) {
        String userId = extractUserIdFromHeader(authHeader);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        ChatRes chat = routerService.getChat(chatId, userId);
        if (chat == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(chat);
    }

    // Создать чат
    @PostMapping("/chat/create")
    public ResponseEntity<Long> createChat(@RequestBody CreateChatRq createChatRq,
                                           @RequestHeader("Authorization") String authHeader) {
        String userId = extractUserIdFromHeader(authHeader);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        createChatRq.setUserId(userId);
        Long chatId = routerService.createChat(createChatRq);
        if (chatId == null) {
            return ResponseEntity.internalServerError().build();
        }
        return ResponseEntity.ok(chatId);
    }

    // Установить промпт
    @PostMapping("/chat/setPrompt")
    public ResponseEntity<Void> setPrompt(@RequestBody SetPromptRq setPromptRq,
                                          @RequestHeader("Authorization") String authHeader) {
        String userId = extractUserIdFromHeader(authHeader);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        boolean success = routerService.setPrompt(setPromptRq, userId);
        if (!success) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().build();
    }

    // Переименовать чат
    @PutMapping("/chat/rename")
    public ResponseEntity<Void> renameChat(@RequestBody RenameChatRq renameChatRq,
                                           @RequestHeader("Authorization") String authHeader) {
        String userId = extractUserIdFromHeader(authHeader);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        boolean success = routerService.renameChat(renameChatRq, userId);
        if (!success) {
            return ResponseEntity.internalServerError().build();
        }
        return ResponseEntity.ok().build();
    }

    // Удалить чат
    @DeleteMapping("/chat/delete/{chatId}")
    public ResponseEntity<Void> deleteChat(@PathVariable long chatId,
                                           @RequestHeader("Authorization") String authHeader) {
        String userId = extractUserIdFromHeader(authHeader);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        boolean success = routerService.deleteChat(chatId, userId);
        if (!success) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().build();
    }

    // Получить сообщения чата
    @GetMapping("/chat/{chatId}/messages")
    public ResponseEntity<List<MessageRes>> getMessages(@PathVariable long chatId,
                                                        @RequestHeader("Authorization") String authHeader) {
        String userId = extractUserIdFromHeader(authHeader);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<MessageRes> messages = routerService.getMessages(chatId, userId);
        return ResponseEntity.ok(messages);
    }

    // Отправить сообщение в чат
    @PostMapping("/chat/write")
    public ResponseEntity<WriteToChatRs> writeToChat(@RequestBody WriteToChatRq writeToChatRq,
                                                     @RequestHeader("Authorization") String authHeader) {
        String userId = extractUserIdFromHeader(authHeader);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        WriteToChatRs response = routerService.writeToChat(writeToChatRq, userId);
        if (response == null) {
            return ResponseEntity.internalServerError().build();
        }
        return ResponseEntity.ok(response);
    }

    private String extractUserIdFromHeader(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            return extractUserIdFromToken(token);
        }
        return null;
    }

    private String extractUserIdFromToken(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) return null;

            String payload = parts[1];
            java.util.Base64.Decoder decoder = java.util.Base64.getUrlDecoder();
            byte[] decodedBytes = decoder.decode(payload);
            String payloadJson = new String(decodedBytes);

            if (payloadJson.contains("\"preferred_username\"")) {
                int start = payloadJson.indexOf("\"preferred_username\"") + 20;
                start = payloadJson.indexOf("\"", start) + 1;
                int end = payloadJson.indexOf("\"", start);
                return payloadJson.substring(start, end);
            }
            if (payloadJson.contains("\"email\"")) {
                int start = payloadJson.indexOf("\"email\"") + 7;
                start = payloadJson.indexOf("\"", start) + 1;
                int end = payloadJson.indexOf("\"", start);
                return payloadJson.substring(start, end);
            }
            if (payloadJson.contains("\"sub\"")) {
                int start = payloadJson.indexOf("\"sub\"") + 5;
                start = payloadJson.indexOf("\"", start) + 1;
                int end = payloadJson.indexOf("\"", start);
                return payloadJson.substring(start, end);
            }

            return null;
        } catch (Exception e) {
            return null;
        }
    }
}