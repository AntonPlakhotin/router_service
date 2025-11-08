package com.antonplakhotin.spring.springboot.router_service.service;

import com.antonplakhotin.spring.springboot.router_service.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

@Service
public class RouterService {

    private static final Logger log = LoggerFactory.getLogger(RouterService.class);

    @Autowired
    private RestTemplate restTemplate;

    private final String chatServiceBaseUrl = "http://localhost:8110/api/chat";

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    public List<ChatRes> getAllChats(String userId) {
        try {
            String url = chatServiceBaseUrl + "/list/" + userId;
            HttpEntity<Void> entity = new HttpEntity<>(createHeaders());

            ResponseEntity<List<ChatRes>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<>() {}
            );
            return response.getBody() != null ? response.getBody() : Collections.emptyList();
        } catch (Exception e) {
            log.error("Error fetching chats for user {}: {}", userId, e.getMessage());
            return Collections.emptyList();
        }
    }

    public ChatRes getChat(long chatId, String userId) {
        try {
            String url = chatServiceBaseUrl + "/" + chatId;
            HttpEntity<Void> entity = new HttpEntity<>(createHeaders());

            ResponseEntity<ChatRes> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, ChatRes.class
            );

            // Проверяем, принадлежит ли чат пользователю
            ChatRes chat = response.getBody();
            if (chat != null && userId.equals(chat.getUserId())) {
                return chat;
            }
            return null;
        } catch (Exception e) {
            log.error("Error fetching chat {}: {}", chatId, e.getMessage());
            return null;
        }
    }

    public Long createChat(CreateChatRq createChatRq) {
        try {
            String url = chatServiceBaseUrl + "/create";
            HttpEntity<CreateChatRq> entity = new HttpEntity<>(createChatRq, createHeaders());

            ResponseEntity<Long> response = restTemplate.postForEntity(url, entity, Long.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("Error creating chat: {}", e.getMessage());
            return null;
        }
    }

    public boolean setPrompt(SetPromptRq setPromptRq, String userId) {
        try {
            // Сначала проверяем доступ к чату
            ChatRes chat = getChat(setPromptRq.getChatId(), userId);
            if (chat == null) {
                return false;
            }

            String url = chatServiceBaseUrl + "/setPrompt";
            HttpEntity<SetPromptRq> entity = new HttpEntity<>(setPromptRq, createHeaders());

            ResponseEntity<Void> response = restTemplate.postForEntity(url, entity, Void.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.error("Error setting prompt: {}", e.getMessage());
            return false;
        }
    }

    public boolean renameChat(RenameChatRq renameChatRq, String userId) {
        try {
            // Проверяем доступ к чату
            ChatRes chat = getChat(renameChatRq.getChatId(), userId);
            if (chat == null) {
                return false;
            }

            String url = chatServiceBaseUrl + "/rename";
            HttpEntity<RenameChatRq> entity = new HttpEntity<>(renameChatRq, createHeaders());

            restTemplate.exchange(url, HttpMethod.PUT, entity, Void.class);
            return true;
        } catch (Exception e) {
            log.error("Error renaming chat: {}", e.getMessage());
            return false;
        }
    }

    public boolean deleteChat(long chatId, String userId) {
        try {
            // Проверяем доступ к чату
            ChatRes chat = getChat(chatId, userId);
            if (chat == null) {
                return false;
            }

            String url = chatServiceBaseUrl + "/delete/" + chatId;
            HttpEntity<Void> entity = new HttpEntity<>(createHeaders());

            restTemplate.exchange(url, HttpMethod.DELETE, entity, Void.class);
            return true;
        } catch (Exception e) {
            log.error("Error deleting chat {}: {}", chatId, e.getMessage());
            return false;
        }
    }

    public List<MessageRes> getMessages(long chatId, String userId) {
        try {
            ChatRes chat = getChat(chatId, userId);
            if (chat == null) return Collections.emptyList();

            String url = chatServiceBaseUrl + "/messages/" + chatId;
            HttpEntity<Void> entity = new HttpEntity<>(createHeaders());

            ResponseEntity<List<MessageRes>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<>() {}
            );
            return response.getBody() != null ? response.getBody() : Collections.emptyList();
        } catch (Exception e) {
            log.error("Error fetching messages for chat {}: {}", chatId, e.getMessage());
            return Collections.emptyList();
        }
    }

    public WriteToChatRs writeToChat(WriteToChatRq writeToChatRq, String userId) {
        try {
            ChatRes chat = getChat(writeToChatRq.getChatId(), userId);
            if (chat == null) {
                log.warn("User {} tried to write to foreign or nonexistent chat {}", userId, writeToChatRq.getChatId());
                return null;
            }

            String url = chatServiceBaseUrl + "/write";
            HttpEntity<WriteToChatRq> entity = new HttpEntity<>(writeToChatRq, createHeaders());

            ResponseEntity<WriteToChatRs> response = restTemplate.postForEntity(url, entity, WriteToChatRs.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("Error writing to chat {}: {}", writeToChatRq.getChatId(), e.getMessage());
            return null;
        }
    }
}