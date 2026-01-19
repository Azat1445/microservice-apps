package org.example.inventoryservice.mapper;

import org.example.inventoryservice.dto.ProductsResponseDto;
import org.example.inventoryservice.dto.ProductsUpdateDto;
import org.example.inventoryservice.entity.Products;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface ProductsMapperDto {

    // Entity -> Dto
    ProductsResponseDto toDto(Products products);

    // Dto -> Entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Products toEntity(ProductsResponseDto productsResponseDto);

    // Update DTO -> Entity
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(ProductsUpdateDto updateDto, @MappingTarget Products products);

    // Обогащение Response DTO вычисляемыми полями
    @AfterMapping
    default void enrichResponseDto(@MappingTarget ProductsResponseDto dto, Products entity) {
        dto.setFinalPrice(entity.getFinalPrice());
        dto.setAvailable(entity.isAvailable());
    }
}
