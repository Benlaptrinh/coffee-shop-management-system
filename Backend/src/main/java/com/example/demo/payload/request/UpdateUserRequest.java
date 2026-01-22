package com.example.demo.payload.request;
import lombok.Getter;
import lombok.Setter;
/**
 * Request payload for Update User.
 */
@Getter
@Setter
public class UpdateUserRequest {
    private String password;
    private String role;
    private String avatar;
    private Boolean enabled;
}
