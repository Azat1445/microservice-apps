package org.example.orderservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.orderservice.dto.request.UpdateUserRequestDto;
import org.example.orderservice.dto.response.UserResponseDto;
import org.example.orderservice.security.CustomUserDetails;
import org.example.orderservice.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<Page<UserResponseDto>> findAllUser(@PageableDefault(size = 20, sort = "createdAt",
                                                                            direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("Fetching users with page: {}", pageable);

        Page<UserResponseDto> users = userService.findAllUser(pageable);
        log.info("Found {} users", users.getTotalElements());

        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> findUserById(@PathVariable Long id) {
        log.info("Getting user by id {}", id);

        UserResponseDto user = userService.findUserById(id);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getCurrentUser(@AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("Getting current user info: {}", userDetails.getId());

        UserResponseDto user = userService.findUserById(userDetails.getId());
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDto> updateUser(@PathVariable Long id,
                                                             @AuthenticationPrincipal CustomUserDetails userDetails,
                                                             @Valid @RequestBody UpdateUserRequestDto updateDto) {
        Long currentUserId = userDetails.getId();
        log.info("Updating user: {} by user: {}, dto: {}", id, currentUserId, updateDto);

        UserResponseDto updated = userService.updateUser(id, currentUserId, updateDto);
        log.info("User {} updated successfully", id);

        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id,
                                           @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long currentUserId = userDetails.getId();
        log.info("Deleting user: {} by user: {}", id, currentUserId);

        userService.deleteUser(id, currentUserId);
        log.info("User {} deleted successfully", id);

        return ResponseEntity.noContent().build();
    }
}
