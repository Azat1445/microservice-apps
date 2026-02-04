package org.example.notificationservice.mapper;

import org.example.notificationservice.dto.OrderEventDTO;
import org.example.notificationservice.dto.OrderResponseDTO;
import org.example.notificationservice.entity.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface OrderMapper {

    // Kafka Dto -> Entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Order toEntity(OrderEventDTO dto);

    // Entity -> Dto
    @Mapping(target = "quantity", ignore = true)
    OrderResponseDTO toResponseDTO(Order order);

    // List Entity -> Dto
    List<OrderResponseDTO> toResponseListDTO(List<Order> orders);
}
