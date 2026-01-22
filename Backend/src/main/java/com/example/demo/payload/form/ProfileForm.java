package com.example.demo.payload.form;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Form payload for Profile.
 */
@Getter
@Setter
public class ProfileForm {

    @NotBlank(message = "Họ tên bắt buộc")
    @Size(max = 100, message = "Họ tên tối đa 100 ký tự")
    private String hoTen;

    @NotBlank(message = "Địa chỉ bắt buộc")
    @Size(max = 10, message = "Địa chỉ tối đa 10 ký tự")
    private String diaChi;

    @Pattern(regexp = "^\\d{9,15}$", message = "Số điện thoại chỉ được nhập số (9-15 chữ số)")
    private String soDienThoai;
}
