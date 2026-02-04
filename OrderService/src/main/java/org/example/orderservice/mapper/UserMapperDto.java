package org.example.orderservice.mapper;

import org.example.orderservice.dto.request.RegisterRequestDto;
import org.example.orderservice.dto.request.UpdateUserRequestDto;
import org.example.orderservice.dto.response.UserResponseDto;
import org.example.orderservice.entity.User;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface UserMapperDto {

    //Entity -> DTO
    UserResponseDto toDto(User user);

    //DTO -> Entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    User toEntity(RegisterRequestDto dto);

    //Update entity of dto
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateEntity(UpdateUserRequestDto dto, @MappingTarget User user);
}
