package com.kyut.ordo.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@AllArgsConstructor
@Data
@Builder
public class UserDTO {
    private String id;
    private String email;
    private String name;
    private String picture;
}
