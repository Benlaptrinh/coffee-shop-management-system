package com.example.demo.payload.request;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
/**
 * Request payload for Create Chuc Vu.
 */
@Getter
@Setter
public class CreateChucVuRequest {
    @NotBlank
    private String tenChucVu;
}
