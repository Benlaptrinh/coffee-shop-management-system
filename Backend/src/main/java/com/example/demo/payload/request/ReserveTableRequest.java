package com.example.demo.payload.request;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Request payload for reserving a table.
 */
@Getter
@Setter
public class ReserveTableRequest {
    @Size(max = 100, message = "Tên khách tối đa 100 ký tự")
    private String tenKhach;

    @Pattern(regexp = "^\\d{9,15}$", message = "Số điện thoại chỉ được nhập số (9-15 chữ số)")
    private String sdt;

    @NotNull(message = "Ngày giờ đặt bắt buộc")
    private LocalDateTime ngayGio;
}
