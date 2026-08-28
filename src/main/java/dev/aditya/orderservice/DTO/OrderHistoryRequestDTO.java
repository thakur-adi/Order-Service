package dev.aditya.orderservice.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderHistoryRequestDTO {
    private int pageNumber;
    private int pageSize;
}
