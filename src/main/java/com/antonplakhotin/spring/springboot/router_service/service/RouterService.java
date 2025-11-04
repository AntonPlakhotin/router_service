package com.antonplakhotin.spring.springboot.router_service.service;

import com.antonplakhotin.spring.springboot.router_service.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.security.oauth2.jwt.Jwt;
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

    private HttpHeaders createHeadersWithJwt(Jwt jwt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwt.getTokenValue());
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private String extractUserId(Jwt jwt) {
        try {
            if (jwt.hasClaim("user_id"))
                return jwt.getClaimAsString("user_id");

            if (jwt.hasClaim("preferred_username"))
                return jwt.getClaimAsString("preferred_username");

            if (jwt.hasClaim("email"))
                return jwt.getClaimAsString("email");

            return jwt.getSubject();
        } catch (Exception e) {
            log.error("Failed to extract user_id from JWT: {}", e.getMessage());
            return jwt.getSubject();
        }
    }

    public List<ChatRes> getAllChats(Jwt jwt) {
        String userId = extractUserId(jwt);
        try {
            String url = chatServiceBaseUrl + "/list/" + userId;
            HttpEntity<Void> entity = new HttpEntity<>(null, createHeadersWithJwt(jwt));

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

    public ChatRes getChat(long chatId, Jwt jwt) {
        try {
            String url = chatServiceBaseUrl + "/" + chatId;
            HttpEntity<Void> entity = new HttpEntity<>(null, createHeadersWithJwt(jwt));

            ResponseEntity<ChatRes> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, ChatRes.class
            );

            ChatRes chat = response.getBody();
            if (chat == null) return null;

            String userId = extractUserId(jwt);
            if (!userId.equals(chat.getUserId())) {
                log.warn("Access denied to chat {} by user {}", chatId, userId);
                return null;
            }

            return chat;
        } catch (Exception e) {
            log.error("Error fetching chat {}: {}", chatId, e.getMessage());
            return null;
        }
    }

    public Long createChat(CreateChatRq createChatRq, Jwt jwt) {
        try {
            String userId = extractUserId(jwt);
            createChatRq.setUserId(userId);

            String url = chatServiceBaseUrl + "/create";
            HttpEntity<CreateChatRq> entity = new HttpEntity<>(createChatRq, createHeadersWithJwt(jwt));

            ResponseEntity<Long> response = restTemplate.postForEntity(url, entity, Long.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("Error creating chat: {}", e.getMessage());
            return null;
        }
    }

    public boolean setPrompt(SetPromptRq setPromptRq, Jwt jwt) {
        try {
            String url = chatServiceBaseUrl + "/setPrompt";
            HttpEntity<SetPromptRq> entity = new HttpEntity<>(setPromptRq, createHeadersWithJwt(jwt));

            ResponseEntity<Void> response = restTemplate.postForEntity(url, entity, Void.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.error("Error setting prompt: {}", e.getMessage());
            return false;
        }
    }

    public boolean renameChat(RenameChatRq renameChatRq, Jwt jwt) {
        try {
            String url = chatServiceBaseUrl + "/rename";
            HttpEntity<RenameChatRq> entity = new HttpEntity<>(renameChatRq, createHeadersWithJwt(jwt));

            restTemplate.exchange(url, HttpMethod.PUT, entity, Void.class);
            return true;
        } catch (Exception e) {
            log.error("Error renaming chat: {}", e.getMessage());
            return false;
        }
    }

    public boolean deleteChat(long chatId, Jwt jwt) {
        try {
            String url = chatServiceBaseUrl + "/delete/" + chatId;
            HttpEntity<Void> entity = new HttpEntity<>(null, createHeadersWithJwt(jwt));

            restTemplate.exchange(url, HttpMethod.DELETE, entity, Void.class);
            return true;
        } catch (Exception e) {
            log.error("Error deleting chat {}: {}", chatId, e.getMessage());
            return false;
        }
    }

    public List<MessageRes> getMessages(long chatId, Jwt jwt) {
        try {
            ChatRes chat = getChat(chatId, jwt);
            if (chat == null) return Collections.emptyList();

            String url = chatServiceBaseUrl + "/messages/" + chatId;
            HttpEntity<Void> entity = new HttpEntity<>(null, createHeadersWithJwt(jwt));

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

    public WriteToChatRs writeToChat(WriteToChatRq writeToChatRq, Jwt jwt) {
        try {
            ChatRes chat = getChat(writeToChatRq.getChatId(), jwt);
            if (chat == null) {
                log.warn("User {} tried to write to foreign or nonexistent chat {}", extractUserId(jwt), writeToChatRq.getChatId());
                return null;
            }

            String url = chatServiceBaseUrl + "/write";
            HttpEntity<WriteToChatRq> entity = new HttpEntity<>(writeToChatRq, createHeadersWithJwt(jwt));

            ResponseEntity<WriteToChatRs> response = restTemplate.postForEntity(url, entity, WriteToChatRs.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("Error writing to chat {}: {}", writeToChatRq.getChatId(), e.getMessage());
            return null;
        }
    }
}

