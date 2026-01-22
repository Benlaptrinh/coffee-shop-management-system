package com.example.demo.payload.request;
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
