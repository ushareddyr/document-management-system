package com.docmanagement.service;

import com.docmanagement.dto.request.LoginRequest;
import com.docmanagement.dto.request.RegisterRequest;
import com.docmanagement.dto.response.JwtResponse;
import com.docmanagement.model.Role;
import com.docmanagement.model.User;
import com.docmanagement.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthService authService;

    private LoginRequest loginRequest;
    private RegisterRequest registerRequest;
    private User testUser;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        loginRequest = LoginRequest.builder()
                .username("testuser")
                .password("password")
                .build();

        Set<Role> roles = new HashSet<>();
        roles.add(Role.VIEWER);

        registerRequest = RegisterRequest.builder()
                .username("newuser")
                .email("new@example.com")
                .password("password")
                .firstName("New")
                .lastName("User")
                .roles(roles)
                .build();

        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("encodedPassword")
                .firstName("Test")
                .lastName("User")
                .roles(roles)
                .build();

        authentication = mock(Authentication.class);
    }

    @Test
    void authenticateUser_ValidCredentials_ReturnsJwtResponse() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(tokenProvider.generateToken(authentication)).thenReturn("jwt-token");

        JwtResponse response = authService.authenticateUser(loginRequest);

        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals(testUser.getId(), response.getId());
        assertEquals(testUser.getUsername(), response.getUsername());
        assertEquals(testUser.getEmail(), response.getEmail());
        assertEquals(testUser.getRoles(), response.getRoles());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(tokenProvider).generateToken(authentication);
    }

    @Test
    void registerUser_ValidRequest_ReturnsUser() {
        when(userService.registerUser(registerRequest)).thenReturn(testUser);

        User result = authService.registerUser(registerRequest);

        assertNotNull(result);
        assertEquals(testUser, result);
        verify(userService).registerUser(registerRequest);
    }
}
