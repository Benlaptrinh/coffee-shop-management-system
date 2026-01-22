package com.example.demo.payload.dto;
import lombok.Getter;
import lombok.Setter;
/**
 * DTO for User.
 */
@Getter
@Setter
public class UserDto {
    private Long id;
    private String username;
    private String role;
    private String avatar;
    private boolean enabled;
}
