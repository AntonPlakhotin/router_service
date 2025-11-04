package com.antonplakhotin.spring.springboot.router_service.dto;

import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RenameChatRq {
    private long chatId;
    private String newTitle;
}