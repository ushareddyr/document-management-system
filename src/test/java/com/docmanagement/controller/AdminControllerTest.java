package com.docmanagement.controller;

import com.docmanagement.dto.response.ApiResponse;
import com.docmanagement.dto.response.UserSummaryResponse;
import com.docmanagement.model.Role;
import com.docmanagement.security.JwtTokenProvider;
import com.docmanagement.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AdminController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private UserService userService;

    private UserSummaryResponse user1;
    private UserSummaryResponse user2;
    private List<UserSummaryResponse> userList;
    private Set<Role> roles;

    @BeforeEach
    void setUp() {
        user1 = UserSummaryResponse.builder()
                .id(1L)
                .username("admin")
                .firstName("Admin")
                .lastName("User")
                .build();

        user2 = UserSummaryResponse.builder()
                .id(2L)
                .username("editor")
                .firstName("Editor")
                .lastName("User")
                .build();

        userList = Arrays.asList(user1, user2);

        roles = new HashSet<>();
        roles.add(Role.EDITOR);
        roles.add(Role.VIEWER);
    }

    // Success Cases

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_WithAdminRole_ReturnsAllUsers() throws Exception {
        when(userService.getAllUsers()).thenReturn(userList);

        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].username").value("admin"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].username").value("editor"));

        verify(userService, times(1)).getAllUsers();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUserById_WithAdminRole_ReturnsUser() throws Exception {
        when(userService.getUserById(1L)).thenReturn(user1);

        mockMvc.perform(get("/admin/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("admin"))
                .andExpect(jsonPath("$.firstName").value("Admin"))
                .andExpect(jsonPath("$.lastName").value("User"));

        verify(userService, times(1)).getUserById(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateUserRoles_WithAdminRole_UpdatesRolesSuccessfully() throws Exception {
        doNothing().when(userService).updateUserRoles(eq(1L), any(Set.class));

        mockMvc.perform(put("/admin/users/1/roles")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roles)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User roles updated successfully"));

        verify(userService, times(1)).updateUserRoles(eq(1L), any(Set.class));
    }

    // Error Cases

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUserById_UserNotFound_ReturnsNotFound() throws Exception {
        when(userService.getUserById(99L)).thenThrow(new RuntimeException("User not found with id: 99"));

        mockMvc.perform(get("/admin/users/99"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("User not found with id: 99"));

        verify(userService, times(1)).getUserById(99L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateUserRoles_UserNotFound_ReturnsNotFound() throws Exception {
        doThrow(new RuntimeException("User not found with id: 99"))
                .when(userService).updateUserRoles(eq(99L), any(Set.class));

        mockMvc.perform(put("/admin/users/99/roles")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roles)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("User not found with id: 99"));

        verify(userService, times(1)).updateUserRoles(eq(99L), any(Set.class));
    }

    // Edge Cases

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_NoUsers_ReturnsEmptyList() throws Exception {
        when(userService.getAllUsers()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(userService, times(1)).getAllUsers();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateUserRoles_EmptyRoleSet_UpdatesWithEmptyRoles() throws Exception {
        Set<Role> emptyRoles = new HashSet<>();
        doNothing().when(userService).updateUserRoles(eq(1L), eq(emptyRoles));

        mockMvc.perform(put("/admin/users/1/roles")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emptyRoles)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(userService, times(1)).updateUserRoles(eq(1L), eq(emptyRoles));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateUserRoles_InvalidRole_ReturnsBadRequest() throws Exception {
        String invalidRoleJson = "[\"INVALID_ROLE\", \"EDITOR\"]";

        mockMvc.perform(put("/admin/users/1/roles")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRoleJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUserById_InvalidIdFormat_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/admin/users/invalid-id"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateUserRoles_SelfUpdate_PreventsSelfRoleChange() throws Exception {
        // Simulate trying to update the admin's own roles
        doThrow(new RuntimeException("Administrators cannot modify their own roles"))
                .when(userService).updateUserRoles(eq(1L), any(Set.class));

        mockMvc.perform(put("/admin/users/1/roles")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roles)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Administrators cannot modify their own roles"));

        verify(userService, times(1)).updateUserRoles(eq(1L), any(Set.class));
    }
}