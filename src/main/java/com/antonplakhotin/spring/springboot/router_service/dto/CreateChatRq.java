package com.antonplakhotin.spring.springboot.router_service.dto;


import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateChatRq {
    private String userId;
    private String title;
}
