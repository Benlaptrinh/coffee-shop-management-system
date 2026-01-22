package com.example.demo.payload.request;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
/**
 * Request payload for Create User.
 */
@Getter
@Setter
public class CreateUserRequest {
    @NotBlank
    private String username;
    @NotBlank
    private String password;
    private String role;
    private String avatar;
    private Boolean enabled = true;
}
