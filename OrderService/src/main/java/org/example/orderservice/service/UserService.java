package org.example.orderservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.orderservice.dto.request.UpdateUserRequestDto;
import org.example.orderservice.dto.response.UserResponseDto;
import org.example.orderservice.entity.User;
import org.example.orderservice.exception.ResourceNotFoundException;
import org.example.orderservice.mapper.UserMapperDto;
import org.example.orderservice.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapperDto userMapperDto;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public Page<UserResponseDto> findAllUser(Pageable pageable) {
        log.info("Fetching users with page: {}", pageable);

        Page<User> users = userRepository.findAll(pageable);

        log.info("Found {} users", users.getTotalElements());
        return users.map(userMapperDto::toDto);
    }

    @Transactional(readOnly = true)
    public UserResponseDto findUserById(Long id) {
        log.info("Fetching user with id: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        return userMapperDto.toDto(user);
    }

    @Transactional
    public UserResponseDto updateUser(Long id, Long currentUserId, UpdateUserRequestDto userDto) {
        log.info("Updating user: {} by user: {}", id, currentUserId);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        if (!user.getId().equals(currentUserId)) {
            log.warn("User {} attempted to update user {} profile", currentUserId, id);
            throw new AccessDeniedException("You can only update your own profile");
        }

        userMapperDto.updateEntity(userDto, user);

        if (userDto.getPassword() != null && !userDto.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        }

        User updated = userRepository.save(user);
        log.info("User {} updated successfully", id);

        return userMapperDto.toDto(updated);
    }

    @Transactional
    public void deleteUser(Long id, Long currentUserId){
        log.info("Deleting user: {} by user: {}", id, currentUserId);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        if (!user.getId().equals(currentUserId)) {
            log.warn("User {} attempted to delete user {}", currentUserId, id);
            throw new AccessDeniedException("You can only delete your own account");
        }

        userRepository.deleteById(id);
        log.info("User {} deleted successfully", id);
    }
}
