package com.example.demo.payload.request;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
/**
 * Request payload for Create Don Vi.
 */
@Getter
@Setter
public class CreateDonViRequest {
    @NotBlank
    private String tenDonVi;
}
