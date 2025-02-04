package com.kyut.ordo.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@AllArgsConstructor
@Data
@Builder
public class UserReadDTO {
    private String id;
    private String email;
    private String name;
    private String picture;
}
