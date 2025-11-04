package com.antonplakhotin.spring.springboot.router_service.dto;

import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class WriteToChatRq {
    long chatId;
    String message;
    String model;
}
