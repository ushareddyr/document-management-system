package com.docmanagement.controller;

import com.docmanagement.dto.response.ApiResponse;
import com.docmanagement.dto.response.UserSummaryResponse;
import com.docmanagement.model.Role;
import com.docmanagement.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin", description = "Admin API")
@SecurityRequirement(name = "Bearer Authentication")
public class AdminController {

    private final UserService userService;

    @GetMapping("/users")
    @Operation(summary = "Get all users")
    public ResponseEntity<List<UserSummaryResponse>> getAllUsers() {
        List<UserSummaryResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/{id}")
    @Operation(summary = "Get user by ID")
    public ResponseEntity<UserSummaryResponse> getUserById(@PathVariable Long id) {
        UserSummaryResponse user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/users/{id}/roles")
    @Operation(summary = "Update user roles")
    public ResponseEntity<ApiResponse> updateUserRoles(
            @PathVariable Long id,
            @RequestBody Set<Role> roles) {
        userService.updateUserRoles(id, roles);
        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message("User roles updated successfully")
                .build());
    }
}
