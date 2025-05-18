package com.docmanagement.service;

import com.docmanagement.dto.request.RegisterRequest;
import com.docmanagement.dto.response.UserSummaryResponse;
import com.docmanagement.model.Role;
import com.docmanagement.model.User;
import com.docmanagement.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("encodedPassword")
                .firstName("Test")
                .lastName("User")
                .roles(Collections.singleton(Role.VIEWER))
                .createdAt(LocalDateTime.now())
                .build();

        registerRequest = RegisterRequest.builder()
                .username("newuser")
                .email("new@example.com")
                .password("password")
                .firstName("New")
                .lastName("User")
                .roles(Collections.singleton(Role.VIEWER))
                .build();
    }

    @Test
    void loadUserByUsername_UserExists_ReturnsUser() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        User result = (User) userService.loadUserByUsername("testuser");

        assertEquals(testUser, result);
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    void loadUserByUsername_UserNotFound_ThrowsException() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> userService.loadUserByUsername("nonexistent"));
        verify(userRepository).findByUsername("nonexistent");
    }

    @Test
    void registerUser_ValidRequest_ReturnsUser() {
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            savedUser.setId(2L);
            return savedUser;
        });

        User result = userService.registerUser(registerRequest);

        assertNotNull(result);
        assertEquals("newuser", result.getUsername());
        assertEquals("new@example.com", result.getEmail());
        assertEquals("encodedPassword", result.getPassword());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerUser_UsernameExists_ThrowsException() {
        when(userRepository.existsByUsername("newuser")).thenReturn(true);

        assertThrows(RuntimeException.class, () -> userService.registerUser(registerRequest));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerUser_EmailExists_ThrowsException() {
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(true);

        assertThrows(RuntimeException.class, () -> userService.registerUser(registerRequest));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getAllUsers_ReturnsUserList() {
        List<User> users = Arrays.asList(testUser);
        when(userRepository.findAll()).thenReturn(users);

        List<UserSummaryResponse> result = userService.getAllUsers();

        assertEquals(1, result.size());
        assertEquals(testUser.getId(), result.get(0).getId());
        assertEquals(testUser.getUsername(), result.get(0).getUsername());
        verify(userRepository).findAll();
    }

    @Test
    void getUserById_UserExists_ReturnsUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        UserSummaryResponse result = userService.getUserById(1L);

        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
        assertEquals(testUser.getUsername(), result.getUsername());
        verify(userRepository).findById(1L);
    }

    @Test
    void getUserById_UserNotFound_ThrowsException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userService.getUserById(99L));
        verify(userRepository).findById(99L);
    }

    @Test
    void updateUserRoles_UserExists_UpdatesRoles() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        Set<Role> newRoles = new HashSet<>(Arrays.asList(Role.ADMIN, Role.EDITOR));
        userService.updateUserRoles(1L, newRoles);

        assertEquals(newRoles, testUser.getRoles());
        verify(userRepository).save(testUser);
    }

    @Test
    void updateUserRoles_UserNotFound_ThrowsException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        Set<Role> newRoles = new HashSet<>(Arrays.asList(Role.ADMIN, Role.EDITOR));
        assertThrows(RuntimeException.class, () -> userService.updateUserRoles(99L, newRoles));
        verify(userRepository, never()).save(any(User.class));
    }
}
