package com.antonplakhotin.spring.springboot.router_service.controller;

import com.antonplakhotin.spring.springboot.router_service.dto.*;
import com.antonplakhotin.spring.springboot.router_service.service.RouterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/router")
public class RouterController {

    @Autowired
    private RouterService routerService;

    // Получить все чаты пользователя
    @GetMapping("/chats")
    public ResponseEntity<List<ChatRes>> getAllChats(@AuthenticationPrincipal Jwt jwt) {
        List<ChatRes> chats = routerService.getAllChats(jwt);
        // Лучше всегда возвращать 200 с пустым массивом
        return ResponseEntity.ok(chats);
    }

    // Получить конкретный чат
    @GetMapping("/chat/{chatId}")
    public ResponseEntity<ChatRes> getChat(@PathVariable long chatId, @AuthenticationPrincipal Jwt jwt) {
        ChatRes chat = routerService.getChat(chatId, jwt);
        if (chat == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(chat);
    }

    // Создать чат
    @PostMapping("/chat/create")
    public ResponseEntity<Long> createChat(@RequestBody CreateChatRq createChatRq,
                                           @AuthenticationPrincipal Jwt jwt) {
        Long chatId = routerService.createChat(createChatRq, jwt);
        if (chatId == null) {
            return ResponseEntity.internalServerError().build();
        }
        return ResponseEntity.ok(chatId);
    }

    // Установить промпт
    @PostMapping("/chat/setPrompt")
    public ResponseEntity<Void> setPrompt(@RequestBody SetPromptRq setPromptRq,
                                          @AuthenticationPrincipal Jwt jwt) {
        boolean success = routerService.setPrompt(setPromptRq, jwt);
        if (!success) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().build();
    }

    // Переименовать чат
    @PutMapping("/chat/rename")
    public ResponseEntity<Void> renameChat(@RequestBody RenameChatRq renameChatRq,
                                           @AuthenticationPrincipal Jwt jwt) {
        boolean success = routerService.renameChat(renameChatRq, jwt);
        if (!success) {
            return ResponseEntity.internalServerError().build();
        }
        return ResponseEntity.ok().build();
    }

    // Удалить чат
    @DeleteMapping("/chat/delete/{chatId}")
    public ResponseEntity<Void> deleteChat(@PathVariable long chatId,
                                           @AuthenticationPrincipal Jwt jwt) {
        boolean success = routerService.deleteChat(chatId, jwt);
        if (!success) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().build();
    }

    // Получить сообщения чата
    @GetMapping("/chat/{chatId}/messages")
    public ResponseEntity<List<MessageRes>> getMessages(@PathVariable long chatId,
                                                        @AuthenticationPrincipal Jwt jwt) {
        List<MessageRes> messages = routerService.getMessages(chatId, jwt);
        return ResponseEntity.ok(messages);
    }

    // Отправить сообщение в чат
    @PostMapping("/chat/write")
    public ResponseEntity<WriteToChatRs> writeToChat(@RequestBody WriteToChatRq writeToChatRq,
                                                     @AuthenticationPrincipal Jwt jwt) {
        WriteToChatRs response = routerService.writeToChat(writeToChatRq, jwt);
        if (response == null) {
            return ResponseEntity.internalServerError().build();
        }
        return ResponseEntity.ok(response);
    }
}
