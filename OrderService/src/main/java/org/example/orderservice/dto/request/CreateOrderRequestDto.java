package org.example.orderservice.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class CreateOrderRequestDto {

    private List<OrderItemDto> items;
    private String deliveryAddress;
}
