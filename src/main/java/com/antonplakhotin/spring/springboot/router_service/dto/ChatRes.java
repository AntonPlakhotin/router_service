package com.antonplakhotin.spring.springboot.router_service.dto;

import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChatRes {
    private long id;
    private String userId;
    private String title;

}
