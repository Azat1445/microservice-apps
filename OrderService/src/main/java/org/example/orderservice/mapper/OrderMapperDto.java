package org.example.orderservice.mapper;

import org.example.orderservice.dto.request.CreateOrderRequestDto;
import org.example.orderservice.dto.response.OrderResponseDto;
import org.example.orderservice.entity.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface OrderMapperDto {

    //Entity -> Dto
    OrderResponseDto toResponseDto(Order order);

    //Dto -> Entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "totalPrice", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Order toEntity(CreateOrderRequestDto dto);
}
