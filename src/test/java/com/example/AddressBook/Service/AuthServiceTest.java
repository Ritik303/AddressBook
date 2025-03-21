package com.example.AddressBook.Service;

import com.example.AddressBook.Repository.UserRepository;
import com.example.AddressBook.Security.JwtUtil;
import com.example.AddressBook.dto.LoginRequestDTO;
import com.example.AddressBook.dto.LoginResponseDTO;
import com.example.AddressBook.dto.UserDTO;
import com.example.AddressBook.entity.User;
import com.example.AddressBook.services.AuthService;
import com.example.AddressBook.services.EmailService;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Optional;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtils;

    @Mock
    private EmailService emailService;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @InjectMocks
    private AuthService authService;

    private User testUser;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        testUser = new User();
        testUser.setUsername("testUser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    public void testRegisterUser_Success() {
        UserDTO userDTO = new UserDTO("testUser", "test@example.com", "password123");
        when(userRepository.existsByEmail(userDTO.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(userDTO.getPassword())).thenReturn("encodedPassword");

        String result = authService.registerUser(userDTO);

        assertEquals("User registered successfully!", result);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    public void testRegisterUser_EmailAlreadyExists() {
        UserDTO userDTO = new UserDTO("testUser", "test@example.com", "password123");
        when(userRepository.existsByEmail(userDTO.getEmail())).thenReturn(true);

        String result = authService.registerUser(userDTO);

        assertEquals("Error: Email already registered!", result);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void testLoginUser_Success() throws MessagingException {
        LoginRequestDTO loginRequestDTO = new LoginRequestDTO("test@example.com", "password123");
        when(userRepository.findByEmail(loginRequestDTO.getEmail())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(loginRequestDTO.getPassword(), testUser.getPassword())).thenReturn(true);
        when(jwtUtils.generateToken(testUser.getEmail())).thenReturn("mocked-jwt-token");
        doNothing().when(emailService).sendEmail(anyString(), anyString(), anyString());

        LoginResponseDTO response = authService.loginUser(loginRequestDTO);
        assertEquals("Login successful! Check your email for the token.", response.getMessage());
        assertEquals("mocked-jwt-token", response.getToken());
    }

    @Test
    public void testLoginUser_InvalidPassword() {
        LoginRequestDTO loginRequestDTO = new LoginRequestDTO("test@example.com", "wrongPassword");
        when(userRepository.findByEmail(loginRequestDTO.getEmail())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(loginRequestDTO.getPassword(), testUser.getPassword())).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> authService.loginUser(loginRequestDTO));
    }

    @Test
    public void testLoginUser_UserNotFound() {
        LoginRequestDTO loginRequestDTO = new LoginRequestDTO("notfound@example.com", "password123");
        when(userRepository.findByEmail(loginRequestDTO.getEmail())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> authService.loginUser(loginRequestDTO));
    }

    @Test
    public void testLoginWithToken_Success() {
        String token = "mocked-jwt-token";
        when(jwtUtils.getEmailFromToken(token)).thenReturn("test@example.com");
        when(redisTemplate.opsForValue().get("JWT_TOKEN:test@example.com")).thenReturn(token);
        when(jwtUtils.isTokenValid("test@example.com", token)).thenReturn(true);

        boolean result = authService.loginWithToken(token);
        assertTrue(result);
    }

    @Test
    public void testLoginWithToken_InvalidToken() {
        String token = "invalid-token";
        when(jwtUtils.getEmailFromToken(token)).thenReturn("test@example.com");
        when(redisTemplate.opsForValue().get("JWT_TOKEN:test@example.com")).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> authService.loginWithToken(token));
    }
}