package com.example.demo.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserRequest {
    private String password;
    private String role;
    private String avatar;
    private Boolean enabled;
}
