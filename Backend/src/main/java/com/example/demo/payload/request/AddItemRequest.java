package com.example.demo.payload.request;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddItemRequest {
    private Long thucDonId;
    private Integer soLuong;
}
